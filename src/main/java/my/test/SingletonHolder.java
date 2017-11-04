package my.test;

public class SingletonHolder {
    public static final Object INSTANCE = createInstance();

    private static Object createInstance() {
        System.err.println("creating instance");
        return new Object();
    }
}
