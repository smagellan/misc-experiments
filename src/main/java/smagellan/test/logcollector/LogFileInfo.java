package smagellan.test.logcollector;

import java.io.File;
import java.util.Map;
import java.util.Objects;

public class LogFileInfo {
    private final File liveLogFile;
    private final File rolledLogsDir;
    private final Map<String, String> vars;

    public LogFileInfo(File liveLogFile, File rolledLogsDir, Map<String, String> vars) {
        this.liveLogFile = liveLogFile;
        this.rolledLogsDir = rolledLogsDir;
        this.vars = Map.copyOf(vars);
    }

    public File liveLogFile() {
        return liveLogFile;
    }

    public File rolledLogsDir() {
        return rolledLogsDir;
    }

    public Map<String, String> vars() {
        return vars;
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

    @Override
    public String toString() {
        return "LogFileInfo{" +
                "liveLogFile=" + liveLogFile +
                ", rolledLogsDir=" + rolledLogsDir +
                ", vars=" + vars +
                '}';
    }
}
