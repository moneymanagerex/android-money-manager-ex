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
import com.money.manager.ex.database.ISplitTransaction;
import com.money.manager.ex.database.ITransactionEntity;
import com.money.manager.ex.database.WhereStatementGenerator;
import com.money.manager.ex.domainmodel.SplitRecurringCategory;

import java.util.ArrayList;

/**
 * Repository for Recurring Split Categories (TableBudgetSplitTransactions).
 */
public class SplitRecurringCategoriesRepository
    extends RepositoryBase
    implements IRepository {

    public SplitRecurringCategoriesRepository(Context context) {
        super(context, SplitRecurringCategory.TABLE_NAME, DatasetType.TABLE, "budgetsplittransactions");
    }

    @Override
    public String[] getAllColumns() {
        return new String[] {"SPLITTRANSID AS _id",
            SplitRecurringCategory.SPLITTRANSID,
            SplitRecurringCategory.TRANSID,
            SplitRecurringCategory.CATEGID,
            SplitRecurringCategory.SUBCATEGID,
            SplitRecurringCategory.SPLITTRANSAMOUNT };
    }

    /**
     * Loads split transactions for the given transaction id.
     * @param transId Id of the main transaction for which to load the splits.
     * @return list of split categories for the given transaction.
     */
    public ArrayList<ISplitTransaction> loadSplitCategoriesFor(int transId) {
        Cursor curSplit = getContext().getContentResolver().query(getUri(), null,
            SplitRecurringCategory.TRANSID + "=" + Integer.toString(transId),
            null,
            SplitRecurringCategory.SPLITTRANSID);
        if (curSplit == null) return null;

        ArrayList<ISplitTransaction> listSplitTrans = new ArrayList<>();

        while (curSplit.moveToNext()) {
            SplitRecurringCategory splitRecurringCategory = new SplitRecurringCategory();
            splitRecurringCategory.loadFromCursor(curSplit);

            listSplitTrans.add(splitRecurringCategory);
        }
        curSplit.close();

        return listSplitTrans;
    }

    public boolean insert(SplitRecurringCategory item) {
        // Remove any existing id value.
        item.contentValues.remove(SplitRecurringCategory.SPLITTRANSID);

        int id = this.insert(item.contentValues);
        item.setId(id);

        return id > 0;
    }

    public boolean update(SplitRecurringCategory entity) {
        WhereStatementGenerator where = new WhereStatementGenerator();
        where.addStatement(SplitRecurringCategory.SPLITTRANSID, "=", entity.getId());

        return update(entity, where.getWhere());
    }

    public boolean delete (int id) {
        int deleted = super.delete(SplitRecurringCategory.SPLITTRANSID + "=?",
                new String[]{ Integer.toString(id) });

        return deleted == 1;
    }

    public boolean delete(ISplitTransaction entity) {
        return delete(entity.getId());
    }

    public boolean delete(IEntity entity) {
        return delete((ISplitTransaction) entity);
    }
}
