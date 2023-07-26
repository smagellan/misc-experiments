package smagellan.test;

public class NaiveFibPrinter {
    public static void main(String[] args) {
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
