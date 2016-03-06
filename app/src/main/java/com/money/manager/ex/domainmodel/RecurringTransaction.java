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

package com.money.manager.ex.domainmodel;

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.text.TextUtils;

import com.money.manager.ex.database.ITransactionEntity;
import com.money.manager.ex.recurring.transactions.Recurrence;
import com.money.manager.ex.utils.MyDateTimeUtils;

import org.joda.time.DateTime;
import org.parceler.Parcel;

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
        super();
    }

    @Override
    public void loadFromCursor(Cursor c) {
        super.loadFromCursor(c);

        // Reload all money values.
        DatabaseUtils.cursorDoubleToContentValuesIfPresent(c, this.contentValues, ITransactionEntity.TRANSAMOUNT);
        DatabaseUtils.cursorDoubleToContentValuesIfPresent(c, this.contentValues, ITransactionEntity.TOTRANSAMOUNT);
    }

    public Integer getId() {
        return getInt(BDID);
    }

    public void setId(int id) {
        setInteger(BDID, id);
    }

    public Integer getAccountId() {
        return getInt(ITransactionEntity.ACCOUNTID);
    }

    public void setAccountId(int value) {
        setInteger(ITransactionEntity.ACCOUNTID, value);
    }

    public Money getAmount() {
        Double amount = getDouble(ITransactionEntity.TRANSAMOUNT);
        if (amount == null) {
            amount = 0D;
        }
        Money result = MoneyFactory.fromDouble(amount);
        return result;
    }

    public void setAmount(Money value) {
        setMoney(ITransactionEntity.TRANSAMOUNT, value);
    }

    public Money getAmountTo() {
        Double amount = getDouble(ITransactionEntity.TOTRANSAMOUNT);
        if (amount == null) {
            amount = 0D;
        }
        Money result = MoneyFactory.fromDouble(amount);
        return result;
    }

    public void setAmountTo(Money value) {
        setMoney(ITransactionEntity.TOTRANSAMOUNT, value);
    }

    public Integer getCategoryId() {
        return getInt(ITransactionEntity.CATEGID);
    }

    public void setCategoryId(int value) {
        setInteger(ITransactionEntity.CATEGID, value);
    }

    public String getDueDateString() {
        return getString(TRANSDATE);
    }

    public DateTime getDueDate() {
        String dateString = getDueDateString();
        if (TextUtils.isEmpty(dateString)) {
            return null;
        }

        return MyDateTimeUtils.from(dateString);
    }

    public void setDueDate(DateTime value) {
        setDateTime(TRANSDATE, value);
    }

    public DateTime getPaymentDate() {
        String dateString = getString(NEXTOCCURRENCEDATE);
        if (TextUtils.isEmpty(dateString)) {
            return null;
        }

        return MyDateTimeUtils.from(dateString);
    }

    public String getPaymentDateString() {
        return getString(NEXTOCCURRENCEDATE);
    }

    public void setPaymentDate(String value) {
        setString(NEXTOCCURRENCEDATE, value);
    }

    public void setPaymentDate(DateTime value){
        setDateTime(NEXTOCCURRENCEDATE, value);
    }

    public String getNotes() {
        return getString(ITransactionEntity.NOTES);
    }

    public Integer getPaymentsLeft() {
        return getInt(NUMOCCURRENCES);
    }

    public void setPaymentsLeft(Integer value) {
        setInteger(NUMOCCURRENCES, value);
    }

    public Integer getPayeeId() {
        return getInt(ITransactionEntity.PAYEEID);
    }

    /**
     * The recurrence type
     * @return the recurrence type
     */
    public Integer getRecurrenceInt() {
        return getInt(REPEATS);
    }

    public void setRecurrence(Integer value) {
        setInteger(REPEATS, value);
    }

    public Recurrence getRecurrence() {
        int recurrence = getRecurrenceInt();
        return Recurrence.valueOf(recurrence);
    }

    public void setRecurrence(Recurrence value) {
        int recurrence = value.getValue();
        setRecurrence(recurrence);
    }

    public String getStatus() {
        return getString(ITransactionEntity.STATUS);
    }

    public Integer getSubcategoryId() {
        return getInt(ITransactionEntity.SUBCATEGID);
    }

    public void setSubcategoryId(Integer value) {
        setInteger(ITransactionEntity.SUBCATEGID, value);
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
}
