package smagellan.test.logcollector;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.integration.endpoint.MessageProducerSupport;
import org.springframework.integration.file.FileHeaders;
import org.springframework.messaging.Message;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class InitialNonImportedRolledLogsMessageSource extends MessageProducerSupport {
    private final TaskExecutor taskExecutor = new SimpleAsyncTaskExecutor(InitialNonImportedRolledLogsMessageSource.class.getSimpleName() + "-");
    private final Map<LogFileInfo, Collection<File>> initialNonRolledFiles;
    private final Map<File, Collection<File>> liveFileToRolledNonImportedFiles;
    private final LogCollectorConfig config;

    public InitialNonImportedRolledLogsMessageSource(LogCollectorConfig config, Map<LogFileInfo, Collection<File>> initialNonRolledFiles) {
        Objects.requireNonNull(initialNonRolledFiles);
        this.config = config;
        this.initialNonRolledFiles = initialNonRolledFiles;
        this.liveFileToRolledNonImportedFiles = initialNonRolledFiles.entrySet()
                .stream()
                .collect(Collectors.toMap(e -> e.getKey().liveLogFile(), Map.Entry::getValue));
    }

    @Override
    protected void doStart() {
        taskExecutor.execute(this::runExec);
    }

    public Map<File, Collection<File>> getExistingLiveLogsToNonRolledFiles() {
        return liveFileToRolledNonImportedFiles;
    }

    private void runExec() {
        for (Map.Entry<LogFileInfo, Collection<File>> fileEntry : initialNonRolledFiles.entrySet()) {
            Collection<File> rolledLogFiles = fileEntry.getValue();
            if (rolledLogFiles != null && !rolledLogFiles.isEmpty()) {
                Collection<Pair<LogFileInfo, Path>> rolledPaths = rolledLogFiles
                        .stream()
                        .map(File::toPath)
                        .map(p -> ImmutablePair.of(fileEntry.getKey(), p))
                        .collect(Collectors.toList());
                Message<?> message = getMessageBuilderFactory()
                        .withPayload(new RolledFiles(rolledPaths))
                        .setHeader(FileHeaders.FILENAME, fileEntry.getKey().liveLogFile())
                        .setHeader(FileHeaders.ORIGINAL_FILE, fileEntry.getKey())
                        .build();
                sendMessage(message);
            }
        }
    }
}
