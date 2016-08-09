package com.isbx.androidtools.utils;

import android.content.Context;
import android.graphics.Typeface;

import java.util.HashMap;

/**
 * Created by alexs_000 on 7/26/2016.
 */
public class FontCache {
    private static final HashMap<String, Typeface> fonts = new HashMap<>();

    public static Typeface getFont(Context context, String assetPath) {
        Typeface font = fonts.get(assetPath);
        if (font == null) {
            font = Typeface.createFromAsset(context.getAssets(), assetPath);
            fonts.put(assetPath, font);
        }

        return font;
    }
}
