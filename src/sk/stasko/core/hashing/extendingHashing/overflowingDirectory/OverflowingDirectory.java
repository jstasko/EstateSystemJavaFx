package sk.stasko.core.hashing.extendingHashing.overflowingDirectory;

import sk.stasko.core.hashing.directory.Directory;
import sk.stasko.core.hashing.extendingHashing.OverflowingHandler;
import sk.stasko.core.hashing.extendingHashing.extendingHashingDirectory.node.DynamicDirectoryNodeImpl;
import sk.stasko.core.hashing.extendingHashing.overflowingDirectory.node.OverflowingNodeImpl;
import sk.stasko.core.savableObject.SavableObject;

import java.io.IOException;

public interface OverflowingDirectory<T extends SavableObject<U>, U extends Comparable<U>>
        extends Directory<OverflowingNodeImpl<T, U>, Integer> {
    boolean add(OverflowingHandler<OverflowingNodeImpl<T, U>> node, T data) throws IOException;
    T find(DynamicDirectoryNodeImpl<T, U> node, U key) throws IOException;
    OverflowingNodeImpl<T, U> delete(DynamicDirectoryNodeImpl<T, U> node, U key) throws IOException;
    void reorder(OverflowingNodeImpl<T, U> node) throws IOException;
    OverflowingNodeImpl<T, U> findAncestor(OverflowingNodeImpl<T, U> block);
    void addToBlankBlocks(OverflowingNodeImpl<T, U> node);
    T getItemFromLastBlock(OverflowingHandler<OverflowingNodeImpl<T, U>> node) throws IOException;
    void reorderBlankBlocks() throws IOException;
    String print() throws IOException;
    String printBlank();
    String getSettings();
    OverflowingNodeImpl<T, U> getOneByAddress(int address);
}
