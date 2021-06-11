package smagellan.test.logcollector;

import org.slf4j.LoggerFactory;
import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RolledLogsTracker {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(RolledLogsTracker.class);

    public void markImported(Collection<File> fileList) {
        logger.info("markImported: {}", fileList);
    }

    public Set<File> retainNonImportedFiles(List<File> fileList) {
        return new HashSet<>(fileList);
    }
}
