/*
 * Copyright (C) 2012-2026 The Android Money Manager Ex Project Team
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

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;

import com.money.manager.ex.R;
import com.money.manager.ex.account.AccountTypes;
import com.money.manager.ex.core.InfoKeys;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.database.QueryAccountBills;
import com.money.manager.ex.database.SQLDataSet;
import com.money.manager.ex.servicelayer.InfoService;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.settings.LookAndFeelSettings;
import com.money.manager.ex.utils.MmxDate;
import com.money.manager.ex.utils.MmxDateTimeUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import info.javaperformance.money.MoneyFactory;
import timber.log.Timber;

public class SummaryOfAccountsReportFragment extends Fragment {

    private static final String PREF_FILTER_MODE = "SummaryAccountsFilterMode";
    private static final String PREF_FILTER_CUSTOM = "SummaryAccountsFilterCustom";

    private static final int PERIOD_ALL_TIME = R.id.menu_all_time;
    private static final int SORT_ASCENDING = R.id.menu_sort_asceding;
    private static final int SORT_DESCENDING = R.id.menu_sort_desceding;

    private static final String COL_ACCOUNT_ID = "ACCOUNTID";
    private static final String COL_ACCOUNT_TYPE = "ACCOUNTTYPE";
    private static final String COL_INITIAL_BASE = "INITIALBASE";
    private static final String COL_INITIAL_DATE = "INITIALDATE";

    private static final String COL_TRANS_DATE = "TRANSDATE";
    private static final String COL_TRANS_CODE = "TRANSCODE";
    private static final String COL_FROM_ACCOUNT_ID = "ACCOUNTID";
    private static final String COL_TO_ACCOUNT_ID = "TOACCOUNTID";
    private static final String COL_FROM_AMOUNT_BASE = "FROMAMOUNTBASE";
    private static final String COL_TO_AMOUNT_BASE = "TOAMOUNTBASE";

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMM", Locale.getDefault());

    private TableLayout tableLayout;
    private TextView emptyView;
    private int mItemSelected = PERIOD_ALL_TIME;
    private int mSortSelected = SORT_ASCENDING;
    private Date mDateFrom = null;
    private Date mDateTo = null;
    private ReportTableModel mLastModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_summary_of_accounts_report, container, false);
        tableLayout = view.findViewById(R.id.summaryAccountsTable);
        emptyView = view.findViewById(R.id.summaryAccountsEmpty);
        setupMenuProviders();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        loadReportAsync();
    }

    private void loadReportAsync() {
        new Thread(() -> {
            try {
                ReportTableModel model = buildModel();
                if (!isAdded()) {
                    return;
                }
                requireActivity().runOnUiThread(() -> renderModel(model));
            } catch (Exception e) {
                Timber.e(e, "loading summary of accounts report");
                if (!isAdded()) {
                    return;
                }
                requireActivity().runOnUiThread(() -> {
                    emptyView.setVisibility(View.VISIBLE);
                    tableLayout.setVisibility(View.GONE);
                    mLastModel = null;
                });
            }
        }).start();
    }

    private ReportTableModel buildModel() {
        AccountFilter accountFilter = getAccountFilter();
        BuildState state = new BuildState();

        loadAccounts(state, getAccountWhereClause(accountFilter));
        loadTransactions(state);

        normalizeDateBounds(state);

        List<String> orderedTypes = orderAccountTypes(state.accountTypesInUse);
        Map<String, String> typeLabels = getTypeLabels(orderedTypes);

        LocalDate visibleStartDate = resolveVisibleStart(state.minDate, state.maxDate);
        LocalDate visibleEndDate = resolveVisibleEnd(state.minDate, state.maxDate);
        LocalDate[] normalizedRange = normalizeVisibleRange(visibleStartDate, visibleEndDate);

        state.events.sort(Comparator.comparing(BalanceEvent::getDate));
        List<MonthRow> rows = buildRows(state, orderedTypes, normalizedRange[0], normalizedRange[1]);

        if (mSortSelected == SORT_DESCENDING) {
            rows.sort((left, right) -> right.month.compareTo(left.month));
        }

        return new ReportTableModel(orderedTypes, typeLabels, rows);
    }

    private void loadAccounts(BuildState state, String accountWhere) {
        Cursor accountCursor = executeSqlQuery("SELECT "
                + "a.ACCOUNTID, "
                + "a.ACCOUNTTYPE, "
                + "ifnull(a.INITIALBAL, 0) * ifnull(c.BASECONVRATE, 1) AS INITIALBASE, "
                + "date(ifnull(a.INITIALDATE, '1900-01-01')) AS INITIALDATE "
                + "FROM ACCOUNTLIST_V1 a "
                + "LEFT JOIN CURRENCYFORMATS_V1 c ON a.CURRENCYID = c.CURRENCYID "
                + accountWhere);

        if (accountCursor == null) {
            return;
        }

        try {
            while (accountCursor.moveToNext()) {
                long accountId = accountCursor.getLong(accountCursor.getColumnIndexOrThrow(COL_ACCOUNT_ID));
                String accountType = accountCursor.getString(accountCursor.getColumnIndexOrThrow(COL_ACCOUNT_TYPE));
                double initialBase = accountCursor.getDouble(accountCursor.getColumnIndexOrThrow(COL_INITIAL_BASE));
                LocalDate initialDate = parseDate(accountCursor.getString(accountCursor.getColumnIndexOrThrow(COL_INITIAL_DATE)));

                state.accountTypeById.put(accountId, accountType);
                state.accountTypesInUse.add(accountType);

                if (initialDate != null && initialBase != 0d) {
                    state.events.add(new BalanceEvent(initialDate, accountId, initialBase));
                }

                state.minDate = minDate(state.minDate, initialDate);
            }
        } finally {
            accountCursor.close();
        }
    }

    private void loadTransactions(BuildState state) {
        Cursor transactionCursor = executeSqlQuery("SELECT "
                + "date(t.TRANSDATE) AS TRANSDATE, "
                + "t.TRANSCODE, "
                + "t.ACCOUNTID, "
                + "ifnull(t.TOACCOUNTID, -1) AS TOACCOUNTID, "
                + "ifnull(t.TRANSAMOUNT, 0) * ifnull(cfFrom.BASECONVRATE, 1) AS FROMAMOUNTBASE, "
                + "ifnull(t.TOTRANSAMOUNT, 0) * ifnull(cfTo.BASECONVRATE, 1) AS TOAMOUNTBASE "
                + "FROM CHECKINGACCOUNT_V1 t "
                + "LEFT JOIN ACCOUNTLIST_V1 aFrom ON aFrom.ACCOUNTID = t.ACCOUNTID "
                + "LEFT JOIN CURRENCYFORMATS_V1 cfFrom ON cfFrom.CURRENCYID = aFrom.CURRENCYID "
                + "LEFT JOIN ACCOUNTLIST_V1 aTo ON aTo.ACCOUNTID = t.TOACCOUNTID "
                + "LEFT JOIN CURRENCYFORMATS_V1 cfTo ON cfTo.CURRENCYID = aTo.CURRENCYID "
                + "WHERE (t.DELETEDTIME IS NULL OR t.DELETEDTIME = '') "
                + "AND t.STATUS IN ('R', 'F', 'D', '') "
                + "ORDER BY date(t.TRANSDATE), t.TRANSID");

        if (transactionCursor == null) {
            return;
        }

        try {
            while (transactionCursor.moveToNext()) {
                LocalDate txDate = parseDate(transactionCursor.getString(transactionCursor.getColumnIndexOrThrow(COL_TRANS_DATE)));
                if (txDate == null) {
                    continue;
                }

                long fromAccountId = transactionCursor.getLong(transactionCursor.getColumnIndexOrThrow(COL_FROM_ACCOUNT_ID));
                long toAccountId = transactionCursor.getLong(transactionCursor.getColumnIndexOrThrow(COL_TO_ACCOUNT_ID));
                String transCode = transactionCursor.getString(transactionCursor.getColumnIndexOrThrow(COL_TRANS_CODE));
                double fromAmountBase = transactionCursor.getDouble(transactionCursor.getColumnIndexOrThrow(COL_FROM_AMOUNT_BASE));
                double toAmountBase = transactionCursor.getDouble(transactionCursor.getColumnIndexOrThrow(COL_TO_AMOUNT_BASE));

                addTransactionEvents(state.events, txDate, transCode, fromAccountId, toAccountId, fromAmountBase, toAmountBase);
                updateDateBoundsForIncludedAccounts(state, txDate, fromAccountId, toAccountId);
            }
        } finally {
            transactionCursor.close();
        }
    }

    private void addTransactionEvents(List<BalanceEvent> events, LocalDate txDate, String transCode,
            long fromAccountId, long toAccountId, double fromAmountBase, double toAmountBase) {
        if ("Withdrawal".equalsIgnoreCase(transCode)) {
            events.add(new BalanceEvent(txDate, fromAccountId, -fromAmountBase));
            return;
        }

        if ("Deposit".equalsIgnoreCase(transCode)) {
            events.add(new BalanceEvent(txDate, fromAccountId, fromAmountBase));
            return;
        }

        if ("Transfer".equalsIgnoreCase(transCode)) {
            events.add(new BalanceEvent(txDate, fromAccountId, -fromAmountBase));
            if (toAccountId > 0) {
                events.add(new BalanceEvent(txDate, toAccountId, toAmountBase));
            }
        }
    }

    private void updateDateBoundsForIncludedAccounts(BuildState state, LocalDate txDate, long fromAccountId, long toAccountId) {
        boolean touchesIncludedAccount = state.accountTypeById.containsKey(fromAccountId)
                || (toAccountId > 0 && state.accountTypeById.containsKey(toAccountId));

        if (!touchesIncludedAccount) {
            return;
        }

        state.minDate = minDate(state.minDate, txDate);
        state.maxDate = maxDate(state.maxDate, txDate);
    }

    private void normalizeDateBounds(BuildState state) {
        if (state.maxDate == null) {
            state.maxDate = LocalDate.now();
        }
        if (state.minDate == null) {
            state.minDate = state.maxDate;
        }
    }

    private LocalDate resolveVisibleStart(LocalDate minDate, LocalDate maxDate) {
        LocalDate start = mDateFrom == null ? minDate : toLocalDate(mDateFrom);
        if (start != null) {
            return start;
        }
        return maxDate == null ? LocalDate.now() : maxDate;
    }

    private LocalDate resolveVisibleEnd(LocalDate minDate, LocalDate maxDate) {
        LocalDate end = mDateTo == null ? maxDate : toLocalDate(mDateTo);
        if (end != null) {
            return end;
        }
        return minDate == null ? LocalDate.now() : minDate;
    }

    private LocalDate[] normalizeVisibleRange(LocalDate start, LocalDate end) {
        LocalDate safeStart = start == null ? LocalDate.now() : start;
        LocalDate safeEnd = end == null ? safeStart : end;

        if (safeStart.isAfter(safeEnd)) {
            return new LocalDate[]{safeEnd, safeStart};
        }
        return new LocalDate[]{safeStart, safeEnd};
    }

    private List<MonthRow> buildRows(BuildState state, List<String> orderedTypes, LocalDate visibleStartDate,
            LocalDate visibleEndDate) {
        List<MonthRow> rows = new ArrayList<>();
        Map<String, Double> typeTotals = createTypeTotals(orderedTypes);

        int eventIndex = 0;
        YearMonth month = YearMonth.from(visibleStartDate);
        YearMonth end = YearMonth.from(visibleEndDate);

        while (!month.isAfter(end)) {
            LocalDate monthEnd = month.atEndOfMonth();
            eventIndex = consumeEventsUntil(monthEnd, state, typeTotals, eventIndex);
            rows.add(new MonthRow(month, new LinkedHashMap<>(typeTotals)));
            month = month.plusMonths(1);
        }

        return rows;
    }

    private Map<String, Double> createTypeTotals(List<String> orderedTypes) {
        Map<String, Double> typeTotals = new LinkedHashMap<>();
        for (String type : orderedTypes) {
            typeTotals.put(type, 0d);
        }
        return typeTotals;
    }

    private int consumeEventsUntil(LocalDate monthEnd, BuildState state, Map<String, Double> typeTotals, int startIndex) {
        int eventIndex = startIndex;
        while (eventIndex < state.events.size() && !state.events.get(eventIndex).getDate().isAfter(monthEnd)) {
            BalanceEvent event = state.events.get(eventIndex);
            String accountType = state.accountTypeById.get(event.getAccountId());
            if (accountType != null && typeTotals.containsKey(accountType)) {
                typeTotals.put(accountType, typeTotals.get(accountType) + event.getDelta());
            }
            eventIndex++;
        }
        return eventIndex;
    }

    private LocalDate minDate(LocalDate current, LocalDate candidate) {
        if (candidate == null) {
            return current;
        }
        if (current == null || candidate.isBefore(current)) {
            return candidate;
        }
        return current;
    }

    private LocalDate maxDate(LocalDate current, LocalDate candidate) {
        if (candidate == null) {
            return current;
        }
        if (current == null || candidate.isAfter(current)) {
            return candidate;
        }
        return current;
    }

    private void renderModel(ReportTableModel model) {
        tableLayout.removeAllViews();
        mLastModel = model;

        if (model.rows.isEmpty() || model.orderedTypes.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            tableLayout.setVisibility(View.GONE);
            return;
        }

        emptyView.setVisibility(View.GONE);
        tableLayout.setVisibility(View.VISIBLE);

        tableLayout.addView(createHeaderRow(model));

        CurrencyService currencyService = new CurrencyService(requireContext());
        for (MonthRow row : model.rows) {
            TableRow tableRow = new TableRow(requireContext());
            double rowTotal = 0d;

            tableRow.addView(createCell(Integer.toString(row.month.getYear()), true));
            tableRow.addView(createCell(row.month.atDay(1).format(MONTH_FORMATTER), true));

            for (String type : model.orderedTypes) {
                double value = row.valuesByType.getOrDefault(type, 0d);
                rowTotal += value;
                String formatted = currencyService.getBaseCurrencyFormatted(MoneyFactory.fromDouble(value));
                tableRow.addView(createCell(formatted, false));
            }

            String totalFormatted = currencyService.getBaseCurrencyFormatted(MoneyFactory.fromDouble(rowTotal));
            tableRow.addView(createHeaderCell(totalFormatted, false));

            tableLayout.addView(tableRow);
        }
    }

    private TableRow createHeaderRow(ReportTableModel model) {
        TableRow row = new TableRow(requireContext());
        row.addView(createHeaderCell(getString(R.string.year), true));
        row.addView(createHeaderCell(getString(R.string.month), true));

        for (String type : model.orderedTypes) {
            row.addView(createHeaderCell(model.typeLabels.getOrDefault(type, type), false));
        }

        row.addView(createHeaderCell(getString(R.string.total), false));

        return row;
    }

    private TextView createHeaderCell(String text, boolean alignStart) {
        TextView cell = createCell(text, alignStart);
        cell.setTypeface(null, android.graphics.Typeface.BOLD);
        return cell;
    }

    private TextView createCell(String text, boolean alignStart) {
        TextView cell = new TextView(requireContext());
        cell.setText(text);
        cell.setPadding(16, 8, 16, 8);
        cell.setGravity(alignStart ? Gravity.START : Gravity.END);
        return cell;
    }

    private Cursor executeSqlQuery(String sql) {
        if (getContext() == null) {
            return null;
        }
        return requireContext().getContentResolver().query(new SQLDataSet().getUri(), null, sql, null, null);
    }

    private void setupMenuProviders() {
        MenuHost menuHost = requireActivity();
        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.menu_summary_of_accounts, menu);
                menuInflater.inflate(R.menu.menu_period_picker, menu);

                int selectedMode = getFilterMode();
                MenuItem selectedItem = menu.findItem(selectedMode);
                if (selectedItem != null) {
                    selectedItem.setChecked(true);
                }

                MenuItem selectedPeriod = menu.findItem(mItemSelected);
                if (selectedPeriod != null) {
                    selectedPeriod.setChecked(true);
                }

                MenuItem selectedSort = menu.findItem(mSortSelected);
                if (selectedSort != null) {
                    selectedSort.setChecked(true);
                }
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (isAccountFilterMenuItem(itemId)) {
                    handleAccountFilterItemSelected(itemId, item);
                    return true;
                }

                if (itemId == R.id.menu_chart) {
                    showChart();
                    return true;
                }

                if (isSortMenuItem(itemId)) {
                    handleSortItemSelected(itemId, item);
                    return true;
                }

                if (handlePeriodSelection(item)) {
                    return true;
                }

                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    private boolean isAccountFilterMenuItem(int itemId) {
        return itemId == R.id.menu_summary_accounts_all
                || itemId == R.id.menu_summary_accounts_open
                || itemId == R.id.menu_summary_accounts_favorite
                || itemId == R.id.menu_summary_accounts_custom;
    }

    private void handleAccountFilterItemSelected(int itemId, @NonNull MenuItem item) {
        item.setChecked(true);
        saveFilterMode(itemId);

        if (itemId == R.id.menu_summary_accounts_custom) {
            showAccountSelectionDialog();
            return;
        }

        loadReportAsync();
    }

    private boolean isSortMenuItem(int itemId) {
        return itemId == R.id.menu_sort_asceding || itemId == R.id.menu_sort_desceding;
    }

    private void handleSortItemSelected(int itemId, @NonNull MenuItem item) {
        mSortSelected = itemId;
        item.setChecked(true);
        loadReportAsync();
    }

    private boolean handlePeriodSelection(@NonNull MenuItem menuItem) {
        MmxDate dateTime = MmxDate.newDate();
        int itemId = menuItem.getItemId();

        if (itemId == R.id.menu_current_month) {
            mDateFrom = dateTime.firstDayOfMonth().toDate();
            mDateTo = dateTime.lastDayOfMonth().toDate();
        } else if (itemId == R.id.menu_last_month) {
            mDateFrom = dateTime.minusMonths(1).firstDayOfMonth().toDate();
            mDateTo = dateTime.lastDayOfMonth().toDate();
        } else if (itemId == R.id.menu_last_30_days) {
            mDateTo = dateTime.toDate();
            mDateFrom = dateTime.minusDays(30).toDate();
        } else if (itemId == R.id.menu_current_year) {
            mDateFrom = dateTime.firstMonthOfYear().firstDayOfMonth().toDate();
            mDateTo = dateTime.lastMonthOfYear().lastDayOfMonth().toDate();
        } else if (itemId == R.id.menu_last_year) {
            mDateFrom = dateTime.minusYears(1).firstMonthOfYear().firstDayOfMonth().toDate();
            mDateTo = dateTime.lastMonthOfYear().lastDayOfMonth().toDate();
        } else if (itemId == R.id.menu_current_fin_year || itemId == R.id.menu_last_fin_year) {
            InfoService infoService = new InfoService(requireContext());
            int financialYearStartDay = Integer.parseInt(infoService.getInfoValue(InfoKeys.FINANCIAL_YEAR_START_DAY, "1"));
            int financialYearStartMonth = Integer.parseInt(infoService.getInfoValue(InfoKeys.FINANCIAL_YEAR_START_MONTH, "1")) - 1;
            if (financialYearStartMonth < 0) {
                financialYearStartMonth = 0;
            }

            MmxDate fiscalStart = MmxDate.newDate();
            fiscalStart.setDate(financialYearStartDay);
            fiscalStart.setMonth(financialYearStartMonth);
            if (fiscalStart.toDate().after(dateTime.toDate())) {
                fiscalStart.minusYears(1);
            }
            if (itemId == R.id.menu_last_fin_year) {
                fiscalStart.minusYears(1);
            }
            mDateFrom = fiscalStart.toDate();
            mDateTo = fiscalStart.addYear(1).minusDays(1).toDate();
        } else if (itemId == R.id.menu_all_time) {
            mDateFrom = null;
            mDateTo = null;
        } else if (itemId == R.id.menu_custom_dates) {
            menuItem.setChecked(true);
            mItemSelected = itemId;
            showDialogCustomDates();
            return true;
        } else {
            return false;
        }

        mItemSelected = itemId;
        menuItem.setChecked(true);
        loadReportAsync();
        return true;
    }

    private void showChart() {
        if (mLastModel == null || mLastModel.rows.isEmpty() || mLastModel.orderedTypes.isEmpty()) {
            new UIHelper(requireContext()).showToast(R.string.no_data);
            return;
        }

        int rowCount = mLastModel.rows.size();
        int typeCount = mLastModel.orderedTypes.size();

        String[] xTitles = new String[rowCount];
        float[][] values = new float[rowCount][typeCount];
        String[] stackLabels = new String[typeCount];

        for (int typeIndex = 0; typeIndex < typeCount; typeIndex++) {
            String type = mLastModel.orderedTypes.get(typeIndex);
            String label = mLastModel.typeLabels.get(type);
            stackLabels[typeIndex] = TextUtils.isEmpty(label) ? type : label;
        }

        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
            MonthRow row = mLastModel.rows.get(rowIndex);
            xTitles[rowIndex] = row.month.atDay(1).format(DateTimeFormatter.ofPattern("MMM yyyy", Locale.getDefault()));

            for (int typeIndex = 0; typeIndex < typeCount; typeIndex++) {
                String type = mLastModel.orderedTypes.get(typeIndex);
                double value = row.valuesByType.getOrDefault(type, 0d);
                values[rowIndex][typeIndex] = (float) value;
            }
        }

        Intent intent = new Intent(requireContext(), SummaryOfAccountsChartActivity.class);
        intent.putExtra(SummaryOfAccountsChartFragment.KEY_X_TITLES, xTitles);
        intent.putExtra(SummaryOfAccountsChartFragment.KEY_STACK_LABELS, stackLabels);
        intent.putExtra(SummaryOfAccountsChartFragment.KEY_STACK_VALUES, values);
        startActivity(intent);
    }

    private void showDialogCustomDates() {
        View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_choose_date_report, null);
        DatePicker fromDatePicker = dialogView.findViewById(R.id.datePickerFromDate);
        DatePicker toDatePicker = dialogView.findViewById(R.id.datePickerToDate);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(dialogView)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    MmxDateTimeUtils dateTimeUtils = new MmxDateTimeUtils(Locale.getDefault());
                    mDateFrom = dateTimeUtils.from(fromDatePicker);
                    mDateTo = dateTimeUtils.from(toDatePicker);
                    loadReportAsync();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();

        if (mDateFrom == null) {
            mDateFrom = new MmxDate().today().toDate();
        }
        if (mDateTo == null) {
            mDateTo = new MmxDate().today().toDate();
        }

        MmxDateTimeUtils dateTimeUtils = new MmxDateTimeUtils(Locale.getDefault());
        dateTimeUtils.setDatePicker(mDateFrom, fromDatePicker);
        dateTimeUtils.setDatePicker(mDateTo, toDatePicker);
    }

    private int getFilterMode() {
        LookAndFeelSettings settings = new AppSettings(requireContext()).getLookAndFeelSettings();
        String storedValue = settings.get(PREF_FILTER_MODE, Integer.toString(R.id.menu_summary_accounts_open));
        try {
            return Integer.parseInt(storedValue);
        } catch (Exception e) {
            return R.id.menu_summary_accounts_open;
        }
    }

    private void saveFilterMode(int mode) {
        LookAndFeelSettings settings = new AppSettings(requireContext()).getLookAndFeelSettings();
        settings.set(PREF_FILTER_MODE, Integer.toString(mode));
    }

    private AccountFilter getAccountFilter() {
        int mode = getFilterMode();
        List<Long> customAccountIds = parseSelectedAccountIds();
        return new AccountFilter(mode, customAccountIds);
    }

    private String getAccountWhereClause(AccountFilter filter) {
        if (filter.mode == R.id.menu_summary_accounts_all) {
            return "";
        }
        if (filter.mode == R.id.menu_summary_accounts_open) {
            return "WHERE lower(a.STATUS) = 'open'";
        }
        if (filter.mode == R.id.menu_summary_accounts_favorite) {
            return "WHERE lower(a.FAVORITEACCT) = 'true'";
        }
        if (filter.mode == R.id.menu_summary_accounts_custom) {
            if (filter.customAccountIds.isEmpty()) {
                return "WHERE 1=2";
            }
            return "WHERE a.ACCOUNTID IN (" + joinIds(filter.customAccountIds) + ")";
        }
        return "";
    }

    private void showAccountSelectionDialog() {
        ArrayList<Long> selected = new ArrayList<>(parseSelectedAccountIds());

        QueryAccountBills queryAccountBills = new QueryAccountBills(requireContext());
        Cursor cursor = requireContext().getContentResolver().query(
                queryAccountBills.getUri(),
                null,
                null,
                null,
                QueryAccountBills.ACCOUNTTYPE + ", upper(" + QueryAccountBills.ACCOUNTNAME + ")");

        if (cursor == null) {
            return;
        }

        final ArrayList<Long> accountIds = new ArrayList<>();
        final ArrayList<String> accountNames = new ArrayList<>();
        try {
            while (cursor.moveToNext()) {
                long accountId = cursor.getLong(cursor.getColumnIndexOrThrow(QueryAccountBills.ACCOUNTID));
                String accountName = cursor.getString(cursor.getColumnIndexOrThrow(QueryAccountBills.ACCOUNTNAME));
                accountIds.add(accountId);
                accountNames.add(accountName);
            }
        } finally {
            cursor.close();
        }

        final boolean[] checkedItems = new boolean[accountIds.size()];
        for (int i = 0; i < accountIds.size(); i++) {
            checkedItems[i] = selected.contains(accountIds.get(i));
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.menu_cashflow_custom);
        builder.setMultiChoiceItems(accountNames.toArray(new CharSequence[0]), checkedItems,
                (dialog, which, isChecked) -> checkedItems[which] = isChecked);
        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            ArrayList<Long> selectedIds = new ArrayList<>();
            for (int i = 0; i < accountIds.size(); i++) {
                if (checkedItems[i]) {
                    selectedIds.add(accountIds.get(i));
                }
            }
            saveSelectedAccountIds(selectedIds);
            loadReportAsync();
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.show();
    }

    private ArrayList<Long> parseSelectedAccountIds() {
        LookAndFeelSettings settings = new AppSettings(requireContext()).getLookAndFeelSettings();
        String raw = settings.get(PREF_FILTER_CUSTOM, "");
        ArrayList<Long> result = new ArrayList<>();
        if (raw.trim().isEmpty()) {
            return result;
        }

        String[] ids = raw.split(",");
        for (String id : ids) {
            if (id.trim().isEmpty()) {
                continue;
            }
            try {
                result.add(Long.parseLong(id.trim()));
            } catch (Exception e) {
                Timber.w(e, "Invalid account id in filter: %s", id);
            }
        }
        return result;
    }

    private void saveSelectedAccountIds(List<Long> ids) {
        LookAndFeelSettings settings = new AppSettings(requireContext()).getLookAndFeelSettings();
        settings.set(PREF_FILTER_CUSTOM, joinIds(ids));
    }

    private String joinIds(List<Long> ids) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(ids.get(i));
        }
        return builder.toString();
    }

    private LocalDate toLocalDate(@Nullable Date date) {
        if (date == null) {
            return null;
        }
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    @Nullable
    private LocalDate parseDate(@Nullable String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        try {
            String cleanDate = dateString;
            if (cleanDate.length() > 10) {
                cleanDate = cleanDate.substring(0, 10);
            }
            return LocalDate.parse(cleanDate);
        } catch (Exception e) {
            Timber.w(e, "Unable to parse date: %s", dateString);
            return null;
        }
    }

    private List<String> orderAccountTypes(LinkedHashSet<String> accountTypesInUse) {
        List<String> ordered = new ArrayList<>();
        for (String knownType : AccountTypes.getNames()) {
            if (accountTypesInUse.contains(knownType)) {
                ordered.add(knownType);
            }
        }

        for (String type : accountTypesInUse) {
            if (!ordered.contains(type)) {
                ordered.add(type);
            }
        }
        return ordered;
    }

    private Map<String, String> getTypeLabels(List<String> orderedTypes) {
        Map<String, String> labels = new HashMap<>();
        String[] values = AccountTypes.getNames();
        String[] displayItems = getResources().getStringArray(R.array.accounttype_items);

        int size = Math.min(values.length, displayItems.length);
        for (int i = 0; i < size; i++) {
            labels.put(values[i], displayItems[i]);
        }

        for (String type : orderedTypes) {
            labels.putIfAbsent(type, type);
        }

        return labels;
    }

    private static class BalanceEvent {
        private final LocalDate date;
        private final long accountId;
        private final double delta;

        BalanceEvent(LocalDate date, long accountId, double delta) {
            this.date = date;
            this.accountId = accountId;
            this.delta = delta;
        }

        LocalDate getDate() {
            return date;
        }

        long getAccountId() {
            return accountId;
        }

        double getDelta() {
            return delta;
        }
    }

    private static class MonthRow {
        private final YearMonth month;
        private final Map<String, Double> valuesByType;

        MonthRow(YearMonth month, Map<String, Double> valuesByType) {
            this.month = month;
            this.valuesByType = valuesByType;
        }
    }

    private static class ReportTableModel {
        private final List<String> orderedTypes;
        private final Map<String, String> typeLabels;
        private final List<MonthRow> rows;

        ReportTableModel(List<String> orderedTypes, Map<String, String> typeLabels, List<MonthRow> rows) {
            this.orderedTypes = orderedTypes;
            this.typeLabels = typeLabels;
            this.rows = rows;
        }
    }

    private static class AccountFilter {
        private final int mode;
        private final List<Long> customAccountIds;

        AccountFilter(int mode, List<Long> customAccountIds) {
            this.mode = mode;
            this.customAccountIds = customAccountIds;
        }
    }

    private static class BuildState {
        private final Map<Long, String> accountTypeById = new HashMap<>();
        private final LinkedHashSet<String> accountTypesInUse = new LinkedHashSet<>();
        private final List<BalanceEvent> events = new ArrayList<>();
        private LocalDate minDate;
        private LocalDate maxDate;
    }
}
