package com.philliphsu.clock2.stopwatch.ui;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;
import com.philliphsu.clock2.R;
import com.philliphsu.clock2.WeatherAsyncTask;
import com.philliphsu.clock2.data.ActivityColumns;
import com.philliphsu.clock2.list.RecyclerViewFragment;
import com.philliphsu.clock2.stopwatch.Lap;
import com.philliphsu.clock2.stopwatch.StopwatchNotificationService;
import com.philliphsu.clock2.stopwatch.data.LapCursor;
import com.philliphsu.clock2.stopwatch.data.LapsCursorLoader;
import com.philliphsu.clock2.util.ProgressBarUtils;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * Created by joeljohnson on 3/29/17.
 */
//orginial source: https://github.com/philliphsu/ClockPlus
//adapted from StopwatchFragment.java. Added mapfragment, google API client, location services

public class StopwatchMapFragment extends RecyclerViewFragment<
        Lap,
        LapViewHolder,
        LapCursor,
        LapsAdapter> implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "StopwatchMapFragment";

    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    private PolylineOptions mPLO;
    private Polyline polyline;
    private GoogleApiClient mGoogleApiClient;
    private boolean mLocationPermissionGranted;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private Location mLastKnownLocation;
    private LocationRequest mLocationRequest;
    private LocationListener locationListener;
    //private TimerListener timerListener;
    private SpeechRecognizer speechRecognizer;
    private Intent recognizerIntent;
    private Timer speechTimeout;
    private boolean pause;
    private static final String LOG_TAG = "Logs";
    private long startDateAndTime;
    private long endDateAndTime;
    private double distanceRan = 0;
    InterstitialAd mInterstitialAd;
    public static String receivedCommand;


    // Exposed for StopwatchNotificationService
    public static final String KEY_START_TIME = "start_time";
    public static final String KEY_PAUSE_TIME = "pause_time";
    public static final String KEY_CHRONOMETER_RUNNING = "chronometer_running";

    private ObjectAnimator mProgressAnimator;
    private SharedPreferences mPrefs;
    private WeakReference<FloatingActionButton> mActivityFab;
    private Drawable mStartDrawable;
    private Drawable mPauseDrawable;
    public static Location mlocation;

    @Bind(R.id.chronometer)
    ChronometerWithMillis mChronometer;
    @Bind(R.id.new_lap)
    ImageButton mNewLapButton;
    @Bind(R.id.stop)
    ImageButton mStopButton;
    @Bind(R.id.seek_bar)
    SeekBar mSeekBar;

    /**
     * This is called only when a new instance of this Fragment is being created,
     * especially if the user is navigating to this tab for the first time in
     * this app session.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        // TODO: Will these be kept alive after onDestroyView()? If not, we should move these to
        // onCreateView() or any other callback that is guaranteed to be called.
        mStartDrawable = ContextCompat.getDrawable(getActivity(), R.drawable.ic_start_24dp);
        mPauseDrawable = ContextCompat.getDrawable(getActivity(), R.drawable.ic_pause_24dp);
        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .enableAutoManage(getActivity() /* FragmentActivity */,
                        this /* OnConnectionFailedListener */)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();


        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.i("MapsActivity", "onLocationChanged");
                //mPLO.add(new LatLng(location.getLatitude(), location.getLongitude()));
                mlocation = location;
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 20));
                if (mPLO != null & pause) {
                    LatLng myLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    List<LatLng> points = polyline.getPoints();
                    points.add(myLatLng);
                    polyline.setPoints(points);
                    if (points.size() >= 2) {
                        distanceRan += SphericalUtil.computeDistanceBetween(points.get(points.size() - 2), points.get(points.size() - 1));
                    }
                }
            }
        };

        mInterstitialAd = new InterstitialAd(getContext());
        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                ViewPager vp = (ViewPager) getActivity().findViewById(R.id.container);
                vp.setCurrentItem(1, true);
            }
        });

        requestNewInterstitial();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //Log.d(TAG, "onCreateView()");
        View view = super.onCreateView(inflater, container, savedInstanceState);
        mChronometer.setShowCentiseconds(true, true);
        long startTime = getLongFromPref(KEY_START_TIME);
        long pauseTime = getLongFromPref(KEY_PAUSE_TIME);
        // If we have a nonzero startTime from a previous session, restore it as
        // the chronometer's base. Otherwise, leave the default base.
        if (startTime > 0) {
            if (pauseTime > 0) {
                startTime += SystemClock.elapsedRealtime() - pauseTime;
            }
            mChronometer.setBase(startTime);
        }
        if (isStopwatchRunning()) {
            mChronometer.start();
            // Note: mChronometer.isRunning() will return false at this point and
            // in other upcoming lifecycle methods because it is not yet visible
            // (i.e. mVisible == false).
        }
        // The primary reason we call this is to show the mini FABs after rotate,
        // if the stopwatch is running. If the stopwatch is stopped, then this
        // would have hidden the mini FABs, if not for us already setting its
        // visibility to invisible in XML. We haven't initialized the WeakReference to
        // our Activity's FAB yet, so this call does nothing with the FAB.
        setMiniFabsVisible(startTime > 0);
        mPrefs.registerOnSharedPreferenceChangeListener(mPrefChangeListener);
        mChronometer.setContentDescription(mChronometer.getText());
        mStopButton.setContentDescription(getString(R.string.run_stopped));
        mNewLapButton.setContentDescription(getString(R.string.new_lap_button) + mChronometer.getText());
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // TOneverDO: Move to onCreate(). When the device rotates, onCreate() _is_ called,
        // but trying to find the FAB in the Activity's layout will fail, and we would get back
        // a null reference. This is probably because this Fragment's onCreate() is called
        // BEFORE the Activity's onCreate.
        // TODO: Any better alternatives to control the Activity's FAB from here?
        getLoaderManager().initLoader(1, null, this);
        mActivityFab = new WeakReference<>((FloatingActionButton) getActivity().findViewById(R.id.fab));
        // There is no documentation for isMenuVisible(), so what exactly does it do?
        // My guess is it checks for the Fragment's options menu. But we never initialize this
        // Fragment with setHasOptionsMenu(), let alone we don't actually inflate a menu in here.
        // My guess is when this Fragment becomes actually visible, it "hooks" onto the menu
        // options "internal API" and inflates its menu in there if it has one.
        //
        // To us, this just makes for a very good visibility check.
        if (savedInstanceState != null && isMenuVisible()) {
            // This is a pretty good indication that we just rotated.
            // isMenuVisible() filters out the case when you rotate on page 1 and scroll
            // to page 2, the icon will prematurely change; that happens because at page 2,
            // this Fragment will be instantiated for the first time for the current configuration,
            // and so the lifecycle from onCreate() to onActivityCreated() occurs. As such,
            // we will have a non-null savedInstanceState and this would call through.
            //
            // The reason when you open up the app for the first time and scrolling to page 2
            // doesn't prematurely change the icon is the savedInstanceState is null, and so
            // this call would be filtered out sufficiently just from the first check.
            syncFabIconWithStopwatchState(isStopwatchRunning());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        //Log.i(TAG, "User agreed to make required location settings changes.");
                        startLocationUpdates();
                        break;
                    case Activity.RESULT_CANCELED:
                        //Log.i(TAG, "User chose not to make required location settings changes.");
                        break;
                }
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected() && !mLocationPermissionGranted) {
            startLocationUpdates();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //Log.i(LOG_TAG, "onPause() called");

        //Log.i(LOG_TAG,"data added: " + mUri.toString());
    }

    @Override
    public void onStop() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, locationListener);
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    /**
     * If the user navigates away, this is the furthest point in the lifecycle
     * this Fragment gets to. Here, the view hierarchy returned from onCreateView()
     * is destroyed--the Fragment itself is NOT destroyed. If the user navigates back
     * tqo this tab, this Fragment goes through its lifecycle beginning from onCreateView().
     * <p/>
     * TODO: Verify that members are not reset.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Every view that was in our tree is dereferenced for us.
        // The reason we can control the animator here is because members
        // are not dereferenced here.
        if (mProgressAnimator != null) {
            mProgressAnimator.removeAllListeners();
        }
        //Log.d(TAG, "onDestroyView()");
        mPrefs.unregisterOnSharedPreferenceChangeListener(mPrefChangeListener);
    }

    @Override
    protected boolean hasEmptyView() {
        return false;
    }

    @Override
    public Loader<LapCursor> onCreateLoader(int id, Bundle args) {
        return new LapsCursorLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<LapCursor> loader, LapCursor data) {
        //Log.d(TAG, "onLoadFinished()");
        super.onLoadFinished(loader, data);
        // TODO: Will manipulating the cursor's position here affect the current
        // position in the adapter? Should we make a defensive copy and manipulate
        // that copy instead?
        Lap currentLap = null;
        Lap previousLap = null;
        if (data.moveToFirst()) {
            currentLap = data.getItem();
//            Log.d(TAG, "Current lap ID = " + mCurrentLap.getId());
        }
        if (data.moveToNext()) {
            previousLap = data.getItem();
//            Log.d(TAG, "Previous lap ID = " + mPreviousLap.getId());
        }
        if (currentLap != null && previousLap != null) {
            // We really only want to start a new animator when the NEWLY RETRIEVED current
            // and previous laps are different (i.e. different laps, NOT merely different instances)
            // from the CURRENT current and previous laps, as referenced by mCurrentLap and mPreviousLap.
            // However, both equals() and == are insufficient. Our cursor's getItem() will always
            // create new instances of Lap representing the underlying data, so an '== test' will
            // always fail to convey our intention. Also, equals() would fail especially when the
            // physical lap is paused/resumed, because the two instances in comparison
            // (the retrieved and current) would obviously
            // have different values for, e.g., t1 and pauseTime.
            //
            // Therefore, we'll just always end the previous animator and start a new one.
            //
            // NOTE: If we just recreated ourselves due to rotation, mChronometer.isRunning() == false,
            // because it is not yet visible (i.e. mVisible == false).
            if (isStopwatchRunning()) {
                startNewProgressBarAnimator(currentLap, previousLap);
            } else {
                // I verified the bar was visible already without this, so we probably don't need this,
                // but it's just a safety measure..
                // ACTUALLY NOT A SAFETY MEASURE! TODO: Why was this not acceptable?
//                mSeekBar.setVisibility(View.VISIBLE);
                double ratio = getCurrentLapProgressRatio(currentLap, previousLap);
                if (ratio > 0d) {
                    // TODO: To be consistent with the else case, we could set the visibility
                    // to VISIBLE if we cared.
                    ProgressBarUtils.setProgress(mSeekBar, ratio);
                } else {
                    mSeekBar.setVisibility(View.INVISIBLE);
                }
            }
        } else {
            mSeekBar.setVisibility(View.INVISIBLE);
        }
    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(getString(R.string.test_device_id))
                .build();

        mInterstitialAd.loadAd(adRequest);
    }


    @Override
    public void onFabClick() {
        final boolean running = mChronometer.isRunning();
        syncFabIconWithStopwatchState(!running/*invert the current state*/);
        pause = running;
        final Intent serviceIntent = new Intent(getActivity(), StopwatchNotificationService.class);
        if (getLongFromPref(KEY_START_TIME) == 0) {
            setMiniFabsVisible(true);
            // Handle the default action, i.e. post the notification for the first time.
            getActivity().startService(serviceIntent);
        }
        serviceIntent.setAction(StopwatchNotificationService.ACTION_START_PAUSE);
        getActivity().startService(serviceIntent);
        startDateAndTime = Calendar.getInstance().getTimeInMillis();
    }

    @Override
    public void onPageSelected() {
        setMiniFabsVisible(getLongFromPref(KEY_START_TIME) > 0);
        syncFabIconWithStopwatchState(isStopwatchRunning());
    }

    @Override
    protected LapsAdapter onCreateAdapter() {
        return new LapsAdapter();
    }

    @Override
    protected int contentLayout() {
        return R.layout.fragment_stopwatch_map;
    }

    @OnClick(R.id.new_lap)
    void addNewLap() {
        Intent serviceIntent = new Intent(getActivity(), StopwatchNotificationService.class)
                .setAction(StopwatchNotificationService.ACTION_ADD_LAP);
        getActivity().startService(serviceIntent);
    }

    @OnClick(R.id.stop)
    void stop() {
        // Remove the notification. This will also write to prefs and clear the laps table.
        Intent stop = new Intent(getActivity(), StopwatchNotificationService.class)
                .setAction(StopwatchNotificationService.ACTION_STOP);
        getActivity().startService(stop);
        long timeRunning = startDateAndTime - endDateAndTime;
        ContentValues mNewValues = new ContentValues();
        mNewValues.put(ActivityColumns.ActivityEntry.DATE, Calendar.getInstance().getTimeInMillis());
        mNewValues.put(ActivityColumns.ActivityEntry.DIST_RAN, distanceRan * 0.000621371);//value converts meters to miles
        mNewValues.put(ActivityColumns.ActivityEntry.TIME_RUNNING, timeRunning / 1000 / 60); //converts milliseconds to minutes
        mNewValues.put(ActivityColumns.ActivityEntry.IS_LONG_DIST, 1);
        //Log.i(LOG_TAG, mNewValues.toString());
        getActivity().getContentResolver().insert(ActivityColumns.ActivityEntry.CONTENT_URI, mNewValues);
        endDateAndTime = Calendar.getInstance().getTimeInMillis();
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }

    private void setMiniFabsVisible(boolean visible) {
        int vis = visible ? View.VISIBLE : View.INVISIBLE;
        mNewLapButton.setVisibility(vis);
        mStopButton.setVisibility(vis);
    }

    private void syncFabIconWithStopwatchState(boolean running) {
        mActivityFab.get().setImageDrawable(running ? mPauseDrawable : mStartDrawable);
    }

    private void startNewProgressBarAnimator(Lap currentLap, Lap previousLap) {
        final long timeRemaining = remainingTimeBetweenLaps(currentLap, previousLap);
        if (timeRemaining <= 0) {
            mSeekBar.setVisibility(View.INVISIBLE);
            return;
        }
        if (mProgressAnimator != null) {
            mProgressAnimator.end();
        }
        // This can't go in the onAnimationStart() callback because the listener is added
        // AFTER ProgressBarUtils.startNewAnimator() starts the animation.
        mSeekBar.setVisibility(View.VISIBLE);
        mProgressAnimator = ProgressBarUtils.startNewAnimator(mSeekBar,
                getCurrentLapProgressRatio(currentLap, previousLap), timeRemaining);
        mProgressAnimator.addListener(new Animator.AnimatorListener() {
            private boolean cancelled;

            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                // Pausing the stopwatch (and the current lap) uses Animator.cancel(), which will
                // not only fire onAnimationCancel(Animator), but also onAnimationEnd(Animator).
                // We should only let this call through when actually Animator.end() was called,
                // and that happens when we stop() the stopwatch.
                // If we didn't have this check, we'd be hiding the SeekBar every time we pause
                // a lap.
                if (!cancelled) {
                    mSeekBar.setVisibility(View.INVISIBLE);
                }
                cancelled = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                cancelled = true;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    private double getCurrentLapProgressRatio(Lap currentLap, Lap previousLap) {
        if (previousLap == null)
            return 0;
        // The cast is necessary, or else we'd have integer division between two longs and we'd
        // always get zero since the numerator will always be less than the denominator.
        return remainingTimeBetweenLaps(currentLap, previousLap) / (double) previousLap.elapsed();
    }

    private long remainingTimeBetweenLaps(Lap currentLap, Lap previousLap) {
        if (currentLap == null || previousLap == null)
            return 0;
        // TODO: Should we check if the subtraction results in negative number, and return 0?
        return previousLap.elapsed() - currentLap.elapsed();
    }

    /**
     * @return the state of the stopwatch when we're in a resumed and visible state,
     * or when we're going through a rotation
     */
    private boolean isStopwatchRunning() {
        return mChronometer.isRunning() || mPrefs.getBoolean(KEY_CHRONOMETER_RUNNING, false);
    }

    private long getLongFromPref(String key) {
        return mPrefs.getLong(key, 0);
    }

    private final OnSharedPreferenceChangeListener mPrefChangeListener = new OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            // We don't care what key-value pair actually changed, just configure all the views again.
            long startTime = sharedPreferences.getLong(KEY_START_TIME, 0);
            long pauseTime = sharedPreferences.getLong(KEY_PAUSE_TIME, 0);
            boolean running = sharedPreferences.getBoolean(KEY_CHRONOMETER_RUNNING, false);
            setMiniFabsVisible(startTime > 0);
            syncFabIconWithStopwatchState(running);
            // ==================================================
            // TOneverDO: Precede setMiniFabsVisible()
            if (startTime == 0) {
                startTime = SystemClock.elapsedRealtime();
            }
            // ==================================================

            // If we're resuming, the pause duration is already added to the startTime.
            // If we're pausing, then the chronometer will be stopped and we can use
            // the startTime that was originally set the last time we were running.
            //
            // We don't need to add the pause duration if we're pausing because it's going to
            // be negligible at this point.
//            if (pauseTime > 0) {
//                startTime += SystemClock.elapsedRealtime() - pauseTime;
//            }
            mChronometer.setBase(startTime);
            mChronometer.setStarted(running);
            // Starting an instance of Animator is not the responsibility of this method,
            // but is of onLoadFinished().
            if (mProgressAnimator != null && !running) {
                // Wait until both values have been notified of being reset.
                if (startTime == 0 && pauseTime == 0) {
                    mProgressAnimator.end();
                } else {
                    mProgressAnimator.cancel();
                }
            }
        }
    };

// ======================= DO NOT IMPLEMENT ============================

    @Override
    protected void onScrolledToStableId(long id, int position) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onListItemClick(Lap item, int position) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onListItemDeleted(Lap item) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onListItemUpdate(Lap item, int position) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mPLO = new PolylineOptions().geodesic(true);
        polyline = mMap.addPolyline(mPLO);
        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();
        statusCheck();
        // Get the current location of the device and set the position of the map.
        //nullpointerexception seen
        if (statusCheck()) {
           // mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), 20));
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //Log.i("SWMF", "onConnected called");
        //if (mGoogleApiClient.isConnected()) Log.i("SWMF", "client is connected");
        mapFragment = SupportMapFragment.newInstance();
        FragmentTransaction fragmentTransaction =
                getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.activity_map, mapFragment);
        fragmentTransaction.commit();

        mapFragment.getMapAsync(this);
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(2000); //2 seconds
        mLocationRequest.setFastestInterval(1000); //1 second
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        try {
            mLastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
        } catch (SecurityException e) {
        }
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

        if (mLocationPermissionGranted) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        } else {
            mMap.setMyLocationEnabled(false);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mLastKnownLocation = null;
        }
    }

    protected void startLocationUpdates() {
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, locationListener);
        } catch (SecurityException e) {
        }
    }

    public boolean statusCheck() {
        final LocationManager manager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
            return false;
        }
        return true;
    }

    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    private void buildAlertMessageNoGps() {

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                        builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();

                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can
                        // initialize location requests here.
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    StopwatchMapFragment.this.getActivity(),
                                    REQUEST_CHECK_SETTINGS);
                        } catch (SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way
                        // to fix the settings so we won't show the dialog.
                        break;
                }
            }
        });

    }
}
