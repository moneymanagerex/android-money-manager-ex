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
import com.money.manager.ex.fragment.BaseFragmentActivity;
import com.money.manager.ex.inapp.util.IabHelper;
import com.money.manager.ex.inapp.util.IabResult;
import com.money.manager.ex.inapp.util.Inventory;
import com.money.manager.ex.inapp.util.Purchase;
import com.money.manager.ex.inapp.util.SkuDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DonateActivity extends BaseFragmentActivity {
    private static final String LOGCAT = DonateActivity.class.getSimpleName();

    private final String PURCHASED_SKU = "DonateActivity:Purchased_Sku";
    private final String PURCHASED_TOKEN = "DonateActivity:Purchased_Token";

    // List of valid SKUs
    ArrayList<String> skus = new ArrayList<String>();
    ArrayList<SkuDetails> skusToBePublished = new ArrayList<SkuDetails>();
    // purchase
    private String purchasedSku = "";
    private String purchasedToken = "";
    // Helper In-app Billing
    private IabHelper mIabHelper;
    private IabHelper.OnIabPurchaseFinishedListener mConsumeFinishedListener;
    private IabHelper.QueryInventoryFinishedListener mQueryInventoryFinishedListener;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.donate_activity);

        // Set up SKUs
        if (1==2 && BuildConfig.DEBUG) {
            skus.add("android.test.purchased");
            skus.add("android.test.canceled");
            skus.add("android.test.refunded");
            skus.add("android.test.item_unavailable");
            // my items for test
            skus.add("com.android.money.manager.ex.test.1");
        }
        // add SKU application
        skus.add("android.money.manager.ex.donations.small");

        final Spinner inAppSpinner = (Spinner) findViewById(R.id.spinnerDonateInApp);
        final Button inAppButton = (Button) findViewById(R.id.buttonDonateInApp);
        inAppButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(final View v) {
                final int selectedInAppAmount = inAppSpinner.getSelectedItemPosition();
                purchasedSku = skus.get(selectedInAppAmount);
                if (BuildConfig.DEBUG)
                    Log.d(DonateActivity.this.getClass().getSimpleName(), "Clicked " + purchasedSku);
                purchasedToken = UUID.randomUUID().toString();
                //BillingController.requestPurchase(DonateActivity.this, purchasedSku, true, null);
                mIabHelper.launchPurchaseFlow(DonateActivity.this, purchasedSku, 1001, mConsumeFinishedListener, purchasedToken);
            }
        });
        // Disabilito il tasto fin che non è pronto
        inAppButton.setEnabled(false);

        mConsumeFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
            @Override
            public void onIabPurchaseFinished(IabResult result, Purchase info) {
                if (result.isSuccess()) {
                    Toast.makeText(DonateActivity.this, R.string.donate_thank_you, Toast.LENGTH_LONG).show();
                    // close activity
                    DonateActivity.this.finish();
                }
            }
        };
        mQueryInventoryFinishedListener = new IabHelper.QueryInventoryFinishedListener() {
            @Override
            public void onQueryInventoryFinished(IabResult result, Inventory inv) {
                if (result.isSuccess()) {
                    for (String sku : skus) {
                        if (inv.hasDetails(sku)) {
                            SkuDetails skuDetails = inv.getSkuDetails(sku);
                            if (!inv.hasPurchase(sku)) {
                                skusToBePublished.add(skuDetails);
                            }
                        }
                    }
                }
                onStartupInApp(result.isSuccess());
            }
        };
        // init IabHelper
        try {
            mIabHelper = new IabHelper(getApplicationContext(), Core.getAppBase64());
            mIabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
                @Override
                public void onIabSetupFinished(IabResult result) {
                    if (result.isSuccess()) {
                        mIabHelper.queryInventoryAsync(true, skus, mQueryInventoryFinishedListener);
                    }
                }
            });
        } catch (Exception e) {
            Log.e(LOGCAT, "In-App Billing startup error");
            onStartupInApp(false);
        }
        // set enable return
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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

    public void onStartupInApp(boolean supported) {
        final TextView inAppStatus = (TextView) findViewById(R.id.textViewInAppStatus);
        if (supported) {
            final List<String> inAppName = new ArrayList<String>();

            for (SkuDetails sku : skusToBePublished) {
                inAppName.add(sku.getDescription() + " " + sku.getPrice());
            }

            Spinner inAppSpinner = (Spinner) findViewById(R.id.spinnerDonateInApp);
            final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, inAppName);
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // Apply the adapter to the spinner
            inAppSpinner.setAdapter(adapter);
            // visibility button
            final Button inAppButton = (Button) findViewById(R.id.buttonDonateInApp);
            inAppButton.setVisibility(inAppName.size() > 0 ? View.VISIBLE : View.GONE);
            inAppButton.setEnabled(inAppName.size() > 0 );
            // status
            inAppStatus.setText(inAppName.size() <= 0 ? Html.fromHtml("<b>" + getString(R.string.donate_in_app_already_donate) + "</b>") : null);
            // hide spinner if release version
            inAppSpinner.setVisibility(inAppName.size() > 1 ? View.VISIBLE : View.GONE);
        } else {
            inAppStatus.setText(R.string.donate_in_app_error);
            inAppStatus.setTextColor(getResources().getColor(R.color.holo_red_dark));
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
}
