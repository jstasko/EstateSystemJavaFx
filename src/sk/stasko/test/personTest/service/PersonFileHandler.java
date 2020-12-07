package sk.stasko.test.personTest.service;

import sk.stasko.core.fileHandler.FileHandlerImpl;
import sk.stasko.test.personTest.entity.Person;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedList;
import java.util.List;

public class PersonFileHandler extends FileHandlerImpl<Person, Integer> {

    public PersonFileHandler(RandomAccessFile randomAccessFile) {
        super(randomAccessFile);
    }

    @Override
    public List<Person> readBlockByItem(int start, int numberOfItem) throws IOException {
        byte[] bytes = this.read(start, numberOfItem * Person.allocatedMem);
        return convert(bytes);
    }

    @Override
    public byte[] readBlockByByte(int start, int numberOfBytes) throws IOException {
        return this.read(start, numberOfBytes);
    }

    @Override
    public List<Person> convert(byte[] bytes) {
        List<Person> listOfPersons = new LinkedList<>();
        int index = 0;
        while (index < bytes.length) {
            Person person = new Person();
            byte[] personBytes = new byte[Person.allocatedMem];
            System.arraycopy(bytes, index, personBytes, 0, personBytes.length);
            person.setAttributes(personBytes);
            index += personBytes.length;
            listOfPersons.add(person);
        }
        return listOfPersons;
    }
}
