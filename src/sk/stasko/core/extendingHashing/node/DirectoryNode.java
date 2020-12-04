package sk.stasko.core.extendingHashing.node;

import sk.stasko.core.extendingHashing.block.ExtendingBlock;
import sk.stasko.core.savableObject.SavableObject;

import java.util.List;

public interface DirectoryNode<T extends SavableObject<U>, U extends Comparable<U>> extends ExtendingBlock, NodeHandling<T> {
    int getActualNumberOfRecords(boolean isNeighbour);
}
