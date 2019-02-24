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
package com.money.manager.ex.account;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;

import com.money.manager.ex.account.events.RunningBalanceCalculatedEvent;
import com.money.manager.ex.servicelayer.AccountService;
import com.money.manager.ex.common.AllDataListFragment;
import com.money.manager.ex.core.TransactionStatuses;
import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.datalayer.QueryAllDataRepository;
import com.money.manager.ex.utils.MmxDate;
import com.money.manager.ex.viewmodels.AccountTransactionDisplay;

import org.greenrobot.eventbus.EventBus;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;
import timber.log.Timber;

/**
 * Not used!
 * Async task that calculates and updates the amount balance for each transaction in the
 * transactions list.
 * Here the idea is the fast calculation of the balances and caching in memory.
 * The problem is displaying the amounts once they are loaded.
 */
public class CalculateRunningBalanceTask2
    extends AsyncTask<Void, Void, HashMap<Integer, Money>> {

    /**
     * Create the task.
     * @param context Context
     * @param accountId Id of the account for which to load the balances.
     * @param startingDate The date, inclusive, from which to calculate the running balance.
     */
    public CalculateRunningBalanceTask2(Context context, int accountId, Date startingDate,
                                        Bundle selection) {
        this.context = context.getApplicationContext();
        this.accountId = accountId;
        this.startingDate = startingDate;
        this.selectionBundle = selection;
    }

    private Context context;
    private HashMap<Integer, Money> balances;
    private int accountId;
    private Date startingDate;
    private Bundle selectionBundle;

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
    protected HashMap<Integer, Money> doInBackground(Void... params) {
        try {
            return runTask();
        } catch (Exception ex) {
            Timber.e(ex, "balancing amount");
        }
        return null;
    }

    @Override
    protected void onPostExecute(HashMap<Integer, Money> result) {
        EventBus.getDefault().post(new RunningBalanceCalculatedEvent(result));
    }

    private HashMap<Integer, Money> runTask() {
        // load data
        Cursor c = loadData();
        if (c == null) return null;
        int records = c.getCount();
        if (balances != null && records == balances.size()) return null;
        if (c.getCount() <= 0) return null;

        Money startingBalance = null;

        AccountService accountService = new AccountService(this.context);
        AccountTransactionDisplay tx = new AccountTransactionDisplay();

        int originalPosition = c.getPosition();
        balances = new HashMap<>();
        String transType;
        Money amount = MoneyFactory.fromBigDecimal(BigDecimal.ZERO);
        Money runningBalance = MoneyFactory.fromBigDecimal(BigDecimal.ZERO);

        // populate balance amounts

        // Move from the earliest record towards the newer ones.
        int i = c.getCount() - 1;

        while (c.moveToPosition(i)) {
            // load the initial balance based on the date of the first transaction
            if (startingBalance == null) {
                // Get starting balance on the given day.
                startingBalance = accountService.loadInitialBalance(this.accountId);

                String date = new MmxDate(this.startingDate).minusDays(1)
                        .toIsoDateString();
                Money balanceOnDate = accountService.calculateBalanceOn(this.accountId, date);
                startingBalance = startingBalance.add(balanceOnDate);

                runningBalance = startingBalance;
            }

            // adjust the balance for each transaction.

            tx.loadFromCursor(c);

            // Exclude Void transactions from calculation.
            TransactionStatuses status = tx.getStatus();
            if (!status.equals(TransactionStatuses.VOID)) {
                transType = tx.getTransactionTypeName();

                switch (TransactionTypes.valueOf(transType)) {
                    case Withdrawal:
                        amount = tx.getAmount();
                        break;
                    case Deposit:
                        amount = tx.getAmount();
                        break;
                    case Transfer:
                        int accountId = tx.getAccountId();
                        if (accountId == this.accountId) {
                            amount = tx.getAmount();
                        } else {
                            amount = tx.getToAmount();
                        }
                        break;
                }
                runningBalance = runningBalance.add(amount);
            }

            this.balances.put(tx.getId(), runningBalance);
            i--;
        }

        // set back to the original position.
        c.moveToPosition(originalPosition);

        return this.balances;
    }

    private Cursor loadData() {
        String where = this.selectionBundle.getString(AllDataListFragment.KEY_ARGUMENTS_WHERE);
        String sort = this.selectionBundle.getString(AllDataListFragment.KEY_ARGUMENTS_SORT);

        QueryAllDataRepository repo = new QueryAllDataRepository(this.context);
        return repo.query(where, sort);
    }
}
