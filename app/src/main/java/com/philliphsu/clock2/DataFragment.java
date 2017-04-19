package com.philliphsu.clock2;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
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

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DataFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DataFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DataFragment extends Fragment implements LoaderManager.LoaderCallbacks, IAxisValueFormatter {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private Cursor cursor;
    private List<Entry> entries = new ArrayList<Entry>();
    private LineChart chart;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    //private OnFragmentInteractionListener mListener;

    public DataFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DataFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DataFragment newInstance(String param1, String param2) {
        DataFragment fragment = new DataFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_data, container, false);
        chart = (LineChart) rootView.findViewById(R.id.chart);
        return rootView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    /*public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }*/

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        /*if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
        //mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    /*public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }*/

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(getActivity(), ActivityColumns.ActivityEntry.CONTENT_URI,
                new String[]{ActivityColumns.ActivityEntry._ID, ActivityColumns.ActivityEntry.DIST_RAN,ActivityColumns.ActivityEntry.TIME_RUNNING, ActivityColumns.ActivityEntry.DATE,
                        ActivityColumns.ActivityEntry.IS_LONG_DIST,}, ActivityColumns.ActivityEntry.IS_LONG_DIST + " = ?",
                new String[]{"1"}, null);
    }

    @Override
    public void onLoadFinished(Loader loader, Object o) {
        cursor = (Cursor) o;
        if (cursor.getCount() > 0){
        for (int i = 0; i < cursor.getCount(); i++){
            if (cursor.moveToPosition(i)){
                long date = cursor.getLong(cursor.getColumnIndex(ActivityColumns.ActivityEntry.DATE));
                double minutesPerMile = cursor.getLong(cursor.getColumnIndex(ActivityColumns.ActivityEntry.TIME_RUNNING))/
                        cursor.getDouble(cursor.getColumnIndex(ActivityColumns.ActivityEntry.DIST_RAN));
                entries.add(new Entry(date, (float) minutesPerMile));
            }
        }
        } else {
            CharSequence text = "Hello toast!";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(getContext(), text, duration);
            toast.show();
        }
        Collections.sort(entries, new EntryXComparator());
        LineDataSet dataSet = new LineDataSet(entries, "Average Mile Times");
        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(this);
        xAxis.setLabelRotationAngle(45f);
        chart.invalidate();

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
