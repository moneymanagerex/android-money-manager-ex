/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
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
package com.money.manager.ex.currency;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.KeyEvent;

import com.money.manager.ex.R;
import com.money.manager.ex.common.BaseFragmentActivity;
import com.money.manager.ex.utils.ActivityUtils;

/**
 * List of currencies.
 */
public class CurrencyListActivity
        extends BaseFragmentActivity {

    public static final String INTENT_RESULT_CURRENCYID = "CurrencyListActivity:ACCOUNTID";
    public static final String INTENT_RESULT_CURRENCYNAME = "CurrencyListActivity:ACCOUNTNAME";
    private static final String FRAGMENTTAG = CurrencyListActivity.class.getSimpleName() + "_Fragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_toolbar_activity);

        // change home icon to 'back'.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        CurrencyListFragment listFragment = new CurrencyListFragment();

        // take intent
        Intent intent = getIntent();
        if (intent != null && !(TextUtils.isEmpty(intent.getAction()))) {
            // Store the requested action.
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_MAIN)) {
                action = Intent.ACTION_EDIT;
            }
            listFragment.mAction = action;
            // restore previous device orientation if it was modified.
            if(listFragment.mPreviousOrientation != -1) {
                int currentOrientation = ActivityUtils.forceCurrentOrientation(this);
                if(currentOrientation != listFragment.mPreviousOrientation) {
                    ActivityUtils.restoreOrientation(this, listFragment.mPreviousOrientation);
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
            CurrencyListFragment fragment = (CurrencyListFragment)
                    getSupportFragmentManager().findFragmentByTag(FRAGMENTTAG);
            if (fragment != null) {
                fragment.setResultAndFinish();
            }
        }
        return super.onKeyUp(keyCode, event);
    }

}