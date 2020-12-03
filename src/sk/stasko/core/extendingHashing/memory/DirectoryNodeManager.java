package sk.stasko.core.extendingHashing.memory;

import sk.stasko.core.extendingHashing.node.DirectoryNodeImpl;
import sk.stasko.core.fileHandler.FileHandler;
import sk.stasko.core.savableObject.SavableObject;

public class DirectoryNodeManager<T extends SavableObject<U>, U extends Comparable<U>> extends MemoryManager<T, DirectoryNodeImpl<T, U> ,U> {

    public DirectoryNodeManager(FileHandler<T> fileHandler) {
        super(fileHandler);
    }

    @Override
    public DirectoryNodeImpl<T, U> getBlock(int depth) {
        DirectoryNodeImpl<T, U> removed = this.listOfBlocks.remove();
        removed.setBlockDepth(depth);
        return removed;
    }

    @Override
    public int addToDeallocatedBlock(DirectoryNodeImpl<T, U> directoryNode) {
        int depth = directoryNode.getDepthOfBlock();
        directoryNode.clearData();
        directoryNode.setBlockDepth(-1);
        directoryNode.setNextBlock(null);
        this.listOfBlocks.add(directoryNode);
        return depth;
    }
}
