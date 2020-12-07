package sk.stasko.core.hashFunction;

import sk.stasko.core.converter.ByteConverter;
import sk.stasko.core.savableObject.SavableObject;

import java.util.BitSet;

public abstract class AbstractHash<T, U> implements ExtendingHashFunction<T, U> {
    protected BitSet getCorrectBitSet(byte[] hash, int numberOfBits) {
        BitSet bitSet = BitSet.valueOf(this.convertForBitSet(hash));
        BitSet helpBitSet = bitSet.get(0, numberOfBits);
        return this.flipBitSet(helpBitSet, numberOfBits);
    }
    protected int getIndex(byte[] hash, int numberOfBits) {
        BitSet bitSet = getCorrectBitSet(hash, numberOfBits);
        return ByteConverter.bytesIntoInt(this.convertForIntByteBuffer(bitSet), 0, Integer.BYTES);
    }

    protected BitSet getCorrectBitSet(byte[] hash, int numberOfBits, int localDepth) {
        BitSet bitSet = BitSet.valueOf(this.convertForBitSet(hash));
        BitSet helpBitSet = bitSet.get(0, numberOfBits);
        return this.flipBitSet(helpBitSet.get(0, localDepth), numberOfBits);
    }

    protected int getIndex(byte[] hash, int numberOfBits, int localDepth) {
        BitSet bitSet = getCorrectBitSet(hash, numberOfBits, localDepth);
        return ByteConverter.bytesIntoInt(this.convertForIntByteBuffer(bitSet), 0, Integer.BYTES);
    }

    protected BitSet flipBitSet(BitSet bitSet, int numberOfBits) {
        BitSet help = new BitSet();
        int x = numberOfBits - 1;
        for (int i = 0; i < numberOfBits; i++) {
            help.set(x,bitSet.get(i));
            x--;
        }
        return help;
    }

    protected byte[] convertForBitSet(byte[] b) {
        byte[] help = new byte[b.length];
        int x = 0;
        for (int i = b.length - 1; i >= 0; i--) {
            help[x] = b[i];
            x++;
        }
        return help;
    }

    public byte[] convertForIntByteBuffer(BitSet bitSet) {
        byte[] help2 = new byte[Integer.BYTES];
        int y = help2.length - 1;
        for (byte b : bitSet.toByteArray()) {
            help2[y] = b;
            y--;
        }
        return help2;
    }

    protected abstract int hash(SavableObject<U> item);
    protected abstract int hash(U key);
}
