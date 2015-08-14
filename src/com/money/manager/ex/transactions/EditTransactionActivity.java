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
package com.money.manager.ex.transactions;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDiskIOException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
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
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.money.manager.ex.account.AccountListActivity;
import com.money.manager.ex.common.CategoryListActivity;
import com.money.manager.ex.Constants;
import com.money.manager.ex.PayeeActivity;
import com.money.manager.ex.R;
import com.money.manager.ex.SplitTransactionsActivity;
import com.money.manager.ex.businessobjects.CategoryService;
import com.money.manager.ex.businessobjects.PayeeService;
import com.money.manager.ex.businessobjects.RecurringTransactionService;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.database.AccountRepository;
import com.money.manager.ex.database.ISplitTransactionsDataset;
import com.money.manager.ex.database.MoneyManagerOpenHelper;
import com.money.manager.ex.database.RecurringTransactionRepository;
import com.money.manager.ex.database.SplitCategoriesRepository;
import com.money.manager.ex.database.TableBillsDeposits;
import com.money.manager.ex.database.TableBudgetSplitTransactions;
import com.money.manager.ex.database.TableCategory;
import com.money.manager.ex.database.TableCheckingAccount;
import com.money.manager.ex.database.TablePayee;
import com.money.manager.ex.database.TableSplitTransactions;
import com.money.manager.ex.database.TableSubCategory;
import com.money.manager.ex.dropbox.DropboxHelper;
import com.money.manager.ex.common.BaseFragmentActivity;
import com.money.manager.ex.common.IInputAmountDialogListener;
import com.money.manager.ex.common.InputAmountDialog;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.settings.PreferenceConstants;
import com.money.manager.ex.utils.DateUtils;
import com.fourmob.datetimepicker.date.DatePickerDialog;

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
public class EditTransactionActivity
    extends BaseFragmentActivity
    implements IInputAmountDialogListener, YesNoDialogListener {

    public static final String DATEPICKER_TAG = "datepicker";

    // action type intent
    public String mIntentAction;
    public String mToAccountName;
    public int mTransId = Constants.NOT_SET;

    public String mStatus = null;

    // arrays to manage transcode and status
    public String[] mStatusItems, mStatusValues;
    // amount
    public double mTotAmount = 0, mAmount = 0;
    // notes
    public String mNotes = "";
    // transaction numbers
    public String mTransNumber = "";
    // bill deposits
    public int mRecurringTransactionId = Constants.NOT_SET;
    public String mNextOccurrence = null;
    // datepicker value
    public String mDate = "";

    // Controls on the form.
    public ImageButton btnTransNumber;
    public EditText edtTransNumber, edtNotes;
    public TextView txtSelectDate;

    private TableCheckingAccount mCheckingAccount = new TableCheckingAccount();
    private EditTransactionCommonFunctions mCommonFunctions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_account_transaction);

        mCommonFunctions = new EditTransactionCommonFunctions(this);

        try {
            DropboxHelper.getInstance();
        } catch (Exception e) {
            Log.e(EditTransactionActivityConstants.LOGCAT, e.getMessage());
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

        mCommonFunctions.initTransactionTypeSelector();

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
                ExceptionHandler handler = new ExceptionHandler(this, this);
                handler.handle(e, "parsing the date");
            }
        } else {
            txtSelectDate.setTag(Calendar.getInstance().getTime());
        }
        mCommonFunctions.formatExtendedDate(txtSelectDate);

        txtSelectDate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime((Date) txtSelectDate.getTag());
                DatePickerDialog dialog = DatePickerDialog.newInstance(mDateSetListener,
                    calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), false);
                dialog.setCloseOnSingleTapDay(true);
                dialog.show(getSupportFragmentManager(), DATEPICKER_TAG);
            }

            public DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                    try {
                        Date date = new SimpleDateFormat("yyyy-MM-dd", getResources().getConfiguration().locale)
                                .parse(Integer.toString(year) + "-" + Integer.toString(monthOfYear + 1) + "-" + Integer.toString(dayOfMonth));
                        txtSelectDate.setTag(date);
                        mCommonFunctions.formatExtendedDate(txtSelectDate);
                    } catch (Exception e) {
                        ExceptionHandler handler = new ExceptionHandler(EditTransactionActivity.this, this);
                        handler.handle(e, "setting the date");
                    }
                }
            };
        });

        // Payee

        mCommonFunctions.initPayeeControls();
//        mCommonFunctions.txtSelectPayee.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(getApplicationContext(), PayeeActivity.class);
//                intent.setAction(Intent.ACTION_PICK);
//                startActivityForResult(intent, EditTransactionActivityConstants.REQUEST_PICK_PAYEE);
//            }
//        });

        // Category

        mCommonFunctions.txtSelectCategory.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mCommonFunctions.chbSplitTransaction.isChecked()) {
                    // select single category.
                    Intent intent = new Intent(EditTransactionActivity.this, CategoryListActivity.class);
                    intent.setAction(Intent.ACTION_PICK);
                    startActivityForResult(intent, EditTransactionActivityConstants.REQUEST_PICK_CATEGORY);
                } else {
                    // select split categories.
                    Intent intent = new Intent(EditTransactionActivity.this, SplitTransactionsActivity.class);
                    intent.putExtra(SplitTransactionsActivity.KEY_DATASET_TYPE, TableSplitTransactions.class.getSimpleName());
                    intent.putExtra(SplitTransactionsActivity.KEY_TRANSACTION_TYPE, mCommonFunctions.transactionType.getCode());
                    intent.putParcelableArrayListExtra(SplitTransactionsActivity.KEY_SPLIT_TRANSACTION, mCommonFunctions.mSplitTransactions);
                    intent.putParcelableArrayListExtra(SplitTransactionsActivity.KEY_SPLIT_TRANSACTION_DELETED, mCommonFunctions.mSplitTransactionsDeleted);
                    startActivityForResult(intent, EditTransactionActivityConstants.REQUEST_PICK_SPLIT_TRANSACTION);
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

        OnClickListener onClickAmount = new OnClickListener() {

            @Override
            public void onClick(View v) {
                Integer currencyId = null;
                if (mCommonFunctions.txtTotAmount.equals(v)) {
                    if (mCommonFunctions.spinAccount.getSelectedItemPosition() >= 0 &&
                            mCommonFunctions.spinAccount.getSelectedItemPosition() < mCommonFunctions.AccountList.size()) {
                        if (mCommonFunctions.transactionType.equals(TransactionTypes.Transfer)) {
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
                InputAmountDialog dialog = InputAmountDialog.getInstance(EditTransactionActivity.this,
                        v.getId(), amount, currencyId);
                dialog.show(getSupportFragmentManager(), dialog.getClass().getSimpleName());
            }
        };

        // amount
        mCommonFunctions.formatAmount(mCommonFunctions.txtAmount, mAmount,
                !mCommonFunctions.transactionType.equals(TransactionTypes.Transfer)
                        ? mCommonFunctions.toAccountId : mCommonFunctions.accountId);
        mCommonFunctions.txtAmount.setOnClickListener(onClickAmount);

        // total amount

        mCommonFunctions.formatAmount(mCommonFunctions.txtTotAmount, mTotAmount,
                !mCommonFunctions.transactionType.equals(TransactionTypes.Transfer)
                        ? mCommonFunctions.accountId : mCommonFunctions.toAccountId);

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
                        new String[]{Integer.toString(mCommonFunctions.accountId)});
                if (cursor == null) return;

                if (cursor.moveToFirst()) {
                    String transNumber = cursor.getString(0);
                    if (TextUtils.isEmpty(transNumber)) {
                        transNumber = "0";
                    }
                    if ((!TextUtils.isEmpty(transNumber)) && TextUtils.isDigitsOnly(transNumber)) {
                        try {
                            edtTransNumber.setText(Long.toString(Long.parseLong(transNumber) + 1));
                        } catch (Exception e) {
                            Log.e(EditTransactionActivityConstants.LOGCAT, e.getMessage());
                        }
                    }
                }
                cursor.close();
            }
        });

        // notes

        edtNotes = (EditText) findViewById(R.id.editTextNotes);
        if (!(TextUtils.isEmpty(mNotes))) {
            edtNotes.setText(mNotes);
        }

        // refresh user interface
        mCommonFunctions.refreshAfterTransactionCodeChange();
        mCommonFunctions.refreshPayeeName();
        mCommonFunctions.refreshCategoryName();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case EditTransactionActivityConstants.REQUEST_PICK_PAYEE:
                if ((resultCode != Activity.RESULT_OK) || (data == null)) return;

                mCommonFunctions.payeeId = data.getIntExtra(PayeeActivity.INTENT_RESULT_PAYEEID, -1);
                mCommonFunctions.payeeName = data.getStringExtra(PayeeActivity.INTENT_RESULT_PAYEENAME);
                // select last category used from payee. Only if category has not been entered earlier.
                if (!mCommonFunctions.chbSplitTransaction.isChecked() && mCommonFunctions.categoryId == Constants.NOT_SET) {
                    if (mCommonFunctions.setCategoryFromPayee(mCommonFunctions.payeeId)) {
                        mCommonFunctions.refreshCategoryName(); // refresh UI
                    }
                }
                // refresh UI
                mCommonFunctions.refreshPayeeName();
                break;

            case EditTransactionActivityConstants.REQUEST_PICK_ACCOUNT:
                if ((resultCode != Activity.RESULT_OK) || (data == null)) return;

                mCommonFunctions.toAccountId = data.getIntExtra(AccountListActivity.INTENT_RESULT_ACCOUNTID, -1);
                mToAccountName = data.getStringExtra(AccountListActivity.INTENT_RESULT_ACCOUNTNAME);
                break;

            case EditTransactionActivityConstants.REQUEST_PICK_CATEGORY:
                if ((resultCode != Activity.RESULT_OK) || (data == null)) return;

                mCommonFunctions.categoryId = data.getIntExtra(CategoryListActivity.INTENT_RESULT_CATEGID, -1);
                mCommonFunctions.categoryName = data.getStringExtra(CategoryListActivity.INTENT_RESULT_CATEGNAME);
                mCommonFunctions.subCategoryId = data.getIntExtra(CategoryListActivity.INTENT_RESULT_SUBCATEGID, -1);
                mCommonFunctions.subCategoryName = data.getStringExtra(CategoryListActivity.INTENT_RESULT_SUBCATEGNAME);
                // refresh UI category
                mCommonFunctions.refreshCategoryName();
                break;

            case EditTransactionActivityConstants.REQUEST_PICK_SPLIT_TRANSACTION:
                if ((resultCode == Activity.RESULT_OK) && (data != null)) {
                    mCommonFunctions.mSplitTransactions = data.getParcelableArrayListExtra(SplitTransactionsActivity.INTENT_RESULT_SPLIT_TRANSACTION);
                    if (mCommonFunctions.mSplitTransactions != null && mCommonFunctions.mSplitTransactions.size() > 0) {
                        double totAmount = 0;
                        for (int i = 0; i < mCommonFunctions.mSplitTransactions.size(); i++) {
                            totAmount += mCommonFunctions.mSplitTransactions.get(i).getSplitTransAmount();
                        }
                        mCommonFunctions.formatAmount(mCommonFunctions.txtTotAmount, totAmount,
                                !mCommonFunctions.transactionType.equals(TransactionTypes.Transfer)
                                        ? mCommonFunctions.accountId
                                        : mCommonFunctions.toAccountId);
                    }
                    // deleted item
                    if (data.getParcelableArrayListExtra(SplitTransactionsActivity.INTENT_RESULT_SPLIT_TRANSACTION_DELETED) != null) {
                        mCommonFunctions.mSplitTransactionsDeleted = data.getParcelableArrayListExtra(SplitTransactionsActivity.INTENT_RESULT_SPLIT_TRANSACTION_DELETED);
                    }
                }
                break;
        }
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

        mCommonFunctions.transactionType = TransactionTypes.Transfer;

        mCommonFunctions.refreshAfterTransactionCodeChange();
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
        outState.putInt(EditTransactionActivityConstants.KEY_TRANS_ID, mTransId);
        outState.putInt(EditTransactionActivityConstants.KEY_ACCOUNT_ID, mCommonFunctions.accountId);
        outState.putInt(EditTransactionActivityConstants.KEY_TO_ACCOUNT_ID, mCommonFunctions.toAccountId);
        outState.putString(EditTransactionActivityConstants.KEY_TO_ACCOUNT_NAME, mToAccountName);
        outState.putString(EditTransactionActivityConstants.KEY_TRANS_DATE,
                new SimpleDateFormat(Constants.PATTERN_DB_DATE).format(txtSelectDate.getTag()));
        outState.putString(EditTransactionActivityConstants.KEY_TRANS_CODE, mCommonFunctions.getTransactionType());
        outState.putString(EditTransactionActivityConstants.KEY_TRANS_STATUS, mStatus);
        outState.putDouble(EditTransactionActivityConstants.KEY_TRANS_TOTAMOUNT, (Double) mCommonFunctions.txtTotAmount.getTag());
        outState.putDouble(EditTransactionActivityConstants.KEY_TRANS_AMOUNT, (Double) mCommonFunctions.txtAmount.getTag());
        outState.putInt(EditTransactionActivityConstants.KEY_PAYEE_ID, mCommonFunctions.payeeId);
        outState.putString(EditTransactionActivityConstants.KEY_PAYEE_NAME, mCommonFunctions.payeeName);
        outState.putInt(EditTransactionActivityConstants.KEY_CATEGORY_ID, mCommonFunctions.categoryId);
        outState.putString(EditTransactionActivityConstants.KEY_CATEGORY_NAME, mCommonFunctions.categoryName);
        outState.putInt(EditTransactionActivityConstants.KEY_SUBCATEGORY_ID, mCommonFunctions.subCategoryId);
        outState.putString(EditTransactionActivityConstants.KEY_SUBCATEGORY_NAME, mCommonFunctions.subCategoryName);
        outState.putString(EditTransactionActivityConstants.KEY_TRANS_NUMBER, edtTransNumber.getText().toString());
        outState.putParcelableArrayList(EditTransactionActivityConstants.KEY_SPLIT_TRANSACTION, mCommonFunctions.mSplitTransactions);
        outState.putParcelableArrayList(EditTransactionActivityConstants.KEY_SPLIT_TRANSACTION_DELETED, mCommonFunctions.mSplitTransactionsDeleted);
        outState.putString(EditTransactionActivityConstants.KEY_NOTES, edtNotes.getText().toString());
        // bill deposits
        outState.putInt(EditTransactionActivityConstants.KEY_BDID_ID, mRecurringTransactionId);
        outState.putString(EditTransactionActivityConstants.KEY_NEXT_OCCURRENCE, mNextOccurrence);

        outState.putString(EditTransactionActivityConstants.KEY_ACTION, mIntentAction);
    }

    @Override
    public void onFinishedInputAmountDialog(int id, Double amount) {
        View view = findViewById(id);
        int accountId;
        if (view != null && view instanceof TextView) {
            CurrencyService currencyService = new CurrencyService(getApplicationContext());
            if (mCommonFunctions.transactionType.equals(TransactionTypes.Transfer)) {
                Double originalAmount;
                try {
                    /*Integer toCurrencyId = mAccountList.get(mAccountIdList.indexOf(accountId)).getCurrencyId();
                    Integer fromCurrencyId = mAccountList.get(mAccountIdList.indexOf(toAccountId)).getCurrencyId();*/
                    Integer toCurrencyId = mCommonFunctions.AccountList.get(mCommonFunctions.mAccountIdList
                            .indexOf(view.getId() == R.id.textViewTotAmount
                                    ? mCommonFunctions.accountId
                                    : mCommonFunctions.toAccountId)).getCurrencyId();
                    Integer fromCurrencyId = mCommonFunctions.AccountList.get(mCommonFunctions.mAccountIdList
                            .indexOf(view.getId() == R.id.textViewTotAmount
                                    ? mCommonFunctions.toAccountId :
                                    mCommonFunctions.accountId)).getCurrencyId();
                    // take a original values
                    originalAmount = view.getId() == R.id.textViewTotAmount
                            ? (Double) mCommonFunctions.txtTotAmount.getTag()
                            : (Double) mCommonFunctions.txtAmount.getTag();
                    // convert value
                    Double amountExchange = currencyService.doCurrencyExchange(toCurrencyId, originalAmount, fromCurrencyId);
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
                            amountExchange = currencyService.doCurrencyExchange(toCurrencyId, amount, fromCurrencyId);
                            mCommonFunctions.formatAmount(view.getId() == R.id.textViewTotAmount
                                    ? mCommonFunctions.txtAmount : mCommonFunctions.txtTotAmount,
                                    amountExchange,
                                    view.getId() == R.id.textViewTotAmount
                                            ? mCommonFunctions.accountId
                                            : mCommonFunctions.toAccountId);
                        }
                    }

                } catch (Exception e) {
                    Log.e(EditTransactionActivityConstants.LOGCAT, e.getMessage());
                }
            }
            if (mCommonFunctions.txtTotAmount.equals(view)) {
                if (mCommonFunctions.transactionType.equals(TransactionTypes.Transfer)) {
                    accountId = mCommonFunctions.toAccountId;
                } else {
                    accountId = mCommonFunctions.accountId;
                }
            } else {
                accountId = mCommonFunctions.accountId;
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

    private boolean createSplitCategoriesFromRecurringTransaction() {
        // check if category and sub-category are not set.
        if(!(mCommonFunctions.categoryId <= 0 && mCommonFunctions.subCategoryId <= 0)) return false;

        // Adding transactions to the split list will set the Split checkbox and the category name.

        // create split transactions
        RecurringTransactionService recurringTransaction = new RecurringTransactionService(mRecurringTransactionId, this);
        ArrayList<ISplitTransactionsDataset> splitTemplates = recurringTransaction.loadSplitTransactions();
        if(mCommonFunctions.mSplitTransactions == null) mCommonFunctions.mSplitTransactions = new ArrayList<>();

        // For each of the templates, create a new record.
        for(int i = 0; i <= splitTemplates.size() - 1; i++) {
            TableBudgetSplitTransactions record = (TableBudgetSplitTransactions) splitTemplates.get(i);

            TableSplitTransactions newSplit = new TableSplitTransactions();
            newSplit.setSplitTransAmount(record.getSplitTransAmount());
            newSplit.setCategId(record.getCategId());
            newSplit.setSubCategId(record.getSubCategId());

            mCommonFunctions.mSplitTransactions.add(newSplit);
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
        int originalPosition = adapterTrans.getPosition(mCommonFunctions.getTransactionType());
        mCommonFunctions.spinTransCode.setSelection(originalPosition);
    }

    /**
     * Loads info for Category and Subcategory
     *
     * @param categoryId Id of the category to load.
     * @param subCategoryId Id of the subcategory to load.
     * @return A boolean indicating whether the operation was successful.
     */
    public boolean loadCategorySubName(int categoryId, int subCategoryId) {
        try {
            return loadCategorySubNameInternal(categoryId, subCategoryId);
        } catch (IllegalStateException ex) {
            ExceptionHandler handler = new ExceptionHandler(this, this);
            handler.handle(ex, "loading category & subcategory names");
        }
        return false;
    }

    private boolean loadCategorySubNameInternal(int categoryId, int subCategoryId) {
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
            mCommonFunctions.categoryName = cursor.getString(cursor.getColumnIndex(TableCategory.CATEGNAME));
        } else {
            mCommonFunctions.categoryName = null;
        }
        if (cursor != null) {
            cursor.close();
        }

        // sub-category

        cursor = getContentResolver().query(subCategory.getUri(), subCategory.getAllColumns(),
                TableSubCategory.SUBCATEGID + "=?", new String[]{Integer.toString(subCategoryId)}, null);
        if ((cursor != null) && (cursor.moveToFirst())) {
            // set category name and sub category name
            mCommonFunctions.subCategoryName = cursor.getString(cursor.getColumnIndex(TableSubCategory.SUBCATEGNAME));
        } else {
            mCommonFunctions.subCategoryName = null;
        }
        if (cursor != null) {
            cursor.close();
        }

        return true;
    }

    public boolean loadCheckingAccount(int transId, boolean duplicate) {
        try {
            return loadCheckingAccountInternal(transId, duplicate);
        } catch (SQLiteDiskIOException ex) {
            ExceptionHandler handler = new ExceptionHandler(this, this);
            handler.handle(ex, "loading checking account");
        }
        return false;
    }

    /**
     * getApplicationContext() method allows you to search the transaction data
     *
     * @param transId transaction id
     * @return true if data selected, false nothing
     */
    private boolean loadCheckingAccountInternal(int transId, boolean duplicate) {
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
        mCommonFunctions.accountId = cursor.getInt(cursor.getColumnIndex(TableCheckingAccount.ACCOUNTID));
        mCommonFunctions.toAccountId = cursor.getInt(cursor.getColumnIndex(TableCheckingAccount.TOACCOUNTID));
        String transCode = cursor.getString(cursor.getColumnIndex(TableCheckingAccount.TRANSCODE));
        mCommonFunctions.transactionType = TransactionTypes.valueOf(transCode);
        mStatus = cursor.getString(cursor.getColumnIndex(TableCheckingAccount.STATUS));
        mAmount = cursor.getDouble(cursor.getColumnIndex(TableCheckingAccount.TRANSAMOUNT));
        mTotAmount = cursor.getDouble(cursor.getColumnIndex(TableCheckingAccount.TOTRANSAMOUNT));
        mCommonFunctions.payeeId = cursor.getInt(cursor.getColumnIndex(TableCheckingAccount.PAYEEID));
        mCommonFunctions.categoryId = cursor.getInt(cursor.getColumnIndex(TableCheckingAccount.CATEGID));
        mCommonFunctions.subCategoryId = cursor.getInt(cursor.getColumnIndex(TableCheckingAccount.SUBCATEGID));
        mTransNumber = cursor.getString(cursor.getColumnIndex(TableCheckingAccount.TRANSACTIONNUMBER));
        mNotes = cursor.getString(cursor.getColumnIndex(TableCheckingAccount.NOTES));
        if (!duplicate) {
            mDate = cursor.getString(cursor.getColumnIndex(TableCheckingAccount.TRANSDATE));
        }

        cursor.close();

        // Load Split Categories.
        if (mCommonFunctions.mSplitTransactions == null) {
            SplitCategoriesRepository splitRepo = new SplitCategoriesRepository(this);
            mCommonFunctions.mSplitTransactions = splitRepo.loadSplitCategoriesFor(transId);

            if (duplicate && (mCommonFunctions.mSplitTransactions != null)) {
                // Reset ids so that the transactions get inserted on save.
                for (ISplitTransactionsDataset split : mCommonFunctions.mSplitTransactions) {
                    split.setSplitTransId(Constants.NOT_SET);
                }
            }

        }

        // convert status in uppercase string
        if (!TextUtils.isEmpty(mStatus)) mStatus = mStatus.toUpperCase();

        AccountRepository accountRepository = new AccountRepository(this);
        mToAccountName = accountRepository.loadName(mCommonFunctions.toAccountId);

        mCommonFunctions.selectPayeeName(mCommonFunctions.payeeId);
        loadCategorySubName(mCommonFunctions.categoryId, mCommonFunctions.subCategoryId);

        return true;
    }

    /**
     * Loads a recurring transaction data when entering a recurring transaction.
     * @param recurringTransactionId Id of the recurring transaction.
     * @return A boolean indicating whether the operation was successful.
     */
    public boolean loadRecurringTransaction(int recurringTransactionId) {
        RecurringTransactionRepository repo = new RecurringTransactionRepository(this);
        TableBillsDeposits tx = repo.load(recurringTransactionId);
        if (tx == null) return false;

        // take a data
        mCommonFunctions.accountId = tx.accountId;
        mCommonFunctions.toAccountId = tx.toAccountId;
        String transCode = tx.transactionCode;
        mCommonFunctions.transactionType = TransactionTypes.valueOf(transCode);
        mStatus = tx.status;
        mAmount = tx.amount;
        mTotAmount = tx.totalAmount;
        mCommonFunctions.payeeId = tx.payeeId;
        mCommonFunctions.categoryId = tx.categoryId;
        mCommonFunctions.subCategoryId = tx.subCategoryId;
        mTransNumber = tx.transactionNumber;
        mNotes = tx.notes;
        mDate = tx.nextOccurrence;
        mStatus = tx.status;

        AccountRepository accountRepository = new AccountRepository(this);
        mToAccountName = accountRepository.loadName(mCommonFunctions.toAccountId);

        mCommonFunctions.selectPayeeName(mCommonFunctions.payeeId);
        loadCategorySubName(mCommonFunctions.categoryId, mCommonFunctions.subCategoryId);

        // handle splits
        createSplitCategoriesFromRecurringTransaction();

        return true;
    }

    /**
     * After the user accepts, remove any split categories.
     */
    private void removeAllSplitCategories() {
        if(mCommonFunctions.mSplitTransactions == null) return;

        for(int i = 0; i < mCommonFunctions.mSplitTransactions.size(); i++) {
            ISplitTransactionsDataset split = mCommonFunctions.mSplitTransactions.get(i);
            int id = split.getSplitTransId();
            ArrayList<ISplitTransactionsDataset> deletedSplits = getDeletedSplitCategories();

            if(id == -1) {
                // Remove any newly created splits.
                // transaction id == -1
                mCommonFunctions.mSplitTransactions.remove(i);
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
        mTransId = savedInstanceState.getInt(EditTransactionActivityConstants.KEY_TRANS_ID);
        mCommonFunctions.accountId = savedInstanceState.getInt(EditTransactionActivityConstants.KEY_ACCOUNT_ID);
        mCommonFunctions.toAccountId = savedInstanceState.getInt(EditTransactionActivityConstants.KEY_TO_ACCOUNT_ID);
        mToAccountName = savedInstanceState.getString(EditTransactionActivityConstants.KEY_TO_ACCOUNT_NAME);
        mDate = savedInstanceState.getString(EditTransactionActivityConstants.KEY_TRANS_DATE);
        String transCode = savedInstanceState.getString(EditTransactionActivityConstants.KEY_TRANS_CODE);
        mCommonFunctions.transactionType = TransactionTypes.valueOf(transCode);
        mStatus = savedInstanceState.getString(EditTransactionActivityConstants.KEY_TRANS_STATUS);
        mAmount = savedInstanceState.getDouble(EditTransactionActivityConstants.KEY_TRANS_AMOUNT);
        mTotAmount = savedInstanceState.getDouble(EditTransactionActivityConstants.KEY_TRANS_TOTAMOUNT);
        mCommonFunctions.payeeId = savedInstanceState.getInt(EditTransactionActivityConstants.KEY_PAYEE_ID);
        mCommonFunctions.payeeName = savedInstanceState.getString(EditTransactionActivityConstants.KEY_PAYEE_NAME);
        mCommonFunctions.categoryId = savedInstanceState.getInt(EditTransactionActivityConstants.KEY_CATEGORY_ID);
        mCommonFunctions.categoryName = savedInstanceState.getString(EditTransactionActivityConstants.KEY_CATEGORY_NAME);
        mCommonFunctions.subCategoryId = savedInstanceState.getInt(EditTransactionActivityConstants.KEY_SUBCATEGORY_ID);
        mCommonFunctions.subCategoryName = savedInstanceState.getString(EditTransactionActivityConstants.KEY_SUBCATEGORY_NAME);
        mNotes = savedInstanceState.getString(EditTransactionActivityConstants.KEY_NOTES);
        mTransNumber = savedInstanceState.getString(EditTransactionActivityConstants.KEY_TRANS_NUMBER);
        mCommonFunctions.mSplitTransactions = savedInstanceState.getParcelableArrayList(EditTransactionActivityConstants.KEY_SPLIT_TRANSACTION);
        mCommonFunctions.mSplitTransactionsDeleted = savedInstanceState.getParcelableArrayList(EditTransactionActivityConstants.KEY_SPLIT_TRANSACTION_DELETED);
        mRecurringTransactionId = savedInstanceState.getInt(EditTransactionActivityConstants.KEY_BDID_ID);
        mNextOccurrence = savedInstanceState.getString(EditTransactionActivityConstants.KEY_NEXT_OCCURRENCE);
        // action
        mIntentAction = savedInstanceState.getString(EditTransactionActivityConstants.KEY_ACTION);
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
            mCommonFunctions.accountId = intent.getIntExtra(EditTransactionActivityConstants.KEY_ACCOUNT_ID, -1);

            // Edit transaction.

            if (mIntentAction != null && Intent.ACTION_EDIT.equals(mIntentAction)) {
                mTransId = intent.getIntExtra(EditTransactionActivityConstants.KEY_TRANS_ID, -1);
                // select data transaction
                loadCheckingAccount(mTransId, false);
            } else if (mIntentAction != null && Intent.ACTION_PASTE.equals(mIntentAction)) {
                // select data transaction
                loadCheckingAccount(intent.getIntExtra(EditTransactionActivityConstants.KEY_TRANS_ID, -1), true);
            } else {
                if (intent.getIntExtra(EditTransactionActivityConstants.KEY_BDID_ID, -1) > -1) {
                    mRecurringTransactionId = intent.getIntExtra(EditTransactionActivityConstants.KEY_BDID_ID, -1);
                    mNextOccurrence = intent.getStringExtra(EditTransactionActivityConstants.KEY_NEXT_OCCURRENCE);
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
                            if (payee != null && mCommonFunctions.payeeId == -1) {
                                // get id payee and category
                                mCommonFunctions.payeeId = payee.getPayeeId();
                                mCommonFunctions.payeeName = payee.getPayeeName();
                                mCommonFunctions.categoryId = payee.getCategId();
                                mCommonFunctions.subCategoryId = payee.getSubCategId();
                                // load category and subcategory name
                                loadCategorySubName(mCommonFunctions.categoryId, mCommonFunctions.subCategoryId);
                                return Boolean.TRUE;
                            }
                        } catch (Exception e) {
                            ExceptionHandler handler = new ExceptionHandler(EditTransactionActivity.this,
                                    EditTransactionActivity.this);
                            handler.handle(e, "loading default payee");
                        }
                        return Boolean.FALSE;
                    }

                    @Override
                    protected void onPostExecute(Boolean result) {
                        super.onPostExecute(result);
                        if (result) {
                            try {
                                // refresh field
                                mCommonFunctions.refreshPayeeName();
                                mCommonFunctions.refreshCategoryName();
                            } catch (Exception e) {
                                Log.e(EditTransactionActivityConstants.LOGCAT, e.getMessage());
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
        mCommonFunctions.transactionType = parameters.transactionType;

        if (parameters.accountId > 0) {
            this.mCommonFunctions.accountId = parameters.accountId;
        }
        this.mTotAmount = parameters.amount;
        // payee
        if (parameters.payeeId > 0) {
            this.mCommonFunctions.payeeId = parameters.payeeId;
            this.mCommonFunctions.payeeName = parameters.payeeName;
        } else {
            // create payee if it does not exist
            if (parameters.payeeName != null) {
                PayeeService newPayee = new PayeeService(this);
                mCommonFunctions.payeeId = newPayee.createNew(parameters.payeeName);
                mCommonFunctions.payeeName = parameters.payeeName;
            }
        }

        // category
        if (parameters.categoryId > 0) {
            mCommonFunctions.categoryId = parameters.categoryId;
            mCommonFunctions.categoryName = parameters.categoryName;
        } else {
            // No id sent.
            // create a category if it was sent but does not exist (id not found by the parser).
            if (parameters.categoryName != null) {
                CategoryService newCategory = new CategoryService(this);
                mCommonFunctions.categoryId = newCategory.createNew(mCommonFunctions.categoryName);
                mCommonFunctions.categoryName = parameters.categoryName;
            }
        }
    }

    public ArrayList<ISplitTransactionsDataset> getDeletedSplitCategories() {
        if(mCommonFunctions.mSplitTransactionsDeleted == null){
            mCommonFunctions.mSplitTransactionsDeleted = new ArrayList<>();
        }
        return mCommonFunctions.mSplitTransactionsDeleted;
    }

    /**
     * validate data insert in activity
     *
     * @return a boolean indicating whether the data is valid.
     */
    public boolean validateData() {
        boolean isTransfer = mCommonFunctions.transactionType.equals(TransactionTypes.Transfer);

        // Transfers.
        if (isTransfer) {
            if (mCommonFunctions.toAccountId == -1) {
                Core.alertDialog(this, R.string.error_toaccount_not_selected);
                return false;
            }
            if (mCommonFunctions.toAccountId == mCommonFunctions.accountId) {
                Core.alertDialog(this, R.string.error_transfer_to_same_account);
                return false;
            }
        }

        // Category is required if tx is not a split or transfer.
        if (mCommonFunctions.categoryId == -1 && (!mCommonFunctions.chbSplitTransaction.isChecked()) && !isTransfer) {
            Core.alertDialog(this, R.string.error_category_not_selected);
            return false;
        }
        // Splits.
        if (mCommonFunctions.chbSplitTransaction.isChecked() &&
                (mCommonFunctions.mSplitTransactions == null || mCommonFunctions.mSplitTransactions.size() <= 0)) {
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

        boolean isTransfer = mCommonFunctions.transactionType.equals(TransactionTypes.Transfer);

        values.put(TableCheckingAccount.ACCOUNTID, mCommonFunctions.accountId);
        if (isTransfer) {
            values.put(TableCheckingAccount.TOACCOUNTID, mCommonFunctions.toAccountId);
            values.put(TableCheckingAccount.PAYEEID, -1);
        } else {
            values.put(TableCheckingAccount.PAYEEID, mCommonFunctions.payeeId);
        }
        values.put(TableCheckingAccount.TRANSCODE, mCommonFunctions.getTransactionType());
        if (TextUtils.isEmpty(mCommonFunctions.txtAmount.getText().toString()) || (!isTransfer)) {
            values.put(TableCheckingAccount.TRANSAMOUNT, (Double) mCommonFunctions.txtTotAmount.getTag());
        } else {
            values.put(TableCheckingAccount.TRANSAMOUNT, (Double) mCommonFunctions.txtAmount.getTag());
        }
        values.put(TableCheckingAccount.STATUS, mStatus);
        values.put(TableCheckingAccount.CATEGID, !mCommonFunctions.chbSplitTransaction.isChecked()
                ? mCommonFunctions.categoryId : Constants.NOT_SET);
        values.put(TableCheckingAccount.SUBCATEGID, !mCommonFunctions.chbSplitTransaction.isChecked()
                ? mCommonFunctions.subCategoryId : Constants.NOT_SET);
        String transactionDate = DateUtils.getSQLiteStringDate(this, (Date) txtSelectDate.getTag());
        values.put(TableCheckingAccount.TRANSDATE, transactionDate);
        values.put(TableCheckingAccount.FOLLOWUPID, Constants.NOT_SET);
        values.put(TableCheckingAccount.TOTRANSAMOUNT, (Double) mCommonFunctions.txtTotAmount.getTag());
        values.put(TableCheckingAccount.TRANSACTIONNUMBER, edtTransNumber.getText().toString());
        values.put(TableCheckingAccount.NOTES, edtNotes.getText().toString());

        // check whether the application should do the update or insert
        if (Constants.INTENT_ACTION_INSERT.equals(mIntentAction) || Constants.INTENT_ACTION_PASTE.equals(mIntentAction)) {
            // insert
            Uri insert = getContentResolver().insert(mCheckingAccount.getUri(), values);
            if (insert == null) {
                Toast.makeText(getApplicationContext(), R.string.db_checking_insert_failed, Toast.LENGTH_SHORT).show();
                Log.w(EditTransactionActivityConstants.LOGCAT, "Insert new transaction failed!");
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
                Log.w(EditTransactionActivityConstants.LOGCAT, "Update transaction failed!");
                return false;
            }
        }

        // Split Categories

        // Delete any split categories if split is unchecked.
        if(!mCommonFunctions.chbSplitTransaction.isChecked()) {
            removeAllSplitCategories();
        }

        // has split categories
        boolean hasSplitCategories = mCommonFunctions.hasSplitCategories();
        // update split transaction
        if (hasSplitCategories) {
            for (int i = 0; i < mCommonFunctions.mSplitTransactions.size(); i++) {
                ISplitTransactionsDataset split = mCommonFunctions.mSplitTransactions.get(i);
                // do nothing if the split is marked for deletion.
                ArrayList<ISplitTransactionsDataset> deletedSplits = getDeletedSplitCategories();
                if(deletedSplits.contains(split)) {
                    continue;
                }

                values.clear();
                //put value
                values.put(TableSplitTransactions.CATEGID, mCommonFunctions.mSplitTransactions.get(i).getCategId());
                values.put(TableSplitTransactions.SUBCATEGID, mCommonFunctions.mSplitTransactions.get(i).getSubCategId());
                values.put(TableSplitTransactions.SPLITTRANSAMOUNT, mCommonFunctions.mSplitTransactions.get(i).getSplitTransAmount());
                values.put(TableSplitTransactions.TRANSID, mTransId);

                if (mCommonFunctions.mSplitTransactions.get(i).getSplitTransId() == -1) {
                    // insert data
                    if (getContentResolver().insert(mCommonFunctions.mSplitTransactions.get(i).getUri(), values) == null) {
                        Toast.makeText(getApplicationContext(), R.string.db_checking_insert_failed, Toast.LENGTH_SHORT).show();
                        Log.w(EditTransactionActivityConstants.LOGCAT, "Insert new split transaction failed!");
                        return false;
                    }
                } else {
                    // update data
                    if (getContentResolver().update(mCommonFunctions.mSplitTransactions.get(i).getUri(), values,
                            TableSplitTransactions.SPLITTRANSID + "=?",
                            new String[]{Integer.toString(mCommonFunctions.mSplitTransactions.get(i).getSplitTransId())}) <= 0) {
                        Toast.makeText(getApplicationContext(), R.string.db_checking_update_failed, Toast.LENGTH_SHORT).show();
                        Log.w(EditTransactionActivityConstants.LOGCAT, "Update split transaction failed!");
                        return false;
                    }
                }
            }
        }
        // deleted old split transaction
        if (mCommonFunctions.mSplitTransactionsDeleted != null && !mCommonFunctions.mSplitTransactionsDeleted.isEmpty()) {
            for (int i = 0; i < mCommonFunctions.mSplitTransactionsDeleted.size(); i++) {
                values.clear();
                //put value
                values.put(TableSplitTransactions.SPLITTRANSAMOUNT, mCommonFunctions.mSplitTransactionsDeleted.get(i).getSplitTransAmount());

                // update data
                if (getContentResolver().delete(mCommonFunctions.mSplitTransactionsDeleted.get(i).getUri(),
                        TableSplitTransactions.SPLITTRANSID + "=?",
                        new String[]{Integer.toString(mCommonFunctions.mSplitTransactionsDeleted.get(i).getSplitTransId())}) <= 0) {
                    Toast.makeText(getApplicationContext(), R.string.db_checking_update_failed, Toast.LENGTH_SHORT).show();
                    Log.w(EditTransactionActivityConstants.LOGCAT, "Delete split transaction failed!");
                    return false;
                }
            }
        }

        // update category and subcategory payee
        if ((!isTransfer) && (mCommonFunctions.payeeId > 0) && !hasSplitCategories) {
            // clear content value for update categoryId, subCategoryId
            values.clear();
            // set categoryId and subCategoryId
            values.put(TablePayee.CATEGID, mCommonFunctions.categoryId);
            values.put(TablePayee.SUBCATEGID, mCommonFunctions.subCategoryId);
            // create instance TablePayee for update
            TablePayee payee = new TablePayee();
            // update data
            if (getContentResolver().update(payee.getUri(), values,
                    TablePayee.PAYEEID + "=" + Integer.toString(mCommonFunctions.payeeId), null) <= 0) {
                Toast.makeText(getApplicationContext(), R.string.db_payee_update_failed, Toast.LENGTH_SHORT).show();
                Log.w(EditTransactionActivityConstants.LOGCAT, "Update Payee with Id=" + Integer.toString(mCommonFunctions.payeeId) + " return <= 0");
            }
        }

        //update recurring transaction
        if (mRecurringTransactionId > Constants.NOT_SET && !(TextUtils.isEmpty(mNextOccurrence))) {
            values.clear();

            // handle transactions that do not repeat any more.
            RecurringTransactionService recurringTransaction = new RecurringTransactionService(mRecurringTransactionId, this);
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
}
