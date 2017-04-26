package com.philliphsu.clock2;


import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.utils.EntryXComparator;
import com.philliphsu.clock2.data.ActivityColumns;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 */
public class DataFragment extends BaseFragment implements LoaderManager.LoaderCallbacks, IAxisValueFormatter {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private Cursor cursor;
    private List<Entry> entries = new ArrayList<Entry>();
    @Bind(R.id.chart)
    LineChart chart;

    public DataFragment() {
    }


    @Override
    protected int contentLayout() {
        return R.layout.fragment_data;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = super.onCreateView(inflater, container, savedInstanceState);
        chart.setContentDescription("Graphed Data");
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();

    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(getActivity(), ActivityColumns.ActivityEntry.CONTENT_URI,
                new String[]{ActivityColumns.ActivityEntry._ID, ActivityColumns.ActivityEntry.DIST_RAN, ActivityColumns.ActivityEntry.TIME_RUNNING, ActivityColumns.ActivityEntry.DATE,
                        ActivityColumns.ActivityEntry.IS_LONG_DIST,}, ActivityColumns.ActivityEntry.IS_LONG_DIST + " = ?",
                new String[]{"1"}, null);
    }

    @Override
    public void onLoadFinished(Loader loader, Object o) {
        cursor = (Cursor) o;
        Log.i("Logs", "" + cursor.getCount());
        if (cursor.getCount() > 0) {
            for (int i = 0; i < cursor.getCount(); i++) {
                if (cursor.moveToPosition(i)) {
                    long date = cursor.getLong(cursor.getColumnIndex(ActivityColumns.ActivityEntry.DATE));
                    double minutesPerMile = cursor.getLong(cursor.getColumnIndex(ActivityColumns.ActivityEntry.TIME_RUNNING)) /
                            cursor.getDouble(cursor.getColumnIndex(ActivityColumns.ActivityEntry.DIST_RAN));
                    entries.add(new Entry(date, (float) minutesPerMile));
                }
            }
            Collections.sort(entries, new EntryXComparator());
            LineDataSet dataSet = new LineDataSet(entries, "Average Mile Times");
            LineData lineData = new LineData(dataSet);
            chart.setData(lineData);
            XAxis xAxis = chart.getXAxis();
            xAxis.setValueFormatter(this);
            xAxis.setLabelRotationAngle(45f);
            YAxis yAxis = chart.getAxisLeft();
            yAxis.setAxisMinimum(0f);
            chart.invalidate();
        } else {
            CharSequence text = "No Data";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(getContext(), text, duration);
            toast.show();
        }


    }

    @Override
    public void onLoaderReset(Loader loader) {
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis((long) value);
        return new SimpleDateFormat(getString(R.string.date_format)).format(cal.getTime());
    }
}
