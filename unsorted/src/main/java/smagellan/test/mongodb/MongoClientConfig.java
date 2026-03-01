package smagellan.test.mongodb;

import com.mongodb.*;
import com.mongodb.client.MongoClients;
import com.mongodb.connection.ClusterSettings;
import com.mongodb.connection.ServerSettings;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
public class MongoClientConfig {
    @Bean
    public MongoClient mongoClient(@Qualifier("mongoDbServerAddress") InetSocketAddress serverAddress, MongoClientOptions opts) {
        return new MongoClient(new ServerAddress("localhost", serverAddress.getPort()), null, opts);
    }

    @Bean
    public com.mongodb.client.MongoClient mongoClient2(MongoClientSettings settings) {
        return MongoClients.create(settings);
    }

    @Bean
    public MongoClientSettings mongoClientSettings(@Qualifier("mongoDbServerAddress") InetSocketAddress serverAddress) {
        Block<ServerSettings.Builder> serverSettingsBuilder = (b) -> b
                .serverMonitorListenerList(List.of(new TracingServerMonitorListener()))
                .serverListenerList(List.of(new TracingServerListener()))
                .heartbeatFrequency(10_000, TimeUnit.MILLISECONDS);

        Block<ClusterSettings.Builder> clusterSettingsBuilder = (b) -> b
                .hosts(List.of(new ServerAddress("localhost", serverAddress.getPort())));

        return MongoClientSettings.builder()
                .applicationName("testApp")
                .compressorList(Arrays.asList(MongoCompressor.createZlibCompressor(), MongoCompressor.createSnappyCompressor()))
                .applyToServerSettings(serverSettingsBuilder)
                .applyToClusterSettings(clusterSettingsBuilder)
                .build();
    }

    @Bean
    public MongoClientOptions mongoClientOptions(MongoClientSettings opts) {
        return MongoClientOptions.builder(opts)
                .build();
    }
}
