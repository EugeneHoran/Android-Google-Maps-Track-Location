package com.eugene.googlemaps.Mapping;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataBaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "DB_NAME";
    // Version code
    private static final int DATABASE_VERSION = 1;

    /**
     * In the constructor of your subclass you call the super() method of SQLiteOpenHelper,
     * specifying the database name and the current database version.
     */
    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Called when no database exists in disk and the helper class needs to create a new one.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        HistoryTable.onCreate(db);
    }

    /**
     * Called when there is a database version mismatch meaning that the version
     * of the database on disk needs to be upgraded to the current version.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        HistoryTable.onUpgrade(db, oldVersion, newVersion);
    }

}