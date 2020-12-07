package sk.stasko.core.hashing.extendingHashing.managementFile;

import sk.stasko.core.fileHandler.FileHandler;
import sk.stasko.core.hashing.extendingHashing.extendingHashingDirectory.node.DynamicDirectoryNodeImpl;
import sk.stasko.core.hashing.extendingHashing.extendingHashingDirectory.node.block.ExtendingBlockImpl;
import sk.stasko.core.hashing.extendingHashing.overflowingDirectory.node.OverflowingNodeImpl;
import sk.stasko.core.savableObject.SavableObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class ManagementFileImpl<T extends SavableObject<U>, U extends Comparable<U>> extends ManagementFile<T, U> {

    public ManagementFileImpl(String filename) {
        super(filename);

    }

    public ManagementFile<T, U> load(FileHandler<T> main, FileHandler<T> overflow) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(this.fileName))) {
            String memorySettings = reader.readLine();
            String overMemorySettings = reader.readLine();
            String[] overSettings = reader.readLine().split(";");
            String overNodes = reader.readLine();
            String[] settings = reader.readLine().split(";");
            String nodeSettings = reader.readLine();
            this.maxNumberInMain = Integer.parseInt(settings[0]);
            this.numberOfAllowedBites = Integer.parseInt(settings[1]);
            this.depthOfMainDirectory = Integer.parseInt(settings[2]);
            this.maxNumberInOverflow = Integer.parseInt(overSettings[0]);
            this.setMainManager(memorySettings, main);
            this.setOverflowManager(overMemorySettings, overflow);
            this.setOverflowDirectory(overNodes, overflow);
            this.setMainDirectory(nodeSettings, overflow);
        }
        return this;
    }

    private void setMainManager(String settings, FileHandler<T> mainHandler) {
        if (settings.length() == 0) {
            emptyMain = new LinkedList<>();
        } else {
            String[] data = settings.split(";");
            Queue<DynamicDirectoryNodeImpl<T, U>> queue = new LinkedList<>();
            for (String item : data) {
                queue.add(new ExtendingBlockImpl<>(Integer.parseInt(item), -1, this.maxNumberInMain, mainHandler));
            }
            this.emptyMain = queue;
        }
    }

    private void setOverflowManager(String settings, FileHandler<T> overflow) {
        if (settings.length() == 0) {
            this.emptyOverflow = new LinkedList<>();
        } else {
            String[] data = settings.split(";");
            Queue<OverflowingNodeImpl<T, U>> queue = new LinkedList<>();
            for (String item : data) {
                queue.add(new OverflowingNodeImpl<>(Integer.parseInt(item), this.maxNumberInOverflow, overflow));
            }
            this.emptyOverflow = queue;
        }
    }

    private void setOverflowDirectory(String overNodes, FileHandler<T> overHandler) {
        String[] nodes = overNodes.split(";");
        int index = 0;
        List<Integer> indexes = new ArrayList<>();
        while (index < nodes.length) {
            OverflowingNodeImpl<T, U> node = new OverflowingNodeImpl<>(overHandler);
            for (int i = index; i < index + 3; i++) {
                if (i % 3 == 0) {
                    node.setStartPosition(Integer.parseInt(nodes[i]));
                } else if (i % 3 == 1) {
                    node.setCurrentRecordsNumber(Integer.parseInt(nodes[i]));
                } else {
                    indexes.add(Integer.parseInt(nodes[i]));
                }
            }
            this.overflowDirectory.add(node);
            index = index + 3;
        }
        for (int x = 0; x < indexes.size(); x++) {
            if (indexes.get(x) != -1) {
                this.overflowDirectory.get(x).setNextBlock(this.overflowDirectory.get(indexes.get(x)));
            }
        }
    }

    private void setMainDirectory(String nodeSettings, FileHandler<T> mainHandler) {
        for (int x = 0; x < Math.pow(2, this.depthOfMainDirectory); x++) {
            this.mainDirectory.add(new ExtendingBlockImpl<>(mainHandler, this.maxNumberInMain));
        }
        String[] data = nodeSettings.split(";");
        int index = 0;
        while (index < data.length) {
            DynamicDirectoryNodeImpl<T, U> node = new ExtendingBlockImpl<>(mainHandler, this.maxNumberInMain);
            int helpIndex = -1;
            int realIndex = -1;
            for (int i = index; i < index + 6; i++) {
                if (i % 6 == 0) {
                    realIndex = Integer.parseInt(data[i]);
                } else if (i % 6 == 1) {
                    helpIndex = Integer.parseInt(data[i]);
                } else if (i % 6 == 2) {
                    node.setStartPosition(Integer.parseInt(data[i]));
                } else if (i % 6 == 3) {
                    node.setCurrentRecordsNumber(Integer.parseInt(data[i]));
                } else if (i % 6 == 4) {
                    int help = Integer.parseInt(data[i]);
                    if (help != -1) {
                        node.setNextBlock(this.getOneByAddress(help));
                    }
                } else {
                    node.setBlockDepth(Integer.parseInt(data[i]));
                }
            }
            if (this.mainDirectory.get(helpIndex).getStartPosition() == -1) {
                this.mainDirectory.set(helpIndex, node);
            } else {
                this.mainDirectory.set(realIndex, this.mainDirectory.get(helpIndex));
            }
            index = index + 6;
        }
    }
}
