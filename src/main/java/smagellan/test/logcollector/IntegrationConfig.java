package smagellan.test.logcollector;

import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.integration.channel.FluxMessageChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.FilenameFilter;
import java.time.Duration;
import java.util.*;

@Configuration
@Import(TailedFilesConfigLoader3.class)
@EnableIntegration
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
    @Qualifier(Constants.TAILED_AND_ROLLED_FILES_CHANNEL)
    public FluxMessageChannel tailedFilesChannel() {
        return new FluxMessageChannel();
    }

    @Bean
    public IntegrationFlow liveLogsContinuousFlow() {
        logger.info("registering liveLogsContinuousFlow");
        return IntegrationFlows.from(Constants.TAILED_AND_ROLLED_FILES_CHANNEL)
                .fluxTransform(flux -> flux.windowTimeout(10, Duration.ofSeconds(10))
                        .flatMap(Flux::collectList)
                        .filter(lst -> !lst.isEmpty())
                )
                .transform(new GroupedLogEventsTransformer())
                //TODO: decouple with channel (QueueChannel?)
                .handle(new MyLoggingHandler())
                .get();
    }

    //@Bean
    public InitialNonTrackedRolledLogsMessageSource rolledLogs(RolledLogsTracker logsTracker) {
        Collection<String> liveFiles = new ArrayList<>();
        FilenameFilter filter = (dir, name) -> true;
        Map<File, Collection<File>> liveFileToRolledFile = buildExistingRolledFilesMap(liveFiles, filter);
        return new InitialNonTrackedRolledLogsMessageSource(liveFileToRolledFile);
    }

    @NotNull
    private Map<File, Collection<File>> buildExistingRolledFilesMap(Collection<String> liveFiles, FilenameFilter filter) {
        Map<File, Collection<File>> liveFileToRolledFile = new HashMap<>();
        for (String fileName : liveFiles) {
            File file = new File(fileName);
            File[] files = file.listFiles(filter);
            if (files != null) {
                liveFileToRolledFile.put(file, Collections.unmodifiableCollection(Arrays.asList(files)));
            }
        }
        liveFileToRolledFile = Collections.unmodifiableMap(liveFileToRolledFile);
        return liveFileToRolledFile;
    }

    @Bean
    public RolledLogsTracker rolledLogsTracker() {
        return new RolledLogsTracker();
    }

    //@Bean
    public IntegrationFlow oldRolledLogsSingleRunFlow() {
        return null;
    }


    //clickhouse pool with 2-3 connections. Idle connections are closed after period of inactivity
    //(directory_watchers + file_tailers) -> channel1 -> window(1000_000, 10 min) -> channel2 -> clickhouse_ingestion -> channel3 -> track_rolled_files
    //(initial directory listing) -> channel4 -> window(3) -> clickhouse_ingestion -> channel5 -> track_rolled_files
    @Bean
    public DirectoryWatcher directoryWatcher() {
        DirectoryWatcher watcher = new DirectoryWatcher(Collections.singletonList("/tmp/mon-dir"), (dir, name) -> true);
        watcher.setOutputChannelName(Constants.TAILED_AND_ROLLED_FILES_CHANNEL);
        return watcher;
    }
}
