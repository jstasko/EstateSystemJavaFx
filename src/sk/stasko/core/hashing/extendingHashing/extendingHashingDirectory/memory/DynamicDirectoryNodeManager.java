package sk.stasko.core.hashing.extendingHashing.extendingHashingDirectory.memory;

import sk.stasko.core.hashing.extendingHashing.extendingHashingDirectory.node.DynamicDirectoryNodeImpl;
import sk.stasko.core.fileHandler.FileHandler;
import sk.stasko.core.hashing.extendingHashing.extendingHashingDirectory.node.block.ExtendingBlockImpl;
import sk.stasko.core.hashing.memory.MemoryManager;
import sk.stasko.core.savableObject.SavableObject;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

public class DynamicDirectoryNodeManager<T extends SavableObject<U>, U extends Comparable<U>>
        extends MemoryManager<T, DynamicDirectoryNodeImpl<T, U>,U> {

    public DynamicDirectoryNodeManager(FileHandler<T> fileHandler) {
        super(fileHandler);
    }

    public DynamicDirectoryNodeManager(FileHandler<T> fileHandler, Queue<DynamicDirectoryNodeImpl<T, U>> queue) {
        super(fileHandler, queue);
    }

    @Override
    public DynamicDirectoryNodeImpl<T, U> getBlock(int depth) {
        DynamicDirectoryNodeImpl<T, U> removed = this.listOfBlocks.remove();
        removed.setBlockDepth(depth);
        return removed;
    }

    @Override
    public int addToDeallocatedBlock(DynamicDirectoryNodeImpl<T, U> directoryNode) {
        int depth = directoryNode.getDepthOfBlock();
        directoryNode.clearData();
        directoryNode.setBlockDepth(-1);
        directoryNode.setNextBlock(null);
        this.listOfBlocks.add(directoryNode);
        return depth;
    }
}