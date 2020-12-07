package sk.stasko.core.hashing.extendingHashing;

import sk.stasko.core.savableObject.SavableObject;

import java.io.IOException;

public interface Hashing<T extends SavableObject<U>, U extends Comparable<U>> {
    void add(T item) throws IOException;
    boolean delete(T item) throws IOException;
    T find(U key) throws IOException;
    String printBlocks(int spaceInOneBlock) throws IOException;
    String printBlankBlock();
    void saveSettings() throws IOException;
    String getOverflowPart() throws IOException;
    String getOverflowPartBlank();
}
