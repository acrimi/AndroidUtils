package com.isbx.locationtools;

import android.location.Location;

/**
 * A generic callback interface to retrieve {@link Location}s from asynchronous operations.
 */
public interface LocationCallback {
    /**
     * Invoked when a {@link Location} has been received.
     *
     * @param location A {@link Location} object
     */
    void onLocationReceived(Location location);
}
