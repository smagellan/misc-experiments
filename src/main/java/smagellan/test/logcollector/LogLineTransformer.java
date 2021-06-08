package smagellan.test.logcollector;

import org.springframework.integration.transformer.GenericTransformer;
import org.springframework.messaging.Message;

public class LogLineTransformer implements GenericTransformer<Message<String>, Message<?>> {
    @Override
    public Message<?> transform(Message<String> source) {
        return source;
    }
}
