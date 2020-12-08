package sk.stasko.core.hashing.extendingHashing.overflowingDirectory;

import sk.stasko.core.hashing.directory.DirectoryImpl;
import sk.stasko.core.hashing.extendingHashing.OverflowingHandler;
import sk.stasko.core.hashing.extendingHashing.extendingHashingDirectory.node.DynamicDirectoryNodeImpl;
import sk.stasko.core.fileHandler.FileHandler;
import sk.stasko.core.hashing.memory.MemoryManager;
import sk.stasko.core.hashing.extendingHashing.overflowingDirectory.memory.OverflowingManager;
import sk.stasko.core.hashing.extendingHashing.overflowingDirectory.node.OverflowingNodeImpl;
import sk.stasko.core.savableObject.SavableObject;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class OverflowingDirectoryImpl<T extends SavableObject<U>, U extends Comparable<U>>
        extends DirectoryImpl<ArrayList<OverflowingNodeImpl<T, U>>, Integer, OverflowingNodeImpl<T, U>, T, U>
        implements OverflowingDirectory<T, U> {

    private final FileHandler<T> fileHandler;
    private final int maxItemInBlock;
    private final int sizeOfRecord;
    private final MemoryManager<T, OverflowingNodeImpl<T, U>,U> memoryManager;

    public OverflowingDirectoryImpl(int maxItemInBlock, int sizeOfRecord, FileHandler<T> fileHandler) throws IOException {
        super(new ArrayList<>());
        fileHandler.newLengthOfFile(0);
        this.maxItemInBlock = maxItemInBlock;
        this.sizeOfRecord = sizeOfRecord;
        this.fileHandler = fileHandler;
        this.memoryManager = new OverflowingManager<>(fileHandler);
    }

    public OverflowingDirectoryImpl(
            int maxItemInBlock, int sizeOfRecord, FileHandler<T> fileHandler,
            ArrayList<OverflowingNodeImpl<T, U>> main, Queue<OverflowingNodeImpl<T, U>> blank) {
        super(main);
        this.maxItemInBlock = maxItemInBlock;
        this.sizeOfRecord = sizeOfRecord;
        this.fileHandler = fileHandler;
        this.memoryManager = new OverflowingManager<>(fileHandler, blank);
    }

    public void reorderBlankBlocks() throws IOException {
        int maxAllocatedMemory;
        if (this.directory.size() == 0) {
            maxAllocatedMemory = 0;
        } else {
            maxAllocatedMemory = this.startPositionOfLastAllocatedBlock() + maxItemInBlock*sizeOfRecord;
        }
        this.memoryManager.reorderBlankBlocks(maxAllocatedMemory);
    }

    @Override
    public boolean add(OverflowingHandler<OverflowingNodeImpl<T, U>> item, T data) throws IOException {
        OverflowingNodeImpl<T, U> block = this.findCorrectBlock(item);
        this.fileHandler.write(data,
                block.getStartPosition() + block.getNumberOfRecords() * data.getAllocatedMemory());
        block.setCurrentRecordsNumber(block.getNumberOfRecords() + 1);
        return true;
    }

    @Override
    public OverflowingNodeImpl<T, U> delete(DynamicDirectoryNodeImpl<T, U> node, U key) throws IOException {
        int index = this.directory.indexOf(node.getNextBlock());
        if (index <= -1) {
            return null;
        }
        while (index > -1) {
            OverflowingNodeImpl<T, U> foundedNode = this.directory.get(index);
            List<T> items = foundedNode.read();
            int sizeOfItems = items.size();
            items = items.stream()
                    .filter(i -> i.getKey().compareTo(key) != 0)
                    .collect(Collectors.toList());
            if (sizeOfItems > items.size()) {
                this.fileHandler.write(items, foundedNode.getStartPosition(), foundedNode.getNumberOfRecords());
                foundedNode.setCurrentRecordsNumber(items.size());
                return foundedNode;
            }
            index = this.directory.indexOf(foundedNode.getNextBlock());
        }
        return null;
    }

    @Override
    public T find(DynamicDirectoryNodeImpl<T, U> node, U key) throws IOException {
        int index = this.directory.indexOf(node.getNextBlock());
        if (index <= -1) {
            return null;
        }
        while (index > -1) {
            OverflowingNodeImpl<T, U> foundedNode = this.directory.get(index);
            List<T> items = foundedNode.read();
            for (T item: items) {
                if (item.getKey().compareTo(key) == 0) {
                    return item;
                }
            }
            index = this.directory.indexOf(foundedNode.getNextBlock());
        }
        return null;
    }

    @Override
    public void reorder(OverflowingNodeImpl<T, U> node) throws IOException {
        OverflowingNodeImpl<T, U> helpNode = node;
        int index = this.directory.indexOf(node.getNextBlock());
        List<T> items = new ArrayList<>();
        int numberOfRecords = node.getNumberOfRecords();
        while (index > -1) {
            helpNode = helpNode.getNextBlock();
            index = this.directory.indexOf(helpNode.getNextBlock());
            if (node.getNumberOfRecords() + helpNode.getNumberOfRecords() <= this.maxItemInBlock) {
                items.addAll(helpNode.read());
                helpNode.clearData();
                OverflowingNodeImpl<T, U> ancestorInOverflowing = this.findAncestor(helpNode);
                ancestorInOverflowing.setNextBlock(helpNode.getNextBlock());
                this.addToBlankBlocks(helpNode);
                node.setCurrentRecordsNumber(node.getNumberOfRecords() + items.size());
            }
        }

        if (items.size() > 0) {
            this.fileHandler.write(items,
                    node.getStartPosition() + numberOfRecords * this.sizeOfRecord, items.size());
        }
    }

    private OverflowingNodeImpl<T,U> findCorrectBlock(OverflowingHandler<OverflowingNodeImpl<T, U>> node) {
        int index = this.directory.indexOf(node.getNextBlock());
        OverflowingNodeImpl<T, U> foundedNode = null;
        while (index > -1) {
            foundedNode = this.directory.get(index);
            if (foundedNode.getNumberOfRecords() < this.maxItemInBlock) {
                return foundedNode;
            }
            index = this.directory.indexOf(foundedNode.getNextBlock());
        }
        return this.addBlock(Objects.requireNonNullElse(foundedNode, node));
    }

    private OverflowingNodeImpl<T, U> addBlock(OverflowingHandler<OverflowingNodeImpl<T, U>> node) {
        OverflowingNodeImpl<T, U> block;
        if (this.memoryManager.getSize() == 0) {
            int startPosition = 0;
            if (this.directory.size() != 0) {
                startPosition = startPositionOfLastAllocatedBlock() + this.sizeOfRecord * this.maxItemInBlock;
            }
            block = new OverflowingNodeImpl<>(startPosition, this.maxItemInBlock, fileHandler);
        } else {
            block = this.memoryManager.getBlock(0);
        }
        this.directory.add(block);
        node.setNextBlock(block);
        return block;
    }
    @Override
    public OverflowingNodeImpl<T, U> findAncestor(OverflowingNodeImpl<T, U> block) {
        int index = this.directory.indexOf(block);
        return this.directory
                .stream()
                .filter(i -> this.directory.indexOf(i.getNextBlock()) == index)
                .findFirst()
                .orElse(null);
    }

    @Override
    public void addToBlankBlocks(OverflowingNodeImpl<T, U> node) {
        this.memoryManager.addToDeallocatedBlock(node);
        this.directory.remove(node);
    }

    @Override
    public List<T> reorderFromMain(OverflowingHandler<OverflowingNodeImpl<T, U>> node, int numberOfItems, int maxItems) throws IOException {
        int index = this.directory.indexOf(node.getNextBlock());
        OverflowingNodeImpl<T, U> helpNode;
        List<T> items = new ArrayList<>();
        int currentRecords = numberOfItems;
        while (index > -1) {
            helpNode = this.directory.get(index);
            index = this.directory.indexOf(helpNode.getNextBlock());
            if (currentRecords + helpNode.getNumberOfRecords() <= maxItems) {
                items.addAll(helpNode.read());
                helpNode.clearData();
                OverflowingNodeImpl<T, U> ancestorInOverflowing = this.findAncestor(helpNode);
                if (ancestorInOverflowing == null) {
                    node.setNextBlock(helpNode);
                } else {
                    ancestorInOverflowing.setNextBlock(helpNode.getNextBlock());
                }
                this.addToBlankBlocks(helpNode);
                currentRecords += items.size();
            }
        }
        return items;
    }

    @Override
    public String print() throws IOException {
        if (this.directory.size() == 0) {
            return "No overflow blocks";
        }
        byte[] bytes = this.fileHandler.readBlockByByte(0, this.startPositionOfLastAllocatedBlock() + this.maxItemInBlock*this.sizeOfRecord);
        return this.toString(bytes, this.sizeOfRecord);
    }

    public String printBlank() {
        return this.memoryManager.printOfBlocks();
    }

    @Override
    public String getSettings() {
        String memoryManager = this.memoryManager.getSettings() + "\n";
        String directory = this.maxItemInBlock + "\n";
        List<String> items = new LinkedList<>();
        for (OverflowingNodeImpl<T, U> item: this.directory) {
            String att = + item.getStartPosition() + ";"
                    + item.getNumberOfRecords() + ";"
                    + this.directory.indexOf(item.getNextBlock());
            items.add(att);
        }

        return memoryManager + directory + String.join(";", items);
    }

    @Override
    public OverflowingNodeImpl<T, U> getOneByAddress(int address) {
        return this.directory
                .stream()
                .filter(i -> i.getStartPosition() == address)
                .findFirst()
                .orElse(null);
    }
}
