package sk.stasko.test.entities.compositeKeyTest.entity;

import sk.stasko.core.converter.ByteConverter;
import sk.stasko.core.savableObject.SavableObjectImpl;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class TestObject extends SavableObjectImpl<TestKey> {
    public static final int allocateMemory = Integer.BYTES + TestKey.allocateMemory;
    private static final AtomicInteger idGen = new AtomicInteger(0);
    private int id;

    public TestObject(TestKey key) {
        super(key, allocateMemory);
        this.id = idGen.getAndIncrement();
    }

    public TestObject() {
        super(allocateMemory);
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public byte[] map() {
        byte[] allByteArray = new byte[TestObject.allocateMemory];
        ByteBuffer buff = ByteBuffer.wrap(allByteArray);
        buff.put(this.getKey().map());
        buff.put(ByteConverter.intToBytes(this.id));
        return buff.array();
    }

    @Override
    public void setAttributes(byte[] attributes) {
        int index = 0;
        this.setKey(new TestKey());
        this.getKey().setAttributes(Arrays.copyOfRange(attributes, index, index + TestKey.allocateMemory));
        index += TestKey.allocateMemory;
        this.setId(ByteConverter.bytesIntoInt(attributes, index, index + Integer.BYTES));
    }

    @Override
    public int getNumberOfBytes() {
        return this.getAllocatedMemory();
    }
}
