/*
 * Copyright (C) 2012-2024 The Android Money Manager Ex Project Team
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
package com.money.manager.ex.scheduled;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;
import android.widget.Toast;

import com.money.manager.ex.Constants;
import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.common.MmxBaseFragmentActivity;
import com.money.manager.ex.common.events.AmountEnteredEvent;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.MenuHelper;
import com.money.manager.ex.core.NumericHelper;
import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.database.ISplitTransaction;
import com.money.manager.ex.datalayer.AccountRepository;
import com.money.manager.ex.datalayer.ScheduledTransactionRepository;
import com.money.manager.ex.datalayer.SplitScheduledCategoryRepository;
import com.money.manager.ex.datalayer.TaglinkRepository;
import com.money.manager.ex.domainmodel.RecurringTransaction;
import com.money.manager.ex.domainmodel.SplitRecurringCategory;
import com.money.manager.ex.domainmodel.TagLink;
import com.money.manager.ex.servicelayer.RecurringTransactionService;
import com.money.manager.ex.transactions.EditTransactionCommonFunctions;
import com.money.manager.ex.transactions.events.DialogNegativeClickedEvent;
import com.money.manager.ex.transactions.events.DialogPositiveClickedEvent;
import com.money.manager.ex.utils.MmxDate;
import com.money.manager.ex.utils.MmxDateTimeUtils;
import com.squareup.sqlbrite3.BriteDatabase;

import org.greenrobot.eventbus.Subscribe;
import org.parceler.Parcels;

import java.util.Date;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * Recurring transactions are stored in BillsDeposits table.
 */
public class ScheduledTransactionEditActivity
    extends MmxBaseFragmentActivity {

    public static final String KEY_MODEL = "ScheduledTransactionEditActivity:Model";
    public static final String KEY_BILL_DEPOSITS_ID = "RepeatingTransaction:BillDepositsId";
    public static final String KEY_ACCOUNT_ID = "RepeatingTransaction:AccountId";
    public static final String KEY_TO_ACCOUNT_NAME = "RepeatingTransaction:ToAccountName";
    public static final String KEY_TRANS_CODE = "RepeatingTransaction:TransCode";
    public static final String KEY_TRANS_STATUS = "RepeatingTransaction:TransStatus";
    public static final String KEY_PAYEE_NAME = "RepeatingTransaction:PayeeName";
    public static final String KEY_CATEGORY_NAME = "RepeatingTransaction:CategoryName";
    public static final String KEY_SUBCATEGORY_NAME = "RepeatingTransaction:SubCategoryName";
    public static final String KEY_NOTES = "RepeatingTransaction:Notes";
    public static final String KEY_TRANS_NUMBER = "RepeatingTransaction:TransNumber";
    public static final String KEY_SPLIT_TRANSACTION = "RepeatingTransaction:SplitCategory";
    public static final String KEY_SPLIT_TRANSACTION_DELETED = "RepeatingTransaction:SplitTransactionDeleted";
    public static final String TAG_DATEPICKER = "DatePicker";

    @Inject BriteDatabase database;
    @Inject MmxDateTimeUtils dateUtils;

    String mIntentAction;

    private ScheduledTransactionViewHolder mViewHolder;
    private EditTransactionCommonFunctions mCommon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_recurring_transaction);

        MmexApplication.getApp().iocComponent.inject(this);

        RecurringTransaction tx = initializeModel();
        mCommon = new EditTransactionCommonFunctions(this, tx, database);

        mCommon.initializeToolbar();

        // manage update instance
        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState);
        }

        // manage intent
        if (getIntent() != null) {
            if (savedInstanceState == null) {
                String action = getIntent().getAction();
                if (action != null && action.equals(Intent.ACTION_EDIT)) {
                    long id = getIntent().getLongExtra(KEY_BILL_DEPOSITS_ID, Constants.NOT_SET);
                    // select data transaction
                    loadRecurringTransaction(id);
                } else if (action != null && action.equals(Intent.ACTION_INSERT) && getIntent().getLongExtra(KEY_BILL_DEPOSITS_ID, Constants.NOT_SET) != -1) {
                    long id = getIntent().getLongExtra(KEY_BILL_DEPOSITS_ID, Constants.NOT_SET);
                    // select data transaction
                    loadRecurringTransaction(id);
                    mCommon.transactionEntity.setId(null);
                    // clear cross reference for tag, and split
                    mCommon.transactionEntity.setTagLinks(TagLink.clearCrossReference(mCommon.transactionEntity.getTagLinks()));
                    if ( mCommon.mSplitTransactions != null) {
                        for (ISplitTransaction split : mCommon.mSplitTransactions) {
                            SplitRecurringCategory splitEntity = (SplitRecurringCategory) split;
                            splitEntity.setId(null);
                            splitEntity.setTagLinks(TagLink.clearCrossReference(split.getTagLinks()));
                            splitEntity.setTransId(-1);
                        }
                    }
                } else {
                    mCommon.transactionEntity.setAccountId(getIntent().getLongExtra(KEY_ACCOUNT_ID, Constants.NOT_SET));
                }
            }
            mIntentAction = getIntent().getAction();
            // set title
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(Intent.ACTION_INSERT.equals(mIntentAction)
                        ? R.string.new_repeating_transaction : R.string.edit_repeating_transaction);
            }
        }

        // Controls

        initializeViewHolder();

        initializeControls();

        // refresh user interface
        mCommon.onTransactionTypeChanged(mCommon.transactionEntity.getTransactionType());
        mCommon.showPayeeName();
        mCommon.displayCategoryName();
//        mCommon.displayTags(); allready in init

        showPaymentsLeft();

        mCommon.setDirty(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        mCommon.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuHelper helper = new MenuHelper(this, menu);
        helper.addSaveToolbarIcon();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                return onActionCancelClick();
            case MenuHelper.save:
                return onActionDoneClick();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(KEY_MODEL, Parcels.wrap(mCommon.transactionEntity));

        // update the state interface
        outState.putString(KEY_TO_ACCOUNT_NAME, mCommon.mToAccountName);
        outState.putString(KEY_TRANS_CODE, mCommon.getTransactionType());
        outState.putString(KEY_TRANS_STATUS, mCommon.transactionEntity.getStatus());
        outState.putString(KEY_PAYEE_NAME, mCommon.payeeName);
        outState.putString(KEY_CATEGORY_NAME, mCommon.categoryName);
        outState.putString(KEY_SUBCATEGORY_NAME, mCommon.subCategoryName);
        outState.putString(KEY_TRANS_NUMBER, mCommon.viewHolder.edtTransNumber.getText().toString());
        outState.putParcelable(KEY_SPLIT_TRANSACTION, Parcels.wrap(mCommon.mSplitTransactions));
        outState.putParcelable(KEY_SPLIT_TRANSACTION_DELETED, Parcels.wrap(mCommon.mSplitTransactionsDeleted));
        outState.putString(KEY_NOTES, String.valueOf(mCommon.viewHolder.edtNotes.getTag()));
    }

    @Override
    public boolean onActionCancelClick() {
        return mCommon.onActionCancelClick();
    }

    @Override
    public void onBackPressed() {
        if (!onActionCancelClick())
            super.onBackPressed();
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

        mCommon.onFinishedInputAmountDialog(id, event.amount);
    }

    @Subscribe
    public void onEvent(DialogPositiveClickedEvent event) {
        mCommon.confirmDeletingCategories();
    }

    @Subscribe
    public void onEvent(DialogNegativeClickedEvent event) {
        mCommon.cancelChangingTransactionToTransfer();
    }

    // Public

    /**
     * refresh the UI control Payments Left
     */
    public void showPaymentsLeft() {
        Recurrence recurrence = getRecurringTransaction().getRecurrence();

        // Recurrence label

        mViewHolder.recurrenceLabel.setText((recurrence == Recurrence.IN_X_DAYS) || (recurrence == Recurrence.IN_X_MONTHS)
                ? R.string.activates : R.string.occurs);

        // Payments Left header

        mViewHolder.paymentsLeftTextView.setVisibility(recurrence.getValue() > 0 ? View.VISIBLE : View.GONE);
        mViewHolder.paymentsLeftTextView.setText(recurrence.getValue() >= 11 ? R.string.activates : R.string.payments_left);

        // Payments Left input

        mViewHolder.paymentsLeftEditText.setVisibility(recurrence.getValue() > 0 ? View.VISIBLE : View.GONE);
        mViewHolder.paymentsLeftEditText.setHint(recurrence.getValue() >= 11 ? R.string.activates : R.string.payments_left);

        Long occurrences = getRecurringTransaction().getPaymentsLeft();
        if (occurrences == null) {
            occurrences = Constants.NOT_SET;
            getRecurringTransaction().setPaymentsLeft(Constants.NOT_SET);
        }
        String value = occurrences == Constants.NOT_SET
                ? "∞"
                : Long.toString(occurrences);
        mViewHolder.paymentsLeftEditText.setText(value);

//        if (mRecurringTransaction.getPaymentsLeft() != null && mRecurringTransaction.getPaymentsLeft() >= 0) {
//            mViewHolder.paymentsLeftEditText.setText(Integer.toString(mRecurringTransaction.getPaymentsLeft()));
//        }
    }

    // Private

    private RecurringTransaction getRecurringTransaction() {
        return (RecurringTransaction) mCommon.transactionEntity;
    }

    private void initializeControls() {
        // Payment Date
        initializePaymentDateSelector();

        // Account(s)
        mCommon.initAccountSelectors();

        // Transaction type
        mCommon.initTransactionTypeSelector();

        // status
        mCommon.initStatusSelector();

        // Payee
        mCommon.initPayeeControls();

        // Category
        mCommon.initCategoryControls(SplitRecurringCategory.class.getSimpleName());

        // Split Categories
        mCommon.initSplitCategories();

        // mark checked if there are existing split categories.
        boolean hasSplit = mCommon.hasSplitCategories();
        mCommon.setSplit(hasSplit);

        // Amount and total amount

        mCommon.initAmountSelectors();

        // transaction number
        mCommon.initTransactionNumberControls();

        // notes
        mCommon.initNotesControls();

        // Frequency

        Spinner spinFrequencies = findViewById(R.id.spinnerFrequencies);
        Spinner spinRecurringMode = findViewById(R.id.recurringMode);

        RecurringTransaction tx = (RecurringTransaction) mCommon.transactionEntity;
        int recurrence = tx.getRecurrenceInt().intValue();
        int recurrenceMode = 0;

        recurrenceMode =  recurrence / 100;
        recurrence = recurrence % 100;
/*
        if (recurrence >= 200) {
            recurrence = recurrence - 200;
            recurrenceMode = 2;
        } // set auto execute without user acknowledgement
        if (recurrence >= 100) {
            recurrence = recurrence - 100;
            recurrenceMode = 1;
        } // set auto execute on the next occurrence

 */
        spinFrequencies.setSelection(recurrence, true);
        spinFrequencies.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mCommon.setDirty(true);

                getRecurringTransaction().setRecurrence(position);
                showPaymentsLeft();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                getRecurringTransaction().setRecurrence(Constants.NOT_SET_INT);
                showPaymentsLeft();
            }
        });
        spinRecurringMode.setSelection(recurrenceMode, true);
        spinRecurringMode.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mCommon.setDirty(true);

                getRecurringTransaction().setRecurrenceMode(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                getRecurringTransaction().setRecurrenceMode(0);
            }

                                                  }
        );

        // Tag
        mCommon.initTagsControls();

        // color
        mCommon.initColorControls();

    }

    private void initializePaymentDateSelector() {
        if (mViewHolder.paymentDateTextView == null) return;

        final MmxDateTimeUtils dateUtils = new MmxDateTimeUtils();

        Date paymentDate = getRecurringTransaction().getPaymentDate();


        mViewHolder.paymentDateTextView.setText(dateUtils.format(paymentDate, "EEE, "+dateUtils.getUserDatePattern(this)));
//        mViewHolder.paymentDateTextView.setText(dateUtils.format(paymentDate, Constants.LONG_DATE_PATTERN));
//        mViewHolder.paymentDateTextView.setTag(paymentDate.toString(Constants.ISO_DATE_FORMAT));

        mViewHolder.paymentDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MmxDate dateTime = new MmxDate(getPaymentDate());

                DatePickerDialog.OnDateSetListener listener = (view, year, month, dayOfMonth) -> {
                    Date selectedDate = dateUtils.from(year, month, dayOfMonth);
                    setPaymentDate(selectedDate);
                };

                DatePickerDialog datePicker = new DatePickerDialog(
                        ScheduledTransactionEditActivity.this, // replace with the actual activity or context
                        listener,
                        dateTime.getYear(),
                        dateTime.getMonthOfYear() , // getMonth of year alresy return month -1
//                        dateTime.getMonthOfYear() - 1, // DatePickerDialog month is zero-based
                        dateTime.getDayOfMonth()
                );

                // Customize the DatePickerDialog if needed
                datePicker.show();
            }
        });

        // Icon
        UIHelper ui = new UIHelper(this);
//        mViewHolder.paymentDateTextView.setCompoundDrawablesWithIntrinsicBounds(ui.getIcon(FontAwesome.Icon.faw_calendar), null, null, null);

        mViewHolder.paymentPreviousDayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Date dateTime = new MmxDate(getPaymentDate()).minusDays(1).toDate();
                setPaymentDate(dateTime);
            }
        });

        mViewHolder.paymentNextDayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Date dateTime = new MmxDate(getPaymentDate()).plusDays(1).toDate();
                setPaymentDate(dateTime);
            }
        });
    }

    private RecurringTransaction initializeModel() {
        RecurringTransaction tx = RecurringTransaction.createInstance();

        Date today = new MmxDate().toDate();
        tx.setDueDate(today);
        tx.setPaymentDate(today);

        return tx;
    }

    private void initializeViewHolder() {
        // Controls need to be at the beginning as they are referenced throughout the code.
        mCommon.findControls(this);

        mViewHolder = new ScheduledTransactionViewHolder();

        // Due Date = date
        mCommon.initDateSelector();

        // Payment Date, next occurrence
        mViewHolder.paymentDateTextView = findViewById(R.id.paymentDateTextView);

        // Previous/Next day adjustment buttons for the Payment Day
        mViewHolder.paymentPreviousDayButton = findViewById(R.id.paymentPreviousDayButton);
        mViewHolder.paymentNextDayButton = findViewById(R.id.paymentNextDayButton);

        // Recurrence label
        mViewHolder.recurrenceLabel = findViewById(R.id.recurrenceLabel);

        // Payments Left label
        mViewHolder.paymentsLeftTextView = findViewById(R.id.textViewTimesRepeated);

        // Payments Left text input
        mViewHolder.paymentsLeftEditText = findViewById(R.id.editTextTimesRepeated);
    }

    /**
     * this method allows you to search the transaction data
     * @param recurringTransactionId transaction id
     * @return true if data selected, false nothing
     */
    private boolean loadRecurringTransaction(long recurringTransactionId) {
        ScheduledTransactionRepository repo = new ScheduledTransactionRepository(this);
        mCommon.transactionEntity = repo.load(recurringTransactionId);
        if (mCommon.transactionEntity == null) return false;

        // Read data.
        String transCode = getRecurringTransaction().getTransactionCode();
        mCommon.transactionEntity.setTransactionType(TransactionTypes.valueOf(transCode));

        // load split transactions only if no category selected.
        if (!mCommon.transactionEntity.hasCategory() && mCommon.mSplitTransactions == null) {
            RecurringTransactionService recurringTransaction = new RecurringTransactionService(recurringTransactionId, this);
            mCommon.mSplitTransactions = recurringTransaction.loadSplitTransactions();
        }

        AccountRepository accountRepository = new AccountRepository(this);
        mCommon.mToAccountName = accountRepository.loadName(mCommon.transactionEntity.getToAccountId());

        mCommon.loadPayeeName(mCommon.transactionEntity.getPayeeId());
        mCommon.loadCategoryName();

        // load Tags
        if (mCommon.transactionEntity.getTagLinks() == null ) {
            TaglinkRepository taglinkRepository = new TaglinkRepository(this);
            mCommon.transactionEntity.setTagLinks(taglinkRepository.loadByRef(recurringTransactionId, mCommon.transactionEntity.getTransactionModel()));
        }


        return true;
    }

    /**
     * validate data insert in activity
     *
     * @return validation result
     */
    private boolean validateData() {
        if (!mCommon.validateData()) return false;

        Core core = new Core(this);

        // Due Date is required
        if (TextUtils.isEmpty(getRecurringTransaction().getDueDateString())) {
            core.alert(R.string.due_date_required);
            return false;
        }

        if (TextUtils.isEmpty(mCommon.viewHolder.dateTextView.getText().toString())) {
            core.alert(R.string.error_next_occurrence_not_populate);

            return false;
        }

        // Payments Left must have a value
        if (getRecurringTransaction().getPaymentsLeft() == null) {
            core.alert(R.string.payments_left_required);
            return false;
        }
        return true;
    }

    private void collectDataFromUI() {
        String value;

                // Payment Date

//        DateTime dateTime = MmxJodaDateTimeUtils.from(mViewHolder.paymentDateTextView.getTag().toString());
//        mRecurringTransaction.setPaymentDate(dateTime);

                // Payments Left

                value = mViewHolder.paymentsLeftEditText.getText().toString();
        if (NumericHelper.isNumeric(value)) {
            long paymentsLeft = NumericHelper.toInt(value);
            getRecurringTransaction().setPaymentsLeft(paymentsLeft);
        } else {
            getRecurringTransaction().setPaymentsLeft(Constants.NOT_SET);
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

        // invalidate Cache for ScheduledTransactionForecastListServices
        ScheduledTransactionForecastListServices.destroyInstance();

        boolean isTransfer = mCommon.transactionEntity.getTransactionType().equals(TransactionTypes.Transfer);
        if (!isTransfer) {
            mCommon.resetTransfer();
        }

        // Transaction. Need the id for split categories.

        if (!saveTransaction()) return false;

        // Split Categories

        if (mCommon.convertOneSplitIntoRegularTransaction()) {
            saveTransaction();
        }

        if(!mCommon.isSplitSelected()) {
            // Delete any split categories if split is unchecked.
            mCommon.removeAllSplitCategories();
        }
        return saveSplitCategories();
    }

    private void restoreInstanceState(Bundle savedInstanceState) {
        // Restore the transaction entity.
        mCommon.transactionEntity = Parcels.unwrap(savedInstanceState.getParcelable(KEY_MODEL));

        mCommon.mToAccountName = savedInstanceState.getString(KEY_TO_ACCOUNT_NAME);
        mCommon.payeeName = savedInstanceState.getString(KEY_PAYEE_NAME);
        mCommon.categoryName = savedInstanceState.getString(KEY_CATEGORY_NAME);
        mCommon.subCategoryName = savedInstanceState.getString(KEY_SUBCATEGORY_NAME);
        mCommon.mSplitTransactions = Parcels.unwrap(savedInstanceState.getParcelable(KEY_SPLIT_TRANSACTION));
        mCommon.mSplitTransactionsDeleted = Parcels.unwrap(savedInstanceState.getParcelable(KEY_SPLIT_TRANSACTION_DELETED));

        // action
//        mIntentAction = savedInstanceState.getString(KEY_ACTION);
    }

    private boolean saveSplitCategories() {
        SplitScheduledCategoryRepository splitRepo = new SplitScheduledCategoryRepository(this);

        // deleted old split transaction
        if (mCommon.getDeletedSplitCategories().size() > 0) {
            if (!mCommon.deleteMarkedSplits(splitRepo)) return false;
        }

        // has split transaction
        boolean hasSplitCategories = mCommon.hasSplitCategories();
        if (hasSplitCategories) {
            TaglinkRepository taglinkRepository = new TaglinkRepository(this);
            for (ISplitTransaction item : mCommon.mSplitTransactions) {
                SplitRecurringCategory splitEntity = (SplitRecurringCategory) item;

                splitEntity.setTransId(mCommon.transactionEntity.getId());

                if (splitEntity.getId() == null || splitEntity.getId() == Constants.NOT_SET) {
                    // insert data
                    if (!splitRepo.insert(splitEntity)) {
                        Toast.makeText(getApplicationContext(), R.string.db_checking_insert_failed, Toast.LENGTH_SHORT).show();
                        Timber.w("Insert new split transaction failed!");
                        return false;
                    }
                } else {
                    // update data
                    if (!splitRepo.update(splitEntity)) {
                        Toast.makeText(getApplicationContext(), R.string.db_checking_update_failed, Toast.LENGTH_SHORT).show();
                        Timber.w("Update split transaction failed!");
                        return false;
                    }
                }

                // save tag for split
                taglinkRepository.saveAllFor(splitEntity.getTransactionModel(),
                        splitEntity.getId(),
                        splitEntity.getTagLinks());
            }
        }

        return true;
    }

    private boolean saveTransaction() {
        ScheduledTransactionRepository repo = new ScheduledTransactionRepository(this);

        if (!mCommon.transactionEntity.hasId()) {
            // insert
            mCommon.transactionEntity = repo.insert((RecurringTransaction) mCommon.transactionEntity);

            if (mCommon.transactionEntity.getId() == Constants.NOT_SET) {
                new Core(this).alert(R.string.db_checking_insert_failed);
                Timber.w("Insert new repeating transaction failed!");
                return false;
            }
        } else {
            // update
            if (!repo.update((RecurringTransaction) mCommon.transactionEntity)) {
                new Core(this).alert(R.string.db_checking_update_failed);
                Timber.w("Update repeating  transaction failed!");
                return false;
            }
        }

        // save TagLinks
        mCommon.saveTags();

        return true;
    }

    private Date getPaymentDate() {
        Date dateTime = getRecurringTransaction().getPaymentDate();
        if (dateTime == null) {
            dateTime = dateUtils.now();
            getRecurringTransaction().setPaymentDate(dateTime);
        }

        return dateTime;
    }

    private void setPaymentDate(Date dateTime) {
        mCommon.setDirty(true);

        getRecurringTransaction().setPaymentDate(dateTime);

//        String display = dateUtils.format(dateTime, Constants.LONG_DATE_PATTERN);
        String display = dateUtils.format(dateTime, "EEE, "+dateUtils.getUserDatePattern(this));
        mViewHolder.paymentDateTextView.setText(display);
    }
}
