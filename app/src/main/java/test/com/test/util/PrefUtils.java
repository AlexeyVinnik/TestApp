package test.com.test.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Date;

public class PrefUtils {
    private static final String PREF = "testapp_prefs.";
    private static final String USERS_LOADING_TIME = PREF + "users_loading_time";

    public static void saveUsersLoadingTime(Context context, Date date) {
        putString(context, USERS_LOADING_TIME, Utils.getStringDate(date));
    }

    public static Date getUsersLoadingDate(Context context) {
        return Utils.getDateFromString(getString(context, USERS_LOADING_TIME));
    }


    //------------------------------------------------------------------------

    private static void putBoolean(Context context, String key, boolean value) {
        getPrefs(context).edit().putBoolean(key, value).apply();
    }

    private static boolean getBoolean(Context context, String key) {
        return getPrefs(context).getBoolean(key, false);
    }

    private static void putLong(Context context, String key, long value) {
        getPrefs(context).edit().putLong(key, value).apply();
    }

    private static long getLong(Context context, String key) {
        return getPrefs(context).getLong(key, 0);
    }

    private static void putString(Context context, String key, String value) {
        getPrefs(context).edit().putString(key, value).apply();
    }

    private static String getString(Context context, String key) {
        return getPrefs(context).getString(key, "");
    }

    private static SharedPreferences getPrefs(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
}
