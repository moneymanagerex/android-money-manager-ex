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
package com.money.manager.ex.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
//import net.sqlcipher.database.SQLiteDatabase;
//import net.sqlcipher.database.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import com.money.manager.ex.BuildConfig;
import com.money.manager.ex.Constants;
import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.core.InfoKeys;
import com.money.manager.ex.domainmodel.Category;
import com.money.manager.ex.domainmodel.Info;
import com.money.manager.ex.domainmodel.Subcategory;
import com.money.manager.ex.servicelayer.InfoService;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.utils.MmexFileUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.Currency;

/**
 * Actual helper class for accessing an SQLite database.
 */
public class MmexOpenHelper
    extends SQLiteOpenHelper {

    private static final String LOGCAT = MmexOpenHelper.class.getSimpleName();
    /*
       The version corresponds to the user version in info table, used by the desktop app.
     */
    private static final int databaseVersion = 4;

    // singleton
    private static MmexOpenHelper mInstance;

    /**
     * Returns the singleton instance of the helper for database access.
     * @param context Use Application context for database access (?)
     * @return instance of the db helper
     */
    public static synchronized MmexOpenHelper getInstance(Context context) {
        if (mInstance == null) {
            Log.v(LOGCAT, "MmexOpenHelper.getInstance()");

            mInstance = new MmexOpenHelper(context.getApplicationContext());
        }
        return mInstance;
    }

    public static synchronized void closeDatabase() {
        if (mInstance == null) return;

        mInstance.close();
    }

    /**
     * Constructor. This is where the database path gets set.
     * @param context Current context.
     */
    private MmexOpenHelper(Context context) {
        super(context, MoneyManagerApplication.getDatabasePath(context), null, databaseVersion);
        this.mContext = context;

        if (BuildConfig.DEBUG) Log.d(LOGCAT, "Database path:" + MoneyManagerApplication.getDatabasePath(context));

        Log.v(LOGCAT, "event onCreate( )");
    }

    private Context mContext;
    private String mPassword = "";

    public Context getContext() {
        return this.mContext;
    }

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
//        updateDatabase(db, 0, databaseVersion);

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
        if (BuildConfig.DEBUG) {
            Log.d(LOGCAT, "Upgrading from " + Integer.toString(oldVersion) + " to " + Integer.toString(newVersion));
        }

        try {
            String currentDbFile = db.getPath();
            createDatabaseBackupOnUpgrade(currentDbFile, oldVersion);
        } catch (Exception ex) {
            ExceptionHandler handler = new ExceptionHandler(getContext());
            handler.handle(ex, "creating database backup, can't continue");

            // don't upgrade
            return;
        }

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

//    public SQLiteDatabase getReadableDatabase() {
//        return this.getReadableDatabase(this.mPassword);
//    }

    @Override
    public SQLiteDatabase getReadableDatabase() {
        // String password
        SQLiteDatabase db = null;
        try {
            db = super.getReadableDatabase();
        } catch (Exception ex) {
            ExceptionHandler handler = new ExceptionHandler(mContext, this);
            handler.handle(ex, "opening readable database");
        }
        return db;
    }

//    public SQLiteDatabase getWritableDatabase() {
//        return getWritableDatabase(this.mPassword);
//    }

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

    public void setPassword(String password) {
        this.mPassword = password;
    }

    public boolean hasPassword() {
        return !TextUtils.isEmpty(this.mPassword);
    }

    private SQLiteDatabase getWritableDatabase_Internal() {
        // String password

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
        String sqlRaw = MmexFileUtils.getRawAsString(mContext, rawId);
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
        try {
            if (getReadableDatabase() != null) {
                cursor = getReadableDatabase().rawQuery("select sqlite_version() AS sqlite_version", null);
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
            int idResource = mContext.getResources()
                    .getIdentifier("database_version_" + Integer.toString(newVersion),
                            "raw", mContext.getPackageName());
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
                            contentValues.put(Category.CATEGID, categoryId);
                            contentValues.put(Category.CATEGNAME, mContext.getString(idStringCategory));
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
                        contentValues.put(Subcategory.SUBCATEGID, subCategoryId);
                        contentValues.put(Subcategory.CATEGID, categoryId);
                        contentValues.put(Subcategory.SUBCATEGNAME, mContext.getString(idStringSubcategory));
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

    /**
     * The creation of the record is done in database_create.sql initialization script.
     * Here we only update the record to the current system's date format.
     * @param database Database being initialized.
     */
    private void initDateFormat(SQLiteDatabase database) {
//        Cursor cursor;

        // date format
        try {
            Core core = new Core(mContext);
            String pattern = core.getDefaultSystemDateFormat();
            if (pattern == null) return;

//            InfoRepository repo = new InfoRepository(mContext);

//            cursor = database.rawQuery("SELECT * FROM " + repo.getSource() +
//                            " WHERE " + Info.INFONAME + "=?",
//                    new String[]{InfoService.DATEFORMAT});
//
//            if (cursor == null) return;

//            String existingValue = null;
//            if (cursor.moveToNext()) {
//                // read existing value for comparison
//                existingValue = cursor.getString(cursor.getColumnIndex(InfoService.DATEFORMAT));
//            }
//            cursor.close();

            InfoService infoService = new InfoService(mContext);

//            if (!pattern.equalsIgnoreCase(existingValue)) {
//                // check if pattern exists
//                infoService.insertRaw(database, InfoService.DATEFORMAT, pattern);
//            } else {
                infoService.updateRaw(database, InfoKeys.DATEFORMAT, pattern);
//            }
        } catch (Exception e) {
            ExceptionHandler handler = new ExceptionHandler(mContext, this);
            handler.handle(e, "init database, date format");
        }
    }

    private void initBaseCurrency(SQLiteDatabase db) {
        Cursor currencyCursor;

        // currencies
        try {
            CurrencyService currencyService = new CurrencyService(mContext);
            Currency systemCurrency = currencyService.getSystemDefaultCurrency();
            if (systemCurrency == null) return;

            InfoService infoService = new InfoService(mContext);

            currencyCursor = db.rawQuery("SELECT * FROM " + infoService.repository.getSource() +
                            " WHERE " + Info.INFONAME + "=?",
                    new String[]{ InfoKeys.BASECURRENCYID});
            if (currencyCursor == null) return;

            boolean recordExists = currencyCursor.moveToFirst();
            int recordId = currencyCursor.getInt(currencyCursor.getColumnIndex(Info.INFOID));
            currencyCursor.close();

            // Use the system default currency.
            int currencyId = currencyService.loadCurrencyIdFromSymbolRaw(db, systemCurrency.getCurrencyCode());

            if (!recordExists && (currencyId != Constants.NOT_SET)) {
                long newId = infoService.insertRaw(db, InfoKeys.BASECURRENCYID, currencyId);
                if (newId <= 0) {
                    ExceptionHandler handler = new ExceptionHandler(mContext, this);
                    handler.showMessage("error inserting base currency on init");
                }
            } else {
                // Update the (empty) record to the default currency.
                long updatedRecords = infoService.updateRaw(db, recordId, InfoKeys.BASECURRENCYID, currencyId);
                if (updatedRecords <= 0) {
                    ExceptionHandler handler = new ExceptionHandler(mContext, this);
                    handler.showMessage("error updating base currency on init");
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
        closeDatabase();
//        if (mInstance != null) {
//            mInstance.close();
//        }

        super.finalize();
    }

    public void createDatabaseBackupOnUpgrade(String currentDbFile, int oldVersion) throws IOException {
        File in = new File(currentDbFile);
        String backupFileNameWithExtension = in.getName();

        String backupName = FilenameUtils.getBaseName(backupFileNameWithExtension);
        String backupExtension = FilenameUtils.getExtension(backupFileNameWithExtension);

        // append last db version
        backupName += "_v" + Integer.toString(oldVersion);

        backupFileNameWithExtension = backupName + "." + backupExtension;

        String outPath = FilenameUtils.getFullPath(currentDbFile) + backupFileNameWithExtension;
        File out = new File(outPath);

        FileUtils.copyFile(in, out);
    }
}
