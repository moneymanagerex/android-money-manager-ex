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
package com.money.manager.ex;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.money.manager.ex.common.BaseFragmentActivity;
//import com.money.manager.ex.inapp.util.IabHelper;
import com.money.manager.ex.common.WebViewActivity;
import com.money.manager.ex.core.HttpMethods;
import com.money.manager.ex.view.RobotoButton;

import java.util.ArrayList;
import java.util.HashMap;

public class DonateActivity
        extends BaseFragmentActivity {

//    private final String PURCHASED_SKU = "DonateActivity:Purchased_Sku";
//    private final String PURCHASED_TOKEN = "DonateActivity:Purchased_Token";

    // List of valid SKUs
//    ArrayList<String> skus = new ArrayList<String>();
    // purchase
//    private String purchasedSku = "";
//    private String purchasedToken = "";
    // Helper In-app Billing
//    private IabHelper mIabHelper;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.donate_activity);

        // Set up SKUs
//        if (false && BuildConfig.DEBUG) {
//            skus.add("android.test.purchased");
//            skus.add("android.test.canceled");
//            skus.add("android.test.refunded");
//            skus.add("android.test.item_unavailable");
//            // my items for test
//            skus.add("com.android.money.manager.ex.test.1");
//        }
//        // add SKU application
//        skus.add("android.money.manager.ex.donations.small");

        // set enable return
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // handle clicking on direct donation text
        setupDirectDonationButton();

        setupHomepageButton();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (!mIabHelper.handleActivityResult(requestCode, resultCode, data))
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
//        try {
//            if (mIabHelper != null)
//                mIabHelper.dispose();
//            mIabHelper = null;
//        } catch (Exception e) {
//            Log.e(LOGCAT, e.getMessage());
//        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
//        purchasedSku = savedInstanceState.containsKey(PURCHASED_SKU) ? savedInstanceState.getString(PURCHASED_SKU) : "";
//        purchasedToken = savedInstanceState.containsKey(PURCHASED_TOKEN) ? savedInstanceState.getString(PURCHASED_TOKEN) : "";
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
//        outState.putString(PURCHASED_SKU, purchasedSku);
//        outState.putString(PURCHASED_TOKEN, purchasedToken);
    }

    private void setupDirectDonationButton() {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // paypal.me/mmexAndroid

                // parameters
                HashMap<String, String> values = new HashMap<>();
                values.put("cmd", "_s-xclick");
                values.put("hosted_button_id", "5U7RXC25C9UES");
                values.put("lc", "US");

                // Start web view and open donation form.
                Intent intent = new Intent(getApplicationContext(), WebViewActivity.class);
                intent.putExtra(WebViewActivity.URL, "https://www.paypal.com/cgi-bin/webscr");
                intent.putExtra(WebViewActivity.METHOD, HttpMethods.POST);
                intent.putExtra(WebViewActivity.POST_VALUES, values);

                intent.setAction(Intent.ACTION_DEFAULT);
                startActivity(intent);
            }
        };

        RobotoButton button = (RobotoButton) findViewById(R.id.donateButton);
        if (button != null) {
            button.setOnClickListener(listener);
        }
    }

    private void setupHomepageButton() {
        final String siteUrl = "http://android.moneymanagerex.org/";

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(siteUrl));
                startActivity(browserIntent);
            }
        };
//        directDonationLink.setOnClickListener(listener);

        // Homepage Button
        RobotoButton button = (RobotoButton) findViewById(R.id.homepageButton);
        if (button != null) {
            button.setOnClickListener(listener);
        }
    }
}
