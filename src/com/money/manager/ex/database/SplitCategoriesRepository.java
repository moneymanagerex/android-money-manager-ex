/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.money.manager.ex.database;

import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;

/**
 * Repository for Split Categories (TableSplitTransaction).
 */
public class SplitCategoriesRepository {
    public SplitCategoriesRepository(Context context) {
        mContext = context;
    }

    private Context mContext;

    /**
     * Loads split transactions for the given transaction id.
     * @param transId Id of the main transaction for which to load the splits.
     * @return list of split categories for the given transaction.
     */
    public ArrayList<TableSplitTransactions> loadSplitCategoriesFor(int transId) {
        ArrayList<TableSplitTransactions> listSplitTrans = null;

        TableSplitTransactions split = new TableSplitTransactions();
        Cursor curSplit = mContext.getContentResolver()
                .query(split.getUri(), null,
                        TableSplitTransactions.TRANSID + "=" + Integer.toString(transId),
                        null, TableSplitTransactions.SPLITTRANSID);
        if (curSplit != null && curSplit.moveToFirst()) {
            listSplitTrans = new ArrayList<>();
            while (!curSplit.isAfterLast()) {
                TableSplitTransactions obj = new TableSplitTransactions();
                obj.setValueFromCursor(curSplit);
                listSplitTrans.add(obj);
                curSplit.moveToNext();
            }

            curSplit.close();
        }

        return listSplitTrans;
    }

}
