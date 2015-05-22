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
package com.money.manager.ex.reports;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.money.manager.ex.R;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.database.ViewMobileData;
import com.money.manager.ex.fragment.BaseFragmentActivity;
import com.money.manager.ex.fragment.IncomeVsExpensesChartFragment;
import com.money.manager.ex.fragment.PieChartFragment;
import com.money.manager.ex.utils.CurrencyUtils;

import java.util.ArrayList;

public class PayeesReportActivity extends BaseFragmentActivity {
    static CurrencyUtils currencyUtils;
    public boolean mIsDualPanel = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_chart_fragments_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            // set actionbar
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        //check if is dual panel
        mIsDualPanel = findViewById(R.id.fragmentChart) != null;

        //reference to application
        currencyUtils = new CurrencyUtils(getApplicationContext());
        //create a fragment
        PayeeReportFragment fragment = new PayeeReportFragment();
        FragmentManager fm = getSupportFragmentManager();
        //insert fragment
        if (fm.findFragmentById(R.id.fragmentContent) == null) {
            fm.beginTransaction().add(R.id.fragmentContent, fragment, PayeeReportFragment.class.getSimpleName()).commit();
        }
    }

    private static class PayeeReportAdapter extends CursorAdapter {
        private LayoutInflater mInflater;

        @SuppressWarnings("deprecation")
        public PayeeReportAdapter(Context context, Cursor c) {
            super(context, c);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView txtColumn1 = (TextView) view.findViewById(R.id.textViewColumn1);
            TextView txtColumn2 = (TextView) view.findViewById(R.id.textViewColumn2);
            double total = cursor.getDouble(cursor.getColumnIndex("TOTAL"));
            if (!TextUtils.isEmpty(cursor.getString(cursor.getColumnIndex(ViewMobileData.Payee)))) {
                txtColumn1.setText(cursor.getString(cursor.getColumnIndex(ViewMobileData.Payee)));
            } else {
                txtColumn1.setText(context.getString(R.string.empty_payee));
            }

            txtColumn2.setText(currencyUtils.getCurrencyFormatted(currencyUtils.getBaseCurrencyId(), total));
            Core core = new Core(context);
            if (total < 0) {
                txtColumn2.setTextColor(context.getResources().getColor(core.resolveIdAttribute(R.attr.holo_red_color_theme)));
            } else {
                txtColumn2.setTextColor(context.getResources().getColor(core.resolveIdAttribute(R.attr.holo_green_color_theme)));
            }

            view.setBackgroundColor(core.resolveColorAttribute(cursor.getPosition() % 2 == 1 ? R.attr.row_dark_theme : R.attr.row_light_theme));
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup root) {
            return mInflater.inflate(R.layout.item_generic_report_2_columns, root, false);
        }
    }

    public static class PayeeReportFragment extends BaseReportFragment {
        private LinearLayout mHeaderListView, mFooterListView;

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            setListAdapter(null);
            setShowMenuItemSearch(true);
            //create header view
            mHeaderListView = (LinearLayout) addListViewHeaderFooter(R.layout.item_generic_report_2_columns);
            TextView txtColumn1 = (TextView) mHeaderListView.findViewById(R.id.textViewColumn1);
            TextView txtColumn2 = (TextView) mHeaderListView.findViewById(R.id.textViewColumn2);
            //set header
            txtColumn1.setText(R.string.payee);
            txtColumn1.setTypeface(null, Typeface.BOLD);
            txtColumn2.setText(R.string.amount);
            txtColumn2.setTypeface(null, Typeface.BOLD);
            //add to listview
            getListView().addHeaderView(mHeaderListView);
            //create footer view
            mFooterListView = (LinearLayout) addListViewHeaderFooter(R.layout.item_generic_report_2_columns);
            txtColumn1 = (TextView) mFooterListView.findViewById(R.id.textViewColumn1);
            txtColumn2 = (TextView) mFooterListView.findViewById(R.id.textViewColumn2);
            //set footer
            txtColumn1.setText(R.string.total);
            txtColumn1.setTypeface(null, Typeface.BOLD_ITALIC);
            txtColumn2.setText(R.string.total);
            txtColumn2.setTypeface(null, Typeface.BOLD_ITALIC);
            //add to listview
            //getListView().addFooterView(mFooterListView);
            //set adapter
            PayeeReportAdapter adapter = new PayeeReportAdapter(getActivity(), null);
            setListAdapter(adapter);

            super.onActivityCreated(savedInstanceState);
        }

        @Override
        protected boolean onQueryTextChange(String newText) {
            //recall last where clause
            String whereClause = getWhereClause();
            if (whereClause == null) whereClause = "";

            int start = whereClause.indexOf("/** */");
            if (start >= 0) {
                int end = whereClause.indexOf("/** */", start + 1) + "/** */".length();
                whereClause = whereClause.substring(0, start) + whereClause.substring(end);
                // trim some space
                whereClause = whereClause.trim();
            }

            if (!TextUtils.isEmpty(whereClause)) {
                whereClause += " /** */AND ";
            } else {
                whereClause = "/** */";
            }
            // use token to replace criteria
            whereClause += "(" + ViewMobileData.Payee + " Like '%" + newText + "%')/** */";

            //create arguments
            Bundle args = new Bundle();
            args.putString(KEY_WHERE_CLAUSE, whereClause);
            //starts loader
            startLoader(args);
            return super.onQueryTextChange(newText);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            super.onLoadFinished(loader, data);
            switch (loader.getId()) {
                case ID_LOADER:
                    //parse cursor for calculate total
                    if (data != null && data.moveToFirst()) {
                        double totalAmount = 0;
                        while (!data.isAfterLast()) {
                            totalAmount += data.getDouble(data.getColumnIndex("TOTAL"));
                            data.moveToNext();
                        }
                        TextView txtColumn2 = (TextView) mFooterListView.findViewById(R.id.textViewColumn2);
                        txtColumn2.setText(currencyUtils.getBaseCurrencyFormatted(totalAmount));
                        // soved bug chart
                        if (data.getCount() > 0) {
                            getListView().removeFooterView(mFooterListView);
                            getListView().addFooterView(mFooterListView);
                        }
                        // handler to show chart
                        if (((PayeesReportActivity) getActivity()).mIsDualPanel) {
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {

                                @Override
                                public void run() {
                                    showChart();
                                }
                            }, 1 * 1000);
                        }
                    }
            }
        }

        @SuppressWarnings("deprecation")
        @Override
        protected String prepareQuery(String whereClause) {
            SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
            ViewMobileData mobileData = new ViewMobileData();
            //data to compose builder
            String[] projectionIn = new String[]{ViewMobileData.PayeeID + " AS _id", ViewMobileData.PayeeID, ViewMobileData.Payee, "SUM(" + ViewMobileData.AmountBaseConvRate + ") AS TOTAL"};
            String selection = ViewMobileData.Status + "<>'V' AND " + ViewMobileData.TransactionType + " IN ('Withdrawal', 'Deposit')";
            if (!TextUtils.isEmpty(whereClause)) {
                selection += " AND " + whereClause;
            }
            String groupBy = ViewMobileData.PayeeID + ", " + ViewMobileData.Payee;
            String having = null;
            String sortOrder = ViewMobileData.Payee;
            String limit = null;
            //compose builder
            builder.setTables(mobileData.getSource());
            //return query
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                return builder.buildQuery(projectionIn, selection, groupBy, having, sortOrder, limit);
            } else {
                return builder.buildQuery(projectionIn, selection, null, groupBy, having, sortOrder, limit);
            }
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);

            Core core = new Core(getActivity());
            // pie chart
            MenuItem itemChart = menu.findItem(R.id.menu_chart);
            if (itemChart != null) {
                itemChart.setVisible(!(((PayeesReportActivity) getActivity()).mIsDualPanel));
                itemChart.setIcon(core.resolveIdAttribute(R.attr.ic_action_pie_chart));
            }
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            if (item.getItemId() == R.id.menu_chart) {
                showChart();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        public void showChart() {
            PayeeReportAdapter adapter = (PayeeReportAdapter) getListAdapter();
            if (adapter == null) return;
            Cursor cursor = adapter.getCursor();
            if (cursor == null) return;
            // move first record
            if (!cursor.moveToFirst()) return;
            // create arraylist
            ArrayList<ValuePieEntry> arrayList = new ArrayList<ValuePieEntry>();
            // process cursor
            while (!cursor.isAfterLast()) {
                ValuePieEntry item = new ValuePieEntry();
                // total
                double total = Math.abs(cursor.getDouble(cursor.getColumnIndex("TOTAL")));
                if (!TextUtils.isEmpty(cursor.getString(cursor.getColumnIndex(ViewMobileData.Payee)))) {
                    item.setText(cursor.getString(cursor.getColumnIndex(ViewMobileData.Payee)));
                } else {
                    item.setText(getString(R.string.empty_payee));
                }
                item.setValue(total);
                item.setValueFormatted(currencyUtils.getCurrencyFormatted(currencyUtils.getBaseCurrencyId(), total));
                // add element
                arrayList.add(item);
                // move to next record
                cursor.moveToNext();
            }
            Bundle args = new Bundle();
            args.putSerializable(PieChartFragment.KEY_CATEGORIES_VALUES, arrayList);
            //get fragment manager
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            if (fragmentManager != null) {
                PieChartFragment fragment;
                fragment = (PieChartFragment) fragmentManager.findFragmentByTag(IncomeVsExpensesChartFragment.class.getSimpleName());
                if (fragment == null) {
                    fragment = new PieChartFragment();
                }
                fragment.setChartArguments(args);
                fragment.setDisplayHomeAsUpEnabled(true);

                if (fragment.isVisible()) fragment.onResume();

                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                if (((PayeesReportActivity) getActivity()).mIsDualPanel) {
                    fragmentTransaction.replace(R.id.fragmentChart, fragment, PieChartFragment.class.getSimpleName());
                } else {
                    fragmentTransaction.replace(R.id.fragmentContent, fragment, PieChartFragment.class.getSimpleName());
                    fragmentTransaction.addToBackStack(null);
                }
                fragmentTransaction.commit();
            }
        }

        @Override
        public String getSubTitle() {
            // TODO Auto-generated method stub
            return null;
        }
    }
}
