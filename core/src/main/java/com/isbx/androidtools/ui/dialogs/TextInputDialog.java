package com.isbx.androidtools.ui.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.isbx.androidtools.R;

/**
 * Created by alexs_000 on 6/15/2016.
 */
public class TextInputDialog extends AlertDialog {

    private EditText editText;

    public TextInputDialog(Context context) {
        super(context);

        FrameLayout layout = new FrameLayout(context);
        int padding = context.getResources().getDimensionPixelSize(R.dimen.custom_dialog_padding);
        layout.setPadding(padding, padding, padding, padding);
        editText = new EditText(context);
        layout.addView(editText);
        setView(layout);
    }

    public void setPositiveButton(String text, OnClickListener onClickListener) {
        setButton(BUTTON_POSITIVE, text, onClickListener);
    }

    public void setPositiveButton(int textId, OnClickListener onClickListener) {
        setButton(BUTTON_POSITIVE, getContext().getString(textId), onClickListener);
    }

    public void setNegativeButton(String text, OnClickListener onClickListener) {
        setButton(BUTTON_NEGATIVE, text, onClickListener);
    }

    public void setNegativeButton(int textId, OnClickListener onClickListener) {
        setButton(BUTTON_NEGATIVE, getContext().getString(textId), onClickListener);
    }

    public void setNeutralButton(String text, OnClickListener onClickListener) {
        setButton(BUTTON_NEUTRAL, text, onClickListener);
    }

    public void setNeutralButton(int textId, OnClickListener onClickListener) {
        setButton(BUTTON_NEUTRAL, getContext().getString(textId), onClickListener);
    }

    public void setHint(int hintResId) {
        editText.setHint(hintResId);
    }

    public void setHint(String hint) {
        editText.setHint(hint);
    }

    public String getInput() {
        return editText.getText().toString();
    }
}
