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
package com.money.manager.ex.home;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.analytics.HitBuilders;
import com.money.manager.ex.AnalyticsTrackers;
import com.money.manager.ex.BuildConfig;
import com.money.manager.ex.DonateActivity;
import com.money.manager.ex.HelpActivity;
import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.PasscodeActivity;
import com.money.manager.ex.R;
import com.money.manager.ex.about.AboutActivity;
import com.money.manager.ex.account.AccountTransactionListFragment;
import com.money.manager.ex.assetallocation.AssetAllocationActivity;
import com.money.manager.ex.assetallocation.AssetAllocationOverviewActivity;
import com.money.manager.ex.budget.BudgetsActivity;
import com.money.manager.ex.core.InfoKeys;
import com.money.manager.ex.database.MmexOpenHelper;
import com.money.manager.ex.database.PasswordActivity;
import com.money.manager.ex.dropbox.events.DbFileDownloadedEvent;
import com.money.manager.ex.home.events.AccountsTotalLoadedEvent;
import com.money.manager.ex.home.events.RequestAccountFragmentEvent;
import com.money.manager.ex.home.events.RequestOpenDatabaseEvent;
import com.money.manager.ex.home.events.RequestWatchlistFragmentEvent;
import com.money.manager.ex.home.events.UsernameLoadedEvent;
import com.money.manager.ex.servicelayer.InfoService;
import com.money.manager.ex.common.CategoryListFragment;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.currency.CurrencyListActivity;
import com.money.manager.ex.dropbox.DropboxManager;
import com.money.manager.ex.core.MoneyManagerBootReceiver;
import com.money.manager.ex.core.Passcode;
import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.dropbox.DropboxHelper;
import com.money.manager.ex.dropbox.DropboxServiceIntent;
import com.money.manager.ex.account.AccountListFragment;
import com.money.manager.ex.common.BaseFragmentActivity;
import com.money.manager.ex.fragment.PayeeListFragment;
import com.money.manager.ex.investment.WatchlistFragment;
import com.money.manager.ex.notifications.RepeatingTransactionNotifications;
import com.money.manager.ex.recurring.transactions.RecurringTransactionListFragment;
import com.money.manager.ex.reports.CategoriesReportActivity;
import com.money.manager.ex.reports.IncomeVsExpensesActivity;
import com.money.manager.ex.reports.PayeesReportActivity;
import com.money.manager.ex.search.SearchActivity;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.settings.SettingsActivity;
import com.money.manager.ex.settings.events.AppRestartRequiredEvent;
import com.money.manager.ex.tutorial.TutorialActivity;
import com.money.manager.ex.utils.MyDatabaseUtils;
import com.money.manager.ex.utils.MyFileUtils;
import com.shamanland.fonticon.FontIconDrawable;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.net.URLDecoder;
import java.util.ArrayList;

/**
 * Main activity of the application.
 */
public class MainActivity
    extends BaseFragmentActivity {

    public static final int REQUEST_PICKFILE_CODE = 1;
    public static final int REQUEST_PASSCODE = 2;
    public static final int REQUEST_TUTORIAL = 3;
    public static final int REQUEST_PASSWORD = 4;

    public static final String EXTRA_DATABASE_PATH = "dbPath";

    /**
     * @return the mRestart
     */
    public static boolean isRestartActivitySet() {
        return mRestartActivity;
    }

    public DropboxHelper mDropboxHelper;

    // private

    private static final String LOGCAT = MainActivity.class.getSimpleName();
    private static final String KEY_IS_AUTHENTICATED = "MainActivity:isAuthenticated";
    private static final String KEY_IN_AUTHENTICATION = "MainActivity:isInAuthenticated";
    private static final String KEY_IS_SHOW_TIPS_DROPBOX2 = "MainActivity:isShowTipsDropbox2";
    private static final String KEY_CLASS_FRAGMENT_CONTENT = "MainActivity:Fragment";
    private static final String KEY_ORIENTATION = "MainActivity:Orientation";
    private static final String KEY_RECURRING_TRANSACTION = "MainActivity:RecurringTransaction";
    private static final String KEY_HAS_STARTED = "MainActivity:hasStarted";
    // state if restart activity
    private static boolean mRestartActivity = false;

    private boolean isAuthenticated = false;
    private boolean isInAuthentication = false;
    private boolean isShowTipsDropbox2 = false;
    private boolean isRecurringTransactionStarted = false;
    private boolean hasStarted = false;
    // navigation drawer
    private LinearLayout mDrawerLayout;
    private DrawerLayout mDrawer;
    private MyActionBarDrawerToggle mDrawerToggle;
    // object in drawer
    private LinearLayout mDrawerLinearRepeating;
    private TextView mDrawerTextUserName;
    private TextView mDrawerTextTotalAccounts;
    // state dual panel
    private boolean mIsDualPanel = false;
    private RecentDatabasesProvider recentDbs;
    private boolean mInitialized = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // show tutorial
        boolean tutorialShown = showTutorial();

        // Restore state. Check authentication, etc.
        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState);
        }

        // Initialize the map for recent entries that link to drawer menu items.
        this.recentDbs = new RecentDatabasesProvider(this.getApplicationContext());

        // Close any existing notifications.
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(DropboxServiceIntent.NOTIFICATION_DROPBOX_OPEN_FILE);

        // Create a connection to Dropbox.
        this.mDropboxHelper = DropboxHelper.getInstance(getApplicationContext());

        createLayout();

        pingStats();

        if (!tutorialShown) {
            // Skipped tutorial because it was seen in the past.
            onTutorialComplete(savedInstanceState);
            // Otherwise continue at onActivityResult after tutorial closed.
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);

        // check if has pass-code and authenticate
        if (mInitialized && !isAuthenticated) {
            Passcode passcode = new Passcode(getApplicationContext());
            if (passcode.hasPasscode() && !isInAuthentication) {
                Intent intent = new Intent(this, PasscodeActivity.class);
                // set action and data
                intent.setAction(PasscodeActivity.INTENT_REQUEST_PASSWORD);
                intent.putExtra(PasscodeActivity.INTENT_MESSAGE_TEXT, getString(R.string.enter_your_passcode));
                // start activity
                startActivityForResult(intent, REQUEST_PASSCODE);
                // set in authentication
                isInAuthentication = true;
            }
        }

        // todo: mark the active database file in the navigation panel.
        // mDrawer
    }

    @Override
    protected void onResume() {
        super.onResume();

        // check if restart activity
        if (isRestartActivitySet()) {
            restartActivity(); // restart and exit
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (mDrawerToggle != null) {
            try {
                mDrawerToggle.syncState();
            } catch (Exception e) {
                ExceptionHandler handler = new ExceptionHandler(getApplicationContext(), this);
                handler.handle(e, "drawer sync state");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_PICKFILE_CODE:
                if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                    requestDatabaseChange(data.getData().getPath());
                }
                break;

            case REQUEST_PASSCODE:
                isAuthenticated = false;
                isInAuthentication = false;
                if (resultCode == RESULT_OK && data != null) {
                    Passcode passcode = new Passcode(getApplicationContext());
                    String passIntent = data.getStringExtra(PasscodeActivity.INTENT_RESULT_PASSCODE);
                    String passDb = passcode.getPasscode();
                    if (passIntent != null && passDb != null) {
                        isAuthenticated = passIntent.equals(passDb);
                        if (!isAuthenticated) {
                            Toast.makeText(getApplicationContext(), R.string.passocde_no_macth, Toast.LENGTH_LONG).show();
                        }
                    }
                }
                // close if not authenticated
                if (!isAuthenticated) {
                    this.finish();
                }
                break;

            case REQUEST_TUTORIAL:
                onTutorialComplete(null);
                break;

            case REQUEST_PASSWORD:
                if (resultCode == RESULT_OK && data != null) {
                    String dbPath = data.getStringExtra(EXTRA_DATABASE_PATH);
                    String password = data.getStringExtra(PasswordActivity.EXTRA_PASSWORD);

                    changeDatabase(dbPath, password);
                }
        }
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);

        super.onStop();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setRestartActivity(true);
    }

    // Menu

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // toggle drawer with the menu hardware button.
        if (item.getItemId() == android.R.id.home) {
            if (mDrawer != null) {
                if (mDrawer.isDrawerOpen(mDrawerLayout)) {
                    mDrawer.closeDrawer(mDrawerLayout);
                } else {
                    mDrawer.openDrawer(mDrawerLayout);
                }
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // End Menu.

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Hardware Menu key pressed.
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (mDrawer.isDrawerOpen(mDrawerLayout)) {
                mDrawer.closeDrawer(mDrawerLayout);
            } else {
                mDrawer.openDrawer(mDrawerLayout);
            }
            // Do not propagate the event further. Mark the event as handled.
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Core core = new Core(getApplicationContext());
        if (core.isTablet()) {
            Fragment fragment = getSupportFragmentManager().findFragmentById(getResIdLayoutContent());
            if (fragment != null) {
                if (fragment instanceof AccountTransactionListFragment) {
                    outState.putString(KEY_CLASS_FRAGMENT_CONTENT, ((AccountTransactionListFragment) fragment).getFragmentName());
                } else if ((!(fragment instanceof DashboardFragment)) && (!(fragment instanceof HomeFragment))) {
                    outState.putString(KEY_CLASS_FRAGMENT_CONTENT, fragment.getClass().getName());
                }
                // move pop stack in onCreate event
            }
        }
        outState.putBoolean(KEY_IS_AUTHENTICATED, isAuthenticated);
        outState.putBoolean(KEY_IN_AUTHENTICATION, isInAuthentication);
        outState.putBoolean(KEY_IS_SHOW_TIPS_DROPBOX2, isShowTipsDropbox2);
        outState.putBoolean(KEY_RECURRING_TRANSACTION, isRecurringTransactionStarted);
        outState.putInt(KEY_ORIENTATION, getResources().getConfiguration().orientation);
        outState.putBoolean(KEY_HAS_STARTED, this.hasStarted);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null)
            notificationManager.cancelAll();
        super.onDestroy();

        // close database
//        MmexOpenHelper.closeDatabase();
    }

    @Override
    public void onBackPressed() {
        if(mDrawer.isDrawerOpen(Gravity.LEFT)){
            mDrawer.closeDrawer(Gravity.LEFT);
        }else{
            try {
                super.onBackPressed();
            } catch (IllegalStateException e) {
                ExceptionHandler handler = new ExceptionHandler(this, this);
                handler.handle(e, "on back pressed");
            }
        }
    }

    // Permissions

    @Override
    public void onRequestPermissionsResult (int requestCode, String[] permissions, int[] grantResults) {
        // cancellation
        if (permissions.length == 0) return;

        // Currently the request code does not matter. We ask for read/write permissions.
        initialize(null);
    }

    // Custom methods

    public void checkDropboxForUpdates() {
        if (mDropboxHelper == null || !mDropboxHelper.isLinked()) {
            return;
        }

        String currentDbPath = MoneyManagerApplication.getDatabasePath(getApplicationContext());
        if (StringUtils.isEmpty(currentDbPath)) return;

        File db = new File(currentDbPath);
        String dbName = db.getName();

        String remoteDb = mDropboxHelper.getLinkedRemoteFile();
        if (StringUtils.isEmpty(remoteDb)) return;

        File dropboxDb = new File(remoteDb);
        String remoteDbName = dropboxDb.getName();

        boolean shouldCheckDbUpdates = dbName.equalsIgnoreCase(remoteDbName);
        if (!shouldCheckDbUpdates) return;

        AsyncTask<Void, Integer, Integer> asyncTask = new CheckDropboxForUpdatesTask(this, mDropboxHelper);
        asyncTask.execute();
    }

    public void createLayout() {
        setContentView(R.layout.main_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        LinearLayout fragmentDetail = (LinearLayout) findViewById(R.id.fragmentDetail);
        setDualPanel(fragmentDetail != null && fragmentDetail.getVisibility() == View.VISIBLE);

        initializeDrawer();
    }

    /**
     * @return the mIsDualPanel
     */
    public boolean isDualPanel() {
        return mIsDualPanel;
    }

    public int getResIdLayoutContent() {
        return isDualPanel() ? R.id.fragmentDetail : R.id.fragmentContent;
    }

    /**
     * Handle the callback from the drawer click handler.
     * @param item selected DrawerMenuItem
     * @return boolean indicating whether the action was handled or not.
     */
    //@Override
    public boolean onDrawerMenuAndOptionMenuSelected(DrawerMenuItem item) {
        boolean result = true;
        Intent intent;
        final Core core = new Core(getApplicationContext());
        final Boolean isDarkTheme = core.getThemeId() == R.style.Theme_Money_Manager_Dark;

        if (item.getId() == null && item.getTag() != null) {
            String key = item.getTag().toString();
            RecentDatabaseEntry recentDb = this.recentDbs.map.get(key);
            if (recentDb != null) {
                openDatabase(recentDb);
            }
        }
        if (item.getId() == null) return false;

        switch (item.getId()) {
            case R.id.menu_home:
                showFragment(HomeFragment.class);
                break;
            case R.id.menu_sync_dropbox:
                DropboxManager dropbox = new DropboxManager(MainActivity.this, mDropboxHelper);
                dropbox.synchronizeDropbox();
                break;
            case R.id.menu_open_database:
                openDatabasePicker();
                break;
            case R.id.menu_account:
                showFragment(AccountListFragment.class);
                break;
            case R.id.menu_category:
                showFragment(CategoryListFragment.class);
                break;
            case R.id.menu_currency:
                // Show Currency list.
                intent = new Intent(MainActivity.this, CurrencyListActivity.class);
                intent.setAction(Intent.ACTION_EDIT);
                startActivity(intent);
                break;
            case R.id.menu_payee:
                showFragment(PayeeListFragment.class);
                break;
            case R.id.menu_recurring_transaction:
                showFragment(RecurringTransactionListFragment.class);
                break;
            case R.id.menu_budgets:
                intent = new Intent(this, BudgetsActivity.class);
                startActivity(intent);
                break;
            case R.id.menu_asset_allocation:
                intent = new Intent(this, AssetAllocationActivity.class);
                startActivity(intent);
                break;
            case R.id.menu_search_transaction:
                startActivity(new Intent(MainActivity.this, SearchActivity.class));
                break;
            case R.id.menu_report_categories:
                startActivity(new Intent(this, CategoriesReportActivity.class));
                break;
            case R.id.menu_settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                break;
            case R.id.menu_reports:
                showReportsSelector(isDarkTheme, item.getText());
                break;
            case R.id.menu_report_payees:
                startActivity(new Intent(this, PayeesReportActivity.class));
                break;
            case R.id.menu_report_where_money_goes:
                intent = new Intent(this, CategoriesReportActivity.class);
                intent.putExtra(CategoriesReportActivity.REPORT_FILTERS, TransactionTypes.Withdrawal.name());
                intent.putExtra(CategoriesReportActivity.REPORT_TITLE, getString(R.string.menu_report_where_money_goes));
                startActivity(intent);
                break;
            case R.id.menu_report_where_money_comes_from:
                intent = new Intent(this, CategoriesReportActivity.class);
                intent.putExtra(CategoriesReportActivity.REPORT_FILTERS, TransactionTypes.Deposit.name());
                intent.putExtra(CategoriesReportActivity.REPORT_TITLE, getString(R.string.menu_report_where_money_comes_from));
                startActivity(intent);
                break;
            case R.id.menu_report_income_vs_expenses:
                startActivity(new Intent(this, IncomeVsExpensesActivity.class));
                break;
            case R.id.menu_asset_allocation_overview:
                startActivity(new Intent(this, AssetAllocationOverviewActivity.class));
                break;
            case R.id.menu_help:
                startActivity(new Intent(MainActivity.this, HelpActivity.class));
                break;
            case R.id.menu_about:
                startActivity(new Intent(MainActivity.this, AboutActivity.class));
                break;
            case R.id.menu_donate:
                startActivity(new Intent(this, DonateActivity.class));
                break;
            default:
                // if no match, return false
                result = false;
        }

        return result;
    }

    // Events (EventBus)

    @Subscribe
    public void onEvent(RequestAccountFragmentEvent event) {
        showAccountFragment(event.accountId);
    }

    @Subscribe
    public void onEvent(RequestWatchlistFragmentEvent event) {
        showWatchlistFragment(event.accountId);
    }

    @Subscribe
    public void onEvent(RequestOpenDatabaseEvent event) {
        openDatabasePicker();
    }

    @Subscribe
    public void onEvent(UsernameLoadedEvent event) {
        setDrawerUserName(MoneyManagerApplication.getInstanceApp().getUserName());
    }

    @Subscribe
    public void onEvent(AccountsTotalLoadedEvent event) {
        setDrawerTotalAccounts(event.amount);
    }

    @Subscribe
    public void onEvent(AppRestartRequiredEvent event) {
        MainActivity.mRestartActivity = true;
    }

    /**
     * Dropbox just downloaded the database. Reload fragments.
     */
    public void onEvent(DbFileDownloadedEvent event) {
        // open the new database.
        DropboxManager dropbox = new DropboxManager(this, mDropboxHelper);
        dropbox.openDownloadedDatabase();
    }

    // Private.

    private void initialize(Bundle savedInstanceState) {
        handleIntent();

        // show change log dialog <- separate changelog maintenance is a waste of time
//        Core core = new Core(this);
//        if (core.isToDisplayChangelog()) core.showChangelog();

        MoneyManagerApplication.showCurrentDatabasePath(getApplicationContext());

        // check if we require a password.
        String dbPath = MoneyManagerApplication.getDatabasePath(this);
        if (MyDatabaseUtils.isEncryptedDatabase(dbPath) && !MmexOpenHelper.getInstance(this).hasPassword()) {
            requestDatabasePassword(dbPath);
        } else {
            initializeDatabaseAccess(savedInstanceState);
        }
    }

    private void initializeDatabaseAccess(Bundle savedInstanceState) {
        // Read something from the database at this stage so that the db file gets created.
        InfoService infoService = new InfoService(getApplicationContext());
        String username = infoService.getInfoValue(InfoKeys.USERNAME);

        // fragments
//        displayDefaultFragment();
//        displayLastViewedFragment(savedInstanceState);
        originalShowFragment(savedInstanceState);

        // start notification for recurring transaction
        if (!isRecurringTransactionStarted) {
            AppSettings settings = new AppSettings(this);
            boolean showNotification = settings.getBehaviourSettings().getNotificationRecurringTransaction();
            if (showNotification) {
                RepeatingTransactionNotifications notifications = new RepeatingTransactionNotifications(this);
                notifications.notifyRepeatingTransaction();
                isRecurringTransactionStarted = true;
            }
        }

        // notification send broadcast
        Intent serviceRepeatingTransaction = new Intent(getApplicationContext(), MoneyManagerBootReceiver.class);
        getApplicationContext().sendBroadcast(serviceRepeatingTransaction);

        if (!this.hasStarted) {
            // This is to avoid checking Dropbox on every device rotation.
            checkDropboxForUpdates();
            this.hasStarted = true;
        }

        this.mInitialized = true;
    }

    private void initializeDrawer() {
        // navigation drawer
        mDrawer = (DrawerLayout) findViewById(R.id.drawerLayout);

        // set a custom shadow that overlays the main content when the drawer opens
        if (mDrawer == null) return;

        mDrawerToggle = new MyActionBarDrawerToggle(this, mDrawer, R.string.open, R.string.closed);
        mDrawer.setDrawerListener(mDrawerToggle);

        // create drawer menu
        initializeDrawerVariables();
        createExpandableDrawer();

        // enable ActionBar app icon to behave as action to toggle nav drawer
        setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
    }

    private void onTutorialComplete(Bundle savedInstanceState) {
        // Request external storage permissions.
        MyFileUtils fileUtils = new MyFileUtils(this);
        boolean showingDialog = fileUtils.requestExternalStoragePermissions(this);
        // async, continue at onRequestPermissionsResult().
        if (!showingDialog) {
            initialize(savedInstanceState);
        }
    }

    /**
     * Pick the database file to use.
     *
     * @param file start folder
     */
    public void pickFile(File file) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setDataAndType(Uri.fromFile(file), "vnd.android.cursor.dir/*");
        intent.setType("file/*");

        if (MoneyManagerApplication.getInstanceApp().isUriAvailable(getApplicationContext(), intent)) {
            try {
                startActivityForResult(intent, REQUEST_PICKFILE_CODE);
            } catch (Exception e) {
                ExceptionHandler handler = new ExceptionHandler(this, this);
                handler.handle(e, "selecting a database file");
            }
        } else {
            Toast.makeText(getApplicationContext(), R.string.error_intent_pick_file,
                    Toast.LENGTH_LONG).show();
        }

        // Note that the selected file is handled in onActivityResult.
    }

    /**
     * for the change setting restart process application
     */
    public void restartActivity() {
        if (mRestartActivity) {
//            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB_MR1) {
//                // this is for APIs < 11.
//                Intent intent = getIntent();
//                overridePendingTransition(0, 0);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
//                // finish this activity
//                finish();
//                overridePendingTransition(0, 0);
//                // restart
//                startActivity(intent);
////            // kill process
////            android.os.Process.killProcess(android.os.Process.myPid());
//            } else {
//                // new api:
//                this.recreate();
//            }

            finish();

            startMainActivity();
        }
        // set state a false
        setRestartActivity(false);
    }

    public void startMainActivity() {
        Intent intent = getBaseContext().getPackageManager()
            .getLaunchIntentForPackage( getBaseContext().getPackageName() );
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    /**
     * @param mIsDualPanel the mIsDualPanel to set
     */
    public void setDualPanel(boolean mIsDualPanel) {
        this.mIsDualPanel = mIsDualPanel;
    }

    /**
     * Show fragment using reflection from class
     */
    public void showFragment(Class<?> fragmentClass) {
        if (fragmentClass == null) return;

        Fragment fragment = getSupportFragmentManager().findFragmentByTag(fragmentClass.getName());
        if (fragment == null || fragment.getId() != getResIdLayoutContent()) {
            ClassLoader loader = getClassLoader();
            if (loader != null) {
                try {
                    Class<?> classFragment = loader.loadClass(fragmentClass.getName());
                    fragment = (Fragment) classFragment.newInstance();
                } catch (Exception e) {
                    ExceptionHandler handler = new ExceptionHandler(getApplicationContext(), this);
                    handler.handle(e, "creating new fragment");
                }
            }
        }
        // check if fragment is not null
        if (fragment != null) {
            showFragment(fragment);
        }
    }

    /**
     * Displays the fragment without indicating the tag. The tag will be the classname of the fragment
     *
     * @param fragment
     */
    public void showFragment(Fragment fragment) {
        showFragment(fragment, fragment.getClass().getName());
    }

    /**
     * Displays the fragment and associate the tag
     *
     * @param fragment Fragment to display
     * @param tagFragment Tag/name to search for.
     */
    public void showFragment(Fragment fragment, String tagFragment) {
        try {
            showFragment_Internal(fragment, tagFragment);
        } catch (Exception e) {
            ExceptionHandler handler = new ExceptionHandler(getApplicationContext(), this);
            handler.handle(e, "showing fragment with tag");
        }
    }

    /**
     * Shows a fragment with the selected account (id) and transactions.
     * Called from Home Fragment when an account is clicked in the main list.
     * @param accountId id of the account for which to show the transactions
     */
    public void showAccountFragment(int accountId) {
        String tag = AccountTransactionListFragment.class.getSimpleName() + "_" + Integer.toString(accountId);
        AccountTransactionListFragment fragment = (AccountTransactionListFragment) getSupportFragmentManager().findFragmentByTag(tag);
        if (fragment == null || fragment.getId() != getResIdLayoutContent()) {
            fragment = AccountTransactionListFragment.newInstance(accountId);
        }
        // show fragment
        showFragment(fragment, tag);
    }

    public void showWatchlistFragment(int accountId) {
        String tagFragment = WatchlistFragment.class.getSimpleName() + "_" + Integer.toString(accountId);
        WatchlistFragment fragment = (WatchlistFragment) getSupportFragmentManager()
                .findFragmentByTag(tagFragment);
        if (fragment == null || fragment.getId() != getResIdLayoutContent()) {
            fragment = WatchlistFragment.newInstance(accountId);
        }
        // show fragment
        showFragment(fragment, tagFragment);
    }

    /**
     * Show tutorial on first run.
     * @return boolean indicator whether the tutorial was displayed or not
     */
    public boolean showTutorial() {
        boolean showTutorial = new AppSettings(this).getBehaviourSettings().getShowTutorial();
        if (!showTutorial) return false;

        // else show tutorial.
        Intent intent = new Intent(this, TutorialActivity.class);
        startActivityForResult(intent, REQUEST_TUTORIAL);
        // Tutorial is marked as seen when OK on the last page is clicked.

        // Continued at onActivityResult.
        return true;
    }

    public void setDrawerUserName(String userName) {
        if (mDrawerTextUserName != null)
            mDrawerTextUserName.setText(userName);
    }

    public void setDrawerTotalAccounts(String totalAccounts) {
        if (mDrawerTextTotalAccounts != null)
            mDrawerTextTotalAccounts.setText(totalAccounts);
    }

    public void onClickCardViewIncomesVsExpenses(View v) {
        startActivity(new Intent(this, IncomeVsExpensesActivity.class));
    }

    public void onDrawerItemSubDialogs(final DrawerMenuItemAdapter adapter, CharSequence title, Boolean isDarkTheme) {
        final MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title(title)
                .adapter(adapter, null)
                .build();

        ListView listView = dialog.getListView();
        if (listView != null) {
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    onDrawerMenuAndOptionMenuSelected(adapter.getItem(position));
                    dialog.dismiss();
                }
            });
        }

        dialog.show();
    }

    public void openDatabasePicker() {
        pickFile(Environment.getExternalStorageDirectory());
    }

    // Private

    private void createExpandableDrawer() {
        // Menu.

        final ArrayList<DrawerMenuItem> groupItems = getDrawerMenuItems();
        final ArrayList<Object> childItems = new ArrayList<>();

        // Home
        childItems.add(null);

        // Open Database. Display the recent db list.
        ArrayList<DrawerMenuItem> childDatabases = getRecentDatabases();
        childItems.add(childDatabases);

        // Dropbox
        if (mDropboxHelper != null && mDropboxHelper.isLinked()) {
            childItems.add(null);
        }

        // Entities
        ArrayList<DrawerMenuItem> childTools = new ArrayList<>();
        // manage: account
        childTools.add(new DrawerMenuItem().withId(R.id.menu_account)
                .withText(getString(R.string.accounts))
                .withIconDrawable(FontIconDrawable.inflate(this, R.xml.ic_temple)));
        // manage: categories
        childTools.add(new DrawerMenuItem().withId(R.id.menu_category)
                .withText(getString(R.string.categories))
                .withIconDrawable(FontIconDrawable.inflate(this, R.xml.ic_tag_empty)));
        // manage: currencies
        childTools.add(new DrawerMenuItem().withId(R.id.menu_currency)
                .withText(getString(R.string.currencies))
                .withIconDrawable(FontIconDrawable.inflate(this, R.xml.ic_euro)));
        // manage: payees
        childTools.add(new DrawerMenuItem().withId(R.id.menu_payee)
                .withText(getString(R.string.payees))
                .withIconDrawable(FontIconDrawable.inflate(this, R.xml.ic_people)));
        childItems.add(childTools);

        // Recurring Transactions
        childItems.add(null);

        // Budgets
        childItems.add(null);

        // Asset Allocation
        //if (BuildConfig.DEBUG) <- this was used to hide the menu item while testing.
        childItems.add(null);

        // Search transaction
        childItems.add(null);

        // reports
        childItems.add(null);

        // Settings
        childItems.add(null);

        // Donate
        childItems.add(null);

        // Help
        childItems.add(null);

        // Adapter.
        final ExpandableListView drawerList = (ExpandableListView) findViewById(R.id.drawerExpandableList);
        DrawerMenuGroupAdapter adapter = new DrawerMenuGroupAdapter(this, groupItems, childItems);
        drawerList.setAdapter(adapter);

        // set listener on item click
        drawerList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                if (mDrawer == null) return false;
                // if the group has child items, do not handle.
                ArrayList<String> children = (ArrayList<String>) childItems.get(groupPosition);
                if (children != null) return false;

                // Highlight the selected item, update the title, and close the drawer
                drawerList.setItemChecked(groupPosition, true);

                // You should reset item counter
                mDrawer.closeDrawer(mDrawerLayout);
                // check item selected
                final DrawerMenuItem item = (DrawerMenuItem) drawerList.getExpandableListAdapter()
                        .getGroup(groupPosition);
                if (item != null) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // execute operation
                            onDrawerMenuAndOptionMenuSelected(item);
                        }
                    }, 200);
                }
                return true;
            }
        });

        drawerList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                if (mDrawer == null) return false;

                mDrawer.closeDrawer(mDrawerLayout);

                ArrayList<Object> children = (ArrayList) childItems.get(groupPosition);
                final DrawerMenuItem selectedItem = (DrawerMenuItem) children.get(childPosition);
                if (selectedItem != null) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // execute operation
                            onDrawerMenuAndOptionMenuSelected(selectedItem);
                        }
                    }, 200);
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    private void changeDatabase(String dbFilePath, String password) {
        Core core = new Core(getApplicationContext());
        core.changeDatabase(dbFilePath, password);

        // Store the name into the recent files list.
        if (!this.recentDbs.contains(dbFilePath)) {
            this.recentDbs.add(RecentDatabaseEntry.fromPath(dbFilePath));
        }

        // restart this activity
        setRestartActivity(true);
        restartActivity();
    }

    private void displayDefaultFragment() {
        // show main navigation fragment
        final String homeFragmentTag = HomeFragment.class.getSimpleName();
        HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager()
            .findFragmentByTag(homeFragmentTag);

        if (homeFragment == null) {
            // fragment create
            homeFragment = new HomeFragment();
        }

        final HomeFragment finalFragment = homeFragment;
        // ref: http://stackoverflow.com/a/14178962/202166
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                try {
                    getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContent, finalFragment, homeFragmentTag)
                        .commit();
                } catch (Exception e) {
                    ExceptionHandler handler = new ExceptionHandler(MainActivity.this, MainActivity.this);
                    handler.handle(e, "showing initial fragments");
                }
            }
        });
    }

    private void originalShowFragment(Bundle savedInstanceState) {
        Core core = new Core(this);

        // show home fragment
        HomeFragment fragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag(HomeFragment.class.getSimpleName());
        if (fragment == null) {
            // fragment create
            fragment = new HomeFragment();
            // add to stack
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContent, fragment, HomeFragment.class.getSimpleName())
                //.commit();
                .commitAllowingStateLoss();
        } else if (core.isTablet()) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContent, fragment, HomeFragment.class.getSimpleName())
                //.commit();
                .commitAllowingStateLoss();
        }

        // manage fragment
        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_CLASS_FRAGMENT_CONTENT)) {
            String className = savedInstanceState.getString(KEY_CLASS_FRAGMENT_CONTENT);
            // check if className is null, then setting Home Fragment
            if (TextUtils.isEmpty(className)) {
                className = HomeFragment.class.getName();
            }
            if (className.contains(AccountTransactionListFragment.class.getSimpleName())) {
                // changeFragment(Integer.parseInt(className.substring(className.indexOf("_") + 1)));
                showAccountFragment(Integer.parseInt(className.substring(className.indexOf("_") + 1)));
            } else {
                try {
                    showFragment(Class.forName(className));
                } catch (ClassNotFoundException e) {
                    Log.e(LOGCAT, e.getMessage());
                }
            }
        }
    }

    private ArrayList<DrawerMenuItem> getDrawerMenuItems() {
        ArrayList<DrawerMenuItem> menuItems = new ArrayList<>();

        // Home
        menuItems.add(new DrawerMenuItem().withId(R.id.menu_home)
            .withText(getString(R.string.home))
            .withIconDrawable(FontIconDrawable.inflate(this, R.xml.ic_home)));

        // Open database
        menuItems.add(new DrawerMenuItem().withId(R.id.menu_open_database)
            .withText(getString(R.string.open_database))
            .withIconDrawable(FontIconDrawable.inflate(this, R.xml.ic_open_folder)));

        // Dropbox synchronize
        if (mDropboxHelper != null && mDropboxHelper.isLinked()) {
            menuItems.add(new DrawerMenuItem().withId(R.id.menu_sync_dropbox)
                .withText(getString(R.string.synchronize))
                .withIconDrawable(FontIconDrawable.inflate(this, R.xml.ic_dropbox)));
        }

        // Entities
        menuItems.add(new DrawerMenuItem().withId(R.id.menu_group_main)
            .withText(getString(R.string.entities))
            .withIconDrawable(FontIconDrawable.inflate(this, R.xml.ic_building)));

        // Recurring Transactions
        menuItems.add(new DrawerMenuItem().withId(R.id.menu_recurring_transaction)
            .withText(getString(R.string.repeating_transactions))
            .withIconDrawable(FontIconDrawable.inflate(this, R.xml.ic_recurring)));

        // Budgets
        menuItems.add(new DrawerMenuItem().withId(R.id.menu_budgets)
            .withText(getString(R.string.budgets))
            .withIconDrawable(FontIconDrawable.inflate(this, R.xml.ic_law)));

        // Asset Allocation
        menuItems.add(new DrawerMenuItem().withId(R.id.menu_asset_allocation)
            .withText(getString(R.string.asset_allocation))
            .withIconDrawable(FontIconDrawable.inflate(this, R.xml.ic_pie_chart)));

        // Search transaction
        menuItems.add(new DrawerMenuItem().withId(R.id.menu_search_transaction)
            .withText(getString(R.string.search))
            .withIconDrawable(FontIconDrawable.inflate(this, R.xml.ic_search)));
        // reports
        menuItems.add(new DrawerMenuItem().withId(R.id.menu_reports)
            .withText(getString(R.string.menu_reports))
            .withIconDrawable(FontIconDrawable.inflate(this, R.xml.ic_reports))
            .withDivider(true));
        // Settings
        menuItems.add(new DrawerMenuItem().withId(R.id.menu_settings)
            .withText(getString(R.string.settings))
            .withIconDrawable(FontIconDrawable.inflate(this, R.xml.ic_settings)));
        // Donate
        menuItems.add(new DrawerMenuItem().withId(R.id.menu_donate)
            .withText(getString(R.string.donate))
            .withIconDrawable(FontIconDrawable.inflate(this, R.xml.ic_gift))
            .withDivider(Boolean.TRUE));
        // Help
        menuItems.add(new DrawerMenuItem().withId(R.id.menu_about)
            .withText(getString(R.string.about))
            .withIconDrawable(FontIconDrawable.inflate(this, R.xml.ic_question)));

        return menuItems;
    }

    private ArrayList<DrawerMenuItem> getRecentDatabases() {
        ArrayList<DrawerMenuItem> childDatabases = new ArrayList<>();

        if (this.recentDbs.map != null) {
            for (RecentDatabaseEntry entry : this.recentDbs.map.values()) {
                String title = entry.getFileName();

                DrawerMenuItem item = new DrawerMenuItem()
                    .withText(title);

                item.setTag(entry.filePath);

                if (entry.linkedToDropbox) {
                    item.withIconDrawable(FontIconDrawable.inflate(this, R.xml.ic_dropbox));
                } else {
                    item.withIconDrawable(FontIconDrawable.inflate(this, R.xml.ic_floppy_disk));
                }
                childDatabases.add(item);
            }
        }

        // Other. Simply open the file picker, as before.
        DrawerMenuItem item = new DrawerMenuItem()
            .withId(R.id.menu_open_database)
            .withText(getString(R.string.other));
        childDatabases.add(item);


        return childDatabases;
    }

    private void handleIntent() {
        if (getIntent() == null || getIntent().getData() == null) return;

        String pathFile = getIntent().getData().getEncodedPath();
        // decode
        try {
            pathFile = URLDecoder.decode(pathFile, "UTF-8"); // decode file path
            if (BuildConfig.DEBUG) Log.d(LOGCAT, "Path intent file to open:" + pathFile);
            // Open this database.
            requestDatabaseChange(pathFile);
        } catch (Exception e) {
            ExceptionHandler handler = new ExceptionHandler(this, this);
            handler.handle(e, "opening database from intent");
        }
    }

    private void initializeDrawerVariables() {
        mDrawerLayout = (LinearLayout) findViewById(R.id.linearLayoutDrawer);

        // repeating transaction
        mDrawerLinearRepeating = (LinearLayout) findViewById(R.id.linearLayoutRepeatingTransaction);
        if (mDrawerLinearRepeating != null) {
            mDrawerLinearRepeating.setVisibility(View.GONE);
        }
//        mDrawerTextViewRepeating = (TextView) findViewById(R.id.textViewOverdue);
        mDrawerTextUserName = (TextView) findViewById(R.id.textViewUserName);
        mDrawerTextTotalAccounts = (TextView) findViewById(R.id.textViewTotalAccounts);
    }

    /**
     * called when quick-switching the recent databases from the navigation menu.
     * @param recentDb selected recent database entry
     */
    private void openDatabase(RecentDatabaseEntry recentDb) {
        // do nothing if selecting the currently open database
        String currentDb = MoneyManagerApplication.getDatabasePath(this);
        if (recentDb.filePath.equals(currentDb)) return;

        // set the Dropbox file, if any.
        String dropboxPath = recentDb.linkedToDropbox
            ? recentDb.dropboxFileName
            : "";
        mDropboxHelper.setLinkedRemoteFile(dropboxPath);

        requestDatabaseChange(recentDb.filePath);
    }

    private void pingStats() {
        AnalyticsTrackers.getInstance().get(AnalyticsTrackers.Target.APP)
            .send(new HitBuilders.ScreenViewBuilder().build());
        //mTracker.setScreenName("Activity~MainActivity");
        //mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    /**
     * Change the database.
     *
     * @param dbFilePath The path to the database file.
     */
    private void requestDatabaseChange(String dbFilePath) {
        Log.v(LOGCAT, "Change database: " + dbFilePath);

        // handle encrypted database(s)
        if (MyDatabaseUtils.isEncryptedDatabase(dbFilePath)) {
            requestDatabasePassword(dbFilePath);
        } else {
            changeDatabase(dbFilePath, null);
        }
    }

    private void requestDatabasePassword(String dbFilePath) {
        // request password
        Intent intent = new Intent(this, PasswordActivity.class);
        //intent.setAction(Intent.A.INTENT_REQUEST_PASSWORD);
        intent.putExtra(EXTRA_DATABASE_PATH, dbFilePath);
        startActivityForResult(intent, REQUEST_PASSWORD);
        // continues onActivityResult.
    }

    private void restoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState.containsKey(KEY_IS_AUTHENTICATED))
            isAuthenticated = savedInstanceState.getBoolean(KEY_IS_AUTHENTICATED);
        if (savedInstanceState.containsKey(KEY_IN_AUTHENTICATION))
            isInAuthentication = savedInstanceState.getBoolean(KEY_IN_AUTHENTICATION);
        if (savedInstanceState.containsKey(KEY_RECURRING_TRANSACTION)) {
            isRecurringTransactionStarted = savedInstanceState.getBoolean(KEY_RECURRING_TRANSACTION);
        }

        // todo: this code is suspicious. Try opening another activity, then rotate device,
        // and then come back to this activity and see what this does. Lots of crashes report this.
        Core core = new Core(this);
        if (savedInstanceState.containsKey(KEY_ORIENTATION) && core.isTablet()
            && savedInstanceState.getInt(KEY_ORIENTATION) != getResources().getConfiguration().orientation) {
            for (int i = 0; i < getSupportFragmentManager().getBackStackEntryCount(); ++i) {
                getSupportFragmentManager().popBackStack();
            }
        }

        if(savedInstanceState.containsKey(KEY_HAS_STARTED)) {
            this.hasStarted = savedInstanceState.getBoolean(KEY_HAS_STARTED);
        }
    }

    private void setRestartActivity(boolean mRestart) {
        MainActivity.mRestartActivity = mRestart;
    }

    private void showFragment_Internal(Fragment fragment, String tag) {
        // Check if fragment is already added.
        if (fragment.isAdded()) return;

        // In tablet layout, do not try to display the Home Fragment again. Show empty fragment.
        if (isDualPanel() && tag.equalsIgnoreCase(HomeFragment.class.getName())) {
            fragment = new Fragment();
            tag = "Empty";
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_right, R.anim.slide_out_left);
        // Replace whatever is in the fragment_container view with this fragment.
        if (isDualPanel()) {
            transaction.replace(R.id.fragmentDetail, fragment, tag);
        } else {
            transaction.replace(R.id.fragmentContent, fragment, tag);
        }
        transaction.addToBackStack(null);
        // Commit the transaction
        transaction.commit();
        // use this call to prevent exception in some cases -> commitAllowingStateLoss()
        // The exception is "fragment already added".
//        transaction.commitAllowingStateLoss();
    }

    private void showReportsSelector(boolean isDarkTheme, String text) {
        final DrawerMenuItemAdapter adapter = new DrawerMenuItemAdapter(this);
        // payee
        adapter.add(new DrawerMenuItem().withId(R.id.menu_report_payees)
            .withText(getString(R.string.payees))
            .withIcon(isDarkTheme ? R.drawable.ic_action_pie_chart_dark : R.drawable.ic_action_pie_chart_light));
        // where money goes
        adapter.add(new DrawerMenuItem().withId(R.id.menu_report_where_money_goes)
            .withText(getString(R.string.menu_report_where_money_goes))
            .withIcon(isDarkTheme ? R.drawable.ic_action_pie_chart_dark : R.drawable.ic_action_pie_chart_light));
        // where money comes from
        adapter.add(new DrawerMenuItem().withId(R.id.menu_report_where_money_comes_from)
            .withText(getString(R.string.menu_report_where_money_comes_from))
            .withIcon(isDarkTheme ? R.drawable.ic_action_pie_chart_dark : R.drawable.ic_action_pie_chart_light));
        // where money comes from
        adapter.add(new DrawerMenuItem().withId(R.id.menu_report_categories)
            .withText(getString(R.string.categories))
            .withIcon(isDarkTheme ? R.drawable.ic_action_pie_chart_dark : R.drawable.ic_action_pie_chart_light));// where money comes from
        // income vs. expenses
        adapter.add(new DrawerMenuItem().withId(R.id.menu_report_income_vs_expenses)
            .withText(getString(R.string.menu_report_income_vs_expenses))
            .withIcon(isDarkTheme ? R.drawable.ic_action_bargraph_dark : R.drawable.ic_action_bargraph_light));
        // Asset Allocation Overview
        adapter.add(new DrawerMenuItem().withId(R.id.menu_asset_allocation_overview)
            .withText(getString(R.string.asset_allocation))
            .withIconDrawable(FontIconDrawable.inflate(this, R.xml.ic_pie_chart)));

        onDrawerItemSubDialogs(adapter, text, isDarkTheme);
    }
}
