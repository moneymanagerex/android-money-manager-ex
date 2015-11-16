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
package com.money.manager.ex.transactions;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.money.manager.ex.Constants;
import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.PayeeActivity;
import com.money.manager.ex.R;
import com.money.manager.ex.SplitTransactionsActivity;
import com.money.manager.ex.account.AccountListActivity;
import com.money.manager.ex.database.ITransactionEntity;
import com.money.manager.ex.database.MmexOpenHelper;
import com.money.manager.ex.datalayer.CategoryRepository;
import com.money.manager.ex.datalayer.PayeeRepository;
import com.money.manager.ex.datalayer.SubcategoryRepository;
import com.money.manager.ex.domainmodel.Category;
import com.money.manager.ex.domainmodel.Subcategory;
import com.money.manager.ex.servicelayer.AccountService;
import com.money.manager.ex.common.BaseFragmentActivity;
import com.money.manager.ex.common.CategoryListActivity;
import com.money.manager.ex.common.InputAmountDialog;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.datalayer.AccountRepository;
import com.money.manager.ex.database.QueryCategorySubCategory;
import com.money.manager.ex.database.TablePayee;
import com.money.manager.ex.datalayer.AccountTransactionRepository;
import com.money.manager.ex.domainmodel.Account;
import com.money.manager.ex.domainmodel.Payee;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.utils.DateUtils;
import com.shamanland.fonticon.FontIconView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

/**
 * Functions shared between Checking Account activity and Recurring Transactions activity.
 */
public class EditTransactionCommonFunctions {

    public static final int REQUEST_PICK_PAYEE = 1;
    public static final int REQUEST_PICK_ACCOUNT = 2;
    public static final int REQUEST_PICK_CATEGORY = 3;
    public static final int REQUEST_PICK_SPLIT_TRANSACTION = 4;

    public static final String DATEPICKER_TAG = "datepicker";

    public EditTransactionCommonFunctions(Context context, BaseFragmentActivity parentActivity,
                                          ITransactionEntity transactionEntity) {
        super();

        mContext = context.getApplicationContext();
        mParent = parentActivity;
        this.transactionEntity = transactionEntity;
    }

    // Model
    public ITransactionEntity transactionEntity; // todo: replace all fields with this entity object.
    public String mDate = "";   // datepicker value
    public String status = null;
    public String[] mStatusItems, mStatusValues;    // arrays to manage trans.code and status
    public int payeeId = Constants.NOT_SET; // Payee
    public String payeeName;
//    public int categoryId = Constants.NOT_SET;  // Category
    public int subCategoryId = Constants.NOT_SET;
    public Money amountTo = MoneyFactory.fromDouble(0);
    public Money amount = MoneyFactory.fromDouble(0); // amount
    public int accountId = Constants.NOT_SET, toAccountId = Constants.NOT_SET;  // accounts
    public String mToAccountName;
    public String mNotes = "";
    public String mTransNumber = "";

    public List<Account> AccountList;
    public ArrayList<String> mAccountNameList = new ArrayList<>();
    public ArrayList<Integer> mAccountIdList = new ArrayList<>();
    public TransactionTypes transactionType = TransactionTypes.Withdrawal;
    public TransactionTypes previousTransactionType = TransactionTypes.Withdrawal;
    public String categoryName, subCategoryName;

    public ArrayList<ITransactionEntity> mSplitTransactions;
    public ArrayList<ITransactionEntity> mSplitTransactionsDeleted;

    // Controls
    public ViewHolder viewHolder;
    public ViewGroup tableRowPayee, tableRowAmountTo, tableRowAccountTo;
    public TextView accountFromLabel, txtToAccount;
    public TextView amountHeaderTextView, amountToHeaderTextView;
    public FontIconView removePayeeButton, splitButton;
    public RelativeLayout withdrawalButton, depositButton, transferButton;
    public ImageButton btnTransNumber;
    public EditText edtTransNumber, edtNotes;

    // Other

    private Context mContext;
    private BaseFragmentActivity mParent;
    private boolean mSplitSelected;
    private boolean mDirty = false; // indicate whether the data has been modified by the user.
    private String mDatasetName;

    public void findControls() {
        this.viewHolder = new ViewHolder();

        // Date
        viewHolder.txtSelectDate = (TextView) mParent.findViewById(R.id.textViewDate);

        // Status
        viewHolder.spinStatus = (Spinner) mParent.findViewById(R.id.spinnerStatus);

        // Payee
        this.viewHolder.txtSelectPayee = (TextView) mParent.findViewById(R.id.textViewPayee);
        removePayeeButton = (FontIconView) mParent.findViewById(R.id.removePayeeButton);
        tableRowPayee = (ViewGroup) mParent.findViewById(R.id.tableRowPayee);

        // Category / Split
        splitButton = (FontIconView) mParent.findViewById(R.id.splitButton);
        this.viewHolder.categoryTextView = (TextView) mParent.findViewById(R.id.textViewCategory);

        // Account
        viewHolder.spinAccount = (Spinner) mParent.findViewById(R.id.spinnerAccount);
        accountFromLabel = (TextView) mParent.findViewById(R.id.accountFromLabel);

        tableRowAccountTo = (ViewGroup) mParent.findViewById(R.id.tableRowAccountTo);
        txtToAccount = (TextView) mParent.findViewById(R.id.textViewToAccount);
        viewHolder.spinAccountTo = (Spinner) mParent.findViewById(R.id.spinnerToAccount);

        // Amounts
        amountHeaderTextView = (TextView) mParent.findViewById(R.id.textViewHeaderAmount);
        amountToHeaderTextView = (TextView) mParent.findViewById(R.id.textViewHeaderAmountTo);

        viewHolder.txtAmount = (TextView) mParent.findViewById(R.id.textViewAmount);
        viewHolder.txtAmountTo = (TextView) mParent.findViewById(R.id.textViewTotAmount);
        tableRowAmountTo = (ViewGroup) mParent.findViewById(R.id.tableRowAmountTo);

        // Transaction Type
        withdrawalButton = (RelativeLayout) mParent.findViewById(R.id.withdrawalButton);
        depositButton = (RelativeLayout) mParent.findViewById(R.id.depositButton);
        transferButton = (RelativeLayout) mParent.findViewById(R.id.transferButton);

    }

    public void displayAmountFormatted(TextView view, Money amount, Integer accountId) {
        if (amount == null) return;

        // take currency id
        Integer currencyId = null;

        int index = mAccountIdList.indexOf(accountId);

        if (index != Constants.NOT_SET) {
            currencyId = this.AccountList.get(index).getCurrencyId();
        }

        CurrencyService currencyService = new CurrencyService(mContext);
        String amountDisplay;

        if (currencyId == null) {
            amountDisplay = currencyService.getBaseCurrencyFormatted(amount);
        } else {
            amountDisplay = currencyService.getCurrencyFormatted(currencyId, amount);
        }
        view.setText(amountDisplay);
        view.setTag(amount.toString());
    }

    /**
     * Loads info for Category and Subcategory
     *
     * @return A boolean indicating whether the operation was successful.
     */
    public boolean displayCategoryName() {
        if(this.transactionEntity.getCategoryId() <= 0 && subCategoryId <= 0) return false;

        CategoryRepository categoryRepository = new CategoryRepository(getContext());
        Category category = categoryRepository.load(this.transactionEntity.getCategoryId());
        if (category != null) {
            this.categoryName = category.getName();
        } else {
            this.categoryName = null;
        }

        SubcategoryRepository subRepo = new SubcategoryRepository(getContext());
        Subcategory subcategory = subRepo.load(subCategoryId);
        if (subcategory != null) {
            this.subCategoryName = subcategory.getName();
        } else {
            this.subCategoryName = null;
        }

        return true;
    }

    /**
     * Get content values for saving data.
     * @param isTransfer
     * @return
     */
    public ContentValues getContentValues(boolean isTransfer) {
        ContentValues values = new ContentValues();

        // Date
        String transactionDate = DateUtils.getIsoStringDate((Date) viewHolder.txtSelectDate.getTag());
        values.put(ITransactionEntity.TRANSDATE, transactionDate);

        // Transaction Type
        values.put(ITransactionEntity.TRANSCODE, this.getTransactionType());

        // Status
        values.put(ITransactionEntity.STATUS, this.status);

        // Amount
        Money amount = MoneyFactory.fromString(viewHolder.txtAmount.getTag().toString());
        values.put(ITransactionEntity.TRANSAMOUNT, amount.toDouble());

        // Amount To
        Money amountTo;
        if (isTransfer) {
            amountTo = MoneyFactory.fromString(viewHolder.txtAmountTo.getTag().toString());
        } else {
            // Use the Amount value.
            amountTo = MoneyFactory.fromString(viewHolder.txtAmount.getTag().toString());
        }
        values.put(ITransactionEntity.TOTRANSAMOUNT, amountTo.toDouble());

        // Accounts & Payee
        values.put(ITransactionEntity.ACCOUNTID, this.accountId);
        if (isTransfer) {
            values.put(ITransactionEntity.TOACCOUNTID, this.toAccountId);
            values.put(ITransactionEntity.PAYEEID, Constants.NOT_SET);
        } else {
            values.put(ITransactionEntity.TOACCOUNTID, Constants.NOT_SET);
            values.put(ITransactionEntity.PAYEEID, this.payeeId);
        }

        // Category and subcategory
        if (isTransfer || isSplitSelected()) {
//            this.categoryId = Constants.NOT_SET;
            this.transactionEntity.setCategoryId(Constants.NOT_SET);
            this.subCategoryId = Constants.NOT_SET;
        }
        values.put(ITransactionEntity.CATEGID, this.transactionEntity.getCategoryId());
        values.put(ITransactionEntity.SUBCATEGID, this.subCategoryId);

        values.put(ITransactionEntity.FOLLOWUPID, Constants.NOT_SET);
        values.put(ITransactionEntity.TRANSACTIONNUMBER, this.edtTransNumber.getText().toString());
        values.put(ITransactionEntity.NOTES, this.edtNotes.getText().toString());

        return values;
    }

    public Context getContext() {
        return mContext;
    }

    public String getTransactionType() {
        if (transactionType == null) {
            return null;
        }

        return transactionType.name();
    }

    public FontIconView getDepositButtonIcon() {
        return (FontIconView) mParent.findViewById(R.id.depositButtonIcon);
    }

    public int getDestinationCurrencyId() {
        return this.AccountList.get(
                mAccountIdList.indexOf(this.toAccountId)).getCurrencyId();
    }

    public boolean getDirty() {
        return mDirty;
    }

    public FontIconView getTransferButtonIcon() {
        return (FontIconView) mParent.findViewById(R.id.transferButtonIcon);
    }

    public FontIconView getWithdrawalButtonIcon() {
        return (FontIconView) mParent.findViewById(R.id.withdrawalButtonIcon);
    }

    public boolean hasSplitCategories() {
        return mSplitTransactions != null && !mSplitTransactions.isEmpty();
    }

    /**
     * Initialize account selectors.
     */
    public void initAccountSelectors() {
//        Core core = new Core(context);
        AppSettings settings = new AppSettings(mContext);
//        LookAndFeelSettings settings = new AppSettings(context).getLookAndFeelSettings();

        // account list to populate the spin
        AccountService accountService = new AccountService(mContext);
        this.AccountList = accountService.getTransactionAccounts(
                settings.getLookAndFeelSettings().getViewOpenAccounts(),
                settings.getLookAndFeelSettings().getViewFavouriteAccounts());
        if (this.AccountList == null) return;

        for(Account account : this.AccountList) {
            mAccountNameList.add(account.getName());
            mAccountIdList.add(account.getId());
        }

        AccountRepository accountRepository = new AccountRepository(mContext);
        addMissingAccountToSelectors(accountRepository, accountId);
        addMissingAccountToSelectors(accountRepository, toAccountId);
        // add the default account, if any.
        String defaultAccountString = settings.getGeneralSettings().getDefaultAccountId();
        // Set the current account, if not set already.
        if ((accountId == Constants.NOT_SET) && !TextUtils.isEmpty(defaultAccountString)) {
            int defaultAccount = Integer.parseInt(defaultAccountString);
            addMissingAccountToSelectors(accountRepository, defaultAccount);
            // Set the default account as the active account.
            accountId = defaultAccount;
        }

        // create adapter for spinAccount
        ArrayAdapter<String> accountAdapter = new ArrayAdapter<>(mParent,
                android.R.layout.simple_spinner_item, mAccountNameList);

        accountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        viewHolder.spinAccount.setAdapter(accountAdapter);
        // select current value
        if (mAccountIdList.indexOf(accountId) >= 0) {
            viewHolder.spinAccount.setSelection(mAccountIdList.indexOf(accountId), true);
        }
        viewHolder.spinAccount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setDirty(true);

                if ((position >= 0) && (position <= mAccountIdList.size())) {
                    accountId = mAccountIdList.get(position);
                    Money amount = MoneyFactory.fromString(viewHolder.txtAmount.getTag().toString());
                    displayAmountFormatted(viewHolder.txtAmount, amount, accountId);
                    refreshControlHeaders();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // to account
        accountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        viewHolder.spinAccountTo.setAdapter(accountAdapter);
        if (toAccountId != Constants.NOT_SET && mAccountIdList.indexOf(toAccountId) >= 0) {
            viewHolder.spinAccountTo.setSelection(mAccountIdList.indexOf(toAccountId), true);
        }
        viewHolder.spinAccountTo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setDirty(true);

                if ((position >= 0) && (position <= mAccountIdList.size())) {
                    toAccountId = mAccountIdList.get(position);
                    Money amount = MoneyFactory.fromString(viewHolder.txtAmountTo.getTag().toString());
                    displayAmountFormatted(viewHolder.txtAmountTo, amount , toAccountId);
                    refreshControlHeaders();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    public void initAmountSelectors() {
        View.OnClickListener onClickAmount = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Get currency id from the account for which the amount has been modified.
                Integer currencyId = null;
                int selectedPosition;
                if (v.equals(viewHolder.txtAmountTo)) {
                    selectedPosition = viewHolder.spinAccountTo.getSelectedItemPosition();
                    if (selectedPosition >= 0 && selectedPosition < AccountList.size()) {
                        currencyId = AccountList.get(selectedPosition).getCurrencyId();
                    }
                } else {
                    // Amount.
                    selectedPosition = viewHolder.spinAccount.getSelectedItemPosition();
                    if (selectedPosition >= 0 && selectedPosition < AccountList.size()) {
                        currencyId = AccountList.get(selectedPosition).getCurrencyId();
                    }
                }
                Money amount = MoneyFactory.fromString(v.getTag().toString());
                InputAmountDialog dialog = InputAmountDialog.getInstance(v.getId(),
                        amount, currencyId);
                dialog.show(mParent.getSupportFragmentManager(), dialog.getClass().getSimpleName());

                // The result is received in onFinishedInputAmountDialog.
            }
        };

        // amount
        displayAmountFormatted(viewHolder.txtAmount, amount, accountId);
        viewHolder.txtAmount.setOnClickListener(onClickAmount);

        // amount to
        displayAmountFormatted(viewHolder.txtAmountTo, amountTo, toAccountId);
        viewHolder.txtAmountTo.setOnClickListener(onClickAmount);
    }

    /**
     * Initialize Category selector.
     * @param datasetName name of the dataset (TableBudgetSplitTransactions.class.getSimpleName())
     */
    public void initCategoryControls(final String datasetName) {
        // keep the dataset name for later.
        this.mDatasetName = datasetName;

        this.viewHolder.categoryTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isSplitSelected()) {
                    // select single category.
                    Intent intent = new Intent(mParent, CategoryListActivity.class);
                    intent.setAction(Intent.ACTION_PICK);
                    mParent.startActivityForResult(intent, REQUEST_PICK_CATEGORY);
                } else {
                    // select split categories.
                    showSplitCategoriesForm(mDatasetName);
                }

                // results are handled in onActivityResult.
            }
        });
    }

    public void initDateSelector() {
        if (!(TextUtils.isEmpty(mDate))) {
            try {
                viewHolder.txtSelectDate.setTag(new SimpleDateFormat(Constants.PATTERN_DB_DATE)
                        .parse(mDate));
            } catch (ParseException e) {
                ExceptionHandler handler = new ExceptionHandler(mContext, this);
                handler.handle(e, "parsing the date");
            }
        } else {
            viewHolder.txtSelectDate.setTag(Calendar.getInstance().getTime());
        }
        final DateUtils dateUtils = new DateUtils(mContext);
        dateUtils.formatExtendedDate(viewHolder.txtSelectDate, (Date) viewHolder.txtSelectDate.getTag());

        viewHolder.txtSelectDate.setOnClickListener(new View.OnClickListener() {
            public DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                    setDirty(true);

                    try {
                        Date date = new SimpleDateFormat(Constants.PATTERN_DB_DATE,
                            MoneyManagerApplication.getInstanceApp().getAppLocale())
                            .parse(Integer.toString(year) + "-" + Integer.toString(monthOfYear + 1) + "-" + Integer.toString(dayOfMonth));
                        viewHolder.txtSelectDate.setTag(date);
                        dateUtils.formatExtendedDate(viewHolder.txtSelectDate, date);
                    } catch (Exception e) {
                        ExceptionHandler handler = new ExceptionHandler(mParent, this);
                        handler.handle(e, "setting the date");
                    }
                }
            };

            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime((Date) viewHolder.txtSelectDate.getTag());
                DatePickerDialog dialog = DatePickerDialog.newInstance(mDateSetListener,
                    calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), false);
                dialog.setCloseOnSingleTapDay(true);

                // Set first day of the week.
                int firstDayOfWeek = Calendar.getInstance(MoneyManagerApplication.getInstanceApp().getAppLocale())
                    .getFirstDayOfWeek();
//                dialog.setFirstDayOfWeek(Calendar.MONDAY);
                dialog.setFirstDayOfWeek(firstDayOfWeek);

                dialog.show(mParent.getSupportFragmentManager(), DATEPICKER_TAG);
            }
        });

    }

    public void initNotesControls() {
        edtNotes = (EditText) mParent.findViewById(R.id.editTextNotes);
        if (!(TextUtils.isEmpty(mNotes))) {
            edtNotes.setText(mNotes);
        }

        edtNotes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                setDirty(true);
            }
        });
    }

    public void initPayeeControls() {
        this.viewHolder.txtSelectPayee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mParent, PayeeActivity.class);
                intent.setAction(Intent.ACTION_PICK);
                mParent.startActivityForResult(intent, REQUEST_PICK_PAYEE);

                // the result is handled in onActivityResult
            }
        });

        removePayeeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDirty(true);

                payeeId = Constants.NOT_SET;
                payeeName = "";

                refreshPayeeName();
            }
        });
    }

    public void initSplitCategories() {
        // Split button
        splitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSplit(!mSplitSelected);

                // if the split has just been set, show the splits dialog immediately?
                if (isSplitSelected()) {
                    createSplitForCategoryAndAmount();
                    showSplitCategoriesForm(mDatasetName);
                }
            }
        });

        onSplitSet();
    }

    public void initStatusSelector() {
        mStatusItems = mContext.getResources().getStringArray(R.array.status_items);
        mStatusValues = mContext.getResources().getStringArray(R.array.status_values);

        // create adapter for spinnerStatus
        ArrayAdapter<String> adapterStatus = new ArrayAdapter<>(mParent,
                android.R.layout.simple_spinner_item, mStatusItems);
        adapterStatus.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        viewHolder.spinStatus.setAdapter(adapterStatus);

        // select current value
        if (!(TextUtils.isEmpty(status))) {
            if (Arrays.asList(mStatusValues).indexOf(status) >= 0) {
                viewHolder.spinStatus.setSelection(Arrays.asList(mStatusValues).indexOf(status), true);
            }
        } else {
//            status = (String) this.spinStatus.getSelectedItem();
            status = mStatusValues[0];
        }
        viewHolder.spinStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if ((position >= 0) && (position <= mStatusValues.length)) {
                    String selectedStatus = mStatusValues[position];
                    // If Status has been changed manually, mark data as dirty.
                    if (!selectedStatus.equalsIgnoreCase(EditTransactionCommonFunctions.this.status)) {
                        setDirty(true);
                    }
                    EditTransactionCommonFunctions.this.status = selectedStatus;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    public void initTransactionNumberControls() {
        // Transaction number

        edtTransNumber = (EditText) mParent.findViewById(R.id.editTextTransNumber);
        if (!TextUtils.isEmpty(mTransNumber)) {
            edtTransNumber.setText(mTransNumber);
        }

        // handle change
        edtTransNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                setDirty(true);
            }
        });

        btnTransNumber = (ImageButton) mParent.findViewById(R.id.buttonTransNumber);
        btnTransNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MmexOpenHelper helper = MmexOpenHelper.getInstance(mContext);
                AccountTransactionRepository repo = new AccountTransactionRepository(mContext);

                String query = "SELECT MAX(CAST(" + ITransactionEntity.TRANSACTIONNUMBER + " AS INTEGER)) FROM " +
                    repo.getSource() + " WHERE " +
                    ITransactionEntity.ACCOUNTID + "=?";

                Cursor cursor = helper.getReadableDatabase().rawQuery(query,
                    new String[]{Integer.toString(accountId)});
                if (cursor == null) return;

                if (cursor.moveToFirst()) {
                    String transNumber = cursor.getString(0);
                    if (TextUtils.isEmpty(transNumber)) {
                        transNumber = "0";
                    }
                    if ((!TextUtils.isEmpty(transNumber)) && TextUtils.isDigitsOnly(transNumber)) {
                        try {
                            Money transactionNumber = MoneyFactory.fromString(transNumber);
                            edtTransNumber.setText(transactionNumber.add(MoneyFactory.fromString("1"))
                                .toString());
                        } catch (Exception e) {
                            ExceptionHandler handler = new ExceptionHandler(mContext, this);
                            handler.handle(e, "adding transaction number");
                        }
                    }
                }
                cursor.close();
            }
        });

    }

    public void initTransactionTypeSelector() {

        // Handle click events.

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDirty(true);

                // find which transaction type this is.
                TransactionTypes type = (TransactionTypes) v.getTag();
                selectTransactionType(type);
            }
        };

        if (withdrawalButton != null) {
            withdrawalButton.setTag(TransactionTypes.Withdrawal);

            withdrawalButton.setOnClickListener(onClickListener);
        }
        if (depositButton != null) {
            depositButton.setTag(TransactionTypes.Deposit);

            depositButton.setOnClickListener(onClickListener);
        }
        if (transferButton != null) {
            transferButton.setTag(TransactionTypes.Transfer);

            transferButton.setOnClickListener(onClickListener);
        }

        // Check if the transaction type has been set (for example, when editing an existing
        // transaction).
        TransactionTypes current = transactionType == null
                ? TransactionTypes.Withdrawal
                : transactionType;
        selectTransactionType(current);
    }

    /**
     * Indicate whether the Split Categories is selected/checked.
     * @return boolean
     */
    public boolean isSplitSelected() {
        return mSplitSelected;
    }

    public boolean onActionCancelClick() {
        if (getDirty()) {
            final MaterialDialog dialog = new MaterialDialog.Builder(mParent)
                    .title(android.R.string.cancel)
                    .content(R.string.transaction_cancel_confirm)
                    .positiveText(R.string.discard)
                    .negativeText(R.string.keep_editing)
                    .cancelable(false)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            cancelActivity();

                            super.onPositive(dialog);
                        }

                        @Override
                        public void onNegative(MaterialDialog dialog) {
                            super.onNegative(dialog);
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

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case EditTransactionCommonFunctions.REQUEST_PICK_PAYEE:
                if ((resultCode != Activity.RESULT_OK) || (data == null)) return;

                setDirty(true);

                payeeId = data.getIntExtra(PayeeActivity.INTENT_RESULT_PAYEEID, Constants.NOT_SET);
                payeeName = data.getStringExtra(PayeeActivity.INTENT_RESULT_PAYEENAME);
                // select last category used from payee. Only if category has not been entered earlier.
                if (!isSplitSelected() && this.transactionEntity.getCategoryId() != null
                    && this.transactionEntity.getCategoryId() == Constants.NOT_SET) {
                    if (setCategoryFromPayee(payeeId)) {
                        refreshCategoryName(); // refresh UI
                    }
                }
                // refresh UI
                refreshPayeeName();
                break;

            case EditTransactionCommonFunctions.REQUEST_PICK_ACCOUNT:
                if ((resultCode != Activity.RESULT_OK) || (data == null)) return;

                setDirty(true);

                toAccountId = data.getIntExtra(AccountListActivity.INTENT_RESULT_ACCOUNTID, Constants.NOT_SET);
                mToAccountName = data.getStringExtra(AccountListActivity.INTENT_RESULT_ACCOUNTNAME);
                break;

            case EditTransactionCommonFunctions.REQUEST_PICK_CATEGORY:
                if ((resultCode != Activity.RESULT_OK) || (data == null)) return;

                setDirty(true);

                this.transactionEntity.setCategoryId(data.getIntExtra(CategoryListActivity.INTENT_RESULT_CATEGID, Constants.NOT_SET));
                categoryName = data.getStringExtra(CategoryListActivity.INTENT_RESULT_CATEGNAME);
                subCategoryId = data.getIntExtra(CategoryListActivity.INTENT_RESULT_SUBCATEGID, Constants.NOT_SET);
                subCategoryName = data.getStringExtra(CategoryListActivity.INTENT_RESULT_SUBCATEGNAME);
                // refresh UI category
                refreshCategoryName();
                break;

            case EditTransactionCommonFunctions.REQUEST_PICK_SPLIT_TRANSACTION:
                if ((resultCode != Activity.RESULT_OK) || (data == null)) return;

                setDirty(true);

                mSplitTransactions = data.getParcelableArrayListExtra(SplitTransactionsActivity.INTENT_RESULT_SPLIT_TRANSACTION);
                if (mSplitTransactions != null && mSplitTransactions.size() > 0) {
                    Money splitSum = MoneyFactory.fromString("0");
                    for (int i = 0; i < mSplitTransactions.size(); i++) {
                        splitSum = splitSum.add(mSplitTransactions.get(i).getAmount());
                    }
                    displayAmountFormatted(viewHolder.txtAmount, splitSum,
                            !transactionType.equals(TransactionTypes.Transfer)
                                    ? accountId
                                    : toAccountId);
                }
                // deleted item
                if (data.getParcelableArrayListExtra(SplitTransactionsActivity.INTENT_RESULT_SPLIT_TRANSACTION_DELETED) != null) {
                    mSplitTransactionsDeleted = data.getParcelableArrayListExtra(
                            SplitTransactionsActivity.INTENT_RESULT_SPLIT_TRANSACTION_DELETED);
                }
                break;
        }
    }

    public void onFinishedInputAmountDialog(int id, Money amount) {
        View view = mParent.findViewById(id);
        if (view == null || !(view instanceof TextView)) return;

        setDirty(true);

        int accountId;
        boolean isTransfer = transactionType.equals(TransactionTypes.Transfer);
        boolean isSourceAmount = id == R.id.textViewAmount;

        // Update amount value.
        if (isSourceAmount) {
            this.amount = amount;
        } else {
            this.amountTo = amount;
        }

        if (isTransfer) {
            AccountRepository repo = new AccountRepository(mContext);
//            Integer fromCurrencyId = getSourceCurrencyId();
            Integer fromCurrencyId = repo.loadCurrencyIdFor(this.accountId);
            Integer toCurrencyId = getDestinationCurrencyId();
            if (fromCurrencyId.equals(toCurrencyId)) {
                // Same currency.
                // Modify both values if the transfer is in the same currency.
                this.amount = amount;
                this.amountTo = amount;

                refreshSourceAmount();
                refreshDestinationAmount();
                // Exit here.
                return;
            } else {
                // Different currency.
                // Convert the value and write the amount into the other input box.
                try {
                    convertAndDisplayAmount(isSourceAmount, fromCurrencyId, toCurrencyId, amount);
                } catch (Exception e) {
                    ExceptionHandler handler = new ExceptionHandler(mParent, mParent);
                    handler.handle(e, "converting the value for transfer");
                }
            }
        }

        // Display the formatted amount in selected field.
        accountId = isSourceAmount ? this.accountId : this.toAccountId;
        displayAmountFormatted(((TextView) view), amount, accountId);
    }

    /**
     * Handle the controls after the split is checked.
     */
    public void onSplitSet() {
        // update category field
        refreshCategoryName();

        // enable/disable Amount field.
        viewHolder.txtAmount.setEnabled(!mSplitSelected);
        viewHolder.txtAmountTo.setEnabled(!mSplitSelected);

        int buttonColour, buttonBackground;
        if (isSplitSelected()) {
            buttonColour = R.color.button_foreground_active;
            buttonBackground = R.color.button_background_active;
            // #188: if there is a Category selected and we are switching to Split Categories.

        } else {
            buttonColour = R.color.button_foreground_inactive;
            Core core = new Core(mContext);
            buttonBackground = core.usingDarkTheme()
                ? R.color.button_background_inactive_dark
                : R.color.button_background_inactive_light;
        }
        splitButton.setTextColor(mContext.getResources().getColor(buttonColour));
        splitButton.setBackgroundColor(mContext.getResources().getColor(buttonBackground));
    }

    /**
     * Reflect the transaction type change. Show and hide controls appropriately.
     */
    public void onTransactionTypeChange(TransactionTypes transactionType) {
        boolean isTransfer = transactionType.equals(TransactionTypes.Transfer);

        accountFromLabel.setText(isTransfer ? R.string.from_account : R.string.account);
        tableRowAccountTo.setVisibility(isTransfer ? View.VISIBLE : View.GONE);
        tableRowPayee.setVisibility(!isTransfer ? View.VISIBLE : View.GONE);
        tableRowAmountTo.setVisibility(isTransfer ? View.VISIBLE : View.GONE);

//        splitButton.setVisibility(isTransfer ? View.GONE : View.VISIBLE);
//        this.viewHolder.categoryTextView.setVisibility(isTransfer ? View.GONE : View.VISIBLE);

        refreshControlHeaders();
    }

    public void refreshCategoryName() {
        // validation
        if (this.viewHolder.categoryTextView == null) return;

        this.viewHolder.categoryTextView.setText("");

        if (isSplitSelected()) {
            // Split transaction. Show ...
            this.viewHolder.categoryTextView.setText("\u2026");
        } else {
            if (!TextUtils.isEmpty(categoryName)) {
                this.viewHolder.categoryTextView.setText(categoryName);
                if (!TextUtils.isEmpty(subCategoryName)) {
                    this.viewHolder.categoryTextView.setText(Html.fromHtml(
                        this.viewHolder.categoryTextView.getText() + " : <i>" + subCategoryName + "</i>"));
                }
            }
        }
    }

    /**
     * Update input control titles to reflect the transaction type.
     */
    public void refreshControlHeaders() {
        if (amountHeaderTextView == null || amountToHeaderTextView == null) {
            return;
        }

        if (!transactionType.equals(TransactionTypes.Transfer)) {
//            amountToHeaderTextView.setText(R.string.total_amount);
            amountHeaderTextView.setText(R.string.amount);
        } else {
            // Transfer. Adjust the headers on amount text boxes.
            int index = mAccountIdList.indexOf(accountId);
            if (index >= 0) {
                amountHeaderTextView.setText(mParent.getString(R.string.withdrawal_from,
                        this.AccountList.get(index).getName()));
            }
            index = mAccountIdList.indexOf(toAccountId);
            if (index >= 0) {
                amountToHeaderTextView.setText(mParent.getString(R.string.deposit_to,
                        this.AccountList.get(index).getName()));
            }
        }
    }

    /**
     * update UI interface with PayeeName
     */
    public void refreshPayeeName() {
        // write into text button payee name
        if (this.viewHolder.txtSelectPayee != null) {
            String text = !TextUtils.isEmpty(payeeName)
                    ? payeeName : "";

            this.viewHolder.txtSelectPayee.setText(text);
        }
    }

    public void setSplit(final boolean checked) {
        mSplitSelected = checked;
        onSplitSet();
    }

    /**
     * query info payee
     *
     * @param payeeId id payee
     * @return true if the data selected
     */
    public boolean selectPayeeName(int payeeId) {
        PayeeRepository repo = new PayeeRepository(getContext());
        Payee payee = repo.load(payeeId);
        if (payee != null) {
            this.payeeName = payee.getName();
        } else {
            this.payeeName = "";
        }

        return true;
    }

    /**
     * setCategoryFromPayee set last category used from payee
     *
     * @param payeeId Identify of payee
     * @return true if category set
     */
    public boolean setCategoryFromPayee(int payeeId) {
        boolean ret = false;
        // take data of payee
        TablePayee payee = new TablePayee();
        Cursor curPayee = mParent.getContentResolver().query(payee.getUri(),
                payee.getAllColumns(),
                Payee.PAYEEID + "=?",
                new String[]{ Integer.toString(payeeId) },
                null);
        // check cursor is valid
        if ((curPayee != null) && (curPayee.moveToFirst())) {
            // check if category is valid
            if (curPayee.getInt(curPayee.getColumnIndex(Payee.CATEGID)) != Constants.NOT_SET) {
                this.transactionEntity.setCategoryId(curPayee.getInt(curPayee.getColumnIndex(Payee.CATEGID)));
                subCategoryId = curPayee.getInt(curPayee.getColumnIndex(Payee.SUBCATEGID));
                // create instance of query
                QueryCategorySubCategory category = new QueryCategorySubCategory(mParent.getApplicationContext());
                // compose selection
                String where = "CATEGID=" + Integer.toString(this.transactionEntity.getCategoryId()) +
                    " AND SUBCATEGID=" + Integer.toString(subCategoryId);
                Cursor curCategory = mParent.getContentResolver().query(category.getUri(),
                        category.getAllColumns(), where, null, null);
                // check cursor is valid
                if ((curCategory != null) && (curCategory.moveToFirst())) {
                    // take names of category and subcategory
                    categoryName = curCategory.getString(curCategory.getColumnIndex(QueryCategorySubCategory.CATEGNAME));
                    subCategoryName = curCategory.getString(curCategory.getColumnIndex(QueryCategorySubCategory.SUBCATEGNAME));
                    // return true
                    ret = true;
                }
                if (curCategory != null) {
                    curCategory.close();
                }
            }
        }

        if (curPayee != null) {
            curPayee.close();
        }

        return ret;
    }

    public void setDirty(boolean dirty) {
        mDirty = dirty;
    }

    public void selectTransactionType(TransactionTypes transactionType) {
        this.previousTransactionType = this.transactionType;
        this.transactionType = transactionType;

        // Clear all buttons.

        Core core = new Core(mContext);
        int backgroundInactive = core.getColourAttribute(R.attr.button_background_inactive);

        withdrawalButton.setBackgroundColor(backgroundInactive);
        getWithdrawalButtonIcon().setTextColor(mContext.getResources().getColor(R.color.material_red_700));
        depositButton.setBackgroundColor(backgroundInactive);
        getDepositButtonIcon().setTextColor(mContext.getResources().getColor(R.color.material_green_700));
        transferButton.setBackgroundColor(backgroundInactive);
        getTransferButtonIcon().setTextColor(mContext.getResources().getColor(R.color.material_grey_700));

        // Style the selected button.

        int backgroundSelected = mParent.getResources().getColor(R.color.button_background_active);
        int foregroundSelected = mContext.getResources().getColor(R.color.button_foreground_active);

        switch (transactionType) {
            case Deposit:
                depositButton.setBackgroundColor(backgroundSelected);
                getDepositButtonIcon().setTextColor(foregroundSelected);
                break;
            case Withdrawal:
                withdrawalButton.setBackgroundColor(backgroundSelected);
                getWithdrawalButtonIcon().setTextColor(foregroundSelected);
                break;
            case Transfer:
                transferButton.setBackgroundColor(backgroundSelected);
                getTransferButtonIcon().setTextColor(foregroundSelected);
                onTransferSelected();
                break;
        }

        onTransactionTypeChange(transactionType);
    }

    public boolean validateData() {
        boolean isTransfer = transactionType.equals(TransactionTypes.Transfer);

        if (isTransfer) {
            if (toAccountId == Constants.NOT_SET) {
                Core.alertDialog(mParent, R.string.error_toaccount_not_selected);
                return false;
            }
            if (toAccountId == accountId) {
                Core.alertDialog(mParent, R.string.error_transfer_to_same_account);
                return false;
            }

            // Amount To is required and has to be positive.
            Money amountTo = MoneyFactory.fromString(viewHolder.txtAmountTo.getTag().toString());
            if (amountTo.toDouble() <= 0) {
                Core.alertDialog(mParent, R.string.error_amount_must_be_positive);
                return false;
            }
        }

        // Amount is required and must be positive. Sign is determined by transaction type.
        Money amount = MoneyFactory.fromString(viewHolder.txtAmount.getTag().toString());
        if (amount.toDouble() <= 0) {
            Core.alertDialog(mParent, R.string.error_amount_must_be_positive);
            return false;
        }

        // Category is required if tx is not a split or transfer.
        if ((this.transactionEntity.getCategoryId() == null || this.transactionEntity.getCategoryId() == Constants.NOT_SET) &&
            (!isSplitSelected()) && !isTransfer) {
            Core.alertDialog(mParent, R.string.error_category_not_selected);
            return false;
        }

        // Split records must exist if split is checked.
        if (isSplitSelected()
            && (mSplitTransactions == null || mSplitTransactions.size() <= 0)) {
            Core.alertDialog(mParent, R.string.error_split_transaction_empty);
            return false;
        }

        return true;
    }

    // Remove splits when switching to Transfer

    public void confirmDeletingCategories() {
        removeAllSplitCategories();
        setSplit(false);
        transactionType = TransactionTypes.Transfer;
        onTransactionTypeChange(transactionType);
    }

    /**
     * When cancelling changing the transaction type to Transfer, revert back to the
     * previous transaction type.
     */
    public void cancelChangingTransactionToTransfer() {
        // Select the previous transaction type.
        selectTransactionType(previousTransactionType);
    }

    /**
     * After the user accepts, remove any split categories.
     */
    public void removeAllSplitCategories() {
        if(mSplitTransactions == null) return;

        for(int i = 0; i < mSplitTransactions.size(); i++) {
            ITransactionEntity split = mSplitTransactions.get(i);
            int id = split.getId();
            ArrayList<ITransactionEntity> deletedSplits = getDeletedSplitCategories();

            if(id == -1) {
                // Remove any newly created splits.
                mSplitTransactions.remove(i);
                i--;
            } else {
                // Delete any splits already in the database.
                // avoid adding duplicate records.
                if(!deletedSplits.contains(split)) {
                    deletedSplits.add(split);
                }
            }
        }
    }

    public ArrayList<ITransactionEntity> getDeletedSplitCategories() {
        if(mSplitTransactionsDeleted == null){
            mSplitTransactionsDeleted = new ArrayList<>();
        }
        return mSplitTransactionsDeleted;
    }

    // Private

    private void addMissingAccountToSelectors(AccountRepository accountRepository, int accountId) {
        if (accountId <= 0) return;

        // #316. In case the account from recurring transaction is not in the visible list,
        // load it separately.
        Account savedAccount;
        if (!mAccountIdList.contains(accountId)) {
            savedAccount = accountRepository.load(accountId);

            if (savedAccount != null) {
                this.AccountList.add(savedAccount);
                mAccountNameList.add(savedAccount.getName());
                mAccountIdList.add(savedAccount.getId());
            }
        }
    }

    private void cancelActivity() {
        mParent.setResult(Activity.RESULT_CANCELED);
        mParent.finish();
    }

    private void convertAndDisplayAmount(boolean isSourceAmount, int fromCurrencyId, int toCurrencyId,
                                         Money amount) {
        CurrencyService currencyService = new CurrencyService(mContext);
        TextView destinationTextView = viewHolder.txtAmountTo;

        if (!isSourceAmount) {
            fromCurrencyId = getDestinationCurrencyId();
            AccountRepository repo = new AccountRepository(mContext);
//            toCurrencyId = getSourceCurrencyId();
            toCurrencyId = repo.loadCurrencyIdFor(this.accountId);

            destinationTextView = viewHolder.txtAmount;
        }

        Integer destinationAccountId = isSourceAmount
                ? this.toAccountId
                : this.accountId;

        // get the destination value.
        Money destinationAmount = MoneyFactory.fromString(destinationTextView.getTag().toString());
        if (destinationAmount == null) destinationAmount = MoneyFactory.fromString("0");

        // Replace the destination value only if it is zero.
        if (destinationAmount.isZero()) {
            Money amountExchange = currencyService.doCurrencyExchange(toCurrencyId, amount, fromCurrencyId);
            displayAmountFormatted(destinationTextView, amountExchange, destinationAccountId);
        }
    }

    /**
     * Use the existing amount and,
     * if there is a Category selected, and we are enabling Splits, use the selected category for
     * the initial split record.
     */
    private void createSplitForCategoryAndAmount() {
        // Add the new split record.
        ITransactionEntity entity = SplitItemFactory.create(this.mDatasetName);
        // now use the existing amount
        entity.setAmount(this.amount);

        // Add category

        if (this.transactionEntity.getCategoryId() == null || this.transactionEntity.getCategoryId() == Constants.NOT_SET) {
            return;
        }

        // This category should not be inside the any existing splits, then.
        if (!this.getSplitTransactions().isEmpty()) {
            for (ITransactionEntity split : this.mSplitTransactions) {
                if (split.getCategoryId().equals(this.transactionEntity.getCategoryId())) {
                    return;
                }
            }
        }

        entity.setCategoryId(this.transactionEntity.getCategoryId());

        this.getSplitTransactions().add(entity);
    }

    private ArrayList<ITransactionEntity> getSplitTransactions() {
        if (mSplitTransactions == null) {
            mSplitTransactions = new ArrayList<>();
        }
        return mSplitTransactions;
    }

    private void onTransferSelected() {
        // The user is switching to Transfer transaction type.

        if(hasSplitCategories()) {
            // Prompt the user to confirm deleting split categories.
            // Use DialogFragment in order to redraw the dialog when switching device orientation.

            DialogFragment dialog = new YesNoDialog();
            Bundle args = new Bundle();
            args.putString("title", mParent.getString(R.string.warning));
            args.putString("message", mParent.getString(R.string.no_transfer_splits));
            args.putString("purpose", YesNoDialog.PURPOSE_DELETE_SPLITS_WHEN_SWITCHING_TO_TRANSFER);
            dialog.setArguments(args);
//        dialog.setTargetFragment(this, REQUEST_REMOVE_SPLIT_WHEN_TRANSACTION);

            dialog.show(mParent.getSupportFragmentManager(), "tag");

            // Dialog result is handled in onDialogPositiveClick or onDialogNegativeClick
            // in the respective activity classes!
            return;
        }

        // un-check split.
        setSplit(false);
    }

    private void refreshDestinationAmount() {
        displayAmountFormatted(viewHolder.txtAmountTo, this.amountTo, this.toAccountId);
    }

    private void refreshSourceAmount() {
        displayAmountFormatted(viewHolder.txtAmount, this.amount, this.accountId);
    }

    private void showSplitCategoriesForm(String datasetName) {
        Intent intent = new Intent(mParent, SplitTransactionsActivity.class);
        intent.putExtra(SplitTransactionsActivity.KEY_DATASET_TYPE, datasetName);
        intent.putExtra(SplitTransactionsActivity.KEY_TRANSACTION_TYPE, transactionType.getCode());
        intent.putParcelableArrayListExtra(SplitTransactionsActivity.KEY_SPLIT_TRANSACTION, mSplitTransactions);
        intent.putParcelableArrayListExtra(SplitTransactionsActivity.KEY_SPLIT_TRANSACTION_DELETED, mSplitTransactionsDeleted);

        AccountRepository repo = new AccountRepository(mContext);
        Integer fromCurrencyId = repo.loadCurrencyIdFor(this.accountId);
        intent.putExtra(SplitTransactionsActivity.KEY_CURRENCY_ID, fromCurrencyId);

        mParent.startActivityForResult(intent, REQUEST_PICK_SPLIT_TRANSACTION);
    }
}
