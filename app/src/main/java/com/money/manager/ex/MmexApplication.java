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
package com.money.manager.ex;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.TextView;
import com.evernote.android.job.JobManager;
import com.mikepenz.iconics.Iconics;
import com.mikepenz.mmex_icon_font_typeface_library.MMXIconFont;
import com.money.manager.ex.common.MoneyParcelConverter;
import com.money.manager.ex.core.InfoKeys;
import com.money.manager.ex.core.database.DatabaseManager;
import com.money.manager.ex.core.ioc.DaggerMmxComponent;
import com.money.manager.ex.core.ioc.MmxComponent;
import com.money.manager.ex.core.ioc.MmxModule;
import com.money.manager.ex.database.MmxOpenHelper;
import com.money.manager.ex.database.QueryAccountBills;
import com.money.manager.ex.log.DebugTree;
import com.money.manager.ex.log.ScreenTree;
import com.money.manager.ex.log.SysLogTree;
import com.money.manager.ex.servicelayer.InfoService;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.settings.LookAndFeelSettings;
import com.money.manager.ex.settings.PreferenceConstants;
import com.money.manager.ex.sync.jobmanager.SyncJobCreator;
import com.money.manager.ex.view.RobotoView;
import com.shamanland.fonticon.FontIconTypefaceHolder;

import org.parceler.Parcel;
import org.parceler.ParcelClass;
import org.parceler.ParcelClasses;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

import androidx.multidex.MultiDexApplication;
import info.javaperformance.money.Money;
import timber.log.Timber;

/**
 * Here we define the parcel converter for Money type.
 */
@ParcelClasses(
    @ParcelClass(
        value = Money.class,
        annotation = @Parcel(converter = MoneyParcelConverter.class))
)
public class MmexApplication
    extends MultiDexApplication {

    private static MmexApplication appInstance;
    private static float mTextSize;
    private static String userName = "";

    public static MmexApplication getApp() {
        return appInstance;
    }

    public static float getTextSize() {
        return MmexApplication.mTextSize;
    }

    public static void setTextSize(float textSize) {
        MmexApplication.mTextSize = textSize;
    }

    /**
     * close process application
     */
    public static void killApplication() {
        // close application
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    // Instance fields.

    public MmxComponent iocComponent;
    public AtomicReference<MmxOpenHelper> openHelperAtomicReference;

    // Overrides.

    @Override
    public void onCreate() {
        super.onCreate();

        // update instance of application
        appInstance = this;

        // set default text size.
        setTextSize(new TextView(getApplicationContext()).getTextSize());

        // Font
        SharedPreferences appPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        RobotoView.setUserFont(Integer.parseInt(
            appPreferences.getString(getString(PreferenceConstants.PREF_APPLICATION_FONT), "-1")));
        RobotoView.setUserFontSize(getApplicationContext(),
            appPreferences.getString(getString(PreferenceConstants.PREF_APPLICATION_FONT_SIZE), "default"));

        registerCustomFonts();

        // Loggers
        if (BuildConfig.DEBUG) {
            Timber.plant(new DebugTree());
        } else {
            //Timber.plant(new CrashReportingTree());
            Timber.plant(new ScreenTree());
            Timber.plant(new SysLogTree());
        }

        initializeDependencyInjection();

        // Job Manager initialization.
        initializeJobManager();
    }

    /**
     * Initializes job manager.
     * Implemented as a separate method so that it can be overridden in unit tests.
     */
    public void initializeJobManager() {
        JobManager.create(this)
            .addJobCreator(new SyncJobCreator());
    }

    /**
     * Initialize Dagger 2 module(s).
     */
    private void initializeDependencyInjection() {
        // Dependency Injection. IoC
        iocComponent = DaggerMmxComponent.builder()
                .mmxModule(new MmxModule(appInstance))
                .build();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        // on terminate is never called
        // ref: http://stackoverflow.com/questions/15162562/application-lifecycle
        Timber.d("Application terminated");
    }

    @Override
    public void attachBaseContext(Context base) {
        super.attachBaseContext(base);
//        super.attachBaseContext(IconicsContextWrapper.wrap(base));

        // Trying to mitigate issues on some 4.2.2 devices
        // https://code.google.com/p/android/issues/detail?id=78377
        // ref: https://developer.android.com/tools/building/multidex.html
//        MultiDex.install(this);
    }

    // dynamic

    public void initDb(String path) {
        MmxOpenHelper db = createDbInstance(path);

        if (openHelperAtomicReference == null) {
            openHelperAtomicReference = new AtomicReference<>(db);
        } else {
            // close existing db
            openHelperAtomicReference.get().close();
            openHelperAtomicReference.set(db);
        }
    }

    private MmxOpenHelper createDbInstance(String path) {
        if (TextUtils.isEmpty(path)) {
            path = new DatabaseManager(this).getDatabasePath();
        }
        return new MmxOpenHelper(this, path);
    }

    public Locale getAppLocale() {
        Locale locale = null;
        Context context = getApplicationContext();

        String language = new AppSettings(context).getGeneralSettings().getApplicationLanguage();

        if(!TextUtils.isEmpty(language)) {
            try {
                locale = new Locale(language);
            } catch (Exception e) {
                Timber.e(e, "parsing locale: %s", language);
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
     * @param save     update into database
     * @deprecated Use Info Service directly to read and write this value as it is used only in
     * the main activity.
     */
    @Deprecated
    public boolean setUserName(String userName, boolean save) {
        MmexApplication.userName = userName;

        if (save) {
            InfoService service = new InfoService(this.getApplicationContext());
            boolean updateSuccessful = service.setInfoValue(InfoKeys.USERNAME, userName);

            if (!updateSuccessful) {
                return false;
            }
        }

        return true;
    }

    public String loadUserNameFromDatabase(Context context) {
        InfoService service = new InfoService(context);
        String username = service.getInfoValue(InfoKeys.USERNAME);

        String result = TextUtils.isEmpty(username) ? "" : username;

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
        } catch (Exception e) {
            Timber.e(e, "getting summary accounts");
        }
        return 0;
    }

    /**
     * @return the userName
     */
    public String getUserName() {
        return userName;
    }

//    public boolean isUriAvailable(Context context, Intent intent) {
//        return context.getPackageManager().resolveActivity(intent, 0) != null;
//    }

    /*
        Private
    */

    private double getSummaryAccountsInternal(Context context) {
        double curTotal = 0;

        LookAndFeelSettings settings = new AppSettings(context)
                .getLookAndFeelSettings();
        // compose whereClause
        String where = "";
        // check if show only open accounts
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

    private void registerCustomFonts() {
        String iconFontPath = "fonts/mmex.ttf";

        // Font icons
        Iconics.registerFont(new MMXIconFont());

        // Initialize font icons support.
        FontIconTypefaceHolder.init(getAssets(), iconFontPath);
    }

}
