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
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.isbx.androidtools.utils.ActivityLifecycleListener;
import com.isbx.locationtools.LocationCallback;
import com.isbx.locationtools.R;

/**
 * This class to serves to simplify the common tasks of getting the user's current location and
 * requesting recurring location updates.
 *
 * <p>
 * The standard approach to this problem is to connect to the Google API Client, request any
 * necessary permissions from the user, then finally request the most recent location and/or start
 * location updates. This leads to a lot of redundant code that gets rewritten every time you need
 * to get the user's location in your app. This component simplifies the process by handling all of
 * those steps internally as well as automatically handling lifecycle events in the case of
 * recurring location updates.
 * </p>
 *
 * <p>
 * To retrieve the user's last known location, you would initialize the component and request the
 * location:
 * </p>
 *
 * <pre>
 * <code>MyLocationComponent locationComponent = new MyLocationComponent(this);
 * locationComponent.getLastLocation(new LocationCallback() {
 *     &#064;Override
 *     public void onLocationReceived(Location location) {
 *         // Handle location object
 *     }
 * });</code>
 * </pre>
 *
 * <p>
 * To enable periodic location updates:
 * </p>
 *
 * <pre>
 * <code>MyLocationComponent locationComponent = new MyLocationComponent(this);
 * locationComponent.setLocationRequestOptions(priority, updateInterval, fastestUpdateInterval);
 * locationComponent.setLocationUpdatesEnabled(true, locationListener);</code>
 * </pre>
 *
 * <p>
 * <strong>Note:</strong> You must through calls to
 * {@link MyLocationComponent#onRequestPermissionsResult(int, String[], int[])} from your Activity
 * or Fragment in order for automatic permissions checking to work on API &gt;= 6.0.
 * </p>
 */
public class MyLocationComponent extends ActivityLifecycleListener
    implements ActivityCompat.OnRequestPermissionsResultCallback, GoogleApiClient.ConnectionCallbacks {

    private static final String TAG = MyLocationComponent.class.getSimpleName();

    private static final int REQUEST_MY_LOCATION = 100;

    private GoogleApiClient googleApiClient;

    private LocationCallback connectionPendingCallback;
    private LocationCallback requestPendingCallback;

    private boolean backgroundLocationUpdatesEnabled;

    private boolean requestLocationUpdates;
    private boolean locationUpdateIsRunning;
    private int locationRequestPriority;
    private long locationRequestInterval;
    private long locationRequestMinInterval;

    private LocationListener locationListener;

    /**
     * Creates a new MyLocationComponent tied to the given Activity context.
     *
     * @param activity The {@link Activity} to use with this component
     */
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
        if (!backgroundLocationUpdatesEnabled) {
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
        if (connectionPendingCallback != null) {
            LocationCallback callback = connectionPendingCallback;
            connectionPendingCallback = null;
            getLastLocation(callback);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_MY_LOCATION) {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (requestPendingCallback != null) {
                    requestPendingCallback.onLocationReceived(getLastLocation());
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

                if (requestPendingCallback != null) {
                    requestPendingCallback.onLocationReceived(null);
                }
            }
            requestPendingCallback = null;
        }
    }


    /**
     * Retrieves the user's most recent known location and passes it to {@code callback}. If the api
     * client is currently connected and the app has the necessary location permissions, the
     * callback should execute immediately. Otherwise, the necessary connection and permission
     * operations will continue asynchronously and invoke the callback when they have completed.
     *
     * @param callback A {@link LocationCallback} to be notified of the user's last location
     */
    public void getLastLocation(LocationCallback callback) {
        if (!isConnected()) {
            connectionPendingCallback = callback;
            return;
        }
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            callback.onLocationReceived(LocationServices.FusedLocationApi.getLastLocation(googleApiClient));
        } else {
            requestPendingCallback = callback;
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_MY_LOCATION);
        }
    }

    /**
     * Returns the user's last known location if available.
     *
     * @return The last known {@link Location}, or {@code null} if the api client is not connected
     *         or location permissions have not been granted
     *
     * @see MyLocationComponent#getLastLocation(LocationCallback)
     */
    public Location getLastLocation() {
        if (isConnected() &&
            ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        }

        return null;
    }

    /**
     * Returns the connection state of the internal {@link GoogleApiClient}.
     *
     * @return {@code true} if the client is connected, {@code false} otherwise
     */
    public boolean isConnected() {
        return googleApiClient.isConnected();
    }

    /**
     * Register additional callbacks to be notified of the internal {@link GoogleApiClient}'s
     * connection events.
     *
     * @param callbacks A {@link com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks}
     *                  instance
     */
    public void registerConnectionCallbacks(GoogleApiClient.ConnectionCallbacks callbacks) {
        googleApiClient.registerConnectionCallbacks(callbacks);
    }

    /**
     * Sets whether periodic location updates should continue to run in the background. Enabling
     * this will increase the resource consumption of your app and can contribute to battery drain
     * if not used carefully. This has no effect if location updates have not been enabled via
     * {@link MyLocationComponent#setLocationUpdatesEnabled(boolean, LocationListener)}. Defaults to
     * false.
     *
     * @param enabled {@code true} if location updates should continue to run when the app is in the
     *                background, {@code false} otherwise
     *
     * @see MyLocationComponent#setLocationUpdatesEnabled(boolean, LocationListener)
     */
    public void setBackgroundLocationUpdatesEnabled(boolean enabled) {
        backgroundLocationUpdatesEnabled = enabled;
    }

    /**
     * Sets whether this component should request periodic location updates. If enabled,
     * {@code locationListener} will receive the updated {@link Location} objects.
     *
     * @param enabled {@code true} if location updates should be enabled, {@code false} otherwise
     * @param locationListener A {@link LocationListener} to be notified of the periodic location
     *                         updates
     *
     * @see MyLocationComponent#setLocationRequestOptions(int, long, long)
     * @see com.google.android.gms.location.FusedLocationProviderApi#requestLocationUpdates(GoogleApiClient, LocationRequest, LocationListener)
     */
    public void setLocationUpdatesEnabled(boolean enabled, LocationListener locationListener) {
        this.locationListener = locationListener;
        requestLocationUpdates = enabled;
    }

    /**
     * Specifies the location request parameters that will be used to request periodic updates. This
     * has no effect if location updates have not been enabled via
     * {@link MyLocationComponent#setLocationUpdatesEnabled(boolean, LocationListener)}.
     *
     * @param priority A hint to the location client on how accurate the updates should be. Must be
     *                 one of {@link LocationRequest#PRIORITY_HIGH_ACCURACY},
     *                 {@link LocationRequest#PRIORITY_BALANCED_POWER_ACCURACY},
     *                 {@link LocationRequest#PRIORITY_LOW_POWER}, or
     *                 {@link LocationRequest#PRIORITY_NO_POWER}.
     * @param interval The desired interval for active location updates, in milliseconds
     * @param minInterval The fastest interval for location updates, in milliseconds.
     *
     * @see LocationRequest#setPriority(int)
     * @see LocationRequest#setInterval(long)
     * @see LocationRequest#setFastestInterval(long)
     */
    public void setLocationRequestOptions(int priority, long interval, long minInterval) {
        locationRequestPriority = priority;
        locationRequestInterval = interval;
        locationRequestMinInterval = minInterval;
    }

    /**
     * Performs permission request if necessary and starts location updates using the parameters
     * set by {@link MyLocationComponent#setLocationRequestOptions(int, long, long)} and the
     * {@link LocationListener} set by
     * {@link MyLocationComponent#setLocationUpdatesEnabled(boolean, LocationListener)}.
     */
    protected void startLocationUpdates() {
        if (locationUpdateIsRunning) {
            return;
        }
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

    /**
     * Cancels periodic location updates for this component.
     *
     * @see MyLocationComponent#resumeLocationUpdates()
     */
    public void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, locationListener);
        locationUpdateIsRunning = false;
    }

    /**
     * Starts location updates if not already running.
     *
     * @see MyLocationComponent#stopLocationUpdates()
     */
    public void resumeLocationUpdates() {
        if (googleApiClient.isConnected() && !locationUpdateIsRunning) {
            startLocationUpdates();
        }
    }
}
