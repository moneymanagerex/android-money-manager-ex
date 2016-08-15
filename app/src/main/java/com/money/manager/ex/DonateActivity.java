/*
 * Copyright (C) 2012-2016 The Android Money Manager Ex Project Team
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
import android.view.MenuItem;

import com.money.manager.ex.common.BaseFragmentActivity;
//import com.money.manager.ex.inapp.util.IabHelper;
import com.money.manager.ex.common.WebViewActivity;
import com.money.manager.ex.log.ExceptionHandler;
import com.money.manager.ex.core.HttpMethods;

import java.util.HashMap;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class DonateActivity
    extends BaseFragmentActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.donate_activity);
        ButterKnife.bind(this);

        // set enable return
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
    }

    // UI Event Handlers

    @OnClick(R.id.donateButton)
    protected void onDirectDonationClick() {
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

    @OnClick(R.id.homepageButton)
    protected void onHomepageClick() {
        String siteUrl = "http://android.moneymanagerex.org/";

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(siteUrl));

        try {
            startActivity(browserIntent);
        } catch (Exception e) {
            ExceptionHandler handler = new ExceptionHandler(this);
            handler.e(e, "opening the homepage in a browser");
        }
    }
}
