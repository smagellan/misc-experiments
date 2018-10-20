package smagellan.test;

import org.apache.commons.collections4.trie.KeyAnalyzer;



/**
 * Created by vladimir on 7/29/16.
 */
public class ByteKeyAnalyzer extends KeyAnalyzer<UInt128V0> {
    private static final byte[] masks = createByteMasks();

    public static byte[] createByteMasks() {
        for (int i = 0; i < Byte.SIZE; ++i) {
            byte mask = (byte)(1 << i);
            masks[i] = mask;
        }
        return masks;
    }

    @Override
    public int bitsPerElement() {
        return 1;
    }

    @Override
    public int lengthInBits(UInt128V0 key) {
        return key.getUsedBits();
    }

    @Override
    public boolean isBitSet(UInt128V0 key, int bitIndex, int lengthInBits) {
        boolean result;
        if (key == null || bitIndex >= lengthInBits) {
            result = false;
        } else {
            assert(bitIndex < key.getUsedBits());
            byte bValue = 0;
            byte mask   = (byte)(1 << (bitIndex % Byte.SIZE));
            result      = (bValue & mask) != 0;
            throw new RuntimeException("not implemented");
        }
        return result;
    }

    @Override
    public int bitIndex(UInt128V0 key, int offsetInBits, int lengthInBits, UInt128V0 other, int otherOffsetInBits, int otherLengthInBits) {
        return 0;
    }

    @Override
    public boolean isPrefix(UInt128V0 prefix, int offsetInBits, int lengthInBits, UInt128V0 key) {
        return false;
    }
}
