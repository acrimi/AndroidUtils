package com.isbx.androidtools.ui.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Message;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.isbx.androidtools.R;

/**
 * A subclass of {@link AlertDialog} that contains and editable text field as its view and up to
 * three buttons.
 */
public class TextInputDialog extends AlertDialog {

    private EditText editText;

    /**
     * Creates a new input dialog with the default alert dialog theme.
     *
     * @param context The parent {@link Context}
     *
     * @see AlertDialog#AlertDialog(Context)
     */
    public TextInputDialog(Context context) {
        super(context);

        FrameLayout layout = new FrameLayout(context);
        int padding = context.getResources().getDimensionPixelSize(R.dimen.custom_dialog_padding);
        layout.setPadding(padding, padding, padding, padding);
        editText = new EditText(context);
        layout.addView(editText);
        setView(layout);
    }

    /**
     * Set a listener to be invoked when the positive button of the dialog is pressed.
     *
     * @param text The text to display in the positive button
     * @param onClickListener The {@link android.content.DialogInterface.OnClickListener} to use
     *
     * @see AlertDialog#setButton(int, CharSequence, Message)
     * @see AlertDialog#BUTTON_POSITIVE
     */
    public void setPositiveButton(String text, OnClickListener onClickListener) {
        setButton(BUTTON_POSITIVE, text, onClickListener);
    }

    /**
     * Set a listener to be invoked when the positive button of the dialog is pressed.
     *
     * @param textId The resource id of the text to display in the positive button
     * @param onClickListener The {@link android.content.DialogInterface.OnClickListener} to use
     *
     * @see AlertDialog#setButton(int, CharSequence, Message)
     * @see AlertDialog#BUTTON_POSITIVE
     */
    public void setPositiveButton(int textId, OnClickListener onClickListener) {
        setButton(BUTTON_POSITIVE, getContext().getString(textId), onClickListener);
    }

    /**
     * Set a listener to be invoked when the negative button of the dialog is pressed.
     *
     * @param text The text to display in the negative button
     * @param onClickListener The {@link android.content.DialogInterface.OnClickListener} to use
     *
     * @see AlertDialog#setButton(int, CharSequence, Message)
     * @see AlertDialog#BUTTON_NEGATIVE
     */
    public void setNegativeButton(String text, OnClickListener onClickListener) {
        setButton(BUTTON_NEGATIVE, text, onClickListener);
    }

    /**
     * Set a listener to be invoked when the negative button of the dialog is pressed.
     *
     * @param textId The resource id of the text to display in the negative button
     * @param onClickListener The {@link android.content.DialogInterface.OnClickListener} to use
     *
     * @see AlertDialog#setButton(int, CharSequence, Message)
     * @see AlertDialog#BUTTON_NEGATIVE
     */
    public void setNegativeButton(int textId, OnClickListener onClickListener) {
        setButton(BUTTON_NEGATIVE, getContext().getString(textId), onClickListener);
    }

    /**
     * Set a listener to be invoked when the neutral button of the dialog is pressed.
     *
     * @param text The text to display in the neutral button
     * @param onClickListener The {@link android.content.DialogInterface.OnClickListener} to use
     *
     * @see AlertDialog#setButton(int, CharSequence, Message)
     * @see AlertDialog#BUTTON_NEUTRAL
     */
    public void setNeutralButton(String text, OnClickListener onClickListener) {
        setButton(BUTTON_NEUTRAL, text, onClickListener);
    }

    /**
     * Set a listener to be invoked when the neutral button of the dialog is pressed.
     *
     * @param textId The resource id of the text to display in the neutral button
     * @param onClickListener The {@link android.content.DialogInterface.OnClickListener} to use
     *
     * @see AlertDialog#setButton(int, CharSequence, Message)
     * @see AlertDialog#BUTTON_NEUTRAL
     */
    public void setNeutralButton(int textId, OnClickListener onClickListener) {
        setButton(BUTTON_NEUTRAL, getContext().getString(textId), onClickListener);
    }

    /**
     * Sets the hint text to be displayed inside the {@link EditText} in this dialog.
     *
     * @param hintResId The resource id of the text to display as the text field's hint
     *
     * @see EditText#setHint(int)
     */
    public void setHint(int hintResId) {
        editText.setHint(hintResId);
    }

    /**
     * Sets the hint text to be displayed inside the {@link EditText} in this dialog.
     *
     * @param hint The text to display as the text field's hint
     *
     * @see EditText#setHint(CharSequence)
     */
    public void setHint(String hint) {
        editText.setHint(hint);
    }

    /**
     * Returns the text that is currently entered into this dialog's {@link EditText}.
     *
     * @return The text input of the EditText
     */
    public String getInput() {
        return editText.getText().toString();
    }
}
