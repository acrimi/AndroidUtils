package com.isbx.locationtools.animations;

import android.animation.TypeEvaluator;

/**
 * An implementation of {@link TypeEvaluator} that interpolates between arc degree values across
 * the shortest arc between them.
 */
public class DegreeEvaluator implements TypeEvaluator<Float> {

    /**
     * This function returns the result of linearly interpolating the start and end values, with
     * fraction representing the proportion between the start and end values. The result will
     * always be a value along the shortest arc between the two values.
     *
     * @param fraction The fraction from the starting to the ending values
     * @param startValue The start value
     * @param endValue The end value
     * @return A linear interpolation between the start and end values, given the fraction parameter
     */
    @Override
    public Float evaluate(float fraction, Float startValue, Float endValue) {
        // Animate along shortest arc
        float delta = endValue - startValue;
        if (Math.abs(delta) > 180) {
            delta -= Math.signum(delta) * 360;
        }
        return startValue + delta*fraction;
    }
}
