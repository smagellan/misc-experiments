package smagellan.test;

public class Singleton {
    public static void main(String[] args) {
        System.err.println("about to access singleton");
        System.err.println("" + SingletonHolder.INSTANCE);
    }
}
