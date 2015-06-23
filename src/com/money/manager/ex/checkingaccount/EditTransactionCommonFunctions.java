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
package com.money.manager.ex.checkingaccount;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.database.AccountRepository;
import com.money.manager.ex.database.TableAccountList;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.utils.CurrencyUtils;
import com.money.manager.ex.view.RobotoCheckBox;

import java.util.ArrayList;
import java.util.List;

/**
 * Functions shared between Checking Account activity and Recurring Transactions activity.
 */
public class EditTransactionCommonFunctions {

    public EditTransactionCommonFunctions(Context context) {
        mContext = context;
    }

    public List<TableAccountList> AccountList;
    public ArrayList<String> mAccountNameList = new ArrayList<>();
    public ArrayList<Integer> mAccountIdList = new ArrayList<>();
    public int mAccountId = Constants.NOT_SET, mToAccountId = Constants.NOT_SET;
    public TransactionTypes mTransactionType;
    public String mCategoryName, mSubCategoryName;

    public Spinner spinAccount, spinToAccount, spinStatus, spinTransCode;
    public TextView txtSelectPayee, txtTotAmount, txtAmount, txtSelectCategory;
    public CheckBox chbSplitTransaction;

    private Context mContext;

    public void findControls() {
        Activity parent = (Activity) mContext;

        spinStatus = (Spinner) parent.findViewById(R.id.spinnerStatus);
        spinTransCode = (Spinner) parent.findViewById(R.id.spinnerTransCode);
        txtSelectPayee = (TextView) parent.findViewById(R.id.textViewPayee);

        chbSplitTransaction = (CheckBox) parent.findViewById(R.id.checkBoxSplitTransaction);
        txtSelectCategory = (TextView) parent.findViewById(R.id.textViewCategory);
        spinAccount = (Spinner) parent.findViewById(R.id.spinnerAccount);
        spinToAccount = (Spinner) parent.findViewById(R.id.spinnerToAccount);

        txtAmount = (TextView) parent.findViewById(R.id.textViewAmount);
        txtTotAmount = (TextView) parent.findViewById(R.id.textViewTotAmount);
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

    public void formatAmount(TextView view, double amount, Integer accountId) {
        // take currency id
        Integer currencyId = null;

        int index = mAccountIdList.indexOf(accountId);

        if (index >= 0) {
            currencyId = this.AccountList.get(index).getCurrencyId();
        }

        CurrencyUtils currencyUtils = new CurrencyUtils(mContext.getApplicationContext());

        if (currencyId == null) {
            view.setText(currencyUtils.getBaseCurrencyFormatted(amount));
        } else {
            view.setText(currencyUtils.getCurrencyFormatted(currencyId, amount));
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

}
