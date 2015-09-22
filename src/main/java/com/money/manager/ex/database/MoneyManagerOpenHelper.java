/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
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
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.money.manager.ex.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.money.manager.ex.BuildConfig;
import com.money.manager.ex.Constants;
import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.businessobjects.InfoService;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.utils.RawFileUtils;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Currency;
import java.util.Locale;

/**
 * @author Alessandro Lazzari (lazzari.ale@gmail.com)
 * @version 1.0.1
 */
public class MoneyManagerOpenHelper
        extends SQLiteOpenHelper {

    /**
     * Returns the singleton instance of the helper for database access.
     * @param context Use Application context for database access (?)
     * @return
     */
    public static synchronized MoneyManagerOpenHelper getInstance(Context context) {
        if (mInstance == null) {
            Log.v(LOGCAT, "MoneyManagerOpenHelper.getInstance()");

            mInstance = new MoneyManagerOpenHelper(context.getApplicationContext());
        }
        return mInstance;
    }

    private static final String LOGCAT = MoneyManagerOpenHelper.class.getSimpleName();
    // The version corresponds to the user version in info table, used by the desktop app.
    private static final int databaseCurrentVersion = 3;

    // singleton
    private static MoneyManagerOpenHelper mInstance;

    private MoneyManagerOpenHelper(Context context) {
        super(context, MoneyManagerApplication.getDatabasePath(context), null, databaseCurrentVersion);
        this.mContext = context;

        if (BuildConfig.DEBUG) Log.d(LOGCAT, "Database path:" + MoneyManagerApplication.getDatabasePath(context));

        Log.v(LOGCAT, "event onCreate( )");
    }

    private Context mContext;

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        Log.v(LOGCAT, "event onConfigure( )");
        db.rawQuery("PRAGMA journal_mode=OFF", null).close();
    }

    /**
     * Called when the database is being created.
     * @param db Database instance.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        if (BuildConfig.DEBUG) Log.d(LOGCAT, "execute onCreate method");

        executeRawSql(db, R.raw.database_create);

//        if (BuildConfig.DEBUG) Log.d(LOGCAT, "db version after creation of tables: " + db.getVersion());
        // Execute update scripts?
//        updateDatabase(db, 0, databaseCurrentVersion);

        try {
            initDatabase(db);
        } catch (Exception e) {
            ExceptionHandler handler = new ExceptionHandler(mContext, this);
            handler.handle(e, "initializing database");
        }
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);

//        int version = db.getVersion();
//        if (BuildConfig.DEBUG) Log.d(LOGCAT, "opening db version: " + version);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (BuildConfig.DEBUG) Log.d(LOGCAT, "Upgrading from " + Integer.toString(oldVersion) +
                " to " + Integer.toString(newVersion));
        // update databases
        updateDatabase(db, oldVersion, newVersion);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // nothing to do for now.
        if (BuildConfig.DEBUG) Log.d(LOGCAT, "Downgrade attempt from " + Integer.toString(oldVersion) +
            " to " + Integer.toString(newVersion));
    }

    @Override
    public synchronized void close() {
        // close CurrencyService
        CurrencyService.destroy();
        super.close();
        mInstance = null;
    }

    @Override
    public SQLiteDatabase getReadableDatabase() {
        SQLiteDatabase db = null;
        try {
            db = super.getReadableDatabase();
        } catch (Exception ex) {
            ExceptionHandler handler = new ExceptionHandler(mContext, this);
            handler.handle(ex, "opening readable database");
        }
        return db;
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        try {
            return getWritableDatabase_Internal();
        } catch (Exception ex) {
            ExceptionHandler handler = new ExceptionHandler(mContext, this);
            handler.handle(ex, "opening writable database");
        }
        return null;
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
        String sqlRaw = RawFileUtils.getRawAsString(mContext, rawId);
        String sqlStatement[] = sqlRaw.split(";");
        // process all statements
        for (String aSqlStatment : sqlStatement) {
            if (BuildConfig.DEBUG) Log.d(LOGCAT, aSqlStatment);

            try {
                db.execSQL(aSqlStatment);
            } catch (SQLException e) {
                String errorMessage = e.getMessage();
                if (errorMessage != null && errorMessage.equals("not an error (code 0)")) {
                    Log.w(LOGCAT, errorMessage);
                } else {
                    ExceptionHandler handler = new ExceptionHandler(mContext, this);
                    handler.handle(e, "executing raw sql: " + aSqlStatment);
                }
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
        SQLiteDatabase database;
        try {
            database = getReadableDatabase();
            if (database != null) {
                cursor = database.rawQuery("select sqlite_version() AS sqlite_version", null);
                if (cursor != null && cursor.moveToFirst()) {
                    sqliteVersion = cursor.getString(0);
                }
            }
        } catch (Exception e) {
            ExceptionHandler handler = new ExceptionHandler(mContext, this);
            handler.handle(e, "getting sqlite version");
        } finally {
            if (cursor != null) cursor.close();
//            if (database != null) database.close();
        }
        return sqliteVersion;
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

        initBaseCurrency(database);
        initDateFormat(database);
        initCategories(database);

        return true;
    }

    private void initCategories(SQLiteDatabase database) {
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
                        int idStringCategory = mContext.getResources()
                                .getIdentifier("category_" + Integer.toString(categoryId), "string", mContext.getPackageName());
                        if (idStringCategory > 0) {
                            ContentValues contentValues = new ContentValues();
                            contentValues.put(TableCategory.CATEGID, categoryId);
                            contentValues.put(TableCategory.CATEGNAME, mContext.getString(idStringCategory));
                            long newCategoryId = database.insert(tableCategory, null, contentValues);

                            if (newCategoryId <= 0) {
                                Log.e(LOGCAT, "insert " + contentValues.toString() +
                                                "result id: " + Long.toString(newCategoryId));
                            }
                        }
                    }
                    int idStringSubcategory = mContext.getResources()
                            .getIdentifier("subcategory_" + Integer.toString(subCategoryId), "string", mContext.getPackageName());
                    if (idStringSubcategory > 0) {
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(TableSubCategory.SUBCATEGID, subCategoryId);
                        contentValues.put(TableSubCategory.CATEGID, categoryId);
                        contentValues.put(TableSubCategory.SUBCATEGNAME, mContext.getString(idStringSubcategory));
                        long newSubCategoryId = database.insert(tableSubcategory, null, contentValues);

                        if (newSubCategoryId <= 0) {
                            Log.e(LOGCAT, "try insert " + contentValues.toString() +
                                    "result id: " + Long.toString(newSubCategoryId));
                        }
                    }
                }

                countCategories.close();
            }
        } catch (Exception e) {
            ExceptionHandler handler = new ExceptionHandler(mContext, this);
            handler.handle(e, "init database, categories");
        }
    }

    private void initDateFormat(SQLiteDatabase database) {
        Cursor infoDate = null;

        // date format
        try {
            Core core = new Core(mContext);
            String pattern = core.getDefaultSystemDateFormat();
            if (pattern == null) return;

            TableInfoTable infoTable = new TableInfoTable();

            infoDate = database.rawQuery("SELECT * FROM " + infoTable.getSource() + " WHERE " + TableInfoTable.INFONAME + "=?",
                    new String[]{InfoService.INFOTABLE_DATEFORMAT});

            boolean recordExists = (infoDate != null && infoDate.moveToFirst());

            InfoService infoService = new InfoService(mContext);

            if (!recordExists) {
                // check if pattern exists
                infoService.insertRaw(database, InfoService.INFOTABLE_DATEFORMAT, pattern);
            } else {
                infoService.updateRaw(database, InfoService.INFOTABLE_DATEFORMAT, pattern);
            }
        } catch (Exception e) {
            ExceptionHandler handler = new ExceptionHandler(mContext, this);
            handler.handle(e, "init database, date format");
        } finally {
            if (infoDate != null)
                infoDate.close();
        }
    }

    private void initBaseCurrency(SQLiteDatabase db) {
        Cursor currencyCursor;

        // currencies
        try {
            CurrencyService currencyService = new CurrencyService(mContext);
            Currency systemCurrency = currencyService.getSystemDefaultCurrency();
            if (systemCurrency == null) return;

            TableInfoTable infoTable = new TableInfoTable();
            InfoService infoService = new InfoService(mContext);

            currencyCursor = db.rawQuery("SELECT * FROM " + infoTable.getSource() +
                            " WHERE " + TableInfoTable.INFONAME + "=?",
                    new String[]{ InfoService.BASECURRENCYID});
            if (currencyCursor == null) return;

            boolean recordExists = currencyCursor.moveToFirst();
            int recordId = currencyCursor.getInt(currencyCursor.getColumnIndex(TableInfoTable.INFOID));
            currencyCursor.close();

            // get system default currency
            int currencyId = currencyService.loadCurrencyIdFromSymbolRaw(db, systemCurrency.getCurrencyCode());

            if (!recordExists && (currencyId != Constants.NOT_SET)) {
                long newId = infoService.insertRaw(db, InfoService.BASECURRENCYID, currencyId);
                if (newId <= 0) {
                    ExceptionHandler handler = new ExceptionHandler(mContext, this);
                    handler.showMessage("updating base currency on init");
                }
            } else {
                // Update the (empty) record to the default currency.
                long updatedRecords = infoService.updateRaw(db, recordId, InfoService.BASECURRENCYID, currencyId);
                if (updatedRecords <= 0) {
                    ExceptionHandler handler = new ExceptionHandler(mContext, this);
                    handler.showMessage("updating base currency on init");
                }
            }

            // Can't use provider here as the database is not ready.
//            int currencyId = currencyService.loadCurrencyIdFromSymbol(systemCurrency.getCurrencyCode());
//            String baseCurrencyId = infoService.getInfoValue(InfoService.BASECURRENCYID);
//            if (!StringUtils.isEmpty(baseCurrencyId)) return;
//            infoService.setInfoValue(InfoService.BASECURRENCYID, Integer.toString(currencyId));
        } catch (Exception e) {
            ExceptionHandler handler = new ExceptionHandler(mContext, this);
            handler.handle(e, "init database, currency");
        }
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

        super.finalize();
    }
}
