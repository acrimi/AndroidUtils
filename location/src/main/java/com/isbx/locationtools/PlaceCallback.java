package com.isbx.locationtools;

import com.google.android.gms.location.places.Place;

/**
 * Created by lvinson on 7/29/16.
 */
public interface PlaceCallback {
        void onPlaceReceived(Place place);
}
