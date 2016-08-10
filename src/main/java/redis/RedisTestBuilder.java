package redis;

import java.io.Closeable;

/**
 * Created by vladimir on 7/9/16.
 */
public interface RedisTestBuilder extends Closeable{
    RedisRunnable build();
}
