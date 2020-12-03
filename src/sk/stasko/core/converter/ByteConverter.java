package sk.stasko.core.converter;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;

public final class ByteConverter {
    public static String bytesToString(byte[] b,int startIndex, int endIndex, int stringLength) {
        byte[] helpArray = Arrays.copyOfRange(b, 0, stringLength);
        Charset charset = StandardCharsets.US_ASCII;
        return charset.decode(ByteBuffer.wrap(helpArray))
                .toString();
    }
    public static byte[] stringToBytes(final String i, int lengthOfString) {
        if (i.length() > lengthOfString) {
            throw new RuntimeException("Bad length");
        }
        Charset charset = StandardCharsets.US_ASCII;
        byte[] bArray = charset.encode(i).array();
        byte[] returnArray = new byte[lengthOfString + Integer.BYTES];
        System.arraycopy(bArray, 0, returnArray, 0, bArray.length);
        byte[] length = intToBytes(i.length());
        System.arraycopy(length, 0, returnArray,lengthOfString, Integer.BYTES);
        return returnArray;
    }

    public static byte[] doubleToBytes(final double i) {
        ByteBuffer bb = ByteBuffer.allocate(Double.BYTES);
        bb.putDouble(i);
        return bb.array();
    }

    public static double bytesIntoDouble(byte[] b, int startIndex, int endIndex) {
        byte[] helpArray = Arrays.copyOfRange(b, startIndex, endIndex);
        ByteBuffer bb = ByteBuffer.wrap(helpArray);
        return bb.getDouble();
    }

    public static byte[] intToBytes(final int i ) {
        ByteBuffer bb = ByteBuffer.allocate(Integer.BYTES);
        bb.putInt(i);
        return bb.array();
    }

    public static int bytesIntoInt(byte[] b, int startIndex, int endIndex) {
        byte[] helpArray = Arrays.copyOfRange(b, startIndex, endIndex);
        ByteBuffer bb = ByteBuffer.wrap(helpArray);
        return bb.getInt();
    }
}
