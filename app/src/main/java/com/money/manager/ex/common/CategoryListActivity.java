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
package com.money.manager.ex.common;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;

import com.money.manager.ex.Constants;
import com.money.manager.ex.R;

import androidx.fragment.app.FragmentManager;

/**
 * List of categories.
 */
public class CategoryListActivity
    extends MmxBaseFragmentActivity {

    public static final String KEY_REQUEST_ID = "CategorySubCategory:RequestId";
    public static final String INTENT_RESULT_CATEGID = "CategorySubCategory:CategId";
    public static final String INTENT_RESULT_CATEGNAME = "CategorySubCategory:CategName";
    public static final String INTENT_RESULT_SUBCATEGID = "CategorySubCategory:SubCategId";
    public static final String INTENT_RESULT_SUBCATEGNAME = "CategorySubCategory:SubCategName";

    public static final String FRAGMENTTAG = CategoryListActivity.class.getSimpleName() + "_Fragment";

    CategoryListFragment listFragment = new CategoryListFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.base_toolbar_activity);

        // enable home button into actionbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // get intent
        Intent intent = getIntent();

        if (intent != null && !(TextUtils.isEmpty(intent.getAction()))) {
            listFragment.mAction = intent.getAction();

            int requestId = intent.getIntExtra(KEY_REQUEST_ID, Constants.NOT_SET);
            listFragment.requestId = requestId;
        }

        // management fragment
        FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentById(R.id.content) == null) {
            // todo: use replace?
            fm.beginTransaction()
                .add(R.id.content, listFragment, FRAGMENTTAG)
                .commit();
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // set result and terminate activity
            CategoryListFragment fragment =
                    (CategoryListFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENTTAG);
            if (fragment != null) {
                fragment.setResultAndFinish();
            }
        }
        return super.onKeyUp(keyCode, event);
    }

}
