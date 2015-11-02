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

import android.content.Context;
import android.database.Cursor;

import com.money.manager.ex.database.DatasetType;
import com.money.manager.ex.database.ISplitTransactionsDataset;
import com.money.manager.ex.domainmodel.SplitCategory;

import java.util.ArrayList;

/**
 * Repository for Split Categories (TableSplitTransaction).
 */
public class SplitCategoriesRepository
    extends RepositoryBase {

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
    public ArrayList<ISplitTransactionsDataset> loadSplitCategoriesFor(int transId) {
        Cursor curSplit = getContext().getContentResolver().query(getUri(), null,
            SplitCategory.TRANSID + "=" + Integer.toString(transId),
            null,
            SplitCategory.SPLITTRANSID);
        if (curSplit == null) return null;

        ArrayList<ISplitTransactionsDataset> listSplitTrans = new ArrayList<>();

        while (curSplit.moveToNext()) {
            SplitCategory obj = new SplitCategory();
            obj.loadFromCursor(curSplit);

            listSplitTrans.add(obj);
        }
        curSplit.close();

        return listSplitTrans;
    }

    public SplitCategory insert(SplitCategory item) {
        // Remove any existing id value.
        item.contentValues.remove(SplitCategory.SPLITTRANSID);

        int id = this.insert(item.contentValues);
        item.setId(id);
        return item;
    }

}
