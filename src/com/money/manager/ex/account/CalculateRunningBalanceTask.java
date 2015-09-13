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
package com.money.manager.ex.account;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDiskIOException;
import android.os.AsyncTask;
import android.view.View;
import android.widget.TextView;

import com.money.manager.ex.businessobjects.AccountService;
import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.database.ISplitTransactionsDataset;
import com.money.manager.ex.database.TableCheckingAccount;

import java.math.BigDecimal;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

/**
 * NOT USED
 * Async task that calculates and updates the amount balance in the transaction list.
 * The idea here is to keep a reference to the view and calculate the balance for each
 * transaction. This might be slow and processor intensive.
 */
public class CalculateRunningBalanceTask
        extends AsyncTask<Void, Void, Boolean> {

    private int mAccountId;
    private int mCurrencyId = -1;
    private int mTransId;
    private String mDate;
    private TextView mTextView;
    private Context mContext;
    private double total = 0;

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            return runTask();
        } catch (IllegalStateException | SQLiteDiskIOException ex) {
            ExceptionHandler handler = new ExceptionHandler(getContext(), this);
            handler.handle(ex, "balancing amount");
        } catch (Exception e) {
            throw new RuntimeException("Error in Balance Amount Task", e);
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result && getTextView() != null) {
            CurrencyService currencyService = new CurrencyService(getContext());

            getTextView().setText(currencyService.getCurrencyFormatted(getCurrencyId(), MoneyFactory.fromDouble(total)));
            if (getTextView().getVisibility() != View.VISIBLE) getTextView().setVisibility(View.VISIBLE);
        }
    }

    private boolean runTask() {
        TableCheckingAccount checkingAccount = new TableCheckingAccount();

        String selection = "(" + ISplitTransactionsDataset.ACCOUNTID + "=" + Integer.toString(getAccountId()) +
                " OR " + ISplitTransactionsDataset.TOACCOUNTID + "=" + Integer.toString(getAccountId()) + ") " +
            "AND (" + ISplitTransactionsDataset.TRANSDATE + "<'" + getDate() +
                "' OR (" + ISplitTransactionsDataset.TRANSDATE + "='" + getDate() +
                    "' AND " + TableCheckingAccount.TRANSID + "<=" + Integer.toString(getTransId()) + ")) " +
            "AND " + ISplitTransactionsDataset.STATUS + "<>'V'";

        // sorting required for the correct balance calculation.
        String sort = ISplitTransactionsDataset.TRANSDATE + " DESC, " +
            ISplitTransactionsDataset.TRANSCODE + ", " +
            TableCheckingAccount.TRANSID + " DESC";


        Cursor cursor = mContext.getContentResolver().query(checkingAccount.getUri(),
                checkingAccount.getAllColumns(),
                selection, null,
                sort);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String transType = cursor.getString(cursor.getColumnIndex(ISplitTransactionsDataset.TRANSCODE));

                // Some users have invalid Transaction Type. Should we check .contains()?

                switch (TransactionTypes.valueOf(transType)) {
                    case Withdrawal:
                        total -= cursor.getDouble(cursor.getColumnIndex(ISplitTransactionsDataset.TRANSAMOUNT));
                        break;
                    case Deposit:
                        total += cursor.getDouble(cursor.getColumnIndex(ISplitTransactionsDataset.TRANSAMOUNT));
                        break;
                    case Transfer:
                        if (cursor.getInt(cursor.getColumnIndex(ISplitTransactionsDataset.ACCOUNTID)) == getAccountId()) {
                            total -= cursor.getDouble(cursor.getColumnIndex(ISplitTransactionsDataset.TRANSAMOUNT));
                        } else {
                            total += cursor.getDouble(cursor.getColumnIndex(ISplitTransactionsDataset.TOTRANSAMOUNT));
                        }
                        break;
                }
            }

            cursor.close();
        }

        // Retrieve initial balance.
        AccountService accountService = new AccountService(mContext);
        Money initialBalance = accountService.loadInitialBalance(getAccountId());
        total += initialBalance.toDouble();

        return true;
    }

    /**
     * @return the accountId
     */
    public int getAccountId() {
        return mAccountId;
    }

    /**
     * @param mAccountId the accountId to set
     */
    public void setAccountId(int mAccountId) {
        this.mAccountId = mAccountId;
    }

    /**
     * @return the mDate
     */
    public String getDate() {
        return mDate;
    }

    /**
     * @param mDate the mDate to set
     */
    public void setDate(String mDate) {
        this.mDate = mDate;
    }

    /**
     * @return the mTextView
     */
    public TextView getTextView() {
        return mTextView;
    }

    /**
     * @param mTextView the mTextView to set
     */
    public void setTextView(TextView mTextView) {
        this.mTextView = mTextView;
    }

    /**
     * @return the mContext
     */
//    @SuppressWarnings("unused")
    public Context getContext() {
        return mContext;
    }

    /**
     * @param mContext the mContext to set
     */
    public void setContext(Context mContext) {
        this.mContext = mContext;
    }

    /**
     * @return the mTransId
     */
    public int getTransId() {
        return mTransId;
    }

    /**
     * @param mTransId the mTransId to set
     */
    public void setTransId(int mTransId) {
        this.mTransId = mTransId;
    }

    public int getCurrencyId() {
        return mCurrencyId;
    }

    public void setCurrencyId(int mCurrencyId) {
        this.mCurrencyId = mCurrencyId;
    }

}
