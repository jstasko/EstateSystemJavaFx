package sk.stasko.core.hashing.extendingHashing.overflowingDirectory.node;

import sk.stasko.core.hashing.extendingHashing.OverflowingHandler;
import sk.stasko.core.hashing.extendingHashing.extendingHashingDirectory.node.NodeHandler;
import sk.stasko.core.hashing.node.DirectoryNode;
import sk.stasko.core.fileHandler.FileHandler;
import sk.stasko.core.savableObject.SavableObject;

import java.util.LinkedList;
import java.util.List;

public class OverflowingNodeImpl<T extends SavableObject<U>, U extends Comparable<U>> extends DirectoryNode<T, U> implements OverflowingHandler<OverflowingNodeImpl<T, U>> , NodeHandler<T> {
    private OverflowingNodeImpl<T, U> nextBlock;
    protected List<T> temporaryRecords;

    public OverflowingNodeImpl(int startPosition, int maxNumberOfRecords, FileHandler<T> fileHandler) {
        super(startPosition, fileHandler, maxNumberOfRecords);
        this.nextBlock = null;
        this.temporaryRecords = new LinkedList<>();
    }

    public OverflowingNodeImpl(FileHandler<T> fileHandler) {
        super(-1, fileHandler, -1);
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
    public void clearData() {
        this.numberOfCurrentRecord = 0;
    }


    @Override
    public String toString(byte[] records, int sizeOfRecord) {
        byte[] arrRecords = new byte[this.getNumberOfRecords()*sizeOfRecord];
        System.arraycopy(records, this.getStartPosition(), arrRecords, 0, arrRecords.length);
        List<T> convertedRecords = this.fileHandler.convert(arrRecords);
        String result = "****     START BLOCK AT : " + this.getStartPosition() + "    **** \n" +
                "    *** Number of Records : " + this.getNumberOfRecords() + " *** \n";
        result = result.concat(this.printBlocks(convertedRecords, this.nextBlock, this.numberOfCurrentRecord));
        return result;
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
}
