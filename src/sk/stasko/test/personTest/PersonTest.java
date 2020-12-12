package sk.stasko.test.personTest;

import sk.stasko.core.fileHandler.FileHandlerImpl;
import sk.stasko.core.hashing.extendingHashing.ExtendingHashing;
import sk.stasko.core.hashing.extendingHashing.overflowingDirectory.OverflowingDirectory;
import sk.stasko.core.hashing.extendingHashing.overflowingDirectory.OverflowingDirectoryImpl;
import sk.stasko.test.AbstractTest;
import sk.stasko.test.TesterMain;
import sk.stasko.test.personTest.entity.Person;
import sk.stasko.test.personTest.service.PersonFileHandler;
import sk.stasko.test.personTest.service.PersonKeyHash;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Random;

public class PersonTest extends AbstractTest<Person, Integer> {
    private static final String filename = "person.dat";
    private PersonTest(Random random, ExtendingHashing<Person, Integer> extendibleHashing, FileHandlerImpl<Person, Integer> fileHandler, OverflowingDirectory<Person, Integer> o) {
        super(random, fileHandler, extendibleHashing, o);
    }

    public void addToList() {
        for (int i = 0; i < 10000; i++) {
            this.list.add(new Person(TesterMain.getString(random, random.nextInt(20)), TesterMain.getString(random,random.nextInt(20))));
        }
        System.out.println("ADDED TO LIST");
        System.out.println("--");
    }

    public void randomOperation(int iteration) throws IOException {
        double decision = Math.random();
        if (decision < 0.33) {
            System.out.println("FINDING ALL ELEMENTS _ SEED " + iteration );
            this.findAllElements(iteration);
            System.out.println("--------");
        } else if (decision >= 0.33 && decision < 0.66) {
            int index = random.nextInt(this.list.size() - 1);
            Person person = this.list.get(index);
            System.out.println("REMOVING SEED _ " + iteration + " removing key " + person.getKey());
            Person foundIt = this.extendibleHashing.find(person.getKey());
            if (foundIt == null) {
                throw new RuntimeException("Error in delete, found first " + iteration + " key " + person.getKey());
            }
            this.extendibleHashing.delete(person);
            this.list.remove(person);
            Person notFoundIt = this.extendibleHashing.find(person.getKey());
            if (notFoundIt != null) {
                throw new RuntimeException("Error in delete, found first " + iteration + " key " + person.getKey());
            }
            this.findWholeList(iteration);
            System.out.println("--------");
        } else {
            System.out.println("ADDING SEED _ " + iteration);
            Person person = new Person(TesterMain.getString(random, random.nextInt(20)), TesterMain.getString(random,random.nextInt(20)));
            this.list.add(person);
            this.extendibleHashing.add(person);
            Person foundIt = this.extendibleHashing.find(person.getKey());
            if (foundIt == null) {
                throw new RuntimeException("FOUNDIT in ADDED IS ERROR key " + person.getKey() + " iteration " + iteration);
            }
            if (person.getKey().compareTo(foundIt.getKey()) != 0) {
                throw new RuntimeException("ERROR in adding key " + person.getKey() + " iteration " + iteration);
            }
            this.findWholeList(iteration);
            System.out.println("--------");
        }
    }

    public static void firstTest() throws IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile(filename, "rw");
        RandomAccessFile overFile = new RandomAccessFile("personOverflow.dat", "rw");
        for (int i = 0; i < 1000; i++) {
            Person.idGen.set(0);
            System.out.println("SEED " + i);
            FileHandlerImpl<Person, Integer> fileHandler = new PersonFileHandler(randomAccessFile);
            FileHandlerImpl<Person, Integer> handlerOver = new PersonFileHandler(overFile);
            Random random = new Random();
            OverflowingDirectory<Person, Integer> o = new OverflowingDirectoryImpl<>(20, Person.allocatedMem, handlerOver);
            ExtendingHashing<Person, Integer> extendibleHashing = new ExtendingHashing<>(10, Person.allocatedMem, 7,new PersonKeyHash(), fileHandler, o);
            PersonTest personTest = new PersonTest(random, extendibleHashing, fileHandler,o);
            random.setSeed(i);
            personTest.addToList();
            personTest.integrationTest(i);
            personTest.clear();
        }
    }

    public static void secondTest() throws IOException {
            RandomAccessFile randomAccessFile = new RandomAccessFile(filename, "rw");
            RandomAccessFile over = new RandomAccessFile("personOverflow.dat", "rw");
            Person.idGen.set(0);
            FileHandlerImpl<Person, Integer> fileHandler = new PersonFileHandler(randomAccessFile);
            FileHandlerImpl<Person, Integer> overHandler = new PersonFileHandler(over);
            Random random = new Random();
            OverflowingDirectory<Person, Integer> o = new OverflowingDirectoryImpl<>(30, Person.allocatedMem, overHandler);
            ExtendingHashing<Person, Integer> extendibleHashing = new ExtendingHashing<>(30, Person.allocatedMem, 7, new PersonKeyHash(), fileHandler, o);
            PersonTest personTest = new PersonTest(random, extendibleHashing, fileHandler,o);
            personTest.addToList();
            personTest.addingList(0);
            for (int i = 0; i < 10000; i++) {
                personTest.randomOperation(i);
            }
            randomAccessFile.setLength(0);
            over.setLength(0);
            randomAccessFile.close();
            over.close();
    }
}
