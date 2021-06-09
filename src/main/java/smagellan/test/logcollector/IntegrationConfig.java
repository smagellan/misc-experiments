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
import java.time.Duration;
import java.util.*;

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
        Map<File, File> liveFileToRolledFile = Map.of(
                new File("/tmp/aa-test3"), new File("/tmp/mon-dir1"),
                new File("/tmp/aa-test4"), new File("/tmp/mon-dir2")
        );
        return new LogCollectorConfig(liveFileToRolledFile);
    }

    @Bean
    @Qualifier(Constants.TAILED_AND_ROLLED_FILES_CHANNEL)
    public FluxMessageChannel tailedFilesChannel() {
        return new FluxMessageChannel();
    }

    @Autowired
    @Bean
    public LiveLogsImportingHandler liveLogsImportingHandler(RolledLogsTracker logsTracker) {
        return new LiveLogsImportingHandler(Constants.ROLLED_LOGS_IMPORT_CHANNEL, logsTracker);
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
    public InitialNonImportedRolledLogsMessageSource initialRolledNonImportedLogs(LogCollectorConfig collectorConfig, RolledLogsTracker logsTracker) {
        FilenameFilter filter = (dir, name) -> true;
        Map<File, Collection<File>> liveFileToRolledFile = buildExistingRolledNonImportedFilesMap(logsTracker, collectorConfig.liveFileToRolledFile().values(), filter);
        InitialNonImportedRolledLogsMessageSource ret = new InitialNonImportedRolledLogsMessageSource(liveFileToRolledFile);
        ret.setOutputChannelName(Constants.ROLLED_LOGS_IMPORT_CHANNEL);
        return ret;
    }

    @NotNull
    private Map<File, Collection<File>> buildExistingRolledNonImportedFilesMap(RolledLogsTracker logsTracker, Collection<File> rolledFilesDirectories, FilenameFilter filter) {
        Map<File, Collection<File>> liveFileToRolledFile = new HashMap<>();
        for (File  file : rolledFilesDirectories) {
            File[] files = file.listFiles(filter);
            if (files != null) {
                Set<File> dirFiles = logsTracker.retainNonImportedFiles(Arrays.asList(files));
                liveFileToRolledFile.put(file, Collections.unmodifiableCollection(dirFiles));
            }
        }
        liveFileToRolledFile = Collections.unmodifiableMap(liveFileToRolledFile);
        return liveFileToRolledFile;
    }

    @Bean
    public RolledLogsTracker rolledLogsTracker() {
        return new RolledLogsTracker();
    }

    @Qualifier(Constants.ROLLED_LOGS_IMPORT_CHANNEL)
    @Bean
    public QueueChannel rolledLogsImportChannel() {
        return new QueueChannel();
    }

    @Autowired
    @Bean
    public RolledLogsImportingHandler rolledLogsImportingHandler(RolledLogsTracker logsTracker) {
        return new RolledLogsImportingHandler(logsTracker);
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
        DirectoryWatcher watcher = new DirectoryWatcher(collectorConfig.liveFileToRolledFile().values(), (dir, name) -> true);
        watcher.setOutputChannelName(Constants.TAILED_AND_ROLLED_FILES_CHANNEL);
        return watcher;
    }
}
