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
import android.app.Activity;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.MaterialDialog;
import com.money.manager.ex.AccountListActivity.AccountLoaderListFragment;
import com.money.manager.ex.CategorySubCategoryExpandableListActivity.CategorySubCategoryExpandableLoaderListFragment;
import com.money.manager.ex.CurrencyFormatsListActivity.CurrencyFormatsLoaderListFragment;
import com.money.manager.ex.PayeeActivity.PayeeLoaderListFragment;
import com.money.manager.ex.RepeatingTransactionListActivity.RepeatingTransactionListFragment;
import com.money.manager.ex.about.AboutActivity;
import com.money.manager.ex.adapter.DrawerMenuItem;
import com.money.manager.ex.adapter.DrawerMenuItemAdapter;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.MoneyManagerBootReceiver;
import com.money.manager.ex.core.Passcode;
import com.money.manager.ex.database.TableAccountList;
import com.money.manager.ex.dropbox.DropboxHelper;
import com.money.manager.ex.dropbox.DropboxServiceIntent;
import com.money.manager.ex.fragment.AccountFragment;
import com.money.manager.ex.fragment.BaseFragmentActivity;
import com.money.manager.ex.fragment.DashboardFragment;
import com.money.manager.ex.fragment.HomeFragment;
import com.money.manager.ex.notifications.RepeatingTransactionNotifications;
import com.money.manager.ex.preferences.PreferencesConstant;
import com.money.manager.ex.reports.CategoriesReportActivity;
import com.money.manager.ex.reports.IncomeVsExpensesActivity;
import com.money.manager.ex.settings.SettingsActivity;
import com.money.manager.ex.utils.CurrencyUtils;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.listeners.ActionClickListener;

import java.io.File;
import java.net.URLDecoder;
import java.util.List;

/**
 * @author Alessandro Lazzari (lazzari.ale@gmail.com)
 */
@SuppressLint("DefaultLocale")
public class MainActivity extends BaseFragmentActivity {
    // requestcode
    public static final int REQUEST_PICKFILE_CODE = 1;
    public static final int REQUEST_PASSCODE = 2;
    private static final String LOGCAT = MainActivity.class.getSimpleName();
    private static final String KEY_IS_AUTHENTICATED = "MainActivity:isAuthenticated";
    private static final String KEY_IN_AUTHENTICATION = "MainActivity:isInAuthenticated";
    private static final String KEY_IS_SHOW_TIPS_DROPBOX2 = "MainActivity:isShowTipsDropbox2";
    private static final String KEY_CLASS_FRAGMENT_CONTENT = "MainActivity:Fragment";
    private static final String KEY_ORIENTATION = "MainActivity:Orientation";
    private static final String KEY_RECURRING_TRANSACTION = "MainActivity:RecurringTransaction";
    // state if restart activity
    private static boolean mRestartActivity = false;
    // list of account visible
    List<TableAccountList> mAccountList;
    private boolean isAuthenticated = false;
    private boolean isInAuthentication = false;
    private boolean isShowTipsDropbox2 = false;
    private boolean isRecurringTransactionStarted = false;
    // navigation drawer
    private LinearLayout mDrawerLayout;
    private ListView mDrawerList;
    private DrawerLayout mDrawer;
    private MyActionBarDrawerToggle mDrawerToggle;
    // object in drawer
    private LinearLayout mDrawerLinearRepeating;
    private TextView mDrawerTextUserName;
    private TextView mDrawerTextTotalAccounts;
    private TextView mDrawerTextViewRepeating;
    // dropbox object
    private DropboxHelper mDropboxHelper;
    // state dual panel
    private boolean mIsDualPanel = false;

    /**
     * @return the mRestart
     */
    public static boolean isRestartActivitySet() {
        return mRestartActivity;
    }

    /**
     * @param mRestart the mRestart to set
     */
    public static void setRestartActivity(boolean mRestart) {
        MainActivity.mRestartActivity = mRestart;
    }

    /**
     * @return the mIsDualPanel
     */
    public boolean isDualPanel() {
        return mIsDualPanel;
    }

    /**
     * @param mIsDualPanel the mIsDualPanel to set
     */
    public void setDualPanel(boolean mIsDualPanel) {
        this.mIsDualPanel = mIsDualPanel;
    }

    public int getResIdLayoutContent() {
        return isDualPanel() ? R.id.fragmentDetail : R.id.fragmentContent;
    }

    /**
     * Change database applications
     *
     * @param pathDatabase new path of databases
     */
    public void changeDatabase(String pathDatabase) {
        Log.v(LOGCAT, "Change database: " + pathDatabase);

        Core core = new Core(getApplicationContext());
        core.changeDatabase(pathDatabase);
        // restart this activity
        setRestartActivity(true);
        restartActivity();
    }

    public void changeFragment(int accountId) {
        String tagFragment = AccountFragment.class.getSimpleName() + "_" + Integer.toString(accountId);
        AccountFragment fragment;
        fragment = (AccountFragment) getSupportFragmentManager().findFragmentByTag(tagFragment);
        if (fragment == null || fragment.getId() != getResIdLayoutContent()) {
            fragment = AccountFragment.newIstance(accountId);
        }
        // set if shown open menu
        fragment.setShownOpenDatabaseItemMenu(isDualPanel());
        // show fragment
        showFragment(fragment, tagFragment);
    }

    /**
     * Dialog to choose exit from application
     */
    public void exitApplication() {
        AlertDialogWrapper.Builder exitDialog = new AlertDialogWrapper.Builder(getApplicationContext());
        exitDialog.setTitle(R.string.close_application);
        exitDialog.setMessage(R.string.question_close_application);
        exitDialog.setIcon(R.drawable.ic_launcher);
        exitDialog.setPositiveButton(android.R.string.yes, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MoneyManagerApplication.killApplication();
            }
        });
        exitDialog.setNegativeButton(android.R.string.no, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        // show dialog
        exitDialog.create().show();
    }

    public Fragment getFragmentDisplay() {
        return getSupportFragmentManager().findFragmentById(isDualPanel() ? R.id.fragmentDetail : R.id.fragmentContent);
    }

    /**
     * pick a file to use
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
                Log.e(LOGCAT, e.getMessage());
                Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), R.string.error_intent_pick_file, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Reload all fragment into activity
     */
    public void reloadAllFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager != null) {
            // content
            Fragment fragment = fragmentManager.findFragmentById(R.id.fragmentContent);
            if (fragment != null)
                fragment.onResume();
            // check if is dual panel
            if (isDualPanel()) {
                fragment = fragmentManager.findFragmentById(R.id.fragmentDetail);
                if (fragment != null)
                    fragment.onResume();
            }
        }
    }

    /**
     * for the change setting restart process application
     */
    public void restartActivity() {
        if (mRestartActivity) {
            Intent intent = getIntent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            // finish this activity
            finish();
            // restart
            startActivity(intent);
            // kill process
            android.os.Process.killProcess(android.os.Process.myPid());
        }
        // set state a false
        setRestartActivity(false);
    }

    /**
     * show dashboard fragment
     */
    public void showDashboardFragment() {
        DashboardFragment dashboardFragment = (DashboardFragment) getSupportFragmentManager().findFragmentByTag(DashboardFragment.class.getSimpleName());
        if (dashboardFragment == null || dashboardFragment.getId() != getResIdLayoutContent()) {
            dashboardFragment = new DashboardFragment();
        }
        // fragment dashboard
        showFragment(dashboardFragment, DashboardFragment.class.getSimpleName());
    }

    /**
     * Show fragment using reflection from class
     */
    public void showFragment(Class<?> clsFragment) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(clsFragment.getName());
        if (fragment == null || fragment.getId() != getResIdLayoutContent()) {
            ClassLoader loader = getClassLoader();
            if (loader != null) {
                try {
                    Class<?> classFragment = loader.loadClass(clsFragment.getName());
                    fragment = (Fragment) classFragment.newInstance();
                } catch (Exception e) {
                    Log.e(LOGCAT, e.getMessage());
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
     * @param fragment
     * @param tagFragment
     */
    public void showFragment(Fragment fragment, String tagFragment) {
        // transaction
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        // animation
        transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_right, R.anim.slide_out_left);
        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack
        if (isDualPanel()) {
            transaction.replace(R.id.fragmentDetail, fragment, tagFragment);
        } else {
            transaction.replace(R.id.fragmentContent, fragment, tagFragment);
            transaction.addToBackStack(null);
        }
        // Commit the transaction
        transaction.commit();
    }

    /**
     * show a fragment select with position or account id
     *
     * @param position  to page
     * @param accountId account id of the fragment to be loaded
     */
    public void showFragmentAccount(int position, int accountId) {
        changeFragment(accountId);
    }

    /**
     * Show tutorial on first run.
     *
     * @param savedInstanceState
     */
    public void showTutorial(Bundle savedInstanceState) {
        Context context = getApplicationContext();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        //SharedPreferences settings = getSharedPreferences(key, 0);
        String key = context.getString(PreferencesConstant.PREF_SHOW_TUTORIAL);
        boolean showTutorial = settings.getBoolean(key, true);

        if(!showTutorial) return;

        // else show tutorial.
        Log.d("Tutorial", "Show tutorial the first time");

        // mark the tutorial as seen.
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(key, false);
        editor.commit();
    }

    public void showSnackbarDropbox() {
        if (mDropboxHelper != null && mDropboxHelper.isLinked()) {
            AsyncTask<Void, Void, Integer> asyncTask = new AsyncTask<Void, Void, Integer>() {
                @Override
                protected Integer doInBackground(Void... voids) {
                    return mDropboxHelper.checkIfFileIsSync();
                }

                @Override
                protected void onPostExecute(Integer ret) {
                    if (DropboxServiceIntent.INTENT_EXTRA_MESSENGER_DOWNLOAD == ret) {
                        Snackbar.with(getApplicationContext()) // context
                                .text(getString(R.string.dropbox_database_can_be_updted))
                                .actionLabel(getString(R.string.sync))
                                .actionColor(getResources().getColor(R.color.md_primary))
                                .actionListener(new ActionClickListener() {
                                    @Override
                                    public void onActionClicked(Snackbar snackbar) {
                                        startServiceSyncDropbox();
                                    }
                                })
                                .duration(5 * 1000)
                                .show(MainActivity.this);
                    }
                }
            };
            asyncTask.execute();
        }
    }

    public void startServiceSyncDropbox() {
        if (mDropboxHelper != null && mDropboxHelper.isLinked()) {
            Intent service = new Intent(getApplicationContext(), DropboxServiceIntent.class);
            service.setAction(DropboxServiceIntent.INTENT_ACTION_SYNC);
            service.putExtra(DropboxServiceIntent.INTENT_EXTRA_LOCAL_FILE, MoneyManagerApplication.getDatabasePath(getApplicationContext()));
            service.putExtra(DropboxServiceIntent.INTENT_EXTRA_REMOTE_FILE, mDropboxHelper.getLinkedRemoteFile());
            //progress dialog
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setCancelable(false);
            progressDialog.setMessage(getString(R.string.dropbox_syncProgress));
            progressDialog.setIndeterminate(true);
            progressDialog.show();
            //create a messenger
            Messenger messenger = new Messenger(new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what == DropboxServiceIntent.INTENT_EXTRA_MESSENGER_NOT_CHANGE) {
                        // close dialog
                        if (progressDialog != null && progressDialog.isShowing())
                            progressDialog.hide();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, R.string.dropbox_database_is_synchronized, Toast.LENGTH_LONG).show();
                            }
                        });
                    } else if (msg.what == DropboxServiceIntent.INTENT_EXTRA_MESSENGER_START_DOWNLOAD) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, R.string.dropbox_download_is_starting, Toast.LENGTH_LONG).show();
                            }
                        });
                    } else if (msg.what == DropboxServiceIntent.INTENT_EXTRA_MESSENGER_DOWNLOAD) {
                        // close dialog
                        if (progressDialog != null && progressDialog.isShowing())
                            progressDialog.hide();
                        // reload fragment
                        reloadAllFragment();
                    } else if (msg.what == DropboxServiceIntent.INTENT_EXTRA_MESSENGER_START_UPLOAD) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, R.string.dropbox_upload_is_starting, Toast.LENGTH_LONG).show();
                            }
                        });
                    } else if (msg.what == DropboxServiceIntent.INTENT_EXTRA_MESSENGER_UPLOAD) {
                        // close dialog
                        if (progressDialog != null && progressDialog.isShowing())
                            progressDialog.hide();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, R.string.upload_file_to_dropbox_complete, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            });
            service.putExtra(DropboxServiceIntent.INTENT_EXTRA_MESSENGER, messenger);

            this.startService(service);
        }
    }

    public void setDrawableUserName(String userName) {
        if (mDrawerTextUserName != null)
            mDrawerTextUserName.setText(userName);
    }

    public void setDrawableTotalAccounts(String totalAccounts) {
        if (mDrawerTextTotalAccounts != null)
            mDrawerTextTotalAccounts.setText(totalAccounts);
    }

    public void setDrawableRepeatingTransactions(int repeatingTransaction) {
        if (mDrawerLinearRepeating != null && mDrawerTextViewRepeating != null) {
            mDrawerLinearRepeating.setVisibility(repeatingTransaction <= 0 ? View.GONE : View.VISIBLE);
            mDrawerTextViewRepeating.setText(getString(R.string.num_repeating_transaction_expired, repeatingTransaction));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Core core = new Core(this);

        // close notification
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(DropboxServiceIntent.NOTIFICATION_DROPBOX_OPEN_FILE);

        // check intent is valid
        if (getIntent() != null && getIntent().getData() != null) {
            String pathFile = getIntent().getData().getEncodedPath();
            // decode
            try {
                pathFile = URLDecoder.decode(pathFile, "UTF-8"); // decode file path
                if (BuildConfig.DEBUG)
                    Log.d(LOGCAT, "Path intent file to open:" + pathFile);
                if (!core.changeDatabase(pathFile)) {
                    Log.w(LOGCAT, "Path intent file to open:" + pathFile + " not correct!!!");
                }
            } catch (Exception e) {
                Log.e(LOGCAT, e.getMessage());
            }
        }

        // check authentication
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(KEY_IS_AUTHENTICATED))
                isAuthenticated = savedInstanceState.getBoolean(KEY_IS_AUTHENTICATED);
            if (savedInstanceState.containsKey(KEY_IN_AUTHENTICATION))
                isInAuthentication = savedInstanceState.getBoolean(KEY_IN_AUTHENTICATION);
            if (savedInstanceState.containsKey(KEY_RECURRING_TRANSACTION))
                isRecurringTransactionStarted = savedInstanceState.getBoolean(KEY_RECURRING_TRANSACTION);
            if (savedInstanceState.containsKey(KEY_ORIENTATION)) {
                if (core.isTablet()) {
                    if (savedInstanceState.getInt(KEY_ORIENTATION) != getResources().getConfiguration().orientation) {
                        for (int i = 0; i < getSupportFragmentManager().getBackStackEntryCount(); ++i) {
                            getSupportFragmentManager().popBackStack();
                        }
                    }
                }
            }
        }

        // load base currency and compose hash currencies
        CurrencyUtils currencyUtils = new CurrencyUtils(getApplicationContext());
        if (!currencyUtils.isInit())
            currencyUtils.reInit();

        // create a connection to dropbox
        mDropboxHelper = DropboxHelper.getInstance(getApplicationContext());
        // check type mode
        onCreateFragments(savedInstanceState);
        // show tips dialog
        showTutorial(savedInstanceState);
        // show changelog dialog
        if (core.isToDisplayChangelog())
            core.showChangelog();

        MoneyManagerApplication.showDatabasePathWork(getApplicationContext());

        // notification send broadcast
        Intent serviceRepeatingTransaction = new Intent(getApplicationContext(), MoneyManagerBootReceiver.class);
        getApplicationContext().sendBroadcast(serviceRepeatingTransaction);

        showSnackbarDropbox();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // check if has passcode and authenticate
        if (!isAuthenticated) {
            Passcode passcode = new Passcode(this);
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        // check if restart activity
        if (isRestartActivitySet()) {
            restartActivity(); // restart and exit
            return;
        }
    }

    /**
     * this method call for classic method (show fragments)
     *
     * @param savedInstanceState
     */
    public void onCreateFragments(Bundle savedInstanceState) {
        Core core = new Core(getApplicationContext());

        setContentView(R.layout.main_fragments_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        LinearLayout fragmentDetail = (LinearLayout) findViewById(R.id.fragmentDetail);
        setDualPanel(fragmentDetail != null && fragmentDetail.getVisibility() == View.VISIBLE);
        // show home fragment
        HomeFragment fragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag(HomeFragment.class.getSimpleName());
        if (fragment == null) {
            // fragment create
            fragment = new HomeFragment();
            // add to stack
            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContent, fragment, HomeFragment.class.getSimpleName()).commit();
        } else if (core.isTablet()) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContent, fragment, HomeFragment.class.getSimpleName()).commit();
        }

        // manage fragment
        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_CLASS_FRAGMENT_CONTENT)) {
            String className = savedInstanceState.getString(KEY_CLASS_FRAGMENT_CONTENT);
            if (className.contains(AccountFragment.class.getSimpleName())) {
                changeFragment(Integer.parseInt(className.substring(className.indexOf("_") + 1)));
            } else {
                try {
                    showFragment(Class.forName(className));
                } catch (ClassNotFoundException e) {
                    Log.e(LOGCAT, e.getMessage());
                }
            }
        }
        // navigation drawer
        mDrawer = (DrawerLayout) findViewById(R.id.drawerLayout);

        // set a custom shadow that overlays the main content when the drawer opens
        if (mDrawer != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                mDrawerToggle = new MyActionBarDrawerToggle(this, mDrawer, R.string.open, R.string.closed);
                mDrawer.setDrawerListener(mDrawerToggle);
                // create drawer menu
                createDrawerMenu();
                // enable ActionBar app icon to behave as action to toggle nav drawer
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowTitleEnabled(true);
            } else {
                mDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            }
        }
        // start notification for recurring transaction
        if (!isRecurringTransactionStarted) {
            RepeatingTransactionNotifications notifications = new RepeatingTransactionNotifications(getApplicationContext());
            notifications.notifyRepeatingTransaction();
            isRecurringTransactionStarted = true;
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (mDrawerToggle != null) {
            try {
                mDrawerToggle.syncState();
            } catch (Exception e) {
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // check request code
        switch (requestCode) {
            case REQUEST_PICKFILE_CODE:
                if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                    changeDatabase(data.getData().getPath());
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
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setRestartActivity(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // quick-fix convert 'switch' to 'if-else'
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (mDrawer.isDrawerOpen(mDrawerLayout)) {
                mDrawer.closeDrawer(mDrawerLayout);
            } else {
                mDrawer.openDrawer(mDrawerLayout);
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Core core = new Core(getApplicationContext());
        if (core.isTablet()) {
            Fragment fragment = getSupportFragmentManager().findFragmentById(getResIdLayoutContent());
            if (fragment != null) {
                if (fragment instanceof AccountFragment) {
                    outState.putString(KEY_CLASS_FRAGMENT_CONTENT, ((AccountFragment) fragment).getNameFragment());
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

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null)
            notificationManager.cancelAll();
        super.onDestroy();
    }

    /**
     * drawer management
     */
    public void createDrawerMenu() {
        Core core = new Core(getApplicationContext());
        Boolean isDarkTheme = core.getThemeApplication() == R.style.Theme_Money_Manager;

        mDrawerLayout = (LinearLayout) findViewById(R.id.linearLayoutDrawer);
        mDrawerList = (ListView) findViewById(R.id.listViewDrawer);
        // repeating transaction
        mDrawerLinearRepeating = (LinearLayout) findViewById(R.id.linearLayoutRepeatingTransaction);
        mDrawerLinearRepeating.setVisibility(View.GONE);
        mDrawerTextViewRepeating = (TextView) findViewById(R.id.textViewOverdue);
        mDrawerTextUserName = (TextView) findViewById(R.id.textViewUserName);
        mDrawerTextTotalAccounts = (TextView) findViewById(R.id.textViewTotalAccounts);

        // create adapter
        DrawerMenuItemAdapter adapter = new DrawerMenuItemAdapter(this);
        // home
        adapter.add(new DrawerMenuItem().withId(R.id.menu_home)
                .withText(getString(R.string.home))
                .withIcon(isDarkTheme ? R.drawable.ic_home_dark : R.drawable.ic_home_light));
        // open database
        adapter.add(new DrawerMenuItem().withId(R.id.menu_open_database)
                .withText(getString(R.string.open_database))
                .withIcon(isDarkTheme ? R.drawable.ic_action_folder_open_dark : R.drawable.ic_action_folder_open_light));
        // dropbox synchronize
        if (mDropboxHelper != null && mDropboxHelper.isLinked())
            adapter.add(new DrawerMenuItem().withId(R.id.menu_sync_dropbox)
                    .withText(getString(R.string.synchronize))
                    .withIcon(isDarkTheme ? R.drawable.ic_action_dropbox_dark : R.drawable.ic_action_dropbox_light));
        // manage: add transaction
        // adapter.add(new DrawerMenuItem(R.id.menu_add_transaction_account, getString(R.string.add_transaction), isDarkTheme ? R.drawable.ic_action_add_dark : R.drawable.ic_action_add_light));
        // tools
        adapter.add(new DrawerMenuItem().withId(R.id.menu_group_main)
                .withText(getString(R.string.tools))
                .withIcon(isDarkTheme ? R.drawable.ic_action_domain_dark : R.drawable.ic_action_domain_light));
        // manage: repeating transactions
        adapter.add(new DrawerMenuItem().withId(R.id.menu_repeating_transaction)
                .withText(getString(R.string.repeating_transactions))
                .withIcon(isDarkTheme ? R.drawable.ic_action_history_dark : R.drawable.ic_action_history_light));
        // search transaction
        adapter.add(new DrawerMenuItem().withId(R.id.menu_search_transaction)
                .withText(getString(R.string.search))
                .withIcon(isDarkTheme ? R.drawable.ic_action_search_dark : R.drawable.ic_action_search_light));
        // reports
        adapter.add(new DrawerMenuItem().withId(R.id.menu_reports)
                .withText(getString(R.string.menu_reports))
                .withIcon(isDarkTheme ? R.drawable.ic_action_bargraph_dark : R.drawable.ic_action_bargraph_light)
                .withDivider(true));
        // settings
        adapter.add(new DrawerMenuItem().withId(R.id.menu_settings)
                .withText(getString(R.string.settings))
                .withIcon(isDarkTheme ? R.drawable.ic_action_settings_dark : R.drawable.ic_action_settings_light));
        // donate
        adapter.add(new DrawerMenuItem().withId(R.id.menu_donate)
                .withText(getString(R.string.donate))
                .withIcon(isDarkTheme ? R.drawable.ic_action_reports_dark : R.drawable.ic_action_redeem_light)
                .withDivider(Boolean.TRUE));
        // help
        adapter.add(new DrawerMenuItem().withId(R.id.menu_about)
                .withText(getString(R.string.about))
                .withIcon(isDarkTheme ? R.drawable.ic_action_help_dark : R.drawable.ic_action_help_light));

        // get drawerlist and set adapter
        if (mDrawerList != null)
            mDrawerList.setAdapter(adapter);
        // set listener on item click
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
    }

    public boolean onDrawerMenuAndOptionMenuSelected(DrawerMenuItem item) {
        Intent intent;
        final Core core = new Core(getApplicationContext());
        final Boolean isDarkTheme = core.getThemeApplication() == R.style.Theme_Money_Manager;

        if (item.getId() == R.id.menu_home) {
            showFragment(HomeFragment.class);
            return true;
        } else if (item.getId() == R.id.menu_sync_dropbox) {
            startServiceSyncDropbox();
            return true;
        } else if (item.getId() == R.id.menu_open_database) {
            pickFile(Environment.getExternalStorageDirectory());
            return true;
        } else if (item.getId() == R.id.menu_add_transaction_account) {
            intent = new Intent(MainActivity.this, CheckingAccountActivity.class);
            intent.setAction(Intent.ACTION_INSERT);
            startActivity(intent);
            return true;
        } else if (item.getId() == R.id.menu_group_main) {
            final DrawerMenuItemAdapter adapter = new DrawerMenuItemAdapter(this);
            // manage: account
            adapter.add(new DrawerMenuItem().withId(R.id.menu_account)
                    .withText(getString(R.string.accounts))
                    .withIcon(isDarkTheme ? R.drawable.ic_action_bank_dark : R.drawable.ic_action_bank_light));
            // manage: categories
            adapter.add(new DrawerMenuItem().withId(R.id.menu_category)
                    .withText(getString(R.string.categories))
                    .withIcon(isDarkTheme ? R.drawable.ic_action_label_outline_dark : R.drawable.ic_action_label_outline_light));
            // manage: currencies
            adapter.add(new DrawerMenuItem().withId(R.id.menu_currency)
                    .withText(getString(R.string.currencies))
                    .withIcon(isDarkTheme ? R.drawable.ic_action_attach_money_dark : R.drawable.ic_action_attach_money_light));
            // manage: payees
            adapter.add(new DrawerMenuItem().withId(R.id.menu_payee)
                    .withText(getString(R.string.payees))
                    .withIcon(isDarkTheme ? R.drawable.ic_action_users_dark : R.drawable.ic_action_users_light));

            onDrawerItemSubDialogs(adapter, item.getText(), isDarkTheme);
        } else if (item.getId() == R.id.menu_account) {
            showFragment(AccountLoaderListFragment.class);
            return true;
        } else if (item.getId() == R.id.menu_category) {
            showFragment(CategorySubCategoryExpandableLoaderListFragment.class);
            return true;
        } else if (item.getId() == R.id.menu_currency) {
            showFragment(CurrencyFormatsLoaderListFragment.class);
            return true;
        } else if (item.getId() == R.id.menu_payee) {
            showFragment(PayeeLoaderListFragment.class);
            return true;
        } else if (item.getId() == R.id.menu_repeating_transaction) {
            showFragment(RepeatingTransactionListFragment.class);
            return true;
        } else if (item.getId() == R.id.menu_search_transaction) {
            startActivity(new Intent(MainActivity.this, SearchActivity.class));
            return true;
        } else if (item.getId() == R.id.menu_settings) {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        } else if (item.getId() == R.id.menu_dashboard) {
            showFragment(DashboardFragment.class);
            return true;
        } else if (item.getId() == R.id.menu_reports) {
            final DrawerMenuItemAdapter adapter = new DrawerMenuItemAdapter(this);
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
            onDrawerItemSubDialogs(adapter, item.getText(), isDarkTheme);
            return true;
        } else if (item.getId() == R.id.menu_report_where_money_goes) {
            intent = new Intent(this, CategoriesReportActivity.class);
            intent.putExtra(CategoriesReportActivity.REPORT_FILTERS, Constants.TRANSACTION_TYPE_WITHDRAWAL);
            intent.putExtra(CategoriesReportActivity.REPORT_TITLE, getString(R.string.menu_report_where_money_goes));
            startActivity(intent);
            return true;
        } else if (item.getId() == R.id.menu_report_where_money_comes_from) {
            intent = new Intent(this, CategoriesReportActivity.class);
            intent.putExtra(CategoriesReportActivity.REPORT_FILTERS, Constants.TRANSACTION_TYPE_DEPOSIT);
            intent.putExtra(CategoriesReportActivity.REPORT_TITLE, getString(R.string.menu_report_where_money_comes_from));
            startActivity(intent);
            return true;
        } else if (item.getId() == R.id.menu_report_categories) {
            startActivity(new Intent(this, CategoriesReportActivity.class));
            return true;
        } else if (item.getId() == R.id.menu_report_income_vs_expenses) {
            startActivity(new Intent(this, IncomeVsExpensesActivity.class));
            return true;
        } else if (item.getId() == R.id.menu_donate) {
            startActivity(new Intent(this, DonateActivity.class));
            return true;
        } else if (item.getId() == R.id.menu_help) {
            startActivity(new Intent(MainActivity.this, HelpActivity.class));
            return true;
        } else if (item.getId() == R.id.menu_about) {
            startActivity(new Intent(MainActivity.this, AboutActivity.class));
            return true;
        }
        return false;
    }

    public void onDrawerItemSubDialogs(final DrawerMenuItemAdapter adapter, CharSequence title, Boolean isDarkTheme) {
        final MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title(title)
                .adapter(adapter)
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

    private class DrawerItemClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (mDrawer == null) return;
            // Highlight the selected item, update the title, and close the
            // drawer
            // update selected item and title, then close the drawer
            mDrawerList.setItemChecked(position, true);

            // You should reset item counter
            mDrawer.closeDrawer(mDrawerLayout);
            // check item selected
            final DrawerMenuItem item = ((DrawerMenuItemAdapter) mDrawerList.getAdapter()).getItem(position);
            if (item != null) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // execute operation
                        onDrawerMenuAndOptionMenuSelected(item);
                    }
                }, 250);
            }
        }
    }

    public class MyActionBarDrawerToggle extends ActionBarDrawerToggle {

        public MyActionBarDrawerToggle(Activity activity, DrawerLayout drawerLayout, int openDrawerContentDescRes, int closeDrawerContentDescRes) {
            super(activity, drawerLayout, openDrawerContentDescRes, closeDrawerContentDescRes);
        }

        public MyActionBarDrawerToggle(Activity activity, DrawerLayout drawerLayout, Toolbar toolbar, int openDrawerContentDescRes, int closeDrawerContentDescRes) {
            super(activity, drawerLayout, toolbar, openDrawerContentDescRes, closeDrawerContentDescRes);
        }

        @Override
        public void onDrawerOpened(View drawerView) {
            super.onDrawerOpened(drawerView);
        }

        @Override
        public void onDrawerClosed(View drawerView) {
            super.onDrawerClosed(drawerView);
        }
    }
}
