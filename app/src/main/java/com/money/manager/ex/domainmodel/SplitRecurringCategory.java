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

    public static String TABLE_NAME = "budgetsplittransactions_v1";

    public static final String SPLITTRANSID = "SPLITTRANSID";
    public static final String TRANSID = "TRANSID";
    public static final String CATEGID = "CATEGID";
    public static final String SUBCATEGID = "SUBCATEGID";
    public static final String SPLITTRANSAMOUNT = "SPLITTRANSAMOUNT";

    public static SplitRecurringCategory create(TransactionTypes parentTransactionType,
                                                int transactionId, int categoryId, int subcategoryId,
                                                double amount) {
        SplitRecurringCategory entity = new SplitRecurringCategory();

        entity.setCategoryId(categoryId);
        entity.setSubcategoryId(subcategoryId);
        entity.setAmount(MoneyFactory.fromDouble(amount));
        entity.setTransId(transactionId);

        entity.setParentTransactionType(parentTransactionType);

        return entity;
    }

    private TransactionTypes parentTransactionType;

    @Override
    public Integer getId() {
        return getInt(SPLITTRANSID);
    }

    @Override
    public void setId(int value) {
        setInt(SPLITTRANSID, value);
    }

    @Override
    public boolean hasId() {
        return getId() != null && getId() != Constants.NOT_SET;
    }

    @Override
    public Integer getAccountId() {
        return getInt(ITransactionEntity.ACCOUNTID);
    }

    @Override
    public void setAccountId(int value) {
        setInt(ITransactionEntity.ACCOUNTID, value);
    }

    @Override
    public Integer getCategoryId() {
        return getInt(CATEGID);
    }

    @Override
    public Money getAmount() {
        return getMoney(SPLITTRANSAMOUNT);
    }

    @Override
    public Integer getSubcategoryId() {
        return getInt(SUBCATEGID);
    }

    @Override
    public void setCategoryId(int categId) {
        setInt(CATEGID, categId);
    }

    @Override
    public void setAmount(Money splitTransAmount) {
        setMoney(SPLITTRANSAMOUNT, splitTransAmount);
    }

    @Override
    public void setSubcategoryId(Integer subCategoryId) {
        setInt(SUBCATEGID, subCategoryId);
    }

    @Override
    public void loadFromCursor(Cursor c) {
        super.loadFromCursor(c);

        DatabaseUtils.cursorDoubleToContentValuesIfPresent(c, contentValues, SPLITTRANSAMOUNT);
    }

    public Integer getTransId() {
        return getInt(TRANSID);
    }

    public void setTransId(int value) {
        setInt(TRANSID, value);
    }

    public TransactionTypes getTransactionType() {
        return CommonSplitCategoryLogic.getTransactionType(this.parentTransactionType, this.getAmount());
    }

    public void setTransactionType(TransactionTypes value) {
        TransactionTypes transactionType = getTransactionType();

        // If the type is being changed, just revert the sign.
        if (value != transactionType) {
            this.setAmount(this.getAmount().negate());
        }
    }

    public TransactionTypes getParentTransactionType() {
        return this.parentTransactionType;
    }

    public void setParentTransactionType(TransactionTypes value) {
        this.parentTransactionType = value;
    }
}
