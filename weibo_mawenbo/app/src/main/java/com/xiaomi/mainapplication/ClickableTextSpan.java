package com.xiaomi.mainapplication;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

class ClickableTextSpan extends ClickableSpan {
    private String text;
    private String toastMessage;
    private boolean isClicked;
    private TextView textView;
    private ForegroundColorSpan clickedColorSpan;

    public ClickableTextSpan(String text, String toastMessage, TextView textView) {
        this.text = text;
        this.toastMessage = toastMessage;
        this.isClicked = false;
        this.textView = textView;
        this.clickedColorSpan = new ForegroundColorSpan(Color.BLUE);
    }

    public boolean isClicked() {
        return isClicked;
    }

    public void setClicked(boolean clicked) {
        isClicked = clicked;
    }

    @Override
    public void onClick(@NonNull View widget) {
        showToast(toastMessage);
        setClicked(true);
        updateTextStyles();
    }

    @Override
    public void updateDrawState(@NonNull TextPaint ds) {
        super.updateDrawState(ds);
        ds.setUnderlineText(false);
        if (isClicked) {
            ds.setColor(Color.RED);
        }
    }

    private void updateTextStyles() {
        SpannableString spannableString = new SpannableString(textView.getText());
        int startIndex = textView.getText().toString().indexOf(text);
        int endIndex = startIndex + text.length();

        if (isClicked) {
            spannableString.setSpan(clickedColorSpan, startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            spannableString.removeSpan(clickedColorSpan);
        }

        textView.setText(spannableString);
    }

    private void showToast(String message) {
        Toast.makeText(textView.getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
