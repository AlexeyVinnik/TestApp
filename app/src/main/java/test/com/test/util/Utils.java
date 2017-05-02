package test.com.test.util;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utils {

    private static final SimpleDateFormat SDF_DATE_TIME = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    private static final SimpleDateFormat SDF_DATE = new SimpleDateFormat("yyyy-MM-dd ", Locale.US);

    public static String getStringDateTime(Date date) {
        return SDF_DATE_TIME.format(date);
    }

    public static Date getDateTimeFromString(String dateString) {
        try {
            return SDF_DATE_TIME.parse(dateString);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getStringDate(Date date) {
        return SDF_DATE.format(date);
    }

    public static Date getDateFromString(String dateString) {
        try {
            return SDF_DATE.parse(dateString);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
