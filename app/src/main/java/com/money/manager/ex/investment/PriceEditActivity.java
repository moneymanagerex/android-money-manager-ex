/*
 * Copyright (C) 2012-2017 The Android Money Manager Ex Project Team
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

package com.money.manager.ex.investment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.view.Menu;
import android.view.MenuItem;

import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.common.Calculator;
import com.money.manager.ex.common.CalculatorActivity;
import com.money.manager.ex.common.MmxBaseFragmentActivity;
import com.money.manager.ex.core.IntentFactory;
import com.money.manager.ex.core.MenuHelper;
import com.money.manager.ex.core.RequestCodes;
import com.money.manager.ex.utils.MmxDate;

import butterknife.ButterKnife;
import butterknife.OnClick;
import icepick.State;
import info.javaperformance.money.MoneyFactory;
import timber.log.Timber;

public class PriceEditActivity
    extends MmxBaseFragmentActivity {

    public static final String ARG_CURRENCY_ID = "PriceEditActivity:CurrencyId";

    //@State
    protected PriceEditModel model;
    private EditPriceViewHolder viewHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_price_edit);

        ButterKnife.bind(this);

        initializeToolbar();

        initializeModel();

        viewHolder = new EditPriceViewHolder();
        viewHolder.bind(this);

        model.display(this, viewHolder);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ((resultCode == Activity.RESULT_CANCELED) || data == null) return;

        String stringExtra;

        switch (requestCode) {
            case RequestCodes.AMOUNT:
                stringExtra = data.getStringExtra(CalculatorActivity.RESULT_AMOUNT);
                model.price = MoneyFactory.fromString(stringExtra);
                model.display(this, viewHolder);
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuHelper menuHelper = new MenuHelper(this, menu);
        menuHelper.addSaveToolbarIcon();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // cancel clicked. Prompt to confirm?
                Timber.d("going back");
                break;
            case MenuHelper.save:
                return onActionDoneClick();
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.amountTextView)
    protected void onPriceClick() {
        Calculator.forActivity(this)
            .amount(model.price)
            .show(RequestCodes.AMOUNT);
    }

    @OnClick(R.id.dateTextView)
    protected void onDateClick() {
        // todo: show date picker.
    }

    private void initializeModel() {
        model = new PriceEditModel();

        // get parameters
        readParameters();
    }

    private void initializeToolbar() {
        // Title
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle(getString(R.string.edit_price));

        // Back arrow / cancel.
        setDisplayHomeAsUpEnabled(true);
    }

    private void readParameters() {
        Intent intent = getIntent();
        if (intent == null) return;

        model.accountId = intent.getIntExtra(EditPriceDialog.ARG_ACCOUNT, Constants.NOT_SET);
        model.symbol = intent.getStringExtra(EditPriceDialog.ARG_SYMBOL);

        String priceString = intent.getStringExtra(EditPriceDialog.ARG_PRICE);
        model.price = MoneyFactory.fromString(priceString);

        String dateString = intent.getStringExtra(EditPriceDialog.ARG_DATE);
        model.date = new MmxDate(dateString);

        // currency!
        model.currencyId = intent.getIntExtra(ARG_CURRENCY_ID, Constants.NOT_SET);
    }
}
