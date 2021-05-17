package smagellan.test;

import org.springframework.integration.channel.FluxMessageChannel;
import org.springframework.integration.file.tail.FileTailingMessageProducerSupport;
import org.springframework.integration.file.tail.OSDelegatingFileTailingMessageProducer;
import org.springframework.messaging.MessageChannel;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.io.File;
import java.time.Duration;

public class FileTailTest {
    public static void main(String[] args) {
        FluxMessageChannel channel = new FluxMessageChannel();
        Disposable disposable = Flux.from(channel)
                .windowTimeout(10, Duration.ofSeconds(15))
                .flatMap(Flux::collectList)
                .filter(lst -> !lst.isEmpty())
                .subscribe(lst -> System.err.println("list of size " + lst.size()));

        subscribe("/tmp/aa1", channel);
        subscribe("/tmp/aa2", channel);
        System.err.println("disposable: " + disposable);
    }

    private static void subscribe(String filename, MessageChannel channel) {
        FileTailingMessageProducerSupport producer = new OSDelegatingFileTailingMessageProducer();
        producer.setFile(new File(filename));
        producer.setOutputChannel(channel);
        producer.start();
    }
}
