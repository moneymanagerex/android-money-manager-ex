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
import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.database.Dataset;
import com.money.manager.ex.database.DatasetType;
import com.money.manager.ex.domainmodel.StockHistory;
import com.money.manager.ex.utils.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

import info.javaperformance.money.Money;

/**
 * Stock History
 */
public class StockHistoryRepository
        extends Dataset {

    /**
     * Constructor for content provider.
     */
    public StockHistoryRepository() {
        super(TABLE_NAME, DatasetType.TABLE, "stockhistory");
    }

    /**
     * Constructor that is used when instantiating manually.
     */
    public StockHistoryRepository(Context context) {
        super(TABLE_NAME, DatasetType.TABLE, "stockhistory");

        mContext = context.getApplicationContext();
    }

    private static final String TABLE_NAME = "stockhistory_v1";

    private enum UpdateType {
        Online(1),
        Manual(2);

        UpdateType(int i) {
            this.type = i;
        }
        private int type;
    }

    private Context mContext;
    private String LOGCAT = this.getClass().getSimpleName();

    @Override
    public String[] getAllColumns() {
        return new String[] { "HISTID AS _id",
                StockHistory.HISTID, StockHistory.SYMBOL, StockHistory.DATE,
                StockHistory.VALUE, StockHistory.UPDTYPE
        };
    }

    public boolean addStockHistoryRecord(String symbol, Money price, Date date) {
        boolean success = false;

        boolean recordExists = recordExists(symbol, date);

        ContentValues values = getContentValues(symbol, price, date);

        // check whether to insert or update.
        if (!recordExists) {
            Uri insert = mContext.getContentResolver().insert(getUri(), values);
             long id = ContentUris.parseId(insert);

            if (id > 0) {
                // success
                success = true;
            } else {
                Log.w(LOGCAT, "Failed inserting stock history record.");
            }
        } else {
            // update
            success = updateHistory(symbol, price, date);
        }

        return success;
    }

    public boolean recordExists(String symbol, Date date) {
        boolean result;

        String isoDate = DateUtils.getIsoStringDate(date);
        String selection = StockHistory.SYMBOL + "=? AND " + StockHistory.DATE + "=?";

        Cursor cursor = mContext.getContentResolver().query(getUri(),
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
     * @param symbol
     * @param price
     * @param date
     * @return
     */
    public boolean updateHistory(String symbol, Money price, Date date) {
        boolean result;

        ContentValues values = getContentValues(symbol, price, date);
        String where = StockHistory.SYMBOL + "=?";
        where = DatabaseUtils.concatenateWhere(where, StockHistory.DATE + "=?");
        String[] whereArgs = new String[] { symbol, values.getAsString(StockHistory.DATE) };

        int records = mContext.getContentResolver().update(getUri(),
                values,
                where, whereArgs);

        result = records > 0;

        return result;
    }

    public ContentValues getContentValues(String symbol, Money price, Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.PATTERN_DB_DATE);
        String isoDate = dateFormat.format(date);

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
            ExceptionHandler handler = new ExceptionHandler(mContext, this);
            handler.handle(sqlex, "reading price for " + symbol);
        }
        return null;
    }

    private StockHistory getLatestPriceFor_Internal(String symbol) {

        Cursor cursor = mContext.getContentResolver().query(getUri(),
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

    /**
     * deletes all price history
     * @return number of deleted records
     */
    public int deletePriceHistory() {

        // Delete all manually downloaded prices.
        int actionResult = mContext.getContentResolver().delete(getUri(),
                StockHistory.UPDTYPE + "=?",
                new String[] { "1" });


        return actionResult;
    }
}
