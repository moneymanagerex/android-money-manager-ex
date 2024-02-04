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
    public int recurringTransactionId = Constants.NOT_SET;
    private RecurringTransactionRepository mRepository;
    private RecurringTransaction mRecurringTransaction;

    public RecurringTransactionService(final Context context) {
        super(context);

    }

    public RecurringTransactionService(final int recurringTransactionId, final Context context) {
        super(context);

        this.recurringTransactionId = recurringTransactionId;
    }

    /**
     * @param date            to start calculate
     * @param repeatType      type of repeating transactions
     * @param numberOfPeriods Number of instances (days, months) parameter. Used for In (x) Days, for
     *                        example to indicate x.
     * @return next Date
     */
    public Date getNextScheduledDate(final Date date, Recurrence repeatType, Integer numberOfPeriods) {
        if (null == numberOfPeriods || Constants.NOT_SET == numberOfPeriods) {
            numberOfPeriods = 0;
        }

        if (200 <= repeatType.getValue()) {
            repeatType = Recurrence.valueOf(repeatType.getValue() - 200);
        } // set auto execute without user acknowledgement
        if (100 <= repeatType.getValue()) {
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
                final MmxDate lastDayOfMonth = result.lastDayOfMonth();
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
                final MmxDate lastDayOfMonth2 = result.lastDayOfMonth();
                if (!result.equals(lastDayOfMonth2)) {
                    // set to last day of the month
                    result = lastDayOfMonth2;
                } else {
                    result = lastDayOfMonth2.plusMonths(1);
                }
                // get the last day of the next month,
                // then iterate backwards until we are on a weekday.
                while (Calendar.SATURDAY == result.getDayOfWeek() ||
                        Calendar.SUNDAY == result.getDayOfWeek()) {
                    result = result.minusDays(1);
                }
                break;
        }
        return result.toDate();
    }

    public RecurringTransactionRepository getRepository() {
        if (null == mRepository) {
            mRepository = new RecurringTransactionRepository(getContext());
        }

        return mRepository;
    }

    public RecurringTransaction load(final int id) {
        return getRepository().load(id);
    }

    /**
     * This will process the Recurring Transaction so that the dates are moved to the next
     * occurrence, if it is to occur again.
     * If not, the recurring transaction is deleted.
     */
    public void moveNextOccurrence() {
        final RecurringTransaction tx = getRecurringTransaction();
        if (null == tx) {
            throw new IllegalArgumentException("Recurring Transaction is not set!");
        }

        final Integer recurrenceType = tx.getRecurrenceInt();
        if (null == recurrenceType) {
            final String message = getContext().getString(R.string.recurrence_type_not_set);
            throw new IllegalArgumentException(message);
        }

        /**
         * The action will depend on the transaction preferences.
         */
        final Recurrence recurrence = Recurrence.valueOf(recurrenceType);
        if (null == recurrence) {
            final String recurrenceTypeString = Integer.toString(recurrenceType);
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

        final RecurringTransactionRepository repo = getRepository();
        final boolean updated = repo.update(mRecurringTransaction);
        if (!updated) {
            new UIHelper(getContext()).showToast(R.string.error_saving_record);
        }
    }

    /**
     * Delete current recurring transaction record.
     *
     * @return success
     */
    public boolean delete() {
        boolean result;

        // Delete any related split transactions.
        result = deleteSplitCategories();
        // Exit if the deletion of splits failed.
        if (!result) return false;

        // Delete recurring transactions.
        final RecurringTransactionRepository repo = new RecurringTransactionRepository(getContext());
        final int deleteResult = repo.delete(recurringTransactionId);
//        int deleteResult = getContext().getContentResolver().delete(repo.getUri(),
//                RecurringTransaction.BDID + "=" + this.recurringTransactionId, null);
        if (0 == deleteResult) {
            Toast.makeText(getContext(), R.string.db_delete_failed, Toast.LENGTH_SHORT).show();
            Log.w(LOGCAT, "Deleting recurring transaction " +
                    recurringTransactionId + " failed.");
            result = false;
        }

        // result is true if deletion of related splits was successful.

        return result;
    }

    /**
     * Delete any split categories for the current recurring transaction.
     *
     * @return success
     */
    public boolean deleteSplitCategories() {
        boolean result = false;

        // first check if there are any records.
        final Cursor cursor = getCursorForSplitTransactions();
        if (null == cursor) return false;

        final int existingRecords = cursor.getCount();
        cursor.close();
        if (0 == existingRecords) {
            return true;
        }

        // delete them

        final SplitRecurringCategoriesRepository repo = new SplitRecurringCategoriesRepository(getContext());

        final int deleteResult = getContext().getContentResolver().delete(
                repo.getUri(),
                SplitRecurringCategory.TRANSID + "=" + recurringTransactionId, null);
        if (0 != deleteResult) {
            result = true;
        } else {
            Toast.makeText(getContext(), R.string.db_delete_failed, Toast.LENGTH_SHORT).show();
            Log.w(LOGCAT, "Deleting split categories for recurring transaction " +
                    recurringTransactionId + " failed.");
        }

        return result;
    }

    /**
     * Load split transactions.
     *
     * @return array list of all related split transactions
     */
    public ArrayList<ISplitTransaction> loadSplitTransactions() {
        final ArrayList<ISplitTransaction> result = new ArrayList<>();

        final Cursor cursor = getCursorForSplitTransactions();
        if (null == cursor) return result;

        while (cursor.moveToNext()) {
            final SplitRecurringCategory entity = new SplitRecurringCategory();
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
        if (200 <= repeat) {
            repeat = repeat - 200;
        }
        // set auto execute on the next occurrence
        if (100 <= repeat) {
            repeat = repeat - 100;
        }

        final String[] arrays = getContext().getResources().getStringArray(R.array.frequencies_items);
        if (null != arrays && 0 <= repeat && repeat <= arrays.length) {
            return arrays[repeat];
        }
        return "";
    }

    // Private.

    private void decreasePaymentsLeft() {
        final RecurringTransaction tx = getRecurringTransaction();

        Integer paymentsLeft = tx.getPaymentsLeft();
        if (null == paymentsLeft) {
            tx.setPaymentsLeft(0);
            return;
        }

        if (1 < paymentsLeft) {
            paymentsLeft = paymentsLeft - 1;
        }

        tx.setPaymentsLeft(paymentsLeft);
    }

    private void deleteIfLastPayment() {
        final RecurringTransaction tx = getRecurringTransaction();

        final Integer paymentsLeft = tx.getPaymentsLeft();
        if (null == paymentsLeft) {
            tx.setPaymentsLeft(0);
            return;
        }

        if (1 == paymentsLeft) {
            delete();
        }
    }

    /**
     * Creates a query for getting all related split transactions.
     *
     * @return cursor for all the related split transactions
     */
    private Cursor getCursorForSplitTransactions() {
        final SplitRecurringCategoriesRepository repo = new SplitRecurringCategoriesRepository(getContext());

        return getContext().getContentResolver().query(
                repo.getUri(),
                null,
                SplitRecurringCategory.TRANSID + "=" + recurringTransactionId,
                null,
                SplitRecurringCategory.SPLITTRANSID);
    }

    private RecurringTransaction getRecurringTransaction() {
        if (null == mRecurringTransaction) {
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

        final RecurringTransaction tx = getRecurringTransaction();
        final Recurrence repeatType = Recurrence.valueOf(tx.getRecurrenceInt());
        Date newPaymentDate = tx.getPaymentDate();
        final Integer paymentsLeft = tx.getPaymentsLeft();

        // calculate the next payment date
        newPaymentDate = getNextScheduledDate(newPaymentDate, repeatType, paymentsLeft);

        if (null != newPaymentDate) {
            tx.setPaymentDate(newPaymentDate);
        }
    }

    private void moveDueDateForward() {
        final RecurringTransaction tx = getRecurringTransaction();

        final Recurrence repeats = Recurrence.valueOf(tx.getRecurrenceInt());
        final Date dueDate = tx.getDueDate();
        final Integer paymentsLeft = tx.getPaymentsLeft();

        final Date newDueDate = getNextScheduledDate(dueDate, repeats, paymentsLeft);

        if (null != newDueDate) {
            tx.setDueDate(newDueDate);
        }
    }
}
