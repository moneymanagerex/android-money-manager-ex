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

package com.money.manager.ex.home;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.Build;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.money.manager.ex.R;
import com.money.manager.ex.common.MmxCursorLoader;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.database.QueryBillDeposits;
import com.money.manager.ex.database.QueryReportIncomeVsExpenses;
import com.money.manager.ex.database.SQLDataSet;
import com.money.manager.ex.database.ViewMobileData;
import com.money.manager.ex.datalayer.Select;
import com.money.manager.ex.reports.IncomeVsExpensesChartFragment;
import com.money.manager.ex.utils.MmxDate;
import com.money.manager.ex.view.RobotoTextView;
import com.money.manager.ex.viewmodels.IncomeVsExpenseReportEntity;

import java.util.Calendar;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import info.javaperformance.money.MoneyFactory;

/**
 * This fragment is not used (?).
 */
public class DashboardFragment
    extends Fragment
    implements LoaderManager.LoaderCallbacks<Cursor> {

    // ID LOADER
    private static final int ID_LOADER_SCREEN1 = 0x000;
    private static final int ID_LOADER_SCREEN2 = 0x001;
    private static final int ID_LOADER_SCREEN3 = 0x002;
    private static final int ID_LOADER_SCREEN4 = 0x003;
    // Padding
    final int padding_in_dp = 6; // 6 dps
    double scale;
    int padding_in_px;

    // array of part screen
    LinearLayout[] linearScreens;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //set has option menu to close dashboard item
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container == null) return null;

        // parse layout
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.dashboard_fragment, container, false);
        if (layout == null)
            return null;
        linearScreens = new LinearLayout[ID_LOADER_SCREEN4 + 1];
        // get sub linearlayout
        linearScreens[ID_LOADER_SCREEN1] = (LinearLayout) layout.findViewById(R.id.linearLayoutScreen1);
        linearScreens[ID_LOADER_SCREEN2] = (LinearLayout) layout.findViewById(R.id.linearLayoutScreen2);
        linearScreens[ID_LOADER_SCREEN3] = (LinearLayout) layout.findViewById(R.id.linearLayoutScreen3);
        linearScreens[ID_LOADER_SCREEN4] = (LinearLayout) layout.findViewById(R.id.linearLayoutScreen4);
        // calculate padding
        scale = getResources().getDisplayMetrics().density;
        padding_in_px = (int) (padding_in_dp * scale + 0.5f);

        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        //find menu dashboard
        MenuItem itemDashboard = menu.findItem(R.id.menu_dashboard);
        if (itemDashboard != null)
            itemDashboard.setVisible(false);
    }

    public void loadData() {
        // restart loader
        if (linearScreens[ID_LOADER_SCREEN1].getVisibility() == View.VISIBLE)
            getLoaderManager().restartLoader(ID_LOADER_SCREEN1, null, this);

        if (linearScreens[ID_LOADER_SCREEN2].getVisibility() == View.VISIBLE)
            getLoaderManager().restartLoader(ID_LOADER_SCREEN2, null, this);

        if (linearScreens[ID_LOADER_SCREEN3].getVisibility() == View.VISIBLE)
            getLoaderManager().restartLoader(ID_LOADER_SCREEN3, null, this);

        if (linearScreens[ID_LOADER_SCREEN4].getVisibility() == View.VISIBLE)
            getLoaderManager().restartLoader(ID_LOADER_SCREEN4, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // add progress bar
        ProgressBar progressBar = new ProgressBar(getActivity());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        progressBar.setLayoutParams(layoutParams);
        progressBar.setIndeterminate(true);

        if (id <= ID_LOADER_SCREEN4) {
            linearScreens[id].removeAllViews();
            // add view
            linearScreens[id].addView(progressBar);
        }

        Select query;

        // start loader
        switch (id) {
            case ID_LOADER_SCREEN1:
                QueryReportIncomeVsExpenses report = new QueryReportIncomeVsExpenses(getActivity());
                query = new Select(report.getAllColumns())
                    .where(IncomeVsExpenseReportEntity.Month + "=" + Integer.toString(Calendar.getInstance().get(Calendar.MONTH) + 1) +
                            " AND " +
                            IncomeVsExpenseReportEntity.YEAR + "=" + Integer.toString(Calendar.getInstance().get(Calendar.YEAR)));

                return new MmxCursorLoader(getActivity(), report.getUri(), query);

            case ID_LOADER_SCREEN2:
                query = new Select().where(prepareQueryTopWithdrawals());
                return new MmxCursorLoader(getActivity(), new SQLDataSet().getUri(), query);

            case ID_LOADER_SCREEN3:
                query = new Select().where(prepareQueryTopPayees());
                return new MmxCursorLoader(getActivity(), new SQLDataSet().getUri(), query);

            case ID_LOADER_SCREEN4:
                QueryBillDeposits billDeposits = new QueryBillDeposits(getActivity());
                query = new Select(billDeposits.getAllColumns())
                    .where(QueryBillDeposits.DAYSLEFT + "<=10")
                    .orderBy(QueryBillDeposits.DAYSLEFT);

                return new MmxCursorLoader(getActivity(), billDeposits.getUri(), query);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() <= ID_LOADER_SCREEN4)
            linearScreens[loader.getId()].removeAllViews();

        switch (loader.getId()) {
            case ID_LOADER_SCREEN1:
                showChartIncomeVsExpensesCurrentMonth(data);
                break;
            case ID_LOADER_SCREEN2:
                linearScreens[loader.getId()].addView(showTableLayoutTopWithdrawals(data));
                break;
            case ID_LOADER_SCREEN3:
                linearScreens[loader.getId()].addView(showTableLayoutTopPayees(data));
                break;
            case ID_LOADER_SCREEN4:
                linearScreens[loader.getId()].addView(showTableLayoutUpComingTransactions(data));
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    // End loader

    //@SuppressWarnings("deprecation")
    private String prepareQueryTopWithdrawals() {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        ViewMobileData mobileData = new ViewMobileData(getContext());
        // data to compose builder
        String[] projectionIn = new String[]{"ROWID AS _id", ViewMobileData.CATEGID, ViewMobileData.Category, ViewMobileData.SubcategID,
                ViewMobileData.Subcategory, "SUM(" + ViewMobileData.AmountBaseConvRate + ") AS TOTAL", "COUNT(*) AS NUM"};

        String selection = ViewMobileData.Status + "<>'V' AND " + ViewMobileData.TransactionType + " IN ('Withdrawal')"
                + " AND (julianday(date('now')) - julianday(" + ViewMobileData.Date + ") <= 30)";

        String groupBy = ViewMobileData.CATEGID + ", " + ViewMobileData.Category + ", " + ViewMobileData.SubcategID + ", " + ViewMobileData.Subcategory;
        String having = "SUM(" + ViewMobileData.AmountBaseConvRate + ") < 0";
        String sortOrder = "ABS(SUM(" + ViewMobileData.AmountBaseConvRate + ")) DESC";
        String limit = "10";
        // compose builder
        builder.setTables(mobileData.getSource());
        // return query
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            return builder.buildQuery(projectionIn, selection, groupBy, having, sortOrder, limit);
        } else {
            return builder.buildQuery(projectionIn, selection, null, groupBy, having, sortOrder, limit);
        }
    }

    @SuppressWarnings("deprecation")
    private String prepareQueryTopPayees() {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        ViewMobileData mobileData = new ViewMobileData(getContext());
        // data to compose builder
        String[] projectionIn = new String[]{"ROWID AS _id",
                ViewMobileData.PAYEEID, ViewMobileData.Payee,
                "ABS(SUM(" + ViewMobileData.AmountBaseConvRate + ")) AS TOTAL",
                "COUNT(*) AS NUM"};

        String selection = ViewMobileData.Status + "<>'V' AND " + ViewMobileData.TransactionType
                + " IN ('Withdrawal', 'Deposit') AND (julianday(date('now')) - julianday(" + ViewMobileData.Date + ") <= 30)";

        String groupBy = ViewMobileData.PAYEEID + ", " + ViewMobileData.Payee;
        String having = null;
        String sortOrder = "ABS(SUM(" + ViewMobileData.AmountBaseConvRate + ")) DESC";
        String limit = "10";
        // compose builder
        builder.setTables(mobileData.getSource());
        // return query
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            return builder.buildQuery(projectionIn, selection, groupBy, having, sortOrder, limit);
        } else {
            return builder.buildQuery(projectionIn, selection, null, groupBy, having, sortOrder, limit);
        }
    }

    /*
     * Show Chart of Income Vs. Expenses Cur
     */
    private void showChartIncomeVsExpensesCurrentMonth(Cursor cursor) {
        // move to first
        if (!cursor.moveToFirst())
            return;
        // arrays
        double[] incomes = new double[3];
        double[] expenses = new double[3];
        String[] titles = new String[3];

        // incomes and expenses
        incomes[1] = cursor.getDouble(cursor.getColumnIndex(IncomeVsExpenseReportEntity.Income));
        expenses[1] = Math.abs(cursor.getDouble(cursor.getColumnIndex(IncomeVsExpenseReportEntity.Expenses)));
        // titles
        int year = cursor.getInt(cursor.getColumnIndex(IncomeVsExpenseReportEntity.YEAR));
        int month = cursor.getInt(cursor.getColumnIndex(IncomeVsExpenseReportEntity.Month));

        // format month
//        Calendar calendar = Calendar.getInstance();
//        calendar.set(year, month - 1, 1);
        MmxDate dateTime = new MmxDate(year, month - 1, 1);
        // titles
//        titles[1] = Integer.toString(year) + "-" + new SimpleDateFormat("MMM").format(calendar.getTime());
        titles[1] = Integer.toString(year) + "-" + dateTime.toString("MMM");

        // compose bundle for arguments
        Bundle args = new Bundle();
        args.putDoubleArray(IncomeVsExpensesChartFragment.KEY_EXPENSES_VALUES, expenses);
        args.putDoubleArray(IncomeVsExpensesChartFragment.KEY_INCOME_VALUES, incomes);
        args.putStringArray(IncomeVsExpensesChartFragment.KEY_XTITLES, titles);
        args.putString(IncomeVsExpensesChartFragment.KEY_TITLE, getString(R.string.income_vs_expenses_current_month));
        args.putBoolean(IncomeVsExpensesChartFragment.KEY_DISPLAY_AS_UP_ENABLED, false);

        // get fragment manager
        FragmentManager fragmentManager = getChildFragmentManager();
        if (fragmentManager != null) {
            IncomeVsExpensesChartFragment fragment;

            fragment = new IncomeVsExpensesChartFragment();
            fragment.setChartArguments(args);

            if (fragment.isVisible())
                fragment.onResume();

            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.linearLayoutScreen1, fragment, IncomeVsExpensesChartFragment.class.getSimpleName());

            fragmentTransaction.commit();
        }
    }

    private View showTableLayoutTopWithdrawals(Cursor cursor) {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.dashboard_summary_layout, null);
        CurrencyService currencyService = new CurrencyService(getActivity().getApplicationContext());

        // Textview Title
        TextView title = (TextView) layout.findViewById(R.id.textViewTitle);
        title.setText(R.string.top_withdrawals_last_30_days);
        // Table
        TableLayout tableLayout = (TableLayout) layout.findViewById(R.id.tableLayoutSummary);
        // Create Title
        tableLayout.addView(createTableRow(new String[]{"<small><b>" + getString(R.string.category) + "</b></small>",
                "<small><b>" + getString(R.string.quantity) + "</b></small>", "<small><b>" + getString(R.string.summary) + "</b></small>"}, new Float[]{1f,
                null, null}, new Integer[]{null, Gravity.RIGHT, Gravity.RIGHT}, new Integer[][]{null, {0, 0, padding_in_px, 0}, null}));
        // add rows
        while (cursor.moveToNext()) {
            // load values
            String category = "<b>" + cursor.getString(cursor.getColumnIndex(ViewMobileData.Category)) + "</b>";
            if (!TextUtils.isEmpty(cursor.getString(cursor.getColumnIndex(ViewMobileData.Subcategory)))) {
                category += " : " + cursor.getString(cursor.getColumnIndex(ViewMobileData.Subcategory));
            }
            double total = cursor.getDouble(cursor.getColumnIndex("TOTAL"));
            int num = cursor.getInt(cursor.getColumnIndex("NUM"));
            // Add Row
            tableLayout.addView(createTableRow(new String[]{"<small>" + category + "</small>",
                            "<small><i>" + Integer.toString(num) + "</i></small>",
                            "<small>" + currencyService.getCurrencyFormatted(
                                    currencyService.getBaseCurrencyId(), MoneyFactory.fromDouble(total)) + "</small>"},
                    new Float[]{1f, null, null},
                    new Integer[]{null, Gravity.RIGHT, Gravity.RIGHT}, new Integer[][]{null, {0, 0, padding_in_px, 0}, null}));
        }
        // return Layout
        return layout;
    }

    private View showTableLayoutTopPayees(Cursor cursor) {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.dashboard_summary_layout, null);
        CurrencyService currencyService = new CurrencyService(getActivity().getApplicationContext());

        // Textview Title
        TextView title = (TextView) layout.findViewById(R.id.textViewTitle);
        title.setText(R.string.top_payees_last_30_days);
        // Table
        TableLayout tableLayout = (TableLayout) layout.findViewById(R.id.tableLayoutSummary);
        // Create Title
        tableLayout.addView(createTableRow(new String[]{"<small><b>" + getString(R.string.payee) + "</b></small>",
                "<small><b>" + getString(R.string.quantity) + "</b></small>", "<small><b>" + getString(R.string.summary) + "</b></small>"}, new Float[]{1f,
                null, null}, new Integer[]{null, Gravity.RIGHT, Gravity.RIGHT}, new Integer[][]{null, {0, 0, padding_in_px, 0}, null}));
        // add rows
        while (cursor.moveToNext()) {
            // load values
            String payee = cursor.getString(cursor.getColumnIndex(ViewMobileData.Payee));
            double total = cursor.getDouble(cursor.getColumnIndex("TOTAL"));
            int num = cursor.getInt(cursor.getColumnIndex("NUM"));
            // Add Row
            tableLayout.addView(createTableRow(new String[]{"<small>" + payee + "</small>",
                            "<small><i>" + Integer.toString(num) + "</i></small>",
                            "<small>" + currencyService.getCurrencyFormatted(
                                    currencyService.getBaseCurrencyId(), MoneyFactory.fromDouble(total)) + "</small>"},
                    new Float[]{1f, null, null},
                    new Integer[]{null, Gravity.RIGHT, Gravity.RIGHT},
                    new Integer[][]{null, {0, 0, padding_in_px, 0}, null}));
        }

        // return Layout
        return layout;
    }

    private View showTableLayoutUpComingTransactions(Cursor cursor) {
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.dashboard_summary_layout, null);
        CurrencyService currencyService = new CurrencyService(getActivity().getApplicationContext());
        Core core = new Core(getActivity().getApplicationContext());

        // Textview Title
        TextView title = (TextView) layout.findViewById(R.id.textViewTitle);
        title.setText(R.string.upcoming_transactions);
        // Table
        TableLayout tableLayout = (TableLayout) layout.findViewById(R.id.tableLayoutSummary);
        // add rows
        while (cursor.moveToNext()) {
            // load values
            String payee = "<i>" + cursor.getString(cursor.getColumnIndex(QueryBillDeposits.PAYEENAME)) + "</i>";
            double total = cursor.getDouble(cursor.getColumnIndex(QueryBillDeposits.AMOUNT));
            int daysLeft = cursor.getInt(cursor.getColumnIndex(QueryBillDeposits.DAYSLEFT));
            int currencyId = cursor.getInt(cursor.getColumnIndex(QueryBillDeposits.CURRENCYID));
            String daysLeftText = "";
            daysLeftText = Integer.toString(Math.abs(daysLeft)) + " " + getString(daysLeft >= 0 ? R.string.days_remaining : R.string.days_overdue);
            TableRow row = createTableRow(new String[]{"<small>" + payee + "</small>",
                            "<small>" + currencyService.getCurrencyFormatted(currencyId, MoneyFactory.fromDouble(total)) + "</small>",
                            "<small>" + daysLeftText + "</small>"}, new Float[]{1f, null, 1f},
                    new Integer[]{null, Gravity.RIGHT, Gravity.RIGHT}, new Integer[][]{null, {0, 0, padding_in_px, 0}, null});
            TextView txt = (TextView) row.getChildAt(2);
            UIHelper uiHelper = new UIHelper(getActivity());
            int color = daysLeft >= 0
                    ? uiHelper.resolveAttribute(R.attr.holo_green_color_theme)
                    : uiHelper.resolveAttribute(R.attr.holo_red_color_theme);
            txt.setTextColor(ContextCompat.getColor(getActivity(), color));
            // Add Row
            tableLayout.addView(row);
        }
        // return Layout
        return layout;
    }

    private TableRow createTableRow(String[] fields, Float[] weight, Integer[] gravity, Integer[][] margin) {
        // create row
        TableRow row = new TableRow(getActivity());
        row.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        for (int i = 0; i < fields.length; i++) {
            RobotoTextView txtField = new RobotoTextView(getActivity(), null);
            TableRow.LayoutParams layoutParams;
            if (weight[i] != null) {
                layoutParams = new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, weight[i]);
            } else {
                layoutParams = new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            }
            // set margin
            if (margin[i] != null)
                layoutParams.setMargins(margin[i][0], margin[i][1], margin[i][2], margin[i][3]);
            txtField.setLayoutParams(layoutParams);
            if (gravity[i] != null)
                txtField.setGravity(gravity[i]);
            // set text
            txtField.setText(UIHelper.fromHtml(fields[i]));
            // set singleline
            txtField.setSingleLine(true);
            // add field
            row.addView(txtField);
        }
        // return row
        return row;
    }
}
