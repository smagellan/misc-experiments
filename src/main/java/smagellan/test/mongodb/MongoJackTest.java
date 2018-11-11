package smagellan.test.mongodb;

import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.mongojack.JacksonMongoCollection;

import java.util.UUID;

public class MongoJackTest {
    public static void main(String[] args) {
        try (MongoClient client = new MongoClient(new ServerAddress("localhost", 27017))) {
            MongoDatabase db = client.getDatabase("mydb");
            MongoCollection<Document> collection = db.getCollection("mongojack-collection");

            //doInsert(collection);
            doFetch(collection);
        }
    }

    private static void doInsert(MongoCollection<Document> collection) {
        JacksonMongoCollection<MongoJackEntity> coll = JacksonMongoCollection.<MongoJackEntity>builder()
                .build(collection, MongoJackEntity.class);

        coll.insert(new MongoJackEntity().withName("name-" + UUID.randomUUID()),
                new MongoJackEntity().withName("name-" + UUID.randomUUID()));
    }

    private static void doFetch(MongoCollection<Document> collection) {
        JacksonMongoCollection<MongoJackEntity> coll = JacksonMongoCollection.<MongoJackEntity>builder()
                .build(collection, MongoJackEntity.class);


        FindIterable<MongoJackEntity> iterable = coll.find();
        for (Object obj : iterable) {
            System.err.println(obj);
        }
    }
}
