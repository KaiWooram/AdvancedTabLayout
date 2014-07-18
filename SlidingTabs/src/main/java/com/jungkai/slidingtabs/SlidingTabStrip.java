/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jungkai.slidingtabs;

import android.R;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;

class SlidingTabStrip extends LinearLayout {

    public static final int DEFAULT_BOTTOM_BORDER_THICKNESS_DIPS = 2;
    public static final int DEFAULT_BOTTOM_BORDER_COLOR = 0xff000000;
    public static final int DEFAULT_BOTTOM_BORDER_COLOR_ALPHA = 0x26;
    public static final int DEFAULT_INDICATOR_THICKNESS_DIPS = 6;
    public static final int DEFAULT_SELECTED_INDICATOR_COLOR = 0xFF33B5E5;

    public static final int DEFAULT_DIVIDER_THICKNESS_DIPS = 0;
    public static final byte DEFAULT_DIVIDER_COLOR_ALPHA = 0x20;
    public static final float DEFAULT_DIVIDER_HEIGHT = 0.5f;

    private int mBottomBorderThickness;

    private final Paint mBottomBorderPaint;

    private int indicatorThickness;

    private int dividerThickness;

    private final int bottomBorderColor;

    private final Paint mDividerPaint;

    private final float mDividerHeight;

    private int mSelectedPosition;

    private float mSelectionOffset;

    private Drawable tabIndicatorDrawable = new ColorDrawable(DEFAULT_SELECTED_INDICATOR_COLOR);

    private int tabDividerColor = setColorAlpha(DEFAULT_BOTTOM_BORDER_COLOR,
            DEFAULT_DIVIDER_COLOR_ALPHA);

    SlidingTabStrip(Context context) {
        this(context, null);
    }

    SlidingTabStrip(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);

        final float density = getResources().getDisplayMetrics().density;

        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorForeground, outValue, true);
        final int themeForegroundColor =  outValue.data;

        bottomBorderColor = setColorAlpha(themeForegroundColor, DEFAULT_BOTTOM_BORDER_COLOR_ALPHA);

        mBottomBorderThickness = (int) (DEFAULT_BOTTOM_BORDER_THICKNESS_DIPS * density);
        mBottomBorderPaint = new Paint();
        mBottomBorderPaint.setColor(bottomBorderColor);

        indicatorThickness = (int) (DEFAULT_INDICATOR_THICKNESS_DIPS * density);

        mDividerHeight = DEFAULT_DIVIDER_HEIGHT;
        mDividerPaint = new Paint();

        dividerThickness = (int) (DEFAULT_DIVIDER_THICKNESS_DIPS * density);
    }

    void setBottomBorderThickness(int bottomBorderThickness) {
        this.mBottomBorderThickness = bottomBorderThickness;
    }

    void setIndicatorThickness(int thicknessInPixelSize) {
        indicatorThickness = thicknessInPixelSize;
    }

    void setDividerThickness(int thicknessInPixelSize) {
        dividerThickness = thicknessInPixelSize;
    }

    void setIndicatorDrawable(Drawable drawable) {
        tabIndicatorDrawable = drawable;
        invalidate();
    }

    void setDividerColors(int color, int alpha) {
        tabDividerColor = setColorAlpha(color, alpha);
        invalidate();
    }

    void onViewPagerPageChanged(int position, float positionOffset) {
        mSelectedPosition = position;
        mSelectionOffset = positionOffset;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        final int height = getHeight();
        final int childCount = getChildCount();
        final int dividerHeightPx = (int) (Math.min(Math.max(0f, mDividerHeight), 1f) * height);

        if (childCount > 0) {
            View selectedTitle = getChildAt(mSelectedPosition);
            int left = selectedTitle.getLeft();
            int right = selectedTitle.getRight();

            if (mSelectionOffset > 0f && mSelectedPosition < (getChildCount() - 1)) {

                View nextTitle = getChildAt(mSelectedPosition + 1);
                left = (int) (mSelectionOffset * nextTitle.getLeft() +
                        (1.0f - mSelectionOffset) * left);
                right = (int) (mSelectionOffset * nextTitle.getRight() +
                        (1.0f - mSelectionOffset) * right);
            }

            tabIndicatorDrawable.setBounds(left, height - indicatorThickness, right, height);
            tabIndicatorDrawable.draw(canvas);
        }

        canvas.drawRect(0, height - mBottomBorderThickness, getWidth(), height, mBottomBorderPaint);

        mDividerPaint.setStrokeWidth(dividerThickness);

        int separatorTop = (height - dividerHeightPx) / 2;
        for (int i = 0; i < childCount - 1; i++) {
            View child = getChildAt(i);
            mDividerPaint.setColor(tabDividerColor);
            canvas.drawLine(child.getRight(), separatorTop, child.getRight(),
                    separatorTop + dividerHeightPx, mDividerPaint);
        }
    }

    /**
     * Set the alpha value of the {@code color} to be the given {@code alpha} value.
     */
    private static int setColorAlpha(int color, int alpha) {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }

    /**
     * Blend {@code color1} and {@code color2} using the given ratio.
     *
     * @param ratio of which to blend. 1.0 will return {@code color1}, 0.5 will give an even blend,
     *              0.0 will return {@code color2}.
     */
    private static int blendColors(int color1, int color2, float ratio) {
        final float inverseRation = 1f - ratio;
        float r = (Color.red(color1) * ratio) + (Color.red(color2) * inverseRation);
        float g = (Color.green(color1) * ratio) + (Color.green(color2) * inverseRation);
        float b = (Color.blue(color1) * ratio) + (Color.blue(color2) * inverseRation);
        return Color.rgb((int) r, (int) g, (int) b);
    }
}