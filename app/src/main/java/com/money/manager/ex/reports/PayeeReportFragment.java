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
package com.money.manager.ex.reports;

import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteQueryBuilder;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.money.manager.ex.R;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.database.ViewMobileData;
import com.money.manager.ex.domainmodel.Payee;
import com.money.manager.ex.search.SearchActivity;
import com.money.manager.ex.search.SearchParameters;

import org.parceler.Parcels;

import java.util.ArrayList;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.loader.content.Loader;
import info.javaperformance.money.MoneyFactory;
import timber.log.Timber;

/**
 * Content fragment for the Payee report.
 */
public class PayeeReportFragment
        extends BaseReportFragment {

    private LinearLayout mHeaderListView, mFooterListView;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        setListAdapter(null);
        setSearchMenuVisible(true);

        //create header view
        mHeaderListView = (LinearLayout) addListViewHeaderFooter(R.layout.item_generic_report_2_columns);
        TextView txtColumn1 = (TextView) mHeaderListView.findViewById(R.id.textViewColumn1);
        TextView txtColumn2 = (TextView) mHeaderListView.findViewById(R.id.textViewColumn2);
        //set header
        txtColumn1.setText(R.string.payee);
        txtColumn1.setTypeface(null, Typeface.BOLD);
        txtColumn2.setText(R.string.amount);
        txtColumn2.setTypeface(null, Typeface.BOLD);
        //add to list view
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
        //add to list view
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
                if (data == null) return;

                //parse cursor for calculate total
                double totalAmount = 0;
                while (data.moveToNext()) {
                    totalAmount += data.getDouble(data.getColumnIndex("TOTAL"));
                }

                CurrencyService currencyService = new CurrencyService(getContext());

                TextView txtColumn2 = (TextView) mFooterListView.findViewById(R.id.textViewColumn2);
                txtColumn2.setText(currencyService.getBaseCurrencyFormatted(MoneyFactory.fromDouble(totalAmount)));

                // solve bug chart
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

    @Override
    protected String prepareQuery(String whereClause) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        ViewMobileData mobileData = new ViewMobileData(getContext());
        //data to compose builder
        String[] projectionIn = new String[]{ ViewMobileData.PAYEEID + " AS _id",
                ViewMobileData.PAYEEID, ViewMobileData.Payee,
                "SUM(" + ViewMobileData.AmountBaseConvRate + ") AS TOTAL"};
        String selection = ViewMobileData.Status + "<>'V' AND " +
                ViewMobileData.TransactionType + " IN ('Withdrawal', 'Deposit')";
        if (!TextUtils.isEmpty(whereClause)) {
            selection += " AND " + whereClause;
        }
        String groupBy = ViewMobileData.PAYEEID + ", " + ViewMobileData.Payee;
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

        // pie chart
        MenuItem itemChart = menu.findItem(R.id.menu_chart);
        if (itemChart != null) {
            itemChart.setVisible(!(((PayeesReportActivity) getActivity()).mIsDualPanel));
            UIHelper uiHelper = new UIHelper(getActivity());
            itemChart.setIcon(uiHelper.resolveAttribute(R.attr.ic_action_pie_chart));
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
        if (!cursor.moveToFirst()) return;

        ArrayList<ValuePieEntry> arrayList = new ArrayList<ValuePieEntry>();
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
            CurrencyService currencyService = new CurrencyService(getContext());
            item.setValueFormatted(currencyService.getBaseCurrencyFormatted(MoneyFactory.fromDouble(total)));
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
                fragmentTransaction.replace(R.id.fragmentMain, fragment, PieChartFragment.class.getSimpleName());
                fragmentTransaction.addToBackStack(null);
            }
            try {
                fragmentTransaction.commit();
            } catch (IllegalStateException e) {
                Timber.e(e, "adding fragment");
            }
        }
    }

    @Override
    public String getSubTitle() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Payee payee = getPayeeFromSelectedItem(l, position);
        if (payee == null) return;

        // now list the transactions for the given payee,
        // in the selected time period.

        // Show search activity with the results.
        SearchParameters parameters = new SearchParameters();
        parameters.payeeId = payee.getId();
        parameters.payeeName = payee.getName();
        parameters.dateFrom = mDateFrom;
        parameters.dateTo = mDateTo;

        showSearchActivityFor(parameters);
    }

    private Payee getPayeeFromSelectedItem(ListView l, int position) {
        // Reading item from the list view, not adapter!
        Object item = l.getItemAtPosition(position);
        if (item == null) return null;

        Cursor cursor = (Cursor) item;
        Payee payee = new Payee();
        /*for (String col : cursor.getColumnNames()) {
            int idx = cursor.getColumnIndex(col);
            Log.d("PayeeReportFragment", " Name " + col + "\t Type " + cursor.getType(idx) + "\t Value " + cursor.getString(idx));
        }*/
//        payee.loadFromCursor(cursor);
        // The fields are different! Can't use standard loadFromCursor.
        DatabaseUtils.cursorIntToContentValues(cursor, ViewMobileData._ID,
                payee.contentValues, Payee.PAYEEID);
        DatabaseUtils.cursorStringToContentValues(cursor, ViewMobileData.Payee,
                payee.contentValues, Payee.PAYEENAME);

        return payee;
    }

    private void showSearchActivityFor(SearchParameters parameters) {
        Intent intent = new Intent(getActivity(), SearchActivity.class);
        intent.putExtra(SearchActivity.EXTRA_SEARCH_PARAMETERS, Parcels.wrap(parameters));
        intent.setAction(Intent.ACTION_INSERT);
        startActivity(intent);
    }

}
