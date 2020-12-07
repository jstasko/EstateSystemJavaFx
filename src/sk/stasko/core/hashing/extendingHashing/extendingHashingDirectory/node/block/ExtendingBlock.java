package sk.stasko.core.hashing.extendingHashing.extendingHashingDirectory.node.block;

public interface ExtendingBlock {
    void setBlockDepth(int newDepth);
    int getDepthOfBlock();
    void incrementDepthBlock();
    void decreaseDepthBlock();
}
