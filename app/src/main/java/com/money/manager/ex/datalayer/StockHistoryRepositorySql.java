/*
 * Copyright (C) 2012-2019 The Android Money Manager Ex Project Team
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

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;

import com.money.manager.ex.domainmodel.StockHistory;
import com.money.manager.ex.utils.MmxDate;
import com.squareup.sqlbrite.BriteDatabase;

import java.util.Date;

import javax.inject.Inject;

import info.javaperformance.money.Money;
import timber.log.Timber;

/**
 * Sqlite-based repository
 */
public class StockHistoryRepositorySql
    extends SqlRepositoryBase<StockHistory> {

    private static final String TABLE_NAME = "stockhistory_v1";

    @Inject
    public StockHistoryRepositorySql(BriteDatabase db) {
        super(TABLE_NAME, db);

//        application.iocComponent.inject(this);
    }

    public boolean addStockHistoryRecord(String symbol, Money price, Date date) {
        boolean success = false;

        boolean recordExists = recordExists(symbol, date);

        // check whether to insert or update.
        if (!recordExists) {
            ContentValues values = getContentValues(symbol, price, date);
            long id = database.insert(TABLE_NAME, values);

            if (id > 0) {
                // success
                success = true;
            } else {
                Timber.w("Failed inserting stock history record.");
            }
        } else {
            // update
            success = updateHistory(symbol, price, date);
        }

        // todo: notify of changes. sync manager.

        return success;
    }

    public ContentValues getContentValues(String symbol, Money price, Date date) {
        String isoDate = new MmxDate(date).toIsoDateString();

        ContentValues values = new ContentValues();
        values.put(StockHistory.SYMBOL, symbol);
        values.put(StockHistory.DATE, isoDate);
        values.put(StockHistory.VALUE, price.toString());
        values.put(StockHistory.UPDTYPE, StockHistoryRepository.UpdateType.Online.type);

        return values;
    }

    public boolean recordExists(String symbol, Date date) {
        boolean result;

        String isoDate = new MmxDate(date).toIsoDateString();

        String sql = new Select()
            .from(TABLE_NAME)
            .where(StockHistory.SYMBOL + "=? AND " + StockHistory.DATE + "=?")
            .toString();

        Cursor cursor = database.query(sql, symbol, isoDate);
        if (cursor == null) return false;

        int records = cursor.getCount();
        result = records > 0;

        cursor.close();

        return result;
    }

    public boolean updateHistory(String symbol, Money price, Date date) {
        boolean result;

        ContentValues values = getContentValues(symbol, price, date);
        String where = StockHistory.SYMBOL + "=?";
        where = DatabaseUtils.concatenateWhere(where, StockHistory.DATE + "=?");
        String[] whereArgs = new String[] { symbol, values.getAsString(StockHistory.DATE) };

        int records = database.update(TABLE_NAME, values, where, whereArgs);

        result = records > 0;

        return result;
    }

}
