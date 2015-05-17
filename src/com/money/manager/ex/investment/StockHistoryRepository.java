package com.money.manager.ex.investment;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.money.manager.ex.Constants;
import com.money.manager.ex.database.Dataset;
import com.money.manager.ex.database.DatasetType;
import com.money.manager.ex.database.MoneyManagerOpenHelper;

import java.math.BigDecimal;
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

    private Context mContext;
    private String LOGCAT = this.getClass().getSimpleName();

    public boolean addStockHistoryRecord(String symbol, BigDecimal price, Date date) {
        boolean success = false;

        SQLiteDatabase db = MoneyManagerOpenHelper.getInstance(mContext)
                .getReadableDatabase();

        SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.PATTERN_DB_DATE);
        String isoDate = dateFormat.format(date);

        ContentValues values = new ContentValues();
        values.put(SYMBOL, symbol);
        values.put(DATE, isoDate);
        values.put(VALUE, price.toPlainString());
        values.put(UPDTYPE, 2);

        long records = db.insert(getSource(), null, values);

        if (records >= 1) {
            // success
            success = true;
        } else {
            Log.w(LOGCAT, "Failed inserting stock history record.");
        }

        db.close();

        return success;
    }

}
