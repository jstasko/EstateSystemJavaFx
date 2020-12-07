package sk.stasko.core.hashing.extendingHashing.extendingHashingDirectory;

import sk.stasko.core.hashing.directory.DirectoryImpl;
import sk.stasko.core.hashing.extendingHashing.OverflowingHandler;
import sk.stasko.core.fileHandler.FileHandler;
import sk.stasko.core.hashing.extendingHashing.overflowingDirectory.node.OverflowingNodeImpl;
import sk.stasko.core.savableObject.SavableObject;
import sk.stasko.core.hashing.extendingHashing.extendingHashingDirectory.node.block.ExtendingBlockImpl;
import sk.stasko.core.hashing.extendingHashing.extendingHashingDirectory.node.DynamicDirectoryNodeImpl;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DynamicDirectoryImpl<T extends SavableObject<U>, U extends Comparable<U>>
        extends DirectoryImpl<ArrayList<DynamicDirectoryNodeImpl<T, U>>, Integer, DynamicDirectoryNodeImpl<T, U>, T, U>
        implements DynamicDirectory<T, U> {
    private int depthOfDirectory;

    public DynamicDirectoryImpl(int numberOfRecords, int sizeOfRecord, FileHandler<T> fileHandler) {
        super(new ArrayList<>((int)Math.pow(2, 1)));
        this.depthOfDirectory = 1;
        this.directory.add(new ExtendingBlockImpl<>(0, this.depthOfDirectory, numberOfRecords,fileHandler));
        this.directory.add(new ExtendingBlockImpl<>(numberOfRecords*sizeOfRecord, this.depthOfDirectory, numberOfRecords,fileHandler));
    }

    public DynamicDirectoryImpl(int depthOfDirectory, ArrayList<DynamicDirectoryNodeImpl<T, U>> list) {
        super(list);
        this.depthOfDirectory = depthOfDirectory;
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

    @Override
    public int startPositionOfLastAllocatedBlock() {
        return Collections.max(this.directory.stream().map(DynamicDirectoryNodeImpl::getStartPosition)
                .collect(Collectors.toList()));
    }

    private void createShrunkDirectory() {
        ArrayList<DynamicDirectoryNodeImpl<T, U>> list = new ArrayList<>(this.directory.size()/2);
        for (DynamicDirectoryNodeImpl<T, U> node: this.directory) {
            if (!list.contains(node)) {
                int range = Collections.frequency(this.directory, node) / 2;
                IntStream.range(0, Math.max(range, 1))
                        .forEach(i -> list.add(node));
            }
        }
        this.directory = list;
    }

    private void createExpandedDirectory() {
        ArrayList<DynamicDirectoryNodeImpl<T, U>> list = new ArrayList<>(this.directory.size()*2);
        this.directory.forEach(i -> IntStream.range(0, 2).forEach(x -> list.add(i)));
        this.directory = list;
    }

    @Override
    public OverflowingHandler<OverflowingNodeImpl<T, U>> findAncestor(OverflowingNodeImpl<T, U> node) {
        return this.directory
                .stream()
                .filter(i -> node.equals(i.getNextBlock()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public String getSettings() {
        List<String> items = new LinkedList<>();
        int index = 0;
        for (DynamicDirectoryNodeImpl<T, U> item: this.directory) {
            String next = item.getNextBlock() == null ? "-1" : String.valueOf(item.getNextBlock().getStartPosition());
            String att = index + ";"
                    + this.directory.indexOf(item) + ";"
                    + item.getStartPosition() + ";"
                    + item.getNumberOfRecords() + ";"
                    + next + ";"
                    + item.getDepthOfBlock();
            items.add(att);
            index++;
        }
        return String.join(";", items);
    }

    @Override
    public int indexOf(DynamicDirectoryNodeImpl<T, U> node) {
        return this.directory.indexOf(node);
    }

    @Override
    public void loadSettings(ArrayList<DynamicDirectoryNodeImpl<T, U>> dynamicDirectoryNodes) {
        this.directory = dynamicDirectoryNodes;
    }
}
