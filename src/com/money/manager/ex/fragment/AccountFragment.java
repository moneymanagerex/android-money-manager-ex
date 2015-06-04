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
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.money.manager.ex.AccountListEditActivity;
import com.money.manager.ex.CheckingAccountActivity;
import com.money.manager.ex.MainActivity;
import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.checkingaccount.AccountTransactionsFilter;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.database.AccountRepository;
import com.money.manager.ex.database.MoneyManagerOpenHelper;
import com.money.manager.ex.database.QueryAccountBills;
import com.money.manager.ex.database.QueryAllData;
import com.money.manager.ex.database.TableAccountList;
import com.money.manager.ex.inapp.util.SpinnerValues;
import com.money.manager.ex.settings.PreferenceConstants;
import com.money.manager.ex.utils.CurrencyUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Checking account fragment.
 * Shows the list of transactions, etc.
 * @author a.lazzari
 */
public class AccountFragment
        extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>, IAllDataFragmentLoaderCallbacks {

    public static final String ACCOUNT_SPINNER_TAG = "AccountFragment::AccountSpinner";

    private static final String KEY_CONTENT = "AccountFragment:AccountId";
    private static final int ID_LOADER_SUMMARY = 2;

    private final String LOGCAT = this.getClass().getSimpleName();

    // all data fragment
    AllDataFragment mAllDataFragment;
    // id account
    private Integer mAccountId = null;
    // string name fragment
    private String mFragmentName;
    // account balance
    private double mAccountBalance = 0, mAccountReconciled = 0;
    // Dataset: accountlist e alldata
    private TableAccountList mAccountList;
    // Controls
    private TextView txtAccountBalance, txtAccountReconciled, txtAccountDifference;
    private ImageView imgAccountFav, imgGotoAccount;
    // name account
    private String mAccountName;
    // Filtering
    private AccountTransactionsFilter mFilter;
    private SpinnerValues mAccountSpinnerValues;

    /**
     * @param accountId Id of the Account to be displayed
     * @return
     */
    public static AccountFragment newInstance(int accountId) {
        AccountFragment fragment = new AccountFragment();
        fragment.mAccountId = accountId;

        fragment.mFilter = new AccountTransactionsFilter();
        fragment.mFilter.setAccountId(accountId);

        // set name of child fragment
        fragment.setFragmentName(AccountFragment.class.getSimpleName() + "_" + Integer.toString(accountId));

        return fragment;
    }

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_CONTENT)) {
            mAccountId = savedInstanceState.getInt(KEY_CONTENT);
        }
    }

    // Menu

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        // call create option menu of fragment
        mAllDataFragment.onCreateOptionsMenu(menu, inflater);

        // Accounts list dropdown in toolbar.
        // Ref: http://stackoverflow.com/questions/11377760/adding-spinner-to-actionbar-not-navigation

        // Add options available only in account transactions list(s).
        inflater.inflate(R.menu.menu_account_transactions, menu);
        initAccountsDropdown(menu);
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

    private void initAccountsDropdown(Menu menu) {
        // Hide the toolbar title?
//        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
//        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Load accounts into the list.
        MenuItem item = menu.findItem(R.id.menuAccountSelector);
        Spinner spinner = (Spinner) MenuItemCompat.getActionView(item);
        loadAccountsToSpinner(getActivity(), spinner);
        // select the current account
        spinner.setSelection(mAccountSpinnerValues.getPositionOfValue(Integer.toString(mAccountId)));

        // handle switching of accounts.
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // switch account.
                String selectedAccountIdString = mAccountSpinnerValues.getValueAtPosition(i);
                int accountId = Integer.parseInt(selectedAccountIdString);
                if (accountId != mAccountId) {
                    // switch account. Reload transactions.
                    mAccountId = accountId;
                    mAllDataFragment.AccountId = accountId;
                    mAllDataFragment.loadData(prepareArgsForChildFragment());
                }            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void loadAccountsToSpinner(Context context, Spinner spinner) {
        if (spinner == null) return;

        // load accounts with ids.
        Core core = new Core(context.getApplicationContext());
        AccountRepository repo = new AccountRepository(context);
        List<TableAccountList> accounts = repo.getTransactionAccounts(core.getAccountsOpenVisible(),
                core.getAccountFavoriteVisible());
        mAccountSpinnerValues = new SpinnerValues();
        for(TableAccountList account : accounts) {
            mAccountSpinnerValues.add(Integer.toString(account.getAccountId()), account.getAccountName());
        }

        ArrayAdapter<String> accountAdapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item,
                mAccountSpinnerValues.getTextsArray());
        accountAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(accountAdapter);
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
            mAccountList = MoneyManagerOpenHelper.getInstance(getActivity().getApplicationContext())
                    .getTableAccountList(mAccountId);
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
                // populate contentvalues for update
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
            mAccountName = mAccountList.getAccountName();
            setImageViewFavorite();
        }

        setHasOptionsMenu(true);

        return view;
    }

    private void showTransactionsFragment(ViewGroup header) {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();

        mAllDataFragment = AllDataFragment.newInstance(mAccountId);

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

    // Loader events.

    /**
     * Start Loader to retrieve data
     */
    public void loadTransactions() {
        if (mAllDataFragment != null) {
            mAllDataFragment.loadData();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case ID_LOADER_SUMMARY:
                mFilter.setAccountId(mAccountId);
                // create account filter based on the settings and manually selected options.
                String selection = mFilter.getSelection();
                String[] arguments = mFilter.getSelectionArguments();

                return new CursorLoader(getActivity(),
                        new QueryAccountBills(getActivity()).getUri(),
                        null,
                        selection,
                        arguments,
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

                // set titles
                raiseShowAccountName(mAccountName);
                break;
        }
    }

    // end loader events

    private void raiseShowAccountName(String accountName) {
        // todo: is this required?
//        if (getActivity() instanceof IToolbarSubtitleCallbacks) {
//            IToolbarSubtitleCallbacks callbacks = (IToolbarSubtitleCallbacks) getActivity();
//            callbacks.onSetToolbarSubtitleRequested(accountName);
//        }

//                BaseFragmentActivity activity = (BaseFragmentActivity) getActivity();
//                if (activity != null) {
//                    activity.getSupportActionBar().setSubtitle(mAccountName);
//                }

    }

    @Override
    public void onResume() {
        super.onResume();
        // restart loader
        loadTransactions();
        // set subtitle account name
        raiseShowAccountName(mAccountName);
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
        intent.putExtra(CheckingAccountActivity.KEY_ACCOUNT_ID, mAccountId);
        // check transId not null
        if (transId != null) {
            intent.putExtra(CheckingAccountActivity.KEY_TRANS_ID, transId);
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
}
