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
package com.money.manager.ex.datalayer;

import android.content.ContentValues;
import android.content.Context;

import com.money.manager.ex.database.DatabaseUtilities;
import com.money.manager.ex.database.DatasetType;
import com.money.manager.ex.database.ISplitTransactionsDataset;
import com.money.manager.ex.domainmodel.AccountTransaction;

/**
 * Repository for Checking Account records.
 * Source: Table Checking Account.
 */
public class AccountTransactionRepository
    extends RepositoryBase {

    public AccountTransactionRepository(Context context) {
        super(context, "checkingaccount_v1", DatasetType.TABLE, "checkingaccount");
    }

    @Override
    public String[] getAllColumns() {
        return new String[] {
                "TRANSID AS _id", AccountTransaction.TRANSID,
                ISplitTransactionsDataset.ACCOUNTID,
                ISplitTransactionsDataset.TOACCOUNTID,
                ISplitTransactionsDataset.PAYEEID,
                ISplitTransactionsDataset.TRANSCODE,
                ISplitTransactionsDataset.TRANSAMOUNT,
                ISplitTransactionsDataset.STATUS,
                ISplitTransactionsDataset.TRANSACTIONNUMBER,
                ISplitTransactionsDataset.NOTES,
                ISplitTransactionsDataset.CATEGID,
                ISplitTransactionsDataset.SUBCATEGID,
                ISplitTransactionsDataset.TRANSDATE,
                ISplitTransactionsDataset.FOLLOWUPID,
                ISplitTransactionsDataset.TOTRANSAMOUNT};
    }

    public AccountTransaction load(int id) {
        ContentValues cv = single(AccountTransaction.TRANSID + "=?", DatabaseUtilities.getArgsForId(id));
        AccountTransaction tx = new AccountTransaction(cv);
        return tx;
    }
}
