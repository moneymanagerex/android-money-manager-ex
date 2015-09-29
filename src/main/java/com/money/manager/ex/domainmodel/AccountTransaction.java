/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
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

import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.database.ISplitTransactionsDataset;

import info.javaperformance.money.Money;

/**
 * Account Transaction entity. Table checkingaccount_v1.
 */
public class AccountTransaction
    extends EntityBase {

    public static final String TRANSID = "TRANSID";

    public static AccountTransaction create(int accountId, int payeeId, TransactionTypes type,
                                            Money amount) {
        AccountTransaction tx = new AccountTransaction();

        tx.setAccountId(accountId);
        tx.setPayeeId(payeeId);
        tx.setType(type);
        tx.setAmount(amount);

        return tx;
    }

    public void setAccountId(int value) {
        setInt(ISplitTransactionsDataset.ACCOUNTID, value);
    }

    public void setPayeeId(int value) {
        setInt(ISplitTransactionsDataset.PAYEEID, value);
    }

    public void setType(TransactionTypes value) {
        setString(ISplitTransactionsDataset.TRANSCODE, value.name());
    }

    public void setAmount(Money value) {
        setMoney(ISplitTransactionsDataset.TRANSAMOUNT, value);
    }
}
