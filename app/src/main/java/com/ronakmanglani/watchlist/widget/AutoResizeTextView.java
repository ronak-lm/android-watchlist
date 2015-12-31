package com.ronakmanglani.watchlist.widget;

import android.content.Context;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

public class AutoResizeTextView  extends TextView {

    /** Our ellipsis string. */
    private static final String mEllipsis = "\u2026";

    /**
     * Upper bounds for text size.
     * This acts as a starting point for resizing.
     */
    private float mMaxTextSizePixels;

    /** Lower bounds for text size. */
    private float mMinTextSizePixels;

    /** TextView line spacing multiplier. */
    private float mLineSpacingMultiplier = 1.0f;

    /** TextView additional line spacing. */
    private float mLineSpacingExtra = 0.0f;

    /**
     * Default constructor override.
     *
     * @param context
     */
    public AutoResizeTextView(Context context) {
        this(context, null);
        initialise();
        setMinTextSize(14);
    }

    /**
     * Default constructor when inflating from XML file.
     *
     * @param context
     * @param attrs
     */
    public AutoResizeTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        initialise();
        setMinTextSize(14);
    }

    /**
     * Default constructor override.
     *
     * @param context
     * @param attrs
     * @param defStyle
     */
    public AutoResizeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initialise();
        setMinTextSize(14);
    }

    @Override
    protected void onLayout (boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        resizeText();
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        requestLayout();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (w != oldw || h != oldh) {
            requestLayout();
        }
    }

    @Override
    public void setTextSize(float size) {
        setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
    }

    @Override
    public void setTextSize(int unit, float size) {
        super.setTextSize(unit, size);
        mMaxTextSizePixels = getTextSize();
        requestLayout();
    }

    @Override
    public void setLineSpacing(float add, float mult) {
        super.setLineSpacing(add, mult);
        mLineSpacingMultiplier = mult;
        mLineSpacingExtra = add;
        requestLayout();
    }

    @Override
    public void setEllipsize(TextUtils.TruncateAt where) {
        super.setEllipsize(where);
        requestLayout();
    }

    /**
     * Sets the lower text size limit and invalidates the view.
     *
     * @param minTextSizeScaledPixels the minimum size to use for text in this view,
     * in scaled pixels.
     */
    public void setMinTextSize(float minTextSizeScaledPixels) {
        mMinTextSizePixels = convertSpToPx(minTextSizeScaledPixels);
        requestLayout();
    }

    /**
     * @return lower text size limit, in pixels.
     */
    public float getMinTextSizePixels() {
        return mMinTextSizePixels;
    }

    private void initialise() {
        mMaxTextSizePixels = getTextSize();
    }

    /**
     * Resizes this view's text size with respect to its width and height
     * (minus padding).
     */
    private void resizeText() {
        final int availableHeightPixels = getHeight() - getCompoundPaddingBottom() - getCompoundPaddingTop();

        final int availableWidthPixels = getWidth() - getCompoundPaddingLeft() - getCompoundPaddingRight();

        final CharSequence text = getText();

        // Safety check
        // (Do not resize if the view does not have dimensions or if there is no text)
        if (text == null
                || text.length() <= 0
                || availableHeightPixels <= 0
                || availableWidthPixels <= 0
                || mMaxTextSizePixels <= 0) {
            return;
        }

        float targetTextSizePixels = mMaxTextSizePixels;
        int targetTextHeightPixels = getTextHeightPixels(text, availableWidthPixels, targetTextSizePixels);

        // Until we either fit within our TextView
        // or we have reached our minimum text size,
        // incrementally try smaller sizes
        while (targetTextHeightPixels > availableHeightPixels
                && targetTextSizePixels > mMinTextSizePixels) {
            targetTextSizePixels = Math.max(
                    targetTextSizePixels - 2,
                    mMinTextSizePixels);

            targetTextHeightPixels = getTextHeightPixels(
                    text,
                    availableWidthPixels,
                    targetTextSizePixels);
        }

        // If we have reached our minimum text size and the text still doesn't fit,
        // append an ellipsis
        // (NOTE: Auto-ellipsize doesn't work hence why we have to do it here)
        // depending on the value of getEllipsize())
        if (getEllipsize() != null
                && targetTextSizePixels == mMinTextSizePixels
                && targetTextHeightPixels > availableHeightPixels) {
            // Make a copy of the original TextPaint object for measuring
            TextPaint textPaintCopy = new TextPaint(getPaint());
            textPaintCopy.setTextSize(targetTextSizePixels);

            // Measure using a StaticLayout instance
            StaticLayout staticLayout = new StaticLayout(
                    text,
                    textPaintCopy,
                    availableWidthPixels,
                    Layout.Alignment.ALIGN_NORMAL,
                    mLineSpacingMultiplier,
                    mLineSpacingExtra,
                    false);

            // Check that we have a least one line of rendered text
            if (staticLayout.getLineCount() > 0) {
                // Since the line at the specific vertical position would be cut off,
                // we must trim up to the previous line and add an ellipsis
                int lastLine = staticLayout.getLineForVertical(availableHeightPixels) - 1;

                if (lastLine >= 0) {
                    int startOffset = staticLayout.getLineStart(lastLine);
                    int endOffset = staticLayout.getLineEnd(lastLine);
                    float lineWidthPixels = staticLayout.getLineWidth(lastLine);
                    float ellipseWidth = textPaintCopy.measureText(mEllipsis);

                    // Trim characters off until we have enough room to draw the ellipsis
                    while (availableWidthPixels < lineWidthPixels + ellipseWidth) {
                        endOffset--;
                        lineWidthPixels = textPaintCopy.measureText(
                                text.subSequence(startOffset, endOffset + 1).toString());
                    }

                    setText(text.subSequence(0, endOffset) + mEllipsis);
                }
            }
        }

        super.setTextSize(TypedValue.COMPLEX_UNIT_PX, targetTextSizePixels);
        // Some devices try to auto adjust line spacing, so force default line spacing
        super.setLineSpacing(mLineSpacingExtra, mLineSpacingMultiplier);
    }

    /**
     * Sets the text size of a clone of the view's {@link TextPaint} object
     * and uses a {@link StaticLayout} instance to measure the height of the text.
     *
     * @param source
     * @param availableWidthPixels
     * @param textSizePixels
     * @return the height of the text when placed in a view
     * with the specified width
     * and when the text has the specified size.
     */
    private int getTextHeightPixels(
            CharSequence source,
            int availableWidthPixels,
            float textSizePixels) {
        // Make a copy of the original TextPaint object
        // since the object gets modified while measuring
        // (see also the docs for TextView.getPaint()
        // which states to access it read-only)
        TextPaint textPaintCopy = new TextPaint(getPaint());
        textPaintCopy.setTextSize(textSizePixels);

        // Measure using a StaticLayout instance
        StaticLayout staticLayout = new StaticLayout(
                source,
                textPaintCopy,
                availableWidthPixels,
                Layout.Alignment.ALIGN_NORMAL,
                mLineSpacingMultiplier,
                mLineSpacingExtra,
                true);

        return staticLayout.getHeight();
    }

    /**
     * @param scaledPixels
     * @return the number of pixels which scaledPixels corresponds to on the device.
     */
    private float convertSpToPx(float scaledPixels) {
        float pixels = scaledPixels * getContext().getResources().getDisplayMetrics().scaledDensity;
        return pixels;
    }
}