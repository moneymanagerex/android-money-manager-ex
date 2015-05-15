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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
public class StockRepository
    extends Dataset {

    /**
     * Constructor for Stock dataset.
     *
     * source   table/view/query
     * type     of dataset
     * basepath for match uri
     */
    public StockRepository(Context context) {
        super(TABLE_NAME, DatasetType.TABLE, "stock");

        mContext = context;
    }

    private static final String TABLE_NAME = "stock_v1";

    // fields
    public static final String STOCKID = "STOCKID";
    public static final String HELDAT = "HELDAT";
    public static final String PURCHASEDATE = "PURCHASEDATE";
    public static final String STOCKNAME = "STOCKNAME";
    public static final String SYMBOL = "SYMBOL";
    public static final String CURRENTPRICE = "CURRENTPRICE";

    private Context mContext;

    @Override
    public String[] getAllColumns() {
        return new String[] {
                "STOCKID AS _id", STOCKID, HELDAT, PURCHASEDATE, STOCKNAME, SYMBOL, CURRENTPRICE
        };
    }

    public boolean load(int accountId) {
        boolean result = false;

        String selection = TableAccountList.ACCOUNTID + "=?";
        SQLiteDatabase database = MoneyManagerOpenHelper.getInstance(mContext).getReadableDatabase();
        if (database == null) return result;

        Cursor cursor = database.query(this.getSource(), null, selection,
                new String[]{Integer.toString(accountId)}, null, null, null);
        // check if cursor is valid
        if (cursor != null && cursor.moveToFirst()) {
            this.setValueFromCursor(cursor);

            cursor.close();
            result = true;
        }
        database.close();

        return result;
    }

    public CursorLoader getCursorLoader(int accountId) {
        String selection = HELDAT + "=?";

        CursorLoader loader = new CursorLoader(mContext, getUri(),
                null,
                selection,
                new String[] { Integer.toString(accountId) }, null);

        return loader;
    }

}
