package sk.stasko.service;

import java.io.IOException;

public interface PrintService {
    String getMainPart() throws IOException;
    String getMainPartBlank();
    String getOverflowPart() throws IOException;
    String getOverflowPartBlank();
}
