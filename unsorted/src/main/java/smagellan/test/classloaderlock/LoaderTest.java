package smagellan.test.classloaderlock;

public class LoaderTest {
    public static void main(String[] args) {
        new Child();
    }
}


class Parent {
    static {
        System.err.println("loading Parent");
    }
}

class Child extends Parent{
    static {
        System.err.println("loading Child");
    }
}