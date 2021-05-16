package smagellan.test;

import org.springframework.integration.file.tail.FileTailingMessageProducerSupport;
import org.springframework.integration.file.tail.OSDelegatingFileTailingMessageProducer;

import java.io.File;

public class FileTailTest {
    public static void main(String[] args) {
        FileTailingMessageProducerSupport producer = new OSDelegatingFileTailingMessageProducer();
        producer.setFile(new File("/tmp/aa"));
        producer.setOutputChannel((msg, timeout) -> {
            System.err.println(msg);
            return true;
        });
        producer.start();
    }
}
