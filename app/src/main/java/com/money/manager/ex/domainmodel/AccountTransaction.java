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

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;

import com.money.manager.ex.Constants;
import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.database.ISplitTransaction;
import com.money.manager.ex.database.ITransactionEntity;
import com.money.manager.ex.utils.MmxDate;

import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.Date;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

/**
 * Account Transaction entity. Table checkingaccount_v1.
 */
@Parcel
public class AccountTransaction
    extends EntityBase
    implements ITransactionEntity {

    public static final String TRANSID = "TRANSID";
    public static final String LASTUPDATEDTIME = "LASTUPDATEDTIME";

    private ArrayList<Attachment> mAttachments;
    private ArrayList<TagLink> tagLinks = null;
    private ArrayList<ISplitTransaction> splitTransactions = null;

    /**
     * Creates default, empty transaction.
     * @return Account Transaction records with default values.
     */
    public static AccountTransaction create() {
        return create(Constants.NOT_SET, Constants.NOT_SET, TransactionTypes.Withdrawal,
                Constants.NOT_SET, MoneyFactory.fromDouble(0));
    }

    public static AccountTransaction create(long accountId, long payeeId, TransactionTypes type,
                                            long categoryId, Money amount) {
        AccountTransaction tx = new AccountTransaction();

        tx.setAccountId(accountId);
        tx.setPayeeId(payeeId);
        tx.setTransactionType(type);
        tx.setCategoryId(categoryId);
        tx.setAmount(amount);

        tx.setAmountTo(MoneyFactory.fromDouble(0));

        return tx;
    }

    public AccountTransaction() {
        super();

        setAccountToId(Constants.NOT_SET);
        setCategoryId(Constants.NOT_SET);
        setFollowUpId(Constants.NOT_SET);
    }

    public AccountTransaction(ContentValues contentValues) {
        super(contentValues);
    }

    @Override
    public void loadFromCursor(Cursor c) {
        super.loadFromCursor(c);

        // Reload all money values.
        DatabaseUtils.cursorDoubleToCursorValues(c, TRANSAMOUNT, this.contentValues);
        DatabaseUtils.cursorDoubleToCursorValues(c, TOTRANSAMOUNT, this.contentValues);
    }

    @Override
    public String getPrimaryKeyColumn() {
        return TRANSID;  // This returns the column name
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
    public Long getAccountToId() {
        return getLong(ITransactionEntity.TOACCOUNTID);
    }

    @Override
    public void setAccountToId(Long value) {
        setLong(ITransactionEntity.TOACCOUNTID, value);
    }

    public boolean hasAccountTo() {
        return getAccountToId() != null && getAccountToId() != Constants.NOT_SET;
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

    public Long getCategoryId() {
        return getLong(ITransactionEntity.CATEGID);
    }

    public void setCategoryId(Long value) {
        setLong(ITransactionEntity.CATEGID, value);
    }

    public boolean hasCategory() {
        return getCategoryId() != null && getCategoryId() != Constants.NOT_SET;
    }

    public String getDateString() {
        return getString(ITransactionEntity.TRANSDATE);
    }

    public Date getDate() {
        String dateString = getDateString();
        return dateString != null
            ? new MmxDate(dateString).toDate()
            : null;
    }

    public void setDate(Date value) {
        String dateString = new MmxDate(value).toIsoDateString();
        setString(ITransactionEntity.TRANSDATE, dateString);
    }

    public Long getFollowUpId() {
        return getLong(FOLLOWUPID);
    }

    public void setFollowUpId(Long value) {
        setLong(FOLLOWUPID, value);
    }

    public String getNotes() {
        return getString(ITransactionEntity.NOTES);
    }

    public void setNotes(String value) {
        setString(ITransactionEntity.NOTES, value);
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

    public String getStatus() {
        return getString(ITransactionEntity.STATUS);
    }

    public void setStatus(String value) {
        setString(ITransactionEntity.STATUS, value);
    }

    public String getTransactionCode() {
        return getString(ITransactionEntity.TRANSCODE);
    }

    public String getTransactionNumber() {
        return getString(ITransactionEntity.TRANSACTIONNUMBER);
    }

    public void setTransactionNumber(String value) {
        setString(ITransactionEntity.TRANSACTIONNUMBER, value);
    }

    public TransactionTypes getTransactionType() {
        String code = getTransactionCode();
        return TransactionTypes.valueOf(code);
    }

    @Override
    public void setTransactionType(TransactionTypes value) {
        setString(ITransactionEntity.TRANSCODE, value.name());
    }

    @Override
    public RefType getTransactionModel() {
        return RefType.TRANSACTION;
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

    public void setLastUpdatedTime(String value) {
        setString(LASTUPDATEDTIME, value);
    }

//    public void createSplitFromRecurring(ArrayList<SplitRecurringCategory> source) {
    public void createSplitFromRecurring(ArrayList<ISplitTransaction> source) {
        if ( source == null ) {
            splitTransactions = null;
            return;
        }
        if (splitTransactions == null ) {
            splitTransactions = new ArrayList<>();
        } else {
            splitTransactions.clear();
        }
        for ( ISplitTransaction split : source ) {
            SplitCategory scat = new SplitCategory();
            scat.setId(Constants.NOT_SET);
            scat.setTransId(getId());
            scat.setCategoryId(split.getCategoryId());
            scat.setAmount(split.getAmount());
            scat.setNotes(split.getNotes());
            scat.setTagLinks(TagLink.clearCrossReference(split.getTagLinks()));// clear cros reference for tag
            splitTransactions.add(scat);
        }
    }
}
