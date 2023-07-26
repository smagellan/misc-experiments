package smagellan.test;

public class Threads {

    public static void main(String[] args) throws InterruptedException {
        SyncObject obj = new SyncObject();
        synchronized (obj) {
            obj.waitFor(1);
        }
    }
}
