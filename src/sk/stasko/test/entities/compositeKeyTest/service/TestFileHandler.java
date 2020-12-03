package sk.stasko.test.entities.compositeKeyTest.service;

import sk.stasko.core.fileHandler.FileHandlerImpl;
import sk.stasko.test.entities.compositeKeyTest.entity.TestKey;
import sk.stasko.test.entities.compositeKeyTest.entity.TestObject;
import sk.stasko.test.entities.personTest.entity.Person;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedList;
import java.util.List;

public class TestFileHandler extends FileHandlerImpl<TestObject, TestKey> {
    public TestFileHandler(RandomAccessFile randomAccessFile) {
        super(randomAccessFile);
    }

    @Override
    public List<TestObject> readBlockByItem(int start, int numberOfItem) throws IOException {
        byte[] bytes = this.read(start, numberOfItem * TestObject.allocateMemory);
        return this.convert(bytes);
    }

    @Override
    public List<TestObject> readBlockByByte(int start, int numberOfBytes) throws IOException {
        byte[] bytes = this.read(start, numberOfBytes);
        return this.convert(bytes);
    }

    private List<TestObject> convert(byte[] bytes) {
        List<TestObject> listOfTests = new LinkedList<>();
        int index = 0;
        while (index < bytes.length) {
            TestObject testObj = new TestObject();
            byte[] testBytes = new byte[TestObject.allocateMemory];
            System.arraycopy(bytes, index, testBytes, 0, testBytes.length);
            testObj.setAttributes(testBytes);
            index += testBytes.length;
            listOfTests.add(testObj);
        }
        return listOfTests;
    }
}
