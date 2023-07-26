package smagellan.test;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class SpringSingleton implements InitializingBean {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SpringSingleton.class);

    @Override
    public void afterPropertiesSet() throws Exception {
        logger.info("afterPropertiesSet called");
    }
}
