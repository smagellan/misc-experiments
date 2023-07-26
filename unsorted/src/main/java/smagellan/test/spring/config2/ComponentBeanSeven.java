package smagellan.test.spring.config2;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

public class ComponentBeanSeven {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ComponentBeanSeven.class);

    @Value("${myconfig.port}")
    private int port;

    public ComponentBeanSeven() {
        logger.info("ComponentBeanSeven");
    }
}
