package sk.stasko.core.hashing.extendingHashing.extendingHashingDirectory.node;

import java.util.List;

public interface DynamicDirectoryNodeHandler<T> {
    void clearTemporaryList();
    void addToTemporaryList(T item);
    List<T> getTemporaryList();
}
