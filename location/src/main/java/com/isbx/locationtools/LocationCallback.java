package com.isbx.locationtools;

import android.location.Location;

/**
 * Created by alexs_000 on 6/1/2016.
 */
public interface LocationCallback {
    void onLocationReceived(Location location);
}
