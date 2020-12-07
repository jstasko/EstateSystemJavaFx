package sk.stasko.core.hashing.directory;

import sk.stasko.core.hashing.node.DirectoryNode;
import sk.stasko.core.savableObject.SavableObject;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public  abstract class DirectoryImpl<T extends List<S>, U, S extends DirectoryNode<X, Y>, X extends SavableObject<Y>, Y extends Comparable<Y>> implements Directory<S, U> {
    protected T directory;

    public DirectoryImpl(T directory) {
        this.directory = directory;
    }

    public S getOne(U key) {
        int index = (int) key;
        if (index < this.directory.size()) {
            return this.directory.get(index);
        }
        return null;
    }

    @Override
    public int sizeOfDirectory() {
        return this.directory.size();
    }

    @Override
    public void setOne(U key, S item)  {
        int index = (int) key;
        this.directory.set(index, item);
    }

    @Override
    public int startPositionOfLastAllocatedBlock() {
        return Collections.max(this.directory.stream().map(S::getStartPosition)
                .collect(Collectors.toList()));
    }

    @Override
    public String toString(byte[] records, int sizeOfRecord) {
        List<S> helper = this.directory
                .stream()
                .sorted(Comparator.comparingInt(S::getStartPosition))
                .distinct()
                .collect(Collectors.toList());
        String concatString = "";
        for(S i: helper) {
            concatString = concatString.concat(i.toString(records, sizeOfRecord));
        }
        return concatString;
    }
}
