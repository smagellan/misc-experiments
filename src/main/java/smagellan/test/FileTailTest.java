package smagellan.test;

import com.sun.nio.file.SensitivityWatchEventModifier;
import org.springframework.context.SmartLifecycle;
import org.springframework.integration.channel.FluxMessageChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.endpoint.AbstractEndpoint;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.file.tail.FileTailingMessageProducerSupport;
import org.springframework.integration.file.tail.OSDelegatingFileTailingMessageProducer;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.util.concurrent.Queues;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.Duration;
import java.util.List;

public class FileTailTest {


    private static void regularDirectoryWatch() throws IOException, InterruptedException {
        WatchService svc = FileSystems.getDefault().newWatchService();
        Path path = Paths.get("/tmp/mon-dir");
        path.register(svc,
                new WatchEvent.Kind[] { StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY },
                SensitivityWatchEventModifier.MEDIUM);
        WatchKey key;
        while ((key = svc.take()) != null) {
            System.err.println("key: " + key);
            for (WatchEvent<?> event : key.pollEvents()) {
                System.err.println("evt: " + event + "; kind: " + event.kind() + "; context: " + event.context() + "/" + event.context().getClass() + "; count: " + event.count());
            }
            key.reset();
        }
        svc.close();
    }

    private static void springIntegrationDirectoryWatch(MessageChannel channel) {
        FileReadingMessageSource msgSource = new FileReadingMessageSource();
        msgSource.setUseWatchService(true);
        msgSource.setDirectory(new File("/tmp/mon-dir"));
        msgSource.setLoggingEnabled(true);
        msgSource.setWatchEvents(FileReadingMessageSource.WatchEventType.MODIFY, FileReadingMessageSource.WatchEventType.CREATE);
        IntegrationFlow flow = IntegrationFlow
                .from(msgSource, config -> config.poller(Pollers.fixedDelay(1000)))
                .channel(channel)
                .get();
        ((SmartLifecycle)flow).start();
    }

    private static void tailTest() throws InterruptedException {
        FluxMessageChannel channel = new FluxMessageChannel();
        Flux<List<Message<?>>> flux = Flux.from(channel)
                .windowTimeout(10, Duration.ofSeconds(10))
                .flatMap(Flux::collectList, Queues.SMALL_BUFFER_SIZE)
                .filter(lst -> !lst.isEmpty())
                .doOnComplete(() -> System.err.println("flux completed"))
                .doOnCancel(() -> System.err.println("flux cancelled"))
                .doOnDiscard(Object.class, (t) -> System.err.println("discarded: " + t))
                .doOnTerminate(() -> System.err.println("flux terminated"));

        Disposable disposable = flux
                .subscribe(lst -> System.err.println("list of size " + lst.size()));

        AbstractEndpoint ep1 = subscribe("/tmp/aa1", channel);
        AbstractEndpoint ep2 = subscribe("/tmp/aa2", channel);
        springIntegrationDirectoryWatch(channel);
        System.err.println("disposable: " + disposable);
        Thread.sleep(50_000);
        System.err.println("stopping flux");
        ep2.stop();
        ep1.stop();
        disposable.dispose();
    }

    static FileTailingMessageProducerSupport subscribe(String filename, MessageChannel channel) {
        FileTailingMessageProducerSupport producer = new OSDelegatingFileTailingMessageProducer();
        producer.setFile(new File(filename));
        producer.setOutputChannel(channel);
        producer.start();
        return producer;
    }
}
