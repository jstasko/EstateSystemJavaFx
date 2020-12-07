package sk.stasko.core.hashing.extendingHashing.extendingHashingDirectory.node.block;

import sk.stasko.core.hashing.extendingHashing.extendingHashingDirectory.node.DynamicDirectoryNodeImpl;
import sk.stasko.core.fileHandler.FileHandler;
import sk.stasko.core.savableObject.SavableObject;

import java.util.List;

public class ExtendingBlockImpl<T extends SavableObject<U>, U extends Comparable<U>> extends DynamicDirectoryNodeImpl<T, U> {
    private int depthOfBlock;

    public ExtendingBlockImpl(int startPosition, int depthOfBlock, int maxRecord, FileHandler<T> fileHandler) {
        super(startPosition, maxRecord, fileHandler);
        this.depthOfBlock = depthOfBlock;
        this.numberOfCurrentRecord = 0;
    }

    public ExtendingBlockImpl(FileHandler<T> fileHandler, int maxRecord) {
        super(-1, maxRecord, fileHandler);
        this.depthOfBlock = -1;
        this.numberOfCurrentRecord = -1;
    }

    @Override
    public int getDepthOfBlock() {
        return this.depthOfBlock;
    }

    @Override
    public void setBlockDepth(int newDepth) {
        this.depthOfBlock = newDepth;
    }

    @Override
    public void incrementDepthBlock() {
        this.depthOfBlock++;
    }

    @Override
    public void decreaseDepthBlock() {
        this.depthOfBlock--;
    }

    @Override
    public String toString(byte[] records, int sizeOfRecord) {
        byte[] arrRecords = new byte[this.maxNumberOfRecords*sizeOfRecord];
        System.arraycopy(records, this.getStartPosition(), arrRecords, 0, arrRecords.length);
        List<T> convertedRecords = this.fileHandler.convert(arrRecords);
        String result = "****     START BLOCK AT : " + this.getStartPosition() + "    **** \n" +
                "    *** Depth of block : " + this.getDepthOfBlock() + " *** \n" +
                "    *** Number of Records : " + this.getNumberOfRecords() + " *** \n";
        result = result.concat(this.printBlocks(convertedRecords, this.nextBlock, this.numberOfCurrentRecord));
        return result;
    }
}
