package smagellan.test.logcollector;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;
import org.springframework.integration.handler.AbstractMessageHandler;
import org.springframework.messaging.Message;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

public class RolledLogsImportingHandler extends AbstractMessageHandler {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(RolledLogsImportingHandler.class);

    private final LogIngestor logIngestor;
    private final RolledLogsTracker logsTracker;
    private final Queue<Pair<LogFileInfo, File>> files2Import;
    private final Queue<Pair<LogFileInfo, File>> errorFilesQueue;


    public RolledLogsImportingHandler(RolledLogsTracker logsTracker, LogIngestor logIngestor) {
        this.logsTracker = logsTracker;
        this.logIngestor = logIngestor;
        this.files2Import = new LinkedBlockingQueue<>();
        this.errorFilesQueue = new LinkedBlockingQueue<>();
    }

    @Override
    protected void handleMessageInternal(Message<?> message) {
        logger.info("handleMessageInternal: {}", message);
        List<Pair<LogFileInfo, File>> rolledFiles = ((RolledFiles)message.getPayload())
                .rolledFiles()
                .stream()
                .map(e -> ImmutablePair.of(e.getLeft(), e.getRight().toFile()))
                .collect(Collectors.toList());
        files2Import.addAll(rolledFiles);
    }

    @Scheduled(initialDelay = 10_000, fixedRate = 60_000)
    private void processFiles() {
        logger.info("processing internal queue");
        processQueue(files2Import, 3);
        processQueue(errorFilesQueue, 1);
    }

    private void processQueue(Queue<Pair<LogFileInfo, File>> queue, int filesPerBatch) {
        List<Pair<LogFileInfo, File>> batch = accumulateBatch(queue, filesPerBatch);
        if (!batch.isEmpty()) {
            try {
                logIngestor.ingestFiles(batch);
                logsTracker.markImported(batch);
            } catch (IOException ex) {
                errorFilesQueue.addAll(batch);
            }
        }
    }

    @NotNull
    private List<Pair<LogFileInfo, File>> accumulateBatch(Queue<Pair<LogFileInfo, File>> queue, int filesPerBatch) {
        List<Pair<LogFileInfo, File>> batch = new ArrayList<>(filesPerBatch);
        Pair<LogFileInfo, File> tmp;
        for (int i = 0; i < filesPerBatch && (tmp = queue.poll()) != null; ++i) {
            batch.add(tmp);
        }
        return batch;
    }
}
