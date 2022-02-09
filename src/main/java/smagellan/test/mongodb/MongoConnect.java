package smagellan.test.mongodb;

import com.google.common.collect.ImmutableList;
import com.mongodb.*;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.event.*;
import de.bwaldvogel.mongo.MongoBackend;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.ServerVersion;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;

public class MongoConnect {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(MongoConnect.class);

    public static void main(String[] args) throws InterruptedException {
        MongoClientOptions opts = new MongoClientOptions.Builder()
                .applicationName("testApp")
                .addServerListener(new TracingServerListener())
                .addServerMonitorListener(new TracingServerMonitorListener())
                .heartbeatFrequency(10_000)
                .build();
        //MongoCredential credential = MongoCredential.createCredential("mongouser", "admin", "someothersecret".toCharArray());
        //MongoCredential credential = MongoCredential.createCredential("mongoadmin", "admin", "secret".toCharArray());
        //MongoCredential credential = MongoCredential.createPlainCredential("mongoadmin", "admin", "secret".toCharArray());
        MongoCredential credential = null;

        MongoServer srv = null;
        try {
            MongoBackend backend = new MemoryBackend()
                    .version(ServerVersion.MONGO_3_6);
            srv = new MongoServer(backend);
            InetSocketAddress serverAddress = srv.bind();
            logger.info("server status: {}", backend.getServerStatus());
            try (MongoClient client = new MongoClient(new ServerAddress("localhost", serverAddress.getPort()), credential, opts)) {
                MongoIterable<String> namesIterable = client.listDatabaseNames();
                List<String> dbNames = pull(namesIterable);
                logger.info("listDatabaseNames: {}", dbNames);
                for (String dbName : dbNames) {
                    MongoDatabase db = client.getDatabase(dbName);
                    traceUsers(db);
                    List<String> collectionNames = pull(db.listCollectionNames());
                    logger.info("listCollectionNames({}): {}", dbName, collectionNames);
                }
                Thread.sleep(30 * 1000);
            }
        } finally {
            if (srv != null) {
                srv.shutdown();
            }
        }
    }

    private static void traceUsers(MongoDatabase db) {
        BasicDBObject dbStats = new BasicDBObject("usersInfo", 1);
        Document command = db.runCommand(dbStats);
        System.out.println(command.get("users"));
        List users = (List) command.get("users");
        logger.info("users of {}", db.getName());
        for (Object u : users) {
            logger.info("user: {}", u);
        }
    }

    @NotNull
    private static <T> List<T> pull(MongoIterable<T> namesIterable) {
        try (MongoCursor<T>  iter = namesIterable.iterator()) {
            return ImmutableList.copyOf(iter);
        }
    }
}

class TracingServerListener implements ServerListener {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(TracingServerListener.class);

    @Override
    public void serverOpening(ServerOpeningEvent event) {
        logger.info("serverOpening: {}", event);
    }

    @Override
    public void serverClosed(ServerClosedEvent event) {
        logger.info("serverClosed: {}", event);
    }

    @Override
    public void serverDescriptionChanged(ServerDescriptionChangedEvent event) {
        logger.info("serverDescriptionChanged: {}", event);
    }
}

class TracingServerMonitorListener implements ServerMonitorListener {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(TracingServerMonitorListener.class);

    @Override
    public void serverHearbeatStarted(ServerHeartbeatStartedEvent event) {
        logger.info("serverHearbeatStarted: {}", event);
    }

    @Override
    public void serverHeartbeatSucceeded(ServerHeartbeatSucceededEvent event) {
        logger.info("serverHeartbeatSucceeded: {}", event);
    }

    @Override
    public void serverHeartbeatFailed(ServerHeartbeatFailedEvent event) {
        logger.info("serverHeartbeatFailed: {}", event);
    }
}