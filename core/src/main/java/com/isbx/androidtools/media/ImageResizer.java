package com.isbx.androidtools.media;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;

/**
 * Created by alexs_000 on 5/16/2016.
 */
public class ImageResizer {

    private static final int MAX_FILES = 10; // TODO handle files more intelligently
    private static final String FILE_NAME_FORMAT = "image%d.jpg";

    private Context context;
    private ImageResizeConfig config;

    private int savedFiles = 0;

    public ImageResizer(Context context, ImageResizeConfig config) {
        this.context = context;
        this.config = config;
    }

    public ImageResizer(Context context) {
        this(context, new ImageResizeConfig());
    }

    public void resizeImage(final Uri sourceUri, final ImageResizeCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Uri largeUri = null;
                Uri mediumUri = null;
                Uri smallUri = null;

                if (config.isLargeOutputEnabled()) {
                    largeUri = createLargeImage(sourceUri);
                }
                if (config.isMediumOutputEnabled()) {
                    mediumUri = createMediumImage(sourceUri);
                }
                if (config.isSmallOutputEnabled()) {
                    smallUri = createSmallImage(sourceUri);
                }

                callback.onResizeComplete(largeUri, mediumUri, smallUri);
            }
        }).start();
    }

    public Uri createLargeImage(Uri sourceUri) {
        return scaleImage(sourceUri, config.getLargeDimension());
    }

    public Uri createMediumImage(Uri sourceUri) {
        return scaleImage(sourceUri, config.getMediumDimension());
    }

    public Uri createSmallImage(Uri sourceUri) {
        return scaleImage(sourceUri, config.getSmallDimension());
    }

    public Uri scaleImage(Uri sourceUri, ImageResizeConfig.Dimension targetDimension) {
        Uri dstUri = null;

        Bitmap bm = null;
        try {
            bm = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(sourceUri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (bm != null) {
            Bitmap out = scaleBitmap(bm, targetDimension);
            bm.recycle();

            OutputStream os = null;
            try {
                if (savedFiles >= MAX_FILES) {
                    savedFiles = 0;
                }
                String fileName = String.format(Locale.US, FILE_NAME_FORMAT, savedFiles++);
                os = context.openFileOutput(fileName, Context.MODE_PRIVATE);
                out.compress(Bitmap.CompressFormat.JPEG, 100, os);
                dstUri = Uri.fromFile(context.getFileStreamPath(fileName));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            out.recycle();
        }

        return dstUri;
    }

    public static Bitmap scaleBitmap(Bitmap source, ImageResizeConfig.Dimension targetDimension) {
        float targetRatio = targetDimension.getWidth() / (float) targetDimension.getHeight();
        float srcRatio = source.getWidth() / (float) source.getHeight();

        int width;
        int height;
        if (targetRatio > srcRatio) {
            height = targetDimension.getHeight();
            width = (int) (height * srcRatio);
        } else {
            width = targetDimension.getWidth();
            height = (int) (width / srcRatio);
        }

        return Bitmap.createScaledBitmap(source, width, height, false);
    }

    public void clearFiles() {
        for (int i = 0; i < MAX_FILES; i++) {
            String fileName = String.format(Locale.US, FILE_NAME_FORMAT, i);
            context.deleteFile(fileName);
        }
        savedFiles = 0;
    }

    public interface ImageResizeCallback {
        public void onResizeComplete(Uri largeUri, Uri mediumUri, Uri smallUri);
    }
}
