package com.isbx.databinding.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.isbx.androidtools.ui.TextIconAdapter;

/**
 * Created by alexs_000 on 5/17/2016.
 */
public class BindingTextIconAdapter extends TextIconAdapter {
    private int resourceId;

    public BindingTextIconAdapter(Context context, int resource, int textViewId, int imageViewId, int labelArrayId, int iconArrayId) {
        super(context, resource, textViewId, imageViewId, labelArrayId, iconArrayId);
        resourceId = resource;
    }

    public BindingTextIconAdapter(Context context, int resource, int textViewId, int imageViewId, String[] labels, TypedArray icons) {
        super(context, resource, textViewId, imageViewId, labels, icons);
        resourceId = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = DataBindingUtil.inflate(LayoutInflater.from(getContext()), resourceId, parent, false).getRoot();
        }
        return super.getView(position, convertView, parent);
    }
}
