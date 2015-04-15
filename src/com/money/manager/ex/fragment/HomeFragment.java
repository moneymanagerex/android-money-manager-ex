/*
 * Copyright (C) 2012-2015 Alessandro Lazzari
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
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
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

import com.melnykov.fab.FloatingActionButton;
import com.money.manager.ex.AccountListEditActivity;
import com.money.manager.ex.CheckingAccountActivity;
import com.money.manager.ex.Constants;
import com.money.manager.ex.MainActivity;
import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.database.QueryAccountBills;
import com.money.manager.ex.database.QueryBillDeposits;
import com.money.manager.ex.database.QueryReportIncomeVsExpenses;
import com.money.manager.ex.database.TableInfoTable;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.settings.PreferencesConstant;
import com.money.manager.ex.settings.DropboxSettingsActivity;
import com.money.manager.ex.utils.CurrencyUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * @author Alessandro Lazzari (lazzari.ale@gmail.com)
 */
@SuppressWarnings("static-access")
public class HomeFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
    // ID Loader Manager
    private static final int ID_LOADER_USER_NAME = 1;
    private static final int ID_LOADER_ACCOUNT_BILLS = 2;
    private static final int ID_LOADER_BILL_DEPOSITS = 3;
    private static final int ID_LOADER_INCOME_EXPENSES = 4;
    private CurrencyUtils currencyUtils;
    // dataset table/view/query manage into class
    private TableInfoTable infoTable = new TableInfoTable();
    private QueryAccountBills accountBills;
    // view show in layout
    private TextView txtTotalAccounts;
    private ExpandableListView mExpandableListView;
    private ViewGroup linearHome, linearFooter, linearWelcome;
    private TextView txtFooterSummary;
    private TextView txtFooterSummaryReconciled;
    private ProgressBar prgAccountBills;
    private FloatingActionButton mFloatingActionButton;

    private List<String> mAccountTypes = new ArrayList<>();
    private HashMap<String, List<QueryAccountBills>> mAccountsByType = new HashMap<>();
    private HashMap<String, QueryAccountBills> mTotalsByType = new HashMap<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        currencyUtils = new CurrencyUtils(getActivity().getApplicationContext());
        accountBills = new QueryAccountBills(getActivity());
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Core core = new Core(getActivity().getApplicationContext());

        switch (id) {
            case ID_LOADER_USER_NAME:
                return new CursorLoader(getActivity(), infoTable.getUri(),
                        new String[]{infoTable.INFONAME, infoTable.INFOVALUE}, null, null, null);

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
                return new CursorLoader(getActivity(), accountBills.getUri(),
                        accountBills.getAllColumns(), where, null,
                        accountBills.ACCOUNTTYPE + ", upper(" + accountBills.ACCOUNTNAME + ")");

            case ID_LOADER_BILL_DEPOSITS:
                QueryBillDeposits billDeposits = new QueryBillDeposits(getActivity());
                return new CursorLoader(getActivity(), billDeposits.getUri(), null, QueryBillDeposits.DAYSLEFT + "<=0", null, null);

            case ID_LOADER_INCOME_EXPENSES:
                QueryReportIncomeVsExpenses report = new QueryReportIncomeVsExpenses(getActivity());
                return new CursorLoader(getActivity(), report.getUri(), report.getAllColumns(), QueryReportIncomeVsExpenses.Month + "="
                        + Integer.toString(Calendar.getInstance().get(Calendar.MONTH) + 1) + " AND " + QueryReportIncomeVsExpenses.Year + "="
                        + Integer.toString(Calendar.getInstance().get(Calendar.YEAR)), null, null);

            default:
                return null;
        }
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
                    //intent.putExtra(Constants.INTENT_REQUEST_PREFERENCES_SCREEN, PreferencesConstant.PREF_DROPBOX_HOWITWORKS);
                    startActivity(intent);
                }
            });
        }

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

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFloatingActionButton.attachToListView(mExpandableListView);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case ID_LOADER_ACCOUNT_BILLS:
                txtTotalAccounts.setText(currencyUtils.getBaseCurrencyFormatted((double) 0));

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
                mainActivity.setDrawableUserName(MoneyManagerApplication.getInstanceApp().getUserName());
                break;

            case ID_LOADER_ACCOUNT_BILLS:
                double curTotal = 0, curReconciled = 0;
                AccountBillsExpandableAdapter expandableAdapter = null;

                linearHome.setVisibility(data != null && data.getCount() > 0 ? View.VISIBLE : View.GONE);
                linearWelcome.setVisibility(linearHome.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);

                mAccountsByType.clear();
                mTotalsByType.clear();
                mAccountTypes.clear();

                // cycle cursor
                if (data != null && data.moveToFirst()) {
                    while (!data.isAfterLast()) {
                        curTotal += data.getDouble(data.getColumnIndex(QueryAccountBills.TOTALBASECONVRATE));
                        curReconciled += data.getDouble(data.getColumnIndex(QueryAccountBills.RECONCILEDBASECONVRATE));

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
                            if (Constants.ACCOUNT_TYPE_CHECKING.equalsIgnoreCase(accountType)) {
                                totals.setAccountName(getString(R.string.bank_accounts));
                            } else if (Constants.ACCOUNT_TYPE_TERM.equalsIgnoreCase(accountType)) {
                                totals.setAccountName(getString(R.string.term_accounts));
                            } else if (Constants.ACCOUNT_TYPE_CREDIT_CARD.equalsIgnoreCase(accountType)) {
                                totals.setAccountName(getString(R.string.credit_card_accounts));
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

                        data.moveToNext();
                    }
                    // create adapter
                    expandableAdapter = new AccountBillsExpandableAdapter(getActivity());
                }
                // write accounts total

                addFooterExpandableListView(curTotal, curReconciled);
                // set adapter and shown
                mExpandableListView.setAdapter(expandableAdapter);
                setVisibilityOfAccountGroups();
                setListViewAccountBillsVisible(true);

                // set total accounts in drawer
                if (mainActivity != null)
                    mainActivity.setDrawableTotalAccounts(txtTotalAccounts.getText().toString());

                break;

            case ID_LOADER_BILL_DEPOSITS:
                mainActivity.setDrawableRepeatingTransactions(data != null ? data.getCount() : 0);
                break;

            case ID_LOADER_INCOME_EXPENSES:
                double income = 0, expenses = 0;
                if (data != null && data.moveToFirst()) {
                    while (!data.isAfterLast()) {
                        expenses = data.getDouble(data.getColumnIndex(QueryReportIncomeVsExpenses.Expenses));
                        income = data.getDouble(data.getColumnIndex(QueryReportIncomeVsExpenses.Income));
                        //move to next record
                        data.moveToNext();
                    }
                }
                TextView txtIncome = (TextView) getActivity().findViewById(R.id.textViewIncome);
                TextView txtExpenses = (TextView) getActivity().findViewById(R.id.textViewExpenses);
                TextView txtDifference = (TextView) getActivity().findViewById(R.id.textViewDifference);
                // set value
                if (txtIncome != null)
                    txtIncome.setText(currencyUtils.getCurrencyFormatted(currencyUtils.getBaseCurrencyId(), income));
                if (txtExpenses != null)
                    txtExpenses.setText(currencyUtils.getCurrencyFormatted(currencyUtils.getBaseCurrencyId(), Math.abs(expenses)));
                if (txtDifference != null)
                    txtDifference.setText(currencyUtils.getCurrencyFormatted(currencyUtils.getBaseCurrencyId(), income - Math.abs(expenses)));
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

    @Override
    public void onResume() {
        super.onResume();
        // clear subTitle of ActionBar
        ActionBarActivity activity = (ActionBarActivity) getActivity();
        if (activity != null)
            activity.getSupportActionBar().setSubtitle(null);
        // start loader data
        startLoader();
    }

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

    public void startLoader() {
        getLoaderManager().restartLoader(ID_LOADER_USER_NAME, null, this);
        getLoaderManager().restartLoader(ID_LOADER_ACCOUNT_BILLS, null, this);
        /*getLoaderManager().restartLoader(ID_LOADER_BILL_DEPOSITS, null, this);*/
        getLoaderManager().restartLoader(ID_LOADER_INCOME_EXPENSES, null, this);
    }

    private void addFooterExpandableListView(double curTotal, double curReconciled) {
        // manage footer list view
        if (linearFooter == null) {
            linearFooter = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.item_account_bills, null);
            // textview into layout
            txtFooterSummary = (TextView) linearFooter.findViewById(R.id.textVievItemAccountTotal);
            txtFooterSummaryReconciled = (TextView) linearFooter.findViewById(R.id.textVievItemAccountTotalReconciled);
            // set text
            TextView txtTextSummary = (TextView) linearFooter.findViewById(R.id.textVievItemAccountName);
            txtTextSummary.setText(R.string.summary);
            // invisibile image
            ImageView imgSummary = (ImageView) linearFooter.findViewById(R.id.imageViewAccountType);
            imgSummary.setVisibility(View.INVISIBLE);
            // set color textview
            txtTextSummary.setTextColor(Color.GRAY);
            txtFooterSummary.setTextColor(Color.GRAY);
            txtFooterSummaryReconciled.setTextColor(Color.GRAY);
        }
        // remove footer
        mExpandableListView.removeFooterView(linearFooter);
        // set text
        txtTotalAccounts.setText(currencyUtils.getBaseCurrencyFormatted(curTotal));
        txtFooterSummary.setText(txtTotalAccounts.getText());
        txtFooterSummaryReconciled.setText(currencyUtils.getBaseCurrencyFormatted(curReconciled));
        // add footer
        mExpandableListView.addFooterView(linearFooter, null, false);
    }

    private void setUpAccountsList(View view) {
        mExpandableListView = (ExpandableListView) view.findViewById(R.id.listViewAccountBills);

        mExpandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                QueryAccountBills selectedAccount = mAccountsByType.get(mAccountTypes.get(groupPosition)).get(childPosition);
                if (selectedAccount != null) {
                    MainActivity activity = (MainActivity) getActivity();
                    if (activity != null) {
                        activity.showFragmentAccount(childPosition, selectedAccount.getAccountId());
                        return true;
                    }
                }
                return false;
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
    }

    private String getSettingsKeyFromGroupPosition(int groupPosition) {
        // get group name from position
        String accountType = mAccountTypes.get(groupPosition);
        String key = getActivity().getString(PreferencesConstant.PREF_DASHBOARD_GROUP_VISIBLE);
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

    private class AccountBillsExpandableAdapter extends BaseExpandableListAdapter {
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

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            ViewHolderAccountBills holder;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.item_account_bills, null);

                holder = new ViewHolderAccountBills();
                holder.txtAccountName = (TextView) convertView.findViewById(R.id.textVievItemAccountName);
                holder.txtAccountTotal = (TextView) convertView.findViewById(R.id.textVievItemAccountTotal);
                holder.txtAccountReconciled = (TextView) convertView.findViewById(R.id.textVievItemAccountTotalReconciled);
                holder.imgAccountType = (ImageView) convertView.findViewById(R.id.imageViewAccountType);

                holder.txtAccountName.setTypeface(null, Typeface.BOLD);
                holder.txtAccountTotal.setTypeface(null, Typeface.BOLD);
                holder.txtAccountReconciled.setTypeface(null, Typeface.BOLD);

                convertView.setTag(holder);
            }
            holder = (ViewHolderAccountBills) convertView.getTag();

            String accountType = mAccountTypes.get(groupPosition);
            QueryAccountBills total = mTotalsByType.get(accountType);
            if (total != null) {
                // set account type value
                holder.txtAccountTotal.setText(currencyUtils.getBaseCurrencyFormatted(total.getTotalBaseConvRate()));
                holder.txtAccountReconciled.setText(currencyUtils.getBaseCurrencyFormatted(total.getReconciledBaseConvRate()));
                // set account name
                holder.txtAccountName.setText(total.getAccountName());
            }
            // set imageview account type
            if (!TextUtils.isEmpty(accountType)) {
                if (Constants.ACCOUNT_TYPE_TERM.equalsIgnoreCase(accountType)) {
                    holder.imgAccountType.setImageDrawable(getResources().getDrawable(R.drawable.ic_money_finance));
                } else if (Constants.ACCOUNT_TYPE_CREDIT_CARD.equalsIgnoreCase(accountType)) {
                    holder.imgAccountType.setImageDrawable(getResources().getDrawable(R.drawable.ic_credit_card));
                }
            }

            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            ViewHolderAccountBills holder;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.item_account_bills, null);

                holder = new ViewHolderAccountBills();
                holder.txtAccountName = (TextView) convertView.findViewById(R.id.textVievItemAccountName);
                holder.txtAccountTotal = (TextView) convertView.findViewById(R.id.textVievItemAccountTotal);
                holder.txtAccountReconciled = (TextView) convertView.findViewById(R.id.textVievItemAccountTotalReconciled);
                holder.imgAccountType = (ImageView) convertView.findViewById(R.id.imageViewAccountType);

                holder.txtAccountTotal.setTypeface(null, Typeface.NORMAL);
                holder.imgAccountType.setVisibility(View.INVISIBLE);

                convertView.setTag(holder);
            }
            holder = (ViewHolderAccountBills) convertView.getTag();
            String accountType = mAccountTypes.get(groupPosition);

            QueryAccountBills bills = mAccountsByType.get(accountType).get(childPosition);
            // set account name
            holder.txtAccountName.setText(bills.getAccountName());
            // import formatted
            String value = currencyUtils.getCurrencyFormatted(bills.getCurrencyId(), bills.getTotal());
            // set amount value
            holder.txtAccountTotal.setText(value);
            // reconciled
            value = currencyUtils.getCurrencyFormatted(bills.getCurrencyId(), bills.getReconciled());
            holder.txtAccountReconciled.setText(value);

            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        private class ViewHolderAccountBills {
            TextView txtAccountName;
            TextView txtAccountTotal;
            TextView txtAccountReconciled;
            ImageView imgAccountType;
        }
    }

}
