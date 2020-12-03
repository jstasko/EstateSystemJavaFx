package sk.stasko.util;

import sk.stasko.model.realEstate.RealEstate;
import sk.stasko.service.ServiceImpl;
import sk.stasko.util.exception.ErrorLengthException;

public class Helper {
    public static void checkString(String str) {
        if (str.length() > 20) {
            throw new ErrorLengthException("String have to below 20");
        }
    }

    public static RealEstate handleRealEstate(String catalogNumber, String desc, String lat, String lon) {
        RealEstate estate;
        try {
            estate = ServiceImpl.getInstance().createRealEstate(catalogNumber,desc, lat, lon);
        } catch (RuntimeException e) {
            if (e instanceof NumberFormatException) {
                AlertHandler.errorDialog("Error", "Bad input for numbers");
            } else {
                AlertHandler.errorDialog("Error", e.getMessage());
            }
            return null;
        }
        return estate;
    }
}
