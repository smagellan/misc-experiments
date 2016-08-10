import java.util.Objects;

/**
 * Created by vladimir on 7/29/16.
 */
public class UInt128V0 implements Comparable<UInt128V0>{
    private final long hiBits;
    private final long loBits;
    private final int usedBits;

    public UInt128V0(long hiBits, long loBits, int usedBits) {
        this.hiBits = hiBits;
        this.loBits = loBits;
        this.usedBits = usedBits;
    }

    public long getHiBits() {
        return hiBits;
    }

    public long getLoBits() {
        return loBits;
    }

    public int getUsedBits() {
        return usedBits;
    }

    public boolean getBit(int bitIdx) {
        assert(bitIdx < usedBits);
        final long val;
        final long idx;
        if (bitIdx <= 15) {
            val = loBits;
            idx = bitIdx - 16;
        } else {
            val = hiBits;
            idx = bitIdx;
        }
        throw new IllegalStateException("not implemented yet");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UInt128V0 uInt128V0 = (UInt128V0) o;
        return hiBits == uInt128V0.hiBits && loBits == uInt128V0.loBits && usedBits == uInt128V0.usedBits;
    }

    @Override
    public int hashCode() {
        return Objects.hash(hiBits, loBits, usedBits);
    }

    @Override
    public int compareTo(UInt128V0 o) {
        int ret = Long.compareUnsigned(hiBits, o.hiBits);
        return ret == 0 ? Long.compareUnsigned(loBits, o.loBits) : ret;
    }
}
