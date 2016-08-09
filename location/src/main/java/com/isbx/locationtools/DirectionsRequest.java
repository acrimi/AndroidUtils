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
 * Created by alexs_000 on 6/1/2016.
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

    public DirectionsRequest() {
        this(DIRECTIONS_ENDPOINT);
    }

    public DirectionsRequest(String endpointUrl) {
        setEndpointUrl(endpointUrl);
        handler = new Handler(Looper.getMainLooper());
    }

    public DirectionsRequest(LatLng start, LatLng end) {
        this(DIRECTIONS_ENDPOINT, start, end);
    }

    public DirectionsRequest(String endpointUrl, LatLng start, LatLng end) {
        this(endpointUrl, start.latitude, start.longitude, end.latitude, end.longitude);
    }

    public DirectionsRequest(double startLatitude, double startLongitude, double endLatitude, double endLongitude) {
        this(DIRECTIONS_ENDPOINT, startLatitude, startLongitude, endLatitude, endLongitude);
    }

    public DirectionsRequest(String endpointUrl, double startLatitude, double startLongitude, double endLatitude, double endLongitude) {
        this(endpointUrl);
        setStart(startLatitude, startLongitude);
        setEnd(endLatitude, endLongitude);
    }

    public void setEndpointUrl(String url) {
        this.endpointUrl = url;
    }

    public void setStart(LatLng start) {
        setStart(start.latitude, start.longitude);
    }

    public void setEnd(LatLng end) {
        setEnd(end.latitude, end.longitude);
    }

    public void setStart(double latitude, double longitude) {
        this.startLatitude = latitude;
        this.startLongitude = longitude;
    }

    public void setEnd(double latitude, double longitude) {
        this.endLatitude = latitude;
        this.endLongitude = longitude;
    }

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


    public interface DirectionsJSONCallback {
        void onDirectionsResult(JSONObject directions);
    }

    public interface DirectionsPolylineCallback {
        void onDirectionsResult(PolylineOptions directions, int meters, int seconds);
    }
}
