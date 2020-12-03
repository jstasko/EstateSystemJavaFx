package sk.stasko.core.fileHandler;

import java.io.IOException;
import java.util.List;

public interface FileHandler<T> {
    void write(List<T> dataToSave, int startPosition, int numberOfRecords) throws IOException;
    void write(T data, int startPosition) throws IOException;
    List<T> readBlockByItem(int start, int numberOfItem) throws IOException;
    List<T> readBlockByByte(int start, int numberOfBytes) throws IOException;
    void newLengthOfFile(int length) throws IOException;
}
