package sk.stasko.core.extendingHashing.block;

public interface ExtendingBlock {
    void setBlockDepth(int newDepth);
    int getDepthOfBlock();
    void incrementDepthBlock();
    void decreaseDepthBlock();
}
