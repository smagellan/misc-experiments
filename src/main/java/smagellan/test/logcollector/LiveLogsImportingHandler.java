package smagellan.test.logcollector;

import org.springframework.integration.handler.AbstractMessageHandler;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

public class LiveLogsImportingHandler extends AbstractMessageHandler {
    private final RolledLogsTracker logsTracker;
    private final String rolledLogsImportingChannelName;

    public LiveLogsImportingHandler(String rolledLogsImportingChannelName, RolledLogsTracker logsTracker) {
        this.rolledLogsImportingChannelName = rolledLogsImportingChannelName;
        this.logsTracker = logsTracker;
    }

    @Override
    protected void handleMessageInternal(Message<?> message) {
        MessageChannel chan = getChannelResolver().resolveDestination(rolledLogsImportingChannelName);
        MessageLogger.logMessage(message);
    }
}
