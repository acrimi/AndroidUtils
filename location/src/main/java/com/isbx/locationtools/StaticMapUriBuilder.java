package com.isbx.locationtools;

import android.net.Uri;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;
import java.util.Locale;

/**
 * <p>
 * A convenience class for utilizing
 * <a href="https://developers.google.com/maps/documentation/static-maps/intro">
 * Google's Static Maps API</a>. An instance of this class can be used to configure and create a
 * {@link Uri} representing a request to the Static Maps API for the appropriate map image. The
 * Static Maps API returns an image file, so the uri can be used directly with any remote image
 * loading library.
 * </p>
 *
 * <p><strong>Note:</strong> A Google developer account and API key are required to use the Static
 * Maps API.</p>
 */
public class StaticMapUriBuilder {

    /**
     * Map type that displays the standard map ui as normally shown on the Google Maps website.
     *
     * @see #setMapType(String)
     * @see <a href="https://developers.google.com/maps/documentation/static-maps/intro#MapTypes">
     *     https://developers.google.com/maps/documentation/static-maps/intro#MapTypes</a>
     */
    public static final String MAP_TYPE_ROADMAP = "roadmap";
    /**
     * Map type that displays a satellite image.
     *
     * @see #setMapType(String)
     * @see <a href="https://developers.google.com/maps/documentation/static-maps/intro#MapTypes">
     *     https://developers.google.com/maps/documentation/static-maps/intro#MapTypes</a>
     */
    public static final String MAP_TYPE_SATELLITE = "satellite";
    /**
     * Map type that displays a combination of the roadmap and satellite images.
     *
     * @see #setMapType(String)
     * @see <a href="https://developers.google.com/maps/documentation/static-maps/intro#MapTypes">
     *     https://developers.google.com/maps/documentation/static-maps/intro#MapTypes</a>
     */
    public static final String MAP_TYPE_HYBRID = "hybrid";
    /**
     * Map type that displays a physical relief map.
     *
     * @see #setMapType(String)
     * @see <a href="https://developers.google.com/maps/documentation/static-maps/intro#MapTypes">
     *     https://developers.google.com/maps/documentation/static-maps/intro#MapTypes</a>
     */
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

    /**
     * Creates a new StaticMapUriBuilder that will use the given API key for any requests it
     * creates.
     *
     * @param apiKey The API key to use for all {@link Uri}s built by this instance
     */
    public StaticMapUriBuilder(String apiKey) {
        uriBuilder = Uri.parse(STATIC_MAPS_URL).buildUpon();
        uriBuilder.appendQueryParameter("key", apiKey);
    }

    /**
     * Sets the size in pixels of the requested map image. This parameter is required.
     *
     * @param width The desired width in pixels of the map image
     * @param height The desired height in pixels of the map image
     * @return This builder object for method chaining
     */
    public StaticMapUriBuilder setSize(int width, int height) {
        uriBuilder.appendQueryParameter("size", String.format(Locale.US, SIZE_FORMAT, width, height));

        return this;
    }

    /**
     * Sets the zoom level of the requested map image. This parameter is required unless
     * {@link StaticMapUriBuilder#setPathPoints(List)} is set.
     *
     * @param zoom The desired zoom level of the map image
     * @return This builder object for method chaining
     */
    public StaticMapUriBuilder setZoom(int zoom) {
        uriBuilder.appendQueryParameter("zoom", ""+zoom);

        return this;
    }

    /**
     * Sets the geographical center of the requested map image. The center parameter is required
     * unless {@link StaticMapUriBuilder#setPathPoints(List)} is set.
     *
     * @param latLng A {@link LatLng} object representing the desired center of the map image
     * @return This builder object for method chaining
     *
     * @see StaticMapUriBuilder#setCenter(double, double)
     */
    public StaticMapUriBuilder setCenter(LatLng latLng) {
        return setCenter(latLng.latitude, latLng.longitude);
    }

    /**
     * Sets the geographical center of the requested map image. The center parameter is required
     * unless {@link StaticMapUriBuilder#setPathPoints(List)} is set.
     *
     * @param lat The desired latitude of the center of the map image
     * @param lng The desired longitude of the center of the map image
     * @return This builder object for method chaining
     *
     * @see StaticMapUriBuilder#setCenter(LatLng)
     */
    public StaticMapUriBuilder setCenter(double lat, double lng) {
        uriBuilder.appendQueryParameter("center", String.format(Locale.US, POINT_FORMAT, lat, lng));


        return this;
    }

    /**
     * Sets the graphics format of the map image. Defaults to 8-bit png.
     *
     * @param format The graphics format, must be one of "png", "png8", "png32", "gif", "jpg",
     *               "jpg-baseline"
     * @return This builder object for method chaining
     */
    public StaticMapUriBuilder setFormat(String format) {
        uriBuilder.appendQueryParameter("format", format);

        return this;
    }

    /**
     * Sets the map type of the requested map image. If omitted, this defaults to "roadmap".
     *
     * <p>
     * For more information on different map types, see
     * <a href="https://developers.google.com/maps/documentation/static-maps/intro#MapTypes">
     * https://developers.google.com/maps/documentation/static-maps/intro#MapTypes</a>.
     * </p>
     *
     * @param mapType The desired map type of the map image, must be one of
     *                {@link StaticMapUriBuilder#MAP_TYPE_ROADMAP},
     *                {@link StaticMapUriBuilder#MAP_TYPE_SATELLITE},
     *                {@link StaticMapUriBuilder#MAP_TYPE_TERRAIN}, or
     *                {@link StaticMapUriBuilder#MAP_TYPE_HYBRID}
     * @return This builder object for method chaining
     */
    public StaticMapUriBuilder setMapType(String mapType) {
        uriBuilder.appendQueryParameter("mapType", mapType);

        return this;
    }

    /**
     * Sets the weight of the polyline that will be drawn if
     * {@link StaticMapUriBuilder#setPathPoints(List)} is set. Defaults to 5.
     *
     * @param weight The thickness of the polyline in pixels
     * @return This builder object for method chaining
     *
     * @see StaticMapUriBuilder#setPathPoints(List)
     */
    public StaticMapUriBuilder setPathWeight(int weight) {
        this.pathWeight = weight;

        return this;
    }

    /**
     * Sets the color of the polyline that will be drawn if
     * {@link StaticMapUriBuilder#setPathPoints(List)} is set. Defaults to the standard blue color
     * used by Google Maps.
     *
     * @param color A string specifying either a color name of the set {"black", "brown", "green",
     *              "purple", "yellow", "blue", "gray", "orange", "red", "white"} or a 24-bit or
     *              32-bit hexadecimal color value (eg "0xFFFFFFFF")
     * @return This builder object for method chaining
     *
     * @see StaticMapUriBuilder#setPathPoints(List)
     */
    public StaticMapUriBuilder setPathColor(String color) {
        this.pathColor = color;

        return this;
    }

    /**
     * Sets a color to be used to fill the enclosed space of the polyline that will be drawn if
     * {@link StaticMapUriBuilder#setPathPoints(List)} is set. Defaults to no fill color.
     *
     * @param fillColor A string specifying either a color name of the set {"black", "brown", "green",
     *                  "purple", "yellow", "blue", "gray", "orange", "red", "white"} or a 24-bit or
     *                  32-bit hexadecimal color value (eg "0xFFFFFFFF")
     * @return This builder object for method chaining
     *
     * @see StaticMapUriBuilder#setPathPoints(List)
     */
    public StaticMapUriBuilder setPathFillColor(String fillColor) {
        this.pathFillColor = fillColor;

        return this;
    }

    /**
     * Defines a list of points to use to draw a polyline on the requested map image. If a path is
     * set, you do not need to set {@link StaticMapUriBuilder#setZoom(int)} or
     * {@link StaticMapUriBuilder#setCenter(LatLng)}, and the map image will zoom and pan to fit the
     * path as necessary.
     *
     * <p>
     * Note that there is a 8192 character limit on the length of Static Maps url requests, and
     * setting a excessively long path is an easy way to exceed this limit.
     * </p>
     *
     * @param points A {@link List} of {@link LatLng}s representing the points on the path to draw
     * @return This builder object for method chaining
     */
    public StaticMapUriBuilder setPathPoints(List<LatLng> points) {
        this.pathPoints = points;

        return this;
    }

    /**
     * Constructs a {@link Uri} pointing to the Static Maps API with the parameters configured in
     * this builder instance.
     *
     * @return A fully configured {@link Uri}
     */
    public Uri build() {
        constructPathString();
        return uriBuilder.build();
    }

    /**
     * Constructs a path string parameter using the current path properties set in this builder
     * instance and appends it to the internal {@link android.net.Uri.Builder}. This path string
     * conforms to the specification outlined by the
     * <a href="https://developers.google.com/maps/documentation/static-maps/intro#Paths">Static
     * Maps API</a>.
     *
     *
     */
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
