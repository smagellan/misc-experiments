package my.test.redis;

import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by vladimir on 7/9/16.
 */
public class TestRunnable implements RedisRunnable {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(TestRunnable.class);

    private final RedisRunnable r;
    public TestRunnable(RedisRunnable r) {
        this.r = r;
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < ConfigData.ITEMS_PER_THREAD; ++i) {
            r.run();
        }
        LOGGER.info("ops took {} msec", System.currentTimeMillis() - startTime);
    }

    @Override
    public void close() throws IOException {
        r.close();
    }
}
