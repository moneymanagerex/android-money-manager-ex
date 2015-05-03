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

package com.money.manager.ex.currency;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.money.manager.ex.R;
import com.money.manager.ex.database.TableCurrencyFormats;
import com.money.manager.ex.fragment.BaseFragmentActivity;


public class CurrencyChartActivity extends BaseFragmentActivity {

    public static final String BASE_CURRENCY_SYMBOL = "CurrencyChartActivity::BaseCurrencySymbol";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency_chart);

        String currencySymbol = null;
        String baseCurrencySymbol = null;
        // get the currency information from the intent.
        Intent intent = getIntent();
        if(intent != null) {
            currencySymbol = intent.getStringExtra(TableCurrencyFormats.CURRENCY_SYMBOL);
            baseCurrencySymbol = intent.getStringExtra(BASE_CURRENCY_SYMBOL);
        }

        // load currency chart.
        loadCurrencyChart(currencySymbol, baseCurrencySymbol);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_currency_chart, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadCurrencyChart(String currencySymbol, String baseCurrencySymbol) {
        if(currencySymbol == null) return;

        // ref: http://stackoverflow.com/questions/4678296/yahoo-historical-currency-rates-api
        // Yahoo API reference:
        // https://code.google.com/p/yahoo-finance-managed/wiki/miscapiImageDownload

        String url = String.format("http://chart.finance.yahoo.com/z?s=%s%s=x&t=5d&z=m",
                baseCurrencySymbol, currencySymbol);

        ImageView imageView = (ImageView) findViewById(R.id.imageChart);

        new ImageLoadTask(url, imageView).execute();
    }
}
