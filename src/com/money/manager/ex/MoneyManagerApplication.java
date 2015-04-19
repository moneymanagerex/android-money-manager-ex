/*
 * Copyright (C) 2012-2014 Alessandro Lazzari
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

import android.annotation.SuppressLint;
import android.app.Application;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.client2.session.Session.AccessType;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.database.MoneyManagerOpenHelper;
import com.money.manager.ex.database.QueryAccountBills;
import com.money.manager.ex.database.TableInfoTable;
import com.money.manager.ex.dropbox.DropboxHelper;
import com.money.manager.ex.settings.PreferencesConstant;
import com.money.manager.ex.view.RobotoView;
import com.money.manager.ex.widget.AccountBillsWidgetProvider;
import com.money.manager.ex.widget.SummaryWidgetProvider;

import java.io.File;

/**
 * This class extends Application and implements all the methods common in the
 * former money manager application for Android
 *
 * @author Alessandro Lazzari (lazzari.ale@gmail.com)
 * @version 1.1.0
 */
public class MoneyManagerApplication extends Application {
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
    public static final int TYPE_HOME_CLASSIC = R.layout.main_fragments_activity;
    public static final int TYPE_HOME_ADVANCE = R.layout.main_pager_activity;
    private static final String LOGCAT = "MoneyManagerApplication";
    public static String PATTERN_DB_DATE = "yyyy-MM-dd";
    private static MoneyManagerApplication myInstance;
    private static SharedPreferences appPreferences;
    private static float mTextSize;
    // user name application
    private static String userName = "";
    ///////////////////////////////////////////////////////////////////////////
    //                           PREFERENCES                                 //
    ///////////////////////////////////////////////////////////////////////////
    private Editor editPreferences;

    public static MoneyManagerApplication getInstanceApp() {
        return myInstance;
    }

    /**
     * @param context Executing context.
     * @return path database file
     */
    @SuppressLint("SdCardPath")
    public static String getDatabasePath(Context context) {
        String databasePath = PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(PreferencesConstant.PREF_DATABASE_PATH), null);
        if (databasePath != null) {
            File databaseFile = new File(databasePath);
            if (databaseFile.getAbsoluteFile().exists()) return databaseFile.toString();
        }
        Core core = new Core(context);
        File defaultFolder = core.getExternalStorageDirectoryApplication();
        if (defaultFolder.getAbsoluteFile().exists()) {
            databasePath = defaultFolder.toString() + "/data.mmb";
        } else {
            String internalFolder;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                internalFolder = context.getApplicationInfo().dataDir;
            } else {
                internalFolder = "/data/data/" + context.getApplicationContext().getPackageName();
            }
            // add databases
            internalFolder += "/databases/data.mmb";
            databasePath = internalFolder;
        }
        MoneyManagerApplication.setDatabasePath(context, databasePath);
        return databasePath;
    }

    /**
     * @param context Executing context for which to get the preferences.
     * @param dbpath  path of database file to save
     */
    public static void setDatabasePath(Context context, String dbpath) {
        // save a reference db path
        Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(context.getString(PreferencesConstant.PREF_DATABASE_PATH), dbpath);
//        editor.commit();
        editor.apply();
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
    public static void showDatabasePathWork(Context context) {
        String currentPath = getDatabasePath(context);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String lastPath = preferences.getString(context.getString(PreferencesConstant.PREF_LAST_DB_PATH_SHOWN), "");
        if (!lastPath.equals(currentPath)) {
            preferences.edit().putString(context.getString(PreferencesConstant.PREF_LAST_DB_PATH_SHOWN), currentPath).apply();
//                    .commit();
            try {
                Toast.makeText(context, Html.fromHtml(context.getString(R.string.path_database_using, "<b>" + currentPath + "</b>")), Toast.LENGTH_LONG)
                        .show();
            } catch (Exception e) {
                Log.e(LOGCAT, e.getMessage());
            }
        }
    }

    /**
     * @param context Executing context.
     * @return the username
     */
    public String getFromDatabaseUserName(Context context) {
        TableInfoTable infoTable = new TableInfoTable();
        MoneyManagerOpenHelper helper = MoneyManagerOpenHelper.getInstance(context);
        Cursor data = helper.getReadableDatabase().query(infoTable.getSource(), null, TableInfoTable.INFONAME + "=?", new String[]{"USERNAME"}, null, null, null);
        String ret = "";
        if (data != null && data.moveToFirst()) {
            ret = data.getString(data.getColumnIndex(TableInfoTable.INFOVALUE));
        }
        if (data != null) {
            data.close();
        }
        //helper.close();

        return ret;
    }

    /**
     * @param repeat frequency repeats
     * @return frequency
     */
    public String getRepeatAsString(int repeat) {
        if (repeat >= 200) {
            repeat = repeat - 200;
        } // set auto execute without user acknowlegement
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
     * Convert date object in string SQLite date format
     * @param date to convert
     * @return string formatted date SQLite
     */
    /*public String getSQLiteStringDate(Date date) {
        return getStringFromDate(date, PATTERN_DB_DATE);
	}*/

    /**
     * @return the show transaction
     */
    public String getShowTransaction() {
        return PreferenceManager.getDefaultSharedPreferences(this).getString(getString(PreferencesConstant.PREF_SHOW_TRANSACTION), getResources().getString(R.string.last7days));
    }

    /**
     * Compute account balance and returns balance
     *
     * @param context Executing context
     * @return total
     */
    public double getSummaryAccounts(Context context) {
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
        Cursor data = context.getContentResolver().query(accountBills.getUri(), null, where, null, null);
        double curTotal = 0;

        if (data != null && data.moveToFirst()) {
            // calculate summary
            while (!data.isAfterLast()) {
                curTotal = curTotal + data.getDouble(data.getColumnIndex(QueryAccountBills.TOTALBASECONVRATE));
                data.moveToNext();
            }
        }
        if (data != null) {
            data.close();
        }

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

    @Override
    public void onCreate() {
        super.onCreate();
        // save instance of application
        myInstance = this;

        if (BuildConfig.DEBUG) Log.d(LOGCAT, "Application created");
        // create application folder
        Core core = new Core(getApplicationContext());
        core.getExternalStorageDirectoryApplication();

        // create instance drobpox
//        DropboxHelper dropboxHelper = DropboxHelper.getInstance(getApplicationContext());

        // set default value
        setTextSize(new TextView(getApplicationContext()).getTextSize());
        // preference
        if (appPreferences == null) {
            appPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            RobotoView.setUserFont(Integer.parseInt(appPreferences.getString(getString(PreferencesConstant.PREF_APPLICATION_FONT), "-1")));
            RobotoView.setUserFontSize(getApplicationContext(), appPreferences.getString(getString(PreferencesConstant.PREF_APPLICATION_FONT_SIZE), "default"));
        }
    }

    @Override
    public void onTerminate() {
        if (BuildConfig.DEBUG) Log.d(LOGCAT, "Application terminated");
    }

//    /**
//     * @param theme to save into preferences
//     */
//    public void setApplicationTheme(String theme) {
//        Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
//        editor.putString(getString(PreferencesConstant.PREF_THEME), theme);
////        editor.commit();
//        editor.apply();
//    }

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
        editPreferences = appPreferences.edit();
        editPreferences.putString(getString(PreferencesConstant.PREF_USER_NAME), userName);
        // commit
//        editPreferences.commit();
        editPreferences.apply();
        // set the value
        MoneyManagerApplication.userName = userName;
        return true;
    }

//    /**
//     * update all widget of application
//     */
//    public void updateAllWidget() {
//        Class<?>[] classes = {AccountBillsWidgetProvider.class, SummaryWidgetProvider.class};
//        for (Class<?> cls : classes) {
//            try {
//                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
//                ComponentName componentName = new ComponentName(getApplicationContext(), cls);
//                int[] ids = appWidgetManager.getAppWidgetIds(componentName);
//                Intent update_widget = new Intent();
//                update_widget.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
//                update_widget.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
//                getApplicationContext().sendBroadcast(update_widget);
//            } catch (Exception e) {
//                Log.e(LOGCAT, "update All Widget:" + e.getMessage());
//            }
//        }
//    }
}
