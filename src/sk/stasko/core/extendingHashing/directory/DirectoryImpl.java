package sk.stasko.core.extendingHashing.directory;

import sk.stasko.core.extendingHashing.block.OverflowingHandler;
import sk.stasko.core.fileHandler.FileHandler;
import sk.stasko.core.extendingHashing.overflowingFile.block.OverflowingBlock;
import sk.stasko.core.savableObject.SavableObject;
import sk.stasko.core.extendingHashing.block.ExtendingBlockImpl;
import sk.stasko.core.extendingHashing.node.DirectoryNodeImpl;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DirectoryImpl<T extends SavableObject<U>, U extends Comparable<U>> implements Directory<T, U> {
    private ArrayList<DirectoryNodeImpl<T, U>> directory;
    private int depthOfDirectory;

    public DirectoryImpl(int numberOfRecords, int sizeOfRecord, FileHandler<T> fileHandler) {
        this.depthOfDirectory = 1;
        this.directory = new ArrayList<>((int)Math.pow(2, this.depthOfDirectory));
        this.directory.add(new ExtendingBlockImpl<>(0, this.depthOfDirectory, numberOfRecords,fileHandler));
        this.directory.add(new ExtendingBlockImpl<>(numberOfRecords*sizeOfRecord, this.depthOfDirectory, numberOfRecords,fileHandler));
    }

    public int sizeOfDirectory() {
        return this.directory.size();
    }

    public DirectoryNodeImpl<T, U> getOne(int index) {
        if (index < this.directory.size()) {
            return this.directory.get(index);
        }
        return null;
    }

    public int getDepthOfDirectory() {
        return depthOfDirectory;
    }

    public boolean needToBeShrunk(int depth) {
        return this.directory.stream().noneMatch((item) -> item.getDepthOfBlock() >= depth);
    }

    public void shrunkDirectory() {
        this.createShrunkDirectory();
        this.depthOfDirectory--;
    }

    public void doubleDirectory() {
        this.createExpandedDirectory();
        this.depthOfDirectory++;
    }

    public void setOne(int index, DirectoryNodeImpl<T, U> item)  {
        this.directory.set(index, item);
    }

    @Override
    public int startPositionOfLastAllocatedBlock() {
        return Collections.max(this.directory.stream().map(DirectoryNodeImpl::getStartPosition)
                .collect(Collectors.toList()));
    }

    private void createShrunkDirectory() {
        ArrayList<DirectoryNodeImpl<T, U>> list = new ArrayList<>(this.directory.size()/2);
        for (DirectoryNodeImpl<T, U> node: this.directory) {
            if (!list.contains(node)) {
                int range = Collections.frequency(this.directory, node) / 2;
                IntStream.range(0, Math.max(range, 1))
                        .forEach(i -> list.add(node));
            }
        }
        this.directory = list;
    }

    private void createExpandedDirectory() {
        ArrayList<DirectoryNodeImpl<T, U>> list = new ArrayList<>(this.directory.size()*2);
        this.directory.forEach(i -> IntStream.range(0, 2).forEach(x -> list.add(i)));
        this.directory = list;
    }

    @Override
    public String toString(List<T> records) throws IOException {
        int sizeOfRecord = records.get(0).getAllocatedMemory();
        List<DirectoryNodeImpl<T, U>> helper = this.directory
                .stream()
                .sorted(Comparator.comparingInt(DirectoryNodeImpl::getStartPosition))
                .distinct()
                .collect(Collectors.toList());
        String concatString = "";
        var index = new Object() {int index = 0;};
        for(DirectoryNodeImpl<T, U> i: helper) {
            index.index = i.getStartPosition() / sizeOfRecord;
            List<T> help = i.read();
            concatString = concatString.concat(i.toString(help));
        }
        return concatString;
    }

    @Override
    public boolean contains(DirectoryNodeImpl<T, U> node) {
        return this.directory.contains(node);
    }

    @Override
    public OverflowingHandler<OverflowingBlock<T, U>> findAncestor(OverflowingBlock<T, U> node) {
        return this.directory
                .stream()
                .filter(i -> node.equals(i.getNextBlock()))
                .findFirst()
                .orElse(null);
    }
}
