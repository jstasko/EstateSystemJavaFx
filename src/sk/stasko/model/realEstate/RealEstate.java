package sk.stasko.model.realEstate;

import sk.stasko.core.converter.ByteConverter;
import sk.stasko.core.savableObject.SavableObjectImpl;
import sk.stasko.model.gps.Gps;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class RealEstate extends SavableObjectImpl<Integer> {
    public static final int allocatedMemory = 4*Integer.BYTES + allowStringBytes + Gps.allocatedMemory;
    private static final AtomicInteger idGen = new AtomicInteger(0);
    private int id;
    private int catalogNumber;
    private String description;
    private Gps gps;

    public RealEstate(int catalogNumber, String description, Gps gps) {
        super(allocatedMemory);
        this.id = idGen.getAndIncrement();
        this.catalogNumber = catalogNumber;
        this.description = description;
        this.gps = gps;
        this.setKey(this.id);
    }

    public RealEstate() {
        super(allocatedMemory);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCatalogNumber() {
        return catalogNumber;
    }

    public void setCatalogNumber(int catalogNumber) {
        this.catalogNumber = catalogNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Gps getGps() {
        return gps;
    }

    public void setGps(Gps gps) {
        this.gps = gps;
    }

    @Override
    public int getNumberOfBytes() {
        return this.getAllocatedMemory();
    }

    @Override
    public byte[] map() {
        byte[] allByteArray = new byte[RealEstate.allocatedMemory];
        ByteBuffer buff = ByteBuffer.wrap(allByteArray);
        buff.put(ByteConverter.intToBytes(this.getKey()));
        buff.put(ByteConverter.intToBytes(this.id));
        buff.put(ByteConverter.intToBytes(this.catalogNumber));
        buff.put(this.gps.map());
        buff.put(ByteConverter.stringToBytes(this.description, allowStringBytes));

        return buff.array();
    }

    @Override
    public void setAttributes(byte[] attributes) {
        int index = 0;
        this.setKey(ByteConverter.bytesIntoInt(attributes, index, Integer.BYTES));
        index += Integer.BYTES;
        this.setId(ByteConverter.bytesIntoInt(attributes, index, index + Integer.BYTES));
        index += Integer.BYTES;
        this.setCatalogNumber(ByteConverter.bytesIntoInt(attributes, index, index + Integer.BYTES));
        index += Integer.BYTES;
        this.setGps(new Gps());
        this.gps.setAttributes(Arrays.copyOfRange(attributes, index, index + Gps.allocatedMemory));
        index += Gps.allocatedMemory;
        byte[] stringArray = Arrays.copyOfRange(attributes, index, index + RealEstate.getAllowStringBytes());
        index += RealEstate.getAllowStringBytes();
        int length = ByteConverter.bytesIntoInt(attributes, index, index + Integer.BYTES);
        this.setDescription(ByteConverter.bytesToString(stringArray, index, index + RealEstate.getAllowStringBytes() ,length));
    }

    @Override
    public String toString() {
        return "id : " + id +
                ", catalogNumber : " + catalogNumber +
                ", description : " + description +
                ", gps : " + gps.toString();
    }
}
