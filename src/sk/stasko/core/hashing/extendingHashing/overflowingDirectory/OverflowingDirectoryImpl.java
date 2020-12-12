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
                foundedNode.clearData();
                items.forEach(foundedNode::addToTemporaryList);
                return foundedNode;
            }
            index = this.directory.indexOf(foundedNode.getNextBlock());
        }
        return null;
    }

    @Override
    public T find(OverflowingHandler<OverflowingNodeImpl<T, U>> node, U key) throws IOException {
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
    public OverflowingNodeImpl<T, U> findAncestor(OverflowingNodeImpl<T, U> block) {
        int index = this.directory.indexOf(block);
        return this.directory
                .stream()
                .filter(i -> this.directory.indexOf(i.getNextBlock()) == index)
                .findFirst()
                .orElse(null);
    }

    @Override
    public void edit(T item, OverflowingHandler<OverflowingNodeImpl<T, U>> node, U key) throws IOException {
        int index = this.directory.indexOf(node.getNextBlock());
        if (index <= -1) {
            return;
        }
        while (index > -1) {
            OverflowingNodeImpl<T, U> foundedNode = this.directory.get(index);
            List<T> items = foundedNode.read();
            int number = 0;
            for (T data: items) {
                if (data.getKey().compareTo(key) == 0) {
                    this.fileHandler.write(item, foundedNode.getStartPosition() + number* this.sizeOfRecord);
                    return;
                }
                number++;
            }
            index = this.directory.indexOf(foundedNode.getNextBlock());
        }
    }

    @Override
    public List<T> reorder(OverflowingNodeImpl<T, U> node, OverflowingHandler<OverflowingNodeImpl<T, U>> mainNode, int numberOfItems, int maxInMain) throws IOException {
        if (node.getNextBlock() != null) {
            OverflowingNodeImpl<T, U> helpNode;
            int index = this.directory.indexOf(node.getNextBlock());
            while (index > -1) {
                helpNode = this.directory.get(index);
                OverflowingNodeImpl<T, U> nextBlock = helpNode.getNextBlock();
                if (node.getTemporaryList().size() + helpNode.getNumberOfRecords() <= this.maxItemInBlock) {
                    helpNode.read().forEach(node::addToTemporaryList);
                    this.reorderBlocks(helpNode, null);
                }
                index = this.directory.indexOf(nextBlock);
            }
        }
        List<T> helpList = new LinkedList<>();
        if (numberOfItems + node.getTemporaryList().size() <= maxInMain) {
            helpList.addAll(node.getTemporaryList());
            this.reorderBlocks(node, mainNode);
        } else {
            this.fileHandler.write(node.getTemporaryList(), node.getStartPosition(), node.getTemporaryList().size());
            node.setCurrentRecordsNumber(node.getTemporaryList().size());
            node.clearTemporaryList();
        }
        return helpList;
    }

    @Override
    public List<T> reorderFromMain(OverflowingHandler<OverflowingNodeImpl<T, U>> node, int numberOfItems, int maxItems) throws IOException {
        int index = this.directory.indexOf(node.getNextBlock());
        OverflowingNodeImpl<T, U> helpNode;
        List<T> items = new ArrayList<>();
        int currentRecords = numberOfItems;
        while (index > -1) {
            helpNode = this.directory.get(index);
            OverflowingNodeImpl<T, U> nextBlock = helpNode.getNextBlock();
            if (currentRecords + helpNode.getNumberOfRecords() <= maxItems) {
                items.addAll(helpNode.read());
                this.reorderBlocks(helpNode, node);
                currentRecords += items.size();
            }
            index = this.directory.indexOf(nextBlock);
        }
        return items;
    }

    @Override
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
    public void addToBlankBlocks(OverflowingNodeImpl<T, U> node) {
        this.memoryManager.addToDeallocatedBlock(node);
        this.directory.remove(node);
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

    private void reorderBlocks(OverflowingNodeImpl<T, U> helpNode, OverflowingHandler<OverflowingNodeImpl<T, U>> node) {
        helpNode.clearData();
        OverflowingNodeImpl<T, U> ancestorInOverflowing = this.findAncestor(helpNode);
        if (node != null && ancestorInOverflowing == null) {
            node.setNextBlock(helpNode.getNextBlock());
        } else {
            ancestorInOverflowing.setNextBlock(helpNode.getNextBlock());
        }
        this.addToBlankBlocks(helpNode);
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
}
