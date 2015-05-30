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
package com.money.manager.ex.currency;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.KeyEvent;

import com.money.manager.ex.R;
import com.money.manager.ex.fragment.BaseFragmentActivity;
import com.money.manager.ex.utils.ActivityUtils;

/**
 * @author Alessandro Lazzari (lazzari.ale@gmail.com)
 * @version 1.0.0
 */
public class CurrencyFormatsListActivity
        extends BaseFragmentActivity {

    public static final String INTENT_RESULT_CURRENCYID = "CurrencyListActivity:ACCOUNTID";
    public static final String INTENT_RESULT_CURRENCYNAME = "CurrencyListActivity:ACCOUNTNAME";
    public static final String LOGCAT = CurrencyFormatsListActivity.class.getSimpleName();
    private static final String FRAGMENTTAG = CurrencyFormatsListActivity.class.getSimpleName() + "_Fragment";

    // Instance fragment list
    private CurrencyFormatsLoaderListFragment listFragment = new CurrencyFormatsLoaderListFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_toolbar_activity);

        // enabled home to come back
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // take intent
        Intent intent = getIntent();
        if (intent != null && !(TextUtils.isEmpty(intent.getAction()))) {
            // Store the requested action.
            listFragment.mAction = intent.getAction();
            // restore previous device orientation if it was modified.
            if(listFragment.PreviousOrientation != -1) {
                int currentOrientation = ActivityUtils.forceCurrentOrientation(this);
                if(currentOrientation != listFragment.PreviousOrientation) {
                    ActivityUtils.restoreOrientation(this, listFragment.PreviousOrientation);
                }
            }
        }

        FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentById(R.id.content) == null) {
            fm.beginTransaction().add(R.id.content, listFragment, FRAGMENTTAG).commit();
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // intercept key back
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            CurrencyFormatsLoaderListFragment fragment = (CurrencyFormatsLoaderListFragment)
                    getSupportFragmentManager().findFragmentByTag(FRAGMENTTAG);
            if (fragment != null) {
                fragment.setResultAndFinish();
            }
        }
        return super.onKeyUp(keyCode, event);
    }

}