/*
 * Copyright (C) 2012-2018 The Android Money Manager Ex Project Team
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

import android.content.ContentValues;
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
import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.database.ViewMobileData;
import com.money.manager.ex.search.CategorySub;
import com.money.manager.ex.search.SearchActivity;
import com.money.manager.ex.search.SearchParameters;

import org.parceler.Parcels;

import java.util.ArrayList;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.loader.content.Loader;
import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;
import timber.log.Timber;

/**
 * Categories report fragment.
 */
public class CategoriesReportFragment
    extends BaseReportFragment {

    private LinearLayout mListViewFooter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        setListAdapter(null);
        setSearchMenuVisible(true);

        //create header view
        LinearLayout mListViewHeader = (LinearLayout) addListViewHeaderFooter(R.layout.item_generic_report_2_columns);
        TextView txtColumn1 = (TextView) mListViewHeader.findViewById(R.id.textViewColumn1);
        TextView txtColumn2 = (TextView) mListViewHeader.findViewById(R.id.textViewColumn2);
        //set header
        txtColumn1.setText(R.string.category);
        txtColumn1.setTypeface(null, Typeface.BOLD);
        txtColumn2.setText(R.string.amount);
        txtColumn2.setTypeface(null, Typeface.BOLD);
        //add to list view
        getListView().addHeaderView(mListViewHeader);

        //create footer view
        mListViewFooter = (LinearLayout) addListViewHeaderFooter(R.layout.item_generic_report_2_columns);
        txtColumn1 = (TextView) mListViewFooter.findViewById(R.id.textViewColumn1);
        txtColumn2 = (TextView) mListViewFooter.findViewById(R.id.textViewColumn2);
        //set footer
        txtColumn1.setText(R.string.total);
        txtColumn1.setTypeface(null, Typeface.BOLD_ITALIC);
        txtColumn2.setText(R.string.total);
        txtColumn2.setTypeface(null, Typeface.BOLD_ITALIC);

        //add to list view --> move to load finished
        //getListView().addFooterView(mListViewFooter);

        //set adapter
        CategoriesReportAdapter adapter = new CategoriesReportAdapter(getActivity(), null);
        setListAdapter(adapter);
        //call super method
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        // pie chart
        MenuItem itemChart = menu.findItem(R.id.menu_chart);
        if (itemChart != null) {
            itemChart.setVisible(!(((CategoriesReportActivity) getActivity()).mIsDualPanel));
            UIHelper uiHelper = new UIHelper(getActivity());
            itemChart.setIcon(uiHelper.resolveAttribute(R.attr.ic_action_pie_chart));
        }
    }

    // Loader

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        super.onLoadFinished(loader, data);

        switch (loader.getId()) {
            case ID_LOADER:
                //parse cursor for calculate total
                if (data == null) return;

                CurrencyService currencyService = new CurrencyService(getActivity().getApplicationContext());

                Money totalAmount = MoneyFactory.fromString("0");
                while (data.moveToNext()) {
                    String totalRow = data.getString(data.getColumnIndex("TOTAL"));
                    if (!TextUtils.isEmpty(totalRow)) {
                        totalAmount = totalAmount.add(MoneyFactory.fromString(totalRow));
                    } else {
                        new UIHelper(getActivity()).showToast("reading total");
                    }
                }
                TextView txtColumn2 = (TextView) mListViewFooter.findViewById(R.id.textViewColumn2);
                txtColumn2.setText(currencyService.getBaseCurrencyFormatted(totalAmount));

                // solved bug chart
                if (data.getCount() > 0) {
                    getListView().removeFooterView(mListViewFooter);
                    getListView().addFooterView(mListViewFooter);
                }

                if (((CategoriesReportActivity) getActivity()).mIsDualPanel) {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            showChart();

                        }
                    }, 1000);
                }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_chart:
                showChart();
                break;
        }

        if (item.getItemId() < 0) {
            // category
            String whereClause = getWhereClause();
            if (!TextUtils.isEmpty(whereClause))
                whereClause += " AND ";
            else
                whereClause = "";
            whereClause += " " + ViewMobileData.CATEGID + "=" + Integer.toString(Math.abs(item.getItemId()));
            //create arguments
            Bundle args = new Bundle();
            args.putString(KEY_WHERE_CLAUSE, whereClause);
            //starts loader
            startLoader(args);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected boolean onQueryTextChange(String newText) {
        //recall last where clause
        String whereClause = getWhereClause();
        if (whereClause == null) whereClause = "";

        int start = whereClause.indexOf("/** */");
        if (start > 0) {
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
        whereClause += "(" + ViewMobileData.Category + " Like '%" + newText + "%' OR " +
                ViewMobileData.Subcategory + " Like '%" + newText + "%')/** */";

        //create arguments
        Bundle args = new Bundle();
        args.putString(KEY_WHERE_CLAUSE, whereClause);
        //starts loader
        startLoader(args);
        return super.onQueryTextChange(newText);
    }

    @Override
    protected String prepareQuery(String whereClause) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        ViewMobileData mobileData = new ViewMobileData(getContext());

        //data to compose builder
        String[] projectionIn = new String[]{
            "ROWID AS _id", // this does not fetch anything, unfortunately.
            ViewMobileData.CATEGID, ViewMobileData.Category,
            ViewMobileData.SubcategID, ViewMobileData.Subcategory,
            "SUM(" + ViewMobileData.AmountBaseConvRate + ") AS TOTAL"
        };

        String selection = ViewMobileData.Status + "<>'V' AND " +
            ViewMobileData.TransactionType + " IN ('Withdrawal', 'Deposit')";
        if (!TextUtils.isEmpty(whereClause)) {
            selection += " AND " + whereClause;
        }

        String groupBy = ViewMobileData.CATEGID + ", " + ViewMobileData.Category + ", " +
                ViewMobileData.SubcategID + ", " + ViewMobileData.Subcategory;

        String having = null;
        if (!TextUtils.isEmpty(((CategoriesReportActivity) getActivity()).mFilter)) {
            String filter = ((CategoriesReportActivity) getActivity()).mFilter;
            if (TransactionTypes.valueOf(filter).equals(TransactionTypes.Withdrawal)) {
                having = "SUM(" + ViewMobileData.AmountBaseConvRate + ") < 0";
            } else {
                having = "SUM(" + ViewMobileData.AmountBaseConvRate + ") > 0";
            }
        }

        String sortOrder = ViewMobileData.Category + ", " + ViewMobileData.Subcategory;

        //compose builder
        builder.setTables(mobileData.getSource());

        //return query
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            return builder.buildQuery(projectionIn, selection, groupBy, having, sortOrder, null);
        } else {
            return builder.buildQuery(projectionIn, selection, null, groupBy, having, sortOrder, null);
        }
    }

    @Override
    public String getSubTitle() {
        return null;
    }

    /**
     * List item clicked. Show the transaction list for the category.
     * @param l        The ListView where the click happened
     * @param v        The view that was clicked within the ListView
     * @param position The position of the view in the list
     * @param id       The row id of the item that was clicked
     */
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        CategorySub category = null;
        try {
            category = getCategoryFromSelectedItem(l, position);
        } catch (Exception e) {
            Timber.e(e, "getting category from selected item");
        }
        if (category == null) return;

        // now list the transactions for the given category/subcategory combination,
        // in the selected time period.

//        showTransactionsFragment(values);

        // Show search activity with the results.
        SearchParameters parameters = new SearchParameters();
        parameters.category = category;
        parameters.dateFrom = mDateFrom;
        parameters.dateTo = mDateTo;

        showSearchActivityFor(parameters);
    }

    public void showChart() {
        CategoriesReportAdapter adapter = (CategoriesReportAdapter) getListAdapter();
        if (adapter == null) return;
        Cursor cursor = adapter.getCursor();
        if (cursor == null) return;
        if (cursor.getCount() <= 0) return;

        ArrayList<ValuePieEntry> arrayList = new ArrayList<>();
        CurrencyService currencyService = new CurrencyService(getActivity().getApplicationContext());

        // Reset cursor to initial position.
        cursor.moveToPosition(-1);
        // process cursor
        while (cursor.moveToNext()) {
            ValuePieEntry item = new ValuePieEntry();
            String category = cursor.getString(cursor.getColumnIndex(ViewMobileData.Category));
            if (!TextUtils.isEmpty(cursor.getString(cursor.getColumnIndex(ViewMobileData.Subcategory)))) {
                category += " : " + cursor.getString(cursor.getColumnIndex(ViewMobileData.Subcategory));
            }
            // total
            double total = Math.abs(cursor.getDouble(cursor.getColumnIndex("TOTAL")));
            // check if category is empty
            if (TextUtils.isEmpty(category)) {
                category = getString(R.string.empty_category);
            }

            item.setText(category);
            item.setValue(total);
            item.setValueFormatted(currencyService.getCurrencyFormatted(currencyService.getBaseCurrencyId(),
                    MoneyFactory.fromDouble(total)));
            // add element
            arrayList.add(item);
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
            if (((CategoriesReportActivity) getActivity()).mIsDualPanel) {
                fragmentTransaction.replace(R.id.fragmentChart, fragment, PieChartFragment.class.getSimpleName());
            } else {
                fragmentTransaction.replace(R.id.fragmentMain, fragment, PieChartFragment.class.getSimpleName());
                fragmentTransaction.addToBackStack(null);
            }
            fragmentTransaction.commit();
        }
    }

    // Private

    private CategorySub getCategoryFromSelectedItem(ListView l, int position) {
        // Reading item from the list view, not adapter!
        Object item = l.getItemAtPosition(position);
        if (item == null) return null;

        Cursor cursor = (Cursor) item;

        ContentValues values = new ContentValues();
        DatabaseUtils.cursorIntToContentValues(cursor, ViewMobileData.CATEGID, values);
        DatabaseUtils.cursorStringToContentValues(cursor, ViewMobileData.Category, values);
        DatabaseUtils.cursorIntToContentValues(cursor, ViewMobileData.SubcategID, values);
        DatabaseUtils.cursorStringToContentValues(cursor, ViewMobileData.Subcategory, values);

        CategorySub result = new CategorySub();
        result.categId = values.getAsInteger(ViewMobileData.CATEGID);
        result.categName = values.getAsString(ViewMobileData.Category);
        result.subCategId = values.getAsInteger(ViewMobileData.SubcategID);
        result.subCategName = values.getAsString(ViewMobileData.Subcategory);
        return result;
    }

    private void showSearchActivityFor(SearchParameters parameters) {
        Intent intent = new Intent(getActivity(), SearchActivity.class);
        intent.putExtra(SearchActivity.EXTRA_SEARCH_PARAMETERS, Parcels.wrap(parameters));
        intent.setAction(Intent.ACTION_INSERT);
        startActivity(intent);
    }

}
