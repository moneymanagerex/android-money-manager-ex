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

import androidx.annotation.NonNull;

import com.money.manager.ex.Constants;
import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.database.ISplitTransaction;
import com.money.manager.ex.database.ITransactionEntity;
import com.money.manager.ex.scheduled.Recurrence;
import com.money.manager.ex.utils.MmxDate;

import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

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

    ArrayList<Attachment> mAttachments;
    ArrayList<TagLink> tagLinks = null;
    ArrayList<ISplitTransaction> splitTransactions;

    /**
     * Payment Date
     */
    public static final String NEXTOCCURRENCEDATE = "NEXTOCCURRENCEDATE";
    /**
     * Repetitions / Payments Left
     */
    public static final String NUMOCCURRENCES = "NUMOCCURRENCES";

    public static RecurringTransaction createInstance() {
        RecurringTransaction tx = new RecurringTransaction();

        tx.setAmount(MoneyFactory.fromDouble(0));
        tx.setToAmount(MoneyFactory.fromDouble(0));

        return tx;
    }

    public RecurringTransaction() {
        super();

        setPayeeId(Constants.NOT_SET);
        setCategoryId(Constants.NOT_SET);
        setRecurrence(Recurrence.ONCE);
        setTransactionType(TransactionTypes.Withdrawal);
    }

    @Override
    public void loadFromCursor(Cursor c) {
        super.loadFromCursor(c);

        // Reload all money values.
        DatabaseUtils.cursorDoubleToContentValuesIfPresent(c, this.contentValues, ITransactionEntity.TRANSAMOUNT);
        DatabaseUtils.cursorDoubleToContentValuesIfPresent(c, this.contentValues, ITransactionEntity.TOTRANSAMOUNT);
    }

    @Override
    public String getPrimaryKeyColumn() {
        return BDID;  // This returns the column name
    }

    public boolean hasId() {
        return getId() != null && getId() != Constants.NOT_SET;
    }

    public Long getAccountId() {
        return getLong(ITransactionEntity.ACCOUNTID);
    }

    public void setAccountId(Long value) {
        setLong(ITransactionEntity.ACCOUNTID, value);
    }

    @Override
    public Long getToAccountId() {
        return getLong(ITransactionEntity.TOACCOUNTID);
    }

    @Override
    public void setToAccountId(Long value) {
        setLong(ITransactionEntity.TOACCOUNTID, value);
    }

    public boolean hasToAccount() {
        return getToAccountId() != null && getToAccountId() != Constants.NOT_SET;
    }

    public Money getAmount() {
        Double amount = getDouble(ITransactionEntity.TRANSAMOUNT);
        if (amount == null) {
            amount = 0D;
        }
        return MoneyFactory.fromDouble(amount);
    }

    public void setAmount(Money value) {
        setMoney(ITransactionEntity.TRANSAMOUNT, value);
    }

    public Money getToAmount() {
        Double amount = getDouble(ITransactionEntity.TOTRANSAMOUNT);
        if (amount == null) {
            amount = 0D;
        }
        return MoneyFactory.fromDouble(amount);
    }

    public Money getRealSignedAmount() {
        if ( this.getStatus().equals("V") || this.getTransactionType().equals(TransactionTypes.Transfer) ) {
            return MoneyFactory.fromDouble(0);
        } else if (getTransactionType().equals(TransactionTypes.Deposit)) {
            return getAmount();
        } else {
            return getAmount().multiply(-1);
        }
    }

    public void setToAmount(Money value) {
        setMoney(ITransactionEntity.TOTRANSAMOUNT, value);
    }

    @Override
    public Long getCategoryId() {
        return getLong(ITransactionEntity.CATEGID);
    }

    @Override
    public void setCategoryId(Long value) {
        setLong(ITransactionEntity.CATEGID, value);
    }

    public boolean hasCategory() {
        return getCategoryId() != null && getCategoryId() != Constants.NOT_SET;
    }

    public String getDueDateString() {
        return getString(TRANSDATE);
    }

    public Date getDueDate() {
        String dateString = getDueDateString();
        if (TextUtils.isEmpty(dateString)) {
            return null;
        }

        return new MmxDate(dateString).toDate();
    }

    public void setDueDate(Date value) {
        setDate(TRANSDATE, value);
    }

    /**
     * For recurring transaction, the date is Due Date.
     * @return Due date of the transaction.
     */
    public Date getDate() {
        return getDueDate();
    }

    public String getDateString() {
        return getDueDateString();
    }

    /**
     * Due Date
     * @param value Date to set
     */
    public void setDate(Date value) {
        setDueDate(value);
    }

//    public DateTime getPaymentDate() {
//        String dateString = getString(NEXTOCCURRENCEDATE);
//        if (TextUtils.isEmpty(dateString)) {
//            return null;
//        }
//
//        return MmxJodaDateTimeUtils.from(dateString);
//    }

    public MmxDate getPaymentDateAsMmxDate() {
        String dateString = getString(NEXTOCCURRENCEDATE);
        if (TextUtils.isEmpty(dateString)) {
            return null;
        }
        return new MmxDate(dateString);
    }

    public Date getPaymentDate() {
        return getPaymentDateAsMmxDate().toDate();
    }

    public String getPaymentDateString() {
        return getString(NEXTOCCURRENCEDATE);
    }

    public void setPaymentDate(String value) {
        setString(NEXTOCCURRENCEDATE, value);
    }

    public void setPaymentDate(Date value){
        setDate(NEXTOCCURRENCEDATE, value);
    }

    public String getNotes() {
        return getString(ITransactionEntity.NOTES);
    }

    public void setNotes(String value) {
        setString(ITransactionEntity.NOTES, value);
    }

    public Long getPaymentsLeft() {
        return getLong(NUMOCCURRENCES);
    }

    public void setPaymentsLeft(Long value) {
        setLong(NUMOCCURRENCES, value);
    }

    public Long getPayeeId() {
        return getLong(ITransactionEntity.PAYEEID);
    }

    public void setPayeeId(Long value) {
        setLong(ITransactionEntity.PAYEEID, value);
    }

    public boolean hasPayee() {
        return getPayeeId() != null && getPayeeId() != Constants.NOT_SET;
    }

    /**
     * The recurrence type
     * @return the recurrence type
     */
    public Integer getRecurrenceInt() {
        Integer result = getInt(REPEATS);
        if (result == null) {
            setRecurrence(Recurrence.ONCE);
            return Recurrence.ONCE.getValue();
        } else {
            return result;
        }
    }

    public void setRecurrence(Integer value) {
        setInt(REPEATS, value + getRecurrenceMode() * 100);
    }

    public Recurrence getRecurrence() {
        int recurrence = getRecurrenceInt();
        return Recurrence.valueOf(recurrence);
    }

    public void setRecurrence(Recurrence value) {
        int recurrence = value.getValue();
        setRecurrence(recurrence);
    }

    // EP get RecurringMode
    public void setRecurrenceMode(long value) {
        long rec = getRecurrenceInt();
        rec = rec % 100;  // get base value
        setLong(REPEATS,  rec + ( value * 100 ) ); // set recurrency mode
    }
    public Integer getRecurrenceMode() {
        try {
            return getInt(REPEATS) / 100;
        } catch ( Exception e ) {
            return 0;
        }
    }

    public String getStatus() {
        return getString(ITransactionEntity.STATUS);
    }

    public void setStatus(String value) {
        setString(ITransactionEntity.STATUS, value);
    }

    // duplicate of getToAccountId()
//    public Long getToAccountId() {
//        return getLong(ITransactionEntity.TOACCOUNTID);
//    }

    public String getTransactionCode() {
        return getString(ITransactionEntity.TRANSCODE);
    }

    public String getTransactionNumber() {
        return Objects.requireNonNullElse(getString(ITransactionEntity.TRANSACTIONNUMBER),"");
    }

    public void setTransactionNumber(String value) {
        setString(ITransactionEntity.TRANSACTIONNUMBER, value);
    }

    public TransactionTypes getTransactionType() {
        String code = getTransactionCode();
        return TransactionTypes.valueOf(code);
    }

    public void setTransactionType(TransactionTypes value) {
        setString(ITransactionEntity.TRANSCODE, value.name());
    }

    @Override
    public RefType getTransactionModel() {
        return RefType.RECURRING_TRANSACTION;
    }

    @Override
    public void setAttachments(ArrayList<Attachment> attachments) {
        this.mAttachments = attachments;
    }

    @Override
    public ArrayList<Attachment> getAttachments() {
        return this.mAttachments;
    }

    @Override
    public void setTagLinks(ArrayList<TagLink> tagLinks) {
        this.tagLinks = tagLinks;
    }

    @Override
    public ArrayList<TagLink> getTagLinks() {
        return tagLinks;
    }

    // EP handle recurring mode
    public boolean isRecurringModeManual() {
        return (this.getRecurrenceInt() < 100 );
    }

    public boolean isRecurringModePrompt() {
        return (this.getRecurrenceInt() < 200 &&
                this.getRecurrenceInt() >= 100 );
    }

    public boolean isRecurringModeAuto() {
        return (this.getRecurrenceInt() >= 200 );
    }

    @Override
    public void setColor(int value) {
        setInt(ITransactionEntity.COLOR, value);
    }

    @Override
    public int getColor() {
        if (getInt(ITransactionEntity.COLOR) == null) {
            return (int)Constants.NOT_SET;
        }
        return getInt(ITransactionEntity.COLOR);
    }

    @Override
    public void setSplit(ArrayList<ISplitTransaction> split) {
        splitTransactions = split;
    }

    @Override
    public ArrayList<ISplitTransaction> getSplit() {
        return splitTransactions;
    }

    @NonNull
    @Override
    public String toString() {
        return "RecurringTransaction{" +
                "id=" + getId() +
                ", payementDate=" + getPaymentDateString() +
                ", categoryId=" + getCategoryId() +
                ", payeeId=" + getPayeeId() +
                ", amount=" + getAmount() +
                ", note="+getNotes()+
                "}";
    }

}
