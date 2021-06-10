package smagellan.test.logcollector;

import com.google.common.collect.ListMultimap;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class LogIngestor {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(LogIngestor.class);

    public void ingestLogLines(ListMultimap<Path, String> lines) throws IOException {
        logger.info("ingesting log lines: {}", lines);
    }

    public void ingestFiles(List<File> batch) throws IOException {
        logger.info("ingesting rolled log-files: {}", batch);
    }
}
