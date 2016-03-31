package by.ingman.ice.retailerrequest.v2.helpers;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Calendar;

/**
 * Created by Yahor_Fralou on 3/24/2016.
 */
public class Helper {
    public static final String APP_FOLDER = "icev2";

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

    public static Calendar roundDayToStart(Calendar c) {
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        return c;
    }
}
