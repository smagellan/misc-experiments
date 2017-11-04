package my.test.classloaderlock.hardened;

import java.util.concurrent.CountDownLatch;

public class HardenedClassloaderLock {
    public static void main(String[] args) {
        Thread t = new ExternalThread();
        t.start();
        new ChildClass().hello();
    }
}

class ExternalThread extends Thread {
    @Override
    public void run(){
        ParentClass.childClassInstance.hello();
    }
}

class ParentClass {
    static {
        System.err.println("initializing " + ParentClass.class.getSimpleName() + "; " + Thread.currentThread().getName());
    }
    static ChildClass childClassInstance;
    static {
        childClassInstance = new ChildClass();
        Latches.countdownLatch();
        Latches.awaitLatch();
    }
}

class ChildClass extends ParentClass {
    static {
        System.err.println("initializing " + ChildClass.class.getSimpleName() + "; " + Thread.currentThread().getName());
        Latches.countdownLatch();
        Latches.awaitLatch();
    }

    void hello() {
        System.out.println("Hello");
    }
}


class Latches{

    private static CountDownLatch cdLatch = new CountDownLatch(2);

    static void countdownLatch() {
        System.err.println("countDown by " + Thread.currentThread().getName());
        cdLatch.countDown();
    }

    static void awaitLatch() {
        try {
            System.err.println("await by " + Thread.currentThread().getName());
            cdLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}