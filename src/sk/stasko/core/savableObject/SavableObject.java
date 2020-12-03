package sk.stasko.core.savableObject;

public interface SavableObject<T> {
    T getKey();
    int getAllocatedMemory();
    byte[] map();
    void setAttributes(byte[] attributes);
    int getNumberOfBytes();
}
