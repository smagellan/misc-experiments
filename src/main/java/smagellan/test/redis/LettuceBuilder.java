package smagellan.test.redis;

import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisURI;
import com.lambdaworks.redis.api.sync.RedisCommands;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by vladimir on 7/9/16.
 */
public class LettuceBuilder implements RedisTestBuilder{
    private final RedisClient client;
    private final List<String> keyCache;
    public LettuceBuilder(List<String> keyCache) {
        //RedisURI redisURI = RedisURI.builder().socket("/var/run/redis/redis.sock").build();
        RedisURI redisURI = RedisURI.builder().withHost("localhost").withPort(6379).build();
        this.client = RedisClient.create(redisURI);
        this.keyCache   = keyCache;
    }

    public LettuceRunnable build() {
        RedisCommands<String, String> connection = client.connect().sync();
        return new LettuceRunnable(connection, keyCache);
    }

    public void close() throws IOException{
        client.shutdown();
    }
}

class LettuceRunnable implements RedisRunnable {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(LettuceRunnable.class);
    private final RedisCommands<String, String> connection;
    private final List<String> keyCache;

    public LettuceRunnable(RedisCommands<String, String> connection, List<String> keyCache) {
        this.connection = connection;
        this.keyCache =  keyCache;
    }

    @Override
    public void run() {
        Random rnd = ThreadLocalRandom.current();
        final int sz = keyCache.size();
        String key = keyCache.get(rnd.nextInt(sz));
        String value = keyCache.get(rnd.nextInt(sz));
        connection.psetex(key, 10000, value );
    }

    @Override
    public void close() throws IOException {
        connection.close();
    }
}