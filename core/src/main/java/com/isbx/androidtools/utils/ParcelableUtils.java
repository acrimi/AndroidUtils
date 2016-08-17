package com.isbx.androidtools.utils;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A utility class to bring expanded functionality to Android's {@link Parcel} library
 */
public class ParcelableUtils {

    /**
     * @deprecated Use {@link Parcel#writeValue(Object)} instead
     *
     * @param parcelable The parcelable object to be written
     * @param dest The parcel to write to
     * @param flags Contextual flags as per {@link Parcelable#writeToParcel(Parcel, int) Parcelable.writeToParcel()}
     */
    public static void writeNullableParcelable(@Nullable Parcelable parcelable, Parcel dest, int flags) {
        if (parcelable != null) {
            dest.writeByte((byte) 1);
            dest.writeParcelable(parcelable, flags);
        } else {
            dest.writeByte((byte) 0);
        }
    }

    /**
     * @deprecated Use {@link Parcel#readValue(ClassLoader)} instead
     *
     * @param in The parcel to read from
     * @param loader A ClassLoader from which to instantiate the Parcelable
     *               object, or null for the default class loader.
     * @param <T> The type of {@link Parcelable} expected
     * @return Returns the newly created {@link Parcelable}, or null if a null
     * object has been written.
     */
    @Nullable
    public static <T extends Parcelable> T readNullableParcelable(Parcel in, ClassLoader loader) {
        if (in.readByte() != 0) {
            return in.readParcelable(loader);
        }

        return null;
    }

    /**
     * Writes a {@link JSONObject} to the given {@link Parcel}. Internally, this method serializes
     * the JSON object to a string and writes that to the parcel.
     *
     * @param jsonObject The {@link JSONObject} to write to the parcel. Can be null.
     * @param dest The {@link Parcel} to write the JSON object to
     *
     * @see ParcelableUtils#readNullableJSONObject(Parcel)
     */
    public static void writeNullableJSONObject(@Nullable JSONObject jsonObject, Parcel dest) {
        if (jsonObject != null) {
            dest.writeByte((byte) 1);
            dest.writeString(jsonObject.toString());
        } else {
            dest.writeByte((byte) 0);
        }
    }

    /**
     * Attempts to read a {@link JSONObject} from the given {@link Parcel} at its current data
     * position.
     *
     * @param in The {@link Parcel} to read the JSON object from
     * @return The {@link JSONObject} at the parcel's current data position, or {@code null} if the
     *         parcel doesn't contain valid JSON data or if the JSON object was null.
     */
    @Nullable
    public static JSONObject readNullableJSONObject(Parcel in) {
        if (in.readByte() != 0) {
            try {
                return new JSONObject(in.readString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}
