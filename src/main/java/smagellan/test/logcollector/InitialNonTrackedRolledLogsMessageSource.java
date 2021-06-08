package smagellan.test.logcollector;

import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.integration.file.FileHeaders;
import org.springframework.messaging.Message;

import java.io.File;
import java.util.*;

public class InitialNonTrackedRolledLogsMessageSource extends MessageProducerSupport {
    private TaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
    private Map<File, Collection<File>> liveFileToRolledFile;

    public InitialNonTrackedRolledLogsMessageSource(Map<File, Collection<File>> initialRolledFiles) {
        this.liveFileToRolledFile = initialRolledFiles;
    }

    @Override
    protected void doStart() {
        taskExecutor.execute(this::runExec);
    }

    public Map<File, Collection<File>> getExistingRolledFiles() {
        return liveFileToRolledFile;
    }

    private void runExec() {
        for (Map.Entry<File, Collection<File>> fileEntry : liveFileToRolledFile.entrySet()) {
            Message<?> message = getMessageBuilderFactory()
                    .withPayload(fileEntry.getValue())
                    .setHeader(FileHeaders.FILENAME, fileEntry.getKey().getName())
                    .setHeader(FileHeaders.ORIGINAL_FILE, fileEntry.getKey())
                    .build();
            sendMessage(message);
        }
    }
}
