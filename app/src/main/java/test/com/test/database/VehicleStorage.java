package test.com.test.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Date;

import test.com.test.model.Vehicle;
import test.com.test.util.Utils;

public class VehicleStorage {

    private static final String TABLE_NAME = "vehicle_table";
    private static final String ID = "_id";
    private static final String FK_USER_ID = "fk_userid";
    private static final String MAKE = "make";
    private static final String MODEL = "model";
    private static final String YEAR = "year";
    private static final String COLOR = "color";
    private static final String VIN = "vin";
    private static final String PHOTO = "photo";
    private static final String TIME = "time";
    private static final String LAT = "lat";
    private static final String LON = "lon";

    public static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME
            + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + FK_USER_ID + " INTEGER, "
            + MAKE + " TEXT, "
            + MODEL + " TEXT, "
            + YEAR + " TEXT, "
            + COLOR + " TEXT, "
            + VIN + " TEXT, "
            + LAT + " DOUBLE, "
            + LON + " DOUBLE, "
            + TIME + " DATETIME, "
            + PHOTO + " TEXT );";

    public static final String DROP_TABLE = "DROP TABLE IF EXISTS "
            + TABLE_NAME;

    public static final String SELECT_ALL = "SELECT * FROM " + TABLE_NAME;

    public static final String SELECT_BY_USER = "SELECT * FROM " + TABLE_NAME
            + " WHERE " + FK_USER_ID + " = ?";

    private SQLiteDatabase mDb;

    public VehicleStorage(SQLiteDatabase sqliteDb) {
        this.mDb = sqliteDb;
    }

    private Vehicle toEntity(Cursor c) {
        Vehicle vehicle = new Vehicle();

        vehicle.vehicleid = c.getInt(c.getColumnIndex(ID));
        vehicle.make = c.getString(c.getColumnIndex(MAKE));
        vehicle.model = c.getString(c.getColumnIndex(MODEL));
        vehicle.year = c.getString(c.getColumnIndex(YEAR));
        vehicle.color = c.getString(c.getColumnIndex(COLOR));
        vehicle.vin = c.getString(c.getColumnIndex(VIN));
        vehicle.latitude = c.getDouble(c.getColumnIndex(LAT));
        vehicle.longitude = c.getDouble(c.getColumnIndex(LON));
        vehicle.lastUpdate = Utils.getDateTimeFromString(c.getString(c.getColumnIndex(TIME)));
        vehicle.foto = c.getString(c.getColumnIndex(PHOTO));

        return vehicle;
    }

    private ContentValues toContentValue(Vehicle vehicle, int userId) {
        ContentValues values = new ContentValues();

        values.put(ID, vehicle.vehicleid);
        values.put(FK_USER_ID, userId);
        values.put(MAKE, vehicle.make);
        values.put(MODEL, vehicle.model);
        values.put(YEAR, vehicle.year);
        values.put(COLOR, vehicle.color);
        values.put(VIN, vehicle.vin);
        values.put(LAT, vehicle.latitude);
        values.put(LON, vehicle.longitude);

        values.put(PHOTO, vehicle.foto);

        return values;
    }

    public Cursor getCursor() {
        return mDb.rawQuery(SELECT_ALL, null);
    }

    public long addVehicle(Vehicle vehicle, int userId) {
        return mDb.insertWithOnConflict(TABLE_NAME, null, toContentValue(vehicle, userId), SQLiteDatabase.CONFLICT_REPLACE);
    }

    public ArrayList<Vehicle> getUserVehicles(int userId) {
        Cursor cursor = mDb.rawQuery(SELECT_BY_USER, new String[] {String.valueOf(userId)});
        ArrayList<Vehicle> list = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            list.add(toEntity(cursor));
            cursor.moveToNext();
        }

        cursor.close();

        return list;
    }

    public void updateVehicleLocation(int vehicleId, double lat, double lon) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(LAT, lat);
        contentValues.put(LON, lon);
        contentValues.put(TIME, Utils.getStringDateTime(new Date()));

        mDb.update(TABLE_NAME, contentValues, ID + "=" + vehicleId, null);
    }

    public ArrayList<Vehicle> getAllVehicles() {
        Cursor cursor = mDb.rawQuery(SELECT_ALL, null);
        ArrayList<Vehicle> list = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            list.add(toEntity(cursor));
            cursor.moveToNext();
        }

        cursor.close();

        return list;
    }
}
