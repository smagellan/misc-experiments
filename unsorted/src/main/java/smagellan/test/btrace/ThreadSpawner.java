package smagellan.test.btrace;

public class ThreadSpawner {
    public static void main(String[] args) throws InterruptedException {
        System.err.println("ours pid: " + ProcessHandle.current().pid());
        for (int i = 0; i < 1000; ++i) {
            spawnThread();
            Thread.sleep(5 * 1000);
        }
    }

    private static void spawnThread() throws InterruptedException {
        Thread t = new Thread( () -> System.err.println("thread started"));
        t.setName("testThread");
        t.start();
        t.interrupt();
        t.join();
    }
}
