package smagellan.test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfiguration.class)
public class EmployeeRestControllerIntegrationTestJunit5V2 {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(EmployeeRestControllerIntegrationTestJunit5V2.class);

    @Autowired
    TestConfiguration configuration;

    @Test
    public void doTest() {
        logger.info("configuration: {}", configuration);
    }
}
