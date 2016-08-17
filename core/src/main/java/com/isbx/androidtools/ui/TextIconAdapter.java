package com.isbx.androidtools.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

/**
 * An implementation of {@link ArrayAdapter} that is tailored for lists whose rows contain one image
 * icon and one text label. This is useful for common scenarios such as nav drawers where a list of
 * text labels is decorated with unique icons.
 */
public class TextIconAdapter extends ArrayAdapter<String> {
    private int imageViewId;
    private int[] iconIds;

    /**
     * Creates a new TextIconAdapter instance. The layout found at {@code resource} will be inflated
     * for each {@link View} generated by this adapter. In each of these views, the
     * {@link android.widget.TextView} found with {@code textViewId} will be populated with the
     * appropriate text from {@code labels}, and the {@link ImageView} found with
     * {@code imageViewId} will be populated with the appropriate image resource id from
     * {@code icons}.
     *
     * @param context The current {@link Context}
     * @param resource The resource ID for a layout file containing the layout to use when
     *                 instantiating views
     * @param textViewId The id of the {@link android.widget.TextView} within the layout resource to
     *                   be populated
     * @param imageViewId The id of the {@link ImageView} within the layout resource to be populated
     * @param labels An array of Strings to use as labels for each view
     * @param icons A {@link TypedArray} of image resource ids to use as icons for each view
     */
    public TextIconAdapter(Context context, int resource, int textViewId, int imageViewId, String[] labels, TypedArray icons) {
        super(context, resource, textViewId, labels);
        this.imageViewId = imageViewId;

        iconIds = new int[labels.length];
        for (int i = 0; i < icons.length(); i++) {
            int id = icons.getResourceId(i, 0);
            if (id == 0) {
                break;
            }

            iconIds[i] = id;
        }
    }

    /**
     * Creates a new TextIconAdapter instance. The layout found at {@code resource} will be inflated
     * for each {@link View} generated by this adapter. In each of these views, the
     * {@link android.widget.TextView} found with {@code textViewId} will be populated with the
     * appropriate text from the String array resource denoted by {@code labelArrayId}, and the
     * {@link ImageView} found with {@code imageViewId} will be populated with the appropriate image
     * resource id from the {@link TypedArray} resource denoted by {@code iconArrayId}.
     *
     * @param context The current {@link Context}
     * @param resource The resource ID for a layout file containing the layout to use when
     *                 instantiating views
     * @param textViewId The id of the {@link android.widget.TextView} within the layout resource to
     *                   be populated
     * @param imageViewId The id of the {@link ImageView} within the layout resource to be populated
     * @param labelArrayId The resource ID for a String array to use as labels for each view
     * @param iconArrayId The resource ID for a {@link TypedArray} of images to use as icons for
     *                    each view
     */
    public TextIconAdapter(Context context, int resource, int textViewId, int imageViewId, int labelArrayId, int iconArrayId) {
        this(context, resource, textViewId, imageViewId,
            context.getResources().getStringArray(labelArrayId), context.getResources().obtainTypedArray(iconArrayId));
    }

    /**
     * Extends the base {@link ArrayAdapter#getView(int, View, ViewGroup)} implementation to find
     * the {@link ImageView} with the {@code imageViewId} specified in this adapter. If the image
     * view is not null, its source image will be set to the appropriate resource id from this
     * adapter's {@code iconIds} array for the given {@code position}.
     *
     * @param position The position of the item within the adapter's data set of the item whose view we want.
     * @param convertView The old {@link View} to reuse, if possible. Note: You should check that
     *                    this view is non-null and of an appropriate type before using. If it is
     *                    not possible to convert this view to display the correct data, this method
     *                    can create a new view. Heterogeneous lists can specify their number of
     *                    view types, so that this View is always of the right type
     *                    (see {@link ArrayAdapter#getViewTypeCount()} and
     *                    {@link ArrayAdapter#getItemViewType(int)}.
     * @param parent The {@link ViewGroup} parent that this view will eventually be attached to
     * @return A {@link View} corresponding to the data at the specified position.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = super.getView(position, convertView, parent);
        ImageView imageView = (ImageView) v.findViewById(imageViewId);
        if (imageView != null) {
            imageView.setImageResource(iconIds[position]);
        }

        return v;
    }
}
