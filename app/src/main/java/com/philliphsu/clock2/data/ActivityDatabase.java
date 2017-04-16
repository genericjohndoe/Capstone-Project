package com.philliphsu.clock2.data;

import net.simonvt.schematic.annotation.Database;
import net.simonvt.schematic.annotation.Table;

/**
 * Created by joeljohnson on 4/13/17.
 */

@Database(version = ActivityDatabase.VERSION)
public class ActivityDatabase {

    public static final int VERSION = 1;

    @Table(ActivityColumns.class)
    public static final String LONG_DIST_RUNNING = "LONG_DIST_RUNNING";
}
