package com.isbx.locationtools.components;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.isbx.androidtools.utils.ActivityLifecycleListener;
import com.isbx.locationtools.LocationCallback;
import com.isbx.locationtools.R;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by alexs_000 on 6/1/2016.
 */
public class MyLocationComponent extends ActivityLifecycleListener
    implements ActivityCompat.OnRequestPermissionsResultCallback, GoogleApiClient.ConnectionCallbacks {
    private static final String TAG = MyLocationComponent.class.getSimpleName();
    public static final int PRIORITY_BALANCED_POWER_ACCURACY = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
    public static final int PRIORITY_HIGH_ACCURACY = LocationRequest.PRIORITY_HIGH_ACCURACY;
    public static final int PRIORITY_LOW_POWER = LocationRequest.PRIORITY_LOW_POWER;
    public static final int PRIORITY_NO_POWER = LocationRequest.PRIORITY_NO_POWER;

    private static final int REQUEST_MY_LOCATION = 100;

    private GoogleApiClient googleApiClient;

    private LocationCallback pendingCallback;

    private boolean backgroundLocationService;

    private boolean requestLocationUpdates;
    private boolean locationUpdateIsRunning;
    private int locationRequestPriority;
    private long locationRequestInterval;
    private long locationRequestMinInterval;

    private LocationListener locationListener;

    public MyLocationComponent(Activity activity) {
        super(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        googleApiClient = new GoogleApiClient.Builder(getActivity())
            .addConnectionCallbacks(this)
            .addApi(LocationServices.API)
            .build();
    }

    @Override
    public void onStart() {
        googleApiClient.connect();
    }

    @Override
    public void onStop() {
        if (!backgroundLocationService) {
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onDestroy() {
        if (requestLocationUpdates && googleApiClient.isConnected()) {
            stopLocationUpdates();
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (requestLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_MY_LOCATION) {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (pendingCallback != null) {
                    pendingCallback.onLocationReceived(getLastLocation());
                }

                if (requestLocationUpdates) {
                    startLocationUpdates();
                }
            } else {
                new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.location_error_title)
                    .setMessage(R.string.location_error_msg)
                    .setPositiveButton(R.string.alert_button_ok, null)
                    .show();

                if (pendingCallback != null) {
                    pendingCallback.onLocationReceived(null);
                }
            }
            pendingCallback = null;
        }
    }


    public void getLastLocation(LocationCallback callback) {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            callback.onLocationReceived(LocationServices.FusedLocationApi.getLastLocation(googleApiClient));
        } else {
            pendingCallback = callback;
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_MY_LOCATION);
        }
    }

    public Location getLastLocation() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        }

        return null;
    }

    public boolean isConnected() {
        return googleApiClient.isConnected();
    }

    public void registerConnectionCallbacks(GoogleApiClient.ConnectionCallbacks callbacks) {
        googleApiClient.registerConnectionCallbacks(callbacks);
    }

    public void setBackgroundLocationService(boolean enable) {
        backgroundLocationService = enable;
    }

    // requestLocationUpdates
    public void setRequestLocationUpdates(boolean enable, LocationListener locationListener) {
        this.locationListener = locationListener;
        requestLocationUpdates = enable;
    }

    public void setLocationRequest(int priority, long interval, long minInterval) {
        locationRequestPriority = priority;
        locationRequestInterval = interval;
        locationRequestMinInterval = minInterval;
    }

    protected void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, LocationRequest
                    .create()
                    .setPriority(locationRequestPriority)
                    .setInterval(locationRequestInterval)
                    .setFastestInterval(locationRequestMinInterval), locationListener);
            locationUpdateIsRunning = true;
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_MY_LOCATION);
        }
    }

    public void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, locationListener);
        locationUpdateIsRunning = false;
    }

    public void resumeLocationUpdates() {
        if (googleApiClient.isConnected() && !locationUpdateIsRunning) {
            startLocationUpdates();
        }
    }
}
