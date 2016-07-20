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
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.SpinnerAdapter;

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
import com.money.manager.ex.servicelayer.AccountService;
import com.money.manager.ex.utils.MyDateTimeUtils;
import com.money.manager.ex.utils.SpinnerHelper;
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

    public static final String ARG_ACCOUNT_ID = "InvestmentTransactionEditActivity:AccountId";
    public static final String ARG_STOCK_ID = "InvestmentTransactionEditActivity:StockId";
    public static final String DATEPICKER_TAG = "datepicker";
    public static final int ID_NUM_SHARES = 1;
    public static final int ID_PURCHASE_PRICE = 2;
    public static final int ID_COMMISSION = 3;
    public static final int ID_CURRENT_PRICE = 4;

    private boolean mDirty = false;
    private Account mAccount;
    private Stock mStock;
    private InvestmentTransactionViewHolder mViewHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_investment_transaction_edit);

        // this handles OK/Cancel button clicks in the toolbar.
        showStandardToolbarActions();

        // load account & currency
        Intent intent = getIntent();
        if (intent != null) {
            int accountId = intent.getIntExtra(ARG_ACCOUNT_ID, Constants.NOT_SET);
            if (accountId != Constants.NOT_SET) {
                AccountRepository repository = new AccountRepository(this);
                mAccount = repository.load(accountId);
            }

            int stockId = intent.getIntExtra(ARG_STOCK_ID, Constants.NOT_SET);
            if (stockId != Constants.NOT_SET) {
                StockRepository repo = new StockRepository(this);
                mStock = repo.load(stockId);
            } else {
                mStock = Stock.create();
                if (mAccount != null) {
                    mStock.setHeldAt(mAccount.getId());
                }
            }
        }

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
//        int id = item.getItemId();

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

    // Private

    private void initializeForm() {
        View rootView = this.findViewById(R.id.content);
        mViewHolder = new InvestmentTransactionViewHolder(rootView);

        initDateControl(mViewHolder);
        initAccountSelectors(mViewHolder);
        initNumberOfShares(mViewHolder);
        initPurchasePrice();
        initCommission();
        initCurrentPrice();

        displayStock(mStock, mViewHolder);
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

    /*
        Private
     */

    private void collectData() {
        String stockName = mViewHolder.stockNameEdit.getText().toString().trim();
        mStock.setName(stockName);

        // Symbols are always uppercase.
        String symbol = mViewHolder.symbolEdit.getText().toString()
            .trim().replace(" ", "").toUpperCase();
        mStock.setSymbol(symbol);

        mStock.setNotes(mViewHolder.notesEdit.getText().toString());
    }

    private void displayStock(Stock stock, InvestmentTransactionViewHolder viewHolder) {
        // Date
        viewHolder.dateView.setText(stock.getPurchaseDate().toString(Constants.LONG_DATE_PATTERN));

        // Account.
        Cursor cursor = ((CursorAdapter) viewHolder.accountSpinner.getAdapter()).getCursor();
        int accountIndex = SpinnerHelper.getPosition(mAccount.getName(), Account.ACCOUNTNAME, cursor);
        if (accountIndex >= 0) {
            viewHolder.accountSpinner.setSelection(accountIndex, true);
        }

        viewHolder.stockNameEdit.setText(stock.getName());
        viewHolder.symbolEdit.setText(stock.getSymbol());

        showNumberOfShares();
        showPurchasePrice();
        viewHolder.notesEdit.setText(stock.getNotes());
        showCommission();
        showCurrentPrice();
        showValue();
    }

    private void setDate(DateTime dateTime) {
        setDirty(true);

        mStock.setPurchaseDate(dateTime);

        showDate(dateTime);
    }

    /**
     * Initialize account selectors.
     */
    private void initAccountSelectors(final InvestmentTransactionViewHolder viewHolder) {
        Context context = this;

        // Account list as the data source to populate the drop-downs.

        AccountService accountService = new AccountService(context);
        accountService.loadInvestmentAccountsToSpinner(viewHolder.accountSpinner, false);

//        AccountRepository accountRepository = new AccountRepository(context);
        final Integer accountId = mStock.getHeldAt();
//        if (accountId != null) {
//            addMissingAccountToSelectors(accountRepository, accountId);
//        }

        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SpinnerAdapter adapter = viewHolder.accountSpinner.getAdapter();
                Cursor cursor = (Cursor) adapter.getItem(position);
                Account account = Account.from(cursor);

                if (!account.getId().equals(accountId)) {
                    setDirty(true);
                    mStock.setHeldAt(account.getId());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };
        viewHolder.accountSpinner.setOnItemSelectedListener(listener);
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
    }

    private void initCurrentPrice() {
        View.OnClickListener onAmountClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AmountInputDialog dialog = AmountInputDialog.getInstance(ID_CURRENT_PRICE,
                    mStock.getCurrentPrice(), mAccount.getCurrencyId(), false);
                dialog.show(getSupportFragmentManager(), dialog.getClass().getSimpleName());
            }
        };
        RobotoTextView purchasePriceView = (RobotoTextView) this.findViewById(R.id.currentPriceView);
        purchasePriceView.setOnClickListener(onAmountClick);
    }

    private void initDateControl(final InvestmentTransactionViewHolder viewHolder) {
        // Purchase Date

        viewHolder.dateView.setOnClickListener(new View.OnClickListener() {
            CalendarDatePickerDialogFragment.OnDateSetListener listener = new CalendarDatePickerDialogFragment.OnDateSetListener() {
                @Override
                public void onDateSet(CalendarDatePickerDialogFragment dialog, int year, int monthOfYear, int dayOfMonth) {
                    setDirty(true);

                    DateTime dateTime = MyDateTimeUtils.from(year, monthOfYear + 1, dayOfMonth);
                    viewHolder.dateView.setText(dateTime.toString(Constants.LONG_DATE_PATTERN));
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
        // prev/next day
        viewHolder.previousDayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DateTime dateTime = mStock.getPurchaseDate().minusDays(1);
                setDate(dateTime);
            }
        });
        viewHolder.nextDayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DateTime dateTime = mStock.getPurchaseDate().plusDays(1);
                setDate(dateTime);
            }
        });
    }

    private void initNumberOfShares(InvestmentTransactionViewHolder viewHolder) {
        View.OnClickListener onAmountClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AmountInputDialog dialog = AmountInputDialog.getInstance(ID_NUM_SHARES,
                    MoneyFactory.fromDouble(mStock.getNumberOfShares()), null, false);
                dialog.show(getSupportFragmentManager(), dialog.getClass().getSimpleName());
            }
        };

        if (viewHolder.numSharesView == null) return;

        viewHolder.numSharesView.setOnClickListener(onAmountClick);
    }

    private void initPurchasePrice() {
        View.OnClickListener onAmountClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AmountInputDialog dialog = AmountInputDialog.getInstance(ID_PURCHASE_PRICE,
                        mStock.getPurchasePrice(), mAccount.getCurrencyId(), false);
                dialog.show(getSupportFragmentManager(), dialog.getClass().getSimpleName());
            }
        };
        RobotoTextView view = (RobotoTextView) this.findViewById(R.id.purchasePriceView);
        view.setOnClickListener(onAmountClick);
    }

    private void showCommission() {
        RobotoTextView view = (RobotoTextView) this.findViewById(R.id.commissionView);
        if (view == null) return;

        // todo: format the number of shares based on selected locale.
        view.setText(mStock.getCommission().toString());
    }

    private void showCurrentPrice() {
        RobotoTextView view = (RobotoTextView) this.findViewById(R.id.currentPriceView);
        // todo: format the number of shares based on selected locale.
        view.setText(mStock.getCurrentPrice().toString());
    }

    private void showNumberOfShares() {
        RobotoTextViewFontIcon view = (RobotoTextViewFontIcon) this.findViewById(R.id.numSharesView);
        if (view == null) return;

        // todo: format the number of shares based on selected locale?

        view.setText(mStock.getNumberOfShares().toString());
    }

    private void showPurchasePrice() {
        RobotoTextView view = (RobotoTextView) this.findViewById(R.id.purchasePriceView);
        // todo: format the number of shares based on selected locale.
        view.setText(mStock.getPurchasePrice().toString());
    }

    private void showValue() {
        RobotoTextView view = (RobotoTextView) this.findViewById(R.id.valueView);
        //mViewHolder.
        view.setText(mStock.getValue().toString());
    }

    private boolean save() {
        collectData();

        if (!validate()) return false;

        // save
        StockRepository repository = new StockRepository(getApplicationContext());
        if (mStock.getId() != null) {
            repository.save(mStock);
        } else {
            repository.insert(mStock);
        }

        return true;
    }

    private void showDate(DateTime date) {
        mViewHolder.dateView.setText(date.toString(Constants.LONG_DATE_PATTERN));
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
