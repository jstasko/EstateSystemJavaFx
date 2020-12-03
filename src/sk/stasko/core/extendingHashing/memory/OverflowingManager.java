package sk.stasko.core.extendingHashing.memory;

import sk.stasko.core.fileHandler.FileHandler;
import sk.stasko.core.extendingHashing.overflowingFile.block.OverflowingBlock;
import sk.stasko.core.savableObject.SavableObject;

public class OverflowingManager<T extends SavableObject<U>, U extends Comparable<U>> extends MemoryManager<T, OverflowingBlock<T, U>, U> {
    public OverflowingManager(FileHandler<T> fileHandler) {
        super(fileHandler);
    }

    @Override
    public OverflowingBlock<T, U> getBlock(int depth) {
        return this.listOfBlocks.remove();
    }

    @Override
    public int addToDeallocatedBlock(OverflowingBlock<T, U> node) {
        node.clearData();
        node.setNextBlock(null);
        this.listOfBlocks.add(node);
        return 0;
    }
}
