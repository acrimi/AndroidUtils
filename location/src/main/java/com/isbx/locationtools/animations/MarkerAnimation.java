package com.isbx.locationtools.animations;

/* Copyright 2013 Google Inc.
   Licensed under Apache 2.0: http://www.apache.org/licenses/LICENSE-2.0.html
   https://gist.github.com/broady/6314689*/

import android.animation.FloatEvaluator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.view.animation.LinearInterpolator;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class MarkerAnimation {
    public static void animateMarker(final Marker marker, final LatLng finalPosition, final float finalHeading, final LatLngInterpolator latLngInterpolator, long duration) {
        final TypeEvaluator<LatLng> typeEvaluator = new TypeEvaluator<LatLng>() {
            @Override
            public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
                return latLngInterpolator.interpolate(fraction, startValue, endValue);
            }
        };

        final FloatEvaluator floatEvaluator = new FloatEvaluator();
        final LatLng startPosition = marker.getPosition();
        final Float startRotation = marker.getRotation();

        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
        animator.setDuration(duration);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                marker.setPosition(typeEvaluator.evaluate(valueAnimator.getAnimatedFraction(), startPosition, finalPosition));
                marker.setRotation(floatEvaluator.evaluate(valueAnimator.getAnimatedFraction(), startRotation, finalHeading));
            }
        });
        animator.start();
    }
}
