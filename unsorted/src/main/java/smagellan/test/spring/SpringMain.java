package smagellan.test.spring;

import com.google.common.base.Stopwatch;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.scheduler.Schedulers;
import smagellan.test.spring.config1.AppConfig;
import smagellan.test.spring.config1.ComponentBeanOne;
import smagellan.test.spring.config1.ComponentBeanTwo;
import smagellan.test.spring.config2.AppConfig2;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.LongAdder;

public class SpringMain {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(AppConfig2.class);

    public static void main(String[] args) throws Exception {
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

            String http2Endpoint = "https://localhost:443";
            String http11Endpoint = "https://localhost:444";
            doFetch(wc, http2Endpoint, 330);
            //doFetch(wc, http2Endpoint, 1_000);

            Stopwatch sw = Stopwatch.createStarted();
            doFetch(wc, http2Endpoint, 330);
            Duration d1 = sw.elapsed();
            sw.reset().start();

            //HTTP 1.1
            doFetch(wc, http11Endpoint, 330);
            Duration d2 = sw.elapsed();
            sw.reset();

            logger.info("d1: {}. d2: {}", d1.toMillis(), d2.toMillis());
        }
    }

    private static void doFetch(WebClient wc, String uri, int limit) throws ExecutionException, InterruptedException {
        Collection<CompletableFuture<Void>> responses = new ArrayList<>();
        LongAdder adder = new LongAdder();
        for (int i = 0; i < limit; ++i) {
            CompletableFuture<Void> future = wc.get()
                    .uri(uri + "?i=" + i)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(5L))
                    .subscribeOn(Schedulers.single())
                    .toFuture()
                    .thenAccept((s) -> adder.add(s.length()));
            responses.add(future);
        }

        for (CompletableFuture<Void> r : responses) {
            r.get();
        }
        logger.info("len: {}", adder.sum());
    }
}

