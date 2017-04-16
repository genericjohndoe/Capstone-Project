package com.philliphsu.clock2.data;

import android.net.Uri;

import net.simonvt.schematic.annotation.ContentProvider;
import net.simonvt.schematic.annotation.ContentUri;
import net.simonvt.schematic.annotation.InexactContentUri;
import net.simonvt.schematic.annotation.TableEndpoint;

/**
 * Created by joeljohnson on 4/13/17.
 */

@ContentProvider(authority = ActivityProvider.AUTHORITY, database = ActivityDatabase.class,
name = "com.philliphsu.clock2.data.provider.ActivityProvider", packageName = "com.philliphsu.clock2.debug")
public class ActivityProvider {
    public static final String AUTHORITY = "com.philliphsu.clock2.data.ActivityProvider";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    interface Path {
        String LONG_DIST_RUNNING = "LONG_DIST_RUNNING";
    }

    private static Uri buildUri(String... paths) {
        Uri.Builder builder = BASE_CONTENT_URI.buildUpon();
        for (String path : paths) {
            builder.appendPath(path);
        }
        return builder.build();
    }

    @TableEndpoint(table = ActivityDatabase.LONG_DIST_RUNNING)
    public static class LongDistRunning {
        @ContentUri(
                path = Path.LONG_DIST_RUNNING,
                type = "vnd.android.cursor.dir/activity"
        )
        public static final Uri CONTENT_URI = buildUri(Path.LONG_DIST_RUNNING);

        @InexactContentUri(
                name = "IS_LD_ID",
                path = Path.LONG_DIST_RUNNING + "/*",
                type = "vnd.android.cursor.item/activity",
                whereColumn = ActivityColumns.IS_LONG_DIST,
                pathSegment = 1
        )
        public static Uri withIsLongDist(String activity) {
            return buildUri(Path.LONG_DIST_RUNNING, activity);
        }
    }
}
