package com.philliphsu.clock2.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by joeljohnson on 4/13/17.
 */

public class ActivityColumns {

    public static final String CONTENT_AUTHORITY = "com.philliphsu.clock2.app";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_LONG_DIST_RUNNING = "LONG_DIST_RUNNING";

    public static final class ActivityEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_LONG_DIST_RUNNING).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_LONG_DIST_RUNNING;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_LONG_DIST_RUNNING;

        public static final String TABLE_NAME = "Long_Dist_Running";

        public static String _ID = "_id";// not shown

        public static String DATE = "date"; // x coord graph

        public static String DIST_RAN = "dist_ran"; //y coord graph

        public static String TIME_RUNNING = "time_running"; //textview

        public final static String IS_LONG_DIST = "is_long_dist"; //not shown

        public static Uri buildMovieUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}
