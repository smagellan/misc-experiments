package smagellan.test.embeddedtomcat.tomcat;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

import java.io.File;
import java.io.IOException;

public abstract class TomcatUtil {
    private static final Log log = LogFactory.getLog(TomcatUtil.class);
    public static File createTempDir(String prefix, int port) throws IOException {
        File tempDir = File.createTempFile(prefix + ".", "." + port);
        if (!tempDir.delete()) {
            String msg = "failed to delete dir: " + tempDir;
            log.error(msg);
            throw new IOException(msg);
        }
        if (!tempDir.mkdir()) {
            String msg = "failed to create dir: " + tempDir;
            log.error(msg);
            throw new IOException(msg);
        }
        tempDir.deleteOnExit();
        return tempDir;
    }
}