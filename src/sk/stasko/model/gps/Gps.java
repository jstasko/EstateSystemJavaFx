package sk.stasko.model.gps;

import sk.stasko.core.converter.ByteConverter;
import sk.stasko.core.savableObject.SavableObjectImpl;

import java.nio.ByteBuffer;

public class Gps extends SavableObjectImpl<Integer> {
    public static final int allocatedMemory = 2*Double.BYTES;
    private double latitude;
    private double longitude;

    public Gps(double lat, double lon) {
        super(allocatedMemory);
        this.latitude = lat;
        this.longitude = lon;
    }

    public Gps() {}

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public int getNumberOfBytes() {
        return this.getAllocatedMemory();
    }

    @Override
    public byte[] map() {
        byte[] allByteArray = new byte[Gps.allocatedMemory];
        ByteBuffer buff = ByteBuffer.wrap(allByteArray);
        buff.put(ByteConverter.doubleToBytes(this.latitude));
        buff.put(ByteConverter.doubleToBytes(this.longitude));
        return buff.array();
    }

    @Override
    public void setAttributes(byte[] attributes) {
        this.setLatitude(ByteConverter.bytesIntoDouble(attributes, 0, allocatedMemory/2));
        this.setLongitude(ByteConverter.bytesIntoDouble(attributes, allocatedMemory/2, allocatedMemory));
    }

    @Override
    public String toString() {
        return " latitude : " + latitude +
                ", longitude : " + longitude;
    }
}
