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
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLngBounds;
import com.isbx.androidtools.utils.ActivityLifecycleListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * A convenience class for interacting with the
 * <a href="https://developers.google.com/places/android-api/start">Google Places API</a>.
 *
 * <p>
 * This class handles the necessary {@link GoogleApiClient} connection link internally to reduce the
 * need for boilerplate code, and provides a simple interface for retrieving {@link Place}s and
 * performing autocomplete requests:
 * </p>
 *
 * <pre>
 * <code>PlacesComponent placesComponent = new PlacesComponent(this);
 * placesComponent.getAutoCompletePredictions(query, latLngBounds, new AutocompleteCallback() {
 *     &#064;Override
 *     public void onAutocompletePredictionsReceived(List&lt;AutocompletePrediction&gt; autocompletePredictionList) {
 *         // Handle autcomplete results
 *     }
 * });</code>
 * </pre>
 */
public class PlacesComponent extends ActivityLifecycleListener implements GoogleApiClient.ConnectionCallbacks {
    private static final String TAG = PlacesComponent.class.getSimpleName();

    private GoogleApiClient googleApiClient;

    private RetrieveAutocompletePredictionsTask pendingAutocompleteTask;
    private String pendingAutocompleteQuery;

    /**
     * Creates a new PlacesComponent tied to the given Activity context.
     *
     * @param activity The {@link Activity} to use with this component
     */
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
    public void onConnected(@Nullable Bundle bundle) {
        if (pendingAutocompleteTask != null) {
            RetrieveAutocompletePredictionsTask task = pendingAutocompleteTask;
            String query = pendingAutocompleteQuery;
            pendingAutocompleteTask = null;
            pendingAutocompleteQuery = null;
            task.execute(query);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {}

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
     * Performs an asynchronous request to find autocomplete results for the given query string.
     * The request will attempt to limit results to those that lie within {@code bounds}. The
     * results will be passed to {@code callback}, or {@code null} if the request fails.
     *
     * @param query The query string to get autocomplete results for
     * @param bounds A {@link LatLngBounds} object to use to restrict the results set
     * @param callback A {@link AutocompleteCallback} that will receive the
     *                 {@link AutocompletePrediction} results of the request, or {@code null} if the
     *                 request fails
     */
    public void getAutoCompletePredictions(String query, LatLngBounds bounds, final AutocompleteCallback callback) {
        RetrieveAutocompletePredictionsTask task = new RetrieveAutocompletePredictionsTask(bounds, new AutocompleteCallback() {
            @Override
            public void onAutocompletePredictionsReceived(List<AutocompletePrediction> autocompletePredictionList) {
                if (callback != null) {
                    callback.onAutocompletePredictionsReceived(autocompletePredictionList);
                }
            }
        });

        if (isConnected()) {
            task.execute(query);
        } else {
            pendingAutocompleteTask = task;
            pendingAutocompleteQuery = query;
        }
    }

    /**
     * Implementation of {@link AsyncTask} that performs autocomplete lookups for the given query
     * strings within a geographic area defined by the {@link LatLngBounds} passed into its
     * constructor. If successful, this task will return an {@link ArrayList} of the
     * {@link AutocompletePrediction} results, otherwise it will return null.
     *
     * @see com.google.android.gms.location.places.GeoDataApi#getAutocompletePredictions(GoogleApiClient, String, LatLngBounds, AutocompleteFilter)
     */
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

    /**
     * Performs an asynchronous request to retrieve a {@link Place} by its given id.
     *
     * @param placeId The id of the place to retrieve
     * @param callback A {@link PlaceCallback} to be notified with the resulting {@link Place}
     *
     * @see com.google.android.gms.location.places.GeoDataApi#getPlaceById(GoogleApiClient, String...)
     */
    public void getPlaceById(final String placeId, final PlaceCallback callback) {
        PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(googleApiClient, placeId);
        placeResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
            @Override
            public void onResult(@NonNull PlaceBuffer places) {
                if (!places.getStatus().isSuccess()) {
                    Log.e(TAG, "Error getting place for id `"+ placeId +"` "+ places.getStatus().toString());
                    callback.onPlaceReceived(null);
                    return;
                }
                callback.onPlaceReceived(places.get(0));
            }
        });
    }


    /**
     * Callback interface to receive {@link AutocompletePrediction}s from asynchronous operations.
     */
    public interface AutocompleteCallback {
        /**
         * Invoked when a list of autocomplete predictions has been received.
         *
         * @param autocompletePredictionList A {@link List} of {@link AutocompletePrediction}s
         */
        void onAutocompletePredictionsReceived(List<AutocompletePrediction> autocompletePredictionList);
    }

    /**
     * Callback interface to receive {@link Place} objects from asynchronous operations.
     */
    public interface PlaceCallback {
        /**
         * Invoked when a {@link Place} object has been received.
         *
         * @param place A {@link Place} object
         */
        void onPlaceReceived(Place place);
    }
}
