package smagellan.test;

public class NaiveFibPrinter {
    public static void main(String[] args) {
        FibPrinter2 printer = new FibPrinter2();
        for (int i = 0; i < 7; ++i) {
            printer.doCycle();
        }
    }

    private static void fib1() {
        int a = 1;
        int b = 2;
        System.err.println("c: " + a);
        System.err.println("c: " + b);
        int i = 0;
        int j = 2;
        while (i < 10) {
            int c = a + b;
            a = b;
            b = c;
            ++j;
            if (c % 2 == 0) {
                System.err.println("c: " + c + "; j " + j);
                ++i;
            }
        }
    }
}


class FibPrinter2 {
    private int fib1 = 0;
    private int fib2 = 1;

    //0, 1, 1, 2, 3, 5, 8, 13, 21, 34, 55, 89, 144.
    public void doCycle() {
        System.err.println("fib1: " + fib1 + "; fib2: " + fib2);
        int oldFib2 = fib2;
        fib1 += fib2;
        fib2 = fib1 + oldFib2;
    }
}