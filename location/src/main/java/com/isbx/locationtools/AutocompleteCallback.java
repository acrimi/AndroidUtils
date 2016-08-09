package com.isbx.locationtools;

import com.google.android.gms.location.places.AutocompletePrediction;

import java.util.List;

/**
 * Created by lvinson on 7/29/16.
 */
public interface AutocompleteCallback {
    void onAutocompletePredictionsReceived(List<AutocompletePrediction> autocompletePredictionList);
}
