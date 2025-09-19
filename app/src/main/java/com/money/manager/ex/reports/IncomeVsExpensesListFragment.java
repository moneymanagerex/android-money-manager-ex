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
import com.money.manager.ex.database.QueryMobileData;
import com.money.manager.ex.database.QueryReportIncomeVsExpenses;
import com.money.manager.ex.database.SQLDataSet;
import com.money.manager.ex.datalayer.Select;
import com.money.manager.ex.search.SearchParameters;
import com.money.manager.ex.utils.MmxDate;
import com.money.manager.ex.viewmodels.IncomeVsExpenseReportEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
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
    private static final int ID_LOADER_YEARS = 2;
    private static final String SORT_ASCENDING = "ASC";
    private static final String SORT_DESCENDING = "DESC";
    private static final String KEY_BUNDLE_YEAR = "IncomeVsExpensesListFragment:Years";

    private View mFooterListView;
    private final SparseBooleanArray mYearsSelected = new SparseBooleanArray();
    private String mSort = SORT_ASCENDING;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // setHasOptionsMenu(true);
        setupMenuProviders();

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
    private void setupMenuProviders() {
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

        // Chiamata al metodo che le classi derivate possono sovrascrivere
        addCustomMenuProviders(menuHost);
    }

    // Metodo hook che le classi derivate possono sovrascrivere
    protected void addCustomMenuProviders(MenuHost menuHost) {
        // Implementazione di default vuota
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection = null;
        Select query;

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
                query = new Select(report.getAllColumns())
                    .where(selection)
                    .orderBy(IncomeVsExpenseReportEntity.YEAR + " " + mSort + ", " + IncomeVsExpenseReportEntity.Month + " " + mSort);

                return new MmxCursorLoader(getActivity(), report.getUri(), query);

            case ID_LOADER_YEARS:
                QueryMobileData mobileData = new QueryMobileData(getContext());
                selection = "SELECT DISTINCT Year as Year FROM " + mobileData.getSource() + " ORDER BY Year DESC";
                query = new Select().where(selection);
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
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            showChart();
                        }
                    }, 1000);
                }
                break;

            case ID_LOADER_YEARS:
                if (data != null && data.moveToFirst()) {

                    while (!data.isAfterLast()) {
                        int year = data.getInt(data.getColumnIndexOrThrow(IncomeVsExpenseReportEntity.YEAR));
                        if (!mYearsSelected.get(year, false)) {
                            mYearsSelected.put(year, false);
                        }
                        data.moveToNext();
                    }
                    startLoader();
                }
        }
    }

    // Menu

    public void old_onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_report_income_vs_expenses, menu);
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
        } else if (item.getItemId() == R.id.menu_period) {
            showDialogYears();
        }

        return false;
    }

    //

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ArrayList<Integer> years = new ArrayList();
        for (int i = 0; i < mYearsSelected.size(); i++) {
            if (mYearsSelected.get(mYearsSelected.keyAt(i))) {
                years.add(mYearsSelected.keyAt(i));
            }
        }

        // ArrayUtils.toPrimitive(years.toArray(new Integer[0]))
        int[] yearsArray = Ints.toArray(years);
        outState.putIntArray(KEY_BUNDLE_YEAR, yearsArray);
    }

    // Other

    public void showDialogYears() {
        // Assuming mYearsSelected is a SparseBooleanArray
        ArrayList<String> years = new ArrayList<>();
        List<Integer> selected = new ArrayList<>();

        for (int i = 0; i < mYearsSelected.size(); i++) {
            years.add(String.valueOf(mYearsSelected.keyAt(i)));

            if (mYearsSelected.valueAt(i)) {
                // SparseBooleanArray will be always in ASC order, so reversing the selection index
                selected.add(mYearsSelected.size()-(i+1));
            }
        }

        // SparseBooleanArray will be always in ASC order, so using sort option
        Collections.sort(years, Collections.reverseOrder());

        // Convert years to CharSequence array
        final CharSequence[] items = years.toArray(new CharSequence[years.size()]);
        // Convert selected to boolean array
        final boolean[] checkedItems = new boolean[items.length];
        for (int i = 0; i < items.length; i++) {
            checkedItems[i] = selected.contains(i);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMultiChoiceItems(items, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        checkedItems[which] = isChecked;
                    }
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Reset all years to false
                        for (int i = 0; i < mYearsSelected.size(); i++) {
                            mYearsSelected.put(mYearsSelected.keyAt(i), false);
                        }
                        // Set selected years
                        for (int i = 0; i < checkedItems.length; i++) {
                            mYearsSelected.put(mYearsSelected.keyAt(checkedItems.length-(i+1)), checkedItems[i]);
                        }
                        startLoader();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
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
     * Start loader with arrays year
     *
     */
    private void startLoader() {
        Bundle bundle = new Bundle();
        String years = "";
        for (int i = 0; i < mYearsSelected.size(); i++) {
            if (mYearsSelected.get(mYearsSelected.keyAt(i))) {
                years += (!TextUtils.isEmpty(years) ? ", " : "") + mYearsSelected.keyAt(i);
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
