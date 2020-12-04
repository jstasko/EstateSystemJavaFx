package sk.stasko.core.extendingHashing.directory;

import sk.stasko.core.extendingHashing.block.OverflowingHandler;
import sk.stasko.core.extendingHashing.overflowingFile.block.OverflowingBlock;
import sk.stasko.core.savableObject.SavableObject;
import sk.stasko.core.extendingHashing.node.DirectoryNodeImpl;

import java.io.IOException;
import java.util.List;

public interface Directory<T extends SavableObject<U>, U extends Comparable<U>> {
    int sizeOfDirectory();
    DirectoryNodeImpl<T, U> getOne(int index);
    int getDepthOfDirectory();
    boolean needToBeShrunk(int depth);
    void shrunkDirectory();
    void doubleDirectory();
    void setOne(int index, DirectoryNodeImpl<T, U> item);
    int startPositionOfLastAllocatedBlock();
    String toString(byte[] records, int sizeOfRecord);
    OverflowingHandler<OverflowingBlock<T, U>> findAncestor(OverflowingBlock<T, U> index);
    int indexOf(DirectoryNodeImpl<T, U> node);
}
