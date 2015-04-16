package com.eugene.googlemaps.Mapping;

import android.content.Context;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class Utils {

    public static byte[] fromLocationArrayToByteArray(Location[] locationArray) {
        int[] intArray = new int[locationArray.length * 2];
        for (int i = 0; i < locationArray.length; i++) {
            intArray[i * 2] = (int) (locationArray[i].getLatitude() * 1E6);
            intArray[(i * 2) + 1] = (int) (locationArray[i].getLongitude() * 1E6);
        }
        ByteBuffer byteBuffer = ByteBuffer.allocate(intArray.length
            * Integer.SIZE);
        IntBuffer intBuffer = byteBuffer.asIntBuffer();
        intBuffer.put(intArray);
        return byteBuffer.array();
    }

    public static Location[] fromByteArrayToLocationArray(byte[] bytePointArray) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytePointArray);
        IntBuffer intBuffer = byteBuffer.asIntBuffer();
        int[] intArray = new int[bytePointArray.length / Integer.SIZE];
        intBuffer.get(intArray);
        Location[] locationArray = new Location[intArray.length / 2];
        for (int i = 0; i < locationArray.length; i++) {
            locationArray[i] = new Location("");
            locationArray[i].setLatitude((double) intArray[i * 2] / 1E6F);
            locationArray[i].setLongitude((double) intArray[i * 2 + 1] / 1E6F);
        }
        return locationArray;
    }

    public static LatLng fromLocationToLatLng(Location location) {
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    public static int dpToPx(Context context, float dp) {
        // Took from http://stackoverflow.com/questions/8309354/formula-px-to-dp-dp-to-px-android
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) ((dp * scale) + 0.5f);
    }
}
