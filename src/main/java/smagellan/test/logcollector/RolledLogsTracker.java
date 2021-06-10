package smagellan.test.logcollector;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RolledLogsTracker {
    public void markImported(Collection<File> fileList) {
    }

    public Set<File> retainNonImportedFiles(List<File> fileList) {
        return new HashSet<>(fileList);
    }
}
