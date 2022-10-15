/*
 * Copyright (C) 2012-2018 The Android Money Manager Ex Project Team
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
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.database.ISplitTransaction;
import com.money.manager.ex.datalayer.RecurringTransactionRepository;
import com.money.manager.ex.datalayer.SplitRecurringCategoriesRepository;
import com.money.manager.ex.domainmodel.RecurringTransaction;
import com.money.manager.ex.domainmodel.SplitRecurringCategory;
import com.money.manager.ex.recurring.transactions.Recurrence;
import com.money.manager.ex.utils.MmxDate;

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
     * @param numberOfPeriods Number of instances (days, months) parameter. Used for In (x) Days, for
     *                  example to indicate x.
     * @return next Date
     */
    public Date getNextScheduledDate(Date date, Recurrence repeatType, Integer numberOfPeriods) {
        if (numberOfPeriods == null || numberOfPeriods == Constants.NOT_SET) {
            numberOfPeriods = 0;
        }

        if (repeatType.getValue() >= 200) {
            repeatType = Recurrence.valueOf(repeatType.getValue() - 200);
        } // set auto execute without user acknowledgement
        if (repeatType.getValue() >= 100) {
            repeatType = Recurrence.valueOf(repeatType.getValue() - 100);
        } // set auto execute on the next occurrence

        MmxDate result = new MmxDate(date);

        switch (repeatType) {
            case ONCE: //none
                break;
            case WEEKLY: //weekly
                result = result.plusWeeks(1);
                break;
            case BIWEEKLY: //bi_weekly
                result = result.plusWeeks(2);
                break;
            case MONTHLY: //monthly
                result = result.plusMonths(1);
                break;
            case BIMONTHLY: //bi_monthly
                result = result.plusMonths(2);
                break;
            case QUARTERLY: //quarterly
                result = result.plusMonths(3);
                break;
            case SEMIANNUALLY: //half_year
                result = result.plusMonths(6);
                break;
            case ANNUALLY: //yearly
                result = result.plusYears(1);
                break;
            case FOUR_MONTHS: //four_months
                result = result.plusMonths(4);
                break;
            case FOUR_WEEKS: //four_weeks
                result = result.plusWeeks(4);
                break;
            case DAILY: //daily
                result = result.plusDays(1);
                break;
            case IN_X_DAYS: //in_x_days
            case EVERY_X_DAYS: //every_x_days
                result = result.plusDays(numberOfPeriods);
                break;
            case IN_X_MONTHS: //in_x_months
            case EVERY_X_MONTHS: //every_x_months
                result = result.plusMonths(numberOfPeriods);
                break;

            case MONTHLY_LAST_DAY: //month (last day)
                // if the date is not the last day of this month, set it to the end of the month.
                // else set it to the end of the next month.
                MmxDate lastDayOfMonth = result.lastDayOfMonth();
                if (!result.equals(lastDayOfMonth)) {
                    // set to last day of the month
                    result = lastDayOfMonth;
                } else {
                    result = lastDayOfMonth.plusMonths(1);
                }
                break;

            case MONTHLY_LAST_BUSINESS_DAY: //month (last business day)
                // if the date is not the last day of this month, set it to the end of the month.
                // else set it to the end of the next month.
                MmxDate lastDayOfMonth2 = result.lastDayOfMonth();
                if (!result.equals(lastDayOfMonth2)) {
                    // set to last day of the month
                    result = lastDayOfMonth2;
                } else {
                    result = lastDayOfMonth2.plusMonths(1);
                }
                // get the last day of the next month,
                // then iterate backwards until we are on a weekday.
                while(result.getDayOfWeek() == Calendar.SATURDAY ||
                        result.getDayOfWeek() == Calendar.SUNDAY) {
                    result = result.minusDays(1);
                }
                break;
        }
        return result.toDate();
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
     * This will process the Recurring Transaction so that the dates are moved to the next
     * occurrence, if it is to occur again.
     * If not, the recurring transaction is deleted.
     */
    public void moveNextOccurrence() {
        RecurringTransaction tx = getRecurringTransaction();
        if (tx == null) {
            throw new IllegalArgumentException("Recurring Transaction is not set!");
        }

        Integer recurrenceType = tx.getRecurrenceInt();
        if (recurrenceType == null) {
            String message = getContext().getString(R.string.recurrence_type_not_set);
            throw new IllegalArgumentException(message);
        }

        /**
         * The action will depend on the transaction preferences.
         */
        Recurrence recurrence = Recurrence.valueOf(recurrenceType);
        if (recurrence == null) {
            String recurrenceTypeString = Integer.toString(recurrenceType);
            throw new IllegalArgumentException(getContext().getString(R.string.invalid_recurrence_type)
                    + " " + recurrenceTypeString);
        }

        switch (recurrence) {
            // periodical (monthly, weekly)
            case ONCE:
                delete();
                // exit now.
                return;

            case WEEKLY:
            case BIWEEKLY:
            case MONTHLY:
            case BIMONTHLY:
            case QUARTERLY:
            case SEMIANNUALLY:
            case ANNUALLY:
            case FOUR_MONTHS:
            case FOUR_WEEKS:
            case DAILY:
            case MONTHLY_LAST_DAY:
            case MONTHLY_LAST_BUSINESS_DAY:
                moveDatesForward();
                // Delete if occurrence is down to 1. 0 means repeat forever.
                deleteIfLastPayment();
                decreasePaymentsLeft();
                break;
            // every n periods
            case EVERY_X_DAYS:
            case EVERY_X_MONTHS:
                moveDatesForward();
                break;
            // in n periods
            case IN_X_DAYS:
            case IN_X_MONTHS:
                // reset number of periods
                mRecurringTransaction.setPaymentsLeft(Constants.NOT_SET);
                break;
            default:
                break;
        }

        // Save changes

        RecurringTransactionRepository repo = getRepository();
        boolean updated = repo.update(mRecurringTransaction);
        if (!updated) {
            new UIHelper(getContext()).showToast(R.string.error_saving_record);
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
        RecurringTransactionRepository repo = new RecurringTransactionRepository(getContext());
        int deleteResult = repo.delete(this.recurringTransactionId);
//        int deleteResult = getContext().getContentResolver().delete(repo.getUri(),
//                RecurringTransaction.BDID + "=" + this.recurringTransactionId, null);
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
        cursor.close();
        if(existingRecords == 0) {
            return true;
        }

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
    public ArrayList<ISplitTransaction> loadSplitTransactions() {
        ArrayList<ISplitTransaction> result = new ArrayList<>();

        Cursor cursor = this.getCursorForSplitTransactions();
        if (cursor == null) return result;

        while (cursor.moveToNext()) {
            SplitRecurringCategory entity = new SplitRecurringCategory();
            entity.loadFromCursor(cursor);

            result.add(entity);
        }
        cursor.close();

        return result;
    }

    /**
     * @param repeat frequency repeats
     * @return frequency
     */
    public String getRecurrenceLocalizedName(int repeat) {
        // set auto execute without user acknowledgement
        if (repeat >= 200) {
            repeat = repeat - 200;
        }
        // set auto execute on the next occurrence
        if (repeat >= 100) {
            repeat = repeat - 100;
        }

        String[] arrays = getContext().getResources().getStringArray(R.array.frequencies_items);
        if (arrays != null && repeat >= 0 && repeat <= arrays.length) {
            return arrays[repeat];
        }
        return "";
    }

    // Private.

    private void decreasePaymentsLeft() {
        RecurringTransaction tx = getRecurringTransaction();

        Integer paymentsLeft = tx.getPaymentsLeft();
        if (paymentsLeft == null) {
            tx.setPaymentsLeft(0);
            return;
        }

        if (paymentsLeft > 1) {
            paymentsLeft = paymentsLeft - 1;
        }

        tx.setPaymentsLeft(paymentsLeft);
    }

    private void deleteIfLastPayment() {
        RecurringTransaction tx = getRecurringTransaction();

        Integer paymentsLeft = tx.getPaymentsLeft();
        if (paymentsLeft == null) {
            tx.setPaymentsLeft(0);
            return;
        }

        if (paymentsLeft == 1) {
            delete();
        }
    }

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

    private RecurringTransaction getRecurringTransaction() {
        if (mRecurringTransaction == null) {
            mRecurringTransaction = getRepository().load(recurringTransactionId);
        }
        return mRecurringTransaction;
    }

    /**
     * Set the recurring transaction's Due date and the Payment date to the next occurrence.
     * Saves changes to the database.
     */
    private void moveDatesForward() {
        // Due date.

        moveDueDateForward();

        // Payment date.

        RecurringTransaction tx = getRecurringTransaction();
        Recurrence repeatType = Recurrence.valueOf(tx.getRecurrenceInt());
        Date newPaymentDate = tx.getPaymentDate();
        Integer paymentsLeft = tx.getPaymentsLeft();

        // calculate the next payment date
        newPaymentDate = getNextScheduledDate(newPaymentDate, repeatType, paymentsLeft);

        if (newPaymentDate != null) {
            tx.setPaymentDate(newPaymentDate);
        }
    }

    private void moveDueDateForward() {
        RecurringTransaction tx = getRecurringTransaction();

        Recurrence repeats = Recurrence.valueOf(tx.getRecurrenceInt());
        Date dueDate = tx.getDueDate();
        Integer paymentsLeft = tx.getPaymentsLeft();

        Date newDueDate = getNextScheduledDate(dueDate, repeats, paymentsLeft);

        if (newDueDate != null) {
            tx.setDueDate(newDueDate);
        }
    }
}
