package sk.stasko.test;

import sk.stasko.core.fileHandler.FileHandlerImpl;
import sk.stasko.core.hashing.extendingHashing.overflowingDirectory.OverflowingDirectory;
import sk.stasko.core.savableObject.SavableObjectImpl;
import sk.stasko.core.hashing.extendingHashing.ExtendingHashing;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class AbstractTest<T extends SavableObjectImpl<U>, U extends Comparable<U>> {
    protected static final int numberOfRecord = 2;
    protected final ExtendingHashing<T, U> extendibleHashing;
    protected final List<T> list;
    protected Random random;
    protected FileHandlerImpl<T, U> fileHandler;
    protected OverflowingDirectory<T ,U> o;

    public AbstractTest(Random random, FileHandlerImpl<T, U> fileHandler, ExtendingHashing<T, U> extendibleHashing, OverflowingDirectory<T, U> o) {
        this.fileHandler = fileHandler;
        this.extendibleHashing = extendibleHashing;
        this.list = new LinkedList<>();
        this.random = random;
        this.o = o;
    }

    public void addingList(int seed) throws IOException {
        Collections.shuffle(this.list);
        for (T item : this.list) {
            extendibleHashing.add(item);
        }
        System.out.println("ADDED TO Dictionary");
        System.out.println("--");
        List<T> findOneList = findAllElements(seed);
        int lengthOfFounds = convertListToByteArray(findOneList).length;
        int lengthOfList = convertListToByteArray(this.list).length;
        this.findWholeList(seed);
        if (lengthOfFounds != lengthOfList) {
            throw new RuntimeException("Error in length " + seed);
        } else {
            System.out.println("FOUND ALL Records");
            System.out.println("--");
        }
    }

    public void integrationTest(int seed) {
        try {
            this.addingList(seed);
            int count = 0;
            for (T item: this.list) {
                this.extendibleHashing.delete(item);
                T s =  this.extendibleHashing.find(item.getKey());
                if (s != null) {
                    throw new RuntimeException("ERROR IN SINGLE DELETE seed " + seed + " item " + s.getKey().toString());
                }
            }
            System.out.println("DELETE");
            for (T item: this.list) {
                T s = this.extendibleHashing.find(item.getKey());
                if (s != null) {
                    throw new RuntimeException("ERROR in DELETE + " + seed + " key " + count);
                }
                count++;
            }
            System.out.println("DELETED");
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.getStackTrace();
        }
    }

    private byte[] convertListToByteArray(List<T> listOfItems) {
        byte[] arrHelp = new byte[list.size() * listOfItems.get(0).getAllocatedMemory()];
        int index = 0;
        for(T item: listOfItems) {
            byte[] itemBytes = item.map();
            System.arraycopy(itemBytes, 0, arrHelp, index, item.getAllocatedMemory());
            index += item.getAllocatedMemory();
        }
        return arrHelp;
    }

    public List<T> findAllElements(int seed) {
        List<T> findOneList = new LinkedList<>();
        int index = 1;
        try {
            for (T item : this.list) {
                T f = extendibleHashing.find(item.getKey());
                if (f == null) {
                    throw new RuntimeException("I have did not found one - seed " + seed + " element " + index + " key " + item.getKey().toString());
                }
                findOneList.add(f);
                index++;
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.getStackTrace();
        }
        System.out.println("-------");
        return findOneList;
    }

    public void clear() {
        try {
            this.fileHandler.newLengthOfFile(0);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.getStackTrace();
        }
    }

    public void findWholeList(int iteration) {
        List<T> findOneList = findAllElements(iteration);
        for (T item: this.list) {
            boolean found = false;
            for (T item2: findOneList) {
                if (item.getKey().compareTo(item2.getKey()) == 0) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new RuntimeException("ERROR in Found adding + " + iteration + " key " + item.getKey());
            }
        }
    }
}
