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
package com.money.manager.ex.investment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment;
import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.common.AmountInputDialog;
import com.money.manager.ex.common.BaseFragmentActivity;
import com.money.manager.ex.common.events.AmountEnteredEvent;
import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.datalayer.AccountRepository;
import com.money.manager.ex.datalayer.StockRepository;
import com.money.manager.ex.domainmodel.Account;
import com.money.manager.ex.domainmodel.Stock;
import com.money.manager.ex.utils.MyDateTimeUtils;
import com.money.manager.ex.view.RobotoEditTextFontIcon;
import com.money.manager.ex.view.RobotoTextView;
import com.money.manager.ex.view.RobotoTextViewFontIcon;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.joda.time.DateTime;

import java.util.Calendar;

import info.javaperformance.money.MoneyFactory;

/**
 * Edit investment transaction (stock purchase).
 */
public class InvestmentTransactionEditActivity
    extends BaseFragmentActivity {

    public static final String EXTRA_ACCOUNT_ID = "InvestmentTransactionEditActivity:AccountId";
    public static final String DATEPICKER_TAG = "datepicker";
    public static final int ID_NUM_SHARES = 1;
    public static final int ID_PURCHASE_PRICE = 2;
    public static final int ID_COMMISSION = 3;
    public static final int ID_CURRENT_PRICE = 4;

    private Stock mStock;
    private boolean mDirty = false;
    private Account mAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_investment_transaction_edit);

        // this handles OK/Cancel button clicks in the toolbar.
        setToolbarStandardAction(getToolbar());

        // todo: receive the account id (and read currency)

        // todo: change this initialization after adding editing feature.
        mStock = Stock.create();

        initializeForm();
    }

    @Override
    public void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);

        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_edit_investment_transaction, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        onActionCancelClick();
    }

    @Override
    public boolean onActionCancelClick() {
        setResult(Activity.RESULT_CANCELED);
        finish();

        return true;
    }

    @Override
    public boolean onActionDoneClick() {
        if (save()) {
            // set result ok and finish activity
            setResult(RESULT_OK);
            finish();
            return true;
        } else {
            return false;
        }
    }

    private void initializeForm() {
        // load account & currency
        Intent intent = getIntent();
        if (intent != null) {
            int accountId = intent.getIntExtra(EXTRA_ACCOUNT_ID, Constants.NOT_SET);
            if (accountId != Constants.NOT_SET) {
                AccountRepository repository = new AccountRepository(getApplicationContext());
                mAccount = repository.load(accountId);
            }
        }

        // Purchase Date

        final RobotoTextViewFontIcon dateView = (RobotoTextViewFontIcon) this.findViewById(R.id.textViewDate);
        dateView.setText(mStock.getPurchaseDate().toString(Constants.LONG_DATE_PATTERN));
        dateView.setOnClickListener(new View.OnClickListener() {
            CalendarDatePickerDialogFragment.OnDateSetListener listener = new CalendarDatePickerDialogFragment.OnDateSetListener() {
                @Override
                public void onDateSet(CalendarDatePickerDialogFragment dialog, int year, int monthOfYear, int dayOfMonth) {
                    setDirty(true);

                    DateTime dateTime = MyDateTimeUtils.from(year, monthOfYear + 1, dayOfMonth);
                    dateView.setText(dateTime.toString(Constants.LONG_DATE_PATTERN));
                }
            };

            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(mStock.getPurchaseDate().toDate());

                CalendarDatePickerDialogFragment datePicker = new CalendarDatePickerDialogFragment()
                    .setOnDateSetListener(listener)
                    .setPreselectedDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
                    .setThemeDark();
                datePicker.show(getSupportFragmentManager(), DATEPICKER_TAG);
            }
        });

        initNumberOfShares();
        initPurchasePrice();
        initCommission();
        initCurrentPrice();
        showValue();
    }

    public void setDirty(boolean dirty) {
        mDirty = dirty;
    }

    /**
     * Raised after the amount has been entered in the number input dialog.
     */
    @Subscribe
    public void onEvent(AmountEnteredEvent event) {
        int id = Integer.parseInt(event.requestId);

        switch (id) {
            case ID_NUM_SHARES:
                mStock.setNumberOfShares(event.amount.toDouble());
                showNumberOfShares();
                showValue();
                break;
            case ID_PURCHASE_PRICE:
                mStock.setPurchasePrice(event.amount);
                showPurchasePrice();

                if (mStock.getCurrentPrice().isZero()) {
                    mStock.setCurrentPrice(event.amount);
                    showCurrentPrice();
                    // recalculate value
                    showValue();
                }
                break;
            case ID_COMMISSION:
                mStock.setCommission(event.amount);
                showCommission();
                break;
            case ID_CURRENT_PRICE:
                mStock.setCurrentPrice(event.amount);
                showCurrentPrice();
                showValue();
                break;
        }
    }

    private void initCommission() {
        View.OnClickListener onAmountClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AmountInputDialog dialog = AmountInputDialog.getInstance(ID_COMMISSION,
                        mStock.getCommission(),
                        mAccount.getCurrencyId());
                dialog.show(getSupportFragmentManager(), dialog.getClass().getSimpleName());
            }
        };
        RobotoTextView purchasePriceView = (RobotoTextView) this.findViewById(R.id.commissionView);
        purchasePriceView.setOnClickListener(onAmountClick);
        // todo: format the number of shares based on selected locale.
        showCommission();
    }

    private void initCurrentPrice() {
        View.OnClickListener onAmountClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AmountInputDialog dialog = AmountInputDialog.getInstance(ID_CURRENT_PRICE,
                        mStock.getCurrentPrice(),
                        mAccount.getCurrencyId());
                dialog.roundToCurrencyDecimals = false;
                dialog.show(getSupportFragmentManager(), dialog.getClass().getSimpleName());
            }
        };
        RobotoTextView purchasePriceView = (RobotoTextView) this.findViewById(R.id.currentPriceView);
        purchasePriceView.setOnClickListener(onAmountClick);
        // todo: format the number of shares based on selected locale.
        showCurrentPrice();
    }

    private void initNumberOfShares() {
        View.OnClickListener onAmountClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // todo: use currency
                AmountInputDialog dialog = AmountInputDialog.getInstance(ID_NUM_SHARES,
                        MoneyFactory.fromDouble(mStock.getNumberOfShares()));
                dialog.roundToCurrencyDecimals = false;
                dialog.show(getSupportFragmentManager(), dialog.getClass().getSimpleName());
            }
        };
        RobotoTextViewFontIcon numSharesView = (RobotoTextViewFontIcon) this.findViewById(R.id.numSharesView);
        numSharesView.setOnClickListener(onAmountClick);
        // todo: format the number of shares based on selected locale.
        showNumberOfShares();
    }

    private void initPurchasePrice() {
        View.OnClickListener onAmountClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AmountInputDialog dialog = AmountInputDialog.getInstance(ID_PURCHASE_PRICE,
                        mStock.getPurchasePrice(), mAccount.getCurrencyId());
                dialog.roundToCurrencyDecimals = false;
                dialog.show(getSupportFragmentManager(), dialog.getClass().getSimpleName());
            }
        };
        RobotoTextView view = (RobotoTextView) this.findViewById(R.id.purchasePriceView);
        view.setOnClickListener(onAmountClick);
        // todo: format the number of shares based on selected locale.
        showPurchasePrice();
    }

    private void showCommission() {
        RobotoTextView view = (RobotoTextView) this.findViewById(R.id.commissionView);
        view.setText(mStock.getCommission().toString());
    }

    private void showCurrentPrice() {
        RobotoTextView view = (RobotoTextView) this.findViewById(R.id.currentPriceView);
        view.setText(mStock.getCurrentPrice().toString());
    }

    private void showNumberOfShares() {
        RobotoTextViewFontIcon view = (RobotoTextViewFontIcon) this.findViewById(R.id.numSharesView);
        view.setText(mStock.getNumberOfShares().toString());
    }

    private void showPurchasePrice() {
        RobotoTextView view = (RobotoTextView) this.findViewById(R.id.purchasePriceView);
        view.setText(mStock.getPurchasePrice().toString());
    }

    private void showValue() {
        RobotoTextView view = (RobotoTextView) this.findViewById(R.id.valueView);
        view.setText(mStock.getValue().toString());
    }

    private boolean save() {
        if (!validate()) return false;

        boolean result;

        // add missing fields (text) and sanitize text values.

        mStock.setHeldAt(mAccount.getId());

        RobotoEditTextFontIcon nameText = (RobotoEditTextFontIcon) findViewById(R.id.stockNameEdit);
        String stockName = nameText.getText().toString().trim();
        mStock.setName(stockName);

        RobotoEditTextFontIcon symbolText = (RobotoEditTextFontIcon) findViewById(R.id.symbolEdit);
        String symbol = symbolText.getText().toString().trim().replace(" ", "");
        mStock.setSymbol(symbol);

        RobotoEditTextFontIcon notesText = (RobotoEditTextFontIcon) findViewById(R.id.notesEdit);
        mStock.setNotes(notesText.getText().toString());

        // save
        StockRepository repository = new StockRepository(getApplicationContext());
        if (mStock.getId() != null) {
            repository.save(mStock);
        } else {
            repository.insert(mStock);
        }
        result = true;

        return result;
    }

    private boolean validate() {
        ExceptionHandler handler = new ExceptionHandler(this);

        // symbol must not be empty.
        if (StringUtils.isEmpty(mStock.getSymbol())) {
            handler.showMessage(getString(R.string.symbol_required));
            return false;
        }

        // number of shares, price?

        return true;
    }
}
