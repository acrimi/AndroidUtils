package com.isbx.locationtools.components;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.data.DataBufferUtils;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLngBounds;
import com.isbx.androidtools.utils.ActivityLifecycleListener;
import com.isbx.locationtools.AutocompleteCallback;
import com.isbx.locationtools.PlaceCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by alexs_000 on 8/2/2016.
 */
public class PlacesComponent extends ActivityLifecycleListener implements GoogleApiClient.ConnectionCallbacks {
    private static final String TAG = PlacesComponent.class.getSimpleName();

    private GoogleApiClient googleApiClient;

    public PlacesComponent(Activity activity) {
        super(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        googleApiClient = new GoogleApiClient.Builder(getActivity())
            .addConnectionCallbacks(this)
            .addApi(Places.GEO_DATA_API)
            .build();
    }

    @Override
    public void onStart() {
        googleApiClient.connect();
    }

    @Override
    public void onStop() {
        googleApiClient.disconnect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {}

    @Override
    public void onConnectionSuspended(int i) {}

    public boolean isConnected() {
        return googleApiClient.isConnected();
    }

    public void registerConnectionCallbacks(GoogleApiClient.ConnectionCallbacks callbacks) {
        googleApiClient.registerConnectionCallbacks(callbacks);
    }


    public void getAutoCompletePredictions(String query, LatLngBounds bounds, final AutocompleteCallback callback) {
        new RetrieveAutocompletePredictionsTask(bounds, new AutocompleteCallback() {
            @Override
            public void onAutocompletePredictionsReceived(List<AutocompletePrediction> autocompletePredictionList) {
                if (callback != null) {
                    callback.onAutocompletePredictionsReceived(autocompletePredictionList);
                }
            }
        }).execute(query);
    }

    public class RetrieveAutocompletePredictionsTask extends AsyncTask<String, Integer, ArrayList<AutocompletePrediction>> {
        private AutocompleteCallback callback;
        private LatLngBounds bounds;

        public RetrieveAutocompletePredictionsTask(@Nullable LatLngBounds bounds, AutocompleteCallback callback) {
            this.callback = callback;
            this.bounds = bounds;
        }

        @Override
        protected ArrayList<AutocompletePrediction> doInBackground(String... params) {
            String query = params[0];
            PendingResult<AutocompletePredictionBuffer> result = Places.GeoDataApi.getAutocompletePredictions(googleApiClient, query, bounds ,null);
            AutocompletePredictionBuffer autocompletePredictions = result.await(60, TimeUnit.SECONDS);

            final com.google.android.gms.common.api.Status status = autocompletePredictions.getStatus();
            if (!status.isSuccess()) {
                Log.e(TAG, "Error getting autocomplete predictions: " + status.toString());
                autocompletePredictions.release();
                return null;
            }
            ArrayList<AutocompletePrediction> test = DataBufferUtils.freezeAndClose(autocompletePredictions);
            return test;
        }

        @Override
        protected void onPostExecute(ArrayList<AutocompletePrediction> predictions) {
            callback.onAutocompletePredictionsReceived(predictions);
        }
    }

    public void getPlaceById(String placeId, final PlaceCallback callback) {
        PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(googleApiClient, placeId);
        placeResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
            @Override
            public void onResult(@NonNull PlaceBuffer places) {
                if (!places.getStatus().isSuccess()) {
                    Log.e(TAG, "Error getting autocomplete predictions: " + places.getStatus().toString());
                    callback.onPlaceReceived(null);
                }
                callback.onPlaceReceived(places.get(0));
            }
        });
    }
}
