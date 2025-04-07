/*
 * Copyright (C) 2012-2024 The Android Money Manager Ex Project Team
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

package com.money.manager.ex.scheduled;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;

import com.money.manager.ex.database.QueryBillDeposits;
import com.money.manager.ex.domainmodel.RecurringTransaction;
import com.money.manager.ex.servicelayer.RecurringTransactionService;
import com.money.manager.ex.utils.MmxDate;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import timber.log.Timber;

/*
 * Scheduled Transaction Services
 * create scheduled transaction forecast from single recurring transaction
 */
public class ScheduledTransactionForecastListServices {

    private final Context mContext;
    private int monthInAdvance = 12;
    private ScheduleTransactionForecastList scheduleTransactionForecastList;

    public ScheduledTransactionForecastListServices(Context context) {
        mContext = context;
        scheduleTransactionForecastList = new ScheduleTransactionForecastList();
    }

    public void setMonthInAdvance(int months) {
        monthInAdvance = months;
    }

    public CompletableFuture<ScheduleTransactionForecastList> createScheduledTransactionForecastAsync(Function f) {
        return CompletableFuture.supplyAsync(() -> {
            return createScheduledTransactionForecast();
        }).thenApply(f);
    }

    public ScheduleTransactionForecastList createScheduledTransactionForecast () {
        scheduleTransactionForecastList = new ScheduleTransactionForecastList();

        MmxDate endDate = new MmxDate();
        endDate.addMonth(monthInAdvance);
        QueryBillDeposits billDeposits = new QueryBillDeposits(mContext);

        Cursor cursor = null;
        try {
            cursor = mContext.getContentResolver().query(billDeposits.getUri(),
                    billDeposits.getAllColumns(),
                    null,
                    null,
                    QueryBillDeposits.NEXTOCCURRENCEDATE);
        } catch (Exception e) {
            Timber.d(e);
        }
        if (cursor == null ||
                cursor.getCount() == 0)
            return scheduleTransactionForecastList; // is empty

        while (cursor.moveToNext()) {
            @SuppressLint("Range") RecurringTransactionService recurringTransactionService = new RecurringTransactionService(cursor.getLong(cursor.getColumnIndex(QueryBillDeposits.BDID)), mContext);
            RecurringTransaction rx = recurringTransactionService.getSimulatedTransactionAsClone();
            Timber.d("Recurring Transaction: " + rx.toString());
            if (rx.getPaymentDate().after(endDate.toDate())) {
                // first occurence of this transaction is over cashflow visibility
                continue;
            }
            scheduleTransactionForecastList.add(rx); // add first entry of series
            while (recurringTransactionService.simulateMoveNext() && recurringTransactionService.getSimulatedTransaction().getPaymentDate().before(endDate.toDate())) {
                RecurringTransaction rx2 = recurringTransactionService.getSimulatedTransactionAsClone();
                // Timber.d("Recurring Transaction Occurrence: " + rx2.toString());
                scheduleTransactionForecastList.add(rx2);
            }

        }
        cursor.close();
        scheduleTransactionForecastList.orderByDateAscending();
        return scheduleTransactionForecastList;
    }

    public ScheduleTransactionForecastList getRecurringTransactions() {
        if (scheduleTransactionForecastList == null) createScheduledTransactionForecast();
        return scheduleTransactionForecastList;
    }

}
