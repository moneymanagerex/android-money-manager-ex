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

package com.money.manager.ex.servicelayer;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
//import net.sqlcipher.database.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.database.TableInfoTable;

/**
 * Access and manipulation of the info in the Info Table
 */
public class InfoService {

    // Keys for the info values in the info table.
    public static final String BASECURRENCYID = "BASECURRENCYID";
    public static final String SHOW_OPEN_ACCOUNTS = "android:show_open_accounts";
    public static final String SHOW_FAVOURITE_ACCOUNTS = "android:show_fav_accounts";
    public static final String INFOTABLE_USERNAME = "USERNAME";
    public static final String INFOTABLE_DATEFORMAT = "DATEFORMAT";
    public static final String INFOTABLE_FINANCIAL_YEAR_START_DAY = "FINANCIAL_YEAR_START_DAY";
    public static final String INFOTABLE_FINANCIAL_YEAR_START_MONTH = "FINANCIAL_YEAR_START_MONTH";
    public static final String INFOTABLE_SKU_ORDER_ID = "SKU_ORDER_ID";

    public InfoService(Context context) {
        mContext = context;
        mInfoTable = new TableInfoTable();
    }

    private Context mContext;
    private TableInfoTable mInfoTable;

    public long insertRaw(SQLiteDatabase db, String key, Integer value) {
        ContentValues values = new ContentValues();

        values.put(TableInfoTable.INFONAME, key);
        values.put(TableInfoTable.INFOVALUE, value);

        return db.insert(mInfoTable.getSource(), null, values);
    }

    public long insertRaw(SQLiteDatabase db, String key, String value) {
        ContentValues values = new ContentValues();

        values.put(TableInfoTable.INFONAME, key);
        values.put(TableInfoTable.INFOVALUE, value);

        return db.insert(mInfoTable.getSource(), null, values);
    }

    /**
     * Update the values via direct access to the database.
     * @param db        Database to use
     * @param recordId  Id of the info record. Required for the update statement.
     * @param key       Info Name
     * @param value     Info Value
     * @return the number of rows affected
     */
    public long updateRaw(SQLiteDatabase db, int recordId, String key, Integer value) {
        ContentValues values = new ContentValues();
        values.put(TableInfoTable.INFONAME, key);
        values.put(TableInfoTable.INFOVALUE, value);

        return db.update(mInfoTable.getSource(),
                values,
                TableInfoTable.INFOID + "=?",
                new String[] { Integer.toString(recordId)}
        );
    }

    public long updateRaw(SQLiteDatabase db, String key, String value) {
        ContentValues values = new ContentValues();
        values.put(TableInfoTable.INFONAME, key);
        values.put(TableInfoTable.INFOVALUE, value);

        return db.update(mInfoTable.getSource(), values,
                TableInfoTable.INFONAME + "=?",
                new String[] { key });
    }

    /**
     * Retrieve value of info
     *
     * @param info to be retrieve
     * @return value
     */
    public String getInfoValue(String info) {
        Cursor cursor;
        String ret = null;

        try {
            cursor = mContext.getContentResolver().query(mInfoTable.getUri(),
                    null,
                    TableInfoTable.INFONAME + "=?",
                    new String[]{ info },
                    null);
            if (cursor == null) return null;

            if (cursor.moveToFirst()) {
//                ContentValues values = new ContentValues();
//                DatabaseUtils.cursorRowToContentValues(cursor, values);
                ret = cursor.getString(cursor.getColumnIndex(TableInfoTable.INFOVALUE));
            }
            cursor.close();
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
        // check if info exists
        boolean exists = (getInfoValue(key) != null);

        ContentValues values = new ContentValues();
        values.put(TableInfoTable.INFOVALUE, value);

        try {
            if (exists) {
                int updated = mContext.getContentResolver().update(mInfoTable.getUri(),
                        values,
                        TableInfoTable.INFONAME + "=?",
                        new String[]{key});
                result = updated >= 0;
            } else {
                values.put(TableInfoTable.INFONAME, key);
                Uri insertUri = mContext.getContentResolver().insert(mInfoTable.getUri(),
                        values);
                long id = ContentUris.parseId(insertUri);
                result = id > 0;
            }
        } catch (Exception e) {
            ExceptionHandler handler = new ExceptionHandler(mContext, this);
            handler.handle(e, "writing info value");
        }

        return result;
    }

}
