package com.isbx.androidtools.media;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.android.mms.exif.ExifInterface;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;

/**
 * A convenience class to resize an image to a new resolution while maintaining aspect ratio. Can be
 * configured to output up to three different target resolutions via an {@link ImageResizeConfig}
 * object.
 *
 * <p>
 * The resized images are saved as temporary files to the app's internal private storage. The
 * persistence of these files is not guaranteed, and they may be overwritten at any time by any of
 * the following actions:
 * </p>
 *
 * <ul>
 * <li>The ImageResizer is used to process several different images consecutively</li>
 * <li>Another ImageResizer is used to process an image</li>
 * <li>An explicit call to {@link ImageResizer#clearFiles()} is made</li>
 * </ul>
 *
 * <p>
 * For this reason, it is recommended to take whatever action is needed on the resized files
 * immediately after the scaling operation is completed, or copy them to a persistent location if
 * they are needed long-term.
 * </p>
 */
public class ImageResizer {

    private static final int MAX_FILES = 10; // TODO handle files more intelligently
    private static final String FILE_NAME_FORMAT = "image%d.jpg";

    private Context context;
    private ImageResizeConfig config;

    private int savedFiles = 0;

    /**
     * Creates a new ImageResizer that will use the given config to scale images.
     *
     * @param context The {@link Context} to use for reading/writing the image files
     * @param config A {@link ImageResizeConfig} object specifying how this ImageResizer should
     *               process images
     *
     * @see ImageResizeConfig
     */
    public ImageResizer(Context context, ImageResizeConfig config) {
        this.context = context;
        this.config = config;
    }

    /**
     * Creates a new ImageResizer with a default {@link ImageResizeConfig}.
     *
     * @param context The {@link Context} to use for reading/writing the image files
     *
     * @see ImageResizeConfig
     */
    public ImageResizer(Context context) {
        this(context, new ImageResizeConfig());
    }

    /**
     * Creates scaled copies of the given image according to the settings of this ImageResizer's
     * {@link ImageResizeConfig} object.
     *
     * <p>
     * A Uri for each resulting scaled image will be passed to {@code callback} once all scaling
     * operations are complete. A null value will be returned for any scaling sizes that have been
     * disabled by the resize configuration or that fail during processing.
     * </p>
     *
     * @param sourceUri The {@link Uri} of the image to be resized
     * @param callback An {@link ImageResizeCallback} that will be called once the scaling is
     *                 complete
     */
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

    /**
     * Creates a scaled copy of the given image according to the large output settings in this
     * ImageResizer's {@link ImageResizeConfig}. Note the copy will be created even if large output
     * is disabled in the configuration.
     *
     * @param sourceUri The {@link Uri} of the image to be resized
     * @return A {@link Uri} pointing to the scaled image copy, or {@code null} if the operation
     * failed
     *
     * @see ImageResizeConfig#getLargeDimension()
     * @see ImageResizer#scaleImage(Uri, ImageResizeConfig.Dimension)
     */
    public Uri createLargeImage(Uri sourceUri) {
        return scaleImage(sourceUri, config.getLargeDimension());
    }

    /**
     * Creates a scaled copy of the given image according to the medium output settings in this
     * ImageResizer's {@link ImageResizeConfig}. Note the copy will be created even if medium output
     * is disabled in the configuration.
     *
     * @param sourceUri The {@link Uri} of the image to be resized
     * @return A {@link Uri} pointing to the scaled image copy, or {@code null} if the operation
     * failed
     *
     * @see ImageResizeConfig#getMediumDimension()
     * @see ImageResizer#scaleImage(Uri, ImageResizeConfig.Dimension)
     */
    public Uri createMediumImage(Uri sourceUri) {
        return scaleImage(sourceUri, config.getMediumDimension());
    }

    /**
     * Creates a scaled copy of the given image according to the small output settings in this
     * ImageResizer's {@link ImageResizeConfig}. Note the copy will be created even if small output
     * is disabled in the configuration.
     *
     * @param sourceUri The {@link Uri} of the image to be resized
     * @return A {@link Uri} pointing to the scaled image copy, or {@code null} if the operation
     * failed
     *
     * @see ImageResizeConfig#getSmallDimension()
     * @see ImageResizer#scaleImage(Uri, ImageResizeConfig.Dimension)
     */
    public Uri createSmallImage(Uri sourceUri) {
        return scaleImage(sourceUri, config.getSmallDimension());
    }

    /**
     * Creates a copy of the given image scaled to the size specified by {@code targetDimension}.
     *
     * <p>
     * This transformation maintains the aspect ratio of the source image. If the aspect ratio of
     * {@code targetDimension} is not equal to the aspect ratio of the source image, the scaled
     * image will be made as large as possible without exceeding the dimensions of
     * {@code targetDimension}.
     * </p>
     *
     * @param sourceUri The {@link Uri} of the image to be resized
     * @param targetDimension The desired dimensions of the copied image
     * @return A {@link Uri} pointing to the scaled image copy, or {@code null} if the operation
     * failed
     */
    public Uri scaleImage(Uri sourceUri, ImageResizeConfig.Dimension targetDimension) {
        Uri dstUri = null;

        Bitmap bm = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(context.getContentResolver().openInputStream(sourceUri), null, options);

            options.inSampleSize = calculateInSampleSize(options, targetDimension.getWidth(), targetDimension.getHeight());
            options.inJustDecodeBounds = false;

            bm = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(sourceUri), null, options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (bm != null) {
            Bitmap out = scaleBitmap(bm, targetDimension);
            if (bm != out) {
                // output maybe the same bitmap if scaling wasn't needed, otherwise we can recycle it
                bm.recycle();
            }

            OutputStream os = null;
            try {
                if (savedFiles >= MAX_FILES) {
                    savedFiles = 0;
                }
                String fileName = String.format(Locale.US, FILE_NAME_FORMAT, savedFiles++);
                os = context.openFileOutput(fileName, Context.MODE_PRIVATE);

                ExifInterface exif = new ExifInterface();
                exif.readExif(context.getContentResolver().openInputStream(sourceUri));

                if (exif.getAllTags() != null) {
                    exif.writeExif(out, os);
                } else {
                    ExifInterface exif2 = new ExifInterface();
                    exif2.addDateTimeStampTag(exif.TAG_DATE_TIME_ORIGINAL, Calendar.getInstance().getTime().getTime(), TimeZone.getDefault());
                    exif2.writeExif(out, os);
                }

                dstUri = Uri.fromFile(context.getFileStreamPath(fileName));
            } catch (IOException e) {
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

    /**
     * For a bitmap whose dimensions are represented by {@code options}, calculates the largest
     * sample size that will result in a sampled bitmap whose dimensions will be equal to or
     * greater than {@code reqWidth} and {@code reqHeight}.
     *
     * <p>See <a href="https://developer.android.com/training/displaying-bitmaps/load-bitmap.html">
     * https://developer.android.com/training/displaying-bitmaps/load-bitmap.html</a></p>
     *
     * @param options A {@link android.graphics.BitmapFactory.Options} object containing the bounds
     *                of a bitmap
     * @param reqWidth The desired width of the bitmap to be created using the resulting sample size
     * @param reqHeight The desired height of the bitmap to be created using the resulting sample
     *                  size
     * @return A power-of-2 sample size that will approximately yield the requested dimensions.
     */
    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /**
     * Creates a copy of the given bitmap scaled to the size specified by {@code targetDimension}.
     *
     * <p>
     * This transformation maintains the aspect ratio of the source image. If the aspect ratio of
     * {@code targetDimension} is not equal to the aspect ratio of the source image, the scaled
     * bitmap will be made as large as possible without exceeding the dimensions of
     * {@code targetDimension}.
     * </p>
     *
     * @param source The {@link Bitmap} to be resized
     * @param targetDimension The desired dimensions of the copied bitmap
     * @return The scaled {@link Bitmap} object
     */
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

    /**
     * Deletes any temporary files that may have been created by previous resize operations. This
     * will clear temporary files created by <strong>all</strong> ImageResizer instances, not just
     * the current one.
     */
    public void clearFiles() {
        for (int i = 0; i < MAX_FILES; i++) {
            String fileName = String.format(Locale.US, FILE_NAME_FORMAT, i);
            context.deleteFile(fileName);
        }
        savedFiles = 0;
    }

    /**
     * Callback interface for asynchronous resize operations.
     *
     * @see ImageResizer#resizeImage(Uri, ImageResizeCallback)
     */
    public interface ImageResizeCallback {
        /**
         * This method will be invoked when an asynchronous resize operation is completed.
         *
         * @param largeUri A {@link Uri} pointing to the large image copy
         * @param mediumUri A {@link Uri} pointing to the medium image copy
         * @param smallUri A {@link Uri} pointing to the small image copy
         */
        void onResizeComplete(Uri largeUri, Uri mediumUri, Uri smallUri);
    }
}
