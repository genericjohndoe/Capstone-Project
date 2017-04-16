package com.philliphsu.clock2.data;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;

/**
 * Created by joeljohnson on 4/13/17.
 */

public class ActivityColumns {
    @DataType(DataType.Type.INTEGER) @PrimaryKey
    @AutoIncrement
    public static String _ID = "_id";// not shown
    @DataType(DataType.Type.REAL) @NotNull
    public static String DATE = "date"; // x coord graph
    @DataType(DataType.Type.REAL)
    public static String DIST_RAN = "dist_ran"; //y coord graph
    @DataType(DataType.Type.REAL)
    public static String TIME_RUNNING = "time_running"; //textview
    @DataType(DataType.Type.INTEGER) @NotNull
    public final static String IS_LONG_DIST = "is_long_dist"; //not shown
}
