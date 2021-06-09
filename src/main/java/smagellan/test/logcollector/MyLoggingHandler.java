package smagellan.test.logcollector;

import org.slf4j.LoggerFactory;
import org.springframework.integration.handler.AbstractMessageHandler;
import org.springframework.messaging.Message;

class MyLoggingHandler extends AbstractMessageHandler {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(MyLoggingHandler.class);

    @Override
    protected void handleMessageInternal(Message<?> message) {
        MessageLogger.logMessage(message);
    }
}
