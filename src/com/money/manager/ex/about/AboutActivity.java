/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.money.manager.ex.about;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.util.Log;

import com.astuetz.PagerSlidingTabStrip;
import com.money.manager.ex.R;
import com.money.manager.ex.fragment.BaseFragmentActivity;

/**
 * @author Alessandro Lazzari (lazzari.ale@gmail.com)
 * @version 1.0.0
 */
public class AboutActivity extends BaseFragmentActivity implements ActionBar.TabListener {
    @SuppressWarnings("unused")
    private static final String LOGCAT = AboutActivity.class.getSimpleName();
    private static final String BUNDLE_KEY_TABINDEX = "AboutActivity:tabindex";

    private ViewPager mViewPager;

    public static class MyPagerAdapter extends FragmentPagerAdapter {
        private static int NUM_ITEMS = 3;
        private String tabTitles[];

        public MyPagerAdapter(FragmentManager fragmentManager, String titles[]) {
            super(fragmentManager);
            tabTitles = titles;
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: // Fragment # 0 - This will show FirstFragment
                    return AboutFragment.newInstance(position);
                case 1: // Fragment # 0 - This will show FirstFragment different title
                    return AboutChangelogFragment.newInstance(position);
                case 2: // Fragment # 1 - This will show SecondFragment
                    return AboutCreditsFragment.newInstance(position);
                default:
                    return null;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // Generate title based on item position
            return tabTitles[position];
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_activity);
        setDisplayHomeAsUpEnabled(true);

        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setAdapter(new MyPagerAdapter(getSupportFragmentManager(), new String[]{getString(R.string.about), getString(R.string.changelog), getString(R.string.credits)}));

        // Give the PagerSlidingTabStrip the ViewPager
        PagerSlidingTabStrip tabsStrip = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        // Attach the view pager to the tab strip
        tabsStrip.setViewPager(mViewPager);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt(BUNDLE_KEY_TABINDEX, mViewPager.getCurrentItem());
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mViewPager.setCurrentItem(savedInstanceState.getInt(BUNDLE_KEY_TABINDEX));
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        Fragment fragment = null;
        switch (tab.getPosition()) {
            case 0: //about
                fragment = new AboutFragment();
                break;
            case 1: //changelog
                fragment = new AboutChangelogFragment();
                break;
            case 2: //credits
                fragment = new AboutCreditsFragment();
                break;
            default:
                break;
        }
        if (fragment != null)
            fragmentTransaction.replace(android.R.id.content, fragment);
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    protected void setTheme() {
        try {
            this.setTheme(R.style.Theme_Money_Manager_Light_DarkActionBar);
        } catch (Exception e) {
            Log.e(BaseFragmentActivity.class.getSimpleName(), e.getMessage());
        }
    }
}
