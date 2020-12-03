package sk.stasko.service;

import sk.stasko.core.extendingHashing.ExtendingHashing;
import sk.stasko.core.extendingHashing.Hashing;
import sk.stasko.core.extendingHashing.overflowingFile.OverflowingFile;
import sk.stasko.core.extendingHashing.overflowingFile.OverflowingFileImpl;
import sk.stasko.core.fileHandler.FileHandler;
import sk.stasko.core.hash.AbstractHash;
import sk.stasko.model.gps.Gps;
import sk.stasko.model.realEstate.RealEstate;
import sk.stasko.model.realEstate.RealEstateKeyHash;
import sk.stasko.util.Helper;

import java.io.IOException;

public class ServiceImpl implements Service<RealEstate> {
    private static Service<RealEstate> instance;
    private final Hashing<RealEstate, Integer> realEstateHashing;
    private final OverflowingFile<RealEstate, Integer> overflowingFile;

    public static void setInstance(FileHandler<RealEstate> mainFile, FileHandler<RealEstate> overflow) {
        if (instance == null) {
            instance = new ServiceImpl(mainFile, overflow);
        }
    }

    public static Service<RealEstate> getInstance() {
        return instance;
    }

    private ServiceImpl(FileHandler<RealEstate> mainFile, FileHandler<RealEstate> overflow) {
        AbstractHash<RealEstate, Integer> hashFunction = new RealEstateKeyHash();
        int numberOfAllowedBits = 7;
        int numberOfRecords = 10;
        this.overflowingFile =
                new OverflowingFileImpl<>(numberOfRecords, RealEstate.allocatedMemory, overflow);
        this.realEstateHashing =
                new ExtendingHashing<>(
                        numberOfRecords,
                        RealEstate.allocatedMemory,
                        numberOfAllowedBits,
                        hashFunction,
                        mainFile,
                        this.overflowingFile
                );
    }

    @Override
    public void add(RealEstate estate) throws IOException {
        this.realEstateHashing.add(estate);
    }

    @Override
    public boolean delete(RealEstate estate) throws IOException {
        return this.realEstateHashing.delete(estate);
    }

    @Override
    public RealEstate find(int id) throws IOException {
        return this.realEstateHashing.find(id);
    }

    public RealEstate createRealEstate(String catalogNum, String desc, String latStr, String lonStr) throws RuntimeException {
        int catalogNumber = Integer.parseInt(catalogNum);
        double lon = Double.parseDouble(lonStr);
        double lat = Double.parseDouble(latStr);
        Helper.checkString(desc);
        return new RealEstate(catalogNumber, desc, new Gps(lat, lon));
    }

    @Override
    public String getMainPart() throws IOException {
        return this.realEstateHashing.printBlocks(RealEstate.allocatedMemory);
    }

    @Override
    public String getMainPartBlank() {
        return this.realEstateHashing.printBlankBlock();
    }

    @Override
    public String getOverflowPart() throws IOException {
        return this.overflowingFile.print();
    }

    @Override
    public String getOverflowPartBlank() {
        return this.overflowingFile.printBlank();
    }
}
