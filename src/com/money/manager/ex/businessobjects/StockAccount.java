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
package com.money.manager.ex.businessobjects;

import android.content.Context;
import android.support.v4.content.CursorLoader;

import com.money.manager.ex.R;
import com.money.manager.ex.database.Dataset;
import com.money.manager.ex.database.DatasetType;
import com.money.manager.ex.database.MoneyManagerOpenHelper;
import com.money.manager.ex.database.QueryAccountBills;
import com.money.manager.ex.database.TableAccountList;
import com.money.manager.ex.utils.RawFileUtils;

/**
 * Stocks account
 */
public class StockAccount
    extends Dataset {

    /**
     * Constructor for Stock dataset.
     *
     * source   table/view/query
     * type     of dataset
     * basepath for match uri
     */
    public StockAccount(Context context) {
        super(TABLE_NAME, DatasetType.TABLE, "stock");

        mContext = context;
    }

    private static final String TABLE_NAME = "stock_v1";

    private Context mContext;

//    public void load(int accountId) {
//        String selection = TableAccountList.ACCOUNTID + "=?";
//        SQLiteDatabase database = MoneyManagerOpenHelper..getReadableDatabase();
//        if (database != null) {
//            Cursor cursor = database.query(new TableAccountList().getSource(), null, selection,
//                    new String[]{Integer.toString(id)}, null, null, null);
//            // check if cursor is valid
//            if (cursor != null && cursor.moveToFirst()) {
//                TableAccountList account = new TableAccountList();
//                account.setValueFromCursor(cursor);
//
//                cursor.close();
//                return account;
//            }
//            database.close();
//            //close();
//        }
//        // find is false then return null
//        return null;
//
//    }

    public CursorLoader getCursorLoader(int accountId) {
        String selection = QueryAccountBills.ACCOUNTID + "=?";

        CursorLoader loader = new CursorLoader(mContext,
                new QueryAccountBills(mContext).getUri(),
                null,
                selection,
                new String[] { Integer.toString(accountId) }, null);

        return loader;
    }
}
