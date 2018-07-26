package test.com.test;

import android.app.Application;

import test.com.test.database.BaseStorage;

public class App extends Application {

    private static App sInstance;

    public static App getInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        sInstance = this;

        BaseStorage.getInstance(this);
    }
}
