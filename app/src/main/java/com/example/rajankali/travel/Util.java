package com.example.rajankali.travel;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.graphics.Typeface;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by rajan.kali on 7/26/2017.
 * Utility Class
 */

public class Util {
    static double calculateDistance(LatLng source, LatLng dest) {
        double lat2 = source.latitude;
        double lon2 = source.longitude;
        double lat1 = dest.latitude;
        double lon1 = dest.longitude;
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist * 1.609344);
    }

    //This function converts decimal degrees to radians
    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    //This function converts radians to decimal degrees
    private static double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    static double round(float value) {
        long factor = (long) Math.pow(10, 2);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    @BindingAdapter("font")
    public static void setFont(TextView view, String fontName) {
        Context context = view.getContext();
        String fontPath = "Fonts/"+fontName+".ttf";
        try {
            view.setTypeface(Typeface.createFromAsset(context.getAssets(), fontPath));
        } catch (Exception e) {
            view.setTypeface(Typeface.DEFAULT);
        }
    }
}
