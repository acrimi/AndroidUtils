package com.isbx.locationtools;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A convenience class for interacting with Android's {@link Geocoder} class. Provided are helper
 * methods for performing reverse geocoding operations as well performing address queries with a
 * search string.
 *
 * <p>
 * For more advanced location based searching, see the
 * {@link com.isbx.locationtools.components.PlacesComponent} class.
 * </p>
 */
public class GeocodingHelper {

    private static final int ADDRESS_RESULT_LIMIT = 15;
    private Geocoder geocoder;
    private Handler handler;

    /**
     * Creates a new GeocodingHelper with the given context.
     *
     * @param context A {@link Context} that will be used for executing request callbacks
     */
    public GeocodingHelper(Context context) {
        geocoder = new Geocoder(context, Locale.getDefault());
        handler = new Handler(context.getMainLooper());
    }

    /**
     * Uses reverse geocoding to retrieve a human readable address for the given coordinates. The
     * result will be parsed into an address string and passed to {@code callback}.
     *
     * <p>
     * <strong>Note:</strong> The callback will be executed on the main thread of the application.
     * </p>
     *
     * @param lat The latitude coordinate to process
     * @param lng The longitude coordinate to process
     * @param callback A {@link AddressCallback} that will be notified when the request completes
     *
     * @see Geocoder#getFromLocation(double, double, int)
     */
    public void getAddress(final double lat, final double lng, final AddressCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                StringBuilder addressBuilder = new StringBuilder();
                try {
                    List<Address> results = geocoder.getFromLocation(lat, lng, 1);
                    if (results != null && results.size() > 0) {
                        Address address = results.get(0);
                        int len = address.getMaxAddressLineIndex() + 1;
                        for (int i = 0; i < len; i++) {
                            if (i > 0) {
                                addressBuilder.append(", ");
                            }
                            addressBuilder.append(address.getAddressLine(i));
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (callback != null) {
                    final String result = addressBuilder.toString();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onAddressRetrieved(result);
                        }
                    });
                }
            }
        }).start();
    }

    /**
     * Searches for addresses that are similar to the specified {@code locationName} asynchronously.
     * The search will attempt to limit results to those that lie within the bounds specified. The
     * response is limited to the top 15 results.
     *
     * <p>
     * For higher accuracy results, it is recommended to use the Google Places API and
     * {@link com.isbx.locationtools.components.PlacesComponent}.
     * </p>
     *
     * @param locationName The location name to search for
     * @param lowerLeftLatitude The the lower latitude of the search bounds
     * @param lowerLeftLongitude The left longitude of the search bounds
     * @param upperRightLatitude The upper latitude of the search bounds
     * @param upperRightLongitude The right longitude of the search bounds
     * @param callback A {@link AddressListCallback} to be notified of the results of the request
     *
     * @see Geocoder#getFromLocationName(String, int, double, double, double, double)
     */
    public void searchAddress(final String locationName, final double lowerLeftLatitude, final double lowerLeftLongitude, final double upperRightLatitude, final double upperRightLongitude, final AddressListCallback callback) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                List<Address> addresses = new ArrayList<>();
                try {
                    addresses = geocoder.getFromLocationName(locationName, ADDRESS_RESULT_LIMIT, lowerLeftLatitude, lowerLeftLongitude, upperRightLatitude, upperRightLongitude);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                notifyAddressResults(addresses, callback);
            }
        }).start();
    }

    /**
     * Searches for addresses that are similar to the specified {@code locationName} asynchronously.
     * The response is limited to the top 15 results.
     *
     * <p>
     * For higher accuracy results, it is recommended to use the Google Places API and
     * {@link com.isbx.locationtools.components.PlacesComponent}.
     * </p>
     *
     * @param locationName The location name to search for
     * @param callback A {@link AddressListCallback} to be notified of the results of the request
     *
     * @see Geocoder#getFromLocationName(String, int)
     */
    public void searchAddress(final String locationName, final AddressListCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<Address> addresses = new ArrayList<>();
                try {
                    addresses = geocoder.getFromLocationName(locationName, ADDRESS_RESULT_LIMIT);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                notifyAddressResults(addresses, callback);
            }
        }).start();
    }

    private void notifyAddressResults(List<Address> addresses, final AddressListCallback callback) {
        if (callback != null) {
            final List<Address> results = addresses;
            final Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    callback.onAddressesSearched(results);
                }
            };
            handler.post(runnable);
        }
    }

    /**
     * Callback interface to retrieve single addresses from asynchronous operations.
     */
    public interface AddressCallback {
        /**
         * Invoked when an address has been retrieved.
         *
         * @param address The address string
         */
        void onAddressRetrieved(String address);
    }

    /**
     * Callback interface to retrieve {@link List}s of {@link Address}es from asynchronous
     * operations.
     */
    public interface AddressListCallback {
        /**
         * Invoked when a list of addresses has been retrieved.
         *
         * @param addresses A {@link List} of {@link Address}es
         */
        void onAddressesSearched(List<Address> addresses);
    }
}
