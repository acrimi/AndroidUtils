package com.isbx.androidtools.media;

/**
 * Created by alexs_000 on 5/16/2016.
 */
public class ImageResizeConfig {

    private static final int DEFAULT_LARGE_SIZE = 1024;
    private static final int DEFAULT_MEDIUM_SIZE = 512;
    private static final int DEFAULT_SMALL_SIZE = 256;

    private final Dimension largeDimension = new Dimension(DEFAULT_LARGE_SIZE, DEFAULT_LARGE_SIZE);
    private final Dimension mediumDimension = new Dimension(DEFAULT_MEDIUM_SIZE, DEFAULT_MEDIUM_SIZE);
    private final Dimension smallDimension = new Dimension(DEFAULT_SMALL_SIZE, DEFAULT_SMALL_SIZE);

    private boolean largeOutputEnabled = true;
    private boolean mediumOutputEnabled = true;
    private boolean smallOutputEnabled = true;

    public boolean isLargeOutputEnabled() {
        return largeOutputEnabled;
    }

    public void setLargeOutputEnabled(boolean largeOutputEnabled) {
        this.largeOutputEnabled = largeOutputEnabled;
    }

    public boolean isMediumOutputEnabled() {
        return mediumOutputEnabled;
    }

    public void setMediumOutputEnabled(boolean mediumOutputEnabled) {
        this.mediumOutputEnabled = mediumOutputEnabled;
    }

    public boolean isSmallOutputEnabled() {
        return smallOutputEnabled;
    }

    public void setSmallOutputEnabled(boolean smallOutputEnabled) {
        this.smallOutputEnabled = smallOutputEnabled;
    }

    public Dimension getLargeDimension() {
        return largeDimension;
    }

    public ImageResizeConfig setLargeDimensions(Dimension dimension) {
        largeDimension.height = dimension.height;
        largeDimension.width = dimension.width;
        return this;
    }

    public ImageResizeConfig setLargeDimensions(int width, int height) {
        largeDimension.height = height;
        largeDimension.width = width;
        return this;
    }

    public Dimension getMediumDimension() {
        return mediumDimension;
    }

    public ImageResizeConfig setMediumDimensions(Dimension dimension) {
        mediumDimension.height = dimension.height;
        mediumDimension.width = dimension.width;
        return this;
    }

    public ImageResizeConfig setMediumDimensions(int width, int height) {
        mediumDimension.height = height;
        mediumDimension.width = width;
        return this;
    }

    public Dimension getSmallDimension() {
        return smallDimension;
    }

    public ImageResizeConfig setSmallDimensions(Dimension dimension) {
        smallDimension.height = dimension.height;
        smallDimension.width = dimension.width;
        return this;
    }
    public ImageResizeConfig setSmallDimensions(int width, int height) {
        smallDimension.height = height;
        smallDimension.width = width;
        return this;
    }


    public static class Dimension {
        private int width;
        private int height;

        public Dimension(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }
}
