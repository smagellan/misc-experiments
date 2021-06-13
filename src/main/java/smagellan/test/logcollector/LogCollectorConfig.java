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
    private final File trackedRolledFiles;

    public LogCollectorConfig(Collection<LogFileInfo> liveFileToRolledFile, File trackedRolledFiles) {
        this.liveFileToRolledFile = liveFileToRolledFile;
        this.liveLogToLogInfo = Maps.uniqueIndex(liveFileToRolledFile, LogFileInfo::liveLogFile);
        this.liveLogPathToLogInfo = Maps.uniqueIndex(liveFileToRolledFile, info -> info.liveLogFile().toPath());
        this.trackedRolledFiles = trackedRolledFiles;
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

    public File trackedRolledFilesLocation() {
        return trackedRolledFiles;
    }
}
