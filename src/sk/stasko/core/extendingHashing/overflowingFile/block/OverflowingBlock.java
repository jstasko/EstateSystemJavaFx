package sk.stasko.core.extendingHashing.overflowingFile.block;

import sk.stasko.core.extendingHashing.block.OverflowingHandler;
import sk.stasko.core.extendingHashing.node.FileNode;
import sk.stasko.core.fileHandler.FileHandler;
import sk.stasko.core.savableObject.SavableObject;

import java.util.List;
import java.util.stream.IntStream;

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

    public String toString(List<T> records) {
        String result = "****     START BLOCK AT : " + this.getStartPosition() + "    **** \n" +
                "    *** Number of Records : " + this.getNumberOfRecords() + " *** \n";
        var help = new Object(){ String helper = ""; };
        IntStream
                .range(0, records.size())
                .forEach(r ->
                        help.helper = help.helper.concat(
                                "        +++++ RECORD KEY " +
                                        records.get(r).getKey().toString() +
                                        " +++++ \n"
                        )
                );
        IntStream
                .range(0, this.maxNumberOfRecords - records.size())
                .forEach(r -> help.helper = help.helper.concat("        ***** No Record ***** \n"));
        result = result.concat(help.helper);
        if (this.getNextBlock() != null) {
            result = result.concat("    *** BLOCK has overflow block at position " +
                    this.getNextBlock().getStartPosition() + " *** \n");
        } else {
            result = result.concat("    *** BLOCK has no overflow block *** \n");
        }
        result = result.concat("****     END OF BLOCK    **** \n");
        result = result.concat("------------------------------\n");
        result = result.concat("\n");
        return result;
    }
}
