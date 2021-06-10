package smagellan.test.logcollector;

import java.io.File;
import java.util.Objects;

public class LogFileInfo {
    private final File liveLogFile;
    private final File rolledLogsDir;

    public LogFileInfo(File liveLogFile, File rolledLogsDir) {
        this.liveLogFile = liveLogFile;
        this.rolledLogsDir = rolledLogsDir;
    }

    public File liveLogFile() {
        return liveLogFile;
    }

    public File rolledLogsDir() {
        return rolledLogsDir;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LogFileInfo that = (LogFileInfo) o;
        return Objects.equals(liveLogFile, that.liveLogFile) && Objects.equals(rolledLogsDir, that.rolledLogsDir);
    }

    @Override
    public int hashCode() {
        return Objects.hash(liveLogFile, rolledLogsDir);
    }
}
