package com.eugene.googlemaps.Mapping;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

public class MappingEntryHelper {
    private MappingEntry mData;
    private ArrayList<Location> mLocationList;
    private int mNLocations; // Number of location points
    public static final DecimalFormat decimalFormat = new DecimalFormat("#.##");

    public MappingEntryHelper() {
        mData = new MappingEntry();
    }

    public long insertToDB(Context context) {
        assert (mLocationList != null);
        Location[] mTrack;
        synchronized (mLocationList) {
            if (mLocationList.size() < 2)
                return -1;
            mTrack = new Location[mLocationList.size()];
            mTrack = mLocationList.toArray(mTrack);
        }
        ContentValues value = new ContentValues();
        // put all the data saved in ExerciseEntry into the ContentValues object.
        value.put(HistoryTable.KEY_INPUT_TYPE, mData.getInputType());
        value.put(HistoryTable.KEY_ACTIVITY_TYPE, mData.getActivityType());
        value.put(HistoryTable.KEY_DISTANCE, mData.getDistance());
        value.put(HistoryTable.KEY_GPS_DATA, Utils.fromLocationArrayToByteArray(mTrack));
        // get the content resolver, insert the ContentValues into HistoryProvider.
        Uri uri = context.getContentResolver().insert(HistoryProvider.CONTENT_URI, value);
        // set current ExerciseEntry's id.
        mData.setId(Long.valueOf(uri.getLastPathSegment()));
        return Long.valueOf(uri.getLastPathSegment());
    }

    // Read an exercise entry specified by the id field from database
    public void readFromDB(Context context) throws Exception {
        long id = mData.getId();
        if (id <= 0)
            throw new Exception();
        // Cursor has all column values of the entry specified by id
        Cursor c = context.getContentResolver().query(
            Uri.parse(HistoryProvider.CONTENT_URI + "/"
                + String.valueOf(id)), null, null, null, null);
        c.moveToFirst();
        setID(id);
        setInputType(c.getInt(c.getColumnIndex(Globals.KEY_INPUT_TYPE)));
        setActivityType(c.getInt(c.getColumnIndex(Globals.KEY_ACTIVITY_TYPE)));
        setDistance(c.getInt(c.getColumnIndex(Globals.KEY_DISTANCE)));
        // Read GPS traces into byte Array.
        byte[] byteTrack = c.getBlob(c.getColumnIndex(Globals.KEY_GPS_DATA));
        Location[] locarray = Utils.fromByteArrayToLocationArray(byteTrack);
        ArrayList<Location> loclist = new ArrayList<>(
            Arrays.asList(locarray));
        setLocationList(loclist);
        c.close();
    }

    public static void deleteEntryInDB(Context context, long id) {
        context.getContentResolver().delete(
            Uri.parse(HistoryProvider.CONTENT_URI + "/"
                + String.valueOf(id)), null, null);
    }

    public void deleteEntryInDB(Context context) {
        deleteEntryInDB(context, getID());
    }

    public String[] getStatsDescription() {
        String[] stats = new String[2];
        stats[1] = "" + decimalFormat.format(getDistance() / Globals.KILO / Globals.KM2MILE_RATIO) + " ";
        return stats;
    }

    public void startLogging() throws Exception {
        mData.setDistance(0.0);
        mNLocations = 0;
    }

    // Update the stats based on the newly acquired data in mLocationList
    public void updateStats() throws Exception {
        // Dumping mLocationList to Location[] track
        synchronized (mLocationList) {
            if (mLocationList.size() == mNLocations || mLocationList.size() < 2) {
                return;
            }
            int iStart = (mNLocations == 0) ? 1 : mNLocations;
            Location prevLoc, currLoc;
            for (int i = iStart; i < mLocationList.size(); i++) {
                prevLoc = mLocationList.get(i - 1);
                currLoc = mLocationList.get(i);
                mData.setDistance(mData.getDistance()
                    + prevLoc.distanceTo(currLoc));
            }
            mNLocations = mLocationList.size();
        }
    }

    public void setLocationList(ArrayList<Location> list) {
        mLocationList = list;
    }

    public ArrayList<Location> getLocationList() {
        return mLocationList;
    }

    public double getDistance() {
        return mData.getDistance();
    }

    public void setDistance(int distanceInMeters) {
        mData.setDistance(distanceInMeters);
    }

    public void setActivityType(int activityTypeCode) {
        mData.setActivityType(activityTypeCode);
    }

    public void setInputType(int inputTypeCode) {
        mData.setInputType(inputTypeCode);
    }

    public void setID(long id) {
        mData.setId(id);
    }

    public long getID() {
        return mData.getId();
    }
}
