package smagellan.test;

import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.time.Duration;

/**
 * Created by vladimir on 10/5/16.
 */
public class SpringTaskExecMain {
    static {
        System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");
    }
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SpringTaskExecMain.class);
    private static final java.util.logging.Logger logger2 = java.util.logging.LogManager.getLogManager().getLogger(SpringTaskExecMain.class.getName());
    public static void main(String[] args) {
        //Configurator.setRootLevel(Level.TRACE);
        MDC.put(MyRunnable.MDC_KEY, "myValue");
        logger2.info("jul log message");
        logger.debug("mdc key: {}", MyRunnable.mdcValue());
        ConfigurableApplicationContext context = new ClassPathXmlApplicationContext("spring-thread-executor.xml");
        context.refresh();
        System.err.println("refreshed");
        logger.info("refreshed");
        context.refresh();
        System.err.println("refreshed");
        ThreadPoolTaskScheduler scheduler = context.getBean(ThreadPoolTaskScheduler.class);
        scheduler.scheduleAtFixedRate(new MyRunnable(), Duration.ofMillis(5000));
        Thread t = new Thread(new MyRunnable2(), "MDC Test Thread");
        t.start();
    }
}


class MyRunnable implements Runnable {
    public static final String MDC_KEY = "MY_KEY";
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(MyRunnable.class);

    @Override
    public void run() {
        logger.debug("mdc key: {}", mdcValue());
    }

    public static String mdcValue() {
        return MDC.get(MDC_KEY);
    }
}

class MyRunnable2 implements Runnable {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(MyRunnable2.class);

    @Override
    public void run() {
        logger.debug("mdc key: {}", MyRunnable.mdcValue());
    }
}