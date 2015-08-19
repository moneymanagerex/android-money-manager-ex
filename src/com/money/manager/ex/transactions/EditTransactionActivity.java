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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.businessobjects.CategoryService;
import com.money.manager.ex.businessobjects.PayeeService;
import com.money.manager.ex.businessobjects.RecurringTransactionService;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.core.TransactionTypes;
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
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.settings.PreferenceConstants;
import com.money.manager.ex.utils.DateUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * @author Alessandro Lazzari (lazzari.ale@gmail.com)
 * @version 1.0.1
 */
public class EditTransactionActivity
    extends BaseFragmentActivity
    implements IInputAmountDialogListener, YesNoDialogListener {

    // action type intent
    public String mIntentAction;
    public int mTransId = Constants.NOT_SET;

    // notes
    public String mNotes = "";
    // transaction numbers
    public String mTransNumber = "";
    // bill deposits
    public int mRecurringTransactionId = Constants.NOT_SET;
    public String mNextOccurrence = null;

    // Controls on the form.
    public ImageButton btnTransNumber;
    public EditText edtTransNumber, edtNotes;

    private TableCheckingAccount mCheckingAccount = new TableCheckingAccount();
    private EditTransactionCommonFunctions mCommonFunctions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_account_transaction);

        mCommonFunctions = new EditTransactionCommonFunctions(getApplicationContext(), this);

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
        mCommonFunctions.initCategoryControls(TableSplitTransactions.class.getSimpleName());

        // Split Categories
        mCommonFunctions.initSplitCategories();

        // mark checked if there are existing split categories.
        boolean hasSplit = mCommonFunctions.hasSplitCategories();
        mCommonFunctions.setSplit(hasSplit);

        // Amount and total amount

        mCommonFunctions.initAmountSelectors();

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
                String query = "SELECT MAX(CAST(" + ISplitTransactionsDataset.TRANSACTIONNUMBER + " AS INTEGER)) FROM " +
                        new TableCheckingAccount().getSource() + " WHERE " +
                        ISplitTransactionsDataset.ACCOUNTID + "=?";

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
        mCommonFunctions.onTransactionTypeChange(mCommonFunctions.transactionType);
        mCommonFunctions.refreshPayeeName();
        mCommonFunctions.refreshCategoryName();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mCommonFunctions.onActivityResult(requestCode, resultCode, data);
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

        mCommonFunctions.onTransactionTypeChange(mCommonFunctions.transactionType);
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
        outState.putString(EditTransactionActivityConstants.KEY_TO_ACCOUNT_NAME, mCommonFunctions.mToAccountName);
        outState.putString(EditTransactionActivityConstants.KEY_TRANS_DATE,
                new SimpleDateFormat(Constants.PATTERN_DB_DATE).format(mCommonFunctions.txtSelectDate.getTag()));
        outState.putString(EditTransactionActivityConstants.KEY_TRANS_CODE, mCommonFunctions.getTransactionType());
        outState.putString(EditTransactionActivityConstants.KEY_TRANS_STATUS, mCommonFunctions.status);
        outState.putDouble(EditTransactionActivityConstants.KEY_TRANS_TOTAMOUNT, (Double) mCommonFunctions.txtAmountTo.getTag());
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
        mCommonFunctions.onFinishedInputAmountDialog(id, amount);
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
        if (saveData()) {
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
        mCommonFunctions.accountId = cursor.getInt(cursor.getColumnIndex(ISplitTransactionsDataset.ACCOUNTID));
        mCommonFunctions.toAccountId = cursor.getInt(cursor.getColumnIndex(ISplitTransactionsDataset.TOACCOUNTID));
        String transCode = cursor.getString(cursor.getColumnIndex(ISplitTransactionsDataset.TRANSCODE));
        mCommonFunctions.transactionType = TransactionTypes.valueOf(transCode);
        mCommonFunctions.status = cursor.getString(cursor.getColumnIndex(ISplitTransactionsDataset.STATUS));
        mCommonFunctions.amount = cursor.getDouble(cursor.getColumnIndex(ISplitTransactionsDataset.TRANSAMOUNT));
        mCommonFunctions.amountTo = cursor.getDouble(cursor.getColumnIndex(ISplitTransactionsDataset.TOTRANSAMOUNT));
        mCommonFunctions.payeeId = cursor.getInt(cursor.getColumnIndex(ISplitTransactionsDataset.PAYEEID));
        mCommonFunctions.categoryId = cursor.getInt(cursor.getColumnIndex(ISplitTransactionsDataset.CATEGID));
        mCommonFunctions.subCategoryId = cursor.getInt(cursor.getColumnIndex(ISplitTransactionsDataset.SUBCATEGID));
        mTransNumber = cursor.getString(cursor.getColumnIndex(ISplitTransactionsDataset.TRANSACTIONNUMBER));
        mNotes = cursor.getString(cursor.getColumnIndex(ISplitTransactionsDataset.NOTES));
        if (!duplicate) {
            mCommonFunctions.mDate = cursor.getString(cursor.getColumnIndex(ISplitTransactionsDataset.TRANSDATE));
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
        if (!TextUtils.isEmpty(mCommonFunctions.status)) {
            mCommonFunctions.status = mCommonFunctions.status.toUpperCase();
        }

        AccountRepository accountRepository = new AccountRepository(this);
        mCommonFunctions.mToAccountName = accountRepository.loadName(mCommonFunctions.toAccountId);

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
        mCommonFunctions.status = tx.status;
        mCommonFunctions.amount = tx.amount;
        mCommonFunctions.amountTo = tx.totalAmount;
        mCommonFunctions.payeeId = tx.payeeId;
        mCommonFunctions.categoryId = tx.categoryId;
        mCommonFunctions.subCategoryId = tx.subCategoryId;
        mTransNumber = tx.transactionNumber;
        mNotes = tx.notes;
        mCommonFunctions.mDate = tx.nextOccurrence;

        AccountRepository accountRepository = new AccountRepository(this);
        mCommonFunctions.mToAccountName = accountRepository.loadName(mCommonFunctions.toAccountId);

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
        mCommonFunctions.mToAccountName = savedInstanceState.getString(EditTransactionActivityConstants.KEY_TO_ACCOUNT_NAME);
        mCommonFunctions.mDate = savedInstanceState.getString(EditTransactionActivityConstants.KEY_TRANS_DATE);
        String transCode = savedInstanceState.getString(EditTransactionActivityConstants.KEY_TRANS_CODE);
        mCommonFunctions.transactionType = TransactionTypes.valueOf(transCode);
        mCommonFunctions.status = savedInstanceState.getString(EditTransactionActivityConstants.KEY_TRANS_STATUS);
        mCommonFunctions.amount = savedInstanceState.getDouble(EditTransactionActivityConstants.KEY_TRANS_AMOUNT);
        mCommonFunctions.amountTo = savedInstanceState.getDouble(EditTransactionActivityConstants.KEY_TRANS_TOTAMOUNT);
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
            mCommonFunctions.accountId = intent.getIntExtra(EditTransactionActivityConstants.KEY_ACCOUNT_ID,
                    Constants.NOT_SET);

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
        mCommonFunctions.amountTo = parameters.amount;
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
        if (!mCommonFunctions.validateData()) return false;

        return true;
    }

    /**
     * update data into database
     *
     * @return true if update data successful
     */
    public boolean saveData() {
        if (!validateData()) return false;

        boolean isTransfer = mCommonFunctions.transactionType.equals(TransactionTypes.Transfer);

        // content value for insert or update data
        ContentValues values = getContentValues(isTransfer);

        // Insert or update?
        if (Constants.INTENT_ACTION_INSERT.equals(mIntentAction) || Constants.INTENT_ACTION_PASTE.equals(mIntentAction)) {
            // insert
            Uri insert = getContentResolver().insert(mCheckingAccount.getUri(), values);
            if (insert == null) {
                Toast.makeText(getApplicationContext(), R.string.db_checking_insert_failed, Toast.LENGTH_SHORT).show();
                Log.w(EditTransactionActivityConstants.LOGCAT, "Insert new transaction failed!");
                return false;
            }
            long id = ContentUris.parseId(insert);
            mTransId = (int) id;
        } else {
            // update
            if (getContentResolver().update(mCheckingAccount.getUri(),
                    values,
                    TableCheckingAccount.TRANSID + "=?",
                    new String[]{Integer.toString(mTransId)}) <= 0) {
                Toast.makeText(getApplicationContext(), R.string.db_checking_update_failed, Toast.LENGTH_SHORT).show();
                Log.w(EditTransactionActivityConstants.LOGCAT, "Update transaction failed!");
                return false;
            }
        }

        // Split Categories

        // Delete any split categories if split is unchecked.
        if(!mCommonFunctions.isSplitSelected()) {
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
            String transactionDate = values.getAsString(ISplitTransactionsDataset.TRANSDATE);
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

    private ContentValues getContentValues(boolean isTransfer) {
        ContentValues values = mCommonFunctions.getContentValues(isTransfer);

        values.put(ISplitTransactionsDataset.TRANSACTIONNUMBER, edtTransNumber.getText().toString());
        values.put(ISplitTransactionsDataset.NOTES, edtNotes.getText().toString());

        return values;
    }
}
