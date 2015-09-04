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
package com.money.manager.ex;

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.multidex.MultiDex;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.client2.session.Session.AccessType;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.database.QueryAccountBills;
import com.money.manager.ex.database.TableInfoTable;
import com.money.manager.ex.settings.PreferenceConstants;
import com.money.manager.ex.view.RobotoView;
import com.shamanland.fonticon.FontIconTypefaceHolder;

import java.io.File;

/**
 * This class extends Application and implements all the methods common in the
 * former money manager application for Android
 *
 * @author Alessandro Lazzari (lazzari.ale@gmail.com)
 * @version 1.1.0
 */
public class MoneyManagerApplication
        extends Application {

    ///////////////////////////////////////////////////////////////////////////
    public static final String KEY = "8941ED03A52BF76CD48EF951CA623B0709564CA238DB7FE1BA3980E4F617CD52";
    ///////////////////////////////////////////////////////////////////////////
    //                      drop box app settings                            //
    ///////////////////////////////////////////////////////////////////////////
    public static final String DROPBOX_APP_KEY = "cakbv9zh9l083ep";
    public static final String DROPBOX_APP_SECRET = "5E01FC70A055AE4A6C9DB346343CE822";
    public static final AccessType DROPBOX_ACCESS_TYPE = AccessType.APP_FOLDER;
    ///////////////////////////////////////////////////////////////////////////
    //                         CONSTANTS VALUES                              //
    ///////////////////////////////////////////////////////////////////////////
//    public static final int TYPE_HOME_CLASSIC = R.layout.main_fragments_activity;
//    public static final int TYPE_HOME_ADVANCE = R.layout.main_pager_activity;
    private static final String LOGCAT = "MoneyManagerApplication";
    private static MoneyManagerApplication myInstance;
    private static SharedPreferences appPreferences;
    private static float mTextSize;
    // user name application
    private static String userName = "";

    public static MoneyManagerApplication getInstanceApp() {
        return myInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(getApplicationContext()));

        // save instance of application
        myInstance = this;

        if (BuildConfig.DEBUG) Log.d(LOGCAT, "Application created");

        // create application folder
        Core core = new Core(getApplicationContext());
        core.getExternalStorageDirectoryApplication();

        // set default text size.
        setTextSize(new TextView(getApplicationContext()).getTextSize());
        // preference
        if (appPreferences == null) {
            appPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            RobotoView.setUserFont(Integer.parseInt(
                    appPreferences.getString(getString(PreferenceConstants.PREF_APPLICATION_FONT), "-1")));
            RobotoView.setUserFontSize(getApplicationContext(),
                    appPreferences.getString(getString(PreferenceConstants.PREF_APPLICATION_FONT_SIZE), "default"));
        }

        // Initialize font icons support.
        FontIconTypefaceHolder.init(getAssets(), "fonts/mmex.ttf");
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        // on terminate is never called
        // ref: http://stackoverflow.com/questions/15162562/application-lifecycle
        if (BuildConfig.DEBUG) Log.d(LOGCAT, "Application terminated");
    }

    @Override
    public void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        // Trying to mitigate issues on some 4.2.2 devices
        // https://code.google.com/p/android/issues/detail?id=78377
        // ref: https://developer.android.com/tools/building/multidex.html
        MultiDex.install(this);
    }

    /**
     * Reads the current database path from the settings and checks for the existence of the
     * database file.
     * Creates a default database file if the one from settings is not found. Sets this file as
     * the default database.
     * @param context Executing context.
     * @return path database file
     */
    public static String getDatabasePath(Context context) {
        String databasePath = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(PreferenceConstants.PREF_DATABASE_PATH), null);
        if (databasePath != null) {
            // Use the db path stored in the preferences.
            File databaseFile = new File(databasePath);
            if (databaseFile.getAbsoluteFile().exists())  {
                return databaseFile.toString();
            }
        }

        // otherwise try other paths or create the default database.

        String defaultDirectory = getDatabaseDirectory(context);
        String defaultPath = defaultDirectory + "/data.mmb";

        setDatabasePath(context, defaultPath);

        // Show notification
        ExceptionHandler handler = new ExceptionHandler(context);
        handler.showMessage("Database " + databasePath + " not found. Using default:" + defaultPath);

        return defaultPath;
    }

    /**
     * Returns only the directory name for the databases. This is where the new databases are
     * created by default.
     * @return String containing the path to the default directory for storing databases.
     */
    public static String getDatabaseDirectory(Context context) {
        Core core = new Core(context);
        File defaultFolder = core.getExternalStorageDirectoryApplication();
        String databasePath;

        if (defaultFolder.getAbsoluteFile().exists()) {
            databasePath = defaultFolder.toString();
        } else {
            String internalFolder;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                internalFolder = context.getApplicationInfo().dataDir;
            } else {
                internalFolder = "/data/data/" + context.getApplicationContext().getPackageName();
            }
            // add databases
            internalFolder += "/databases"; // "/data.mmb";
            databasePath = internalFolder;
        }

        return databasePath;
    }

    /**
     * @param context Executing context for which to set the preferences.
     * @param dbPath  path of database file to save
     */
    public static void setDatabasePath(Context context, String dbPath) {
        // save a reference db path
        Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(context.getString(PreferenceConstants.PREF_DATABASE_PATH), dbPath);
        editor.commit();
//        editor.apply();
    }

    public static float getTextSize() {
        return MoneyManagerApplication.mTextSize;
    }

    public static void setTextSize(float textSize) {
        MoneyManagerApplication.mTextSize = textSize;
    }

    /**
     * close process application
     */
    public static void killApplication() {
        // close application
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    /**
     * Shown database path with toast message
     *
     * @param context Executing context.
     */
    public static void showCurrentDatabasePath(Context context) {
        String currentPath = getDatabasePath(context);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String lastPath = preferences.getString(context.getString(PreferenceConstants.PREF_LAST_DB_PATH_SHOWN), "");

        if (!lastPath.equals(currentPath)) {
            preferences.edit()
                    .putString(context.getString(PreferenceConstants.PREF_LAST_DB_PATH_SHOWN), currentPath)
//                    .apply();
                    .commit();
            try {
                Toast.makeText(context, Html.fromHtml(context.getString(R.string.path_database_using, "<b>" + currentPath + "</b>")), Toast.LENGTH_LONG)
                        .show();
            } catch (Exception e) {
                Log.e(LOGCAT, e.getMessage());
            }
        }
    }

    // custom

    public boolean setUserName(String userName) {
        return this.setUserName(userName, false);
    }

    /**
     * @param userName the userName to set
     * @param save     save into database
     */
    public boolean setUserName(String userName, boolean save) {
        if (save) {
            TableInfoTable infoTable = new TableInfoTable();
            // update data into database
            ContentValues values = new ContentValues();
            values.put(TableInfoTable.INFOVALUE, userName);

            if (getContentResolver().update(infoTable.getUri(), values, TableInfoTable.INFONAME + "='USERNAME'", null) != 1) {
                return false;
            }
        }
        // edit preferences
        Editor editPreferences = appPreferences.edit();
        editPreferences.putString(getString(PreferenceConstants.PREF_USER_NAME), userName);
        // commit
//        editPreferences.commit();
        editPreferences.apply();
        // set the value
        MoneyManagerApplication.userName = userName;
        return true;
    }

    public String loadUserNameFromDatabase(Context context) {
        TableInfoTable infoTable = new TableInfoTable();
        Cursor cursor = context.getContentResolver().query(infoTable.getUri(),
                null,
                TableInfoTable.INFONAME + "=?",
                new String[]{ "USERNAME" },
                null);
        if (cursor == null) return Constants.EMPTY_STRING;

        String ret = "";
        if (cursor.moveToFirst()) {
            ret = cursor.getString(cursor.getColumnIndex(TableInfoTable.INFOVALUE));
        }
        cursor.close();

        return ret;
    }

    /**
     * @param repeat frequency repeats
     * @return frequency
     */
    public String getRepeatAsString(int repeat) {
        if (repeat >= 200) {
            repeat = repeat - 200;
        } // set auto execute without user acknowledgement
        if (repeat >= 100) {
            repeat = repeat - 100;
        } // set auto execute on the next occurrence
        String[] arrays = getResources().getStringArray(R.array.frequencies_items);
        if (arrays != null && repeat >= 0 && repeat <= arrays.length) {
            return arrays[repeat];
        }
        return "";
    }

    /**
     * Compute account balance and returns balance
     *
     * @param context Executing context
     * @return total
     */
    public double getSummaryAccounts(Context context) {
        try {
            return getSummaryAccountsInternal(context);
        } catch (IllegalStateException ise) {
            ExceptionHandler handler = new ExceptionHandler(context, this);
            handler.handle(ise, "getting summary accounts");
        }
        return 0;
    }

    private double getSummaryAccountsInternal(Context context) {
        double curTotal = 0;

        Core core = new Core(context);
        // compose whereClause
        String where = "";
        // check if show only open accounts
        if (core.getAccountsOpenVisible()) {
            where = "LOWER(STATUS)='open'";
        }
        // check if show fav accounts
        if (core.getAccountFavoriteVisible()) {
            where = "LOWER(FAVORITEACCT)='true'";
        }
        QueryAccountBills accountBills = new QueryAccountBills(context);
        Cursor cursor = context.getContentResolver().query(accountBills.getUri(),
                null,
                where,
                null,
                null);
        if (cursor == null) return 0;

        // calculate summary
        while (cursor.moveToNext()) {
            curTotal = curTotal + cursor.getDouble(cursor.getColumnIndex(QueryAccountBills.TOTALBASECONVRATE));
        }
        cursor.close();

        return curTotal;
    }

    /**
     * @return the userName
     */
    public String getUserName() {
        return userName;
    }

    public boolean isUriAvailable(Context context, Intent intent) {
        return context.getPackageManager().resolveActivity(intent, 0) != null;
    }
}
