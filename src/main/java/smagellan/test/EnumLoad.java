package smagellan.test;

public class EnumLoad {
    public static void main(String[] args) {
        System.err.println("hello");
        System.err.println("" + T.T);

        System.err.println("" + C.i1);
    }
}


enum T{
    T;

    static {
        System.err.println("loading T");
    }
}

class C{
    public static final C i1 = new C("i1");
    public static final C i2 = new C("i2");
    public C(String nm){
        System.err.println("nm: " + nm);
    }
}