package smagellan.test.logcollector;

import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.integration.channel.FluxMessageChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.scheduling.annotation.EnableScheduling;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Configuration
@Import(TailedFilesConfigLoader3.class)
@EnableIntegration
@EnableScheduling
class IntegrationConfig {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(IntegrationConfig.class);

    @Bean
    public static PropertySourcesPlaceholderConfigurer props() {
        PropertySourcesPlaceholderConfigurer ret = new PropertySourcesPlaceholderConfigurer();
        Properties props = new Properties();
        props.setProperty("key", "value");
        MutablePropertySources sources = new MutablePropertySources();
        sources.addLast(new PropertiesPropertySource("name", props));
        ret.setPropertySources(sources);
        return ret;
    }

    @Bean
    public LogCollectorConfig collectorConfig() {
        Collection<LogFileInfo> liveFileToRolledFile = List.of(
                new LogFileInfo(new File("/tmp/aa-test3"), new File("/tmp/mon-dir1"), Map.of("app-name", "app1")),
                new LogFileInfo(new File("/tmp/aa-test4"), new File("/tmp/mon-dir2"), Map.of("app-name", "app2"))
        );
        return new LogCollectorConfig(liveFileToRolledFile, new File("/tmp/tracked-logs.properties"));
    }

    @Bean
    @Qualifier(Constants.TAILED_AND_ROLLED_FILES_CHANNEL)
    public FluxMessageChannel tailedFilesChannel() {
        return new FluxMessageChannel();
    }

    @Autowired
    @Bean
    public LogIngestor logIngestor(LogCollectorConfig config, RolledLogsTracker logsTracker) {
        return new LogIngestor(config, logsTracker);
    }

    @Autowired
    @Bean
    public LiveLogsImportingHandler liveLogsImportingHandler(LogCollectorConfig config, RolledLogsTracker logsTracker, LogIngestor logIngestor) {
        return new LiveLogsImportingHandler(config, Constants.ROLLED_LOGS_IMPORT_CHANNEL, logsTracker, logIngestor);
    }

    @Autowired
    @Bean
    public IntegrationFlow liveLogsContinuousFlow(LiveLogsImportingHandler liveLogsImportingHandler) {
        return IntegrationFlows.from(Constants.TAILED_AND_ROLLED_FILES_CHANNEL)
                .fluxTransform(flux -> flux.windowTimeout(10, Duration.ofSeconds(10))
                        .flatMap(Flux::collectList)
                        .filter(lst -> !lst.isEmpty())
                )
                .transform(new GroupedLogEventsTransformer())
                //TODO: decouple with channel (QueueChannel?)
                //.channel(MessageChannels.queue())
                .handle(liveLogsImportingHandler)
                .get();
    }

    @Autowired
    @Bean
    public InitialNonImportedRolledLogsMessageSource initialRolledNonImportedLogs(LogCollectorConfig collectorConfig, RolledLogsTracker logsTracker) throws IOException {
        FilenameFilter filter = (dir, name) -> true;
        Map<LogFileInfo, Collection<File>> liveFileToRolledFile = buildExistingRolledNonImportedFilesMap(logsTracker, collectorConfig.logFilesInfo(), filter);
        InitialNonImportedRolledLogsMessageSource ret = new InitialNonImportedRolledLogsMessageSource(collectorConfig, liveFileToRolledFile);
        ret.setOutputChannelName(Constants.ROLLED_LOGS_IMPORT_CHANNEL);
        return ret;
    }

    @NotNull
    private Map<LogFileInfo, Collection<File>> buildExistingRolledNonImportedFilesMap(RolledLogsTracker logsTracker,
                                                                                      Collection<LogFileInfo> rolledFilesDirectories,
                                                                                      FilenameFilter filter) throws IOException {
        Map<LogFileInfo, Collection<File>> liveFileToRolledFile = new HashMap<>();
        for (LogFileInfo fileInfo : rolledFilesDirectories) {
            File[] files = fileInfo.rolledLogsDir().listFiles(filter);
            if (files != null) {
                Set<File> dirFiles = logsTracker.retainNonImportedFiles(Arrays.asList(files));
                liveFileToRolledFile.put(fileInfo, Collections.unmodifiableCollection(dirFiles));
            }
        }
        liveFileToRolledFile = Collections.unmodifiableMap(liveFileToRolledFile);
        return liveFileToRolledFile;
    }

    @Autowired
    @Bean
    public RolledLogsTracker rolledLogsTracker(LogCollectorConfig config) throws IOException {
        return new RolledLogsTracker(config);
    }

    @Qualifier(Constants.ROLLED_LOGS_IMPORT_CHANNEL)
    @Bean
    public QueueChannel rolledLogsImportChannel() {
        return new QueueChannel();
    }

    @Autowired
    @Bean
    public RolledLogsImportingHandler rolledLogsImportingHandler(RolledLogsTracker logsTracker, LogIngestor logIngestor) {
        return new RolledLogsImportingHandler(logsTracker, logIngestor);
    }

    @Autowired
    @Bean
    public IntegrationFlow rolledLogsFlow(RolledLogsImportingHandler importingHandler) {
        return IntegrationFlows
                .from(Constants.ROLLED_LOGS_IMPORT_CHANNEL)
                .handle(importingHandler, e -> e.poller(Pollers.fixedDelay(Duration.ofSeconds(10))))
                .get();
    }


    //clickhouse pool with 2-3 connections. Idle connections are closed after period of inactivity
    //(directory_watchers + file_tailers) -> channel1 -> window(1000_000, 10 min) -> channel2 -> clickhouse_ingestion -> channel3 -> track_rolled_files
    //(initial directory listing) -> channel4 -> window(3) -> clickhouse_ingestion -> channel5 -> track_rolled_files
    @Autowired
    @Bean
    public DirectoryWatcher directoryWatcher(LogCollectorConfig collectorConfig) {
        Collection<File> rolledLogsDirs = collectorConfig.logFilesInfo()
                .stream()
                .map(LogFileInfo::rolledLogsDir)
                .collect(Collectors.toList());
        DirectoryWatcher watcher = new DirectoryWatcher(rolledLogsDirs, collectorConfig, (dir, name) -> true);
        watcher.setOutputChannelName(Constants.TAILED_AND_ROLLED_FILES_CHANNEL);
        return watcher;
    }
}
