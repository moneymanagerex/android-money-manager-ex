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

    private Context mContext;
//    private int monthInAdvance = 12;
    private MmxDate mDateTo;
    private ScheduleTransactionForecastList scheduleTransactionForecastList = null;
    private Boolean isReady = false;

    private static ScheduledTransactionForecastListServices mInstance = null;

    public static ScheduledTransactionForecastListServices getInstance(Context context) {
        if ( mInstance == null ) {
            mInstance = new ScheduledTransactionForecastListServices(context);
        }
        return mInstance;
    }

    public ScheduledTransactionForecastListServices setContext(Context context) {
        mContext = context;
        return this;
    }

    public static void destroyInstance() {
        mInstance = null;
    }

    public Boolean isReady() {
        return isReady && (scheduleTransactionForecastList != null);
    }

    public ScheduledTransactionForecastListServices() {
    }

    public ScheduledTransactionForecastListServices(Context context) {
        mContext = context;
        isReady = false;
        setMonthInAdvance(12); // default 12 months
    }

    public ScheduledTransactionForecastListServices setMonthInAdvance(int months) {
        return setDateTo(new MmxDate().addMonth(months));
    }

    public ScheduledTransactionForecastListServices setDateTo(MmxDate dateTo) {
        if ( mDateTo != null && mDateTo.toDate().before(dateTo.toDate())) return this; // no change
        mDateTo = dateTo;
        isReady = false;
        // invalidate scheduleTransactionForecastList
        scheduleTransactionForecastList = null;
        return this;
    }


    public CompletableFuture<ScheduleTransactionForecastList> createScheduledTransactionForecastAsync(Function f) {
        return CompletableFuture.supplyAsync(() -> {
            isReady = false;
            createScheduledTransactionForecast();
            scheduleTransactionForecastList.populateCacheForCategory();
            isReady = true;
            return scheduleTransactionForecastList;
        }).thenApply(f);
    }

    public ScheduleTransactionForecastList createScheduledTransactionForecast () {
        scheduleTransactionForecastList = new ScheduleTransactionForecastList();
        if (mDateTo == null) return scheduleTransactionForecastList;

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
            if (rx.getPaymentDate().after(mDateTo.toDate())) {
                // first occurence of this transaction is over cashflow visibility
                continue;
            }
            scheduleTransactionForecastList.add(rx); // add first entry of series
            while (recurringTransactionService.simulateMoveNext() && recurringTransactionService.getSimulatedTransaction().getPaymentDate().before(mDateTo.toDate())) {
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
        return scheduleTransactionForecastList;
    }

}
