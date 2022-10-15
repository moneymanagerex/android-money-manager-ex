/*
 * Copyright (C) 2012-2018 The Android Money Manager Ex Project Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.money.manager.ex.about;


import com.mikepenz.aboutlibraries.LibsBuilder;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

/**
 * Adapter for the tabs on the About page.
 */
public class AboutTabAdapter
    extends FragmentPagerAdapter {

    private static int NUM_ITEMS = 4;
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
            case 0:
                return AboutFragment.newInstance(position);
            case 1:
//                    return AboutChangelogFragment.newInstance(position);
                return WebChangelogFragment.newInstance();
            case 2:
                return AboutCreditsFragment.newInstance(position);
            case 3:
                return new LibsBuilder().supportFragment();
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
