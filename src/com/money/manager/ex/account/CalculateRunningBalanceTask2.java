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

import com.money.manager.ex.adapter.AllDataAdapter;
import com.money.manager.ex.businessobjects.AccountService;
import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.core.TransactionStatuses;
import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.utils.DateUtils;
import com.money.manager.ex.viewmodels.AccountTransaction;

import java.math.BigDecimal;

/**
 * Async task that calculates and updates the amount balance for each transaction in the
 * transactions list.
 * Created by Alen Siljak on 11/09/2015.
 */
public class CalculateRunningBalanceTask2
        extends AsyncTask<Void, Void, BigDecimal[]> {

    public CalculateRunningBalanceTask2(Context context, BigDecimal[] balances, Cursor c,
                                        int accountId, String dateFieldName,
                                        ICalculateRunningBalanceTaskCallbacks listener) {
        this.context = context.getApplicationContext();
        this.balances = balances;
        this.cursor = c;
        this.accountId = accountId;
        this.dateFieldName = dateFieldName;
        this.listener = listener;
    }

    private Context context;
    private BigDecimal[] balances;
    private Cursor cursor;
    private int accountId;
    private String dateFieldName;
    private ICalculateRunningBalanceTaskCallbacks listener;

    /**
     * Override this method to perform a computation on a background thread. The
     * specified parameters are the parameters passed to {@link #execute}
     * by the caller of this task.
     * <p/>
     * This method can call {@link #publishProgress} to publish updates
     * on the UI thread.
     *
     * @param params The parameters of the task.
     * @return A result, defined by the subclass of this task.
     * @see #onPreExecute()
     * @see #onPostExecute
     * @see #publishProgress
     */
    @Override
    protected BigDecimal[] doInBackground(Void... params) {
        try {
            return runTask();
        } catch (IllegalStateException | SQLiteDiskIOException ex) {
            ExceptionHandler handler = new ExceptionHandler(this.context, this);
            handler.handle(ex, "balancing amount");
        } catch (Exception e) {
            throw new RuntimeException("Error in Balance Amount Task", e);
        }
        return null;
    }

    @Override
    protected void onPostExecute(BigDecimal[] result) {
        // todo: raise event
        if (this.listener != null) {
            listener.onTaskComplete(result);
        }
    }

    private BigDecimal[] runTask() {
        Cursor c = this.cursor;
        if (c == null) return null;
        int records = c.getCount();
        if (balances != null && records == balances.length) return null;
        if (c.getCount() <= 0) return null;

        BigDecimal startingBalance = null;

        AccountService accountService = new AccountService(this.context);
        AccountTransaction tx = new AccountTransaction();

        int originalPosition = c.getPosition();
        balances = new BigDecimal[c.getCount()];
        String transType;
        BigDecimal amount;
        BigDecimal runningBalance = BigDecimal.ZERO;

        // populate balance amounts

        // Move from the earliest record towards the newer ones.
        int i = c.getCount() - 1;

        while (c.moveToPosition(i)) {
            // load the initial balance based on the date of the first transaction
            if (startingBalance == null) {
                // Get starting balance on the given day.
                startingBalance = accountService.loadInitialBalance(this.accountId);

                String date = c.getString(c.getColumnIndex(this.dateFieldName));
                DateUtils dateUtils = new DateUtils();
                date = dateUtils.getYesterdayFrom(date);
                BigDecimal balanceOnDate = accountService.calculateBalanceOn(this.accountId, date);
                startingBalance = startingBalance.add(balanceOnDate);

                runningBalance = startingBalance;
            }

            // adjust the balance for each transaction.
            tx.loadFromCursor(c);

            // check whether the transaction is Void and exclude from calculation.
            if (!tx.getStatus().equals(TransactionStatuses.VOID)) {
                transType = tx.getTransactionType();

                switch (TransactionTypes.valueOf(transType)) {
                    case Withdrawal:
//                    runningBalance -= amount;
                        amount = tx.getAmount();
                        runningBalance = runningBalance.subtract(amount);
                        break;
                    case Deposit:
//                    runningBalance += amount;
                        amount = tx.getAmount();
                        runningBalance = runningBalance.add(amount);
                        break;
                    case Transfer:
                        int accountId = tx.getAccountId();
                        if (accountId == this.accountId) {
//                        runningBalance += tx.getAmount();
                            amount = tx.getAmount();
                            runningBalance = runningBalance.add(amount);
                        } else {
//                        runningBalance += amount;
                            amount = tx.getToAmount();
                            runningBalance = runningBalance.add(amount);
                        }
                        break;
                }
            }

            this.balances[i] = runningBalance;
            i--;
        }

        // set back to the original position.
        c.moveToPosition(originalPosition);

        return this.balances;
    }
}
