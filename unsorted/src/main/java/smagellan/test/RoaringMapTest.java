package smagellan.test;

import org.roaringbitmap.RoaringBitmap;

public class RoaringMapTest {
    public static void main(String[] args) {
        RoaringBitmap expected = RoaringBitmap.bitmapOf(100, 101, 102, 104, 105, 106);
        boolean applied = expected.runOptimize();
        System.err.println("was optimized: " + applied);
        System.err.println("has compression: " +  expected.hasRunCompression());
        System.err.println(expected.contains( 9));
        expected.add(9);
        System.err.println(expected.contains( 9));
        System.err.println(expected.contains(242));
        System.err.println(expected.contains(1));
    }
}
