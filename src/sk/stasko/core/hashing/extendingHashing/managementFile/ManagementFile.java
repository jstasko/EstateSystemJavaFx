package sk.stasko.core.hashing.extendingHashing.managementFile;

import sk.stasko.core.fileHandler.FileHandler;
import sk.stasko.core.hashing.extendingHashing.extendingHashingDirectory.node.DynamicDirectoryNodeImpl;
import sk.stasko.core.hashing.extendingHashing.overflowingDirectory.node.OverflowingNodeImpl;
import sk.stasko.core.savableObject.SavableObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;
import java.util.stream.Collectors;

public abstract class ManagementFile<T extends SavableObject<U>, U extends Comparable<U>> {
    protected ArrayList<DynamicDirectoryNodeImpl<T, U>> mainDirectory;
    protected ArrayList<OverflowingNodeImpl<T, U>> overflowDirectory;
    protected int depthOfMainDirectory;
    protected int maxNumberInMain;
    protected int numberOfAllowedBites;
    protected int maxNumberInOverflow;
    protected Queue<DynamicDirectoryNodeImpl<T, U>> emptyMain;
    protected Queue<OverflowingNodeImpl<T, U>> emptyOverflow;
    protected String fileName;

    protected ManagementFile(String file) {
        this.mainDirectory = new ArrayList<>();
        this.overflowDirectory = new ArrayList<>();
        this.maxNumberInMain = -1;
        this.numberOfAllowedBites = -1;
        this.emptyMain = new LinkedList<>();
        this.emptyOverflow = new LinkedList<>();
        this.maxNumberInOverflow = -1;
        this.depthOfMainDirectory = -1;
        this.fileName = file;
    }

    public int getDepthOfMainDirectory() {
        return depthOfMainDirectory;
    }

    public ArrayList<DynamicDirectoryNodeImpl<T, U>> getMainDirectory() {
        return mainDirectory;
    }

    public ArrayList<OverflowingNodeImpl<T, U>> getOverflowDirectory() {
        return overflowDirectory;
    }

    public int getMaxNumberInMain() {
        return maxNumberInMain;
    }

    public int getNumberOfAllowedBites() {
        return numberOfAllowedBites;
    }

    public Queue<DynamicDirectoryNodeImpl<T, U>> getEmptyMain() {
        return emptyMain;
    }

    public Queue<OverflowingNodeImpl<T, U>> getEmptyOverflow() {
        return emptyOverflow;
    }

    public int getMaxNumberInOverflow() {
        return maxNumberInOverflow;
    }

    protected OverflowingNodeImpl<T, U> getOneByAddress(int address) {
        return this.overflowDirectory
                .stream()
                .filter(i -> i.getStartPosition() == address)
                .findFirst()
                .orElse(null);
    }

    public abstract ManagementFile<T, U> load(FileHandler<T> main, FileHandler<T> overflow) throws IOException;
}
