/*
 * Copyright (C) 2012-2024 The Android Money Manager Ex Project Team
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

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.text.TextUtils;

import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;

import com.google.common.io.Files;
import com.money.manager.ex.Constants;
import com.money.manager.ex.sqlite3mc.SupportFactory;
import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.InfoKeys;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.datalayer.InfoRepositorySql;
import com.money.manager.ex.domainmodel.Info;
import com.money.manager.ex.servicelayer.InfoService;
import com.money.manager.ex.utils.MmxFileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Currency;

import timber.log.Timber;

/**
 * Actual helper class for accessing an SQLite database.
 */
public class MmxOpenHelper extends SupportSQLiteOpenHelper.Callback {

    /**
     * Database schema version.
     */
    private static final int DATABASE_VERSION = 19;
    private String dbPath;

    // Dynamic

    /**
     * Constructor. This is where the database path gets set.
     * @param context Current context.
     */
    public MmxOpenHelper(Context context, String dbPath) {
        super(DATABASE_VERSION);
        this.mContext = context;
        this.dbPath = dbPath;
        this.mPassword = MmexApplication.getApp().getPassword();

        // Load the sqlite3mc native library.
        System.loadLibrary("sqliteX");
    }

    private final Context mContext;
    private String mPassword = "";

    public Context getContext() {
        return this.mContext;
    }

    public String getDbPath() {
        return this.dbPath;
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
    public void onCreate(SupportSQLiteDatabase db) {
        Timber.d("OpenHelper onCreate");

        try {
            executeRawSql(db, R.raw.tables_v1);
            initDatabase(db);
        } catch (Exception e) {
            Timber.e(e, "initializing database");
        }
    }

    public void onOpen(SupportSQLiteDatabase db) {
        db.disableWriteAheadLogging();
   //     super.onOpen(db);

//        long version = db.getVersion();
    }

    public void onUpgrade(SupportSQLiteDatabase db, int oldVersion, int newVersion) {
        Timber.d("Upgrading from version %d to %d", oldVersion, newVersion);

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
    }

    private SupportSQLiteDatabase getDatabase(boolean writable) {
        SupportSQLiteOpenHelper.Factory factory = new SupportFactory(this.mPassword.getBytes());
        SupportSQLiteOpenHelper.Configuration configuration =
                SupportSQLiteOpenHelper.Configuration.builder(mContext)
                        .name(this.dbPath)
                        .callback(this)
                        .build();
        SupportSQLiteDatabase sqLiteDatabase = writable
                ? factory.create(configuration).getWritableDatabase()
                : factory.create(configuration).getReadableDatabase();
        return sqLiteDatabase;
    }

    public SupportSQLiteDatabase getReadableDatabase() {
        return getDatabase(false);
    }

    public SupportSQLiteDatabase getWritableDatabase() {
        return getDatabase(true);
    }

    public void setPassword(String password) {
        this.mPassword = password;
    }
    public String getPassword() { return this.mPassword;}

    public boolean hasPassword() {
        return !TextUtils.isEmpty(this.mPassword);
    }

    /**
     * @param db    SQLite database to execute raw SQL
     * @param rawId id raw resource
     */
    private void executeRawSql(SupportSQLiteDatabase db, int rawId) {
        String sqlRaw = MmxFileUtils.getRawAsString(getContext(), rawId);
        String[] sqlStatement = sqlRaw.split(";");

        // process all statements
        for (String aSqlStatement : sqlStatement) {
            if (aSqlStatement.trim().isEmpty())
                continue;
            Timber.d(aSqlStatement);

            try {
                db.execSQL(aSqlStatement);
            } catch (Exception e) {
                String errorMessage = e.getMessage();
                if (e instanceof SQLiteException && errorMessage != null && errorMessage.contains("not an error (code 0)")) {
                    Timber.w(errorMessage);
                } else {
                    Timber.e(e, "executing raw sql: %s", aSqlStatement);
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
                cursor = getReadableDatabase().query("select sqlite_version() AS sqlite_version");
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

    private void updateDatabase(SupportSQLiteDatabase db, int oldVersion, int newVersion) {
        // Execute every script between the old and the new version of the database schema.
        for (int i = oldVersion + 1; i <= newVersion; i++) {
            int resourceId = mContext.getResources()
                    .getIdentifier("database_version_" + i,
                            "raw", mContext.getPackageName());
            if (resourceId > 0) {
                executeRawSql(db, resourceId);
            }
        }
    }

    private boolean initDatabase(SupportSQLiteDatabase database) {
        try {
            initBaseCurrency(database);
        } catch (Exception e) {
            Timber.e(e, "init database, base currency");
        }

        initDateFormat(database);
        return true;
    }

    /**
     * The creation of the record is done in tables_v1.sql initialization script.
     * Here we only update the record to the current system's date format.
     * @param database Database being initialized.
     */
    private void initDateFormat(SupportSQLiteDatabase database) {
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

    private void initBaseCurrency(SupportSQLiteDatabase db) {
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

        Cursor currencyCursor = db.query(
            "SELECT * FROM " + InfoRepositorySql.TABLE_NAME +
            " WHERE " + Info.INFONAME + "=?",
            new String[]{ InfoKeys.BASECURRENCYID});
        if (currencyCursor == null) return;

        // Get id of the base currency record.
        long recordId = Constants.NOT_SET;
        boolean recordExists = currencyCursor.moveToFirst();
        if (recordExists) {
            recordId = currencyCursor.getInt(currencyCursor.getColumnIndex(Info.INFOID));
        }
        currencyCursor.close();

        // Use the system default currency.
        long currencyId = currencyService.loadCurrencyIdFromSymbolRaw(db, systemCurrency.getCurrencyCode());
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
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    public static void createDatabaseBackupOnUpgrade(String currentDbFile, long oldVersion) throws IOException {
        File in = new File(currentDbFile);
        String backupFileNameWithExtension = in.getName();

        //String backupName = FilenameUtils.getBaseName(backupFileNameWithExtension);
        String backupName = Files.getNameWithoutExtension(backupFileNameWithExtension);
        //String backupExtension = FilenameUtils.getExtension(backupFileNameWithExtension);
        String backupExtension = Files.getFileExtension(backupFileNameWithExtension);

        // append last db version
        backupName += "_v" + oldVersion;

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
