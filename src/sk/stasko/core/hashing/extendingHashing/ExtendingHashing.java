package sk.stasko.core.hashing.extendingHashing;

import sk.stasko.core.hashing.extendingHashing.managementFile.ManagementFile;
import sk.stasko.core.hashing.extendingHashing.managementFile.ManagementFileImpl;
import sk.stasko.core.hashing.extendingHashing.overflowingDirectory.OverflowingDirectoryImpl;
import sk.stasko.core.hashing.node.DirectoryNode;
import sk.stasko.core.fileHandler.FileHandler;
import sk.stasko.core.hashing.extendingHashing.extendingHashingDirectory.memory.DynamicDirectoryNodeManager;
import sk.stasko.core.hashing.memory.MemoryManager;
import sk.stasko.core.hashing.extendingHashing.overflowingDirectory.OverflowingDirectory;
import sk.stasko.core.hashing.extendingHashing.overflowingDirectory.node.OverflowingNodeImpl;
import sk.stasko.core.hashFunction.ExtendingHashFunction;
import sk.stasko.core.savableObject.SavableObject;
import sk.stasko.core.hashing.extendingHashing.extendingHashingDirectory.node.block.ExtendingBlockImpl;
import sk.stasko.core.hashing.extendingHashing.extendingHashingDirectory.node.DynamicDirectoryNodeImpl;
import sk.stasko.core.hashing.extendingHashing.extendingHashingDirectory.DynamicDirectory;
import sk.stasko.core.hashing.extendingHashing.extendingHashingDirectory.DynamicDirectoryImpl;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ExtendingHashing<T extends SavableObject<U>, U extends Comparable<U>> implements Hashing<T, U> {
    private final DynamicDirectory<T, U> dynamicDirectory;
    private final int numberOfRecords;
    private final FileHandler<T> fileHandler;
    private final ExtendingHashFunction<T, U> hashingFunction;
    private final MemoryManager<T, DynamicDirectoryNodeImpl<T, U>,U> memoryManager;
    private final OverflowingDirectory<T, U> overflowingDirectory;
    private final int numberOfAllowedBits;
    private static final String filename = "settingsFile.txt";
    private final ManagementFile<T, U> management;

    public ExtendingHashing
            (int numberOfRecords, int sizeOfRecord, int numberOfAllowedBits,
             ExtendingHashFunction<T, U> hashingFunction, FileHandler<T> fileHandler,
             OverflowingDirectory<T, U> overflowingDirectory)
            throws IOException {
        fileHandler.newLengthOfFile(0);
        this.memoryManager = new DynamicDirectoryNodeManager<>(fileHandler);
        this.dynamicDirectory = new DynamicDirectoryImpl<>(numberOfRecords, sizeOfRecord, fileHandler);
        this.numberOfRecords = numberOfRecords;
        this.fileHandler = fileHandler;
        this.hashingFunction = hashingFunction;

        this.numberOfAllowedBits = numberOfAllowedBits;
        this.overflowingDirectory = overflowingDirectory;
        this.management = new ManagementFileImpl<>(filename);
    }

    @Override
    public ManagementFile<T, U> getManagementFile() {
        return this.management;
    }

    public ExtendingHashing(int sizeOfRecord, FileHandler<T> mainHandler, FileHandler<T> overHandler, ExtendingHashFunction<T, U> hashingFunction) throws IOException {
        this.management = new ManagementFileImpl<>(filename);
        management.load(mainHandler, overHandler);
        this.memoryManager = new DynamicDirectoryNodeManager<>(mainHandler, management.getEmptyMain());
        this.dynamicDirectory = new DynamicDirectoryImpl<>(management.getDepthOfMainDirectory(), management.getMainDirectory());
        this.numberOfRecords = management.getMaxNumberInMain();
        this.fileHandler = mainHandler;
        this.hashingFunction = hashingFunction;
        this.numberOfAllowedBits = management.getNumberOfAllowedBites();
        this.overflowingDirectory = new OverflowingDirectoryImpl<>(management.getMaxNumberInOverflow(), sizeOfRecord, overHandler,
                management.getOverflowDirectory(), management.getEmptyOverflow());
    }

    @Override
    public void add(T item) throws IOException {
        int spaceInOneBucket = this.numberOfRecords *item.getNumberOfBytes();
        boolean isAdded = false;
        while (!isAdded) {
            int maxBytes = this.dynamicDirectory.startPositionOfLastAllocatedBlock() + spaceInOneBucket;
            int index = this.hashingFunction.getIndexFromItem(item, this.dynamicDirectory.getDepthOfDirectory());
            DynamicDirectoryNodeImpl<T, U> blockToInsert = this.dynamicDirectory.getOne(index);
            List<T> items = blockToInsert.read();
            T foundItem = this.findRecordInMainPart(items, item.getKey());
            if (foundItem == null) {
                foundItem = this.overflowingDirectory.find(blockToInsert, item.getKey());
            }
            if (foundItem != null) {
                return;
            }
            if (!blockToInsert.isFull()) {
                this.fileHandler.write(item,
                        blockToInsert.getStartPosition() + blockToInsert.getNumberOfRecords() * item.getAllocatedMemory());
                blockToInsert.setCurrentRecordsNumber(blockToInsert.getNumberOfRecords() + 1);
                isAdded = true;
            } else {
                if (this.numberOfAllowedBits > blockToInsert.getDepthOfBlock()) {
                    if (blockToInsert.getDepthOfBlock() == this.dynamicDirectory.getDepthOfDirectory()) {
                        this.dynamicDirectory.doubleDirectory();
                    }
                    isAdded = splitOfBlocks(blockToInsert, maxBytes, item, items);
                } else {
                    isAdded = this.overflowingDirectory.add(blockToInsert, item);
                }
            }
        }
    }

    @Override
    public boolean delete(T item) throws IOException {
        boolean isDeleteFinished = false;
        int index = this.hashingFunction.getIndexFromKey(item.getKey(), this.dynamicDirectory.getDepthOfDirectory());
        DynamicDirectoryNodeImpl<T, U> block = this.dynamicDirectory.getOne(index);
        List<T> itemsInBlock = block.read();
        T foundRecord = this.findRecordInMainPart(itemsInBlock, item.getKey());
        DirectoryNode<T, U> eraseFrom;
        if (foundRecord == null) {
            eraseFrom = this.overflowingDirectory.delete(block, item.getKey());
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

        if (eraseFrom instanceof DynamicDirectoryNodeImpl) {
            this.reorderFromDirectory( (DynamicDirectoryNodeImpl<T, U>) eraseFrom);
        } else {
            this.reorder((OverflowingNodeImpl<T, U>) eraseFrom, block);
        }
        while(!isDeleteFinished) {
            DynamicDirectoryNodeImpl<T, U> neighbour = this.findNeighbour(block);
            if (neighbour != null) {
                if (block.getActualNumberOfRecords(false) + neighbour.getActualNumberOfRecords(true) <= this.numberOfRecords) {
                    int depthOfNeighbour = this.connectBlock(block, neighbour);
                    if (this.dynamicDirectory.needToBeShrunk(depthOfNeighbour)) {
                        this.dynamicDirectory.shrunkDirectory();
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
        this.memoryManager.reorderBlankBlocks(this.dynamicDirectory.startPositionOfLastAllocatedBlock() + spaceInOneBucket);
        this.overflowingDirectory.reorderBlankBlocks();
        return true;
    }

    @Override
    public T find(U key) throws IOException {
        int index = this.hashingFunction.getIndexFromKey(key, this.dynamicDirectory.getDepthOfDirectory());
        DynamicDirectoryNodeImpl<T, U> foundedNode = this.dynamicDirectory.getOne(index);
        return findRecordInBlock(foundedNode, key);
    }

    @Override
    public String printBlocks(int sizeOfRecord) throws IOException {
        byte[] records = this.fileHandler.readBlockByByte(0, this.dynamicDirectory.startPositionOfLastAllocatedBlock() + this.numberOfRecords*sizeOfRecord);
        return this.dynamicDirectory.toString(records, sizeOfRecord);
    }

    @Override
    public String printBlankBlock() {
        return this.memoryManager.printOfBlocks();
    }

    @Override
    public void saveSettings(int maxId) throws IOException {
        try (PrintWriter printWriter = new PrintWriter(filename, StandardCharsets.UTF_8)) {
            String mainSettings = this.numberOfRecords + ";" + this.numberOfAllowedBits + ";" + this.dynamicDirectory.getDepthOfDirectory() + ";" + maxId;
            printWriter.println(this.memoryManager.getSettings());
            printWriter.println(this.overflowingDirectory.getSettings());
            printWriter.println(mainSettings);
            printWriter.println(this.dynamicDirectory.getSettings());
        }
    }

    private T findRecordInBlock(DynamicDirectoryNodeImpl<T, U> foundedNode, U key) throws IOException {
        List<T> foundList = foundedNode.read();
        if (foundList == null) {
            return null;
        }
        T foundedRecord = findRecordInMainPart(foundList, key);
        if (foundedRecord == null) {
            foundedRecord = this.overflowingDirectory.find(foundedNode, key);
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

    private DynamicDirectoryNodeImpl<T, U> findNeighbour(DynamicDirectoryNodeImpl<T, U> node) {
        if (node.getDepthOfBlock() == 1) {
            return null;
        }

        int indexOfNode = IntStream.range(0, this.dynamicDirectory.sizeOfDirectory())
                .filter(i -> this.dynamicDirectory.getOne(i).equals(node))
                .findFirst()
                .orElse(-1);

        if (indexOfNode == -1) {
            return null;
        }

        List<DynamicDirectoryNodeImpl<T, U>> possibleNeighbours = IntStream.range(0, this.dynamicDirectory.sizeOfDirectory())
                .filter(i -> this.dynamicDirectory.getOne(i).getDepthOfBlock() == node.getDepthOfBlock())
                .filter(i -> !this.dynamicDirectory.getOne(i).equals(node))
                .boxed()
                .map(this.dynamicDirectory::getOne)
                .distinct()
                .collect(Collectors.toList());

        int indexOfPrefix = this.hashingFunction.getPrefixFromIndex(indexOfNode, this.dynamicDirectory.getDepthOfDirectory(),node.getDepthOfBlock());
        var wrapper = new Object(){ int index = -1; };
        possibleNeighbours.forEach(i -> {
            int value = this.dynamicDirectory.indexOf(i);
            int possibleNeighbourPrefix = this.hashingFunction.getPrefixFromIndex(value, this.dynamicDirectory.getDepthOfDirectory(),i.getDepthOfBlock());
            if (possibleNeighbourPrefix == indexOfPrefix) {
                wrapper.index = value;
            }
        });

        if (wrapper.index != -1) {
            return this.dynamicDirectory.getOne(wrapper.index);
        }
        return null;
    }

    private boolean splitOfBlocks(DynamicDirectoryNodeImpl<T, U> oldBlock, int byteToStart, T myItem, List<T> items) throws IOException {
        oldBlock.incrementDepthBlock();
        DynamicDirectoryNodeImpl<T, U> newBlock;
        if (this.memoryManager.getSize() > 0) {
            newBlock = this.memoryManager.getBlock(oldBlock.getDepthOfBlock());
        } else {
            newBlock = new ExtendingBlockImpl<>(byteToStart, oldBlock.getDepthOfBlock(), this.numberOfRecords,this.fileHandler);
        }

        List<Integer> directoryNodesIndexes = IntStream.range(0, this.dynamicDirectory.sizeOfDirectory())
                .filter(item -> this.dynamicDirectory.getOne(item).getStartPosition() == oldBlock.getStartPosition())
                .boxed()
                .collect(Collectors.toList());

        directoryNodesIndexes
                .stream()
                .skip(directoryNodesIndexes.size() / 2)
                .forEach(i -> this.dynamicDirectory.setOne(i, newBlock));
        oldBlock.clearData();
        items.add(myItem);
        var wrapper = new Object(){ boolean added = false; };
        List<DynamicDirectoryNodeImpl<T, U>> nodes = new ArrayList<>(2);
        IntStream.range(0, items.size())
                .forEach(i -> {
                    int newIndex = this.hashingFunction.getIndexFromItem(items.get(i), this.dynamicDirectory.getDepthOfDirectory(), oldBlock.getDepthOfBlock());
                    DynamicDirectoryNodeImpl<T, U> block = this.dynamicDirectory.getOne(newIndex);
                    if (block.getTemporaryList().size() < this.numberOfRecords) {
                        block.addToTemporaryList(items.get(i));
                        nodes.add(block);
                        if (items.get(i).equals(myItem)) {
                            wrapper.added = true;
                        }
                    }
                });
        for (DynamicDirectoryNodeImpl<T, U> node: nodes) {
            if (node.getTemporaryList().size() > 0) {
                this.fileHandler.write(node.getTemporaryList(), node.getStartPosition(), node.getNumberOfRecords());
                node.setCurrentRecordsNumber(node.getTemporaryList().size());
                node.clearTemporaryList();
            }
        }
        return wrapper.added;
    }

    private int connectBlock(DynamicDirectoryNodeImpl<T, U> blockTo, DynamicDirectoryNodeImpl<T, U> blockFrom) throws IOException {
        blockFrom.read().forEach(blockTo::addToTemporaryList);
        blockTo.decreaseDepthBlock();
//        blockTo.setNextBlock(blockFrom.getNextBlock());
        IntStream.range(0, this.dynamicDirectory.sizeOfDirectory())
                .filter(i -> this.dynamicDirectory.getOne(i).getStartPosition() == blockFrom.getStartPosition())
                .forEach(i -> this.dynamicDirectory.setOne(i, blockTo));
        return this.memoryManager.addToDeallocatedBlock(blockFrom);
    }

    private void reorder(OverflowingNodeImpl<T, U> node, DynamicDirectoryNodeImpl<T, U> mainNode) throws IOException {
        if (node.getTemporaryList().size() == 0) {
            OverflowingHandler<OverflowingNodeImpl<T, U>> ancestor = findAncestor(node);
            ancestor.setNextBlock(node.getNextBlock());
            this.overflowingDirectory.addToBlankBlocks(node);
        } else {
            List<T> helpList = this.overflowingDirectory.reorder(node, mainNode, mainNode.getTemporaryList().size(),this.numberOfRecords);
            helpList.forEach(mainNode::addToTemporaryList);
        }
    }

    private OverflowingHandler<OverflowingNodeImpl<T, U>> findAncestor(OverflowingNodeImpl<T, U> node) {
        OverflowingHandler<OverflowingNodeImpl<T, U>> ancestor = this.overflowingDirectory.findAncestor(node);
        return ancestor != null ? ancestor : this.dynamicDirectory.findAncestor(node);
    }

    private void reorderFromDirectory(DynamicDirectoryNodeImpl<T, U> node) throws IOException{
        if (node.getNextBlock() == null) {
            return;
        }
        List<T> items = this.overflowingDirectory.reorderFromMain(node, node.getTemporaryList().size(), this.numberOfRecords);
        items.forEach(node::addToTemporaryList);
    }

    @Override
    public String getOverflowPart() throws IOException {
        return this.overflowingDirectory.print();
    }

    @Override
    public String getOverflowPartBlank() {
        return this.overflowingDirectory.printBlank();
    }

    @Override
    public void edit(T item) throws IOException {
        int index = this.hashingFunction.getIndexFromItem(item, this.dynamicDirectory.getDepthOfDirectory());
        DynamicDirectoryNodeImpl<T, U> foundedNode = this.dynamicDirectory.getOne(index);
        List<T> foundList = foundedNode.read();
        if (foundList == null) {
            return;
        }
        int number = 0;
        boolean isAdded = false;
        for (T s: foundList) {
            if (s.getKey().compareTo(item.getKey()) == 0) {
                this.fileHandler.write(item,
                        foundedNode.getStartPosition() + number * item.getAllocatedMemory());
                isAdded = true;
                break;
            }
            number++;
        }
        if (isAdded) {
            return;
        }
        this.overflowingDirectory.edit(item, foundedNode, item.getKey());

    }
}
