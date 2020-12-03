package sk.stasko.core.extendingHashing.block;

public interface Block<T> extends BlockHandler<T> {
    void clearData();
    boolean isFull();
    int getNumberOfRecords();
    void setCurrentRecordsNumber(int number);
}
