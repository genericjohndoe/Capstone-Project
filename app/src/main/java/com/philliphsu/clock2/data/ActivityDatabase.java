package com.philliphsu.clock2.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by joeljohnson on 4/13/17.
 */


public class ActivityDatabase extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "LONG_DIST_RUNNING.db";

    public ActivityDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String CREATE_ACTIVITY_DATABASE = "CREATE TABLE " + ActivityColumns.ActivityEntry.TABLE_NAME + " (" +
                ActivityColumns.ActivityEntry._ID + " INTEGER PRIMARY KEY, " +
                ActivityColumns.ActivityEntry.DATE + " REAL NOT NULL, " +
                ActivityColumns.ActivityEntry.DIST_RAN + " REAL NOT NULL, " +
                ActivityColumns.ActivityEntry.TIME_RUNNING + " REAL NOT NULL, "+
                ActivityColumns.ActivityEntry.IS_LONG_DIST + " INTEGER NOT NULL" +
                " );";

        sqLiteDatabase.execSQL(CREATE_ACTIVITY_DATABASE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ActivityColumns.ActivityEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
