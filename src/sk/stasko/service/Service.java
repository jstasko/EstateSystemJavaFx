package sk.stasko.service;

import java.io.IOException;

public interface Service<T> extends PrintService {
    void add(T item) throws IOException;
    boolean delete(T item) throws IOException;
    T find(int id) throws IOException;
    T createRealEstate(String catalogNum, String desc, String latStr, String lonStr) throws RuntimeException;
}
