package sk.stasko.test.entities.compositeKeyTest.entity;

import sk.stasko.core.converter.ByteConverter;
import sk.stasko.core.savableObject.SavableObjectImpl;

import java.nio.ByteBuffer;
public class TestKey extends SavableObjectImpl<Integer> implements Comparable<TestKey> {
    public static final int allocateMemory = 2*Integer.BYTES;
    private int one;
    private int two;

    public TestKey(int one, int two) {
        super(allocateMemory);
        this.one = one;
        this.two = two;
    }

    public TestKey() {
        super(allocateMemory);
    }

    public void setOne(int one) {
        this.one = one;
    }

    public void setTwo(int two) {
        this.two = two;
    }

    public int getOne() {
        return one;
    }

    public int getTwo() {
        return two;
    }

    @Override
    public byte[] map() {
        byte[] allByteArray = new byte[TestKey.allocateMemory];
        ByteBuffer buff = ByteBuffer.wrap(allByteArray);
        buff.put(ByteConverter.intToBytes(this.one));
        buff.put(ByteConverter.intToBytes(this.two));
        return buff.array();
    }

    @Override
    public void setAttributes(byte[] attributes) {
        int index = 0;
        this.setOne(ByteConverter.bytesIntoInt(attributes, index, Integer.BYTES));
        index += Integer.BYTES;
        this.setTwo(ByteConverter.bytesIntoInt(attributes, index, index + Integer.BYTES));
    }

    @Override
    public int getNumberOfBytes() {
        return this.getAllocatedMemory();
    }

    @Override
    public int compareTo(TestKey o) {
        if (this.getOne() == o.getOne() &&
                this.getTwo() == o.getTwo()) {
            return 0;
        }
        return -1;
    }

    @Override
    public String toString() {
        return " First Key " + one +
                ", Second Key " + two;
    }
}
