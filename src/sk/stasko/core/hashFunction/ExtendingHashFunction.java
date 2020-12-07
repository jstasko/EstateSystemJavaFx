package sk.stasko.core.hashFunction;

public interface ExtendingHashFunction<T, U> {
    int getIndexFromItem(T item, int numberOfBits);
    int getIndexFromKey(U key, int numberOfBits);
    int getIndexFromItem(T item, int numberOfBits, int localDepth);
    int getPrefixFromIndex(int index, int depthOfDictionary, int depthOfBlock);
}
