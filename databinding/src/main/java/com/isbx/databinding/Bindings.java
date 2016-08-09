package com.isbx.databinding;

import android.databinding.BindingAdapter;
import android.databinding.InverseBindingListener;
import android.databinding.InverseBindingMethod;
import android.databinding.InverseBindingMethods;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.support.design.widget.TextInputLayout;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.isbx.androidtools.utils.FontCache;

/**
 * Created by alexs_000 on 4/27/2016.
 */
@InverseBindingMethods({
        @InverseBindingMethod(type = ViewPager.class, attribute = "currentItem")
})
public class Bindings {

    @BindingAdapter("android:layout_marginBottom")
    public static void setBottomMargin(View view, float pixelMarginBottom) {
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        layoutParams.setMargins(layoutParams.leftMargin, layoutParams.topMargin,
            layoutParams.rightMargin, (int) pixelMarginBottom);
        view.setLayoutParams(layoutParams);
    }

    @BindingAdapter("android:layout_marginTop")
    public static void setTopMargin(View view, float pixelMarginTop) {
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        layoutParams.setMargins(layoutParams.leftMargin, (int) pixelMarginTop,
            layoutParams.rightMargin, layoutParams.bottomMargin);
        view.setLayoutParams(layoutParams);
    }

    @BindingAdapter("android:layout_marginLeft")
    public static void setLeftMargin(View view, float pixelMarginLeft) {
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        layoutParams.setMargins((int) pixelMarginLeft, layoutParams.topMargin,
            layoutParams.rightMargin, layoutParams.bottomMargin);
        view.setLayoutParams(layoutParams);
    }

    @BindingAdapter("android:layout_marginRight")
    public static void setRightMargin(View view, float pixelMarginRight) {
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        layoutParams.setMargins(layoutParams.leftMargin, layoutParams.topMargin,
            (int) pixelMarginRight, layoutParams.bottomMargin);
        view.setLayoutParams(layoutParams);
    }

    @BindingAdapter("android:layout_margin")
    public static void setMargin(View view, float pixelMargin) {
        int margin = (int) pixelMargin;
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        layoutParams.setMargins(margin, margin, margin, margin);
        view.setLayoutParams(layoutParams);
    }

    @BindingAdapter("bind:font")
    public static void setFont(TextView view, String fontName) {
        Typeface typeface = FontCache.getFont(view.getContext(), fontName);
        view.setTypeface(typeface);
    }

    @BindingAdapter("bind:font")
    public static void setFont(TextInputLayout view, String fontName) {
        Typeface typeface = FontCache.getFont(view.getContext(), fontName);
        view.setTypeface(typeface);
    }

    @BindingAdapter({"android:background", "backgroundRadiusDp"})
    public static void setBackgroundRadius(View view, int backgroundColor, int backgroundRadiusDp) {
        float pixelRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, backgroundRadiusDp,
                view.getResources().getDisplayMetrics());
        setBackgroundRadius(view, backgroundColor, pixelRadius);
    }

    @BindingAdapter({"android:background", "backgroundRadius"})
    public static void setBackgroundRadius(View view, int backgroundColor, float pixelRadius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setColor(backgroundColor);
        drawable.setCornerRadius(pixelRadius);
        view.setBackground(drawable);
    }

    @BindingAdapter("backgroundRadiusDp")
    public static void setBackgroundRadius(View view, int backgroundRadiusDp) {
        float pixelRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, backgroundRadiusDp,
                view.getResources().getDisplayMetrics());
        setBackgroundRadius(view, pixelRadius);
    }

    @BindingAdapter("backgroundRadius")
    public static void setBackgroundRadius(View view, float pixelRadius) {
        Drawable d = view.getBackground();
        if (d != null) {
            Drawable newBackground = null;

            if (d instanceof GradientDrawable) {
                newBackground = d.mutate();
                ((GradientDrawable) newBackground).setCornerRadius(pixelRadius);
            } else if (d instanceof ColorDrawable) {
                int color = ((ColorDrawable) d).getColor();
                GradientDrawable gradientDrawable = new GradientDrawable();
                gradientDrawable.setShape(GradientDrawable.RECTANGLE);
                gradientDrawable.setColor(color);
                gradientDrawable.setCornerRadius(pixelRadius);
                newBackground = gradientDrawable;
            } else if (d instanceof BitmapDrawable) {
                Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
                RoundedBitmapDrawable roundedDrawable = RoundedBitmapDrawableFactory.create(view.getResources(), bitmap);
                roundedDrawable.setCornerRadius(pixelRadius);
                newBackground = roundedDrawable;
            }

            if (newBackground != null) {
                view.setBackground(newBackground);
            }
        }
    }

    @BindingAdapter(value = {"android:src", "circular"}, requireAll = false)
    public static void setImageRadius(ImageView view, Uri src, boolean circular) {
        if (src == null) {
            return;
        }

        view.setImageURI(src);
        Drawable d = view.getDrawable();
        if (d != null && circular) {
            Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
            int dimen = Math.min(bitmap.getWidth(), bitmap.getHeight());
            bitmap = ThumbnailUtils.extractThumbnail(bitmap, dimen, dimen, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
            RoundedBitmapDrawable roundedDrawable = RoundedBitmapDrawableFactory.create(view.getResources(), bitmap);
            roundedDrawable.setCircular(true);
            view.setImageDrawable(roundedDrawable);
            view.setLayerType(View.LAYER_TYPE_SOFTWARE, null); // Needed to fix black images on lollipop
        }
    }

    @BindingAdapter("currentItemAttrChanged")
    public static void setCurrentItemListener(ViewPager view, final InverseBindingListener currentItemChange) {
        if (currentItemChange != null) {
            view.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    currentItemChange.onChange();
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        }
    }
}
