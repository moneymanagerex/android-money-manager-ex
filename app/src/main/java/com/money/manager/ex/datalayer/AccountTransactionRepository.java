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

import com.money.manager.ex.database.DatasetType;
import com.money.manager.ex.database.ITransactionEntity;
import com.money.manager.ex.database.WhereStatementGenerator;
import com.money.manager.ex.domainmodel.AccountTransaction;
import com.money.manager.ex.domainmodel.RefType;
import com.money.manager.ex.utils.MmxDate;

/**
 * Repository for Checking Account records.
 * Source: Table Checking Account.
 */
public class AccountTransactionRepository
    extends RepositoryBase<AccountTransaction> {

    private static final String TABLE_NAME = "checkingaccount_v1";
    private static final String ID_COLUMN = AccountTransaction.TRANSID;
    private static final String NAME_COLUMN = "";

    public AccountTransactionRepository(Context context) {
        super(context, TABLE_NAME, DatasetType.TABLE, "checkingaccount", ID_COLUMN, NAME_COLUMN);
    }

    @Override
    protected AccountTransaction createEntity() {
        return new AccountTransaction();
    }

    @Override
    public String[] getAllColumns() {
        return new String[] {
                ID_COLUMN + " AS _id", AccountTransaction.TRANSID,
                ITransactionEntity.ACCOUNTID,
                ITransactionEntity.TOACCOUNTID,
                ITransactionEntity.PAYEEID,
                ITransactionEntity.TRANSCODE,
                ITransactionEntity.TRANSAMOUNT,
                ITransactionEntity.STATUS,
                ITransactionEntity.TRANSACTIONNUMBER,
                ITransactionEntity.NOTES,
                ITransactionEntity.CATEGID,
                ITransactionEntity.TRANSDATE,
                ITransactionEntity.FOLLOWUPID,
                ITransactionEntity.TOTRANSAMOUNT,
                ITransactionEntity.COLOR
        };
    }

    @Override
    protected RefType refType () {
        return RefType.TRANSACTION;
    }

    // custom func

    @Override
    public AccountTransaction load(Long id) {
        AccountTransaction txn = super.load(id);

        txn.setAttachments(loadAttachments(id));
        /// TODO other associated items

        return txn;
    }

    public AccountTransaction insert(AccountTransaction entity) {
        entity.contentValues.remove(AccountTransaction.TRANSID);
        entity.contentValues.put(AccountTransaction.LASTUPDATEDTIME, (new MmxDate()).toIsoCombinedString());

        long id = this.add(entity);

        entity.setId(id);

        return entity;
    }

    public boolean update(AccountTransaction item) {
        WhereStatementGenerator where = new WhereStatementGenerator();
        where.addStatement(AccountTransaction.TRANSID, "=", item.getId());

        item.setLastUpdatedTime((new MmxDate()).toIsoCombinedString());

        return super.update(item, where.getWhere());
    }
}
