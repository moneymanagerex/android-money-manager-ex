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

import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.loader.content.Loader;

import com.google.common.primitives.Doubles;
import com.money.manager.ex.R;
import com.money.manager.ex.core.IntentFactory;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.database.QueryReportIncomeVsExpenses;
import com.money.manager.ex.search.SearchParameters;
import com.money.manager.ex.utils.MmxDate;
import com.money.manager.ex.viewmodels.IncomeVsExpenseReportEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import info.javaperformance.money.MoneyFactory;
import timber.log.Timber;
import androidx.annotation.NonNull;
import com.money.manager.ex.datalayer.Select;

/**
 * Income/Expense Report, list.
 */
public class IncomeVsExpensesListFragment
    extends BaseReportFragment {

    private static final String SORT_ASCENDING = "ASC";
    private static final String SORT_DESCENDING = "DESC";

    private View mFooterListView;
    private String mSort = SORT_ASCENDING;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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
    }

    // To remove Obsolete code we need to:
    // a) move all view related instruction into onViewCreated
    // b) move all fragment related instruction into onCreate
    // c) move all activity related instruction into onStart
//    @Override
//    public void onActivityCreated(Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//
 //    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == ID_LOADER) {
            if (data == null) return;

            Cursor displayCursor = buildDisplayCursor(data);
            ((IncomeVsExpensesAdapter) getListAdapter()).changeCursor(displayCursor);

            if (isResumed()) {
                setListShown(true);
            } else {
                setListShownNoAnimation(true);
            }

            double income = 0;
            double expenses = 0;

            // move to first record #1539
            displayCursor.moveToPosition(-1);

            while (displayCursor.moveToNext()) {
                if (displayCursor.getInt(displayCursor.getColumnIndexOrThrow(IncomeVsExpenseReportEntity.Month)) != IncomeVsExpensesActivity.SUBTOTAL_MONTH) {
                    income += displayCursor.getDouble(displayCursor.getColumnIndexOrThrow(IncomeVsExpenseReportEntity.Income));
                    expenses += displayCursor.getDouble(displayCursor.getColumnIndexOrThrow(IncomeVsExpenseReportEntity.Expenses));
                }
            }

            updateListViewFooter(mFooterListView, income, expenses);
            if (displayCursor.getCount() > 0) {
                getListView().removeFooterView(mFooterListView);
                getListView().addFooterView(mFooterListView);
            }

            if (((IncomeVsExpensesActivity) getActivity()).mIsDualPanel) {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showChart();
                    }
                }, 1000);
            }
        }
    }

    private Cursor buildDisplayCursor(Cursor sourceCursor) {
        SourceMonthData sourceData = extractSourceMonthData(sourceCursor);
        MonthRange range = resolveDisplayRange(sourceData);
        if (range == null) {
            return sourceCursor;
        }

        ArrayList<RowValues> rows = buildMonthRows(sourceData.valuesByMonth, range);
        appendYearSubtotals(rows);
        sortRows(rows);
        return toMatrixCursor(rows);
    }

    private SourceMonthData extractSourceMonthData(Cursor sourceCursor) {
        SourceMonthData sourceData = new SourceMonthData();

        sourceCursor.moveToPosition(-1);
        while (sourceCursor.moveToNext()) {
            int year = sourceCursor.getInt(sourceCursor.getColumnIndexOrThrow(IncomeVsExpenseReportEntity.YEAR));
            int month = sourceCursor.getInt(sourceCursor.getColumnIndexOrThrow(IncomeVsExpenseReportEntity.Month));
            if (month == IncomeVsExpensesActivity.SUBTOTAL_MONTH) {
                continue;
            }

            MonthValues monthValues = new MonthValues();
            monthValues.income = sourceCursor.getDouble(sourceCursor.getColumnIndexOrThrow(IncomeVsExpenseReportEntity.Income));
            monthValues.expenses = sourceCursor.getDouble(sourceCursor.getColumnIndexOrThrow(IncomeVsExpenseReportEntity.Expenses));
            monthValues.transfers = sourceCursor.getDouble(sourceCursor.getColumnIndexOrThrow(IncomeVsExpenseReportEntity.Transfers));
            sourceData.valuesByMonth.put(getMonthKey(year, month), monthValues);

            sourceData.updateBounds(year, month);
        }

        return sourceData;
    }

    private MonthRange resolveDisplayRange(SourceMonthData sourceData) {
        if (mDateFrom != null && mDateTo != null) {
            Calendar startMonth = calendarMonthFromDate(mDateFrom);
            Calendar endMonth = calendarMonthFromDate(mDateTo);
            if (startMonth.after(endMonth)) {
                Calendar temp = (Calendar) startMonth.clone();
                startMonth = endMonth;
                endMonth = temp;
            }
            return new MonthRange(startMonth, endMonth);
        }

        if (!sourceData.hasBounds()) {
            return null;
        }

        Calendar startMonth = Calendar.getInstance();
        startMonth.clear();
        startMonth.set(sourceData.minYear, sourceData.minMonth - 1, 1);

        Calendar endMonth = Calendar.getInstance();
        endMonth.clear();
        endMonth.set(sourceData.maxYear, sourceData.maxMonth - 1, 1);

        return new MonthRange(startMonth, endMonth);
    }

    private Calendar calendarMonthFromDate(java.util.Date date) {
        Calendar month = Calendar.getInstance();
        month.setTime(date);
        month.set(Calendar.DAY_OF_MONTH, 1);
        return month;
    }

    private ArrayList<RowValues> buildMonthRows(Map<String, MonthValues> valuesByMonth, MonthRange range) {
        ArrayList<RowValues> rows = new ArrayList<>();
        Calendar monthIterator = (Calendar) range.startMonth.clone();

        while (!monthIterator.after(range.endMonth)) {
            int year = monthIterator.get(Calendar.YEAR);
            int month = monthIterator.get(Calendar.MONTH) + 1;
            MonthValues monthValues = valuesByMonth.get(getMonthKey(year, month));
            if (monthValues == null) {
                monthValues = new MonthValues();
            }

            rows.add(new RowValues(year, month, monthValues.income, monthValues.expenses, monthValues.transfers));
            monthIterator.add(Calendar.MONTH, 1);
        }

        return rows;
    }

    private void appendYearSubtotals(ArrayList<RowValues> rows) {
        Map<Integer, MonthValues> subtotalByYear = new HashMap<>();
        for (RowValues row : rows) {
            MonthValues subtotal = subtotalByYear.get(row.year);
            if (subtotal == null) {
                subtotal = new MonthValues();
                subtotalByYear.put(row.year, subtotal);
            }

            subtotal.income += row.income;
            subtotal.expenses += row.expenses;
            subtotal.transfers += row.transfers;
        }

        for (Map.Entry<Integer, MonthValues> entry : subtotalByYear.entrySet()) {
            int year = entry.getKey();
            MonthValues subtotal = entry.getValue();
            rows.add(new RowValues(year, (int) IncomeVsExpensesActivity.SUBTOTAL_MONTH,
                    subtotal.income, subtotal.expenses, subtotal.transfers));
        }
    }

    private void sortRows(ArrayList<RowValues> rows) {
        rows.sort((left, right) -> {
            int yearCompare = Integer.compare(left.year, right.year);
            if (yearCompare == 0) {
                return Integer.compare(left.month, right.month);
            }
            return yearCompare;
        });

        if (SORT_DESCENDING.equals(mSort)) {
            rows.sort((left, right) -> {
                int yearCompare = Integer.compare(right.year, left.year);
                if (yearCompare == 0) {
                    return Integer.compare(right.month, left.month);
                }
                return yearCompare;
            });
        }
    }

    private MatrixCursor toMatrixCursor(ArrayList<RowValues> rows) {
        MatrixCursor displayCursor = new MatrixCursor(new String[]{
                "_id",
                IncomeVsExpenseReportEntity.YEAR,
                IncomeVsExpenseReportEntity.Month,
                IncomeVsExpenseReportEntity.Income,
                IncomeVsExpenseReportEntity.Expenses,
                IncomeVsExpenseReportEntity.Transfers
        });

        long rowId = 0;
        for (RowValues row : rows) {
            displayCursor.addRow(new Object[]{
                    rowId++,
                    row.year,
                    row.month,
                    row.income,
                    row.expenses,
                    row.transfers
            });
        }

        return displayCursor;
    }

    private String getMonthKey(int year, int month) {
        return year + "-" + month;
    }

    private static class MonthValues {
        double income;
        double expenses;
        double transfers;
    }

    private static class SourceMonthData {
        final Map<String, MonthValues> valuesByMonth = new HashMap<>();
        Integer minYear;
        Integer minMonth;
        Integer maxYear;
        Integer maxMonth;

        void updateBounds(int year, int month) {
            if (minYear == null || (year < minYear || (year == minYear && month < minMonth))) {
                minYear = year;
                minMonth = month;
            }

            if (maxYear == null || (year > maxYear || (year == maxYear && month > maxMonth))) {
                maxYear = year;
                maxMonth = month;
            }
        }

        boolean hasBounds() {
            return minYear != null && minMonth != null && maxYear != null && maxMonth != null;
        }
    }

    private static class MonthRange {
        final Calendar startMonth;
        final Calendar endMonth;

        MonthRange(Calendar startMonth, Calendar endMonth) {
            this.startMonth = startMonth;
            this.endMonth = endMonth;
        }
    }

    private static class RowValues {
        final int year;
        final int month;
        final double income;
        final double expenses;
        final double transfers;

        RowValues(int year, int month, double income, double expenses, double transfers) {
            this.year = year;
            this.month = month;
            this.income = income;
            this.expenses = expenses;
            this.transfers = transfers;
        }
    }

    // Menu

    public void old_onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (menu.findItem(R.id.menu_chart) == null) {
            inflater.inflate(R.menu.menu_report, menu);
        }

        if (menu.findItem(R.id.menu_sort) == null) {
            inflater.inflate(R.menu.menu_report_income_vs_expenses_sort, menu);
        }

        MenuItem itemChart = menu.findItem(R.id.menu_chart);
        if (itemChart != null) {
            FragmentActivity activity = getActivity();
            if (activity instanceof IncomeVsExpensesActivity) {
                itemChart.setVisible(!((IncomeVsExpensesActivity) activity).mIsDualPanel);
            }

            UIHelper uiHelper = new UIHelper(getActivity());
            itemChart.setIcon(uiHelper.resolveAttribute(R.attr.ic_action_bargraph));
        }

        MenuItem selectedSortItem = menu.findItem(SORT_ASCENDING.equals(mSort)
                ? R.id.menu_sort_asceding
                : R.id.menu_sort_desceding);
        if (selectedSortItem != null) {
            selectedSortItem.setChecked(true);
        }
    }

    public boolean old_onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_sort_asceding || item.getItemId() == R.id.menu_sort_desceding) {
            mSort = item.getItemId() == R.id.menu_sort_asceding ? SORT_ASCENDING : SORT_DESCENDING;
            startLoader(null);
            item.setChecked(true);
            return true;
        } else if (item.getItemId() == R.id.menu_chart) {
            showChart();
            return true;
        }

        return false;
    }

    // Private

    /**
     * Add footer to ListView
     *
     * @return View of footer
     */
    private View addListViewFooter() {
        TableRow row = (TableRow) View.inflate(getActivity(), R.layout.tablerow_income_vs_expenses, null);
        TextView txtYear = row.findViewById(R.id.textViewYear);
        txtYear.setText(getString(R.string.total));
        txtYear.setTypeface(null, Typeface.BOLD);
        TextView txtMonth = row.findViewById(R.id.textViewMonth);
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
            TextView textView = row.findViewById(id);
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
            Timber.e(e, "adding header and footer in income vs expense report");
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
                MmxDate dateTime = new MmxDate();
                dateTime.setYear(entity.getYear());
                long month = entity.getMonth();
                if (month != IncomeVsExpensesActivity.SUBTOTAL_MONTH) {
                    dateTime.setMonth(entity.getMonth() - 1);
                } else {
                    // full year
                    dateTime.setMonth(Calendar.JANUARY);
                }
                dateTime.firstDayOfMonth();
                params.dateFrom = dateTime.toDate();

                if (month == IncomeVsExpensesActivity.SUBTOTAL_MONTH) {
                    dateTime.setMonth(Calendar.DECEMBER);
                }
                dateTime.lastDayOfMonth();
                params.dateTo = dateTime.toDate();

                Intent intent = IntentFactory.getSearchIntent(getActivity(), params);
                startActivity(intent);
            }
        });
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
        TextView txtIncome = footer.findViewById(R.id.textViewIncome);
        TextView txtExpenses = footer.findViewById(R.id.textViewExpenses);
        TextView txtDifference = footer.findViewById(R.id.textViewDifference);

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
        UIHelper uiHelper = new UIHelper(getActivity());
        if (income - Math.abs(expenses) < 0) {
            txtDifference.setTextColor(ContextCompat.getColor(getActivity(),
                uiHelper.resolveAttribute(R.attr.holo_red_color_theme)));
        } else {
            txtDifference.setTextColor(ContextCompat.getColor(getActivity(),
                uiHelper.resolveAttribute(R.attr.holo_green_color_theme)));
        }
    }

    private void showChart() {
        try {
            showChartInternal();
        } catch (IllegalStateException ise) {
            Timber.e(ise, "showing chart");
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
            int month = cursor.getInt(cursor.getColumnIndexOrThrow(IncomeVsExpenseReportEntity.Month));
            // check if not subtotal
            if (month != IncomeVsExpensesActivity.SUBTOTAL_MONTH) {
                // incomes and expenses
                incomes.add(cursor.getDouble(cursor.getColumnIndexOrThrow(IncomeVsExpenseReportEntity.Income)));
                expenses.add(Math.abs(cursor.getDouble(cursor.getColumnIndexOrThrow(IncomeVsExpenseReportEntity.Expenses))));
                // titles
                int year = cursor.getInt(cursor.getColumnIndexOrThrow(IncomeVsExpenseReportEntity.YEAR));

                // format month
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month - 1, 1);
                // titles
                titles.add(year + "-" + new SimpleDateFormat("MMM")
                        .format(calendar.getTime()));
            }
        }
        //compose bundle for arguments
        Bundle args = new Bundle();
        //double[] expensesArray = ArrayUtils.toPrimitive(expenses.toArray(new Double[0]));
        double[] expensesArray = Doubles.toArray(expenses);
        args.putDoubleArray(IncomeVsExpensesChartFragment.KEY_EXPENSES_VALUES, expensesArray);

        //double[] incomesArray = ArrayUtils.toPrimitive(incomes.toArray(new Double[0]));
        double[] incomesArray = Doubles.toArray(incomes);
        args.putDoubleArray(IncomeVsExpensesChartFragment.KEY_INCOME_VALUES, incomesArray);

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
                fragmentTransaction.replace(R.id.fragmentChart, fragment,
                        IncomeVsExpensesChartFragment.class.getSimpleName());
            } else {
                fragmentTransaction.replace(R.id.fragmentMain, fragment,
                        IncomeVsExpensesChartFragment.class.getSimpleName());
                fragmentTransaction.addToBackStack(null);
            }
            fragmentTransaction.commit();
        }
    }

    @Override
    protected String prepareQuery(String whereClause) {
        QueryReportIncomeVsExpenses report = new QueryReportIncomeVsExpenses(getActivity(), whereClause);

        return new Select(report.getAllColumns())
                .from(report.getSource())
                .orderBy(IncomeVsExpenseReportEntity.YEAR + " " + mSort + ", " + IncomeVsExpenseReportEntity.Month + " " + mSort)
                .toString();
    }

    @Override
    public String getSubTitle() {
        return null;
    }
}
