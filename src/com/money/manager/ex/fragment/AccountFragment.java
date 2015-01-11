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
package com.money.manager.ex.fragment;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.money.manager.ex.CheckingAccountActivity;
import com.money.manager.ex.MainActivity;
import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.database.MoneyManagerOpenHelper;
import com.money.manager.ex.database.QueryAccountBills;
import com.money.manager.ex.database.QueryAllData;
import com.money.manager.ex.database.TableAccountList;
import com.money.manager.ex.fragment.AllDataFragment.AllDataFragmentLoaderCallbacks;
import com.money.manager.ex.preferences.PreferencesConstant;
import com.money.manager.ex.utils.CurrencyUtils;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * @author a.lazzari
 */
public class AccountFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, AllDataFragmentLoaderCallbacks {

    private static final String KEY_CONTENT = "AccountFragment:AccountId";
    private static final int ID_LOADER_SUMMARY = 2;
    // all data fragment
    AllDataFragment mAllDataFragment;
    // id account
    private int mAccountId = 0;
    // string name fragment
    private String mNameFragment;
    // account balance
    private double mAccountBalance = 0;
    private double mAccountReconciled = 0;
    // Dataset: accountlist e alldata
    private TableAccountList mAccountList;
    // view into layout
    private TextView txtAccountBalance, txtAccountReconciled, txtAccountDifference;
    private ImageView imgAccountFav;
    // name account
    private String mAccountName;
    // setting for shown open database item menu
    private boolean mShownOpenDatabaseItemMenu = false;

    /**
     * @param accountid ID Account to be display
     * @return
     */
    public static AccountFragment newIstance(int accountid) {
        AccountFragment fragment = new AccountFragment();
        fragment.mAccountId = accountid;
        // set name of child fragment
        fragment.setNameFragment(AccountFragment.class.getSimpleName() + "_" + Integer.toString(accountid));

        return fragment;
    }

    @Override
    public void onCallbackCreateLoader(int id, Bundle args) {
        return;
    }

    @Override
    public void onCallbackLoaderFinished(Loader<Cursor> loader, Cursor data) {
        getLoaderManager().restartLoader(ID_LOADER_SUMMARY, null, this);
    }

    @Override
    public void onCallbackLoaderReset(Loader<Cursor> loader) {
        return;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection = "";
        switch (id) {
            case ID_LOADER_SUMMARY:
                selection = QueryAccountBills.ACCOUNTID + "=?";
                return new CursorLoader(getActivity(), new QueryAccountBills(getActivity()).getUri(), null, selection,
                        new String[]{Integer.toString(mAccountId)}, null);
        }
        return null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        // call create option menu of fragment
        mAllDataFragment.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        //force show add transaction
        MenuItem itemAddTransaction = menu.findItem(R.id.menu_add_transaction_account);
        if (itemAddTransaction != null)
            itemAddTransaction.setVisible(true);
        //manage dual panel
        if (getActivity() != null && getActivity() instanceof MainActivity) {
            MainActivity activity = (MainActivity) getActivity();
            if (!activity.isDualPanel()) {
                //hide dropbox toolbar
                MenuItem itemDropbox = menu.findItem(R.id.menu_sync_dropbox);
                if (itemDropbox != null)
                    itemDropbox.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                // hide menu open database
                MenuItem itemOpenDatabase = menu.findItem(R.id.menu_open_database);
                if (itemOpenDatabase != null) {
                    //itemOpenDatabase.setVisible(isShownOpenDatabaseItemMenu());
                    itemOpenDatabase.setShowAsAction(!itemDropbox.isVisible() ? MenuItem.SHOW_AS_ACTION_ALWAYS : MenuItem.SHOW_AS_ACTION_NEVER);
                }

                //hide dash board
                MenuItem itemDashboard = menu.findItem(R.id.menu_dashboard);
                if (itemDashboard != null)
                    itemDashboard.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
            }
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_CONTENT)) {
            mAccountId = savedInstanceState.getInt(KEY_CONTENT);
        }
        if (container == null) {
            return null;
        }
        // inflate layout
        View view = (LinearLayout) inflater.inflate(R.layout.account_fragment, container, false);
        // take object AccountList
        if (mAccountList == null) {
            mAccountList = MoneyManagerOpenHelper.getInstance(getActivity()).getTableAccountList(mAccountId);
        }
        // take reference textview from layout
        txtAccountBalance = (TextView) view.findViewById(R.id.textViewAccountBalance);
        txtAccountReconciled = (TextView) view.findViewById(R.id.textViewAccountReconciled);
        txtAccountDifference = (TextView) view.findViewById(R.id.textViewDifference);
        // favorite icon
        imgAccountFav = (ImageView) view.findViewById(R.id.imageViewAccountFav);
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
        // manage fragment
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        mAllDataFragment = AllDataFragment.newInstance(mAccountId);
        // set arguments and settings of fragment
        mAllDataFragment.setArguments(prepareArgsForChildFragment());
        mAllDataFragment.setShownBalance(PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean(getString(PreferencesConstant.PREF_TRANSACTION_SHOWN_BALANCE), false));
        mAllDataFragment.setAutoStarLoader(false);
        mAllDataFragment.setContextMenuGroupId(mAccountId);
        mAllDataFragment.setSearResultFragmentLoaderCallbacks(this);
        // add fragment
        transaction.replace(R.id.fragmentContent, mAllDataFragment, getNameFragment());
        transaction.commit();

        // refresh user interface
        if (mAccountList != null) {
            mAccountName = mAccountList.getAccountName();
            setImageViewFavorite();
        }
        // set has optionmenu
        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        return;
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
                BaseFragmentActivity activity = (BaseFragmentActivity) getActivity();
                if (activity != null) {
                    activity.getSupportActionBar().setSubtitle(mAccountName);
                }
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_add_transaction_account) {
            startCheckingAccountActivity();
            return true;
        } else if (item.getItemId() == R.id.menu_export_to_csv) {
            if (mAllDataFragment != null && mAccountList != null)
                mAllDataFragment.exportDataToCSVFile(mAccountList.getAccountName());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        // restart loader
        startLoaderData();
        // set subtitle account name
        BaseFragmentActivity activity = (BaseFragmentActivity) getActivity();
        if (activity != null) {
            activity.getSupportActionBar().setSubtitle(mAccountName);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_CONTENT, mAccountId);
    }

    private Bundle prepareArgsForChildFragment() {
        // compose selection and sort
        ArrayList<String> selection = new ArrayList<String>();
        selection.add("(" + QueryAllData.ACCOUNTID + "=" + Integer.toString(mAccountId) + " OR " + QueryAllData.ToAccountID + "="
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
            imgAccountFav.setBackgroundResource(R.drawable.ic_rate_star_on);
        } else {
            imgAccountFav.setBackgroundResource(R.drawable.ic_rate_star_off);
        }
    }

    /**
     * refresh user interface with total
     */
    private void setTextViewBalance() {
        // write account balance
        if (mAccountList != null) {
            CurrencyUtils currencyUtils = new CurrencyUtils(getActivity());

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

    /**
     * Start Loader to retrive data
     */
    public void startLoaderData() {
        if (mAllDataFragment != null) {
            mAllDataFragment.startLoaderData();
        }
    }

    /**
     * @return the mShownOpenDatabaseItemMenu
     */
    public boolean isShownOpenDatabaseItemMenu() {
        return mShownOpenDatabaseItemMenu;
    }

    /**
     * @param mShownOpenDatabaseItemMenu the mShownOpenDatabaseItemMenu to set
     */
    public void setShownOpenDatabaseItemMenu(boolean mShownOpenDatabaseItemMenu) {
        this.mShownOpenDatabaseItemMenu = mShownOpenDatabaseItemMenu;
    }

    public String getNameFragment() {
        return mNameFragment;
    }

    public void setNameFragment(String mNameFragment) {
        this.mNameFragment = mNameFragment;
    }
}
