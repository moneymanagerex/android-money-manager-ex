/*
 * Copyright (C) 2012-2019 The Android Money Manager Ex Project Team
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
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.money.manager.ex.Constants;
import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.common.MmxBaseFragmentActivity;
import com.money.manager.ex.common.events.AmountEnteredEvent;
import com.money.manager.ex.core.MenuHelper;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.database.ISplitTransaction;
import com.money.manager.ex.database.ITransactionEntity;
import com.money.manager.ex.datalayer.PayeeRepository;
import com.money.manager.ex.domainmodel.RecurringTransaction;
import com.money.manager.ex.domainmodel.SplitCategory;
import com.money.manager.ex.domainmodel.SplitRecurringCategory;
import com.money.manager.ex.servicelayer.CategoryService;
import com.money.manager.ex.servicelayer.PayeeService;
import com.money.manager.ex.servicelayer.RecurringTransactionService;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.datalayer.AccountRepository;
import com.money.manager.ex.datalayer.AccountTransactionRepository;
import com.money.manager.ex.datalayer.RecurringTransactionRepository;
import com.money.manager.ex.datalayer.SplitCategoriesRepository;
import com.money.manager.ex.domainmodel.AccountTransaction;
import com.money.manager.ex.domainmodel.Payee;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.settings.PreferenceConstants;
import com.money.manager.ex.transactions.events.DialogNegativeClickedEvent;
import com.money.manager.ex.transactions.events.DialogPositiveClickedEvent;
import com.money.manager.ex.utils.MmxDate;
import com.squareup.sqlbrite.BriteDatabase;

import org.greenrobot.eventbus.Subscribe;
import org.parceler.Parcels;

import java.util.ArrayList;

import javax.inject.Inject;

import icepick.State;
import info.javaperformance.money.MoneyFactory;
import timber.log.Timber;

import static java.lang.Integer.parseInt;

/**
 * Activity for editing Checking Account Transaction
 */
public class CheckingTransactionEditActivity
    extends MmxBaseFragmentActivity {

    @State public String mIntentAction;

    // bill deposits
    public int mRecurringTransactionId = Constants.NOT_SET;

    @Inject
    BriteDatabase database;

    private EditTransactionCommonFunctions mCommon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_checking_account_transaction);

        MmexApplication.getApp().iocComponent.inject(this);

        ITransactionEntity model = AccountTransaction.create();
        mCommon = new EditTransactionCommonFunctions(this, model, database);

//        showStandardToolbarActions();
        mCommon.initializeToolbar();

        // restore state, if any.
        if ((savedInstanceState != null)) {
            restoreInstanceState(savedInstanceState);
        }

        // Controls need to be at the beginning as they are referenced throughout the code.
        mCommon.findControls(this);

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
        mCommon.onTransactionTypeChanged(mCommon.transactionEntity.getTransactionType());
        mCommon.showPayeeName();
        mCommon.displayCategoryName();

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

        // Save the whole transaction.
        outState.putParcelable(EditTransactionActivityConstants.KEY_TRANSACTION_ENTITY,
                Parcels.wrap(mCommon.transactionEntity));

        // update the state interface
        outState.putString(EditTransactionActivityConstants.KEY_TO_ACCOUNT_NAME, mCommon.mToAccountName);
        outState.putString(EditTransactionActivityConstants.KEY_TRANS_CODE, mCommon.getTransactionType());
        outState.putString(EditTransactionActivityConstants.KEY_PAYEE_NAME, mCommon.payeeName);
        outState.putString(EditTransactionActivityConstants.KEY_CATEGORY_NAME, mCommon.categoryName);
        outState.putString(EditTransactionActivityConstants.KEY_SUBCATEGORY_NAME, mCommon.subCategoryName);
        outState.putParcelable(EditTransactionActivityConstants.KEY_SPLIT_TRANSACTION,
                Parcels.wrap(mCommon.mSplitTransactions));
        outState.putParcelable(EditTransactionActivityConstants.KEY_SPLIT_TRANSACTION_DELETED,
                Parcels.wrap(mCommon.mSplitTransactionsDeleted));
        outState.putInt(EditTransactionActivityConstants.KEY_BDID_ID, mRecurringTransactionId);

//        outState.putString(EditTransactionActivityConstants.KEY_ACTION, mIntentAction);
    }

    @Override
    public boolean onActionCancelClick() {
        return mCommon.onActionCancelClick();
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
        mCommon.onFinishedInputAmountDialog(id, event.amount);
    }

    /**
     * Handle user's confirmation to delete any Split Categories when switching to
     * Transfer transaction type.
     * @param event
     */
    @Subscribe
    public void onEvent(DialogPositiveClickedEvent event) {
        mCommon.confirmDeletingCategories();
    }

    @Subscribe
    public void onEvent(DialogNegativeClickedEvent event) {
        mCommon.cancelChangingTransactionToTransfer();
    }

    /*
    // Private
    */

    private boolean createSplitCategoriesFromRecurringTransaction() {
        // check if category and sub-category are not set.
        if(!(mCommon.transactionEntity.getCategoryId() <= 0 && mCommon.transactionEntity.getSubcategoryId() <= 0)) return false;

        // Adding transactions to the split list will set the Split checkbox and the category name.

        // create split transactions
        RecurringTransactionService recurringTransaction = new RecurringTransactionService(mRecurringTransactionId, this);
        ArrayList<ISplitTransaction> splitTemplates = recurringTransaction.loadSplitTransactions();
        if(mCommon.mSplitTransactions == null) mCommon.mSplitTransactions = new ArrayList<>();

        // For each of the templates, create a new record.
        for(int i = 0; i <= splitTemplates.size() - 1; i++) {
            SplitRecurringCategory record = (SplitRecurringCategory) splitTemplates.get(i);

            SplitCategory newSplit = new SplitCategory();
            newSplit.setAmount(record.getAmount());
            newSplit.setCategoryId(record.getCategoryId());
            newSplit.setSubcategoryId(record.getSubcategoryId());

            mCommon.mSplitTransactions.add(newSplit);
        }

        return true;
    }

    private void duplicateTransaction() {
        // Reset transaction id. To be inserted when the transaction is saved.
        mCommon.transactionEntity.setId(null);

        // Use today's date.
        mCommon.transactionEntity.setDate(new MmxDate().toDate());

        // Remove transaction id in split categories.
        if (mCommon.mSplitTransactions != null) {
            // Reset ids so that the transactions get inserted on update.
            for (ISplitTransaction split : mCommon.mSplitTransactions) {
                split.setId(Constants.NOT_SET);
            }
        }

    }

    /**
     * Get any parameters, if sent, when intent was raised. This is used when called
     * from Tasker or any external caller.
     * @param intent The intent received.
     */
    private boolean externalIntegration(Intent intent) {
        Uri data = intent.getData();
        if (data == null) return false;

        IntentDataParameters parameters = IntentDataParameters.parseData(this, data);

        // current date
        mCommon.transactionEntity.setDate(new MmxDate().toDate());

        // transaction type
        mCommon.transactionEntity.setTransactionType(parameters.transactionType);

        if (parameters.accountId > 0) {
            this.mCommon.transactionEntity.setAccountId(parameters.accountId);
        }
        if (parameters.accountToId > 0) {
            this.mCommon.transactionEntity.setAccountToId(parameters.accountToId);
        }

        mCommon.transactionEntity.setAmount(parameters.amount);

        // transfer amount
        if (parameters.transactionType == TransactionTypes.Transfer){
            if (parameters.amountTo != null){
                mCommon.transactionEntity.setAmountTo(parameters.amountTo);
            } else {
                //convert the to amount from the both currency details
                CurrencyService currencyService = new CurrencyService(this);
                AccountRepository accountRepository = new AccountRepository(this);
                mCommon.transactionEntity.setAmountTo(currencyService.doCurrencyExchange(accountRepository.loadCurrencyIdFor(mCommon.transactionEntity.getAccountId()),
                        mCommon.transactionEntity.getAmount(),
                        accountRepository.loadCurrencyIdFor(mCommon.transactionEntity.getAccountToId())));
            }
        }

        // payee
        if (parameters.payeeId > 0) {
            this.mCommon.transactionEntity.setPayeeId(parameters.payeeId);
            this.mCommon.payeeName = parameters.payeeName;
        } else {
            // create payee if it does not exist
            if (parameters.payeeName != null) {
                PayeeService payeeService = new PayeeService(this);
                Payee payee = payeeService.createNew(parameters.payeeName);
                mCommon.transactionEntity.setPayeeId(payee.getId());
                mCommon.payeeName = payee.getName();
            }
        }

        // category
        if (parameters.categoryId > 0) {
            mCommon.transactionEntity.setCategoryId(parameters.categoryId);
            mCommon.categoryName = parameters.categoryName;
        } else {
            // No id sent. Create a category if it was sent but does not exist (id not found by the parser).
            if (parameters.categoryName != null && !parameters.categoryName.isEmpty()) {
                CategoryService newCategory = new CategoryService(this);
                mCommon.transactionEntity.setCategoryId(newCategory.createNew(parameters.categoryName));
                mCommon.categoryName = parameters.categoryName;
            } else {
                // try to resolve the category from the payee
                mCommon.setCategoryFromPayee(mCommon.transactionEntity.getPayeeId());
            }
        }

        // subcategory
        if (parameters.subcategoryId > 0) {
            mCommon.transactionEntity.setSubcategoryId(parameters.subcategoryId);
            mCommon.subCategoryName = parameters.subcategoryName;
        } else {
            // No id sent. Create a subcategory if it was sent but does not exist (id not found by the parser).
            if (parameters.subcategoryName != null && !parameters.subcategoryName.isEmpty()) {
                CategoryService categorySvc = new CategoryService(this);
                mCommon.transactionEntity.setSubcategoryId(categorySvc.createNewSubcategory(parameters.subcategoryName, mCommon.transactionEntity.getCategoryId()));
                mCommon.subCategoryName = parameters.subcategoryName;
            }
        }

        // notes
        mCommon.transactionEntity.setNotes(parameters.notes);

        // stop further handling if Silent Mode is requested
        if (parameters.isSilentMode && saveData()) return false;

        return true;
    }

    private void initializeInputControls() {
        // Transaction Type
        mCommon.initTransactionTypeSelector();
        // status
        mCommon.initStatusSelector();

        // Transaction date
        mCommon.initDateSelector();

        // account(s)
        mCommon.initAccountSelectors();

        // Payee
        mCommon.initPayeeControls();

        // Category
        mCommon.initCategoryControls(SplitCategory.class.getSimpleName());

        // Split Categories
        mCommon.initSplitCategories();

        // mark checked if there are existing split categories.
        boolean hasSplit = mCommon.hasSplitCategories();
        mCommon.setSplit(hasSplit);

        // Amount and total amount
        mCommon.initAmountSelectors();

        // Transaction Number
        mCommon.initTransactionNumberControls();

        // notes
        mCommon.initNotesControls();
    }

    private boolean loadTransaction(int transId) {
        AccountTransactionRepository repo = new AccountTransactionRepository(this);
        AccountTransaction tx = repo.load(transId);
        if (tx == null) return false;

        mCommon.transactionEntity = tx;

        // Load Split Categories.
        if (mCommon.mSplitTransactions == null) {
            SplitCategoriesRepository splitRepo = new SplitCategoriesRepository(this);
            mCommon.mSplitTransactions = splitRepo.loadSplitCategoriesFor(transId);
        }

        AccountRepository accountRepository = new AccountRepository(this);
        mCommon.mToAccountName = accountRepository.loadName(mCommon.transactionEntity.getAccountToId());

        mCommon.loadPayeeName(mCommon.transactionEntity.getPayeeId());
        mCommon.loadCategoryName();

        return true;
    }

    private boolean loadRecurringTransaction(int recurringTransactionId) {
        try {
            return loadRecurringTransactionInternal(recurringTransactionId);
        } catch (RuntimeException ex) {
            Timber.e(ex, "loading recurring transaction");
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

        mCommon.transactionEntity.setDate(recurringTx.getPaymentDate());
        mCommon.transactionEntity.setAccountId(recurringTx.getAccountId());
        mCommon.transactionEntity.setAccountToId(recurringTx.getToAccountId());

        String transCode = recurringTx.getTransactionCode();
        mCommon.transactionEntity.setTransactionType(TransactionTypes.valueOf(transCode));
        mCommon.transactionEntity.setStatus(recurringTx.getStatus());
        mCommon.transactionEntity.setAmount(recurringTx.getAmount());
        mCommon.transactionEntity.setAmountTo(recurringTx.getAmountTo());
        mCommon.transactionEntity.setPayeeId(recurringTx.getPayeeId());
        mCommon.transactionEntity.setCategoryId(recurringTx.getCategoryId());
        mCommon.transactionEntity.setSubcategoryId(recurringTx.getSubcategoryId());
        mCommon.transactionEntity.setTransactionNumber(recurringTx.getTransactionNumber());
        mCommon.transactionEntity.setNotes(recurringTx.getNotes());

        AccountRepository accountRepository = new AccountRepository(this);
        mCommon.mToAccountName = accountRepository.loadName(mCommon.transactionEntity.getAccountToId());

        mCommon.loadPayeeName(mCommon.transactionEntity.getPayeeId());
        mCommon.loadCategoryName();

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
            Timber.w("no intent action passed to CheckingTransactionEditActivity e intent");
            return false;
        }

        if (savedInstanceState == null) {
            int accountId = intent.getIntExtra(EditTransactionActivityConstants.KEY_ACCOUNT_ID, Constants.NOT_SET);
            if (accountId != Constants.NOT_SET) {
                mCommon.transactionEntity.setAccountId(accountId);
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
            if (mCommon.transactionEntity.getStatus() == null) {
                String defaultStatus = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                        .getString(getString(PreferenceConstants.PREF_DEFAULT_STATUS), "");
                mCommon.transactionEntity.setStatus(defaultStatus);
            }

            if ("L".equals(PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                    .getString(getString(PreferenceConstants.PREF_DEFAULT_PAYEE), "N"))) {
                AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
                    @Override
                    protected Boolean doInBackground(Void... params) {
                        try {
                            Core core = new Core(getApplicationContext());
                            Payee payee = core.getLastPayeeUsed();
                            if (payee != null && mCommon.transactionEntity.getPayeeId() == Constants.NOT_SET) {
                                // get id payee and category
                                mCommon.transactionEntity.setPayeeId(payee.getId());
                                mCommon.payeeName = payee.getName();
                                mCommon.transactionEntity.setCategoryId(payee.getCategoryId());
                                mCommon.transactionEntity.setSubcategoryId(payee.getSubcategoryId());
                                // load category and subcategory name
                                mCommon.loadCategoryName();
                                return Boolean.TRUE;
                            }
                        } catch (Exception e) {
                            Timber.e(e, "loading default payee");
                        }
                        return Boolean.FALSE;
                    }

                    @Override
                    protected void onPostExecute(Boolean result) {
                        super.onPostExecute(result);
                        if (result) {
                            try {
                                // refresh field
                                mCommon.showPayeeName();
                                mCommon.displayCategoryName();
                            } catch (Exception e) {
                                Timber.e(e, "showing payee and category names");
                            }
                        }
                    }
                };
                task.execute();
            }

            if (intent.getData() != null) {
                if (!externalIntegration(intent)) return false;
            }
            else //start activity from SMS Receiver Transaction
            {
                try
                {
                    Bundle extras = intent.getExtras();

                    if(extras != null &&
                            extras.getString(EditTransactionActivityConstants.KEY_TRANS_SOURCE)
                                    .contentEquals("SmsReceiverTransactions.java"))
                    {
                        AccountRepository accountRepository = new AccountRepository(this);

                        if(Integer.parseInt(extras.getString(EditTransactionActivityConstants.KEY_ACCOUNT_ID)) > 0)
                        {
                            mCommon.transactionEntity.setAccountId(Integer.parseInt(extras.getString(EditTransactionActivityConstants.KEY_ACCOUNT_ID)));
                            mCommon.transactionEntity.setAccountToId(Integer.parseInt(extras.getString(EditTransactionActivityConstants.KEY_TO_ACCOUNT_ID)));

                            //convert the to amount from the both currency details
                            CurrencyService currencyService = new CurrencyService(this);
                            mCommon.transactionEntity.setAmountTo(currencyService.doCurrencyExchange(accountRepository.loadCurrencyIdFor(mCommon.transactionEntity.getAccountId()),
                                    mCommon.transactionEntity.getAmount(),
                                    accountRepository.loadCurrencyIdFor(mCommon.transactionEntity.getAccountToId())));

                        }

                        mCommon.transactionEntity.setTransactionType(TransactionTypes.valueOf(extras.getString(EditTransactionActivityConstants.KEY_TRANS_CODE)));
                        mCommon.transactionEntity.setAmount(MoneyFactory.fromString(extras.getString(EditTransactionActivityConstants.KEY_TRANS_AMOUNT)));

                        mCommon.transactionEntity.setTransactionNumber(extras.getString(EditTransactionActivityConstants.KEY_TRANS_NUMBER));
                        mCommon.transactionEntity.setNotes(extras.getString(EditTransactionActivityConstants.KEY_NOTES));
                        mCommon.transactionEntity.setDate(new MmxDate().toDate());

                        if (extras.getString(EditTransactionActivityConstants.KEY_PAYEE_NAME).isEmpty())
                        {
                            mCommon.payeeName="";

                            if("L".equals(PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                                    .getString(getString(PreferenceConstants.PREF_DEFAULT_PAYEE), "N"))) {
                                Core core = new Core(this);
                                Payee payee = core.getLastPayeeUsed();

                                if (payee != null) {
                                    mCommon.transactionEntity.setPayeeId(payee.getId());
                                    mCommon.payeeName = payee.getName();
                                    mCommon.setCategoryFromPayee(mCommon.transactionEntity.getPayeeId());
                                }
                            }
                        }
                        else
                        {
                            mCommon.transactionEntity.setPayeeId(Integer.parseInt(extras.getString(EditTransactionActivityConstants.KEY_PAYEE_ID)));
                            mCommon.payeeName = extras.getString(EditTransactionActivityConstants.KEY_PAYEE_NAME);
                            mCommon.setCategoryFromPayee(mCommon.transactionEntity.getPayeeId());
                        }

                        //keeping the Category or Sub from the intent if payee name is empty or not defaulted
                        if(mCommon.payeeName.isEmpty())
                        {
                            String catID = extras.getString(EditTransactionActivityConstants.KEY_CATEGORY_ID);
                            String subCatID = extras.getString(EditTransactionActivityConstants.KEY_SUBCATEGORY_ID);

                            if (!catID.isEmpty()) { mCommon.transactionEntity.setCategoryId(parseInt(catID)); }

                            if (!subCatID.isEmpty()) { mCommon.transactionEntity.setSubcategoryId(parseInt(subCatID)); }

                            mCommon.loadCategoryName();
                        }

                        mCommon.mToAccountName = accountRepository.loadName(mCommon.transactionEntity.getAccountToId());
                        extras = null;
                    }
                }
                catch(Exception e)
                {
                    Timber.e(e);
                    Toast.makeText(this, "MMEX: Bank Transaction Process EXCEPTION --> "
                            +  e, Toast.LENGTH_LONG).show();
                }
            }

            // Select the default account if none set.
            Integer account = mCommon.transactionEntity.getAccountId();
            if (account == null || account == Constants.NOT_SET) {
                AppSettings settings = new AppSettings(this);
                Integer defaultAccountId = settings.getGeneralSettings().getDefaultAccountId();
                if (defaultAccountId == null) {
                    // Show toast message.
                    new UIHelper(this).showToast(getString(R.string.default_account_not_set));
                    return false;
                } else {
                    mCommon.transactionEntity.setAccountId(defaultAccountId);
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
        mCommon.transactionEntity = Parcels.unwrap(parcelTx);

        mCommon.mToAccountName = savedInstanceState.getString(EditTransactionActivityConstants.KEY_TO_ACCOUNT_NAME);
        mCommon.payeeName = savedInstanceState.getString(EditTransactionActivityConstants.KEY_PAYEE_NAME);
        mCommon.categoryName = savedInstanceState.getString(EditTransactionActivityConstants.KEY_CATEGORY_NAME);
        mCommon.subCategoryName = savedInstanceState.getString(EditTransactionActivityConstants.KEY_SUBCATEGORY_NAME);

        mCommon.mSplitTransactions = Parcels.unwrap(savedInstanceState.getParcelable(EditTransactionActivityConstants.KEY_SPLIT_TRANSACTION));

        mCommon.mSplitTransactionsDeleted = Parcels.unwrap(savedInstanceState.getParcelable(
                EditTransactionActivityConstants.KEY_SPLIT_TRANSACTION_DELETED));

        mRecurringTransactionId = savedInstanceState.getInt(EditTransactionActivityConstants.KEY_BDID_ID);

        // action
//        mIntentAction = savedInstanceState.getString(EditTransactionActivityConstants.KEY_ACTION);
    }

    /**
     * Save data to the database.
     * @return true if update data successful
     */
    private boolean saveData() {
        if (!mCommon.validateData()) return false;

        boolean isTransfer = mCommon.transactionEntity.getTransactionType().equals(TransactionTypes.Transfer);

        if (!isTransfer) {
            mCommon.resetTransfer();
        }

        // Transaction. Need the Id for split categories.

        if (!saveTransaction()) return false;

        // Split Categories

        if (mCommon.convertOneSplitIntoRegularTransaction()) {
            saveTransaction();
        }

        if(!mCommon.isSplitSelected()) {
            // Delete any split categories if split is unchecked.
            mCommon.removeAllSplitCategories();
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
        if ((isTransfer) || !mCommon.hasPayee() || mCommon.hasSplitCategories()) {
            return;
        }
        if (mCommon.transactionEntity == null) return;

        PayeeRepository payeeRepository = new PayeeRepository(this);
        Payee payee = payeeRepository.load(mCommon.transactionEntity.getPayeeId());
        if (payee == null) return;

        payee.setCategoryId(mCommon.transactionEntity.getCategoryId());
        payee.setSubcategoryId(mCommon.transactionEntity.getSubcategoryId());

        boolean saved = payeeRepository.save(payee);
        if (!saved) {
            Toast.makeText(getApplicationContext(), R.string.db_payee_update_failed, Toast.LENGTH_SHORT).show();
            Timber.w("Update Payee with Id=%d return <= 0", mCommon.transactionEntity.getPayeeId());
        }

    }

    private boolean saveSplitCategories() {
        Integer transactionId = mCommon.transactionEntity.getId();
        SplitCategoriesRepository splitRepo = new SplitCategoriesRepository(this);
        ArrayList<ISplitTransaction> deletedSplits = mCommon.getDeletedSplitCategories();

        // deleted old split transaction
        if (!deletedSplits.isEmpty()) {
            if (!mCommon.deleteMarkedSplits(splitRepo)) return false;
        }

        // update split transaction
        boolean hasSplitCategories = mCommon.hasSplitCategories();
        if (hasSplitCategories) {
            for (ISplitTransaction split : mCommon.mSplitTransactions) {
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
                        Timber.w("Insert new split transaction failed!");
                        return false;
                    }
                } else {
                    // update data
                    if (!splitRepo.update(entity)) {
                        Toast.makeText(getApplicationContext(), R.string.db_checking_update_failed, Toast.LENGTH_SHORT).show();
                        Timber.w("Update split transaction failed!");
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private boolean saveTransaction() {
        AccountTransactionRepository repo = new AccountTransactionRepository(this);

        if (!mCommon.transactionEntity.hasId()) {
            // insert
            mCommon.transactionEntity = repo.insert((AccountTransaction) mCommon.transactionEntity);

            if (!mCommon.transactionEntity.hasId()) {
                Toast.makeText(getApplicationContext(), R.string.db_checking_insert_failed, Toast.LENGTH_SHORT).show();
                Timber.w("Insert new transaction failed!");
                return false;
            }
        } else {
            // update
            boolean updated = repo.update((AccountTransaction) mCommon.transactionEntity);
            if (!updated) {
                Toast.makeText(getApplicationContext(), R.string.db_checking_update_failed, Toast.LENGTH_SHORT).show();
                Timber.w("Update transaction failed!");
                return false;
            }
        }
        return true;
    }
}
