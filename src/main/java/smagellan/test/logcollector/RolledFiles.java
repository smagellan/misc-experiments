package smagellan.test.logcollector;

import java.nio.file.Path;
import java.util.Collection;

class RolledFiles {
    private final Collection<Path> rolledFiles;

    public RolledFiles(Collection<Path> rolledFiles) {
        this.rolledFiles = rolledFiles;
    }

    public Collection<Path> rolledFiles() {
        return rolledFiles;
    }
}
