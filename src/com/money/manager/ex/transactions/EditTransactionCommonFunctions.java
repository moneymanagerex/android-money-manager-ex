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

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.money.manager.ex.Constants;
import com.money.manager.ex.PayeeActivity;
import com.money.manager.ex.R;
import com.money.manager.ex.common.BaseFragmentActivity;
import com.money.manager.ex.common.IInputAmountDialogListener;
import com.money.manager.ex.common.InputAmountDialog;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.database.AccountRepository;
import com.money.manager.ex.database.ISplitTransactionsDataset;
import com.money.manager.ex.database.QueryCategorySubCategory;
import com.money.manager.ex.database.TableAccountList;
import com.money.manager.ex.database.TablePayee;
import com.money.manager.ex.settings.AppSettings;
import com.shamanland.fonticon.FontIconView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Functions shared between Checking Account activity and Recurring Transactions activity.
 */
public class EditTransactionCommonFunctions {

    public static final int REQUEST_PICK_PAYEE = 1;

    public EditTransactionCommonFunctions(Context context) {
        mContext = context;
        if (context instanceof BaseFragmentActivity) {
            mParent = (BaseFragmentActivity) context;
        }
    }

    // Payee
    public int payeeId = Constants.NOT_SET;
    public String payeeName;
    // Category
    public int categoryId = Constants.NOT_SET;
    public int subCategoryId = Constants.NOT_SET;
    // amount
    public double amountTo = 0, amount = 0;
    // accounts
    public int accountId = Constants.NOT_SET, toAccountId = Constants.NOT_SET;

    // Controls
    public List<TableAccountList> AccountList;
    public ArrayList<String> mAccountNameList = new ArrayList<>();
    public ArrayList<Integer> mAccountIdList = new ArrayList<>();
    public TransactionTypes transactionType = TransactionTypes.Withdrawal;
    public String categoryName, subCategoryName;

    public ArrayList<ISplitTransactionsDataset> mSplitTransactions = null;
    public ArrayList<ISplitTransactionsDataset> mSplitTransactionsDeleted = null;

    public ViewGroup tableRowPayee, tableRowAmountTo;
    public Spinner spinAccount, spinAccountTo, spinStatus, spinTransCode;
    public TextView accountFromLabel, txtToAccount;
    public TextView txtSelectPayee, txtAmountTo, txtAmount, txtSelectCategory;
    public TextView amountHeaderTextView, amountToHeaderTextView;
    public FontIconView removePayeeButton, splitButton;
    public RelativeLayout withdrawalButton, depositButton, transferButton;

    private Context mContext;
    private BaseFragmentActivity mParent;
    private boolean mSplitSelected;

    public void findControls() {
        // Status
        spinStatus = (Spinner) mParent.findViewById(R.id.spinnerStatus);

        // Payee
        txtSelectPayee = (TextView) mParent.findViewById(R.id.textViewPayee);
        removePayeeButton = (FontIconView) mParent.findViewById(R.id.removePayeeButton);
        tableRowPayee = (ViewGroup) mParent.findViewById(R.id.tableRowPayee);

        // Category / Split
        splitButton = (FontIconView) mParent.findViewById(R.id.splitButton);
        txtSelectCategory = (TextView) mParent.findViewById(R.id.textViewCategory);

        // Account
        spinAccount = (Spinner) mParent.findViewById(R.id.spinnerAccount);
        spinAccountTo = (Spinner) mParent.findViewById(R.id.spinnerToAccount);
        accountFromLabel = (TextView) mParent.findViewById(R.id.accountFromLabel);
        txtToAccount = (TextView) mParent.findViewById(R.id.textViewToAccount);

        // Amounts
        amountHeaderTextView = (TextView) mParent.findViewById(R.id.textViewHeaderAmount);
        amountToHeaderTextView = (TextView) mParent.findViewById(R.id.textViewHeaderAmountTo);

        txtAmount = (TextView) mParent.findViewById(R.id.textViewAmount);
        txtAmountTo = (TextView) mParent.findViewById(R.id.textViewTotAmount);
        tableRowAmountTo = (ViewGroup) mParent.findViewById(R.id.tableRowAmountTo);

        // Transaction Type
        withdrawalButton = (RelativeLayout) mParent.findViewById(R.id.withdrawalButton);
        depositButton = (RelativeLayout) mParent.findViewById(R.id.depositButton);
        transferButton = (RelativeLayout) mParent.findViewById(R.id.transferButton);

    }

    public void formatAmount(TextView view, double amount, Integer accountId) {
        // take currency id
        Integer currencyId = null;

        int index = mAccountIdList.indexOf(accountId);

        if (index != Constants.NOT_SET) {
            currencyId = this.AccountList.get(index).getCurrencyId();
        }

        CurrencyService currencyService = new CurrencyService(mParent.getApplicationContext());

        if (currencyId == null) {
            view.setText(currencyService.getBaseCurrencyFormatted(amount));
        } else {
            view.setText(currencyService.getCurrencyFormatted(currencyId, amount));
        }
        view.setTag(amount);
    }

    public void formatExtendedDate(TextView dateTextView) {
        try {
            Locale locale = mParent.getResources().getConfiguration().locale;
            //SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE dd MMMM yyyy", locale);
            // use a shorted, defined, format, i.e. Tue, 28 Aug 2015 for fixed width.
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy", locale);

            dateTextView.setText(dateFormat.format((Date) dateTextView.getTag()));
        } catch (Exception e) {
            ExceptionHandler handler = new ExceptionHandler(mParent, mParent);
            handler.handle(e, "formatting extended date");
        }
    }

    public Integer getCurrencyIdFromAccountId(int accountId) {
        try {
            int currencyId = AccountList.get(mAccountIdList.indexOf(accountId)).getCurrencyId();
            return currencyId;
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    public String getTransactionType() {
        if (transactionType == null) {
            return null;
        }

        return transactionType.name();
    }

    public FontIconView getDepositButtonIcon() {
        return (FontIconView) mParent.findViewById(R.id.depositButtonIcon);
    }

    public FontIconView getTransferButtonIcon() {
        return (FontIconView) mParent.findViewById(R.id.transferButtonIcon);
    }

    public FontIconView getWithdrawalButtonIcon() {
        return (FontIconView) mParent.findViewById(R.id.withdrawalButtonIcon);
    }

    public boolean hasSplitCategories() {
        return mSplitTransactions != null && !mSplitTransactions.isEmpty();
    }

    /**
     * Initialize account selectors.
     */
    public void initAccountSelectors() {
        Core core = new Core(mParent.getApplicationContext());

        // account list to populate the spin
        AccountRepository accountRepository = new AccountRepository(mParent.getApplicationContext());
        this.AccountList = accountRepository.getTransactionAccounts(core.getAccountsOpenVisible(),
                core.getAccountFavoriteVisible());
        if (this.AccountList == null) return;

        for(TableAccountList account : this.AccountList) {
            mAccountNameList.add(account.getAccountName());
            mAccountIdList.add(account.getAccountId());
        }

        addMissingAccountToSelectors(accountRepository, accountId);
        addMissingAccountToSelectors(accountRepository, toAccountId);
        // add the default account, if any.
        AppSettings settings = new AppSettings(mParent);
        String defaultAccountString = settings.getGeneralSettings().getDefaultAccount();
        // Set the current account, if not set already.
        if ((accountId == Constants.NOT_SET) && !TextUtils.isEmpty(defaultAccountString)) {
            int defaultAccount = Integer.parseInt(defaultAccountString);
            addMissingAccountToSelectors(accountRepository, defaultAccount);
            // Set the default account as the active account.
            accountId = defaultAccount;
        }

        // create adapter for spinAccount
        ArrayAdapter<String> adapterAccount = new ArrayAdapter<>(mParent,
                android.R.layout.simple_spinner_item, mAccountNameList);
        adapterAccount.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinAccount.setAdapter(adapterAccount);
        // select current value
        if (mAccountIdList.indexOf(accountId) >= 0) {
            spinAccount.setSelection(mAccountIdList.indexOf(accountId), true);
        }
        spinAccount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if ((position >= 0) && (position <= mAccountIdList.size())) {
                    accountId = mAccountIdList.get(position);
                    formatAmount(txtAmount, (Double) txtAmount.getTag(), accountId);
//                    formatAmount(txtAmountTo, (Double) txtAmountTo.getTag(), accountId);
                    refreshControlHeaders();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // to account
        adapterAccount.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinAccountTo.setAdapter(adapterAccount);
        if (toAccountId != Constants.NOT_SET && mAccountIdList.indexOf(toAccountId) >= 0) {
            spinAccountTo.setSelection(mAccountIdList.indexOf(toAccountId), true);
        }
        spinAccountTo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if ((position >= 0) && (position <= mAccountIdList.size())) {
                    toAccountId = mAccountIdList.get(position);
//                    formatAmount(txtAmount, (Double) txtAmount.getTag(), accountId);
                    formatAmount(txtAmountTo, (Double) txtAmountTo.getTag(), toAccountId);
                    refreshControlHeaders();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    public void initAmountSelectors() {
        View.OnClickListener onClickAmount = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Get currency id from the account for which the amount has been modified.
                Integer currencyId = null;
                int selectedPosition;
                if (v.equals(txtAmountTo)) {
                    selectedPosition = spinAccountTo.getSelectedItemPosition();
                    if (selectedPosition >= 0 && selectedPosition < AccountList.size()) {
                        currencyId = AccountList.get(selectedPosition).getCurrencyId();
                    }
                } else {
                    // Amount.
                    selectedPosition = spinAccount.getSelectedItemPosition();
                    if (selectedPosition >= 0 && selectedPosition < AccountList.size()) {
                        currencyId = AccountList.get(selectedPosition).getCurrencyId();
                    }
                }
                double amount = (Double) v.getTag();
                InputAmountDialog dialog = InputAmountDialog.getInstance((IInputAmountDialogListener) mParent,
                        v.getId(), amount, currencyId);
                dialog.show(mParent.getSupportFragmentManager(), dialog.getClass().getSimpleName());
            }
        };

        // amount
        formatAmount(txtAmount, amount, accountId);
        txtAmount.setOnClickListener(onClickAmount);

        // amount to
        formatAmount(txtAmountTo, amountTo, toAccountId);
        txtAmountTo.setOnClickListener(onClickAmount);
    }
    
    public void initPayeeControls() {
        txtSelectPayee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mParent, PayeeActivity.class);
                intent.setAction(Intent.ACTION_PICK);
                mParent.startActivityForResult(intent, REQUEST_PICK_PAYEE);
            }
        });

        removePayeeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                payeeId = Constants.NOT_SET;
                payeeName = "";

                refreshPayeeName();
            }
        });
    }

    public void initSplitCategories() {
        // Split button
        splitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSplitSelected = !mSplitSelected;
                onSplitSet();
            }
        });

        onSplitSet();
    }

    public void initTransactionTypeSelector() {

        // Handle click events.

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // find which transaction type this is.
                TransactionTypes type = (TransactionTypes) v.getTag();
                selectTransactionType(type);
            }
        };

        if (withdrawalButton != null) {
            withdrawalButton.setTag(TransactionTypes.Withdrawal);

            withdrawalButton.setOnClickListener(onClickListener);
        }
        if (depositButton != null) {
            depositButton.setTag(TransactionTypes.Deposit);

            depositButton.setOnClickListener(onClickListener);
        }
        if (transferButton != null) {
            transferButton.setTag(TransactionTypes.Transfer);

            transferButton.setOnClickListener(onClickListener);
        }

        // Check if the transaction type has been set (for example, when editing an existing
        // transaction).
        TransactionTypes current = transactionType == null
                ? TransactionTypes.Withdrawal
                : transactionType;
        selectTransactionType(current);
    }

    /**
     * Indicate whether the Split Categories is selected/checked.
     * @return boolean
     */
    public boolean isSplitSelected() {
        return mSplitSelected;
    }

    public void onFinishedInputAmountDialog(int id, Double amount) {
        View view = mParent.findViewById(id);
        if (view == null || !(view instanceof TextView)) return;

        int accountId;
        boolean isTransfer = transactionType.equals(TransactionTypes.Transfer);
        CurrencyService currencyService = new CurrencyService(mParent.getApplicationContext());

        if (isTransfer) {
            Double originalAmount;
            try {
                Integer toCurrencyId = AccountList.get(mAccountIdList
                        .indexOf(id == R.id.textViewTotAmount
                                ? this.accountId
                                : this.toAccountId)).getCurrencyId();
                Integer fromCurrencyId = AccountList.get(mAccountIdList
                        .indexOf(id == R.id.textViewTotAmount
                                ? this.toAccountId
                                : this.accountId)).getCurrencyId();
                // take a original values
                originalAmount = id == R.id.textViewTotAmount
                        ? (Double) txtAmountTo.getTag()
                        : (Double) txtAmount.getTag();
                // convert value
                Double amountExchange = currencyService.doCurrencyExchange(toCurrencyId, originalAmount, fromCurrencyId);
                // take original amount converted
                originalAmount = id == R.id.textViewTotAmount
                        ? (Double) txtAmount.getTag()
                        : (Double) txtAmountTo.getTag();
                if (originalAmount == null)
                    originalAmount = 0d;
                // check if two values is equals, and then convert value
                if (originalAmount == 0) {
                    DecimalFormat decimalFormat = new DecimalFormat("0.00");
                    if (decimalFormat.format(originalAmount).equals(decimalFormat.format(amountExchange))) {
                        amountExchange = currencyService.doCurrencyExchange(toCurrencyId, amount, fromCurrencyId);
                        formatAmount(id == R.id.textViewTotAmount
                                        ? txtAmount : txtAmountTo,
                                amountExchange,
                                this.getCurrencyIdFromAccountId(id == R.id.textViewTotAmount
                                        ? this.accountId
                                        : this.toAccountId));
                    }
                }

            } catch (Exception e) {
                ExceptionHandler handler = new ExceptionHandler(mParent, mParent);
                handler.handle(e, "returning from number input");
            }
        }
        if (view.equals(txtAmount)) {
            accountId = this.accountId;
        } else {
            accountId = this.toAccountId;
        }
//        Integer currencyId = getCurrencyIdFromAccountId(accountId);
        formatAmount(((TextView) view), amount, accountId);
    }

    /**
     * Handle the controls after the split is checked.
     */
    public void onSplitSet() {
        // update category field
        refreshCategoryName();

        // enable/disable Amount field.
        txtAmount.setEnabled(!mSplitSelected);
        txtAmountTo.setEnabled(!mSplitSelected);

        int buttonColour, buttonBackground;
        if (mSplitSelected) {
            buttonColour = R.color.button_foreground_active;
            buttonBackground = R.color.button_background_active;
        } else {
            buttonColour = R.color.button_foreground_inactive;
            Core core = new Core(mContext);
            buttonBackground = core.usingDarkTheme()
                ? R.color.button_background_inactive_dark
                : R.color.button_background_inactive_light;
        }
        splitButton.setTextColor(mContext.getResources().getColor(buttonColour));
        splitButton.setBackgroundColor(mContext.getResources().getColor(buttonBackground));
    }

    public void refreshCategoryName() {
        // validation
        if (txtSelectCategory == null) return;

        txtSelectCategory.setText("");

        if (isSplitSelected()) {
            // Split transaction. Show ...
            txtSelectCategory.setText("\u2026");
        } else {
            if (!TextUtils.isEmpty(categoryName)) {
                txtSelectCategory.setText(categoryName);
                if (!TextUtils.isEmpty(subCategoryName)) {
                    txtSelectCategory.setText(Html.fromHtml(txtSelectCategory.getText() + " : <i>" + subCategoryName + "</i>"));
                }
            }
        }
    }

    /**
     * Update input control titles to reflect the transaction type.
     */
    public void refreshControlHeaders() {
        if (amountHeaderTextView == null || amountToHeaderTextView == null) {
            return;
        }

        if (!transactionType.equals(TransactionTypes.Transfer)) {
//            amountToHeaderTextView.setText(R.string.total_amount);
            amountHeaderTextView.setText(R.string.amount);
        } else {
            // Transfer. Adjust the headers on amount text boxes.
            int index = mAccountIdList.indexOf(accountId);
            if (index >= 0) {
                amountHeaderTextView.setText(mParent.getString(R.string.withdrawal_from,
                        this.AccountList.get(index).getAccountName()));
            }
            index = mAccountIdList.indexOf(toAccountId);
            if (index >= 0) {
                amountToHeaderTextView.setText(mParent.getString(R.string.deposit_to,
                        this.AccountList.get(index).getAccountName()));
            }
        }
    }

    /**
     * update UI interface with PayeeName
     */
    public void refreshPayeeName() {
        // write into text button payee name
        if (txtSelectPayee != null) {
            String text = !TextUtils.isEmpty(payeeName)
                    ? payeeName : "";

            txtSelectPayee.setText(text);
        }
    }

    /**
     * Reflect the transaction type change. Show and hide controls appropriately.
     */
    public void onTransactionTypeChange() {
        // hide and show
        boolean isTransfer = transactionType.equals(TransactionTypes.Transfer);

        accountFromLabel.setText(isTransfer ? R.string.from_account : R.string.account);
        txtToAccount.setVisibility(isTransfer ? View.VISIBLE : View.GONE);

        tableRowPayee.setVisibility(!isTransfer ? View.VISIBLE : View.GONE);
//        tableRowAmount.setVisibility(isTransfer ? View.VISIBLE : View.GONE);
        tableRowAmountTo.setVisibility(isTransfer ? View.VISIBLE : View.GONE);

        spinAccountTo.setVisibility(isTransfer ? View.VISIBLE : View.GONE);
        tableRowPayee.setVisibility(!isTransfer ? View.VISIBLE : View.GONE);

        // hide split controls
//        chbSplitTransaction.setVisibility(isTransfer ? View.GONE : View.VISIBLE);
        splitButton.setVisibility(isTransfer ? View.GONE : View.VISIBLE);

        txtSelectCategory.setVisibility(isTransfer ? View.GONE : View.VISIBLE);

        refreshControlHeaders();
    }

    public void setSplit(final boolean checked) {
        mSplitSelected = checked;
        onSplitSet();
    }

    /**
     * query info payee
     *
     * @param payeeId id payee
     * @return true if the data selected
     */
    public boolean selectPayeeName(int payeeId) {
        TablePayee payee = new TablePayee();
        Cursor cursor = mParent.getContentResolver().query(payee.getUri(),
                payee.getAllColumns(),
                TablePayee.PAYEEID + "=?",
                new String[]{Integer.toString(payeeId)}, null);
        // check if cursor is valid and open
        if (cursor == null) return false;

        if (cursor.moveToFirst()) {
            // set payee name
            payeeName = cursor.getString(cursor.getColumnIndex(TablePayee.PAYEENAME));
        }
        cursor.close();

        return true;
    }

    /**
     * setCategoryFromPayee set last category used from payee
     *
     * @param payeeId Identify of payee
     * @return true if category set
     */
    public boolean setCategoryFromPayee(int payeeId) {
        boolean ret = false;
        // take data of payee
        TablePayee payee = new TablePayee();
        Cursor curPayee = mParent.getContentResolver().query(payee.getUri(),
                payee.getAllColumns(),
                TablePayee.PAYEEID + "=?",
                new String[]{ Integer.toString(payeeId) },
                null);
        // check cursor is valid
        if ((curPayee != null) && (curPayee.moveToFirst())) {
            // check if category is valid
            if (curPayee.getInt(curPayee.getColumnIndex(TablePayee.CATEGID)) != -1) {
                categoryId = curPayee.getInt(curPayee.getColumnIndex(TablePayee.CATEGID));
                subCategoryId = curPayee.getInt(curPayee.getColumnIndex(TablePayee.SUBCATEGID));
                // create instance of query
                QueryCategorySubCategory category = new QueryCategorySubCategory(mParent.getApplicationContext());
                // compose selection
                String where = "CATEGID=" + Integer.toString(categoryId) + " AND SUBCATEGID=" + Integer.toString(subCategoryId);
                Cursor curCategory = mParent.getContentResolver().query(category.getUri(),
                        category.getAllColumns(), where, null, null);
                // check cursor is valid
                if ((curCategory != null) && (curCategory.moveToFirst())) {
                    // take names of category and subcategory
                    categoryName = curCategory.getString(curCategory.getColumnIndex(QueryCategorySubCategory.CATEGNAME));
                    subCategoryName = curCategory.getString(curCategory.getColumnIndex(QueryCategorySubCategory.SUBCATEGNAME));
                    // return true
                    ret = true;
                }
                if (curCategory != null) {
                    curCategory.close();
                }
            }
        }

        if (curPayee != null) {
            curPayee.close();
        }

        return ret;
    }

    // Private

    private void addMissingAccountToSelectors(AccountRepository accountRepository, int accountId) {
        if (accountId <= 0) return;

        // #316. In case the account from recurring transaction is not in the visible list,
        // load it separately.
        TableAccountList savedAccount;
        if (!mAccountIdList.contains(accountId)) {
            savedAccount = accountRepository.load(accountId);

            if (savedAccount != null) {
                this.AccountList.add(savedAccount);
                mAccountNameList.add(savedAccount.getAccountName());
                mAccountIdList.add(savedAccount.getAccountId());
            }
        }
    }

    private void onTransferSelected() {
        // The user is switching to Transfer transaction type.

        if(hasSplitCategories()) {
            // Prompt the user to confirm deleting split categories.
            // Use DialogFragment in order to redraw the dialog when switching device orientation.

            DialogFragment dialog = new YesNoDialog();
            Bundle args = new Bundle();
            args.putString("title", mParent.getString(R.string.warning));
            args.putString("message", mParent.getString(R.string.no_transfer_splits));
            args.putString("purpose", YesNoDialog.PURPOSE_DELETE_SPLITS_WHEN_SWITCHING_TO_TRANSFER);
            dialog.setArguments(args);
//        dialog.setTargetFragment(this, REQUEST_REMOVE_SPLIT_WHEN_TRANSACTION);

            dialog.show(mParent.getSupportFragmentManager(), "tag");

            // Dialog result is handled in onDialogPositiveClick.
            return;
        }

        // un-check split.
        setSplit(false);

        // Clear category.
        categoryId = Constants.NOT_SET;
    }

    private void selectTransactionType(TransactionTypes transactionType) {
        this.transactionType = transactionType;

        // Clear all buttons.

        Core core = new Core(mContext);
        int backgroundInactive = core.getColourAttribute(R.attr.button_background_inactive);

        withdrawalButton.setBackgroundColor(backgroundInactive);
        getWithdrawalButtonIcon().setTextColor(mContext.getResources().getColor(R.color.material_red_700));
        depositButton.setBackgroundColor(backgroundInactive);
        getDepositButtonIcon().setTextColor(mContext.getResources().getColor(R.color.material_green_700));
        transferButton.setBackgroundColor(backgroundInactive);
        getTransferButtonIcon().setTextColor(mContext.getResources().getColor(R.color.material_grey_700));

        // Style the selected button.

        int backgroundSelected = mParent.getResources().getColor(R.color.button_background_active);
        int foregroundSelected = mContext.getResources().getColor(R.color.button_foreground_active);

        switch (transactionType) {
            case Deposit:
                depositButton.setBackgroundColor(backgroundSelected);
                getDepositButtonIcon().setTextColor(foregroundSelected);
                break;
            case Withdrawal:
                withdrawalButton.setBackgroundColor(backgroundSelected);
                getWithdrawalButtonIcon().setTextColor(foregroundSelected);
                break;
            case Transfer:
                transferButton.setBackgroundColor(backgroundSelected);
                getTransferButtonIcon().setTextColor(foregroundSelected);
                onTransferSelected();
                break;
        }

        onTransactionTypeChange();
    }

    public boolean validateData() {
        boolean isTransfer = transactionType.equals(TransactionTypes.Transfer);

        if (isTransfer) {
            if (toAccountId == Constants.NOT_SET) {
                Core.alertDialog(mParent, R.string.error_toaccount_not_selected);
                return false;
            }
            if (toAccountId == accountId) {
                Core.alertDialog(mParent, R.string.error_transfer_to_same_account);
                return false;
            }

            // Amount To is required and has to be positive.
            double amountTo = (Double) txtAmountTo.getTag();
            if (amountTo <= 0) {
                Core.alertDialog(mParent, R.string.error_amount_must_be_positive);
                return false;
            }
        }

        // Amount is required and must be positive. Sign is determined by transaction type.
        double amount = (Double) txtAmount.getTag();
        if (amount <= 0) {
            Core.alertDialog(mParent, R.string.error_amount_must_be_positive);
            return false;
        }

        // Category is required if tx is not a split or transfer.
        if (categoryId == Constants.NOT_SET && (!isSplitSelected()) && !isTransfer) {
            Core.alertDialog(mParent, R.string.error_category_not_selected);
            return false;
        }

        // Split records must exist if split is checked.
        if (isSplitSelected()
                && (mSplitTransactions == null || mSplitTransactions.size() <= 0)) {
            Core.alertDialog(mParent, R.string.error_split_transaction_empty);
            return false;
        }

        return true;
    }
}
