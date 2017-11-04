package my.test.spring;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class SpringMain {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext beanFactory = new AnnotationConfigApplicationContext(AppConfig.class);
        beanFactory.registerShutdownHook();
        ComponentBeanOne beanOne = beanFactory.getBean(ComponentBeanOne.class);
        ComponentBeanTwo beanTwo = beanFactory.getBean(ComponentBeanTwo.class);
    }
}
