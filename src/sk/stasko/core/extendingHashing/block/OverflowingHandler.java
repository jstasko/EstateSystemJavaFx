package sk.stasko.core.extendingHashing.block;

public interface OverflowingHandler<T> {
    void setNextBlock(T nextBlock);
    T getNextBlock();
}
