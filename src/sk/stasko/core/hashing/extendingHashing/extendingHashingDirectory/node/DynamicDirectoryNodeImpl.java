package sk.stasko.core.hashing.extendingHashing.extendingHashingDirectory.node;

import sk.stasko.core.hashing.extendingHashing.OverflowingHandler;
import sk.stasko.core.fileHandler.FileHandler;
import sk.stasko.core.hashing.extendingHashing.overflowingDirectory.node.OverflowingNodeImpl;
import sk.stasko.core.hashing.node.DirectoryNode;
import sk.stasko.core.savableObject.SavableObject;

import java.util.LinkedList;
import java.util.List;

public abstract class DynamicDirectoryNodeImpl<T extends SavableObject<U>, U extends Comparable<U>>
        extends DirectoryNode<T, U>
        implements DynamicDirectoryNode<T, U>, OverflowingHandler<OverflowingNodeImpl<T, U>> {
    protected List<T> temporaryRecords;

    public DynamicDirectoryNodeImpl(int startPosition, int maxNumberOfRecords, FileHandler<T> fileHandler) {
        super(startPosition, fileHandler, maxNumberOfRecords);
        this.nextBlock = null;
        this.temporaryRecords = new LinkedList<>();

    }

    @Override
    public OverflowingNodeImpl<T, U> getNextBlock() {
        return nextBlock;
    }

    @Override
    public void setNextBlock(OverflowingNodeImpl<T, U> nextBlock) {
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
        OverflowingNodeImpl<T, U> node = this.nextBlock;
        int counter = 0;
        while(node != null) {
            counter += node.getNumberOfRecords();
            node = node.getNextBlock();
        }
        return isNeighbour ? this.getNumberOfRecords() + counter : this.temporaryRecords.size() + counter;
    }
}
