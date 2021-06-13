package smagellan.test.logcollector;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class RolledLogsTracker {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(RolledLogsTracker.class);

    private final LogCollectorConfig config;
    private final Properties trackedFiles;

    public RolledLogsTracker(LogCollectorConfig config) throws IOException {
       this.trackedFiles = new Properties();
       this.config = config;
       if (config.trackedRolledFilesLocation().exists()) {
           try (InputStream is = new FileInputStream(config.trackedRolledFilesLocation())) {
               trackedFiles.load(is);
           }
       }
    }

    public void markImported(Collection<Pair<LogFileInfo, File>> fileList) throws IOException {
        logger.info("markImported: {}", fileList);
        boolean updated = false;
        for (Pair<LogFileInfo, File> f : fileList) {
            File file = f.getRight();
            String md5Hex = DigestUtils.md5Hex(Files.readAllBytes(file.toPath()));
            String trackedHex = trackedFiles.getProperty(file.getAbsolutePath());
            if (!md5Hex.equals(trackedHex)) {
                trackedFiles.setProperty(file.getAbsolutePath(), md5Hex);
                updated = true;
            }
        }
        if (updated) {
            logger.info("updating {}", config.trackedRolledFilesLocation());
            try (OutputStream os = new FileOutputStream(config.trackedRolledFilesLocation())) {
                trackedFiles.store(os, "");
            }
        }
    }

    public Set<File> retainNonImportedFiles(Collection<File> fileList) throws IOException {
        Set<File> ret = new HashSet<>();
        for (File f : fileList) {
            String trackedInfo = trackedFiles.getProperty(f.getAbsolutePath());
            if (trackedInfo == null) {
                ret.add(f);
            } else {
                if (f.exists()) {
                    String md5Hex = DigestUtils.md5Hex(Files.readAllBytes(f.toPath()));
                    if (!trackedInfo.equals(md5Hex)) {
                        ret.add(f);
                    }
                }
            }
        }
        return ret;
    }
}
