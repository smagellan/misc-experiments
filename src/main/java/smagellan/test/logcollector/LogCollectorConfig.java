package smagellan.test.logcollector;

import java.util.Collection;

public class LogCollectorConfig {
    private final Collection<LogFileInfo> liveFileToRolledFile;

    public LogCollectorConfig(Collection<LogFileInfo> liveFileToRolledFile) {
        this.liveFileToRolledFile = liveFileToRolledFile;
    }

    public Collection<LogFileInfo> liveFileToRolledFile() {
        return liveFileToRolledFile;
    }
}
