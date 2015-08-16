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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;

import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.database.ISplitTransactionsDataset;
import com.money.manager.ex.database.RecurringTransactionRepository;
import com.money.manager.ex.database.TableBillsDeposits;
import com.money.manager.ex.database.TableBudgetSplitTransactions;
import com.money.manager.ex.utils.DateUtils;

import java.util.ArrayList;
import java.util.Date;

/**
 * Represent a single Recurring Transaction object and provides related operations.
 */
public class RecurringTransactionService {

    public RecurringTransactionService(int recurringTransactionId, Context context){
        this.RecurringTransactionId = recurringTransactionId;
        this.mContext = context;
    }

    public static final String LOGCAT = RecurringTransactionService.class.getSimpleName();

    public int RecurringTransactionId = Constants.NOT_SET;
    public Context mContext;

    private TableBillsDeposits mRecurringTransaction;
    private TableBudgetSplitTransactions mSplitCategories = new TableBudgetSplitTransactions();

    /**
     * Skip next occurrence.
     * If this is the last occurrence, delete the recurring transaction.
     * Otherwise, move the due date to the next occurrence date.
     */
    public void skipNextOccurrence() {
        this.load();

        int repeats = mRecurringTransaction.repeats;

        if(repeats == 0) {
            // no more occurrences, this is the only one. Delete the transaction.
            this.delete();
        } else {
            // Just move the date.
            this.moveNextOccurrenceForward();
        }
    }

    /**
     * Set the date for the current record.
     * @param nextOccurrenceDate ISO-formatted string representation of the date. i.e. 2015-05-25
     * @return success
     */
    public boolean setNextOccurrenceDate(String nextOccurrenceDate) {
        boolean result = false;

        ContentValues values = new ContentValues();
        values.put(TableBillsDeposits.NEXTOCCURRENCEDATE, nextOccurrenceDate);

        int updateResult = mContext.getContentResolver().update(mRecurringTransaction.getUri(), values,
                TableBillsDeposits.BDID + "=?",
                new String[]{Integer.toString(this.RecurringTransactionId)});

        if (updateResult > 0) {
            result = true;
        } else {
            Toast.makeText(mContext.getApplicationContext(), R.string.db_update_failed, Toast.LENGTH_SHORT).show();
            Log.w(LOGCAT, "Update Bill Deposits with Id=" + Integer.toString(this.RecurringTransactionId) + " return <= 0");
        }

        return result;
    }

    public boolean setNextOccurrenceDate(Date nextOccurrenceDate) {
        // format the date into ISO
        String stringDate = DateUtils.getSQLiteStringDate(mContext, nextOccurrenceDate);

        return this.setNextOccurrenceDate(stringDate);
    }

    /**
     * Set the recurring action's due date to the next occurrence.
     */
    public void moveNextOccurrenceForward() {
        this.load();

        int repeats = mRecurringTransaction.repeats;
        String currentNextOccurrence = mRecurringTransaction.nextOccurrence;
        Date newNextOccurrence = DateUtils.getDateFromString(mContext, currentNextOccurrence, Constants.PATTERN_DB_DATE);
        int instances = mRecurringTransaction.numOccurrence;
        // calculate the next occurrence date
        newNextOccurrence = DateUtils.getDateNextOccurrence(newNextOccurrence, repeats, instances);

        if (newNextOccurrence != null) {
            this.setNextOccurrenceDate(newNextOccurrence);
        }
    }

    /**
     * Delete current recurring transaction record.
     * @return success
     */
    public boolean delete() {
        boolean result;

        // Delete any related split transactions.
        result = this.deleteSplitCategories();
        // Exit if the deletion of splits failed.
        if(!result) return false;

        // Delete recurring transactions.
        int deleteResult = mContext.getContentResolver().delete(
                new TableBillsDeposits().getUri(),
                TableBillsDeposits.BDID + "=" + this.RecurringTransactionId, null);
        if (deleteResult == 0) {
            Toast.makeText(mContext, R.string.db_delete_failed, Toast.LENGTH_SHORT).show();
            Log.w(LOGCAT, "Deleting recurring transaction " +
                    this.RecurringTransactionId + " failed.");
            result = false;
        }

        // result is true if deletion of related splits was successful.

        return result;
    }

    /**
     * Delete any split categories for the current recurring transaction.
     * @return success
     */
    public boolean deleteSplitCategories() {
        boolean result = false;

        // first check if there are any records.
        Cursor cursor = this.getCursorForSplitTransactions();
        if (cursor == null) return false;

        int existingRecords = cursor.getCount();
        if(existingRecords == 0) return true;

        // delete them

        int deleteResult = mContext.getContentResolver().delete(
                mSplitCategories.getUri(),
                TableBudgetSplitTransactions.TRANSID + "=" + this.RecurringTransactionId, null);
        if (deleteResult != 0) {
            result = true;
        } else {
            Toast.makeText(mContext, R.string.db_delete_failed, Toast.LENGTH_SHORT).show();
            Log.w(LOGCAT, "Deleting split categories for recurring transaction " +
                    this.RecurringTransactionId + " failed.");
        }

        return result;
    }

    /**
     * Load split transactions.
     * @return array list of all related split transactions
     */
    public ArrayList<ISplitTransactionsDataset> loadSplitTransactions() {
        ArrayList<ISplitTransactionsDataset> listSplitTrans = new ArrayList<>();

        Cursor curSplit = this.getCursorForSplitTransactions();

        if (curSplit != null && curSplit.moveToFirst()) {
            while (!curSplit.isAfterLast()) {
                TableBudgetSplitTransactions obj = new TableBudgetSplitTransactions();
                obj.setValueFromCursor(curSplit);

                listSplitTrans.add(obj);

                curSplit.moveToNext();
            }
        }

        return listSplitTrans;
    }

    /**
     * Creates a query for getting all related split transactions.
     * @return cursor for all the related split transactions
     */
    private Cursor getCursorForSplitTransactions(){
        return mContext.getContentResolver().query(
                mSplitCategories.getUri(), null,
                TableBudgetSplitTransactions.TRANSID + "=" + Integer.toString(this.RecurringTransactionId),
                null,
                TableBudgetSplitTransactions.SPLITTRANSID);
    }

    private boolean load() {
        if (mRecurringTransaction != null) return true;

        RecurringTransactionRepository repo = new RecurringTransactionRepository(mContext);
        mRecurringTransaction = repo.load(this.RecurringTransactionId);

        return (mRecurringTransaction == null);
    }
}
