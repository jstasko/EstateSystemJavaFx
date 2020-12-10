package sk.stasko.core.hashing.extendingHashing.overflowingDirectory;

import sk.stasko.core.hashing.directory.Directory;
import sk.stasko.core.hashing.extendingHashing.OverflowingHandler;
import sk.stasko.core.hashing.extendingHashing.extendingHashingDirectory.node.DynamicDirectoryNodeImpl;
import sk.stasko.core.hashing.extendingHashing.overflowingDirectory.node.OverflowingNodeImpl;
import sk.stasko.core.savableObject.SavableObject;

import java.io.IOException;
import java.util.List;

public interface OverflowingDirectory<T extends SavableObject<U>, U extends Comparable<U>>
        extends Directory<OverflowingNodeImpl<T, U>, Integer> {
    boolean add(OverflowingHandler<OverflowingNodeImpl<T, U>> node, T data) throws IOException;
    T find(OverflowingHandler<OverflowingNodeImpl<T, U>> node, U key) throws IOException;
    OverflowingNodeImpl<T, U> delete(DynamicDirectoryNodeImpl<T, U> node, U key) throws IOException;
    List<T> reorder(OverflowingNodeImpl<T, U> node, OverflowingHandler<OverflowingNodeImpl<T, U>> mainNode, int numberOfItems, int maxInMain) throws IOException;
    List<T> reorderFromMain(OverflowingHandler<OverflowingNodeImpl<T, U>> node, int numberOfRecords, int maxItems) throws IOException;
    String print() throws IOException;
    String printBlank();
    String getSettings();
    void reorderBlankBlocks() throws IOException;
    void addToBlankBlocks(OverflowingNodeImpl<T, U> node);
    OverflowingNodeImpl<T, U> findAncestor(OverflowingNodeImpl<T, U> block);
}
