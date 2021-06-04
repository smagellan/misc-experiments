package smagellan.test;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.MultimapBuilder;
import com.sun.nio.file.SensitivityWatchEventModifier;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.integration.channel.FluxMessageChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.integration.file.FileHeaders;
import org.springframework.integration.file.tail.OSDelegatingFileTailingMessageProducer;
import org.springframework.integration.handler.AbstractMessageHandler;
import org.springframework.integration.transformer.GenericTransformer;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.GenericMessage;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class SpringIntegrationFileTailTest {
    public static void main(String[] args) throws Throwable {
        AnnotationConfigApplicationContext cfg = new AnnotationConfigApplicationContext();
        cfg.register(IntegrationConfig.class);
        cfg.registerShutdownHook();
        cfg.refresh();
        System.err.println("waiting");
        Thread.sleep(30_000);
        System.err.println("closing spring context");
        cfg.close();
    }
}


@Configuration
@Import(TailedFilesConfigLoader.class)
@EnableIntegration
class IntegrationConfig {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(IntegrationConfig.class);
    public static final String CHANNEL_NAME = "channel";


    @Bean
    @Qualifier(TailedFilesConfigLoader.TAILED_AND_ROLLED_FILES_CHANNEL)
    public FluxMessageChannel tailedFilesChannel() {
        return new FluxMessageChannel();
    }

    @Bean
    @Autowired
    public IntegrationFlow liveLogsFlow() {
        return IntegrationFlows.from(TailedFilesConfigLoader.TAILED_AND_ROLLED_FILES_CHANNEL)
                .fluxTransform(flux -> flux.windowTimeout(10, Duration.ofSeconds(10))
                        .flatMap(Flux::collectList)
                        .filter(lst -> !lst.isEmpty())
                )
                .transform(new GroupedLogEventsTransformer())
                .handle(new MyLoggingHandler())
                .get();
    }

    //clickhouse pool with 2-3 connections. Idle connections are closed after period of inactivity
    //(directory_watchers + file_tailers) -> channel1 -> window(1000_000, 10 min) -> channel2 -> clickhouse_ingestion -> channel3 -> track_rolled_files
    //(initial directory listing) -> channel4 -> window(3) -> clickhouse_ingestion -> channel5 -> track_rolled_files
    @Bean
    public DirectoryWatcher directoryWatcher() {
        DirectoryWatcher watcher = new DirectoryWatcher(Collections.singletonList("/tmp/mon-dir"), (dir, name) -> {logger.info("filefilter: {}/{}", dir, name); return true;});
        watcher.setOutputChannelName(TailedFilesConfigLoader.TAILED_AND_ROLLED_FILES_CHANNEL);
        return watcher;
    }

    @Bean
    public IntegrationFlow channelSink() {
        return IntegrationFlows.from(CHANNEL_NAME)
                .handle((msg) -> System.err.println("new message: " + msg))
                .get();
    }
}

class DirectoryWatcher extends MessageProducerSupport implements Runnable {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(DirectoryWatcher.class);

    private final Collection<String> dirs2Watch;
    private final FilenameFilter filter;

    private Thread watcherThread;
    private WatchService svc;
    private List<WatchKey> dirsWatchKeys;

    DirectoryWatcher(Collection<String> dirs2Watch, FilenameFilter filter) {
        this.dirs2Watch = dirs2Watch;
        this.filter = filter;
    }

    @Override
    public void run() {
        try {
            WatchKey key;
            while ((key = svc.take()) != null) {
                logger.info("key: {}", key);
                MessageChannel channel = getOutputChannel();
                List<WatchEvent<?>> events = key.pollEvents();
                if (channel == null) {
                    logger.info("output channel is null, events are discarded");
                } else {
                    List<Path> contexts = events
                            .stream()
                            .map(WatchEvent::context)
                            .map(obj -> (Path) obj)
                            .filter(path -> filter.accept(path.toAbsolutePath().getParent().toFile(), path.getFileName().toString()))
                            .collect(Collectors.toList());
                    logger.info("publishing files events for {}", contexts);
                    channel.send(new RolledFileMessage(contexts));
                }
                key.reset();
            }
        } catch (InterruptedException ex) {
            logger.info("directory watcher thread stopping(interrupted)");
        }
    }

    @Override
    protected void doStart() {
        logger.info("starting directory watcher at {}", dirs2Watch);
        try {
            initWatcher();
            watcherThread = new Thread(this);
            watcherThread.start();
        } catch (IOException ex) {
            try {
                teardownWatcher();
            } catch (IOException ex1) {
                RuntimeException tmp = new RuntimeException(ex);
                tmp.addSuppressed(ex1);
                throw tmp;
            }
            throw new RuntimeException(ex);
        }
    }

    private void initWatcher() throws IOException {
        svc = FileSystems.getDefault().newWatchService();
        dirsWatchKeys = new ArrayList<>(dirs2Watch.size());
        for (String dir : dirs2Watch) {
            Path path = Paths.get(dir);
            //TODO: check which event log4j2 generates: StandardWatchEventKinds.ENTRY_MODIFY or ENTRY_CREATE (or both)
            WatchKey dirWatchKey = path.register(svc,
                    new WatchEvent.Kind[]{ StandardWatchEventKinds.ENTRY_CREATE },
                    SensitivityWatchEventModifier.MEDIUM);
            dirsWatchKeys.add(dirWatchKey);
        }
    }

    @Override
    protected void doStop() {
        try {
            watcherThread.interrupt();
            logger.info("waiting for directory watcher thread");
            watcherThread.join();
            logger.info("waiting for directory watcher thread done");
            teardownWatcher();
        } catch (IOException | InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void teardownWatcher() throws IOException {
        if (dirsWatchKeys != null) {
            for (WatchKey key : dirsWatchKeys) {
                key.cancel();
            }
            dirsWatchKeys = null;
        }
        if (svc != null) {
            svc.close();
            svc = null;
        }
    }
}

class MyLoggingHandler extends AbstractMessageHandler {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(MyLoggingHandler.class);

    @Override
    protected void handleMessageInternal(Message<?> message) {
        Object payload = message.getPayload();
        logger.info("payload: {}", payload);
        if (payload instanceof GroupedLogEvents) {
            GroupedLogEvents evts = (GroupedLogEvents) payload;
            int idx = 0;
            logger.info("GroupedLogEvents tailed files:");
            for (Map.Entry<Path, Collection<String>> path : evts.tailedLines().asMap().entrySet()) {
                ++idx;
                logger.info("el{}: {}({} lines)", idx, path.getKey(), path.getValue().size());
            }
            logger.info("GroupedLogEvents rolled files:");
            idx = 0;
            for (Path path : evts.rolledFiles()) {
                ++idx;
                logger.info("el{}: {}", idx, path);
            }
        } else if (payload instanceof Collection) {
            logger.info("payload list:");
            int idx = 0;
            for (Message<?> obj : (Collection<Message<?>>)payload) {
                ++idx;
                logger.info("el{}: {}", idx, obj);
            }
        } else {
            if (payload instanceof Flux) {
                Flux<Message<?>> flux = (Flux<Message<?>>) payload;
                logger.info("subscribing to {}", flux);
                flux.collectList().subscribe((msg -> logger.info("subscribe result: {}", msg)));
            }
        }
    }
}

@Configuration
class TailedFilesConfigLoader implements BeanDefinitionRegistryPostProcessor {
    public static final String TAILED_AND_ROLLED_FILES_CHANNEL = "tailedAndRolledFilesChannel";

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(TailedFilesConfigLoader.class);

    private final List<String> filesList;
    private final List<String> dirsList;

    public TailedFilesConfigLoader() {
        this.filesList = Arrays.asList("/tmp/aa-test3", "/tmp/aa-test4");
        this.dirsList = Arrays.asList("/tmp/mon-dir1");
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        for (String filename : filesList) {
            registerFileTailMessageProducer(filename, TAILED_AND_ROLLED_FILES_CHANNEL, registry);
        }
    }

    private void registerFileTailMessageProducer(String filename, String outputChannelName, BeanDefinitionRegistry registry) throws BeansException {
        String beanName = filename.replace('/', '_').replace('\\', '_') + "_message_producer";
        BeanDefinitionBuilder builder = BeanDefinitionBuilder
                .genericBeanDefinition(OSDelegatingFileTailingMessageProducer.class, () -> fileTailer(new File(filename), outputChannelName))
                .setLazyInit(false);
        logger.info("registering bean {}", beanName);
        registry.registerBeanDefinition(beanName, builder.getBeanDefinition());
    }

    private OSDelegatingFileTailingMessageProducer fileTailer(File file, String outputChannelName) {
        OSDelegatingFileTailingMessageProducer ret = new OSDelegatingFileTailingMessageProducer();
        ret.setFile(file);
        ret.setOutputChannelName(outputChannelName);
        return ret;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    }
}

class RolledFileMessage extends GenericMessage<List<Path>> {
    public RolledFileMessage(List<Path> payload) {
        super(payload);
    }

    public RolledFileMessage(List<Path> payload, Map<String, Object> headers) {
        super(payload, headers);
    }

    public RolledFileMessage(List<Path> payload, MessageHeaders headers) {
        super(payload, headers);
    }
}

class GroupedLogEventsTransformer implements GenericTransformer<Message<List<Message<?>>>, Message<GroupedLogEvents>> {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(GroupedLogEventsTransformer.class);

    @Override
    public Message<GroupedLogEvents> transform(Message<List<Message<?>>> source) {
        ListMultimap<Path, String> tailedLines = MultimapBuilder
                .linkedHashKeys()
                .arrayListValues()
                .build();
        List<Path> rolledFiles = new ArrayList<>();
        for (Message<?> msg : source.getPayload()) {
            Object payload = msg.getPayload();
            if (msg instanceof RolledFileMessage) {
                RolledFileMessage rollMsg = (RolledFileMessage) msg;
                rolledFiles.addAll(rollMsg.getPayload());
            } else if (payload instanceof String) {
                File file = msg.getHeaders().get(FileHeaders.ORIGINAL_FILE, File.class);
                if (file != null) {
                    tailedLines.put(file.toPath(), (String) payload);
                } else {
                    logger.error("did not find header '{}' in message for payload {}", FileHeaders.ORIGINAL_FILE, payload);
                }
            }
        }

        return new GenericMessage<>(new GroupedLogEvents(tailedLines, rolledFiles), source.getHeaders());
    }
}

class GroupedLogEvents {
    private final ListMultimap<Path, String> tailedLines;
    private final List<Path> rolledFiles;

    public GroupedLogEvents(ListMultimap<Path, String> tailedLines, List<Path> rolledFiles) {
        this.tailedLines = tailedLines;
        this.rolledFiles = rolledFiles;
    }

    public ListMultimap<Path, String> tailedLines() {
        return tailedLines;
    }

    public List<Path> rolledFiles() {
        return rolledFiles;
    }

    @Override
    public String toString() {
        return "GroupedLogEvents{" +
                "tailedLines=" + tailedLines +
                ", rolledFiles=" + rolledFiles +
                '}';
    }
}