package sk.stasko.test.entities.compositeKeyTest;

import sk.stasko.core.fileHandler.FileHandlerImpl;
import sk.stasko.core.extendingHashing.ExtendingHashing;
import sk.stasko.core.extendingHashing.overflowingFile.OverflowingFile;
import sk.stasko.core.extendingHashing.overflowingFile.OverflowingFileImpl;
import sk.stasko.test.AbstractTest;
import sk.stasko.test.entities.compositeKeyTest.entity.TestKey;
import sk.stasko.test.entities.compositeKeyTest.entity.TestObject;
import sk.stasko.test.entities.compositeKeyTest.service.TestFileHandler;
import sk.stasko.test.entities.compositeKeyTest.service.TestKeyHash;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Random;

public class Test extends AbstractTest<TestObject, TestKey> {
    private static final String filename = "test.dat";

    private Test(Random random, FileHandlerImpl<TestObject, TestKey> fileHandler, ExtendingHashing<TestObject, TestKey> extendibleHashing, OverflowingFile<TestObject, TestKey>overflowingFile) {
        super(random, fileHandler, extendibleHashing, overflowingFile);
    }

    public void addToList() {
        for (int i = 0; i < 10; i++) {
            this.list.add(new TestObject(new TestKey(random.nextInt(100), random.nextInt(100))));
        }
    }

    public static void runTest() throws IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile(filename, "rw");
        RandomAccessFile overFile = new RandomAccessFile(filename, "rw");
        for (int i = 0; i < 1; i++) {
            FileHandlerImpl<TestObject, TestKey> fileHandler = new TestFileHandler(randomAccessFile);
            FileHandlerImpl<TestObject, TestKey> fileHandlerOver = new TestFileHandler(overFile);
            OverflowingFile<TestObject, TestKey> o = new OverflowingFileImpl<>(numberOfRecord, TestObject.allocateMemory, fileHandlerOver);
            ExtendingHashing<TestObject, TestKey> extendibleHashing = new ExtendingHashing<>(numberOfRecord, 3,TestObject.allocateMemory, new TestKeyHash(), fileHandler, o);
            Random random = new Random();
            Test test = new Test(random, fileHandler, extendibleHashing ,o);
            random.setSeed(i);
            test.addToList();
            test.integrationTest(i);
            test.findAllElements(i);
            test.clear();
        }
        randomAccessFile.close();
    }
}
