import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Created by vladimir on 10/5/16.
 */
public class SpringTaskExecMain {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SpringTaskExecMain.class);
    public static void main(String[] args) {
        MDC.put(MyRunnable.MDC_KEY, "myValue");
        logger.debug("mdc key: {}", MyRunnable.mdcValue());
        ApplicationContext context = new ClassPathXmlApplicationContext("spring-thread-executor.xml");
        ThreadPoolTaskScheduler scheduler = context.getBean(ThreadPoolTaskScheduler.class);
        scheduler.scheduleAtFixedRate(new MyRunnable(), 5000);
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