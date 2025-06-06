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
import android.database.Cursor;

import com.money.manager.ex.database.DatasetType;
import com.money.manager.ex.database.ITransactionEntity;
import com.money.manager.ex.database.WhereStatementGenerator;
import com.money.manager.ex.domainmodel.RecurringTransaction;
import com.money.manager.ex.domainmodel.RefType;

/**
 * Recurring transaction repository.
 */
public class ScheduledTransactionRepository
    extends RepositoryBase<RecurringTransaction>{

    private static final String TABLE_NAME = "billsdeposits_v1";
    private static final String ID_COLUMN = RecurringTransaction.BDID;
    private static final String NAME_COLUMN = "";

    public ScheduledTransactionRepository(Context context) {
        super(context, TABLE_NAME, DatasetType.TABLE, "billsdeposits", ID_COLUMN, NAME_COLUMN);
    }

    @Override
    public RecurringTransaction createEntity() {
        return new RecurringTransaction();
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public String[] getAllColumns() {
        return new String [] { ID_COLUMN + " AS _id", RecurringTransaction.BDID,
                ITransactionEntity.ACCOUNTID,
                ITransactionEntity.TOACCOUNTID,
                ITransactionEntity.PAYEEID,
                ITransactionEntity.TRANSCODE,
                ITransactionEntity.TRANSAMOUNT,
                ITransactionEntity.STATUS,
                ITransactionEntity.TRANSACTIONNUMBER,
                ITransactionEntity.NOTES,
                ITransactionEntity.CATEGID,
                ITransactionEntity.COLOR,
                ITransactionEntity.TRANSDATE,
                ITransactionEntity.FOLLOWUPID,
                ITransactionEntity.TOTRANSAMOUNT,
                RecurringTransaction.REPEATS,
                RecurringTransaction.NEXTOCCURRENCEDATE,
                RecurringTransaction.NUMOCCURRENCES};
    }

    @Override
    protected RefType refType () {
        return RefType.RECURRING_TRANSACTION;
    }

    // custom func
    @Override
    public RecurringTransaction load(Long id) {
        RecurringTransaction txn = super.load(id);

        txn.setAttachments(loadAttachments(id));
        /// TODO other associated items

        return txn;
    }

    public RecurringTransaction first(String[] projection, String selection, String[] args) {
        Cursor c = openCursor(projection, selection, args);

        if (c == null) return null;

        RecurringTransaction entity = null;

        if (c.moveToNext()) {
            entity = new RecurringTransaction();
            entity.loadFromCursor(c);
        }

        c.close();

        return entity;
    }

    public RecurringTransaction insert(RecurringTransaction entity) {
        entity.contentValues.remove(RecurringTransaction.BDID);

        long id = this.add(entity);

        entity.setId(id);

        return entity;
    }

    public boolean update(RecurringTransaction value) {
        long id = value.getId();

        WhereStatementGenerator generator = new WhereStatementGenerator();
        String where = generator.getStatement(RecurringTransaction.BDID, "=", id);

        return update(value, where);
    }

}
