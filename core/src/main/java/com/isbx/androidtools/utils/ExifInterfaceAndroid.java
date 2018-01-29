package com.isbx.androidtools.utils;

import android.media.ExifInterface;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by rwieghard on 1/29/18.
 */

public class ExifInterfaceAndroid extends ExifInterface {
    public ExifInterfaceAndroid(String filename) throws IOException {
        super(filename);
    }
}
