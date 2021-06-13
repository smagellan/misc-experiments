package smagellan.test.logcollector;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
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

    private final LogCollectorConfig config;
    private final RolledLogsTracker logsTracker;
    private final String rolledLogsImportingChannelName;
    private final LogIngestor logIngestor;

    public LiveLogsImportingHandler(LogCollectorConfig config, String rolledLogsImportingChannelName, RolledLogsTracker logsTracker, LogIngestor logIngestor) {
        this.config = config;
        this.rolledLogsImportingChannelName = rolledLogsImportingChannelName;
        this.logsTracker = logsTracker;
        this.logIngestor = logIngestor;
    }

    @Override
    protected void handleMessageInternal(Message<?> message) {
        MessageLogger.logMessage(message);
        GroupedLogEvents groupedEvents = (GroupedLogEvents) message.getPayload();
        Collection<Pair<LogFileInfo, File>> paths = groupedEvents
                .rolledFiles()
                .stream()
                .map(f -> ImmutablePair.of(f.getLeft(), f.getRight().toFile()))
                .collect(Collectors.toList());
        try {
            logIngestor.ingestLogLines(groupedEvents.tailedLines());
            logsTracker.markImported(paths);
        } catch (Exception ex) {
            sendErroneousRolledLogFiles(paths);
            logger.error("can't ingest live lines, will attempt to import rolled files {} later", groupedEvents.rolledFiles(), ex);
            //throw new MessageDeliveryException(message, ex);
        }
    }

    private void sendErroneousRolledLogFiles(Collection<Pair<LogFileInfo, File>> rolledLogs) {
        MessageChannel chan = getChannelResolver().resolveDestination(rolledLogsImportingChannelName);
        Collection<Pair<LogFileInfo, Path>> rolledPaths = rolledLogs
                .stream()
                .map(e -> ImmutablePair.of(e.getLeft(), e.getRight().toPath()))
                .collect(Collectors.toList());
        Message<?> message = getMessageBuilderFactory()
                .withPayload(new RolledFiles(rolledPaths))
                .build();
        chan.send(message);
    }
}
