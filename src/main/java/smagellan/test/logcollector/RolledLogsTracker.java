package smagellan.test.logcollector;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class RolledLogsTracker {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(RolledLogsTracker.class);

    public void markImported(Collection<Pair<LogFileInfo, File>> fileList) {
        logger.info("markImported: {}", fileList);
    }

    public Set<File> retainNonImportedFiles(Collection<File> fileList) {
        return new HashSet<>(fileList);
    }
}
