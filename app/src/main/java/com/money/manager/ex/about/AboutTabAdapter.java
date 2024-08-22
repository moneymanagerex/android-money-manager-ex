/*
 * Copyright (C) 2012-2024 The Android Money Manager Ex Project Team
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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/**
 * Adapter for the tabs on the About page.
 */
public class AboutTabAdapter
        extends FragmentStateAdapter {

    private static final int NUM_ITEMS = 4;
    private final String[] tabTitles;

    public AboutTabAdapter(@NonNull FragmentActivity fragmentActivity, String[] titles) {
        super(fragmentActivity);
        tabTitles = titles;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return AboutFragment.newInstance();
            case 1:
                return WebChangelogFragment.newInstance();
            case 2:
                return AboutCreditsFragment.newInstance();
            case 3:
                return new LibsBuilder().supportFragment();
            default:
                throw new IllegalArgumentException("Invalid position: " + position);
        }
    }

    @Override
    public int getItemCount() {
        return NUM_ITEMS;
    }

    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }
}
