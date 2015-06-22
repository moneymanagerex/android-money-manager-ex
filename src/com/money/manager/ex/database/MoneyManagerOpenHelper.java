/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.money.manager.ex.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDiskIOException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import com.money.manager.ex.BuildConfig;
import com.money.manager.ex.Constants;
import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.utils.CurrencyUtils;
import com.money.manager.ex.utils.RawFileUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

/**
 * @author Alessandro Lazzari (lazzari.ale@gmail.com)
 * @version 1.0.1
 */
public class MoneyManagerOpenHelper
        extends SQLiteOpenHelper {

    private static final String LOGCAT = MoneyManagerOpenHelper.class.getSimpleName();
    private static final int databaseCurrentVersion = 1;
    // singleton
    private static MoneyManagerOpenHelper mInstance;
    // context of creation
    private Context mContext;

    private MoneyManagerOpenHelper(Context context) {
        super(context, MoneyManagerApplication.getDatabasePath(context), null, databaseCurrentVersion);
        this.mContext = context;

        if (BuildConfig.DEBUG) Log.d(LOGCAT, "Database path:" + MoneyManagerApplication.getDatabasePath(context));

        Log.v(LOGCAT, "event onCreate( )");
    }

    /**
     * Returns the singleton instance of the helper for database access.
     * @param context Use Application context for database access (?)
     * @return
     */
    public static synchronized MoneyManagerOpenHelper getInstance(Context context) {
        if (mInstance == null) {
            Log.v(LOGCAT, "MoneyManagerOpenHelper.getInstance()");
            mInstance = new MoneyManagerOpenHelper(context);
        }
        return mInstance;
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        Log.v(LOGCAT, "event onConfigure( )");
        db.rawQuery("PRAGMA journal_mode=OFF", null).close();
    }

    @Override
    public synchronized void close() {
        // close CurrencyUtils
        CurrencyUtils.destroy();
        super.close();
        mInstance = null;
    }

    /**
     * Execute a single SQL statement that is NOT a SELECT or any other SQL statement that returns data.
     * It has no means to return any data (such as the number of affected rows). Instead, you're
     * encouraged to use insert(String, String, ContentValues), update(String, ContentValues, String, String[]), et al, when possible.
     * When using enableWriteAheadLogging(), journal_mode is automatically managed by this class.
     * So, do not set journal_mode using "PRAGMA journal_mode'" statement if your app is using enableWriteAheadLogging()
     *
     * @param db  the database
     * @param sql the SQL statement to be executed. Multiple statements separated by semicolons are not supported.
     * @since versionCode = 12 Version = 0.5.2
     */
    public void execSQL(SQLiteDatabase db, String sql) throws SQLException {
        db.execSQL(sql);
    }

    /**
     * Execute a single SQL statement that is NOT a SELECT or any other SQL statement that returns data.
     * It has no means to return any data (such as the number of affected rows). Instead, you're
     * encouraged to use insert(String, String, ContentValues), update(String, ContentValues, String, String[]), et al, when possible.
     * When using enableWriteAheadLogging(), journal_mode is automatically managed by this class.
     * So, do not set journal_mode using "PRAGMA journal_mode'" statement if your app is using enableWriteAheadLogging()
     *
     * @param db       the database
     * @param sql      the SQL statement to be executed. Multiple statements separated by semicolons are not supported.
     * @param bindArgs only byte[], String, Long and Double are supported in bindArgs.
     * @since versionCode = 12 Version = 0.5.2
     */
    public void execSQL(SQLiteDatabase db, String sql, Object[] bindArgs) throws SQLException {
        db.execSQL(sql, bindArgs);
    }

    /**
     * Execute a single SQL statement that is NOT a SELECT or any other SQL statement that returns data.
     * It has no means to return any data (such as the number of affected rows). Instead, you're
     * encouraged to use insert(String, String, ContentValues), update(String, ContentValues, String, String[]), et al, when possible.
     * When using enableWriteAheadLogging(), journal_mode is automatically managed by this class.
     * So, do not set journal_mode using "PRAGMA journal_mode'" statement if your app is using enableWriteAheadLogging()
     *
     * @param sql the SQL statement to be executed. Multiple statements separated by semicolons are not supported.
     * @since versionCode = 12 Version = 0.5.2
     */
    public void execSQL(String sql) throws SQLException {
        execSQL(this.getWritableDatabase(), sql);
    }

    /**
     * Execute a single SQL statement that is NOT a SELECT or any other SQL statement that returns data.
     * It has no means to return any data (such as the number of affected rows). Instead, you're encouraged to use
     * insert(String, String, ContentValues), update(String, ContentValues, String, String[]), et al, when possible.
     * When using enableWriteAheadLogging(), journal_mode is automatically managed by this class. So, do not set
     * journal_mode using "PRAGMA journal_mode'" statement if your app is using enableWriteAheadLogging()
     *
     * @param sql      the SQL statement to be executed. Multiple statements separated by semicolons are not supported.
     * @param bindArgs only byte[], String, Long and Double are supported in bindArgs.
     * @since versionCode = 12 Version = 0.5.2
     */
    public void execSQL(String sql, Object[] bindArgs) throws SQLException {
        execSQL(this.getWritableDatabase(), sql, bindArgs);
    }

    @Override
    public SQLiteDatabase getReadableDatabase() {
        SQLiteDatabase db = null;
        try {
            db = super.getReadableDatabase();
        } catch (SQLiteDiskIOException dex) {
            ExceptionHandler handler = new ExceptionHandler(mContext, this);
            handler.handle(dex, "Error opening database");
        }
        return db;
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        try {
            return getWritableDatabase_Internal();
        } catch (Exception ex) {
            String error = "Error getting writable database";
            Log.e(LOGCAT, error + ": " + ex.getLocalizedMessage());
            Toast.makeText(mContext, error, Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    private SQLiteDatabase getWritableDatabase_Internal() {
        SQLiteDatabase db = super.getWritableDatabase();

        if (db != null) {
            db.rawQuery("PRAGMA journal_mode=OFF", null).close();
        }

        return db;

    }

    /**
     * @param db    SQLite database to execute raw SQL
     * @param rawId id raw resource
     */
    private void executeRawSql(SQLiteDatabase db, int rawId) {
        String sqlCreate = RawFileUtils.getRawAsString(mContext, rawId);
        String sqlStatement[] = sqlCreate.split(";");
        // process all statements
        for (String aSqlStatment : sqlStatement) {
            if (BuildConfig.DEBUG) Log.d(LOGCAT, aSqlStatment);

            try {
                db.execSQL(aSqlStatment);
            } catch (SQLException E) {
                Log.e(LOGCAT, "Error in executeRawSql: " + E.getMessage());
            }
        }
    }

    /**
     * Get SQLite Version installed
     *
     * @return version of SQLite
     */
    public String getSQLiteVersion() {
        String sqliteVersion = null;
        Cursor cursor = null;
        SQLiteDatabase database = null;
        try {
            database = getReadableDatabase();
            if (database != null) {
                cursor = database.rawQuery("select sqlite_version() AS sqlite_version", null);
                if (cursor != null && cursor.moveToFirst()) {
                    sqliteVersion = cursor.getString(0);
                }
            }
        } catch (Exception e) {
            Log.e(LOGCAT, e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
//            if (database != null) database.close();
        }
        return sqliteVersion;
    }

    /**
     * @param id account id to be search
     * @return TableAccountList, return null if account id not find
     */
    public TableAccountList getTableAccountList(int id) {
        TableAccountList account = null;
        String selection = TableAccountList.ACCOUNTID + "=?";
        SQLiteDatabase database = getReadableDatabase();
        if (database == null) return null;

        Cursor cursor = database.query(new TableAccountList().getSource(), null, selection,
                new String[]{Integer.toString(id)}, null, null, null);
        if (cursor == null) return null;

        // check if cursor is valid
        if (cursor.moveToFirst()) {
            account = new TableAccountList();
            account.setValueFromCursor(cursor);

            cursor.close();
        }

        return account;
    }

    /**
     * Called when the database is being created.
     * @param db Database instance.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        if (BuildConfig.DEBUG) Log.d(LOGCAT, "execute onCreate method");
        executeRawSql(db, R.raw.database_create);
        // force update database
        updateDatabase(db, 0, databaseCurrentVersion);
        // init database
        try {
            initDatabase(db);
        } catch (Exception e) {
            Log.e(LOGCAT, e.getMessage());
        }
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (BuildConfig.DEBUG) Log.d(LOGCAT, "execute onUpgrade(" + Integer.toString(oldVersion) + ", " + Integer.toString(newVersion) + " method");
        // update databases
        updateDatabase(db, oldVersion, newVersion);
    }

    private void updateDatabase(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (int i = oldVersion + 1; i <= newVersion; i++) {
            // take a id of instance
            int idResource = mContext.getResources().getIdentifier("database_version_" + Integer.toString(newVersion), "raw", mContext.getPackageName());
            if (idResource > 0) {
                executeRawSql(db, idResource);
            }
        }
    }

    private boolean initDatabase(SQLiteDatabase database) {
        TableInfoTable infoTable = new TableInfoTable();
        Cursor infoCurrency = null, infoDate = null;
        // check if database is initial
        // currencies
        try {
            infoCurrency = database.rawQuery("SELECT * FROM " + infoTable.getSource() + " WHERE " + TableInfoTable.INFONAME + "=?",
                    new String[]{Constants.INFOTABLE_BASECURRENCYID});

            if (!(infoCurrency != null && infoCurrency.moveToFirst())) {
                // get current currencies
                Currency currency = Currency.getInstance(Locale.getDefault());

                if (currency != null) {
                    Cursor cursor = database.rawQuery(
                            "SELECT CURRENCYID FROM CURRENCYFORMATS_V1 WHERE CURRENCY_SYMBOL=?",
                            new String[]{currency.getCurrencyCode()});
                    if (cursor != null && cursor.moveToFirst()) {
                        ContentValues values = new ContentValues();

                        values.put(TableInfoTable.INFONAME, Constants.INFOTABLE_BASECURRENCYID);
                        values.put(TableInfoTable.INFOVALUE, cursor.getInt(0));

                        database.insert(infoTable.getSource(), null, values);
                        cursor.close();
                    }
                }
            }
        } catch (Exception e) {
            Log.e(LOGCAT, e.getMessage());
        } finally {
            if (infoCurrency != null)
                infoCurrency.close();
        }

        // date format
        try {
            infoDate = database.rawQuery("SELECT * FROM " + infoTable.getSource() + " WHERE " + TableInfoTable.INFONAME + "=?",
                    new String[]{Constants.INFOTABLE_DATEFORMAT});
            if (!(infoDate != null && infoDate.moveToFirst())) {
                Locale loc = Locale.getDefault();
                SimpleDateFormat sdf = (SimpleDateFormat) SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT, loc);
                String pattern = sdf.toLocalizedPattern();
                // replace date
                if (pattern.contains("dd")) {
                    pattern = pattern.replace("dd", "%d");
                } else {
                    pattern = pattern.replace("d", "%d");
                }
                // replace month
                if (pattern.contains("MM")) {
                    pattern = pattern.replace("MM", "%m");
                } else {
                    pattern = pattern.replace("M", "%m");
                }
                // replace year
                pattern = pattern.replace("yyyy", "%Y");
                pattern = pattern.replace("yy", "%y");
                // check if exists in format definition
                boolean find = false;
                for (int i = 0; i < mContext.getResources().getStringArray(R.array.date_format_mask).length; i++) {
                    if (pattern.equals(mContext.getResources().getStringArray(R.array.date_format_mask)[i])) {
                        find = true;
                        break;
                    }
                }
                // check if pattern exists
                if (find) {
                    ContentValues values = new ContentValues();

                    values.put(TableInfoTable.INFONAME, Constants.INFOTABLE_DATEFORMAT);
                    values.put(TableInfoTable.INFOVALUE, pattern);

                    database.insert(infoTable.getSource(), null, values);
                }
            }
        } catch (Exception e) {
            Log.e(LOGCAT, e.getMessage());
        } finally {
            if (infoDate != null)
                infoDate.close();
        }

        try {
            Cursor countCategories = database.rawQuery("SELECT * FROM CATEGORY_V1", null);
            if (countCategories != null && countCategories.getCount() <= 0) {
                int keyCategory = 0;
                String[] categories = new String[]{"1;1", "2;1", "3;1", "4;1", "5;1", "6;1", "7;1",
                        "8;2", "9;2", "10;3", "11;3", "12;3", "13;4", "14;4", "15;4", "16;4", "17;5",
                        "18;5", "19;5", "20;6", "21;6", "22;6", "23;7", "24;7", "25;7", "26;7", "27;7",
                        "28;8", "29;8", "30;8", "31;8", "32;9", "33;9", "34;9", "35;10", "36;10",
                        "37;10", "38;10", "39;13", "40;13", "41;13"};
                final String tableCategory = new TableCategory().getSource();
                final String tableSubcategory = new TableSubCategory().getSource();
                for (String item : categories) {
                    int subCategoryId = Integer.parseInt(item.substring(0, item.indexOf(";")));
                    int categoryId = Integer.parseInt(item.substring(item.indexOf(";") + 1));
                    if (categoryId != keyCategory) {
                        keyCategory = categoryId;
                        int idStringCategory = mContext.getResources().getIdentifier("category_" + Integer.toString(categoryId), "string", mContext.getPackageName());
                        if (idStringCategory > 0) {
                            ContentValues contentValues = new ContentValues();
                            contentValues.put(TableCategory.CATEGID, categoryId);
                            contentValues.put(TableCategory.CATEGNAME, mContext.getString(idStringCategory));
                            database.insert(tableCategory, null, contentValues);
                        }
                    }
                    int idStringSubcategory = mContext.getResources().getIdentifier("subcategory_" + Integer.toString(subCategoryId), "string", mContext.getPackageName());
                    if (idStringSubcategory > 0) {
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(TableSubCategory.SUBCATEGID, subCategoryId);
                        contentValues.put(TableSubCategory.CATEGID, categoryId);
                        contentValues.put(TableSubCategory.SUBCATEGNAME, mContext.getString(idStringSubcategory));
                        database.insert(tableSubcategory, null, contentValues);
                    }
                }

                countCategories.close();
            }
        } catch (Exception e) {
            Log.e(LOGCAT, e.getMessage());
        }

        return true;
    }

    /**
     * Try to close everything when destroyed.
     * @throws Throwable
     * reference:
     * http://stackoverflow.com/questions/4557154/android-sqlite-db-when-to-close
     * http://stackoverflow.com/questions/14469782/android-sqlite-right-way-to-open-close-db
     */
    @Override
    public void finalize() throws Throwable {
        if (mInstance != null) {
            mInstance.close();
        }
//        SQLiteDatabase db = mInstance.getReadableDatabase();
//        if (db != null) {
//            db.close();
//        }

        super.finalize();
    }
}
