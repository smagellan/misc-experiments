package redis;

import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by vladimir on 7/9/16.
 */
public class JedisBuilder implements RedisTestBuilder {
    private final List<String> keyCache;
    private final List<byte[]> keyCache2;
    private final JedisPool pool;
    public JedisBuilder(List<String> keyCache, List<byte[]> keyCache2, int maxThreads) {
        this.keyCache = keyCache;
        this.keyCache2 = keyCache2;
        JedisPoolConfig conf = new JedisPoolConfig();
        conf.setMaxTotal(maxThreads * 8);
        pool = new JedisPool(conf, "localhost");
        JedisCluster cl = new JedisCluster(HostAndPort.parseString(""));
    }

    public JedisRunnable build() {
        return new JedisRunnable(pool, keyCache, keyCache2);
    }

    public void close() throws IOException {
        pool.close();
    }
}


class JedisRunnable implements  RedisRunnable {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(JedisRunnable.class);
    private final JedisPool pool;
    private final List<String> keyCache;
    private final List<byte[]> keyCache2;
    private final Jedis jedis;
    public static final byte[] TIMEOUT = "1000".getBytes(StandardCharsets.UTF_8);

    public JedisRunnable(JedisPool pool, List<String> keyCache, List<byte[]> keyCache2) {
        this.pool = pool;
        //this.jedis = pool.getResource();
        this.jedis = new Jedis("localhost");
        //this.jedis = null;
        this.keyCache = keyCache;
        this.keyCache2 = keyCache2;
    }

    @Override
    public void run() {
        run3();
    }


    public void run1() {
        Random rnd = ThreadLocalRandom.current();
        final int sz = keyCache.size();
        String key = keyCache.get(rnd.nextInt(sz));
        String value = keyCache.get(rnd.nextInt(sz));
        try (Jedis jedis = pool.getResource()) {
            jedis.psetex(key, 1000L, value);
        }
    }


    public void run2() {
        Random rnd = ThreadLocalRandom.current();
        final int sz = keyCache.size();
        String key = keyCache.get(rnd.nextInt(sz));
        String value = keyCache.get(rnd.nextInt(sz));
        //String key = String.valueOf(rnd.nextInt());
        //String value = String.valueOf(rnd.nextInt());
        jedis.psetex(key, 1000L, value);
    }

    public void run3() {
        Random rnd = ThreadLocalRandom.current();
        final int sz = keyCache.size();
        byte[] key = keyCache2.get(rnd.nextInt(sz));
        byte[] value = keyCache2.get(rnd.nextInt(sz));
        //String key = String.valueOf(rnd.nextInt());
        //String value = String.valueOf(rnd.nextInt());
        jedis.psetex(key, 1000L, value);
    }

    @Override
    public void close() throws IOException {

    }
}