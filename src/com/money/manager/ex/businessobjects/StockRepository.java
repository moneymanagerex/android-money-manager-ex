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
import android.database.DatabaseUtils;
import android.support.v4.database.DatabaseUtilsCompat;
import android.util.Log;

import com.money.manager.ex.Constants;
import com.money.manager.ex.database.Dataset;
import com.money.manager.ex.database.DatasetType;
import com.money.manager.ex.database.TableAccountList;
import com.money.manager.ex.database.TableStock;

import org.apache.commons.lang3.ArrayUtils;

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
    public static final String NUMSHARES = "NUMSHARES";
    public static final String VALUE = "VALUE";

    private Context mContext;
    private String LOGCAT = this.getClass().getSimpleName();

    @Override
    public String[] getAllColumns() {
        String [] idColumn = new String[] {
                "STOCKID AS _id"
        };

        return ArrayUtils.addAll(idColumn, tableColumns());
    }

    public String[] tableColumns() {
        return new String[] {
                STOCKID, HELDAT, PURCHASEDATE, STOCKNAME, SYMBOL, CURRENTPRICE,
                NUMSHARES, VALUE
        };
    }

    public ContentValues load(int id) {
        if (id == Constants.NOT_SET) return null;

        Cursor cursor = mContext.getContentResolver().query(this.getUri(),
                null,
                STOCKID + "=?",
                new String[] { Integer.toString(id)},
                null);
        if (cursor == null) return null;
        if (!cursor.moveToNext()) return null;

        ContentValues stockValues = new ContentValues();

        String[] columns = tableColumns();
        for(String column : columns) {
            DatabaseUtils.cursorDoubleToContentValuesIfPresent(cursor, stockValues, column);
        }

        cursor.close();

        return stockValues;
    }

    public boolean loadFor(int accountId) {
        boolean result = false;

        String selection = TableAccountList.ACCOUNTID + "=?";
        Cursor cursor = mContext.getContentResolver().query(this.getUri(),
                null,
                selection,
                new String[] { Integer.toString(accountId) },
                null
        );
        if (cursor == null) return false;

        // check if cursor is valid
        if (cursor.moveToFirst()) {
            this.setValueFromCursor(cursor);

            result = true;
        }
        cursor.close();

        return result;
    }

    /**
     * Retrieves all record ids which refer the given symbol.
     * @return array of ids of records which contain the symbol.
     */
    public int[] findIdsBySymbol(String symbol) {
        int[] result = null;

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

        return result;
    }

    public boolean update(int id, ContentValues values) {
        boolean result = false;

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

        return  result;
    }


    /**
     * Update price for all the records with this symbol.
     * @param symbol
     * @param price
     */
    public void updateCurrentPrice(String symbol, BigDecimal price) {
        int[] ids = findIdsBySymbol(symbol);

        // recalculate value

        for (int id : ids) {
            //updatePrice(id, price);

            ContentValues oldValues = load(id);
            double numberOfSharesD = oldValues.getAsDouble(NUMSHARES);
            BigDecimal numberOfShares = new BigDecimal(numberOfSharesD);
            BigDecimal value = numberOfShares.multiply(price);

            ContentValues newValues = new ContentValues();
            newValues.put(CURRENTPRICE, price.doubleValue());
            newValues.put(VALUE, value.doubleValue());

            update(id, newValues);
        }
    }

}
