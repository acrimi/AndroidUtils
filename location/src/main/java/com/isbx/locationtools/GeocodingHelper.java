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
 * Created by alexs_000 on 5/25/2016.
 */
public class GeocodingHelper {

    private static final int ADDRESS_RESULT_LIMIT = 15;
    private Geocoder geocoder;
    private Handler handler;

    public GeocodingHelper(Context context) {
        geocoder = new Geocoder(context, Locale.getDefault());
        handler = new Handler(context.getMainLooper());
    }

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
                            callback.onAddressRetreived(result);
                        }
                    });
                }
            }
        }).start();
    }

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

    public interface AddressCallback {
        void onAddressRetreived(String address);
    }

    public interface AddressListCallback {
        void onAddressesSearched(List<Address> addresses);
    }
}
