package sk.stasko.core.extendingHashing.block;

import java.io.IOException;
import java.util.List;

public interface BlockHandler<T> {
    List<T> read() throws IOException;
}
