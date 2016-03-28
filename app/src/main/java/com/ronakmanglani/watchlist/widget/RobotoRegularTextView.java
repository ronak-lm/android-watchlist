package com.ronakmanglani.watchlist.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.ronakmanglani.watchlist.util.FontCache;

public class RobotoRegularTextView extends TextView {

    public RobotoRegularTextView(Context context) {
        super(context);

        applyCustomFont(context);
    }

    public RobotoRegularTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        applyCustomFont(context);
    }

    public RobotoRegularTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        applyCustomFont(context);
    }

    private void applyCustomFont(Context context) {
        Typeface customFont = FontCache.getTypeface(FontCache.ROBOTO_REGULAR, context);
        setTypeface(customFont);
    }
}