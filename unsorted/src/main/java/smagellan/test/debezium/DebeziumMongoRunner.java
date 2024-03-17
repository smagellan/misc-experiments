package smagellan.test.debezium;


import com.mongodb.MongoClientSettings;
import io.debezium.config.CommonConnectorConfig;
import io.debezium.connector.mongodb.MongoDbConnectorConfig;
import io.debezium.embedded.EmbeddedEngineConfig;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.RecordChangeEvent;
import io.debezium.engine.format.ChangeEventFormat;
import io.debezium.embedded.Connect;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.runtime.ConnectorConfig;
import org.apache.kafka.connect.source.SourceRecord;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import smagellan.test.SpringTaskExecMain;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DebeziumMongoRunner {
    public static void main(String[] args) throws InterruptedException {
        try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(MongobbDebeziumContext.class)) {
            Thread.sleep(10_000);
        }
    }
}

class DebeziumSourceEventListener {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SpringTaskExecMain.class);

    private final Executor executor;
    private final DebeziumEngine<RecordChangeEvent<SourceRecord>> debeziumEngine;

    public DebeziumSourceEventListener(io.debezium.config.Configuration mongodbConnector) {
        this.executor = Executors.newSingleThreadExecutor();
        this.debeziumEngine = DebeziumEngine.create(ChangeEventFormat.of(Connect.class))
                .using(mongodbConnector.asProperties())
                .notifying(this::handleChangeEvent)
                .build();
    }

    private void handleChangeEvent(RecordChangeEvent<SourceRecord> sourceRecordRecordChangeEvent) {
        SourceRecord sourceRecord = sourceRecordRecordChangeEvent.record();
        Struct sourceRecordKey = (Struct) sourceRecord.key();
        Struct sourceRecordValue = (Struct) sourceRecord.value();

        logger.info("handleChangeEvent: Key = '" + sourceRecordKey + "' value = '" + sourceRecordValue + "'");
    }

    @PostConstruct
    private void start() {
        this.executor.execute(new LoggingRunnable("debeziumEngine", debeziumEngine));
    }

    @PreDestroy
    private void stop() throws IOException {
        if (this.debeziumEngine != null) {
            this.debeziumEngine.close();
        }
    }
}

@Configuration
class MongobbDebeziumContext {
    @Bean
    public io.debezium.config.Configuration debeziumConfig() throws IOException {
        //File offsetStorageTempFile = File.createTempFile("offsets_", ".dat");
        File offsetStorageTempFile = new File("offsets_.dat");
        return io.debezium.config.Configuration.create()
                .with(EmbeddedEngineConfig.ENGINE_NAME, "sbd-mongo")
                .with(EmbeddedEngineConfig.CONNECTOR_CLASS, "io.debezium.connector.mongodb.MongoDbConnector")
                .with(EmbeddedEngineConfig.OFFSET_STORAGE, "org.apache.kafka.connect.storage.FileOffsetBackingStore")
                .with(EmbeddedEngineConfig.OFFSET_STORAGE_FILE_FILENAME, offsetStorageTempFile.getAbsolutePath())
                .with(EmbeddedEngineConfig.OFFSET_FLUSH_INTERVAL_MS, "60000")
                .with(MongoDbConnectorConfig.CONNECTION_MODE, "replica_set")
                // connector specific properties
                .with(MongoDbConnectorConfig.CONNECTION_STRING, "mongodb://localhost:12345")
                .with(CommonConnectorConfig.TOPIC_PREFIX, "sbd-mongodb-connector")
                .with(CommonConnectorConfig.MAX_BATCH_SIZE, "5")
                //.with(MongoDbConnectorConfig.USER, "user")
                //.with(MongoDbConnectorConfig.PASSWORD, "password")
                //.with(MongoDbConnectorConfig.SSL_ENABLED, "true") // default false
                .with(MongoDbConnectorConfig.DATABASE_INCLUDE_LIST, "test") // default empty
                .with(CommonConnectorConfig.SNAPSHOT_DELAY_MS, "100")
                .with(ConnectorConfig.ERRORS_LOG_INCLUDE_MESSAGES_CONFIG, "true")
                .build();
    }

    @Bean
    public DebeziumSourceEventListener eventListener(io.debezium.config.Configuration conf) {
        return new DebeziumSourceEventListener(conf);
    }
}

class LoggingRunnable implements Runnable {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(LoggingRunnable.class);


    private final Runnable wrapped;
    private final String name;

    LoggingRunnable(String name, Runnable wrapped) {
        this.name = name;
        this.wrapped = wrapped;
    }

    @Override
    public void run() {
        logger.info("{}: started", name);
        wrapped.run();
        logger.info("{}: finished", name);
    }
}