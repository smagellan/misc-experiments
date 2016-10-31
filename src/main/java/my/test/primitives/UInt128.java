package my.test.primitives;

import java.io.Serializable;
import java.util.Objects;

public class UInt128 implements Serializable, Comparable<UInt128> {
    private final long hi;
    private final long lo;

    public UInt128(long hi, long lo) {
        this.hi = hi;
        this.lo = lo;
    }

    public long getHi() {
        return hi;
    }

    public long getLo() {
        return lo;
    }

    public UInt128 xor(UInt128 rhs) {
        long xoredHi = hi ^ rhs.hi;
        long xoredLo = lo ^ rhs.lo;
        return new UInt128(xoredHi, xoredLo);
    }

    public int numberOfLeadingZeros() {
        int hiLZeros = Long.numberOfLeadingZeros(hi);
        return hiLZeros == Long.SIZE ? hiLZeros + Long.numberOfLeadingZeros(lo) : hiLZeros;
    }

    public int numberOfTrailingZeros(){
        int loTZeros = Long.numberOfTrailingZeros(lo);
        return loTZeros == Long.SIZE ? loTZeros + Long.numberOfTrailingZeros(hi) : loTZeros;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UInt128 uInt128 = (UInt128) o;
        return hi == uInt128.hi && lo == uInt128.lo;
    }

    @Override
    public int hashCode() {
        return Objects.hash(hi, lo);
    }

    @Override
    public int compareTo(UInt128 o) {
        int ret = Long.compareUnsigned(hi, o.hi);
        return ret == 0 ? Long.compareUnsigned(lo, o.lo) : ret;
    }
}
