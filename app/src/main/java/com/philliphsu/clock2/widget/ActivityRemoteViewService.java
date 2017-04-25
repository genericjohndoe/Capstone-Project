package com.philliphsu.clock2.widget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.philliphsu.clock2.R;
import com.philliphsu.clock2.data.ActivityColumns;

import java.util.Calendar;

/**
 * Created by joeljohnson on 4/24/17.
 */

public class ActivityRemoteViewService extends RemoteViewsService {
    final String[] columns = {ActivityColumns.ActivityEntry._ID, ActivityColumns.ActivityEntry.DATE,
            ActivityColumns.ActivityEntry.DIST_RAN, ActivityColumns.ActivityEntry.TIME_RUNNING};

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {

        return new RemoteViewsFactory() {
            private Cursor data = null;

            String dateString;
            double distance;
            double minutes;


            @Override
            public void onCreate() {
                // Nothing to do
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission

                final long identityToken = Binder.clearCallingIdentity();

                data = getContentResolver().query(ActivityColumns.ActivityEntry.CONTENT_URI,
                        columns,
                        ActivityColumns.ActivityEntry.DIST_RAN + " > 0",
                        null,
                        null);
                Binder.restoreCallingIdentity(identityToken);

            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_list_item);
                long timeInMills = data.getLong(data.getColumnIndex(columns[1]));
                Calendar date = Calendar.getInstance();
                date.setTimeInMillis(timeInMills);
                dateString = date.toString();
                distance = data.getDouble(data.getColumnIndex(columns[2]));
                minutes = data.getDouble(data.getColumnIndex(columns[3]));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    setRemoteContentDescription(views, null);
                }
                views.setTextViewText(R.id.date, dateString);
                views.setTextViewText(R.id.min_per_mi, "" + (minutes/distance));



                //final Intent fillInIntent = new Intent();
                //fillInIntent.setData(ActivityColumns.ActivityEntry.CONTENT_URI.buildUpon().appendPath(ActivityColumns.ActivityEntry._ID).build());
                //views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);
                return views;
            }

            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
            private void setRemoteContentDescription(RemoteViews views, String description) {
                views.setContentDescription(R.id.date, dateString);
                views.setContentDescription(R.id.min_per_mi, "" + (minutes/distance));
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(data.getColumnIndex(ActivityColumns.ActivityEntry._ID));
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
