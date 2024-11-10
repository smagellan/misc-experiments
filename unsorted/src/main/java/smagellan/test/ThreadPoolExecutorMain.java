package smagellan.test;

import java.time.Duration;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolExecutorMain {
    public static void main(String[] args) throws InterruptedException {
        ScheduledThreadPoolExecutor stpe = new ScheduledThreadPoolExecutor(1);
        Runnable r = () -> System.err.println("hello");
        stpe.schedule(r, 20, TimeUnit.MINUTES);
        Thread.sleep(60 * 1000);
    }
}
