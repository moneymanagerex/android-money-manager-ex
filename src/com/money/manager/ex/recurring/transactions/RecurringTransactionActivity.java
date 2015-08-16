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
//import android.app.DatePickerDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.money.manager.ex.common.CategoryListActivity;
import com.money.manager.ex.Constants;
import com.money.manager.ex.PayeeActivity;
import com.money.manager.ex.R;
import com.money.manager.ex.SplitTransactionsActivity;
import com.money.manager.ex.businessobjects.RecurringTransactionService;
import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.database.AccountRepository;
import com.money.manager.ex.database.RecurringTransactionRepository;
import com.money.manager.ex.transactions.EditTransactionCommonFunctions;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.NumericHelper;
import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.database.MoneyManagerOpenHelper;
import com.money.manager.ex.database.TableBillsDeposits;
import com.money.manager.ex.database.TableBudgetSplitTransactions;
import com.money.manager.ex.database.TableCategory;
import com.money.manager.ex.database.TableCheckingAccount;
import com.money.manager.ex.database.TablePayee;
import com.money.manager.ex.database.TableSplitTransactions;
import com.money.manager.ex.database.TableSubCategory;
import com.money.manager.ex.common.BaseFragmentActivity;
import com.money.manager.ex.common.IInputAmountDialogListener;
import com.money.manager.ex.common.InputAmountDialog;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Recurring transactions are stored in BillsDeposits table.
 * @author Alessandro Lazzari (lazzari.ale@gmail.com)
 */
public class RecurringTransactionActivity
        extends BaseFragmentActivity
        implements IInputAmountDialogListener {

    public static final String DATEPICKER_TAG = "datepicker";
    private static final String LOGCAT = RecurringTransactionActivity.class.getSimpleName();
    // ID REQUEST Data
    private static final int REQUEST_PICK_CATEGORY = 3;
    public static final int REQUEST_PICK_SPLIT_TRANSACTION = 4;

    public static final String KEY_MODEL = "RecurringTransactionActivity:Model";
    public static final String KEY_BILL_DEPOSITS_ID = "RepeatingTransaction:BillDepositsId";
    public static final String KEY_ACCOUNT_ID = "RepeatingTransaction:AccountId";
    public static final String KEY_TO_ACCOUNT_ID = "RepeatingTransaction:ToAccountId";
    public static final String KEY_TO_ACCOUNT_NAME = "RepeatingTransaction:ToAccountName";
    public static final String KEY_TRANS_CODE = "RepeatingTransaction:TransCode";
    public static final String KEY_TRANS_STATUS = "RepeatingTransaction:TransStatus";
    public static final String KEY_TRANS_AMOUNT = "RepeatingTransaction:TransAmount";
    public static final String KEY_TRANS_AMOUNTTO = "RepeatingTransaction:TransTotAmount";
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

    private TableBillsDeposits mRecurringTransaction;
    private int mBillDepositsId = Constants.NOT_SET;
    private String mStatus;
    private String[] mStatusItems, mStatusValues;
    // notes
    private String mNotes = "";
    // transaction numbers
    private String mTransNumber = "";
    // next occurrence
    private String mNextOccurrence = "";
    private int mFrequencies = 0;
//    private int mNumOccurrence = Constants.NOT_SET;

    // Controls on the form.
    private Spinner spinFrequencies;
    private ImageButton btnTransNumber;
    private EditText edtTransNumber, edtNotes, edtTimesRepeated;
    private TextView txtRepeats, txtTimesRepeated, txtNextOccurrence;

    private EditTransactionCommonFunctions mCommonFunctions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recurringtransaction_activity);

        setToolbarStandardAction(getToolbar());

        mCommonFunctions = new EditTransactionCommonFunctions(this);

        // manage save instance
        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState);
        }

        // Controls need to be at the beginning as they are referenced throughout the code.
        mCommonFunctions.findControls();

        // manage intent
        if (getIntent() != null) {
            if (savedInstanceState == null) {
                mCommonFunctions.accountId = getIntent().getIntExtra(KEY_ACCOUNT_ID, Constants.NOT_SET);
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

//        txtPayee = (TextView) findViewById(R.id.textViewPayee);
        spinFrequencies = (Spinner) findViewById(R.id.spinnerFrequencies);
        txtRepeats = (TextView) findViewById(R.id.textViewRepeat);
        txtTimesRepeated = (TextView) findViewById(R.id.textViewTimesRepeated);

        Core core = new Core(getApplicationContext());

        // Account(s)
        mCommonFunctions.initAccountSelectors();

        // Transaction type
        mCommonFunctions.initTransactionTypeSelector();

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

//        txtSelectPayee = (TextView) findViewById(R.id.textViewPayee);
//        txtSelectPayee.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(RecurringTransactionActivity.this, PayeeActivity.class);
//                intent.setAction(Intent.ACTION_PICK);
//                startActivityForResult(intent, REQUEST_PICK_PAYEE);
//            }
//        });
        mCommonFunctions.initPayeeControls();

        // Category

        mCommonFunctions.txtSelectCategory.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mCommonFunctions.chbSplitTransaction.isChecked()) {
                    Intent intent = new Intent(RecurringTransactionActivity.this,
                            CategoryListActivity.class);
                    intent.setAction(Intent.ACTION_PICK);
                    startActivityForResult(intent, REQUEST_PICK_CATEGORY);
                } else {
                    // Open the activity for creating split transactions.
                    Intent intent = new Intent(RecurringTransactionActivity.this, SplitTransactionsActivity.class);
                    // Pass the name of the entity/data set.
                    intent.putExtra(SplitTransactionsActivity.KEY_DATASET_TYPE,
                            TableBudgetSplitTransactions.class.getSimpleName());
                    intent.putExtra(SplitTransactionsActivity.KEY_TRANSACTION_TYPE, mCommonFunctions.transactionType.getCode());
                    intent.putParcelableArrayListExtra(SplitTransactionsActivity.KEY_SPLIT_TRANSACTION,
                            mCommonFunctions.mSplitTransactions);
                    intent.putParcelableArrayListExtra(SplitTransactionsActivity.KEY_SPLIT_TRANSACTION_DELETED,
                            mCommonFunctions.mSplitTransactionsDeleted);
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
        boolean hasSplit = mCommonFunctions.hasSplitCategories();
        mCommonFunctions.setSplit(hasSplit);

        // Amount and total amount

        mCommonFunctions.initAmountSelectors();

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
                        new String[]{Integer.toString(mCommonFunctions.accountId)});
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
                txtNextOccurrence.setTag(new SimpleDateFormat(Constants.PATTERN_DB_DATE)
                        .parse(mNextOccurrence));
            } catch (ParseException e) {
                Log.e(LOGCAT, e.getMessage());
            }
        } else {
            txtNextOccurrence.setTag(Calendar.getInstance().getTime());
        }
        mCommonFunctions.formatExtendedDate(txtNextOccurrence);
        txtNextOccurrence.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime((Date) txtNextOccurrence.getTag());

                DatePickerDialog dialog = DatePickerDialog.newInstance(mDateSetListener,
                        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), false);
                dialog.setCloseOnSingleTapDay(true);
                dialog.show(getSupportFragmentManager(), DATEPICKER_TAG);
            }

            public DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

                @Override
//                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                    try {
                        Locale locale = getResources().getConfiguration().locale;
                        Date date = new SimpleDateFormat("yyyy-MM-dd", locale)
                                .parse(Integer.toString(year) + "-" + Integer.toString(monthOfYear + 1) +
                                        "-" + Integer.toString(dayOfMonth));
                        txtNextOccurrence.setTag(date);
                        mCommonFunctions.formatExtendedDate(txtNextOccurrence);
                    } catch (Exception e) {
                        Log.e(LOGCAT, e.getMessage());
                    }
                }
            };

        });

        // times repeated
        edtTimesRepeated = (EditText) findViewById(R.id.editTextTimesRepeated);
        if (mRecurringTransaction != null && mRecurringTransaction.numOccurrence >= 0) {
            edtTimesRepeated.setText(Integer.toString(mRecurringTransaction.numOccurrence));
        }
        // frequencies
        if (mFrequencies >= 200) {
            mFrequencies = mFrequencies - 200;
        } // set auto execute without user acknowledgement
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
        mCommonFunctions.onTransactionTypeChange();
        mCommonFunctions.refreshPayeeName();
        mCommonFunctions.refreshCategoryName();
        refreshTimesRepeated();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case EditTransactionCommonFunctions.REQUEST_PICK_PAYEE:
                if ((resultCode == Activity.RESULT_OK) && (data != null)) {
                    mCommonFunctions.payeeId = data.getIntExtra(PayeeActivity.INTENT_RESULT_PAYEEID, Constants.NOT_SET);
                    mCommonFunctions.payeeName = data.getStringExtra(PayeeActivity.INTENT_RESULT_PAYEENAME);
                    // select last category used from payee
                    if (!mCommonFunctions.chbSplitTransaction.isChecked()) {
                        if (mCommonFunctions.setCategoryFromPayee(mCommonFunctions.payeeId)) {
                            mCommonFunctions.refreshCategoryName(); // refresh UI
                        }
                    }
                    // refresh UI
                    mCommonFunctions.refreshPayeeName();
                }
                break;
            
            case REQUEST_PICK_CATEGORY:
                if ((resultCode == Activity.RESULT_OK) && (data != null)) {
                    mCommonFunctions.categoryId = data.getIntExtra(CategoryListActivity.INTENT_RESULT_CATEGID, Constants.NOT_SET);
                    mCommonFunctions.categoryName = data.getStringExtra(CategoryListActivity.INTENT_RESULT_CATEGNAME);
                    mCommonFunctions.subCategoryId = data.getIntExtra(CategoryListActivity.INTENT_RESULT_SUBCATEGID, Constants.NOT_SET);
                    mCommonFunctions.subCategoryName = data.getStringExtra(CategoryListActivity.INTENT_RESULT_SUBCATEGNAME);
                    // refresh UI category
                    mCommonFunctions.refreshCategoryName();
                }
                break;
            case REQUEST_PICK_SPLIT_TRANSACTION:
                if ((resultCode == Activity.RESULT_OK) && (data != null)) {
                    mCommonFunctions.mSplitTransactions = data.getParcelableArrayListExtra(SplitTransactionsActivity.INTENT_RESULT_SPLIT_TRANSACTION);
                    if (mCommonFunctions.mSplitTransactions != null && mCommonFunctions.mSplitTransactions.size() > 0) {
                        double totAmount = 0;
                        for (int i = 0; i < mCommonFunctions.mSplitTransactions.size(); i++) {
                            totAmount += mCommonFunctions.mSplitTransactions.get(i).getSplitTransAmount();
                        }
                        Core core = new Core(getBaseContext());
//                        formatAmount(txtAmountTo, totAmount, !Constants.TRANSACTION_TYPE_TRANSFER.equals(mTransCode) ? accountId : toAccountId);
                        int accountId = !mCommonFunctions.transactionType.equals(TransactionTypes.Transfer)
                                ? mCommonFunctions.accountId
                                : mCommonFunctions.toAccountId;
                        core.formatAmountTextView(mCommonFunctions.txtAmountTo, totAmount,
                                mCommonFunctions.getCurrencyIdFromAccountId(accountId));
                    }
                    // deleted item
                    if (data.getParcelableArrayListExtra(SplitTransactionsActivity.INTENT_RESULT_SPLIT_TRANSACTION_DELETED) != null) {
                        mCommonFunctions.mSplitTransactionsDeleted = data.getParcelableArrayListExtra(SplitTransactionsActivity.INTENT_RESULT_SPLIT_TRANSACTION_DELETED);
                    }
                }
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save the state interface
        outState.putInt(KEY_BILL_DEPOSITS_ID, mBillDepositsId);
        outState.putInt(KEY_ACCOUNT_ID, mCommonFunctions.accountId);
        outState.putInt(KEY_TO_ACCOUNT_ID, mCommonFunctions.toAccountId);
        outState.putString(KEY_TO_ACCOUNT_NAME, mToAccountName);
        outState.putString(KEY_TRANS_CODE, mCommonFunctions.getTransactionType());
        outState.putString(KEY_TRANS_STATUS, mStatus);
        outState.putDouble(KEY_TRANS_AMOUNTTO, (Double) mCommonFunctions.txtAmountTo.getTag());
        outState.putDouble(KEY_TRANS_AMOUNT, (Double) mCommonFunctions.txtAmount.getTag());
        outState.putInt(KEY_PAYEE_ID, mCommonFunctions.payeeId);
        outState.putString(KEY_PAYEE_NAME, mCommonFunctions.payeeName);
        outState.putInt(KEY_CATEGORY_ID, mCommonFunctions.categoryId);
        outState.putString(KEY_CATEGORY_NAME, mCommonFunctions.categoryName);
        outState.putInt(KEY_SUBCATEGORY_ID, mCommonFunctions.subCategoryId);
        outState.putString(KEY_SUBCATEGORY_NAME, mCommonFunctions.subCategoryName);
        outState.putString(KEY_TRANS_NUMBER, edtTransNumber.getText().toString());
        outState.putParcelableArrayList(KEY_SPLIT_TRANSACTION, mCommonFunctions.mSplitTransactions);
        outState.putParcelableArrayList(KEY_SPLIT_TRANSACTION_DELETED, mCommonFunctions.mSplitTransactionsDeleted);
        outState.putString(KEY_NOTES, String.valueOf(edtNotes.getTag()));
//        Locale locale = getResources().getConfiguration().locale;
        outState.putString(KEY_NEXT_OCCURRENCE, new SimpleDateFormat(Constants.PATTERN_DB_DATE)
                .format(txtNextOccurrence.getTag()));
        outState.putInt(KEY_REPEATS, mFrequencies);

        NumericHelper helper = new NumericHelper();
        int timesRepeated = helper.tryParse(edtTimesRepeated.getText().toString());
        if (timesRepeated != Constants.NOT_SET) {
//            outState.putInt(KEY_NUM_OCCURRENCE, timesRepeated);
            mRecurringTransaction.numOccurrence = timesRepeated;
        } else {
//            outState.putInt(KEY_NUM_OCCURRENCE, Constants.NOT_SET);
            mRecurringTransaction.numOccurrence = Constants.NOT_SET;
        }
        outState.putParcelable(KEY_MODEL, mRecurringTransaction);

        outState.putString(KEY_ACTION, mIntentAction);
    }

    @Override
    public void onFinishedInputAmountDialog(int id, Double amount) {
        mCommonFunctions.onFinishedInputAmountDialog(id, amount);
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
     * this method allows you to search the transaction data
     *
     * @param recurringTransactionId transaction id
     * @return true if data selected, false nothing
     */
    private boolean loadRecurringTransaction(int recurringTransactionId) {
        RecurringTransactionRepository repo = new RecurringTransactionRepository(this);
        TableBillsDeposits tx = repo.load(recurringTransactionId);
        if (tx == null) return false;

        // todo: just use a model object instead of a bunch of individual properties.
        mRecurringTransaction = tx;

        // Read data.
        mBillDepositsId = tx.id;
        mCommonFunctions.accountId = tx.accountId;
        mCommonFunctions.toAccountId = tx.toAccountId;
        String transCode = tx.transactionCode;
        mCommonFunctions.transactionType = TransactionTypes.valueOf(transCode);
        mStatus = tx.status;
        mCommonFunctions.mAmount = tx.amount;
        mCommonFunctions.mAmountTo = tx.totalAmount;
        mCommonFunctions.payeeId = tx.payeeId;
        mCommonFunctions.categoryId = tx.categoryId;
        mCommonFunctions.subCategoryId = tx.subCategoryId;
        mTransNumber = tx.transactionNumber;
        mNotes = tx.notes;
        mNextOccurrence = tx.nextOccurrence;
        mFrequencies = tx.repeats;
//        mNumOccurrence = tx.numOccurrence;

        // load split transactions only if no category selected.
        if (mCommonFunctions.categoryId == Constants.NOT_SET && mCommonFunctions.mSplitTransactions == null) {
            RecurringTransactionService recurringTransaction = new RecurringTransactionService(recurringTransactionId, this);
            mCommonFunctions.mSplitTransactions = recurringTransaction.loadSplitTransactions();
        }

        AccountRepository accountRepository = new AccountRepository(this);
        mToAccountName = accountRepository.loadName(mCommonFunctions.toAccountId);

        mCommonFunctions.selectPayeeName(mCommonFunctions.payeeId);
        selectSubcategoryName(mCommonFunctions.categoryId, mCommonFunctions.subCategoryId);

        return true;
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
     * Query info of Category and Subcategory
     *
     * @param categoryId Id of the category
     * @param subCategoryId Id of the sub-category
     * @return indicator whether the operation was successful.
     */
    private boolean selectSubcategoryName(int categoryId, int subCategoryId) {
        TableCategory category = new TableCategory();
        TableSubCategory subCategory = new TableSubCategory();
        Cursor cursor;
        // category
        cursor = getContentResolver().query(category.getUri(), category.getAllColumns(),
                TableCategory.CATEGID + "=?", new String[]{Integer.toString(categoryId)}, null);
        if ((cursor != null) && (cursor.moveToFirst())) {
            // set category name and sub category name
            mCommonFunctions.categoryName = cursor.getString(cursor.getColumnIndex(TableCategory.CATEGNAME));
            cursor.close();
        } else {
            mCommonFunctions.categoryName = null;
        }
        // sub-category
        cursor = getContentResolver().query(subCategory.getUri(), subCategory.getAllColumns(),
                TableSubCategory.SUBCATEGID + "=?", new String[]{Integer.toString(subCategoryId)}, null);
        if ((cursor != null) && (cursor.moveToFirst())) {
            // set category name and sub category name
            mCommonFunctions.subCategoryName = cursor.getString(cursor.getColumnIndex(TableSubCategory.SUBCATEGNAME));
            cursor.close();
        } else {
            mCommonFunctions.subCategoryName = null;
        }

        return true;
    }

    /**
     * validate data insert in activity
     *
     * @return validation result
     */
    private boolean validateData() {
        boolean isTransfer = mCommonFunctions.transactionType.equals(TransactionTypes.Transfer);

        if (isTransfer) {
            if (mCommonFunctions.toAccountId == Constants.NOT_SET) {
                Core.alertDialog(this, R.string.error_toaccount_not_selected);
                return false;
            }
            if (mCommonFunctions.toAccountId == mCommonFunctions.accountId) {
                Core.alertDialog(this, R.string.error_transfer_to_same_account);
                return false;
            }
        }

        // Category is required if tx is not a split or transfer.
        if (mCommonFunctions.categoryId == Constants.NOT_SET && (!mCommonFunctions.chbSplitTransaction.isChecked()) && !isTransfer) {
            Core.alertDialog(this, R.string.error_category_not_selected);
            return false;
        }
        if (mCommonFunctions.chbSplitTransaction.isChecked()
                && (mCommonFunctions.mSplitTransactions == null || mCommonFunctions.mSplitTransactions.size() <= 0)) {
            Core.alertDialog(this, R.string.error_split_transaction_empty);
            return false;
        }
        if (TextUtils.isEmpty(mCommonFunctions.txtAmountTo.getText())) {
            if (TextUtils.isEmpty(mCommonFunctions.txtAmount.getText())) {
                Core.alertDialog(this, R.string.error_totamount_empty);

                return false;
            } else {
                mCommonFunctions.txtAmountTo.setText(mCommonFunctions.txtAmount.getText());
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

        boolean isTransfer = mCommonFunctions.transactionType.equals(TransactionTypes.Transfer);

        ContentValues values = getContentValues(isTransfer);

        // Insert or update
        TableBillsDeposits recurringTransaction = new TableBillsDeposits();
        if (Constants.INTENT_ACTION_INSERT.equals(mIntentAction)) {
            // insert
            Uri insert = getContentResolver().insert(recurringTransaction.getUri(), values);
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
            if (getContentResolver().update(recurringTransaction.getUri(), values,
                    TableBillsDeposits.BDID + "=?", new String[]{Integer.toString(mBillDepositsId)}) <= 0) {
                Core.alertDialog(this, R.string.db_checking_update_failed);
                Log.w(LOGCAT, "Update repeating  transaction failed!");
                return false;
            }
        }
        // has split transaction
        boolean hasSplitTransaction = mCommonFunctions.mSplitTransactions != null && mCommonFunctions.mSplitTransactions.size() > 0;
        if (hasSplitTransaction) {
            for (int i = 0; i < mCommonFunctions.mSplitTransactions.size(); i++) {
                values.clear();
                values.put(TableBudgetSplitTransactions.CATEGID, mCommonFunctions.mSplitTransactions.get(i).getCategId());
                values.put(TableBudgetSplitTransactions.SUBCATEGID, mCommonFunctions.mSplitTransactions.get(i).getSubCategId());
                values.put(TableBudgetSplitTransactions.SPLITTRANSAMOUNT, mCommonFunctions.mSplitTransactions.get(i).getSplitTransAmount());
                values.put(TableBudgetSplitTransactions.TRANSID, mBillDepositsId);

                if (mCommonFunctions.mSplitTransactions.get(i).getSplitTransId() == Constants.NOT_SET) {
                    // insert data
                    Uri insert = getContentResolver().insert(mCommonFunctions.mSplitTransactions.get(i).getUri(), values);
                    if (insert == null) {
                        Toast.makeText(getApplicationContext(), R.string.db_checking_insert_failed, Toast.LENGTH_SHORT).show();
                        Log.w(LOGCAT, "Insert new split transaction failed!");
                        return false;
                    }
                } else {
                    // update data
                    if (getContentResolver().update(mCommonFunctions.mSplitTransactions.get(i).getUri(), values,
                            TableSplitTransactions.SPLITTRANSID + "=?",
                            new String[]{Integer.toString(mCommonFunctions.mSplitTransactions.get(i).getSplitTransId())}) <= 0) {
                        Toast.makeText(getApplicationContext(), R.string.db_checking_update_failed, Toast.LENGTH_SHORT).show();
                        Log.w(LOGCAT, "Update split transaction failed!");
                        return false;
                    }
                }
            }
        }

        // deleted old split transaction
        if (mCommonFunctions.mSplitTransactionsDeleted != null && mCommonFunctions.mSplitTransactionsDeleted.size() > 0) {
            for (int i = 0; i < mCommonFunctions.mSplitTransactionsDeleted.size(); i++) {
                values.clear();
                //put value
                values.put(TableSplitTransactions.SPLITTRANSAMOUNT, mCommonFunctions.mSplitTransactionsDeleted.get(i).getSplitTransAmount());

                // update data
                if (getContentResolver().delete(mCommonFunctions.mSplitTransactionsDeleted.get(i).getUri(),
                        TableSplitTransactions.SPLITTRANSID + "=?",
                        new String[]{Integer.toString(mCommonFunctions.mSplitTransactionsDeleted.get(i).getSplitTransId())}) <= 0) {
                    Toast.makeText(getApplicationContext(), R.string.db_checking_update_failed, Toast.LENGTH_SHORT).show();
                    Log.w(LOGCAT, "Delete split transaction failed!");
                    return false;
                }
            }
        }
        // update category and subcategory payee
        if ((!(isTransfer)) && (mCommonFunctions.payeeId > 0) && (!hasSplitTransaction)) {
            // clear content value for update categoryId, subCategoryId
            values.clear();
            // set categoryId and subCategoryId
            values.put(TablePayee.CATEGID, mCommonFunctions.categoryId);
            values.put(TablePayee.SUBCATEGID, mCommonFunctions.subCategoryId);
            // create instance TablePayee for update
            TablePayee payee = new TablePayee();
            // update data
            if (getContentResolver().update(payee.getUri(),
                    values,
                    TablePayee.PAYEEID + "=" + Integer.toString(mCommonFunctions.payeeId),
                    null) <= 0) {
                Toast.makeText(getApplicationContext(), R.string.db_payee_update_failed, Toast.LENGTH_SHORT).show();
                Log.w(LOGCAT, "Update Payee with Id=" + Integer.toString(mCommonFunctions.payeeId) + " return <= 0");
            }
        }

        return true;
    }

    private ContentValues getContentValues(boolean isTransfer) {
        ContentValues values = new ContentValues();

        // Accounts & Payee
        values.put(TableBillsDeposits.ACCOUNTID, mCommonFunctions.accountId);
        values.put(TableBillsDeposits.TOACCOUNTID, mCommonFunctions.toAccountId);
        if (isTransfer) {
            values.put(TableBillsDeposits.PAYEEID, Constants.NOT_SET);
        } else {
            values.put(TableBillsDeposits.PAYEEID, mCommonFunctions.payeeId);
        }

        // Transaction Type
        values.put(TableBillsDeposits.TRANSCODE, mCommonFunctions.getTransactionType());

        // Amount
        values.put(TableBillsDeposits.TRANSAMOUNT, (Double) mCommonFunctions.txtAmount.getTag());

        // Amount To
        values.put(TableBillsDeposits.TOTRANSAMOUNT, (Double) mCommonFunctions.txtAmountTo.getTag());

        values.put(TableBillsDeposits.STATUS, mStatus);
        values.put(TableBillsDeposits.CATEGID, !mCommonFunctions.chbSplitTransaction.isChecked()
                ? mCommonFunctions.categoryId : Constants.NOT_SET);
        values.put(TableBillsDeposits.SUBCATEGID, !mCommonFunctions.chbSplitTransaction.isChecked()
                ? mCommonFunctions.subCategoryId : Constants.NOT_SET);
        values.put(TableBillsDeposits.FOLLOWUPID, Constants.NOT_SET);
        values.put(TableBillsDeposits.TRANSACTIONNUMBER, edtTransNumber.getText().toString());
        values.put(TableBillsDeposits.NOTES, edtNotes.getText().toString());
        values.put(TableBillsDeposits.NEXTOCCURRENCEDATE, new SimpleDateFormat(Constants.PATTERN_DB_DATE)
                .format(txtNextOccurrence.getTag()));
        values.put(TableBillsDeposits.TRANSDATE, new SimpleDateFormat(Constants.PATTERN_DB_DATE)
                .format(txtNextOccurrence.getTag()));
        values.put(TableBillsDeposits.REPEATS, mFrequencies);
        values.put(TableBillsDeposits.NUMOCCURRENCES, mFrequencies > 0
                ? edtTimesRepeated.getText().toString() : null);

        return values;
    }

    private void restoreInstanceState(Bundle savedInstanceState) {
        mRecurringTransaction = savedInstanceState.getParcelable(KEY_MODEL);

        mBillDepositsId = savedInstanceState.getInt(KEY_BILL_DEPOSITS_ID);
        mCommonFunctions.accountId = savedInstanceState.getInt(KEY_ACCOUNT_ID);
        mCommonFunctions.toAccountId = savedInstanceState.getInt(KEY_TO_ACCOUNT_ID);
        mToAccountName = savedInstanceState.getString(KEY_TO_ACCOUNT_NAME);
        String transCode = savedInstanceState.getString(KEY_TRANS_CODE);
        mCommonFunctions.transactionType = TransactionTypes.valueOf(transCode);
        mStatus = savedInstanceState.getString(KEY_TRANS_STATUS);
        mCommonFunctions.mAmount = savedInstanceState.getDouble(KEY_TRANS_AMOUNT);
        mCommonFunctions.mAmountTo = savedInstanceState.getDouble(KEY_TRANS_AMOUNTTO);
        mCommonFunctions.payeeId = savedInstanceState.getInt(KEY_PAYEE_ID);
        mCommonFunctions.payeeName = savedInstanceState.getString(KEY_PAYEE_NAME);
        mCommonFunctions.categoryId = savedInstanceState.getInt(KEY_CATEGORY_ID);
        mCommonFunctions.categoryName = savedInstanceState.getString(KEY_CATEGORY_NAME);
        mCommonFunctions.subCategoryId = savedInstanceState.getInt(KEY_SUBCATEGORY_ID);
        mCommonFunctions.subCategoryName = savedInstanceState.getString(KEY_SUBCATEGORY_NAME);
        mNotes = savedInstanceState.getString(KEY_NOTES);
        mTransNumber = savedInstanceState.getString(KEY_TRANS_NUMBER);
        mCommonFunctions.mSplitTransactions = savedInstanceState.getParcelableArrayList(KEY_SPLIT_TRANSACTION);
        mCommonFunctions.mSplitTransactionsDeleted = savedInstanceState.getParcelableArrayList(KEY_SPLIT_TRANSACTION_DELETED);
        mNextOccurrence = savedInstanceState.getString(KEY_NEXT_OCCURRENCE);
        mFrequencies = savedInstanceState.getInt(KEY_REPEATS);
//        mNumOccurrence = savedInstanceState.getInt(KEY_NUM_OCCURRENCE);

        // action
        mIntentAction = savedInstanceState.getString(KEY_ACTION);
    }
}

