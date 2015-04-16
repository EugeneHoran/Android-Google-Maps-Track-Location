/**
 * Globals.java
 * <p/>
 * Created by Xiaochao Yang on Dec 9, 2011 1:43:35 PM
 */

package com.eugene.googlemaps.Mapping;

public abstract class Globals {
    // Const for distance/time conversions
    public static final double KM2MILE_RATIO = 1.609344;
    public static final double KILO = 1000;

    // Some map display related consts
    public static final int GPS_LOCATION_CACHE_SIZE = 100000;

    // Table schema, column names
    public static final String KEY_ROWID = "_id";
    public static final String KEY_INPUT_TYPE = "input_type";
    public static final String KEY_ACTIVITY_TYPE = "activity_type";
    public static final String KEY_DISTANCE = "distance";
    public static final String KEY_GPS_DATA = "gps_data";

    // Int encoded input types
    public static final int TYPE_MOVING = 0;
    public static final int INPUT_TYPE_GPS = 0;

    // Consts for task types
    public static final String KEY_TASK_TYPE = "TASK_TYPE";
    public static final int TASK_TYPE_NEW = 1;
    public static final int TASK_TYPE_HISTORY = 2;

    // Motion sensor buffering related consts
    public static final int ACCELEROMETER_BUFFER_CAPACITY = 2048;

    // Consts for broadcast in the BackgroundService.java
    public static final String ACTION_LOCATION_UPDATED = "LOCATION_UPDATED";
    public static final String ACTION_MOTION_UPDATED = "MOTION_UPDATED";
}
