package com.isbx.locationtools;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A helper class for making requests to
 * <a href="https://developers.google.com/maps/documentation/directions/start">Google's Directions
 * API</a>.
 *
 * <p>
 * By default, this will make anonymous requests directly to the Directions API endpoint, which is
 * subject to stricter rate limiting quotas. If you wish to use an API key instead, the
 * recommended approach is to obtain a server key and set up your own remote server to proxy
 * requests from the mobile client to the Directions API. To support this, you can optionally
 * specify a custom endpoint url pointing to your remote server. Your custom endpoint will be
 * expected to conform to the same specification as the Directions API for the {@code origin} and
 * {@code destination} parameters.
 * </p>
 */
public class DirectionsRequest {
    private static final String TAG = DirectionsRequest.class.getSimpleName();

    private static final String DIRECTIONS_ENDPOINT = "https://maps.googleapis.com/maps/api/directions/json";
    private static final String QUERY_STRING_FORMAT = "?origin=%f,%f&destination=%f,%f";

    private String endpointUrl = DIRECTIONS_ENDPOINT;

    private double startLatitude;
    private double startLongitude;
    private double endLatitude;
    private double endLongitude;

    private Handler handler;

    /**
     * Creates a new DirectionsRequest pointing directly to the Directions API endpoint.
     */
    public DirectionsRequest() {
        this(DIRECTIONS_ENDPOINT);
    }

    /**
     * Creates a new DirectionsRequest that will use {@code endpointUrl} to make its request.
     *
     * @param endpointUrl A custom url that will be used for the request
     */
    public DirectionsRequest(String endpointUrl) {
        setEndpointUrl(endpointUrl);
        handler = new Handler(Looper.getMainLooper());
    }

    /**
     * Creates a new DirectionsRequest for the given start and end locations pointing directly to
     * the Directions API endpoint.
     *
     * @param start A {@link LatLng} representing the start location for the directions
     * @param end A {@link LatLng} representing the end location for the directions
     */
    public DirectionsRequest(LatLng start, LatLng end) {
        this(DIRECTIONS_ENDPOINT, start, end);
    }

    /**
     * Creates a new DirectionsRequest for the given start and end locations that will use
     * {@code endpointUrl} to make its request.
     *
     * @param endpointUrl A custom url that will be used for the request
     * @param start A {@link LatLng} representing the start location for the directions
     * @param end A {@link LatLng} representing the end location for the directions
     */
    public DirectionsRequest(String endpointUrl, LatLng start, LatLng end) {
        this(endpointUrl, start.latitude, start.longitude, end.latitude, end.longitude);
    }

    /**
     * Creates a new DirectionsRequest for the given start and end locations pointing directly to
     * the Directions API endpoint.
     *
     * @param startLatitude The latitude of the start point for the directions
     * @param startLongitude The longitude of the start point for the directions
     * @param endLatitude The latitude of the end point for the directions
     * @param endLongitude The longitude of the end point for the directions
     */
    public DirectionsRequest(double startLatitude, double startLongitude, double endLatitude, double endLongitude) {
        this(DIRECTIONS_ENDPOINT, startLatitude, startLongitude, endLatitude, endLongitude);
    }

    /**
     * Creates a new DirectionsRequest for the given start and end locations that will use
     * {@code endpointUrl} to make its request.
     *
     * @param endpointUrl A custom url that will be used for the request
     * @param startLatitude The latitude of the start point for the directions
     * @param startLongitude The longitude of the start point for the directions
     * @param endLatitude The latitude of the end point for the directions
     * @param endLongitude The longitude of the end point for the directions
     */
    public DirectionsRequest(String endpointUrl, double startLatitude, double startLongitude, double endLatitude, double endLongitude) {
        this(endpointUrl);
        setStart(startLatitude, startLongitude);
        setEnd(endLatitude, endLongitude);
    }

    /**
     * Sets the custom endpoint url for this DirectionsRequest to use. The custom endpoint will be
     * expected to conform to the same specification as the Directions API for the {@code origin} and
     * {@code destination} parameters.
     *
     * @param url The custom url that will be used for the request
     */
    public void setEndpointUrl(String url) {
        this.endpointUrl = url;
    }

    /**
     * Sets the start location for the directions.
     *
     * @param start A {@link LatLng} representing the start location for the directions
     */
    public void setStart(LatLng start) {
        setStart(start.latitude, start.longitude);
    }

    /**
     * Sets the end location for the directions.
     *
     * @param end A {@link LatLng} representing the end location for the directions
     */
    public void setEnd(LatLng end) {
        setEnd(end.latitude, end.longitude);
    }

    /**
     * Sets the start location for the directions.
     *
     * @param latitude The latitude of the start point for the directions
     * @param longitude The longitude of the start point for the directions
     */
    public void setStart(double latitude, double longitude) {
        this.startLatitude = latitude;
        this.startLongitude = longitude;
    }

    /**
     * Sets the end location for the directions.
     *
     * @param latitude The latitude of the end point for the directions
     * @param longitude The longitude of the end point for the directions
     */
    public void setEnd(double latitude, double longitude) {
        this.endLatitude = latitude;
        this.endLongitude = longitude;
    }

    /**
     * Executes the directions request asynchronously, and returns the raw {@link JSONObject} result
     * to {@code callback}.
     *
     * @param callback A {@link DirectionsJSONCallback} to be notified when the request completes
     */
    public void getDirections(final DirectionsJSONCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection urlConnection = null;
                JSONObject res = null;

                try {
                    String queryParams = String.format(Locale.US, QUERY_STRING_FORMAT, startLatitude, startLongitude, endLatitude, endLongitude);
                    URL url = new URL(endpointUrl + queryParams);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    String line;

                    StringBuilder builder = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        builder.append(line);
                    }

                    res = new JSONObject(builder.toString());
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }

                if (res != null && res.has("error_message")) {
                    Log.d(TAG, res.optString("status"));
                    Log.d(TAG, res.optString("error_message"));
                }

                final JSONObject directions = res;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onDirectionsResult(directions);
                    }
                });
            }
        }).start();
    }

    /**
     * Executes the directions request and parses the relevant polyline, time, and distance
     * information from the response to return to {@code callback}.
     *
     * @param callback A {@link DirectionsPolylineCallback} to be notified when the request
     *                 completes
     */
    public void getDirectionsAsPolyline(final DirectionsPolylineCallback callback) {
        getDirections(new DirectionsJSONCallback() {
            @Override
            public void onDirectionsResult(JSONObject directions) {
                PolylineOptions polylineOptions = new PolylineOptions();
                int meters = 0;
                int seconds = 0;
                if (directions != null) {
                    try {
                        JSONArray routes = directions.getJSONArray("routes");
                        if (routes.length() > 0) {
                            JSONObject route = routes.getJSONObject(0);
                            JSONArray legs = route.getJSONArray("legs");

                            int len = legs.length();
                            for (int i = 0; i < len; i++) {
                                JSONObject leg = legs.getJSONObject(i);
                                meters += leg.getJSONObject("distance").getInt("value");
                                seconds += leg.getJSONObject("duration").getInt("value");
                                JSONArray steps = leg.getJSONArray("steps");

                                int stepLen = steps.length();
                                for (int j = 0; j < stepLen; j++) {
                                    JSONObject step = steps.getJSONObject(j);
                                    String encodedPoints = step.getJSONObject("polyline").getString("points");
                                    List<LatLng> points = decodePoly(encodedPoints);

                                    for (LatLng p : points) {
                                        polylineOptions.add(p);
                                    }
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                callback.onDirectionsResult(polylineOptions, meters, seconds);
            }
        });
    }


    /**
     * Method Courtesy :
     * jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
     * */
    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }


    /**
     * Callback interface to receive a directions {@link JSONObject} from an asynchronous request.
     */
    public interface DirectionsJSONCallback {
        /**
         * Invoked when a directions object has been received.
         *
         * @param directions A {@link JSONObject} containing the directions information
         */
        void onDirectionsResult(JSONObject directions);
    }

    /**
     * Callback interface to receive a {@link PolylineOptions} from an asynchronous request.
     */
    public interface DirectionsPolylineCallback {
        /**
         * Invoked when a directions response has been received and parsed into a
         * {@link PolylineOptions} and distance/time values.
         *
         * @param directions A {@link PolylineOptions} object representing the path of the directions
         * @param meters The total distance of the directions in meters
         * @param seconds The total estimated time for the directions in seconds
         */
        void onDirectionsResult(PolylineOptions directions, int meters, int seconds);
    }
}
