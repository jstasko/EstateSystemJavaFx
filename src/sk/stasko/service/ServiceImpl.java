package sk.stasko.service;

import sk.stasko.core.hashing.extendingHashing.ExtendingHashing;
import sk.stasko.core.hashing.extendingHashing.Hashing;
import sk.stasko.core.hashing.extendingHashing.overflowingDirectory.OverflowingDirectoryImpl;
import sk.stasko.core.fileHandler.FileHandler;
import sk.stasko.core.hashFunction.AbstractHash;
import sk.stasko.model.gps.Gps;
import sk.stasko.model.realEstate.RealEstate;
import sk.stasko.model.realEstate.RealEstateKeyHash;
import sk.stasko.util.Helper;

import java.io.IOException;

public class ServiceImpl implements Service<RealEstate> {
    private static Service<RealEstate> instance;
    private final Hashing<RealEstate, Integer> realEstateHashing;

    public static void setInstance(FileHandler<RealEstate> mainFile, FileHandler<RealEstate> overflow) throws IOException {
        if (instance == null) {
            instance = new ServiceImpl(mainFile, overflow);
        }
    }

    public static void setInstance(int numberOfRecords, int numberOfAllowedBits, int numberInOverflow,
                                   FileHandler<RealEstate> mainFile,
                                   FileHandler<RealEstate> overflow) throws IOException {
        if (instance == null) {
            instance = new ServiceImpl(numberOfRecords, numberOfAllowedBits, numberInOverflow,mainFile, overflow);
        }
    }

    public static Service<RealEstate> getInstance() {
        return instance;
    }

    private ServiceImpl(int numberOfRecords, int numberOfAllowedBits, int numberInOverflow,
                        FileHandler<RealEstate> mainFile,
                        FileHandler<RealEstate> overflow) throws IOException {
        AbstractHash<RealEstate, Integer> hashFunction = new RealEstateKeyHash();
        this.realEstateHashing =
                new ExtendingHashing<>(
                        numberOfRecords,
                        RealEstate.allocatedMemory,
                        numberOfAllowedBits,
                        hashFunction,
                        mainFile,
                        new OverflowingDirectoryImpl<>(numberInOverflow, RealEstate.allocatedMemory, overflow)
                );
    }

    public ServiceImpl(FileHandler<RealEstate> mainFile, FileHandler<RealEstate> overflow) throws IOException {
            this.realEstateHashing =
                    new ExtendingHashing<>(
                            RealEstate.allocatedMemory,
                            mainFile, overflow,
                            new RealEstateKeyHash());
            RealEstate.idGen.set(this.realEstateHashing.getManagementFile().getId());
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
    public void saveSettings() throws IOException {
        this.realEstateHashing.saveSettings(RealEstate.idGen.get());
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
        return this.realEstateHashing.getOverflowPart();
    }

    @Override
    public String getOverflowPartBlank() {
        return this.realEstateHashing.getOverflowPartBlank();
    }
}
