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
package com.money.manager.ex.recurring.transactions;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.database.ITransactionEntity;
import com.money.manager.ex.datalayer.SplitRecurringCategoriesRepository;
import com.money.manager.ex.domainmodel.SplitCategory;
import com.money.manager.ex.domainmodel.SplitRecurringCategory;
import com.money.manager.ex.servicelayer.RecurringTransactionService;
import com.money.manager.ex.datalayer.AccountRepository;
import com.money.manager.ex.datalayer.RecurringTransactionRepository;
import com.money.manager.ex.domainmodel.Payee;
import com.money.manager.ex.transactions.EditTransactionCommonFunctions;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.NumericHelper;
import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.database.TableBillsDeposits;
import com.money.manager.ex.database.TablePayee;
import com.money.manager.ex.common.BaseFragmentActivity;
import com.money.manager.ex.common.IInputAmountDialogListener;
import com.money.manager.ex.transactions.YesNoDialogListener;
import com.money.manager.ex.viewmodels.RecurringTransaction;

import java.text.SimpleDateFormat;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

/**
 * Recurring transactions are stored in BillsDeposits table.
 */
public class EditRecurringTransactionActivity
    extends BaseFragmentActivity
    implements IInputAmountDialogListener, YesNoDialogListener {

    private static final String LOGCAT = EditRecurringTransactionActivity.class.getSimpleName();

    public static final String KEY_MODEL = "EditRecurringTransactionActivity:Model";
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
//    public static final String KEY_NUM_OCCURRENCE = "RepeatingTransaction:NumOccurrence";
    public static final String KEY_SPLIT_TRANSACTION = "RepeatingTransaction:SplitCategory";
    public static final String KEY_SPLIT_TRANSACTION_DELETED = "RepeatingTransaction:SplitTransactionDeleted";
    public static final String KEY_ACTION = "RepeatingTransaction:Action";

    // action type intent
    private String mIntentAction;

    // Model
    private TableBillsDeposits mRecurringTransaction;
    private int mBillDepositsId = Constants.NOT_SET;
    private int mFrequencies = 0;

    // Controls on the form.
//    private ImageButton btnTransNumber;
    private EditText edtTimesRepeated;
    private TextView txtRepeats, txtTimesRepeated;

    private EditTransactionCommonFunctions mCommonFunctions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_recurring_transaction);

        mRecurringTransaction = new TableBillsDeposits().initialize();

        mCommonFunctions = new EditTransactionCommonFunctions(this, this,
            new RecurringTransaction());

        setToolbarStandardAction(getToolbar());

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
            getSupportActionBar().setTitle(Intent.ACTION_INSERT.equals(mIntentAction)
                    ? R.string.new_repeating_transaction : R.string.edit_repeating_transaction);
        }

        // Controls

        txtRepeats = (TextView) findViewById(R.id.textViewRepeat);
        txtTimesRepeated = (TextView) findViewById(R.id.textViewTimesRepeated);

        // Account(s)
        mCommonFunctions.initAccountSelectors();

        // Transaction type
        mCommonFunctions.initTransactionTypeSelector();

        // status
        mCommonFunctions.initStatusSelector();

        // Payee
        mCommonFunctions.initPayeeControls();

        // Category
        mCommonFunctions.initCategoryControls(SplitRecurringCategory.class.getSimpleName());

        // Split Categories
        mCommonFunctions.initSplitCategories();

        // mark checked if there are existing split categories.
        boolean hasSplit = mCommonFunctions.hasSplitCategories();
        mCommonFunctions.setSplit(hasSplit);

        // Amount and total amount

        mCommonFunctions.initAmountSelectors();

        // transaction number
        mCommonFunctions.initTransactionNumberControls();

        // notes
        mCommonFunctions.initNotesControls();

        // next occurrence
        mCommonFunctions.initDateSelector();

        // times repeated
        edtTimesRepeated = (EditText) findViewById(R.id.editTextTimesRepeated);
        if (mRecurringTransaction.numOccurrence != null && mRecurringTransaction.numOccurrence >= 0) {
            edtTimesRepeated.setText(Integer.toString(mRecurringTransaction.numOccurrence));
        }

        // Frequency

        Spinner spinFrequencies = (Spinner) findViewById(R.id.spinnerFrequencies);

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
                mCommonFunctions.setDirty(true);

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
        mCommonFunctions.onTransactionTypeChange(mCommonFunctions.transactionType);
        mCommonFunctions.refreshPayeeName();
        mCommonFunctions.refreshCategoryName();
        refreshTimesRepeated();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mCommonFunctions.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save the state interface
        outState.putInt(KEY_BILL_DEPOSITS_ID, mBillDepositsId);
        outState.putInt(KEY_ACCOUNT_ID, mCommonFunctions.accountId);
        outState.putInt(KEY_TO_ACCOUNT_ID, mCommonFunctions.toAccountId);
        outState.putString(KEY_TO_ACCOUNT_NAME, mCommonFunctions.mToAccountName);
        outState.putString(KEY_TRANS_CODE, mCommonFunctions.getTransactionType());
        outState.putString(KEY_TRANS_STATUS, mCommonFunctions.status);

        // Amount To
        String value = "";
        Object tag = mCommonFunctions.viewHolder.txtAmountTo.getTag();
        if (tag != null) {
            value = tag.toString();
        }
        outState.putString(KEY_TRANS_AMOUNTTO, value);

        // amount
        value = "";
        tag = mCommonFunctions.viewHolder.txtAmount.getTag();
        if (tag != null) {
            value = tag.toString();
        }
        outState.putString(KEY_TRANS_AMOUNT, value);

        outState.putInt(KEY_PAYEE_ID, mCommonFunctions.payeeId);
        outState.putString(KEY_PAYEE_NAME, mCommonFunctions.payeeName);
        int categoryId = mCommonFunctions.transactionEntity.getCategoryId() != null
            ? mCommonFunctions.transactionEntity.getCategoryId()
            : Constants.NOT_SET;
        outState.putInt(KEY_CATEGORY_ID, categoryId);
        outState.putString(KEY_CATEGORY_NAME, mCommonFunctions.categoryName);
        outState.putInt(KEY_SUBCATEGORY_ID, mCommonFunctions.subCategoryId);
        outState.putString(KEY_SUBCATEGORY_NAME, mCommonFunctions.subCategoryName);
        outState.putString(KEY_TRANS_NUMBER, mCommonFunctions.edtTransNumber.getText().toString());
        outState.putParcelableArrayList(KEY_SPLIT_TRANSACTION, mCommonFunctions.mSplitTransactions);
        outState.putParcelableArrayList(KEY_SPLIT_TRANSACTION_DELETED, mCommonFunctions.mSplitTransactionsDeleted);
        outState.putString(KEY_NOTES, String.valueOf(mCommonFunctions.edtNotes.getTag()));
//        Locale locale = getResources().getConfiguration().locale;
        outState.putString(KEY_NEXT_OCCURRENCE, new SimpleDateFormat(Constants.PATTERN_DB_DATE)
                .format(mCommonFunctions.viewHolder.txtSelectDate.getTag()));
        outState.putInt(KEY_REPEATS, mFrequencies);

        NumericHelper helper = new NumericHelper(getApplicationContext());
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
    public void onFinishedInputAmountDialog(int id, Money amount) {
        mCommonFunctions.onFinishedInputAmountDialog(id, amount);
    }

    @Override
    public boolean onActionCancelClick() {
        return mCommonFunctions.onActionCancelClick();
    }

    @Override
    public void onBackPressed() {
        onActionCancelClick();
    }

    @Override
    public boolean onActionDoneClick() {
        if (saveData()) {
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
        mCommonFunctions.status = tx.status;
        mCommonFunctions.transactionEntity.setAmount(tx.amount);
        mCommonFunctions.amountTo = tx.totalAmount;
        mCommonFunctions.payeeId = tx.payeeId;
        mCommonFunctions.transactionEntity.setCategoryId(tx.categoryId);
        mCommonFunctions.subCategoryId = tx.subCategoryId;
        mCommonFunctions.mTransNumber = tx.transactionNumber;
        mCommonFunctions.mNotes = tx.notes;
        mCommonFunctions.mDate = tx.nextOccurrence;
        mFrequencies = tx.repeats;

        // load split transactions only if no category selected.
        if ((mCommonFunctions.transactionEntity.getCategoryId() == null || mCommonFunctions.transactionEntity.getCategoryId() == Constants.NOT_SET)
            && mCommonFunctions.mSplitTransactions == null) {
            RecurringTransactionService recurringTransaction = new RecurringTransactionService(recurringTransactionId, this);
            mCommonFunctions.mSplitTransactions = recurringTransaction.loadSplitTransactions();
        }

        AccountRepository accountRepository = new AccountRepository(this);
        mCommonFunctions.mToAccountName = accountRepository.loadName(mCommonFunctions.toAccountId);

        mCommonFunctions.selectPayeeName(mCommonFunctions.payeeId);
        mCommonFunctions.displayCategoryName();

        return true;
    }

    /**
     * refresh the UI control times repeated
     */
    public void refreshTimesRepeated() {
        txtRepeats.setText((mFrequencies == 11) || (mFrequencies == 12) ? R.string.activates : R.string.occurs);

        txtTimesRepeated.setVisibility(mFrequencies > 0 ? View.VISIBLE : View.GONE);
        txtTimesRepeated.setText(mFrequencies >= 11 ? R.string.activates : R.string.times_repeated);

        edtTimesRepeated.setVisibility(mFrequencies > 0 ? View.VISIBLE : View.GONE);
        edtTimesRepeated.setHint(mFrequencies >= 11 ? R.string.activates : R.string.times_repeated);
    }

    /**
     * validate data insert in activity
     *
     * @return validation result
     */
    private boolean validateData() {
        if (!mCommonFunctions.validateData()) return false;

        if (TextUtils.isEmpty(mCommonFunctions.viewHolder.txtSelectDate.getText().toString())) {
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
    private boolean saveData() {
        if (!validateData()) return false;

        boolean isTransfer = mCommonFunctions.transactionType.equals(TransactionTypes.Transfer);
        ContentValues values = getContentValues(isTransfer);

        // Insert or update
        TableBillsDeposits recurringTransaction = new TableBillsDeposits();
        if (Intent.ACTION_INSERT.equals(mIntentAction)) {
            // insert
            Uri insert = getContentResolver().insert(recurringTransaction.getUri(), values);
            if (insert == null) {
                Core.alertDialog(this, R.string.db_checking_insert_failed);
                Log.w(LOGCAT, "Insert new repeating transaction failed!");
                return false;
            }
            long id = ContentUris.parseId(insert);
            mBillDepositsId = (int) id;
        } else {
            // update
            if (getContentResolver().update(recurringTransaction.getUri(), values,
                    TableBillsDeposits.BDID + "=?",
                    new String[]{Integer.toString(mBillDepositsId)}) <= 0) {
                Core.alertDialog(this, R.string.db_checking_update_failed);
                Log.w(LOGCAT, "Update repeating  transaction failed!");
                return false;
            }
        }

        // has split transaction
        boolean hasSplitTransaction = mCommonFunctions.mSplitTransactions != null && mCommonFunctions.mSplitTransactions.size() > 0;
        if (hasSplitTransaction) {
            SplitRecurringCategoriesRepository splitRepo = new SplitRecurringCategoriesRepository(this);

            for (ITransactionEntity item : mCommonFunctions.mSplitTransactions) {
                SplitRecurringCategory splitEntity = (SplitRecurringCategory) item;

                splitEntity.setTransId(mBillDepositsId);

                if (splitEntity.getId() == null || splitEntity.getId() == Constants.NOT_SET) {
                    // insert data
                    if (!splitRepo.insert(splitEntity)) {
                        Toast.makeText(getApplicationContext(), R.string.db_checking_insert_failed, Toast.LENGTH_SHORT).show();
                        Log.w(LOGCAT, "Insert new split transaction failed!");
                        return false;
                    }
                } else {
                    // update data
                    if (!splitRepo.update(splitEntity)) {
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
                values.put(SplitCategory.SPLITTRANSAMOUNT,
                        mCommonFunctions.mSplitTransactionsDeleted.get(i).getAmount().toString());

                SplitRecurringCategoriesRepository splitRepo = new SplitRecurringCategoriesRepository(this);
                // todo: use repo to delete the record.
                if (getContentResolver().delete(splitRepo.getUri(),
                    SplitCategory.SPLITTRANSID + "=?",
                        new String[]{Integer.toString(mCommonFunctions.mSplitTransactionsDeleted.get(i).getId())}) <= 0) {
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
            values.put(Payee.CATEGID, mCommonFunctions.transactionEntity.getCategoryId());
            values.put(Payee.SUBCATEGID, mCommonFunctions.subCategoryId);
            // create instance TablePayee for update
            TablePayee payee = new TablePayee();
            // update data
            if (getContentResolver().update(payee.getUri(),
                    values,
                    Payee.PAYEEID + "=" + Integer.toString(mCommonFunctions.payeeId),
                    null) <= 0) {
                Toast.makeText(getApplicationContext(), R.string.db_payee_update_failed, Toast.LENGTH_SHORT).show();
                Log.w(LOGCAT, "Update Payee with Id=" + Integer.toString(mCommonFunctions.payeeId) + " return <= 0");
            }
        }

        return true;
    }

    private ContentValues getContentValues(boolean isTransfer) {
        ContentValues values = mCommonFunctions.getContentValues(isTransfer);

        values.put(TableBillsDeposits.NEXTOCCURRENCEDATE, new SimpleDateFormat(Constants.PATTERN_DB_DATE)
                .format(mCommonFunctions.viewHolder.txtSelectDate.getTag()));
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
        mCommonFunctions.mToAccountName = savedInstanceState.getString(KEY_TO_ACCOUNT_NAME);
        String transCode = savedInstanceState.getString(KEY_TRANS_CODE);
        mCommonFunctions.transactionType = TransactionTypes.valueOf(transCode);
        mCommonFunctions.status = savedInstanceState.getString(KEY_TRANS_STATUS);

//        NumericHelper numericHelper = new NumericHelper(getApplicationContext());

        mCommonFunctions.transactionEntity.setAmount(MoneyFactory.fromString(savedInstanceState.getString(KEY_TRANS_AMOUNT)));
        mCommonFunctions.amountTo = MoneyFactory.fromString(savedInstanceState.getString(KEY_TRANS_AMOUNTTO));

        mCommonFunctions.payeeId = savedInstanceState.getInt(KEY_PAYEE_ID);
        mCommonFunctions.payeeName = savedInstanceState.getString(KEY_PAYEE_NAME);
        mCommonFunctions.transactionEntity.setCategoryId(savedInstanceState.getInt(KEY_CATEGORY_ID));
        mCommonFunctions.categoryName = savedInstanceState.getString(KEY_CATEGORY_NAME);
        mCommonFunctions.subCategoryId = savedInstanceState.getInt(KEY_SUBCATEGORY_ID);
        mCommonFunctions.subCategoryName = savedInstanceState.getString(KEY_SUBCATEGORY_NAME);
        mCommonFunctions.mNotes = savedInstanceState.getString(KEY_NOTES);
        mCommonFunctions.mTransNumber = savedInstanceState.getString(KEY_TRANS_NUMBER);
        mCommonFunctions.mSplitTransactions = savedInstanceState.getParcelableArrayList(KEY_SPLIT_TRANSACTION);
        mCommonFunctions.mSplitTransactionsDeleted = savedInstanceState.getParcelableArrayList(KEY_SPLIT_TRANSACTION_DELETED);
        mCommonFunctions.mDate = savedInstanceState.getString(KEY_NEXT_OCCURRENCE);
        mFrequencies = savedInstanceState.getInt(KEY_REPEATS);

        // action
        mIntentAction = savedInstanceState.getString(KEY_ACTION);
    }

    // YesNoDialogListener

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        mCommonFunctions.confirmDeletingCategories();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        mCommonFunctions.cancelChangingTransactionToTransfer();
    }
}

