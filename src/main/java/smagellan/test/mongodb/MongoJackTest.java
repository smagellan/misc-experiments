package smagellan.test.mongodb;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.UuidRepresentation;
import org.mongojack.JacksonMongoCollection;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class MongoJackTest {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(MongoJackTest.class);

    public static void main(String[] args) {
        MongoClientOptions opts = new MongoClientOptions.Builder()
                .applicationName("testApp")
                .build();
        try (MongoClient client = new MongoClient(new ServerAddress("localhost", 27017), opts)) {
            MongoDatabase db = client.getDatabase("mydb");
            MongoCollection<MongoJackEntity> collection = db.getCollection("mongojack-collection", MongoJackEntity.class);

            //doInsert(collection);
            doFetch(collection);
        }
    }

    private static void doInsert(MongoCollection<MongoJackEntity> collection) {
        JacksonMongoCollection<MongoJackEntity> coll = JacksonMongoCollection.builder()
                .build(collection, MongoJackEntity.class, UuidRepresentation.JAVA_LEGACY);

        coll.insert(new MongoJackEntity().withName("name-" + UUID.randomUUID()),
                new MongoJackEntity().withName("name-" + UUID.randomUUID()));
    }

    private static void doFetch(MongoCollection<MongoJackEntity> collection) {
        JacksonMongoCollection<MongoJackEntity> coll = JacksonMongoCollection.builder()
                .build(collection, MongoJackEntity.class, UuidRepresentation.JAVA_LEGACY);


        FindIterable<MongoJackEntity> iterable = coll.find();
        for (Object obj : iterable) {
            logger.info("obj: {}", obj);
        }
    }
}
