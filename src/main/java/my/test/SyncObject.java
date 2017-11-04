package my.test;

public class SyncObject {
    public void waitFor(long milliseconds) throws InterruptedException {
        this.wait(milliseconds);
    }
}
