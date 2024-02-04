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

import org.parceler.Parcel;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

/**
 * Split Category item for recurring transaction item.
 */
@Parcel
public class SplitRecurringCategory
        extends EntityBase
        implements ISplitTransaction {

    public static final String SPLITTRANSID = "SPLITTRANSID";
    public static final String TRANSID = "TRANSID";
    public static final String CATEGID = "CATEGID";
    public static final String SUBCATEGID = "SUBCATEGID";
    public static final String SPLITTRANSAMOUNT = "SPLITTRANSAMOUNT";
    public static final String NOTES = "NOTES";
    public static String TABLE_NAME = "budgetsplittransactions_v1";
    TransactionTypes transactionType;

    public static SplitRecurringCategory create(final int transactionId, final int categoryId, final int subcategoryId,
                                                final TransactionTypes parentTransactionType, final Money amount, final String notes) {
        final SplitRecurringCategory entity = new SplitRecurringCategory();

        entity.setCategoryId(categoryId);
        entity.setAmount(amount);
        entity.setTransId(transactionId);
        entity.setNotes(notes);

        final TransactionTypes splitType;
        if (amount.isZero() || -1 == amount.compareTo(MoneyFactory.fromDouble(0))) {
            splitType = TransactionTypes.Withdrawal;
        } else {
            splitType = TransactionTypes.Deposit;
        }
        entity.setTransactionType(splitType, parentTransactionType);

        return entity;
    }

    @Override
    public Integer getId() {
        return getInt(SPLITTRANSID);
    }

    @Override
    public void setId(final int value) {
        setInt(SPLITTRANSID, value);
    }

    @Override
    public boolean hasId() {
        return null != getId() && Constants.NOT_SET != getId();
    }

    @Override
    public Integer getAccountId() {
        return getInt(ITransactionEntity.ACCOUNTID);
    }

    @Override
    public void setAccountId(final int value) {
        setInt(ITransactionEntity.ACCOUNTID, value);
    }

    @Override
    public Integer getCategoryId() {
        return getInt(CATEGID);
    }

    @Override
    public void setCategoryId(final int categId) {
        setInt(CATEGID, categId);
    }

    @Override
    public Money getAmount() {
        return getMoney(SPLITTRANSAMOUNT);
    }

    @Override
    public void setAmount(final Money splitTransAmount) {
        setMoney(SPLITTRANSAMOUNT, splitTransAmount);
    }

    @Override
    public String getNotes() {
        return getString(NOTES);
    }

    @Override
    public void setNotes(final String value) {
        setString(NOTES, value);
    }

    @Override
    public void loadFromCursor(final Cursor c) {
        super.loadFromCursor(c);

        DatabaseUtils.cursorDoubleToContentValuesIfPresent(c, contentValues, SPLITTRANSAMOUNT);
    }

    public Integer getTransId() {
        return getInt(TRANSID);
    }

    public void setTransId(final int value) {
        setInt(TRANSID, value);
    }

    public TransactionTypes getTransactionType(final TransactionTypes parentTransactionType) {
        if (null == transactionType) {
            transactionType = CommonSplitCategoryLogic.getTransactionType(parentTransactionType, getAmount());
        }
        return transactionType;
    }

    public void setTransactionType(final TransactionTypes value, final TransactionTypes parentTransactionType) {
        final TransactionTypes currentType = getTransactionType(parentTransactionType);

        transactionType = value;

        // If the type is being changed, just revert the sign.
        if (value != currentType) {
            setAmount(getAmount().negate());
        }
    }
}
