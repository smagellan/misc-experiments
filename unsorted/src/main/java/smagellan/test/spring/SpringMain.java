package smagellan.test.spring;

import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.web.reactive.function.client.WebClient;
import smagellan.test.spring.config1.AppConfig;
import smagellan.test.spring.config1.ComponentBeanOne;
import smagellan.test.spring.config1.ComponentBeanTwo;
import smagellan.test.spring.config2.AppConfig2;

import java.util.Map;

public class SpringMain {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(AppConfig2.class);

    public static void main(String[] args) {
        int port = 0;
        try (AnnotationConfigApplicationContext beanFactory = new AnnotationConfigApplicationContext()) {
            beanFactory.register(AppConfig.class);
            beanFactory.registerShutdownHook();
            beanFactory.refresh();
            ComponentBeanOne beanOne = beanFactory.getBean(ComponentBeanOne.class);
            ComponentBeanTwo beanTwo = beanFactory.getBean(ComponentBeanTwo.class);
            port = beanOne.port();
        }

        PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
        MutablePropertySources propertySources = new MutablePropertySources();
        MapPropertySource propertySource = new MapPropertySource("custom0", Map.of("myconfig.port", port));
        propertySources.addLast(propertySource);
        configurer.setPropertySources(propertySources);
        try (AnnotationConfigApplicationContext beanFactory = new AnnotationConfigApplicationContext()) {
            //ConfigurableEnvironment env = beanFactory.getEnvironment();
            //env.getPropertySources().addLast(propertySource);
            beanFactory.addBeanFactoryPostProcessor(configurer);
            beanFactory.register(AppConfig2.class);
            beanFactory.registerShutdownHook();
            beanFactory.refresh();

            WebClient wc = beanFactory.getBean(WebClient.class);
            String ret = wc.get()
                    .uri("https://google.com")
                    .retrieve()
                    .toEntity(String.class)
                    .block()
                    .getBody();
            System.err.println(ret);
        }
    }
}
