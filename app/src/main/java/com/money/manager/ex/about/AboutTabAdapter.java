package com.money.manager.ex.about;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Adapter for the tabs on the About page.
 */
public class AboutTabAdapter
    extends FragmentPagerAdapter {

    private static int NUM_ITEMS = 3;
    private String tabTitles[];

    public AboutTabAdapter(FragmentManager fragmentManager, String titles[]) {
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
//                    return AboutChangelogFragment.newInstance(position);
                return WebChangelogFragment.newInstance();
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
