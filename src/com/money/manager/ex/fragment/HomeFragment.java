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
package com.money.manager.ex.fragment;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;
import com.money.manager.ex.AccountListEditActivity;
import com.money.manager.ex.CheckingAccountActivity;
import com.money.manager.ex.Constants;
import com.money.manager.ex.MainActivity;
import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.businessobjects.BalanceAccountTask;
import com.money.manager.ex.core.AccountTypes;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.database.AccountRepository;
import com.money.manager.ex.database.DatabaseMigrator14To20;
import com.money.manager.ex.database.QueryAccountBills;
import com.money.manager.ex.database.QueryBillDeposits;
import com.money.manager.ex.database.QueryReportIncomeVsExpenses;
import com.money.manager.ex.database.TableInfoTable;
import com.money.manager.ex.search.SearchActivity;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.settings.DropboxSettingsActivity;
import com.money.manager.ex.settings.PreferenceConstants;
import com.money.manager.ex.utils.CurrencyUtils;
import com.money.manager.ex.view.RobotoTextView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * The fragment that contains the accounts groups with accounts and their balances.
 * @author Alessandro Lazzari (lazzari.ale@gmail.com)
 */
@SuppressWarnings("static-access")
public class HomeFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    // ID Loader Manager
    private static final int ID_LOADER_USER_NAME = 1;
    private static final int ID_LOADER_ACCOUNT_BILLS = 2;
    private static final int ID_LOADER_BILL_DEPOSITS = 3;
    private static final int ID_LOADER_INCOME_EXPENSES = 4;

    private final String LOGCAT = this.getClass().getSimpleName();

    private CurrencyUtils mCurrencyUtils;
    private boolean mHideReconciled;
    // dataset table/view/query manage into class
    private TableInfoTable infoTable = new TableInfoTable();
    private QueryAccountBills accountBills;

    // Controls. view show in layout
    // This is the collapsible list of account groups with accounts.
    private ExpandableListView mExpandableListView;
    private ViewGroup linearHome, linearFooter, linearWelcome;
    private TextView txtTotalAccounts, txtFooterSummary, txtFooterSummaryReconciled;
    private ProgressBar prgAccountBills;
    private FloatingActionButton mFloatingActionButton;

    private List<String> mAccountTypes = new ArrayList<>();
    private HashMap<String, List<QueryAccountBills>> mAccountsByType = new HashMap<>();
    private HashMap<String, QueryAccountBills> mTotalsByType = new HashMap<>();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

//        registerForContextMenu(getListView());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCurrencyUtils = new CurrencyUtils(getActivity().getApplicationContext());
        accountBills = new QueryAccountBills(getActivity());

        AppSettings settings = new AppSettings(getActivity());
        mHideReconciled = settings.getHideReconciledAmounts();

        // The fragment is using a custom option in the actionbar menu.
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (container == null) {
            return null;
        }

        // inflate layout
        View view = inflater.inflate(R.layout.home_fragment, container, false);

        // reference view into layout
        linearHome = (FrameLayout) view.findViewById(R.id.linearLayoutHome);

        createWelcomeView(view);

        txtTotalAccounts = (TextView) view.findViewById(R.id.textViewTotalAccounts);

        setUpAccountsList(view);

        prgAccountBills = (ProgressBar) view.findViewById(R.id.progressAccountBills);

        mFloatingActionButton = (FloatingActionButton) view.findViewById(R.id.fab);
        mFloatingActionButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CheckingAccountActivity.class);
                intent.setAction(Intent.ACTION_INSERT);
                startActivity(intent);
            }
        });

        return view;
    }

    private void createWelcomeView(View view) {
        linearWelcome = (ViewGroup) view.findViewById(R.id.linearLayoutWelcome);

        // add account button
        Button btnAddAccount = (Button) view.findViewById(R.id.buttonAddAccount);
        if (btnAddAccount != null) {
            btnAddAccount.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), AccountListEditActivity.class);
                    intent.setAction(Constants.INTENT_ACTION_INSERT);
                    startActivity(intent);
                }
            });
        }

        // link to dropbox
        Button btnLinkDropbox = (Button) view.findViewById(R.id.buttonLinkDropbox);
        if (btnLinkDropbox != null) {
            btnLinkDropbox.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), DropboxSettingsActivity.class);
                    //intent.putExtra(Constants.INTENT_REQUEST_PREFERENCES_SCREEN, PreferenceConstants.PREF_DROPBOX_HOWITWORKS);
                    startActivity(intent);
                }
            });
        }

        // Database migration v1.4 -> v2.0 location.
        setUpMigrationButton(view);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mFloatingActionButton.attachToListView(mExpandableListView);
    }

    // Loader event handlers

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Core core = new Core(getActivity().getApplicationContext());
        CursorLoader result = null;

        switch (id) {
            case ID_LOADER_USER_NAME:
                result = new CursorLoader(getActivity(), infoTable.getUri(),
                        new String[]{infoTable.INFONAME, infoTable.INFOVALUE}, null, null, null);
                break;
            case ID_LOADER_ACCOUNT_BILLS:
                setListViewAccountBillsVisible(false);
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
                result = new CursorLoader(getActivity(), accountBills.getUri(),
                        accountBills.getAllColumns(), where, null,
                        accountBills.ACCOUNTTYPE + ", upper(" + accountBills.ACCOUNTNAME + ")");
                break;

            case ID_LOADER_BILL_DEPOSITS:
                QueryBillDeposits billDeposits = new QueryBillDeposits(getActivity());
                result = new CursorLoader(getActivity(), billDeposits.getUri(), null, QueryBillDeposits.DAYSLEFT + "<=0", null, null);
                break;

            case ID_LOADER_INCOME_EXPENSES:
                QueryReportIncomeVsExpenses report = new QueryReportIncomeVsExpenses(getActivity());
                result = new CursorLoader(getActivity(), report.getUri(), report.getAllColumns(),
                        QueryReportIncomeVsExpenses.Month + "="
                            + Integer.toString(Calendar.getInstance().get(Calendar.MONTH) + 1) + " AND "
                        + QueryReportIncomeVsExpenses.Year + "="
                            + Integer.toString(Calendar.getInstance().get(Calendar.YEAR)),
                        null, null);
                break;

            default:
                result = null;
        }
        return  result;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case ID_LOADER_ACCOUNT_BILLS:
                txtTotalAccounts.setText(mCurrencyUtils.getBaseCurrencyFormatted((double) 0));

                setListViewAccountBillsVisible(false);
                mAccountsByType.clear();
                mTotalsByType.clear();
                mAccountTypes.clear();
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        MainActivity mainActivity = null;
        if (getActivity() != null && getActivity() instanceof MainActivity) {
            mainActivity = (MainActivity) getActivity();
        }

        switch (loader.getId()) {
            case ID_LOADER_USER_NAME:
                if (data != null && data.moveToFirst()) {
                    while (!data.isAfterLast()) {
                        String infoValue = data.getString(data.getColumnIndex(infoTable.INFONAME));
                        // save into preferences username and base currency id
                        if (Constants.INFOTABLE_USERNAME.equalsIgnoreCase(infoValue)) {
                            MoneyManagerApplication.getInstanceApp().setUserName(data.getString(data.getColumnIndex(infoTable.INFOVALUE)));
                        }
                        data.moveToNext();
                    }
                }
                mainActivity.setDrawerUserName(MoneyManagerApplication.getInstanceApp().getUserName());
                break;

            case ID_LOADER_ACCOUNT_BILLS:
                renderAccountsList(data);

                // set total for accounts in the main Drawer.
                // todo: use a callback interface for this.
                if (mainActivity != null) {
                    mainActivity.setDrawerTotalAccounts(txtTotalAccounts.getText().toString());
                }

                break;

            case ID_LOADER_BILL_DEPOSITS:
                // Recurring Transactions.
                mainActivity.setDrawableRepeatingTransactions(data != null ? data.getCount() : 0);
                break;

            case ID_LOADER_INCOME_EXPENSES:
                double income = 0, expenses = 0;
                if (data != null) {
                    while (data.moveToNext()) {
                        expenses = data.getDouble(data.getColumnIndex(QueryReportIncomeVsExpenses.Expenses));
                        income = data.getDouble(data.getColumnIndex(QueryReportIncomeVsExpenses.Income));
                    }
                }
                TextView txtIncome = (TextView) getActivity().findViewById(R.id.textViewIncome);
                TextView txtExpenses = (TextView) getActivity().findViewById(R.id.textViewExpenses);
                TextView txtDifference = (TextView) getActivity().findViewById(R.id.textViewDifference);
                // set value
                if (txtIncome != null)
                    txtIncome.setText(mCurrencyUtils.getCurrencyFormatted(mCurrencyUtils.getBaseCurrencyId(), income));
                if (txtExpenses != null)
                    txtExpenses.setText(mCurrencyUtils.getCurrencyFormatted(mCurrencyUtils.getBaseCurrencyId(), Math.abs(expenses)));
                if (txtDifference != null)
                    txtDifference.setText(mCurrencyUtils.getCurrencyFormatted(mCurrencyUtils.getBaseCurrencyId(), income - Math.abs(expenses)));
                // manage progressbar
                final ProgressBar barIncome = (ProgressBar) getActivity().findViewById(R.id.progressBarIncome);
                final ProgressBar barExpenses = (ProgressBar) getActivity().findViewById(R.id.progressBarExpenses);

                if (barIncome != null && barExpenses != null) {
                    barIncome.setMax((int) (Math.abs(income) + Math.abs(expenses)));
                    barExpenses.setMax((int) (Math.abs(income) + Math.abs(expenses)));

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
                        ObjectAnimator animationIncome = ObjectAnimator.ofInt(barIncome, "progress", (int) Math.abs(income));
                        animationIncome.setDuration(1000); // 0.5 second
                        animationIncome.setInterpolator(new DecelerateInterpolator());
                        animationIncome.start();

                        ObjectAnimator animationExpenses = ObjectAnimator.ofInt(barExpenses, "progress", (int) Math.abs(expenses));
                        animationExpenses.setDuration(1000); // 0.5 second
                        animationExpenses.setInterpolator(new DecelerateInterpolator());
                        animationExpenses.start();
                    } else {
                        barIncome.setProgress((int) Math.abs(income));
                        barExpenses.setProgress((int) Math.abs(expenses));
                    }
                }
        }
    }

    private void renderAccountsList(Cursor data) {
        // Accounts list

        BigDecimal curTotal = new BigDecimal(0);
        BigDecimal curReconciled = new BigDecimal(0);

        linearHome.setVisibility(data != null && data.getCount() > 0 ? View.VISIBLE : View.GONE);
        linearWelcome.setVisibility(linearHome.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);

        mAccountsByType.clear();
        mTotalsByType.clear();
        mAccountTypes.clear();

        // cycle cursor
        if (data != null) {
            while (data.moveToNext()) {
                double total = data.getDouble(data.getColumnIndex(QueryAccountBills.TOTALBASECONVRATE));
                curTotal = curTotal.add(BigDecimal.valueOf(total));
                double totalReconciled = data.getDouble(data.getColumnIndex(QueryAccountBills.RECONCILEDBASECONVRATE));
                curReconciled = curReconciled.add(BigDecimal.valueOf(totalReconciled));

                // find element
                QueryAccountBills bills = new QueryAccountBills(getActivity());
                bills.setAccountId(data.getInt(data.getColumnIndex(QueryAccountBills.ACCOUNTID)));
                bills.setAccountName(data.getString(data.getColumnIndex(QueryAccountBills.ACCOUNTNAME)));
                bills.setAccountType(data.getString(data.getColumnIndex(QueryAccountBills.ACCOUNTTYPE)));
                bills.setCurrencyId(data.getInt(data.getColumnIndex(QueryAccountBills.CURRENCYID)));
                bills.setTotal(data.getDouble(data.getColumnIndex(QueryAccountBills.TOTAL)));
                bills.setReconciled(data.getDouble(data.getColumnIndex(QueryAccountBills.RECONCILED)));
                bills.setTotalBaseConvRate(data.getDouble(data.getColumnIndex(QueryAccountBills.TOTALBASECONVRATE)));
                bills.setReconciledBaseConvRate(data.getDouble(data.getColumnIndex(QueryAccountBills.RECONCILEDBASECONVRATE)));

                String accountType = data.getString(data.getColumnIndex(QueryAccountBills.ACCOUNTTYPE));
                QueryAccountBills totals;
                if (mAccountTypes.indexOf(accountType) == -1) {
                    mAccountTypes.add(accountType);

                    totals = new QueryAccountBills(getActivity());
                    totals.setAccountType(accountType);
                    // set group title
                    if (AccountTypes.CHECKING.toString().equalsIgnoreCase(accountType)) {
                        totals.setAccountName(getString(R.string.bank_accounts));
                    } else if (AccountTypes.TERM.toString().equalsIgnoreCase(accountType)) {
                        totals.setAccountName(getString(R.string.term_accounts));
                    } else if (AccountTypes.CREDIT_CARD.toString().equalsIgnoreCase(accountType)) {
                        totals.setAccountName(getString(R.string.credit_card_accounts));
                    } else if (AccountTypes.INVESTMENT.toString().equalsIgnoreCase(accountType)) {
                        totals.setAccountName(getString(R.string.investment_accounts));
                    }
                    totals.setReconciledBaseConvRate(.0);
                    totals.setTotalBaseConvRate(.0);
                    mTotalsByType.put(accountType, totals);
                }
                totals = mTotalsByType.get(accountType);
                totals.setReconciledBaseConvRate(totals.getReconciledBaseConvRate() + data.getDouble(data.getColumnIndex(QueryAccountBills.RECONCILEDBASECONVRATE)));
                totals.setTotalBaseConvRate(totals.getTotalBaseConvRate() + data.getDouble(data.getColumnIndex(QueryAccountBills.TOTALBASECONVRATE)));

                List<QueryAccountBills> list = mAccountsByType.get(accountType);
                if (list == null) {
                    list = new ArrayList<>();
                    mAccountsByType.put(accountType, list);
                }
                list.add(bills);
            }
        }
        // write accounts total
        addFooterExpandableListView(curTotal.doubleValue(), curReconciled.doubleValue());

        // create adapter
        AccountBillsExpandableAdapter expandableAdapter = new AccountBillsExpandableAdapter(getActivity());
        // set adapter and shown
        mExpandableListView.setAdapter(expandableAdapter);

        setVisibilityOfAccountGroups();
        setListViewAccountBillsVisible(true);

    }

    public void startLoader() {
        getLoaderManager().restartLoader(ID_LOADER_USER_NAME, null, this);
        getLoaderManager().restartLoader(ID_LOADER_ACCOUNT_BILLS, null, this);
        /*getLoaderManager().restartLoader(ID_LOADER_BILL_DEPOSITS, null, this);*/
        getLoaderManager().restartLoader(ID_LOADER_INCOME_EXPENSES, null, this);
    }

    // Menu

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.home_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean result = false;

        switch (item.getItemId()) {
            case R.id.menu_search:
                startActivity(new Intent(getActivity(), SearchActivity.class));
                result = true;
                break;
//            case R.id.menu_sync_dropbox:
//                if (getActivity() instanceof MainActivity) {
//                    MainActivity parent = (MainActivity) getActivity();
//                    DropboxManager dropbox = new DropboxManager(parent, parent.mDropboxHelper, parent);
//                    dropbox.synchronizeDropbox();
//                    result = true;
//                }
//                break;
        }

        if (result) {
            return result;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    // End menu.

    @Override
    public void onResume() {
        super.onResume();
        // clear subTitle of ActionBar
//        ActionBarActivity activity = (ActionBarActivity) getActivity();
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null)
            activity.getSupportActionBar().setSubtitle(null);
        // start loader data
        startLoader();
    }

    // Context menu

    /**
     * Context menu for account entries.
     * @param menu
     * @param v
     * @param menuInfo
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        if (!(v instanceof ExpandableListView)) return;

        ExpandableListView.ExpandableListContextMenuInfo info =
                (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
        int type = ExpandableListView.getPackedPositionType(info.packedPosition);
        int groupPosition = ExpandableListView.getPackedPositionGroup(info.packedPosition);
        int childPosition = ExpandableListView.getPackedPositionChild(info.packedPosition);

        // ignore long-press on group items.
        if (type != ExpandableListView.PACKED_POSITION_TYPE_CHILD) return;

        // get adapter.
        AccountBillsExpandableAdapter accountsAdapter = (AccountBillsExpandableAdapter) mExpandableListView.getExpandableListAdapter();
        Object childItem = accountsAdapter.getChild(groupPosition, childPosition);
        QueryAccountBills account = (QueryAccountBills) childItem;

//        menu.setHeaderIcon(android.R.drawable.ic_menu_manage);
        menu.setHeaderTitle(account.getAccountName());
        String[] menuItems = getResources().getStringArray(R.array.context_menu_account_dashboard);
        for(String menuItem : menuItems) {
            menu.add(menuItem);
        }

        // balance account should work only for transaction accounts.
        AccountRepository accountRepository = new AccountRepository(getActivity());
        List<String> accountTypes = accountRepository.getTransactionAccountTypeNames();
        if (accountTypes.contains(account.getAccountType())) {
            menu.add(R.string.balance_account);
        }
    }

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
        boolean result = false;

        // get account id
        QueryAccountBills account = getSelectedAccount(item);
        if (account == null) return false;

        int accountId = account.getAccountId();

        // get the action
        String menuItemTitle = item.getTitle().toString();
        
        if (menuItemTitle.equalsIgnoreCase(getString(R.string.edit))) {
            Intent intent = new Intent(getActivity(), AccountListEditActivity.class);
            intent.putExtra(AccountListEditActivity.KEY_ACCOUNT_ID, accountId);
            intent.setAction(Intent.ACTION_EDIT);
            startActivity(intent);

            result = true;
        }
        if (menuItemTitle.equalsIgnoreCase(getString(R.string.balance_account))) {
            getBalanceAccountTask().startBalanceAccount(account);
        }

        return result;
    }

    // End context menu.

    // Private custom methods.

    /**
     * @param visible if visible set true show the listview; false show progressbar
     */
    private void setListViewAccountBillsVisible(boolean visible) {
        if (visible) {
            mExpandableListView.setVisibility(View.VISIBLE);
            prgAccountBills.setVisibility(View.GONE);
        } else {
            mExpandableListView.setVisibility(View.GONE);
            prgAccountBills.setVisibility(View.VISIBLE);
        }
    }

    private void addFooterExpandableListView(double curTotal, double curReconciled) {
        // manage footer list view
        if (linearFooter == null) {
            linearFooter = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.item_account_bills, null);
            // textview into layout
            txtFooterSummary = (TextView) linearFooter.findViewById(R.id.textViewItemAccountTotal);
            txtFooterSummaryReconciled = (TextView) linearFooter.findViewById(R.id.textViewItemAccountTotalReconciled);
            if(mHideReconciled) {
                txtFooterSummaryReconciled.setVisibility(View.GONE);
            }
            // set text
            TextView txtTextSummary = (TextView) linearFooter.findViewById(R.id.textViewItemAccountName);
            txtTextSummary.setText(R.string.summary);
            // invisibile image
            ImageView imgSummary = (ImageView) linearFooter.findViewById(R.id.imageViewAccountType);
            imgSummary.setVisibility(View.INVISIBLE);
            // set color textview
            txtTextSummary.setTextColor(Color.GRAY);
            txtFooterSummary.setTextColor(Color.GRAY);
            if(!mHideReconciled) {
                txtFooterSummaryReconciled.setTextColor(Color.GRAY);
            }
        }
        // remove footer
        mExpandableListView.removeFooterView(linearFooter);
        // set text
        txtTotalAccounts.setText(mCurrencyUtils.getBaseCurrencyFormatted(curTotal));
        txtFooterSummary.setText(txtTotalAccounts.getText());
        if(!mHideReconciled) {
            txtFooterSummaryReconciled.setText(mCurrencyUtils.getBaseCurrencyFormatted(curReconciled));
        }
        // add footer
        mExpandableListView.addFooterView(linearFooter, null, false);
    }

    private void setUpAccountsList(View view) {
        mExpandableListView = (ExpandableListView) view.findViewById(R.id.listViewAccountBills);

        // Handle clicking on an account.
        mExpandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                        int childPosition, long id) {
                QueryAccountBills selectedAccount = mAccountsByType.get(mAccountTypes.get(groupPosition))
                        .get(childPosition);
                if (selectedAccount == null) return false;

                int accountId = selectedAccount.getAccountId();

                MainActivity activity = (MainActivity) getActivity();
                if (activity == null) return false;

                String accountType = mAccountTypes.get(groupPosition);
                if (accountType.equalsIgnoreCase(getString(R.string.investment))) {
                    activity.showWatchlistFragment(accountId);
                } else {
                    activity.showAccountFragment(accountId);
                }
                return true;
            }
        });

        // store settings when groups are collapsed/expanded
        mExpandableListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int groupPosition) {
                // save collapsed group setting
                final boolean groupVisible = false;
                // save each group visibility into its own settings.
                AppSettings settings = new AppSettings(getActivity());
                String key = getSettingsKeyFromGroupPosition(groupPosition);
                // store value.
                settings.set(key, groupVisible);
            }
        });
        mExpandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                // save expanded group setting
                final boolean groupVisible = true;
                // save each group visibility into its own settings.
                AppSettings settings = new AppSettings(getActivity());
                String key = getSettingsKeyFromGroupPosition(groupPosition);
                // store value.
                settings.set(key, groupVisible);
            }
        });

        registerForContextMenu(mExpandableListView);
    }

    private String getSettingsKeyFromGroupPosition(int groupPosition) {
        // get group name from position
        String accountType = mAccountTypes.get(groupPosition);
        String key = getActivity().getString(PreferenceConstants.PREF_DASHBOARD_GROUP_VISIBLE);
        key += "-" + accountType;

        return key;
    }

    private void setVisibilityOfAccountGroups() {
        // set visibility of the account groups.
        AppSettings settings = new AppSettings(getActivity());
        // Expand groups based on their visibility settings.
        for (int i = 0; i < mAccountTypes.size(); i++) {
            // Check saved visibility settings. Some groups might be collapsed.
            String key = getSettingsKeyFromGroupPosition(i);
            Boolean expanded = settings.get(key, true);

            if(expanded) {
                mExpandableListView.expandGroup(i);
            }
        }
    }

    private void setUpMigrationButton(View view) {
        // check if there is a database at the old location.
        final DatabaseMigrator14To20 migrator = new DatabaseMigrator14To20(getActivity());
        boolean legacyDataExists = migrator.legacyDataExists();

        // hide option if there is no old database.
        if (!legacyDataExists) return;

        // otherwise show the options
        LinearLayout panel = (LinearLayout) view.findViewById(R.id.panelMigration);
        panel.setVisibility(View.VISIBLE);

        // handle events, etc.

        Button migrateDatabaseButton = (Button) view.findViewById(R.id.buttonMigrateDatabase);
        if (migrateDatabaseButton != null) {
            if (!migrator.legacyDataExists()) {

                // add handler
                OnClickListener migrateClickListener = new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        boolean migrationSuccess = migrator.migrateLegacyDatabase();
                        if (migrationSuccess) {
                            Toast.makeText(getActivity(), R.string.database_migrate_14_to_20_success,
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getActivity(), R.string.database_migrate_14_to_20_failure,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                };
                migrateDatabaseButton.setOnClickListener(migrateClickListener);
            } else {
                // hide migration notification.
                RobotoTextView textMigrate = (RobotoTextView) view.findViewById(R.id.textMigrateDatabase);
                textMigrate.setVisibility(View.GONE);

                migrateDatabaseButton.setVisibility(View.GONE);
            }

        }
    }

    private QueryAccountBills getSelectedAccount(android.view.MenuItem item){
        ExpandableListView.ExpandableListContextMenuInfo info = null;
        try {
            info = (ExpandableListView.ExpandableListContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException cex) {
            ExceptionHandler handler = new ExceptionHandler(getActivity(), this);
            handler.handle(cex, "Error casting context menu");
        }
        if (info == null) return null;

        int groupPos, childPos;
        int type = ExpandableListView.getPackedPositionType(info.packedPosition);
        if (type != ExpandableListView.PACKED_POSITION_TYPE_CHILD) return null;

        // Get the account.

        groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
        childPos = ExpandableListView.getPackedPositionChild(info.packedPosition);

        AccountBillsExpandableAdapter accountsAdapter = (AccountBillsExpandableAdapter) mExpandableListView.getExpandableListAdapter();
        QueryAccountBills account = null;
        try {
            account = (QueryAccountBills) accountsAdapter.getChild(groupPos, childPos);
        } catch (Exception e) {
            ExceptionHandler handler = new ExceptionHandler(getActivity(), this);
            handler.handle(e, "Error getting the selected account id");
        }

        return account;
    }

    private BalanceAccountTask mBalanceAccountTask;

    private BalanceAccountTask getBalanceAccountTask() {
        if (mBalanceAccountTask == null) {
            mBalanceAccountTask = new BalanceAccountTask(getActivity());
        }
        return mBalanceAccountTask;
    }

    private class AccountBillsExpandableAdapter
            extends BaseExpandableListAdapter {

        private Context mContext;

        public AccountBillsExpandableAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getGroupCount() {
            return mAccountTypes.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return mAccountsByType.get(mAccountTypes.get(groupPosition)).size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return mAccountTypes.get(groupPosition);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return mAccountsByType.get(mAccountTypes.get(groupPosition)).get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        /**
         * Creates a view for the group header row.
         * @param groupPosition
         * @param isExpanded
         * @param convertView
         * @param parent
         * @return
         */
        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            ViewHolderAccountBills holder;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.item_account_bills, null);

                holder = new ViewHolderAccountBills();

                holder.txtAccountName = (TextView) convertView.findViewById(R.id.textViewItemAccountName);
                holder.txtAccountName.setTypeface(null, Typeface.BOLD);

                holder.txtAccountTotal = (TextView) convertView.findViewById(R.id.textViewItemAccountTotal);
                holder.txtAccountTotal.setTypeface(null, Typeface.BOLD);

                holder.txtAccountReconciled = (TextView) convertView.findViewById(R.id.textViewItemAccountTotalReconciled);
                if(mHideReconciled) {
                    holder.txtAccountReconciled.setVisibility(View.GONE);
                } else {
                    holder.txtAccountReconciled.setTypeface(null, Typeface.BOLD);
                }

                holder.imgAccountType = (ImageView) convertView.findViewById(R.id.imageViewAccountType);

                convertView.setTag(holder);
            }
            holder = (ViewHolderAccountBills) convertView.getTag();

            String accountType = mAccountTypes.get(groupPosition);
            QueryAccountBills total = mTotalsByType.get(accountType);
            if (total != null) {
                // set account type value
                holder.txtAccountTotal.setText(mCurrencyUtils.getBaseCurrencyFormatted(total.getTotalBaseConvRate()));
                if(!mHideReconciled) {
                    holder.txtAccountReconciled.setText(mCurrencyUtils.getBaseCurrencyFormatted(total.getReconciledBaseConvRate()));
                }
                // set account name
                holder.txtAccountName.setText(total.getAccountName());
            }
            // set image depending on the account type
            if (!TextUtils.isEmpty(accountType)) {
                if(AccountTypes.CHECKING.toString().equalsIgnoreCase(accountType)){
                    holder.imgAccountType.setImageDrawable(getResources().getDrawable(R.drawable.ic_money_safe));
                } else if (AccountTypes.TERM.toString().equalsIgnoreCase(accountType)) {
                    holder.imgAccountType.setImageDrawable(getResources().getDrawable(R.drawable.ic_money_finance));
                } else if (AccountTypes.CREDIT_CARD.toString().equalsIgnoreCase(accountType)) {
                    holder.imgAccountType.setImageDrawable(getResources().getDrawable(R.drawable.ic_credit_card));
                } else if (AccountTypes.INVESTMENT.toString().equalsIgnoreCase(accountType)) {
                    holder.imgAccountType.setImageDrawable(getResources().getDrawable(R.drawable.portfolio));
                }
            }

            return convertView;
        }

        /**
         * Creates a view for the group item row.
         * @param groupPosition
         * @param childPosition
         * @param isLastChild
         * @param convertView
         * @param parent
         * @return
         */
        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                                 View convertView, ViewGroup parent) {
            ViewHolderAccountBills holder;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.item_account_bills, null);

                holder = new ViewHolderAccountBills();
                holder.txtAccountName = (TextView) convertView.findViewById(R.id.textViewItemAccountName);
                holder.txtAccountTotal = (TextView) convertView.findViewById(R.id.textViewItemAccountTotal);
                holder.txtAccountReconciled = (TextView) convertView.findViewById(R.id.textViewItemAccountTotalReconciled);
                holder.imgAccountType = (ImageView) convertView.findViewById(R.id.imageViewAccountType);

                holder.txtAccountTotal.setTypeface(null, Typeface.NORMAL);
                holder.imgAccountType.setVisibility(View.INVISIBLE);

                convertView.setTag(holder);
            }
            holder = (ViewHolderAccountBills) convertView.getTag();

            QueryAccountBills account = getAccountData(groupPosition, childPosition);

            // set account name
            holder.txtAccountName.setText(account.getAccountName());
            // import formatted
            String value = mCurrencyUtils.getCurrencyFormatted(account.getCurrencyId(), account.getTotal());
            // set amount value
            holder.txtAccountTotal.setText(value);

            // reconciled
            if(mHideReconciled) {
                holder.txtAccountReconciled.setVisibility(View.GONE);
            } else {
                value = mCurrencyUtils.getCurrencyFormatted(account.getCurrencyId(), account.getReconciled());
                holder.txtAccountReconciled.setText(value);
            }

            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        public QueryAccountBills getAccountData(int groupPosition, int childPosition) {
            String accountType = mAccountTypes.get(groupPosition);
            QueryAccountBills account = mAccountsByType.get(accountType).get(childPosition);

            return account;
        }

        private class ViewHolderAccountBills {
            TextView txtAccountName;
            TextView txtAccountTotal;
            TextView txtAccountReconciled;
            ImageView imgAccountType;
        }
    }

}
