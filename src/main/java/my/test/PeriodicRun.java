package my.test;

public class PeriodicRun {
    public static void main(String[] args) throws InterruptedException {
        while (true) {
            Thread.sleep(10 * 1000L);
            doWork();
        }
    }

    public static void doWork() {
        System.err.println("stealing your cpu timez");
    }
}
