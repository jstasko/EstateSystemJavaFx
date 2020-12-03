package sk.stasko.core.extendingHashing.block;

import sk.stasko.core.extendingHashing.node.DirectoryNodeImpl;
import sk.stasko.core.fileHandler.FileHandler;
import sk.stasko.core.savableObject.SavableObject;

import java.util.List;
import java.util.stream.IntStream;

public class ExtendingBlockImpl<T extends SavableObject<U>, U extends Comparable<U>> extends DirectoryNodeImpl<T, U> {
    private int depthOfBlock;

    public ExtendingBlockImpl(int startPosition, int depthOfBlock, int maxRecord, FileHandler<T> fileHandler) {
        super(startPosition, maxRecord, fileHandler);
        this.depthOfBlock = depthOfBlock;
        this.numberOfCurrentRecord = 0;
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
    public String toString(List<T> records) {
        String result = "****     START BLOCK AT : " + this.getStartPosition() + "    **** \n" +
                "    *** Depth of block : " + this.getDepthOfBlock() + " *** \n" +
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
