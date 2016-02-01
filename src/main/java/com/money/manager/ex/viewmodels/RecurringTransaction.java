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
package com.money.manager.ex.viewmodels;

import com.money.manager.ex.database.ITransactionEntity;
import com.money.manager.ex.database.QueryBillDeposits;
import com.money.manager.ex.domainmodel.EntityBase;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

/**
 * Record from All Data adapter. Used for recurring transactions lists.
 * Source is QueryBillDeposits.
 */
public class RecurringTransaction
    extends EntityBase
    implements ITransactionEntity {

    public Integer getId() {
        return getInt(QueryBillDeposits.BDID);
    }

    public void setId(int id) {
        setInteger(QueryBillDeposits.BDID, id);
    }

    public Integer getAccountId() {
        return getInt(ITransactionEntity.ACCOUNTID);
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

    public Integer getSubcategoryId() {
        return getInt(ITransactionEntity.SUBCATEGID);
    }

    public void setSubcategoryId(Integer value) {
        setInteger(ITransactionEntity.SUBCATEGID, value);
    }

}
