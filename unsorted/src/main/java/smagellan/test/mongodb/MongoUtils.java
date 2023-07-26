package smagellan.test.mongodb;

import com.google.common.collect.ImmutableList;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoIterable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MongoUtils {
    @NotNull
    public static <T> List<T> pull(MongoIterable<T> namesIterable) {
        try (MongoCursor<T> iter = namesIterable.iterator()) {
            return ImmutableList.copyOf(iter);
        }
    }
}
