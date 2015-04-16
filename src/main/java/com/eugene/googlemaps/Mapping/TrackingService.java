/**
 * LocationService.java
 * <p/>
 * Created by Xiaochao Yang on Sep 11, 2011 4:50:19 PM
 * http://www.cs.dartmouth.edu/~campbell/cs65/code/MyRunsDataCollector/src/edu/dartmouth/cs/myrunscollector/SensorsService.java
 */

package com.eugene.googlemaps.Mapping;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

import com.eugene.googlemaps.MapsActivity;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Tracking service will: read and process GPS data.
 */
public class TrackingService extends Service implements LocationListener, SensorEventListener {
    /**
     * A buffer list to store all GPS track points
     * It's accessed at different places
     */
    public ArrayList<Location> mLocationList;
    // Location manager and Notification manager
    private LocationManager mLocationManager;
    // Context for "this"
    private Context mContext;
    // Intents for broadcasting location/motion updates
    private Intent mLocationUpdateBroadcast;
    // A blocking queue for buffering motion sensor data
    private static ArrayBlockingQueue<Double> mAccBuffer;
    // Standard service stuff.
    private final IBinder binder = new MyRunsBinder();

    public class MyRunsBinder extends Binder {
        public TrackingService getService() {
            return TrackingService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        mContext = this;
        mLocationList = new ArrayList<>(Globals.GPS_LOCATION_CACHE_SIZE);
        mLocationUpdateBroadcast = new Intent();
        mLocationUpdateBroadcast.setAction(Globals.ACTION_LOCATION_UPDATED);
        // Initialize mAccBuffer, mActivityClassificationBroadcast here
        mAccBuffer = new ArrayBlockingQueue<>(Globals.ACCELEROMETER_BUFFER_CAPACITY);
        Intent mActivityClassificationBroadcast = new Intent();
        mActivityClassificationBroadcast.setAction(Globals.ACTION_MOTION_UPDATED);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Get LocationManager and set related provider.
        // GPS_PROVIDER recommended.
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        Intent i = new Intent(this, MapsActivity.class);
        /**
         * Set flags to avoid re-invent activity.
         * IMPORTANT!. no re-create activity
         */
        i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        // Unregister location manager, foreground notification, and sensor listener.
        mLocationManager.removeUpdates(this);
        super.onDestroy();
    }

    /**
     * @param location Gets called when new GPS location updates
     */
    public void onLocationChanged(Location location) {

        if (location == null || Math.abs(location.getLatitude()) > 90
            || Math.abs(location.getLongitude()) > 180)
            return;
        /**
         *  Buffer the new location. mLocation is connected by reference by
         *  several other classes. Accessed with "synchronized" lock
         */
        synchronized (mLocationList) {
            mLocationList.add(location);
        }
        // Send broadcast new location is updated
        mContext.sendBroadcast(mLocationUpdateBroadcast);
    }

    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            // Compute m for 3-axis accelerometer input.
            // m=sqrt(x^2+y^2+z^2)
            double x = event.values[0];
            double y = event.values[1];
            double z = event.values[2];
            double m = Math.sqrt(x * x + y * y + z * z);
            // Add m to the mAccBuffer one by one.
            try {
                mAccBuffer.add(m);
            } catch (IllegalStateException e) {
                // Exception happens when reach the capacity.
                // Doubling the buffer. ListBlockingQueue has no such issue,
                // But generally has worse performance
                ArrayBlockingQueue<Double> newBuf = new ArrayBlockingQueue<>(mAccBuffer.size() * 2);
                mAccBuffer.drainTo(newBuf);
                mAccBuffer = newBuf;
                mAccBuffer.add(m);
            }
        }
    }

    /**
     * Not Being Used
     */

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void onProviderDisabled(String provider) {
    }

    public void onProviderEnabled(String provider) {
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
    }
}