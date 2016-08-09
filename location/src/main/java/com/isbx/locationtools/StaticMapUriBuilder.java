package com.isbx.locationtools;

import android.net.Uri;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;
import java.util.Locale;

/**
 * Created by alexs_000 on 7/11/2016.
 */
public class StaticMapUriBuilder {

    public static final String MAP_TYPE_ROADMAP = "roadmap";
    public static final String MAP_TYPE_SATELLITE = "satellite";
    public static final String MAP_TYPE_HYBRID = "hybrid";
    public static final String MAP_TYPE_TERRAIN = "terrain";

    private static final String STATIC_MAPS_URL = "https://maps.googleapis.com/maps/api/staticmap";
    private static final String POINT_FORMAT = "%1$f,%2$f";
    private static final String SIZE_FORMAT = "%1$dx%2$d";
    private static final char PATH_PART_SEPARATOR = '|';

    private Uri.Builder uriBuilder;

    private int pathWeight = 5; // default weight
    private String pathColor;
    private String pathFillColor;
    private List<LatLng> pathPoints;

    public StaticMapUriBuilder(String apiKey) {
        uriBuilder = Uri.parse(STATIC_MAPS_URL).buildUpon();
        uriBuilder.appendQueryParameter("key", apiKey);
    }

    public StaticMapUriBuilder setSize(int width, int height) {
        uriBuilder.appendQueryParameter("size", String.format(Locale.US, SIZE_FORMAT, width, height));

        return this;
    }

    public StaticMapUriBuilder setZoom(int zoom) {
        uriBuilder.appendQueryParameter("zoom", ""+zoom);

        return this;
    }

    public StaticMapUriBuilder setCenter(LatLng latLng) {
        return setCenter(latLng.latitude, latLng.longitude);
    }

    public StaticMapUriBuilder setCenter(double lat, double lng) {
        uriBuilder.appendQueryParameter("center", String.format(Locale.US, POINT_FORMAT, lat, lng));


        return this;
    }

    public StaticMapUriBuilder setFormat(String format) {
        uriBuilder.appendQueryParameter("format", format);

        return this;
    }

    public StaticMapUriBuilder setMapType(String mapType) {
        uriBuilder.appendQueryParameter("mapType", mapType);

        return this;
    }

    public StaticMapUriBuilder setPathWeight(int weight) {
        this.pathWeight = weight;

        return this;
    }

    public StaticMapUriBuilder setPathColor(String color) {
        this.pathColor = color;

        return this;
    }

    public StaticMapUriBuilder setPathFillColor(String fillColor) {
        this.pathFillColor = fillColor;

        return this;
    }

    public StaticMapUriBuilder setPathPoints(List<LatLng> points) {
        this.pathPoints = points;

        return this;
    }

    public Uri build() {
        constructPathString();
        return uriBuilder.build();
    }

    private void constructPathString() {
        if (pathPoints != null && pathPoints.size() > 0) {
            StringBuilder pathBuilder = new StringBuilder();
            pathBuilder.append(pathWeight);

            if (pathColor != null) {
                pathBuilder.append(PATH_PART_SEPARATOR);
                pathBuilder.append(pathColor);
            }
            if (pathFillColor != null) {
                pathBuilder.append(PATH_PART_SEPARATOR);
                pathBuilder.append(pathFillColor);
            }

            for (LatLng latLng : pathPoints) {
                pathBuilder.append(PATH_PART_SEPARATOR);
                pathBuilder.append(String.format(Locale.US, POINT_FORMAT, latLng.latitude, latLng.longitude));
            }

            uriBuilder.appendQueryParameter("path", pathBuilder.toString());
        }
    }
}
