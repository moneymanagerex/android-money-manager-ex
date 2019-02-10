/*
 * Copyright (C) 2012-2018 The Android Money Manager Ex Project Team
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
//import net.sqlcipher.database.SQLiteDatabase;
//import net.sqlcipher.database.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.common.io.Files;
import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.core.InfoKeys;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.datalayer.CategoryRepository;
import com.money.manager.ex.datalayer.InfoRepositorySql;
import com.money.manager.ex.datalayer.SubcategoryRepository;
import com.money.manager.ex.domainmodel.Category;
import com.money.manager.ex.domainmodel.Info;
import com.money.manager.ex.domainmodel.Subcategory;
import com.money.manager.ex.servicelayer.InfoService;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.sync.SyncManager;
import com.money.manager.ex.utils.MmxFileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Currency;

import timber.log.Timber;

/**
 * Actual helper class for accessing an SQLite database.
 */
public class MmxOpenHelper
    extends SQLiteOpenHelper {

    /**
     * Database schema version.
     */
    private static final int databaseVersion = 7;

    // Dynamic

    /**
     * Constructor. This is where the database path gets set.
     * @param context Current context.
     */
    public MmxOpenHelper(Context context, String dbPath) {
        super(context, dbPath, null, databaseVersion);
        this.mContext = context;

    }

    private Context mContext;
    private String mPassword = "";

    public Context getContext() {
        return this.mContext;
    }

//    @Override
//    public void onConfigure(SQLiteDatabase db) {
//        super.onConfigure(db);
//        db.rawQuery("PRAGMA journal_mode=OFF", null).close();
//    }

    /**
     * Called when the database is being created.
     * @param db Database instance.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        Timber.d("OpenHelper onCreate");

        try {
            executeRawSql(db, R.raw.tables_v1);
            db.disableWriteAheadLogging();
            initDatabase(db);
        } catch (Exception e) {
            Timber.e(e, "initializing database");
        }
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        db.disableWriteAheadLogging();
        super.onOpen(db);

//        int version = db.getVersion();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Timber.d("Upgrading from %1$d  to %2$d", oldVersion, newVersion);

        try {
            String currentDbFile = db.getPath();
            createDatabaseBackupOnUpgrade(currentDbFile, oldVersion);
        } catch (Exception ex) {
            Timber.e(ex, "creating database backup, can't continue");

            // don't upgrade
            return;
        }

        // update databases
        updateDatabase(db, oldVersion, newVersion);

        // notify sync about the db update.
        new SyncManager(getContext()).dataChanged();
    }

//    @Override
//    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        // nothing to do for now.
//        Timber.d("Downgrade attempt from %1$d to %2$d", oldVersion, newVersion);
//    }

//    @Override
//    public synchronized void close() {
//        super.close();
//
//        mInstance = null;
//    }

    @Override
    public SQLiteDatabase getReadableDatabase() {
        SQLiteDatabase db = null;
        try {
            db = super.getReadableDatabase();
        } catch (Exception ex) {
            Timber.e(ex, "opening readable database");
        }
        return db;
    }

//    public SQLiteDatabase getReadableDatabase() {
//        return this.getReadableDatabase(this.mPassword);
//    }
//    @Override
//    public SQLiteDatabase getReadableDatabase(String password) {
//        SQLiteDatabase db = null;
//        try {
//            db = super.getReadableDatabase(password);
//        } catch (Exception ex) {
//            Timber.e(ex, "opening readable database");
//        }
//        return db;
//    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        try {
            //return getWritableDatabase_Internal();
            return super.getWritableDatabase();
        } catch (Exception ex) {
            Timber.e(ex, "opening writable database");
        }
        return null;
    }

//    public SQLiteDatabase getWritableDatabase() {
//        return getWritableDatabase(this.mPassword);
//    }
//    @Override
//    public SQLiteDatabase getWritableDatabase(String password) {
//        try {
//            return getWritableDatabase_Internal(password);
//        } catch (Exception ex) {
//            Timber.e(ex, "opening writable database");
//        }
//        return null;
//    }

    public void setPassword(String password) {
        this.mPassword = password;
    }

//    public boolean hasPassword() {
//        return !TextUtils.isEmpty(this.mPassword);
//    }

//    private SQLiteDatabase getWritableDatabase_Internal() {
//        // String password
////
////        SQLiteDatabase db = super.getWritableDatabase(password);
//        SQLiteDatabase db = super.getWritableDatabase();
//
//        if (db != null) {
//            db.rawQuery("PRAGMA journal_mode=OFF", null).close();
//        }
//
//        return db;
//    }

    /**
     * @param db    SQLite database to execute raw SQL
     * @param rawId id raw resource
     */
    private void executeRawSql(SQLiteDatabase db, int rawId) {
        String sqlRaw = MmxFileUtils.getRawAsString(getContext(), rawId);
        String sqlStatement[] = sqlRaw.split(";");

        // process all statements
        for (String aSqlStatment : sqlStatement) {
            Timber.d(aSqlStatment);

            try {
                db.execSQL(aSqlStatment);
            } catch (Exception e) {
                String errorMessage = e.getMessage();
                if (e instanceof SQLiteException && errorMessage != null && errorMessage.contains("not an error (code 0)")) {
                    Timber.w(errorMessage);
                } else {
                    Timber.e(e, "executing raw sql: %s", aSqlStatment);
                }
            }
        }
    }

    /**
     * Get SQLite Version installed
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
            Timber.e(e, "getting sqlite version");
        } finally {
            if (cursor != null) cursor.close();
//            if (database != null) database.close();
        }
        return sqliteVersion;
    }

    private void updateDatabase(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Execute every script between the old and the new version of the database schema.
        for (int i = oldVersion + 1; i <= newVersion; i++) {
            int resourceId = mContext.getResources()
                    .getIdentifier("database_version_" + Integer.toString(i),
                            "raw", mContext.getPackageName());
            if (resourceId > 0) {
                executeRawSql(db, resourceId);
            }
        }
    }

    private boolean initDatabase(SQLiteDatabase database) {
        try {
            initBaseCurrency(database);
        } catch (Exception e) {
            Timber.e(e, "init database, base currency");
        }

        initDateFormat(database);
        initCategories(database);

        return true;
    }

    private void initCategories(SQLiteDatabase database) {
        try {
            Cursor countCategories = database.rawQuery("SELECT * FROM CATEGORY_V1", null);
            if (countCategories == null || countCategories.getCount() > 0) return;

            int keyCategory = 0;
            String[] categories = new String[]{"1;1", "2;1", "3;1", "4;1", "5;1", "6;1", "7;1",
                    "8;2", "9;2", "10;3", "11;3", "12;3", "13;4", "14;4", "15;4", "16;4", "17;5",
                    "18;5", "19;5", "20;6", "21;6", "22;6", "23;7", "24;7", "25;7", "26;7", "27;7",
                    "28;8", "29;8", "30;8", "31;8", "32;9", "33;9", "34;9", "35;10", "36;10",
                    "37;10", "38;10", "39;13", "40;13", "41;13"};

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

                        // Update existing records, inserted via the db creation script.
                        int updated = database.update(CategoryRepository.tableName, contentValues,
                                Category.CATEGID + "=?", new String[] { Integer.toString(categoryId) });
                        if (updated <= 0) {
                            Timber.w("updating %s for category %s", contentValues.toString(), Integer.toString(categoryId));
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

                    int updated = database.update(SubcategoryRepository.tableName, contentValues,
                            Subcategory.SUBCATEGID + "=?", new String[]{ Integer.toString(subCategoryId)});
                    if (updated <= 0) {
                        Timber.w("update failed, %s for subcategory %s", contentValues.toString(),
                                Integer.toString(subCategoryId));
                    }
                }
            }

            countCategories.close();
        } catch (Exception e) {
            Timber.e(e, "init database, categories");
        }
    }

    /**
     * The creation of the record is done in tables_v1.sql initialization script.
     * Here we only update the record to the current system's date format.
     * @param database Database being initialized.
     */
    private void initDateFormat(SQLiteDatabase database) {
        try {
            Core core = new Core(getContext());
            String pattern = core.getDefaultSystemDateFormat();
            if (pattern == null) return;

            InfoService infoService = new InfoService(getContext());
            infoService.updateRaw(database, InfoKeys.DATEFORMAT, pattern);
        } catch (Exception e) {
            Timber.e(e, "init database, date format");
        }
    }

    private void initBaseCurrency(SQLiteDatabase db) {
        // currencies
        CurrencyService currencyService = new CurrencyService(getContext());
        Currency systemCurrency = currencyService.getSystemDefaultCurrency();
        if (systemCurrency == null) return;

        InfoService infoService = new InfoService(getContext());

        // todo: try query generator.
//        String sql = new Select()
//                .select()
//                .from(InfoRepositorySql.TABLE_NAME)
//                .where(Info.INFONAME + "=?", InfoKeys.BASECURRENCYID)
//                .toString();

        Cursor currencyCursor = db.rawQuery(
            "SELECT * FROM " + InfoRepositorySql.TABLE_NAME +
            " WHERE " + Info.INFONAME + "=?",
            new String[]{ InfoKeys.BASECURRENCYID});
        if (currencyCursor == null) return;

        // Get id of the base currency record.
        int recordId = Constants.NOT_SET;
        boolean recordExists = currencyCursor.moveToFirst();
        if (recordExists) {
            recordId = currencyCursor.getInt(currencyCursor.getColumnIndex(Info.INFOID));
        }
        currencyCursor.close();

        // Use the system default currency.
        int currencyId = currencyService.loadCurrencyIdFromSymbolRaw(db, systemCurrency.getCurrencyCode());
        if (currencyId == Constants.NOT_SET) {
            // Use Euro by default.
            currencyId = 2;
        }

        UIHelper uiHelper = new UIHelper(getContext());

        // Insert/update base currency record into info table.
        if (!recordExists) {
            long newId = infoService.insertRaw(db, InfoKeys.BASECURRENCYID, currencyId);
            if (newId <= 0) {
                Timber.e("error inserting base currency on init");
            }
        } else {
            // Update the (by default empty) record to the default currency.
            long updatedRecords = infoService.updateRaw(db, recordId, InfoKeys.BASECURRENCYID, currencyId);
            if (updatedRecords <= 0) {
                Timber.e("error updating base currency on init");
            }
        }

        // Can't use provider here as the database is not ready.
//            int currencyId = currencyService.loadCurrencyIdFromSymbol(systemCurrency.getCurrencyCode());
//            String baseCurrencyId = infoService.getInfoValue(InfoService.BASECURRENCYID);
//            if (!StringUtils.isEmpty(baseCurrencyId)) return;
//            infoService.setInfoValue(InfoService.BASECURRENCYID, Integer.toString(currencyId));
    }

    @Override
    public void finalize() throws Throwable {
        super.finalize();
    }

    public void createDatabaseBackupOnUpgrade(String currentDbFile, int oldVersion) throws IOException {
        File in = new File(currentDbFile);
        String backupFileNameWithExtension = in.getName();

        //String backupName = FilenameUtils.getBaseName(backupFileNameWithExtension);
        String backupName = Files.getNameWithoutExtension(backupFileNameWithExtension);
        //String backupExtension = FilenameUtils.getExtension(backupFileNameWithExtension);
        String backupExtension = Files.getFileExtension(backupFileNameWithExtension);

        // append last db version
        backupName += "_v" + Integer.toString(oldVersion);

        backupFileNameWithExtension = backupName + "." + backupExtension;

        File outFile = new File(currentDbFile);
        String folder = outFile.getParent();
        //String outPath = FilenameUtils.getFullPath(currentDbFile) + backupFileNameWithExtension;
        String outPath = folder + backupFileNameWithExtension;
        File out = new File(outPath);

        //FileUtils.copyFile(in, out);
        Files.copy(in, out);
    }
}
