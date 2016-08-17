package com.isbx.androidtools.utils;

import android.content.Context;
import android.graphics.Typeface;

import java.util.HashMap;

/**
 * A simple cache implementation to reuse {@link Typeface} objects across an application instance.
 */
public class FontCache {
    private static final HashMap<String, Typeface> fonts = new HashMap<>();

    /**
     * Retrieves a font from the cache, optionally creating it if it hasn't already been loaded.
     *
     * @param context A {@link Context} object to use to create the font if necessary
     * @param assetPath The path of the font file relative to the app's assets directory
     * @return A {@link Typeface} instance representing the specified font
     */
    public static Typeface getFont(Context context, String assetPath) {
        Typeface font = fonts.get(assetPath);
        if (font == null) {
            font = Typeface.createFromAsset(context.getAssets(), assetPath);
            fonts.put(assetPath, font);
        }

        return font;
    }
}
