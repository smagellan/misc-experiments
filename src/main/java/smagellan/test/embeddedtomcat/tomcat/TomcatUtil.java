package smagellan.test.embeddedtomcat.tomcat;

import java.io.File;
import java.io.IOException;

public abstract class TomcatUtil {

    public static File createTempDir(String prefix, int port) throws IOException {
        File tempDir = File.createTempFile(prefix + ".", "." + port);
        tempDir.delete();
        tempDir.mkdir();
        tempDir.deleteOnExit();
        return tempDir;
    }
}