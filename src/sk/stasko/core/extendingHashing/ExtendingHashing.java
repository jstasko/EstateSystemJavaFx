package sk.stasko.core.extendingHashing;

import sk.stasko.core.extendingHashing.block.OverflowingHandler;
import sk.stasko.core.extendingHashing.node.FileNode;
import sk.stasko.core.fileHandler.FileHandler;
import sk.stasko.core.extendingHashing.memory.DirectoryNodeManager;
import sk.stasko.core.extendingHashing.memory.MemoryManager;
import sk.stasko.core.extendingHashing.overflowingFile.OverflowingFile;
import sk.stasko.core.extendingHashing.overflowingFile.block.OverflowingBlock;
import sk.stasko.core.hash.ExtendingHashFunction;
import sk.stasko.core.savableObject.SavableObject;
import sk.stasko.core.extendingHashing.block.ExtendingBlockImpl;
import sk.stasko.core.extendingHashing.node.DirectoryNodeImpl;
import sk.stasko.core.extendingHashing.directory.Directory;
import sk.stasko.core.extendingHashing.directory.DirectoryImpl;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ExtendingHashing<T extends SavableObject<U>, U extends Comparable<U>> implements Hashing<T, U> {
    private final Directory<T, U> directory;
    private final int numberOfRecords;
    private final FileHandler<T> fileHandler;
    private final ExtendingHashFunction<T, U> hashingFunction;
    private final MemoryManager<T, DirectoryNodeImpl<T, U>,U> memoryManager;
    private final OverflowingFile<T, U> overflowingFile;
    private final int numberOfAllowedBits;

    public ExtendingHashing(int numberOfRecords, int sizeOfRecord, int numberOfAllowedBits,ExtendingHashFunction<T, U> hashingFunction, FileHandler<T> fileHandler, OverflowingFile<T, U> overflowingFile) {
        this.memoryManager = new DirectoryNodeManager<>(fileHandler);
        this.directory = new DirectoryImpl<>(numberOfRecords, sizeOfRecord, fileHandler);
        this.numberOfRecords = numberOfRecords;
        this.fileHandler = fileHandler;
        this.hashingFunction = hashingFunction;

        this.numberOfAllowedBits = numberOfAllowedBits;
        this.overflowingFile = overflowingFile;
    }

    @Override
    public void add(T item) throws IOException {
        int spaceInOneBucket = this.numberOfRecords *item.getNumberOfBytes();
        boolean isAdded = false;
        while (!isAdded) {
            int maxBytes = this.directory.startPositionOfLastAllocatedBlock() + spaceInOneBucket;
            int index = this.hashingFunction.getIndexFromItem(item, this.directory.getDepthOfDirectory());
            DirectoryNodeImpl<T, U> blockToInsert = this.directory.getOne(index);
            if (!blockToInsert.isFull()) {
                this.fileHandler.write(item,
                        blockToInsert.getStartPosition() + blockToInsert.getNumberOfRecords() * item.getAllocatedMemory());
                blockToInsert.setCurrentRecordsNumber(blockToInsert.getNumberOfRecords() + 1);
                isAdded = true;
            } else {
                if (this.numberOfAllowedBits > blockToInsert.getDepthOfBlock()) {
                    if (blockToInsert.getDepthOfBlock() == this.directory.getDepthOfDirectory()) {
                        this.directory.doubleDirectory();
                    }
                    isAdded = splitOfBlocks(blockToInsert, maxBytes, item);
                } else {
                    isAdded = this.overflowingFile.add(blockToInsert, item);
                }
            }
        }
    }

    @Override
    public boolean delete(T item) throws IOException {
        boolean isDeleteFinished = false;
        int index = this.hashingFunction.getIndexFromKey(item.getKey(), this.directory.getDepthOfDirectory());
        DirectoryNodeImpl<T, U> block = this.directory.getOne(index);
        List<T> itemsInBlock = block.read();
        T foundRecord = this.findRecordInMainPart(itemsInBlock, item.getKey());
        FileNode<T, U> eraseFrom;
        if (foundRecord == null) {
            eraseFrom = this.overflowingFile.delete(block, item.getKey());
        } else {
            eraseFrom = block;
        }
        itemsInBlock = itemsInBlock.stream()
                .filter(i -> i.getKey().compareTo(item.getKey()) != 0)
                .collect(Collectors.toList());
        block.clearData();
        itemsInBlock.forEach(block::addToTemporaryList);
        if (eraseFrom == null) {
            return false;
        }

        if (eraseFrom instanceof DirectoryNodeImpl) {
            this.reorderFromDirectory( (DirectoryNodeImpl<T, U>) eraseFrom);
        } else {
            this.reorder((OverflowingBlock<T, U>) eraseFrom);
        }
        while(!isDeleteFinished) {
            DirectoryNodeImpl<T, U> neighbour = this.findNeighbour(block);
            if (neighbour != null) {
                if (block.getActualNumberOfRecords(false) + neighbour.getActualNumberOfRecords(true) <= this.numberOfRecords) {
                    int depthOfNeighbour = this.connectBlock(block, neighbour);
                    if (this.directory.needToBeShrunk(depthOfNeighbour)) {
                        this.directory.shrunkDirectory();
                    }
                } else {
                    isDeleteFinished = true;
                }
            } else {
                isDeleteFinished = true;
            }
        }
        this.fileHandler.write(block.getTemporaryList(), block.getStartPosition(), block.getNumberOfRecords());
        block.setCurrentRecordsNumber(block.getTemporaryList().size());
        block.clearTemporaryList();
        int spaceInOneBucket = this.numberOfRecords * item.getNumberOfBytes();
        this.memoryManager.reorderBlankBlocks(this.directory.startPositionOfLastAllocatedBlock() + spaceInOneBucket);
        this.overflowingFile.reorderBlankBlocks();
        return true;
    }

    @Override
    public T find(U key) throws IOException {
        int index = this.hashingFunction.getIndexFromKey(key, this.directory.getDepthOfDirectory());
        DirectoryNodeImpl<T, U> foundedNode = this.directory.getOne(index);
        return findRecordInBlock(foundedNode, key);
    }

    @Override
    public String printBlocks(int sizeOfRecord) throws IOException {
        byte[] records = this.fileHandler.readBlockByByte(0, this.directory.startPositionOfLastAllocatedBlock() + this.numberOfRecords*sizeOfRecord);
        return this.directory.toString(records, sizeOfRecord);
    }

    @Override
    public String printBlankBlock() {
        return this.memoryManager.printOfBlocks();
    }

    private T findRecordInBlock(DirectoryNodeImpl<T, U> foundedNode, U key) throws IOException {
        List<T> foundList = foundedNode.read();
        if (foundList == null) {
            return null;
        }
        T foundedRecord = findRecordInMainPart(foundList, key);
        if (foundedRecord == null) {
            foundedRecord = this.overflowingFile.find(foundedNode, key);
        }
        return foundedRecord;
    }

    private T findRecordInMainPart(List<T> foundList, U key) {
        T foundedRecord = null;
        for (T item: foundList) {
            if (item.getKey().compareTo(key) == 0) {
                foundedRecord = item;
            }
        }
        return foundedRecord;
    }

    private DirectoryNodeImpl<T, U> findNeighbour(DirectoryNodeImpl<T, U> node) {
        if (node.getDepthOfBlock() == 1) {
            return null;
        }

        int indexOfNode = IntStream.range(0, this.directory.sizeOfDirectory())
                .filter(i -> this.directory.getOne(i).equals(node))
                .findFirst()
                .orElse(-1);

        if (indexOfNode == -1) {
            return null;
        }

        List<DirectoryNodeImpl<T, U>> possibleNeighbours = IntStream.range(0, this.directory.sizeOfDirectory())
                .filter(i -> this.directory.getOne(i).getDepthOfBlock() == node.getDepthOfBlock())
                .filter(i -> !this.directory.getOne(i).equals(node))
                .boxed()
                .map(this.directory::getOne)
                .distinct()
                .collect(Collectors.toList());

        int indexOfPrefix = this.hashingFunction.getPrefixFromIndex(indexOfNode, this.directory.getDepthOfDirectory(),node.getDepthOfBlock());
        var wrapper = new Object(){ int index = -1; };
        possibleNeighbours.forEach(i -> {
            int value = this.directory.indexOf(i);
            int possibleNeighbourPrefix = this.hashingFunction.getPrefixFromIndex(value, this.directory.getDepthOfDirectory(),i.getDepthOfBlock());
            if (possibleNeighbourPrefix == indexOfPrefix) {
                wrapper.index = value;
            }
        });

        if (wrapper.index != -1) {
            return this.directory.getOne(wrapper.index);
        }
        return null;
    }

    private boolean splitOfBlocks(DirectoryNodeImpl<T, U> oldBlock, int byteToStart, T myItem) throws IOException {
        oldBlock.incrementDepthBlock();
        DirectoryNodeImpl<T, U> newBlock;
        if (this.memoryManager.getSize() > 0) {
            newBlock = this.memoryManager.getBlock(oldBlock.getDepthOfBlock());
        } else {
            newBlock = new ExtendingBlockImpl<>(byteToStart, oldBlock.getDepthOfBlock(), this.numberOfRecords,this.fileHandler);
        }

        List<Integer> directoryNodesIndexes = IntStream.range(0, this.directory.sizeOfDirectory())
                .filter(item -> this.directory.getOne(item).getStartPosition() == oldBlock.getStartPosition())
                .boxed()
                .collect(Collectors.toList());

        directoryNodesIndexes
                .stream()
                .skip(directoryNodesIndexes.size() / 2)
                .forEach(i -> this.directory.setOne(i, newBlock));
        List<T> items = oldBlock.read();
        oldBlock.clearData();
        items.add(myItem);
        var wrapper = new Object(){ boolean added = false; };
        List<DirectoryNodeImpl<T, U>> nodes = new ArrayList<>(2);
        IntStream.range(0, items.size())
                .forEach(i -> {
                    int newIndex = this.hashingFunction.getIndexFromItem(items.get(i), this.directory.getDepthOfDirectory(), oldBlock.getDepthOfBlock());
                    DirectoryNodeImpl<T, U> block = this.directory.getOne(newIndex);
                    if (block.getTemporaryList().size() < this.numberOfRecords) {
                        block.addToTemporaryList(items.get(i));
                        nodes.add(block);
                        if (items.get(i).equals(myItem)) {
                            wrapper.added = true;
                        }
                    }
                });
        for (DirectoryNodeImpl<T, U> node: nodes) {
            if (node.getTemporaryList().size() > 0) {
                this.fileHandler.write(node.getTemporaryList(), node.getStartPosition(), node.getNumberOfRecords());
                node.setCurrentRecordsNumber(node.getTemporaryList().size());
                node.clearTemporaryList();
            }
        }
        return wrapper.added;
    }

    private int connectBlock(DirectoryNodeImpl<T, U> blockTo, DirectoryNodeImpl<T, U> blockFrom) throws IOException {
        blockFrom.read().forEach(blockTo::addToTemporaryList);
        blockTo.decreaseDepthBlock();
        blockTo.setNextBlock(blockFrom.getNextBlock());
        IntStream.range(0, this.directory.sizeOfDirectory())
                .filter(i -> this.directory.getOne(i).getStartPosition() == blockFrom.getStartPosition())
                .forEach(i -> this.directory.setOne(i, blockTo));
        return this.memoryManager.addToDeallocatedBlock(blockFrom);
    }

    private void reorder(OverflowingBlock<T, U> node) throws IOException {
        if (node.getNumberOfRecords() == 0) {
            OverflowingHandler<OverflowingBlock<T, U>> ancestor = findAncestor(node);
            ancestor.setNextBlock(node.getNextBlock());
            this.overflowingFile.addToBlankBlocks(node);
        } else {
            if (node.getNextBlock() != null) {
                this.overflowingFile.reorder(node);
            }
        }
    }

    private OverflowingHandler<OverflowingBlock<T, U>> findAncestor(OverflowingBlock<T, U> node) {
        OverflowingHandler<OverflowingBlock<T, U>> ancestor = this.overflowingFile.findAncestor(node);
        return ancestor != null ? ancestor : this.directory.findAncestor(node);
    }

    private void reorderFromDirectory(DirectoryNodeImpl<T, U> node) throws IOException{
        if (node.getNextBlock() == null) {
            return;
        }
        T item = this.overflowingFile.getItemFromLastBlock(node);
        node.addToTemporaryList(item);
    }
}
