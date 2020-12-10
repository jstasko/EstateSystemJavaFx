package sk.stasko.core.hashing.extendingHashing;

import java.util.List;

public interface NodeHandler<T> {
    void clearTemporaryList();
    void addToTemporaryList(T item);
    List<T> getTemporaryList();
}
