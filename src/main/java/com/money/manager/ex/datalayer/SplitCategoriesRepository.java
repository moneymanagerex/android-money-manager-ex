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
import com.money.manager.ex.database.TableSplitTransactions;
import com.money.manager.ex.domainmodel.SplitTransaction;

import java.util.ArrayList;

/**
 * Repository for Split Categories (TableSplitTransaction).
 */
public class SplitCategoriesRepository
    extends RepositoryBase {

    public SplitCategoriesRepository(Context context) {
        super(context, SplitTransaction.TABLE_NAME, DatasetType.TABLE, "splittransaction");
    }

    @Override
    public String[] getAllColumns() {
        return new String[] {"SPLITTRANSID AS _id",
            SplitTransaction.SPLITTRANSID,
            SplitTransaction.TRANSID,
            SplitTransaction.CATEGID,
            SplitTransaction.SUBCATEGID,
            SplitTransaction.SPLITTRANSAMOUNT };
    }

    /**
     * Loads split transactions for the given transaction id.
     * @param transId Id of the main transaction for which to load the splits.
     * @return list of split categories for the given transaction.
     */
    public ArrayList<ISplitTransactionsDataset> loadSplitCategoriesFor(int transId) {
        Cursor curSplit = getContext().getContentResolver()
                .query(getUri(), null,
                    SplitTransaction.TRANSID + "=" + Integer.toString(transId),
                    null,
                    SplitTransaction.SPLITTRANSID);
        if (curSplit == null) return null;

        ArrayList<ISplitTransactionsDataset> listSplitTrans = new ArrayList<>();

        while (curSplit.moveToNext()) {
            // todo:
//            SplitTransaction obj = new SplitTransaction();
            TableSplitTransactions obj = new TableSplitTransactions();
            obj.setValueFromCursor(curSplit);
            listSplitTrans.add(obj);
            curSplit.moveToNext();
        }
        curSplit.close();

        return listSplitTrans;
    }

    public void insert() {

    }

}
