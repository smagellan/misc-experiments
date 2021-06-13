package smagellan.test.logcollector;

import com.google.common.collect.Maps;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;

public class LogCollectorConfig {
    private final Map<File, LogFileInfo> liveLogToLogInfo;
    private final Map<Path, LogFileInfo> liveLogPathToLogInfo;
    private final Collection<LogFileInfo> liveFileToRolledFile;

    public LogCollectorConfig(Collection<LogFileInfo> liveFileToRolledFile) {
        this.liveFileToRolledFile = liveFileToRolledFile;
        this.liveLogToLogInfo = Maps.uniqueIndex(liveFileToRolledFile, LogFileInfo::liveLogFile);
        this.liveLogPathToLogInfo = Maps.uniqueIndex(liveFileToRolledFile, info -> info.liveLogFile().toPath());
    }

    public Collection<LogFileInfo> logFilesInfo() {
        return liveFileToRolledFile;
    }

    public LogFileInfo logInfoByLiveFile(File file) {
        return liveLogToLogInfo.get(file);
    }

    public LogFileInfo logInfoByLivePath(Path path) {
        return liveLogPathToLogInfo.get(path);
    }
}
