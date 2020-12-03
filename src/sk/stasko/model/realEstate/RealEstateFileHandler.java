package sk.stasko.model.realEstate;

import sk.stasko.core.fileHandler.FileHandlerImpl;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedList;
import java.util.List;

public class RealEstateFileHandler extends FileHandlerImpl<RealEstate, Integer> {

    public RealEstateFileHandler(RandomAccessFile randomAccessFile) {
        super(randomAccessFile);
    }

    @Override
    public List<RealEstate> readBlockByItem(int start, int numberOfItem) throws IOException {
        byte[] bytes = this.read(start, numberOfItem *RealEstate.allocatedMemory);
        return this.convert(bytes);
    }

    @Override
    public List<RealEstate> readBlockByByte(int start, int numberOfBytes) throws IOException {
        byte[] bytes = this.read(start, numberOfBytes);
        return this.convert(bytes);
    }

    private List<RealEstate> convert(byte[] bytes) {
        List<RealEstate> estates = new LinkedList<>();
        int index = 0;
        while (index < bytes.length) {
            RealEstate realEstate = new RealEstate();
            byte[] estateBytes = new byte[RealEstate.allocatedMemory];
            System.arraycopy(bytes, index, estateBytes, 0, estateBytes.length);
            realEstate.setAttributes(estateBytes);
            index += estateBytes.length;
            estates.add(realEstate);
        }
        return estates;
    }
}
