package my.test.primitives;

import java.nio.ByteBuffer;
import java.util.StringJoiner;

public class PrimitiveUtils {
    private PrimitiveUtils() {
    }

    public static String longToBytesString(UInt128 src) {
        byte[] bytes = ByteBuffer
                .allocate(Long.SIZE * 2 / Byte.SIZE)
                .putLong(src.getHi()).putLong(src.getLo())
                .array();
        StringJoiner joiner = new StringJoiner(":");
        //2 octects = 4 chars
        StringBuilder octetPairBuilder  = new StringBuilder(4);
        for (int ci = 0; ci < bytes.length; ci += 2) {
            //reset StringBuilder before reuse
            octetPairBuilder.setLength(0);
            String c0 = Integer.toHexString(Byte.toUnsignedInt(bytes[ci]));
            octetPairBuilder.append(c0);
            String c1 = Integer.toHexString(Byte.toUnsignedInt(bytes[ci + 1]));
            octetPairBuilder.append(c1);
            joiner.add(octetPairBuilder);
        }
        return joiner.toString();
    }
}
