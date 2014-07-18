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

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SlidingTabLayout extends HorizontalScrollView {

    public enum DisplayType {
        TITLE_ONLY, ICON_ONLY, TITLE_AND_ICON
    }

    public interface TabIconProvider {
        int getImageResourceId(int position);
    }

    public interface OnTabChangedListener {
        void onTabChanged(int position);
    }

    private OnTabChangedListener tabChangedListener;

    private static final int TITLE_OFFSET_DIPS = 24;

    private DisplayType displayType;

    private TextView tabTitleView;

    private ImageView tabIconView;

    private int mTitleOffset;

    private int mTabViewLayoutId;

    private ViewPager mViewPager;

    private ViewPager.OnPageChangeListener mViewPagerPageChangeListener;

    private SlidingTabStrip mTabStrip;

    private boolean stretchToParent = false;

    private Context context;

    public SlidingTabLayout(Context context) {
        this(context, null);
    }

    public SlidingTabLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlidingTabLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {

        this.context = context;

        displayType = DisplayType.TITLE_ONLY;

        setHorizontalScrollBarEnabled(false);

        setFillViewport(true);

        mTitleOffset = (int) (TITLE_OFFSET_DIPS * getResources().getDisplayMetrics().density);

        mTabStrip = new SlidingTabStrip(context);

        addView(mTabStrip, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    }

    public void setStyle(int styleResId) {
        applyStyle(context, styleResId);
    }

    private void applyStyle(Context context, int styleResId) {
        TypedArray a = context.obtainStyledAttributes(styleResId, R.styleable.SlidingTabLayout);
        final float density = getResources().getDisplayMetrics().density;
        if (a != null) {
            int n = a.getIndexCount();
            for (int i = 0; i < n; i++) {
                int attr = a.getIndex(i);

                switch (attr) {
                    case R.styleable.SlidingTabLayout_tabBottomBorderHeight:
                        setBottomBorderThickness(a.getDimensionPixelSize(attr, (int) (SlidingTabStrip.DEFAULT_BOTTOM_BORDER_THICKNESS_DIPS * density)));
                        break;
                    case R.styleable.SlidingTabLayout_tabIndicatorThickness:
                        setIndicatorThickness(a.getDimensionPixelSize(attr, (int) (SlidingTabStrip.DEFAULT_INDICATOR_THICKNESS_DIPS * density)));
                        break;
                    case R.styleable.SlidingTabLayout_tabIndicatorDrawable:
                        setIndicatorDrawable(a.getDrawable(attr));
                        break;
                    case R.styleable.SlidingTabLayout_tabDividerThickness:
                        setDividerThickness(a.getDimensionPixelSize(attr, (int) (SlidingTabStrip.DEFAULT_DIVIDER_THICKNESS_DIPS * density)));
                        break;
                    default:
                        break;
                }
            }
        }
    }

    public void setDisplayType(DisplayType displayType) {
        this.displayType = displayType;
    }

    public DisplayType getDisplayType() {
        return this.displayType;
    }

    public void setStretchToParent(boolean stretchToParent) {
        this.stretchToParent = stretchToParent;
    }

    public void setBottomBorderThickness(int bottomBorderThickness){
        this.mTabStrip.setBottomBorderThickness(bottomBorderThickness);
    }

    public void setDividerThickness(int bottomBorderThickness){
        this.mTabStrip.setDividerThickness(bottomBorderThickness);
    }

    public boolean isStretchToParent() {
        return this.stretchToParent;
    }

    public void setIndicatorDrawable(Drawable drawable) {
        mTabStrip.setIndicatorDrawable(drawable);
    }

    public void setIndicatorThickness(int thickness) {
        mTabStrip.setIndicatorThickness(thickness);
    }

    public void setDividerColors(int color, int alpha) {
        mTabStrip.setDividerColors(color, alpha);
    }

    public void setOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
        mViewPagerPageChangeListener = listener;
    }

    public void setCustomTabView(int layoutResId) {
        mTabViewLayoutId = layoutResId;
    }

    public void setViewPager(ViewPager viewPager) {
        mTabStrip.removeAllViews();

        mViewPager = viewPager;
        if (viewPager != null) {
            viewPager.setOnPageChangeListener(new InternalViewPagerListener());
            populateTabStrip();
        }
    }

    private void populateTabStrip() {

        final PagerAdapter adapter = mViewPager.getAdapter();
        final OnClickListener tabClickListener = new TabClickListener();

        int itemCount = adapter.getCount();

        for (int position = 0; position < itemCount; position++) {

            View tabView = LayoutInflater.from(getContext()).inflate(R.layout.tab_item_layout, mTabStrip, false);
            tabTitleView = (TextView) tabView.findViewById(R.id.tv_tab_title);
            tabIconView = (ImageView) tabView.findViewById(R.id.iv_tab_icon);

            tabView.setOnClickListener(tabClickListener);

            if (stretchToParent) {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
                tabView.setLayoutParams(params);
            }

            if (displayType == DisplayType.TITLE_ONLY || displayType == DisplayType.TITLE_AND_ICON) {
                tabTitleView.setVisibility(View.VISIBLE);
                tabTitleView.setText(adapter.getPageTitle(position));
            } else {
                tabTitleView.setVisibility(View.GONE);
            }

            if ((displayType == DisplayType.ICON_ONLY || displayType == DisplayType.TITLE_AND_ICON) && adapter instanceof TabIconProvider) {
                final int imageResourceId = ((TabIconProvider) adapter).getImageResourceId(position);
                tabIconView.setVisibility(View.VISIBLE);
                tabIconView.setImageResource(imageResourceId);
            } else {
                tabIconView.setVisibility(View.GONE);
            }

            mTabStrip.addView(tabView);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mViewPager != null) {
            scrollToTab(mViewPager.getCurrentItem(), 0);
        }
    }

    private void scrollToTab(int tabIndex, int positionOffset) {
        final int tabStripChildCount = mTabStrip.getChildCount();
        if (tabStripChildCount == 0 || tabIndex < 0 || tabIndex >= tabStripChildCount) {
            return;
        }

        View selectedChild = mTabStrip.getChildAt(tabIndex);
        if (selectedChild != null) {
            int targetScrollX = selectedChild.getLeft() + positionOffset;

            if (tabIndex > 0 || positionOffset > 0) {
                targetScrollX -= mTitleOffset;
            }

            scrollTo(targetScrollX, 0);
        }
    }

    private class InternalViewPagerListener implements ViewPager.OnPageChangeListener {
        private int mScrollState;

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            int tabStripChildCount = mTabStrip.getChildCount();
            if ((tabStripChildCount == 0) || (position < 0) || (position >= tabStripChildCount)) {
                return;
            }

            mTabStrip.onViewPagerPageChanged(position, positionOffset);

            View selectedTab = mTabStrip.getChildAt(position);
            int extraOffset = (selectedTab != null)
                    ? (int) (positionOffset * selectedTab.getWidth())
                    : 0;
            scrollToTab(position, extraOffset);

            if (mViewPagerPageChangeListener != null) {
                mViewPagerPageChangeListener.onPageScrolled(position, positionOffset,
                        positionOffsetPixels);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            mScrollState = state;

            if (mViewPagerPageChangeListener != null) {
                mViewPagerPageChangeListener.onPageScrollStateChanged(state);
            }
        }

        @Override
        public void onPageSelected(int position) {
            if (mScrollState == ViewPager.SCROLL_STATE_IDLE) {
                mTabStrip.onViewPagerPageChanged(position, 0f);
                scrollToTab(position, 0);

                for (int i = 0; i < mTabStrip.getChildCount(); i++) {

                    View selectedTab = mTabStrip.getChildAt(position);
                    if (position == i) {
                        selectedTab.setSelected(true);
                    } else {
                        selectedTab.setSelected(false);
                    }
                }
            }

            if (mViewPagerPageChangeListener != null) {
                mViewPagerPageChangeListener.onPageSelected(position);
            }
        }
    }

    public void setOnTabChangedListner(OnTabChangedListener listener) {
        tabChangedListener = listener;
    }

    private class TabClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            for (int i = 0; i < mTabStrip.getChildCount(); i++) {
                if (v == mTabStrip.getChildAt(i)) {
                    mViewPager.setCurrentItem(i);

                    if (tabChangedListener != null) {
                        tabChangedListener.onTabChanged(i);
                    }

                    return;
                }
            }
        }
    }
}
