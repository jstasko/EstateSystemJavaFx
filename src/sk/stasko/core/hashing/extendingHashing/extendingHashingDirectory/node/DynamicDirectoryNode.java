package sk.stasko.core.hashing.extendingHashing.extendingHashingDirectory.node;

import sk.stasko.core.hashing.extendingHashing.extendingHashingDirectory.node.block.ExtendingBlock;
import sk.stasko.core.savableObject.SavableObject;

public interface DynamicDirectoryNode<T extends SavableObject<U>, U extends Comparable<U>> extends ExtendingBlock, NodeHandler<T> {
    int getActualNumberOfRecords(boolean isNeighbour);
}
