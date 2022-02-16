package smagellan.test;

import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.reactive.DispatcherHandler;
import org.springframework.web.server.WebHandler;

@Configuration
@Import(MyWebFluxConfigSupport.class)
public class TestConfiguration {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(TestConfiguration.class);

    public TestConfiguration() {
        logger.info("TestConfiguration created");
    }

    @Bean
    public WebHandler webHandler() {
        return new DispatcherHandler();
    }
}
