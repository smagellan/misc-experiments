package smagellan.test.logcollector;

import org.slf4j.LoggerFactory;
import org.springframework.integration.handler.AbstractMessageHandler;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.stream.Collectors;

public class LiveLogsImportingHandler extends AbstractMessageHandler {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(LiveLogsImportingHandler.class);

    private final RolledLogsTracker logsTracker;
    private final String rolledLogsImportingChannelName;
    private final LogIngestor logIngestor;

    public LiveLogsImportingHandler(String rolledLogsImportingChannelName, RolledLogsTracker logsTracker, LogIngestor logIngestor) {
        this.rolledLogsImportingChannelName = rolledLogsImportingChannelName;
        this.logsTracker = logsTracker;
        this.logIngestor = logIngestor;
    }

    @Override
    protected void handleMessageInternal(Message<?> message) {
        MessageLogger.logMessage(message);
        GroupedLogEvents groupedEvents = (GroupedLogEvents) message.getPayload();
        try {
            Collection<File> paths = groupedEvents
                    .rolledFiles()
                    .stream()
                    .map(p -> p.toAbsolutePath().toFile())
                    .collect(Collectors.toList());
            logIngestor.ingestLogLines(groupedEvents.tailedLines());
            logsTracker.markImported(paths);
        } catch (Exception ex) {
            sendErroneousRolledLogFiles(groupedEvents.rolledFiles());
            logger.error("can't ingest live lines, will attempt to import rolled files {} later", groupedEvents.rolledFiles(), ex);
            //throw new MessageDeliveryException(message, ex);
        }
    }

    private void sendErroneousRolledLogFiles(Collection<Path> rolledLogs) {
        MessageChannel chan = getChannelResolver().resolveDestination(rolledLogsImportingChannelName);
        chan.send(new RolledFileMessage(rolledLogs));
    }
}
