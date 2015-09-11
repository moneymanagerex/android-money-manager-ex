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
package com.money.manager.ex.account;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.money.manager.ex.adapter.AllDataAdapter;
import com.money.manager.ex.adapter.AllDataViewHolder;
import com.money.manager.ex.businessobjects.AccountService;
import com.money.manager.ex.common.AllDataListFragment;
import com.money.manager.ex.common.MmexCursorLoader;
import com.money.manager.ex.core.DateRange;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.database.WhereClauseGenerator;
import com.money.manager.ex.database.WhereStatementGenerator;
import com.money.manager.ex.domainmodel.Account;
import com.money.manager.ex.transactions.EditTransactionActivity;
import com.money.manager.ex.Constants;
import com.money.manager.ex.home.MainActivity;
import com.money.manager.ex.R;
import com.money.manager.ex.transactions.EditTransactionActivityConstants;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.database.QueryAccountBills;
import com.money.manager.ex.database.QueryAllData;
import com.money.manager.ex.database.TableAccountList;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.settings.LookAndFeelSettings;
import com.money.manager.ex.settings.PreferenceConstants;
import com.money.manager.ex.utils.CalendarUtils;
import com.money.manager.ex.utils.DateUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Checking account fragment.
 * Shows the list of transactions, etc.
 * @author a.lazzari
 */
public class AccountTransactionsFragment
        extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>, ICalculateRunningBalanceTaskCallbacks {

    private static final String KEY_CONTENT = "AccountTransactionsFragment:AccountId";
    private static final int ID_LOADER_SUMMARY = 2;

    private final String LOGCAT = this.getClass().getSimpleName();

    private AllDataListFragment mAllDataListFragment;
    private Integer mAccountId = null;
    private String mFragmentName;
    private double mAccountBalance = 0, mAccountReconciled = 0;
    private TableAccountList mAccountList;
    // Controls
    private TextView txtAccountBalance, txtAccountReconciled, txtAccountDifference;
    private ImageView imgAccountFav, imgGotoAccount;
    private Activity mActivity;
    private BigDecimal[] balances;

    // filter
    DateRange mDateRange;

    /**
     * @param accountId Id of the Account to be displayed
     * @return initialized instance of Account Fragment.
     */
    public static AccountTransactionsFragment newInstance(int accountId) {
        AccountTransactionsFragment fragment = new AccountTransactionsFragment();
        fragment.mAccountId = accountId;

        // set name of child fragment
        fragment.setFragmentName(AccountTransactionsFragment.class.getSimpleName() + "_" +
                Integer.toString(accountId));

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_CONTENT)) {
            mAccountId = savedInstanceState.getInt(KEY_CONTENT);
        }

        // Set the default period.
        String period = new AppSettings(getContext()).getShowTransaction();
        DateUtils dateUtils = new DateUtils(getContext());
        mDateRange = dateUtils.getDateRangeForPeriod(period);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // Keep the direct reference to the activity to avoing null exceptions on getActivity().
        // http://stackoverflow.com/questions/6215239/getactivity-returns-null-in-fragment-function
        if (context instanceof Activity) {
            mActivity = (Activity) context;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // restart loader
        loadTransactions();
    }

    // Menu

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        // Accounts list dropdown in toolbar.
        // Ref: http://stackoverflow.com/questions/11377760/adding-spinner-to-actionbar-not-navigation
        // Add options available only in account transactions list(s).
        inflater.inflate(R.menu.menu_account_transactions, menu);
        initAccountsDropdown(menu);

        // add the date picker.
        inflater.inflate(R.menu.menu_period_picker_transactions, menu);

        // Transaction Type picker
        // todo: inflater.inflate(R.menu.menu_transaction_types_selector, menu);
//        initTransactionTypeDropdown(menu);

        // call create option menu of fragment
        mAllDataListFragment.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        //force show add transaction
        MenuItem itemAddTransaction = menu.findItem(R.id.menu_add_transaction_account);
        if (itemAddTransaction != null) itemAddTransaction.setVisible(true);
        //manage dual panel
        if (getActivity() != null && getActivity() instanceof MainActivity) {
            MainActivity activity = (MainActivity) getActivity();
            if (!activity.isDualPanel()) {
                //hide dropbox toolbar
                MenuItem itemDropbox = menu.findItem(R.id.menu_sync_dropbox);
                if (itemDropbox != null) itemDropbox.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                // hide menu open database
                MenuItem itemOpenDatabase = menu.findItem(R.id.menu_open_database);
                if (itemOpenDatabase != null) {
                    //itemOpenDatabase.setVisible(isShownOpenDatabaseItemMenu());
                    itemOpenDatabase.setShowAsAction(!itemDropbox.isVisible()
                            ? MenuItem.SHOW_AS_ACTION_ALWAYS : MenuItem.SHOW_AS_ACTION_NEVER);
                }

                //hide dash board
                MenuItem itemDashboard = menu.findItem(R.id.menu_dashboard);
                if (itemDashboard != null)
                    itemDashboard.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            }
        }

        // select the current account?
        selectCurrentAccount(menu);

        selectCurrentPeriod(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean result;

        result = datePeriodItemSelected(item);
        if (result) return result;

        switch (item.getItemId()) {
            case R.id.menu_add_transaction_account:
                startCheckingAccountActivity();
                result = true;
                break;
            case R.id.menu_export_to_csv:
                if (mAllDataListFragment != null && mAccountList != null)
                    mAllDataListFragment.exportDataToCSVFile(mAccountList.getAccountName());
                result = true;
                break;

            default:
                result = false;
                break;
        }

        // If not consumed here (true), send for further processing to the parent.
        if (result) {
            return result;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    // End menu.

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_CONTENT)) {
            mAccountId = savedInstanceState.getInt(KEY_CONTENT);
        }
        if (container == null) {
            return null;
        }
        // inflate layout
        View view = inflater.inflate(R.layout.account_fragment, container, false);

        // take object AccountList
        if (mAccountList == null) {
            reloadAccountInfo();
        }

        ViewGroup header = (ViewGroup) inflater.inflate(R.layout.account_header_fragment, null, false);
        // take reference text view from layout
        txtAccountBalance = (TextView) header.findViewById(R.id.textViewAccountBalance);
        txtAccountReconciled = (TextView) header.findViewById(R.id.textViewAccountReconciled);
        txtAccountDifference = (TextView) header.findViewById(R.id.textViewDifference);
        // favorite icon
        imgAccountFav = (ImageView) header.findViewById(R.id.imageViewAccountFav);
        // set listener click on favorite icon for change image
        imgAccountFav.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // set status account
                mAccountList.setFavoriteAcct(!(mAccountList.isFavoriteAcct()));
                // populate content values for update
                ContentValues values = new ContentValues();
                values.put(TableAccountList.FAVORITEACCT, mAccountList.getFavoriteAcct());
                // update
                if (getActivity().getContentResolver().update(mAccountList.getUri(), values, TableAccountList.ACCOUNTID + "=?",
                        new String[]{Integer.toString(mAccountId)}) != 1) {
                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.db_update_failed), Toast.LENGTH_LONG).show();
                } else {
                    setImageViewFavorite();
                }
            }
        });
        // goto account
        imgGotoAccount = (ImageView) header.findViewById(R.id.imageViewGotoAccount);
        imgGotoAccount.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AccountEditActivity.class);
                intent.putExtra(AccountEditActivity.KEY_ACCOUNT_ID, mAccountId);
                intent.setAction(Intent.ACTION_EDIT);
                startActivity(intent);
            }
        });

        // Transactions
        showTransactionsFragment(header);

        // refresh user interface
        if (mAccountList != null) {
//            mAccountName = mAccountList.getAccountName();
            setImageViewFavorite();
        }

        setHasOptionsMenu(true);

        return view;
    }

    private void reloadAccountInfo() {
        AccountService service = new AccountService(getActivity().getApplicationContext());
        mAccountList = service.getTableAccountList(mAccountId);
    }

    // Loader events.

    /**
     * Start Loader to retrieve data
     */
    public void loadTransactions() {
        if (mAllDataListFragment != null) {
            Bundle arguments = prepareArgsForChildFragment();
            mAllDataListFragment.loadData(arguments);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case ID_LOADER_SUMMARY:
                // Account summary (balances).
                return new MmexCursorLoader(getActivity(),
                    new QueryAccountBills(getActivity()).getUri(),
                    null,
                    QueryAccountBills.ACCOUNTID + "=?",
                    new String[] { Integer.toString(mAccountId) },
                    null);
        }
        return null;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is already handled in AllDataListFragment.
//        switch (loader.getId()) {
//            case ID_LOADER_SUMMARY:
//                mAdapter.swapCursor(null);
//                break;
//        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case ID_LOADER_SUMMARY:
                if (data != null && data.moveToFirst()) {
                    mAccountBalance = data.getDouble(data.getColumnIndex(QueryAccountBills.TOTAL));
                    mAccountReconciled = data.getDouble(data.getColumnIndex(QueryAccountBills.RECONCILED));
                } else {
                    mAccountBalance = 0;
                    mAccountReconciled = 0;
                }
                // show balance values
                setTextViewBalance();
                break;

            case AllDataListFragment.ID_LOADER_ALL_DATA_DETAIL:
                // Notification received from AllDataListFragment.
                // Once the transactions are loaded, load the summary data.
                getLoaderManager().restartLoader(ID_LOADER_SUMMARY, null, this);
                // load/reset running balance
                reloadRunningBalance(data);

                break;
        }
    }

    // end loader events

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mAccountId != null) {
            outState.putInt(KEY_CONTENT, mAccountId);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onTaskComplete(BigDecimal[] balances) {
        this.balances = balances;
        // Update the UI controls
//        this.notifyDataSetChanged();
        updateVisibleRows();
    }

    // Private

    /**
     * Prepare SQL query for record selection.
     * @return bundle with query
     */
    private Bundle prepareArgsForChildFragment() {
        // compose selection and sort
        ArrayList<String> selection = new ArrayList<>();
        selection.add("(" + QueryAllData.TOACCOUNTID + "=" + Integer.toString(mAccountId) +
            " OR " + QueryAllData.ACCOUNTID + "=" + Integer.toString(mAccountId) + ")");

//        WhereClauseGenerator whereClause = new WhereClauseGenerator(getContext());
//        ArrayList<String> periodClauses = whereClause.getWhereClausesForPeriod(period);
//        selection.addAll(periodClauses);
        WhereStatementGenerator where = new WhereStatementGenerator();
        where.addStatement(QueryAllData.Date, ">=", mDateRange.dateFrom);
        where.addStatement(QueryAllData.Date, "<=", mDateRange.dateFrom);

        // create a bundle to returns
        Bundle args = new Bundle();
        args.putStringArrayList(AllDataListFragment.KEY_ARGUMENTS_WHERE, selection);
        args.putString(AllDataListFragment.KEY_ARGUMENTS_SORT,
                QueryAllData.Date + " DESC, " + QueryAllData.TransactionType + ", " +
                        QueryAllData.ID + " DESC");

        return args;
    }

    /**
     * refresh UI, show favorite icon
     */
    private void setImageViewFavorite() {
        if (mAccountList.isFavoriteAcct()) {
            imgAccountFav.setBackgroundResource(R.drawable.ic_star);
        } else {
            imgAccountFav.setBackgroundResource(R.drawable.ic_star_outline);
        }
    }

    /**
     * Show the account balances (current & reconciled) in the header.
     */
    private void setTextViewBalance() {
        // Reload account info as it can be changed via dropdown. Need a currency info here.
        reloadAccountInfo();

        // write account balance
        if (mAccountList != null) {
            CurrencyService currencyService = new CurrencyService(getActivity().getApplicationContext());

            txtAccountBalance.setText(currencyService.getCurrencyFormatted(mAccountList.getCurrencyId(), mAccountBalance));
            txtAccountReconciled.setText(currencyService.getCurrencyFormatted(mAccountList.getCurrencyId(), mAccountReconciled));
            txtAccountDifference.setText(currencyService.getCurrencyFormatted(mAccountList.getCurrencyId(), mAccountReconciled - mAccountBalance));
        }
    }

    /**
     * start the activity of transaction management
     */
    private void startCheckingAccountActivity() {
        this.startCheckingAccountActivity(null);
    }

    /**
     * start the activity of transaction management
     *
     * @param transId null set if you want to do a new transaction, or transaction id
     */
    private void startCheckingAccountActivity(Integer transId) {
        // create intent, set Account ID
        Intent intent = new Intent(getActivity(), EditTransactionActivity.class);
        intent.putExtra(EditTransactionActivityConstants.KEY_ACCOUNT_ID, mAccountId);
        // check transId not null
        if (transId != null) {
            intent.putExtra(EditTransactionActivityConstants.KEY_TRANS_ID, transId);
            intent.setAction(Intent.ACTION_EDIT);
        } else {
            intent.setAction(Intent.ACTION_INSERT);
        }
        // launch activity
        startActivity(intent);
    }

    public String getFragmentName() {
        return mFragmentName;
    }

    public void setFragmentName(String mFragmentName) {
        this.mFragmentName = mFragmentName;
    }

    private void showTransactionsFragment(ViewGroup header) {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();

        mAllDataListFragment = AllDataListFragment.newInstance(mAccountId);

        // set arguments and settings of fragment
        mAllDataListFragment.setArguments(prepareArgsForChildFragment());
        if (header != null) mAllDataListFragment.setListHeader(header);
        mAllDataListFragment.setShownBalance(PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getBoolean(getString(PreferenceConstants.PREF_TRANSACTION_SHOWN_BALANCE), false));
        mAllDataListFragment.setAutoStarLoader(false);
        mAllDataListFragment.setSearResultFragmentLoaderCallbacks(this);

        // add fragment
        transaction.replace(R.id.fragmentContent, mAllDataListFragment, getFragmentName());
        transaction.commit();
    }

    // Menu

    private boolean datePeriodItemSelected(MenuItem item) {
        LookAndFeelSettings settings = new AppSettings(getActivity()).getLookAndFeelSettings();
        int resourceId;

        switch (item.getItemId()) {
            case R.id.menu_today:
                resourceId = R.string.today;
                break;
            case R.id.menu_last7days:
                resourceId = R.string.last7days;
                break;
            case R.id.menu_last15days:
                resourceId = R.string.last15days;
                break;
            case R.id.menu_current_month:
                resourceId = R.string.current_month;
                break;
            case R.id.menu_last30days:
                resourceId = R.string.last30days;
                break;
            case R.id.menu_last3months:
                resourceId = R.string.last3months;
                break;
            case R.id.menu_last6months:
                resourceId = R.string.last6months;
                break;
            case R.id.menu_current_year:
                resourceId = R.string.current_year;
                break;
            case R.id.menu_future_transactions:
                resourceId = R.string.future_transactions;
                break;
            case R.id.menu_all_time:
                resourceId = R.string.all_time;
                break;
            default:
                return false;
        }
        settings.setShowTransactions(resourceId);

        // Save the selected period.
        DateUtils dateUtils = new DateUtils(getContext());
        mDateRange = dateUtils.getDateRangeForPeriod(resourceId);

        //check item
        item.setChecked(true);
//        mPeriodIndex = item.getItemId();

        loadTransactions();

        return true;
    }

    private Spinner getAccountsSpinner(Menu menu) {
        Spinner spinner = null;

        MenuItem item = menu.findItem(R.id.menuAccountSelector);
        if (item != null) {
            spinner = (Spinner) MenuItemCompat.getActionView(item);
        }

        return spinner;
    }

    private void initAccountsDropdown(Menu menu) {
        // Hide the toolbar title?
//        AppCompatActivity toolbarActivity = (AppCompatActivity) getActivity();
//        ActionBar actionBar = toolbarActivity.getSupportActionBar();
//        toolbarActivity.setSupportActionBar(toolbar);
//        actionBar.setDisplayShowTitleEnabled(false);
//        actionBar.setLogo(R.drawable.ic_logo_money_manager_ex);
//        actionBar.setDisplayUseLogoEnabled(true);
//        actionBar.setDisplayShowHomeEnabled(true);

        // Load accounts into the list.
        Spinner spinner = getAccountsSpinner(menu);
        if (spinner == null) return;

        loadAccountsToSpinner(getActivity(), spinner);

        // handle switching of accounts.
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // switch account.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(i);
                Account account = new Account();
                account.loadFromCursor(cursor);

                int accountId = account.getId();
                if (accountId != mAccountId) {
                    // switch account. Reload transactions.
                    mAccountId = accountId;
                    mAllDataListFragment.AccountId = accountId;
                    mAllDataListFragment.loadData(prepareArgsForChildFragment());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    /**
     * To be used for the Transaction Type filter.
     * @param menu
     */
    private void initTransactionTypeDropdown(Menu menu) {
        MenuItem item = menu.findItem(R.id.menuAccountSelector);
        Spinner spinner = null;
        if (item != null) {
            spinner = (Spinner) MenuItemCompat.getActionView(item);
        }
        if (spinner == null) return;

        // todo: fill statuses
        //loadAccountsToSpinner(getActivity(), spinner);

        // handle switching of accounts.
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // switch account.
                Spinner spinner1 = (Spinner) adapterView;
//                Account account = getAccountAtPosition(spinner1, i);
//                int accountId = account.getAccountId();
//                if (accountId != mAccountId) {
//                    // switch account. Reload transactions.
//                    mAccountId = accountId;
//                    mAllDataListFragment.AccountId = accountId;
//                    mAllDataListFragment.loadData(prepareArgsForChildFragment());
//                }
                // todo: handle change.
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    /**
     * Show the current account selected in the accounts dropdown.
     * @param menu The toolbar/menu that contains the dropdown.
     */
    private void selectCurrentAccount(Menu menu) {
        Spinner spinner = getAccountsSpinner(menu);
        if (spinner == null) return;

        // find account
        SimpleCursorAdapter adapter = (SimpleCursorAdapter) spinner.getAdapter();
        if (adapter == null) return;

        Cursor cursor = adapter.getCursor();
        int position = Constants.NOT_SET;

        for (int i = 0; i < adapter.getCount(); i++) {
            cursor.moveToPosition(i);
            String accountIdString = cursor.getString(cursor.getColumnIndex(TableAccountList.ACCOUNTID));
            int accountId = Integer.parseInt(accountIdString);
            if (accountId == mAccountId) {
                position = i;
                break;
            }
        }

        spinner.setSelection(position);
    }

    private void selectCurrentPeriod(Menu menu) {
        MenuItem item = menu.findItem(R.id.menu_period);
        if (item == null) return;

        Context context = getContext();
        if (context == null) {
            context = mActivity;
        }

        SubMenu subMenu = item.getSubMenu();

        // on init, mark the default item as checked
        AppSettings settings = new AppSettings(mActivity);
        String preference = settings.getLookAndFeelSettings().getShowTransactions();

        int id = Constants.NOT_SET;
        if (preference.equals(context.getString(R.string.last7days))) {
            id = R.id.menu_last7days;
        } else if (preference.equals(context.getString(R.string.last15days))) {
            id = R.id.menu_last15days;
        } else if (preference.equals(context.getString(R.string.current_month))) {
            id = R.id.menu_current_month;
        } else if (preference.equals(context.getString(R.string.last30days))) {
            id = R.id.menu_last30days;
        } else if (preference.equals(context.getString(R.string.last3months))) {
            id = R.id.menu_last3months;
        } else if (preference.equals(context.getString(R.string.last6months))) {
            id = R.id.menu_last6months;
        } else if (preference.equals(context.getString(R.string.current_year))) {
            id = R.id.menu_current_year;
        } else if (preference.equals(context.getString(R.string.all_time))) {
            id = R.id.menu_all_time;
        }

        // set the date range
        DateUtils dateUtils = new DateUtils(getContext());
        mDateRange = dateUtils.getDateRangeForPeriod(id);

        MenuItem itemToMark = subMenu.findItem(id);
        if (itemToMark == null) return;

        itemToMark.setChecked(true);
    }

    private void loadAccountsToSpinner(Context context, Spinner spinner) {
        if (spinner == null) return;
        if (context == null) {
            Log.e(LOGCAT, "Context not sent when loading accounts");
            return;
        }

        AccountService service = new AccountService(context);
        Core core = new Core(context.getApplicationContext());

        Cursor cursor = service.getCursor(core.getAccountsOpenVisible(),
                core.getAccountFavoriteVisible(), service.getTransactionAccountTypeNames());

        int[] adapterRowViews = new int[] { android.R.id.text1 };

        SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(context,
                android.R.layout.simple_spinner_item,
                cursor,
                new String[] { TableAccountList.ACCOUNTNAME, TableAccountList.ACCOUNTID },
                adapterRowViews,
                SimpleCursorAdapter.NO_SELECTION);
        cursorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(cursorAdapter);
    }

    // Running balance

    /**
     * Refreshes the running balance.
     * @param cursor
     */
    private void reloadRunningBalance(Cursor cursor) {
//        if (mAccountId == Constants.NOT_SET) return;
        this.balances = null;
        this.populateRunningBalance(cursor);
    }

    private void populateRunningBalance(Cursor c) {
//        AllDataAdapter adapter = (AllDataAdapter) mAllDataListFragment.getListAdapter();

        CalculateRunningBalanceTask2 task = new CalculateRunningBalanceTask2(
                getContext(), this.balances, c, this.mAccountId, mDateRange.dateFrom, this);
        task.execute();

        // the result is received in #onTaskComplete.
    }

    private void updateVisibleRows() {
        // This is called when the balances are loaded.
        ListView listView = mAllDataListFragment.getListView();
        int start = listView.getFirstVisiblePosition();
        int end = listView.getLastVisiblePosition();

        AccountService accountService = new AccountService(getContext());
        int currencyId = accountService.loadCurrencyId(this.mAccountId);
        CurrencyService currencyService = new CurrencyService(getContext());

        for (int i = start; i <= end; i++) {
            View view = listView.getChildAt(i);
            AllDataViewHolder holder = (AllDataViewHolder) view.getTag();
            int row = i;
            // the first row can be the header.
            if (mAllDataListFragment.isShownHeader()) {
                row = i + 1;
            }

            if (holder != null && this.balances.length > row) {
                BigDecimal currentBalance = this.balances[row];
                String balanceFormatted = currencyService.getCurrencyFormatted(currencyId,
                        currentBalance.doubleValue());

                holder.txtBalance.setText(balanceFormatted);
                holder.txtBalance.setVisibility(View.VISIBLE);
            }
        }
    }

}
