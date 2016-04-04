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
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment;
import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.common.events.AmountEnteredEvent;
import com.money.manager.ex.core.NumericHelper;
import com.money.manager.ex.database.ISplitTransaction;
import com.money.manager.ex.datalayer.PayeeRepository;
import com.money.manager.ex.datalayer.SplitRecurringCategoriesRepository;
import com.money.manager.ex.domainmodel.RecurringTransaction;
import com.money.manager.ex.domainmodel.SplitRecurringCategory;
import com.money.manager.ex.servicelayer.RecurringTransactionService;
import com.money.manager.ex.datalayer.AccountRepository;
import com.money.manager.ex.datalayer.RecurringTransactionRepository;
import com.money.manager.ex.domainmodel.Payee;
import com.money.manager.ex.transactions.EditTransactionCommonFunctions;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.common.BaseFragmentActivity;
import com.money.manager.ex.transactions.events.DialogNegativeClickedEvent;
import com.money.manager.ex.transactions.events.DialogPositiveClickedEvent;
import com.money.manager.ex.utils.MyDateTimeUtils;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.joda.time.DateTime;
import org.parceler.Parcels;

/**
 * Recurring transactions are stored in BillsDeposits table.
 */
public class RecurringTransactionEditActivity
    extends BaseFragmentActivity {

    private static final String LOGCAT = RecurringTransactionEditActivity.class.getSimpleName();

    public static final String KEY_MODEL = "RecurringTransactionEditActivity:Model";
    public static final String KEY_BILL_DEPOSITS_ID = "RepeatingTransaction:BillDepositsId";
    public static final String KEY_ACCOUNT_ID = "RepeatingTransaction:AccountId";
    public static final String KEY_TO_ACCOUNT_NAME = "RepeatingTransaction:ToAccountName";
    public static final String KEY_TRANS_CODE = "RepeatingTransaction:TransCode";
    public static final String KEY_TRANS_STATUS = "RepeatingTransaction:TransStatus";
    public static final String KEY_TRANS_AMOUNT = "RepeatingTransaction:TransAmount";
    public static final String KEY_TRANS_AMOUNTTO = "RepeatingTransaction:TransTotAmount";
    public static final String KEY_PAYEE_NAME = "RepeatingTransaction:PayeeName";
    public static final String KEY_CATEGORY_NAME = "RepeatingTransaction:CategoryName";
    public static final String KEY_SUBCATEGORY_NAME = "RepeatingTransaction:SubCategoryName";
    public static final String KEY_NOTES = "RepeatingTransaction:Notes";
    public static final String KEY_TRANS_NUMBER = "RepeatingTransaction:TransNumber";
    public static final String KEY_SPLIT_TRANSACTION = "RepeatingTransaction:SplitCategory";
    public static final String KEY_SPLIT_TRANSACTION_DELETED = "RepeatingTransaction:SplitTransactionDeleted";
    public static final String KEY_ACTION = "RepeatingTransaction:Action";
    public static final String TAG_DATEPICKER = "DatePicker";

    // action type intent
    private String mIntentAction;

    // Model
    private RecurringTransaction mRecurringTransaction;

    // Form controls
    private RecurringTransactionViewHolder mViewHolder;
    private EditTransactionCommonFunctions mCommonFunctions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_recurring_transaction);

        initializeModel();

        mCommonFunctions = new EditTransactionCommonFunctions(this, mRecurringTransaction);

        setToolbarStandardAction(getToolbar());

        // manage save instance
        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState);
        }

        // manage intent
        if (getIntent() != null) {
            if (savedInstanceState == null) {
                String action = getIntent().getAction();
                if (action != null && action.equals(Intent.ACTION_EDIT)) {
                    int id = getIntent().getIntExtra(KEY_BILL_DEPOSITS_ID, Constants.NOT_SET);
                    // select data transaction
                    loadRecurringTransaction(id);
                } else {
                    mRecurringTransaction.setAccountId(getIntent().getIntExtra(KEY_ACCOUNT_ID, Constants.NOT_SET));
                }
            }
            mIntentAction = getIntent().getAction();
            // set title
            getSupportActionBar().setTitle(Intent.ACTION_INSERT.equals(mIntentAction)
                ? R.string.new_repeating_transaction : R.string.edit_repeating_transaction);
        }

        // Controls

        initializeViewHolder();

        initializeControls();

        // refresh user interface
        mCommonFunctions.onTransactionTypeChange(mCommonFunctions.transactionEntity.getTransactionType());
        mCommonFunctions.refreshPayeeName();
        mCommonFunctions.displayCategoryName();

        showPaymentsLeft();
    }

    @Override
    protected void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);

        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mCommonFunctions.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(KEY_MODEL, Parcels.wrap(mRecurringTransaction));

        // save the state interface
        outState.putString(KEY_TO_ACCOUNT_NAME, mCommonFunctions.mToAccountName);
        outState.putString(KEY_TRANS_CODE, mCommonFunctions.getTransactionType());
        outState.putString(KEY_TRANS_STATUS, mCommonFunctions.transactionEntity.getStatus());

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

        outState.putString(KEY_PAYEE_NAME, mCommonFunctions.payeeName);
        outState.putString(KEY_CATEGORY_NAME, mCommonFunctions.categoryName);
        outState.putString(KEY_SUBCATEGORY_NAME, mCommonFunctions.subCategoryName);
        outState.putString(KEY_TRANS_NUMBER, mCommonFunctions.edtTransNumber.getText().toString());
        outState.putParcelable(KEY_SPLIT_TRANSACTION, Parcels.wrap(mCommonFunctions.mSplitTransactions));
        outState.putParcelable(KEY_SPLIT_TRANSACTION_DELETED, Parcels.wrap(mCommonFunctions.mSplitTransactionsDeleted));
        outState.putString(KEY_NOTES, String.valueOf(mCommonFunctions.edtNotes.getTag()));

        outState.putString(KEY_ACTION, mIntentAction);
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

    // Events

    @Subscribe
    public void onEvent(AmountEnteredEvent event) {
        int id = Integer.parseInt(event.requestId);

        mCommonFunctions.onFinishedInputAmountDialog(id, event.amount);
    }

    @Subscribe
    public void onEvent(DialogPositiveClickedEvent event) {
        mCommonFunctions.confirmDeletingCategories();
    }

    @Subscribe
    public void onEvent(DialogNegativeClickedEvent event) {
        mCommonFunctions.cancelChangingTransactionToTransfer();
    }

    // Public

    /**
     * refresh the UI control Payments Left
     */
    public void showPaymentsLeft() {
        Recurrence recurrence = mRecurringTransaction.getRecurrence();

        // Recurrence label

        mViewHolder.recurrenceLabel.setText((recurrence == Recurrence.IN_X_DAYS) || (recurrence == Recurrence.IN_X_MONTHS)
                ? R.string.activates : R.string.occurs);

        // Payments Left header

        mViewHolder.paymentsLeftTextView.setVisibility(recurrence.getValue() > 0 ? View.VISIBLE : View.GONE);
        mViewHolder.paymentsLeftTextView.setText(recurrence.getValue() >= 11 ? R.string.activates : R.string.payments_left);

        // Payments Left input

        mViewHolder.paymentsLeftEditText.setVisibility(recurrence.getValue() > 0 ? View.VISIBLE : View.GONE);
        mViewHolder.paymentsLeftEditText.setHint(recurrence.getValue() >= 11 ? R.string.activates : R.string.payments_left);

        Integer occurrences = mRecurringTransaction.getPaymentsLeft();
        if (occurrences == null) {
            occurrences = Constants.NOT_SET;
            mRecurringTransaction.setPaymentsLeft(Constants.NOT_SET);
        }
        String value = occurrences == Constants.NOT_SET
                ? "∞"
                : Integer.toString(occurrences);
        mViewHolder.paymentsLeftEditText.setText(value);

//        if (mRecurringTransaction.getPaymentsLeft() != null && mRecurringTransaction.getPaymentsLeft() >= 0) {
//            mViewHolder.paymentsLeftEditText.setText(Integer.toString(mRecurringTransaction.getPaymentsLeft()));
//        }
    }

    // Private

    private void initializeControls() {
        // Payment Date
        initializePaymentDateSelector();

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

        // Frequency

        Spinner spinFrequencies = (Spinner) findViewById(R.id.spinnerFrequencies);

        Integer recurrence = mRecurringTransaction.getRecurrenceInt();
        if (recurrence >= 200) {
            recurrence = recurrence - 200;
        } // set auto execute without user acknowledgement
        if (recurrence >= 100) {
            recurrence = recurrence - 100;
        } // set auto execute on the next occurrence
        spinFrequencies.setSelection(recurrence, true);
        spinFrequencies.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mCommonFunctions.setDirty(true);

                mRecurringTransaction.setRecurrence(position);
                showPaymentsLeft();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mRecurringTransaction.setRecurrence(Constants.NOT_SET);
                showPaymentsLeft();
            }
        });
    }

    private void initializePaymentDateSelector() {
        if (mViewHolder.paymentDateTextView == null) return;

        DateTime paymentDate = mRecurringTransaction.getPaymentDate();
        mViewHolder.paymentDateTextView.setText(paymentDate.toString(Constants.LONG_DATE_PATTERN));
        mViewHolder.paymentDateTextView.setTag(paymentDate.toString(Constants.ISO_DATE_FORMAT));

        mViewHolder.paymentDateTextView.setOnClickListener(new View.OnClickListener() {
            CalendarDatePickerDialogFragment.OnDateSetListener listener = new CalendarDatePickerDialogFragment.OnDateSetListener() {
                @Override
                public void onDateSet(CalendarDatePickerDialogFragment dialog, int year, int monthOfYear, int dayOfMonth) {
                    mCommonFunctions.setDirty(true);

                    DateTime dateTime = MyDateTimeUtils.from(year, monthOfYear + 1, dayOfMonth);

                    mViewHolder.paymentDateTextView.setTag(dateTime.toString(Constants.ISO_DATE_FORMAT));
                    mRecurringTransaction.setPaymentDate(dateTime);
                    mViewHolder.paymentDateTextView.setText(dateTime.toString(Constants.LONG_DATE_PATTERN));
                }
            };

            @Override
            public void onClick(View v) {
                // Show calendar with the current date selected.

                DateTime dateTime = mRecurringTransaction.getPaymentDate();
                if (dateTime == null) {
                    dateTime = DateTime.now();
                    mRecurringTransaction.setPaymentDate(dateTime);
                }

                CalendarDatePickerDialogFragment datePicker = new CalendarDatePickerDialogFragment()
                        .setOnDateSetListener(listener)
                        .setPreselectedDate(dateTime.getYear(), dateTime.getMonthOfYear() - 1, dateTime.getDayOfMonth())
                        .setThemeDark();
                datePicker.show(getSupportFragmentManager(), TAG_DATEPICKER);
            }
        });

    }

    private void initializeModel() {
        mRecurringTransaction = RecurringTransaction.createInstance();

        mRecurringTransaction.setDueDate(MyDateTimeUtils.today());
        mRecurringTransaction.setPaymentDate(MyDateTimeUtils.today());
    }

    private void initializeViewHolder() {
        // Controls need to be at the beginning as they are referenced throughout the code.
        mCommonFunctions.findControls();

        mViewHolder = new RecurringTransactionViewHolder();

        // Due Date = date
        mCommonFunctions.initDateSelector();

        // Payment Date, next occurrence
        mViewHolder.paymentDateTextView = (TextView) findViewById(R.id.paymentDateTextView);

        // Recurrence label
        mViewHolder.recurrenceLabel = (TextView) findViewById(R.id.recurrenceLabel);

        // Payments Left label
        mViewHolder.paymentsLeftTextView = (TextView) findViewById(R.id.textViewTimesRepeated);

        // Payments Left text input
        mViewHolder.paymentsLeftEditText = (EditText) findViewById(R.id.editTextTimesRepeated);
    }

    /**
     * this method allows you to search the transaction data
     *
     * @param recurringTransactionId transaction id
     * @return true if data selected, false nothing
     */
    private boolean loadRecurringTransaction(int recurringTransactionId) {
        RecurringTransactionRepository repo = new RecurringTransactionRepository(this);
        mRecurringTransaction = repo.load(recurringTransactionId);
        if (mRecurringTransaction == null) return false;

        mCommonFunctions.transactionEntity = mRecurringTransaction;

        // Read data.
        String transCode = mRecurringTransaction.getTransactionCode();
        mCommonFunctions.transactionEntity.setTransactionType(TransactionTypes.valueOf(transCode));

        // load split transactions only if no category selected.
        if (!mCommonFunctions.transactionEntity.hasCategory() && mCommonFunctions.mSplitTransactions == null) {
            RecurringTransactionService recurringTransaction = new RecurringTransactionService(recurringTransactionId, this);
            mCommonFunctions.mSplitTransactions = recurringTransaction.loadSplitTransactions();
        }

        AccountRepository accountRepository = new AccountRepository(this);
        mCommonFunctions.mToAccountName = accountRepository.loadName(mCommonFunctions.transactionEntity.getAccountToId());

        mCommonFunctions.selectPayeeName(mCommonFunctions.transactionEntity.getPayeeId());
        mCommonFunctions.loadCategoryName();

        return true;
    }

    /**
     * validate data insert in activity
     *
     * @return validation result
     */
    private boolean validateData() {
        if (!mCommonFunctions.validateData()) return false;

        // Due Date is required
        if (StringUtils.isEmpty(mRecurringTransaction.getDueDateString())) {
            Core.alertDialog(this, R.string.due_date_required);
            return false;
        }

        if (TextUtils.isEmpty(mCommonFunctions.viewHolder.dateTextView.getText().toString())) {
            Core.alertDialog(this, R.string.error_next_occurrence_not_populate);

            return false;
        }

        // Payments Left must have a value
        if (mRecurringTransaction.getPaymentsLeft() == null) {
            Core.alertDialog(this, R.string.payments_left_required);
            return false;
        }
        return true;
    }

    private void collectDataFromUI() {
        String value;

        // Payment Date

        DateTime dateTime = MyDateTimeUtils.from(mViewHolder.paymentDateTextView.getTag().toString());
        mRecurringTransaction.setPaymentDate(dateTime);

        // Payments Left

        value = mViewHolder.paymentsLeftEditText.getText().toString();
        if (NumericHelper.isNumeric(value)) {
            int paymentsLeft = NumericHelper.toInt(value);
            mRecurringTransaction.setPaymentsLeft(paymentsLeft);
        } else {
            mRecurringTransaction.setPaymentsLeft(Constants.NOT_SET);
        }

    }

    /**
     * update data into database
     *
     * @return true if update data successful
     */
    private boolean saveData() {
        // get data from input controls
        collectDataFromUI();

        if (!validateData()) return false;

        // Transaction. Need the id for split categories.

        if (!saveTransaction()) return false;

        // Split Categories

        if (mCommonFunctions.handleOneSplit()) {
            saveTransaction();
        }

        if(!mCommonFunctions.isSplitSelected()) {
            // Delete any split categories if split is unchecked.
            mCommonFunctions.removeAllSplitCategories();
        }
        if (!saveSplitCategories()) return false;

        return true;
    }

    private ContentValues getContentValues(boolean isTransfer) {
        ContentValues values = mCommonFunctions.getContentValues(isTransfer);

        values.put(RecurringTransaction.TRANSDATE, mRecurringTransaction.getDueDateString());
        values.put(RecurringTransaction.NEXTOCCURRENCEDATE, mRecurringTransaction.getPaymentDateString());
        values.put(RecurringTransaction.REPEATS, mRecurringTransaction.getRecurrenceInt());
        values.put(RecurringTransaction.NUMOCCURRENCES, mRecurringTransaction.getPaymentsLeft());

        return values;
    }

    private void restoreInstanceState(Bundle savedInstanceState) {
        // Restore the transaction entity.
        mRecurringTransaction = savedInstanceState.getParcelable(KEY_MODEL);

        mCommonFunctions.mToAccountName = savedInstanceState.getString(KEY_TO_ACCOUNT_NAME);
        String transCode = savedInstanceState.getString(KEY_TRANS_CODE);
//        mCommonFunctions.transactionType = TransactionTypes.valueOf(transCode);
        mCommonFunctions.payeeName = savedInstanceState.getString(KEY_PAYEE_NAME);
        mCommonFunctions.categoryName = savedInstanceState.getString(KEY_CATEGORY_NAME);
        mCommonFunctions.subCategoryName = savedInstanceState.getString(KEY_SUBCATEGORY_NAME);
        mCommonFunctions.mSplitTransactions = Parcels.unwrap(savedInstanceState.getParcelable(KEY_SPLIT_TRANSACTION));
        mCommonFunctions.mSplitTransactionsDeleted = Parcels.unwrap(savedInstanceState.getParcelable(KEY_SPLIT_TRANSACTION_DELETED));

        // action
        mIntentAction = savedInstanceState.getString(KEY_ACTION);
    }

    private boolean saveSplitCategories() {
        SplitRecurringCategoriesRepository splitRepo = new SplitRecurringCategoriesRepository(this);

        // deleted old split transaction
        if (mCommonFunctions.getDeletedSplitCategories().size() > 0) {
            if (!mCommonFunctions.deleteMarkedSplits(splitRepo)) return false;
        }

        // has split transaction
        boolean hasSplitCategories = mCommonFunctions.hasSplitCategories();
        if (hasSplitCategories) {
            for (ISplitTransaction item : mCommonFunctions.mSplitTransactions) {
                SplitRecurringCategory splitEntity = (SplitRecurringCategory) item;

                splitEntity.setTransId(mRecurringTransaction.getId());

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

        return true;
    }

    private boolean saveTransaction() {
        RecurringTransactionRepository repo = new RecurringTransactionRepository(this);

        boolean isTransfer = mCommonFunctions.transactionEntity.getTransactionType().equals(TransactionTypes.Transfer);
        ContentValues values = getContentValues(isTransfer);

        // mIntentAction.equals(Intent.ACTION_INSERT)
        if (!mCommonFunctions.transactionEntity.hasId()) {
            // insert
//            Uri insert = getContentResolver().insert(repo.getUri(), values);
//            if (insert == null) {
            mCommonFunctions.transactionEntity = repo.insert((RecurringTransaction) mCommonFunctions.transactionEntity);

            if (mCommonFunctions.transactionEntity.getId() == Constants.NOT_SET) {
                Core.alertDialog(this, R.string.db_checking_insert_failed);
                Log.w(LOGCAT, "Insert new repeating transaction failed!");
                return false;
            }
//            long id = ContentUris.parseId(insert);
//            mRecurringTransaction.setId((int) id);
        } else {
            // update
//            if (getContentResolver().update(repo.getUri(), values,
//                    com.money.manager.ex.domainmodel.RecurringTransaction.BDID + "=?",
//                    new String[]{Integer.toString(mRecurringTransaction.getId())}) <= 0) {
            if (!repo.update((RecurringTransaction) mCommonFunctions.transactionEntity)) {
                Core.alertDialog(this, R.string.db_checking_update_failed);
                Log.w(LOGCAT, "Update repeating  transaction failed!");
                return false;
            }
        }
        return true;
    }
}

