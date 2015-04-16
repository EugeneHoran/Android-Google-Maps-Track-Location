package com.eugene.googlemaps.Mapping;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

/**
 * The HistoryProvider class is the ContentProvider.
 * It stores all the ExerciseEntry into the ContentProvider.
 */
public class HistoryProvider extends ContentProvider {
    private DataBaseHelper database;
    private static final String AUTHORITY = "com.eugene.googlemaps.Mapping.historyprovider";
    public static final int ENTRIES_DIR = 100;
    public static final int ENTRIES_ID = 110;
    private static final String BASE_PATH = "history";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);
    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI(AUTHORITY, BASE_PATH, ENTRIES_DIR);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", ENTRIES_ID);
    }

    @Override
    public boolean onCreate() {
        database = new DataBaseHelper(getContext());
        return false;
    }

    // queries the provider for the records specified by either uri or 'selection'.
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // Using SQLiteQueryBuilder instead of query() method
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        // Set the table
        queryBuilder.setTables(HistoryTable.TABLE_NAME_ENTRIES);
        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case ENTRIES_DIR:
                break;
            case ENTRIES_ID:
                // Adding the ID to the original query
                queryBuilder.appendWhere(HistoryTable.KEY_ROWID + "=" + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SQLiteDatabase db = database.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        // Make sure that potential listeners are getting notified
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // put the values in ContentValues to the ContentProvider which is denoted by the Uri.
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        long id = sqlDB.insert(HistoryTable.TABLE_NAME_ENTRIES, null, values);
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(BASE_PATH + "/" + id);
    }

    // Deletes row(s) specified by a content URI.
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsDeleted;
        switch (uriType) {
            case ENTRIES_DIR:
                rowsDeleted = sqlDB.delete(HistoryTable.TABLE_NAME_ENTRIES,
                    selection, selectionArgs);
                break;
            case ENTRIES_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB.delete(HistoryTable.TABLE_NAME_ENTRIES,
                        HistoryTable.KEY_ROWID + "=" + id, null);
                } else {
                    rowsDeleted = sqlDB.delete(HistoryTable.TABLE_NAME_ENTRIES,
                        HistoryTable.KEY_ROWID + "=" + id + " and "
                            + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    // Update row(s) in a content URI.
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsUpdated;
        switch (uriType) {
            case ENTRIES_DIR:
                rowsUpdated = sqlDB.update(HistoryTable.TABLE_NAME_ENTRIES, values, selection, selectionArgs);
                break;
            case ENTRIES_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = sqlDB.update(HistoryTable.TABLE_NAME_ENTRIES, values, HistoryTable.KEY_ROWID + "=" + id, null);
                } else {
                    rowsUpdated = sqlDB.update(HistoryTable.TABLE_NAME_ENTRIES, values, HistoryTable.KEY_ROWID + "=" + id + " and "
                        + selection, selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

}