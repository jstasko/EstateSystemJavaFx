package sk.stasko.core.fileHandler;

import sk.stasko.core.savableObject.SavableObject;

import java.io.*;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.List;

public abstract class FileHandlerImpl<T extends SavableObject<U>, U extends Comparable<U>> implements FileHandler<T> {
    private final RandomAccessFile file;
    public FileHandlerImpl(RandomAccessFile randomAccessFile) {
        file = randomAccessFile;
    }

    @Override
    public void write(List<T> dataToSave, int startPosition, int numberOfRecords) throws IOException {
        if (dataToSave.size() < 1) {
            return;
        }
        ByteBuffer buff = getDataToByteBuffer(dataToSave);
        writeBytes(buff, startPosition);
    }

    @Override
    public void write(T data, int startPosition) throws IOException {
        byte[] bytes = new byte[data.getAllocatedMemory()];
        ByteBuffer buff = ByteBuffer.wrap(bytes);
        buff.put(data.map());
        writeBytes(buff, startPosition);
    }

    @Override
    public void newLengthOfFile(int length) throws IOException {
        file.setLength(length);
    }

    protected byte[] read(int startAt, int len) throws IOException, BufferOverflowException {
        file.seek(startAt);
        byte[] bytes = new byte[len];
        file.read(bytes);
        return bytes;
    }

    private ByteBuffer getDataToByteBuffer(List<T> dataToSave) {
        byte[] bytes = new byte[dataToSave.get(0).getAllocatedMemory() * dataToSave.size()];
        ByteBuffer buff = ByteBuffer.wrap(bytes);
        dataToSave.forEach(item -> buff.put(item.map()));
        return buff;
    }

    private void writeBytes(ByteBuffer buff, int startAt) throws IOException {
        file.seek(startAt);
        file.write(buff.array());
    }
}
