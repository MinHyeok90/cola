package com.example.android.cola;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by KM on 2016. 11. 7..
 */

public class DatabaseOpenHelper {
    private static final String DATABASE_NAME = "friendslog.db";
    private static final int DATABASE_VERSION = 1;
    public static SQLiteDatabase mDB;
    private DatabaseHelper mDBHelper;
    private Context mCtx;

    private class DatabaseHelper extends SQLiteOpenHelper {

        // 생성자
        public DatabaseHelper(Context context, String name,
                              SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        // 최초 DB를 만들때 한번만 호출된다.
        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(Databases.CreateDB._CREATE);

        }

        // 버전이 업데이트 되었을 경우 DB를 다시 만들어 준다.
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS "+Databases.CreateDB._TABLENAME);
            onCreate(db);
        }

    }

    public DatabaseOpenHelper(Context context){
        this.mCtx = context;
    }

    public DatabaseOpenHelper open() throws SQLException {
        mDBHelper = new DatabaseHelper(mCtx, DATABASE_NAME, null, DATABASE_VERSION);
        mDB = mDBHelper.getWritableDatabase();
        return this;
    }
    public void insert(FriendsDeviceLog friendsDeviceLogs){
        // 새로운 BluetoothService.FriendsDeviceLog 추가
        this.open();

        ContentValues values = new ContentValues();
        values.put(Databases.CreateDB.EMAIL, friendsDeviceLogs.getUserID()); // Name 필드명
        values.put(Databases.CreateDB.TIMESTAMP, friendsDeviceLogs.getTimestamp()); // Name 필드명

        // 새로운 Row 추가
        mDB.insert(Databases.CreateDB._TABLENAME, null, values);

        this.close();
    }
    public void dropTable(){
        this.open();
        mDB.execSQL("DROP table "+Databases.CreateDB._TABLENAME);
        mDB.execSQL(Databases.CreateDB._CREATE);
        this.close();

    }
    // 모든 BluetoothService.FriendsDeviceLog 정보 가져오기
    public List<FriendsDeviceLog> getAllFriendsDeviceLogs() {

        List<FriendsDeviceLog> friendsDeviceLogs = new ArrayList<FriendsDeviceLog>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + Databases.CreateDB._TABLENAME;

        this.open();
        Cursor cursor = mDB.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                String email = cursor.getString(2);
                String timestamp = cursor.getString(1);
                FriendsDeviceLog friendsDeviceLog = new FriendsDeviceLog(email, timestamp);

                // Adding BluetoothService.FriendsDeviceLog to list
                friendsDeviceLogs.add(friendsDeviceLog);
            } while (cursor.moveToNext());
        }
        this.close();
        // return BluetoothService.FriendsDeviceLog list
        return friendsDeviceLogs;
    }
    public void close(){
        mDB.close();
    }
}
