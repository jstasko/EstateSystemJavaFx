package sk.stasko.core.extendingHashing.overflowingFile.block;

import sk.stasko.core.extendingHashing.block.OverflowingHandler;
import sk.stasko.core.extendingHashing.node.FileNode;
import sk.stasko.core.fileHandler.FileHandler;
import sk.stasko.core.savableObject.SavableObject;

import java.util.List;

public class OverflowingBlock <T extends SavableObject<U>, U extends Comparable<U>> extends FileNode<T, U> implements OverflowingHandler<OverflowingBlock<T, U>> {
    private OverflowingBlock<T, U> nextBlock;
    public OverflowingBlock(int startPosition, int maxNumberOfRecords, FileHandler<T> fileHandler) {
        super(startPosition, fileHandler, maxNumberOfRecords);
        this.nextBlock = null;
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
}
