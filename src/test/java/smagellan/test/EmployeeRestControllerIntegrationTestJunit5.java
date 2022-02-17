package smagellan.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.moditect.jfrunit.EnableEvent;
import org.moditect.jfrunit.JfrEventTest;
import org.moditect.jfrunit.JfrEvents;
import org.moditect.jfrunit.events.GarbageCollection;
import org.moditect.jfrunit.events.ThreadSleep;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.ConfigurationWarningsApplicationContextInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.context.support.DefaultActiveProfilesResolver;
import org.springframework.test.util.ExceptionCollector;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@SpringJUnitWebConfig(value = TestConfiguration.class, initializers = ConfigurationWarningsApplicationContextInitializer.class)
@RecordApplicationEvents
@ActiveProfiles(value = "test", resolver = DefaultActiveProfilesResolver.class)
@JfrEventTest
public class EmployeeRestControllerIntegrationTestJunit5 {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(EmployeeRestControllerIntegrationTestJunit5.class);

    @Autowired
    TestConfiguration configuration;

    @Autowired
    ApplicationContext appContext;

    @Autowired
    ApplicationEvents applicationEvents;

    ExceptionCollector collector = new ExceptionCollector();

    private MockMvc mockMvc;

    private WebTestClient client;

    public JfrEvents jfrEvents = new JfrEvents();

    @BeforeEach
    public void setup(WebApplicationContext wac) {
        logger.info("wac: {}", wac);
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
                .build();

        client = WebTestClient
                .bindToApplicationContext(appContext)
                .build();
    }

    @Test
    @EnableEvent(GarbageCollection.EVENT_NAME)
    @EnableEvent(ThreadSleep.EVENT_NAME)
    public void doTest() {
        jfrEvents.awaitEvents();
        logger.info("configuration: {}", configuration);
        logger.info("application events: {}", applicationEvents.stream().collect(Collectors.toList()));
        logger.info("jfr events: {}", jfrEvents.events().collect(Collectors.toList()));
        client.get()
                .uri("/resource/javamail.providers")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Type", MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .expectHeader().valueEquals("Content-Length", 118);
    }
}
