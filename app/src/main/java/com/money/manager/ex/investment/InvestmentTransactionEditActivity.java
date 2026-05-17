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
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.money.manager.ex.Constants;
import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.common.Calculator;
import com.money.manager.ex.common.CategoryListActivity;
import com.money.manager.ex.common.MmxBaseFragmentActivity;
import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.core.TransactionStatuses;
import com.money.manager.ex.core.MenuHelper;
import com.money.manager.ex.core.RequestCodes;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.datalayer.AccountRepository;
import com.money.manager.ex.datalayer.AccountTransactionRepository;
import com.money.manager.ex.datalayer.PayeeRepository;
import com.money.manager.ex.datalayer.Select;
import com.money.manager.ex.datalayer.ShareInfoRepository;
import com.money.manager.ex.datalayer.StockHistoryRepository;
import com.money.manager.ex.datalayer.StockRepository;
import com.money.manager.ex.datalayer.TransactionLinkRepository;
import com.money.manager.ex.domainmodel.Account;
import com.money.manager.ex.domainmodel.AccountTransaction;
import com.money.manager.ex.domainmodel.Payee;
import com.money.manager.ex.domainmodel.ShareInfo;
import com.money.manager.ex.domainmodel.Stock;
import com.money.manager.ex.domainmodel.StockHistory;
import com.money.manager.ex.domainmodel.TransactionLink;
import com.money.manager.ex.database.ITransactionEntity;
import com.money.manager.ex.servicelayer.AccountService;
import com.money.manager.ex.utils.MmxDate;
import com.money.manager.ex.utils.MmxDateTimeUtils;
import com.money.manager.ex.view.RobotoTextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import com.squareup.sqlbrite3.BriteDatabase;
import com.money.manager.ex.transactions.EditTransactionCommonFunctions;
import com.money.manager.ex.domainmodel.SplitCategory;

import dagger.Lazy;
import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

/**
 * Edit investment transaction (stock purchase).
 */
public class InvestmentTransactionEditActivity
    extends MmxBaseFragmentActivity {

    public static final String ARG_ACCOUNT_ID = "InvestmentTransactionEditActivity:AccountId";
    public static final String ARG_STOCK_ID = "InvestmentTransactionEditActivity:StockId";
    public static final String ARG_TRANS_ID = "InvestmentTransactionEditActivity:TransId";
    public static final String ARG_NEW_SHARE_TRANSACTION = "InvestmentTransactionEditActivity:NewShareTransaction";
    public static final String ARG_INITIAL_TRANSACTION_TYPE = "InvestmentTransactionEditActivity:InitialTransactionType";

    public static final int REQUEST_NUM_SHARES = 1;
    public static final int REQUEST_PURCHASE_PRICE = 2;
    public static final int REQUEST_COMMISSION = 3;
    public static final int REQUEST_CURRENT_PRICE = 4;

    @Inject Lazy<MmxDateTimeUtils> dateTimeUtilsLazy;
    @Inject BriteDatabase database;

    private EditTransactionCommonFunctions mCommon;

    private Account mAccount;
    private Stock mStock;
    private AccountTransaction mLinkedTransaction;
    private InvestmentTransactionViewHolder mViewHolder;
    private boolean mIsShareTransactionMode;
    private boolean mIsStockOverviewMode;
    private long mCategoryId = Constants.NOT_SET;
    private String mCategoryName = "";
    private String mInitialTransactionType = null;
    private Date mOriginalStockDate = null;
    private ShareInfo mShareInfo = null;

    private final ArrayList<TransactionTypes> mTransactionTypes = new ArrayList<>();
    private final ArrayList<String> mStatusCodes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_investment_transaction_edit);

        MmexApplication.getApp().iocComponent.inject(this);

        setDisplayHomeAsUpEnabled(true);

        // load account & currency
        Intent intent = getIntent();
        if (intent != null) {
            long accountId = intent.getLongExtra(ARG_ACCOUNT_ID, Constants.NOT_SET);
            if (accountId != Constants.NOT_SET) {
                AccountRepository repository = new AccountRepository(this);
                mAccount = repository.load(accountId);
            }

            long stockId = intent.getLongExtra(ARG_STOCK_ID, Constants.NOT_SET);
            if (stockId != Constants.NOT_SET) {
                StockRepository repo = new StockRepository(this);
                mStock = repo.load(stockId);
            } else {
                mStock = Stock.create();
                if (mAccount != null) {
                    mStock.setHeldAt(mAccount.getId());
                }
            }

            boolean newShareTransaction = intent.getBooleanExtra(ARG_NEW_SHARE_TRANSACTION, false);
            mInitialTransactionType = intent.getStringExtra(ARG_INITIAL_TRANSACTION_TYPE);

            long transId = intent.getLongExtra(ARG_TRANS_ID, Constants.NOT_SET);
            if (transId != Constants.NOT_SET) {
                mLinkedTransaction = new AccountTransactionRepository(this).load(transId);
            }
            // Note: we intentionally do NOT auto-load linked transaction for plain portfolio
            // stock clicks. That path shows the stock-overview mode (read-only position view).

            // For an explicit cash-ledger transaction, load per-transaction share data from
            // ShareInfo and override the stock's cumulative position values for display.
            if (mLinkedTransaction != null && mLinkedTransaction.hasId()) {
                mShareInfo = new ShareInfoRepository(this).loadByTransactionId(mLinkedTransaction.getId());
                Date txDate = mLinkedTransaction.getDate();
                if (txDate != null) {
                    mStock.setPurchaseDate(txDate);
                }
                if (mShareInfo != null) {
                    double shares = mShareInfo.getShareNumber() != null ? Math.abs(mShareInfo.getShareNumber()) : 0.0;
                    mStock.setNumberOfShares(shares);
                    double sharePrice = mShareInfo.getSharePrice() != null ? mShareInfo.getSharePrice() : 0.0;
                    mStock.setPurchasePrice(MoneyFactory.fromDouble(sharePrice));
                    double shareComm = mShareInfo.getShareCommission() != null ? mShareInfo.getShareCommission() : 0.0;
                    mStock.setCommission(MoneyFactory.fromDouble(shareComm));
                }
            }
            if (newShareTransaction) {
                mLinkedTransaction = AccountTransaction.create();
                if (mAccount != null) {
                    mLinkedTransaction.setAccountId(mAccount.getId());
                }
                Date today = new MmxDate().toDate();
                mLinkedTransaction.setDate(today);
                if (mInitialTransactionType != null) {
                    try {
                        mLinkedTransaction.setTransactionType(TransactionTypes.valueOf(mInitialTransactionType));
                    } catch (IllegalArgumentException ignored) {
                    }
                }
                // For an existing stock, preserve its original purchase date and show today
                if (mStock.getId() != null) {
                    mOriginalStockDate = mStock.getPurchaseDate();
                    mStock.setPurchaseDate(today);
                }
                // Start a new trade with blank quantities
                mStock.setNumberOfShares(0.0);
                mStock.setPurchasePrice(MoneyFactory.fromDouble(0));
                mStock.setCommission(MoneyFactory.fromDouble(0));
            }

            mIsShareTransactionMode = mLinkedTransaction != null;
            mIsStockOverviewMode = !mIsShareTransactionMode && mStock.getId() != null;
        }

        initializeForm();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Returning from Buy/Sell child activity should refresh overview figures
        // (shares, prices, value) from persisted STOCK_V1 data.
        if (!mIsStockOverviewMode || mStock == null || mStock.getId() == null || mViewHolder == null) {
            return;
        }

        Stock latest = new StockRepository(this).load(mStock.getId());
        if (latest == null) {
            return;
        }

        mStock = latest;

        if (mAccount != null && mStock.getHeldAt() != mAccount.getId()) {
            Account account = new AccountRepository(this).load(mStock.getHeldAt());
            if (account != null) {
                mAccount = account;
            }
        }

        displayStock(mStock, mViewHolder);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (mCommon != null) {
            mCommon.onActivityResult(requestCode, resultCode, data);
        }

        if (resultCode == Activity.RESULT_CANCELED || data == null) return;

        Money amount = Calculator.getAmountFromResult(data);

        switch (requestCode) {
            case REQUEST_NUM_SHARES:
                mStock.setNumberOfShares(amount.toDouble());
                showNumberOfShares();
                if (mIsShareTransactionMode) {
                    showTotalPrice();
                } else {
                    showValue();
                }
                break;

            case REQUEST_PURCHASE_PRICE:
                mStock.setPurchasePrice(amount);
                showPurchasePrice();
                if (mIsShareTransactionMode) {
                    showTotalPrice();
                } else {
                    if (mStock.getCurrentPrice().isZero()) {
                        mStock.setCurrentPrice(amount);
                        showCurrentPrice();
                    }
                    showValue();
                }
                break;

            case REQUEST_COMMISSION:
                mStock.setCommission(amount);
                showCommission();
                if (mIsShareTransactionMode) {
                    showTotalPrice();
                }
                break;

            case REQUEST_CURRENT_PRICE:
                mStock.setCurrentPrice(amount);
                showCurrentPrice();
                showValue();
                break;

            case RequestCodes.CATEGORY:
                if (mCommon != null) {
                    long selectedId = mLinkedTransaction.getCategoryId() == null ? Constants.NOT_SET : mLinkedTransaction.getCategoryId();
                    mCategoryId = selectedId;
                    mCategoryName = mCommon.categoryName;
                    displayCategoryName();
                } else {
                    long selectedId = data.getLongExtra(CategoryListActivity.INTENT_RESULT_CATEGID, Constants.NOT_SET);
                    if (selectedId != Constants.NOT_SET) {
                        mCategoryId = selectedId;
                        mCategoryName = data.getStringExtra(CategoryListActivity.INTENT_RESULT_CATEGNAME);
                        displayCategoryName();
                    }
                }
                break;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        new MenuHelper(this, menu).addSaveToolbarIcon();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically e clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        long id = item.getItemId();

        if (id == MenuHelper.save) {
            return onActionDoneClick();
        }

        return super.onOptionsItemSelected(item);
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

    public void setDirty(boolean dirty) {
    }

    private void onNumSharesClick() {
        Money amount = MoneyFactory.fromDouble(mStock.getNumberOfShares());

        Calculator.forActivity(this)
                .amount(amount)
                .roundToCurrency(false)
                .show(REQUEST_NUM_SHARES);
    }

    private void onPurchasePriceClick() {
        if (mAccount == null) return;

        Calculator.forActivity(this)
                .roundToCurrency(false)
                .amount(mStock.getPurchasePrice())
                .currency(mAccount.getCurrencyId())
                .show(REQUEST_PURCHASE_PRICE);
    }

    private void onCommissionClick() {
        if (mAccount == null) return;

        Calculator.forActivity(this)
                .amount(mStock.getCommission())
                .currency(mAccount.getCurrencyId())
                .show(REQUEST_COMMISSION);
    }

    private void onCurrentPriceClick() {
        if (mAccount == null) return;
        Calculator.forActivity(this)
                .currency(mAccount.getCurrencyId())
                .amount(mStock.getCurrentPrice())
                .show(REQUEST_CURRENT_PRICE);
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

        if (mLinkedTransaction != null) {
            mLinkedTransaction.setNotes(mViewHolder.notesEdit.getText().toString());
            mLinkedTransaction.setTransactionType(mTransactionTypes.get(mViewHolder.transactionTypeSpinner.getSelectedItemPosition()));
            mLinkedTransaction.setStatus(mStatusCodes.get(mViewHolder.statusSpinner.getSelectedItemPosition()));
            if (mCommon != null) {
                // mCommon updates transactionEntity directly via its controls
                // ensure local category id reflects common functions
                mCategoryId = mLinkedTransaction.getCategoryId() == null ? Constants.NOT_SET : mLinkedTransaction.getCategoryId();
                mLinkedTransaction.setCategoryId(mCategoryId);
            } else {
                // No shared controls available; keep existing payee and set category from UI state
                mLinkedTransaction.setCategoryId(mCategoryId);
            }
            Long accountId = mLinkedTransaction.getAccountId();
            if (accountId == null || accountId == Constants.NOT_SET) {
                accountId = mStock.getHeldAt();
            }
            mLinkedTransaction.setToAccountId(isTransferTransaction()
                ? accountId
                : Constants.NOT_SET);
        } else {
            mStock.setNotes(mViewHolder.notesEdit.getText().toString());
        }
    }

    private void displayStock(Stock stock, InvestmentTransactionViewHolder viewHolder) {
        if (mAccount == null) return;

        // Date (use user-configured format with weekday prefix to match normal transactions)
        String userPattern = dateTimeUtilsLazy.get().getUserDatePattern(InvestmentTransactionEditActivity.this);
        String format = "EEE, " + userPattern;
        String dateDisplay = dateTimeUtilsLazy.get().format(stock.getPurchaseDate(), format);
        viewHolder.dateView.setText(dateDisplay);

        // Account.
        SpinnerAdapter adapter = viewHolder.accountSpinner.getAdapter();
        if (adapter != null) {
            ArrayAdapter<Account> accountAdapter = (ArrayAdapter<Account>) adapter;
            for (int i = 0; i < accountAdapter.getCount(); i++) {
                Account acc = accountAdapter.getItem(i);
                if (acc != null && acc.getId().equals(mAccount.getId())) {
                    viewHolder.accountSpinner.setSelection(i, true);
                    break;
                }
            }
        }

        viewHolder.stockNameEdit.setText(stock.getName());
        viewHolder.symbolEdit.setText(stock.getSymbol());

        showNumberOfShares();
        showPurchasePrice();
        if (mLinkedTransaction != null) {
            viewHolder.notesEdit.setText(mLinkedTransaction.getNotes());
            selectTransactionType(mLinkedTransaction.getTransactionType());
            selectStatus(mLinkedTransaction.getStatus());
            if (mCommon != null) {
                mCommon.loadPayeeName(mLinkedTransaction.getPayeeId() == null ? Constants.NOT_SET : mLinkedTransaction.getPayeeId());
                mCommon.showPayeeName();
                mCategoryId = mLinkedTransaction.getCategoryId() == null ? Constants.NOT_SET : mLinkedTransaction.getCategoryId();
                loadCategoryName(mCategoryId);
                mCommon.categoryName = mCategoryName;
                mCommon.displayCategoryName();
            } else {
                mCategoryId = mLinkedTransaction.getCategoryId() == null ? Constants.NOT_SET : mLinkedTransaction.getCategoryId();
                loadCategoryName(mCategoryId);
            }
            if (viewHolder.transferCheckBox != null) {
                viewHolder.transferCheckBox.setVisibility(View.VISIBLE);
                Long accountId = mLinkedTransaction.getAccountId();
                if (accountId == null || accountId == Constants.NOT_SET) {
                    accountId = mStock.getHeldAt();
                }
                Long toAccountId = mLinkedTransaction.getToAccountId();
                viewHolder.transferCheckBox.setChecked(toAccountId != null && toAccountId.equals(accountId));
            }
        } else {
            viewHolder.notesEdit.setText(stock.getNotes());
            if (viewHolder.transferCheckBox != null) {
                viewHolder.transferCheckBox.setVisibility(View.GONE);
            }
        }
        displayCategoryName();
        showCommission();
        if (mIsShareTransactionMode) {
            showTotalPrice();
        } else {
            showCurrentPrice();
            showValue();
        }
    }

    private void initializeForm() {
        mViewHolder = new InvestmentTransactionViewHolder(this);

        initDateControl(mViewHolder);
        initAccountSelectors(mViewHolder);

        initTransactionDetailsControls(mViewHolder);
        updateShareTransactionSectionVisibility();

        displayStock(mStock, mViewHolder);

        // Icons
        UIHelper ui = new UIHelper(this);
        mViewHolder.symbolEdit.setCompoundDrawablesWithIntrinsicBounds(ui.getIcon(GoogleMaterial.Icon.gmd_account_balance), null, null, null);
        mViewHolder.notesEdit.setCompoundDrawablesWithIntrinsicBounds(ui.getIcon(GoogleMaterial.Icon.gmd_content_paste), null, null, null);
        mViewHolder.numSharesView.setCompoundDrawablesWithIntrinsicBounds(ui.getIcon(FontAwesome.Icon.faw_hashtag), null, null, null);

        mViewHolder.numSharesView.setOnClickListener(view -> {
            onNumSharesClick();
        });

        mViewHolder.purchasePriceView.setOnClickListener(view -> {
            onPurchasePriceClick();
        });

        mViewHolder.commissionView.setOnClickListener(view -> {
            onCommissionClick();
        });

        mViewHolder.currentPriceView.setOnClickListener(view -> {
            onCurrentPriceClick();
        });

        mViewHolder.buyButton.setOnClickListener(view -> {
            openShareTransaction(TransactionTypes.Withdrawal);
        });

        mViewHolder.sellButton.setOnClickListener(view -> {
            openShareTransaction(TransactionTypes.Deposit);
        });

        if (mViewHolder.transferCheckBox != null) {
            mViewHolder.transferCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    setDirty(true);
                }
            });
        }

        // Hide buttons by default (only show in stock overview mode)
        mViewHolder.buyButton.setVisibility(View.GONE);
        mViewHolder.sellButton.setVisibility(View.GONE);

        if (mIsStockOverviewMode) {
            applyStockOverviewMode();
        }
    }

    private void applyStockOverviewMode() {
        mViewHolder.dateView.setClickable(true);
        mViewHolder.previousDayButton.setVisibility(View.VISIBLE);
        mViewHolder.nextDayButton.setVisibility(View.VISIBLE);

        // Show Buy/Sell buttons in stock overview mode
        mViewHolder.buyButton.setVisibility(View.VISIBLE);
        mViewHolder.sellButton.setVisibility(View.VISIBLE);

        // Account spinner: read-only
        mViewHolder.accountSpinner.setEnabled(false);

        // Shares: read-only (remove tap-to-edit)
        mViewHolder.numSharesView.setOnClickListener(null);
        mViewHolder.numSharesView.setClickable(false);

        // Purchase price: read-only
        mViewHolder.purchasePriceView.setOnClickListener(null);
        mViewHolder.purchasePriceView.setClickable(false);
        // Hide calculator drawable
        mViewHolder.purchasePriceView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

        // Commission: read-only
        mViewHolder.commissionView.setOnClickListener(null);
        mViewHolder.commissionView.setClickable(false);
        mViewHolder.commissionView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

        // currentPriceView stays clickable (user can update current price)
        // stockNameEdit and symbolEdit stay editable
    }

    /**
     * Initialize account selectors.
     */
    private void initAccountSelectors(final InvestmentTransactionViewHolder viewHolder) {
        Context context = this;
        // Account list as the data source to populate the drop-downs.

        AccountService accountService = new AccountService(context);
        accountService.loadInvestmentAccountsToSpinner(viewHolder.accountSpinner, false);

        final Long accountId = mStock.getHeldAt();

        viewHolder.accountSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Account selected = (Account) parent.getItemAtPosition(position);

                if (!selected.getId().equals(accountId)) {
                    setDirty(true);
                    mStock.setHeldAt(selected.getId());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void initDateControl(final InvestmentTransactionViewHolder viewHolder) {
        // Purchase Date

        viewHolder.dateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(mStock.getPurchaseDate());

                DatePickerDialog.OnDateSetListener listener = (view, year, month, dayOfMonth) -> {
                    setDate(new MmxDate(year, month, dayOfMonth).toDate());
                };

                DatePickerDialog datePicker = new DatePickerDialog(
                        InvestmentTransactionEditActivity.this,
                        listener,
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                );

                // Customize the DatePickerDialog if needed
                datePicker.show();
            }
        });

        // Icon
        UIHelper ui = new UIHelper(this);
        viewHolder.dateView.setCompoundDrawablesWithIntrinsicBounds(ui.getIcon(FontAwesome.Icon.faw_calendar), null, null, null);

        // prev/next day
        viewHolder.previousDayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MmxDate dateTime = new MmxDate(mStock.getPurchaseDate()).minusDays(1);
                setDate(dateTime.toDate());
            }
        });
        viewHolder.nextDayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MmxDate dateTime = new MmxDate(mStock.getPurchaseDate()).plusDays(1);
                setDate(dateTime.toDate());
            }
        });
    }

    private void showCommission() {
        RobotoTextView view = this.findViewById(R.id.commissionView);
        if (view == null) return;

        // todo: format the number of shares based on selected locale.
        view.setText(mStock.getCommission().toString());
    }

    private void showCurrentPrice() {
        RobotoTextView view = this.findViewById(R.id.currentPriceView);
        // todo: format the number of shares based on selected locale.
        view.setText(mStock.getCurrentPrice().toString());
    }

    private void showNumberOfShares() {
        RobotoTextView view = this.findViewById(R.id.numSharesView);
        if (view == null) return;

        // todo: format the number of shares based on selected locale?

        view.setText(mStock.getNumberOfShares().toString());
    }

    private void showPurchasePrice() {
        RobotoTextView view = this.findViewById(R.id.purchasePriceView);
        // todo: format the number of shares based on selected locale.
        view.setText(mStock.getPurchasePrice().toString());
    }

    private void showTotalPrice() {
        if (mViewHolder == null || mViewHolder.totalPriceView == null) return;
        Money total = getShareTransactionAmount();
        mViewHolder.totalPriceView.setText(total.toString());
    }

    private TransactionTypes getSelectedShareTransactionType() {
        if (!mIsShareTransactionMode || mViewHolder == null || mViewHolder.transactionTypeSpinner == null
                || mTransactionTypes.isEmpty()) {
            return TransactionTypes.Withdrawal;
        }

        int pos = mViewHolder.transactionTypeSpinner.getSelectedItemPosition();
        if (pos < 0 || pos >= mTransactionTypes.size()) {
            return TransactionTypes.Withdrawal;
        }
        return mTransactionTypes.get(pos);
    }

    private Money getShareTransactionAmount() {
        Money gross = mStock.getPurchasePrice().multiply(mStock.getNumberOfShares());
        Money commission = mStock.getCommission();

        // Match desktop formula (TrxShareDialog::get_amount):
        // Buy  (Withdrawal): shares*price + commission
        // Sell (Deposit):    shares*price - commission
        if (getSelectedShareTransactionType() == TransactionTypes.Deposit) {
            return gross.subtract(commission);
        }

        return gross.add(commission);
    }

    private void showValue() {
        RobotoTextView view = this.findViewById(R.id.valueView);
        Money valueToDisplay;
        if (mIsStockOverviewMode) {
            // Desktop-like summary view: display market value.
            valueToDisplay = mStock.getCurrentPrice().multiply(mStock.getNumberOfShares());
        } else {
            // Other contexts use stored cost basis (STOCK_V1.VALUE).
            valueToDisplay = mStock.getValue();
        }
        view.setText(valueToDisplay.toString());
    }

    private boolean save() {
        collectData();

        if (!validate()) return false;

        AccountTransactionRepository txRepo = new AccountTransactionRepository(getApplicationContext());
        StockRepository stockRepo = new StockRepository(getApplicationContext());

        if (mLinkedTransaction != null && mLinkedTransaction.hasId()) {
            // Editing an existing share transaction.
            // Only update name/symbol on the stock — do not overwrite its cumulative position.
            if (mStock.getId() != null) {
                Stock current = stockRepo.load(mStock.getId());
                if (current != null) {
                    current.setName(mStock.getName());
                    current.setSymbol(mStock.getSymbol());
                    stockRepo.save(current);
                }
            }
            // Sync amount then persist the accounting transaction.
                mLinkedTransaction.setAmount(getShareTransactionAmount());
            ensureTransactionPayee(mLinkedTransaction);
            txRepo.update(mLinkedTransaction);
            // Persist per-transaction share data.
            saveOrUpdateShareInfo(mLinkedTransaction.getId());
            // Recalculate cumulative position after editing an existing trade.
            if (mStock.getId() != null) {
                recalculateStockPosition(mStock.getId(), stockRepo);
            }
        } else {
            // New share transaction (or plain stock save with no linked transaction).
            if (mLinkedTransaction != null && mOriginalStockDate != null) {
                mStock.setPurchaseDate(mOriginalStockDate);
            }

            if (mStock.getId() == null) {
                // Brand-new stock: initialize CURRENTPRICE from purchase price before inserting.
                if (mStock.getCurrentPrice().isZero()) {
                    mStock.setCurrentPrice(mStock.getPurchasePrice());
                }
                stockRepo.add(mStock);
            } else if (mLinkedTransaction == null) {
                // Existing stock with no new trade (plain portfolio overview save).
                stockRepo.save(mStock);
            } else {
                // Existing stock + new share transaction: update only name/symbol here;
                // the cumulative position is recalculated below after recording the trade.
                Stock current = stockRepo.load(mStock.getId());
                if (current != null) {
                    current.setName(mStock.getName());
                    current.setSymbol(mStock.getSymbol());
                    stockRepo.save(current);
                }
            }

            if (mLinkedTransaction != null) {
                mLinkedTransaction.setAccountId(mStock.getHeldAt());
                mLinkedTransaction.setAmount(getShareTransactionAmount());
                ensureTransactionPayee(mLinkedTransaction);
                txRepo.insert(mLinkedTransaction);

                TransactionLink link = new TransactionLink();
                link.setCheckingAccountId(mLinkedTransaction.getId());
                link.setLinkType("Stock");
                link.setLinkRecordId(mStock.getId());
                new TransactionLinkRepository(getApplicationContext()).add(link);

                saveOrUpdateShareInfo(mLinkedTransaction.getId());

                // Add a manual price history entry so the Desktop and price charts have data.
                String symbol = mStock.getSymbol();
                Money price = mStock.getPurchasePrice();
                Date tradeDate = mLinkedTransaction.getDate();
                if (symbol != null && !symbol.isEmpty() && price != null && !price.isZero()
                        && tradeDate != null) {
                    StockHistoryRepository histRepo =
                            new StockHistoryRepository(getApplicationContext());
                    if (!histRepo.recordExists(symbol, tradeDate)) {
                        StockHistory sh = histRepo.getStockHistory(symbol, price, tradeDate);
                        sh.contentValues.put(StockHistory.UPDTYPE, 2L); // Manual
                        histRepo.add(sh);
                    }
                }

                // Recalculate cumulative STOCK_V1 position from all recorded trades.
                recalculateStockPosition(mStock.getId(), stockRepo);
            }
        }

        return true;
    }

    private void saveOrUpdateShareInfo(long transactionId) {
        if (mShareInfo == null) {
            mShareInfo = new ShareInfo();
        }
        mShareInfo.setCheckingAccountId(transactionId);

        // Sells (Deposit) store a negative share count to match Desktop behavior.
        double shares = mStock.getNumberOfShares();
        if (mLinkedTransaction != null
                && mLinkedTransaction.getTransactionType() == TransactionTypes.Deposit) {
            shares = -Math.abs(shares);
        }
        mShareInfo.setShareNumber(shares);
        mShareInfo.setSharePrice(mStock.getPurchasePrice().toDouble());
        mShareInfo.setShareCommission(mStock.getCommission().toDouble());
        if (mStock.getId() != null) {
            mShareInfo.setShareLot(String.valueOf(mStock.getId()));
        }
        new ShareInfoRepository(getApplicationContext()).save(mShareInfo);
    }

    private boolean isTransferTransaction() {
        return mViewHolder != null
            && mViewHolder.transferCheckBox != null
            && mViewHolder.transferCheckBox.isChecked();
    }

    private void initTransactionDetailsControls(InvestmentTransactionViewHolder viewHolder) {
        if (!mIsShareTransactionMode) {
            return;
        }

        initTransactionTypeSelector(viewHolder.transactionTypeSpinner);
        initStatusSelector(viewHolder.statusSpinner);

        // Use shared transaction controls for payee/category to match normal transactions.
        if (mLinkedTransaction != null) {
            mCategoryId = mLinkedTransaction.getCategoryId() == null ? Constants.NOT_SET : mLinkedTransaction.getCategoryId();
            loadCategoryName(mCategoryId);
            mCommon = new EditTransactionCommonFunctions(this, mLinkedTransaction, database);
            mCommon.findControls(this);
            // populate payee and category names
            mCommon.loadPayeeName(mLinkedTransaction.getPayeeId() == null ? Constants.NOT_SET : mLinkedTransaction.getPayeeId());
            mCommon.categoryName = mCategoryName;
            mCommon.initPayeeControls();
            mCommon.initCategoryControls(SplitCategory.class.getSimpleName());
            mCommon.showPayeeName();
            mCommon.displayCategoryName();
        }

        viewHolder.transactionTypeSpinner.setEnabled(true);
        viewHolder.statusSpinner.setEnabled(true);
        if (viewHolder.categoryTextView != null) viewHolder.categoryTextView.setEnabled(true);
    }

    private void updateShareTransactionSectionVisibility() {
        int shareVis = mIsShareTransactionMode ? View.VISIBLE : View.GONE;
        int stockVis = mIsShareTransactionMode ? View.GONE : View.VISIBLE;

        setSectionVisibility(R.id.shareTransactionSection, shareVis);
        setSectionVisibility(R.id.shareStatusSection, shareVis);
        setSectionVisibility(R.id.sharePayeeSection, shareVis);
        setSectionVisibility(R.id.shareCategorySection, shareVis);
        setSectionVisibility(R.id.totalPriceSection, shareVis);
        setSectionVisibility(R.id.currentPriceSection, stockVis);
        setSectionVisibility(R.id.valueSection, stockVis);
    }

    private void setSectionVisibility(int viewId, int visibility) {
        View view = findViewById(viewId);
        if (view != null) {
            view.setVisibility(visibility);
        }
    }

    private void initTransactionTypeSelector(Spinner spinner) {
        mTransactionTypes.clear();
        mTransactionTypes.add(TransactionTypes.Withdrawal);
        mTransactionTypes.add(TransactionTypes.Deposit);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_spinner_item,
            new String[]{getString(R.string.buy), getString(R.string.sell)}
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (mIsShareTransactionMode) {
                    showTotalPrice();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void initStatusSelector(Spinner spinner) {
        mStatusCodes.clear();
        mStatusCodes.add(TransactionStatuses.NONE.getCode());
        mStatusCodes.add(TransactionStatuses.RECONCILED.getCode());
        mStatusCodes.add(TransactionStatuses.VOID.getCode());
        mStatusCodes.add(TransactionStatuses.FOLLOWUP.getCode());
        mStatusCodes.add(TransactionStatuses.DUPLICATE.getCode());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_spinner_item,
            new String[]{
                getString(R.string.status_none),
                getString(R.string.status_reconciled),
                getString(R.string.status_void),
                getString(R.string.status_follow_up),
                getString(R.string.status_duplicate)
            }
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void loadCategoryName(long categoryId) {
        if (categoryId == Constants.NOT_SET) {
            mCategoryName = "";
            return;
        }

        com.money.manager.ex.datalayer.CategoryRepository repository = new com.money.manager.ex.datalayer.CategoryRepository(this);
        com.money.manager.ex.domainmodel.Category category = repository.load(categoryId);
        mCategoryName = category != null ? category.getName() : "";
    }

    private void displayCategoryName() {
        if (mViewHolder == null || mViewHolder.categoryTextView == null) {
            return;
        }

        if (TextUtils.isEmpty(mCategoryName)) {
            // Keep empty text so the control hint is shown, matching normal transaction UI.
            mViewHolder.categoryTextView.setText("");
        } else {
            mViewHolder.categoryTextView.setText(mCategoryName);
        }
    }

    private void selectTransactionType(TransactionTypes transactionType) {
        if (transactionType == null) {
            mViewHolder.transactionTypeSpinner.setSelection(0);
            return;
        }

        for (int i = 0; i < mTransactionTypes.size(); i++) {
            if (mTransactionTypes.get(i) == transactionType) {
                mViewHolder.transactionTypeSpinner.setSelection(i);
                return;
            }
        }
    }

    private void selectStatus(String statusCode) {
        if (statusCode == null) {
            mViewHolder.statusSpinner.setSelection(0);
            return;
        }

        for (int i = 0; i < mStatusCodes.size(); i++) {
            if (mStatusCodes.get(i).equals(statusCode)) {
                mViewHolder.statusSpinner.setSelection(i);
                return;
            }
        }
    }

    private void setDate(Date dateTime) {
        setDirty(true);

        mStock.setPurchaseDate(dateTime);
        if (mLinkedTransaction != null) {
            mLinkedTransaction.setDate(dateTime);
        }

        showDate(dateTime);
    }

    private void showDate(Date date) {
        String userPattern = dateTimeUtilsLazy.get().getUserDatePattern(InvestmentTransactionEditActivity.this);
        String format = "EEE, " + userPattern;
        String display = dateTimeUtilsLazy.get().format(date, format);
        mViewHolder.dateView.setText(display);
    }

    private boolean validate() {
        // symbol must not be empty.
        if (TextUtils.isEmpty(mStock.getSymbol())) {
            new UIHelper(this).showToast(getString(R.string.symbol_required));
            return false;
        }

        // number of shares, price?

        return true;
    }

    /**
     * Ensures the transaction has a valid PAYEEID.
     * If no payee is selected, finds or creates one named after the stock.
     */
    private void ensureTransactionPayee(AccountTransaction tx) {
        if (tx.hasPayee()) return;

        String payeeName = !TextUtils.isEmpty(mStock.getName())
                ? mStock.getName() : mStock.getSymbol();
        if (TextUtils.isEmpty(payeeName)) return;

        PayeeRepository payeeRepo = new PayeeRepository(getApplicationContext());
        Payee payee = payeeRepo.loadByName(payeeName);
        if (payee == null) {
            payee = new Payee();
            payee.setName(payeeName);
            payeeRepo.add(payee);
        }
        tx.setPayeeId(payee.getId());
    }

    /**
     * Opens a new share transaction for the current stock with the specified transaction type.
     */
    private void openShareTransaction(TransactionTypes transactionType) {
        Intent intent = new Intent(this, InvestmentTransactionEditActivity.class);
        intent.putExtra(ARG_ACCOUNT_ID, mStock.getHeldAt());
        intent.putExtra(ARG_STOCK_ID, mStock.getId());
        intent.putExtra(ARG_NEW_SHARE_TRANSACTION, true);
        intent.putExtra(ARG_INITIAL_TRANSACTION_TYPE, transactionType.name());
        intent.setAction(Intent.ACTION_INSERT);
        startActivity(intent);
    }

    /**
     * Recomputes STOCK_V1 (NUMSHARES, PURCHASEPRICE, CURRENTPRICE) from all TRANSLINK entries
     * for the given stock, matching the Desktop's update_data_position() logic.
     */
    private void recalculateStockPosition(long stockId, StockRepository stockRepo) {
        TransactionLinkRepository linkRepo = new TransactionLinkRepository(getApplicationContext());
        List<TransactionLink> links = linkRepo.query(
                new Select(linkRepo.getAllColumns())
                        .where("LOWER(" + TransactionLink.LINKTYPE + ")=? AND "
                                       + TransactionLink.LINKRECORDID + "=?",
                               "stock", String.valueOf(stockId))
        );
        if (links.isEmpty()) return;

        AccountTransactionRepository transactionRepo = new AccountTransactionRepository(getApplicationContext());
        ShareInfoRepository shareInfoRepo = new ShareInfoRepository(getApplicationContext());
        PositionTotals totals = new PositionTotals();
        Set<Long> processedTransactionIds = new HashSet<>();

        for (TransactionLink link : links) {
            Long transactionId = link.getCheckingAccountId();
            if (transactionId == null || !processedTransactionIds.add(transactionId)) {
                continue;
            }

            ShareInfo shareInfo = shareInfoRepo.loadByTransactionId(transactionId);
            if (shareInfo == null) {
                continue;
            }

            AccountTransaction transaction = transactionRepo.first(
                    transactionRepo.getAllColumns(),
                    AccountTransaction.TRANSID + "=?",
                    new String[] { String.valueOf(transactionId) },
                    null
            );
            PositionEntry entry = new PositionEntry();
            entry.transactionId = transactionId;
            entry.date = transaction == null ? null : transaction.getDate();
            entry.shares = shareInfo.getShareNumber() != null ? shareInfo.getShareNumber() : 0.0;
            entry.price = shareInfo.getSharePrice() != null ? shareInfo.getSharePrice() : 0.0;
            entry.commission = shareInfo.getShareCommission() != null ? shareInfo.getShareCommission() : 0.0;

            totals.entries.add(entry);
        }

        Collections.sort(totals.entries, (left, right) -> {
            Date leftDate = left.date;
            Date rightDate = right.date;

            if (leftDate == null && rightDate != null) return -1;
            if (leftDate != null && rightDate == null) return 1;
            if (leftDate != null && rightDate != null) {
                int byDate = leftDate.compareTo(rightDate);
                if (byDate != 0) return byDate;
            }

            return Long.compare(left.transactionId, right.transactionId);
        });

        for (PositionEntry entry : totals.entries) {
            totals.totalShares += entry.shares;
            if (totals.totalShares < 0) {
                totals.totalShares = 0;
            }

            if (entry.shares > 0) {
                totals.totalInitialValue += entry.shares * entry.price + entry.commission;
            } else {
                totals.totalInitialValue += entry.shares * totals.averageSharePrice;
            }

            if (totals.totalInitialValue < 0) {
                totals.totalInitialValue = 0;
            }
            if (totals.totalShares > 0) {
                totals.averageSharePrice = totals.totalInitialValue / totals.totalShares;
            }

            totals.totalCommission += entry.commission;
            if (entry.price > 0) {
                totals.latestPrice = entry.price;
            }
            if (entry.date != null && (totals.minTradeDate == null || entry.date.before(totals.minTradeDate))) {
                totals.minTradeDate = entry.date;
            }
        }

        Stock stock = stockRepo.load(stockId);
        if (stock == null) return;

        stock.setNumberOfShares(totals.totalShares);
        if (totals.minTradeDate != null) {
            stock.setPurchaseDate(totals.minTradeDate);
        }
        if (totals.totalShares > 0 && totals.averageSharePrice > 0) {
            stock.setPurchasePrice(MoneyFactory.fromDouble(totals.averageSharePrice));
        }
        stock.setValue(MoneyFactory.fromDouble(totals.totalInitialValue));
        stock.setCommission(MoneyFactory.fromDouble(totals.totalCommission));
        if (stock.getCurrentPrice().isZero() && totals.latestPrice > 0) {
            stock.setCurrentPrice(MoneyFactory.fromDouble(totals.latestPrice));
        }
        stockRepo.save(stock);
    }

    private static final class PositionEntry {
        private long transactionId;
        private Date date;
        private double shares;
        private double price;
        private double commission;
    }

    private static final class PositionTotals {
        private final List<PositionEntry> entries = new ArrayList<>();
        private double totalShares;
        private double totalInitialValue;
        private double averageSharePrice;
        private double totalCommission;
        private double latestPrice;
        private Date minTradeDate;
    }
}
