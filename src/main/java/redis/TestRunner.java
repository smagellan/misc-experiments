package redis;

import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by vladimir on 7/9/16.
 */
public class TestRunner {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(TestRunner.class);


    public static void main(String[] args) throws IOException, InterruptedException{
        List<String> keyCache = keysCache(1000);
        logger.info("running lettuce");
        try (LettuceBuilder bldr1 = new LettuceBuilder(keyCache)) {
            runTests(bldr1);
        }
        logger.info("running jedis");
        List<byte[]> keysCache2 = keysCache2(1000);
        try (JedisBuilder bldr2   = new JedisBuilder(keyCache, keysCache2,  ConfigData.NUM_THREADS)) {
            //runTests(bldr2);
        }
    }

    public static void runTests(RedisTestBuilder bldr) throws InterruptedException{
        ExecutorService executor = Executors.newFixedThreadPool(ConfigData.NUM_THREADS);
        for (int i = 0; i < ConfigData.NUM_THREADS; ++i) {
            executor.submit(new TestRunnable(bldr.build()));
        }
        logger.info("sent {} runnables", ConfigData.NUM_THREADS);
        executor.shutdown();
        executor.awaitTermination(1000, TimeUnit.DAYS);
    }

    public static List<byte[]> keysCache2(int limit) {
        List<byte[]> tmp = new ArrayList<>(IntStream.range(0, limit).boxed().
                map(e -> e.toString().getBytes(StandardCharsets.UTF_8) ).collect(Collectors.toList()));
        return Collections.unmodifiableList(tmp);
    }


    public static List<String> keysCache(int limit) {
        List<String> tmp = new ArrayList<>(IntStream.range(0, limit).boxed().map(e -> e.toString() ).collect(Collectors.toList()));
        return Collections.unmodifiableList(tmp);
    }
}
