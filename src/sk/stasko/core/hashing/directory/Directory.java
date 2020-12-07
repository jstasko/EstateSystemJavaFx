package sk.stasko.core.hashing.directory;

import sk.stasko.core.hashing.extendingHashing.extendingHashingDirectory.node.DynamicDirectoryNodeImpl;

public interface Directory<T, U> {
    T getOne(U key);
    int sizeOfDirectory();
    void setOne(U index, T item);
    int startPositionOfLastAllocatedBlock();
    String toString(byte[] records, int sizeOfRecord);
}
