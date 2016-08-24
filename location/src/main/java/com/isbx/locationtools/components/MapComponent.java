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
 * This class serves to simplify the process of integrating a
 * <a href="https://developers.google.com/maps/documentation/android-api/">Google Maps</a> view into
 * your app and the common task of initializing it to the user's location.
 *
 * <p>
 * Initializing a simple map operates very similarly to the standard implementation. For instance,
 * to use a {@link com.google.android.gms.maps.SupportMapFragment} inside an Activity:
 * </p>
 *
 * <pre>
 * <code>MapComponent mapComponent = new MapComponent(this);
 * SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
 * mapFragment.getMapAsync(mapComponent);</code>
 * </pre>
 *
 * <p>
 * To then have this map fragment be initialized to the user's current location automatically, you
 * simply add one line after creating the MapComponent:
 * </p>
 *
 * <pre>
 * <code>MapComponent mapComponent = new MapComponent(this);
 * <strong>mapComponent.setInitToMyLocation(this);</strong>
 * SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
 * mapFragment.getMapAsync(mapComponent);</code>
 * </pre>
 *
 * <p>
 * The MapComponent will take care of connecting to the Google API Client, requesting any necessary
 * permissions, retrieving the location, and updating the map object automatically, saving a lot of
 * boilerplate code.
 * </p>
 *
 * <p>
 * MapComponent implements {@link OnMapReadyCallback} and in most cases should be used as the
 * callback in your map fragments in views. If, however, your code also needs to be notified when
 * the map object is ready, you can use your own {@link OnMapReadyCallback}, just be sure to call
 * {@link MapComponent#onMapReady(GoogleMap)} from it in order for the MapComponent to behave
 * correctly.
 * </p>
 *
 * <p>
 * <strong>Note:</strong> You must through calls to
 * {@link MapComponent#onRequestPermissionsResult(int, String[], int[])} from your Activity
 * or Fragment in order for automatic permissions checking to work on API &gt;= 6.0.
 * </p>
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

    /**
     * Creates a new MapComponent for the given Activity context.
     *
     * @param activity The {@link Activity} to tie this MapComponent to
     */
    public MapComponent(Activity activity) {
        this.activity = activity;
    }

    /**
     * Specifies whether or not this MapComponent should automatically center the map object on the
     * user's current location when it initializes.
     *
     * @param initToMyLocation {@code true} if the map should center on the user's location,
     *                         {@code false} otherwise
     */
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


    /**
     * Returns the {@link GoogleMap} that has been loaded with this MapComponent.
     *
     * @return The {@link GoogleMap} object for this component, or null if the map hasn't loaded yet
     */
    public GoogleMap getMap() {
        return map;
    }

    /**
     * Returns whether this component has an initialized {@link GoogleMap} associated with it.
     *
     * @return {@code true} if this component has a loaded {@link GoogleMap}, {@code false} otherwise
     */
    public boolean isReady() {
        return map != null;
    }

    /**
     * Returns the location that this component initialized the map with if
     * {@link MapComponent#setInitToMyLocation(boolean)} is enabled.
     *
     * @return A {@link Location} object, or {@code null} if
     *         {@link MapComponent#setInitToMyLocation(boolean)} is disabled or the location request
     *         hasn't completed yet.
     */
    public Location getInitialLocation() {
        return initialLocation;
    }

    /**
     * Returns the {@link MyLocationComponent} that this component uses internally if
     * {@link MapComponent#setInitToMyLocation(boolean)} is enabled. This is helpful if you need to
     * perform additional location operations and want to reuse the same location component for
     * efficiency's sake.
     *
     * @return The {@link MyLocationComponent} use for the initial location request, or {@code null}
     *         if {@link MapComponent#setInitToMyLocation(boolean)} is disabled
     */
    public MyLocationComponent getLocationComponent() {
        return locationComponent;
    }

    /**
     * Sets a {@link LocationCallback} listener to be notified when the initial user location is
     * received by this component. This listener will never be called if
     * {@link MapComponent#setInitToMyLocation(boolean)} is disabled.
     *
     * @param listener A {@link LocationCallback} instance
     */
    public void setListener(LocationCallback listener) {
        this.listener = listener;
    }


    /**
     * Internal method that handles setting up the map instance with the user's initial location.
     * Since both operations are asynchronous and we don't know which will complete first, both
     * call through to this method which makes sure both the map and initial location ready before
     * attempting to update the map.
     */
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
