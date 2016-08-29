package com.isbx.locationtools.animations;

import android.view.animation.Animation;
import android.view.animation.Transformation;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

/**
 * An implementation of {@link Animation} to animate both the position and rotation of a Google
 * Maps {@link Marker}.
 */
public class MarkerAnimation extends Animation {

    private Marker marker;
    private LatLng startPosition;
    private LatLng endPosition;
    private float startRotation;
    private float endRotation;

    private LatLngInterpolator latLngInterpolator;

    /**
     * Creates a new MarkerAnimation that will animate both the position and rotation of the given
     * marker.
     *
     * @param marker The {@link Marker} to animate
     * @param endPosition The final {@link LatLng} position of the marker
     * @param endRotation The final rotation of the marker in degrees
     *
     * @see Marker#setPosition(LatLng)
     * @see Marker#setRotation(float)
     */
    public MarkerAnimation(Marker marker, LatLng endPosition, float endRotation) {
        this.marker = marker;
        this.startPosition = marker.getPosition();
        this.startRotation = marker.getRotation();
        this.endPosition = endPosition;
        this.endRotation = endRotation;

        latLngInterpolator = new LatLngInterpolator.LinearFixed();
    }

    /**
     * Creates a new MarkerAnimation that will animate the position of the given marker.
     *
     * @param marker The {@link Marker} to animate
     * @param endPosition The final {@link LatLng} position of the marker
     *
     * @see Marker#setPosition(LatLng)
     */
    public MarkerAnimation(Marker marker, LatLng endPosition) {
        this(marker, endPosition, marker.getRotation());
    }

    /**
     * Creates a new MarkerAnimation that will animate the rotation of the given marker.
     *
     * @param marker The {@link Marker} to animate
     * @param endRotation The final rotation of the marker in degrees
     *
     * @see Marker#setRotation(float)
     */
    public MarkerAnimation(Marker marker, float endRotation) {
        this(marker, marker.getPosition(), endRotation);
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        if (startPosition != endPosition) {
            marker.setPosition(latLngInterpolator.interpolate(interpolatedTime, startPosition, endPosition));
        }

        // Animate along shortest arc
        float delta = endRotation - startRotation;
        if (Math.abs(delta) > 180) {
            delta -= Math.signum(delta) * 360;
        }
        marker.setRotation(startRotation + delta*interpolatedTime);
    }

    /**
     * Sets the {@link LatLngInterpolator} this animation will use for animating the position of
     * the marker. Defaults to {@link com.isbx.locationtools.animations.LatLngInterpolator.LinearFixed}.
     *
     * @param latLngInterpolator A {@link LatLngInterpolator} to use for position animation
     *
     * @see LatLngInterpolator
     */
    public void setLatLngInterpolator(LatLngInterpolator latLngInterpolator) {
        this.latLngInterpolator = latLngInterpolator;
    }
}
