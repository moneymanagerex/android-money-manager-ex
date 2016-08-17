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

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.money.manager.ex.DonateActivity;
import com.money.manager.ex.HelpActivity;
import com.money.manager.ex.MmexContentProvider;
import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.PasscodeActivity;
import com.money.manager.ex.R;
import com.money.manager.ex.about.AboutActivity;
import com.money.manager.ex.account.AccountTransactionListFragment;
import com.money.manager.ex.assetallocation.AssetAllocationOverviewActivity;
import com.money.manager.ex.assetallocation.full.FullAssetAllocationActivity;
import com.money.manager.ex.budget.BudgetsActivity;
import com.money.manager.ex.core.InfoKeys;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.database.PasswordActivity;
import com.money.manager.ex.sync.events.DbFileDownloadedEvent;
import com.money.manager.ex.home.events.AccountsTotalLoadedEvent;
import com.money.manager.ex.home.events.RequestAccountFragmentEvent;
import com.money.manager.ex.home.events.RequestOpenDatabaseEvent;
import com.money.manager.ex.home.events.RequestPortfolioFragmentEvent;
import com.money.manager.ex.home.events.RequestWatchlistFragmentEvent;
import com.money.manager.ex.home.events.UsernameLoadedEvent;
import com.money.manager.ex.investment.PortfolioFragment;
import com.money.manager.ex.servicelayer.InfoService;
import com.money.manager.ex.common.CategoryListFragment;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.currency.list.CurrencyListActivity;
import com.money.manager.ex.core.RecurringTransactionBootReceiver;
import com.money.manager.ex.core.Passcode;
import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.account.AccountListFragment;
import com.money.manager.ex.common.BaseFragmentActivity;
import com.money.manager.ex.fragment.PayeeListFragment;
import com.money.manager.ex.investment.watchlist.WatchlistFragment;
import com.money.manager.ex.notifications.RecurringTransactionNotifications;
import com.money.manager.ex.recurring.transactions.RecurringTransactionListFragment;
import com.money.manager.ex.reports.CategoriesReportActivity;
import com.money.manager.ex.reports.IncomeVsExpensesActivity;
import com.money.manager.ex.reports.PayeesReportActivity;
import com.money.manager.ex.search.SearchActivity;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.settings.SettingsActivity;
import com.money.manager.ex.settings.events.AppRestartRequiredEvent;
import com.money.manager.ex.sync.SyncConstants;
import com.money.manager.ex.sync.SyncManager;
import com.money.manager.ex.tutorial.TutorialActivity;
import com.money.manager.ex.utils.MmexDatabaseUtils;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;
import com.shamanland.fonticon.FontIconDrawable;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.net.URLDecoder;
import java.util.ArrayList;

import timber.log.Timber;

/**
 * Main activity of the application.
 */
public class MainActivity
    extends BaseFragmentActivity {

    public static final int REQUEST_PICKFILE = 1;
    public static final int REQUEST_PASSCODE = 2;
    public static final int REQUEST_PASSWORD = 3;

    public static final String EXTRA_DATABASE_PATH = "dbPath";

    /**
     * @return the mRestart
     */
    public static boolean isRestartActivitySet() {
        return mRestartActivity;
    }

    public static void setRestartActivity(boolean mRestart) {
        MainActivity.mRestartActivity = mRestart;
    }

    // private

    private static final String KEY_IS_AUTHENTICATED = "MainActivity:isAuthenticated";
    private static final String KEY_IN_AUTHENTICATION = "MainActivity:isInAuthenticated";
    private static final String KEY_CLASS_FRAGMENT_CONTENT = "MainActivity:Fragment";
    private static final String KEY_ORIENTATION = "MainActivity:Orientation";
    private static final String KEY_RECURRING_TRANSACTION = "MainActivity:RecurringTransaction";
    private static final String KEY_HAS_STARTED = "MainActivity:hasStarted";
    // state if restart activity
    private static boolean mRestartActivity = false;

    private boolean isAuthenticated = false;
    private boolean isInAuthentication = false;
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

    // sync stuff
    // The authority for the sync adapter's content provider
    public static final String AUTHORITY = "com.money.manager.ex.sync.adapter";
    // An account type, in the form of a domain name
    public static final String ACCOUNT_TYPE = "example.com";
    // The account name
    public static final String ACCOUNT = "dummyaccount";

    /**
     * Create a new dummy account for the sync adapter
     *
     * @param context The application context
     */
    public static Account CreateSyncAccount(Context context) {
        // Create the account type and default account
        Account newAccount = new Account(ACCOUNT, ACCOUNT_TYPE);
        // Get an instance of the Android account manager
        AccountManager accountManager = (AccountManager) context.getSystemService(ACCOUNT_SERVICE);
        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
        if (accountManager.addAccountExplicitly(newAccount, null, null)) {
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call context.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */
        } else {
            /*
             * The account exists or some other error occurred. Log this, report it,
             * or handle it internally.
             */
            Timber.e("can't add account for sync!");
        }
        return newAccount;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (showPrerequisite()) {
            finish();
            return;
        }

        // create the sync account
        CreateSyncAccount(this);

        // Layout

        setContentView(R.layout.main_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) setSupportActionBar(toolbar);

        LinearLayout fragmentDetail = (LinearLayout) findViewById(R.id.fragmentDetail);
        setDualPanel(fragmentDetail != null && fragmentDetail.getVisibility() == View.VISIBLE);

        // end layout

        // Restore state. Check authentication, etc.
        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState);
        }

        // Initialize the map for recent entries that link to drawer menu items.
        this.recentDbs = new RecentDatabasesProvider(this);

        // Close any existing notifications.
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(SyncConstants.NOTIFICATION_SYNC_OPEN_FILE);

        handleIntent();

        // show change log dialog
        Core core = new Core(this);
        if (core.isToDisplayChangelog()) core.showChangelog();

        MoneyManagerApplication.showCurrentDatabasePath(getApplicationContext());

        // check if we require a password.
        String dbPath = MoneyManagerApplication.getDatabasePath(this);
        if (MmexDatabaseUtils.isEncryptedDatabase(dbPath)) {
            // todo: && !MmexOpenHelper.getInstance(this).hasPassword()
            requestDatabasePassword();
        } else {
            initializeDatabaseAccess(savedInstanceState);
        }

        initializeDrawer();
    }

    @Override
    protected void onStart() {
        super.onStart();

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
        try {
            super.onResume();
        } catch (Exception e) {
            Timber.e(e, "resuming main activity");
        }

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
                Timber.e(e, "drawer sync state");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_PICKFILE:
                // data.getData() == null
                if (resultCode != RESULT_OK) return;
                if (data == null || !data.hasExtra(FilePickerActivity.RESULT_FILE_PATH)) {
                    return;
                }

                // data.getData().getPath()
                String selectedPath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
                requestDatabaseChange(selectedPath);
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

            case REQUEST_PASSWORD:
                if (resultCode == RESULT_OK && data != null) {
                    String dbPath = data.getStringExtra(EXTRA_DATABASE_PATH);
                    String password = data.getStringExtra(PasswordActivity.EXTRA_PASSWORD);

                    // Figure out what to do next. Switch the db or continue with init?
                    if (StringUtils.isEmpty(dbPath)) {
                        // todo: MmexOpenHelper.getInstance(this).setPassword(password);
                        // continue
                        initializeDatabaseAccess(null);
                    } else {
                        changeDatabase(dbPath, password);
                    }
                }
        }
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
//        outState.putBoolean(KEY_IS_SHOW_TIPS_DROPBOX2, isShowTipsDropbox2);
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
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            try {
                super.onBackPressed();
            } catch (IllegalStateException e) {
                Timber.e(e, "on back pressed");
            }
        }
    }

    // Custom methods

    public void checkCloudForDbUpdates() {
        SyncManager sync = new SyncManager(this);
        if (!sync.isActive()) {
            return;
        }

        // todo: redo this using Rx!
        AsyncTask<Void, Integer, Integer> asyncTask = new CheckCloudStorageForUpdatesTask(this);
        asyncTask.execute();
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
     *
     * @param item selected DrawerMenuItem
     * @return boolean indicating whether the action was handled or not.
     */
    //@Override
    public boolean onDrawerMenuAndOptionMenuSelected(DrawerMenuItem item) {
        boolean result = true;
        Intent intent;
        final Core core = new Core(this);
        final Boolean isDarkTheme = core.getThemeId() == R.style.Theme_Money_Manager_Dark;

        if (item.getId() == null && item.getTag() != null) {
            String key = item.getTag().toString();
            RecentDatabaseEntry recentDb = this.recentDbs.map.get(key);
            if (recentDb != null) {
                onOpenDatabaseClick(recentDb);
            }
        }
        if (item.getId() == null) return false;

        switch (item.getId()) {
            case R.id.menu_home:
                showFragment(HomeFragment.class);
                break;
            case R.id.menu_sync:
                new SyncManager(this).triggerSynchronization();
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
//                intent = new Intent(MainActivity.this, CurrencyRecyclerListActivity.class);
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
                intent = new Intent(this, FullAssetAllocationActivity.class);
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
    public void onEvent(RequestPortfolioFragmentEvent event) {
        showPortfolioFragment(event.accountId);
    }

    @Subscribe
    public void onEvent(RequestOpenDatabaseEvent event) {
        openDatabasePicker();
    }

    @Subscribe
    public void onEvent(UsernameLoadedEvent event) {
        setDrawerUserName(MoneyManagerApplication.getApp().getUserName());
    }

    @Subscribe
    public void onEvent(AccountsTotalLoadedEvent event) {
        setDrawerTotalAccounts(event.amount);
    }

    @Subscribe
    public void onEvent(AppRestartRequiredEvent event) {
        MainActivity.mRestartActivity = true;
//        restartActivity();
    }

    /**
     * A newer database file has just been downloaded. Reload.
     */
    @Subscribe
    public void onEvent(DbFileDownloadedEvent event) {
        // open the new database.
        new SyncManager(this).openDatabase();
    }

    // Private.

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
                RecurringTransactionNotifications notifications = new RecurringTransactionNotifications(this);
                notifications.notifyRepeatingTransaction();
                isRecurringTransactionStarted = true;
            }
        }

        // notification send broadcast
        Intent serviceRepeatingTransaction = new Intent(getApplicationContext(), RecurringTransactionBootReceiver.class);
        getApplicationContext().sendBroadcast(serviceRepeatingTransaction);

        if (!this.hasStarted) {
            // This is to avoid checking Dropbox on every device rotation.
            checkCloudForDbUpdates();
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

    /**
     * for the change setting restart process application
     */
    public void restartActivity() {
        if (mRestartActivity) {
            // <= 10
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
                // this is for APIs < 11.
                Intent intent = getIntent();
                overridePendingTransition(0, 0);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                // finish this activity
                finish();
                overridePendingTransition(0, 0);
                // restart
                startActivity(intent);
                // kill process
//                android.os.Process.killProcess(android.os.Process.myPid());
            } else {
//                // New api. This will keep the Intent, which hangs after Dropbox update.
//                this.recreate();

                finish();
                startMainActivity();
            }
        }

        setRestartActivity(false);
    }

    private void resetContentProvider() {
        ContentResolver resolver = this.getContentResolver();
        String authority = this.getApplicationContext().getPackageName() + ".provider";
        ContentProviderClient client = resolver.acquireContentProviderClient(authority);
        MmexContentProvider provider = (MmexContentProvider) client.getLocalContentProvider();
        provider.resetDatabase();
//        client.release();
    }

//    private void shutdownApp() {
//        finish();
//        android.os.Process.killProcess(android.os.Process.myPid());
//        System.exit(1);
//    }

//    private void shutdownWithPrompt() {
//        new MaterialDialog.Builder(this)
//                .content(R.string.app_restart)
//                .positiveText(android.R.string.ok)
//                .onPositive(new MaterialDialog.SingleButtonCallback() {
//                    @Override
//                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
//                        shutdownApp();
//                    }
//                })
//                .neutralText(android.R.string.cancel)
//                .show();
//    }

    private void startMainActivity() {
        // Don't reuse the same Intent. It loops when called after Dropbox download.

        Context baseContext = getBaseContext();
        Intent intent = baseContext.getPackageManager()
                .getLaunchIntentForPackage(baseContext.getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

        // option 2
//        Intent intent = getIntent();
//        finish();
//        startActivity(intent);
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
                    Timber.e(e, "creating new fragment");
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
     */
    public void showFragment(Fragment fragment) {
        showFragment(fragment, fragment.getClass().getName());
    }

    /**
     * Displays the fragment and associate the tag
     *
     * @param fragment    Fragment to display
     * @param tagFragment Tag/name to search for.
     */
    public void showFragment(Fragment fragment, String tagFragment) {
        try {
            showFragment_Internal(fragment, tagFragment);
        } catch (Exception e) {
            Timber.e(e, "showing fragment with tag");
        }
    }

    /**
     * Shows a fragment with the selected account (id) and transactions.
     * Called from Home Fragment when an account is clicked in the main list.
     *
     * @param accountId id of the account for which to show the transactions
     */
    public void showAccountFragment(int accountId) {
        String tag = AccountTransactionListFragment.class.getSimpleName() + "_" + Integer.toString(accountId);
        AccountTransactionListFragment fragment = (AccountTransactionListFragment) getSupportFragmentManager().findFragmentByTag(tag);
        if (fragment == null || fragment.getId() != getResIdLayoutContent()) {
            fragment = AccountTransactionListFragment.newInstance(accountId);
        }
        showFragment(fragment, tag);
    }

    public void showPortfolioFragment(int accountId) {
        String tag = PortfolioFragment.class.getSimpleName() + "_" + Integer.toString(accountId);
        PortfolioFragment fragment = (PortfolioFragment) getSupportFragmentManager().findFragmentByTag(tag);
        if (fragment == null) {
            fragment = PortfolioFragment.newInstance(accountId);
        }
        showFragment(fragment, tag);
    }

    public void showWatchlistFragment(int accountId) {
        String tag = WatchlistFragment.class.getSimpleName() + "_" + Integer.toString(accountId);
        WatchlistFragment fragment = (WatchlistFragment) getSupportFragmentManager().findFragmentByTag(tag);
        if (fragment == null || fragment.getId() != getResIdLayoutContent()) {
            fragment = WatchlistFragment.newInstance(accountId);
        }
        showFragment(fragment, tag);
    }

    /**
     * Show tutorial activity.
     */
    public void showTutorial() {
        Intent intent = new Intent(this, TutorialActivity.class);
        // make top-level so there's no going back.
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        // Tutorial is marked as seen when OK on the last page is clicked.

        // Close this activity. A new one will start from Tutorial.
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

    public void onDrawerItemSubDialogs(final DrawerMenuItemAdapter adapter, CharSequence title) {
        final MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title(title)
                .adapter(adapter, new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                        onDrawerMenuAndOptionMenuSelected(adapter.getItem(which));
                        dialog.dismiss();
                    }
                })
                .build();

//        ListView listView = dialog.getListView();
//        if (listView != null) {
//            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                @Override
//                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                    onDrawerMenuAndOptionMenuSelected(adapter.getItem(position));
//                    dialog.dismiss();
//                }
//            });
//        }

        dialog.show();
    }

    public void openDatabasePicker() {
        //pickFile(Environment.getDefaultDatabaseDirectory());
        MmexDatabaseUtils dbUtils = new MmexDatabaseUtils(this);
        String dbDirectory = dbUtils.getDefaultDatabaseDirectory();

        // Environment.getDefaultDatabaseDirectory().getPath()
        try {
            UIHelper uiHelper = new UIHelper(this);
            uiHelper.pickFileInternal(dbDirectory, REQUEST_PICKFILE);
        } catch (Exception e) {
            Timber.e(e, "displaying the open-database picker");
        }
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

        // Synchronization
        if (new SyncManager(this).isActive()) {
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
                // if the group has child items, do not e.
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
        try {
            Core core = new Core(getApplicationContext());
            core.changeDatabase(dbFilePath, password);
        } catch (Exception e) {
            //if (e instanceof )
            Timber.e(e, "changing the database");
            return;
        }

        // Store the name into the recent files list.
        if (!this.recentDbs.contains(dbFilePath)) {
            this.recentDbs.add(RecentDatabaseEntry.fromPath(dbFilePath));
        }

        resetContentProvider();

//        if (currentDatabase.contentEquals(dbFilePath)) {
//            // just restart the main Activity?
            setRestartActivity(true);
            restartActivity();
//        } else {
//            // db changed, restart the app.
//            shutdownWithPrompt();
//        }
    }

//    private void displayDefaultFragment() {
//        // show main navigation fragment
//        final String homeFragmentTag = HomeFragment.class.getSimpleName();
//        HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager()
//                .findFragmentByTag(homeFragmentTag);
//
//        if (homeFragment == null) {
//            // fragment create
//            homeFragment = new HomeFragment();
//        }
//
//        final HomeFragment finalFragment = homeFragment;
//        // ref: http://stackoverflow.com/a/14178962/202166
//        new Handler().post(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    getSupportFragmentManager().beginTransaction()
//                            .replace(R.id.fragmentContent, finalFragment, homeFragmentTag)
//                            .commit();
//                } catch (Exception e) {
//                    ExceptionHandler handler = new ExceptionHandler(MainActivity.this, MainActivity.this);
//                    handler.e(e, "showing initial fragments");
//                }
//            }
//        });
//    }

    private Drawable getDrawableFromResource(int resourceId) {
        Drawable icon;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            icon = getDrawable(resourceId);
        } else {
            icon = getResources().getDrawable(resourceId);
        }
        return icon;
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

        // Cloud synchronize
        if (new SyncManager(this).isActive()) {
            menuItems.add(new DrawerMenuItem().withId(R.id.menu_sync)
                    .withText(getString(R.string.synchronize))
                    .withIconDrawable(FontIconDrawable.inflate(this, R.xml.ic_sync)));
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

                if (entry.linkedToCloud) {
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
        Intent intent = getIntent();
        if (intent == null || intent.getData() == null) return;

        String pathFile = getIntent().getData().getEncodedPath();
        // decode
        try {
            pathFile = URLDecoder.decode(pathFile, "UTF-8"); // decode file path
            Timber.d("Path intent file to open: %s", pathFile);
            // Open this database.
            requestDatabaseChange(pathFile);
        } catch (Exception e) {
            Timber.e(e, "opening database from intent");
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

    private boolean isDatabaseAvailable() {
        // Do we have a database set?
        String dbPath = new AppSettings(this).getDatabaseSettings().getDatabasePath();
        if (StringUtils.isEmpty(dbPath)) return false;

        // Does the database file exist?
        File dbFile = new File(dbPath);
        return dbFile.exists();
    }

    private boolean needTutorial() {
        return new AppSettings(this).getBehaviourSettings().getShowTutorial();
    }

    /**
     * called when quick-switching the recent databases from the navigation menu.
     *
     * @param recentDb selected recent database entry
     */
    private void onOpenDatabaseClick(RecentDatabaseEntry recentDb) {
        // do nothing if selecting the currently open database
        String currentDb = MoneyManagerApplication.getDatabasePath(this);
        if (recentDb.filePath.equals(currentDb)) return;

        // set the remote file path, if any.
        String remotePath = recentDb.linkedToCloud
                ? recentDb.remoteFileName
                : "";
        new SyncManager(this).setRemotePath(remotePath);

        requestDatabaseChange(recentDb.filePath);
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
                    Timber.e(e, "showing fragment %s", className);
                }
            }
        }
    }

//    /**
//     * Pick the database file to use with any registered provider in the user's system.
//     * @param startFolder start folder
//     */
//    private void pickFile(File startFolder) {
//        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//        intent.setDataAndType(Uri.fromFile(startFolder), "vnd.android.cursor.dir/*");
//        intent.setType("file/*");
//
//        if (MoneyManagerApplication.getApp().isUriAvailable(this, intent)) {
//            try {
//                startActivityForResult(intent, REQUEST_PICKFILE);
//            } catch (Exception e) {
//                Timber.e(e, "selecting a database file");
//            }
//        } else {
//            Toast.makeText(this, R.string.error_intent_pick_file,
//                    Toast.LENGTH_LONG).show();
//        }
//
//        // Note that the selected file is handled in onActivityResult.
//    }

    /**
     * Change the database.
     *
     * @param dbFilePath The path to the database file.
     */
    private void requestDatabaseChange(String dbFilePath) {
        Timber.v("Changing database to: %s", dbFilePath);

        // e encrypted database(s)
        if (MmexDatabaseUtils.isEncryptedDatabase(dbFilePath)) {
            requestDatabasePassword(dbFilePath);
        } else {
            changeDatabase(dbFilePath, null);
        }
    }

    private void requestDatabasePassword() {
        // request password for the current database.
        requestDatabasePassword(null);
    }

    private void requestDatabasePassword(String dbFilePath) {
        // request password
        Intent intent = new Intent(this, PasswordActivity.class);
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

        if (savedInstanceState.containsKey(KEY_HAS_STARTED)) {
            this.hasStarted = savedInstanceState.getBoolean(KEY_HAS_STARTED);
        }
    }

    private void showSelectDatabaseActivity() {
        Intent intent = new Intent(this, SelectDatabaseActivity.class);
        // make top-level so there's no going back.
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
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

    /**
     * display any screens that need to be shown before the app actually runs.
     * This usually happens only on the first run of the app.
     * @return Indicator if another screen is showing. This means that this activity should close
     * and not proceed with initialisation.
     */
    private boolean showPrerequisite() {
        // show tutorial on the first run
        if (needTutorial()) {
            showTutorial();
            return true;
        }

        // show database chooser if no valid database
        if (!isDatabaseAvailable()) {
            showSelectDatabaseActivity();
            return true;
        }

        return false;
    }

    private void showReportsSelector(boolean isDarkTheme, String text) {
        DrawerMenuItemAdapter adapter = new DrawerMenuItemAdapter(this);
        int iconId;

        // payee
        iconId = isDarkTheme ? R.drawable.ic_action_pie_chart_dark : R.drawable.ic_action_pie_chart_light;
        adapter.add(new DrawerMenuItem().withId(R.id.menu_report_payees)
                .withText(getString(R.string.payees))
                .withIconDrawable(getDrawableFromResource(iconId)));

        // where money goes
        iconId = isDarkTheme ? R.drawable.ic_action_pie_chart_dark : R.drawable.ic_action_pie_chart_light;
        adapter.add(new DrawerMenuItem().withId(R.id.menu_report_where_money_goes)
                .withText(getString(R.string.menu_report_where_money_goes))
                .withIconDrawable(getDrawableFromResource(iconId)));

        // where money comes from
        iconId = isDarkTheme ? R.drawable.ic_action_pie_chart_dark : R.drawable.ic_action_pie_chart_light;
        adapter.add(new DrawerMenuItem().withId(R.id.menu_report_where_money_comes_from)
                .withText(getString(R.string.menu_report_where_money_comes_from))
                .withIconDrawable(getDrawableFromResource(iconId)));

        // where money comes from
        iconId = isDarkTheme ? R.drawable.ic_action_pie_chart_dark : R.drawable.ic_action_pie_chart_light;
        adapter.add(new DrawerMenuItem().withId(R.id.menu_report_categories)
                .withText(getString(R.string.categories))
                .withIconDrawable(getDrawableFromResource(iconId)));

        // income vs. expenses
        iconId = isDarkTheme ? R.drawable.ic_action_bargraph_dark : R.drawable.ic_action_bargraph_light;
        adapter.add(new DrawerMenuItem().withId(R.id.menu_report_income_vs_expenses)
                .withText(getString(R.string.menu_report_income_vs_expenses))
                .withIconDrawable(getDrawableFromResource(iconId)));

        // Asset Allocation Overview
        adapter.add(new DrawerMenuItem().withId(R.id.menu_asset_allocation_overview)
                .withText(getString(R.string.asset_allocation))
                .withIconDrawable(FontIconDrawable.inflate(this, R.xml.ic_pie_chart)));

        onDrawerItemSubDialogs(adapter, text);
    }

}
