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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.money.manager.ex.database.Dataset;
import com.money.manager.ex.database.DatasetType;
import com.money.manager.ex.database.TableAccountList;

import java.math.BigDecimal;

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
    private String LOGCAT = this.getClass().getSimpleName();

    @Override
    public String[] getAllColumns() {
        return new String[] {
                "STOCKID AS _id", STOCKID, HELDAT, PURCHASEDATE, STOCKNAME, SYMBOL, CURRENTPRICE
        };
    }

    public boolean load(int accountId) {
        boolean result = false;

//        SQLiteDatabase database = MoneyManagerOpenHelper.getInstance(mContext)
//                .getReadableDatabase();
//        if (database == null) return result;

        String selection = TableAccountList.ACCOUNTID + "=?";
//        Cursor cursor = database.query(
//                // table
//                this.getSource(),
//                // columns
//                null,
//                // selection, arguments
//                selection, new String[] { Integer.toString(accountId) },
//                // group by, having, order by
//                null, null, null);
        Cursor cursor = mContext.getContentResolver().query(this.getUri(),
                null,
                selection,
                new String[] { Integer.toString(accountId) },
                null
        );

        // check if cursor is valid
        if (cursor != null && cursor.moveToFirst()) {
            this.setValueFromCursor(cursor);

            cursor.close();
            result = true;
        }
//        database.close();

        return result;
    }

//    public CursorLoader getCursorLoader(int accountId) {
//        String selection = HELDAT + "=?";
//
//        CursorLoader loader = new CursorLoader(mContext, getUri(),
//                null,
//                selection,
//                new String[] { Integer.toString(accountId) }, null);
//
//        return loader;
//    }

    /**
     * Retrieves all record ids which refer the given symbol.
     * @return array of ids of records which contain the symbol.
     */
    public int[] findIdsBySymbol(String symbol) {
        int[] result = null;

//        SQLiteDatabase db = MoneyManagerOpenHelper.getInstance(mContext)
//                .getReadableDatabase();
//        Cursor cursor = db.query(this.getSource(),
//                new String[]{STOCKID},
//                SYMBOL + "=?", new String[]{symbol},
//                null, null, null
//        );

        Cursor cursor = mContext.getContentResolver().query(this.getUri(),
                new String[]{STOCKID},
                SYMBOL + "=?", new String[]{symbol},
                null);

        if (cursor != null) {
            int records = cursor.getCount();
            result = new int[records];

            for (int i = 0; i < records; i++) {
                cursor.moveToNext();
                result[i] = cursor.getInt(cursor.getColumnIndex(STOCKID));
            }
            cursor.close();
        }

//        db.close();

        return result;
    }

    /**
     * Update price for the security id.
     */
    public boolean updatePrice(int id, BigDecimal price) {
        boolean result = false;

        ContentValues values = new ContentValues();
//        values.put(STOCKID, id);
        values.put(CURRENTPRICE, price.doubleValue());

//        SQLiteDatabase db = MoneyManagerOpenHelper.getInstance(mContext)
//                .getWritableDatabase();
//        int updateResult = db.update(
//                getSource(),
//                values,
//                STOCKID + "=?",
//                new String[]{Integer.toString(id)}
//        );
        int updateResult = mContext.getContentResolver().update(this.getUri(),
                values,
                STOCKID + "=?",
                new String[]{Integer.toString(id)}
                );

        if (updateResult != 0) {
            result = true;
        } else {
            Log.w(LOGCAT, "Price update failed for stock id:" + id);
        }

//        db.close();
        return  result;
    }

    /**
     * Update price for all the records with this symbol.
     * @param symbol
     * @param price
     */
    public void updateCurrentPrice(String symbol, BigDecimal price) {
        int[] ids = findIdsBySymbol(symbol);

        for (int id : ids) {
            updatePrice(id, price);
        }
    }
}
