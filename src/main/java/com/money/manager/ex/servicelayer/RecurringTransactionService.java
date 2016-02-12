/*
 * Copyright (C) 2012-2016 The Android Money Manager Ex Project Team
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
package com.money.manager.ex.servicelayer;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;

import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.database.ITransactionEntity;
import com.money.manager.ex.datalayer.RecurringTransactionRepository;
import com.money.manager.ex.datalayer.SplitRecurringCategoriesRepository;
import com.money.manager.ex.domainmodel.RecurringTransaction;
import com.money.manager.ex.domainmodel.SplitRecurringCategory;
import com.money.manager.ex.utils.DateUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Represent a first Recurring Transaction object and provides related operations.
 */
public class RecurringTransactionService
    extends ServiceBase {

    public static final String LOGCAT = RecurringTransactionService.class.getSimpleName();

    public RecurringTransactionService(Context context){
        super(context);

    }

    public RecurringTransactionService(int recurringTransactionId, Context context){
        super(context);

        this.recurringTransactionId = recurringTransactionId;
    }

    public int recurringTransactionId = Constants.NOT_SET;

    private RecurringTransactionRepository mRepository;
    private RecurringTransaction mRecurringTransaction;

    /**
     * @param date    to start calculate
     * @param repeatType type of repeating transactions
     * @param paymentsLeft Number of instances (days, months) parameter. Used for In (x) Days, for
     *                  example to indicate x.
     * @return next Date
     */
    public Date getNextScheduledDate(Date date, int repeatType, Integer paymentsLeft) {
        if (paymentsLeft == null || paymentsLeft == Constants.NOT_SET) {
            paymentsLeft = 0;
        }

        if (repeatType >= 200) {
            repeatType = repeatType - 200;
        } // set auto execute without user acknowledgement
        if (repeatType >= 100) {
            repeatType = repeatType - 100;
        } // set auto execute on the next occurrence

        // create object calendar
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        switch (repeatType) {
            case 0: //none
                break;
            case 1: //weekly
                calendar.add(Calendar.DATE, 7);
                break;
            case 2: //bi_weekly
                calendar.add(Calendar.DATE, 14);
                break;
            case 3: //monthly
                calendar.add(Calendar.MONTH, 1);
                break;
            case 4: //bi_monthly
                calendar.add(Calendar.MONTH, 2);
                break;
            case 5: //quarterly
                calendar.add(Calendar.MONTH, 3);
                break;
            case 6: //half_year
                calendar.add(Calendar.MONTH, 6);
                break;
            case 7: //yearly
                calendar.add(Calendar.YEAR, 1);
                break;
            case 8: //four_months
                calendar.add(Calendar.MONTH, 4);
                break;
            case 9: //four_weeks
                calendar.add(Calendar.DATE, 28);
                break;
            case 10: //daily
                calendar.add(Calendar.DATE, 1);
                break;
            case 11: //in_x_days
            case 13: //every_x_days
                calendar.add(Calendar.DATE, paymentsLeft);
                break;
            case 12: //in_x_months
            case 14: //every_x_months
                calendar.add(Calendar.MONTH, paymentsLeft);
                break;
            case 15: //month (last day)
                calendar.add(Calendar.MONTH, 1);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                calendar.add(Calendar.DATE, -1);
                break;
            case 16: //month (last business day)
                // get the last day of the month first.
                calendar.add(Calendar.MONTH, 1);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                calendar.add(Calendar.DATE, -1);
                // now iterate backwards until we are not on weekend day.
                while(calendar.equals(Calendar.SATURDAY) || calendar.equals(Calendar.SUNDAY)) {
                    calendar.add(Calendar.DATE, -1);
                }
                break;
        }
        return calendar.getTime();
    }

    public RecurringTransactionRepository getRepository(){
        if (mRepository == null) {
            mRepository = new RecurringTransactionRepository(getContext());
        }

        return mRepository;
    }

    public RecurringTransaction load(int id) {
        return getRepository().load(id);
    }

    /**
     * Skip next occurrence.
     * If this is the last occurrence, delete the recurring transaction.
     * Otherwise, move the due date to the next occurrence date.
     */
    public void skipNextOccurrence() {
        load();

        int repeats = mRecurringTransaction.getRepeats();

        if(repeats == 0) {
            // no more occurrences, this is the only one. Delete the transaction.
            delete();
        } else {
            // Just move the date.
            moveDatesForward();
        }
    }

    /**
     * Set the recurring transaction's Due date and the Payment date to the next occurrence.
     * Saves changes to the database.
     */
    public void moveDatesForward() {
        load();

        // Due date.

        moveDueDateForward();

        // Payment date.

        int repeatType = mRecurringTransaction.getRepeats();
        Date newPaymentDate = mRecurringTransaction.getPaymentDate();
        Integer paymentsLeft = mRecurringTransaction.getNumOccurrences();

        // calculate the next payment date
        newPaymentDate = getNextScheduledDate(newPaymentDate, repeatType, paymentsLeft);

        if (newPaymentDate != null) {
            mRecurringTransaction.setPaymentDate(newPaymentDate);
        }

        // Save changes

        RecurringTransactionRepository repo = getRepository();
        boolean updated = repo.update(mRecurringTransaction);
        if (!updated) {
            Core.alertDialog(getContext(), R.string.error_saving_record);
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
        int deleteResult = getContext().getContentResolver().delete(
                new RecurringTransactionRepository(getContext()).getUri(),
                RecurringTransaction.BDID + "=" + this.recurringTransactionId, null);
        if (deleteResult == 0) {
            Toast.makeText(getContext(), R.string.db_delete_failed, Toast.LENGTH_SHORT).show();
            Log.w(LOGCAT, "Deleting recurring transaction " +
                    this.recurringTransactionId + " failed.");
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

        SplitRecurringCategoriesRepository repo = new SplitRecurringCategoriesRepository(getContext());

        int deleteResult = getContext().getContentResolver().delete(
            repo.getUri(),
            SplitRecurringCategory.TRANSID + "=" + this.recurringTransactionId, null);
        if (deleteResult != 0) {
            result = true;
        } else {
            Toast.makeText(getContext(), R.string.db_delete_failed, Toast.LENGTH_SHORT).show();
            Log.w(LOGCAT, "Deleting split categories for recurring transaction " +
                    this.recurringTransactionId + " failed.");
        }

        return result;
    }

    /**
     * Load split transactions.
     * @return array list of all related split transactions
     */
    public ArrayList<ITransactionEntity> loadSplitTransactions() {
        ArrayList<ITransactionEntity> result = new ArrayList<>();

        Cursor cursor = this.getCursorForSplitTransactions();
        if (cursor == null) return result;

        while (cursor.moveToNext()) {
            SplitRecurringCategory entity = new SplitRecurringCategory();
            entity.loadFromCursor(cursor);

            result.add(entity);
        }

        return result;
    }

    // Private.

    /**
     * Creates a query for getting all related split transactions.
     * @return cursor for all the related split transactions
     */
    private Cursor getCursorForSplitTransactions(){
        SplitRecurringCategoriesRepository repo = new SplitRecurringCategoriesRepository(getContext());

        return getContext().getContentResolver().query(
            repo.getUri(),
            null,
            SplitRecurringCategory.TRANSID + "=" + Integer.toString(this.recurringTransactionId),
            null,
            SplitRecurringCategory.SPLITTRANSID);
    }

    private boolean load() {
        if (mRecurringTransaction != null) return true;

        RecurringTransactionRepository repo = getRepository();

        mRecurringTransaction = repo.load(this.recurringTransactionId);

        return (mRecurringTransaction == null);
    }

    private void moveDueDateForward() {
        int repeats = mRecurringTransaction.getRepeats();
        Date dueDate = mRecurringTransaction.getDueDate();
        Integer paymentsLeft = mRecurringTransaction.getNumOccurrences();

        Date newDueDate = getNextScheduledDate(dueDate, repeats, paymentsLeft);

        if (newDueDate != null) {
            mRecurringTransaction.setDueDate(newDueDate);
        }
    }
}
