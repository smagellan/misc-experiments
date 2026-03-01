package smagellan.test.mongodb;

import de.bwaldvogel.mongo.MongoBackend;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.ServerVersion;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;

@Configuration
public class MongoDbServerConfiguration {
    @Bean
    public MongoServer mongoServer(MongoBackend backend) {
        MongoServer srv = new MongoServer(backend);
        srv.bind();
        return srv;
    }

    @Bean
    public MongoBackend mongoBackend() {
        return new MemoryBackend()
                .version(ServerVersion.MONGO_4_0);
    }

    @Bean("mongoDbServerAddress")
    public InetSocketAddress mongoDbServerAddress(MongoServer srv) {
        return srv.getLocalAddress();
    }
}
