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

import android.database.Cursor;

import com.money.manager.ex.Constants;
import com.money.manager.ex.domainmodel.Stock;
import com.squareup.sqlbrite.BriteDatabase;

import javax.inject.Inject;

import info.javaperformance.money.Money;

/**
 * Stock Repository, using Sqlite via SqlBrite.
 */

public class StockRepositorySql
    extends SqlRepositoryBase<Stock> {

    public static final String TABLE_NAME = "stock_v1";

    @Inject
    public StockRepositorySql(BriteDatabase db) {
        super(TABLE_NAME, db);

//        application.iocComponent.inject(this);
    }


    public boolean delete(int id) {
        int result = super.delete(StockFields.STOCKID + "=?", Integer.toString(id));
        return result > 0;
    }

    public Stock load(int id) {
        if (id == Constants.NOT_SET) return null;

        return first(Stock.class,
                null,
                StockFields.STOCKID + "=?",
                new String[] { Integer.toString(id) },
                null);
    }

    /**
     * Update price for all the records with this symbol.
     * @param symbol Stock symbol
     * @param price Stock price
     */
    public void updateCurrentPrice(String symbol, Money price) {
        int[] ids = findIdsBySymbol(symbol);
        if (ids == null) return;

        // recalculate value

        for (int id : ids) {
            Stock stock = load(id);
            stock.setCurrentPrice(price);
            // recalculate & assign the value
            Money value = stock.getValue();

            save(stock);
        }
        // todo: update notification for sync
    }

    public boolean save(Stock stock) {
        int id = stock.getId();

        return update(stock, StockFields.STOCKID + "=?", Integer.toString(id));
    }

    // Private

    /**
     * Retrieves all record ids which refer the given symbol.
     * @return array of ids of records which contain the symbol.
     */
    private int[] findIdsBySymbol(String symbol) {
        int[] result;

        String sql = new Select(StockFields.STOCKID)
            .from(tableName)
            .where(StockFields.SYMBOL + "=?")
            .toString();

        Cursor cursor = database.query(sql, symbol);
        if (cursor == null) return null;

        int records = cursor.getCount();
        result = new int[records];

        for (int i = 0; i < records; i++) {
            cursor.moveToNext();
            result[i] = cursor.getInt(cursor.getColumnIndex(StockFields.STOCKID));
        }
        cursor.close();

        return result;
    }

}
