package com.money.manager.ex.assetallocation;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;

/**
 * Renders text as drawable
 */

public class TextDrawable2
    extends Drawable {

    private static final int DEFAULT_COLOR = Color.WHITE;
    private static final int DEFAULT_TEXTSIZE = 15;
    private Paint mPaint;
    private CharSequence mText;
    private int mIntrinsicWidth;
    private int mIntrinsicHeight;

    public TextDrawable2(Resources res, CharSequence text, Float textSize) {
        mText = text;
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(DEFAULT_COLOR);
        mPaint.setTextAlign(Paint.Align.CENTER);

        if (textSize == null) {
            // float
            textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                    DEFAULT_TEXTSIZE, res.getDisplayMetrics());
        }
        mPaint.setTextSize(textSize);

        mIntrinsicWidth = (int) (mPaint.measureText(mText, 0, mText.length()) + .5);
        mIntrinsicHeight = mPaint.getFontMetricsInt(null);
    }
    @Override
    public void draw(Canvas canvas) {
        Rect bounds = getBounds();
        canvas.drawText(mText, 0, mText.length(),
                bounds.centerX(), bounds.centerY(), mPaint);
    }
    @Override
    public int getOpacity() {
        return mPaint.getAlpha();
    }
    @Override
    public int getIntrinsicWidth() {
        return mIntrinsicWidth;
    }
    @Override
    public int getIntrinsicHeight() {
        return mIntrinsicHeight;
    }
    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter filter) {
        mPaint.setColorFilter(filter);
    }
}