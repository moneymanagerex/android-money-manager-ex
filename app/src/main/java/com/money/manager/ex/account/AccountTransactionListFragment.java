/*
 * Copyright (C) 2012-2019 The Android Money Manager Ex Project Team
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

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.account.events.RunningBalanceCalculatedEvent;
import com.money.manager.ex.common.AllDataListFragment;
import com.money.manager.ex.common.MmxCursorLoader;
import com.money.manager.ex.core.DefinedDateRange;
import com.money.manager.ex.core.DefinedDateRangeName;
import com.money.manager.ex.core.DefinedDateRanges;
import com.money.manager.ex.core.TransactionStatuses;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.database.ITransactionEntity;
import com.money.manager.ex.database.QueryAccountBills;
import com.money.manager.ex.database.QueryAllData;
import com.money.manager.ex.database.WhereStatementGenerator;
import com.money.manager.ex.datalayer.AccountRepository;
import com.money.manager.ex.datalayer.Select;
import com.money.manager.ex.domainmodel.Account;
import com.money.manager.ex.home.MainActivity;
import com.money.manager.ex.servicelayer.AccountService;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.settings.LookAndFeelSettings;
import com.money.manager.ex.settings.PreferenceConstants;
import com.money.manager.ex.transactions.CheckingTransactionEditActivity;
import com.money.manager.ex.transactions.EditTransactionActivityConstants;
import com.money.manager.ex.utils.MmxDate;
import com.money.manager.ex.utils.MmxDateTimeUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

/**
 * Checking account fragment.
 * Shows the list of transactions.
 */
public class AccountTransactionListFragment
    extends Fragment
    implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String ARG_ACCOUNT_ID = "arg:accountId";

    private static final String KEY_CONTENT = "AccountTransactionListFragment:AccountId";
    private static final String KEY_STATUS = "AccountTransactionListFragment:StatusFilter";

    private static final int ID_LOADER_SUMMARY = 2;
    private static final String TAG_FILTER_DIALOG = "FilterDialogTag";

    /**
     * @param accountId Id of the Account to be displayed
     * @return initialized instance of Account Fragment.
     */
    public static AccountTransactionListFragment newInstance(int accountId) {
        AccountTransactionListFragment fragment = new AccountTransactionListFragment();

        Bundle args = new Bundle();
        args.putInt(ARG_ACCOUNT_ID, accountId);
        fragment.setArguments(args);

        // set name of child fragment
        fragment.setFragmentName(AccountTransactionListFragment.class.getSimpleName() + "_" +
            Integer.toString(accountId));

        return fragment;
    }

    private AllDataListFragment mAllDataListFragment;
    private Integer mAccountId = null;
    private String mFragmentName;
    private Money mAccountBalance = MoneyFactory.fromDouble(0),
            mAccountReconciled = MoneyFactory.fromDouble(0);
    private Account mAccount;
    private AccountTransactionsListViewHolder viewHolder;

    // filter
    private TransactionFilter mFilter;

    private boolean mSortTransactionsByType = true;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get account id from the arguments first.
        mAccountId = getArguments().getInt(ARG_ACCOUNT_ID);

        // initialize filter(s)
        this.mFilter = new TransactionFilter();

        // Select the default period.
        updateFilterDateRange();

        // Default value.
        mFilter.transactionStatus = new StatusFilter();

        restoreInstanceState(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_CONTENT)) {
            mAccountId = savedInstanceState.getInt(KEY_CONTENT);
        }

        if (container == null) return null;

        // inflate layout
        View view = inflater.inflate(R.layout.fragment_account_transactions, container, false);

        // take object AccountList
        if (mAccount == null) {
            reloadAccountInfo();
        }

        this.viewHolder = new AccountTransactionsListViewHolder();
        initializeListHeader(inflater);

        // Transactions
        showTransactionsFragment(viewHolder.listHeader);

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
    public void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        refreshSettings();

        initializeAccountsSelector();
        selectCurrentAccount();

        // restart loader
        loadTransactions();
    }

    @Override
    public void onStop() {
        super.onStop();

        EventBus.getDefault().unregister(this);
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

        // add the date picker.
        inflater.inflate(R.menu.menu_period_picker_transactions, menu);

        // transaction Status picker
        inflater.inflate(R.menu.menu_transaction_status_selector, menu);
        initTransactionStatusMenu(menu);

        // filter
        inflater.inflate(R.menu.menu_transaction_filters, menu);
        initializeFilterMenu(menu);

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
                //hide sync toolbar
                MenuItem itemSync = menu.findItem(R.id.menu_sync);
                if (itemSync != null) itemSync.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

                // hide menu open database
                MenuItem itemOpenDatabase = menu.findItem(R.id.menu_open_database);
                if (itemOpenDatabase != null) {
                    //itemOpenDatabase.setVisible(isShownOpenDatabaseItemMenu());
                    itemOpenDatabase.setShowAsAction(!itemSync.isVisible()
                        ? MenuItem.SHOW_AS_ACTION_ALWAYS : MenuItem.SHOW_AS_ACTION_NEVER);
                }

                //hide dash board
                MenuItem itemDashboard = menu.findItem(R.id.menu_dashboard);
                if (itemDashboard != null)
                    itemDashboard.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            }
        }

        selectCurrentPeriod(menu);
        selectCurrentStatus(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean result;

        result = datePeriodItemSelected(item);
        if (result) return true;

        result = isStatusSelectionHandled(item);
        if (result) return true;

        result = isFilterSelected(item);
        if (result) return true;

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
        if (mAllDataListFragment == null) return;

        Bundle arguments = prepareQuery();
        mAllDataListFragment.loadData(arguments);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case ID_LOADER_SUMMARY:
                // Account summary (balances).
                Select query = new Select()
                    .where(QueryAccountBills.ACCOUNTID + "=?",
                            new String[] { Integer.toString(mAccountId) });

                return new MmxCursorLoader(getActivity(),
                    new QueryAccountBills(getActivity()).getUri(),
                    query);
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

    // Other

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mAccountId != null) {
            outState.putInt(KEY_CONTENT, mAccountId);
        }

        outState.putStringArrayList(KEY_STATUS, mFilter.transactionStatus.filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public String getFragmentName() {
        return mFragmentName;
    }

    public void setFragmentName(String mFragmentName) {
        this.mFragmentName = mFragmentName;
    }

    // Events

    @Subscribe
    public void onEvent(RunningBalanceCalculatedEvent event) {
        // Update the UI controls
        mAllDataListFragment.displayRunningBalances(event.balances);
    }

    // Private

    private boolean datePeriodItemSelected(MenuItem item) {
        int stringId;
        int itemId = item.getItemId();

        DefinedDateRanges dateRanges = new DefinedDateRanges(getActivity());
        DefinedDateRange range = dateRanges.getByMenuId(itemId);
        if (range == null) return false;
        stringId = range.nameResourceId;

        LookAndFeelSettings settings = new AppSettings(getActivity()).getLookAndFeelSettings();
        settings.setShowTransactions(range.key);

        // Save the selected period.
        mFilter.dateRange = new MmxDateTimeUtils().getDateRangeForPeriod(getActivity(), stringId);

        item.setChecked(true);

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

    private void initializeFilterMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.menuTransactionFilters);

        Drawable icon = new UIHelper(getActivity()).getIcon(GoogleMaterial.Icon.gmd_keyboard_arrow_down);
        item.setIcon(icon);

        // selection handled in onOptionsItemSelected
    }

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

    /**
     * Initialize the Transaction Type filter.
     * @param menu menu/toolbar to add the icon to.
     */
    private void initTransactionStatusMenu(Menu menu) {
//        MenuItem item = menu.findItem(R.id.menuTransactionStatusSelector);

        // todo Use a font icon.
//        UIHelper uiHelper = new UIHelper(getActivity());
//        IconicsDrawable icon = new IconicsDrawable(getActivity())
//                .icon(MMXIconFont.Icon.mmx_filter)
//                .color(uiHelper.getPrimaryColor())
//                .sizeDp(uiHelper.getToolbarIconSize());

        // selection handled in onOptionsItemSelected
    }

    private void initializeListHeader(LayoutInflater inflater) {
        this.viewHolder.listHeader = (ViewGroup) inflater.inflate(R.layout.account_header_fragment, null, false);

        // take reference text view from layout
        this.viewHolder.txtAccountBalance = (TextView) this.viewHolder.listHeader.findViewById(R.id.textViewAccountBalance);
        this.viewHolder.txtAccountReconciled = (TextView) this.viewHolder.listHeader.findViewById(R.id.textViewAccountReconciled);
        this.viewHolder.txtAccountDifference = (TextView) this.viewHolder.listHeader.findViewById(R.id.textViewDifference);
        // favorite icon
        this.viewHolder.imgAccountFav = (ImageView) this.viewHolder.listHeader.findViewById(R.id.imageViewAccountFav);

        // set listener click on favorite icon for change image
        this.viewHolder.imgAccountFav.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // set status account
                mAccount.setFavorite(!(mAccount.getFavorite()));

                AccountRepository repo = new AccountRepository(getActivity());
                boolean updated = repo.save(mAccount);

                if (!updated) {
                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.db_update_failed), Toast.LENGTH_LONG).show();
                } else {
                    setImageViewFavorite();
                }
            }
        });

        // goto account
        this.viewHolder.imgGotoAccount = (ImageView) this.viewHolder.listHeader.findViewById(R.id.imageViewGotoAccount);
        this.viewHolder.imgGotoAccount.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AccountEditActivity.class);
                intent.putExtra(AccountEditActivity.KEY_ACCOUNT_ID, mAccountId);
                intent.setAction(Intent.ACTION_EDIT);
                startActivity(intent);
            }
        });
    }

    private boolean isStatusSelectionHandled(MenuItem item) {
        int id = item.getItemId();
        boolean result = true;

        // turn filters on/off
        TransactionStatuses status = null;

        switch(id) {
            case R.id.menu_none:
                status = TransactionStatuses.NONE;
                break;
            case R.id.menu_reconciled:
                status = TransactionStatuses.RECONCILED;
                break;
            case R.id.menu_void:
                status = TransactionStatuses.VOID;
                break;
            case R.id.menu_follow_up:
                status = TransactionStatuses.FOLLOWUP;
                break;
            case R.id.menu_duplicate:
                status = TransactionStatuses.DUPLICATE;
                break;
            default:
                // not handled here.
                result = false;
                break;
        }

        if (result) {
            if (item.isChecked()) {
                // remove filter
                mFilter.transactionStatus.filter.remove(status.getCode());
                item.setChecked(false);
            } else {
                // add filter
                mFilter.transactionStatus.filter.add(status.getCode());
                item.setChecked(true);
            }

            loadTransactions();
        }

        return result;
    }

    private boolean isFilterSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menuTransactionFilters) {
            // show binaryDialog
            showFilterDialog();
            return true;
        }

        return false;
    }

    private void loadAccountsInto(final Spinner spinner) {
        if (spinner == null) return;

        AccountService accountService = new AccountService(getActivity());
        accountService.loadTransactionAccountsToSpinner(spinner);

        // e switching of accounts.
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // switch account.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(i);
                Account account = new Account();
                account.loadFromCursor(cursor);

                int accountId = account.getId();
                switchAccount(accountId);

                // color the spinner text of the selected item.
                int spinnerItemTextColor = ContextCompat.getColor(getActivity(), R.color.material_grey_50);
                ((TextView) spinner.getSelectedView()).setTextColor(spinnerItemTextColor);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    /**
     * Refreshes the running balance.
     */
    private void populateRunningBalance() {
        Bundle arguments = prepareQuery();

        CalculateRunningBalanceTask2 task = new CalculateRunningBalanceTask2(
            getContext(), this.mAccountId, mFilter.dateRange.dateFrom, arguments);
        // events now handled in onEvent, using an event bus.
        task.execute();

        // the result is received in #onTaskComplete.
    }

    /**
     * Implementation using Rx instead of AsyncTask.
     */
    private void populateRunningBalanceRx() {
        Bundle arguments = prepareQuery();


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
                where.getStatement(ITransactionEntity.TOACCOUNTID, "=", mAccountId),
                where.getStatement(ITransactionEntity.ACCOUNTID, "=", mAccountId)
            ));

        where.addStatement(QueryAllData.Date, ">=", new MmxDate(mFilter.dateRange.dateFrom)
                .toIsoDateString());
        where.addStatement(QueryAllData.Date, "<=", new MmxDate(mFilter.dateRange.dateTo)
                .toIsoDateString());

        // Status
        where.addStatement(QueryAllData.Status, "IN", mFilter.transactionStatus.getSqlParameters());

        // create a bundle to returns

        String sortArgument = QueryAllData.Date + " DESC, ";
        if (mSortTransactionsByType) {
            sortArgument += QueryAllData.TransactionType + ", ";
        }
        sortArgument += QueryAllData.ID + " DESC";

        Bundle args = new Bundle();
        args.putString(AllDataListFragment.KEY_ARGUMENTS_WHERE, where.getWhere());
        args.putString(AllDataListFragment.KEY_ARGUMENTS_SORT, sortArgument);

        return args;
    }

    private void reloadAccountInfo() {
        AccountRepository repo = new AccountRepository(getActivity());
        mAccount = repo.load(mAccountId);
    }

    private void restoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState == null) return;

        mAccountId = savedInstanceState.getInt(KEY_CONTENT);

        mFilter.transactionStatus.filter = savedInstanceState.getStringArrayList(KEY_STATUS);
    }

    /**
     * refresh UI, show favorite icon
     */
    private void setImageViewFavorite() {
        if (mAccount.getFavorite()) {
            this.viewHolder.imgAccountFav.setBackgroundResource(R.drawable.ic_star);
        } else {
            this.viewHolder.imgAccountFav.setBackgroundResource(R.drawable.ic_star_outline);
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

            this.viewHolder.txtAccountBalance.setText(currencyService.getCurrencyFormatted(
                mAccount.getCurrencyId(), mAccountBalance));
            this.viewHolder.txtAccountReconciled.setText(currencyService.getCurrencyFormatted(
                mAccount.getCurrencyId(), mAccountReconciled));
            this.viewHolder.txtAccountDifference.setText(currencyService.getCurrencyFormatted(
                mAccount.getCurrencyId(), mAccountReconciled.subtract(mAccountBalance)));
        }
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
        AppSettings settings = new AppSettings(getActivity());
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

    private void selectCurrentStatus(Menu menu) {
        MenuItem toolbarItem = menu.findItem(R.id.menuTransactionStatusSelector);
        if (toolbarItem == null) return;

        SubMenu subMenu = toolbarItem.getSubMenu();

        for (int i = 0; i < subMenu.size(); i++) {
            MenuItem subItem = subMenu.getItem(i);
            int menuId = subItem.getItemId();

            if (mFilter.transactionStatus.contains(menuId)) {
                subItem.setChecked(true);
            }
        }
    }

    private void switchAccount(int accountId) {
        if (accountId == mAccountId) return;

        // switch account. Reload transactions.
        mAccountId = accountId;
        mAllDataListFragment.AccountId = accountId;
        mAllDataListFragment.loadData(prepareQuery());

        // hide account details bar if all accounts are selected
        if (accountId == Constants.NOT_SET) {
            /*
            See Watchlist Fragment for reference.
             */
//            mAllDataListFragment.getListView().removeHeaderView(this.viewHolder.listHeader);
            this.viewHolder.listHeader.findViewById(R.id.headerRow).setVisibility(View.GONE);
        } else {
            if (mAllDataListFragment.getListView().getHeaderViewsCount() == 0) {
                mAllDataListFragment.getListView().addHeaderView(this.viewHolder.listHeader);
            }
            this.viewHolder.listHeader.findViewById(R.id.headerRow).setVisibility(View.VISIBLE);
        }
    }

    private void showFilterDialog() {
        int numberOfRecords = mAllDataListFragment.getListAdapter().getCount();
        FilterDialogFragment dialog = FilterDialogFragment.newInstance(mFilter, mAccount, numberOfRecords);
        dialog.show(getActivity().getSupportFragmentManager(), TAG_FILTER_DIALOG);
    }

    private void showTransactionsFragment(ViewGroup header) {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();

        mAllDataListFragment = AllDataListFragment.newInstance(mAccountId);

        // append search arguments
//        mAllDataListFragment.setArguments(prepareQuery());
        Bundle searchQuery = prepareQuery();
        mAllDataListFragment.getArguments().putAll(searchQuery);

        if (header != null) mAllDataListFragment.setListHeader(header);
        updateAllDataListFragmentShowBalance();
        mAllDataListFragment.setAutoStarLoader(false);
        mAllDataListFragment.setSearResultFragmentLoaderCallbacks(this);

        // add fragment
        transaction.replace(R.id.fragmentMain, mAllDataListFragment, getFragmentName());
        transaction.commit();
    }

    /**
     * start the activity of transaction management
     *
     * @param transId null set if you want to do a new transaction, or transaction id
     */
    private void startCheckingAccountActivity(Integer transId) {
        // create intent, set Account ID
        Intent intent = new Intent(getActivity(), CheckingTransactionEditActivity.class);
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

    /**
     * start the activity of transaction management
     */
    private void startCheckingAccountActivity() {
        this.startCheckingAccountActivity(null);
    }

    private void refreshSettings() {
        mSortTransactionsByType = new AppSettings(getActivity()).getLookAndFeelSettings().getSortTransactionsByType();

        updateFilterDateRange();
        updateAllDataListFragmentShowBalance();

        getActivity().invalidateOptionsMenu();
    }

    private void updateFilterDateRange() {
        DefinedDateRangeName rangeName = new AppSettings(getActivity()).getLookAndFeelSettings()
            .getShowTransactions();
        DefinedDateRanges ranges = new DefinedDateRanges(getActivity());
        DefinedDateRange range = ranges.get(rangeName);

        mFilter.dateRange = new MmxDateTimeUtils().getDateRangeForPeriod(getActivity(), range.nameResourceId);
    }

    private void updateAllDataListFragmentShowBalance() {
        if (mAllDataListFragment == null) {
            return;
        }

        mAllDataListFragment.setShownBalance(PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getBoolean(getString(PreferenceConstants.PREF_TRANSACTION_SHOWN_BALANCE), false));
    }
}
