package com.unipi.sleepmonitoringproject.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class EventManagerDbHelper extends SQLiteOpenHelper {
    // If we change the database schema, we must increment the database version.
    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "EventManager.db";
    private static final String[] SQL_CREATE_ENTRIES = {
            "CREATE TABLE " + EventManagerContract.SleepEvent.TABLE_NAME1 + " (" +
                    EventManagerContract.SleepEvent._ID + " INTEGER PRIMARY KEY," +
                    EventManagerContract.SleepEvent.COLUMN_NAME_TIMESTAMP + " INTEGER," +
                    EventManagerContract.SleepEvent.COLUMN_NAME_EVENT1 + " FLOAT);",
            "CREATE TABLE " + EventManagerContract.SleepEvent.TABLE_NAME2 + " (" +
                    EventManagerContract.SleepEvent._ID + " INTEGER PRIMARY KEY," +
                    EventManagerContract.SleepEvent.COLUMN_NAME_TIMESTAMP + " INTEGER," +
                    EventManagerContract.SleepEvent.COLUMN_NAME_EVENT2_x + " FLOAT," +
                    EventManagerContract.SleepEvent.COLUMN_NAME_EVENT2_y + " FLOAT," +
                    EventManagerContract.SleepEvent.COLUMN_NAME_EVENT2_z + " FLOAT);"
    };
    private static final String[] SQL_DELETE_ENTRIES = {
            "DROP TABLE IF EXISTS " + EventManagerContract.SleepEvent.TABLE_NAME1,
            "DROP TABLE IF EXISTS " + EventManagerContract.SleepEvent.TABLE_NAME2
    };

    public EventManagerDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        for (String createTableStatement : SQL_CREATE_ENTRIES) {
            db.execSQL(createTableStatement);
        }
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Elimina le tabelle esistenti
        for (String deleteTableStatement : SQL_DELETE_ENTRIES) {
            db.execSQL(deleteTableStatement);
        }
        // Ricrea le tabelle
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}