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
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import com.money.manager.ex.Constants;
import com.money.manager.ex.PayeeActivity;
import com.money.manager.ex.R;
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
import com.shamanland.fonticon.FontIconButton;

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
    }

    // Payee
    public int payeeId = Constants.NOT_SET;
    public String payeeName;
    // Category
    public int mCategoryId = Constants.NOT_SET;
    public int mSubCategoryId = Constants.NOT_SET;

    public List<TableAccountList> AccountList;
    public ArrayList<String> mAccountNameList = new ArrayList<>();
    public ArrayList<Integer> mAccountIdList = new ArrayList<>();
    public int mAccountId = Constants.NOT_SET, mToAccountId = Constants.NOT_SET;
    public TransactionTypes mTransactionType;
    public String mCategoryName, mSubCategoryName;

    public ArrayList<ISplitTransactionsDataset> mSplitTransactions = null;
    public ArrayList<ISplitTransactionsDataset> mSplitTransactionsDeleted = null;

    public Spinner spinAccount, spinToAccount, spinStatus, spinTransCode;
    public TextView txtSelectPayee, txtTotAmount, txtAmount, txtSelectCategory;
    public TextView txtCaptionAmount;
    public CheckBox chbSplitTransaction;
    public FontIconButton removePayeeButton;

    private Context mContext;

    public void findControls() {
        Activity parent = (Activity) mContext;

        spinStatus = (Spinner) parent.findViewById(R.id.spinnerStatus);
        spinTransCode = (Spinner) parent.findViewById(R.id.spinnerTransCode);

        // Payee
        txtSelectPayee = (TextView) parent.findViewById(R.id.textViewPayee);
        removePayeeButton = (FontIconButton) parent.findViewById(R.id.removePayeeButton);

        chbSplitTransaction = (CheckBox) parent.findViewById(R.id.checkBoxSplitTransaction);
        txtSelectCategory = (TextView) parent.findViewById(R.id.textViewCategory);
        spinAccount = (Spinner) parent.findViewById(R.id.spinnerAccount);
        spinToAccount = (Spinner) parent.findViewById(R.id.spinnerToAccount);

        txtCaptionAmount = (TextView) parent.findViewById(R.id.textViewHeaderTotalAmount);
        txtAmount = (TextView) parent.findViewById(R.id.textViewAmount);
        txtTotAmount = (TextView) parent.findViewById(R.id.textViewTotAmount);
    }

    public void formatExtendedDate(TextView dateTextView) {
        try {
            Locale locale = mContext.getResources().getConfiguration().locale;
            dateTextView.setText(new SimpleDateFormat("EEEE dd MMMM yyyy", locale)
                    .format((Date) dateTextView.getTag()));
        } catch (Exception e) {
            ExceptionHandler handler = new ExceptionHandler(mContext, mContext);
            handler.handle(e, "formatting extended date");
        }
    }

    public String getTransactionType() {
        if (mTransactionType == null) {
            return null;
        }

        // mTransType
        return mTransactionType.name();
    }

    public boolean hasSplitCategories() {
        return mSplitTransactions != null && !mSplitTransactions.isEmpty();
    }

    /**
     * Initialize account selectors.
     */
    public void initAccountSelectors() {
        Core core = new Core(mContext.getApplicationContext());

        // account list to populate the spin
        AccountRepository accountRepository = new AccountRepository(mContext.getApplicationContext());
        // MoneyManagerOpenHelper.getInstance(getApplicationContext())
        this.AccountList = accountRepository.getTransactionAccounts(core.getAccountsOpenVisible(),
                core.getAccountFavoriteVisible());
        if (this.AccountList == null) return;

        for(TableAccountList account : this.AccountList) {
            mAccountNameList.add(account.getAccountName());
            mAccountIdList.add(account.getAccountId());
        }

        addMissingAccountToSelectors(accountRepository, mAccountId);
        addMissingAccountToSelectors(accountRepository, mToAccountId);
        // add the default account, if any.
        AppSettings settings = new AppSettings(mContext);
        String defaultAccountString = settings.getGeneralSettings().getDefaultAccount();
        // Set the current account, if not set already.
        if ((mAccountId == Constants.NOT_SET) && !TextUtils.isEmpty(defaultAccountString)) {
            int defaultAccount = Integer.parseInt(defaultAccountString);
            addMissingAccountToSelectors(accountRepository, defaultAccount);
            // Set the default account as the active account.
            mAccountId = defaultAccount;
        }

        // create adapter for spinAccount
        ArrayAdapter<String> adapterAccount = new ArrayAdapter<>(mContext,
                android.R.layout.simple_spinner_item, mAccountNameList);
        adapterAccount.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinAccount.setAdapter(adapterAccount);
        // select current value
        if (mAccountIdList.indexOf(mAccountId) >= 0) {
            spinAccount.setSelection(mAccountIdList.indexOf(mAccountId), true);
        }
        spinAccount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if ((position >= 0) && (position <= mAccountIdList.size())) {
                    mAccountId = mAccountIdList.get(position);
                    if (mTransactionType.equals(TransactionTypes.Transfer)) {
                        formatAmount(txtAmount, (Double) txtAmount.getTag(), mAccountId);
                    } else {
                        formatAmount(txtTotAmount, (Double) txtTotAmount.getTag(), mAccountId);
                    }
                    refreshHeaderAmount();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // to account
        adapterAccount.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinToAccount.setAdapter(adapterAccount);
        if (mToAccountId > 0) {
            if (mAccountIdList.indexOf(mToAccountId) >= 0) {
                spinToAccount.setSelection(mAccountIdList.indexOf(mToAccountId), true);
            }
        }
        spinToAccount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if ((position >= 0) && (position <= mAccountIdList.size())) {
                    mToAccountId = mAccountIdList.get(position);
                    formatAmount(txtAmount, (Double) txtAmount.getTag(), mAccountId);
                    formatAmount(txtTotAmount, (Double) txtTotAmount.getTag(), mToAccountId);
                    refreshHeaderAmount();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    public void initPayeeControls() {
        txtSelectPayee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity parent = (Activity) mContext;

                Intent intent = new Intent(mContext, PayeeActivity.class);
                intent.setAction(Intent.ACTION_PICK);
                parent.startActivityForResult(intent, REQUEST_PICK_PAYEE);
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

    public void formatAmount(TextView view, double amount, Integer accountId) {
        // take currency id
        Integer currencyId = null;

        int index = mAccountIdList.indexOf(accountId);

        if (index >= 0) {
            currencyId = this.AccountList.get(index).getCurrencyId();
        }

        CurrencyService currencyService = new CurrencyService(mContext.getApplicationContext());

        if (currencyId == null) {
            view.setText(currencyService.getBaseCurrencyFormatted(amount));
        } else {
            view.setText(currencyService.getCurrencyFormatted(currencyId, amount));
        }
        view.setTag(amount);
    }

    public void refreshHeaderAmount() {
        Activity parent = (Activity) mContext;

        TextView txtHeaderTotAmount = (TextView) parent.findViewById(R.id.textViewHeaderTotalAmount);
        TextView txtHeaderAmount = (TextView) parent.findViewById(R.id.textViewHeaderAmount);

        if (txtHeaderAmount == null || txtHeaderTotAmount == null)
            return;

        if (!mTransactionType.equals(TransactionTypes.Transfer)) {
            txtHeaderTotAmount.setText(R.string.total_amount);
            txtHeaderAmount.setText(R.string.amount);
        } else {
            // Transfer. Adjust the headers on amount text boxes.
            int index = mAccountIdList.indexOf(mAccountId);
            if (index >= 0) {
                txtHeaderAmount.setText(mContext.getString(R.string.withdrawal_from,
                        this.AccountList.get(index).getAccountName()));
            }
            index = mAccountIdList.indexOf(mToAccountId);
            if (index >= 0) {
                txtHeaderTotAmount.setText(mContext.getString(R.string.deposit_to,
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

    public void setSplit(final boolean checked) {
        chbSplitTransaction.post(new Runnable() {
            @Override
            public void run() {
                chbSplitTransaction.setChecked(checked);

                onSplitSet();
            }
        });
    }

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

    public void onSplitSet() {
        // update category field
        refreshCategoryName();

        boolean isSplit = chbSplitTransaction.isChecked();

        // enable/disable Amount field.
        txtAmount.setEnabled(!isSplit);
        txtTotAmount.setEnabled(!isSplit);
    }

    public void refreshCategoryName() {
        // validation
        if (txtSelectCategory == null) return;

        txtSelectCategory.setText("");

        if (chbSplitTransaction.isChecked()) {
            // Split transaction. Show ...
            txtSelectCategory.setText("\u2026");
        } else {
            if (!TextUtils.isEmpty(mCategoryName)) {
                txtSelectCategory.setText(mCategoryName);
                if (!TextUtils.isEmpty(mSubCategoryName)) {
                    txtSelectCategory.setText(Html.fromHtml(txtSelectCategory.getText() + " : <i>" + mSubCategoryName + "</i>"));
                }
            }
        }
    }

    /**
     * Handle transaction type change.
     */
    public void refreshAfterTransactionCodeChange() {
        Activity parent = (Activity) mContext;

        TextView txtFromAccount = (TextView) parent.findViewById(R.id.textViewFromAccount);
        TextView txtToAccount = (TextView) parent.findViewById(R.id.textViewToAccount);
        ViewGroup tableRowPayee = (ViewGroup) parent.findViewById(R.id.tableRowPayee);
        ViewGroup tableRowAmount = (ViewGroup) parent.findViewById(R.id.tableRowAmount);

        // hide and show
        boolean isTransfer = mTransactionType.equals(TransactionTypes.Transfer);

        txtFromAccount.setText(isTransfer ? R.string.from_account : R.string.account);
        txtToAccount.setVisibility(isTransfer ? View.VISIBLE : View.GONE);

        tableRowPayee.setVisibility(!isTransfer ? View.VISIBLE : View.GONE);
        tableRowAmount.setVisibility(isTransfer ? View.VISIBLE : View.GONE);
//        if (txtCaptionAmount != null) {
//            txtCaptionAmount.setVisibility(isTransfer ? View.VISIBLE : View.GONE);
//        }
//        txtAmount.setVisibility(isTransfer ? View.VISIBLE : View.GONE);
        spinToAccount.setVisibility(isTransfer ? View.VISIBLE : View.GONE);
        tableRowPayee.setVisibility(!isTransfer ? View.VISIBLE : View.GONE);
//        txtSelectPayee.setVisibility(!isTransfer ? View.VISIBLE : View.GONE);

        // hide split controls
        chbSplitTransaction.setVisibility(isTransfer ? View.GONE : View.VISIBLE);

        txtSelectCategory.setVisibility(isTransfer ? View.GONE : View.VISIBLE);

        refreshHeaderAmount();
    }

    /**
     * query info payee
     *
     * @param payeeId id payee
     * @return true if the data selected
     */
    public boolean selectPayeeName(int payeeId) {
        TablePayee payee = new TablePayee();
        Cursor cursor = mContext.getContentResolver().query(payee.getUri(),
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
        Cursor curPayee = mContext.getContentResolver().query(payee.getUri(),
                payee.getAllColumns(),
                TablePayee.PAYEEID + "=?",
                new String[]{ Integer.toString(payeeId) },
                null);
        // check cursor is valid
        if ((curPayee != null) && (curPayee.moveToFirst())) {
            // check if category is valid
            if (curPayee.getInt(curPayee.getColumnIndex(TablePayee.CATEGID)) != -1) {
                mCategoryId = curPayee.getInt(curPayee.getColumnIndex(TablePayee.CATEGID));
                mSubCategoryId = curPayee.getInt(curPayee.getColumnIndex(TablePayee.SUBCATEGID));
                // create instance of query
                QueryCategorySubCategory category = new QueryCategorySubCategory(mContext.getApplicationContext());
                // compose selection
                String where = "CATEGID=" + Integer.toString(mCategoryId) + " AND SUBCATEGID=" + Integer.toString(mSubCategoryId);
                Cursor curCategory = mContext.getContentResolver().query(category.getUri(),
                        category.getAllColumns(), where, null, null);
                // check cursor is valid
                if ((curCategory != null) && (curCategory.moveToFirst())) {
                    // take names of category and subcategory
                    mCategoryName = curCategory.getString(curCategory.getColumnIndex(QueryCategorySubCategory.CATEGNAME));
                    mSubCategoryName = curCategory.getString(curCategory.getColumnIndex(QueryCategorySubCategory.SUBCATEGNAME));
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

}
