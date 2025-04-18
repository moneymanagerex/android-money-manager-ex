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

//    private int monthInAdvance = 12;
    private MmxDate mDateTo;
    private ScheduleTransactionForecastList scheduleTransactionForecastList = null;
    private Boolean isReady;

    private static ScheduledTransactionForecastListServices mInstance = null;

    public static ScheduledTransactionForecastListServices getInstance() {
        if ( mInstance == null ) {
            mInstance = new ScheduledTransactionForecastListServices();
        }
        return mInstance;
    }

    public static void destroyInstance() {
        Timber.d("ScheduledTransactionForecastListServices: destroy instance");
        mInstance = null;
    }

    public Boolean isReady() {
        return isReady && (scheduleTransactionForecastList != null);
    }

    public ScheduledTransactionForecastListServices() {
        isReady = false;
        setMonthInAdvance(12); // default 12 months
    }

    public ScheduledTransactionForecastListServices setMonthInAdvance(int months) {
        return setDateTo(new MmxDate().addMonth(months));
    }

    public ScheduledTransactionForecastListServices setDateTo(MmxDate dateTo) {
        if ( mDateTo != null && mDateTo.toDate().before(dateTo.toDate())) {
            Timber.d("ScheduledTransactionForecastListServices: Request new DateTo [%s] before actual cached value [%s]", dateTo.toIsoDateString(), mDateTo.toIsoDateString());
            return this; // no change
        }
        Timber.d("ScheduledTransactionForecastListServices: start new DateTo [%s] invalidating cache.", dateTo.toIsoDateString());
        mDateTo = dateTo;
        isReady = false;
        // invalidate scheduleTransactionForecastList
        scheduleTransactionForecastList = null;
        return this;
    }


    public CompletableFuture createScheduledTransactionForecastAsync(Context context, Function f) {
        return CompletableFuture.supplyAsync(() -> {
            Timber.d("ScheduledTransactionForecastListServices: Start compute forecast.");
            isReady = false;
            createScheduledTransactionForecast(context);
            scheduleTransactionForecastList.populateCacheForCategory();
            isReady = true;
            Timber.d("ScheduledTransactionForecastListServices: End compute forecast.");
            return scheduleTransactionForecastList;
        }).thenApply(f);

    }

    public ScheduleTransactionForecastList createScheduledTransactionForecast (Context context) {
        scheduleTransactionForecastList = new ScheduleTransactionForecastList();
        if (mDateTo == null) return scheduleTransactionForecastList;

        QueryBillDeposits billDeposits = new QueryBillDeposits(context);

        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(billDeposits.getUri(),
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
            @SuppressLint("Range") RecurringTransactionService recurringTransactionService = new RecurringTransactionService(cursor.getLong(cursor.getColumnIndexOrThrow(QueryBillDeposits.BDID)), context);
            RecurringTransaction rx = recurringTransactionService.getSimulatedTransactionAsClone();
            Timber.d("Recurring Transaction: %s", rx.toString());
            if (rx.getPaymentDate().after(mDateTo.toDate())) {
                // first occurrence of this transaction is over cash flow visibility
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
