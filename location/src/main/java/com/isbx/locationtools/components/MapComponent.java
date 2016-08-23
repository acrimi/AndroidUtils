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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.isbx.locationtools.LocationCallback;
import com.isbx.locationtools.R;

/**
 * Created by alexs_000 on 6/1/2016.
 */
public class MapComponent implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback,
    GoogleApiClient.ConnectionCallbacks {

    private static final int REQUEST_MY_LOCATION = 100;
    private static final int DEFAULT_MAP_ZOOM_LEVEL = 14;

    private Activity activity;
    private GoogleMap map;

    private boolean initToMyLocation;
    private Location initialLocation;

    private MyLocationComponent locationComponent;
    private LocationCallback listener;

    public MapComponent(Activity activity) {
        this.activity = activity;
    }

    public void setInitToMyLocation(boolean initToMyLocation) {
        this.initToMyLocation = initToMyLocation;
        if (initToMyLocation) {
            locationComponent = new MyLocationComponent(activity);
            locationComponent.registerConnectionCallbacks(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        if (initToMyLocation) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                setupCurrentLocation();
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_MY_LOCATION);
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (initToMyLocation) {
            setupCurrentLocation();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_MY_LOCATION) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                setupCurrentLocation();
            } else {
                new AlertDialog.Builder(activity)
                    .setTitle(R.string.location_error_title)
                    .setMessage(R.string.location_error_msg)
                    .setPositiveButton(R.string.alert_button_ok, null)
                    .show();
            }
        }
    }


    public GoogleMap getMap() {
        return map;
    }

    public boolean isReady() {
        return map != null;
    }

    public Location getInitialLocation() {
        return initialLocation;
    }

    public MyLocationComponent getLocationComponent() {
        return locationComponent;
    }

    public void setListener(LocationCallback listener) {
        this.listener = listener;
    }


    private void setupCurrentLocation() {
        if (initToMyLocation && ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (map != null && !map.isMyLocationEnabled()) {
                map.setMyLocationEnabled(true);
            }
            if (locationComponent.isConnected() && initialLocation == null) {
                initialLocation = locationComponent.getLastLocation();
                if (initialLocation != null && map != null) {
                    LatLng latLng = new LatLng(initialLocation.getLatitude(), initialLocation.getLongitude());
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_MAP_ZOOM_LEVEL));

                    if (listener != null) {
                        listener.onLocationReceived(initialLocation);
                    }
                }
            }
        }
    }
}
