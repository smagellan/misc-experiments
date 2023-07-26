package smagellan.test.classloaderlock.naive;


public class ClassloaderLock {
    public static void main(String[] args) throws InterruptedException {
        Thread thread = new Thread() {
            @Override
            public void run() {
                A.bInstance.hello();
            }
        };
        thread.start();
        new B().hello();
        thread.join();
    }
}

class A {
    static B bInstance = new B();
}

class B extends A {
    void hello() {
        System.out.println("Hello");
    }
}