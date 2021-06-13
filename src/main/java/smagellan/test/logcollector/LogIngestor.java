package smagellan.test.logcollector;

import com.google.common.collect.ListMultimap;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class LogIngestor {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(LogIngestor.class);
    private final LogCollectorConfig config;
    private final RolledLogsTracker logsTracker;

    public LogIngestor(LogCollectorConfig config, RolledLogsTracker logsTracker) {
        this.config = config;
        this.logsTracker = logsTracker;
    }

    public void ingestLogLines(ListMultimap<Path, Map<String, String>> lines) throws IOException {
        logger.info("ingesting log lines: {}", lines);
        for (Map.Entry<Path, Collection<Map<String, String>>> entry : lines.asMap().entrySet()) {
            LogFileInfo fileInfo = config.logInfoByLivePath(entry.getKey());
            logger.info("config file info: {}", fileInfo);
        }
    }

    public void ingestFiles(Collection<Pair<LogFileInfo, File>> batch) throws IOException {
        logger.info("ingesting rolled log-files: {}", batch);
        for (Pair<LogFileInfo, File> filePair : batch) {
            File file = filePair.getRight();
            if (file.exists()) {
                Collection<File> nonImportedFiles = logsTracker.retainNonImportedFiles(Collections.singleton(file));
                if (!nonImportedFiles.isEmpty()) {
                    InputStream tmp = new FileInputStream(file);
                    if (file.getName().endsWith(".gz")) {
                        tmp = new GZIPInputStream(tmp);
                    }
                    try (InputStream is = tmp) {
                        LogLineTransformer transformer = new LogLineTransformer(filePair.getKey());
                    }
                } else {
                    logger.warn("file {} already imported, skipping", file);
                }
            } else {
                logger.info("file {} does not exist", filePair);
            }
        }
    }
}
