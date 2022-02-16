package smagellan.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = TestConfiguration.class)
@AutoConfigureMockMvc
//@TestPropertySource(locations = "classpath:application-integrationtest.properties")
public class EmployeeRestControllerIntegrationTestJunit4 {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(EmployeeRestControllerIntegrationTestJunit4.class);

    @Autowired
    private MockMvc mvc;

    @Autowired
    TestConfiguration configuration;

    // write test cases here

    @Test
    public void doTest() {
        logger.info("configuration: {}", configuration);
    }
}