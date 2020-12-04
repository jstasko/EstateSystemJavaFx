package sk.stasko.core.extendingHashing.overflowingFile;

import sk.stasko.core.extendingHashing.block.OverflowingHandler;
import sk.stasko.core.extendingHashing.node.DirectoryNodeImpl;
import sk.stasko.core.fileHandler.FileHandler;
import sk.stasko.core.extendingHashing.memory.MemoryManager;
import sk.stasko.core.extendingHashing.memory.OverflowingManager;
import sk.stasko.core.extendingHashing.overflowingFile.block.OverflowingBlock;
import sk.stasko.core.savableObject.SavableObject;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class OverflowingFileImpl<T extends SavableObject<U>, U extends Comparable<U>> implements OverflowingFile<T, U> {
    private final List<OverflowingBlock<T, U>> overflowingFile;
    private final FileHandler<T> fileHandler;
    private final int maxItemInBlock;
    private final int sizeOfRecord;
    private final MemoryManager<T, OverflowingBlock<T, U>,U> memoryManager;

    public OverflowingFileImpl(int maxItemInBlock, int sizeOfRecord, FileHandler<T> fileHandler) {
        this.maxItemInBlock = maxItemInBlock;
        this.overflowingFile = new ArrayList<>();
        this.sizeOfRecord = sizeOfRecord;
        this.fileHandler = fileHandler;
        this.memoryManager = new OverflowingManager<>(fileHandler);
    }

    public void reorderBlankBlocks() throws IOException {
        int maxAllocatedMemory;
        if (this.overflowingFile.size() == 0) {
            maxAllocatedMemory = 0;
        } else {
            maxAllocatedMemory = this.startPositionOfLastAllocatedBlock() + maxItemInBlock*sizeOfRecord;
        }
        this.memoryManager.reorderBlankBlocks(maxAllocatedMemory);
    }

    @Override
    public boolean add(OverflowingHandler<OverflowingBlock<T, U>> item, T data) throws IOException {
        OverflowingBlock<T, U> block = this.findCorrectBlock(item);
        this.fileHandler.write(data,
                block.getStartPosition() + block.getNumberOfRecords() * data.getAllocatedMemory());
        block.setCurrentRecordsNumber(block.getNumberOfRecords() + 1);
        return true;
    }

    @Override
    public OverflowingBlock<T, U> delete(DirectoryNodeImpl<T, U> node, U key) throws IOException {
        int index = this.overflowingFile.indexOf(node.getNextBlock());
        if (index <= -1) {
            return null;
        }
        while (index > -1) {
            OverflowingBlock<T, U> foundedNode = this.overflowingFile.get(index);
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
            index = this.overflowingFile.indexOf(foundedNode.getNextBlock());
        }
        return null;
    }

    @Override
    public T find(DirectoryNodeImpl<T, U> node, U key) throws IOException {
        int index = this.overflowingFile.indexOf(node.getNextBlock());
        if (index <= -1) {
            return null;
        }
        while (index > -1) {
            OverflowingBlock<T, U> foundedNode = this.overflowingFile.get(index);
            List<T> items = foundedNode.read();
            for (T item: items) {
                if (item.getKey().compareTo(key) == 0) {
                    return item;
                }
            }
            index = this.overflowingFile.indexOf(foundedNode.getNextBlock());
        }
        return null;
    }

    @Override
    public void reorder(OverflowingBlock<T, U> node) throws IOException {
        T item = this.getItemFromLastBlock(node);
        this.fileHandler.write(item,
                node.getStartPosition() + node.getNumberOfRecords() * item.getAllocatedMemory());
        node.setCurrentRecordsNumber(node.getNumberOfRecords() + 1);
    }

    private OverflowingBlock<T,U> findCorrectBlock(OverflowingHandler<OverflowingBlock<T, U>> node) {
        int index = this.overflowingFile.indexOf(node.getNextBlock());
        OverflowingBlock<T, U> foundedNode = null;
        while (index > -1) {
            foundedNode = this.overflowingFile.get(index);
            if (foundedNode.getNumberOfRecords() < this.maxItemInBlock) {
                return foundedNode;
            }
            index = this.overflowingFile.indexOf(foundedNode.getNextBlock());
        }
        return this.addBlock(Objects.requireNonNullElse(foundedNode, node));
    }

    private int startPositionOfLastAllocatedBlock() {
        return Collections.max(this.overflowingFile.stream().map(OverflowingBlock::getStartPosition)
                .collect(Collectors.toList()));
    }

    private OverflowingBlock<T, U> addBlock(OverflowingHandler<OverflowingBlock<T, U>> node) {
        OverflowingBlock<T, U> block;
        if (this.memoryManager.getSize() == 0) {
            int startPosition = 0;
            if (this.overflowingFile.size() != 0) {
                startPosition = startPositionOfLastAllocatedBlock() + this.sizeOfRecord * this.maxItemInBlock;
            }
            block = new OverflowingBlock<>(startPosition, this.maxItemInBlock, fileHandler);
        } else {
            block = this.memoryManager.getBlock(0);
        }
        this.overflowingFile.add(block);
        node.setNextBlock(block);
        return block;
    }
    @Override
    public OverflowingBlock<T, U> findAncestor(OverflowingBlock<T, U> block) {
        int index = this.overflowingFile.indexOf(block);
        return this.overflowingFile
                .stream()
                .filter(i -> this.overflowingFile.indexOf(i.getNextBlock()) == index)
                .findFirst()
                .orElse(null);
    }

    @Override
    public void addToBlankBlocks(OverflowingBlock<T, U> node) {
        this.memoryManager.addToDeallocatedBlock(node);
        this.overflowingFile.remove(node);
    }

    @Override
    public T getItemFromLastBlock(OverflowingHandler<OverflowingBlock<T, U>> node) throws IOException {
        OverflowingBlock<T, U> helpNode = this.findLastBlockFromChain(node);
        List<T> items = helpNode.read();
        helpNode.clearData();
        T returnData = items.get(items.size() - 1);
        OverflowingBlock<T, U> ancestorInOverflowing = this.findAncestor(helpNode);
        this.removeDataFromBlock(items, helpNode);
        if (helpNode.getNumberOfRecords() == 0 && ancestorInOverflowing == null) {
            node.setNextBlock(null);
        } else if (helpNode.getNumberOfRecords() == 0) {
            ancestorInOverflowing.setNextBlock(null);
        }
        return returnData;
    }

    private void removeDataFromBlock(List<T> items, OverflowingBlock<T, U> eraseFrom) throws IOException {
        items.remove(items.get(items.size() - 1));
        if (items.size() == 0) {
            this.addToBlankBlocks(eraseFrom);
        } else {
            this.fileHandler.write(items, eraseFrom.getStartPosition(), eraseFrom.getNumberOfRecords());
            eraseFrom.setCurrentRecordsNumber(items.size());
        }
    }

    private OverflowingBlock<T, U> findLastBlockFromChain(OverflowingHandler<OverflowingBlock<T ,U>> node) {
        int index = this.overflowingFile.indexOf(node.getNextBlock());
        OverflowingBlock<T, U> helpNode = null;
        while (index > -1) {
            helpNode = this.overflowingFile.get(index);
            index = this.overflowingFile.indexOf(helpNode.getNextBlock());
        }
        return helpNode;
    }

    @Override
    public String print() throws IOException {
        List<OverflowingBlock<T, U>> helper = this.overflowingFile
                .stream()
                .sorted(Comparator.comparingInt(OverflowingBlock::getStartPosition))
                .distinct()
                .collect(Collectors.toList());
        String concatString = "";
        if (helper.size() == 0) {
            return "No overflow blocks";
        }
        byte[] bytes = this.fileHandler.readBlockByByte(0, this.startPositionOfLastAllocatedBlock());
        for(OverflowingBlock<T, U> i: helper) {
            concatString = concatString.concat(i.toString(bytes, this.sizeOfRecord));
        }
        return concatString;
    }

    public String printBlank() {
        return this.memoryManager.printOfBlocks();
    }
}
