package smagellan.test.mongodb;

import com.mongodb.internal.build.MongoDriverVersion;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.util.MongoClientVersion;

public class SpringDataMongodbRunner {
    public static void main(String[] args) {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(MongoDbServerConfiguration.class, MongoClientConfig.class)) {
            System.err.println("MongoClientVersion.isVersion5orNewer: " + MongoClientVersion.isVersion5orNewer() + "; MongoDriverVersion.VERSION: " + MongoDriverVersion.VERSION);
            com.mongodb.client.MongoClient client = ctx.getBean(com.mongodb.client.MongoClient.class);
            MongoOperations ops = new MongoTemplate(client, "local");
            ops.getCollectionNames();
        }
    }
}
