package smagellan.test;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
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

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringJUnitWebConfig(value = TestConfiguration.class, initializers = ConfigurationWarningsApplicationContextInitializer.class)
@RecordApplicationEvents
@ActiveProfiles(value = "test", resolver = DefaultActiveProfilesResolver.class)
public class EmployeeRestControllerIntegrationTestJunit5 {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(EmployeeRestControllerIntegrationTestJunit5.class);

    TestConfiguration configuration;

    ApplicationContext appContext;

    ApplicationEvents applicationEvents;

    MongoClient mongoClient;

    private final ExceptionCollector collector = new ExceptionCollector();

    private MockMvc mockMvc;

    private WebTestClient client;

    public EmployeeRestControllerIntegrationTestJunit5(TestConfiguration configuration, ApplicationContext appContext, ApplicationEvents applicationEvents, MongoClient mongoClient) {
        this.configuration = configuration;
        this.appContext = appContext;
        this.applicationEvents = applicationEvents;
        this.mongoClient = mongoClient;
    }

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
    public void doWebTest() {
        logger.info("configuration: {}", configuration);
        logger.info("application events: {}", applicationEvents.stream().collect(Collectors.toList()));
        client.get()
                .uri("/resource/javamail.providers")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Type", MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .expectHeader().valueEquals("Content-Length", 118);
    }

    @Test
    public void doMongoTest(TestInfo testInfo) {
        MongoCollection<Document> collection = mongoClient.getDatabase("testDb").getCollection("testCollection");
        assertEquals(0, collection.countDocuments());
        Document doc = new Document("_id", 1)
                .append("key42", "value42")
                .append("key43", "value43");
        collection.insertOne(doc);
        assertEquals(1, collection.countDocuments());
        FindIterable<Document> docs = collection.find(new BasicDBObject("key42", "value42"));
        Document docFromDb = docs.iterator().next();
        assertEquals("value43", docFromDb.get("key43", String.class));
        logger.info("testInfo: {}", testInfo);
    }
}
