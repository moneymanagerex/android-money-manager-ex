/*
 * Copyright (C) 2012-2019 The Android Money Manager Ex Project Team
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
import com.money.manager.ex.database.ITransactionEntity;
import com.money.manager.ex.utils.MmxDate;
import com.money.manager.ex.utils.MmxDateTimeUtils;

import org.parceler.Parcel;

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

    /**
     * Creates default, empty transaction.
     * @return Account Transaction records with default values.
     */
    public static AccountTransaction create() {
        return create(Constants.NOT_SET, Constants.NOT_SET, TransactionTypes.Withdrawal,
                Constants.NOT_SET, Constants.NOT_SET, MoneyFactory.fromDouble(0));
    }

    public static AccountTransaction create(int accountId, int payeeId, TransactionTypes type,
                                            int categoryId, int subCategoryId, Money amount) {
        AccountTransaction tx = new AccountTransaction();

        tx.setAccountId(accountId);
        tx.setPayeeId(payeeId);
        tx.setTransactionType(type);
        tx.setCategoryId(categoryId);
        tx.setSubcategoryId(subCategoryId);
        tx.setAmount(amount);

        tx.setAmountTo(MoneyFactory.fromDouble(0));

        return tx;
    }

    public AccountTransaction() {
        super();

        setAccountToId(Constants.NOT_SET);
        setCategoryId(Constants.NOT_SET);
        setSubcategoryId(Constants.NOT_SET);
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

    public Integer getId() {
        return getInt(TRANSID);
    }

    public void setId(Integer id) {
        setInt(TRANSID, id);
    }

    public boolean hasId() {
        return getId() != null && getId() != Constants.NOT_SET;
    }

    public Integer getAccountId() {
        return getInt(ITransactionEntity.ACCOUNTID);
    }

    public void setAccountId(Integer value) {
        setInt(ITransactionEntity.ACCOUNTID, value);
    }

    @Override
    public Integer getAccountToId() {
        return getInt(ITransactionEntity.TOACCOUNTID);
    }

    @Override
    public void setAccountToId(Integer value) {
        setInt(ITransactionEntity.TOACCOUNTID, value);
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

    public Integer getCategoryId() {
        return getInt(ITransactionEntity.CATEGID);
    }

    public void setCategoryId(Integer value) {
        setInt(ITransactionEntity.CATEGID, value);
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

    public Integer getFollowUpId() {
        return getInt(FOLLOWUPID);
    }

    public void setFollowUpId(Integer value) {
        setInt(FOLLOWUPID, value);
    }

    public String getNotes() {
        return getString(ITransactionEntity.NOTES);
    }

    public void setNotes(String value) {
        setString(ITransactionEntity.NOTES, value);
    }

    public Integer getPayeeId() {
        return getInt(ITransactionEntity.PAYEEID);
    }

    public void setPayeeId(Integer value) {
        setInt(ITransactionEntity.PAYEEID, value);
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

    public Integer getSubcategoryId() {
        return getInt(ITransactionEntity.SUBCATEGID);
    }

    public void setSubcategoryId(Integer value) {
        setInt(ITransactionEntity.SUBCATEGID, value);
    }

    public String getTransCode() {
        return getString(ITransactionEntity.TRANSCODE);
    }

    public String getTransactionNumber() {
        return getString(ITransactionEntity.TRANSACTIONNUMBER);
    }

    public void setTransactionNumber(String value) {
        setString(ITransactionEntity.TRANSACTIONNUMBER, value);
    }

    public TransactionTypes getTransactionType() {
        String code = getTransCode();
        return TransactionTypes.valueOf(code);
    }

    @Override
    public void setTransactionType(TransactionTypes value) {
        setString(ITransactionEntity.TRANSCODE, value.name());
    }
}
