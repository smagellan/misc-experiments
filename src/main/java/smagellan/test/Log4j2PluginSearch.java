package smagellan.test;

import com.google.common.base.Stopwatch;
import org.apache.logging.log4j.core.config.plugins.util.PluginManager;
import org.apache.logging.log4j.core.config.plugins.util.PluginType;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Log4j2PluginSearch {
    public static final int N = 1;
    public static void main(String[] args) {
        doTest(Collections.singletonList(null));
        doTest(Collections.singletonList("com.*"));
    }

    private static void doTest(List<String> packages) {
        Stopwatch st = Stopwatch.createUnstarted();
        Map<String, PluginType<?>> s = new HashMap<>();
        st.start();
        for (int i  = 0; i < N; ++i) {
            PluginManager manager = new PluginManager("Converter");
            manager.collectPlugins(packages);
            s.putAll(manager.getPlugins());
        }
        System.err.println("total: " + (st.stop().elapsed(TimeUnit.MILLISECONDS) / (double)N));
        System.err.println("plugins: " + s);
    }
}
