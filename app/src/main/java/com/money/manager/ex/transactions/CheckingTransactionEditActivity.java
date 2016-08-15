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

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.money.manager.ex.Constants;
import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.common.events.AmountEnteredEvent;
import com.money.manager.ex.database.ISplitTransaction;
import com.money.manager.ex.database.ITransactionEntity;
import com.money.manager.ex.database.MmexOpenHelper;
import com.money.manager.ex.datalayer.PayeeRepository;
import com.money.manager.ex.domainmodel.RecurringTransaction;
import com.money.manager.ex.domainmodel.SplitCategory;
import com.money.manager.ex.domainmodel.SplitRecurringCategory;
import com.money.manager.ex.servicelayer.CategoryService;
import com.money.manager.ex.servicelayer.PayeeService;
import com.money.manager.ex.servicelayer.RecurringTransactionService;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.log.ExceptionHandler;
import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.datalayer.AccountRepository;
import com.money.manager.ex.datalayer.AccountTransactionRepository;
import com.money.manager.ex.datalayer.RecurringTransactionRepository;
import com.money.manager.ex.datalayer.SplitCategoriesRepository;
import com.money.manager.ex.domainmodel.AccountTransaction;
import com.money.manager.ex.domainmodel.Payee;
import com.money.manager.ex.common.BaseFragmentActivity;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.settings.PreferenceConstants;
import com.money.manager.ex.transactions.events.DialogNegativeClickedEvent;
import com.money.manager.ex.transactions.events.DialogPositiveClickedEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.joda.time.DateTime;
import org.parceler.Parcels;

import java.util.ArrayList;

import javax.inject.Inject;

/**
 * Activity for editing Checking Account Transaction
 */
public class CheckingTransactionEditActivity
    extends BaseFragmentActivity {

    public String mIntentAction;

    // bill deposits
    public int mRecurringTransactionId = Constants.NOT_SET;

    @Inject MmexOpenHelper openHelper;

    private EditTransactionCommonFunctions mCommonFunctions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_checking_account_transaction);

        MoneyManagerApplication.getInstance().mainComponent.inject(this);

        showStandardToolbarActions();

        ITransactionEntity model = AccountTransaction.create();
        mCommonFunctions = new EditTransactionCommonFunctions(this, model, openHelper);

        // restore state, if any.
        if ((savedInstanceState != null)) {
            restoreInstanceState(savedInstanceState);
        }

        // Controls need to be at the beginning as they are referenced throughout the code.
        mCommonFunctions.findControls(this);

        // manage intent
        if (getIntent() != null) {
            boolean handled = handleIntent(savedInstanceState);
            if (!handled) {
                finish();
                return;
            }
        }

        initializeInputControls();

        // refresh user interface
        mCommonFunctions.onTransactionTypeChanged(mCommonFunctions.transactionEntity.getTransactionType());
        mCommonFunctions.showPayeeName();
        mCommonFunctions.displayCategoryName();
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

        // Save the whole transaction.
        outState.putParcelable(EditTransactionActivityConstants.KEY_TRANSACTION_ENTITY,
                Parcels.wrap(mCommonFunctions.transactionEntity));

        // save the state interface
        outState.putString(EditTransactionActivityConstants.KEY_TO_ACCOUNT_NAME, mCommonFunctions.mToAccountName);
        outState.putString(EditTransactionActivityConstants.KEY_TRANS_CODE, mCommonFunctions.getTransactionType());
        outState.putString(EditTransactionActivityConstants.KEY_PAYEE_NAME, mCommonFunctions.payeeName);
        outState.putString(EditTransactionActivityConstants.KEY_CATEGORY_NAME, mCommonFunctions.categoryName);
        outState.putString(EditTransactionActivityConstants.KEY_SUBCATEGORY_NAME, mCommonFunctions.subCategoryName);
        outState.putParcelable(EditTransactionActivityConstants.KEY_SPLIT_TRANSACTION,
                Parcels.wrap(mCommonFunctions.mSplitTransactions));
        outState.putParcelable(EditTransactionActivityConstants.KEY_SPLIT_TRANSACTION_DELETED,
                Parcels.wrap(mCommonFunctions.mSplitTransactionsDeleted));
        outState.putInt(EditTransactionActivityConstants.KEY_BDID_ID, mRecurringTransactionId);

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

    private void duplicateTransaction() {
        // Reset transaction id. To be inserted when the transaction is saved.
        mCommonFunctions.transactionEntity.setId(null);

        // Use today's date.
        mCommonFunctions.transactionEntity.setDate(DateTime.now());

        // Remove transaction id in split categories.
        if (mCommonFunctions.mSplitTransactions != null) {
            // Reset ids so that the transactions get inserted on save.
            for (ISplitTransaction split : mCommonFunctions.mSplitTransactions) {
                split.setId(Constants.NOT_SET);
            }
        }

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
        mCommonFunctions.transactionEntity.setTransactionType(parameters.transactionType);

        if (parameters.accountId > 0) {
            this.mCommonFunctions.transactionEntity.setAccountId(parameters.accountId);
        }
        mCommonFunctions.transactionEntity.setAmount(parameters.amount);
        // payee
        if (parameters.payeeId > 0) {
            this.mCommonFunctions.transactionEntity.setPayeeId(parameters.payeeId);
            this.mCommonFunctions.payeeName = parameters.payeeName;
        } else {
            // create payee if it does not exist
            if (parameters.payeeName != null) {
                PayeeService payeeService = new PayeeService(this);
                Payee payee = payeeService.createNew(parameters.payeeName);
                mCommonFunctions.transactionEntity.setPayeeId(payee.getId());
                mCommonFunctions.payeeName = payee.getName();
            }
        }

        // category
        if (parameters.categoryId > 0) {
            mCommonFunctions.transactionEntity.setCategoryId(parameters.categoryId);
            mCommonFunctions.categoryName = parameters.categoryName;
        } else {
            // No id sent. Create a category if it was sent but does not exist (id not found by the parser).
            if (parameters.categoryName != null) {
                CategoryService newCategory = new CategoryService(this);
                mCommonFunctions.transactionEntity.setCategoryId(newCategory.createNew(parameters.categoryName));
                mCommonFunctions.categoryName = parameters.categoryName;
            }
        }
    }

    private void initializeInputControls() {
        // Transaction date
        mCommonFunctions.initDateSelector();

        // Transaction Type
        mCommonFunctions.initTransactionTypeSelector();

        // status
        mCommonFunctions.initStatusSelector();

        // account(s)
        mCommonFunctions.initAccountSelectors();

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

    private boolean loadTransaction(int transId) {
        AccountTransactionRepository repo = new AccountTransactionRepository(this);
        AccountTransaction tx = repo.load(transId);
        if (tx == null) return false;

        mCommonFunctions.transactionEntity = tx;

        // Load Split Categories.
        if (mCommonFunctions.mSplitTransactions == null) {
            SplitCategoriesRepository splitRepo = new SplitCategoriesRepository(this);
            mCommonFunctions.mSplitTransactions = splitRepo.loadSplitCategoriesFor(transId);
        }

        AccountRepository accountRepository = new AccountRepository(this);
        mCommonFunctions.mToAccountName = accountRepository.loadName(mCommonFunctions.transactionEntity.getAccountToId());

        mCommonFunctions.loadPayeeName(mCommonFunctions.transactionEntity.getPayeeId());
        mCommonFunctions.loadCategoryName();

        return true;
    }

    private boolean loadRecurringTransaction(int recurringTransactionId) {
        try {
            return loadRecurringTransactionInternal(recurringTransactionId);
        } catch (RuntimeException ex) {
            ExceptionHandler handler = new ExceptionHandler(getApplicationContext(), this);
            handler.e(ex, "loading recurring transaction");
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
        mCommonFunctions.transactionEntity.setAccountToId(recurringTx.getToAccountId());

        String transCode = recurringTx.getTransactionCode();
        mCommonFunctions.transactionEntity.setTransactionType(TransactionTypes.valueOf(transCode));
        mCommonFunctions.transactionEntity.setStatus(recurringTx.getStatus());
        mCommonFunctions.transactionEntity.setAmount(recurringTx.getAmount());
        mCommonFunctions.transactionEntity.setAmountTo(recurringTx.getAmountTo());
        mCommonFunctions.transactionEntity.setPayeeId(recurringTx.getPayeeId());
        mCommonFunctions.transactionEntity.setCategoryId(recurringTx.getCategoryId());
        mCommonFunctions.transactionEntity.setSubcategoryId(recurringTx.getSubcategoryId());
        mCommonFunctions.transactionEntity.setTransactionNumber(recurringTx.getTransactionNumber());
        mCommonFunctions.transactionEntity.setNotes(recurringTx.getNotes());

        AccountRepository accountRepository = new AccountRepository(this);
        mCommonFunctions.mToAccountName = accountRepository.loadName(mCommonFunctions.transactionEntity.getAccountToId());

        mCommonFunctions.loadPayeeName(mCommonFunctions.transactionEntity.getPayeeId());
        mCommonFunctions.loadCategoryName();

        // e splits
        createSplitCategoriesFromRecurringTransaction();

        return true;
    }

    /**
     * Get the parameters from the intent (parameters sent from the caller).
     * Also used for Tasker integration, for example.
     * @param savedInstanceState parameters
     */
    private boolean handleIntent(Bundle savedInstanceState) {
        Intent intent = getIntent();
        mIntentAction = intent.getAction();

        if (mIntentAction == null) {
            ExceptionHandler.warn("no intent action passed to CheckingTransactionEditActivity e intent");
            return false;
        }

        if (savedInstanceState == null) {
            int accountId = intent.getIntExtra(EditTransactionActivityConstants.KEY_ACCOUNT_ID, Constants.NOT_SET);
            if (accountId != Constants.NOT_SET) {
                mCommonFunctions.transactionEntity.setAccountId(accountId);
            }

            // Edit transaction.

            if (mIntentAction != null) {
                int transactionId = intent.getIntExtra(EditTransactionActivityConstants.KEY_TRANS_ID, Constants.NOT_SET);

                switch (mIntentAction) {
                    case Intent.ACTION_EDIT:
                        loadTransaction(transactionId);
                        break;
                    case Intent.ACTION_PASTE:
                        // duplicate transaction
                        loadTransaction(transactionId);
                        duplicateTransaction();
                        break;
                    case Intent.ACTION_INSERT:
                        mRecurringTransactionId = intent.getIntExtra(EditTransactionActivityConstants.KEY_BDID_ID, Constants.NOT_SET);
                        if (mRecurringTransactionId > Constants.NOT_SET) {
                            loadRecurringTransaction(mRecurringTransactionId);
                        }
                }
            }
        }

        // New transaction

        if (mIntentAction.equals(Intent.ACTION_INSERT)) {
            if (mCommonFunctions.transactionEntity.getStatus() == null) {
                String defaultStatus = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                        .getString(getString(PreferenceConstants.PREF_DEFAULT_STATUS), "");
                mCommonFunctions.transactionEntity.setStatus(defaultStatus);
            }

            if ("L".equals(PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                    .getString(getString(PreferenceConstants.PREF_DEFAULT_PAYEE), "N"))) {
                AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
                    @Override
                    protected Boolean doInBackground(Void... params) {
                        try {
                            Core core = new Core(getApplicationContext());
                            Payee payee = core.getLastPayeeUsed();
                            if (payee != null && mCommonFunctions.transactionEntity.getPayeeId() == Constants.NOT_SET) {
                                // get id payee and category
                                mCommonFunctions.transactionEntity.setPayeeId(payee.getId());
                                mCommonFunctions.payeeName = payee.getName();
                                mCommonFunctions.transactionEntity.setCategoryId(payee.getCategoryId());
                                mCommonFunctions.transactionEntity.setSubcategoryId(payee.getSubcategoryId());
                                // load category and subcategory name
                                mCommonFunctions.loadCategoryName();
                                return Boolean.TRUE;
                            }
                        } catch (Exception e) {
                            ExceptionHandler handler = new ExceptionHandler(CheckingTransactionEditActivity.this,
                                    CheckingTransactionEditActivity.this);
                            handler.e(e, "loading default payee");
                        }
                        return Boolean.FALSE;
                    }

                    @Override
                    protected void onPostExecute(Boolean result) {
                        super.onPostExecute(result);
                        if (result) {
                            try {
                                // refresh field
                                mCommonFunctions.showPayeeName();
                                mCommonFunctions.displayCategoryName();
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
            Integer account = mCommonFunctions.transactionEntity.getAccountId();
            if (account == null || account == Constants.NOT_SET) {
                AppSettings settings = new AppSettings(this);
                Integer defaultAccountId = settings.getGeneralSettings().getDefaultAccountId();
                if (defaultAccountId == null) {
                    // Show toast message.
                    new ExceptionHandler(this).showMessage(getString(R.string.default_account_not_set));
                    return false;
                } else {
                    mCommonFunctions.transactionEntity.setAccountId(defaultAccountId);
                }
            }
        }

        // set title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(Intent.ACTION_INSERT.equals(mIntentAction)
                    ? R.string.new_transaction
                    : R.string.edit_transaction);
        }

        return true;
    }

    private void restoreInstanceState(Bundle savedInstanceState) {
        Parcelable parcelTx = savedInstanceState.getParcelable(EditTransactionActivityConstants.KEY_TRANSACTION_ENTITY);
        mCommonFunctions.transactionEntity = Parcels.unwrap(parcelTx);

        mCommonFunctions.mToAccountName = savedInstanceState.getString(EditTransactionActivityConstants.KEY_TO_ACCOUNT_NAME);
        mCommonFunctions.payeeName = savedInstanceState.getString(EditTransactionActivityConstants.KEY_PAYEE_NAME);
        mCommonFunctions.categoryName = savedInstanceState.getString(EditTransactionActivityConstants.KEY_CATEGORY_NAME);
        mCommonFunctions.subCategoryName = savedInstanceState.getString(EditTransactionActivityConstants.KEY_SUBCATEGORY_NAME);

        mCommonFunctions.mSplitTransactions = Parcels.unwrap(savedInstanceState.getParcelable(EditTransactionActivityConstants.KEY_SPLIT_TRANSACTION));

        mCommonFunctions.mSplitTransactionsDeleted = Parcels.unwrap(savedInstanceState.getParcelable(
                EditTransactionActivityConstants.KEY_SPLIT_TRANSACTION_DELETED));

        mRecurringTransactionId = savedInstanceState.getInt(EditTransactionActivityConstants.KEY_BDID_ID);

        // action
        mIntentAction = savedInstanceState.getString(EditTransactionActivityConstants.KEY_ACTION);
    }

    /**
     * Save data to the database.
     * @return true if update data successful
     */
    private boolean saveData() {
        if (!mCommonFunctions.validateData()) return false;

        boolean isTransfer = mCommonFunctions.transactionEntity.getTransactionType().equals(TransactionTypes.Transfer);

        if (!isTransfer) {
            mCommonFunctions.resetTransfer();
        }

        // Transaction. Need the Id for split categories.

        if (!saveTransaction()) return false;

        // Split Categories

        if (mCommonFunctions.convertOneSplitIntoRegularTransaction()) {
            saveTransaction();
        }

        if(!mCommonFunctions.isSplitSelected()) {
            // Delete any split categories if split is unchecked.
            mCommonFunctions.removeAllSplitCategories();
        }
        if (!saveSplitCategories()) return false;

        // update category and subcategory for the default payee
        saveDefaultPayee(isTransfer);

        // Process recurring transaction.
        if (mRecurringTransactionId != Constants.NOT_SET) {
            RecurringTransactionService service = new RecurringTransactionService(mRecurringTransactionId, this);
            service.moveNextOccurrence();
        }

        return true;
    }

    private void saveDefaultPayee(boolean isTransfer) {
        if ((isTransfer) || !mCommonFunctions.hasPayee() || mCommonFunctions.hasSplitCategories()) {
            return;
        }

        PayeeRepository payeeRepository = new PayeeRepository(this);
        Payee payee = payeeRepository.load(mCommonFunctions.transactionEntity.getPayeeId());

        payee.setCategoryId(mCommonFunctions.transactionEntity.getCategoryId());
        payee.setSubcategoryId(mCommonFunctions.transactionEntity.getSubcategoryId());

        boolean saved = payeeRepository.save(payee);
        if (!saved) {
            Toast.makeText(getApplicationContext(), R.string.db_payee_update_failed, Toast.LENGTH_SHORT).show();
            Log.w(EditTransactionActivityConstants.LOGCAT, "Update Payee with Id=" +
                    Integer.toString(mCommonFunctions.transactionEntity.getPayeeId()) + " return <= 0");
        }

    }

    private boolean saveSplitCategories() {
        Integer transactionId = mCommonFunctions.transactionEntity.getId();
        SplitCategoriesRepository splitRepo = new SplitCategoriesRepository(this);
        ArrayList<ISplitTransaction> deletedSplits = mCommonFunctions.getDeletedSplitCategories();

        // deleted old split transaction
        if (!deletedSplits.isEmpty()) {
            if (!mCommonFunctions.deleteMarkedSplits(splitRepo)) return false;
        }

        // update split transaction
        boolean hasSplitCategories = mCommonFunctions.hasSplitCategories();
        if (hasSplitCategories) {
            for (ISplitTransaction split : mCommonFunctions.mSplitTransactions) {
                SplitCategory entity = (SplitCategory) split;

                // do nothing if the split is marked for deletion.
                if(deletedSplits.contains(split)) {
                    continue;
                }

                entity.setTransId(transactionId);

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

        return true;
    }

    private boolean saveTransaction() {
        AccountTransactionRepository repo = new AccountTransactionRepository(this);

        if (!mCommonFunctions.transactionEntity.hasId()) {
            // insert
            mCommonFunctions.transactionEntity = repo.insert((AccountTransaction) mCommonFunctions.transactionEntity);

            if (!mCommonFunctions.transactionEntity.hasId()) {
                Toast.makeText(getApplicationContext(), R.string.db_checking_insert_failed, Toast.LENGTH_SHORT).show();
                Log.w(EditTransactionActivityConstants.LOGCAT, "Insert new transaction failed!");
                return false;
            }
        } else {
            // update
            boolean updated = repo.update((AccountTransaction) mCommonFunctions.transactionEntity);
            if (!updated) {
                Toast.makeText(getApplicationContext(), R.string.db_checking_update_failed, Toast.LENGTH_SHORT).show();
                Log.w(EditTransactionActivityConstants.LOGCAT, "Update transaction failed!");
                return false;
            }
        }
        return true;
    }
}
