package com.example.smartlocationreminder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "SmartReminder.db";
    public static final int DATABASE_VERSION = 3; // تحديث النسخة لإضافة ربط المستخدمين

    // جدول التذكيرات
    public static final String TABLE_REMINDERS = "reminders_table";
    public static final String COL_ID = "ID";
    public static final String COL_TITLE = "TITLE";
    public static final String COL_LAT = "LATITUDE";
    public static final String COL_LNG = "LONGITUDE";
    public static final String COL_USER_OWNER = "USER_OWNER"; // العمود الجديد للربط

    // جدول المستخدمين
    public static final String TABLE_USERS = "users_table";
    public static final String COL_USER_ID = "USER_ID";
    public static final String COL_USERNAME = "USERNAME";
    public static final String COL_PASSWORD = "PASSWORD";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_REMINDERS + " (" + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COL_TITLE + " TEXT, " + COL_LAT + " REAL, " + COL_LNG + " REAL, " + COL_USER_OWNER + " TEXT)");
        db.execSQL("CREATE TABLE " + TABLE_USERS + " (" + COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COL_USERNAME + " TEXT, " + COL_PASSWORD + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REMINDERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // دوال المستخدمين
    public boolean insertUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_USERNAME, username);
        contentValues.put(COL_PASSWORD, password);
        return db.insert(TABLE_USERS, null, contentValues) != -1;
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE USERNAME = ? AND PASSWORD = ?", new String[]{username, password});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // دوال التذكيرات (تعتمد على اسم المستخدم)
    public boolean insertReminder(String title, double lat, double lng, String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_TITLE, title);
        contentValues.put(COL_LAT, lat);
        contentValues.put(COL_LNG, lng);
        contentValues.put(COL_USER_OWNER, username);
        return db.insert(TABLE_REMINDERS, null, contentValues) != -1;
    }

    public Cursor getRemindersByUser(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_REMINDERS + " WHERE " + COL_USER_OWNER + " = ?", new String[]{username});
    }

    public Integer deleteReminder(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_REMINDERS, "ID = ?", new String[]{id});
    }


}