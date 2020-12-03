package sk.stasko.core.extendingHashing.node;

import java.util.List;

public interface NodeHandling<T> {
    void clearTemporaryList();
    void addToTemporaryList(T item);
    List<T> getTemporaryList();
}
