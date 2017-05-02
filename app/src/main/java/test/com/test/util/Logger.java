package test.com.test.util;

import android.util.Log;

import test.com.test.BuildConfig;

public class Logger {

    private final static String TAG = "TESTAPP_LOG";

    public static void log(String log) {
        if (BuildConfig.DEBUG) {
            Log.v(TAG, log);
        }
    }

    public static void log_e(String log, Throwable e) {
        if (BuildConfig.DEBUG) {
            Log.e(TAG, log, e);
        }
    }
}
