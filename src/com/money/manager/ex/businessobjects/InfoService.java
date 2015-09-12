package com.money.manager.ex.businessobjects;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.money.manager.ex.Constants;
import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.database.MoneyManagerOpenHelper;
import com.money.manager.ex.database.TableCurrencyFormats;
import com.money.manager.ex.database.TableInfoTable;

/**
 * Access and manipulation of the info in the Info Table
 * Created by Alen Siljak on 12/08/2015.
 */
public class InfoService {

    public static String BASECURRENCYID = "BASECURRENCYID";

    public InfoService(Context context) {
        mContext = context;
    }

    private Context mContext;

    public long insertRaw(SQLiteDatabase db, String key, Integer value) {
        ContentValues values = new ContentValues();

        values.put(TableInfoTable.INFONAME, key);
        values.put(TableInfoTable.INFOVALUE, value);

        return db.insert(new TableInfoTable().getSource(), null, values);
    }

    public long insertRaw(SQLiteDatabase db, String key, String value) {
        ContentValues values = new ContentValues();

        values.put(TableInfoTable.INFONAME, key);
        values.put(TableInfoTable.INFOVALUE, value);

        return db.insert(new TableInfoTable().getSource(), null, values);
    }

    public long updateRaw(SQLiteDatabase db, String key, Integer value) {
        TableCurrencyFormats currencyFormats = new TableCurrencyFormats();

        ContentValues values = new ContentValues();
        values.put(TableInfoTable.INFONAME, key);
        values.put(TableInfoTable.INFOVALUE, value);

        return db.update(currencyFormats.getSource(), values,
                null, null);
    }

    public long updateRaw(SQLiteDatabase db, String key, String value) {
        TableCurrencyFormats currencyFormats = new TableCurrencyFormats();

        ContentValues values = new ContentValues();
        values.put(TableInfoTable.INFONAME, key);
        values.put(TableInfoTable.INFOVALUE, value);

        return db.update(currencyFormats.getSource(), values,
                null, null);
    }

    /**
     * Retrieve value of info
     *
     * @param info to be retrieve
     * @return value
     */
    public String getInfoValue(String info) {
        TableInfoTable infoTable = new TableInfoTable();
        Cursor data;
        String ret = null;

        try {
            data = mContext.getContentResolver().query(infoTable.getUri(),
                    null,
                    TableInfoTable.INFONAME + "=?",
                    new String[]{ info },
                    null);
            if (data == null) return null;

            if (data.moveToFirst()) {
                ret = data.getString(data.getColumnIndex(TableInfoTable.INFOVALUE));
            }
            data.close();
        } catch (Exception e) {
            ExceptionHandler handler = new ExceptionHandler(mContext, this);
            handler.handle(e, "retrieving info value: " + info);
        }

        return ret;
    }

    /**
     * Update value of info.
     *
     * @param key  to be updated
     * @param value value to be used
     * @return true if update success otherwise false
     */
    public boolean setInfoValue(String key, String value) {
        boolean result = false;
        TableInfoTable infoTable = new TableInfoTable();
        // check if exists info
        boolean exists = !TextUtils.isEmpty(getInfoValue(key));

        ContentValues values = new ContentValues();
        values.put(TableInfoTable.INFOVALUE, value);

        try {
            MoneyManagerOpenHelper helper = MoneyManagerOpenHelper.getInstance(mContext);
            if (exists) {
                result = helper.getWritableDatabase().update(infoTable.getSource(),
                        values,
                        TableInfoTable.INFONAME + "=?",
                        new String[]{key}) >= 0;
            } else {
                values.put(TableInfoTable.INFONAME, key);
                result = helper.getWritableDatabase().insert(infoTable.getSource(),
                        null,
                        values) >= 0;
            }
        } catch (Exception e) {
            ExceptionHandler handler = new ExceptionHandler(mContext, this);
            handler.handle(e, "writing info value");
        }

        return result;
    }

}
