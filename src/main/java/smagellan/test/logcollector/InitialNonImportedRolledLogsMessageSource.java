package smagellan.test.logcollector;

import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.integration.file.FileHeaders;
import org.springframework.messaging.Message;

import java.io.File;
import java.util.*;

public class InitialNonImportedRolledLogsMessageSource extends MessageProducerSupport {
    private TaskExecutor taskExecutor = new SimpleAsyncTaskExecutor(InitialNonImportedRolledLogsMessageSource.class.getSimpleName() + "-");
    private Map<File, Collection<File>> initialNonRolledFiles;

    public InitialNonImportedRolledLogsMessageSource(Map<File, Collection<File>> initialNonRolledFiles) {
        this.initialNonRolledFiles = initialNonRolledFiles;
    }

    @Override
    protected void doStart() {
        taskExecutor.execute(this::runExec);
    }

    public Map<File, Collection<File>> getExistingNonRolledFiles() {
        return initialNonRolledFiles;
    }

    private void runExec() {
        for (Map.Entry<File, Collection<File>> fileEntry : initialNonRolledFiles.entrySet()) {
            if (fileEntry.getValue() != null && !fileEntry.getValue().isEmpty()) {
                Message<?> message = getMessageBuilderFactory()
                        .withPayload(fileEntry.getValue())
                        .setHeader(FileHeaders.FILENAME, fileEntry.getKey().getName())
                        .setHeader(FileHeaders.ORIGINAL_FILE, fileEntry.getKey())
                        .build();
                sendMessage(message);
            }
        }
    }
}
