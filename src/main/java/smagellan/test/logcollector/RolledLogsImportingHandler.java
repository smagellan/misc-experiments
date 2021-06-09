package smagellan.test.logcollector;

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

public class RolledLogsImportingHandler extends AbstractMessageHandler {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(RolledLogsImportingHandler.class);

    private final RolledLogsTracker logsTracker;
    private final Queue<File> files2Import;
    private final Queue<File> errorFilesQueue;

    public RolledLogsImportingHandler(RolledLogsTracker logsTracker) {
        this.logsTracker = logsTracker;
        this.files2Import = new LinkedBlockingQueue<>();
        this.errorFilesQueue = new LinkedBlockingQueue<>();
    }

    @Override
    protected void handleMessageInternal(Message<?> message) {
        logger.info("handleMessageInternal: {}", message);
        files2Import.add((File) message.getPayload());
    }

    @Scheduled(fixedRate = 60_000)
    private void processFiles() {
        processQueue(files2Import, 3);
        processQueue(errorFilesQueue, 1);
    }

    private void processQueue(Queue<File> queue, int filesPerBatch) {
        List<File> batch = accumulateBatch(queue, filesPerBatch);
        try {
            ingestFiles(batch);
            logsTracker.markImported(batch);
        } catch (IOException ex) {
            errorFilesQueue.addAll(batch);
        }
    }

    @NotNull
    private List<File> accumulateBatch(Queue<File> queue, int filesPerBatch) {
        List<File> batch = new ArrayList<>(filesPerBatch);
        File tmp;
        for (int i = 0; i < filesPerBatch && (tmp = queue.poll()) != null; ++i) {
            batch.add(tmp);
        }
        return batch;
    }

    private void ingestFiles(List<File> batch) throws IOException {

    }
}
