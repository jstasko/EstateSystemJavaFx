package sk.stasko.core.hashing.memory;

import sk.stasko.core.hashing.node.DirectoryNode;
import sk.stasko.core.fileHandler.FileHandler;
import sk.stasko.core.savableObject.SavableObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class MemoryManager<T extends SavableObject<U>, S extends DirectoryNode<T, U>, U extends Comparable<U>> {
    protected Queue<S> listOfBlocks;
    protected final FileHandler<T> fileHandler;

    public MemoryManager(FileHandler<T> fileHandler) {
        this.listOfBlocks = new LinkedList<>();
        this.fileHandler = fileHandler;
    }

    public MemoryManager(FileHandler<T> fileHandler, Queue<S> queue) {
        this.listOfBlocks = queue;
        this.fileHandler = fileHandler;
    }

    public int getSize() {
        return this.listOfBlocks.size();
    }

    public void reorderBlankBlocks(int maxAllocatedMemory) throws IOException {
        this.listOfBlocks = this.listOfBlocks
                .stream()
                .filter(i -> i.getStartPosition() < maxAllocatedMemory)
                .collect(Collectors.toCollection(LinkedList::new));
        this.fileHandler.newLengthOfFile(maxAllocatedMemory);
    }

    public String printOfBlocks() {
        List<S> helper = this.listOfBlocks
                .stream()
                .sorted(Comparator.comparingInt(S::getStartPosition))
                .collect(Collectors.toList());
        String concatString = "";
        if (helper.size() != 0) {
            for (S node : helper) {
                concatString = concatString.concat(node.toString());
            }
        } else {
            concatString = concatString.concat("No Empty blocks \n");
        }
        return concatString;
    }

    public String getSettings() {
        return this.listOfBlocks
                .stream()
                .map(S::getStartPosition)
                .map(Objects::toString)
                .collect(Collectors.joining(";"));
    }

    public abstract S getBlock(int depth);
    public abstract int addToDeallocatedBlock(S directoryNode);
}
