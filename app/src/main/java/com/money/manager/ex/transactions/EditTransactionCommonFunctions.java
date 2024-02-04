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
package com.money.manager.ex.transactions;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.money.manager.ex.Constants;
import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.PayeeActivity;
import com.money.manager.ex.R;
import com.money.manager.ex.account.AccountListActivity;
import com.money.manager.ex.common.Calculator;
import com.money.manager.ex.common.CategoryListActivity;
import com.money.manager.ex.common.CommonSplitCategoryLogic;
import com.money.manager.ex.common.MmxBaseFragmentActivity;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.RequestCodes;
import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.database.ISplitTransaction;
import com.money.manager.ex.database.ITransactionEntity;
import com.money.manager.ex.datalayer.AccountRepository;
import com.money.manager.ex.datalayer.AccountTransactionRepository;
import com.money.manager.ex.datalayer.CategoryRepository;
import com.money.manager.ex.datalayer.IRepository;
import com.money.manager.ex.datalayer.PayeeRepository;
import com.money.manager.ex.domainmodel.Account;
import com.money.manager.ex.domainmodel.Category;
import com.money.manager.ex.domainmodel.Payee;
import com.money.manager.ex.servicelayer.AccountService;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.settings.BehaviourSettings;
import com.money.manager.ex.utils.MmxDate;
import com.money.manager.ex.utils.MmxDateTimeUtils;
import com.shamanland.fonticon.FontIconView;
import com.squareup.sqlbrite.BriteDatabase;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import dagger.Lazy;
import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;
import timber.log.Timber;

/**
 * Functions shared between Checking Account activity and Recurring Transactions activity.
 */
public class EditTransactionCommonFunctions {

    private static final String DATEPICKER_TAG = "datepicker";
    private final MmxBaseFragmentActivity activity;
    private final BriteDatabase mDatabase;
    private final ArrayList<String> mAccountNameList = new ArrayList<>();
    private final ArrayList<Integer> mAccountIdList = new ArrayList<>();
    // Model
    public ITransactionEntity transactionEntity;
    public String payeeName;
    public String mToAccountName;
    public String categoryName, subCategoryName;
    public ArrayList<ISplitTransaction> mSplitTransactions;
    public ArrayList<ISplitTransaction> mSplitTransactionsDeleted;
    // Controls
    public EditTransactionViewHolder viewHolder;
    @Inject
    Lazy<MmxDateTimeUtils> dateTimeUtilsLazy;
    private boolean mSplitSelected;
    private boolean mDirty; // indicate whether the data has been modified by the user.
    private String mSplitCategoryEntityName;
    private List<Account> AccountList;
    private TransactionTypes previousTransactionType = TransactionTypes.Withdrawal;
    private String[] mStatusItems, mStatusValues;    // arrays to manage trans.code and status
    private String mUserDateFormat;

    public EditTransactionCommonFunctions(final MmxBaseFragmentActivity parentActivity,
                                          final ITransactionEntity transactionEntity, final BriteDatabase database) {

        activity = parentActivity;
        this.transactionEntity = transactionEntity;
        mDatabase = database;

        MmexApplication.getApp().iocComponent.inject(this);
    }

    public boolean deleteMarkedSplits(final IRepository repository) {
        for (int i = 0; i < mSplitTransactionsDeleted.size(); i++) {
            final ISplitTransaction splitToDelete = mSplitTransactionsDeleted.get(i);

            // Ignore unsaved entities.
            if (!splitToDelete.hasId()) continue;

            if (!repository.delete(splitToDelete)) {
                Toast.makeText(activity, R.string.db_checking_update_failed, Toast.LENGTH_SHORT).show();
                Timber.w("Delete split transaction failed!");
                return false;
            }
        }

        return true;
    }

    public void displayCategoryName() {
        // validation
        if (null == this.viewHolder.categoryTextView) return;

        viewHolder.categoryTextView.setText("");

        if (mSplitSelected) {
            // Split transaction. Show ...
            viewHolder.categoryTextView.setText("\u2026");
        } else {
            if (!TextUtils.isEmpty(categoryName)) {
                viewHolder.categoryTextView.setText(categoryName);
                if (!TextUtils.isEmpty(subCategoryName)) {
                    viewHolder.categoryTextView.setText(Html.fromHtml(
                            viewHolder.categoryTextView.getText() + " : <i>" + subCategoryName + "</i>"));
                }
            }
        }
    }

    public void findControls(final AppCompatActivity view) {
        viewHolder = new EditTransactionViewHolder(view);
    }

    public Integer getAccountCurrencyId(final int accountId) {
        if (Constants.NOT_SET == accountId) return Constants.NOT_SET;

        final AccountRepository repo = new AccountRepository(activity);
        Integer currencyId = repo.loadCurrencyIdFor(accountId);
        if (null == currencyId) {
            new UIHelper(activity).showToast(R.string.error_loading_currency);

            currencyId = Constants.NOT_SET;
        }
        return currencyId;
    }

    public String getTransactionType() {
        if (null == this.transactionEntity.getTransactionType()) {
            return null;
        }

        return transactionEntity.getTransactionType().name();
    }

    public FontIconView getDepositButtonIcon() {
        return activity.findViewById(R.id.depositButtonIcon);
    }

    public Integer getDestinationCurrencyId() {
        Integer accountId = transactionEntity.getAccountToId();
        // The destination account/currency is hidden by default and may be uninitialized.
        if (!transactionEntity.hasAccountTo() && !mAccountIdList.isEmpty()) {
            accountId = mAccountIdList.get(0);
        }

        // Handling some invalid values.
        if (null == accountId || 0 == accountId) accountId = Constants.NOT_SET;

        return getAccountCurrencyId(accountId);
    }

    public ArrayList<ISplitTransaction> getDeletedSplitCategories() {
        if (null == mSplitTransactionsDeleted) {
            mSplitTransactionsDeleted = new ArrayList<>();
        }
        return mSplitTransactionsDeleted;
    }

    public boolean getDirty() {
        return mDirty;
    }

    public void setDirty(final boolean dirty) {
        mDirty = dirty;
    }

    public Integer getSourceCurrencyId() {
        Integer accountId = transactionEntity.getAccountId();

        //if (!transactionEntity.has)
        if (null == accountId && !mAccountIdList.isEmpty()) {
            accountId = mAccountIdList.get(0);
        }

        if (null == accountId || 0 == accountId) accountId = Constants.NOT_SET;

        return getAccountCurrencyId(accountId);
    }

    public FontIconView getTransferButtonIcon() {
        return activity.findViewById(R.id.transferButtonIcon);
    }

    public FontIconView getWithdrawalButtonIcon() {
        return activity.findViewById(R.id.withdrawalButtonIcon);
    }

    public boolean hasPayee() {
        return 0 < this.transactionEntity.getPayeeId();
    }

    public boolean hasSplitCategories() {
        return !getSplitTransactions().isEmpty();
    }

    /**
     * Initialize account selectors.
     */
    public void initAccountSelectors() {
        final AppSettings settings = new AppSettings(activity);

        // Account list as the data source to populate the drop-downs.

        final AccountService accountService = new AccountService(activity);
        AccountList = accountService.getTransactionAccounts(
                settings.getLookAndFeelSettings().getViewOpenAccounts(),
                settings.getLookAndFeelSettings().getViewFavouriteAccounts());
        if (null == this.AccountList) return;

        for (final Account account : AccountList) {
            mAccountNameList.add(account.getName());
            mAccountIdList.add(account.getId());
        }

        final AccountRepository accountRepository = new AccountRepository(activity);
        Integer accountId = transactionEntity.getAccountId();
        if (null != accountId) {
            addMissingAccountToSelectors(accountRepository, accountId);
        }
        addMissingAccountToSelectors(accountRepository, transactionEntity.getAccountToId());
        // add the default account, if any.
        final Integer defaultAccount = settings.getGeneralSettings().getDefaultAccountId();
        // Set the current account, if not set already.
        if ((null != accountId && Constants.NOT_SET == accountId) && (null != defaultAccount && Constants.NOT_SET != defaultAccount)) {
            accountId = defaultAccount;
            addMissingAccountToSelectors(accountRepository, accountId);
            // Set the default account as the active account.
            transactionEntity.setAccountId(accountId);
        }

        // Adapter for account selectors.

        final ArrayAdapter<String> accountAdapter = new ArrayAdapter<>(activity,
                android.R.layout.simple_spinner_item, mAccountNameList);

        accountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        viewHolder.spinAccount.setAdapter(accountAdapter);
        viewHolder.spinAccountTo.setAdapter(accountAdapter);

        // Selection handler.

        final AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {
                if ((0 > position) || (position > mAccountIdList.size())) {
                    return;
                }

                setDirty(true);

                final boolean isSource = parent == viewHolder.spinAccount;
                final boolean isTransfer = TransactionTypes.Transfer == transactionEntity.getTransactionType();
                final Integer accountId = mAccountIdList.get(position);

                if (isSource) {
                    final int originalCurrencyId = getSourceCurrencyId();

                    transactionEntity.setAccountId(accountId);

                    if (isTransfer) {
                        // calculate the exchange amount if it is 0.
                        if (transactionEntity.getAmountTo().isZero()) {
                            final Money convertedAmount = calculateAmountTo();
                            transactionEntity.setAmountTo(convertedAmount);
                            displayAmountTo();
                        }
                        // Recalculate the original amount when the currency changes.
                        if (originalCurrencyId != getSourceCurrencyId()) {
                            final Money exchangeAmount = calculateAmountFrom();
                            transactionEntity.setAmount(exchangeAmount);
                            displayAmountFrom();
                        }
                    } else {
                        displayAmountFrom();
                    }
                } else {
                    final int originalCurrencyId = getDestinationCurrencyId();

                    transactionEntity.setAccountToId(accountId);

                    if (isTransfer) {
                        // calculate the exchange amount if it is 0.
                        if (transactionEntity.getAmount().isZero()) {
                            final Money convertedAmount = calculateAmountFrom();
                            transactionEntity.setAmount(convertedAmount);
                            displayAmountFrom();
                        }
                        // Recalculate the original amount when the currency changes.
                        if (originalCurrencyId != getDestinationCurrencyId()) {
                            final Money exchangeAmount = calculateAmountTo();
                            transactionEntity.setAmountTo(exchangeAmount);
                            displayAmountTo();
                        }
                    } else {
                        displayAmountTo();
                    }
                }

                refreshControlTitles();
            }

            @Override
            public void onNothingSelected(final AdapterView<?> parent) {
            }
        };

        // Account

        final int accountIndex = mAccountIdList.indexOf(accountId);
        if (0 <= accountIndex) {
            viewHolder.spinAccount.setSelection(accountIndex, true);
        }
        viewHolder.spinAccount.setOnItemSelectedListener(listener);

        // To Account

        if (transactionEntity.hasAccountTo() && mAccountIdList.contains(transactionEntity.getAccountToId())) {
            viewHolder.spinAccountTo.setSelection(mAccountIdList.indexOf(transactionEntity.getAccountToId()), true);
        }
        viewHolder.spinAccountTo.setOnItemSelectedListener(listener);
    }

    public void initAmountSelectors() {
//        View.OnClickListener onClickAmount = new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                // Get currency id from the account for which the amount has been modified.
//                Integer currencyId;
//                Money amount;
//
//                if (v.equals(viewHolder.txtAmountTo)) {
//                    // clicked Amount To.
//                    currencyId = getDestinationCurrencyId();
//                    amount = transactionEntity.getAmountTo();
//                } else {
//                    // clicked Amount.
//                    currencyId = getSourceCurrencyId();
//                    amount = transactionEntity.getAmount();
//                }
//
//                AmountInputDialog dialog = AmountInputDialog.getInstance(v.getId(), amount, currencyId);
//                dialog.show(activity.getSupportFragmentManager(), dialog.getClass().getSimpleName());
//
//                // The result is received in onFinishedInputAmountDialog.
//            }
//        };

        // amount
        displayAmountFrom();
        viewHolder.txtAmount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                final int currencyId = getSourceCurrencyId();
                final Money amount = transactionEntity.getAmount();

//                Intent intent = IntentFactory.getNumericInputIntent(getContext(), amount, currencyId);
//                getActivity().startActivityForResult(intent, REQUEST_AMOUNT);
                Calculator.forActivity(getActivity())
                        .currency(currencyId)
                        .amount(amount)
                        .show(RequestCodes.AMOUNT);
            }
        });

        // amount to
        displayAmountTo();
        viewHolder.txtAmountTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                final int currencyId = getDestinationCurrencyId();
                final Money amount = transactionEntity.getAmountTo();

//                Intent intent = IntentFactory.getNumericInputIntent(getContext(), amount, currencyId);
//                getActivity().startActivityForResult(intent, REQUEST_AMOUNT_TO);
                Calculator.forActivity(getActivity())
                        .amount(amount).currency(currencyId)
                        .show(RequestCodes.AMOUNT_TO);
            }
        });
    }

    /**
     * Initialize Category selector.
     *
     * @param datasetName name of the dataset (TableBudgetSplitTransactions.class.getSimpleName())
     */
    public void initCategoryControls(final String datasetName) {
        // keep the dataset name for later.
        mSplitCategoryEntityName = datasetName;

        viewHolder.categoryTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (!isSplitSelected()) {
                    // select first category.
                    final Intent intent = new Intent(getActivity(), CategoryListActivity.class);
                    intent.setAction(Intent.ACTION_PICK);
                    getActivity().startActivityForResult(intent, RequestCodes.CATEGORY);
                } else {
                    // select split categories.
                    showSplitCategoriesForm(mSplitCategoryEntityName);
                }

                // results are handled in onActivityResult.
            }
        });
    }

    /**
     * Due Date picker
     */
    public void initDateSelector() {
        Date date = transactionEntity.getDate();
        if (null == date) {
            date = new MmxDate().toDate();
            transactionEntity.setDate(date);
        }
        showDate(date);

        viewHolder.dateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final MmxDate dateTime = new MmxDate(transactionEntity.getDate());

                final DatePickerDialog.OnDateSetListener listener = (view, year, month, dayOfMonth) -> {
                    final Date selectedDate = dateTimeUtilsLazy.get().from(year, month, dayOfMonth);
                    setDate(selectedDate);
                };

                final DatePickerDialog datePicker = new DatePickerDialog(
                        getContext(),
                        listener,
                        dateTime.getYear(),
                        dateTime.getMonthOfYear(),
                        dateTime.getDayOfMonth()
                );

                // Customize the DatePickerDialog if needed
                datePicker.show();
            }
        });

        viewHolder.previousDayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                final Date dateTime = new MmxDate(transactionEntity.getDate()).minusDays(1).toDate();
                setDate(dateTime);
            }
        });

        viewHolder.nextDayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                final Date dateTime = new MmxDate(transactionEntity.getDate()).plusDays(1).toDate();
                setDate(dateTime);
            }
        });
    }

    public void initializeToolbar() {
//        activity.showStandardToolbarActions();

        // String title
//        ActionBar actionBar = activity.getSupportActionBar();
//        actionBar.setTitle(title);

        // Title
//        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
//        collapsingToolbarLayout.setTitle(getString(R.string.budget));

        // Back arrow / cancel.
        activity.setDisplayHomeAsUpEnabled(true);

        // todo: add Save button

    }

    public void initNotesControls() {
        if (!(TextUtils.isEmpty(transactionEntity.getNotes()))) {
            viewHolder.edtNotes.setText(transactionEntity.getNotes());
        }

        viewHolder.edtNotes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence charSequence, final int i, final int i1, final int i2) {

            }

            @Override
            public void onTextChanged(final CharSequence charSequence, final int i, final int i1, final int i2) {

            }

            @Override
            public void afterTextChanged(final Editable editable) {
                setDirty(true);
                transactionEntity.setNotes(editable.toString());
            }
        });
    }

    public void initPayeeControls() {
        viewHolder.txtSelectPayee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final Intent intent = new Intent(getContext(), PayeeActivity.class);
                intent.setAction(Intent.ACTION_PICK);
                getActivity().startActivityForResult(intent, RequestCodes.PAYEE);

                // the result is handled in onActivityResult
            }
        });

        viewHolder.removePayeeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                setDirty(true);

                transactionEntity.setPayeeId(Constants.NOT_SET);
                payeeName = "";

                showPayeeName();
            }
        });
    }

    /**
     * Initialize Split Categories button & controls.
     */
    public void initSplitCategories() {
        viewHolder.splitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final boolean splitting = !isSplitSelected();

                if (splitting) {
                    showSplitCategoriesForm(mSplitCategoryEntityName);
                } else {
                    // User wants to remove split.
                    final int splitCount = getSplitTransactions().size();
                    switch (splitCount) {
                        case 0:
                            // just remove split
                            setSplit(false);
                            break;
                        case 1:
                            convertOneSplitIntoRegularTransaction();
                            break;
                        default:
                            showSplitResetNotice();
                            break;
                    }
                }
            }
        });

        refreshSplitControls();
    }

    public void initStatusSelector() {
        mStatusItems = activity.getResources().getStringArray(R.array.status_items);
        mStatusValues = activity.getResources().getStringArray(R.array.status_values);

        // create adapter for spinnerStatus
        final ArrayAdapter<String> adapterStatus = new ArrayAdapter<>(activity,
                android.R.layout.simple_spinner_item, mStatusItems);
        adapterStatus.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        viewHolder.spinStatus.setAdapter(adapterStatus);

        // select current value
        if (!(TextUtils.isEmpty(transactionEntity.getStatus()))) {
            if (Arrays.asList(mStatusValues).contains(transactionEntity.getStatus())) {
                viewHolder.spinStatus.setSelection(Arrays.asList(mStatusValues).indexOf(transactionEntity.getStatus()), true);
            }
        } else {
            transactionEntity.setStatus(mStatusValues[0]);
        }
        viewHolder.spinStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {
                if ((0 <= position) && (position <= mStatusValues.length)) {
                    final String selectedStatus = mStatusValues[position];
                    // If Status has been changed manually, mark data as dirty.
                    if (!selectedStatus.equalsIgnoreCase(transactionEntity.getStatus())) {
                        setDirty(true);
                    }
                    transactionEntity.setStatus(selectedStatus);
                }
            }

            @Override
            public void onNothingSelected(final AdapterView<?> parent) {
            }
        });
    }

    public void initTransactionNumberControls() {
        // Transaction number

        if (!TextUtils.isEmpty(transactionEntity.getTransactionNumber())) {
            viewHolder.edtTransNumber.setText(transactionEntity.getTransactionNumber());
        }

        viewHolder.edtTransNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence charSequence, final int i, final int i1, final int i2) {

            }

            @Override
            public void onTextChanged(final CharSequence charSequence, final int i, final int i1, final int i2) {

            }

            @Override
            public void afterTextChanged(final Editable editable) {
                setDirty(true);

                transactionEntity.setTransactionNumber(editable.toString());
            }
        });

        viewHolder.btnTransNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final AccountTransactionRepository repo = new AccountTransactionRepository(getContext());

                final String sql = "SELECT MAX(CAST(" + ITransactionEntity.TRANSACTIONNUMBER + " AS INTEGER)) FROM " +
                        repo.getSource() + " WHERE " +
                        ITransactionEntity.ACCOUNTID + "=?";

                final String accountId = transactionEntity.getAccountId().toString();
                final Cursor cursor = mDatabase.query(sql, accountId);
                if (null == cursor) return;

                if (cursor.moveToFirst()) {
                    String transNumber = cursor.getString(0);
                    if (TextUtils.isEmpty(transNumber)) {
                        transNumber = "0";
                    }
                    if ((!TextUtils.isEmpty(transNumber)) && TextUtils.isDigitsOnly(transNumber)) {
                        try {
                            // Use Money type to support very large numbers.
                            final Money transactionNumber = MoneyFactory.fromString(transNumber);
                            viewHolder.edtTransNumber.setText(transactionNumber.add(MoneyFactory.fromString("1"))
                                    .toString());
                        } catch (final Exception e) {
                            Timber.e(e, "increasing transaction number");
                        }
                    }
                }
                cursor.close();
            }
        });

        if (!transactionEntity.hasId() && (new BehaviourSettings(activity).getAutoTransactionNumber())) {
            viewHolder.btnTransNumber.callOnClick();
        }
    }

    public void initTransactionTypeSelector() {
        // Handle click events.
        final View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                setDirty(true);

                // find which transaction type this is.
                final TransactionTypes type = (TransactionTypes) v.getTag();
                changeTransactionTypeTo(type);
            }
        };

        if (null != viewHolder.withdrawalButton) {
            viewHolder.withdrawalButton.setTag(TransactionTypes.Withdrawal);

            viewHolder.withdrawalButton.setOnClickListener(onClickListener);
        }
        if (null != viewHolder.depositButton) {
            viewHolder.depositButton.setTag(TransactionTypes.Deposit);

            viewHolder.depositButton.setOnClickListener(onClickListener);
        }
        if (null != viewHolder.transferButton) {
            viewHolder.transferButton.setTag(TransactionTypes.Transfer);

            viewHolder.transferButton.setOnClickListener(onClickListener);
        }

        // Check if the transaction type has been set (for example, when editing an existing transaction).
        final TransactionTypes current = null == transactionEntity.getTransactionType()
                ? TransactionTypes.Withdrawal
                : transactionEntity.getTransactionType();
        changeTransactionTypeTo(current);
    }

    /**
     * Indicate whether the Split Categories is selected/checked.
     *
     * @return boolean
     */
    public boolean isSplitSelected() {
        return mSplitSelected;
    }

    /**
     * Loads info for Category and Subcategory
     *
     * @return A boolean indicating whether the operation was successful.
     */
    public boolean loadCategoryName() {
        if (!transactionEntity.hasCategory()) return false;

        final CategoryRepository categoryRepository = new CategoryRepository(activity);
        final Category category = categoryRepository.load(transactionEntity.getCategoryId());
        if (null != category) {
            categoryName = category.getName();
            // TODO parent category : category
            if (0 < category.getParentId()) {
                final Category parentCategory = categoryRepository.load(category.getParentId());
                if (null != parentCategory)
                    categoryName = parentCategory.getName() + " : " + category.getName();
            }
        } else {
            categoryName = null;
        }

        return true;
    }

    public boolean onActionCancelClick() {
        if (mDirty) {
            final MaterialDialog dialog = new MaterialDialog.Builder(activity)
                    .title(android.R.string.cancel)
                    .content(R.string.transaction_cancel_confirm)
                    .positiveText(R.string.discard)
                    .negativeText(R.string.keep_editing)
                    .cancelable(false)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull final MaterialDialog dialog, @NonNull final DialogAction which) {
                            cancelActivity();
                        }
                    })
                    .build();
            dialog.show();
        } else {
            // Just close activity
            cancelActivity();
        }
        return true;
    }

    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if ((Activity.RESULT_OK != resultCode) || (null == data)) return;

        mDirty = true;

        String stringExtra;

        switch (requestCode) {
            case RequestCodes.PAYEE:
                transactionEntity.setPayeeId(data.getIntExtra(PayeeActivity.INTENT_RESULT_PAYEEID, Constants.NOT_SET));
                payeeName = data.getStringExtra(PayeeActivity.INTENT_RESULT_PAYEENAME);
                // select last category used from payee. Only if category has not been entered earlier.
                if (!mSplitSelected && !transactionEntity.hasCategory()) {
                    if (setCategoryFromPayee(transactionEntity.getPayeeId())) {
                        displayCategoryName(); // refresh UI
                    }
                }
                // refresh UI
                showPayeeName();
                break;

            case RequestCodes.ACCOUNT:
                transactionEntity.setAccountToId(data.getIntExtra(AccountListActivity.INTENT_RESULT_ACCOUNTID, Constants.NOT_SET));
                mToAccountName = data.getStringExtra(AccountListActivity.INTENT_RESULT_ACCOUNTNAME);
                break;

            case RequestCodes.AMOUNT:
                onFinishedInputAmountDialog(R.id.textViewAmount, Calculator.getAmountFromResult(data));
                break;

            case RequestCodes.AMOUNT_TO:
                onFinishedInputAmountDialog(R.id.textViewToAmount, Calculator.getAmountFromResult(data));
                break;

            case RequestCodes.CATEGORY:
                transactionEntity.setCategoryId(data.getIntExtra(CategoryListActivity.INTENT_RESULT_CATEGID, Constants.NOT_SET));
                categoryName = data.getStringExtra(CategoryListActivity.INTENT_RESULT_CATEGNAME);
                // refresh UI category
                displayCategoryName();
                break;

            case RequestCodes.SPLIT_TX:
                mSplitTransactions = Parcels.unwrap(data.getParcelableExtra(SplitCategoriesActivity.INTENT_RESULT_SPLIT_TRANSACTION));

                // deleted items
                final Parcelable parcelDeletedSplits = data.getParcelableExtra(SplitCategoriesActivity.INTENT_RESULT_SPLIT_TRANSACTION_DELETED);
                if (null != parcelDeletedSplits) {
                    mSplitTransactionsDeleted = Parcels.unwrap(parcelDeletedSplits);
                }

                // Splits and deleted splits must be restored before any action takes place.
                onSplitConfirmed(getSplitTransactions());

                break;
        }
    }

    public void onFinishedInputAmountDialog(final int id, final Money amount) {
        final View view = activity.findViewById(id);
        if (null == view || !(view instanceof TextView)) return;

        mDirty = true;

        final boolean isTransfer = transactionEntity.getTransactionType() == TransactionTypes.Transfer;
        final boolean isAmountFrom = R.id.textViewAmount == id;

        // Set and display the selected amount.
        if (isAmountFrom) {
            transactionEntity.setAmount(amount);
            displayAmountFrom();
        } else {
            transactionEntity.setAmountTo(amount);
            displayAmountTo();
        }

        // Handle currency exchange on Transfers.
        if (isTransfer) {
            final Integer fromCurrencyId = getSourceCurrencyId();
            final Integer toCurrencyId = getDestinationCurrencyId();
            if (fromCurrencyId.equals(toCurrencyId)) {
                // Same currency. Update both values if the transfer is in the same currency.
                transactionEntity.setAmount(amount);
                transactionEntity.setAmountTo(amount);

                displayAmountFrom();
                displayAmountTo();
                // Exit here.
                return;
            }

            // Different currency. Recalculate the other amount only if it has not been set.
            final boolean shouldConvert = isAmountFrom
                    ? transactionEntity.getAmountTo().isZero()
                    : transactionEntity.getAmount().isZero();
            if (shouldConvert) {
                // Convert the value and write the amount into the other input box.
                final Money convertedAmount;
                if (isAmountFrom) {
                    convertedAmount = calculateAmountTo();
                    transactionEntity.setAmountTo(convertedAmount);
                    displayAmountTo();
                } else {
                    convertedAmount = calculateAmountFrom();
                    transactionEntity.setAmount(convertedAmount);
                    displayAmountFrom();
                }
            }
        }
    }

    /**
     * Handle the controls after the split is checked.
     */
    public void refreshSplitControls() {
        // display category field
        displayCategoryName();

        // enable/disable Amount field.
        viewHolder.txtAmount.setEnabled(!mSplitSelected);
        viewHolder.txtAmountTo.setEnabled(!mSplitSelected);

        updateSplitButton();
    }

    /**
     * Reflect the transaction type change. Show and hide controls appropriately.
     */
    public void onTransactionTypeChanged(final TransactionTypes transactionType) {
        transactionEntity.setTransactionType(transactionType);

        final boolean isTransfer = transactionType == TransactionTypes.Transfer;

        viewHolder.accountFromLabel.setText(isTransfer ? R.string.from_account : R.string.account);
        viewHolder.tableRowAccountTo.setVisibility(isTransfer ? View.VISIBLE : View.GONE);
        viewHolder.tableRowPayee.setVisibility(!isTransfer ? View.VISIBLE : View.GONE);
        viewHolder.tableRowAmountTo.setVisibility(isTransfer ? View.VISIBLE : View.GONE);

        refreshControlTitles();

        if (isTransfer) {
            onTransferSelected();
            viewHolder.splitButton.setEnabled(false);
        } else {
            // Change sign for the split records. Transfers should delete split records.
            CommonSplitCategoryLogic.changeSign(getSplitTransactions());
            viewHolder.splitButton.setEnabled(true);
        }
    }

    /**
     * Update input control titles to reflect the transaction type.
     */
    public void refreshControlTitles() {
        if (null == viewHolder.amountHeaderTextView || null == viewHolder.amountToHeaderTextView) return;

        final boolean isTransfer = transactionEntity.getTransactionType() == TransactionTypes.Transfer;

        if (!isTransfer) {
            viewHolder.amountHeaderTextView.setText(R.string.amount);
        } else {
            // Transfer. Adjust the headers on amount text boxes.
            int index = mAccountIdList.indexOf(transactionEntity.getAccountId());
            if (0 <= index) {
                // the title depends on whether we are showing the destination amount.
                if (areCurrenciesSame()) {
                    viewHolder.amountHeaderTextView.setText(activity.getString(R.string.transfer_amount));
                } else {
                    viewHolder.amountHeaderTextView.setText(activity.getString(R.string.withdrawal_from,
                            AccountList.get(index).getName()));
                }
            }

            index = mAccountIdList.indexOf(transactionEntity.getAccountToId());
            if (0 <= index) {
                viewHolder.amountToHeaderTextView.setText(activity.getString(R.string.deposit_to,
                        AccountList.get(index).getName()));
            }
        }
    }

    /**
     * update UI interface with PayeeName
     */
    public void showPayeeName() {
        // write into text button payee name
        if (null != this.viewHolder.txtSelectPayee) {
            final String text = !TextUtils.isEmpty(payeeName)
                    ? payeeName : "";

            viewHolder.txtSelectPayee.setText(text);
        }
    }

    /**
     * Reset the effects of transfer when switching to Withdrawal/Deposit.
     */
    public void resetTransfer() {
        // reset destination account and amount
        transactionEntity.setAccountToId(Constants.NOT_SET);
        transactionEntity.setAmountTo(MoneyFactory.fromDouble(0));
    }

    public void setSplit(final boolean checked) {
        mSplitSelected = checked;

        refreshSplitControls();
    }

    /**
     * query info payee
     *
     * @param payeeId id payee
     * @return true if the data selected
     */
    public boolean loadPayeeName(final int payeeId) {
        final PayeeRepository repo = new PayeeRepository(activity);
        final Payee payee = repo.load(payeeId);
        if (null != payee) {
            payeeName = payee.getName();
        } else {
            payeeName = "";
        }

        return true;
    }

    /**
     * setCategoryFromPayee set last category used from payee
     *
     * @param payeeId Identify of payee
     * @return true if category set
     */
    public boolean setCategoryFromPayee(final int payeeId) {
        if (Constants.NOT_SET == payeeId) return false;

        final PayeeRepository repo = new PayeeRepository(activity);
        final Payee payee = repo.load(payeeId);
        if (null == payee) return false;
        if (!payee.hasCategory()) return false;

        // otherwise

        transactionEntity.setCategoryId(payee.getCategoryId());

        loadCategoryName();

        return true;
    }

    /**
     * Select, or change, the type of transaction (withdrawal, deposit, transfer).
     * Entry point and the handler for the type selector input control.
     *
     * @param transactionType The type to set the transaction to.
     */
    public void changeTransactionTypeTo(final TransactionTypes transactionType) {
        previousTransactionType = transactionEntity.getTransactionType();
        transactionEntity.setTransactionType(transactionType);

        // Clear all buttons.

        final Core core = new Core(activity);
        final int backgroundInactive = core.getColourFromAttribute(R.attr.button_background_inactive);

        viewHolder.withdrawalButton.setBackgroundColor(backgroundInactive);
        getWithdrawalButtonIcon().setTextColor(ContextCompat.getColor(activity, R.color.material_red_700));
        viewHolder.depositButton.setBackgroundColor(backgroundInactive);
        getDepositButtonIcon().setTextColor(ContextCompat.getColor(activity, R.color.material_green_700));
        viewHolder.transferButton.setBackgroundColor(backgroundInactive);
        getTransferButtonIcon().setTextColor(ContextCompat.getColor(activity, R.color.material_grey_700));

        // Style the selected button.

        final UIHelper uiHelper = new UIHelper(activity);
        final int backgroundSelected = ContextCompat.getColor(activity, R.color.md_accent);
        final int foregroundSelected = uiHelper.getToolbarItemColor();

        switch (transactionType) {
            case Deposit:
                viewHolder.depositButton.setBackgroundColor(backgroundSelected);
                getDepositButtonIcon().setTextColor(foregroundSelected);
                break;
            case Withdrawal:
                viewHolder.withdrawalButton.setBackgroundColor(backgroundSelected);
                getWithdrawalButtonIcon().setTextColor(foregroundSelected);
                break;
            case Transfer:
                viewHolder.transferButton.setBackgroundColor(backgroundSelected);
                getTransferButtonIcon().setTextColor(foregroundSelected);
                break;
        }

        // Handle the change.

        onTransactionTypeChanged(transactionType);
    }

    public boolean validateData() {
        final boolean isTransfer = transactionEntity.getTransactionType() == TransactionTypes.Transfer;
        final Core core = new Core(activity);

        if (isTransfer) {
            if (Constants.NOT_SET == transactionEntity.getAccountToId()) {
                core.alert(R.string.error_toaccount_not_selected);
                return false;
            }
            if (transactionEntity.getAccountToId().equals(transactionEntity.getAccountId())) {
                core.alert(R.string.error_transfer_to_same_account);
                return false;
            }

            // Amount To is required and has to be positive.
            if (0 >= this.transactionEntity.getAmountTo().toDouble()) {
                core.alert(R.string.error_amount_must_be_positive);
                return false;
            }
        }

        // Amount is required and must be positive. Sign is determined by transaction type.
        if (0 >= transactionEntity.getAmount().toDouble()) {
            core.alert(R.string.error_amount_must_be_positive);
            return false;
        }

        // Category is required if tx is not a split or transfer.
        final boolean hasCategory = transactionEntity.hasCategory();
        if (!hasCategory && (!mSplitSelected) && !isTransfer) {
            core.alert(R.string.error_category_not_selected);
            return false;
        }

        // Split records must exist if split is checked.
        if (mSplitSelected && getSplitTransactions().isEmpty()) {
            core.alert(R.string.error_split_transaction_empty);
            return false;
        }
        // Splits sum must be positive.
        if (!CommonSplitCategoryLogic.validateSumSign(getSplitTransactions())) {
            core.alert(R.string.split_amount_negative);
            return false;
        }

        return true;
    }

    /**
     * Remove splits when switching to Transfer.
     */
    public void confirmDeletingCategories() {
        removeAllSplitCategories();
        setSplit(false);
        transactionEntity.setTransactionType(TransactionTypes.Transfer);
        onTransactionTypeChanged(TransactionTypes.Transfer);
    }

    /**
     * When cancelling changing the transaction type to Transfer, revert back to the
     * previous transaction type.
     */
    public void cancelChangingTransactionToTransfer() {
        // Select the previous transaction type.
        changeTransactionTypeTo(previousTransactionType);
    }

    /**
     * After the user accepts, remove any split categories.
     */
    public void removeAllSplitCategories() {
        final List<ISplitTransaction> splitTransactions = getSplitTransactions();

        for (int i = 0; i < splitTransactions.size(); i++) {
            final ISplitTransaction split = splitTransactions.get(i);
            // How do we get this?
            //if (split == null) continue;

            final int id = split.getId();
            final ArrayList<ISplitTransaction> deletedSplits = getDeletedSplitCategories();

            if (-1 == id) {
                // Remove any newly created splits.
                splitTransactions.remove(i);
                i--;
            } else {
                // Delete any splits already in the database. Avoid adding duplicate records.
                if (!deletedSplits.contains(split)) {
                    deletedSplits.add(split);
                }
            }
        }
    }

    /**
     * Check if there is only one Split Category and transforms the transaction to a non-split
     * transaction, removing the split category record.
     *
     * @return True if there is only one split. Need to update the transaction.
     */
    public boolean convertOneSplitIntoRegularTransaction() {
        if (1 != getSplitTransactions().size()) return false;

        // use the first split category record.
        final ISplitTransaction splitTransaction = getSplitTransactions().get(0);

        // reuse the amount & category
        transactionEntity.setAmount(splitTransaction.getAmount());
        displayAmountFrom();

        transactionEntity.setCategoryId(splitTransaction.getCategoryId());
        loadCategoryName();
//        displayCategoryName();

        // reset split indicator & display category
        setSplit(false);

        getDeletedSplitCategories().add(splitTransaction);
        getSplitTransactions().remove(splitTransaction);

        // e deletion in the specific implementation.
        return true;
    }

    /*
        Private
    */

    private void addMissingAccountToSelectors(final AccountRepository accountRepository, final Integer accountId) {
        if (null == accountId || 0 >= accountId) return;

        // #316. In case the account from recurring transaction is not in the visible list,
        // load it separately.
        if (!mAccountIdList.contains(accountId)) {
            final Account savedAccount = accountRepository.load(accountId);

            if (null != savedAccount) {
                AccountList.add(savedAccount);
                mAccountNameList.add(savedAccount.getName());
                mAccountIdList.add(savedAccount.getId());
            }
        }
    }

    private boolean areCurrenciesSame() {
        if (null == transactionEntity.getAccountId()) return false;
        if (null == transactionEntity.getAccountToId()) return false;

        final AccountRepository repo = new AccountRepository(activity);
        final Account accountFrom = repo.load(transactionEntity.getAccountId());
        if (null == accountFrom) return false;

        final Account accountTo = repo.load(transactionEntity.getAccountToId());
        if (null == accountTo) return false;

        return accountFrom.getCurrencyId().equals(accountTo.getCurrencyId());
    }

    /**
     * Perform currency exchange to get the Amount From.
     */
    private Money calculateAmountFrom() {
        final CurrencyService currencyService = new CurrencyService(activity);

        return currencyService.doCurrencyExchange(getSourceCurrencyId(), transactionEntity.getAmountTo(),
                getDestinationCurrencyId());
    }

    private Money calculateAmountTo() {
        final CurrencyService currencyService = new CurrencyService(activity);

        return currencyService.doCurrencyExchange(getDestinationCurrencyId(), transactionEntity.getAmount(),
                getSourceCurrencyId());
    }

    private void cancelActivity() {
        activity.setResult(Activity.RESULT_CANCELED);
        activity.finish();
    }

    /**
     * Create a split item using the amount and category from the existing transaction.
     * if there is a Category selected, and we are enabling Splits, use the selected category for
     * the initial split record.
     */
    private ISplitTransaction createSplitFromTransaction() {
        // Add the new split record of the same type as the parent.
        final ISplitTransaction entity = SplitItemFactory.create(mSplitCategoryEntityName, transactionEntity.getTransactionType());

        entity.setAmount(transactionEntity.getAmount());

        if (transactionEntity.hasCategory()) {
            entity.setCategoryId(transactionEntity.getCategoryId());
        }

        return entity;
    }

    private void displayAmountFrom() {
        final Money amount = null == transactionEntity.getAmount()
                ? MoneyFactory.fromDouble(0)
                : transactionEntity.getAmount();

        displayAmountFormatted(viewHolder.txtAmount, amount, getSourceCurrencyId());
    }

    private void displayAmountTo() {
        // if the currencies are the same, show only one Amount field.
        final int amountToVisibility = areCurrenciesSame() ? View.GONE : View.VISIBLE;
        viewHolder.tableRowAmountTo.setVisibility(amountToVisibility);

        final Money amount = null == transactionEntity.getAmountTo() ? MoneyFactory.fromDouble(0) : transactionEntity.getAmountTo();
        //displayAmountTo(amount);

        displayAmountFormatted(viewHolder.txtAmountTo, amount, getDestinationCurrencyId());
    }

    private void displayAmountFormatted(final TextView view, final Money amount, final Integer currencyId) {
        if (null == amount) return;
        if (null == currencyId || Constants.NOT_SET == currencyId) return;

        final CurrencyService currencyService = new CurrencyService(activity);

        final String amountDisplay = currencyService.getCurrencyFormatted(currencyId, amount);

        view.setText(amountDisplay);
        view.setTag(amount.toString());
    }

    private MmxBaseFragmentActivity getActivity() {
        return activity;
    }

    private MmxBaseFragmentActivity getContext() {
        return activity;
    }

    private ArrayList<ISplitTransaction> getSplitTransactions() {
        if (null == mSplitTransactions) {
            mSplitTransactions = new ArrayList<>();
        }
        return mSplitTransactions;
    }

    private String getUserDateFormat() {
        if (TextUtils.isEmpty(mUserDateFormat)) {
            mUserDateFormat = dateTimeUtilsLazy.get().getUserDatePattern(activity);
        }
        return mUserDateFormat;
    }

    /**
     * Returning from the Split Categories form after OK button was pressed.
     */
    private void onSplitConfirmed(final List<ISplitTransaction> splits) {
        if (splits.isEmpty()) {
            // All split categories removed.
            resetCategory();
            setSplit(false);
            return;
        }

        // if there is only one split item, e it immediately.
        if (1 == splits.size()) {
            convertOneSplitIntoRegularTransaction();
            return;
        }

        // Multiple split categories exist at this point.

        resetCategory();

        // indicate that the split is active & refresh display
        setSplit(true);

        // Use the sum of all splits as the Amount.
        Money splitSum = MoneyFactory.fromString("0");
        for (int i = 0; i < splits.size(); i++) {
            splitSum = splitSum.add(splits.get(i).getAmount());
        }
        transactionEntity.setAmount(splitSum);
        displayAmountFrom();
    }

    /**
     * The user is switching to Transfer transaction type.
     */
    private void onTransferSelected() {
        // Check whether to delete split categories, if any.
        if (hasSplitCategories()) {
            // Prompt the user to confirm deleting split categories.
            // Use DialogFragment in order to redraw the binaryDialog when switching device orientation.

            final DialogFragment dialog = new YesNoDialog();
            final Bundle args = new Bundle();
            args.putString("title", activity.getString(R.string.warning));
            args.putString("message", activity.getString(R.string.no_transfer_splits));
            args.putString("purpose", YesNoDialog.PURPOSE_DELETE_SPLITS_WHEN_SWITCHING_TO_TRANSFER);
            dialog.setArguments(args);

            dialog.show(activity.getSupportFragmentManager(), "tag");

            // Dialog result is handled in onEvent handlers in the listeners.

            return;
        }

        // un-check split.
        setSplit(false);

        // Set the destination account, if not already.
        if (null == transactionEntity.getAccountToId() || transactionEntity.getAccountToId().equals(Constants.NOT_SET)) {
            if (0 == mAccountIdList.size()) {
                // notify the user and exit.
                new MaterialDialog.Builder(activity)
                        .title(R.string.warning)
                        .content(R.string.no_accounts_available_for_selection)
                        .positiveText(android.R.string.ok)
                        .show();
                return;
            } else {
                transactionEntity.setAccountToId(mAccountIdList.get(0));
            }
        }

        // calculate AmountTo only if not set previously.
        if (transactionEntity.getAmountTo().isZero()) {
            final Money amountTo = calculateAmountTo();
            transactionEntity.setAmountTo(amountTo);
        }
        displayAmountTo();
    }

    private void resetCategory() {
        // Reset the Sub/Category on the transaction.
        transactionEntity.setCategoryId(Constants.NOT_SET);
    }

    private void showDate(final Date dateTime) {
        // Constants.LONG_DATE_MEDIUM_DAY_PATTERN
        final String format = "EEE, " + getUserDateFormat();
        //String display = dateTime.toString(format);
        final String display = dateTimeUtilsLazy.get().format(dateTime, format);
        viewHolder.dateTextView.setText(display);
    }

    private void showSplitCategoriesForm(final String datasetName) {
        // If there are no splits, use the current values for the initial split record.
        final List<ISplitTransaction> splitsToShow = getSplitTransactions();
        if (getSplitTransactions().isEmpty()) {
            final ISplitTransaction currentTransaction = createSplitFromTransaction();
            splitsToShow.add(currentTransaction);
        }

        final Intent intent = new Intent(activity, SplitCategoriesActivity.class);
        intent.putExtra(SplitCategoriesActivity.KEY_DATASET_TYPE, datasetName);
        intent.putExtra(SplitCategoriesActivity.KEY_TRANSACTION_TYPE, transactionEntity.getTransactionType().getCode());
        intent.putExtra(SplitCategoriesActivity.KEY_SPLIT_TRANSACTION, Parcels.wrap(splitsToShow));
        intent.putExtra(SplitCategoriesActivity.KEY_SPLIT_TRANSACTION_DELETED, Parcels.wrap(mSplitTransactionsDeleted));

        final Integer fromCurrencyId = getSourceCurrencyId();
        intent.putExtra(SplitCategoriesActivity.KEY_CURRENCY_ID, fromCurrencyId);

        activity.startActivityForResult(intent, RequestCodes.SPLIT_TX);
    }

    /**
     * If the user wants to reset the Split but there are multiple records, show the notice
     * that the records must be adjusted manually.
     */
    private void showSplitResetNotice() {
        new MaterialDialog.Builder(activity)
                .title(R.string.split_transaction)
                .content(R.string.split_reset_notice)
                .positiveText(android.R.string.ok)
                .show();
    }

    private void setDate(final Date dateTime) {
        mDirty = true;

        transactionEntity.setDate(dateTime);

        showDate(dateTime);
    }

    private void updateSplitButton() {
        // update Split button
        final int buttonColour;
        final int buttonBackground;
        if (mSplitSelected) {
//            buttonColour = R.color.button_foreground_active;
            buttonColour = R.color.md_accent;
            buttonBackground = R.color.md_primary;
            // #188: if there is a Category selected and we are switching to Split Categories.
        } else {
            buttonColour = R.color.button_foreground_inactive;
            buttonBackground = new UIHelper(activity).isUsingDarkTheme()
                    ? R.color.button_background_inactive_dark
                    : R.color.button_background_inactive_light;
        }
        viewHolder.splitButton.setTextColor(ContextCompat.getColor(activity, buttonColour));
        viewHolder.splitButton.setBackgroundColor(ContextCompat.getColor(activity, buttonBackground));
    }
}
