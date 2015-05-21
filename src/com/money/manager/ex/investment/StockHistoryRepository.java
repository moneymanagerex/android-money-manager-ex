package com.money.manager.ex.investment;

import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.money.manager.ex.Constants;
import com.money.manager.ex.database.Dataset;
import com.money.manager.ex.database.DatasetType;
import com.money.manager.ex.database.MoneyManagerOpenHelper;
import com.money.manager.ex.database.StockHistory;
import com.money.manager.ex.utils.DateUtils;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Stock History
 */
public class StockHistoryRepository
        extends Dataset {

    /**
     *
     */
    public StockHistoryRepository(Context context) {
        super(TABLE_NAME, DatasetType.TABLE, "stockhistory");

        mContext = context;
    }

    private static final String TABLE_NAME = "stockhistory_v1";
    // fields
    public static final String HISTID = "HISTID";
    public static final String SYMBOL = "SYMBOL";
    public static final String DATE = "DATE";
    public static final String VALUE = "VALUE";
    public static final String UPDTYPE = "UPDTYPE";

    // UpdateType: online = 1, manual = 2
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

    public boolean addStockHistoryRecord(String symbol, BigDecimal price, Date date) {
        boolean success = false;

        boolean recordExists = recordExists(symbol, date);

        SQLiteDatabase db = MoneyManagerOpenHelper.getInstance(mContext)
                .getWritableDatabase();
        ContentValues values = getContentValues(symbol, price, date);

        // check whether to insert or update.
        if (!recordExists) {
            long records = db.insert(getSource(), null, values);

            if (records >= 1) {
                // success
                success = true;
            } else {
                Log.w(LOGCAT, "Failed inserting stock history record.");
            }
        } else {
            // update
            success = updateHistory(symbol, price, date);
        }

        db.close();

        return success;
    }

    public boolean recordExists(String symbol, Date date) {
        boolean result = false;
        SQLiteDatabase db = MoneyManagerOpenHelper.getInstance(mContext)
                .getReadableDatabase();

        String isoDate = DateUtils.getSQLiteStringDate(mContext, date);
        String selection = SYMBOL + "=? AND " + DATE + "=?";

        Cursor cursor = db.query(getSource(), null,
                selection,
                new String[]{symbol, isoDate},
                null, null, null
        );

        if (cursor != null) {
            int records = cursor.getCount();
            result = records > 0;

            cursor.close();
        }

        db.close();

        return result;
    }

    /**
     * Update history record.
     * @param symbol
     * @param price
     * @param date
     * @return
     */
    public boolean updateHistory(String symbol, BigDecimal price, Date date) {
        boolean result = false;

        SQLiteDatabase db = MoneyManagerOpenHelper.getInstance(mContext).getWritableDatabase();

        ContentValues values = getContentValues(symbol, price, date);
        String where = SYMBOL + "=?";
        where = DatabaseUtils.concatenateWhere(where, DATE + "=?");
        String[] whereArgs = new String[] { symbol, values.getAsString(DATE) };

        int records = db.update(getSource(),
                values,
                where, whereArgs);

        result = records > 0;

        db.close();

        return result;
    }

    public ContentValues getContentValues(String symbol, BigDecimal price, Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.PATTERN_DB_DATE);
        String isoDate = dateFormat.format(date);

        ContentValues values = new ContentValues();
        values.put(SYMBOL, symbol);
        values.put(DATE, isoDate);
        values.put(VALUE, price.toPlainString());
        values.put(UPDTYPE, UpdateType.Online.type);

        return values;
    }

    /*
    select histid, symbol, date, value
    from stockhistory_v1
    group by symbol
     */

    public ContentValues getLatestPriceFor(String symbol) {
        SQLiteDatabase db = MoneyManagerOpenHelper.getInstance(mContext)
                .getReadableDatabase();

        String selection = SYMBOL + "=?";

        Cursor cursor = db.query(getSource(),
                null,
                selection,
                new String[] { symbol },
                null, null, null
        );

        ContentValues result = null;

        if (cursor != null) {
            //StockHistory history = new StockHistory();
            Date date = getDateFromCursor(cursor);
            BigDecimal price = getPriceFromCursor(cursor);
            result = getContentValues(symbol, price, date);

            cursor.close();
        }

        db.close();

        return result;
    }

    public Date getDateFromCursor(Cursor cursor) {
        String dateString = cursor.getString(cursor.getColumnIndex(DATE));
        SimpleDateFormat format = new SimpleDateFormat(Constants.PATTERN_DB_DATE);
        Date date = null;
        try {
            date = format.parse(dateString);
        } catch (ParseException pex) {
            Log.e(LOGCAT, "Error parsing the date from stock history.");
//            date = new Date();
        }
        return date;
    }

    public BigDecimal getPriceFromCursor(Cursor cursor) {
        String priceString = cursor.getString(cursor.getColumnIndex(VALUE));
        BigDecimal result = new BigDecimal(priceString);
        return result;
    }
}
