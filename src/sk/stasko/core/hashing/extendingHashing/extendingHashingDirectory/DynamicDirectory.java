package sk.stasko.core.hashing.extendingHashing.extendingHashingDirectory;

import sk.stasko.core.fileHandler.FileHandler;
import sk.stasko.core.hashing.directory.Directory;
import sk.stasko.core.hashing.extendingHashing.OverflowingHandler;
import sk.stasko.core.hashing.extendingHashing.extendingHashingDirectory.node.DynamicDirectoryNode;
import sk.stasko.core.hashing.extendingHashing.overflowingDirectory.node.OverflowingNodeImpl;
import sk.stasko.core.savableObject.SavableObject;
import sk.stasko.core.hashing.extendingHashing.extendingHashingDirectory.node.DynamicDirectoryNodeImpl;

import java.util.ArrayList;
import java.util.List;

public interface DynamicDirectory<T extends SavableObject<U>, U extends Comparable<U>>
        extends Directory<DynamicDirectoryNodeImpl<T, U>, Integer> {
    int getDepthOfDirectory();
    boolean needToBeShrunk(int depth);
    void shrunkDirectory();
    void doubleDirectory();
    int startPositionOfLastAllocatedBlock();
    int indexOf(DynamicDirectoryNodeImpl<T, U> node);
    OverflowingHandler<OverflowingNodeImpl<T, U>> findAncestor(OverflowingNodeImpl<T, U> index);
    String getSettings();
    void loadSettings(ArrayList<DynamicDirectoryNodeImpl<T,U>> nodes);
}
