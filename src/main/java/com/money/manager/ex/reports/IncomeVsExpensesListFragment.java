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
package com.money.manager.ex.reports;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TableRow;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.money.manager.ex.R;
import com.money.manager.ex.common.MmexCursorLoader;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.database.QueryReportIncomeVsExpenses;
import com.money.manager.ex.database.SQLDataSet;
import com.money.manager.ex.database.ViewMobileData;
import com.money.manager.ex.search.SearchParameters;
import com.money.manager.ex.utils.CalendarUtils;
import com.money.manager.ex.utils.DateTimeUtils;
import com.money.manager.ex.utils.DateUtils;
import com.money.manager.ex.utils.IntentUtils;
import com.money.manager.ex.viewmodels.IncomeVsExpenseReportEntity;

import org.apache.commons.lang3.ArrayUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import hirondelle.date4j.DateTime;
import info.javaperformance.money.MoneyFactory;

/**
 * Income/Expense Report, list.
 */
public class IncomeVsExpensesListFragment
    extends ListFragment
    implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int ID_LOADER_REPORT = 1;
    private static final int ID_LOADER_YEARS = 2;
    private static final String SORT_ASCENDING = "ASC";
    private static final String SORT_DESCENDING = "DESC";
    private static final String KEY_BUNDLE_YEAR = "IncomeVsExpensesListFragment:Years";

    private View mFooterListView;
    private SparseBooleanArray mYearsSelected = new SparseBooleanArray();
    private String mSort = SORT_ASCENDING;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);

        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_BUNDLE_YEAR) &&
                savedInstanceState.getIntArray(KEY_BUNDLE_YEAR) != null) {
            for (int year : savedInstanceState.getIntArray(KEY_BUNDLE_YEAR)) {
                mYearsSelected.put(year, true);
            }
        } else {
            mYearsSelected.put(Calendar.getInstance().get(Calendar.YEAR), true);
        }

        initializeListView();

        // set home button
//            ActionBarActivity activity = (ActionBarActivity) getActivity();
//        AppCompatActivity activity = (AppCompatActivity) getActivity();
//        if (activity != null) {
            //activity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
//        }

        // create adapter
        IncomeVsExpensesAdapter adapter = new IncomeVsExpensesAdapter(getActivity(), null);
        setListAdapter(adapter);
        setListShown(false);
        // start loader
        //getLoaderManager().restartLoader(ID_LOADER_YEARS, null, this);
        getLoaderManager().initLoader(ID_LOADER_YEARS, null, this);
    }

    // Loader

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection = null;
        switch (id) {
            case ID_LOADER_REPORT:
                if (args != null && args.containsKey(KEY_BUNDLE_YEAR) && args.getString(KEY_BUNDLE_YEAR) != null) {
                    selection = IncomeVsExpenseReportEntity.YEAR + " IN (" + args.getString(KEY_BUNDLE_YEAR) + ")";
                    if (!TextUtils.isEmpty(selection)) {
                        selection = "(" + selection + ")";
                    }
                }
                // if don't have selection abort query
                if (TextUtils.isEmpty(selection)) {
                    selection = "1=2";
                }
                QueryReportIncomeVsExpenses report = new QueryReportIncomeVsExpenses(getActivity());

                return new MmexCursorLoader(getActivity(), report.getUri(),
                    report.getAllColumns(),
                    selection, null,
                    IncomeVsExpenseReportEntity.YEAR + " " + mSort + ", " + IncomeVsExpenseReportEntity.Month + " " + mSort);

            case ID_LOADER_YEARS:
                ViewMobileData mobileData = new ViewMobileData(getContext());
                selection = "SELECT DISTINCT Year FROM " + mobileData.getSource() + " ORDER BY Year DESC";
                return new MmexCursorLoader(getActivity(),
                        new SQLDataSet().getUri(),
                        null,
                        selection,
                        null,
                        null);
        }
        return null;
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
                if (data == null) return;

                while (data.moveToNext()) {
                    if (data.getInt(data.getColumnIndex(IncomeVsExpenseReportEntity.Month)) != IncomeVsExpensesActivity.SUBTOTAL_MONTH) {
                        income += data.getDouble(data.getColumnIndex(IncomeVsExpenseReportEntity.Income));
                        expenses += data.getDouble(data.getColumnIndex(IncomeVsExpenseReportEntity.Expenses));
                    }
                }
                updateListViewFooter(mFooterListView, income, expenses);
                if (data.getCount() > 0) {
                    getListView().removeFooterView(mFooterListView);
                    getListView().addFooterView(mFooterListView);
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

    // Menu

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_report_income_vs_expenses, menu);
        // fix menu char
        MenuItem itemChart = menu.findItem(R.id.menu_chart);
        if (itemChart != null) {
            Activity activity = getActivity();
            if (activity instanceof IncomeVsExpensesActivity) {
                itemChart.setVisible(!((IncomeVsExpensesActivity) activity).mIsDualPanel);
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

    //

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

    // Other

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

    // Private

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
        int[] ids = new int[]{
            R.id.textViewYear, R.id.textViewMonth, R.id.textViewIncome,
            R.id.textViewExpenses, R.id.textViewDifference
        };
        for (int id : ids) {
            TextView textView = (TextView) row.findViewById(id);
            textView.setTypeface(null, Typeface.BOLD);
            textView.setSingleLine(true);
        }
        getListView().addHeaderView(row);

        return row;
    }

    private void initializeListView() {
        setEmptyText(getString(R.string.no_data));

        // add header and footer
        try {
            setListAdapter(null);
            addListViewHeader();
            mFooterListView = addListViewFooter();
        } catch (Exception e) {
            ExceptionHandler handler = new ExceptionHandler(getActivity(), this);
            handler.handle(e, "adding header and footer in income vs expense report");
        }

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object positionObj = parent.getItemAtPosition(position);
                Cursor cursor = (Cursor) positionObj;
                if (cursor == null) return; // i.e. footer row.
                IncomeVsExpenseReportEntity entity = IncomeVsExpenseReportEntity.from(cursor);
                SearchParameters params = new SearchParameters();

                // show the details for the selected month/year.
                CalendarUtils calendar = new CalendarUtils();
                calendar.setYear(entity.getYear());
                int month = entity.getMonth();
                if (month != IncomeVsExpensesActivity.SUBTOTAL_MONTH) {
                    calendar.setMonth(entity.getMonth() - 1);
                } else {
                    // full year
                    calendar.setMonth(Calendar.JANUARY);
                }
                calendar.setFirstDayOfMonth();
//                params.dateFrom = calendar.getTime();
                params.dateFrom = DateTimeUtils.from(calendar.getCalendar());
//                params.dateFrom = new DateTime(entity.getYear(), entity.getMonth(), )

                if (month == IncomeVsExpensesActivity.SUBTOTAL_MONTH) {
                    calendar.setMonth(Calendar.DECEMBER);
                }
                calendar.setLastDayOfMonth();
                params.dateTo = calendar.getTime();

                IntentUtils intentUtils = new IntentUtils(getActivity());
                Intent intent = intentUtils.getIntentForSearch(params);
                startActivity(intent);
            }
        });
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

        CurrencyService currencyService = new CurrencyService(getActivity().getApplicationContext());

        //set income
        txtIncome.setText(currencyService.getCurrencyFormatted(currencyService.getBaseCurrencyId(),
                MoneyFactory.fromDouble(income)));
        txtIncome.setTypeface(null, Typeface.BOLD);
        //set expenses
        txtExpenses.setText(currencyService.getCurrencyFormatted(currencyService.getBaseCurrencyId(),
                MoneyFactory.fromDouble(Math.abs(expenses))));
        txtExpenses.setTypeface(null, Typeface.BOLD);
        //set difference
        txtDifference.setText(currencyService.getCurrencyFormatted(currencyService.getBaseCurrencyId(),
                MoneyFactory.fromDouble(income - Math.abs(expenses))));
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
        try {
            showChartInternal();
        } catch (IllegalStateException ise) {
            ExceptionHandler handler = new ExceptionHandler(getActivity().getApplicationContext(), this);
            handler.handle(ise, "showing chart");
        }
    }

    private void showChartInternal() {
        // take a adapter and cursor
        IncomeVsExpensesAdapter adapter = ((IncomeVsExpensesAdapter) getListAdapter());
        if (adapter == null) return;
        Cursor cursor = adapter.getCursor();
        if (cursor == null) return;
        // Move to the first record.
        if (cursor.getCount() <= 0) return;

        // arrays
        ArrayList<Double> incomes = new ArrayList<>();
        ArrayList<Double> expenses = new ArrayList<>();
        ArrayList<String> titles = new ArrayList<>();

        // Reset cursor to initial position.
        cursor.moveToPosition(-1);
        // cycle cursor
        while (cursor.moveToNext()) {
            int month = cursor.getInt(cursor.getColumnIndex(IncomeVsExpenseReportEntity.Month));
            // check if not subtotal
            if (month != IncomeVsExpensesActivity.SUBTOTAL_MONTH) {
                // incomes and expenses
                incomes.add(cursor.getDouble(cursor.getColumnIndex(IncomeVsExpenseReportEntity.Income)));
                expenses.add(Math.abs(cursor.getDouble(cursor.getColumnIndex(IncomeVsExpenseReportEntity.Expenses))));
                // titles
                int year = cursor.getInt(cursor.getColumnIndex(IncomeVsExpenseReportEntity.YEAR));

                // format month
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month - 1, 1);
                // titles
                titles.add(Integer.toString(year) + "-" + new SimpleDateFormat("MMM").format(calendar.getTime()));
            }
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
