package com.isbx.androidtools.utils;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by alexs_000 on 3/29/2016.
 */
public class ParcelableUtils {

    public static void writeNullableParcelable(Parcelable parcelable, Parcel dest, int flags) {
        if (parcelable != null) {
            dest.writeByte((byte) 1);
            dest.writeParcelable(parcelable, flags);
        } else {
            dest.writeByte((byte) 0);
        }
    }

    public static <T extends Parcelable> T readNullableParcelable(Parcel in, ClassLoader loader) {
        if (in.readByte() != 0) {
            return in.readParcelable(loader);
        }

        return null;
    }

    public static void writeNullableJSONObject(JSONObject jsonObject, Parcel dest) {
        if (jsonObject != null) {
            dest.writeByte((byte) 1);
            dest.writeString(jsonObject.toString());
        } else {
            dest.writeByte((byte) 0);
        }
    }

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
