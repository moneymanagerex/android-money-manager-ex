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

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;

import com.money.manager.ex.Constants;
import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.database.TableBillsDeposits;
import com.money.manager.ex.database.TableBudgetSplitTransactions;
import com.money.manager.ex.utils.DateUtils;

import java.util.ArrayList;
import java.util.Date;

/**
 * Represent a single Recurring Transaction object and provides related operations.
 */
public class RecurringTransaction {

    public RecurringTransaction(int recurringTransactionId, Context context){
        this.RecurringTransactionId = recurringTransactionId;
        this.Context = context;
    }

    public RecurringTransaction(Cursor cursor, Activity container) {
        mCursor = cursor;
        this.Context = container;
        this.RecurringTransactionId = this.getId();
    }

    public static final String LOGCAT = RecurringTransaction.class.getSimpleName();

    public int RecurringTransactionId;
    public Context Context;
    private Cursor mCursor;

    private TableBillsDeposits mRecurringTransaction = new TableBillsDeposits();
    private TableBudgetSplitTransactions mSplitCategories = new TableBudgetSplitTransactions();

    // Properties

    public int getId() {
        return mCursor.getInt(mCursor.getColumnIndex(TableBillsDeposits.BDID));
    }

    public int getRepeats() {
        return mCursor.getInt(mCursor.getColumnIndex(TableBillsDeposits.REPEATS));
    }

    // Methods

    /**
     * Skip next occurrence.
     * If this is the last occurrence, delete the recurring transaction.
     * Otherwise, move the due date to the next occurrence date.
     */
    public void skipNextOccurrence() {
        int repeats = this.getRepeats();

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

        int updateResult = this.Context.getContentResolver().update(mRecurringTransaction.getUri(), values,
                TableBillsDeposits.BDID + "=?",
                new String[]{Integer.toString(this.RecurringTransactionId)});

        if (updateResult > 0) {
            result = true;
        } else {
            Toast.makeText(this.Context.getApplicationContext(), R.string.db_update_failed, Toast.LENGTH_SHORT).show();
            Log.w(LOGCAT, "Update Bill Deposits with Id=" + Integer.toString(this.RecurringTransactionId) + " return <= 0");
        }

        return result;
    }

    public boolean setNextOccurrenceDate(Date nextOccurrenceDate) {
        // format the date into ISO
        String stringDate = DateUtils.getSQLiteStringDate(this.Context, nextOccurrenceDate);

        return this.setNextOccurrenceDate(stringDate);
    }

    /**
     * Set the recurring action's due date to the next occurrence.
     */
    public void moveNextOccurrenceForward() {
        int repeats = this.getRepeats();
        String currentNextOccurrence = mCursor.getString(mCursor.getColumnIndex(TableBillsDeposits.NEXTOCCURRENCEDATE));
        Date newNextOccurrence = DateUtils.getDateFromString(this.Context, currentNextOccurrence, Constants.PATTERN_DB_DATE);
        // calculate the next occurrence date
        newNextOccurrence = DateUtils.getDateNextOccurrence(newNextOccurrence, repeats);

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
        int deleteResult = this.Context.getContentResolver().delete(
                new TableBillsDeposits().getUri(),
                TableBillsDeposits.BDID + "=" + this.RecurringTransactionId, null);
        if (deleteResult == 0) {
            Toast.makeText(this.Context, R.string.db_delete_failed, Toast.LENGTH_SHORT).show();
            Log.w(LOGCAT, "Deleting recurring transaction " +
                    this.RecurringTransactionId + " failed.");
            result = false;
        }

        // result is true if deletion of related splits was successful.
        // result = result;

        return result;
    }

    /**
     * Delete any split categories for the current recurring transaction.
     * @return success
     */
    public boolean deleteSplitCategories() {
        boolean result = false;

        // first check if there are any records.
        Cursor query = this.getCursorForSplitTransactions();
        int existingRecords = query.getCount();
        if(existingRecords == 0) return true;

        // delete them

        int deleteResult = this.Context.getContentResolver().delete(
                mSplitCategories.getUri(),
                TableBudgetSplitTransactions.TRANSID + "=" + this.RecurringTransactionId, null);
        if (deleteResult != 0) {
            result = true;
        } else {
            Toast.makeText(this.Context, R.string.db_delete_failed, Toast.LENGTH_SHORT).show();
            Log.w(LOGCAT, "Deleting split categories for recurring transaction " +
                    this.RecurringTransactionId + " failed.");
        }

        return result;
    }

    /**
     * Load split transactions.
     * @return array list of all related split transactions
     */
    public ArrayList<TableBudgetSplitTransactions> loadSplitTransactions() {

        ArrayList<TableBudgetSplitTransactions> listSplitTrans = new ArrayList<>();

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
        return this.Context.getContentResolver().query(
                mSplitCategories.getUri(), null,
                TableBudgetSplitTransactions.TRANSID + "=" + Integer.toString(this.RecurringTransactionId),
                null,
                TableBudgetSplitTransactions.SPLITTRANSID);
    }
}
