package com.eugene.googlemaps;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.eugene.googlemaps.Mapping.Globals;
import com.eugene.googlemaps.Mapping.MappingEntryHelper;
import com.eugene.googlemaps.Mapping.TrackingService;
import com.eugene.googlemaps.Mapping.Utils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class MapsActivity extends ActionBarActivity {
    public GoogleMap mMap;
    public Marker start;
    public Marker end;
    public boolean mIsBound;
    public boolean mIsDoneDrawing;
    public TrackingService mSensorService;
    public Intent mServiceIntent;
    public int mTaskType;
    private ArrayList<LatLng> mLatLngList;
    public ArrayList<Location> mLocationList;
    public MappingEntryHelper mEntry;
    public boolean mIsFirstLocUpdate;
    public boolean isLocationRequested = true;
    public LatLng firstLatLng;
    private IntentFilter mLocationUpdateFilter;
    float currentZoom = 15;
    private LatLngBounds.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        findViewsById();
        RunningOrHistory();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        }
        if (mTaskType == Globals.TASK_TYPE_NEW) {
            // Register gps location update receiver
            registerReceiver(mLocationUpdateReceiver, mLocationUpdateFilter);
        }
    }

    @Override
    protected void onPause() {
        // Unregister the receiver when the activity is about to go inactive.
        if (mTaskType == Globals.TASK_TYPE_NEW) {
            unregisterReceiver(mLocationUpdateReceiver);
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (mSensorService != null) {
            mSensorService.stopForeground(true);
            doUnbindService();
            stopService(mServiceIntent);
        }
        super.onDestroy();
    }

    LatLng target;
    private TextView distanceStats;
    private Toolbar toolbar;

    private void findViewsById() {
        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        }
        mMap.setPadding(0, Utils.dpToPx(this, 56), 0, 0);
        if (mMap != null)
            mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                @Override
                public void onCameraChange(CameraPosition pos) {
                    if (pos.zoom != currentZoom) {
                        currentZoom = pos.zoom;
                        target = pos.target;
                    }
                }
            });
        distanceStats = (TextView) findViewById(R.id.distanceStats);
        mLatLngList = new ArrayList<>(Globals.GPS_LOCATION_CACHE_SIZE);
        mIsBound = false;
        mEntry = new MappingEntryHelper();

        Bundle extras = getIntent().getExtras();
        mTaskType = extras.getInt(Globals.KEY_TASK_TYPE, Globals.TASK_TYPE_NEW);
        mEntry.setInputType(Globals.INPUT_TYPE_GPS);
        mEntry.setActivityType(Globals.TYPE_MOVING);
        mEntry.setID(extras.getLong(Globals.KEY_ROWID, -1));
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.mipmap.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                int menuId = menuItem.getItemId();
                if (menuId == R.id.action_save) {
                    finishRunning();
                }
                if (menuId == R.id.action_delete) {
                    mEntry.deleteEntryInDB(MapsActivity.this);
                    finish();
                }
                if (menuId == R.id.action_map_normal) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                }
                if (menuId == R.id.action_map_Satellite) {
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                }
                return false;
            }
        });
    }

    private void RunningOrHistory() {

        switch (mTaskType) {
            case Globals.TASK_TYPE_NEW:
                toolbar.inflateMenu(R.menu.menu_new);
                mMap.getUiSettings().setZoomControlsEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mMap.getUiSettings().setCompassEnabled(false);
                checkGPS();
                // Register the GPS location sensor to receive location update.
                mLocationUpdateFilter = new IntentFilter();
                mLocationUpdateFilter.addAction(Globals.ACTION_LOCATION_UPDATED);
                // Set the mIsFirstLocUpdate flag to handle first location.
                mIsFirstLocUpdate = true;
                IntentFilter mMotionUpdateIntentFilter = new IntentFilter();
                mMotionUpdateIntentFilter.addAction(Globals.ACTION_MOTION_UPDATED);
                // Start and bind the tracking service
                mServiceIntent = new Intent(this, TrackingService.class);
                startService(mServiceIntent);
                doBindService();
                break;
            case Globals.TASK_TYPE_HISTORY:
                toolbar.inflateMenu(R.menu.menu_history);
                builder = new LatLngBounds.Builder();
                try {
                    if (mEntry == null)
                        Log.e("mEntry", "mEntry is null");
                    mEntry.readFromDB(MapsActivity.this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mLocationList = mEntry.getLocationList();
                for (int i = 0; i < mLocationList.size() - 1; i++) {
                    Location loc = mLocationList.get(i);
                    mLatLngList.add(Utils.fromLocationToLatLng(loc));
                    builder.include(mLatLngList.get(i));
                }
                mMap.addMarker(new MarkerOptions()
                    .position(mLatLngList.get(0))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    .title("Start"));
                mMap.addMarker(new MarkerOptions().position(mLatLngList.get(mLatLngList.size() - 1))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                    .title("Finish")).showInfoWindow();
                mMap.addPolyline(new PolylineOptions().color(Color.RED).width(7).addAll(mLatLngList));
                mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                    @Override
                    public void onMapLoaded() {
                        LatLngBounds bounds = builder.build();
                        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 350));

                    }
                });
                String[] statDecriptions = mEntry.getStatsDescription();
                distanceStats.setText(statDecriptions[1] + " Mile(s)");
                mLatLngList.removeAll(mLatLngList);
                break;
            default:
        }
    }

    private void checkGPS() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            Toast.makeText(this, "GPS is Enabled in your device", Toast.LENGTH_SHORT).show();
        else {
            Toast.makeText(this, "Turn On Location Services", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void finishRunning() {
        if (mIsDoneDrawing) {
            toolbar.getMenu().clear();
            long id = mEntry.insertToDB(MapsActivity.this);
            if (id > 0)
                Toast.makeText(getApplicationContext(), "Entry #" + " saved.", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(getApplicationContext(), "Entry not saved.", Toast.LENGTH_SHORT).show();
            mSensorService.stopForeground(true);
            doUnbindService();
            finish();
        }
    }

    // Bind service and set binding flag.
    private void doBindService() {
        if (!mIsBound) {
            bindService(mServiceIntent, connection, Context.BIND_AUTO_CREATE);
            mIsBound = true;
        }
    }

    // Unbind service and set binding flag.
    private void doUnbindService() {
        if (mIsBound) {
            unbindService(connection);
            mIsBound = false;
        }
    }

    CameraPosition cameraPosition;
    public BroadcastReceiver mLocationUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LatLng latlng;
            latlng = Utils.fromLocationToLatLng(mLocationList.get(0));
            mIsDoneDrawing = false;
            mMap.setMyLocationEnabled(true);
            if (mIsFirstLocUpdate) {
                firstLatLng = latlng;
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 15));
                mIsFirstLocUpdate = false;
            }

            /**
             * Update location based on position
             */
            mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                @Override
                public void onMapLoaded() {
                    if (firstLatLng != null && isLocationRequested) {
                        Criteria criteria = new Criteria();
                        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                        String provider = locationManager.getBestProvider(criteria, true);
                        Location location = locationManager.getLastKnownLocation(provider);
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        if (mMap.getMyLocation().getBearing() != 0) {
                            cameraPosition = new CameraPosition.Builder().target(latLng).bearing(mMap.getMyLocation().getBearing()).zoom(currentZoom).tilt(60).build();
                        } else {
                            cameraPosition = new CameraPosition.Builder().target(latLng).bearing(mMap.getMyLocation().getBearing()).zoom(currentZoom).tilt(0).build();
                        }
                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    }
                }
            });
            try {
                mEntry.updateStats(); // Set distance
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (mLocationList == null || mLocationList.isEmpty())
                return;
            // Convert the mLocationList to mLatLngList

            for (int i = 0; i < mLocationList.size() - 1; i++) {
                Location loc = mLocationList.get(i);
                mLatLngList.add(Utils.fromLocationToLatLng(loc));
            }
            mMap.addPolyline(new PolylineOptions().color(Color.RED).width(7).addAll(mLatLngList));
            // Draw marker
            if (start == null)
                start = mMap.addMarker(new MarkerOptions()
                    .position(firstLatLng)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    .title("Start"));
            if (end != null)
                end.remove();
            String[] statDecriptions = mEntry.getStatsDescription();
            if (statDecriptions.length != 0) {
                distanceStats.setText(statDecriptions[1] + " Mile(s)");
            }
            mLatLngList.removeAll(mLatLngList);
            mIsDoneDrawing = true;
        }
    };

    private ServiceConnection connection = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            // Initialize mSensorService from TrackingService
            mSensorService = ((TrackingService.MyRunsBinder) service).getService();
            // Get mLocationList from mSensorService
            mLocationList = mSensorService.mLocationList;
            // set Location list for mEntry.
            mEntry.setLocationList(mLocationList);
            // Start logging
            try {
                mEntry.startLogging();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            // Stop the service. This ONLY gets called when crashed.
            stopService(mServiceIntent);
            mSensorService = null;

        }
    };

}
