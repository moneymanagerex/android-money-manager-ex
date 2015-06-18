package com.money.manager.ex.businessobjects;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.money.manager.ex.Constants;
import com.money.manager.ex.businessobjects.StockHistory;
import com.money.manager.ex.database.Dataset;
import com.money.manager.ex.database.DatasetType;
import com.money.manager.ex.database.MoneyManagerOpenHelper;
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

        mContext = context;
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

    public boolean addStockHistoryRecord(String symbol, BigDecimal price, Date date) {
        boolean success = false;

        boolean recordExists = recordExists(symbol, date);

//        SQLiteDatabase db = MoneyManagerOpenHelper.getInstance(mContext)
//                .getWritableDatabase();
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

//        db.close();

        return success;
    }

    public boolean recordExists(String symbol, Date date) {
        boolean result = false;
//        SQLiteDatabase db = MoneyManagerOpenHelper.getInstance(mContext)
//                .getReadableDatabase();

        String isoDate = DateUtils.getSQLiteStringDate(mContext, date);
        String selection = StockHistory.SYMBOL + "=? AND " + StockHistory.DATE + "=?";

//        Cursor cursor = db.query(getSource(), null,
//                selection,
//                new String[]{symbol, isoDate},
//                null, null, null
//        );
        Cursor cursor = mContext.getContentResolver().query(getUri(),
                null,
                selection,
                new String[]{symbol, isoDate},
                null);

        if (cursor != null) {
            int records = cursor.getCount();
            result = records > 0;

            cursor.close();
        }

//        db.close();

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

//        SQLiteDatabase db = MoneyManagerOpenHelper.getInstance(mContext).getWritableDatabase();

        ContentValues values = getContentValues(symbol, price, date);
        String where = StockHistory.SYMBOL + "=?";
        where = DatabaseUtils.concatenateWhere(where, StockHistory.DATE + "=?");
        String[] whereArgs = new String[] { symbol, values.getAsString(StockHistory.DATE) };

//        int records = db.update(getSource(),
//                values,
//                where, whereArgs);
        int records = mContext.getContentResolver().update(getUri(),
                values,
                where, whereArgs);

        result = records > 0;

//        db.close();

        return result;
    }

    public ContentValues getContentValues(String symbol, BigDecimal price, Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.PATTERN_DB_DATE);
        String isoDate = dateFormat.format(date);

        ContentValues values = new ContentValues();
        values.put(StockHistory.SYMBOL, symbol);
        values.put(StockHistory.DATE, isoDate);
        values.put(StockHistory.VALUE, price.toPlainString());
        values.put(StockHistory.UPDTYPE, UpdateType.Online.type);

        return values;
    }

    public ContentValues getLatestPriceFor(String symbol) {
        try {
            return getLatestPriceFor_Internal(symbol);
        } catch (SQLiteException sqlex) {
            String error = "Error reading price for " + symbol;
            Log.e(LOGCAT, error + ": " + sqlex.getLocalizedMessage());
            Toast.makeText(mContext, error, Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    private ContentValues getLatestPriceFor_Internal(String symbol) {
//        SQLiteDatabase db = MoneyManagerOpenHelper.getInstance(mContext)
//                .getReadableDatabase();

        String selection = StockHistory.SYMBOL + "=?";

//        Cursor cursor = db.query(getSource(),
//                null,
//                selection,
//                new String[] { symbol },
//                null, // group by
//                null, // having
//                // order by
//                StockHistory.DATE + " DESC"
//        );
        Cursor cursor = mContext.getContentResolver().query(getUri(),
                null,
                selection,
                new String[]{symbol},
                StockHistory.DATE + " DESC");

        ContentValues result = new ContentValues();

        if (cursor != null) {
            cursor.moveToFirst();

//            Date date = getDateFromCursor(cursor);
//            BigDecimal price = getPriceFromCursor(cursor);
//            result = getContentValues(symbol, price, date);

            // keep the raw values for now
            result.put(StockHistory.SYMBOL, symbol);

            String dateString = cursor.getString(cursor.getColumnIndex(StockHistory.DATE));
            result.put(StockHistory.DATE, dateString);

            String priceString = cursor.getString(cursor.getColumnIndex(StockHistory.VALUE));
            result.put(StockHistory.VALUE, priceString);

            cursor.close();
        }

//        db.close();

        return result;
    }

    public Date getDateFromCursor(Cursor cursor) {
        String dateString = cursor.getString(cursor.getColumnIndex(StockHistory.DATE));
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
        String priceString = cursor.getString(cursor.getColumnIndex(StockHistory.VALUE));
        BigDecimal result = new BigDecimal(priceString);
        return result;
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
