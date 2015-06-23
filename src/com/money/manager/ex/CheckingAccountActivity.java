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

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.money.manager.ex.businessobjects.CategoryService;
import com.money.manager.ex.businessobjects.PayeeService;
import com.money.manager.ex.businessobjects.RecurringTransaction;
import com.money.manager.ex.checkingaccount.CheckingAccountConstants;
import com.money.manager.ex.checkingaccount.EditTransactionCommonFunctions;
import com.money.manager.ex.checkingaccount.IntentDataParameters;
import com.money.manager.ex.checkingaccount.YesNoDialog;
import com.money.manager.ex.checkingaccount.YesNoDialogListener;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.database.AccountRepository;
import com.money.manager.ex.database.MoneyManagerOpenHelper;
import com.money.manager.ex.database.QueryCategorySubCategory;
import com.money.manager.ex.database.SplitCategoriesRepository;
import com.money.manager.ex.database.TableBillsDeposits;
import com.money.manager.ex.database.TableBudgetSplitTransactions;
import com.money.manager.ex.database.TableCategory;
import com.money.manager.ex.database.TableCheckingAccount;
import com.money.manager.ex.database.TablePayee;
import com.money.manager.ex.database.TableSplitTransactions;
import com.money.manager.ex.database.TableSubCategory;
import com.money.manager.ex.dropbox.DropboxHelper;
import com.money.manager.ex.fragment.BaseFragmentActivity;
import com.money.manager.ex.fragment.IInputAmountDialogListener;
import com.money.manager.ex.fragment.InputAmountDialog;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.settings.PreferenceConstants;
import com.money.manager.ex.utils.CurrencyUtils;
import com.money.manager.ex.utils.DateUtils;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Alessandro Lazzari (lazzari.ale@gmail.com)
 * @version 1.0.1
 */
public class CheckingAccountActivity
        extends BaseFragmentActivity
        implements IInputAmountDialogListener, YesNoDialogListener {

    // action type intent
    public String mIntentAction;
    public String mToAccountName;
    public int mTransId = -1;

    public String mStatus = null;

    // info payee
    public int mPayeeId = -1;
    public String mPayeeName;
    // info category and subcategory
    public int mCategoryId = -1, mSubCategoryId = -1;
//    public String mCategoryName, mSubCategoryName;
    // arrays to manage transcode and status
    public String[] mTransCodeItems, mStatusItems;
    public String[] mTransCodeValues, mStatusValues;
    // amount
    public double mTotAmount = 0, mAmount = 0;
    // notes
    public String mNotes = "";
    // transaction numbers
    public String mTransNumber = "";
    // bill deposits
    public int mRecurringTransactionId = -1;
    public String mNextOccurrence = null;
    // datepicker value
    public String mDate = "";

    // Controls on the form.
    public ImageButton btnTransNumber;
    public EditText edtTransNumber, edtNotes;
    public TextView txtSelectDate;

    // object of the table
    private TableCheckingAccount mCheckingAccount = new TableCheckingAccount();
    // list split transactions
    private ArrayList<TableSplitTransactions> mSplitTransactions = null;
    private ArrayList<TableSplitTransactions> mSplitTransactionsDeleted = null;
    private EditTransactionCommonFunctions mCommonFunctions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.checkingaccount_activity);

        mCommonFunctions = new EditTransactionCommonFunctions(this);

        try {
            DropboxHelper.getInstance();
        } catch (Exception e) {
            Log.e(CheckingAccountConstants.LOGCAT, e.getMessage());
            // create helper
            DropboxHelper.getInstance(getApplicationContext());
        }

        setToolbarStandardAction(getToolbar());

        // manage save instance
        if ((savedInstanceState != null)) {
            retrieveValuesFromSavedInstanceState(savedInstanceState);
        }

        // Controls need to be at the beginning as they are referenced throughout the code.
        mCommonFunctions.findControls();

        // manage intent
        if (getIntent() != null) {
            handleIntent(savedInstanceState);
        }

        // Transaction code

        initTransactionTypeSelector();

        // account(s)
        mCommonFunctions.initAccountSelectors();

        // status

        mStatusItems = getResources().getStringArray(R.array.status_items);
        mStatusValues = getResources().getStringArray(R.array.status_values);
        // create adapter for spinnerStatus
        ArrayAdapter<String> adapterStatus = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mStatusItems);
        adapterStatus.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCommonFunctions.spinStatus.setAdapter(adapterStatus);
        // select current value
        if (!(TextUtils.isEmpty(mStatus))) {
            if (Arrays.asList(mStatusValues).indexOf(mStatus) >= 0) {
                mCommonFunctions.spinStatus.setSelection(Arrays.asList(mStatusValues).indexOf(mStatus), true);
            }
        } else {
            mStatus = (String) mCommonFunctions.spinStatus.getSelectedItem();
        }
        mCommonFunctions.spinStatus.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if ((position >= 0) && (position <= mStatusValues.length)) {
                    mStatus = mStatusValues[position];
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Transaction date

        txtSelectDate = (TextView) findViewById(R.id.textViewDate);
        if (!(TextUtils.isEmpty(mDate))) {
            try {
                txtSelectDate.setTag(new SimpleDateFormat(Constants.PATTERN_DB_DATE)
                        .parse(mDate));
            } catch (ParseException e) {
                Log.e(CheckingAccountConstants.LOGCAT, e.getMessage());
            } catch (Exception e) {
                ExceptionHandler handler = new ExceptionHandler(this, this);
                handler.handle(e, "Error parsing the date.");
            }
        } else {
            txtSelectDate.setTag(Calendar.getInstance().getTime());
        }
        formatExtendedDate(txtSelectDate);

        txtSelectDate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar date = Calendar.getInstance();
                date.setTime((Date) txtSelectDate.getTag());
                DatePickerDialog dialog = new DatePickerDialog(CheckingAccountActivity.this,
                        mDateSetListener, date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DATE));
                dialog.show();
            }

            public DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    try {
                        Date date = new SimpleDateFormat("yyyy-MM-dd", getResources().getConfiguration().locale)
                                .parse(Integer.toString(year) + "-" + Integer.toString(monthOfYear + 1) + "-" + Integer.toString(dayOfMonth));
                        txtSelectDate.setTag(date);
                        formatExtendedDate(txtSelectDate);
                    } catch (Exception e) {
                        Log.e(CheckingAccountConstants.LOGCAT, e.getMessage());
                    }
                }
            };
        });

        // Payee

        mCommonFunctions.txtSelectPayee.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), PayeeActivity.class);
                intent.setAction(Intent.ACTION_PICK);
                startActivityForResult(intent, CheckingAccountConstants.REQUEST_PICK_PAYEE);
            }
        });

        // Category

        mCommonFunctions.txtSelectCategory.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mCommonFunctions.chbSplitTransaction.isChecked()) {
                    // select single category.
                    Intent intent = new Intent(CheckingAccountActivity.this, CategorySubCategoryExpandableListActivity.class);
                    intent.setAction(Intent.ACTION_PICK);
                    startActivityForResult(intent, CheckingAccountConstants.REQUEST_PICK_CATEGORY);
                } else {
                    // select split categories.
                    Intent intent = new Intent(CheckingAccountActivity.this, SplitTransactionsActivity.class);
                    intent.putExtra(SplitTransactionsActivity.KEY_DATASET_TYPE, TableSplitTransactions.class.getSimpleName());
                    intent.putExtra(SplitTransactionsActivity.KEY_TRANSACTION_TYPE, mCommonFunctions.mTransactionType.getCode());
                    intent.putParcelableArrayListExtra(SplitTransactionsActivity.KEY_SPLIT_TRANSACTION, mSplitTransactions);
                    intent.putParcelableArrayListExtra(SplitTransactionsActivity.KEY_SPLIT_TRANSACTION_DELETED, mSplitTransactionsDeleted);
                    startActivityForResult(intent, CheckingAccountConstants.REQUEST_PICK_SPLIT_TRANSACTION);
                }
            }
        });

        // Split Categories

        mCommonFunctions.chbSplitTransaction.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCommonFunctions.onSplitSet();
            }
        });

        // mark checked if there are existing split categories.
        boolean hasSplit = hasSplitCategories();
        mCommonFunctions.setSplit(hasSplit);

        // Amount and total amount

        OnClickListener onClickAmount = new OnClickListener() {

            @Override
            public void onClick(View v) {
                Integer currencyId = null;
                if (mCommonFunctions.txtTotAmount.equals(v)) {
                    if (mCommonFunctions.spinAccount.getSelectedItemPosition() >= 0 &&
                            mCommonFunctions.spinAccount.getSelectedItemPosition() < mCommonFunctions.AccountList.size()) {
                        if (mCommonFunctions.mTransactionType.equals(TransactionTypes.Transfer)) {
                            currencyId = mCommonFunctions.AccountList.get(mCommonFunctions.spinToAccount.getSelectedItemPosition()).getCurrencyId();
                        } else {
                            currencyId = mCommonFunctions.AccountList.get(mCommonFunctions.spinAccount.getSelectedItemPosition()).getCurrencyId();
                        }
                    }
                } else {
                    // Amount.
                    if (mCommonFunctions.spinToAccount.getSelectedItemPosition() >= 0 &&
                            mCommonFunctions.spinToAccount.getSelectedItemPosition() < mCommonFunctions.AccountList.size()) {
                        currencyId = mCommonFunctions.AccountList.get(mCommonFunctions.spinAccount.getSelectedItemPosition()).getCurrencyId();
                    }
                }
                double amount = (Double) v.getTag();
                InputAmountDialog dialog = InputAmountDialog.getInstance(CheckingAccountActivity.this,
                        v.getId(), amount, currencyId);
                dialog.show(getSupportFragmentManager(), dialog.getClass().getSimpleName());
            }
        };

        // amount
        mCommonFunctions.formatAmount(mCommonFunctions.txtAmount, mAmount,
                !mCommonFunctions.mTransactionType.equals(TransactionTypes.Transfer)
                        ? mCommonFunctions.mToAccountId : mCommonFunctions.mAccountId);
        mCommonFunctions.txtAmount.setOnClickListener(onClickAmount);

        // total amount

        mCommonFunctions.formatAmount(mCommonFunctions.txtTotAmount, mTotAmount,
                !mCommonFunctions.mTransactionType.equals(TransactionTypes.Transfer)
                        ? mCommonFunctions.mAccountId : mCommonFunctions.mToAccountId);

        mCommonFunctions.txtTotAmount.setOnClickListener(onClickAmount);

        // Transaction number

        edtTransNumber = (EditText) findViewById(R.id.editTextTransNumber);
        if (!TextUtils.isEmpty(mTransNumber)) {
            edtTransNumber.setText(mTransNumber);
        }
        btnTransNumber = (ImageButton) findViewById(R.id.buttonTransNumber);
        btnTransNumber.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                MoneyManagerOpenHelper helper = MoneyManagerOpenHelper.getInstance(getApplicationContext());
                String query = "SELECT MAX(CAST(" + TableCheckingAccount.TRANSACTIONNUMBER + " AS INTEGER)) FROM " +
                        new TableCheckingAccount().getSource() + " WHERE " +
                        TableCheckingAccount.ACCOUNTID + "=?";
                Cursor cursor = helper.getReadableDatabase().rawQuery(query,
                        new String[]{Integer.toString(mCommonFunctions.mAccountId)});
                if (cursor != null && cursor.moveToFirst()) {
                    String transNumber = cursor.getString(0);
                    if (TextUtils.isEmpty(transNumber)) {
                        transNumber = "0";
                    }
                    if ((!TextUtils.isEmpty(transNumber)) && TextUtils.isDigitsOnly(transNumber)) {
                        try {
                            edtTransNumber.setText(Long.toString(Long.parseLong(transNumber) + 1));
                        } catch (Exception e) {
                            Log.e(CheckingAccountConstants.LOGCAT, e.getMessage());
                        }
                    }
                    cursor.close();
                }
                //helper.close();
            }
        });

        // notes

        edtNotes = (EditText) findViewById(R.id.editTextNotes);
        if (!(TextUtils.isEmpty(mNotes))) {
            edtNotes.setText(mNotes);
        }

        // refresh user interface
        refreshAfterTransactionCodeChange();
        refreshPayeeName();
        mCommonFunctions.refreshCategoryName();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CheckingAccountConstants.REQUEST_PICK_PAYEE:
                if ((resultCode == Activity.RESULT_OK) && (data != null)) {
                    mPayeeId = data.getIntExtra(PayeeActivity.INTENT_RESULT_PAYEEID, -1);
                    mPayeeName = data.getStringExtra(PayeeActivity.INTENT_RESULT_PAYEENAME);
                    // select last category used from payee
                    if (!mCommonFunctions.chbSplitTransaction.isChecked()) {
                        if (getCategoryFromPayee(mPayeeId)) {
                            mCommonFunctions.refreshCategoryName(); // refresh UI
                        }
                    }
                    // refresh UI
                    refreshPayeeName();
                }
                break;
            case CheckingAccountConstants.REQUEST_PICK_ACCOUNT:
                if ((resultCode == Activity.RESULT_OK) && (data != null)) {
                    mCommonFunctions.mToAccountId = data.getIntExtra(AccountListActivity.INTENT_RESULT_ACCOUNTID, -1);
                    mToAccountName = data.getStringExtra(AccountListActivity.INTENT_RESULT_ACCOUNTNAME);
                }
                break;
            case CheckingAccountConstants.REQUEST_PICK_CATEGORY:
                if ((resultCode == Activity.RESULT_OK) && (data != null)) {
                    mCategoryId = data.getIntExtra(CategorySubCategoryExpandableListActivity.INTENT_RESULT_CATEGID, -1);
                    mCommonFunctions.mCategoryName = data.getStringExtra(CategorySubCategoryExpandableListActivity.INTENT_RESULT_CATEGNAME);
                    mSubCategoryId = data.getIntExtra(CategorySubCategoryExpandableListActivity.INTENT_RESULT_SUBCATEGID, -1);
                    mCommonFunctions.mSubCategoryName = data.getStringExtra(CategorySubCategoryExpandableListActivity.INTENT_RESULT_SUBCATEGNAME);
                    // refresh UI category
                    mCommonFunctions.refreshCategoryName();
                }
                break;
            case CheckingAccountConstants.REQUEST_PICK_SPLIT_TRANSACTION:
                if ((resultCode == Activity.RESULT_OK) && (data != null)) {
                    mSplitTransactions = data.getParcelableArrayListExtra(SplitTransactionsActivity.INTENT_RESULT_SPLIT_TRANSACTION);
                    if (mSplitTransactions != null && mSplitTransactions.size() > 0) {
                        double totAmount = 0;
                        for (int i = 0; i < mSplitTransactions.size(); i++) {
                            totAmount += mSplitTransactions.get(i).getSplitTransAmount();
                        }
                        mCommonFunctions.formatAmount(mCommonFunctions.txtTotAmount, totAmount,
                                !mCommonFunctions.mTransactionType.equals(TransactionTypes.Transfer)
                                        ? mCommonFunctions.mAccountId
                                        : mCommonFunctions.mToAccountId);
                    }
                    // deleted item
                    if (data.getParcelableArrayListExtra(SplitTransactionsActivity.INTENT_RESULT_SPLIT_TRANSACTION_DELETED) != null) {
                        mSplitTransactionsDeleted = data.getParcelableArrayListExtra(SplitTransactionsActivity.INTENT_RESULT_SPLIT_TRANSACTION_DELETED);
                    }
                }
                break;
        }
    }

    /**
     * After the user accepts, remove any split categories.
     */
    private void removeAllSplitCategories() {
        if(mSplitTransactions == null) return;

        for(int i = 0; i < mSplitTransactions.size(); i++) {
            TableSplitTransactions split = mSplitTransactions.get(i);
            int id = split.getSplitTransId();
            ArrayList<TableSplitTransactions> deletedSplits = getDeletedSplitCategories();

            if(id == -1) {
                // Remove any newly created splits.
                // transaction id == -1
                mSplitTransactions.remove(i);
                i--;
            } else {
                // Delete any splits already in the database.
                // transaction id != -1
                // avoid adding duplicate records.
                if(!deletedSplits.contains(split)) {
                    deletedSplits.add(split);
                }
            }
        }
    }

    private void retrieveValuesFromSavedInstanceState(Bundle savedInstanceState) {
        mTransId = savedInstanceState.getInt(CheckingAccountConstants.KEY_TRANS_ID);
        mCommonFunctions.mAccountId = savedInstanceState.getInt(CheckingAccountConstants.KEY_ACCOUNT_ID);
        mCommonFunctions.mToAccountId = savedInstanceState.getInt(CheckingAccountConstants.KEY_TO_ACCOUNT_ID);
        mToAccountName = savedInstanceState.getString(CheckingAccountConstants.KEY_TO_ACCOUNT_NAME);
        mDate = savedInstanceState.getString(CheckingAccountConstants.KEY_TRANS_DATE);
        String transCode = savedInstanceState.getString(CheckingAccountConstants.KEY_TRANS_CODE);
        mCommonFunctions.mTransactionType = TransactionTypes.valueOf(transCode);
        mStatus = savedInstanceState.getString(CheckingAccountConstants.KEY_TRANS_STATUS);
        mAmount = savedInstanceState.getDouble(CheckingAccountConstants.KEY_TRANS_AMOUNT);
        mTotAmount = savedInstanceState.getDouble(CheckingAccountConstants.KEY_TRANS_TOTAMOUNT);
        mPayeeId = savedInstanceState.getInt(CheckingAccountConstants.KEY_PAYEE_ID);
        mPayeeName = savedInstanceState.getString(CheckingAccountConstants.KEY_PAYEE_NAME);
        mCategoryId = savedInstanceState.getInt(CheckingAccountConstants.KEY_CATEGORY_ID);
        mCommonFunctions.mCategoryName = savedInstanceState.getString(CheckingAccountConstants.KEY_CATEGORY_NAME);
        mSubCategoryId = savedInstanceState.getInt(CheckingAccountConstants.KEY_SUBCATEGORY_ID);
        mCommonFunctions.mSubCategoryName = savedInstanceState.getString(CheckingAccountConstants.KEY_SUBCATEGORY_NAME);
        mNotes = savedInstanceState.getString(CheckingAccountConstants.KEY_NOTES);
        mTransNumber = savedInstanceState.getString(CheckingAccountConstants.KEY_TRANS_NUMBER);
        mSplitTransactions = savedInstanceState.getParcelableArrayList(CheckingAccountConstants.KEY_SPLIT_TRANSACTION);
        mSplitTransactionsDeleted = savedInstanceState.getParcelableArrayList(CheckingAccountConstants.KEY_SPLIT_TRANSACTION_DELETED);
        mRecurringTransactionId = savedInstanceState.getInt(CheckingAccountConstants.KEY_BDID_ID);
        mNextOccurrence = savedInstanceState.getString(CheckingAccountConstants.KEY_NEXT_OCCURRENCE);
        // action
        mIntentAction = savedInstanceState.getString(CheckingAccountConstants.KEY_ACTION);
    }

    /**
     * Get the parameters from the intent (parameters sent from the caller).
     * Also used for Tasker integration, for example.
     * @param savedInstanceState parameters
     */
    private void handleIntent(Bundle savedInstanceState) {
        Intent intent = getIntent();
        mIntentAction = intent.getAction();

        if (savedInstanceState == null) {
            mCommonFunctions.mAccountId = intent.getIntExtra(CheckingAccountConstants.KEY_ACCOUNT_ID, -1);

            // Edit transaction.

            if (mIntentAction != null && Intent.ACTION_EDIT.equals(mIntentAction)) {
                mTransId = intent.getIntExtra(CheckingAccountConstants.KEY_TRANS_ID, -1);
                // select data transaction
                loadCheckingAccount(mTransId, false);
            } else if (mIntentAction != null && Intent.ACTION_PASTE.equals(mIntentAction)) {
                // select data transaction
                loadCheckingAccount(intent.getIntExtra(CheckingAccountConstants.KEY_TRANS_ID, -1), true);
            } else {
                if (intent.getIntExtra(CheckingAccountConstants.KEY_BDID_ID, -1) > -1) {
                    mRecurringTransactionId = intent.getIntExtra(CheckingAccountConstants.KEY_BDID_ID, -1);
                    mNextOccurrence = intent.getStringExtra(CheckingAccountConstants.KEY_NEXT_OCCURRENCE);
                    loadRecurringTransaction(mRecurringTransactionId);
                }
            }
        }

        // New transaction

        if (Constants.INTENT_ACTION_INSERT.equals(mIntentAction)) {
            if (mStatus == null) {
                mStatus = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                        .getString(getString(PreferenceConstants.PREF_DEFAULT_STATUS), "");
            }

            if ("L".equals(PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                    .getString(getString(PreferenceConstants.PREF_DEFAULT_PAYEE), "N"))) {
                AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
                    @Override
                    protected Boolean doInBackground(Void... params) {
                        try {
                            Core core = new Core(getApplicationContext());
                            TablePayee payee = core.getLastPayeeUsed();
                            if (payee != null && mPayeeId == -1) {
                                // get id payee and category
                                mPayeeId = payee.getPayeeId();
                                mPayeeName = payee.getPayeeName();
                                mCategoryId = payee.getCategId();
                                mSubCategoryId = payee.getSubCategId();
                                // load category and subcategory name
                                loadCategorySubName(mCategoryId, mSubCategoryId);
                                return Boolean.TRUE;
                            }
                        } catch (Exception e) {
                            Log.e(CheckingAccountConstants.LOGCAT, e.getMessage());
                        }
                        return Boolean.FALSE;
                    }

                    @Override
                    protected void onPostExecute(Boolean result) {
                        super.onPostExecute(result);
                        if (result) {
                            try {
                                // refresh field
                                refreshPayeeName();
                                mCommonFunctions.refreshCategoryName();
                            } catch (Exception e) {
                                Log.e(CheckingAccountConstants.LOGCAT, e.getMessage());
                            }
                        }
                    }
                };
                task.execute();
            }

            externalIntegration(intent);

            // Select the default account.
            AppSettings settings = new AppSettings(this);
            String defaultAccountSetting = settings.getGeneralSettings().getDefaultAccount();
            if (!TextUtils.isEmpty(defaultAccountSetting)) {
                int defaultAccountId = Integer.parseInt(defaultAccountSetting);
                if (mCommonFunctions.mAccountIdList.contains(defaultAccountId)) {
                    int index = mCommonFunctions.mAccountIdList.indexOf(defaultAccountId);
                    mCommonFunctions.spinAccount.setSelection(index);
                }
            }
        }

        // set title
        getSupportActionBar().setTitle(Constants.INTENT_ACTION_INSERT.equals(mIntentAction)
                ? R.string.new_transaction
                : R.string.edit_transaction);
    }

    /**
     * Get any parameters, if sent, when intent was raised. This is used when called
     * from Tasker or any external caller.
     * @param intent
     */
    public void externalIntegration(Intent intent) {
        Uri data = intent.getData();
        if (data == null) return;

        IntentDataParameters parameters = IntentDataParameters.parseData(this, data);

        // transaction type
        mCommonFunctions.mTransactionType = parameters.transactionType;

        if (parameters.accountId > 0) {
            this.mCommonFunctions.mAccountId = parameters.accountId;
        }
        this.mTotAmount = parameters.amount;
        // payee
        if (parameters.payeeId > 0) {
            this.mPayeeId = parameters.payeeId;
            this.mPayeeName = parameters.payeeName;
        } else {
            // create payee if it does not exist
            if (parameters.payeeName != null) {
                PayeeService newPayee = new PayeeService(this);
                mPayeeId = newPayee.createNew(parameters.payeeName);
                mPayeeName = parameters.payeeName;
            }
        }

        // category
        if (parameters.categoryId > 0) {
            mCategoryId = parameters.categoryId;
            mCommonFunctions.mCategoryName = parameters.categoryName;
        } else {
            // No id sent.
            // create a category if it was sent but does not exist (id not found by the parser).
            if (parameters.categoryName != null) {
                CategoryService newCategory = new CategoryService(this);
                mCategoryId = newCategory.createNew(mCommonFunctions.mCategoryName);
                mCommonFunctions.mCategoryName = parameters.categoryName;
            }
        }
    }

    public ArrayList<TableSplitTransactions> getDeletedSplitCategories() {
        if(mSplitTransactionsDeleted == null){
            mSplitTransactionsDeleted = new ArrayList<>();
        }
        return mSplitTransactionsDeleted;
    }

    private void initTransactionTypeSelector() {
        // populate arrays TransCode
        mTransCodeItems = getResources().getStringArray(R.array.transcode_items);
        mTransCodeValues = getResources().getStringArray(R.array.transcode_values);
        // create adapter for TransCode
        final ArrayAdapter<String> adapterTrans = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                mTransCodeItems);
        adapterTrans.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCommonFunctions.spinTransCode.setAdapter(adapterTrans);

        // select the current value
        if (mCommonFunctions.mTransactionType != null) {
            if (Arrays.asList(mTransCodeValues).indexOf(getTransactionType()) >= 0) {
                mCommonFunctions.spinTransCode.setSelection(Arrays.asList(mTransCodeValues).indexOf(getTransactionType()), true);
            }
        } else {
            mCommonFunctions.mTransactionType = TransactionTypes.values()[mCommonFunctions.spinTransCode.getSelectedItemPosition()];
        }
        mCommonFunctions.spinTransCode.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if ((position >= 0) && (position <= mTransCodeValues.length)) {
                    String selectedValue = mTransCodeValues[position];

                    // Prevent selection if there are split transactions and the type is being
                    // set to Transfer.
                    if (selectedValue.equalsIgnoreCase(getString(R.string.transfer))) {
                        handleSwitchingTransactionTypeToTransfer();
                        return;
                    }

                    mCommonFunctions.mTransactionType = TransactionTypes.values()[position];
                }
                // aggiornamento dell'interfaccia grafica
                refreshAfterTransactionCodeChange();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void handleSwitchingTransactionTypeToTransfer() {
        // The user is switching to Transfer transaction type.

        if(hasSplitCategories()) {
            // Prompt the user to confirm deleting split categories.
            // Use DialogFragment in order to redraw the dialog when switching device orientation.

            DialogFragment dialog = new YesNoDialog();
            Bundle args = new Bundle();
            args.putString("title", getString(R.string.warning));
            args.putString("message", getString(R.string.no_transfer_splits));
            args.putString("purpose", YesNoDialog.PURPOSE_DELETE_SPLITS_WHEN_SWITCHING_TO_TRANSFER);
            dialog.setArguments(args);
//        dialog.setTargetFragment(this, REQUEST_REMOVE_SPLIT_WHEN_TRANSACTION);
            dialog.show(getSupportFragmentManager(), "tag");

            // Dialog result is handled in onDialogPositiveClick.
            return;
        }

        // un-check split.
        mCommonFunctions.setSplit(false);

        // Hide Category picker.
        mCommonFunctions.txtSelectCategory.setVisibility(View.GONE);
        // Clear category.
        mCategoryId = -1;

//        mTransCode = getString(R.string.transfer);
        mCommonFunctions.mTransactionType = TransactionTypes.Transfer;

        refreshAfterTransactionCodeChange();
    }

    /**
     * Handle user's confirmation to delete any Split Categories when switching to
     * Transfer transaction type.
     * @param dialog The dialog that is returning the value.
     */
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
//        YesNoDialog yesNoDialog = (YesNoDialog) dialog;
//        String purpose = yesNoDialog.getPurpose();
        // for now ignore the purpose as we only have one yes-no dialog.

        removeAllSplitCategories();

        mCommonFunctions.setSplit(false);

        mCommonFunctions.mTransactionType = TransactionTypes.Transfer;

        refreshAfterTransactionCodeChange();
    }

    /**
     * The user stopped switching to Transfer. Restore previous state.
     * @param dialog The dialog that is returning the value.
     */
    @Override
    public void onDialogNegativeClick(DialogFragment dialog){
        cancelChangingTransactionToTransfer();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // save the state interface
        outState.putInt(CheckingAccountConstants.KEY_TRANS_ID, mTransId);
        outState.putInt(CheckingAccountConstants.KEY_ACCOUNT_ID, mCommonFunctions.mAccountId);
        outState.putInt(CheckingAccountConstants.KEY_TO_ACCOUNT_ID, mCommonFunctions.mToAccountId);
        outState.putString(CheckingAccountConstants.KEY_TO_ACCOUNT_NAME, mToAccountName);
        outState.putString(CheckingAccountConstants.KEY_TRANS_DATE,
                new SimpleDateFormat(Constants.PATTERN_DB_DATE).format(txtSelectDate.getTag()));
        outState.putString(CheckingAccountConstants.KEY_TRANS_CODE, getTransactionType());
        outState.putString(CheckingAccountConstants.KEY_TRANS_STATUS, mStatus);
        outState.putDouble(CheckingAccountConstants.KEY_TRANS_TOTAMOUNT, (Double) mCommonFunctions.txtTotAmount.getTag());
        outState.putDouble(CheckingAccountConstants.KEY_TRANS_AMOUNT, (Double) mCommonFunctions.txtAmount.getTag());
        outState.putInt(CheckingAccountConstants.KEY_PAYEE_ID, mPayeeId);
        outState.putString(CheckingAccountConstants.KEY_PAYEE_NAME, mPayeeName);
        outState.putInt(CheckingAccountConstants.KEY_CATEGORY_ID, mCategoryId);
        outState.putString(CheckingAccountConstants.KEY_CATEGORY_NAME, mCommonFunctions.mCategoryName);
        outState.putInt(CheckingAccountConstants.KEY_SUBCATEGORY_ID, mSubCategoryId);
        outState.putString(CheckingAccountConstants.KEY_SUBCATEGORY_NAME, mCommonFunctions.mSubCategoryName);
        outState.putString(CheckingAccountConstants.KEY_TRANS_NUMBER, edtTransNumber.getText().toString());
        outState.putParcelableArrayList(CheckingAccountConstants.KEY_SPLIT_TRANSACTION, mSplitTransactions);
        outState.putParcelableArrayList(CheckingAccountConstants.KEY_SPLIT_TRANSACTION_DELETED, mSplitTransactionsDeleted);
        outState.putString(CheckingAccountConstants.KEY_NOTES, edtNotes.getText().toString());
        // bill deposits
        outState.putInt(CheckingAccountConstants.KEY_BDID_ID, mRecurringTransactionId);
        outState.putString(CheckingAccountConstants.KEY_NEXT_OCCURRENCE, mNextOccurrence);

        outState.putString(CheckingAccountConstants.KEY_ACTION, mIntentAction);
    }

    @Override
    public void onFinishedInputAmountDialog(int id, Double amount) {
        View view = findViewById(id);
        int accountId;
        if (view != null && view instanceof TextView) {
            CurrencyUtils currencyUtils = new CurrencyUtils(getApplicationContext());
            if (mCommonFunctions.mTransactionType.equals(TransactionTypes.Transfer)) {
                Double originalAmount;
                try {
                    /*Integer toCurrencyId = mAccountList.get(mAccountIdList.indexOf(mAccountId)).getCurrencyId();
                    Integer fromCurrencyId = mAccountList.get(mAccountIdList.indexOf(mToAccountId)).getCurrencyId();*/
                    Integer toCurrencyId = mCommonFunctions.AccountList.get(mCommonFunctions.mAccountIdList
                            .indexOf(view.getId() == R.id.textViewTotAmount
                                    ? mCommonFunctions.mAccountId
                                    : mCommonFunctions.mToAccountId)).getCurrencyId();
                    Integer fromCurrencyId = mCommonFunctions.AccountList.get(mCommonFunctions.mAccountIdList
                            .indexOf(view.getId() == R.id.textViewTotAmount
                                    ? mCommonFunctions.mToAccountId :
                                    mCommonFunctions.mAccountId)).getCurrencyId();
                    // take a original values
                    originalAmount = view.getId() == R.id.textViewTotAmount
                            ? (Double) mCommonFunctions.txtTotAmount.getTag()
                            : (Double) mCommonFunctions.txtAmount.getTag();
                    // convert value
                    Double amountExchange = currencyUtils.doCurrencyExchange(toCurrencyId, originalAmount, fromCurrencyId);
                    // take original amount converted
                    originalAmount = view.getId() == R.id.textViewTotAmount
                            ? (Double) mCommonFunctions.txtAmount.getTag()
                            : (Double) mCommonFunctions.txtTotAmount.getTag();
                    if (originalAmount == null)
                        originalAmount = 0d;
                    // check if two values is equals, and then convert value
                    if (originalAmount == 0) {
                        DecimalFormat decimalFormat = new DecimalFormat("0.00");
                        if (decimalFormat.format(originalAmount).equals(decimalFormat.format(amountExchange))) {
                            amountExchange = currencyUtils.doCurrencyExchange(toCurrencyId, amount, fromCurrencyId);
                            mCommonFunctions.formatAmount(view.getId() == R.id.textViewTotAmount
                                    ? mCommonFunctions.txtAmount : mCommonFunctions.txtTotAmount,
                                    amountExchange,
                                    view.getId() == R.id.textViewTotAmount
                                            ? mCommonFunctions.mAccountId
                                            : mCommonFunctions.mToAccountId);
                        }
                    }

                } catch (Exception e) {
                    Log.e(CheckingAccountConstants.LOGCAT, e.getMessage());
                }
            }
            if (mCommonFunctions.txtTotAmount.equals(view)) {
                if (mCommonFunctions.mTransactionType.equals(TransactionTypes.Transfer)) {
                    accountId = mCommonFunctions.mToAccountId;
                } else {
                    accountId = mCommonFunctions.mAccountId;
                }
            } else {
                accountId = mCommonFunctions.mAccountId;
            }
            mCommonFunctions.formatAmount(((TextView) view), amount, accountId);
        }
    }

    @Override
    public boolean onActionCancelClick() {
        final MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title(android.R.string.cancel)
                .content(R.string.transaction_cancel_confirm)
                .positiveText(R.string.discard)
                .negativeText(R.string.keep_editing)
                .cancelable(false)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        setResult(RESULT_CANCELED);
                        finish();

                        super.onPositive(dialog);
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                    }
                })
                .build();
        dialog.show();
        return true;
    }

    @Override
    public void onBackPressed() {
        onActionCancelClick();
    }

    @Override
    public boolean onActionDoneClick() {
        if (updateData()) {
            // set result ok and finish activity
            setResult(RESULT_OK);
            finish();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Loads info for Category and Subcategory
     *
     * @param categoryId Id of the category to load.
     * @param subCategoryId Id of the subcategory to load.
     * @return A boolean indicating whether the operation was successful.
     */
    public boolean loadCategorySubName(int categoryId, int subCategoryId) {

        // don't load anything if category & sub-category are not set.
        if(categoryId <= 0 && subCategoryId <= 0) return false;

        TableCategory category = new TableCategory();
        TableSubCategory subCategory = new TableSubCategory();
        Cursor cursor;
        // category
        cursor = getContentResolver().query(category.getUri(), category.getAllColumns(),
                TableCategory.CATEGID + "=?", new String[]{Integer.toString(categoryId)}, null);
        if ((cursor != null) && (cursor.moveToFirst())) {
            // set category name and sub category name
            mCommonFunctions.mCategoryName = cursor.getString(cursor.getColumnIndex(TableCategory.CATEGNAME));
        } else {
            mCommonFunctions.mCategoryName = null;
        }
        if (cursor != null) {
            cursor.close();
        }

        // sub-category

        cursor = getContentResolver().query(subCategory.getUri(), subCategory.getAllColumns(),
                TableSubCategory.SUBCATEGID + "=?", new String[]{Integer.toString(subCategoryId)}, null);
        if ((cursor != null) && (cursor.moveToFirst())) {
            // set category name and sub category name
            mCommonFunctions.mSubCategoryName = cursor.getString(cursor.getColumnIndex(TableSubCategory.SUBCATEGNAME));
        } else {
            mCommonFunctions.mSubCategoryName = null;
        }
        if (cursor != null) {
            cursor.close();
        }

        return true;
    }

    /**
     * getApplicationContext() method allows you to search the transaction data
     *
     * @param transId transaction id
     * @return true if data selected, false nothing
     */
    public boolean loadCheckingAccount(int transId, boolean duplicate) {
        Cursor cursor = getContentResolver().query(mCheckingAccount.getUri(),
                mCheckingAccount.getAllColumns(),
                TableCheckingAccount.TRANSID + "=?",
                new String[]{Integer.toString(transId)}, null);
        // check if cursor is valid and open
        if ((cursor == null) || (!cursor.moveToFirst())) {
            return false;
        }

        // take a data
        if (!duplicate) {
            mTransId = cursor.getInt(cursor.getColumnIndex(TableCheckingAccount.TRANSID));
        }
        mCommonFunctions.mAccountId = cursor.getInt(cursor.getColumnIndex(TableCheckingAccount.ACCOUNTID));
        mCommonFunctions.mToAccountId = cursor.getInt(cursor.getColumnIndex(TableCheckingAccount.TOACCOUNTID));
        String transCode = cursor.getString(cursor.getColumnIndex(TableCheckingAccount.TRANSCODE));
        mCommonFunctions.mTransactionType = TransactionTypes.valueOf(transCode);
        mStatus = cursor.getString(cursor.getColumnIndex(TableCheckingAccount.STATUS));
        mAmount = cursor.getDouble(cursor.getColumnIndex(TableCheckingAccount.TRANSAMOUNT));
        mTotAmount = cursor.getDouble(cursor.getColumnIndex(TableCheckingAccount.TOTRANSAMOUNT));
        mPayeeId = cursor.getInt(cursor.getColumnIndex(TableCheckingAccount.PAYEEID));
        mCategoryId = cursor.getInt(cursor.getColumnIndex(TableCheckingAccount.CATEGID));
        mSubCategoryId = cursor.getInt(cursor.getColumnIndex(TableCheckingAccount.SUBCATEGID));
        mTransNumber = cursor.getString(cursor.getColumnIndex(TableCheckingAccount.TRANSACTIONNUMBER));
        mNotes = cursor.getString(cursor.getColumnIndex(TableCheckingAccount.NOTES));
        if (!duplicate) {
            mDate = cursor.getString(cursor.getColumnIndex(TableCheckingAccount.TRANSDATE));
        }

        cursor.close();

        // Load Split Categories.
        if (mSplitTransactions == null) {
            SplitCategoriesRepository splitRepo = new SplitCategoriesRepository(this);
            mSplitTransactions = splitRepo.loadSplitCategoriesFor(transId);

            if (duplicate && (mSplitTransactions != null)) {
                // Reset ids so that the transactions get inserted on save.
                for (TableSplitTransactions split : mSplitTransactions) {
                    split.setSplitTransId(Constants.NOT_SET);
                }
            }

        }

        // convert status in uppercase string
        if (!TextUtils.isEmpty(mStatus)) mStatus = mStatus.toUpperCase();

        AccountRepository accountRepository = new AccountRepository(this);
        mToAccountName = accountRepository.loadName(mCommonFunctions.mToAccountId);

        getPayeeName(mPayeeId);
        loadCategorySubName(mCategoryId, mSubCategoryId);

        return true;
    }

    /**
     * query info payee
     *
     * @param payeeId id payee
     * @return true if the data selected
     */
    public boolean getPayeeName(int payeeId) {
        TablePayee payee = new TablePayee();
        Cursor cursor = getContentResolver().query(payee.getUri(),
                payee.getAllColumns(),
                TablePayee.PAYEEID + "=?",
                new String[]{Integer.toString(payeeId)}, null);
        // check if cursor is valid and open
        if ((cursor == null) || (!cursor.moveToFirst())) {
            return false;
        }

        // set payeename
        mPayeeName = cursor.getString(cursor.getColumnIndex(TablePayee.PAYEENAME));

        cursor.close();

        return true;
    }

    /**
     * Loads a recurring transaction data when entering a recurring transaction.
     * @param recurringTransactionId Id of the recurring transaction.
     * @return A boolean indicating whether the operation was successful.
     */
    public boolean loadRecurringTransaction(int recurringTransactionId) {
        TableBillsDeposits billDeposits = new TableBillsDeposits();
        Cursor cursor = getContentResolver().query(billDeposits.getUri(),
                billDeposits.getAllColumns(),
                TableBillsDeposits.BDID + "=?",
                new String[]{Integer.toString(recurringTransactionId)}, null);
        // check if cursor is valid and open
        if ((cursor == null) || (!cursor.moveToFirst())) {
            return false;
        }

        // take a data
        mCommonFunctions.mAccountId = cursor.getInt(cursor.getColumnIndex(TableBillsDeposits.ACCOUNTID));
        mCommonFunctions.mToAccountId = cursor.getInt(cursor.getColumnIndex(TableBillsDeposits.TOACCOUNTID));
        String transCode = cursor.getString(cursor.getColumnIndex(TableBillsDeposits.TRANSCODE));
        mCommonFunctions.mTransactionType = TransactionTypes.valueOf(transCode);
        mStatus = cursor.getString(cursor.getColumnIndex(TableBillsDeposits.STATUS));
        mAmount = cursor.getDouble(cursor.getColumnIndex(TableBillsDeposits.TRANSAMOUNT));
        mTotAmount = cursor.getDouble(cursor.getColumnIndex(TableBillsDeposits.TOTRANSAMOUNT));
        mPayeeId = cursor.getInt(cursor.getColumnIndex(TableBillsDeposits.PAYEEID));
        mCategoryId = cursor.getInt(cursor.getColumnIndex(TableBillsDeposits.CATEGID));
        mSubCategoryId = cursor.getInt(cursor.getColumnIndex(TableBillsDeposits.SUBCATEGID));
        mTransNumber = cursor.getString(cursor.getColumnIndex(TableBillsDeposits.TRANSACTIONNUMBER));
        mNotes = cursor.getString(cursor.getColumnIndex(TableBillsDeposits.NOTES));
        mDate = cursor.getString(cursor.getColumnIndex(TableBillsDeposits.NEXTOCCURRENCEDATE));
        mStatus = cursor.getString(cursor.getColumnIndex(TableBillsDeposits.STATUS));

        cursor.close();

        AccountRepository accountRepository = new AccountRepository(this);
        mToAccountName = accountRepository.loadName(mCommonFunctions.mToAccountId);

        getPayeeName(mPayeeId);
        loadCategorySubName(mCategoryId, mSubCategoryId);

        // handle splits
        createSplitCategoriesFromRecurringTransaction();

        return true;
    }

    public void formatExtendedDate(TextView dateTextView) {
        try {
            dateTextView.setText(new SimpleDateFormat("EEEE dd MMMM yyyy", getResources().getConfiguration().locale)
                    .format((Date) dateTextView.getTag()));
        } catch (Exception e) {
            Log.e(CheckingAccountConstants.LOGCAT, e.getMessage());
        }
    }

    public boolean hasSplitCategories() {
        return mSplitTransactions != null && !mSplitTransactions.isEmpty();
    }

    /**
     * update UI interface with PayeeName
     */
    public void refreshPayeeName() {
        // write into text button payee name
        if (mCommonFunctions.txtSelectPayee != null) {
            mCommonFunctions.txtSelectPayee.setText(mPayeeName);
        }
    }

    /**
     * Handle transaction type change.
     */
    public void refreshAfterTransactionCodeChange() {
        // check type of transaction
        TextView txtFromAccount = (TextView) findViewById(R.id.textViewFromAccount);
        TextView txtToAccount = (TextView) findViewById(R.id.textViewToAccount);
        ViewGroup tableRowPayee = (ViewGroup) findViewById(R.id.tableRowPayee);
        ViewGroup tableRowAmount = (ViewGroup) findViewById(R.id.tableRowAmount);

        // hide and show
        boolean isTransfer = mCommonFunctions.mTransactionType.equals(TransactionTypes.Transfer);
        txtFromAccount.setText(isTransfer ? R.string.from_account : R.string.account);
        txtToAccount.setVisibility(isTransfer ? View.VISIBLE : View.GONE);
        tableRowPayee.setVisibility(!isTransfer ? View.VISIBLE : View.GONE);
        tableRowAmount.setVisibility(isTransfer ? View.VISIBLE : View.GONE);
        mCommonFunctions.spinToAccount.setVisibility(isTransfer ? View.VISIBLE : View.GONE);
        // hide split controls
        mCommonFunctions.chbSplitTransaction.setVisibility(isTransfer ? View.GONE : View.VISIBLE);

        mCommonFunctions.refreshHeaderAmount();
    }

    /**
     * validate data insert in activity
     *
     * @return a boolean indicating whether the data is valid.
     */
    public boolean validateData() {
        boolean isTransfer = mCommonFunctions.mTransactionType.equals(TransactionTypes.Transfer);

        // Transfers.
        if (isTransfer) {
            if (mCommonFunctions.mToAccountId == -1) {
                Core.alertDialog(this, R.string.error_toaccount_not_selected);
                return false;
            }
            if (mCommonFunctions.mToAccountId == mCommonFunctions.mAccountId) {
                Core.alertDialog(this, R.string.error_transfer_to_same_account);
                return false;
            }
        }
        // Payee is now optional.
//        if ((!Constants.TRANSACTION_TYPE_TRANSFER.equalsIgnoreCase(mTransCode)) && (mPayeeId == -1)) {
//            Core.alertDialog(this, R.string.error_payee_not_selected).show();
//            return false;
//        }

        // Category is required if tx is not a split or transfer.
        if (mCategoryId == -1 && (!mCommonFunctions.chbSplitTransaction.isChecked()) && !isTransfer) {
            Core.alertDialog(this, R.string.error_category_not_selected);
            return false;
        }
        // Splits.
        if (mCommonFunctions.chbSplitTransaction.isChecked() && (mSplitTransactions == null || mSplitTransactions.size() <= 0)) {
            Core.alertDialog(this, R.string.error_split_transaction_empty);
            return false;
        }
        // Total amount.
        if ((Double) mCommonFunctions.txtTotAmount.getTag() == 0) {
            if ((Double) mCommonFunctions.txtAmount.getTag() == 0) {
                Core.alertDialog(this, R.string.error_totamount_empty);
                return false;
            } else {
                mCommonFunctions.txtTotAmount.setTag(mCommonFunctions.txtAmount.getTag());
            }
        }
        return true;
    }

    /**
     * update data into database
     *
     * @return true if update data successful
     */
    public boolean updateData() {
        if (!validateData()) return false;

        // content value for insert or update data
        ContentValues values = new ContentValues();

        boolean isTransfer = mCommonFunctions.mTransactionType.equals(TransactionTypes.Transfer);

        values.put(TableCheckingAccount.ACCOUNTID, mCommonFunctions.mAccountId);
        if (isTransfer) {
            values.put(TableCheckingAccount.TOACCOUNTID, mCommonFunctions.mToAccountId);
            values.put(TableCheckingAccount.PAYEEID, -1);
        } else {
            values.put(TableCheckingAccount.PAYEEID, mPayeeId);
        }
        values.put(TableCheckingAccount.TRANSCODE, getTransactionType());
        if (TextUtils.isEmpty(mCommonFunctions.txtAmount.getText().toString()) || (!isTransfer)) {
            values.put(TableCheckingAccount.TRANSAMOUNT, (Double) mCommonFunctions.txtTotAmount.getTag());
        } else {
            values.put(TableCheckingAccount.TRANSAMOUNT, (Double) mCommonFunctions.txtAmount.getTag());
        }
        values.put(TableCheckingAccount.STATUS, mStatus);
        values.put(TableCheckingAccount.CATEGID, !mCommonFunctions.chbSplitTransaction.isChecked() ? mCategoryId : -1);
        values.put(TableCheckingAccount.SUBCATEGID, !mCommonFunctions.chbSplitTransaction.isChecked() ? mSubCategoryId : -1);
        String transactionDate = DateUtils.getSQLiteStringDate(this, (Date) txtSelectDate.getTag());
        values.put(TableCheckingAccount.TRANSDATE, transactionDate);
        values.put(TableCheckingAccount.FOLLOWUPID, -1);
        values.put(TableCheckingAccount.TOTRANSAMOUNT, (Double) mCommonFunctions.txtTotAmount.getTag());
        values.put(TableCheckingAccount.TRANSACTIONNUMBER, edtTransNumber.getText().toString());
        values.put(TableCheckingAccount.NOTES, edtNotes.getText().toString());

        // check whether the application should do the update or insert
        if (Constants.INTENT_ACTION_INSERT.equals(mIntentAction) || Constants.INTENT_ACTION_PASTE.equals(mIntentAction)) {
            // insert
            Uri insert = getContentResolver().insert(mCheckingAccount.getUri(), values);
            if (insert == null) {
                Toast.makeText(getApplicationContext(), R.string.db_checking_insert_failed, Toast.LENGTH_SHORT).show();
                Log.w(CheckingAccountConstants.LOGCAT, "Insert new transaction failed!");
                return false;
            }
            long id = ContentUris.parseId(insert);
//            mTransId = Integer.parseInt(insert.getPathSegments().get(1));
            mTransId = (int) id;
        } else {
            // update
            if (getContentResolver().update(mCheckingAccount.getUri(), values,
                    TableCheckingAccount.TRANSID + "=?", new String[]{Integer.toString(mTransId)}) <= 0) {
                Toast.makeText(getApplicationContext(), R.string.db_checking_update_failed, Toast.LENGTH_SHORT).show();
                Log.w(CheckingAccountConstants.LOGCAT, "Update transaction failed!");
                return false;
            }
        }

        // Split Categories

        // Delete any split categories if split is unchecked.
        if(!mCommonFunctions.chbSplitTransaction.isChecked()) {
            removeAllSplitCategories();
        }

        // has split categories
        boolean hasSplitCategories = hasSplitCategories();
        // update split transaction
        if (hasSplitCategories) {
            for (int i = 0; i < mSplitTransactions.size(); i++) {
                TableSplitTransactions split = mSplitTransactions.get(i);
                // do nothing if the split is marked for deletion.
                ArrayList<TableSplitTransactions> deletedSplits = getDeletedSplitCategories();
                if(deletedSplits.contains(split)) {
                    continue;
                }

                values.clear();
                //put value
                values.put(TableSplitTransactions.CATEGID, mSplitTransactions.get(i).getCategId());
                values.put(TableSplitTransactions.SUBCATEGID, mSplitTransactions.get(i).getSubCategId());
                values.put(TableSplitTransactions.SPLITTRANSAMOUNT, mSplitTransactions.get(i).getSplitTransAmount());
                values.put(TableSplitTransactions.TRANSID, mTransId);

                if (mSplitTransactions.get(i).getSplitTransId() == -1) {
                    // insert data
                    if (getContentResolver().insert(mSplitTransactions.get(i).getUri(), values) == null) {
                        Toast.makeText(getApplicationContext(), R.string.db_checking_insert_failed, Toast.LENGTH_SHORT).show();
                        Log.w(CheckingAccountConstants.LOGCAT, "Insert new split transaction failed!");
                        return false;
                    }
                } else {
                    // update data
                    if (getContentResolver().update(mSplitTransactions.get(i).getUri(), values,
                            TableSplitTransactions.SPLITTRANSID + "=?",
                            new String[]{Integer.toString(mSplitTransactions.get(i).getSplitTransId())}) <= 0) {
                        Toast.makeText(getApplicationContext(), R.string.db_checking_update_failed, Toast.LENGTH_SHORT).show();
                        Log.w(CheckingAccountConstants.LOGCAT, "Update split transaction failed!");
                        return false;
                    }
                }
            }
        }
        // deleted old split transaction
        if (mSplitTransactionsDeleted != null && !mSplitTransactionsDeleted.isEmpty()) {
            for (int i = 0; i < mSplitTransactionsDeleted.size(); i++) {
                values.clear();
                //put value
                values.put(TableSplitTransactions.SPLITTRANSAMOUNT, mSplitTransactionsDeleted.get(i).getSplitTransAmount());

                // update data
                if (getContentResolver().delete(mSplitTransactionsDeleted.get(i).getUri(),
                        TableSplitTransactions.SPLITTRANSID + "=?",
                        new String[]{Integer.toString(mSplitTransactionsDeleted.get(i).getSplitTransId())}) <= 0) {
                    Toast.makeText(getApplicationContext(), R.string.db_checking_update_failed, Toast.LENGTH_SHORT).show();
                    Log.w(CheckingAccountConstants.LOGCAT, "Delete split transaction failed!");
                    return false;
                }
            }
        }

        // update category and subcategory payee
        if ((!isTransfer) && (mPayeeId > 0) && !hasSplitCategories) {
            // clear content value for update categoryId, subCategoryId
            values.clear();
            // set categoryId and subCategoryId
            values.put(TablePayee.CATEGID, mCategoryId);
            values.put(TablePayee.SUBCATEGID, mSubCategoryId);
            // create instance TablePayee for update
            TablePayee payee = new TablePayee();
            // update data
            if (getContentResolver().update(payee.getUri(), values,
                    TablePayee.PAYEEID + "=" + Integer.toString(mPayeeId), null) <= 0) {
                Toast.makeText(getApplicationContext(), R.string.db_payee_update_failed, Toast.LENGTH_SHORT).show();
                Log.w(CheckingAccountConstants.LOGCAT, "Update Payee with Id=" + Integer.toString(mPayeeId) + " return <= 0");
            }
        }

        //update recurring transaction
        if (mRecurringTransactionId > -1 && !(TextUtils.isEmpty(mNextOccurrence))) {
            values.clear();

            // handle transactions that do not repeat any more.
            RecurringTransaction recurringTransaction = new RecurringTransaction(mRecurringTransactionId, this);
            if(mNextOccurrence.equals(transactionDate)) {
                // The next occurrence date is the same as the current. Expired.
                recurringTransaction.delete();
            } else {
                // store next occurrence date.
                recurringTransaction.setNextOccurrenceDate(mNextOccurrence);
            }
        }
        return true;
    }

    private boolean createSplitCategoriesFromRecurringTransaction() {
        // check if category and sub-category are not set.
        if(!(mCategoryId <= 0 && mSubCategoryId <= 0)) return false;

        // Adding transactions to the split list will set the Split checkbox and the category name.

        // create split transactions
        RecurringTransaction recurringTransaction = new RecurringTransaction(mRecurringTransactionId, this);
        ArrayList<TableBudgetSplitTransactions> splitTemplates = recurringTransaction.loadSplitTransactions();
        if(mSplitTransactions == null) mSplitTransactions = new ArrayList<>();

        // For each of the templates, create a new record.
        for(int i = 0; i <= splitTemplates.size() - 1; i++) {
            TableBudgetSplitTransactions record = splitTemplates.get(i);

            TableSplitTransactions newSplit = new TableSplitTransactions();
            newSplit.setSplitTransAmount(record.getSplitTransAmount());
            newSplit.setCategId(record.getCategId());
            newSplit.setSubCategId(record.getSubCategId());

            mSplitTransactions.add(newSplit);
        }

        return true;
    }

    /**
     * When cancelling changing the transaction type to Tranfer, revert back to the
     * previous transaction type.
     */
    private void cancelChangingTransactionToTransfer() {
        // Select the previous transaction type.
        @SuppressWarnings("unchecked")
        ArrayAdapter<String> adapterTrans = (ArrayAdapter<String>) mCommonFunctions.spinTransCode.getAdapter();
        int originalPosition = adapterTrans.getPosition(getTransactionType());
        mCommonFunctions.spinTransCode.setSelection(originalPosition);
    }

    /**
     * getCategoryFromPayee set last category used from payee
     *
     * @param payeeId Identify of payee
     * @return true if category set
     */
    public boolean getCategoryFromPayee(int payeeId) {
        boolean ret = false;
        // take data of payee
        TablePayee payee = new TablePayee();
        Cursor curPayee = getContentResolver().query(payee.getUri(),
                payee.getAllColumns(), "PAYEEID=" + Integer.toString(payeeId), null, null);
        // check cursor is valid
        if ((curPayee != null) && (curPayee.moveToFirst())) {
            // chek if category is valid
            if (curPayee.getInt(curPayee.getColumnIndex(TablePayee.CATEGID)) != -1) {
                // prendo la categoria e la subcategorie
                mCategoryId = curPayee.getInt(curPayee.getColumnIndex(TablePayee.CATEGID));
                mSubCategoryId = curPayee.getInt(curPayee.getColumnIndex(TablePayee.SUBCATEGID));
                // create instance of query
                QueryCategorySubCategory category = new QueryCategorySubCategory(getApplicationContext());
                // compose selection
                String where = "CATEGID=" + Integer.toString(mCategoryId) + " AND SUBCATEGID=" + Integer.toString(mSubCategoryId);
                Cursor curCategory = getContentResolver().query(category.getUri(), category.getAllColumns(), where, null, null);
                // check cursor is valid
                if ((curCategory != null) && (curCategory.moveToFirst())) {
                    // take names of category and subcategory
                    mCommonFunctions.mCategoryName = curCategory.getString(curCategory.getColumnIndex(QueryCategorySubCategory.CATEGNAME));
                    mCommonFunctions.mSubCategoryName = curCategory.getString(curCategory.getColumnIndex(QueryCategorySubCategory.SUBCATEGNAME));
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

    public String getTransactionType() {
        if (mCommonFunctions.mTransactionType == null) {
            return null;
        }

        // mTransType
        return mCommonFunctions.mTransactionType.name();
    }

}
