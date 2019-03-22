/*
 * Copyright (C) 2012-2019 The Android Money Manager Ex Project Team
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
package com.money.manager.ex.assetallocation;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.common.MmxBaseFragmentActivity;

import androidx.fragment.app.FragmentManager;

public class SecurityListActivity
    extends MmxBaseFragmentActivity {

    public static final String EXTRA_ASSET_CLASS_ID = "assetClassId";
    private static final String FRAGMENTTAG = SecurityListFragment.class.getSimpleName() + "_Fragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_security_list);
        setContentView(R.layout.base_toolbar_activity);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // get asset class id
        Integer assetClassId = getAssetClassId();
        showFragmentForAssetClass(assetClassId);
    }

    private Integer getAssetClassId() {
        Intent intent = getIntent();
        if (intent == null) return null;

        int assetClassId = intent.getIntExtra(EXTRA_ASSET_CLASS_ID, Constants.NOT_SET);
        if (assetClassId == Constants.NOT_SET) return null;

        return assetClassId;
    }

    private void showFragmentForAssetClass(Integer assetClassId) {
        if (assetClassId == null) return;

        SecurityListFragment listFragment = SecurityListFragment.create(assetClassId);

        Intent intent = getIntent();
        if (intent != null && !(TextUtils.isEmpty(intent.getAction()))) {
            listFragment.action = intent.getAction();
        }
        FragmentManager fm = getSupportFragmentManager();
        // attach fragment to activity
        if (fm.findFragmentById(R.id.content) == null) {
            fm.beginTransaction().add(R.id.content, listFragment, FRAGMENTTAG).commit();
        }
    }
}
