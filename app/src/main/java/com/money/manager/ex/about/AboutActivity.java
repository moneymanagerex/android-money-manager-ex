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

import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;
import com.money.manager.ex.R;
import com.money.manager.ex.common.MmxBaseFragmentActivity;

import androidx.viewpager.widget.ViewPager;
import timber.log.Timber;

/**
 * About the app
 */
public class AboutActivity
    extends MmxBaseFragmentActivity {

    private static final String BUNDLE_KEY_TABINDEX = "AboutActivity:tabindex";

    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_activity);
        setDisplayHomeAsUpEnabled(true);

        mViewPager = findViewById(R.id.viewpager);
        mViewPager.setAdapter(new AboutTabAdapter(getSupportFragmentManager(),
            new String[]{
                getString(R.string.about),
                getString(R.string.changelog),
                getString(R.string.credits),
                getString(R.string.libraries)
            }));

        // Tab Layout
        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
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
    protected void setTheme() {
        try {
            this.setTheme(R.style.Theme_Money_Manager_Light);
        } catch (Exception e) {
            Timber.e(e, "setting theme");
        }
    }
}
