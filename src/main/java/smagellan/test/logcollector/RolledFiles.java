package smagellan.test.logcollector;

import org.apache.commons.lang3.tuple.Pair;

import java.nio.file.Path;
import java.util.Collection;

class RolledFiles {
    private final Collection<Pair<LogFileInfo, Path>> rolledFiles;

    public RolledFiles(Collection<Pair<LogFileInfo, Path>> rolledFiles) {
        this.rolledFiles = rolledFiles;
    }

    public Collection<Pair<LogFileInfo, Path>> rolledFiles() {
        return rolledFiles;
    }
}
