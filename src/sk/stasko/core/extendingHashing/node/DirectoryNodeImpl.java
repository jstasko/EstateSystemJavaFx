package sk.stasko.core.extendingHashing.node;

import sk.stasko.core.extendingHashing.block.OverflowingHandler;
import sk.stasko.core.fileHandler.FileHandler;
import sk.stasko.core.extendingHashing.overflowingFile.block.OverflowingBlock;
import sk.stasko.core.savableObject.SavableObject;

import java.util.LinkedList;
import java.util.List;

public abstract class DirectoryNodeImpl<T extends SavableObject<U>, U extends Comparable<U>> extends FileNode<T, U> implements DirectoryNode<T, U>, OverflowingHandler<OverflowingBlock<T, U>> {
    protected List<T> temporaryRecords;

    public DirectoryNodeImpl(int startPosition, int maxNumberOfRecords, FileHandler<T> fileHandler) {
        super(startPosition, fileHandler, maxNumberOfRecords);
        this.nextBlock = null;
        this.maxNumberOfRecords = maxNumberOfRecords;
        this.temporaryRecords = new LinkedList<>();

    }

    @Override
    public OverflowingBlock<T, U> getNextBlock() {
        return nextBlock;
    }

    @Override
    public void setNextBlock(OverflowingBlock<T, U> nextBlock) {
        this.nextBlock = nextBlock;
    }

    @Override
    public List<T> getTemporaryList() {
        return this.temporaryRecords;
    }

    @Override
    public void clearTemporaryList() {
        this.temporaryRecords = new LinkedList<>();
    }

    @Override
    public void addToTemporaryList(T item) {
        this.temporaryRecords.add(item);
    }

    @Override
    public void clearData() {
        this.numberOfCurrentRecord = 0;
        this.temporaryRecords = new LinkedList<>();
    }

    public int getActualNumberOfRecords(boolean isNeighbour) {
        OverflowingBlock<T, U> node = this.nextBlock;
        int counter = 0;
        while(node != null) {
            counter += node.getNumberOfRecords();
            node = node.getNextBlock();
        }
        return isNeighbour ? this.getNumberOfRecords() + counter : this.temporaryRecords.size() + counter;
    }
}
