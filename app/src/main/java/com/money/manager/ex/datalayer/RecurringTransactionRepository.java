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

package com.money.manager.ex.datalayer;

import android.content.Context;
import android.database.Cursor;

import com.money.manager.ex.Constants;
import com.money.manager.ex.database.DatasetType;
import com.money.manager.ex.database.ITransactionEntity;
import com.money.manager.ex.database.WhereStatementGenerator;
import com.money.manager.ex.domainmodel.RecurringTransaction;

/**
 * Recurring transaction repository.
 */
public class RecurringTransactionRepository
    extends RepositoryBase<RecurringTransaction>{

    public RecurringTransactionRepository(Context context) {
        super(context, "billsdeposits_v1", DatasetType.TABLE, "billsdeposits");
    }

    @Override
    public String[] getAllColumns() {
        return new String [] { RecurringTransaction.BDID + " AS _id", RecurringTransaction.BDID,
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
                ITransactionEntity.TOTRANSAMOUNT,
                RecurringTransaction.REPEATS,
                RecurringTransaction.NEXTOCCURRENCEDATE,
                RecurringTransaction.NUMOCCURRENCES};
    }

    public int delete(int id) {
        return super.delete(RecurringTransaction.BDID + "=?", new String[] { Integer.toString(id)});
    }

    public RecurringTransaction load(int id) {
        if (id == Constants.NOT_SET) return null;

        WhereStatementGenerator where = new WhereStatementGenerator();
        where.addStatement(RecurringTransaction.BDID, "=", id);

        RecurringTransaction tx = first(null, where.getWhere(), null);

        return tx;
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

        int id = insert(entity.contentValues);

        entity.setId(id);

        return entity;
    }

    public boolean update(RecurringTransaction value) {
        int id = value.getId();

        WhereStatementGenerator generator = new WhereStatementGenerator();
        String where = generator.getStatement(RecurringTransaction.BDID, "=", id);

        return update(value, where);
    }

}
