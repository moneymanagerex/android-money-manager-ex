package com.money.manager.ex.businessobjects;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.money.manager.ex.Constants;
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
}
