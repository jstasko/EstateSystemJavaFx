package sk.stasko.core.extendingHashing.overflowingFile;

import sk.stasko.core.extendingHashing.block.OverflowingHandler;
import sk.stasko.core.extendingHashing.node.DirectoryNodeImpl;
import sk.stasko.core.extendingHashing.overflowingFile.block.OverflowingBlock;
import sk.stasko.core.savableObject.SavableObject;

import java.io.IOException;

public interface OverflowingFile<T extends SavableObject<U>, U extends Comparable<U>> {
    boolean add(OverflowingHandler<OverflowingBlock<T, U>> node, T data) throws IOException;
    T find(DirectoryNodeImpl<T, U> node, U key) throws IOException;
    OverflowingBlock<T, U> delete(DirectoryNodeImpl<T, U> node, U key) throws IOException;
    void reorder(OverflowingBlock<T, U> node) throws IOException;
    OverflowingBlock<T, U> findAncestor(OverflowingBlock<T, U> block);
    void addToBlankBlocks(OverflowingBlock<T, U> node);
    T getItemFromLastBlock(OverflowingHandler<OverflowingBlock<T, U>> node) throws IOException;
    void reorderBlankBlocks() throws IOException;
    String print() throws IOException;
    String printBlank();
}
