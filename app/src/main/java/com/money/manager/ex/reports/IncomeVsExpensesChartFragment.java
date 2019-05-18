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

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.money.manager.ex.R;
import com.money.manager.ex.core.UIHelper;

import java.util.ArrayList;
import java.util.List;

public class IncomeVsExpensesChartFragment
    extends Fragment
    implements OnChartValueSelectedListener {

//    private static final String LOGCAT = IncomeVsExpensesChartFragment.class.getSimpleName();

    public static final String KEY_EXPENSES_VALUES = "IncomeExpensesChartFragment:ExpensesValues";
    public static final String KEY_INCOME_VALUES = "IncomeExpensesChartFragment:IncomeValues";
    public static final String KEY_TITLE = "IncomeExpensesChartFragment:Title";
    public static final String KEY_XTITLES = "IncomeExpensesChartFragment:XTitles";
    public static final String KEY_SAVED_INSTANCE = "IncomeExpensesChartFragment:SavedInstance";
    public static final String KEY_DISPLAY_AS_UP_ENABLED = "IncomeExpensesChartFragment:DisplayHomeAsUpEnabled";

    private Bundle mArguments;

    private int mTextColor;

    private LinearLayout mLayout;
    private BarChart mChart;

    private boolean mDisplayHomeAsUpEnabled = false;

    public void buildChart() {
        String[] xVals = getChartArguments().getStringArray(KEY_XTITLES);
        double[] incomes = getChartArguments().getDoubleArray(KEY_INCOME_VALUES);
        double[] expenses = getChartArguments().getDoubleArray(KEY_EXPENSES_VALUES);

        ArrayList<BarEntry> yIncomes = new ArrayList<>();
        ArrayList<BarEntry> yExpenses = new ArrayList<>();

        for (int i = 0; i < xVals.length; i++) {
            yIncomes.add(new BarEntry(i, (float) incomes[i]));
            yExpenses.add(new BarEntry(i, (float) expenses[i]));
        }

        BarDataSet dataSetIncomes = new BarDataSet(yIncomes, getString(R.string.income));
        BarDataSet dataSetExpenses = new BarDataSet(yExpenses, getString(R.string.expenses));

        UIHelper uiHelper = new UIHelper(getActivity());
        dataSetExpenses.setColor(uiHelper.getColor(R.color.material_red_500));
        dataSetIncomes.setColor(uiHelper.getColor(R.color.material_green_500));

        // https://weeklycoding.com/mpandroidchart-documentation/setting-data/
        float groupSpace = 0.06f;
        float barSpace = 0.02f;  // x2 dataset
        float barWidth = 0.45f;  // x2 dataset
        // (0.02 + 0.45) * 2 + 0.06 = 1.00 -> interval per "group"

        BarData data = new BarData(dataSetIncomes, dataSetExpenses);
        data.setBarWidth(barWidth);
        if (mTextColor != -1)
            data.setValueTextColor(uiHelper.getColor(mTextColor));
        mChart.setData(data);
        mChart.groupBars(0, groupSpace, barSpace);
        mChart.animateXY(1500, 1500);
        mChart.invalidate();

        Legend l = mChart.getLegend();
        if (l != null && mTextColor != -1)
            l.setTextColor(uiHelper.getColor(mTextColor));

        // x labels
        XAxis xAxis = mChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setCenterAxisLabels(true);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xVals));
        // https://stackoverflow.com/questions/35240289/how-to-create-a-barchart-with-grouped-bars-with-mpandroidchart
        xAxis.setAxisMinimum(0);
        xAxis.setAxisMaximum((float) xVals.length);
        if (xAxis != null && mTextColor != -1)
            xAxis.setTextColor(uiHelper.getColor(mTextColor));
        // right label
        YAxis yAxis = mChart.getAxisRight();
        if (yAxis != null && mTextColor != -1)
            yAxis.setTextColor(uiHelper.getColor(mTextColor));
        // left label
        yAxis = mChart.getAxisLeft();
        if (yAxis != null && mTextColor != -1)
            yAxis.setTextColor(uiHelper.getColor(mTextColor));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(KEY_SAVED_INSTANCE))
                setChartArguments(savedInstanceState.getBundle(KEY_SAVED_INSTANCE));
            if (savedInstanceState.containsKey(KEY_DISPLAY_AS_UP_ENABLED))
                setDisplayHomeAsUpEnabled(savedInstanceState.getBoolean(KEY_DISPLAY_AS_UP_ENABLED));
        }

        mTextColor = new UIHelper(getActivity()).resolveAttribute(R.attr.chartTextColor);

        // enabled display as home
//        ActionBarActivity activity = (ActionBarActivity) getActivity();
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null && activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(isDisplayHomeAsUpEnabled());
        }
        // set has option menu
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //return buildChart();
        mLayout = (LinearLayout) inflater.inflate(R.layout.chart_bar_fragment, container, false);

        mChart = (BarChart) mLayout.findViewById(R.id.chartBar);
        mChart.setOnChartValueSelectedListener(this);
        mChart.getDescription().setEnabled(false);

//      mChart.setDrawBorders(true);

        // scaling can now only be done on x- and y-axis separately
        mChart.setPinchZoom(false);

        mChart.setDrawBarShadow(false);

        mChart.setDrawGridBackground(false);

        return mLayout;
    }

    @Override
    public void onResume() {
        super.onResume();
        buildChart();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getActivity().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle(KEY_SAVED_INSTANCE, getChartArguments());
        outState.putBoolean(KEY_DISPLAY_AS_UP_ENABLED, isDisplayHomeAsUpEnabled());
    }

    /**
     * @return the mArguments
     */
    public Bundle getChartArguments() {
        return mArguments;
    }

    /**
     * @param mArguments the mArguments to set
     */
    public void setChartArguments(Bundle mArguments) {
        this.mArguments = mArguments;
    }

    /**
     * @return the mDisplayHomeAsUpEnabled
     */
    public boolean isDisplayHomeAsUpEnabled() {
        return mDisplayHomeAsUpEnabled;
    }

    /**
     * @param mDisplayHomeAsUpEnabled the mDisplayHomeAsUpEnabled to set
     */
    public void setDisplayHomeAsUpEnabled(boolean mDisplayHomeAsUpEnabled) {
        this.mDisplayHomeAsUpEnabled = mDisplayHomeAsUpEnabled;
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }
}
