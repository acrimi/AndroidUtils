package com.isbx.androidtools.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

/**
 * Created by alexs_000 on 5/17/2016.
 */
public class TextIconAdapter extends ArrayAdapter<String> {
    private int imageViewId;
    private int[] iconIds;

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

    public TextIconAdapter(Context context, int resource, int textViewId, int imageViewId, int labelArrayId, int iconArrayId) {
        this(context, resource, textViewId, imageViewId,
            context.getResources().getStringArray(labelArrayId), context.getResources().obtainTypedArray(iconArrayId));
    }

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
