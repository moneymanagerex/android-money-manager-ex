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
package com.money.manager.ex;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.KeyEvent;

import com.money.manager.ex.fragment.BaseFragmentActivity;
import com.money.manager.ex.fragment.BaseListFragment;
import com.money.manager.ex.fragment.PayeeLoaderListFragment;

/**
 * @author Alessandro Lazzari (lazzari.ale@gmail.com)
 * @version 0.9.0
 */
public class PayeeActivity
        extends BaseFragmentActivity {

    public static final String INTENT_RESULT_PAYEEID = "PayeeActivity:PayeeId";
    public static final String INTENT_RESULT_PAYEENAME = "PayeeActivity:PayeeName";
    @SuppressWarnings("unused")
    private static final String LOGCAT = PayeeActivity.class.getSimpleName();
    private static final String FRAGMENTTAG = PayeeActivity.class.getSimpleName() + "_Fragment";

    PayeeLoaderListFragment listFragment = new PayeeLoaderListFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_toolbar_activity);

        // enable home button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // process intent
        Intent intent = getIntent();
        String action = intent.getAction();

        if (!TextUtils.isEmpty(action)) {
            PayeeLoaderListFragment.mAction = action;
        }
        FragmentManager fm = getSupportFragmentManager();
        // attach fragment activity
        if (fm.findFragmentById(R.id.content) == null) {
            fm.beginTransaction().add(R.id.content, listFragment, FRAGMENTTAG).commit();
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // set result
            BaseListFragment fragment = (BaseListFragment) getSupportFragmentManager()
                    .findFragmentByTag(FRAGMENTTAG);
            if (fragment != null) {
                fragment.getActivity().setResult(RESULT_CANCELED);
                fragment.getActivity().finish();
            }
        }
        return super.onKeyUp(keyCode, event);
    }
}
