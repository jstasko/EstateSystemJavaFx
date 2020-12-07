package sk.stasko.core.hashing.block;

public interface Block<T> extends BlockHandler<T> {
    void clearData();
    boolean isFull();
    int getNumberOfRecords();
    void setCurrentRecordsNumber(int number);
}
