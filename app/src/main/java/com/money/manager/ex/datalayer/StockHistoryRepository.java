/*
 * Copyright (C) 2012-2016 The Android Money Manager Ex Project Team
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

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.util.Log;

import com.money.manager.ex.Constants;
import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.database.DatasetType;
import com.money.manager.ex.domainmodel.StockHistory;
import com.money.manager.ex.investment.events.PriceDownloadedEvent;
import com.money.manager.ex.utils.MyDateTimeUtils;
import com.squareup.sqlbrite.BriteDatabase;

import org.joda.time.DateTime;


import javax.inject.Inject;

import dagger.Lazy;
import info.javaperformance.money.Money;
import timber.log.Timber;

/**
 * Stock History
 */
public class StockHistoryRepository
    extends RepositoryBase<StockHistory> {

    private static final String TABLE_NAME = "stockhistory_v1";

    /**
     * Constructor that is used when instantiating manually.
     */
    public StockHistoryRepository(Context context) {
        super(context, TABLE_NAME, DatasetType.TABLE, "stockhistory");

    }

    private enum UpdateType {
        Online(1),
        Manual(2);

        UpdateType(int i) {
            this.type = i;
        }
        private int type;
    }

    @Inject
    Lazy<BriteDatabase> database;

    @Override
    public String[] getAllColumns() {
        return new String[] { "HISTID AS _id",
                StockHistory.HISTID,
                StockHistory.SYMBOL,
                StockHistory.DATE,
                StockHistory.VALUE,
                StockHistory.UPDTYPE
        };
    }

    public boolean addStockHistoryRecord(PriceDownloadedEvent price) {
        return addStockHistoryRecord(price.symbol, price.price, price.date);
    }

    public boolean addStockHistoryRecord(String symbol, Money price, DateTime date) {
        boolean success = false;

        boolean recordExists = recordExists(symbol, date);

        // todo: move this to the constructor
        MoneyManagerApplication.getInstance().mainComponent.inject(this);

        // check whether to insert or update.
        if (!recordExists) {
            ContentValues values = getContentValues(symbol, price, date);
            long id = database.get().insert(TABLE_NAME, values);

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

    public boolean recordExists(String symbol, DateTime date) {
        boolean result;

        String isoDate = MyDateTimeUtils.getIsoStringFrom(date);
        String selection = StockHistory.SYMBOL + "=? AND " + StockHistory.DATE + "=?";

        Cursor cursor = getContext().getContentResolver().query(getUri(),
                null,
                selection,
                new String[]{symbol, isoDate},
                null);
        if (cursor == null) return false;

        int records = cursor.getCount();
        result = records > 0;

        cursor.close();

        return result;
    }

    /**
     * Update history record.
     */
    public boolean updateHistory(String symbol, Money price, DateTime date) {
        boolean result;

        ContentValues values = getContentValues(symbol, price, date);
        String where = StockHistory.SYMBOL + "=?";
        where = DatabaseUtils.concatenateWhere(where, StockHistory.DATE + "=?");
        String[] whereArgs = new String[] { symbol, values.getAsString(StockHistory.DATE) };

        int records = database.get().update(TABLE_NAME, values, where, whereArgs);

        result = records > 0;

        return result;
    }

    public ContentValues getContentValues(String symbol, Money price, DateTime date) {
        String isoDate = date.toString(Constants.ISO_DATE_FORMAT);

        ContentValues values = new ContentValues();
        values.put(StockHistory.SYMBOL, symbol);
        values.put(StockHistory.DATE, isoDate);
        values.put(StockHistory.VALUE, price.toString());
        values.put(StockHistory.UPDTYPE, UpdateType.Online.type);

        return values;
    }

    public StockHistory getLatestPriceFor(String symbol) {
        try {
            return getLatestPriceFor_Internal(symbol);
        } catch (SQLiteException sqlex) {
            ExceptionHandler handler = new ExceptionHandler(getContext(), this);
            handler.handle(sqlex, "reading price for " + symbol);
        }
        return null;
    }

    private StockHistory getLatestPriceFor_Internal(String symbol) {
        Cursor cursor = getContext().getContentResolver().query(getUri(),
                null,
                StockHistory.SYMBOL + "=?",
                new String[]{ symbol },
                StockHistory.DATE + " DESC");
        if (cursor == null) return null;

        StockHistory history = null;

        boolean recordFound = cursor.moveToFirst();
        if (recordFound) {
            history = new StockHistory();
            history.loadFromCursor(cursor);
        }

        cursor.close();

        return history;
    }

    public int deleteAutomaticPriceHistory() {
        // Delete all automatically downloaded prices.
        int deleted = getContext().getContentResolver().delete(getUri(),
            StockHistory.UPDTYPE + "=?",
            new String[] { "1" });

        return deleted;
    }

    /**
     * deletes all automatic price history
     * @return number of deleted records
     */
    public int deleteAllPriceHistory() {
        int deleted = getContext().getContentResolver().delete(getUri(),
            "1",
            null);

        return deleted;
    }
}
