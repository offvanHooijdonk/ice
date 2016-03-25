package by.ingman.ice.retailerrequest.v2.helpers;

import java.text.DecimalFormat;
import java.text.ParseException;

/**
 * Created by Yahor_Fralou on 3/24/2016.
 */
public class Helper {
    public static final String MONEY_DECIMAL_FORMAT = "#,##0.00";
    public static final String PACKS_DECIMAL_FORMAT = "0.00";

    public static String formatMoney(Double amount) {
        return new DecimalFormat(MONEY_DECIMAL_FORMAT).format(amount);
    }

    public static String formatPacks(Double amount) {
        return new DecimalFormat(PACKS_DECIMAL_FORMAT).format(amount);
    }

    public static Double parsePacksLocaleString(String str) {
        Number num;
        try {
            num = new DecimalFormat(PACKS_DECIMAL_FORMAT).parse(str);
        } catch (ParseException e) {
            num = Double.valueOf(str);
        }

        return num.doubleValue();
    }
}
