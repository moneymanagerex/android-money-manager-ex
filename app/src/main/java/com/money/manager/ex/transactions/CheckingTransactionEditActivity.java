/*
 * Copyright (C) 2012-2018 The Android Money Manager Ex Project Team
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

import static java.lang.Integer.parseInt;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import com.money.manager.ex.Constants;
import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.common.MmxBaseFragmentActivity;
import com.money.manager.ex.common.events.AmountEnteredEvent;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.MenuHelper;
import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.database.ISplitTransaction;
import com.money.manager.ex.database.ITransactionEntity;
import com.money.manager.ex.datalayer.AccountRepository;
import com.money.manager.ex.datalayer.AccountTransactionRepository;
import com.money.manager.ex.datalayer.PayeeRepository;
import com.money.manager.ex.datalayer.RecurringTransactionRepository;
import com.money.manager.ex.datalayer.SplitCategoriesRepository;
import com.money.manager.ex.domainmodel.AccountTransaction;
import com.money.manager.ex.domainmodel.Payee;
import com.money.manager.ex.domainmodel.RecurringTransaction;
import com.money.manager.ex.domainmodel.SplitCategory;
import com.money.manager.ex.domainmodel.SplitRecurringCategory;
import com.money.manager.ex.servicelayer.CategoryService;
import com.money.manager.ex.servicelayer.PayeeService;
import com.money.manager.ex.servicelayer.RecurringTransactionService;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.settings.PreferenceConstants;
import com.money.manager.ex.transactions.events.DialogNegativeClickedEvent;
import com.money.manager.ex.transactions.events.DialogPositiveClickedEvent;
import com.money.manager.ex.utils.MmxDate;
import com.squareup.sqlbrite.BriteDatabase;

import org.greenrobot.eventbus.Subscribe;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import icepick.State;
import info.javaperformance.money.MoneyFactory;
import timber.log.Timber;

/**
 * Activity for editing Checking Account Transaction
 */
public class CheckingTransactionEditActivity
        extends MmxBaseFragmentActivity {

    @State
    public String mIntentAction;

    // bill deposits
    public int mRecurringTransactionId = Constants.NOT_SET;

    @Inject
    BriteDatabase database;

    private EditTransactionCommonFunctions mCommon;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_checking_account_transaction);

        MmexApplication.getApp().iocComponent.inject(this);

        final ITransactionEntity model = AccountTransaction.create();
        mCommon = new EditTransactionCommonFunctions(this, model, database);

//        showStandardToolbarActions();
        mCommon.initializeToolbar();

        // restore state, if any.
        if ((null != savedInstanceState)) {
            restoreInstanceState(savedInstanceState);
        }

        // Controls need to be at the beginning as they are referenced throughout the code.
        mCommon.findControls(this);

        // manage intent
        if (null != getIntent()) {
            final boolean handled = handleIntent(savedInstanceState);
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
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        mCommon.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        super.onCreateOptionsMenu(menu);

        final MenuHelper helper = new MenuHelper(this, menu);
        helper.addSaveToolbarIcon();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
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
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the whole transaction.
        outState.putParcelable(EditTransactionActivityConstants.KEY_TRANSACTION_ENTITY,
                Parcels.wrap(mCommon.transactionEntity));

        // update the state interface
        outState.putString(EditTransactionActivityConstants.KEY_TO_ACCOUNT_NAME, mCommon.mToAccountName);
        outState.putString(EditTransactionActivityConstants.KEY_TRANS_CODE, mCommon.getTransactionType());
        outState.putString(EditTransactionActivityConstants.KEY_PAYEE_NAME, mCommon.payeeName);
        outState.putString(EditTransactionActivityConstants.KEY_CATEGORY_NAME, mCommon.categoryName);
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
    public void onEvent(final AmountEnteredEvent event) {
        final int id = parseInt(event.requestId);
        mCommon.onFinishedInputAmountDialog(id, event.amount);
    }

    /**
     * Handle user's confirmation to delete any Split Categories when switching to
     * Transfer transaction type.
     *
     * @param event
     */
    @Subscribe
    public void onEvent(final DialogPositiveClickedEvent event) {
        mCommon.confirmDeletingCategories();
    }

    @Subscribe
    public void onEvent(final DialogNegativeClickedEvent event) {
        mCommon.cancelChangingTransactionToTransfer();
    }

    /*
    // Private
    */

    private boolean createSplitCategoriesFromRecurringTransaction() {
        // check if category and sub-category are not set.
        if (!(0 >= mCommon.transactionEntity.getCategoryId())) return false;

        // Adding transactions to the split list will set the Split checkbox and the category name.

        // create split transactions
        final RecurringTransactionService recurringTransaction = new RecurringTransactionService(mRecurringTransactionId, this);
        final ArrayList<ISplitTransaction> splitTemplates = recurringTransaction.loadSplitTransactions();
        if (null == mCommon.mSplitTransactions) mCommon.mSplitTransactions = new ArrayList<>();

        // For each of the templates, create a new record.
        for (int i = 0; i <= splitTemplates.size() - 1; i++) {
            final SplitRecurringCategory record = (SplitRecurringCategory) splitTemplates.get(i);

            final SplitCategory newSplit = new SplitCategory();
            newSplit.setAmount(record.getAmount());
            newSplit.setCategoryId(record.getCategoryId());
            newSplit.setNotes(record.getNotes());

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
        if (null != mCommon.mSplitTransactions) {
            // Reset ids so that the transactions get inserted on update.
            for (final ISplitTransaction split : mCommon.mSplitTransactions) {
                split.setId(Constants.NOT_SET);
            }
        }

    }

    /**
     * Get any parameters, if sent, when intent was raised. This is used when called
     * from Tasker or any external caller.
     *
     * @param intent The intent received.
     */
    private boolean externalIntegration(final Intent intent) {
        final Uri data = intent.getData();
        if (null == data) return false;

        final IntentDataParameters parameters = IntentDataParameters.parseData(this, data);

        // current date
        mCommon.transactionEntity.setDate(new MmxDate().toDate());

        // transaction type
        mCommon.transactionEntity.setTransactionType(parameters.transactionType);

        if (0 < parameters.accountId) {
            mCommon.transactionEntity.setAccountId(parameters.accountId);
        }
        if (0 < parameters.accountToId) {
            mCommon.transactionEntity.setAccountToId(parameters.accountToId);
        }

        mCommon.transactionEntity.setAmount(parameters.amount);

        // transfer amount
        if (TransactionTypes.Transfer == parameters.transactionType) {
            if (null != parameters.amountTo) {
                mCommon.transactionEntity.setAmountTo(parameters.amountTo);
            } else {
                //convert the to amount from the both currency details
                final CurrencyService currencyService = new CurrencyService(this);
                final AccountRepository accountRepository = new AccountRepository(this);
                mCommon.transactionEntity.setAmountTo(currencyService.doCurrencyExchange(accountRepository.loadCurrencyIdFor(mCommon.transactionEntity.getAccountId()),
                        mCommon.transactionEntity.getAmount(),
                        accountRepository.loadCurrencyIdFor(mCommon.transactionEntity.getAccountToId())));
            }
        }

        // payee
        if (0 < parameters.payeeId) {
            mCommon.transactionEntity.setPayeeId(parameters.payeeId);
            mCommon.payeeName = parameters.payeeName;
        } else {
            // create payee if it does not exist
            if (null != parameters.payeeName) {
                final PayeeService payeeService = new PayeeService(this);
                final Payee payee = payeeService.createNew(parameters.payeeName);
                mCommon.transactionEntity.setPayeeId(payee.getId());
                mCommon.payeeName = payee.getName();
            }
        }

        // category
        if (0 < parameters.categoryId) {
            mCommon.transactionEntity.setCategoryId(parameters.categoryId);
            mCommon.categoryName = parameters.categoryName;
        } else {
            // No id sent. Create a category if it was sent but does not exist (id not found by the parser).
            if (null != parameters.categoryName && !parameters.categoryName.isEmpty()) {
                final CategoryService newCategory = new CategoryService(this);
                mCommon.transactionEntity.setCategoryId(newCategory.createNew(parameters.categoryName));
                mCommon.categoryName = parameters.categoryName;
            } else {
                // try to resolve the category from the payee
                mCommon.setCategoryFromPayee(mCommon.transactionEntity.getPayeeId());
            }
        }

        // notes
        mCommon.transactionEntity.setNotes(parameters.notes);

        // stop further handling if Silent Mode is requested
        return !parameters.isSilentMode || !saveData();
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
        final boolean hasSplit = mCommon.hasSplitCategories();
        mCommon.setSplit(hasSplit);

        // Amount and total amount
        mCommon.initAmountSelectors();

        // Transaction Number
        mCommon.initTransactionNumberControls();

        // notes
        mCommon.initNotesControls();
    }

    private boolean loadTransaction(final int transId) {
        final AccountTransactionRepository repo = new AccountTransactionRepository(this);
        final AccountTransaction tx = repo.load(transId);
        if (null == tx) return false;

        mCommon.transactionEntity = tx;

        // Load Split Categories.
        if (null == mCommon.mSplitTransactions) {
            final SplitCategoriesRepository splitRepo = new SplitCategoriesRepository(this);
            mCommon.mSplitTransactions = splitRepo.loadSplitCategoriesFor(transId);
        }

        final AccountRepository accountRepository = new AccountRepository(this);
        mCommon.mToAccountName = accountRepository.loadName(mCommon.transactionEntity.getAccountToId());

        mCommon.loadPayeeName(mCommon.transactionEntity.getPayeeId());
        mCommon.loadCategoryName();

        return true;
    }

    private boolean loadRecurringTransaction(final int recurringTransactionId) {
        try {
            return loadRecurringTransactionInternal(recurringTransactionId);
        } catch (final RuntimeException ex) {
            Timber.e(ex, "loading recurring transaction");
            return false;
        }
    }

    /**
     * Loads a recurring transaction data when entering a recurring transaction.
     *
     * @param recurringTransactionId Id of the recurring transaction.
     * @return A boolean indicating whether the operation was successful.
     */
    private boolean loadRecurringTransactionInternal(final int recurringTransactionId) {
        final RecurringTransactionRepository repo = new RecurringTransactionRepository(this);
        final RecurringTransaction recurringTx = repo.load(recurringTransactionId);
        if (null == recurringTx) return false;

        // Copy properties from recurring transaction

        mCommon.transactionEntity.setDate(recurringTx.getPaymentDate());
        mCommon.transactionEntity.setAccountId(recurringTx.getAccountId());
        mCommon.transactionEntity.setAccountToId(recurringTx.getToAccountId());

        final String transCode = recurringTx.getTransactionCode();
        mCommon.transactionEntity.setTransactionType(TransactionTypes.valueOf(transCode));
        mCommon.transactionEntity.setStatus(recurringTx.getStatus());
        mCommon.transactionEntity.setAmount(recurringTx.getAmount());
        mCommon.transactionEntity.setAmountTo(recurringTx.getAmountTo());
        mCommon.transactionEntity.setPayeeId(recurringTx.getPayeeId());
        mCommon.transactionEntity.setCategoryId(recurringTx.getCategoryId());
        mCommon.transactionEntity.setTransactionNumber(recurringTx.getTransactionNumber());
        mCommon.transactionEntity.setNotes(recurringTx.getNotes());

        final AccountRepository accountRepository = new AccountRepository(this);
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
     *
     * @param savedInstanceState parameters
     */
    private boolean handleIntent(final Bundle savedInstanceState) {
        final Intent intent = getIntent();
        mIntentAction = intent.getAction();

        if (null == mIntentAction) {
            Timber.w("no intent action passed to CheckingTransactionEditActivity e intent");
            return false;
        }

        if (null == savedInstanceState) {
            final int accountId = intent.getIntExtra(EditTransactionActivityConstants.KEY_ACCOUNT_ID, Constants.NOT_SET);
            if (Constants.NOT_SET != accountId) {
                mCommon.transactionEntity.setAccountId(accountId);
            }

            // Edit transaction.

            if (null != mIntentAction) {
                final int transactionId = intent.getIntExtra(EditTransactionActivityConstants.KEY_TRANS_ID, Constants.NOT_SET);

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
                        if (Constants.NOT_SET < mRecurringTransactionId) {
                            loadRecurringTransaction(mRecurringTransactionId);
                        }
                }
            }
        }

        // New transaction

        if (mIntentAction.equals(Intent.ACTION_INSERT)) {
            if (null == mCommon.transactionEntity.getStatus()) {
                final String defaultStatus = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                        .getString(getString(PreferenceConstants.PREF_DEFAULT_STATUS), "");
                mCommon.transactionEntity.setStatus(defaultStatus);
            }

            if ("L".equals(PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                    .getString(getString(PreferenceConstants.PREF_DEFAULT_PAYEE), "N"))) {
                final ExecutorService executor = Executors.newSingleThreadExecutor();

                executor.execute(() -> {
                    try {
                        final Core core = new Core(getApplicationContext());
                        final Payee payee = core.getLastPayeeUsed();
                        if (null != payee && Constants.NOT_SET == mCommon.transactionEntity.getPayeeId()) {
                            // get id payee and category
                            mCommon.transactionEntity.setPayeeId(payee.getId());
                            mCommon.payeeName = payee.getName();
                            mCommon.transactionEntity.setCategoryId(payee.getCategoryId());
                            // load category and subcategory name
                            mCommon.loadCategoryName();

                            runOnUiThread(() -> {
                                // refresh field
                                mCommon.showPayeeName();
                                mCommon.displayCategoryName();
                            });
                        }
                    } catch (final Exception e) {
                        Timber.e(e, "loading default payee");
                    }
                });

                // Remember to shutdown the executor when it's no longer needed
                executor.shutdown();
            }

            if (null != intent.getData()) {
                if (!externalIntegration(intent)) return false;
            } else //start activity from SMS Receiver Transaction
            {
                try {
                    Bundle extras = intent.getExtras();

                    if (null != extras &&
                            extras.getString(EditTransactionActivityConstants.KEY_TRANS_SOURCE)
                                    .contentEquals("SmsReceiverTransactions.java")) {
                        final AccountRepository accountRepository = new AccountRepository(this);

                        if (0 < Integer.parseInt(extras.getString(EditTransactionActivityConstants.KEY_ACCOUNT_ID))) {
                            mCommon.transactionEntity.setAccountId(parseInt(extras.getString(EditTransactionActivityConstants.KEY_ACCOUNT_ID)));
                            mCommon.transactionEntity.setAccountToId(parseInt(extras.getString(EditTransactionActivityConstants.KEY_TO_ACCOUNT_ID)));

                            //convert the to amount from the both currency details
                            final CurrencyService currencyService = new CurrencyService(this);
                            mCommon.transactionEntity.setAmountTo(currencyService.doCurrencyExchange(accountRepository.loadCurrencyIdFor(mCommon.transactionEntity.getAccountId()),
                                    mCommon.transactionEntity.getAmount(),
                                    accountRepository.loadCurrencyIdFor(mCommon.transactionEntity.getAccountToId())));

                        }

                        mCommon.transactionEntity.setTransactionType(TransactionTypes.valueOf(extras.getString(EditTransactionActivityConstants.KEY_TRANS_CODE)));
                        mCommon.transactionEntity.setAmount(MoneyFactory.fromString(extras.getString(EditTransactionActivityConstants.KEY_TRANS_AMOUNT)));

                        mCommon.transactionEntity.setTransactionNumber(extras.getString(EditTransactionActivityConstants.KEY_TRANS_NUMBER));
                        mCommon.transactionEntity.setNotes(extras.getString(EditTransactionActivityConstants.KEY_NOTES));
                        mCommon.transactionEntity.setDate(new MmxDate().toDate());

                        if (extras.getString(EditTransactionActivityConstants.KEY_PAYEE_NAME).isEmpty()) {
                            mCommon.payeeName = "";

                            if ("L".equals(PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                                    .getString(getString(PreferenceConstants.PREF_DEFAULT_PAYEE), "N"))) {
                                final Core core = new Core(this);
                                final Payee payee = core.getLastPayeeUsed();

                                if (null != payee) {
                                    mCommon.transactionEntity.setPayeeId(payee.getId());
                                    mCommon.payeeName = payee.getName();
                                    mCommon.setCategoryFromPayee(mCommon.transactionEntity.getPayeeId());
                                }
                            }
                        } else {
                            mCommon.transactionEntity.setPayeeId(parseInt(extras.getString(EditTransactionActivityConstants.KEY_PAYEE_ID)));
                            mCommon.payeeName = extras.getString(EditTransactionActivityConstants.KEY_PAYEE_NAME);
                            mCommon.setCategoryFromPayee(mCommon.transactionEntity.getPayeeId());
                        }

                        //keeping the Category or Sub from the intent if payee name is empty or not defaulted
                        if (mCommon.payeeName.isEmpty()) {
                            final String catID = extras.getString(EditTransactionActivityConstants.KEY_CATEGORY_ID);
                            if (!catID.isEmpty()) {
                                mCommon.transactionEntity.setCategoryId(parseInt(catID));
                            }

                            mCommon.loadCategoryName();
                        }

                        mCommon.mToAccountName = accountRepository.loadName(mCommon.transactionEntity.getAccountToId());
                        extras = null;
                    }
                } catch (final Exception e) {
                    Timber.e(e);
                    Toast.makeText(this, "MMEX: Bank Transaction Process EXCEPTION --> "
                            + e, Toast.LENGTH_LONG).show();
                }
            }

            // Select the default account if none set.
            final Integer account = mCommon.transactionEntity.getAccountId();
            if (null == account || Constants.NOT_SET == account) {
                final AppSettings settings = new AppSettings(this);
                final Integer defaultAccountId = settings.getGeneralSettings().getDefaultAccountId();
                if (null == defaultAccountId) {
                    // Show toast message.
                    new UIHelper(this).showToast(getString(R.string.default_account_not_set));
                    return false;
                } else {
                    mCommon.transactionEntity.setAccountId(defaultAccountId);
                }
            }
        }

        // set title
        if (null != getSupportActionBar()) {
            getSupportActionBar().setTitle(Intent.ACTION_INSERT.equals(mIntentAction)
                    ? R.string.new_transaction
                    : R.string.edit_transaction);
        }

        return true;
    }

    private void restoreInstanceState(final Bundle savedInstanceState) {
        final Parcelable parcelTx = savedInstanceState.getParcelable(EditTransactionActivityConstants.KEY_TRANSACTION_ENTITY);
        mCommon.transactionEntity = Parcels.unwrap(parcelTx);

        mCommon.mToAccountName = savedInstanceState.getString(EditTransactionActivityConstants.KEY_TO_ACCOUNT_NAME);
        mCommon.payeeName = savedInstanceState.getString(EditTransactionActivityConstants.KEY_PAYEE_NAME);
        mCommon.categoryName = savedInstanceState.getString(EditTransactionActivityConstants.KEY_CATEGORY_NAME);

        mCommon.mSplitTransactions = Parcels.unwrap(savedInstanceState.getParcelable(EditTransactionActivityConstants.KEY_SPLIT_TRANSACTION));

        mCommon.mSplitTransactionsDeleted = Parcels.unwrap(savedInstanceState.getParcelable(
                EditTransactionActivityConstants.KEY_SPLIT_TRANSACTION_DELETED));

        mRecurringTransactionId = savedInstanceState.getInt(EditTransactionActivityConstants.KEY_BDID_ID);

        // action
//        mIntentAction = savedInstanceState.getString(EditTransactionActivityConstants.KEY_ACTION);
    }

    /**
     * Save data to the database.
     *
     * @return true if update data successful
     */
    private boolean saveData() {
        if (!mCommon.validateData()) return false;

        final boolean isTransfer = mCommon.transactionEntity.getTransactionType() == TransactionTypes.Transfer;

        if (!isTransfer) {
            mCommon.resetTransfer();
        }

        // Transaction. Need the Id for split categories.

        if (!saveTransaction()) return false;

        // Split Categories

        if (mCommon.convertOneSplitIntoRegularTransaction()) {
            saveTransaction();
        }

        if (!mCommon.isSplitSelected()) {
            // Delete any split categories if split is unchecked.
            mCommon.removeAllSplitCategories();
        }
        if (!saveSplitCategories()) return false;

        // update category and subcategory for the default payee
        saveDefaultPayee(isTransfer);

        // Process recurring transaction.
        if (Constants.NOT_SET != mRecurringTransactionId) {
            final RecurringTransactionService service = new RecurringTransactionService(mRecurringTransactionId, this);
            service.moveNextOccurrence();
        }

        return true;
    }

    private void saveDefaultPayee(final boolean isTransfer) {
        if ((isTransfer) || !mCommon.hasPayee() || mCommon.hasSplitCategories()) {
            return;
        }
        if (null == mCommon.transactionEntity) return;

        final PayeeRepository payeeRepository = new PayeeRepository(this);
        final Payee payee = payeeRepository.load(mCommon.transactionEntity.getPayeeId());
        if (null == payee) return;

        payee.setCategoryId(mCommon.transactionEntity.getCategoryId());

        final boolean saved = payeeRepository.save(payee);
        if (!saved) {
            Toast.makeText(getApplicationContext(), R.string.db_payee_update_failed, Toast.LENGTH_SHORT).show();
            Timber.w("Update Payee with Id=%d return <= 0", mCommon.transactionEntity.getPayeeId());
        }

    }

    private boolean saveSplitCategories() {
        final Integer transactionId = mCommon.transactionEntity.getId();
        final SplitCategoriesRepository splitRepo = new SplitCategoriesRepository(this);
        final ArrayList<ISplitTransaction> deletedSplits = mCommon.getDeletedSplitCategories();

        // deleted old split transaction
        if (!deletedSplits.isEmpty()) {
            if (!mCommon.deleteMarkedSplits(splitRepo)) return false;
        }

        // update split transaction
        final boolean hasSplitCategories = mCommon.hasSplitCategories();
        if (hasSplitCategories) {
            for (final ISplitTransaction split : mCommon.mSplitTransactions) {
                final SplitCategory entity = (SplitCategory) split;

                // do nothing if the split is marked for deletion.
                if (deletedSplits.contains(split)) {
                    continue;
                }

                entity.setTransId(transactionId);

                if (null == entity.getId() || Constants.NOT_SET == entity.getId()) {
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
        final AccountTransactionRepository repo = new AccountTransactionRepository(this);

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
            final boolean updated = repo.update((AccountTransaction) mCommon.transactionEntity);
            if (!updated) {
                Toast.makeText(getApplicationContext(), R.string.db_checking_update_failed, Toast.LENGTH_SHORT).show();
                Timber.w("Update transaction failed!");
                return false;
            }
        }
        return true;
    }
}
