package smagellan.embeddedserver.clients;

import com.google.common.base.Stopwatch;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.LongAdder;

public class RunClientsTest {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(RunClientsTest.class);
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (AnnotationConfigApplicationContext beanFactory = new AnnotationConfigApplicationContext()) {
            beanFactory.register(HttpClientsClientConfig.class);
            beanFactory.registerShutdownHook();
            beanFactory.refresh();

            WebClient wc = beanFactory.getBean(HttpClientsClientConfig.WEB_CLIENT_APACHE, WebClient.class);
            runRequests(wc);
            logger.info("about to stop the spring context");
        }
    }

    private static void runRequests(WebClient wc) throws ExecutionException, InterruptedException {
        String http2Endpoint = "https://localhost:443";
        String http11Endpoint = "https://localhost:444";
        doFetch(wc, http2Endpoint, 100);
        doFetch(wc, http11Endpoint, 100);
        //doFetch(wc, http2Endpoint, 1_000);

        Stopwatch sw = Stopwatch.createStarted();
        doFetch(wc, http2Endpoint, 200);
        Duration d1 = sw.elapsed();
        sw.reset().start();

        //HTTP 1.1
        doFetch(wc, http11Endpoint, 200);
        Duration d2 = sw.elapsed();
        sw.reset();

        logger.info("d1: {}. d2: {}", d1.toMillis(), d2.toMillis());
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
