package com.jungkai.advancedtab;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jungkai.slidingtabs.SlidingTabLayout;


public class MainActivity extends ActionBarActivity {

    private SlidingTabLayout mSlidingTabLayout;

    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // BEGIN_INCLUDE (setup_viewpager)
        // Get the ViewPager and set it's PagerAdapter so that it can display items
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setAdapter(new SamplePagerAdapter());
        // END_INCLUDE (setup_viewpager)

        // BEGIN_INCLUDE (setup_slidingtablayout)
        // Give the SlidingTabLayout the ViewPager, this must be done AFTER the ViewPager has had
        // it's PagerAdapter set.
        mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setStyle(R.style.SlidingTab_Main);
        mSlidingTabLayout.setDisplayType(SlidingTabLayout.DisplayType.ICON_ONLY);
        mSlidingTabLayout.setStretchToParent(true);
        mSlidingTabLayout.setViewPager(mViewPager);
    }

    class SamplePagerAdapter extends PagerAdapter implements SlidingTabLayout.TabIconProvider{

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return o == view;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "Item " + (position + 1);
        }
        // END_INCLUDE (pageradapter_getpagetitle)

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            // Inflate a new layout from our resources
            View view = getLayoutInflater().inflate(R.layout.pager_item,
                    container, false);
            // Add the newly created View to the ViewPager
            container.addView(view);

            // Retrieve a TextView from the inflated View, and update it's text
            TextView title = (TextView) view.findViewById(R.id.item_title);
            title.setText(String.valueOf(position + 1));

            // Return the View
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getImageResourceId(int position) {
            switch (position) {
                case 0:
                    return R.drawable.tab_friend_icon;
                case 1:
                    return R.drawable.tab_chatting_icon;
                case 2:
                    return R.drawable.tab_recommend_icon;
                case 3:
                    return R.drawable.tab_more_icon;
                default:
                    return R.drawable.tab_friend_icon;
            }

        }
    }
}
