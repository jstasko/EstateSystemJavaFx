package sk.stasko.test;

import sk.stasko.test.personTest.PersonTest;

import java.io.IOException;
import java.util.Random;

public class TesterMain {
    public static void main(String[] args) {
        try {
            PersonTest.firstTest();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.getStackTrace();
        }
    }

    public static String getString(Random rnd, int lengthOfString) {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        while (salt.length() < lengthOfString) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        return salt.toString();
    }
}
