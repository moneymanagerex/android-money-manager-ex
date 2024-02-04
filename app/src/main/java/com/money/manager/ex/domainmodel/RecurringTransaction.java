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

package com.money.manager.ex.domainmodel;

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.text.TextUtils;

import com.money.manager.ex.Constants;
import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.database.ITransactionEntity;
import com.money.manager.ex.recurring.transactions.Recurrence;
import com.money.manager.ex.utils.MmxDate;

import org.parceler.Parcel;

import java.util.Date;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

/**
 * Recurring Transaction
 */
@Parcel
public class RecurringTransaction
        extends EntityBase
        implements ITransactionEntity {

    public static final String BDID = "BDID";
    public static final String REPEATS = "REPEATS";
    /**
     * Payment Date
     */
    public static final String NEXTOCCURRENCEDATE = "NEXTOCCURRENCEDATE";
    /**
     * Repetitions / Payments Left
     */
    public static final String NUMOCCURRENCES = "NUMOCCURRENCES";

    public RecurringTransaction() {

        setPayeeId(Constants.NOT_SET);
        setCategoryId(Constants.NOT_SET);
        setRecurrence(Recurrence.ONCE);
        setTransactionType(TransactionTypes.Withdrawal);
    }

    public static RecurringTransaction createInstance() {
        final RecurringTransaction tx = new RecurringTransaction();

        tx.setAmount(MoneyFactory.fromDouble(0));
        tx.setAmountTo(MoneyFactory.fromDouble(0));

        return tx;
    }

    @Override
    public void loadFromCursor(final Cursor c) {
        super.loadFromCursor(c);

        // Reload all money values.
        DatabaseUtils.cursorDoubleToContentValuesIfPresent(c, contentValues, ITransactionEntity.TRANSAMOUNT);
        DatabaseUtils.cursorDoubleToContentValuesIfPresent(c, contentValues, ITransactionEntity.TOTRANSAMOUNT);
    }

    public Integer getId() {
        return getInt(BDID);
    }

    public void setId(final Integer id) {
        setInt(BDID, id);
    }

    public boolean hasId() {
        return null != getId() && Constants.NOT_SET != getId();
    }

    public Integer getAccountId() {
        return getInt(ITransactionEntity.ACCOUNTID);
    }

    public void setAccountId(final Integer value) {
        setInt(ITransactionEntity.ACCOUNTID, value);
    }

    @Override
    public Integer getAccountToId() {
        return getInt(ITransactionEntity.TOACCOUNTID);
    }

    @Override
    public void setAccountToId(final Integer value) {
        setInt(ITransactionEntity.TOACCOUNTID, value);
    }

    public boolean hasAccountTo() {
        return null != getAccountToId() && Constants.NOT_SET != getAccountToId();
    }

    public Money getAmount() {
        Double amount = getDouble(ITransactionEntity.TRANSAMOUNT);
        if (null == amount) {
            amount = 0.0D;
        }
        final Money result = MoneyFactory.fromDouble(amount);
        return result;
    }

    public void setAmount(final Money value) {
        setMoney(ITransactionEntity.TRANSAMOUNT, value);
    }

    public Money getAmountTo() {
        Double amount = getDouble(ITransactionEntity.TOTRANSAMOUNT);
        if (null == amount) {
            amount = 0.0D;
        }
        final Money result = MoneyFactory.fromDouble(amount);
        return result;
    }

    public void setAmountTo(final Money value) {
        setMoney(ITransactionEntity.TOTRANSAMOUNT, value);
    }

    @Override
    public Integer getCategoryId() {
        return getInt(ITransactionEntity.CATEGID);
    }

    @Override
    public void setCategoryId(final Integer value) {
        setInt(ITransactionEntity.CATEGID, value);
    }

    public boolean hasCategory() {
        return null != getCategoryId() && Constants.NOT_SET != getCategoryId();
    }

    public String getDueDateString() {
        return getString(TRANSDATE);
    }

    public Date getDueDate() {
        final String dateString = getDueDateString();
        if (TextUtils.isEmpty(dateString)) {
            return null;
        }

        return new MmxDate(dateString).toDate();
    }

    public void setDueDate(final Date value) {
        setDate(TRANSDATE, value);
    }

    /**
     * For recurring transaction, the date is Due Date.
     *
     * @return Due date of the transaction.
     */
    public Date getDate() {
        return getDueDate();
    }

    /**
     * Due Date
     *
     * @param value Date to set
     */
    public void setDate(final Date value) {
        setDueDate(value);
    }

    public String getDateString() {
        return getDueDateString();
    }

//    public DateTime getPaymentDate() {
//        String dateString = getString(NEXTOCCURRENCEDATE);
//        if (TextUtils.isEmpty(dateString)) {
//            return null;
//        }
//
//        return MmxJodaDateTimeUtils.from(dateString);
//    }

    public Date getPaymentDate() {
        final String dateString = getString(NEXTOCCURRENCEDATE);
        if (TextUtils.isEmpty(dateString)) {
            return null;
        }

        return new MmxDate(dateString).toDate();
    }

    public void setPaymentDate(final String value) {
        setString(NEXTOCCURRENCEDATE, value);
    }

    public void setPaymentDate(final Date value) {
        setDate(NEXTOCCURRENCEDATE, value);
    }

    public String getPaymentDateString() {
        return getString(NEXTOCCURRENCEDATE);
    }

    public String getNotes() {
        return getString(ITransactionEntity.NOTES);
    }

    public void setNotes(final String value) {
        setString(ITransactionEntity.NOTES, value);
    }

    public Integer getPaymentsLeft() {
        return getInt(NUMOCCURRENCES);
    }

    public void setPaymentsLeft(final Integer value) {
        setInt(NUMOCCURRENCES, value);
    }

    public Integer getPayeeId() {
        return getInt(ITransactionEntity.PAYEEID);
    }

    public void setPayeeId(final Integer value) {
        setInt(ITransactionEntity.PAYEEID, value);
    }

    public boolean hasPayee() {
        return null != getPayeeId() && Constants.NOT_SET != getPayeeId();
    }

    /**
     * The recurrence type
     *
     * @return the recurrence type
     */
    public Integer getRecurrenceInt() {
        final Integer result = getInt(REPEATS);
        if (null == result) {
            setRecurrence(Recurrence.ONCE);
            return Recurrence.ONCE.getValue();
        } else {
            return result;
        }
    }

    public Recurrence getRecurrence() {
        final int recurrence = getRecurrenceInt();
        return Recurrence.valueOf(recurrence);
    }

    public void setRecurrence(final Integer value) {
        setInt(REPEATS, value);
    }

    public void setRecurrence(final Recurrence value) {
        final int recurrence = value.getValue();
        setRecurrence(recurrence);
    }

    public String getStatus() {
        return getString(ITransactionEntity.STATUS);
    }

    public void setStatus(final String value) {
        setString(ITransactionEntity.STATUS, value);
    }

    public Integer getToAccountId() {
        return getInt(ITransactionEntity.TOACCOUNTID);
    }

    public String getTransactionCode() {
        return getString(ITransactionEntity.TRANSCODE);
    }

    public String getTransactionNumber() {
        return getString(ITransactionEntity.TRANSACTIONNUMBER);
    }

    public void setTransactionNumber(final String value) {
        setString(ITransactionEntity.TRANSACTIONNUMBER, value);
    }

    public TransactionTypes getTransactionType() {
        final String code = getTransactionCode();
        return TransactionTypes.valueOf(code);
    }

    public void setTransactionType(final TransactionTypes value) {
        setString(ITransactionEntity.TRANSCODE, value.name());
    }

}
