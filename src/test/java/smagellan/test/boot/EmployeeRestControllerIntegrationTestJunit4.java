package smagellan.test.boot;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import smagellan.test.TestConfiguration;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = BootTestConfiguration.class)
@AutoConfigureMockMvc
//@TestPropertySource(locations = "classpath:application-integrationtest.properties")
public class EmployeeRestControllerIntegrationTestJunit4 {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(EmployeeRestControllerIntegrationTestJunit4.class);

    @Autowired
    private MockMvc mvc;

    @Autowired
    BootTestConfiguration configuration;

    @LocalServerPort
    int port;

    // write test cases here

    @Test
    public void doTest() {
        logger.info("configuration: {}", configuration);
        logger.info("port: {}", port);
    }
}