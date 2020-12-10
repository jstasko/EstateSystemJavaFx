package sk.stasko.core.hashing.extendingHashing.overflowingDirectory.memory;

import sk.stasko.core.fileHandler.FileHandler;
import sk.stasko.core.hashing.extendingHashing.overflowingDirectory.node.OverflowingNodeImpl;
import sk.stasko.core.hashing.memory.MemoryManager;
import sk.stasko.core.savableObject.SavableObject;

import java.util.Queue;

public class OverflowingManager<T extends SavableObject<U>, U extends Comparable<U>> extends MemoryManager<T, OverflowingNodeImpl<T, U>, U> {
    public OverflowingManager(FileHandler<T> fileHandler) {
        super(fileHandler);
    }

    public OverflowingManager(FileHandler<T> fileHandler, Queue<OverflowingNodeImpl<T, U>> queue) {
        super(fileHandler, queue);
    }

    @Override
    public OverflowingNodeImpl<T, U> getBlock(int depth) {
        return this.listOfBlocks.remove();
    }

    @Override
    public int addToDeallocatedBlock(OverflowingNodeImpl<T, U> node) {
        node.clearData();
        node.setNextBlock(null);
        this.listOfBlocks.add(node);
        return 0;
    }
}
