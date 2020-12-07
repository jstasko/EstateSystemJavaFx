package sk.stasko;

import sk.stasko.model.gps.Gps;
import sk.stasko.model.realEstate.RealEstate;
import sk.stasko.service.ServiceImpl;

import java.io.IOException;
import java.util.Random;

public class AppGen {
    /**
     * @param numberOfItems - number of items to be generated
     */
    public static void generateRealEstates(int numberOfItems, Random random) throws IOException {
        for (int x = 0; x < numberOfItems; x++) {
            if (x % 10000 == 0) {
                System.out.println(x);
            }
            int number = random.nextInt(100);
            RealEstate realEstate = new RealEstate(number, getDesc(), generateGps(random.nextInt(1000)));
            ServiceImpl.getInstance().add(realEstate);
        }
    }

    /**
     * @return random String
     */
    private static String getDesc() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 19) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        return salt.toString();
    }

    /**
     * @param seed - seed for generator
     * @return random GPS generated
     */
    public static Gps generateGps(int seed) {
        Random random = new Random();
        random.setSeed(seed);
        double lat = random.nextInt(100 - 1) + 1;
        double lon = random.nextInt(100 - 1) + 1;;
        return new Gps(lat, lon);
    }
}
