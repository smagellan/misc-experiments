package smagellan.test.spring.config2;

import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class AppConfig2 {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(AppConfig2.class);

    @Bean
    public ComponentBeanSeven beanSeven(Environment env) {
        return new ComponentBeanSeven();
    }
}
