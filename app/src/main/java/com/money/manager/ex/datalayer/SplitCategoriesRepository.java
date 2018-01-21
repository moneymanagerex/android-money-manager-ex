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
import com.money.manager.ex.domainmodel.EntityBase;
import com.money.manager.ex.domainmodel.SplitCategory;

import java.util.ArrayList;

/**
 * Repository for Split Categories (TableSplitTransaction).
 */
public class SplitCategoriesRepository
    extends RepositoryBase
    implements IRepository {

    public SplitCategoriesRepository(Context context) {
        super(context, SplitCategory.TABLE_NAME, DatasetType.TABLE, "splittransaction");
    }

    @Override
    public String[] getAllColumns() {
        return new String[] {"SPLITTRANSID AS _id",
            SplitCategory.SPLITTRANSID,
            SplitCategory.TRANSID,
            SplitCategory.CATEGID,
            SplitCategory.SUBCATEGID,
            SplitCategory.SPLITTRANSAMOUNT };
    }

    /**
     * Loads split transactions for the given transaction id.
     * @param transId Id of the main transaction for which to load the splits.
     * @return list of split categories for the given transaction.
     */
    public ArrayList<ISplitTransaction> loadSplitCategoriesFor(int transId) {
        Cursor curSplit = getContext().getContentResolver().query(getUri(), null,
            SplitCategory.TRANSID + "=" + Integer.toString(transId),
            null,
            SplitCategory.SPLITTRANSID);
        if (curSplit == null) return null;

        ArrayList<ISplitTransaction> listSplitTrans = new ArrayList<>();

        while (curSplit.moveToNext()) {
            SplitCategory splitCategory = new SplitCategory();
            splitCategory.loadFromCursor(curSplit);

            listSplitTrans.add(splitCategory);
        }
        curSplit.close();

        return listSplitTrans;
    }

    public boolean insert(SplitCategory item) {
        // Remove any existing id value.
        item.contentValues.remove(SplitCategory.SPLITTRANSID);

        int id = this.insert(item.contentValues);
        item.setId(id);

        return id > 0;
    }

    public boolean update(SplitCategory entity) {
        WhereStatementGenerator where = new WhereStatementGenerator();
        where.addStatement(SplitCategory.SPLITTRANSID, "=", entity.getId());

        return update(entity, where.getWhere());
    }

    public boolean delete(ISplitTransaction entity) {
        int deleted = super.delete(SplitCategory.SPLITTRANSID + "=?",
                new String[]{ Integer.toString(entity.getId()) });
        return deleted == 1;
    }

    public boolean delete(IEntity entity) {
        return delete((ISplitTransaction) entity);
    }
}
