package com.example.abc;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.List;

public class LocationHandler {
    private Activity activity;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLastKnownLocation;
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;
    private OnLocationUpdateListener onLocationUpdateListener;
    private boolean updateStartedInternally = false;

    public LocationHandler(Activity activity, final OnLocationUpdateListener onLocationUpdateListener) {
        this.activity = activity;
        this.onLocationUpdateListener = onLocationUpdateListener;
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity);
        createLocationRequest();
        getDeviceLocation();

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                List<Location> locationList = locationResult.getLocations();
                if (locationList.size() > 0) {
                    //The last location in the list is the newest
                    Location location = locationList.get(locationList.size() - 1);
                    mLastKnownLocation = location;
                    if (onLocationUpdateListener != null) {
                        onLocationUpdateListener.onLocationChange(location);
                        if (updateStartedInternally) {
                            stopLocationUpdate();
                        }
                    }
                }
            }
        };
    }

    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            Task locationResult = mFusedLocationProviderClient.getLastLocation();

            locationResult.addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        // Set the map's camera position to the current location of the device.
                        mLastKnownLocation = (Location) task.getResult();
                        if (mLastKnownLocation == null) {
                            updateStartedInternally = true;
                            mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        } else {
                            onLocationUpdateListener.onLocationChange(mLastKnownLocation);
                        }
                    } else {
                        onLocationUpdateListener.onError("Can't get Location");
                    }
                }
            });
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
            onLocationUpdateListener.onError(e.getMessage());

        }
    }

    public void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        updateStartedInternally = false;
        mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }

    private void stopLocationUpdate() {
        mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
    }


    //other new Methods but not using right now..
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);//set the interval in which you want to get locations
        mLocationRequest.setFastestInterval(5000);//if a location is available sooner you can get it (i.e. another app is using the location services)
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    static interface OnLocationUpdateListener {
        void onLocationChange(Location location);

        void onError(String error);
    }
}


