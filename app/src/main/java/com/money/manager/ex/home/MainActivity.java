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
package com.money.manager.ex.home;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.mmex_icon_font_typeface_library.MMXIconFont;
import com.money.manager.ex.Constants;
import com.money.manager.ex.DonateActivity;
import com.money.manager.ex.HelpActivity;
import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.PasscodeActivity;
import com.money.manager.ex.R;
import com.money.manager.ex.about.AboutActivity;
import com.money.manager.ex.account.AccountListFragment;
import com.money.manager.ex.account.AccountTransactionListFragment;
import com.money.manager.ex.assetallocation.AssetAllocationReportActivity;
import com.money.manager.ex.assetallocation.overview.AssetAllocationOverviewActivity;
import com.money.manager.ex.budget.BudgetsActivity;
import com.money.manager.ex.common.CategoryListFragment;
import com.money.manager.ex.common.MmxBaseFragmentActivity;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.database.DatabaseManager;
import com.money.manager.ex.core.docstorage.FileStorageHelper;
import com.money.manager.ex.core.InfoKeys;
import com.money.manager.ex.core.IntentFactory;
import com.money.manager.ex.core.Passcode;
import com.money.manager.ex.core.RecurringTransactionBootReceiver;
import com.money.manager.ex.core.RequestCodes;
import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.currency.list.CurrencyListActivity;
import com.money.manager.ex.fragment.PayeeListFragment;
import com.money.manager.ex.home.events.AccountsTotalLoadedEvent;
import com.money.manager.ex.home.events.RequestAccountFragmentEvent;
import com.money.manager.ex.home.events.RequestOpenDatabaseEvent;
import com.money.manager.ex.home.events.RequestPortfolioFragmentEvent;
import com.money.manager.ex.home.events.RequestWatchlistFragmentEvent;
import com.money.manager.ex.home.events.UsernameLoadedEvent;
import com.money.manager.ex.investment.PortfolioFragment;
import com.money.manager.ex.investment.watchlist.WatchlistFragment;
import com.money.manager.ex.notifications.RecurringTransactionNotifications;
import com.money.manager.ex.recurring.transactions.RecurringTransactionListFragment;
import com.money.manager.ex.reports.CategoriesReportActivity;
import com.money.manager.ex.reports.IncomeVsExpensesActivity;
import com.money.manager.ex.reports.PayeesReportActivity;
import com.money.manager.ex.search.SearchActivity;
import com.money.manager.ex.servicelayer.InfoService;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.settings.PreferenceConstants;
import com.money.manager.ex.settings.SettingsActivity;
import com.money.manager.ex.settings.SyncPreferences;
import com.money.manager.ex.sync.SyncConstants;
import com.money.manager.ex.sync.SyncManager;
import com.money.manager.ex.sync.events.DbFileDownloadedEvent;
import com.money.manager.ex.sync.events.SyncStartingEvent;
import com.money.manager.ex.sync.events.SyncStoppingEvent;
import com.money.manager.ex.tutorial.TutorialActivity;
import com.money.manager.ex.utils.MmxDatabaseUtils;

import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import dagger.Lazy;
import icepick.State;
import rx.Single;
import rx.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

/**
 * Main activity of the application.
 */
public class MainActivity
    extends MmxBaseFragmentActivity {

    public static final String EXTRA_DATABASE_PATH = "dbPath";
    public static final String EXTRA_SKIP_REMOTE_CHECK = "skipRemoteCheck";

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

    private static final String KEY_IN_AUTHENTICATION = "MainActivity:isInAuthenticated";
    private static final String KEY_RECURRING_TRANSACTION = "MainActivity:RecurringTransaction";

    // state if restart activity
    private static boolean mRestartActivity = false;

    @Inject Lazy<RecentDatabasesProvider> mDatabases;

    @State boolean dbUpdateCheckDone = false;
    @State boolean mIsSynchronizing = false;
    @State boolean isAuthenticated = false;
    @State int deviceOrientation = Constants.NOT_SET;

    private boolean isInAuthentication = false;
    private boolean isRecurringTransactionStarted = false;
    // navigation drawer
    private LinearLayout mDrawerLayout;
    private DrawerLayout mDrawer;
    private MyActionBarDrawerToggle mDrawerToggle;
    private TextView mDrawerTextUserName;
    private TextView mDrawerTextTotalAccounts;
    // state dual panel
    private boolean mIsDualPanel = false;
    // sync rotating icon
    private MenuItem mSyncMenuItem = null;
    private UIHelper mUiHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MmexApplication.getApp().iocComponent.inject(this);

        if (showPrerequisite()) {
            finish();
            return;
        }

        // todo: remove this after the users upgrade the recent files list.
        migrateRecentDatabases();

        // Reset the request for restart. If we are in onCreate, we are restarting already.
        setRestartActivity(false);

        // Layout
        setContentView(R.layout.main_activity);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) setSupportActionBar(toolbar);

        LinearLayout fragmentDetail = findViewById(R.id.fragmentDetail);
        setDualPanel(fragmentDetail != null && fragmentDetail.getVisibility() == View.VISIBLE);

        // Initialize current device orientation.
        if (deviceOrientation == Constants.NOT_SET) {
            deviceOrientation = getResources().getConfiguration().orientation;
        }

        // Intent. Opening from the notification or the file system.
        handleIntent();

        // Restore state. Check authentication, etc.
        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState);
        }

        handleDeviceRotation();

        // Close any existing notifications.
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
                .cancel(SyncConstants.NOTIFICATION_SYNC_OPEN_FILE);

        showCurrentDatabasePath(this);

        // Read something from the database at this stage so that the db file gets created.
        InfoService infoService = new InfoService(this);
        String username = infoService.getInfoValue(InfoKeys.USERNAME);

        // fragments
        initHomeFragment();

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

        initializeDrawer();

        initializeSync();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // check if has pass-code and authenticate
        if (!isAuthenticated) {
            Passcode passcode = new Passcode(getApplicationContext());
            if (passcode.hasPasscode() && !isInAuthentication) {
                Intent intent = new Intent(this, PasscodeActivity.class);
                // set action and data
                intent.setAction(PasscodeActivity.INTENT_REQUEST_PASSWORD);
                intent.putExtra(PasscodeActivity.INTENT_MESSAGE_TEXT, getString(R.string.enter_your_passcode));
                // start activity
                startActivityForResult(intent, RequestCodes.PASSCODE);
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

        if (resultCode != RESULT_OK) return;

        switch (requestCode) {
//            case RequestCodes.SELECT_FILE:
//                // Opening from intent.
//                if (resultCode != RESULT_OK) return;
//
//                String selectedPath = UIHelper.getSelectedFile(data);
//                if(TextUtils.isEmpty(selectedPath)) {
//                    new UIHelper(this).showToast(R.string.invalid_database);
//                    return;
//                }
//
//                DatabaseMetadata db = DatabaseMetadataFactory.getInstance(selectedPath);
//                changeDatabase(db);
//                break;

            case RequestCodes.SELECT_DOCUMENT:
                FileStorageHelper storageHelper = new FileStorageHelper(this);
                DatabaseMetadata db = storageHelper.selectDatabase(data);
                changeDatabase(db);
                break;

            case RequestCodes.PASSCODE:
                isAuthenticated = false;
                isInAuthentication = false;
                if (resultCode == RESULT_OK && data != null) {
                    String passIntent = data.getStringExtra(PasscodeActivity.INTENT_RESULT_PASSCODE);
                    if (!passIntent.equals("FingerprintAuthenticationSuccess")) {
                        Passcode passcode = new Passcode(getApplicationContext());
                        String passDb = passcode.getPasscode();

                        if (passIntent != null && passDb != null) {
                            isAuthenticated = passIntent.equals(passDb);
                            if (!isAuthenticated) {
                                Toast.makeText(getApplicationContext(), R.string.passocde_no_macth, Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                    else {
                        isAuthenticated = true;
                    }
                }
                // close if not authenticated
                if (!isAuthenticated) {
                    this.finish();
                }
                break;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setRestartActivity(true);
    }

    // Menu

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        if (mIsSynchronizing) {
            createSyncToolbarItem(menu);
            startSyncIconRotation(mSyncMenuItem);
        } else {
            stopSyncIconRotation(mSyncMenuItem);
            destroySyncToolbarItem(menu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                // toggle drawer with the menu hardware button.
                if (mDrawer != null) {
                    if (mDrawer.isDrawerOpen(mDrawerLayout)) {
                        mDrawer.closeDrawer(mDrawerLayout);
                    } else {
                        mDrawer.openDrawer(mDrawerLayout);
                    }
                }
                return true;

            default:
                // nothing
                break;
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
        outState.putBoolean(KEY_IN_AUTHENTICATION, isInAuthentication);
        outState.putBoolean(KEY_RECURRING_TRANSACTION, isRecurringTransactionStarted);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null)
            notificationManager.cancelAll();
        super.onDestroy();

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
        FileStorageHelper helper = new FileStorageHelper(this);
        helper.showStorageFilePicker();
    }

    @Subscribe
    public void onEvent(UsernameLoadedEvent event) {
        setDrawerUserName(MmexApplication.getApp().getUserName());
    }

    @Subscribe
    public void onEvent(AccountsTotalLoadedEvent event) {
        setDrawerTotalAccounts(event.amount);
    }

    /**
     * Force execution on the main thread as the event can be received on the service thread.
     * @param event Sync started event.
     */
    @Subscribe
    public void onEvent(SyncStartingEvent event) {
        Single.fromCallable(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                mIsSynchronizing = true;
                invalidateOptionsMenu();
                return null;
            }
        })
                .subscribeOn(AndroidSchedulers.mainThread())
//                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    /**
     * Force execution on the main thread as the event can be received on the service thread.
     * @param event Sync stopped event.
     */
    @Subscribe
    public void onEvent(SyncStoppingEvent event) {
        Single.fromCallable(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                mIsSynchronizing = false;
                invalidateOptionsMenu();
                return null;
            }
        })
            .subscribeOn(AndroidSchedulers.mainThread())
//            .observeOn(AndroidSchedulers.mainThread())
            .subscribe();
    }

    /**
     * A newer database file has just been downloaded. Reload.
     */
    @Subscribe
    public void onEvent(DbFileDownloadedEvent event) {
        // open the new database.
        new SyncManager(this).useDownloadedDatabase();
    }

    /*
        Custom methods
     */

    public void changeDatabase(@NonNull DatabaseMetadata database) {
        // Reuse existing metadata, if found.
        DatabaseMetadata existing = mDatabases.get().get(database.localPath);
        if (existing != null) {
            Timber.v("Existing database found. Reusing metadata.");
            database = existing;
        }

        try {
            new MmxDatabaseUtils(this).useDatabase(database);
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                Timber.w(e.getMessage());
            } else {
                Timber.e(e, "changing the database");
            }
            return;
        }

        // Refresh the recent files list.
        getDatabases().load();

        setRestartActivity(true);
        restartActivity();
    }

    /**
     * @return the mIsDualPanel
     */
    public boolean isDualPanel() {
        return mIsDualPanel;
    }

    public int getContentId() {
        return isDualPanel()
                ? R.id.fragmentDetail
                : R.id.fragmentMain;
    }

    public int getNavigationId() {
        return isDualPanel()
                ? R.id.fragmentMain
                : R.id.fragmentDetail;
    }

    /**
     * Handle the drawer item click. Invoked by the actual click handler.
     * @param item selected DrawerMenuItem
     * @return boolean indicating whether the action was handled or not.
     */
    public boolean onDrawerMenuAndOptionMenuSelected(DrawerMenuItem item) {
        boolean result = true;
        Intent intent;

        // Recent database?
        if (item.getId() == null && item.getTag() != null) {
            String key = item.getTag().toString();
            DatabaseMetadata selectedDatabase = getDatabases().get(key);
            if (selectedDatabase != null) {
                onOpenDatabaseClick(selectedDatabase);
            }
        }
        if (item.getId() == null) return false;

        switch (item.getId()) {
            case R.id.menu_home:
                showFragment(HomeFragment.class);
                break;
            case R.id.menu_sync:
//                SyncManager sync = new SyncManager(this);
//                sync.triggerSynchronization();
//                // re-set the sync timer.
//                sync.startSyncServiceHeartbeat();

                // Synchronize with the storage provider.
                FileStorageHelper storage = new FileStorageHelper(this);
                DatabaseMetadata current = mDatabases.get().getCurrent();
                storage.synchronize(current);
                break;

            case R.id.menu_open_database:
                FileStorageHelper helper = new FileStorageHelper(this);
                helper.showStorageFilePicker();
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
                intent = new Intent(this, AssetAllocationOverviewActivity.class);
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
                showReportsSelector(item.getText());
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
                startActivity(new Intent(this, AssetAllocationReportActivity.class));
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

    /**
     * for the change setting restart process application
     */
    public void restartActivity() {
        if (!mRestartActivity) return;

        setRestartActivity(false);

        // kill process
//                android.os.Process.killProcess(android.os.Process.myPid());
        // New api. This will keep the Intent, which hangs after the db download!
//                this.recreate();

        Intent intent = IntentFactory.getMainActivityNew(this);
        startActivity(intent);

        finish();
    }

//    private void shutdownApp() {
//        finish();
//        android.os.Process.killProcess(android.os.Process.myPid());
//        System.exit(1);
//    }

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
        if (fragment == null || fragment.getId() != getContentId()) {
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
     * @param tag Tag/name to search for.
     */
    public void showFragment(Fragment fragment, String tag) {
        try {
            showFragment_Internal(fragment, tag);
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
        if (fragment == null || fragment.getId() != getContentId()) {
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
        if (fragment == null || fragment.getId() != getContentId()) {
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

    /*
        Private
     */

    private void createExpandableDrawer() {
        UIHelper uiHelper = new UIHelper(this);
        int iconColor = uiHelper.getSecondaryTextColor();

        // Menu.

        final ArrayList<DrawerMenuItem> groupItems = getDrawerMenuItems();
        final ArrayList<Object> childItems = new ArrayList<>();

        // Home
        childItems.add(null);

        // Open Database. Display the recent db list.
        ArrayList<DrawerMenuItem> childDatabases = getRecentDatabasesDrawerMenuItems();
        childItems.add(childDatabases);

        // Synchronization
//        if (new SyncManager(this).isActive()) {
//            childItems.add(null);
//        }
        childItems.add(null);

        // Entities
        ArrayList<DrawerMenuItem> childTools = new ArrayList<>();
        // manage: account
        childTools.add(new DrawerMenuItem().withId(R.id.menu_account)
                .withText(getString(R.string.accounts))
                .withIconDrawable(uiHelper.getIcon(MMXIconFont.Icon.mmx_temple)
                        .color(iconColor)));
        // manage: categories
        childTools.add(new DrawerMenuItem().withId(R.id.menu_category)
                .withText(getString(R.string.categories))
                .withIconDrawable(uiHelper.getIcon(MMXIconFont.Icon.mmx_tag_empty)
                        .color(iconColor)));
        // manage: currencies
        childTools.add(new DrawerMenuItem().withId(R.id.menu_currency)
                .withText(getString(R.string.currencies))
                .withIconDrawable(uiHelper.getIcon(GoogleMaterial.Icon.gmd_euro_symbol)
                    .color(iconColor)));
        // manage: payees
        childTools.add(new DrawerMenuItem().withId(R.id.menu_payee)
                .withText(getString(R.string.payees))
                .withIconDrawable(uiHelper.getIcon(GoogleMaterial.Icon.gmd_group)
                        .color(iconColor)));
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
        final ExpandableListView drawerList = findViewById(R.id.drawerExpandableList);
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

    private void createSyncToolbarItem(Menu menu) {
        if (menu == null) return;

        int id = R.id.menuSyncProgress;

        // We will use the sync button for uploading the database to the storage.
        //if (new SyncManager(this).isActive()) {
            // add rotating icon
            if (menu.findItem(id) == null) {
                boolean hasAnimation = false;

                if (mSyncMenuItem != null && mSyncMenuItem.getActionView() != null) {
                    hasAnimation = true;
                    // There is a running animation. Clear it on the old reference.
                    stopSyncIconRotation(mSyncMenuItem);
                }

                // create (new) menu item.
                MenuInflater inflater = getMenuInflater();
                inflater.inflate(R.menu.menu_item_sync_progress, menu);
                mSyncMenuItem = menu.findItem(id);
                UIHelper ui = new UIHelper(this);
                Drawable syncIcon = ui.getIcon(GoogleMaterial.Icon.gmd_cached);
                mSyncMenuItem.setIcon(syncIcon);

                if (hasAnimation) {
                    // continue animation.
                    startSyncIconRotation(mSyncMenuItem);
                }
            }
//        } else {
//            if (mSyncMenuItem != null) {
//                stopSyncIconRotation(mSyncMenuItem);
//                mSyncMenuItem = null;
//            }
//        }
    }

    private void destroySyncToolbarItem(Menu menu) {
        if (menu == null) return;

        int id = R.id.menuSyncProgress;

        if (menu.findItem(id) != null) {
            menu.removeItem(id);
        }

        if (mSyncMenuItem != null) {
            stopSyncIconRotation(mSyncMenuItem);
            mSyncMenuItem = null;
        }
    }

    private RecentDatabasesProvider getDatabases() {
        return mDatabases.get();
    }

    private ArrayList<DrawerMenuItem> getDrawerMenuItems() {
        ArrayList<DrawerMenuItem> menuItems = new ArrayList<>();
        UIHelper uiHelper = new UIHelper(this);
        int iconColor = uiHelper.getSecondaryTextColor();

        // Home
        menuItems.add(new DrawerMenuItem().withId(R.id.menu_home)
                .withText(getString(R.string.home))
                .withIconDrawable(uiHelper.getIcon(GoogleMaterial.Icon.gmd_home)
                        .color(iconColor)));

        // Open database
        menuItems.add(new DrawerMenuItem().withId(R.id.menu_open_database)
                .withText(getString(R.string.open_database))
                .withIconDrawable(uiHelper.getIcon(GoogleMaterial.Icon.gmd_folder_open)
                        .color(iconColor)));

        // Cloud synchronize
//        if (new SyncManager(this).isActive()) {
            menuItems.add(new DrawerMenuItem().withId(R.id.menu_sync)
                .withText(getString(R.string.synchronize))
                .withIconDrawable(uiHelper.getIcon(GoogleMaterial.Icon.gmd_cached)
                        .color(iconColor)));
//        }

        // Entities
        menuItems.add(new DrawerMenuItem().withId(R.id.menu_group_main)
                .withText(getString(R.string.entities))
                .withIconDrawable(uiHelper.getIcon(GoogleMaterial.Icon.gmd_business)
                        .color(iconColor)));

        // Recurring Transactions
        menuItems.add(new DrawerMenuItem().withId(R.id.menu_recurring_transaction)
                .withText(getString(R.string.recurring_transactions))
                .withIconDrawable(uiHelper.getIcon(MMXIconFont.Icon.mmx_back_in_time)
                        .color(iconColor)));

        // Budgets
        menuItems.add(new DrawerMenuItem().withId(R.id.menu_budgets)
                .withText(getString(R.string.budgets))
                .withIconDrawable(uiHelper.getIcon(MMXIconFont.Icon.mmx_law)
                        .color(iconColor)));

        // Asset Allocation
        menuItems.add(new DrawerMenuItem().withId(R.id.menu_asset_allocation)
                .withText(getString(R.string.asset_allocation))
                .withIconDrawable(uiHelper.getIcon(MMXIconFont.Icon.mmx_chart_pie)
                        .color(iconColor)));

        // Search transaction
        menuItems.add(new DrawerMenuItem().withId(R.id.menu_search_transaction)
                .withText(getString(R.string.search))
                .withIconDrawable(uiHelper.getIcon(MMXIconFont.Icon.mmx_magnifier)
                        .color(iconColor)));
        // reports
        menuItems.add(new DrawerMenuItem().withId(R.id.menu_reports)
                .withText(getString(R.string.menu_reports))
                .withIconDrawable(uiHelper.getIcon(GoogleMaterial.Icon.gmd_equalizer)
                        .color(iconColor))
                .withDivider(true));
        // Settings
        menuItems.add(new DrawerMenuItem().withId(R.id.menu_settings)
                .withText(getString(R.string.settings))
                .withIconDrawable(uiHelper.getIcon(GoogleMaterial.Icon.gmd_settings)
                        .color(iconColor)));
        // Donate
        menuItems.add(new DrawerMenuItem().withId(R.id.menu_donate)
                .withText(getString(R.string.donate))
                .withIconDrawable(uiHelper.getIcon(GoogleMaterial.Icon.gmd_card_giftcard)
                        .color(iconColor))
                .withDivider(Boolean.TRUE));
        // Help
        menuItems.add(new DrawerMenuItem().withId(R.id.menu_about)
                .withText(getString(R.string.about))
//                .withIconDrawable(uiHelper.getIcon(MMXIconFont.Icon.mmx_question)))
            .withIconDrawable(uiHelper.getIcon(GoogleMaterial.Icon.gmd_help_outline)
                    .color(iconColor)));

        return menuItems;
    }

    private ArrayList<DrawerMenuItem> getRecentDatabasesDrawerMenuItems() {
        UIHelper ui = new UIHelper(this);
        int iconColor = ui.getSecondaryTextColor();
        ArrayList<DrawerMenuItem> childDatabases = new ArrayList<>();
        RecentDatabasesProvider databases = getDatabases();

        if (databases.count() > 0) {
            for (DatabaseMetadata entry : databases.map.values()) {
                String title = entry.getFileName();

                DrawerMenuItem item = new DrawerMenuItem().withText(title);
                item.setTag(entry.localPath);

                if (entry.isSynchronised()) {
                    item.withIconDrawable(ui.getIcon(GoogleMaterial.Icon.gmd_cloud)
                        .color(iconColor));
                } else {
                    item.withIconDrawable(ui.getIcon(MMXIconFont.Icon.mmx_floppy_disk)
                        .color(iconColor));
                }
                childDatabases.add(item);
            }
        }

        // Menu item 'Other'. Simply open the file picker, as before.
        DrawerMenuItem item = new DrawerMenuItem()
                .withId(R.id.menu_open_database)
                .withIconDrawable(getUiHelper().getIcon(GoogleMaterial.Icon.gmd_folder_shared)
                    .color(iconColor))
                .withText(getString(R.string.other));
        childDatabases.add(item);

        return childDatabases;
    }

    private UIHelper getUiHelper() {
        if (mUiHelper == null) {
            mUiHelper = new UIHelper(this);
        }
        return mUiHelper;
    }

    private void handleDeviceRotation() {
        // Remove items from back stack on device rotation.
        int currentOrientation = getResources().getConfiguration().orientation;
        boolean isTablet = new Core(this).isTablet();

        if (isTablet && deviceOrientation != currentOrientation) {
            for (int i = 0; i < getSupportFragmentManager().getBackStackEntryCount(); i++) {
                getSupportFragmentManager().popBackStack();
            }
        }

        // update the current orientation.
        deviceOrientation = currentOrientation;
    }

    private void handleIntent() {
        Intent intent = getIntent();
        if (intent == null) return;

        // Open a db file
        if (intent.getData() != null) {
            String pathFile = getIntent().getData().getEncodedPath();
            // decode
            try {
                String filePath = URLDecoder.decode(pathFile, "UTF-8"); // decode file path
                Timber.d("Path intent file to open: %s", filePath);

                // Open this database.
                DatabaseMetadata db = DatabaseMetadataFactory.getInstance(filePath);
                changeDatabase(db);
                return;
            } catch (Exception e) {
                Timber.e(e, "opening database from intent");
            }
        }

        boolean skipRemoteCheck = intent.getBooleanExtra(EXTRA_SKIP_REMOTE_CHECK, false);
        this.dbUpdateCheckDone = skipRemoteCheck;
    }

    private void initializeSync() {
        SyncManager sync = new SyncManager(this);
        if (!sync.isActive()) return;

        SyncPreferences preferences = new SyncPreferences(this);

        // Start the sync timer in case it was stopped for whatever reason.
        if (preferences.getSyncInterval() != 0) {
            sync.startSyncServiceHeartbeat();
        }

        // Check cloud storage for updates?
        boolean syncOnStart = preferences.get(R.string.pref_sync_on_app_start, true);
        if (syncOnStart && !this.dbUpdateCheckDone) {
            sync.triggerSynchronization();

            // This is to avoid checking for online updates on every device rotation.
            dbUpdateCheckDone = true;
        }
    }

    private void initializeDrawer() {
        // navigation drawer
        mDrawer = findViewById(R.id.drawerLayout);

        // set a custom shadow that overlays the main content when the drawer opens
        if (mDrawer == null) return;

        mDrawerToggle = new MyActionBarDrawerToggle(this, mDrawer, R.string.open, R.string.closed);
        mDrawer.addDrawerListener(mDrawerToggle);

        // create drawer menu
        initializeDrawerVariables();
        createExpandableDrawer();

        // enable ActionBar app icon to behave as action to toggle nav drawer
        setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
    }

    private void initHomeFragment() {
        String tag = HomeFragment.class.getSimpleName();

        // See if the fragment is already there.
        Fragment existingFragment = getSupportFragmentManager().findFragmentByTag(tag);
        if (existingFragment != null) return;

        // Create new Home fragment.
        HomeFragment fragment = new HomeFragment();

        int containerId = isDualPanel() ? getNavigationId() : getContentId();

        getSupportFragmentManager().beginTransaction()
                .replace(containerId, fragment, tag)
                .commit();
//                .commitAllowingStateLoss();
    }

    private void initializeDrawerVariables() {
        mDrawerLayout = findViewById(R.id.linearLayoutDrawer);

        // repeating transaction
        LinearLayout mDrawerLinearRepeating = findViewById(R.id.linearLayoutRepeatingTransaction);
        if (mDrawerLinearRepeating != null) {
            mDrawerLinearRepeating.setVisibility(View.GONE);
        }
        mDrawerTextUserName = findViewById(R.id.textViewUserName);
        mDrawerTextTotalAccounts = findViewById(R.id.textViewTotalAccounts);
    }

    private boolean isDatabaseAvailable() {
        // Do we have a database set?
        String dbPath = new AppSettings(this).getDatabaseSettings().getDatabasePath();
        if (TextUtils.isEmpty(dbPath)) return false;

        // Does the database file exist?
        File dbFile = new File(dbPath);
        return dbFile.exists();
    }

    private boolean isTutorialNeeded() {
        return new AppSettings(this).getBehaviourSettings().getShowTutorial();
    }

    /**
     * called when quick-switching the recent databases from the navigation menu.
     * @param recentDb selected recent database entry
     */
    private void onOpenDatabaseClick(DatabaseMetadata recentDb) {
        // do nothing if selecting the currently open database
        String currentDb = new DatabaseManager(this).getDatabasePath();
        if (recentDb.localPath.equals(currentDb)) return;

        changeDatabase(recentDb);
    }

//    private void requestDatabasePassword() {
//        // request password for the current database.
//        requestDatabasePassword(null);
//    }

//    private void requestDatabasePassword(String dbFilePath) {
//        // request password
//        Intent intent = new Intent(this, PasswordActivity.class);
//        intent.putExtra(EXTRA_DATABASE_PATH, dbFilePath);
//        startActivityForResult(intent, REQUEST_PASSWORD);
//        // continues in onActivityResult.
//    }

    private void restoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState.containsKey(KEY_IN_AUTHENTICATION))
            isInAuthentication = savedInstanceState.getBoolean(KEY_IN_AUTHENTICATION);
        if (savedInstanceState.containsKey(KEY_RECURRING_TRANSACTION)) {
            isRecurringTransactionStarted = savedInstanceState.getBoolean(KEY_RECURRING_TRANSACTION);
        }
    }

    private void startSyncIconRotation(MenuItem item) {
        if (item == null) return;

        // define the animation for rotation
        Animation animation = new RotateAnimation(360.0f, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setInterpolator(new LinearInterpolator());
        animation.setDuration(1200);
//        animRotate = AnimationUtils.loadAnimation(this, R.anim.rotation);
        animation.setRepeatCount(Animation.INFINITE);

        ImageView imageView = new ImageView(this);
        UIHelper uiHelper = new UIHelper(this);
        imageView.setImageDrawable(uiHelper.getIcon(GoogleMaterial.Icon.gmd_cached)
            .color(uiHelper.getToolbarItemColor()));
        imageView.setPadding(8, 8, 8, 8);
//        imageView.setLayoutParams(new Toolbar.LayoutParams());

        imageView.startAnimation(animation);
        item.setActionView(imageView);
    }

    private void stopSyncIconRotation(MenuItem item) {
        if (item == null) return;

        View actionView = item.getActionView();
        if (actionView == null) return;

        actionView.clearAnimation();
        item.setActionView(null);
    }

    /**
     * Shown database path with toast message
     * @param context Executing context.
     */
    private void showCurrentDatabasePath(Context context) {
        String currentPath = new DatabaseManager(context).getDatabasePath();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String lastPath = preferences.getString(context.getString(PreferenceConstants.PREF_LAST_DB_PATH_SHOWN), "");

        if (!lastPath.equals(currentPath)) {
            preferences.edit()
                    .putString(context.getString(PreferenceConstants.PREF_LAST_DB_PATH_SHOWN), currentPath)
                    .apply();
//                    .commit();
            try {
                Toast.makeText(context,
                        Html.fromHtml(context.getString(R.string.path_database_using, "<b>" + currentPath + "</b>")),
                        Toast.LENGTH_LONG)
                    .show();
            } catch (Exception e) {
                Timber.e(e, "showing the current database path");
            }
        }
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
            transaction.replace(R.id.fragmentMain, fragment, tag);
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
        if (isTutorialNeeded()) {
            showTutorial();
            return true;
        }

        // show database chooser if no valid database
        if (!isDatabaseAvailable()) {
            showSelectDatabaseActivity();
            return true;
        }

        // todo: show webview changelog?

//        // show change log dialog
//        Core core = new Core(this);
//        if (core.isToDisplayChangelog()) {
//            core.showChangelog();
//        }

        return false;
    }

    private void showReportsSelector(String text) {
        final DrawerMenuItemAdapter adapter = new DrawerMenuItemAdapter(this);
        UIHelper uiHelper = new UIHelper(this);
        int iconColor = uiHelper.getSecondaryTextColor();

        // payee
        adapter.add(new DrawerMenuItem().withId(R.id.menu_report_payees)
                .withText(getString(R.string.payees))
                .withIconDrawable(uiHelper.getIcon(GoogleMaterial.Icon.gmd_donut_large)
                    .color(iconColor)));

        // where money goes
        adapter.add(new DrawerMenuItem().withId(R.id.menu_report_where_money_goes)
                .withText(getString(R.string.menu_report_where_money_goes))
                .withIconDrawable(uiHelper.getIcon(GoogleMaterial.Icon.gmd_donut_large)
                        .color(iconColor)));

        // where money comes from
        adapter.add(new DrawerMenuItem().withId(R.id.menu_report_where_money_comes_from)
                .withText(getString(R.string.menu_report_where_money_comes_from))
                .withIconDrawable(uiHelper.getIcon(GoogleMaterial.Icon.gmd_donut_large)
                        .color(iconColor)));

        // where money comes from
        adapter.add(new DrawerMenuItem().withId(R.id.menu_report_categories)
                .withText(getString(R.string.categories))
                .withIconDrawable(uiHelper.getIcon(GoogleMaterial.Icon.gmd_donut_large)
                        .color(iconColor)));

        // income vs. expenses
        adapter.add(new DrawerMenuItem().withId(R.id.menu_report_income_vs_expenses)
                .withText(getString(R.string.menu_report_income_vs_expenses))
                .withIconDrawable(uiHelper.getIcon(MMXIconFont.Icon.mmx_reports)
                        .color(iconColor)));

        // Asset Allocation Overview
        adapter.add(new DrawerMenuItem().withId(R.id.menu_asset_allocation_overview)
                .withText(getString(R.string.asset_allocation))
                .withIconDrawable(uiHelper.getIcon(MMXIconFont.Icon.mmx_chart_pie)
                        .color(iconColor)));

        new MaterialDialog.Builder(this)
                .title(text)
                .adapter(adapter, new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                        onDrawerMenuAndOptionMenuSelected(adapter.getItem(which));
                        dialog.dismiss();
                    }
                })
                .build()
                .show();
    }

    private void showSelectDatabaseActivity() {
        Intent intent = new Intent(this, SelectDatabaseActivity.class);
        // make top-level so there's no going back.
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    /**
     * New migration - all entries must have the Remote Url as we are now using
     * storage access framework.
     */
    private void migrateRecentDatabases() {
        RecentDatabasesProvider databases = getDatabases();
        // if there are entries with empty remote location, migrate:
        // - clean up the list
        // - create the default entry
        // - save the default entry.
        for (DatabaseMetadata entry : databases.map.values()) {
            if (TextUtils.isEmpty(entry.remotePath)) {
                databases.clear();
                // create & save the default entry.
                DatabaseMetadata currentEntry = databases.getCurrent();

                return;
            }
        }
    }
}
