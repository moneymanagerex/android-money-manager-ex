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
package com.money.manager.ex;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;

import androidx.fragment.app.FragmentManager;

import com.money.manager.ex.common.BaseListFragment;
import com.money.manager.ex.common.MmxBaseFragmentActivity;
import com.money.manager.ex.fragment.PayeeListFragment;

public class PayeeActivity
        extends MmxBaseFragmentActivity {

    public static final String INTENT_RESULT_PAYEEID = "PayeeActivity:PayeeId";
    public static final String INTENT_RESULT_PAYEENAME = "PayeeActivity:PayeeName";
    private static final String FRAGMENTTAG = PayeeActivity.class.getSimpleName() + "_Fragment";

    PayeeListFragment listFragment = new PayeeListFragment();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_toolbar_activity);

        // enable home button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // process intent
        final Intent intent = getIntent();
        final String action = intent.getAction();

        if (!TextUtils.isEmpty(action)) {
            PayeeListFragment.mAction = action;
        }
        final FragmentManager fm = getSupportFragmentManager();
        // attach fragment activity
        if (null == fm.findFragmentById(R.id.content)) {
            // todo: use .replace
            fm.beginTransaction()
                    .add(R.id.content, listFragment, FRAGMENTTAG)
                    .commit();
        }
    }

    @Override
    public boolean onKeyUp(final int keyCode, final KeyEvent event) {
        if (KeyEvent.KEYCODE_BACK == keyCode) {
            // set result
            final BaseListFragment fragment = (BaseListFragment) getSupportFragmentManager()
                    .findFragmentByTag(FRAGMENTTAG);
            if (null != fragment) {
                fragment.getActivity().setResult(RESULT_CANCELED);
                fragment.getActivity().finish();
            }
        }
        return super.onKeyUp(keyCode, event);
    }
}
