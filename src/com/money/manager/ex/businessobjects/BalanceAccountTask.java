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
package com.money.manager.ex.businessobjects;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;

import com.money.manager.ex.CheckingAccountActivity;
import com.money.manager.ex.R;
import com.money.manager.ex.checkingaccount.IntentDataParameters;
import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.database.QueryAccountBills;
import com.money.manager.ex.fragment.IInputAmountDialogListener;
import com.money.manager.ex.fragment.InputAmountDialog;

import java.math.BigDecimal;

/**
 * Balance Account functionality encapsulated.
 */
public class BalanceAccountTask
    implements IInputAmountDialogListener {

    public BalanceAccountTask(Context context) {
        mContext = context;
    }

    private Context mContext;
    QueryAccountBills mAccount;

    public void startBalanceAccount(QueryAccountBills account) {
        mAccount = account;

        // get the amount via input dialog.
        int currencyId = account.getCurrencyId();
        // do we need the id? the first 0.
        InputAmountDialog dialog = InputAmountDialog.getInstance(this, 0, 0.0, currencyId);
        FragmentActivity parent = (FragmentActivity) mContext;
        dialog.show(parent.getSupportFragmentManager(), dialog.getClass().getSimpleName());

        // the task continues in onFinishedInputAmountDialog
    }

    @Override
    public void onFinishedInputAmountDialog(int id, Double amount) {
        // get the account balance (from the screen here)
        //String value = currencyUtils.getCurrencyFormatted(account.getCurrencyId(), account.getTotal());
        BigDecimal currentBalance = BigDecimal.valueOf(mAccount.getTotal());

        // calculate the diff.
        BigDecimal newBalance = BigDecimal.valueOf(amount);
        if (newBalance.compareTo(currentBalance) == 0) return;

        BigDecimal difference;
        TransactionTypes transactionType;

        if (newBalance.compareTo(currentBalance) > 0) {
            // new balance > current balance
            difference = newBalance.subtract(currentBalance);
            transactionType = TransactionTypes.Deposit;
        } else {
            // new balance < current balance
            difference = currentBalance.subtract(newBalance);
            transactionType = TransactionTypes.Withdrawal;
        }

        // ask for category to use? Or just open a new transaction screen!
        // create a transaction to balance to the entered amount?
        Intent intent = new Intent(mContext, CheckingAccountActivity.class);
        intent.setAction(Intent.ACTION_INSERT);
        // add balance and transaction type and payee
        IntentDataParameters params = new IntentDataParameters();
        params.accountName = mAccount.getAccountName();
        params.transactionType = transactionType;
        params.payeeName = mContext.getString(R.string.balance_adjustment);
        params.amount = difference.doubleValue();
        params.categoryName = mContext.getString(R.string.cash);
        intent.setData(params.toUri());

        mContext.startActivity(intent);

    }

}
