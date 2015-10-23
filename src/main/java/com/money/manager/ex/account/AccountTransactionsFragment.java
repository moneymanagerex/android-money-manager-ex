/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
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
package com.money.manager.ex.account;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.money.manager.ex.datalayer.AccountRepository;
import com.money.manager.ex.servicelayer.AccountService;
import com.money.manager.ex.common.AllDataListFragment;
import com.money.manager.ex.common.MmexCursorLoader;
import com.money.manager.ex.core.DateRange;
import com.money.manager.ex.core.DefinedDateRange;
import com.money.manager.ex.core.DefinedDateRangeName;
import com.money.manager.ex.core.DefinedDateRanges;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.database.ISplitTransactionsDataset;
import com.money.manager.ex.database.WhereStatementGenerator;
import com.money.manager.ex.domainmodel.Account;
import com.money.manager.ex.transactions.EditTransactionActivity;
import com.money.manager.ex.Constants;
import com.money.manager.ex.home.MainActivity;
import com.money.manager.ex.R;
import com.money.manager.ex.transactions.EditTransactionActivityConstants;
import com.money.manager.ex.database.QueryAccountBills;
import com.money.manager.ex.database.QueryAllData;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.settings.LookAndFeelSettings;
import com.money.manager.ex.settings.PreferenceConstants;
import com.money.manager.ex.utils.DateUtils;

import java.util.HashMap;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

/**
 * Checking account fragment. Shows the list of transactions.
 */
public class AccountTransactionsFragment
    extends Fragment
    implements LoaderManager.LoaderCallbacks<Cursor>, ICalculateRunningBalanceTaskCallbacks {

    private static final String ARG_ACCOUNT_ID = "arg:accountId";
    private static final String KEY_CONTENT = "AccountTransactionsFragment:AccountId";
    private static final int ID_LOADER_SUMMARY = 2;

    /**
     * @param accountId Id of the Account to be displayed
     * @return initialized instance of Account Fragment.
     */
    public static AccountTransactionsFragment newInstance(int accountId) {
        AccountTransactionsFragment fragment = new AccountTransactionsFragment();

        Bundle args = new Bundle();
        args.putInt(ARG_ACCOUNT_ID, accountId);
        fragment.setArguments(args);

        // set name of child fragment
        fragment.setFragmentName(AccountTransactionsFragment.class.getSimpleName() + "_" +
            Integer.toString(accountId));

        return fragment;
    }

    private AllDataListFragment mAllDataListFragment;
    private Integer mAccountId = null;
    private String mFragmentName;
    private Money mAccountBalance = MoneyFactory.fromString("0"),
            mAccountReconciled = MoneyFactory.fromString("0");
    private Account mAccount;
    // Controls
    private TextView txtAccountBalance, txtAccountReconciled, txtAccountDifference;
    private ImageView imgAccountFav, imgGotoAccount;
    private Activity mActivity;
    ViewGroup mListHeader;

    // filter
    DateRange mDateRange;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get account id from the arguments first.
        mAccountId = getArguments().getInt(ARG_ACCOUNT_ID);

        // this is already in onCreateView.
//        if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_CONTENT)) {
//            mAccountId = savedInstanceState.getInt(KEY_CONTENT);
//        }

        // Set the default period.
        DefinedDateRangeName rangeName = new AppSettings(getActivity()).getLookAndFeelSettings()
                .getShowTransactions();
        DefinedDateRanges ranges = new DefinedDateRanges(getActivity());
        DefinedDateRange range = ranges.get(rangeName);

        // todo: replace this with implemented period on the range object.
        DateUtils dateUtils = new DateUtils(getContext());
        mDateRange = dateUtils.getDateRangeForPeriod(range.nameResourceId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_CONTENT)) {
            mAccountId = savedInstanceState.getInt(KEY_CONTENT);
        }

        if (container == null) return null;

        // inflate layout
        View view = inflater.inflate(R.layout.account_fragment, container, false);

        // take object AccountList
        if (mAccount == null) {
            reloadAccountInfo();
        }

        initializeListHeader(inflater);

        // Transactions
        showTransactionsFragment(mListHeader);

        // refresh user interface
        if (mAccount != null) {
            setImageViewFavorite();
        }

        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // hide the title
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        initializeAccountsSelector();
        selectCurrentAccount();

        // restart loader
        loadTransactions();
    }

    // Menu

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        // hide the title
        if (getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Accounts list dropdown in toolbar.
        // Ref: http://stackoverflow.com/questions/11377760/adding-spinner-to-actionbar-not-navigation
        // Add options available only in account transactions list(s).
//        inflater.inflate(R.menu.menu_account_spinner, menu);
//        initAccountsDropdown(menu);

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
                if (mAllDataListFragment != null && mAccount != null)
                    mAllDataListFragment.exportDataToCSVFile(mAccount.getName());
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

    // Loader events.

    /**
     * Start Loader to retrieve data
     */
    public void loadTransactions() {
        if (mAllDataListFragment != null) {
            Bundle arguments = prepareQuery();
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
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case ID_LOADER_SUMMARY:
                if (data != null && data.moveToFirst()) {
                    String balance = Double.toString(data.getDouble(data.getColumnIndex(QueryAccountBills.TOTAL)));
                    mAccountBalance = MoneyFactory.fromString(balance);
                    String reconciled = Double.toString(data.getDouble(data.getColumnIndex(QueryAccountBills.RECONCILED)));
                    mAccountReconciled = MoneyFactory.fromString(reconciled);
                } else {
                    mAccountBalance = MoneyFactory.fromString("0");
                    mAccountReconciled = MoneyFactory.fromString("0");
                }
                // show balance values
                setTextViewBalance();
                break;

            case AllDataListFragment.ID_LOADER_ALL_DATA_DETAIL:
                // Notification received from AllDataListFragment.
                // Once the transactions are loaded, load the summary data.
                getLoaderManager().restartLoader(ID_LOADER_SUMMARY, null, this);
                // load/reset running balance
                populateRunningBalance();

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
    public void onTaskComplete(HashMap<Integer, Money> balances) {
        // Update the UI controls
        mAllDataListFragment.displayRunningBalances(balances);
    }

    // Private

    private void initializeAccountsSelector() {
        ActionBar actionBar = getActionBar();
        if (actionBar == null) return;

        actionBar.setDisplayShowTitleEnabled(false);

        actionBar.setCustomView(R.layout.spinner);
        actionBar.setDisplayShowCustomEnabled(true);

        Spinner spinner = getAccountsSpinner();
        if (spinner == null) return;

        loadAccountsInto(spinner);
    }

    private void reloadAccountInfo() {
        AccountRepository repo = new AccountRepository(getActivity());
        mAccount = repo.load(mAccountId);
    }

    /**
     * Refreshes the running balance.
     */
    public void populateRunningBalance() {
        Bundle arguments = prepareQuery();

        CalculateRunningBalanceTask2 task = new CalculateRunningBalanceTask2(
            getContext(), this.mAccountId, mDateRange.dateFrom, this, arguments);
        task.execute();

        // the result is received in #onTaskComplete.
    }

    /**
     * Prepare SQL query for record selection.
     * @return bundle with query
     */
    private Bundle prepareQuery() {
        WhereStatementGenerator where = new WhereStatementGenerator();

//        where.addStatement("(" + QueryAllData.TOACCOUNTID + "=" + Integer.toString(mAccountId) +
//            " OR " + QueryAllData.ACCOUNTID + "=" + Integer.toString(mAccountId) + ")");
        where.addStatement(
            where.concatenateOr(
                where.getStatement(ISplitTransactionsDataset.TOACCOUNTID, "=", mAccountId),
                where.getStatement(ISplitTransactionsDataset.ACCOUNTID, "=", mAccountId)
        ));

        where.addStatement(QueryAllData.Date, ">=", DateUtils.getIsoStringDate(mDateRange.dateFrom));
        where.addStatement(QueryAllData.Date, "<=", DateUtils.getIsoStringDate(mDateRange.dateTo));

        // create a bundle to returns
        Bundle args = new Bundle();
        args.putString(AllDataListFragment.KEY_ARGUMENTS_WHERE, where.getWhere());
        args.putString(AllDataListFragment.KEY_ARGUMENTS_SORT,
                QueryAllData.Date + " DESC, " +
                        QueryAllData.TransactionType + ", " +
                        QueryAllData.ID + " DESC");

        return args;
    }

    /**
     * refresh UI, show favorite icon
     */
    private void setImageViewFavorite() {
        if (mAccount.getFavorite()) {
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
        if (mAccount != null) {
            CurrencyService currencyService = new CurrencyService(getActivity().getApplicationContext());

            txtAccountBalance.setText(currencyService.getCurrencyFormatted(mAccount.getCurrencyId(), mAccountBalance));
            txtAccountReconciled.setText(currencyService.getCurrencyFormatted(mAccount.getCurrencyId(), mAccountReconciled));
            txtAccountDifference.setText(currencyService.getCurrencyFormatted(mAccount.getCurrencyId(),
                    mAccountReconciled.subtract(mAccountBalance)));
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
        mAllDataListFragment.setArguments(prepareQuery());
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
        int stringId;

        int itemId = item.getItemId();

        DefinedDateRanges dateRanges = new DefinedDateRanges(getActivity());
        DefinedDateRange range = dateRanges.getByMenuId(itemId);
        if (range == null) return false;
        stringId = range.nameResourceId;

//        switch (itemId) {
//            case R.id.menu_today:
//                stringId = R.string.today;
//                break;
//            case R.id.menu_last7days:
//                stringId = R.string.last7days;
//                break;
//            case R.id.menu_last15days:
//                stringId = R.string.last15days;
//                break;
//            case R.id.menu_current_month:
//                stringId = R.string.current_month;
//                break;
//            case R.id.menu_last30days:
//                stringId = R.string.last30days;
//                break;
//            case R.id.menu_last3months:
//                stringId = R.string.last3months;
//                break;
//            case R.id.menu_last6months:
//                stringId = R.string.last6months;
//                break;
//            case R.id.menu_current_year:
//                stringId = R.string.current_year;
//                break;
//            case R.id.menu_future_transactions:
//                stringId = R.string.future_transactions;
//                break;
//            case R.id.menu_all_time:
//                stringId = R.string.all_time;
//                break;
//            default:
//                return false;
//        }

        LookAndFeelSettings settings = new AppSettings(getActivity()).getLookAndFeelSettings();
        settings.setShowTransactions(range.key);

        // Save the selected period.
        DateUtils dateUtils = new DateUtils(getContext());
        mDateRange = dateUtils.getDateRangeForPeriod(stringId);

        //check item
        item.setChecked(true);
//        mPeriodIndex = item.getItemId();

        loadTransactions();

        return true;
    }

    private Spinner getAccountsSpinner() {
//        Spinner spinner = null;
//
//        MenuItem item = menu.findItem(R.id.menuAccountSelector);
//        if (item != null) {
//            spinner = (Spinner) MenuItemCompat.getActionView(item);
//        }
//
//        return spinner;

        // get from custom view, not the menu.

        ActionBar actionBar = getActionBar();
        if (actionBar == null) return null;

        Spinner spinner = (Spinner) actionBar.getCustomView().findViewById(R.id.spinner);
        return spinner;
    }

    private ActionBar getActionBar() {
        if (!(getActivity() instanceof AppCompatActivity)) return null;

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        ActionBar actionBar = activity.getSupportActionBar();
        return actionBar;
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
        Spinner spinner = getAccountsSpinner();
        if (spinner == null) return;

        AccountService accountService = new AccountService(getActivity());
        accountService.loadTransactionAccountsToSpinner(spinner);

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
                    mAllDataListFragment.loadData(prepareQuery());
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
        //loadTransactionAccountsToSpinner(getActivity(), spinner);

        // handle switching of accounts.
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // switch account.
//                Spinner spinner1 = (Spinner) adapterView;
//                Account account = getAccountAtPosition(spinner1, i);
//                int accountId = account.getAccountId();
//                if (accountId != mAccountId) {
//                    // switch account. Reload transactions.
//                    mAccountId = accountId;
//                    mAllDataListFragment.AccountId = accountId;
//                    mAllDataListFragment.loadData(prepareQuery());
//                }
                // todo: handle change.
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void initializeListHeader(LayoutInflater inflater) {
        mListHeader = (ViewGroup) inflater.inflate(R.layout.account_header_fragment, null, false);
        // take reference text view from layout
        txtAccountBalance = (TextView) mListHeader.findViewById(R.id.textViewAccountBalance);
        txtAccountReconciled = (TextView) mListHeader.findViewById(R.id.textViewAccountReconciled);
        txtAccountDifference = (TextView) mListHeader.findViewById(R.id.textViewDifference);
        // favorite icon
        imgAccountFav = (ImageView) mListHeader.findViewById(R.id.imageViewAccountFav);

        // set listener click on favorite icon for change image
        imgAccountFav.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // set status account
                mAccount.setFavorite(!(mAccount.getFavorite()));
                // populate content values for update
//                ContentValues values = new ContentValues();
//                values.put(Account.FAVORITEACCT, mAccount.getFavorite());
                AccountRepository repo = new AccountRepository(getActivity());
                boolean updated = repo.update(mAccount);
                // update
//                if (getActivity().getContentResolver().update(repo.getUri(),
//                    values,
//                    Account.ACCOUNTID + "=?",
//                    new String[]{Integer.toString(mAccountId)}) != 1) {
                if (!updated) {
                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.db_update_failed), Toast.LENGTH_LONG).show();
                } else {
                    setImageViewFavorite();
                }
            }
        });

        // goto account
        imgGotoAccount = (ImageView) mListHeader.findViewById(R.id.imageViewGotoAccount);
        imgGotoAccount.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AccountEditActivity.class);
                intent.putExtra(AccountEditActivity.KEY_ACCOUNT_ID, mAccountId);
                intent.setAction(Intent.ACTION_EDIT);
                startActivity(intent);
            }
        });
    }

    private void loadAccountsInto(final Spinner spinner) {
        // Load accounts into the list.
//        Menu menu
//        Spinner spinner = getAccountsSpinner(menu);
        if (spinner == null) return;

        AccountService accountService = new AccountService(getActivity());
        accountService.loadTransactionAccountsToSpinner(spinner);

        // handle switching of accounts.
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // switch account.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(i);
                Account account = new Account();
                account.loadFromCursor(cursor);

                int accountId = account.getId();
                switchAccount(accountId);

                // color the spinner text
                ((TextView) spinner.getSelectedView()).setTextColor(getResources().getColor(R.color.material_white));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    /**
     * Select the current account in the accounts dropdown.
     */
    private void selectCurrentAccount() {
        Spinner spinner = getAccountsSpinner();
        if (spinner == null) return;

        // find account
        SimpleCursorAdapter adapter = (SimpleCursorAdapter) spinner.getAdapter();
        if (adapter == null) return;

        Cursor cursor = adapter.getCursor();
        int position = Constants.NOT_SET;

        for (int i = 0; i < adapter.getCount(); i++) {
            cursor.moveToPosition(i);
            String accountIdString = cursor.getString(cursor.getColumnIndex(Account.ACCOUNTID));
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

        SubMenu subMenu = item.getSubMenu();

        // on init, mark the default item as checked
        AppSettings settings = new AppSettings(mActivity);
        DefinedDateRangeName rangeName = settings.getLookAndFeelSettings().getShowTransactions();
        if (rangeName == null) return;

        DefinedDateRanges ranges = new DefinedDateRanges(getActivity());
        DefinedDateRange range = ranges.get(rangeName);
        if (range == null) return;
        int id = range.menuResourceId;

        MenuItem itemToMark = subMenu.findItem(id);
        if (itemToMark == null) return;

        itemToMark.setChecked(true);
    }

    private void switchAccount(int accountId) {
        if (accountId == mAccountId) return;

        // switch account. Reload transactions.
        mAccountId = accountId;
        mAllDataListFragment.AccountId = accountId;
//        mAllDataListFragment.loadData();
        mAllDataListFragment.loadData(prepareQuery());

        // hide account details bar if all accounts are selected
        if (accountId == Constants.NOT_SET) {
//            mDataFragment.setListHeader(null);
            mAllDataListFragment.getListView().removeHeaderView(mListHeader);
//            mListHeader.setVisibility(View.GONE);
        } else {
            if (mAllDataListFragment.getListView().getHeaderViewsCount() == 0) {
                mAllDataListFragment.getListView().addHeaderView(mListHeader);
            }
//            mListHeader.setVisibility(View.VISIBLE);
        }
    }

}
