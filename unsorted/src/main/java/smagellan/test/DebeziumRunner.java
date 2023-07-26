package smagellan.test;

import io.debezium.config.Configuration;
import io.debezium.connector.mysql.MySqlConnectorConfig;
import io.debezium.embedded.EmbeddedEngine;
import io.debezium.relational.history.MemorySchemaHistory;

import java.util.function.Function;

public class DebeziumRunner {

    public static final String APP_NAME = "DebeziumRunner";
    private final Configuration config;

    public DebeziumRunner() {
        config = Configuration.empty().withSystemProperties(Function.identity()).edit()
                .with(EmbeddedEngine.CONNECTOR_CLASS, "io.debezium.connector.mysql.MySqlConnector")
                .with(EmbeddedEngine.ENGINE_NAME, APP_NAME)
                .with(MySqlConnectorConfig.TOPIC_PREFIX,APP_NAME)
                .with(MySqlConnectorConfig.SERVER_ID, 8192)

                // for demo purposes let's store offsets and history only in memory
                .with(EmbeddedEngine.OFFSET_STORAGE, "org.apache.kafka.connect.storage.MemoryOffsetBackingStore")
                .with(MySqlConnectorConfig.SCHEMA_HISTORY, MemorySchemaHistory.class.getName())

                // Send JSON without schema
                .with("schemas.enable", false)
                .build();

    }

    public static void main(String[] args){
    }
}
