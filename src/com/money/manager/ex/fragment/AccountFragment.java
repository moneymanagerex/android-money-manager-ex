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

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.money.manager.ex.AccountListEditActivity;
import com.money.manager.ex.CheckingAccountActivity;
import com.money.manager.ex.Constants;
import com.money.manager.ex.MainActivity;
import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.checkingaccount.CheckingAccountConstants;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.database.AccountRepository;
import com.money.manager.ex.database.MoneyManagerOpenHelper;
import com.money.manager.ex.database.QueryAccountBills;
import com.money.manager.ex.database.QueryAllData;
import com.money.manager.ex.database.TableAccountList;
import com.money.manager.ex.interfaces.IAllDataFragmentCallbacks;
import com.money.manager.ex.settings.PreferenceConstants;
import com.money.manager.ex.utils.CurrencyUtils;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Checking account fragment.
 * Shows the list of transactions, etc.
 * @author a.lazzari
 */
public class AccountFragment
        extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>,
            IAllDataFragmentLoaderCallbacks,
            IAllDataFragmentCallbacks {

    private static final String KEY_CONTENT = "AccountFragment:AccountId";
    private static final int ID_LOADER_SUMMARY = 2;

    private final String LOGCAT = this.getClass().getSimpleName();

    AllDataFragment mAllDataFragment;
    private Integer mAccountId = null;
    private String mFragmentName;
    private double mAccountBalance = 0, mAccountReconciled = 0;
    private TableAccountList mAccountList;
    // Controls
    private TextView txtAccountBalance, txtAccountReconciled, txtAccountDifference;
    private ImageView imgAccountFav, imgGotoAccount;

    /**
     * @param accountId Id of the Account to be displayed
     * @return initialized instance of Account Fragment.
     */
    public static AccountFragment newInstance(int accountId) {
        AccountFragment fragment = new AccountFragment();
        fragment.mAccountId = accountId;

        // set name of child fragment
        fragment.setFragmentName(AccountFragment.class.getSimpleName() + "_" + Integer.toString(accountId));

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_CONTENT)) {
            mAccountId = savedInstanceState.getInt(KEY_CONTENT);
        }
    }

    // IAllDataFragmentLoaderCallbacks

    @Override
    public void onCallbackCreateLoader(int id, Bundle args) {
//        return;
    }

    @Override
    public void onCallbackLoaderFinished(Loader<Cursor> loader, Cursor data) {
        getLoaderManager().restartLoader(ID_LOADER_SUMMARY, null, this);
    }

    @Override
    public void onCallbackLoaderReset(Loader<Cursor> loader) {
//        return;
    }

    // End IAllDataFragmentLoaderCallbacks

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
        // todo: inflater.inflate(R.menu.menu_period_picker, menu);

        // call create option menu of fragment
        mAllDataFragment.onCreateOptionsMenu(menu, inflater);
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
        showCurrentAccount(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean result;

        switch (item.getItemId()) {
            case R.id.menu_add_transaction_account:
                startCheckingAccountActivity();
                result = true;
                break;
            case R.id.menu_export_to_csv:
                if (mAllDataFragment != null && mAccountList != null)
                    mAllDataFragment.exportDataToCSVFile(mAccountList.getAccountName());
                result = true;
                break;
            default:
                result = false;
                break;
        }

        if (result) {
            return result;
        } else {
            return super.onOptionsItemSelected(item);
        }
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
        if (spinner != null) {
            loadAccountsToSpinner(getActivity(), spinner);
        }

        // The current account is selected in 'prepare menu'.
//        showCurrentAccount(menu);

        // handle switching of accounts.
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // switch account.
                Spinner spinner1 = (Spinner) adapterView;
                TableAccountList account = getAccountAtPosition(spinner1, i);
                int accountId = account.getAccountId();
//                String selectedAccountIdString = mAccountSpinnerValues.getValueAtPosition(i);
//                int accountId = Integer.parseInt(selectedAccountIdString);
                if (accountId != mAccountId) {
                    // switch account. Reload transactions.
                    mAccountId = accountId;
                    mAllDataFragment.AccountId = accountId;
                    mAllDataFragment.loadData(prepareArgsForChildFragment());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private TableAccountList getAccountAtPosition(Spinner spinner, int position) {
        SimpleCursorAdapter adapter = (SimpleCursorAdapter) spinner.getAdapter();
        Cursor cursor = (Cursor) adapter.getItem(position);
        TableAccountList account = new TableAccountList();
        account.setValueFromCursor(cursor);

        return account;
    }

    /**
     * Show the current account selected in the accounts dropdown.
     * @param menu The toolbar/menu that contains the dropdown.
     */
    private void showCurrentAccount(Menu menu) {
        Spinner spinner = getAccountsSpinner(menu);
        if (spinner == null) return;

        // find account
        SimpleCursorAdapter adapter = (SimpleCursorAdapter) spinner.getAdapter();
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

    private void loadAccountsToSpinner(Context context, Spinner spinner) {
        if (spinner == null) return;
        if (context == null) {
            Log.e(LOGCAT, "Context not sent when loading accounts");
            return;
        }

        AccountRepository repo = new AccountRepository(context);
        Core core = new Core(context.getApplicationContext());

        Cursor cursor = repo.getCursor(core.getAccountsOpenVisible(),
                core.getAccountFavoriteVisible(), repo.getTransactionAccountTypeNames());

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
                Intent intent = new Intent(getActivity(), AccountListEditActivity.class);
                intent.putExtra(AccountListEditActivity.KEY_ACCOUNT_ID, mAccountId);
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
        mAccountList = MoneyManagerOpenHelper.getInstance(getActivity().getApplicationContext())
                .getTableAccountList(mAccountId);
    }

    // Loader events.

    /**
     * Start Loader to retrieve data
     */
    public void loadTransactions() {
        if (mAllDataFragment != null) {
            Bundle arguments = prepareArgsForChildFragment();
            mAllDataFragment.loadData(arguments);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case ID_LOADER_SUMMARY:
                return new CursorLoader(getActivity(),
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
//        return;
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
        }
    }

    // end loader events

    @Override
    public void onResume() {
        super.onResume();
        // restart loader
        loadTransactions();
    }

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

//        resetToolbar();
    }

    private Bundle prepareArgsForChildFragment() {
        // compose selection and sort
        ArrayList<String> selection = new ArrayList<>();
        selection.add("(" + QueryAllData.ACCOUNTID + "=" + Integer.toString(mAccountId) + " OR " + QueryAllData.FromAccountId + "="
                + Integer.toString(mAccountId) + ")");
        if (MoneyManagerApplication.getInstanceApp().getShowTransaction().equalsIgnoreCase(getString(R.string.last7days))) {
            selection.add("(julianday(date('now')) - julianday(" + QueryAllData.Date + ") <= 7)");
        } else if (MoneyManagerApplication.getInstanceApp().getShowTransaction().equalsIgnoreCase(getString(R.string.last15days))) {
            selection.add("(julianday(date('now')) - julianday(" + QueryAllData.Date + ") <= 14)");
        } else if (MoneyManagerApplication.getInstanceApp().getShowTransaction().equalsIgnoreCase(getString(R.string.current_month))) {
            selection.add(QueryAllData.Month + "=" + Integer.toString(Calendar.getInstance().get(Calendar.MONTH) + 1));
            selection.add(QueryAllData.Year + "=" + Integer.toString(Calendar.getInstance().get(Calendar.YEAR)));
        } else if (MoneyManagerApplication.getInstanceApp().getShowTransaction().equalsIgnoreCase(getString(R.string.last3months))) {
            selection.add("(julianday(date('now')) - julianday(" + QueryAllData.Date + ") <= 90)");
        } else if (MoneyManagerApplication.getInstanceApp().getShowTransaction().equalsIgnoreCase(getString(R.string.last6months))) {
            selection.add("(julianday(date('now')) - julianday(" + QueryAllData.Date + ") <= 180)");
        } else if (MoneyManagerApplication.getInstanceApp().getShowTransaction().equalsIgnoreCase(getString(R.string.current_year))) {
            selection.add(QueryAllData.Year + "=" + Integer.toString(Calendar.getInstance().get(Calendar.YEAR)));
        }
        // create a bundle to returns
        Bundle args = new Bundle();
        args.putStringArrayList(AllDataFragment.KEY_ARGUMENTS_WHERE, selection);
        args.putString(AllDataFragment.KEY_ARGUMENTS_SORT, QueryAllData.Date + " DESC, " + QueryAllData.ID + " DESC");

        return args;
    }

    /**
     * refresh UI, show favorite icome
     */
    private void setImageViewFavorite() {
        if (mAccountList.isFavoriteAcct()) {
            imgAccountFav.setBackgroundResource(R.drawable.ic_star);
        } else {
            imgAccountFav.setBackgroundResource(R.drawable.ic_star_outline);
        }
    }

    /**
     * refresh user interface with total
     */
    private void setTextViewBalance() {
        // Reload account info as it can be changed via dropdown. Need a currency info here.
        reloadAccountInfo();

        // write account balance
        if (mAccountList != null) {
            CurrencyUtils currencyUtils = new CurrencyUtils(getActivity().getApplicationContext());

            txtAccountBalance.setText(currencyUtils.getCurrencyFormatted(mAccountList.getCurrencyId(), mAccountBalance));
            txtAccountReconciled.setText(currencyUtils.getCurrencyFormatted(mAccountList.getCurrencyId(), mAccountReconciled));
            txtAccountDifference.setText(currencyUtils.getCurrencyFormatted(mAccountList.getCurrencyId(), mAccountReconciled - mAccountBalance));
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
        Intent intent = new Intent(getActivity(), CheckingAccountActivity.class);
        intent.putExtra(CheckingAccountConstants.KEY_ACCOUNT_ID, mAccountId);
        // check transId not null
        if (transId != null) {
            intent.putExtra(CheckingAccountConstants.KEY_TRANS_ID, transId);
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

        mAllDataFragment = AllDataFragment.newInstance(mAccountId, this);

        // set arguments and settings of fragment
        mAllDataFragment.setArguments(prepareArgsForChildFragment());
        if (header != null) mAllDataFragment.setListHeader(header);
        mAllDataFragment.setShownBalance(PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getBoolean(getString(PreferenceConstants.PREF_TRANSACTION_SHOWN_BALANCE), false));
        mAllDataFragment.setAutoStarLoader(false);
        mAllDataFragment.setSearResultFragmentLoaderCallbacks(this);

        // add fragment
        transaction.replace(R.id.fragmentContent, mAllDataFragment, getFragmentName());
        transaction.commit();
    }

}
