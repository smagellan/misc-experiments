package smagellan.test;

import net.openhft.chronicle.map.ChronicleMap;

import java.io.File;
import java.io.IOException;

public class ChronicleMapTest {
    static {
        System.setProperty("chronicle.analytics.disable", "true");
    }
    /*
    needs these options to run under JDK 17 (https://chronicle.software/chronicle-support-java-17/):
    --add-exports=java.base/jdk.internal.ref=ALL-UNNAMED --add-exports=java.base/sun.nio.ch=ALL-UNNAMED
    --add-exports=jdk.unsupported/sun.misc=ALL-UNNAMED --add-exports=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED
    --add-opens=jdk.compiler/com.sun.tools.javac=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED
    --add-opens=java.base/java.lang.reflect=ALL-UNNAMED --add-opens=java.base/java.io=ALL-UNNAMED
    --add-opens=java.base/java.util=ALL-UNNAMED --add-exports java.base/jdk.internal.util=ALL-UNNAMED
    --illegal-access=permit
    */
    public static void main(String[] args) throws IOException {
        try (ChronicleMap<String, Long> map = ChronicleMap.of(String.class, Long.class)
                .averageKey("key1")
                .constantValueSizeBySample(1L)
                .entries(1000)
                .createPersistedTo(new File("/tmp/chronicle-map.state"))) {
            map.put("key1", 42L);
            map.put("key2", 420L);
            Long val = map.get("key2");
            System.err.println(val);
        }
    }
}
