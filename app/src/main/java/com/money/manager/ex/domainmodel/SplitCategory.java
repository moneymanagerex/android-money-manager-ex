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

import com.money.manager.ex.Constants;
import com.money.manager.ex.common.CommonSplitCategoryLogic;
import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.database.ISplitTransaction;
import com.money.manager.ex.database.ITransactionEntity;
import com.money.manager.ex.datalayer.IEntity;

import org.parceler.Parcel;

import java.util.ArrayList;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

/**
 * Split Category for checking account transaction.
 */
@Parcel
public class SplitCategory
    extends EntityBase
    implements ISplitTransaction {

    public static String TABLE_NAME = "SPLITTRANSACTIONS_V1";

    public static final String SPLITTRANSID = "SPLITTRANSID";
    public static final String TRANSID = "TRANSID";
    public static final String CATEGID = "CATEGID";
    public static final String SPLITTRANSAMOUNT = "SPLITTRANSAMOUNT";
    public static final String NOTES = "NOTES";

    private ArrayList<Taglink> taglinks = null;

    public static SplitCategory create(long transactionId, long categoryId, TransactionTypes parentTransactionType, Money amount, String notes) {
        SplitCategory entity = new SplitCategory();

        entity.setId(Constants.NOT_SET);
        entity.setCategoryId(categoryId);
        entity.setAmount(amount);
        entity.setTransId(transactionId);
        entity.setNotes(notes);

        TransactionTypes splitType;
        if (amount.isZero() || amount.compareTo(MoneyFactory.fromDouble(0)) == -1) {
            splitType = TransactionTypes.Withdrawal;
        } else {
            splitType = TransactionTypes.Deposit;
        }
        entity.setTransactionType(splitType, parentTransactionType);

        return entity;
    }

    TransactionTypes transactionType;

    @Override
    public String getPrimaryKeyColumn() {
        return SPLITTRANSID;  // This returns the column name
    }

    public Long getId() {
        return getLong(SPLITTRANSID);
    }

    public void setId(long value) {
        setLong(SPLITTRANSID, value);
    }

    @Override
    public boolean hasId() {
        return getId() != null && getId() != Constants.NOT_SET;
    }

    @Override
    public Long getAccountId() {
        return getLong(ITransactionEntity.ACCOUNTID);
    }

    @Override
    public void setAccountId(long value) {
        setLong(ITransactionEntity.ACCOUNTID, value);
    }

    @Override
    public Long getCategoryId() {
        return getLong(CATEGID);
    }

    @Override
    public Money getAmount() {
        return getMoney(SPLITTRANSAMOUNT);
    }

    @Override
    public void setCategoryId(long categoryId) {
        setLong(CATEGID, categoryId);
    }

    @Override
    public void setAmount(Money splitTransAmount) {
        setMoney(SPLITTRANSAMOUNT, splitTransAmount);
    }
    
    @Override
    public String getNotes() {
        return getString(NOTES);
    }
    @Override
    public void setNotes(String value) {
        setString(NOTES, value);
    }

    @Override
    public void loadFromCursor(Cursor c) {
        super.loadFromCursor(c);

        DatabaseUtils.cursorDoubleToContentValuesIfPresent(c, contentValues, SPLITTRANSAMOUNT);
    }

    public Long getTransId() {
        return getLong(TRANSID);
    }

    public void setTransId(Long value) {
        setLong(TRANSID, value);
    }

    public TransactionTypes getTransactionType(TransactionTypes parentTransactionType) {
        if (transactionType == null) {
            transactionType = CommonSplitCategoryLogic.getTransactionType(parentTransactionType, this.getAmount());
        }
        return transactionType;
    }

    public void setTransactionType(TransactionTypes value, TransactionTypes parentTransactionType) {
        TransactionTypes currentType = getTransactionType(parentTransactionType);

        this.transactionType = value;

        // If the type is being changed, just revert the sign.
        if (value != currentType) {
            this.setAmount(this.getAmount().negate());
        }
    }

    @Override
    public void setTags(ArrayList<Taglink> tags) {
        taglinks = tags;
    }

    @Override
    public ArrayList<Taglink> getTags() {
        return taglinks;
    }

    @Override
    public String getTransactionModel() {
        return Taglink.REFTYPE_TRANSACTION_SPLIT;
    }

}
