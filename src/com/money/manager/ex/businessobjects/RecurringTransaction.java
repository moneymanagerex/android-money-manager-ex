/*
 * Copyright (C) 2012-2015 Money Manager Ex project
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

import android.app.Activity;
import android.content.ContentValues;
import android.util.Log;
import android.widget.Toast;

import com.money.manager.ex.R;
import com.money.manager.ex.database.TableBillsDeposits;
import com.money.manager.ex.database.TableBudgetSplitTransactions;

/**
 * Represent a single Recurring Transaction object and provides related operations.
 * Created by Alen on 9/04/2015.
 */
public class RecurringTransaction {

    public RecurringTransaction(int recurringTransactionId, Activity container){
        this.RecurringTransactionId = recurringTransactionId;
        this.Activity = container;
    }

    public static final String LOGCAT = RecurringTransaction.class.getSimpleName();
    public int RecurringTransactionId;
    public Activity Activity;

    private TableBillsDeposits mRecurringTransaction = new TableBillsDeposits();

    /**
     * Set the date for the current record.
     * @param nextOccurrenceDate
     * @return success
     */
    public boolean setNextOccurrenceDate(String nextOccurrenceDate) {
        boolean result = false;

        ContentValues values = new ContentValues();
        values.put(TableBillsDeposits.NEXTOCCURRENCEDATE, nextOccurrenceDate);

        if (this.Activity.getContentResolver().update(mRecurringTransaction.getUri(), values,
                TableBillsDeposits.BDID + "=?",
                new String[]{Integer.toString(this.RecurringTransactionId)}) > 0) {
            result = true;
        } else {
            Toast.makeText(this.Activity.getApplicationContext(), R.string.db_update_failed, Toast.LENGTH_SHORT).show();
            Log.w(LOGCAT, "Update Bill Deposits with Id=" + Integer.toString(this.RecurringTransactionId) + " return <= 0");
        }

        return result;
    }

    /**
     * Delete current recurring transaction record.
     * @return
     */
    public boolean delete() {
        boolean result = false;

        // Delete any related split transactions.
        if (this.Activity.getContentResolver().delete(
                new TableBudgetSplitTransactions().getUri(),
                TableBudgetSplitTransactions.TRANSID + "=" + this.RecurringTransactionId, null) != 0) {
            result = true;
        } else {
            Toast.makeText(this.Activity, R.string.db_delete_failed, Toast.LENGTH_SHORT).show();
        }

        // Delete recurring transactions.
        if (this.Activity.getContentResolver().delete(
                new TableBillsDeposits().getUri(),
                TableBillsDeposits.BDID + "=" + this.RecurringTransactionId, null) != 0) {
            result = result && true;
        } else {
            Toast.makeText(this.Activity, R.string.db_delete_failed, Toast.LENGTH_SHORT).show();
        }

        return result;
    }
}
