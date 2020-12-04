package sk.stasko.core.extendingHashing.node;

import sk.stasko.core.extendingHashing.block.Block;
import sk.stasko.core.extendingHashing.block.OverflowingHandler;
import sk.stasko.core.extendingHashing.overflowingFile.block.OverflowingBlock;
import sk.stasko.core.fileHandler.FileHandler;
import sk.stasko.core.savableObject.SavableObject;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

public abstract class FileNode<T extends SavableObject<U>, U extends Comparable<U>> implements Block<T> {
    private final int startPosition;
    protected FileHandler<T> fileHandler;
    protected int numberOfCurrentRecord;
    protected int maxNumberOfRecords;
    protected OverflowingBlock<T, U> nextBlock;

    public FileNode(int startPosition, FileHandler<T> fileHandler, int maxNumberOfRecords) {
        this.startPosition = startPosition;
        this.fileHandler = fileHandler;
        this.maxNumberOfRecords = maxNumberOfRecords;
        this.numberOfCurrentRecord = 0;
    }

    @Override
    public boolean isFull() {
        return this.numberOfCurrentRecord >= this.maxNumberOfRecords;
    }

    @Override
    public void setCurrentRecordsNumber(int number) {
        this.numberOfCurrentRecord = number;
    }

    @Override
    public int getNumberOfRecords() {
        return this.numberOfCurrentRecord;
    }

    @Override
    public List<T> read() throws IOException {
        if (this.numberOfCurrentRecord == 0 || this.numberOfCurrentRecord > this.maxNumberOfRecords) {
            return new LinkedList<>();
        }
        return this.fileHandler.readBlockByItem(this.getStartPosition(), this.numberOfCurrentRecord);
    }

    @Override
    public String toString() {
        return "*** BLOCK STARTS AT " + this.getStartPosition() + " *** \n";
    }

    public int getStartPosition() {
        return startPosition;
    }

    protected String printBlocks(List<T> records, OverflowingBlock<T, U> nextBlock, int numberOfRecord) {
        var help = new Object(){ String helper = ""; };
        IntStream
                .range(0, numberOfRecord)
                .forEach(r ->
                        help.helper = help.helper.concat(
                                "        +++++ RECORD KEY " +
                                        records.get(r).getKey().toString() +
                                        " +++++ \n"
                        )
                );
        IntStream
                .range(0, this.maxNumberOfRecords - numberOfRecord)
                .forEach(r -> help.helper = help.helper.concat("        ***** No Record ***** \n"));
        if (nextBlock != null) {
            help.helper = help.helper.concat("    *** BLOCK has overflow block at position " +
                    nextBlock.getStartPosition() + " *** \n");
        } else {
            help.helper = help.helper.concat("    *** BLOCK has no overflow block *** \n");
        }
        help.helper = help.helper.concat("****     END OF BLOCK    **** \n");
        help.helper = help.helper.concat("------------------------------\n");
        help.helper = help.helper.concat("\n");
        return help.helper;
    }
}
