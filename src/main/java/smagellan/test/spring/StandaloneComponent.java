package smagellan.test.spring;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StandaloneComponent {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(StandaloneComponent.class);

    public StandaloneComponent(ComponentBeanThree dependency){
        logger.info("dependency1: {}", dependency);
    }

    @Autowired
    public StandaloneComponent(ComponentBeanThree dependency, ComponentBeanTwo two){
        logger.info("dependency1: {}, dependency2: {}", dependency, two);
    }
}
