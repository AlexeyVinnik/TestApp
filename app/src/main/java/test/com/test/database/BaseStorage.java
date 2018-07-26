package test.com.test.database;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.Locale;

import test.com.test.util.Logger;

public class BaseStorage {
    private static final String DB_NAME = "testapp_db";
    private static final int DB_VERSION = 1;

    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    private static volatile BaseStorage sInstance;
    private UserStorage mUserStorage;
    private VehicleStorage mVehicleStorage;

    public static synchronized BaseStorage getInstance(Context context){
        if(sInstance == null)
            sInstance = new BaseStorage(context);
        return sInstance;
    }

    private BaseStorage(Context context) {
        open(context);
        mUserStorage = new UserStorage(mDb);
        mVehicleStorage = new VehicleStorage(mDb);
    }

    private BaseStorage open(Context context){
        mDbHelper = new DatabaseHelper(context);
        mDb = mDbHelper.getWritableDatabase();

        return this;
    }

    private void close() {
        if (mDbHelper != null)
            mDbHelper.close();
    }

    public boolean isOpen() {
        if (mDb != null)
            return mDb.isOpen();
        else
            return false;
    }

    public UserStorage getUserStorage() {
        return mUserStorage;
    }

    public VehicleStorage getVehicleStorage() {
        return mVehicleStorage;
    }

    public class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(final Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(final SQLiteDatabase db) {
            try {
                db.setVersion(DB_VERSION);
                db.setLocale(Locale.getDefault());
                db.execSQL(UserStorage.CREATE_TABLE);
                db.execSQL(VehicleStorage.CREATE_TABLE);
            } catch (Throwable e) {
                Logger.log_e("DB helper creating error", e);
            }
        }

        @Override
        public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
            if (oldVersion != newVersion) {

                try {
                    db.execSQL(UserStorage.DROP_TABLE);
                    db.execSQL(VehicleStorage.DROP_TABLE);
                } catch (Throwable e) {
                    Logger.log_e("DB helper upgrading error", e);
                }
                onCreate(db);
            }
        }
    }
}
