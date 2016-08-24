package com.isbx.locationtools.animations;

/* Copyright 2013 Google Inc.
   Licensed under Apache 2.0: http://www.apache.org/licenses/LICENSE-2.0.html */


import com.google.android.gms.maps.model.LatLng;

import static java.lang.Math.asin;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;

/**
 * A utility interface to interpolate between two {@link LatLng} values. Implementations taken from
 * <a href="https://gist.github.com/broady/6314689">https://gist.github.com/broady/6314689</a>
 */
public interface LatLngInterpolator {
    /**
     * Calculates the distance between two coordinates, and returns a {@link LatLng} that lies at
     * {@code fraction} of that distance.
     *
     * @param fraction The fraction of the distance to find between {@code a} and {@code b}
     * @param a The first {@link LatLng} to interpolate between
     * @param b The second {@link LatLng} to interpolate between
     * @return The interpolated {@link LatLng} between {@code a} and {@code b}
     */
    LatLng interpolate(float fraction, LatLng a, LatLng b);

    /**
     * An implementation of {@link LatLngInterpolator} that linearly interpolates between two
     * {@link LatLng} coordinate sets. This does not take into account the curvature of the earth
     * or the fact that longitude values wrap from 180 to 0. In other words, an interpolation
     * between a longitude of 0 and a longitude of 180 will result in a longitude of 90.
     *
     * @see LinearFixed
     */
    class Linear implements LatLngInterpolator {
        @Override
        public LatLng interpolate(float fraction, LatLng a, LatLng b) {
            double lat = (b.latitude - a.latitude) * fraction + a.latitude;
            double lng = (b.longitude - a.longitude) * fraction + a.longitude;
            return new LatLng(lat, lng);
        }
    }

    /**
     * An implementation of {@link LatLngInterpolator} that linearly interpolates between two
     * {@link LatLng} coordinate sets. Unlike, {@link Linear}, this implementation takes into
     * account the circular nature of longitude coordinates and will find the shortest path across
     * the 180th meridian. In other words, an interpolation between a longitude of 0 and a longitude
     * of 180 will result in the correct longitude of 0/180. This does not take into account the
     * curvature of the earth.
     *
     * @see Linear
     */
    class LinearFixed implements LatLngInterpolator {
        @Override
        public LatLng interpolate(float fraction, LatLng a, LatLng b) {
            double lat = (b.latitude - a.latitude) * fraction + a.latitude;
            double lngDelta = b.longitude - a.longitude;

            // Take the shortest path across the 180th meridian.
            if (Math.abs(lngDelta) > 180) {
                lngDelta -= Math.signum(lngDelta) * 360;
            }
            double lng = lngDelta * fraction + a.longitude;
            return new LatLng(lat, lng);
        }
    }

    /**
     * An implementation of {@link LatLngInterpolator} that interpolates between two {@link LatLng}
     * coordinate sets while accounting for the spherical shape of the earth. This will provide
     * more accurate results than {@link Linear} or {@link LinearFixed}.
     */
    class Spherical implements LatLngInterpolator {

        /* From github.com/googlemaps/android-maps-utils */
        @Override
        public LatLng interpolate(float fraction, LatLng from, LatLng to) {
            // http://en.wikipedia.org/wiki/Slerp
            double fromLat = toRadians(from.latitude);
            double fromLng = toRadians(from.longitude);
            double toLat = toRadians(to.latitude);
            double toLng = toRadians(to.longitude);
            double cosFromLat = cos(fromLat);
            double cosToLat = cos(toLat);

            // Computes Spherical interpolation coefficients.
            double angle = computeAngleBetween(fromLat, fromLng, toLat, toLng);
            double sinAngle = sin(angle);
            if (sinAngle < 1E-6) {
                return from;
            }
            double a = sin((1 - fraction) * angle) / sinAngle;
            double b = sin(fraction * angle) / sinAngle;

            // Converts from polar to vector and interpolate.
            double x = a * cosFromLat * cos(fromLng) + b * cosToLat * cos(toLng);
            double y = a * cosFromLat * sin(fromLng) + b * cosToLat * sin(toLng);
            double z = a * sin(fromLat) + b * sin(toLat);

            // Converts interpolated vector back to polar.
            double lat = atan2(z, sqrt(x * x + y * y));
            double lng = atan2(y, x);
            return new LatLng(toDegrees(lat), toDegrees(lng));
        }

        private double computeAngleBetween(double fromLat, double fromLng, double toLat, double toLng) {
            // Haversine's formula
            double dLat = fromLat - toLat;
            double dLng = fromLng - toLng;
            return 2 * asin(sqrt(pow(sin(dLat / 2), 2) +
                    cos(fromLat) * cos(toLat) * pow(sin(dLng / 2), 2)));
        }
    }
}
