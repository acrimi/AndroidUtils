package com.isbx.locationtools.animations;

import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

/**
 * <p>
 * An implementation of {@link ValueAnimator} to animate both the position and rotation of a Google
 * Maps {@link Marker}. These values can be retrieved during the animation by their respective
 * property names:
 * </p>
 *
 * <pre>
 * <code>markerAnimator.getAnimatedValue("position");
 * markerAnimator.getAnimatedValue("rotation");</code>
 * </pre>
 */
public class MarkerAnimator extends ValueAnimator implements ValueAnimator.AnimatorUpdateListener {

    private Marker marker;
    private PropertyValuesHolder positionHolder;

    /**
     * Creates a new MarkerAnimator that will animate both the position and rotation of the given
     * marker.
     *
     * @param marker The {@link Marker} to animate
     * @param endPosition The final {@link LatLng} position of the marker
     * @param endRotation The final rotation of the marker in degrees
     *
     * @see Marker#setPosition(LatLng)
     * @see Marker#setRotation(float)
     */
    public MarkerAnimator(Marker marker, LatLng endPosition, float endRotation) {
        this.marker = marker;
        positionHolder = PropertyValuesHolder.ofObject("position", new LatLngEvaluator.LinearFixed(), marker.getPosition(), endPosition);
        PropertyValuesHolder rotationHolder = PropertyValuesHolder.ofObject("rotation", new DegreeEvaluator(), marker.getRotation(), endRotation);
        setValues(positionHolder, rotationHolder);

        addUpdateListener(this);
    }

    /**
     * Creates a new MarkerAnimator that will animate the position of the given marker.
     *
     * @param marker The {@link Marker} to animate
     * @param endPosition The final {@link LatLng} position of the marker
     *
     * @see Marker#setPosition(LatLng)
     */
    public MarkerAnimator(Marker marker, LatLng endPosition) {
        this(marker, endPosition, marker.getRotation());
    }

    /**
     * Creates a new MarkerAnimator that will animate the rotation of the given marker.
     *
     * @param marker The {@link Marker} to animate
     * @param endRotation The final rotation of the marker in degrees
     *
     * @see Marker#setRotation(float)
     */
    public MarkerAnimator(Marker marker, float endRotation) {
        this(marker, marker.getPosition(), endRotation);
    }

    /**
     * Sets the {@link LatLngEvaluator} this animation will use for animating the position of
     * the marker. Defaults to {@link LatLngEvaluator.LinearFixed}.
     *
     * @param latLngEvaluator A {@link LatLngEvaluator} to use for position animation
     *
     * @see LatLngEvaluator
     */
    public void setLatLngEvaluator(LatLngEvaluator latLngEvaluator) {
        positionHolder.setEvaluator(latLngEvaluator);
    }

    /**
     * Invoked on each frame of animation. Gets the most recent animated values of the rotation and
     * position properties and sets them to the {@link Marker} instance associated with this
     * animator.
     *
     * @param valueAnimator This MarkerAnimator
     */
    @Override
    public void onAnimationUpdate(ValueAnimator valueAnimator) {
        marker.setPosition((LatLng) valueAnimator.getAnimatedValue("position"));
        marker.setRotation((float) valueAnimator.getAnimatedValue("rotation"));
    }
}
