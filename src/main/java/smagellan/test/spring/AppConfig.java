package smagellan.test.spring;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
@ComponentScan(
        basePackages = "smagellan.test.spring"
        //excludeFilters = @ComponentScan.Filter(value = Configuration.class, type = FilterType.ANNOTATION)
)
public class AppConfig {
    public static final String NAMED_BEAN = "named_bean";

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(AppConfig.class);


    @Bean
    ComponentBeanOne moderationClientOne(ComponentBeanTwo dependency) {
        logger.debug("====== creating moderationClientOne, dependency: {}", dependency);
        return new ComponentBeanOne();
    }

    @Bean
    @Lazy
    @Autowired
    ComponentBeanTwo moderationClientTwo(ComponentBeanThree dependency) {
        return new ComponentBeanTwo();
    }

    @Bean
    ComponentBeanThree dependencyBeanOne() {
        return new ComponentBeanThree();
    }

    @Bean
    ComponentBeanFour dependencyBeanTwo() {
        return new ComponentBeanFour();
    }

    @Bean
    @Lazy
    ComponentBeanFive dependencyBeanThree() {
        logger.debug("======creating dependency bean five");
        return new ComponentBeanFive();
    }

    @Bean
    @Lazy
    @Qualifier("qq")
    ComponentBeanSix dependencyBeanFour() {
        return new ComponentBeanSix();
    }
}
