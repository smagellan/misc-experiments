package smagellan.test;

import de.bwaldvogel.mongo.MongoBackend;
import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.ServerVersion;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.net.InetSocketAddress;
import java.util.Map;

@Configuration
public class MockedMongoConfiguration {
    public static final String MOCKED_MONGO_SERVER = "mockedMongoServer";
    public static final String LOCAL_MONGO_PORT = "local.mongo.port";

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(TestConfiguration.class);

    @Autowired
    @Bean(name = MOCKED_MONGO_SERVER)
    public MongoServer mongoServer(ConfigurableEnvironment evt) {
        long startTime = System.currentTimeMillis();
        MongoBackend backend = new MemoryBackend()
                .version(ServerVersion.MONGO_3_6);
        MongoServer srv = new MongoServer(backend);
        InetSocketAddress serverAddress = srv.bind();
        evt.getPropertySources().addLast(new MapPropertySource("mocked_mongo_params", Map.of(LOCAL_MONGO_PORT, serverAddress.getPort())));
        long startupDuration = System.currentTimeMillis() - startTime;
        logger.info("mongo start duration: {} ms", startupDuration);
        return srv;
    }
}
