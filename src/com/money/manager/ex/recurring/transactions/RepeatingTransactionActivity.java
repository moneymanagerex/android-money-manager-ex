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
package com.money.manager.ex.recurring.transactions;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.money.manager.ex.CategorySubCategoryExpandableListActivity;
import com.money.manager.ex.Constants;
import com.money.manager.ex.PayeeActivity;
import com.money.manager.ex.R;
import com.money.manager.ex.SplitTransactionsActivity;
import com.money.manager.ex.businessobjects.RecurringTransaction;
import com.money.manager.ex.checkingaccount.EditTransactionCommonFunctions;
import com.money.manager.ex.checkingaccount.YesNoDialog;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.NumericHelper;
import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.database.MoneyManagerOpenHelper;
import com.money.manager.ex.database.QueryCategorySubCategory;
import com.money.manager.ex.database.TableAccountList;
import com.money.manager.ex.database.TableBillsDeposits;
import com.money.manager.ex.database.TableBudgetSplitTransactions;
import com.money.manager.ex.database.TableCategory;
import com.money.manager.ex.database.TableCheckingAccount;
import com.money.manager.ex.database.TablePayee;
import com.money.manager.ex.database.TableSplitTransactions;
import com.money.manager.ex.database.TableSubCategory;
import com.money.manager.ex.fragment.BaseFragmentActivity;
import com.money.manager.ex.fragment.IInputAmountDialogListener;
import com.money.manager.ex.fragment.InputAmountDialog;
import com.money.manager.ex.utils.CurrencyUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Recurring transactions are stored in BillsDeposits table.
 * @author Alessandro Lazzari (lazzari.ale@gmail.com)
 */
public class RepeatingTransactionActivity
        extends BaseFragmentActivity
        implements IInputAmountDialogListener {

    private static final String LOGCAT = RepeatingTransactionActivity.class.getSimpleName();
    // ID REQUEST Data
    private static final int REQUEST_PICK_PAYEE = 1;
    private static final int REQUEST_PICK_CATEGORY = 3;
    public static final int REQUEST_PICK_SPLIT_TRANSACTION = 4;
    // KEY INTENT per il passaggio dei dati
    public static final String KEY_BILL_DEPOSITS_ID = "RepeatingTransaction:BillDepositsId";
    public static final String KEY_ACCOUNT_ID = "RepeatingTransaction:AccountId";
    public static final String KEY_TO_ACCOUNT_ID = "RepeatingTransaction:ToAccountId";
    public static final String KEY_TO_ACCOUNT_NAME = "RepeatingTransaction:ToAccountName";
    public static final String KEY_TRANS_CODE = "RepeatingTransaction:TransCode";
    public static final String KEY_TRANS_STATUS = "RepeatingTransaction:TransStatus";
//    public static final String KEY_TRANS_DATE = "RepeatingTransaction:TransDate";
    public static final String KEY_TRANS_AMOUNT = "RepeatingTransaction:TransAmount";
    public static final String KEY_TRANS_TOTAMOUNT = "RepeatingTransaction:TransTotAmount";
    public static final String KEY_PAYEE_ID = "RepeatingTransaction:PayeeId";
    public static final String KEY_PAYEE_NAME = "RepeatingTransaction:PayeeName";
    public static final String KEY_CATEGORY_ID = "RepeatingTransaction:CategoryId";
    public static final String KEY_CATEGORY_NAME = "RepeatingTransaction:CategoryName";
    public static final String KEY_SUBCATEGORY_ID = "RepeatingTransaction:SubCategoryId";
    public static final String KEY_SUBCATEGORY_NAME = "RepeatingTransaction:SubCategoryName";
    public static final String KEY_NOTES = "RepeatingTransaction:Notes";
    public static final String KEY_TRANS_NUMBER = "RepeatingTransaction:TransNumber";
    public static final String KEY_NEXT_OCCURRENCE = "RepeatingTransaction:NextOccurrence";
    public static final String KEY_REPEATS = "RepeatingTransaction:Repeats";
    public static final String KEY_NUM_OCCURRENCE = "RepeatingTransaction:NumOccurrence";
    public static final String KEY_SPLIT_TRANSACTION = "RepeatingTransaction:SplitTransaction";
    public static final String KEY_SPLIT_TRANSACTION_DELETED = "RepeatingTransaction:SplitTransactionDeleted";
    public static final String KEY_ACTION = "RepeatingTransaction:Action";

    // action type intent
    private String mIntentAction;
    private String mToAccountName;
    private int mBillDepositsId = Constants.NOT_SET;
    private String mStatus;
    // info payee
    private int mPayeeId = Constants.NOT_SET;
    private String mPayeeName, mTextDefaultPayee;
    // info category and subcategory
    private int mCategoryId = Constants.NOT_SET, mSubCategoryId = Constants.NOT_SET;
//    private String mCategoryName, mSubCategoryName;
    // arrays to manage transcode and status
    private String[] mTransCodeItems, mStatusItems;
    private String[] mTransCodeValues, mStatusValues;
    // amount
    private double mTotAmount = 0, mAmount = 0;
    // notes
    private String mNotes = "";
    // transaction numbers
    private String mTransNumber = "";
    // next occurrence
    private String mNextOccurrence = "";
    private int mFrequencies = 0;
    private int mNumOccurrence = Constants.NOT_SET;

    // Controls on the form.
    private Spinner spinFrequencies;
    private ImageButton btnTransNumber;
    private EditText edtTransNumber, edtNotes, edtTimesRepeated;
//    public CheckBox chbSplitTransaction;
    private TextView txtPayee, txtSelectPayee, txtCaptionAmount, txtRepeats,
            txtTimesRepeated, txtNextOccurrence;

    // object of the table
    TableBillsDeposits mRepeatingTransaction = new TableBillsDeposits();
    // list split transactions
    ArrayList<TableBudgetSplitTransactions> mSplitTransactions = null;
    ArrayList<TableBudgetSplitTransactions> mSplitTransactionsDeleted = null;
    private EditTransactionCommonFunctions mCommonFunctions;

    /**
     * getCategoryFromPayee set last category used from payee
     *
     * @param payeeId Identify of payee
     * @return true if category set
     */
    private boolean getCategoryFromPayee(int payeeId) {
        boolean ret = false;
        // take data of payee
        TablePayee payee = new TablePayee();
        Cursor curPayee = getContentResolver().query(payee.getUri(), payee.getAllColumns(),
                "PAYEEID=" + Integer.toString(payeeId), null, null);
        // check cursor is valid
        if ((curPayee != null) && (curPayee.moveToFirst())) {
            // chek if category is valid
            if (curPayee.getInt(curPayee.getColumnIndex(TablePayee.CATEGID)) != Constants.NOT_SET) {
                mCategoryId = curPayee.getInt(curPayee.getColumnIndex(TablePayee.CATEGID));
                mSubCategoryId = curPayee.getInt(curPayee.getColumnIndex(TablePayee.SUBCATEGID));
                // create instance of query
                QueryCategorySubCategory category = new QueryCategorySubCategory(getApplicationContext());
                // compose selection
                String where = "CATEGID=" + Integer.toString(mCategoryId) + " AND SUBCATEGID=" + Integer.toString(mSubCategoryId);
                Cursor curCategory = getContentResolver().query(category.getUri(),
                        category.getAllColumns(), where, null, null);
                // check cursor is valid
                if ((curCategory != null) && (curCategory.moveToFirst())) {
                    // take names of category and subcategory
                    mCommonFunctions.mCategoryName = curCategory.getString(curCategory.getColumnIndex(QueryCategorySubCategory.CATEGNAME));
                    mCommonFunctions.mSubCategoryName = curCategory.getString(curCategory.getColumnIndex(QueryCategorySubCategory.SUBCATEGNAME));
                    // return true
                    ret = true;

                    curCategory.close();
                }
            }

            curPayee.close();
        }

        return ret;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_PICK_PAYEE:
                if ((resultCode == Activity.RESULT_OK) && (data != null)) {
                    mPayeeId = data.getIntExtra(PayeeActivity.INTENT_RESULT_PAYEEID, Constants.NOT_SET);
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
            case REQUEST_PICK_CATEGORY:
                if ((resultCode == Activity.RESULT_OK) && (data != null)) {
                    mCategoryId = data.getIntExtra(CategorySubCategoryExpandableListActivity.INTENT_RESULT_CATEGID, Constants.NOT_SET);
                    mCommonFunctions.mCategoryName = data.getStringExtra(CategorySubCategoryExpandableListActivity.INTENT_RESULT_CATEGNAME);
                    mSubCategoryId = data.getIntExtra(CategorySubCategoryExpandableListActivity.INTENT_RESULT_SUBCATEGID, Constants.NOT_SET);
                    mCommonFunctions.mSubCategoryName = data.getStringExtra(CategorySubCategoryExpandableListActivity.INTENT_RESULT_SUBCATEGNAME);
                    // refresh UI category
                    mCommonFunctions.refreshCategoryName();
                }
                break;
            case REQUEST_PICK_SPLIT_TRANSACTION:
                if ((resultCode == Activity.RESULT_OK) && (data != null)) {
                    mSplitTransactions = data.getParcelableArrayListExtra(SplitTransactionsActivity.INTENT_RESULT_SPLIT_TRANSACTION);
                    if (mSplitTransactions != null && mSplitTransactions.size() > 0) {
                        double totAmount = 0;
                        for (int i = 0; i < mSplitTransactions.size(); i++) {
                            totAmount += mSplitTransactions.get(i).getSplitTransAmount();
                        }
                        Core core = new Core(getBaseContext());
//                        formatAmount(txtTotAmount, totAmount, !Constants.TRANSACTION_TYPE_TRANSFER.equals(mTransCode) ? mAccountId : mToAccountId);
                        int accountId = !mCommonFunctions.mTransactionType.equals(TransactionTypes.Transfer)
                                ? mCommonFunctions.mAccountId
                                : mCommonFunctions.mToAccountId;
                        core.formatAmountTextView(mCommonFunctions.txtTotAmount, totAmount, getCurrencyIdFromAccountId(accountId));
                    }
                    // deleted item
                    if (data.getParcelableArrayListExtra(SplitTransactionsActivity.INTENT_RESULT_SPLIT_TRANSACTION_DELETED) != null) {
                        mSplitTransactionsDeleted = data.getParcelableArrayListExtra(SplitTransactionsActivity.INTENT_RESULT_SPLIT_TRANSACTION_DELETED);
                    }
                }
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.repeatingtransaction_activity);

        setToolbarStandardAction(getToolbar());

        mCommonFunctions = new EditTransactionCommonFunctions(this);

        // manage save instance
        if ((savedInstanceState != null)) {
            mBillDepositsId = savedInstanceState.getInt(KEY_BILL_DEPOSITS_ID);
            mCommonFunctions.mAccountId = savedInstanceState.getInt(KEY_ACCOUNT_ID);
            mCommonFunctions.mToAccountId = savedInstanceState.getInt(KEY_TO_ACCOUNT_ID);
            mToAccountName = savedInstanceState.getString(KEY_TO_ACCOUNT_NAME);
            String transCode = savedInstanceState.getString(KEY_TRANS_CODE);
            mCommonFunctions.mTransactionType = TransactionTypes.valueOf(transCode);
            mStatus = savedInstanceState.getString(KEY_TRANS_STATUS);
            mAmount = savedInstanceState.getDouble(KEY_TRANS_AMOUNT);
            mTotAmount = savedInstanceState.getDouble(KEY_TRANS_TOTAMOUNT);
            mPayeeId = savedInstanceState.getInt(KEY_PAYEE_ID);
            mPayeeName = savedInstanceState.getString(KEY_PAYEE_NAME);
            mCategoryId = savedInstanceState.getInt(KEY_CATEGORY_ID);
            mCommonFunctions.mCategoryName = savedInstanceState.getString(KEY_CATEGORY_NAME);
            mSubCategoryId = savedInstanceState.getInt(KEY_SUBCATEGORY_ID);
            mCommonFunctions.mSubCategoryName = savedInstanceState.getString(KEY_SUBCATEGORY_NAME);
            mNotes = savedInstanceState.getString(KEY_NOTES);
            mTransNumber = savedInstanceState.getString(KEY_TRANS_NUMBER);
            mSplitTransactions = savedInstanceState.getParcelableArrayList(KEY_SPLIT_TRANSACTION);
            mSplitTransactionsDeleted = savedInstanceState.getParcelableArrayList(KEY_SPLIT_TRANSACTION_DELETED);
            mNextOccurrence = savedInstanceState.getString(KEY_NEXT_OCCURRENCE);
            mFrequencies = savedInstanceState.getInt(KEY_REPEATS);
            mNumOccurrence = savedInstanceState.getInt(KEY_NUM_OCCURRENCE);
            // action
            mIntentAction = savedInstanceState.getString(KEY_ACTION);
        }

        // Controls need to be at the beginning as they are referenced throughout the code.
        mCommonFunctions.findControls();

        // manage intent
        if (getIntent() != null) {
            if (savedInstanceState == null) {
                mCommonFunctions.mAccountId = getIntent().getIntExtra(KEY_ACCOUNT_ID, Constants.NOT_SET);
                if (getIntent().getAction() != null && Intent.ACTION_EDIT.equals(getIntent().getAction())) {
                    mBillDepositsId = getIntent().getIntExtra(KEY_BILL_DEPOSITS_ID, Constants.NOT_SET);
                    // select data transaction
                    loadRecurringTransaction(mBillDepositsId);
                }
            }
            mIntentAction = getIntent().getAction();
            // set title
            getSupportActionBar().setTitle(Constants.INTENT_ACTION_INSERT.equals(mIntentAction)
                    ? R.string.new_repeating_transaction : R.string.edit_repeating_transaction);
        }

        // Controls

//        mCommonFunctions.txtAmount = (TextView) findViewById(R.id.editTextAmount);
//        mCommonFunctions.txtTotAmount = (TextView) findViewById(R.id.editTextTotAmount);
//        mCommonFunctions.chbSplitTransaction = (CheckBox) findViewById(R.id.checkBoxSplitTransaction);
//        mCommonFunctions.spinAccount = (Spinner) findViewById(R.id.spinnerAccount);
        txtPayee = (TextView) findViewById(R.id.textViewPayee);
        txtCaptionAmount = (TextView) findViewById(R.id.textViewHeaderTotalAmount);
        spinFrequencies = (Spinner) findViewById(R.id.spinnerFrequencies);
        txtRepeats = (TextView) findViewById(R.id.textViewRepeat);
        txtTimesRepeated = (TextView) findViewById(R.id.textViewTimesRepeated);
//        mCommonFunctions.txtSelectCategory = (TextView) findViewById(R.id.textViewSelectCategory);

        Core core = new Core(getApplicationContext());

        // Account(s)
        mCommonFunctions.initAccountSelectors();

        // Transaction type
        initTransactionTypeSelector();

        // status
        mCommonFunctions.spinStatus = (Spinner) findViewById(R.id.spinnerStatus);
        // arrays to manage Status
        mStatusItems = getResources().getStringArray(R.array.status_items);
        mStatusValues = getResources().getStringArray(R.array.status_values);
        // create adapter for spinnerStatus
        ArrayAdapter<String> adapterStatus = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, mStatusItems);
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

        // payee
        txtSelectPayee = (TextView) findViewById(R.id.textViewSelectPayee);
        txtSelectPayee.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RepeatingTransactionActivity.this, PayeeActivity.class);
                intent.setAction(Intent.ACTION_PICK);
                startActivityForResult(intent, REQUEST_PICK_PAYEE);
            }
        });

        // Category

        mCommonFunctions.txtSelectCategory.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mCommonFunctions.chbSplitTransaction.isChecked()) {
                    Intent intent = new Intent(RepeatingTransactionActivity.this,
                            CategorySubCategoryExpandableListActivity.class);
                    intent.setAction(Intent.ACTION_PICK);
                    startActivityForResult(intent, REQUEST_PICK_CATEGORY);
                } else {
                    // Open the activity for creating split transactions.
                    Intent intent = new Intent(RepeatingTransactionActivity.this, SplitTransactionsActivity.class);
                    // Pass the name of the entity/data set.
                    intent.putExtra(SplitTransactionsActivity.KEY_DATASET_TYPE,
                            TableBudgetSplitTransactions.class.getSimpleName());
                    intent.putExtra(SplitTransactionsActivity.KEY_TRANSACTION_TYPE, mCommonFunctions.mTransactionType.getCode());
                    intent.putParcelableArrayListExtra(SplitTransactionsActivity.KEY_SPLIT_TRANSACTION,
                            mSplitTransactions);
                    intent.putParcelableArrayListExtra(SplitTransactionsActivity.KEY_SPLIT_TRANSACTION_DELETED,
                            mSplitTransactionsDeleted);
                    startActivityForResult(intent, REQUEST_PICK_SPLIT_TRANSACTION);
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
                if (mCommonFunctions.spinAccount.getSelectedItemPosition() >= 0
                        && mCommonFunctions.spinAccount.getSelectedItemPosition() < mCommonFunctions.AccountList.size()) {
                    currencyId = mCommonFunctions.AccountList.get(mCommonFunctions.spinAccount.getSelectedItemPosition()).getCurrencyId();
                }
                double amount = (Double) v.getTag();
                InputAmountDialog dialog = InputAmountDialog.getInstance(RepeatingTransactionActivity.this,
                        v.getId(), amount, currencyId);
                dialog.show(getSupportFragmentManager(), dialog.getClass().getSimpleName());
            }
        };

        boolean isTransfer = mCommonFunctions.mTransactionType.equals(TransactionTypes.Transfer);

        // total amount
        core.formatAmountTextView(mCommonFunctions.txtTotAmount, mTotAmount,
                getCurrencyIdFromAccountId(!isTransfer
                        ? mCommonFunctions.mAccountId
                        : mCommonFunctions.mToAccountId));
        mCommonFunctions.txtTotAmount.setOnClickListener(onClickAmount);

        // amount
        core.formatAmountTextView(mCommonFunctions.txtAmount, mAmount,
                getCurrencyIdFromAccountId(!isTransfer
                        ? mCommonFunctions.mToAccountId
                        : mCommonFunctions.mAccountId));
        mCommonFunctions.txtAmount.setOnClickListener(onClickAmount);

        // transaction number
        edtTransNumber = (EditText) findViewById(R.id.editTextTransNumber);
        if (!TextUtils.isEmpty(mTransNumber) && NumericHelper.isNumeric(mTransNumber)) {
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
                        // Use BigDecimal to allow for large numbers.
                        BigDecimal transactionNumber = new BigDecimal(transNumber);
                        edtTransNumber.setText(transactionNumber.add(BigDecimal.ONE).toString());
                    }
                    cursor.close();
                }
            }
        });
        // notes
        edtNotes = (EditText) findViewById(R.id.editTextNotes);
        if (!(TextUtils.isEmpty(mNotes))) {
            edtNotes.setText(mNotes);
        }
        // next occurrence
        txtNextOccurrence = (TextView) findViewById(R.id.editTextNextOccurrence);

        if (!(TextUtils.isEmpty(mNextOccurrence))) {
            try {
//                Locale locale = getResources().getConfiguration().locale;
                txtNextOccurrence.setTag(new SimpleDateFormat(Constants.PATTERN_DB_DATE)
                        .parse(mNextOccurrence));
            } catch (ParseException e) {
                Log.e(LOGCAT, e.getMessage());
            }
        } else {
            txtNextOccurrence.setTag(Calendar.getInstance().getTime());
        }
        formatExtendedDate(txtNextOccurrence);
        txtNextOccurrence.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar date = Calendar.getInstance();
                date.setTime((Date) txtNextOccurrence.getTag());
                DatePickerDialog dialog = new DatePickerDialog(RepeatingTransactionActivity.this,
                        mDateSetListener, date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DATE));
                dialog.show();
            }

            public DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    try {
                        Locale locale = getResources().getConfiguration().locale;
                        Date date = new SimpleDateFormat("yyyy-MM-dd", locale)
                                .parse(Integer.toString(year) + "-" + Integer.toString(monthOfYear + 1) +
                                        "-" + Integer.toString(dayOfMonth));
                        txtNextOccurrence.setTag(date);
                        formatExtendedDate(txtNextOccurrence);
                    } catch (Exception e) {
                        Log.e(LOGCAT, e.getMessage());
                    }
                }
            };

        });

        // times repeated
        edtTimesRepeated = (EditText) findViewById(R.id.editTextTimesRepeated);
        if (mNumOccurrence >= 0) {
            edtTimesRepeated.setText(Integer.toString(mNumOccurrence));
        }
        // frequencies
        if (mFrequencies >= 200) {
            mFrequencies = mFrequencies - 200;
        } // set auto execute without user acknowlegement
        if (mFrequencies >= 100) {
            mFrequencies = mFrequencies - 100;
        } // set auto execute on the next occurrence
        spinFrequencies.setSelection(mFrequencies, true);
        spinFrequencies.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mFrequencies = position;
                refreshTimesRepeated();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mFrequencies = 0;
                refreshTimesRepeated();
            }
        });
        // refresh user interface
        refreshAfterTransactionCodeChange();
        refreshPayeeName();
        mCommonFunctions.refreshCategoryName();
        refreshTimesRepeated();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // save the state interface
        outState.putInt(KEY_BILL_DEPOSITS_ID, mBillDepositsId);
        outState.putInt(KEY_ACCOUNT_ID, mCommonFunctions.mAccountId);
        outState.putInt(KEY_TO_ACCOUNT_ID, mCommonFunctions.mToAccountId);
        outState.putString(KEY_TO_ACCOUNT_NAME, mToAccountName);
        outState.putString(KEY_TRANS_CODE, getTransactionType());
        outState.putString(KEY_TRANS_STATUS, mStatus);
        outState.putDouble(KEY_TRANS_TOTAMOUNT, (Double) mCommonFunctions.txtTotAmount.getTag());
        outState.putDouble(KEY_TRANS_AMOUNT, (Double) mCommonFunctions.txtAmount.getTag());
        outState.putInt(KEY_PAYEE_ID, mPayeeId);
        outState.putString(KEY_PAYEE_NAME, mPayeeName);
        outState.putInt(KEY_CATEGORY_ID, mCategoryId);
        outState.putString(KEY_CATEGORY_NAME, mCommonFunctions.mCategoryName);
        outState.putInt(KEY_SUBCATEGORY_ID, mSubCategoryId);
        outState.putString(KEY_SUBCATEGORY_NAME, mCommonFunctions.mSubCategoryName);
        outState.putString(KEY_TRANS_NUMBER, edtTransNumber.getText().toString());
        outState.putParcelableArrayList(KEY_SPLIT_TRANSACTION, mSplitTransactions);
        outState.putParcelableArrayList(KEY_SPLIT_TRANSACTION_DELETED, mSplitTransactionsDeleted);
        outState.putString(KEY_NOTES, String.valueOf(edtNotes.getTag()));
        Locale locale = getResources().getConfiguration().locale;
        outState.putString(KEY_NEXT_OCCURRENCE, new SimpleDateFormat(Constants.PATTERN_DB_DATE)
                .format(txtNextOccurrence.getTag()));
        outState.putInt(KEY_REPEATS, mFrequencies);
        if (!TextUtils.isEmpty(edtTimesRepeated.getText())) {
            outState.putInt(KEY_NUM_OCCURRENCE, Integer.parseInt(edtTimesRepeated.getText().toString()));
        } else {
            outState.putInt(KEY_NUM_OCCURRENCE, Constants.NOT_SET);
        }
        outState.putString(KEY_ACTION, mIntentAction);
    }

    @Override
    public void onFinishedInputAmountDialog(int id, Double amount) {
        Core core = new Core(getApplicationContext());

        View view = findViewById(id);
        int accountId;
        if (view != null && view instanceof TextView) {
            boolean isTransfer = mCommonFunctions.mTransactionType.equals(TransactionTypes.Transfer);
            CurrencyUtils currencyUtils = new CurrencyUtils(getApplicationContext());
            if (isTransfer) {
                Double originalAmount;
                try {
                    Integer toCurrencyId = mCommonFunctions.AccountList.get(mCommonFunctions.mAccountIdList.indexOf(id == R.id.textViewTotAmount
                            ? mCommonFunctions.mAccountId : mCommonFunctions.mToAccountId)).getCurrencyId();
                    Integer fromCurrencyId = mCommonFunctions.AccountList.get(mCommonFunctions.mAccountIdList.indexOf(id == R.id.textViewTotAmount
                            ? mCommonFunctions.mToAccountId : mCommonFunctions.mAccountId)).getCurrencyId();
                    // take a original values
                    originalAmount = id == R.id.textViewTotAmount
                            ? (Double) mCommonFunctions.txtTotAmount.getTag()
                            : (Double) mCommonFunctions.txtAmount.getTag();
                    // convert value
                    Double amountExchange = currencyUtils.doCurrencyExchange(toCurrencyId, originalAmount, fromCurrencyId);
                    // take original amount converted
                    originalAmount = id == R.id.textViewTotAmount
                            ? (Double) mCommonFunctions.txtAmount.getTag()
                            : (Double) mCommonFunctions.txtTotAmount.getTag();
                    if (originalAmount == null)
                        originalAmount = 0d;
                    // check if two values is equals, and then convert value
                    DecimalFormat decimalFormat = new DecimalFormat("0.00");
                    if (originalAmount == 0) {
                        if (decimalFormat.format(originalAmount).equals(decimalFormat.format(amountExchange))) {
                            amountExchange = currencyUtils.doCurrencyExchange(toCurrencyId, amount, fromCurrencyId);
                            core.formatAmountTextView(id == R.id.textViewTotAmount
                                            ? mCommonFunctions.txtAmount : mCommonFunctions.txtTotAmount,
                                    amountExchange, getCurrencyIdFromAccountId(id == R.id.textViewTotAmount
                                            ? mCommonFunctions.mAccountId : mCommonFunctions.mToAccountId));
                        }
                    }

                } catch (Exception e) {
                    Log.e(LOGCAT, e.getMessage());
                }
            }
            if (mCommonFunctions.txtTotAmount.equals(view)) {
                if (isTransfer) {
                    accountId = mCommonFunctions.mToAccountId;
                } else {
                    accountId = mCommonFunctions.mAccountId;
                }
            } else {
                accountId = mCommonFunctions.mAccountId;
            }
            core.formatAmountTextView((TextView) view, amount, getCurrencyIdFromAccountId(accountId));
        }
    }

    @Override
    public boolean onActionCancelClick() {
        final MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title(android.R.string.cancel)
                .content(R.string.transaction_cancel_confirm)
                .positiveText(R.string.discard)
                .negativeText(R.string.keep_editing)
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
            // set result ok, send broadcast to update widgets and finish activity
            setResult(RESULT_OK);
            finish();
        }

        return super.onActionDoneClick();
    }

    /**
     * refersh UI control times repeated
     */
    public void refreshTimesRepeated() {
        edtTimesRepeated.setVisibility(mFrequencies > 0 ? View.VISIBLE : View.GONE);
        txtRepeats.setText((mFrequencies == 11) || (mFrequencies == 12) ? R.string.activates : R.string.repeats);
        txtTimesRepeated.setVisibility(mFrequencies > 0 ? View.VISIBLE : View.GONE);
        txtTimesRepeated.setText(mFrequencies >= 11 ? R.string.activates : R.string.times_repeated);
        edtTimesRepeated.setHint(mFrequencies >= 11 ? R.string.activates : R.string.times_repeated);
    }

    /**
     * query info payee
     *
     * @param accountId id payee
     * @return true if the data selected
     */
    private boolean selectAccountName(int accountId) {
        TableAccountList account = new TableAccountList();
        Cursor cursor = getContentResolver().query(account.getUri(),
                account.getAllColumns(),
                TableAccountList.ACCOUNTID + "=?",
                new String[]{Integer.toString(accountId)}, null);
        // check if cursor is valid and open
        if ((cursor == null) || (!cursor.moveToFirst())) {
            return false;
        }

        // set payee name
        mToAccountName = cursor.getString(cursor.getColumnIndex(TableAccountList.ACCOUNTNAME));

        cursor.close();

        return true;
    }

    /**
     * Query info of Category and Subcategory
     *
     * @param categoryId Id of the category
     * @param subCategoryId Id of the sub-category
     * @return indicator whether the operation was successful.
     */
    private boolean selectCategSubName(int categoryId, int subCategoryId) {
        TableCategory category = new TableCategory();
        TableSubCategory subCategory = new TableSubCategory();
        Cursor cursor;
        // category
        cursor = getContentResolver().query(category.getUri(), category.getAllColumns(),
                TableCategory.CATEGID + "=?", new String[]{Integer.toString(categoryId)}, null);
        if ((cursor != null) && (cursor.moveToFirst())) {
            // set category name and sub category name
            mCommonFunctions.mCategoryName = cursor.getString(cursor.getColumnIndex(TableCategory.CATEGNAME));
            cursor.close();
        } else {
            mCommonFunctions.mCategoryName = null;
        }
        // sub-category
        cursor = getContentResolver().query(subCategory.getUri(), subCategory.getAllColumns(),
                TableSubCategory.SUBCATEGID + "=?", new String[]{Integer.toString(subCategoryId)}, null);
        if ((cursor != null) && (cursor.moveToFirst())) {
            // set category name and sub category name
            mCommonFunctions.mSubCategoryName = cursor.getString(cursor.getColumnIndex(TableSubCategory.SUBCATEGNAME));
            cursor.close();
        } else {
            mCommonFunctions.mSubCategoryName = null;
        }

        return true;
    }

    /**
     * query info payee
     *
     * @param payeeId id payee
     * @return true if the data selected
     */
    private boolean selectPayeeName(int payeeId) {
        TablePayee payee = new TablePayee();
        Cursor cursor = getContentResolver().query(payee.getUri(),
                payee.getAllColumns(),
                TablePayee.PAYEEID + "=?",
                new String[]{Integer.toString(payeeId)}, null);
        // check if cursor is valid and open
        if ((cursor == null) || (!cursor.moveToFirst())) {
            return false;
        }

        // set payee name
        mPayeeName = cursor.getString(cursor.getColumnIndex(TablePayee.PAYEENAME));

        cursor.close();

        return true;
    }

    /**
     * this method allows you to search the transaction data
     *
     * @param billId transaction id
     * @return true if data selected, false nothing
     */
    private boolean loadRecurringTransaction(int billId) {
        Cursor cursor = getContentResolver().query(mRepeatingTransaction.getUri(),
                mRepeatingTransaction.getAllColumns(),
                TableBillsDeposits.BDID + "=?",
                new String[]{Integer.toString(billId)}, null);
        // check if cursor is valid and open
        if ((cursor == null) || (!cursor.moveToFirst())) {
            return false;
        }

        // Read data.
        mBillDepositsId = cursor.getInt(cursor.getColumnIndex(TableBillsDeposits.BDID));
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
        mNextOccurrence = cursor.getString(cursor.getColumnIndex(TableBillsDeposits.NEXTOCCURRENCEDATE));
        mFrequencies = cursor.getInt(cursor.getColumnIndex(TableBillsDeposits.REPEATS));
        mNumOccurrence = cursor.getInt(cursor.getColumnIndex(TableBillsDeposits.NUMOCCURRENCES));

        // load split transactions only if no category selected.
        if (mCategoryId == Constants.NOT_SET && mSplitTransactions == null) {
            RecurringTransaction recurringTransaction = new RecurringTransaction(billId, this);
            mSplitTransactions = recurringTransaction.loadSplitTransactions();
        }

        cursor.close();

        selectAccountName(mCommonFunctions.mToAccountId);
        selectPayeeName(mPayeeId);
        selectCategSubName(mCategoryId, mSubCategoryId);

        return true;
    }

    public void formatExtendedDate(TextView dateTextView) {
        try {
            Locale locale = getResources().getConfiguration().locale;
            dateTextView.setText(new SimpleDateFormat("EEEE dd MMMM yyyy", locale).format((Date) dateTextView.getTag()));
        } catch (Exception e) {
            Log.e(LOGCAT, e.getMessage());
        }
    }

    public boolean hasSplitCategories() {
        return mSplitTransactions != null && !mSplitTransactions.isEmpty();
    }

//    public void refreshCategoryName() {
//        if (mCommonFunctions.txtSelectCategory == null) return;
//
//        mCommonFunctions.txtSelectCategory.setText("");
//
//        if (!mCommonFunctions.chbSplitTransaction.isChecked()) {
//            if (!TextUtils.isEmpty(mCommonFunctions.mCategoryName)) {
//                mCommonFunctions.txtSelectCategory.setText(mCommonFunctions.mCategoryName);
//                if (!TextUtils.isEmpty(mCommonFunctions.mSubCategoryName)) {
//                    mCommonFunctions.txtSelectCategory.setText(Html.fromHtml(mCommonFunctions.txtSelectCategory.getText() + " : <i>" + mCommonFunctions.mSubCategoryName + "</i>"));
//                }
//            }
//        } else {
//            mCommonFunctions.txtSelectCategory.setText("\u2026");
//        }
//    }

    /**
     * update UI interface with PayeeName
     */
    public void refreshPayeeName() {
        // write into text button payee name
        txtSelectPayee.setText(!TextUtils.isEmpty(mPayeeName) ? mPayeeName : mTextDefaultPayee);
    }

    public void refreshAfterTransactionCodeChange() {
        TextView txtFromAccount = (TextView) findViewById(R.id.textViewFromAccount);
        TextView txtToAccount = (TextView) findViewById(R.id.textViewToAccount);

        // hide and show
        boolean isTransfer = mCommonFunctions.mTransactionType.equals(TransactionTypes.Transfer);

        txtFromAccount.setText(isTransfer ? R.string.from_account : R.string.account);
        txtToAccount.setVisibility(isTransfer ? View.VISIBLE : View.GONE);

        txtCaptionAmount.setVisibility(isTransfer ? View.VISIBLE : View.GONE);
        mCommonFunctions.txtAmount.setVisibility(isTransfer ? View.VISIBLE : View.GONE);
        mCommonFunctions.spinToAccount.setVisibility(isTransfer ? View.VISIBLE : View.GONE);
        txtSelectPayee.setVisibility(!isTransfer ? View.VISIBLE : View.GONE);
        // hide split controls
        mCommonFunctions.chbSplitTransaction.setVisibility(isTransfer ? View.GONE : View.VISIBLE);

        mCommonFunctions.refreshHeaderAmount();
    }

    /**
     * validate data insert in activity
     *
     * @return validation result
     */
    private boolean validateData() {
        boolean isTransfer = mCommonFunctions.mTransactionType.equals(TransactionTypes.Transfer);

        if (isTransfer) {
            if (mCommonFunctions.mToAccountId == Constants.NOT_SET) {
                Core.alertDialog(this, R.string.error_toaccount_not_selected);
                return false;
            }
            if (mCommonFunctions.mToAccountId == mCommonFunctions.mAccountId) {
                Core.alertDialog(this, R.string.error_transfer_to_same_account);
                return false;
            }
        }
        // Payee is now optional.
//        else if ((!isTransfer()) && (mPayeeId == Constants.NOT_SET)) {
//            Core.alertDialog(this, R.string.error_payee_not_selected);
//
//            return false;
//        }

        // Category is required if tx is not a split or transfer.
        if (mCategoryId == Constants.NOT_SET && (!mCommonFunctions.chbSplitTransaction.isChecked()) && !isTransfer) {
            Core.alertDialog(this, R.string.error_category_not_selected);
            return false;
        }
        if (mCommonFunctions.chbSplitTransaction.isChecked() && (mSplitTransactions == null || mSplitTransactions.size() <= 0)) {
            Core.alertDialog(this, R.string.error_split_transaction_empty);
            return false;
        }
        if (TextUtils.isEmpty(mCommonFunctions.txtTotAmount.getText())) {
            if (TextUtils.isEmpty(mCommonFunctions.txtAmount.getText())) {
                Core.alertDialog(this, R.string.error_totamount_empty);

                return false;
            } else {
                mCommonFunctions.txtTotAmount.setText(mCommonFunctions.txtAmount.getText());
            }
        }
        if (TextUtils.isEmpty(txtNextOccurrence.getText().toString())) {
            Core.alertDialog(this, R.string.error_next_occurrence_not_populate);

            return false;
        }
        return true;
    }

    /**
     * update data into database
     *
     * @return true if update data successful
     */
    private boolean updateData() {
        if (!validateData()) {
            return false;
        }

        // content value for insert or update data
        ContentValues values = new ContentValues();

        boolean isTransfer = mCommonFunctions.mTransactionType.equals(TransactionTypes.Transfer);

        values.put(TableBillsDeposits.ACCOUNTID, mCommonFunctions.mAccountId);
        values.put(TableBillsDeposits.TOACCOUNTID, mCommonFunctions.mToAccountId);
        if (isTransfer) {
            values.put(TableBillsDeposits.PAYEEID, Constants.NOT_SET);
        } else {
            values.put(TableBillsDeposits.PAYEEID, mPayeeId);
        }
        values.put(TableBillsDeposits.TRANSCODE, getTransactionType());
        if (TextUtils.isEmpty(mCommonFunctions.txtAmount.getText().toString()) || (!(isTransfer))) {
            values.put(TableBillsDeposits.TRANSAMOUNT, (Double) mCommonFunctions.txtTotAmount.getTag());
        } else {
            values.put(TableBillsDeposits.TRANSAMOUNT, (Double) mCommonFunctions.txtAmount.getTag());
        }
        values.put(TableBillsDeposits.STATUS, mStatus);
        values.put(TableBillsDeposits.CATEGID, !mCommonFunctions.chbSplitTransaction.isChecked() ? mCategoryId : Constants.NOT_SET);
        values.put(TableBillsDeposits.SUBCATEGID, !mCommonFunctions.chbSplitTransaction.isChecked() ? mSubCategoryId : Constants.NOT_SET);
        values.put(TableBillsDeposits.FOLLOWUPID, Constants.NOT_SET);
        values.put(TableBillsDeposits.TOTRANSAMOUNT, (Double) mCommonFunctions.txtTotAmount.getTag());
        values.put(TableBillsDeposits.TRANSACTIONNUMBER, edtTransNumber.getText().toString());
        values.put(TableBillsDeposits.NOTES, edtNotes.getText().toString());
        values.put(TableBillsDeposits.NEXTOCCURRENCEDATE, new SimpleDateFormat(Constants.PATTERN_DB_DATE)
                .format(txtNextOccurrence.getTag()));
        values.put(TableBillsDeposits.TRANSDATE, new SimpleDateFormat(Constants.PATTERN_DB_DATE)
                .format(txtNextOccurrence.getTag()));
        values.put(TableBillsDeposits.REPEATS, mFrequencies);
        values.put(TableBillsDeposits.NUMOCCURRENCES, mFrequencies > 0
                ? edtTimesRepeated.getText().toString() : null);

        // check whether the application should do the update or insert
        if (Constants.INTENT_ACTION_INSERT.equals(mIntentAction)) {
            // insert
            Uri insert = getContentResolver().insert(mRepeatingTransaction.getUri(), values);
            if (insert == null) {
                Core.alertDialog(this, R.string.db_checking_insert_failed);
                Log.w(LOGCAT, "Insert new repeating transaction failed!");
                return false;
            }
            long id = ContentUris.parseId(insert);
//            mBillDepositsId = Integer.parseInt(insert.getPathSegments().get(1));
            mBillDepositsId = (int) id;
        } else {
            // update
            if (getContentResolver().update(mRepeatingTransaction.getUri(), values,
                    TableBillsDeposits.BDID + "=?", new String[]{Integer.toString(mBillDepositsId)}) <= 0) {
                Core.alertDialog(this, R.string.db_checking_update_failed);
                Log.w(LOGCAT, "Update repeating  transaction failed!");
                return false;
            }
        }
        // has split transaction
        boolean hasSplitTransaction = mSplitTransactions != null && mSplitTransactions.size() > 0;
        if (hasSplitTransaction) {
            for (int i = 0; i < mSplitTransactions.size(); i++) {
                values.clear();
                values.put(TableBudgetSplitTransactions.CATEGID, mSplitTransactions.get(i).getCategId());
                values.put(TableBudgetSplitTransactions.SUBCATEGID, mSplitTransactions.get(i).getSubCategId());
                values.put(TableBudgetSplitTransactions.SPLITTRANSAMOUNT, mSplitTransactions.get(i).getSplitTransAmount());
                values.put(TableBudgetSplitTransactions.TRANSID, mBillDepositsId);

                if (mSplitTransactions.get(i).getSplitTransId() == Constants.NOT_SET) {
                    // insert data
                    Uri insert = getContentResolver().insert(mSplitTransactions.get(i).getUri(), values);
                    if (insert == null) {
                        Toast.makeText(getApplicationContext(), R.string.db_checking_insert_failed, Toast.LENGTH_SHORT).show();
                        Log.w(LOGCAT, "Insert new split transaction failed!");
                        return false;
                    }
                } else {
                    // update data
                    if (getContentResolver().update(mSplitTransactions.get(i).getUri(), values,
                            TableSplitTransactions.SPLITTRANSID + "=?",
                            new String[]{Integer.toString(mSplitTransactions.get(i).getSplitTransId())}) <= 0) {
                        Toast.makeText(getApplicationContext(), R.string.db_checking_update_failed, Toast.LENGTH_SHORT).show();
                        Log.w(LOGCAT, "Update split transaction failed!");
                        return false;
                    }
                }
            }
        }

        // deleted old split transaction
        if (mSplitTransactionsDeleted != null && mSplitTransactionsDeleted.size() > 0) {
            for (int i = 0; i < mSplitTransactionsDeleted.size(); i++) {
                values.clear();
                //put value
                values.put(TableSplitTransactions.SPLITTRANSAMOUNT, mSplitTransactionsDeleted.get(i).getSplitTransAmount());

                // update data
                if (getContentResolver().delete(mSplitTransactionsDeleted.get(i).getUri(),
                        TableSplitTransactions.SPLITTRANSID + "=?",
                        new String[]{Integer.toString(mSplitTransactionsDeleted.get(i).getSplitTransId())}) <= 0) {
                    Toast.makeText(getApplicationContext(), R.string.db_checking_update_failed, Toast.LENGTH_SHORT).show();
                    Log.w(LOGCAT, "Delete split transaction failed!");
                    return false;
                }
            }
        }
        // update category and subcategory payee
        if ((!(isTransfer)) && (mPayeeId > 0) && (!hasSplitTransaction)) {
            // clear content value for update categoryId, subCategoryId
            values.clear();
            // set categoryId and subCategoryId
            values.put(TablePayee.CATEGID, mCategoryId);
            values.put(TablePayee.SUBCATEGID, mSubCategoryId);
            // create instance TablePayee for update
            TablePayee payee = new TablePayee();
            // update data
            if (getContentResolver().update(payee.getUri(),
                    values,
                    TablePayee.PAYEEID + "=" + Integer.toString(mPayeeId),
                    null) <= 0) {
                Toast.makeText(getApplicationContext(), R.string.db_payee_update_failed, Toast.LENGTH_SHORT).show();
                Log.w(LOGCAT, "Update Payee with Id=" + Integer.toString(mPayeeId) + " return <= 0");
            }
        }

        return true;
    }

    public Integer getCurrencyIdFromAccountId(int accountId) {
        try {
            return mCommonFunctions.AccountList.get(mCommonFunctions.mAccountIdList.indexOf(accountId)).getCurrencyId();
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

//    private void splitSet() {
//        // update category field
//        mCommonFunctions.refreshCategoryName();
//
//        boolean isSplit = mCommonFunctions.chbSplitTransaction.isChecked();
//
//        // enable/disable Amount field.
//        mCommonFunctions.txtAmount.setEnabled(!isSplit);
//        mCommonFunctions.txtTotAmount.setEnabled(!isSplit);
//    }

    private void initTransactionTypeSelector() {
        // trans-code
        mCommonFunctions.spinTransCode = (Spinner) findViewById(R.id.spinnerTransCode);
        // populate arrays TransCode
        mTransCodeItems = getResources().getStringArray(R.array.transcode_items);
        mTransCodeValues = getResources().getStringArray(R.array.transcode_values);
        // create adapter for TransCode
        ArrayAdapter<String> adapterTrans = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                mTransCodeItems);
        adapterTrans.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCommonFunctions.spinTransCode.setAdapter(adapterTrans);
        // select a current value
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
        mCategoryId = Constants.NOT_SET;

//        mTransCode = getString(R.string.transfer);
        mCommonFunctions.mTransactionType = TransactionTypes.Transfer;

        refreshAfterTransactionCodeChange();
    }

    public String getTransactionType() {
        if (mCommonFunctions.mTransactionType == null) {
            return null;
        }

        // mTransType
        return mCommonFunctions.mTransactionType.name();
    }
}

