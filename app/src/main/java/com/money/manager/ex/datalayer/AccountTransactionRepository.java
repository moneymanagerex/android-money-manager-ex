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
package com.money.manager.ex.datalayer;

import android.content.Context;

import com.money.manager.ex.Constants;
import com.money.manager.ex.database.DatasetType;
import com.money.manager.ex.database.ITransactionEntity;
import com.money.manager.ex.database.WhereStatementGenerator;
import com.money.manager.ex.domainmodel.AccountTransaction;
import com.money.manager.ex.utils.MmxDatabaseUtils;

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
                ITransactionEntity.ACCOUNTID,
                ITransactionEntity.TOACCOUNTID,
                ITransactionEntity.PAYEEID,
                ITransactionEntity.TRANSCODE,
                ITransactionEntity.TRANSAMOUNT,
                ITransactionEntity.STATUS,
                ITransactionEntity.TRANSACTIONNUMBER,
                ITransactionEntity.NOTES,
                ITransactionEntity.CATEGID,
                ITransactionEntity.SUBCATEGID,
                ITransactionEntity.TRANSDATE,
                ITransactionEntity.FOLLOWUPID,
                ITransactionEntity.TOTRANSAMOUNT};
    }

    public AccountTransaction load(int id) {
        if (id == Constants.NOT_SET) return null;

        AccountTransaction tx = (AccountTransaction) first(AccountTransaction.class,
                getAllColumns(),
                AccountTransaction.TRANSID + "=?",
                MmxDatabaseUtils.getArgsForId(id),
                null);

        return tx;
    }

    public AccountTransaction insert(AccountTransaction entity) {
        entity.contentValues.remove(AccountTransaction.TRANSID);

        int id = insert(entity.contentValues);

        entity.setId(id);

        return entity;
    }

    public boolean update(AccountTransaction item) {
        WhereStatementGenerator where = new WhereStatementGenerator();
        where.addStatement(AccountTransaction.TRANSID, "=", item.getId());

        boolean saved = super.update(item, where.getWhere());
        return saved;
    }
}
