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
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableRow;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.money.manager.ex.R;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.database.QueryReportIncomeVsExpenses;
import com.money.manager.ex.database.SQLDataSet;
import com.money.manager.ex.database.ViewMobileData;
import com.money.manager.ex.fragment.BaseFragmentActivity;
import com.money.manager.ex.fragment.IncomeVsExpensesChartFragment;
import com.money.manager.ex.utils.CurrencyUtils;

import org.apache.commons.lang3.ArrayUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class IncomeVsExpensesActivity extends BaseFragmentActivity {
    private static final String LOGCAT = IncomeVsExpensesActivity.class.getSimpleName();
    private static final int SUBTOTAL_MONTH = 99;
    private static CurrencyUtils currencyUtils;
    public boolean mIsDualPanel = false;
    private IncomeVsExpensesListFragment listFragment = new IncomeVsExpensesListFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_chart_fragments_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            // set actionbar
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // check if is dual panel
        mIsDualPanel = findViewById(R.id.fragmentChart) != null;

        FragmentManager fm = getSupportFragmentManager();
        // get application
        currencyUtils = new CurrencyUtils(getApplicationContext());
        // attach fragment activity
        if (fm.findFragmentById(R.id.fragmentContent) == null) {
            fm.beginTransaction().replace(R.id.fragmentContent, listFragment, IncomeVsExpensesListFragment.class.getSimpleName()).commit();
        }
    }

    private static class IncomeVsExpensesAdapter extends CursorAdapter {
        private LayoutInflater mInflater;

        @SuppressWarnings("deprecation")
        public IncomeVsExpensesAdapter(Context context, Cursor c) {
            super(context, c);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView txtYear = (TextView) view.findViewById(R.id.textViewYear);
            TextView txtMonth = (TextView) view.findViewById(R.id.textViewMonth);
            TextView txtIncome = (TextView) view.findViewById(R.id.textViewIncome);
            TextView txtExpenses = (TextView) view.findViewById(R.id.textViewExpenses);
            TextView txtDifference = (TextView) view.findViewById(R.id.textViewDifference);
            // take data
            int year, month;
            year = cursor.getInt(cursor.getColumnIndex(QueryReportIncomeVsExpenses.Year));
            month = cursor.getInt(cursor.getColumnIndex(QueryReportIncomeVsExpenses.Month));
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month - 1, 1);
            double income = 0, expenses = 0;
            expenses = cursor.getDouble(cursor.getColumnIndex(QueryReportIncomeVsExpenses.Expenses));
            income = cursor.getDouble(cursor.getColumnIndex(QueryReportIncomeVsExpenses.Income));
            // attach data
            txtYear.setText(Integer.toString(year));
            //txtMonth.setText(new SimpleDateFormat("MMMM").format(new Date(year, month - 1, 1)));
            String formatMonth = context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? "MMM" : "MMMM";

            if (month != SUBTOTAL_MONTH) {
                txtMonth.setText(new SimpleDateFormat(formatMonth).format(calendar.getTime()));
            } else {
                txtMonth.setText(null);
            }
            txtIncome.setText(currencyUtils.getCurrencyFormatted(currencyUtils.getBaseCurrencyId(), income));
            txtExpenses.setText(currencyUtils.getCurrencyFormatted(currencyUtils.getBaseCurrencyId(), Math.abs(expenses)));
            txtDifference.setText(currencyUtils.getCurrencyFormatted(currencyUtils.getBaseCurrencyId(), income - Math.abs(expenses)));
            Core core = new Core(context);
            if (income - Math.abs(expenses) < 0) {
                txtDifference.setTextColor(context.getResources().getColor(core.resolveIdAttribute(R.attr.holo_red_color_theme)));
            } else {
                txtDifference.setTextColor(context.getResources().getColor(core.resolveIdAttribute(R.attr.holo_green_color_theme)));
            }
            //view.setBackgroundColor(core.resolveColorAttribute(cursor.getPosition() % 2 == 1 ? R.attr.row_dark_theme : R.attr.row_light_theme));
            // check if subtotal
            int typefaceStyle = month == SUBTOTAL_MONTH ? Typeface.BOLD : Typeface.NORMAL;

            txtDifference.setTypeface(null, typefaceStyle);
            txtExpenses.setTypeface(null, typefaceStyle);
            txtIncome.setTypeface(null, typefaceStyle);
            txtMonth.setTypeface(null, typefaceStyle);
            txtYear.setTypeface(null, typefaceStyle);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return mInflater.inflate(R.layout.tablerow_income_vs_expenses, parent, false);
        }
    }

    public static class IncomeVsExpensesListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
        private static final int ID_LOADER_REPORT = 1;
        private static final int ID_LOADER_YEARS = 2;
        private static final String SORT_ASCENDING = "ASC";
        private static final String SORT_DESCENDING = "DESC";
        private static final String KEY_BUNDLE_YEAR = "IncomeVsExpensesListFragment:Years";
        private View mFooterListView;
        private SparseBooleanArray mYearsSelected = new SparseBooleanArray();
        private String mSort = SORT_ASCENDING;

        /**
         * Add footer to ListView
         *
         * @return View of footer
         */
        private View addListViewFooter() {
            TableRow row = (TableRow) View.inflate(getActivity(), R.layout.tablerow_income_vs_expenses, null);
            TextView txtYear = (TextView) row.findViewById(R.id.textViewYear);
            txtYear.setText(getString(R.string.total));
            txtYear.setTypeface(null, Typeface.BOLD);
            TextView txtMonth = (TextView) row.findViewById(R.id.textViewMonth);
            txtMonth.setText(null);
            return row;
        }

        /**
         * Add header to ListView
         */
        private View addListViewHeader() {
            TableRow row = (TableRow) View.inflate(getActivity(), R.layout.tablerow_income_vs_expenses, null);
            int[] ids = new int[]{R.id.textViewYear, R.id.textViewMonth, R.id.textViewIncome, R.id.textViewExpenses, R.id.textViewDifference};
            for (int id : ids) {
                TextView textView = (TextView) row.findViewById(id);
                textView.setTypeface(null, Typeface.BOLD);
                textView.setSingleLine(true);
            }
            getListView().addHeaderView(row);

            return row;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            setHasOptionsMenu(true);
            if (savedInstanceState != null && savedInstanceState.containsKey(KEY_BUNDLE_YEAR) && savedInstanceState.getIntArray(KEY_BUNDLE_YEAR) != null) {
                for (int year : savedInstanceState.getIntArray(KEY_BUNDLE_YEAR)) {
                    mYearsSelected.put(year, true);
                }
            } else {
                mYearsSelected.put(Calendar.getInstance().get(Calendar.YEAR), true);
            }
            // set home button
//            ActionBarActivity activity = (ActionBarActivity) getActivity();
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            if (activity != null) {
                //activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            }
            // set listview
            setEmptyText(getString(R.string.no_data));
            // add header and footer
            try {
                setListAdapter(null);
                addListViewHeader();
                mFooterListView = addListViewFooter();
            } catch (Exception e) {
                Log.e(LOGCAT, e.getMessage());
            }
            // create adapter
            IncomeVsExpensesAdapter adapter = new IncomeVsExpensesAdapter(getActivity(), null);
            setListAdapter(adapter);
            setListShown(false);
            // start loader
            //startLoader();
            getLoaderManager().restartLoader(ID_LOADER_YEARS, null, this);
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            String selection = null;
            switch (id) {
                case ID_LOADER_REPORT:
                    QueryReportIncomeVsExpenses report = new QueryReportIncomeVsExpenses(getActivity());
                    if (args != null && args.containsKey(KEY_BUNDLE_YEAR) && args.getString(KEY_BUNDLE_YEAR) != null) {
                        selection = QueryReportIncomeVsExpenses.Year + " IN (" + args.getString(KEY_BUNDLE_YEAR) + ")";
                        if (!TextUtils.isEmpty(selection)) {
                            selection = "(" + selection + ")";
                        }
                    }
                    // if don't have selection abort query
                    if (TextUtils.isEmpty(selection)) {
                        selection = "1=2";
                    }
                    return new CursorLoader(getActivity(), report.getUri(), report.getAllColumns(), selection, null, QueryReportIncomeVsExpenses.Year + " " + mSort + ", " + QueryReportIncomeVsExpenses.Month + " " + mSort);
                case ID_LOADER_YEARS:
                    selection = "SELECT DISTINCT Year FROM " + ViewMobileData.mobiledata + " ORDER BY Year DESC";
                    return new CursorLoader(getActivity(), new SQLDataSet().getUri(), null, selection, null, null);
            }
            return null;
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            inflater.inflate(R.menu.menu_report_income_vs_expenses, menu);
            // fix menu char
            MenuItem itemChart = menu.findItem(R.id.menu_chart);
            if (itemChart != null) {
                itemChart.setVisible(!((IncomeVsExpensesActivity) getActivity()).mIsDualPanel);
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            ((IncomeVsExpensesAdapter) getListAdapter()).swapCursor(null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            switch (loader.getId()) {
                case ID_LOADER_REPORT:
                    ((IncomeVsExpensesAdapter) getListAdapter()).swapCursor(data);
                    if (isResumed()) {
                        setListShown(true);
                    } else {
                        setListShownNoAnimation(true);
                    }
                    // calculate income, expenses
                    double income = 0, expenses = 0;
                    if (data != null && data.moveToFirst()) {
                        while (!data.isAfterLast()) {
                            if (data.getInt(data.getColumnIndex(QueryReportIncomeVsExpenses.Month)) != SUBTOTAL_MONTH) {
                                income += data.getDouble(data.getColumnIndex(QueryReportIncomeVsExpenses.Income));
                                expenses += data.getDouble(data.getColumnIndex(QueryReportIncomeVsExpenses.Expenses));
                            }
                            // move to next record
                            data.moveToNext();
                        }
                        updateListViewFooter(mFooterListView, income, expenses);
                        if (data.getCount() > 0) {
                            getListView().removeFooterView(mFooterListView);
                            getListView().addFooterView(mFooterListView);
                        }

                    }
                    if (((IncomeVsExpensesActivity) getActivity()).mIsDualPanel) {
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                showChart();
                            }
                        }, 1 * 1000);
                    }
                    break;
                case ID_LOADER_YEARS:
                    if (data != null && data.moveToFirst()) {
                        while (!data.isAfterLast()) {
                            int year = data.getInt(data.getColumnIndex("Year"));
                            if (mYearsSelected.get(year, false) == false) {
                                mYearsSelected.put(year, false);
                            }
                            data.moveToNext();
                        }
                        startLoader();
                    }
            }
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            if (item.getItemId() == android.R.id.home) {
                getActivity().finish();
            } else if (item.getItemId() == R.id.menu_sort_asceding || item.getItemId() == R.id.menu_sort_desceding) {
                mSort = item.getItemId() == R.id.menu_sort_asceding ? SORT_ASCENDING : SORT_DESCENDING;
                startLoader();
                item.setChecked(true);
            } else if (item.getItemId() == R.id.menu_chart) {
                showChart();
            } else if (item.getItemId() == R.id.menu_period) {
                showDialogYears();
            }

            return super.onOptionsItemSelected(item);
        }

        public void showDialogYears() {
            ArrayList<String> years = new ArrayList<String>();
            Integer[] selected = new Integer[0];

            for (int i = 0; i < mYearsSelected.size(); i++) {
                years.add(String.valueOf(mYearsSelected.keyAt(i)));
                if (mYearsSelected.valueAt(i)) {
                    selected = ArrayUtils.add(selected, i);
                }
            }

            new MaterialDialog.Builder(getActivity())
                    .items(years.toArray(new String[years.size()]))
                    .itemsCallbackMultiChoice(selected, new MaterialDialog.ListCallbackMultiChoice() {
                        @Override
                        public boolean onSelection(MaterialDialog materialDialog, Integer[] integers, CharSequence[] charSequences) {
                            // reset to false all years
                            for (int i = 0; i < mYearsSelected.size(); i++) {
                                mYearsSelected.put(mYearsSelected.keyAt(i), false);
                            }
                            // set year select
                            for (int index : integers) {
                                mYearsSelected.put(mYearsSelected.keyAt(index), true);
                            }
                            startLoader();
                            return true;
                        }
                    })
                            //.alwaysCallMultiChoiceCallback()
                    .positiveText(android.R.string.ok)
                    .show();
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            ArrayList<Integer> years = new ArrayList<Integer>();
            for (int i = 0; i < mYearsSelected.size(); i++) {
                if (mYearsSelected.get(mYearsSelected.keyAt(i))) {
                    years.add(mYearsSelected.keyAt(i));
                }
            }
            outState.putIntArray(KEY_BUNDLE_YEAR, ArrayUtils.toPrimitive(years.toArray(new Integer[0])));
        }

        /**
         * Start loader with arrays year
         *
         */
        private void startLoader() {
            Bundle bundle = new Bundle();
            String years = "";
            for (int i = 0; i < mYearsSelected.size(); i++) {
                if (mYearsSelected.get(mYearsSelected.keyAt(i))) {
                    years += (!TextUtils.isEmpty(years) ? ", " : "") + Integer.toString(mYearsSelected.keyAt(i));
                }
            }
            bundle.putString(KEY_BUNDLE_YEAR, years);
            getLoaderManager().restartLoader(ID_LOADER_REPORT, bundle, this);
        }

        /**
         * update View of footer with income, expenses and difference
         *
         * @param footer
         * @param income
         * @param expenses
         */
        private void updateListViewFooter(View footer, double income, double expenses) {
            if (footer == null) {
                return;
            }
            TextView txtIncome = (TextView) footer.findViewById(R.id.textViewIncome);
            TextView txtExpenses = (TextView) footer.findViewById(R.id.textViewExpenses);
            TextView txtDifference = (TextView) footer.findViewById(R.id.textViewDifference);
            //set income
            txtIncome.setText(currencyUtils.getCurrencyFormatted(currencyUtils.getBaseCurrencyId(), income));
            txtIncome.setTypeface(null, Typeface.BOLD);
            //set expenses
            txtExpenses.setText(currencyUtils.getCurrencyFormatted(currencyUtils.getBaseCurrencyId(), Math.abs(expenses)));
            txtExpenses.setTypeface(null, Typeface.BOLD);
            //set difference
            txtDifference.setText(currencyUtils.getCurrencyFormatted(currencyUtils.getBaseCurrencyId(), income - Math.abs(expenses)));
            txtDifference.setTypeface(null, Typeface.BOLD);
            //change colors
            Core core = new Core(getActivity());
            if (income - Math.abs(expenses) < 0) {
                txtDifference.setTextColor(getResources().getColor(core.resolveIdAttribute(R.attr.holo_red_color_theme)));
            } else {
                txtDifference.setTextColor(getResources().getColor(core.resolveIdAttribute(R.attr.holo_green_color_theme)));
            }
        }

        private void showChart() {
            // take a adapter and cursor
            IncomeVsExpensesAdapter adapter = ((IncomeVsExpensesAdapter) getListAdapter());
            if (adapter == null) return;
            Cursor cursor = adapter.getCursor();
            if (cursor == null) return;
            // move to first
            if (!cursor.moveToFirst()) return;
            // arrays
            ArrayList<Double> incomes = new ArrayList<Double>();
            ArrayList<Double> expenses = new ArrayList<Double>();
            ArrayList<String> titles = new ArrayList<String>();
            // cycle cursor
            while (!cursor.isAfterLast()) {
                // check if not subtotal
                if (cursor.getInt(cursor.getColumnIndex(QueryReportIncomeVsExpenses.Month)) != SUBTOTAL_MONTH) {
                    // incomes and expenses
                    incomes.add(cursor.getDouble(cursor.getColumnIndex(QueryReportIncomeVsExpenses.Income)));
                    expenses.add(Math.abs(cursor.getDouble(cursor.getColumnIndex(QueryReportIncomeVsExpenses.Expenses))));
                    // titles
                    int year = cursor.getInt(cursor.getColumnIndex(QueryReportIncomeVsExpenses.Year));
                    int month = cursor.getInt(cursor.getColumnIndex(QueryReportIncomeVsExpenses.Month));

                    // format month
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(year, month - 1, 1);
                    // titles
                    titles.add(Integer.toString(year) + "-" + new SimpleDateFormat("MMM").format(calendar.getTime()));
                }
                // move to next
                cursor.moveToNext();
            }
            //compose bundle for arguments
            Bundle args = new Bundle();
            args.putDoubleArray(IncomeVsExpensesChartFragment.KEY_EXPENSES_VALUES, ArrayUtils.toPrimitive(expenses.toArray(new Double[0])));
            args.putDoubleArray(IncomeVsExpensesChartFragment.KEY_INCOME_VALUES, ArrayUtils.toPrimitive(incomes.toArray(new Double[0])));
            args.putStringArray(IncomeVsExpensesChartFragment.KEY_XTITLES, titles.toArray(new String[titles.size()]));
            //get fragment manager
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            if (fragmentManager != null) {
                IncomeVsExpensesChartFragment fragment;
                fragment = (IncomeVsExpensesChartFragment) fragmentManager.findFragmentByTag(IncomeVsExpensesChartFragment.class.getSimpleName());
                if (fragment == null) {
                    fragment = new IncomeVsExpensesChartFragment();
                }
                fragment.setChartArguments(args);
                fragment.setDisplayHomeAsUpEnabled(true);

                if (fragment.isVisible()) fragment.onResume();

                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                if (((IncomeVsExpensesActivity) getActivity()).mIsDualPanel) {
                    fragmentTransaction.replace(R.id.fragmentChart, fragment, IncomeVsExpensesChartFragment.class.getSimpleName());
                } else {
                    fragmentTransaction.replace(R.id.fragmentContent, fragment, IncomeVsExpensesChartFragment.class.getSimpleName());
                    fragmentTransaction.addToBackStack(null);
                }
                fragmentTransaction.commit();
            }
        }
    }
}
