package smagellan.test.logcollector;

import java.io.File;
import java.util.Map;

public class LogCollectorConfig {
    private final Map<File, File> liveFileToRolledFile;

    public LogCollectorConfig(Map<File, File> liveFileToRolledFile) {
        this.liveFileToRolledFile = liveFileToRolledFile;
    }

    public Map<File, File> liveFileToRolledFile() {
        return liveFileToRolledFile;
    }
}
