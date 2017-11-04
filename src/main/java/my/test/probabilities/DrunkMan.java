package my.test.probabilities;

import java.util.Random;

public class DrunkMan {
    public static final double PROBABILITY = 0.5;

    public static void main(String[] args) {
        int curDistance = 7;
        Random r = new Random();
        int stepCount = 0;
        while (curDistance > 0) {
            int distanceOffset = r.nextBoolean() ? 1 : -1;
            curDistance += distanceOffset;
            ++stepCount;
            System.err.println("distance: " + curDistance);
        }
        System.err.println("stepCount: " + stepCount);
    }
}
