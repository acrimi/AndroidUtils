package com.isbx.androidtools.media;

/**
 * A configuration class for how an image should be resized by {@link ImageResizer}. The default
 * configuration requests three output sizes with the following dimensions:
 *
 * <ul>
 * <li>small - 256x256</li>
 * <li>medium - 512x512</li>
 * <li>large - 1024x1024</li>
 * </ul>
 *
 * @see ImageResizer
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

    /**
     * Returns whether the large output size is requested by this configuration.
     *
     * @return {@code true} if large output is enabled, {@code false} otherwise
     *
     * @see ImageResizeConfig#setLargeOutputEnabled(boolean)
     */
    public boolean isLargeOutputEnabled() {
        return largeOutputEnabled;
    }

    /**
     * Specifies whether or not the large output size is requested by this configuration.
     *
     * @param largeOutputEnabled {@code true} if large output is enabled, {@code false} otherwise
     *
     * @see ImageResizeConfig#isLargeOutputEnabled()
     */
    public void setLargeOutputEnabled(boolean largeOutputEnabled) {
        this.largeOutputEnabled = largeOutputEnabled;
    }

    /**
     * Returns whether the medium output size is requested by this configuration.
     *
     * @return {@code true} if medium output is enabled, {@code false} otherwise
     *
     * @see ImageResizeConfig#setMediumOutputEnabled(boolean)
     */
    public boolean isMediumOutputEnabled() {
        return mediumOutputEnabled;
    }

    /**
     * Specifies whether or not the medium output size is requested by this configuration.
     *
     * @param mediumOutputEnabled {@code true} if medium output is enabled, {@code false} otherwise
     *
     * @see ImageResizeConfig#isMediumOutputEnabled()
     */
    public void setMediumOutputEnabled(boolean mediumOutputEnabled) {
        this.mediumOutputEnabled = mediumOutputEnabled;
    }

    /**
     * Returns whether the small output size is requested by this configuration.
     *
     * @return {@code true} if small output is enabled, {@code false} otherwise
     *
     * @see ImageResizeConfig#setSmallOutputEnabled(boolean)
     */
    public boolean isSmallOutputEnabled() {
        return smallOutputEnabled;
    }

    /**
     * Specifies whether or not the small output size is requested by this configuration.
     *
     * @param smallOutputEnabled {@code true} if small output is enabled, {@code false} otherwise
     *
     * @see ImageResizeConfig#isSmallOutputEnabled()
     */
    public void setSmallOutputEnabled(boolean smallOutputEnabled) {
        this.smallOutputEnabled = smallOutputEnabled;
    }

    /**
     * Returns the pixel dimensions specified for the large output size.
     *
     * @return A {@link Dimension} object containing the large output dimensions
     *
     * @see ImageResizeConfig#setLargeDimensions(Dimension)
     * @see ImageResizeConfig#setLargeDimensions(int, int)
     * @see Dimension
     */
    public Dimension getLargeDimension() {
        return largeDimension;
    }

    /**
     * Sets the pixel dimensions for the large output size.
     * <p>
     * ImageResizerConfig does not hold a reference to the {@code dimension} parameter object.
     * Altering {@code dimension} after invoking this method will not affect the configured size.
     * </p>
     *
     * @param dimension A {@link Dimension} object representing the size to use for large output
     * @return This ImageResizerConfig object to allow for method chaining
     *
     * @see ImageResizeConfig#getLargeDimension()
     * @see ImageResizeConfig#setLargeDimensions(int, int)
     * @see Dimension
     */
    public ImageResizeConfig setLargeDimensions(Dimension dimension) {
        largeDimension.height = dimension.height;
        largeDimension.width = dimension.width;
        return this;
    }

    /**
     * Sets the pixel dimensions for the large output size.
     *
     * @param width The desired width in pixels of the large output image
     * @param height The desired height in pixels of the large output image
     * @return This ImageResizerConfig object to allow for method chaining
     *
     * @see ImageResizeConfig#getLargeDimension()
     * @see ImageResizeConfig#setLargeDimensions(Dimension)
     */
    public ImageResizeConfig setLargeDimensions(int width, int height) {
        largeDimension.height = height;
        largeDimension.width = width;
        return this;
    }

    /**
     * Returns the pixel dimensions specified for the medium output size.
     *
     * @return A {@link Dimension} object containing the medium output dimensions
     *
     * @see ImageResizeConfig#setMediumDimensions(Dimension)
     * @see ImageResizeConfig#setMediumDimensions(int, int)
     * @see Dimension
     */
    public Dimension getMediumDimension() {
        return mediumDimension;
    }

    /**
     * Sets the pixel dimensions for the medium output size.
     * <p>
     * ImageResizerConfig does not hold a reference to the {@code dimension} parameter object.
     * Altering {@code dimension} after invoking this method will not affect the configured size.
     * </p>
     *
     * @param dimension A {@link Dimension} object representing the size to use for medium output
     * @return This ImageResizerConfig object to allow for method chaining
     *
     * @see ImageResizeConfig#getMediumDimension()
     * @see ImageResizeConfig#setMediumDimensions(int, int)
     * @see Dimension
     */
    public ImageResizeConfig setMediumDimensions(Dimension dimension) {
        mediumDimension.height = dimension.height;
        mediumDimension.width = dimension.width;
        return this;
    }

    /**
     * Sets the pixel dimensions for the medium output size.
     *
     * @param width The desired width in pixels of the medium output image
     * @param height The desired height in pixels of the medium output image
     * @return This ImageResizerConfig object to allow for method chaining
     *
     * @see ImageResizeConfig#getMediumDimension()
     * @see ImageResizeConfig#setMediumDimensions(Dimension)
     */
    public ImageResizeConfig setMediumDimensions(int width, int height) {
        mediumDimension.height = height;
        mediumDimension.width = width;
        return this;
    }

    /**
     * Returns the pixel dimensions specified for the small output size.
     *
     * @return A {@link Dimension} object containing the small output dimensions
     *
     * @see ImageResizeConfig#setSmallDimensions(Dimension)
     * @see ImageResizeConfig#setSmallDimensions(int, int)
     * @see Dimension
     */
    public Dimension getSmallDimension() {
        return smallDimension;
    }

    /**
     * Sets the pixel dimensions for the small output size.
     * <p>
     * ImageResizerConfig does not hold a reference to the {@code dimension} parameter object.
     * Altering {@code dimension} after invoking this method will not affect the configured size.
     * </p>
     *
     * @param dimension A {@link Dimension} object representing the size to use for small output
     * @return This ImageResizerConfig object to allow for method chaining
     *
     * @see ImageResizeConfig#getSmallDimension()
     * @see ImageResizeConfig#setSmallDimensions(int, int)
     * @see Dimension
     */
    public ImageResizeConfig setSmallDimensions(Dimension dimension) {
        smallDimension.height = dimension.height;
        smallDimension.width = dimension.width;
        return this;
    }

    /**
     * Sets the pixel dimensions for the small output size.
     *
     * @param width The desired width in pixels of the small output image
     * @param height The desired height in pixels of the small output image
     * @return This ImageResizerConfig object to allow for method chaining
     *
     * @see ImageResizeConfig#getSmallDimension()
     * @see ImageResizeConfig#setSmallDimensions(Dimension)
     */
    public ImageResizeConfig setSmallDimensions(int width, int height) {
        smallDimension.height = height;
        smallDimension.width = width;
        return this;
    }


    /**
     * A class to represent pixel dimensions for width and height of an object.
     */
    public static class Dimension {
        private int width;
        private int height;

        /**
         * Creates a Dimension instance with the provided width and height.
         *
         * @param width The width of the Dimension object
         * @param height The height of the Dimension object
         */
        public Dimension(int width, int height) {
            this.width = width;
            this.height = height;
        }

        /**
         * Returns the {@code width} value of this Dimension object.
         *
         * @return The {@code width} value
         */
        public int getWidth() {
            return width;
        }

        /**
         * Returns the {@code height} value of this Dimension object.
         *
         * @return The {@code height} value
         */
        public int getHeight() {
            return height;
        }
    }
}
