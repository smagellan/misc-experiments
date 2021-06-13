package smagellan.test.logcollector;

import org.springframework.integration.transformer.GenericTransformer;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;

import java.util.Map;

public class LogLineTransformer implements GenericTransformer<Message<String>, Message<?>> {
    private final LogFileInfo fileInfo;
    public LogLineTransformer(LogFileInfo fileInfo) {
        this.fileInfo = fileInfo;
    }

    @Override
    public Message<?> transform(Message<String> source) {
        return new GenericMessage<>(Map.of("field", "value", "line", source.getPayload()), source.getHeaders());
    }
}
