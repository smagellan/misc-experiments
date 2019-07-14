package smagellan.test.stepik.c3089probability;

public class Task2 {
    public static void main(String[] args) {
        long sum = 0;
        for (int shot = 0; shot <= 10; ++shot) {
            sum += Math.pow(6, shot);
        }
        System.err.println(sum);
    }
}
