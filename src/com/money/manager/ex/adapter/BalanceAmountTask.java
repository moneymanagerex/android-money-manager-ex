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
package com.money.manager.ex.adapter;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.view.View;
import android.widget.TextView;

import com.money.manager.ex.Constants;
import com.money.manager.ex.database.MoneyManagerOpenHelper;
import com.money.manager.ex.database.TableAccountList;
import com.money.manager.ex.database.TableCheckingAccount;
import com.money.manager.ex.utils.CurrencyUtils;

/**
 * Async task that updates the balance amount.
 */
public class BalanceAmountTask
        extends AsyncTask<Void, Void, Boolean> {
    private int mAccountId;
    private int mCurrencyId = -1;
    private int mTransId;
    private String mDate;
    private TextView mTextView;
    private Context mContext;
    private double total = 0;
    private SQLiteDatabase mDatabase;

    @Override
    protected Boolean doInBackground(Void... params) {
        TableCheckingAccount checkingAccount = new TableCheckingAccount();
        String selection = "(" + TableCheckingAccount.ACCOUNTID + "=" + Integer.toString(getAccountId()) +
                " OR " + TableCheckingAccount.TOACCOUNTID + "=" + Integer.toString(getAccountId()) + ") " + "" +
                "AND (" + TableCheckingAccount.TRANSDATE + "<'" + getDate() +
                "' OR (" + TableCheckingAccount.TRANSDATE + "='" + getDate() +
                "' AND " + TableCheckingAccount.TRANSID + "<=" + Integer.toString(getTransId()) + ")) " +
                "AND " + TableCheckingAccount.STATUS + "<>'V'";

        Cursor cursor = getDatabase().query(checkingAccount.getSource(),
                checkingAccount.getAllColumns(), selection, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                if (Constants.TRANSACTION_TYPE_WITHDRAWAL.equalsIgnoreCase(
                        cursor.getString(cursor.getColumnIndex(TableCheckingAccount.TRANSCODE)))) {
                    total -= cursor.getDouble(cursor.getColumnIndex(TableCheckingAccount.TRANSAMOUNT));
                } else if (Constants.TRANSACTION_TYPE_DEPOSIT.equalsIgnoreCase(
                        cursor.getString(cursor.getColumnIndex(TableCheckingAccount.TRANSCODE)))) {
                    total += cursor.getDouble(cursor.getColumnIndex(TableCheckingAccount.TRANSAMOUNT));
                } else {
                    if (cursor.getInt(cursor.getColumnIndex(TableCheckingAccount.ACCOUNTID)) == getAccountId()) {
                        total -= cursor.getDouble(cursor.getColumnIndex(TableCheckingAccount.TRANSAMOUNT));
                    } else {
                        total += cursor.getDouble(cursor.getColumnIndex(TableCheckingAccount.TOTRANSAMOUNT));
                    }
                }
                cursor.moveToNext();
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        // calculate initial bal
        TableAccountList accountList = new TableAccountList();
        cursor = getDatabase().query(accountList.getSource(), accountList.getAllColumns(),
                TableAccountList.ACCOUNTID + "=?",
                new String[] { Integer.toString(getAccountId()) },
                null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            total += cursor.getDouble(cursor.getColumnIndex(TableAccountList.INITIALBAL));
        }
        if (cursor != null) {
            cursor.close();
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result && getTextView() != null) {
            CurrencyUtils currencyUtils = new CurrencyUtils(mContext);

            getTextView().setText(currencyUtils.getCurrencyFormatted(getCurrencyId(), total));
            if (getTextView().getVisibility() != View.VISIBLE) getTextView().setVisibility(View.VISIBLE);
            // put in hash map balance total
            // mBalanceTransactions.put(getTransId(), total);
        }
    }

    /**
     * @return the mAccountId
     */
    public int getAccountId() {
        return mAccountId;
    }

    /**
     * @param mAccountId the mAccountId to set
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

    /**
     * @return the mDatabase
     */
    public SQLiteDatabase getDatabase() {
        if (mDatabase == null || !mDatabase.isOpen()) {
            mDatabase = MoneyManagerOpenHelper.getInstance(mContext.getApplicationContext())
                    .getReadableDatabase();
        }

        return mDatabase;
    }

    /**
     * @param mDatabase the mDatabase to set
     */
    public void setDatabase(SQLiteDatabase mDatabase) {
        this.mDatabase = mDatabase;
    }

    public int getCurrencyId() {
        return mCurrencyId;
    }

    public void setCurrencyId(int mCurrencyId) {
        this.mCurrencyId = mCurrencyId;
    }

}
