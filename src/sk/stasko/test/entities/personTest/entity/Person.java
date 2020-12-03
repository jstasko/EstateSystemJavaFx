package sk.stasko.test.entities.personTest.entity;

import sk.stasko.core.converter.ByteConverter;
import sk.stasko.core.savableObject.SavableObjectImpl;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class Person extends SavableObjectImpl<Integer> {
    public static int allocatedMem = 4*Integer.BYTES + 2*allowStringBytes;
    public static final AtomicInteger idGen = new AtomicInteger(0);
    private int id;
    private String name;
    private String lastName;

    public Person(String name, String lastName) {
        super(allocatedMem);
        this.id = idGen.getAndIncrement();
        this.name = name;
        this.lastName = lastName;
        this.setKey(this.id);
    }

    public Person(int id,String name, String lastName) {
        super(allocatedMem);
        this.id = id;
        this.name = name;
        this.lastName = lastName;
        this.setKey(this.id);
    }

    public Person() {
        super(allocatedMem);
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public byte[] map() {
        byte[] allByteArray = new byte[Person.allocatedMem];
        ByteBuffer buff = ByteBuffer.wrap(allByteArray);
        buff.put(ByteConverter.intToBytes(this.getKey()));
        buff.put(ByteConverter.intToBytes(this.id));
        buff.put(ByteConverter.stringToBytes(this.name, allowStringBytes));
        buff.put(ByteConverter.stringToBytes(this.lastName, allowStringBytes));

        return buff.array();
    }

    @Override
    public void setAttributes(byte[] attributes) {
        int index = 0;
        this.setKey(ByteConverter.bytesIntoInt(attributes, index, Integer.BYTES));
        index += Integer.BYTES;
        this.setId(ByteConverter.bytesIntoInt(attributes, index, index + Integer.BYTES));
        index += Integer.BYTES;
        byte[] stringArray = Arrays.copyOfRange(attributes, index, index + Person.getAllowStringBytes());
        index += Person.getAllowStringBytes();
        int length = ByteConverter.bytesIntoInt(attributes, index, index + Integer.BYTES);
        this.setName(ByteConverter.bytesToString(stringArray, index, index + Person.getAllowStringBytes() ,length));
        index += Integer.BYTES;
        stringArray = Arrays.copyOfRange(attributes, index, index + Person.getAllowStringBytes());
        index += Person.getAllowStringBytes();
        length = ByteConverter.bytesIntoInt(attributes, index, index + Integer.BYTES);
        this.setLastName(ByteConverter.bytesToString(stringArray, index, index + Person.getAllowStringBytes() ,length));
    }

    @Override
    public int getNumberOfBytes() {
        return this.getAllocatedMemory();
    }
}
