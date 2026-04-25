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

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
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
import androidx.fragment.app.ListFragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import com.money.manager.ex.R;
import com.money.manager.ex.common.MmxCursorLoader;
import com.money.manager.ex.core.IntentFactory;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.database.QueryAllData;
import com.money.manager.ex.database.QueryMobileData;
import com.money.manager.ex.database.QueryReportIncomeVsExpenses;
import com.money.manager.ex.database.SQLDataSet;
import com.money.manager.ex.datalayer.Select;
import com.money.manager.ex.search.SearchParameters;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.settings.LookAndFeelSettings;
import com.money.manager.ex.utils.MmxDate;
import com.money.manager.ex.viewmodels.IncomeVsExpenseReportEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import info.javaperformance.money.MoneyFactory;
import timber.log.Timber;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.lifecycle.Lifecycle;

/**
 * Income/Expense Report, list.
 */
public class IncomeVsExpensesListFragment
    extends ListFragment
    implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int ID_LOADER_REPORT = 1;
    private static final String SORT_ASCENDING = "ASC";
    private static final String SORT_DESCENDING = "DESC";
    private static final String KEY_BUNDLE_DATE_FROM = "IncomeVsExpensesListFragment:DateFrom";
    private static final String KEY_BUNDLE_DATE_TO = "IncomeVsExpensesListFragment:DateTo";
    private static final String KEY_BUNDLE_ITEM_SELECTED = "IncomeVsExpensesListFragment:ItemSelected";
    private static final String PREF_PERIOD_SELECTED = "IncomeVsExpensesPeriodSelected";
    private static final String PREF_PERIOD_DATE_FROM = "IncomeVsExpensesPeriodFromDate";
    private static final String PREF_PERIOD_DATE_TO = "IncomeVsExpensesPeriodToDate";
    private static final String PREF_FILTER_MODE = "IncomeVsExpensesFilterMode";
    private static final String PREF_FILTER_CUSTOM = "IncomeVsExpensesFilterCustom";

    private View mFooterListView;
    private Date mDateFrom;
    private Date mDateTo;
    private int mItemSelected = R.id.menu_current_year;
    private String mSort = SORT_ASCENDING;
    private boolean mMenuProviderRegistered = false;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupMenuProviders();

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(KEY_BUNDLE_ITEM_SELECTED)) {
                mItemSelected = savedInstanceState.getInt(KEY_BUNDLE_ITEM_SELECTED);
            }
            if (savedInstanceState.containsKey(KEY_BUNDLE_DATE_FROM)) {
                String dateFromString = savedInstanceState.getString(KEY_BUNDLE_DATE_FROM);
                if (!TextUtils.isEmpty(dateFromString)) {
                    mDateFrom = new MmxDate(dateFromString).toDate();
                }
            }
            if (savedInstanceState.containsKey(KEY_BUNDLE_DATE_TO)) {
                String dateToString = savedInstanceState.getString(KEY_BUNDLE_DATE_TO);
                if (!TextUtils.isEmpty(dateToString)) {
                    mDateTo = new MmxDate(dateToString).toDate();
                }
            }
        } else {
            restorePeriodSelectionFromPreferences();
        }

        updateDateRangeForSelectedPeriod();

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
        startLoader();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mMenuProviderRegistered = false;
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

    // Loader
    private void setupMenuProviders() {
        if (mMenuProviderRegistered) return;

        MenuHost menuHost = requireActivity();

        // MenuProvider comune
        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                old_onCreateOptionsMenu(menu, menuInflater);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                return old_onOptionsItemSelected(menuItem);
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

        mMenuProviderRegistered = true;

        // Chiamata al metodo che le classi derivate possono sovrascrivere
        addCustomMenuProviders(menuHost);
    }

    // Metodo hook che le classi derivate possono sovrascrivere
    protected void addCustomMenuProviders(MenuHost menuHost) {
        // Implementazione di default vuota
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case ID_LOADER_REPORT:
                QueryReportIncomeVsExpenses report = new QueryReportIncomeVsExpenses(getActivity(), buildReportFilterClause());
                String reportSql = buildIncomeVsExpensesQuery(report);
                Select query = new Select().where(reportSql);

                return new MmxCursorLoader(getActivity(), new SQLDataSet().getUri(), query);
        }
        return null;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
//        ((IncomeVsExpensesAdapter) getListAdapter()).swapCursor(null);
        ((IncomeVsExpensesAdapter) getListAdapter()).changeCursor(null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case ID_LOADER_REPORT:
//                ((IncomeVsExpensesAdapter) getListAdapter()).swapCursor(data);
                ((IncomeVsExpensesAdapter) getListAdapter()).changeCursor(data);

                if (isResumed()) {
                    setListShown(true);
                } else {
                    setListShownNoAnimation(true);
                }
                // calculate income, expenses
                double income = 0, expenses = 0;
                if (data == null) return;

                // move to first record #1539
                data.moveToPosition(-1);

                while (data.moveToNext()) {
                    if (data.getInt(data.getColumnIndexOrThrow(IncomeVsExpenseReportEntity.Month)) != IncomeVsExpensesActivity.SUBTOTAL_MONTH) {
                        income += data.getDouble(data.getColumnIndexOrThrow(IncomeVsExpenseReportEntity.Income));
                        expenses += data.getDouble(data.getColumnIndexOrThrow(IncomeVsExpenseReportEntity.Expenses));
                    }
                }
                updateListViewFooter(mFooterListView, income, expenses);
                if (data.getCount() > 0) {
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
                break;
        }
    }

    // Menu

    public void old_onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_report_income_vs_expenses, menu);
        inflater.inflate(R.menu.menu_period_picker, menu);
        if (menu.findItem(R.id.menu_account_filter) == null) {
            inflater.inflate(R.menu.menu_report_account_filter, menu);
        }
        MenuItem periodItem = menu.findItem(R.id.menu_period);
        if (periodItem != null) {
            periodItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
        MenuItem selectedPeriod = menu.findItem(mItemSelected);
        if (selectedPeriod != null) {
            selectedPeriod.setChecked(true);
        }
        MenuItem selectedFilter = menu.findItem(getAccountFilterMode());
        if (selectedFilter != null) {
            selectedFilter.setChecked(true);
        }
        // fix menu char
        MenuItem itemChart = menu.findItem(R.id.menu_chart);
        if (itemChart != null) {
            FragmentActivity activity = getActivity();
            if (activity instanceof IncomeVsExpensesActivity) {
                itemChart.setVisible(!((IncomeVsExpensesActivity) activity).mIsDualPanel);
            }
        }
    }

    public boolean old_onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getActivity().finish();
        } else if (item.getItemId() == R.id.menu_sort_asceding || item.getItemId() == R.id.menu_sort_desceding) {
            mSort = item.getItemId() == R.id.menu_sort_asceding ? SORT_ASCENDING : SORT_DESCENDING;
            startLoader();
            item.setChecked(true);
        } else if (item.getItemId() == R.id.menu_chart) {
            showChart();
        } else if (handlePeriodSelection(item)) {
            return true;
        } else if (isAccountFilterMenuItem(item.getItemId())) {
            item.setChecked(true);
            saveAccountFilterMode(item.getItemId());
            if (item.getItemId() == R.id.menu_account_filter_custom) {
                showAccountSelectionDialog();
            } else {
                startLoader();
            }
            return true;
        }

        return false;
    }

    private boolean handlePeriodSelection(MenuItem menuItem) {
        int itemId = menuItem.getItemId();

        if (itemId == R.id.menu_all_time) {
            mDateFrom = null;
            mDateTo = null;
        } else if (itemId == R.id.menu_custom_dates) {
            menuItem.setChecked(true);
            mItemSelected = itemId;
            showDialogCustomDates();
            return true;
        } else {
            com.money.manager.ex.core.DateRange dateRange = ReportDateRangeSupport.resolveDateRange(requireContext(), itemId);
            if (dateRange == null) {
                return false;
            }
            mDateFrom = dateRange.dateFrom;
            mDateTo = dateRange.dateTo;
        }

        menuItem.setChecked(true);
        mItemSelected = itemId;
        savePeriodSelection();
        startLoader();
        return true;
    }

    private boolean isAccountFilterMenuItem(int itemId) {
        return AccountFilterSupport.isAccountFilterMenuItem(itemId);
    }

    private int getAccountFilterMode() {
        LookAndFeelSettings settings = new AppSettings(requireContext()).getLookAndFeelSettings();
        return AccountFilterSupport.getFilterMode(settings, PREF_FILTER_MODE, R.id.menu_account_filter_open);
    }

    private void saveAccountFilterMode(int mode) {
        LookAndFeelSettings settings = new AppSettings(requireContext()).getLookAndFeelSettings();
        AccountFilterSupport.saveFilterMode(settings, PREF_FILTER_MODE, mode);
    }

    private String getAccountFilterSelection() {
        int mode = getAccountFilterMode();
        LookAndFeelSettings settings = new AppSettings(requireContext()).getLookAndFeelSettings();
        return AccountFilterSupport.getSelectionForAccountIdColumn(
                mode, settings, PREF_FILTER_CUSTOM, "TX.ACCOUNTID");
    }

    private void showAccountSelectionDialog() {
        LookAndFeelSettings settings = new AppSettings(requireContext()).getLookAndFeelSettings();
        AccountFilterSupport.showAndPersistAccountSelectionDialog(
                requireContext(), settings, PREF_FILTER_CUSTOM, this::startLoader);
    }

    private String buildReportFilterClause() {
        String accountClause = getAccountFilterSelection();
        String dateClause = ReportDateRangeSupport.buildWhereClause(mDateFrom, mDateTo, "date(TX.TransDate)");

        if (TextUtils.isEmpty(accountClause)) {
            return dateClause;
        }
        if (TextUtils.isEmpty(dateClause)) {
            return accountClause;
        }

        return accountClause + " AND " + dateClause;
    }

    private String buildIncomeVsExpensesQuery(QueryReportIncomeVsExpenses report) {
        StringBuilder sql = new StringBuilder("SELECT ");
        String[] columns = report.getAllColumns();
        for (int i = 0; i < columns.length; i++) {
            if (i > 0) {
                sql.append(", ");
            }
            sql.append(columns[i]);
        }
        sql.append(" FROM (").append(report.getSource()).append(") T");
        sql.append(" ORDER BY ")
                .append(IncomeVsExpenseReportEntity.YEAR).append(" ").append(mSort)
                .append(", ")
                .append(IncomeVsExpenseReportEntity.Month).append(" ").append(mSort);

        return sql.toString();
    }

    //

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mDateFrom != null) {
            outState.putString(KEY_BUNDLE_DATE_FROM, new MmxDate(mDateFrom).toIsoDateString());
        }
        if (mDateTo != null) {
            outState.putString(KEY_BUNDLE_DATE_TO, new MmxDate(mDateTo).toIsoDateString());
        }
        outState.putInt(KEY_BUNDLE_ITEM_SELECTED, mItemSelected);
    }

    private void showDialogCustomDates() {
        ReportDateRangeSupport.showCustomDateDialog(requireContext(), mDateFrom, mDateTo, (fromDate, toDate) -> {
            mDateFrom = fromDate;
            mDateTo = toDate;
            savePeriodSelection();
            startLoader();
        });
    }

    private void restorePeriodSelectionFromPreferences() {
        LookAndFeelSettings settings = new AppSettings(requireContext()).getLookAndFeelSettings();

        String selectedValue = settings.get(PREF_PERIOD_SELECTED, Integer.toString(R.id.menu_current_year));
        try {
            mItemSelected = Integer.parseInt(selectedValue);
        } catch (Exception ignored) {
            mItemSelected = R.id.menu_current_year;
        }

        String fromDateString = settings.get(PREF_PERIOD_DATE_FROM, "");
        if (!TextUtils.isEmpty(fromDateString)) {
            try {
                mDateFrom = new MmxDate(fromDateString).toDate();
            } catch (Exception ignored) {
                mDateFrom = null;
            }
        }

        String toDateString = settings.get(PREF_PERIOD_DATE_TO, "");
        if (!TextUtils.isEmpty(toDateString)) {
            try {
                mDateTo = new MmxDate(toDateString).toDate();
            } catch (Exception ignored) {
                mDateTo = null;
            }
        }
    }

    private void updateDateRangeForSelectedPeriod() {
        if (mItemSelected == R.id.menu_all_time) {
            mDateFrom = null;
            mDateTo = null;
            return;
        }

        if (mItemSelected == R.id.menu_custom_dates) {
            return;
        }

        com.money.manager.ex.core.DateRange dateRange = ReportDateRangeSupport.resolveDateRange(requireContext(), mItemSelected);
        if (dateRange != null) {
            mDateFrom = dateRange.dateFrom;
            mDateTo = dateRange.dateTo;
            return;
        }

        mItemSelected = R.id.menu_current_year;
        com.money.manager.ex.core.DateRange defaultRange = ReportDateRangeSupport.resolveDateRange(requireContext(), mItemSelected);
        if (defaultRange != null) {
            mDateFrom = defaultRange.dateFrom;
            mDateTo = defaultRange.dateTo;
        }
    }

    private void savePeriodSelection() {
        LookAndFeelSettings settings = new AppSettings(requireContext()).getLookAndFeelSettings();
        settings.set(PREF_PERIOD_SELECTED, Integer.toString(mItemSelected));
        settings.set(PREF_PERIOD_DATE_FROM, mDateFrom == null ? "" : new MmxDate(mDateFrom).toIsoDateString());
        settings.set(PREF_PERIOD_DATE_TO, mDateTo == null ? "" : new MmxDate(mDateTo).toIsoDateString());
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

                int mode = getAccountFilterMode();
                LookAndFeelSettings settings = new AppSettings(requireContext()).getLookAndFeelSettings();
                params.accountFilterWhere = AccountFilterSupport.getSelectionForAccountIdColumn(
                        mode, settings, PREF_FILTER_CUSTOM, QueryAllData.ACCOUNTID);

                Intent intent = IntentFactory.getSearchIntent(getActivity(), params);
                startActivity(intent);
            }
        });
    }

    /**
     * Start loader with the current report date range.
     *
     */
    private void startLoader() {
        getLoaderManager().restartLoader(ID_LOADER_REPORT, null, this);
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
}
