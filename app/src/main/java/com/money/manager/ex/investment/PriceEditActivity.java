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

package com.money.manager.ex.investment;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ContentValues;

import androidx.appcompat.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.mikepenz.iconics.view.IconicsImageView;
import com.money.manager.ex.Constants;
import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.common.Calculator;
import com.money.manager.ex.common.CalculatorActivity;
import com.money.manager.ex.common.MmxBaseFragmentActivity;
import com.money.manager.ex.core.DateRange;
import com.money.manager.ex.core.MenuHelper;
import com.money.manager.ex.core.RequestCodes;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.datalayer.StockHistoryRepository;
import com.money.manager.ex.datalayer.StockRepository;
import com.money.manager.ex.domainmodel.Stock;
import com.money.manager.ex.domainmodel.StockHistory;
import com.money.manager.ex.reports.ReportDateRangeSupport;
import com.money.manager.ex.investment.yahoofinance.StockPriceRepository;
import com.money.manager.ex.utils.MmxDate;
import com.money.manager.ex.utils.MmxDateTimeUtils;
import com.money.manager.ex.view.RobotoTextView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.Lazy;
import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;
import timber.log.Timber;

public class PriceEditActivity
    extends MmxBaseFragmentActivity {

    public static final String ARG_CURRENCY_ID = "PriceEditActivity:CurrencyId";
    private static final String STATE_ACCOUNT_ID = "PriceEditActivity:AccountId";
    private static final String STATE_SYMBOL = "PriceEditActivity:Symbol";
    private static final String STATE_PRICE = "PriceEditActivity:Price";
    private static final String STATE_DATE = "PriceEditActivity:Date";
    private static final String STATE_CURRENCY_ID = "PriceEditActivity:CurrencyIdState";
    private static final String STATE_CURRENT_PRICE = "PriceEditActivity:CurrentPrice";
    private static final String STATE_INITIAL_DATE = "PriceEditActivity:InitialDate";
    private static final String STATE_CHART_PERIOD = "PriceEditActivity:ChartPeriod";
    private static final String STATE_CUSTOM_RANGE_FROM = "PriceEditActivity:CustomRangeFrom";
    private static final String STATE_CUSTOM_RANGE_TO = "PriceEditActivity:CustomRangeTo";

    @Inject Lazy<MmxDateTimeUtils> dateTimeUtilsLazy;

    protected PriceEditModel model;
    private EditPriceViewHolder viewHolder;
    private StockHistoryRepository historyRepository;
    private StockHistoryAdapter historyAdapter;
    private RecyclerView priceHistoryRecyclerView;
    private LineChart priceHistoryChart;
    private RobotoTextView stockNameTextView;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private Money mStockCurrentPrice;
    private String mInitialDateIso;
    private List<StockHistory> historyItems = new ArrayList<>();
    private List<String> currentChartIsoDates = new ArrayList<>();
    private PriceChartPeriod selectedChartPeriod = PriceChartPeriod.LAST_6_MONTHS;
    private DateRange selectedCustomRange;
    private boolean suppressChartSelectionCallback;
    private boolean suppressChartValueSelected;
    private int chartTextColor = -1;

    // Helper: populate chart lists from history
    private void buildChartLists(List<StockHistory> filteredHistory, ArrayList<String> xValues, ArrayList<Entry> entries) {
        currentChartIsoDates = new ArrayList<>();
        for (int i = 0; i < filteredHistory.size(); i++) {
            StockHistory history = filteredHistory.get(i);
            String valueString = history.getString(StockHistory.VALUE);
            if (valueString == null) {
                continue;
            }

            currentChartIsoDates.add(history.getString(StockHistory.DATE));
            xValues.add(dateTimeUtilsLazy.get().getUserFormattedDate(this, history.getDate()));
            entries.add(new Entry((float) MoneyFactory.fromString(valueString).toDouble(), i));
        }
    }

    // Helper: apply built data lists to the chart
    private void applyDataSetToChart(ArrayList<String> xValues, ArrayList<Entry> entries) {
        LineDataSet dataSet = new LineDataSet(entries, model.symbol);
        dataSet.setColor(getResources().getColor(R.color.material_blue_500));
        dataSet.setCircleColor(getResources().getColor(R.color.material_blue_500));
        dataSet.setLineWidth(2f);
        dataSet.setDrawCircles(true);
        dataSet.setDrawValues(false);
        dataSet.setHighLightColor(getResources().getColor(R.color.material_deep_orange_500));
        dataSet.setDrawFilled(false);
        dataSet.setAxisDependency(YAxis.AxisDependency.LEFT);

        LineData data = new LineData(xValues, dataSet);
        if (chartTextColor != -1) {
            data.setValueTextColor(getResources().getColor(chartTextColor));
        }

        priceHistoryChart.setData(data);

        XAxis xAxis = priceHistoryChart.getXAxis();
        if (xAxis != null) {
            xAxis.setLabelsToSkip(Math.max(0, xValues.size() / 6));
        }

        priceHistoryChart.notifyDataSetChanged();
        selectChartDate(model != null && model.date != null ? model.date.toDate() : null);
        priceHistoryChart.invalidate();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_price_edit);

        MmexApplication.getApp().iocComponent.inject(this);

        initializeToolbar();

        if (savedInstanceState != null) {
            initializeModel(savedInstanceState);
        } else {
            initializeModel();
        }

        viewHolder = new EditPriceViewHolder();
        viewHolder.bind(this);
        viewHolder.amountTextView.setOnClickListener(view -> onPriceClick());
        viewHolder.addButton.setOnClickListener(view -> onAddClick());
        viewHolder.dateTextView.setOnClickListener(view -> onDateClick());
        viewHolder.previousDayButton.setOnClickListener(view -> onPreviousDayClick());
        viewHolder.nextDayButton.setOnClickListener(view -> onNextDayClick());

        stockNameTextView = findViewById(R.id.stockNameTextView);
        historyRepository = new StockHistoryRepository(this);

        setupPriceHistoryChart();
        setupHistoryRecyclerView();

        IconicsImageView downloadButton = findViewById(R.id.downloadPricesButton);
        downloadButton.setOnClickListener(v -> onDownloadPricesClick());

        IconicsImageView downloadSelectedPriceButton = findViewById(R.id.downloadSelectedPriceButton);
        downloadSelectedPriceButton.setOnClickListener(v -> onDownloadSelectedDatePriceClick());

        model.display(this, viewHolder);

        loadHistoricalPriceForCurrentDate();
        loadHistory();
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadStockName();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ((resultCode == Activity.RESULT_CANCELED) || data == null) return;

        if (requestCode == RequestCodes.AMOUNT) {
            String stringExtra = data.getStringExtra(CalculatorActivity.RESULT_AMOUNT);
            model.price = MoneyFactory.fromString(stringExtra);
            model.display(this, viewHolder);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            // cancel clicked. Prompt to confirm?
            Timber.d("going back");
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        if (model != null) {
            savedInstanceState.putLong(STATE_ACCOUNT_ID, model.accountId);
            savedInstanceState.putString(STATE_SYMBOL, model.symbol);
            savedInstanceState.putString(STATE_PRICE, model.price != null ? model.price.toString() : null);
            savedInstanceState.putString(STATE_DATE, model.date != null ? model.date.toIsoDateString() : null);
            savedInstanceState.putLong(STATE_CURRENCY_ID, model.currencyId);
        }
        savedInstanceState.putString(STATE_CURRENT_PRICE, mStockCurrentPrice != null ? mStockCurrentPrice.toString() : null);
        savedInstanceState.putString(STATE_INITIAL_DATE, mInitialDateIso);
        savedInstanceState.putString(STATE_CHART_PERIOD, selectedChartPeriod.name());
        if (selectedCustomRange != null) {
            savedInstanceState.putLong(STATE_CUSTOM_RANGE_FROM,
                selectedCustomRange.dateFrom != null ? selectedCustomRange.dateFrom.getTime() : Long.MIN_VALUE);
            savedInstanceState.putLong(STATE_CUSTOM_RANGE_TO,
                selectedCustomRange.dateTo != null ? selectedCustomRange.dateTo.getTime() : Long.MIN_VALUE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

    private void onPriceClick() {
        Calculator.forActivity(this)
            .amount(model.price)
            .roundToCurrency(false)
            .show(RequestCodes.AMOUNT);
    }

    private void onDateClick() {
        MmxDate priceDate = model.date;

        DatePickerDialog.OnDateSetListener listener = (view, year, month, dayOfMonth) -> {
            model.date = new MmxDate(year, month, dayOfMonth);
            model.display(PriceEditActivity.this, viewHolder);
            loadHistoricalPriceForCurrentDate();
            scrollToCurrentDate();
            selectChartDate(model.date.toDate());
        };

        DatePickerDialog datePicker = new DatePickerDialog(
                PriceEditActivity.this,
                listener,
                priceDate.getYear(),
                priceDate.getMonthOfYear(),
                priceDate.getDayOfMonth()
        );

        datePicker.show();
    }

    private void onPreviousDayClick() {
        model.date = model.date.minusDays(1);
        model.display(this, viewHolder);
        loadHistoricalPriceForCurrentDate();
        scrollToCurrentDate();
        selectChartDate(model.date.toDate());
    }

    private void onNextDayClick() {
        model.date = model.date.plusDays(1);
        model.display(this, viewHolder);
        loadHistoricalPriceForCurrentDate();
        scrollToCurrentDate();
        selectChartDate(model.date.toDate());
    }

    private void initializeModel() {
        model = new PriceEditModel();
        readParameters();
    }

    private void initializeModel(Bundle savedInstanceState) {
        model = new PriceEditModel();

        if (savedInstanceState == null) {
            readParameters();
            return;
        }

        model.accountId = savedInstanceState.getLong(STATE_ACCOUNT_ID, Constants.NOT_SET);
        model.symbol = savedInstanceState.getString(STATE_SYMBOL);

        String priceString = savedInstanceState.getString(STATE_PRICE);
        model.price = priceString != null ? MoneyFactory.fromString(priceString) : MoneyFactory.fromDouble(0);

        String dateString = savedInstanceState.getString(STATE_DATE);
        model.date = dateString != null ? new MmxDate(dateString) : new MmxDate();
        model.currencyId = savedInstanceState.getLong(STATE_CURRENCY_ID, Constants.NOT_SET);

        String currentPriceString = savedInstanceState.getString(STATE_CURRENT_PRICE);
        mStockCurrentPrice = currentPriceString != null ? MoneyFactory.fromString(currentPriceString) : model.price;
        mInitialDateIso = savedInstanceState.getString(STATE_INITIAL_DATE, model.date.toIsoDateString());

        String periodName = savedInstanceState.getString(STATE_CHART_PERIOD);
        if (periodName != null) {
            selectedChartPeriod = PriceChartPeriod.fromName(periodName);
        }

        long customRangeFrom = savedInstanceState.getLong(STATE_CUSTOM_RANGE_FROM, Long.MIN_VALUE);
        long customRangeTo = savedInstanceState.getLong(STATE_CUSTOM_RANGE_TO, Long.MIN_VALUE);
        if (customRangeFrom != Long.MIN_VALUE && customRangeTo != Long.MIN_VALUE) {
            selectedCustomRange = new DateRange(new Date(customRangeFrom), new Date(customRangeTo));
        }
    }

    private void initializeToolbar() {
        CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle(getString(R.string.price_history));
        setDisplayHomeAsUpEnabled(true);
    }

    private void readParameters() {
        Intent intent = getIntent();
        if (intent == null) return;

        model.accountId = intent.getLongExtra(EditPriceDialog.ARG_ACCOUNT, Constants.NOT_SET);
        model.symbol = intent.getStringExtra(EditPriceDialog.ARG_SYMBOL);

        String priceString = intent.getStringExtra(EditPriceDialog.ARG_PRICE);
        model.price = MoneyFactory.fromString(priceString);
        mStockCurrentPrice = model.price;

        String dateString = intent.getStringExtra(EditPriceDialog.ARG_DATE);
        model.date = new MmxDate(dateString);
        mInitialDateIso = model.date.toIsoDateString();

        model.currencyId = intent.getLongExtra(ARG_CURRENCY_ID, Constants.NOT_SET);
    }

    private void loadStockName() {
        if (stockNameTextView == null || model == null || model.symbol == null) {
            return;
        }

        executor.execute(() -> {
            try {
                StockRepository repo = new StockRepository(this);
                List<Stock> stocks = repo.loadBySymbol(model.symbol);
                Stock stock = (stocks == null || stocks.isEmpty()) ? null : stocks.get(0);
                String stockName = stock != null && stock.getName() != null && !stock.getName().isEmpty()
                        ? stock.getName()
                        : model.symbol;
                runOnUiThread(() -> stockNameTextView.setText(stockName));
            } catch (Exception e) {
                Timber.e(e, "Error loading stock name");
                runOnUiThread(() -> stockNameTextView.setText(model.symbol));
            }
        });
    }

    private void setupHistoryRecyclerView() {
        priceHistoryRecyclerView = findViewById(R.id.priceHistoryRecyclerView);
        historyAdapter = new StockHistoryAdapter(this, (date, price) -> {
            model.date = new MmxDate(date);
            model.price = price;
            model.display(this, viewHolder);
            selectChartDate(date);
        }, (item, position) -> {
            // confirm deletion then delete in background; if the row is synthetic (no DB entry) remove from UI
            new AlertDialog.Builder(PriceEditActivity.this)
                    .setMessage(R.string.confirmDelete)
                    .setPositiveButton(android.R.string.ok, (d, w) -> executor.execute(() -> {
                        try {
                            String isoDate = item.getString(StockHistory.DATE);
                            StockHistoryRepository repo = new StockHistoryRepository(PriceEditActivity.this);
                            // check if a DB record exists for that date
                            StockHistory dbEntry = repo.getPriceForDate(model.symbol, isoDate);
                            if (dbEntry != null) {
                                long deleted = repo.deletePrice(model.symbol, isoDate);
                                if (deleted > 0) {
                                    runOnUiThread(() -> {
                                        loadHistory();
                                        loadHistoricalPriceForCurrentDate();
                                    });
                                }
                            } else {
                                // synthetic row: remove from adapter and, if visible, clear the editor price
                                runOnUiThread(() -> {
                                    historyAdapter.removeAt(position);
                                    renderPriceChart();
                                    if (isoDate.equals(model.date.toIsoDateString())) {
                                        model.price = MoneyFactory.fromDouble(0);
                                        model.display(PriceEditActivity.this, viewHolder);
                                    }
                                });
                            }
                        } catch (Exception e) {
                            Timber.e(e, "Error deleting price");
                        }
                    }))
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        });
        priceHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        priceHistoryRecyclerView.setAdapter(historyAdapter);
        priceHistoryRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }

    private void setupPriceHistoryChart() {
        chartTextColor = new UIHelper(this).resolveAttribute(R.attr.chartTextColor);
        priceHistoryChart = findViewById(R.id.priceHistoryChart);
        Spinner chartPeriodSpinner = findViewById(R.id.priceChartPeriodSpinner);
        if (chartPeriodSpinner != null) {
            setupChartPeriodSpinner(chartPeriodSpinner);
        }

        if (priceHistoryChart == null) {
            return;
        }

        priceHistoryChart.setDescription("");
        priceHistoryChart.setNoDataText(getString(R.string.no_price_history));
        priceHistoryChart.setDrawGridBackground(false);
        priceHistoryChart.setDrawBorders(false);
        priceHistoryChart.setTouchEnabled(true);
        priceHistoryChart.setPinchZoom(false);
        priceHistoryChart.setScaleEnabled(true);
        priceHistoryChart.setDragEnabled(true);
        priceHistoryChart.setDoubleTapToZoomEnabled(false);

        Legend legend = priceHistoryChart.getLegend();
        if (legend != null) {
            legend.setEnabled(false);
        }

        XAxis xAxis = priceHistoryChart.getXAxis();
        if (xAxis != null) {
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setDrawGridLines(false);
            if (chartTextColor != -1) {
                xAxis.setTextColor(getResources().getColor(chartTextColor));
            }
        }

        YAxis rightAxis = priceHistoryChart.getAxisRight();
        if (rightAxis != null) {
            rightAxis.setEnabled(false);
        }

        YAxis leftAxis = priceHistoryChart.getAxisLeft();
        if (leftAxis != null) {
            if (chartTextColor != -1) {
                leftAxis.setTextColor(getResources().getColor(chartTextColor));
            }
            leftAxis.setStartAtZero(false);
        }

        // When the user selects a point in the chart, update the date picker and scroll the table.
        setupChartValueSelectedListener();
    }

    private void setupChartPeriodSpinner(Spinner chartPeriodSpinner) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                buildChartPeriodLabels());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        chartPeriodSpinner.setAdapter(adapter);
        suppressChartSelectionCallback = true;
        chartPeriodSpinner.setSelection(selectedChartPeriod.ordinal());
        chartPeriodSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                if (suppressChartSelectionCallback) {
                    suppressChartSelectionCallback = false;
                    return;
                }

                PriceChartPeriod[] values = PriceChartPeriod.values();
                if (position < 0 || position >= values.length) {
                    return;
                }
                selectedChartPeriod = values[position];
                if (selectedChartPeriod == PriceChartPeriod.CUSTOM_RANGE) {
                    pickCustomRange();
                    return;
                }
                renderPriceChart();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Intentionally no-op: no action required when nothing is selected.
            }
        });
    }

    private void setupChartValueSelectedListener() {
        if (priceHistoryChart == null) return;
        priceHistoryChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
                if (suppressChartValueSelected) return;
                if (e == null || currentChartIsoDates == null) return;

                int index = e.getXIndex();
                if (index < 0 || index >= currentChartIsoDates.size()) return;

                String isoDate = currentChartIsoDates.get(index);
                model.date = new MmxDate(isoDate);
                runOnUiThread(() -> {
                    model.display(PriceEditActivity.this, viewHolder);
                    loadHistoricalPriceForCurrentDate();
                    scrollToCurrentDate();
                });
            }

            // Some MPAndroidChart versions use a different signature (Entry, Highlight).
            public void onValueSelected(Entry e, Highlight h) {
                onValueSelected(e, 0, h);
            }

            public void onNothingSelected() {
                // Intentionally no-op: no action required when nothing is selected.
            }
        });
    }

    private ArrayList<String> buildChartPeriodLabels() {
        ArrayList<String> labels = new ArrayList<>();
        for (PriceChartPeriod period : PriceChartPeriod.values()) {
            labels.add(getString(period.labelResId));
        }
        return labels;
    }

    private void pickCustomRange() {
        Date from = selectedCustomRange != null ? selectedCustomRange.dateFrom : null;
        Date to = selectedCustomRange != null ? selectedCustomRange.dateTo : null;

        ReportDateRangeSupport.showCustomDateDialog(this, from, to, (fromDate, toDate) -> {
            selectedCustomRange = new DateRange(fromDate, toDate);
            renderPriceChart();
        });
    }

    private void renderPriceChart() {
        if (priceHistoryChart == null) {
            return;
        }

        List<StockHistory> filteredHistory = getHistoryForSelectedPeriod();
        if (filteredHistory.isEmpty()) {
            priceHistoryChart.clear();
            priceHistoryChart.invalidate();
            return;
        }

        filteredHistory.sort(Comparator.comparing(StockHistory::getDate));

        ArrayList<String> xValues = new ArrayList<>();
        ArrayList<Entry> entries = new ArrayList<>();
        buildChartLists(filteredHistory, xValues, entries);

        if (entries.isEmpty()) {
            priceHistoryChart.clear();
            priceHistoryChart.invalidate();
            return;
        }

        applyDataSetToChart(xValues, entries);
    }

    private void selectChartDate(Date date) {
        if (priceHistoryChart == null || priceHistoryChart.getData() == null || date == null) {
            return;
        }

        String isoDate = new MmxDate(date).toIsoDateString();
        int position = currentChartIsoDates.indexOf(isoDate);
        if (position < 0) {
            return;
        }

        // Suppress the chart's selection callback while we programmatically highlight.
        suppressChartValueSelected = true;
        priceHistoryChart.highlightValue(position, 0, false);
        priceHistoryChart.invalidate();
        priceHistoryChart.post(() -> suppressChartValueSelected = false);
    }

    private List<StockHistory> getHistoryForSelectedPeriod() {
        if (historyItems == null || historyItems.isEmpty()) {
            return new ArrayList<>();
        }

        if (selectedChartPeriod == PriceChartPeriod.ALL_TIME) {
            return new ArrayList<>(historyItems);
        }

        if (selectedChartPeriod == PriceChartPeriod.CUSTOM_RANGE) {
            if (selectedCustomRange == null || selectedCustomRange.dateFrom == null || selectedCustomRange.dateTo == null) {
                return new ArrayList<>(historyItems);
            }
            return filterHistoryByRange(selectedCustomRange.dateFrom, selectedCustomRange.dateTo);
        }

        String latestIsoDate = getLatestHistoryDateIso(historyItems);
        if (latestIsoDate == null) {
            return new ArrayList<>();
        }

        Date[] range = computeStartEndForPeriod(selectedChartPeriod, latestIsoDate);
        if (range == null) {
            return new ArrayList<>();
        }

        return filterHistoryByRange(range[0], range[1]);
    }

    private Date[] computeStartEndForPeriod(PriceChartPeriod period, String latestIsoDate) {
        if (latestIsoDate == null) return null;
        MmxDate endDate = new MmxDate(latestIsoDate);
        MmxDate startDate = new MmxDate(latestIsoDate);

        switch (period) {
            case LAST_30_DAYS:
                startDate = startDate.minusDays(29);
                break;
            case LAST_3_MONTHS:
                startDate = startDate.minusMonths(3).firstDayOfMonth();
                break;
            case LAST_6_MONTHS:
                startDate = startDate.minusMonths(6).firstDayOfMonth();
                break;
            case CURRENT_YEAR:
                startDate = startDate.firstMonthOfYear().firstDayOfMonth();
                break;
            case ALL_TIME:
            default:
                // ALL_TIME handled elsewhere; return full range
                break;
        }

        return new Date[]{startDate.toDate(), endDate.toDate()};
    }

    private List<StockHistory> filterHistoryByRange(Date fromDate, Date toDate) {
        String startIsoDate = new MmxDate(fromDate).toIsoDateString();
        String endIsoDate = new MmxDate(toDate).toIsoDateString();

        ArrayList<StockHistory> result = new ArrayList<>();
        for (StockHistory history : historyItems) {
            String historyIsoDate = history.getString(StockHistory.DATE);
            if (historyIsoDate == null) {
                continue;
            }

            if (historyIsoDate.compareTo(startIsoDate) >= 0 && historyIsoDate.compareTo(endIsoDate) <= 0) {
                result.add(history);
            }
        }

        return result;
    }

    private String getLatestHistoryDateIso(List<StockHistory> data) {
        for (StockHistory history : data) {
            if (history == null) {
                continue;
            }
            String isoDate = history.getString(StockHistory.DATE);
            if (isoDate != null) {
                return isoDate;
            }
        }
        return null;
    }

    private void scrollToCurrentDate() {
        String isoDate = model.date.toIsoDateString();
        int position = historyAdapter.findPositionForDate(isoDate);
        if (position >= 0) {
            priceHistoryRecyclerView.post(() -> priceHistoryRecyclerView.smoothScrollToPosition(position));
        }
    }

    private void loadHistoricalPriceForCurrentDate() {
        String isoDate = model.date.toIsoDateString();
        executor.execute(() -> {
            try {
                StockHistory history = historyRepository.getPriceForDate(model.symbol, isoDate);
                if (history != null) {
                    String valueStr = history.getString(StockHistory.VALUE);
                    if (valueStr != null) {
                        model.price = MoneyFactory.fromString(valueStr);
                        runOnUiThread(() -> model.display(this, viewHolder));
                    }
                } else if (isoDate.equals(mInitialDateIso) && mStockCurrentPrice != null) {
                    // No history entry for today: restore the stock's current price.
                    model.price = mStockCurrentPrice;
                    runOnUiThread(() -> model.display(this, viewHolder));
                }
            } catch (Exception e) {
                Timber.e(e, "Error loading historical price for date %s", isoDate);
            }
        });
    }

    private void loadHistory() {
        executor.execute(() -> {
            try {
                List<StockHistory> history = historyRepository.getAllPricesForSymbol(model.symbol);
                if (mInitialDateIso != null && mStockCurrentPrice != null) {
                    boolean hasTodayEntry = false;
                    for (StockHistory entry : history) {
                        if (mInitialDateIso.equals(entry.getString(StockHistory.DATE))) {
                            hasTodayEntry = true;
                            break;
                        }
                    }
                    if (!hasTodayEntry) {
                        ContentValues cv = new ContentValues();
                        cv.put(StockHistory.SYMBOL, model.symbol);
                        cv.put(StockHistory.DATE, mInitialDateIso);
                        cv.put(StockHistory.VALUE, mStockCurrentPrice.toString());
                        history.add(0, new StockHistory(cv));
                    }
                }
                runOnUiThread(() -> {
                    historyItems = history;
                    historyAdapter.setData(history);
                    renderPriceChart();
                    scrollToCurrentDate();
                });
            } catch (Exception e) {
                Timber.e(e, "Error loading price history");
            }
        });
    }

    private void onDownloadPricesClick() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.download_prices_explanation)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> showStartDatePicker())
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void onDownloadSelectedDatePriceClick() {
        if (model == null || model.date == null) {
            return;
        }
        Toast.makeText(this, R.string.starting_price_update, Toast.LENGTH_SHORT).show();
        StockPriceRepository priceRepository = new StockPriceRepository(getApplication());
        priceRepository.downloadPriceForDate(model.symbol, model.date.toDate())
                .observe(this, count -> {
                    if (count == null) return;
                    if (count < 0) {
                        Toast.makeText(this, R.string.error_downloading_symbol, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this,
                                getString(R.string.prices_downloaded, count),
                                Toast.LENGTH_SHORT).show();
                        loadHistory();
                        loadHistoricalPriceForCurrentDate();
                    }
                });
    }

    private void showStartDatePicker() {
        MmxDate defaultFrom = new MmxDate().minusDays(30);
        DatePickerDialog picker = new DatePickerDialog(
                this,
                (view, year, month, day) -> {
                    MmxDate fromDate = new MmxDate(year, month, day);
                    downloadPrices(fromDate, new MmxDate());
                },
                defaultFrom.getYear(),
                defaultFrom.getMonthOfYear(),
                defaultFrom.getDayOfMonth()
        );
        picker.setTitle(getString(R.string.from_date));
        picker.show();
    }

    private void downloadPrices(MmxDate fromDate, MmxDate toDate) {
        Toast.makeText(this, R.string.starting_price_update, Toast.LENGTH_SHORT).show();
        StockPriceRepository priceRepository = new StockPriceRepository(getApplication());
        priceRepository.downloadPriceHistory(model.symbol, fromDate.toDate(), toDate.toDate())
                .observe(this, count -> {
                    if (count == null) return;
                    if (count < 0) {
                        Toast.makeText(this, R.string.error_downloading_symbol, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this,
                                getString(R.string.prices_downloaded, count),
                                Toast.LENGTH_SHORT).show();
                        loadHistory();
                        loadHistoricalPriceForCurrentDate();
                    }
                });
    }

    private void onAddClick() {
        if (save()) {
            loadHistory();
            loadHistoricalPriceForCurrentDate();
            scrollToCurrentDate();
        }
    }

    private boolean save() {
        if (isSelectedDateToday()) {
            StockRepository repo = new StockRepository(this);
            repo.updateCurrentPrice(model.symbol, model.price);
        }

        StockHistoryRepository historyRepository = new StockHistoryRepository(this);
        boolean result = historyRepository.addStockHistoryRecord(model);
        if (!result) {
            Toast.makeText(this, getString(R.string.error_update_currency_exchange_rate),
                    Toast.LENGTH_SHORT).show();
        }
        return result;
    }

    private boolean isSelectedDateToday() {
        return model != null
                && model.date != null
                && model.date.toIsoDateString().equals(new MmxDate().toIsoDateString());
    }

    private enum PriceChartPeriod {
        LAST_30_DAYS(R.string.last30days),
        LAST_3_MONTHS(R.string.last3months),
        LAST_6_MONTHS(R.string.last6months),
        CURRENT_YEAR(R.string.current_year),
        ALL_TIME(R.string.all_time),
        CUSTOM_RANGE(R.string.custom_dates);

        private final int labelResId;

        PriceChartPeriod(int labelResId) {
            this.labelResId = labelResId;
        }

        private static PriceChartPeriod fromName(String name) {
            for (PriceChartPeriod period : values()) {
                if (period.name().equals(name)) {
                    return period;
                }
            }
            return LAST_6_MONTHS;
        }
    }
}
