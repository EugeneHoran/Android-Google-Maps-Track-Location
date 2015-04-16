package com.eugene.googlemaps.Mapping;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

//HistoryTable contains constants for the table name and the columns. 
public class HistoryTable {
    // Table name string. (Only one table)
    public static final String TABLE_NAME_ENTRIES = "ENTRIES";
    // Table column names
    public static final String KEY_ROWID = Globals.KEY_ROWID;
    public static final String KEY_INPUT_TYPE = Globals.KEY_INPUT_TYPE;
    public static final String KEY_ACTIVITY_TYPE = Globals.KEY_ACTIVITY_TYPE;
    public static final String KEY_DISTANCE = Globals.KEY_DISTANCE;
    public static final String KEY_GPS_DATA = Globals.KEY_GPS_DATA;

    // SQL query to create the table for the first time
    // Data types are defined below
    public static final String
        CREATE_TABLE_ENTRIES = "CREATE TABLE IF NOT EXISTS "
        + TABLE_NAME_ENTRIES
        + " ("
        + KEY_ROWID
        + " INTEGER PRIMARY KEY AUTOINCREMENT, "
        + KEY_INPUT_TYPE
        + " INTEGER NOT NULL, "
        + KEY_ACTIVITY_TYPE
        + " INTEGER NOT NULL, "
        + KEY_DISTANCE
        + " FLOAT, "
        + " INTEGER, "
        + KEY_GPS_DATA + " BLOB "
        + ");";

    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(CREATE_TABLE_ENTRIES);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion,
                                 int newVersion) {
        Log.w(HistoryTable.class.getName(), "Upgrading database from version "
            + oldVersion + " to " + newVersion
            + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS ");
        onCreate(database);
    }
}