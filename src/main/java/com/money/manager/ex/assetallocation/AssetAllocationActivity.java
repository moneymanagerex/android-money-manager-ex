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
package com.money.manager.ex.assetallocation;

import android.os.Bundle;
import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;

import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.common.BaseFragmentActivity;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.home.MainActivity;

public class AssetAllocationActivity
    extends BaseFragmentActivity {

    private static final String FRAGMENTTAG = AssetAllocationFragment.class.getSimpleName();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_asset_allocation);
        setContentView(R.layout.base_toolbar_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            setToolbarStandardAction(toolbar);
            // change home icon to 'back'.
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Get the currency. Use default currency.
        CurrencyService currencyService = new CurrencyService(this);
        String currencyCode = currencyService.getBaseCurrencyCode();

        // show the fragment
        FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentById(R.id.content) == null) {
            AssetAllocationFragment fragment = AssetAllocationFragment.create();
            fm.beginTransaction().add(R.id.content, fragment, FRAGMENTTAG).commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResultAndFinish();
                return true; // consumed here
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

//        Log.d(this.getClass().getSimpleName(), "Finishing Asset Allocation");
    }

    private void setResultAndFinish() {
        setResult(Activity.RESULT_OK);
        finish();
    }
}
