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
package com.money.manager.ex;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.preference.PreferenceManager;
//import android.support.multidex.MultiDex;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.client2.session.Session.AccessType;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.database.QueryAccountBills;
import com.money.manager.ex.servicelayer.InfoService;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.settings.DatabaseSettings;
import com.money.manager.ex.settings.LookAndFeelSettings;
import com.money.manager.ex.settings.PreferenceConstants;
import com.money.manager.ex.view.RobotoView;
import com.shamanland.fonticon.FontIconTypefaceHolder;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Locale;

/**
 * This class extends Application and implements all the methods common in the
 * former money manager application for Android
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
//    public static final int TYPE_HOME_CLASSIC = R.layout.main_activity;
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

    // Static

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
     * Reads the current database path from the settings and checks for the existence of the
     * database file.
     * Creates a default database file if the one from settings is not found. Sets this file as
     * the default database.
     * @param context Executing context.
     * @return Full path to the current database file.
     */
    public static String getDatabasePath(Context context) {
        DatabaseSettings dbSettings = new AppSettings(context).getDatabaseSettings();

        String databasePath = dbSettings.getDatabasePath();
        if (BuildConfig.DEBUG) Log.d(LOGCAT, "database setting: " + databasePath);

        if (!StringUtils.isEmpty(databasePath)) {
            // Use the db path stored in the preferences.
            File databaseFile = new File(databasePath);
            if (databaseFile.getAbsoluteFile().exists())  {
                return databaseFile.toString();
            }
        }

        // otherwise try other paths or create the default database.

        String defaultDirectory = getDatabaseDirectory(context);
        String defaultPath = defaultDirectory + "/data.mmb";

        dbSettings.setDatabasePath(defaultPath);

        // Show notification
        ExceptionHandler handler = new ExceptionHandler(context);
        if (databasePath.equals(defaultPath)) {
            handler.showMessage("Default database file will be created at " + defaultPath);
        } else {
            handler.showMessage("Database " + databasePath + " not found. Using default:" + defaultPath);
        }

        return defaultPath;
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

    // Overrides.

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
//        MultiDex.install(this);
    }

    // dynamic

    public Locale getAppLocale() {
        Locale locale = null;
        Context context = getApplicationContext();

        String language = new AppSettings(context).getGeneralSettings().getApplicationLanguage();

        if(StringUtils.isNotEmpty(language)) {
            try {
                locale = new Locale(language);
            } catch (Exception e) {
                ExceptionHandler handler = new ExceptionHandler(context, this);
                handler.handle(e, "parsing locale: " + language);
            }
        }

        // in case the above failed
        if (locale == null) {
            // use the default locale.
            locale = context.getResources().getConfiguration().locale;
        }
        if (locale == null) {
            locale = Locale.getDefault();
        }

        return locale;
    }

    public boolean setUserName(String userName) {
        return this.setUserName(userName, false);
    }

    /**
     * @param userName the userName to set
     * @param save     save into database
     */
    public boolean setUserName(String userName, boolean save) {
        if (save) {
            InfoService service = new InfoService(this.getApplicationContext());
            boolean updateSuccessful = service.setInfoValue("USERNAME", userName);

            if (!updateSuccessful) {
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
        InfoService service = new InfoService(context);
        String username = service.getInfoValue("USERNAME");

        String result = StringUtils.isEmpty(username) ? "" : username;

        return result;
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

//        Core core = new Core(context);
        LookAndFeelSettings settings = new AppSettings(context)
            .getLookAndFeelSettings();
        // compose whereClause
        String where = "";
        // check if show only open accounts
//        if (core.getAccountsOpenVisible()) {
        if (settings.getViewOpenAccounts()) {
            where = "LOWER(STATUS)='open'";
        }
        // check if show fav accounts
        if (settings.getViewFavouriteAccounts()) {
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

    private Tracker mTracker;

    /**
     * Gets the default {@link Tracker} for this {@link Application}.
     * @return tracker
     */
    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker(R.xml.global_tracker);

            Core core = new Core(getApplicationContext());
            mTracker.setAppVersion(core.getAppVersionName());
        }

        return mTracker;
    }

}
