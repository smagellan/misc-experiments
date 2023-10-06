package smagellan.test;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;
import org.springframework.web.reactive.DispatcherHandler;
import org.springframework.web.server.WebHandler;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;
import smagellan.test.mongodb.TracingServerListener;
import smagellan.test.mongodb.TracingServerMonitorListener;

@Configuration
@Import({
        MyWebFluxConfiguration.class,
        MockedMongoConfiguration.class
})
public class TestConfiguration {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(TestConfiguration.class);

    public TestConfiguration() {
        logger.info("TestConfiguration created");
    }

    @Bean(name = WebHttpHandlerBuilder.WEB_HANDLER_BEAN_NAME)
    public WebHandler myWebHandler() {
        return new DispatcherHandler();
    }

    //local.mongo.port is similar to @LocalServerPort
    @DependsOn(MockedMongoConfiguration.MOCKED_MONGO_SERVER)
    @Bean
    public MongoClient mongoClient(@Value("${local.mongo.port}") int localMongoPort) {
        MongoClientOptions opts = new MongoClientOptions.Builder()
                .applicationName("testApp")
                .addServerListener(new TracingServerListener())
                .addServerMonitorListener(new TracingServerMonitorListener())
                .heartbeatFrequency(10_000)
                .build();
        MongoCredential credential = null;
        return new MongoClient(new ServerAddress("localhost", localMongoPort), credential, opts);
    }
}
