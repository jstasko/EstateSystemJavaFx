package sk.stasko.core.hashing.extendingHashing;

public interface OverflowingHandler<T> {
    void setNextBlock(T nextBlock);
    T getNextBlock();
}
