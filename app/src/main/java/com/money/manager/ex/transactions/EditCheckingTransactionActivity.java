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
package com.money.manager.ex.transactions;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.common.events.AmountEnteredEvent;
import com.money.manager.ex.database.ISplitTransaction;
import com.money.manager.ex.datalayer.PayeeRepository;
import com.money.manager.ex.domainmodel.RecurringTransaction;
import com.money.manager.ex.domainmodel.SplitCategory;
import com.money.manager.ex.domainmodel.SplitRecurringCategory;
import com.money.manager.ex.servicelayer.CategoryService;
import com.money.manager.ex.servicelayer.PayeeService;
import com.money.manager.ex.servicelayer.RecurringTransactionService;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.datalayer.AccountRepository;
import com.money.manager.ex.database.ITransactionEntity;
import com.money.manager.ex.datalayer.AccountTransactionRepository;
import com.money.manager.ex.datalayer.RecurringTransactionRepository;
import com.money.manager.ex.datalayer.SplitCategoriesRepository;
import com.money.manager.ex.database.TablePayee;
import com.money.manager.ex.domainmodel.AccountTransaction;
import com.money.manager.ex.domainmodel.Payee;
import com.money.manager.ex.dropbox.DropboxHelper;
import com.money.manager.ex.common.BaseFragmentActivity;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.settings.PreferenceConstants;
import com.money.manager.ex.transactions.events.DialogNegativeClickedEvent;
import com.money.manager.ex.transactions.events.DialogPositiveClickedEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.parceler.Parcels;

import java.util.ArrayList;

import info.javaperformance.money.MoneyFactory;

/**
 * Activity for editing Checking Account Transaction
 */
public class EditCheckingTransactionActivity
    extends BaseFragmentActivity {

    // action type intent
    public String mIntentAction;
    public int mTransId = Constants.NOT_SET;

    // bill deposits
    public int mRecurringTransactionId = Constants.NOT_SET;

    private EditTransactionCommonFunctions mCommonFunctions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_checking_account_transaction);

        mCommonFunctions = new EditTransactionCommonFunctions(this, this, new AccountTransaction());

        DropboxHelper dropbox = DropboxHelper.getInstance();
        if (dropbox == null) {
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

        initializeInputControls();

        // refresh user interface
        mCommonFunctions.onTransactionTypeChange(mCommonFunctions.transactionType);
        mCommonFunctions.refreshPayeeName();
        mCommonFunctions.refreshCategoryName();
    }

    @Override
    protected void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mCommonFunctions.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);

        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save the state interface
        outState.putInt(EditTransactionActivityConstants.KEY_TRANS_ID, mTransId);
        outState.putInt(EditTransactionActivityConstants.KEY_ACCOUNT_ID, mCommonFunctions.transactionEntity.getAccountId());
        outState.putInt(EditTransactionActivityConstants.KEY_TO_ACCOUNT_ID, mCommonFunctions.toAccountId);
        outState.putString(EditTransactionActivityConstants.KEY_TO_ACCOUNT_NAME, mCommonFunctions.mToAccountName);
//        outState.putString(EditTransactionActivityConstants.KEY_TRANS_DATE, mCommonFunctions.mDate);
        outState.putString(EditTransactionActivityConstants.KEY_TRANS_CODE, mCommonFunctions.getTransactionType());
        outState.putString(EditTransactionActivityConstants.KEY_TRANS_STATUS, mCommonFunctions.status);
        outState.putString(EditTransactionActivityConstants.KEY_TRANS_TOTAMOUNT, mCommonFunctions.viewHolder.txtAmountTo.getTag().toString());
        outState.putString(EditTransactionActivityConstants.KEY_TRANS_AMOUNT, mCommonFunctions.viewHolder.txtAmount.getTag().toString());
        outState.putInt(EditTransactionActivityConstants.KEY_PAYEE_ID, mCommonFunctions.payeeId);
        outState.putString(EditTransactionActivityConstants.KEY_PAYEE_NAME, mCommonFunctions.payeeName);
        outState.putInt(EditTransactionActivityConstants.KEY_CATEGORY_ID, mCommonFunctions.transactionEntity.getCategoryId());
        outState.putString(EditTransactionActivityConstants.KEY_CATEGORY_NAME, mCommonFunctions.categoryName);
        outState.putInt(EditTransactionActivityConstants.KEY_SUBCATEGORY_ID, mCommonFunctions.transactionEntity.getSubcategoryId());
        outState.putString(EditTransactionActivityConstants.KEY_SUBCATEGORY_NAME, mCommonFunctions.subCategoryName);
        outState.putString(EditTransactionActivityConstants.KEY_TRANS_NUMBER, mCommonFunctions.edtTransNumber.getText().toString());
//        outState.putParcelableArrayList(EditTransactionActivityConstants.KEY_SPLIT_TRANSACTION, mCommonFunctions.mSplitTransactions);
        outState.putParcelable(EditTransactionActivityConstants.KEY_SPLIT_TRANSACTION,
                Parcels.wrap(mCommonFunctions.mSplitTransactions));
//        outState.putParcelableArrayList(EditTransactionActivityConstants.KEY_SPLIT_TRANSACTION_DELETED,
//                mCommonFunctions.mSplitTransactionsDeleted);
        outState.putParcelable(EditTransactionActivityConstants.KEY_SPLIT_TRANSACTION_DELETED,
                Parcels.wrap(mCommonFunctions.mSplitTransactionsDeleted));
        outState.putString(EditTransactionActivityConstants.KEY_NOTES, mCommonFunctions.edtNotes.getText().toString());
        // bill deposits
        outState.putInt(EditTransactionActivityConstants.KEY_BDID_ID, mRecurringTransactionId);
//        outState.putString(EditTransactionActivityConstants.KEY_NEXT_OCCURRENCE, mNextOccurrence);

        outState.putString(EditTransactionActivityConstants.KEY_ACTION, mIntentAction);
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
            setResult(RESULT_OK);
            finish();
            return true;
        } else {
            return false;
        }
    }

    // Events

    @Subscribe
    public void onEvent(AmountEnteredEvent event) {
        int id = Integer.parseInt(event.requestId);
        mCommonFunctions.onFinishedInputAmountDialog(id, event.amount);
    }

    /**
     * Handle user's confirmation to delete any Split Categories when switching to
     * Transfer transaction type.
     * @param event
     */
    @Subscribe
    public void onEvent(DialogPositiveClickedEvent event) {
        mCommonFunctions.confirmDeletingCategories();
    }

    @Subscribe
    public void onEvent(DialogNegativeClickedEvent event) {
        mCommonFunctions.cancelChangingTransactionToTransfer();
    }

    // Private

    private boolean createSplitCategoriesFromRecurringTransaction() {
        // check if category and sub-category are not set.
        if(!(mCommonFunctions.transactionEntity.getCategoryId() <= 0 && mCommonFunctions.transactionEntity.getSubcategoryId() <= 0)) return false;

        // Adding transactions to the split list will set the Split checkbox and the category name.

        // create split transactions
        RecurringTransactionService recurringTransaction = new RecurringTransactionService(mRecurringTransactionId, this);
        ArrayList<ISplitTransaction> splitTemplates = recurringTransaction.loadSplitTransactions();
        if(mCommonFunctions.mSplitTransactions == null) mCommonFunctions.mSplitTransactions = new ArrayList<>();

        // For each of the templates, create a new record.
        for(int i = 0; i <= splitTemplates.size() - 1; i++) {
            SplitRecurringCategory record = (SplitRecurringCategory) splitTemplates.get(i);

            SplitCategory newSplit = new SplitCategory();
            newSplit.setAmount(record.getAmount());
            newSplit.setCategoryId(record.getCategoryId());
            newSplit.setSubcategoryId(record.getSubcategoryId());

            mCommonFunctions.mSplitTransactions.add(newSplit);
        }

        return true;
    }

    private void initializeInputControls() {
        // Transaction Type
        mCommonFunctions.initTransactionTypeSelector();

        // account(s)
        mCommonFunctions.initAccountSelectors();

        // status
        mCommonFunctions.initStatusSelector();

        // Transaction date
        mCommonFunctions.initDateSelector();

        // Payee
        mCommonFunctions.initPayeeControls();

        // Category
        mCommonFunctions.initCategoryControls(SplitCategory.class.getSimpleName());

        // Split Categories
        mCommonFunctions.initSplitCategories();

        // mark checked if there are existing split categories.
        boolean hasSplit = mCommonFunctions.hasSplitCategories();
        mCommonFunctions.setSplit(hasSplit);

        // Amount and total amount
        mCommonFunctions.initAmountSelectors();

        // Transaction Number
        mCommonFunctions.initTransactionNumberControls();

        // notes
        mCommonFunctions.initNotesControls();
    }

    private boolean loadCheckingAccount(int transId, boolean duplicate) {
        try {
            return loadCheckingAccountInternal(transId, duplicate);
        } catch (Exception e) {
            ExceptionHandler handler = new ExceptionHandler(this, this);
            handler.handle(e, "loading checking account");
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
        AccountTransactionRepository repo = new AccountTransactionRepository(this);
        AccountTransaction tx = repo.load(transId);
        if (tx == null) return false;

        mCommonFunctions.transactionEntity = tx;

        if (!duplicate) {
            mTransId = tx.getId();
        }
//        mCommonFunctions.accountId = tx.getAccountId();
        Integer toAccountId = tx.getToAccountId();
        mCommonFunctions.toAccountId = toAccountId == null ? Constants.NOT_SET : toAccountId;
        mCommonFunctions.transactionType = tx.getTransType();
        mCommonFunctions.status = tx.getStatus();
        mCommonFunctions.transactionEntity.setAmount(tx.getAmount());
        mCommonFunctions.transactionEntity.setAmountTo(tx.getAmountTo());
        mCommonFunctions.payeeId = tx.getPayeeId();
        mCommonFunctions.transactionEntity.setCategoryId(tx.getCategoryId());
        mCommonFunctions.transactionEntity.setSubcategoryId(tx.getSubcategoryId());
        mCommonFunctions.mTransNumber = tx.getTransactionNumber();
        mCommonFunctions.mNotes = tx.getNotes();
//        if (!duplicate) {
//            mCommonFunctions.mDate = tx.getDateString();
//        }

        // Load Split Categories.
        if (mCommonFunctions.mSplitTransactions == null) {
            SplitCategoriesRepository splitRepo = new SplitCategoriesRepository(this);
            mCommonFunctions.mSplitTransactions = splitRepo.loadSplitCategoriesFor(transId);

            if (duplicate && (mCommonFunctions.mSplitTransactions != null)) {
                // Reset ids so that the transactions get inserted on save.
                for (ISplitTransaction split : mCommonFunctions.mSplitTransactions) {
                    split.setId(Constants.NOT_SET);
                }
            }
        }

        // convert status in uppercase string
        if (!TextUtils.isEmpty(mCommonFunctions.status)) {
            mCommonFunctions.status = mCommonFunctions.status.toUpperCase();
        }

        AccountRepository accountRepository = new AccountRepository(this);
        mCommonFunctions.mToAccountName = accountRepository.loadName(mCommonFunctions.toAccountId);

        mCommonFunctions.selectPayeeName(mCommonFunctions.payeeId);
        mCommonFunctions.displayCategoryName();

        return true;
    }

    private boolean loadRecurringTransaction(int recurringTransactionId) {
        try {
            return loadRecurringTransactionInternal(recurringTransactionId);
        } catch (RuntimeException ex) {
            ExceptionHandler handler = new ExceptionHandler(getApplicationContext(), this);
            handler.handle(ex, "loading recurring transaction");
            return false;
        }
    }

    /**
     * Loads a recurring transaction data when entering a recurring transaction.
     * @param recurringTransactionId Id of the recurring transaction.
     * @return A boolean indicating whether the operation was successful.
     */
    private boolean loadRecurringTransactionInternal(int recurringTransactionId) {
        RecurringTransactionRepository repo = new RecurringTransactionRepository(this);
        RecurringTransaction recurringTx = repo.load(recurringTransactionId);
        if (recurringTx == null) return false;

        // Copy properties from recurring transaction

        mCommonFunctions.transactionEntity.setDate(recurringTx.getPaymentDate());
        mCommonFunctions.transactionEntity.setAccountId(recurringTx.getAccountId());

        mCommonFunctions.toAccountId = recurringTx.getToAccountId();
        String transCode = recurringTx.getTransactionCode();
        mCommonFunctions.transactionType = TransactionTypes.valueOf(transCode);
        mCommonFunctions.status = recurringTx.getStatus();
        mCommonFunctions.transactionEntity.setAmount(recurringTx.getAmount());
        mCommonFunctions.transactionEntity.setAmountTo(recurringTx.getAmountTo());
        mCommonFunctions.payeeId = recurringTx.getPayeeId();
        mCommonFunctions.transactionEntity.setCategoryId(recurringTx.getCategoryId());
        mCommonFunctions.transactionEntity.setSubcategoryId(recurringTx.getSubcategoryId());
        mCommonFunctions.mTransNumber = recurringTx.getTransactionNumber();
        mCommonFunctions.mNotes = recurringTx.getNotes();

        AccountRepository accountRepository = new AccountRepository(this);
        mCommonFunctions.mToAccountName = accountRepository.loadName(mCommonFunctions.toAccountId);

        mCommonFunctions.selectPayeeName(mCommonFunctions.payeeId);
        mCommonFunctions.displayCategoryName();

        // handle splits
        createSplitCategoriesFromRecurringTransaction();

        return true;
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
            int accountId = intent.getIntExtra(EditTransactionActivityConstants.KEY_ACCOUNT_ID, Constants.NOT_SET);
            if (accountId != Constants.NOT_SET) {
                mCommonFunctions.transactionEntity.setAccountId(accountId);
            }

            // Edit transaction.

            if (mIntentAction != null && Intent.ACTION_EDIT.equals(mIntentAction)) {
                mTransId = intent.getIntExtra(EditTransactionActivityConstants.KEY_TRANS_ID, Constants.NOT_SET);
                // select data transaction
                loadCheckingAccount(mTransId, false);
            } else if (mIntentAction != null && Intent.ACTION_PASTE.equals(mIntentAction)) {
                // select data transaction
                loadCheckingAccount(intent.getIntExtra(EditTransactionActivityConstants.KEY_TRANS_ID, Constants.NOT_SET), true);
            } else {
                mRecurringTransactionId = intent.getIntExtra(EditTransactionActivityConstants.KEY_BDID_ID, Constants.NOT_SET);
                if (mRecurringTransactionId > Constants.NOT_SET) {
                    loadRecurringTransaction(mRecurringTransactionId);
                }
            }
        }

        // New transaction

        if (mIntentAction.equals(Intent.ACTION_INSERT)) {
            if (mCommonFunctions.status == null) {
                mCommonFunctions.status = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
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
                                mCommonFunctions.transactionEntity.setCategoryId(payee.getCategId());
                                mCommonFunctions.transactionEntity.setSubcategoryId(payee.getSubCategId());
                                // load category and subcategory name
                                mCommonFunctions.displayCategoryName();
                                return Boolean.TRUE;
                            }
                        } catch (Exception e) {
                            ExceptionHandler handler = new ExceptionHandler(EditCheckingTransactionActivity.this,
                                    EditCheckingTransactionActivity.this);
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

            // Select the default account if none set.
            AppSettings settings = new AppSettings(this);
            Integer defaultAccountId = settings.getGeneralSettings().getDefaultAccountId();
            if (defaultAccountId != null) {
                mCommonFunctions.transactionEntity.setAccountId(defaultAccountId);
                // selecting the account here won't work because the data has not been loaded.
//                if (mCommonFunctions.mAccountIdList.contains(defaultAccountId)) {
//                    int index = mCommonFunctions.mAccountIdList.indexOf(defaultAccountId);
//                    mCommonFunctions.viewHolder.spinAccount.setSelection(index);
//                }
            }
        }

        // set title
        getSupportActionBar().setTitle(Intent.ACTION_INSERT.equals(mIntentAction)
                ? R.string.new_transaction
                : R.string.edit_transaction);
    }

    /**
     * Get any parameters, if sent, when intent was raised. This is used when called
     * from Tasker or any external caller.
     * @param intent The intent received.
     */
    private void externalIntegration(Intent intent) {
        Uri data = intent.getData();
        if (data == null) return;

        IntentDataParameters parameters = IntentDataParameters.parseData(this, data);

        // transaction type
        mCommonFunctions.transactionType = parameters.transactionType;

        if (parameters.accountId > 0) {
            this.mCommonFunctions.transactionEntity.setAccountId(parameters.accountId);
        }
        mCommonFunctions.transactionEntity.setAmount(parameters.amount);
        // payee
        if (parameters.payeeId > 0) {
            this.mCommonFunctions.payeeId = parameters.payeeId;
            this.mCommonFunctions.payeeName = parameters.payeeName;
        } else {
            // create payee if it does not exist
            if (parameters.payeeName != null) {
                PayeeService payeeService = new PayeeService(this);
                Payee payee = payeeService.createNew(parameters.payeeName);
                mCommonFunctions.payeeId = payee.getId();
                mCommonFunctions.payeeName = payee.getName();
            }
        }

        // category
        if (parameters.categoryId > 0) {
            mCommonFunctions.transactionEntity.setCategoryId(parameters.categoryId);
            mCommonFunctions.categoryName = parameters.categoryName;
        } else {
            // No id sent.
            // create a category if it was sent but does not exist (id not found by the parser).
            if (parameters.categoryName != null) {
                CategoryService newCategory = new CategoryService(this);
                mCommonFunctions.transactionEntity.setCategoryId(newCategory.createNew(parameters.categoryName));
                mCommonFunctions.categoryName = parameters.categoryName;
            }
        }
    }

    private void retrieveValuesFromSavedInstanceState(Bundle savedInstanceState) {
        mTransId = savedInstanceState.getInt(EditTransactionActivityConstants.KEY_TRANS_ID);
        mCommonFunctions.transactionEntity.setAccountId(savedInstanceState.getInt(EditTransactionActivityConstants.KEY_ACCOUNT_ID));
        mCommonFunctions.toAccountId = savedInstanceState.getInt(EditTransactionActivityConstants.KEY_TO_ACCOUNT_ID);
        mCommonFunctions.mToAccountName = savedInstanceState.getString(EditTransactionActivityConstants.KEY_TO_ACCOUNT_NAME);
//        mCommonFunctions.mDate = savedInstanceState.getString(EditTransactionActivityConstants.KEY_TRANS_DATE);
        String transCode = savedInstanceState.getString(EditTransactionActivityConstants.KEY_TRANS_CODE);
        mCommonFunctions.transactionType = TransactionTypes.valueOf(transCode);
        mCommonFunctions.status = savedInstanceState.getString(EditTransactionActivityConstants.KEY_TRANS_STATUS);
        mCommonFunctions.transactionEntity.setAmount(
                MoneyFactory.fromString(
                        savedInstanceState.getString(EditTransactionActivityConstants.KEY_TRANS_AMOUNT)));
        mCommonFunctions.transactionEntity.setAmountTo(
            MoneyFactory.fromString(savedInstanceState.getString(EditTransactionActivityConstants.KEY_TRANS_TOTAMOUNT)));
        mCommonFunctions.payeeId = savedInstanceState.getInt(EditTransactionActivityConstants.KEY_PAYEE_ID);
        mCommonFunctions.payeeName = savedInstanceState.getString(EditTransactionActivityConstants.KEY_PAYEE_NAME);
        mCommonFunctions.transactionEntity.setCategoryId(savedInstanceState.getInt(EditTransactionActivityConstants.KEY_CATEGORY_ID));
        mCommonFunctions.categoryName = savedInstanceState.getString(EditTransactionActivityConstants.KEY_CATEGORY_NAME);
        mCommonFunctions.transactionEntity.setSubcategoryId(savedInstanceState.getInt(EditTransactionActivityConstants.KEY_SUBCATEGORY_ID));
        mCommonFunctions.subCategoryName = savedInstanceState.getString(EditTransactionActivityConstants.KEY_SUBCATEGORY_NAME);
        mCommonFunctions.mNotes = savedInstanceState.getString(EditTransactionActivityConstants.KEY_NOTES);
        mCommonFunctions.mTransNumber = savedInstanceState.getString(EditTransactionActivityConstants.KEY_TRANS_NUMBER);

//        mCommonFunctions.mSplitTransactions = savedInstanceState.getParcelableArrayList(EditTransactionActivityConstants.KEY_SPLIT_TRANSACTION);
        mCommonFunctions.mSplitTransactions = Parcels.unwrap(savedInstanceState.getParcelable(EditTransactionActivityConstants.KEY_SPLIT_TRANSACTION));

        mCommonFunctions.mSplitTransactionsDeleted = Parcels.unwrap(savedInstanceState.getParcelable(
                EditTransactionActivityConstants.KEY_SPLIT_TRANSACTION_DELETED));

        mRecurringTransactionId = savedInstanceState.getInt(EditTransactionActivityConstants.KEY_BDID_ID);
//        mNextOccurrence = savedInstanceState.getString(EditTransactionActivityConstants.KEY_NEXT_OCCURRENCE);
        // action
        mIntentAction = savedInstanceState.getString(EditTransactionActivityConstants.KEY_ACTION);
    }

    /**
     * update data into database
     *
     * @return true if update data successful
     */
    private boolean saveData() {
        if (!mCommonFunctions.validateData()) return false;

        boolean isTransfer = mCommonFunctions.transactionType.equals(TransactionTypes.Transfer);

        AccountTransactionRepository repo = new AccountTransactionRepository(this);
        AccountTransaction tx = new AccountTransaction();
        tx.contentValues = mCommonFunctions.getContentValues(isTransfer);
        tx.setId(this.mTransId);

        // Insert or update?
        if (mIntentAction.equals(Intent.ACTION_INSERT) || mIntentAction.equals(Intent.ACTION_PASTE)) {
            // insert
            tx = repo.insert(tx);

            int id = tx.getId();
            if (id == Constants.NOT_SET) {
                Toast.makeText(getApplicationContext(), R.string.db_checking_insert_failed, Toast.LENGTH_SHORT).show();
                Log.w(EditTransactionActivityConstants.LOGCAT, "Insert new transaction failed!");
                return false;
            }
            mTransId = id;
        } else {
            // update
            boolean updated = repo.update(tx);
            if (!updated) {
                Toast.makeText(getApplicationContext(), R.string.db_checking_update_failed, Toast.LENGTH_SHORT).show();
                Log.w(EditTransactionActivityConstants.LOGCAT, "Update transaction failed!");
                return false;
            }
        }

        // Split Categories

        if(!mCommonFunctions.isSplitSelected()) {
            // Delete any split categories if split is unchecked.
            mCommonFunctions.removeAllSplitCategories();
        }

        if (!saveSplitCategories()) {
            return false;
        }

        // update category and subcategory for the default payee
        if ((!isTransfer) && mCommonFunctions.hasPayee() && !mCommonFunctions.hasSplitCategories()) {
            PayeeRepository payeeRepository = new PayeeRepository(this);
            Payee payee = payeeRepository.load(mCommonFunctions.payeeId);

            payee.setCategoryId(mCommonFunctions.transactionEntity.getCategoryId());
            payee.setSubcategoryId(mCommonFunctions.transactionEntity.getSubcategoryId());

            boolean saved = payeeRepository.save(payee);
            if (!saved) {
                Toast.makeText(getApplicationContext(), R.string.db_payee_update_failed, Toast.LENGTH_SHORT).show();
                Log.w(EditTransactionActivityConstants.LOGCAT, "Update Payee with Id=" + Integer.toString(mCommonFunctions.payeeId) + " return <= 0");
            }
        }

        // Process recurring transaction.
        if (mRecurringTransactionId != Constants.NOT_SET) {
            RecurringTransactionService service = new RecurringTransactionService(mRecurringTransactionId, this);
            service.moveNextOccurrence();
        }

        return true;
    }

    private boolean saveSplitCategories() {
        // has split categories
        boolean hasSplitCategories = mCommonFunctions.hasSplitCategories();
        // update split transaction
        if (hasSplitCategories) {
            SplitCategoriesRepository splitRepo = new SplitCategoriesRepository(this);

            for (ISplitTransaction split : mCommonFunctions.mSplitTransactions) {
                SplitCategory entity = (SplitCategory) split;

                // do nothing if the split is marked for deletion.
                ArrayList<ISplitTransaction> deletedSplits = mCommonFunctions.getDeletedSplitCategories();
                if(deletedSplits.contains(split)) {
                    continue;
                }

                entity.setTransId(mTransId);

                if (entity.getId() == null || entity.getId() == Constants.NOT_SET) {
                    // insert data
                    if (!splitRepo.insert(entity)) {
                        Toast.makeText(getApplicationContext(), R.string.db_checking_insert_failed, Toast.LENGTH_SHORT).show();
                        Log.w(EditTransactionActivityConstants.LOGCAT, "Insert new split transaction failed!");
                        return false;
                    }
                } else {
                    // update data
                    if (!splitRepo.update(entity)) {
                        Toast.makeText(getApplicationContext(), R.string.db_checking_update_failed, Toast.LENGTH_SHORT).show();
                        Log.w(EditTransactionActivityConstants.LOGCAT, "Update split transaction failed!");
                        return false;
                    }
                }
            }
        }
        // deleted old split transaction
        if (mCommonFunctions.mSplitTransactionsDeleted != null && !mCommonFunctions.mSplitTransactionsDeleted.isEmpty()) {
            ContentValues values = new ContentValues();

            for (int i = 0; i < mCommonFunctions.mSplitTransactionsDeleted.size(); i++) {
                values.clear();

                values.put(SplitCategory.SPLITTRANSAMOUNT,
                        mCommonFunctions.mSplitTransactionsDeleted.get(i).getAmount().toString());

                SplitCategoriesRepository splitRepo = new SplitCategoriesRepository(this);
                // todo: use repo to delete the record.
                if (getContentResolver().delete(splitRepo.getUri(),
                        SplitCategory.SPLITTRANSID + "=?",
                        new String[]{Integer.toString(mCommonFunctions.mSplitTransactionsDeleted.get(i).getId())}) <= 0) {
                    Toast.makeText(getApplicationContext(), R.string.db_checking_update_failed, Toast.LENGTH_SHORT).show();
                    Log.w(EditTransactionActivityConstants.LOGCAT, "Delete split transaction failed!");
                    return false;
                }
            }
        }

        return true;
    }
}
