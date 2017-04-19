package com.philliphsu.clock2.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

/**
 * Created by joeljohnson on 4/13/17.
 */


public class ActivityProvider extends ContentProvider {
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private ActivityDatabase activityDatabase;

    private static final int ACTIVITIES = 100;
    private static final int ACTIVITY_ID = 101;

    static UriMatcher buildUriMatcher() {

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = ActivityColumns.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, ActivityColumns.PATH_LONG_DIST_RUNNING, ACTIVITIES);
        matcher.addURI(authority, ActivityColumns.PATH_LONG_DIST_RUNNING + "/#", ACTIVITY_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        activityDatabase = new ActivityDatabase(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.

        return activityDatabase.getReadableDatabase().query(
                ActivityColumns.ActivityEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = activityDatabase.getWritableDatabase();
        Uri returnUri;

        long _id = db.insert(ActivityColumns.ActivityEntry.TABLE_NAME, null, values);

        if (_id > 0) {
            returnUri = ContentUris.withAppendedId(ActivityColumns.ActivityEntry.CONTENT_URI, _id);
        }else {
            throw new android.database.SQLException("Failed to insert row into " + uri.toString());
        }
        return returnUri;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = activityDatabase.getWritableDatabase();


        db.beginTransaction();
        int returnCount = 0;
        try {
            for (ContentValues value : values) {

                long _id = db.insert(ActivityColumns.ActivityEntry.TABLE_NAME, null, value);
                if (_id != -1) {
                    returnCount++;
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            Log.i("path", db.getPath());
            Log.i("concise summary", db.toString());
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnCount;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = activityDatabase.getWritableDatabase();
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if (null == selection) selection = "1";
        rowsDeleted = db.delete(
                ActivityColumns.ActivityEntry.TABLE_NAME, selection, selectionArgs);
        return rowsDeleted;
    }

    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = activityDatabase.getWritableDatabase();

        int rowsUpdated;

        rowsUpdated = db.update(ActivityColumns.ActivityEntry.TABLE_NAME, values, selection,
                selectionArgs);
        return rowsUpdated;
    }

    @Override
    public String getType(Uri uri) {

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case ACTIVITIES:
                return ActivityColumns.ActivityEntry.CONTENT_TYPE;
            case ACTIVITY_ID:
                return ActivityColumns.ActivityEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

    }
}
