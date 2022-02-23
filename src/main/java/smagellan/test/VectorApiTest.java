package smagellan.test;

import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorSpecies;

public class VectorApiTest {
    public static void main(String[] args) {
    }

    static VectorSpecies<Float> SPECIES = FloatVector.SPECIES_256;
    static void doTest(float[] a, float[] b, float[] c) {
        int i = 0;
        int len = a.length & ~(SPECIES.length() - 1);
        for (; i < len; i+= SPECIES.length()) {
            FloatVector va = FloatVector.fromArray(SPECIES, a, i);
            FloatVector vb = FloatVector.fromArray(SPECIES, b, i);
            FloatVector vc = va.mul(va)
                    .add(vb.mul(vb))
                    .neg();
            vc.intoArray(c, i);
        }
        for (; i < a.length; ++i) {
            c[i] = (a[i] * a[i] + b[i] * b[i]) * -1f;
        }
    }
}
