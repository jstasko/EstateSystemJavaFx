package sk.stasko.core.savableObject;

public abstract class SavableObjectImpl<T extends Comparable<T>> implements SavableObject<T> {
    private T key;
    protected final static int allowStringBytes = 20;
    protected final int allocatedMemory;

    public SavableObjectImpl(int allocatedMemory) {
        this.allocatedMemory = allocatedMemory;
    }

    public SavableObjectImpl() {
        allocatedMemory = 0;
    }

    public SavableObjectImpl(T key, int allocatedMemory) {
        this.key = key;
        this.allocatedMemory = allocatedMemory;
    }

    @Override
    public T getKey() {
        return this.key;
    }

    protected void setKey(T key) {
        this.key = key;
    }

    public static int getAllowStringBytes() {
        return allowStringBytes;
    }

    public int getAllocatedMemory() {
        return allocatedMemory;
    }
}
