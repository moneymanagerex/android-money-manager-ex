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
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.money.manager.ex.core.Core;
import com.money.manager.ex.common.BaseFragmentActivity;
import com.money.manager.ex.inapp.util.IabHelper;
import com.money.manager.ex.inapp.util.IabResult;
import com.money.manager.ex.inapp.util.Inventory;
import com.money.manager.ex.inapp.util.Purchase;
import com.money.manager.ex.inapp.util.SkuDetails;
import com.money.manager.ex.view.RobotoButton;
import com.money.manager.ex.view.RobotoTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DonateActivity extends BaseFragmentActivity {
    private static final String LOGCAT = DonateActivity.class.getSimpleName();

    private final String PURCHASED_SKU = "DonateActivity:Purchased_Sku";
    private final String PURCHASED_TOKEN = "DonateActivity:Purchased_Token";

    // List of valid SKUs
    ArrayList<String> skus = new ArrayList<String>();
    // purchase
    private String purchasedSku = "";
    private String purchasedToken = "";
    // Helper In-app Billing
    private IabHelper mIabHelper;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.donate_activity);

        // Set up SKUs
        if (false && BuildConfig.DEBUG) {
            skus.add("android.test.purchased");
            skus.add("android.test.canceled");
            skus.add("android.test.refunded");
            skus.add("android.test.item_unavailable");
            // my items for test
            skus.add("com.android.money.manager.ex.test.1");
        }
        // add SKU application
        skus.add("android.money.manager.ex.donations.small");

        // set enable return
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // handle clicking on direct donation text
        setUpDirectDonationButton();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!mIabHelper.handleActivityResult(requestCode, resultCode, data))
            super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (mIabHelper != null)
                mIabHelper.dispose();
            mIabHelper = null;
        } catch (Exception e) {
            Log.e(LOGCAT, e.getMessage());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        purchasedSku = savedInstanceState.containsKey(PURCHASED_SKU) ? savedInstanceState.getString(PURCHASED_SKU) : "";
        purchasedToken = savedInstanceState.containsKey(PURCHASED_TOKEN) ? savedInstanceState.getString(PURCHASED_TOKEN) : "";
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(PURCHASED_SKU, purchasedSku);
        outState.putString(PURCHASED_TOKEN, purchasedToken);
    }

    private void setUpDirectDonationButton() {
        final String siteUrl = "http://android.moneymanagerex.org/";

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(siteUrl));
                startActivity(browserIntent);
            }
        };
//        directDonationLink.setOnClickListener(listener);

        // Button
        RobotoButton button = (RobotoButton) findViewById(R.id.donateButton);
        if (button != null) {
            button.setOnClickListener(listener);
        }
    }
}
