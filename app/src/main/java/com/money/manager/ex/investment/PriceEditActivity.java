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

package com.money.manager.ex.investment;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.money.manager.ex.Constants;
import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.common.Calculator;
import com.money.manager.ex.common.CalculatorActivity;
import com.money.manager.ex.common.MmxBaseFragmentActivity;
import com.money.manager.ex.core.MenuHelper;
import com.money.manager.ex.core.RequestCodes;
import com.money.manager.ex.datalayer.StockHistoryRepository;
import com.money.manager.ex.datalayer.StockRepository;
import com.money.manager.ex.sync.SyncManager;
import com.money.manager.ex.utils.MmxDate;
import com.money.manager.ex.utils.MmxDateTimeUtils;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.OnClick;
import dagger.Lazy;
import icepick.Icepick;
import info.javaperformance.money.MoneyFactory;
import timber.log.Timber;

public class PriceEditActivity
        extends MmxBaseFragmentActivity {

    public static final String ARG_CURRENCY_ID = "PriceEditActivity:CurrencyId";
    //@State
    protected PriceEditModel model;
    @Inject
    Lazy<MmxDateTimeUtils> dateTimeUtilsLazy;
    private EditPriceViewHolder viewHolder;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_price_edit);

        MmexApplication.getApp().iocComponent.inject(this);

        ButterKnife.bind(this);

        initializeToolbar();

        if (null != savedInstanceState) {
            Icepick.restoreInstanceState(this, savedInstanceState);
        } else {
            initializeModel();
        }

        viewHolder = new EditPriceViewHolder();
        viewHolder.bind(this);

        model.display(this, viewHolder);
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ((Activity.RESULT_CANCELED == resultCode) || null == data) return;

        final String stringExtra;

        if (RequestCodes.AMOUNT == requestCode) {
            stringExtra = data.getStringExtra(CalculatorActivity.RESULT_AMOUNT);
            model.price = MoneyFactory.fromString(stringExtra);
            model.display(this, viewHolder);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        super.onCreateOptionsMenu(menu);

        final MenuHelper menuHelper = new MenuHelper(this, menu);
        menuHelper.addSaveToolbarIcon();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // cancel clicked. Prompt to confirm?
                Timber.d("going back");
                break;
            case MenuHelper.save:
                // save & close
                save();
                setResult(Activity.RESULT_OK);
                finish();
                return onActionDoneClick();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(final Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        Icepick.saveInstanceState(this, savedInstanceState);
    }

    @OnClick(R.id.amountTextView)
    protected void onPriceClick() {
        Calculator.forActivity(this)
                .amount(model.price)
                .roundToCurrency(false)
                .show(RequestCodes.AMOUNT);
    }

    @OnClick(R.id.dateTextView)
    protected void onDateClick() {
        final MmxDate priceDate = model.date;

        final DatePickerDialog.OnDateSetListener listener = (view, year, month, dayOfMonth) -> {
            model.date = new MmxDate(year, month, dayOfMonth);
            model.display(this, viewHolder);
        };

        final DatePickerDialog datePicker = new DatePickerDialog(
                this,
                listener,
                priceDate.getYear(),
                priceDate.getMonthOfYear(),
                priceDate.getDayOfMonth()
        );

        // Customize the DatePickerDialog if needed
        datePicker.show();
    }

    @OnClick(R.id.previousDayButton)
    protected void onPreviousDayClick() {
        model.date = model.date.minusDays(1);

        model.display(this, viewHolder);
    }

    @OnClick(R.id.nextDayButton)
    protected void onNextDayClick() {
        model.date = model.date.plusDays(1);

        model.display(this, viewHolder);
    }

    private void initializeModel() {
        model = new PriceEditModel();

        // get parameters
        readParameters();
    }

    private void initializeToolbar() {
        // Title
        final CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle(getString(R.string.edit_price));

        // Back arrow / cancel.
        setDisplayHomeAsUpEnabled(true);
    }

    private void readParameters() {
        final Intent intent = getIntent();
        if (null == intent) return;

        model.accountId = intent.getIntExtra(EditPriceDialog.ARG_ACCOUNT, Constants.NOT_SET);
        model.symbol = intent.getStringExtra(EditPriceDialog.ARG_SYMBOL);

        final String priceString = intent.getStringExtra(EditPriceDialog.ARG_PRICE);
        model.price = MoneyFactory.fromString(priceString);

        final String dateString = intent.getStringExtra(EditPriceDialog.ARG_DATE);
        model.date = new MmxDate(dateString);

        // currency!
        model.currencyId = intent.getIntExtra(ARG_CURRENCY_ID, Constants.NOT_SET);
    }

    private void save() {
        //update price
        final StockRepository repo = new StockRepository(this);
        repo.updateCurrentPrice(model.symbol, model.price);

        final StockHistoryRepository historyRepository = new StockHistoryRepository(this);
        final boolean result = historyRepository.addStockHistoryRecord(model);
        if (!result) {
            Toast.makeText(this, getString(R.string.error_update_currency_exchange_rate),
                    Toast.LENGTH_SHORT).show();
        }

        new SyncManager(this).dataChanged();
    }
}
