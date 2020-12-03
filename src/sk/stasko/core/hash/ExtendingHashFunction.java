package sk.stasko.core.hash;

import sk.stasko.core.converter.ByteConverter;

import java.util.BitSet;

public interface ExtendingHashFunction<T, U> {
    int getIndexFromItem(T item, int numberOfBits);
    int getIndexFromKey(U key, int numberOfBits);
    int getIndexFromItem(T item, int numberOfBits, int localDepth);
    int getPrefixFromIndex(int index, int depthOfDictionary, int depthOfBlock);
}
