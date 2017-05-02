package test.com.test.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import test.com.test.model.User;
import test.com.test.model.UserInfo;

public class UserStorage {

    public static final String TABLE_NAME = "user_table";
    public static final String ID = "_id";
    public static final String NAME = "name";
    public static final String SURNAME = "surname";
    public static final String PHOTO = "photo";

    protected static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME
            + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + NAME + " TEXT, "
            + SURNAME + " TEXT, "
            + PHOTO + " TEXT );";

    protected static final String DROP_TABLE = "DROP TABLE IF EXISTS "
            + TABLE_NAME;

    protected static final String SELECT_ALL = "SELECT * FROM " + TABLE_NAME;

    protected SQLiteDatabase mDb;

    public UserStorage(SQLiteDatabase sqliteDb) {
        this.mDb = sqliteDb;
    }

    protected UserInfo toEntity(Cursor c) {
        UserInfo user = new UserInfo();
        user.userid = c.getInt(c.getColumnIndex(ID));

        User owner = new User();
        owner.name = c.getString(c.getColumnIndex(NAME));
        owner.surname = c.getString(c.getColumnIndex(SURNAME));
        owner.foto = c.getString(c.getColumnIndex(PHOTO));

        user.owner = owner;

        return user;
    }

    protected ContentValues toContentValue(UserInfo userInfo) {
        ContentValues values = new ContentValues();

        values.put(ID, userInfo.userid);
        values.put(NAME, userInfo.owner.name);
        values.put(SURNAME, userInfo.owner.surname);
        values.put(PHOTO, userInfo.owner.foto);

        return values;
    }

    public Cursor getCursor() {
        return mDb.rawQuery(SELECT_ALL, null);
    }

    public long addUser(UserInfo userInfo) {
        return mDb.insertWithOnConflict(TABLE_NAME, null, toContentValue(userInfo), SQLiteDatabase.CONFLICT_REPLACE);
    }


    public ArrayList<UserInfo> getAllUserInfo() {
        Cursor cursor = mDb.rawQuery(SELECT_ALL, null);
        ArrayList<UserInfo> list = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            list.add(toEntity(cursor));
            cursor.moveToNext();
        }

        cursor.close();

        return list;
    }
}
