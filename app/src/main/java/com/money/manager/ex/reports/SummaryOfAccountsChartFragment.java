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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.money.manager.ex.R;
import com.money.manager.ex.core.UIHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SummaryOfAccountsChartFragment extends Fragment {

    public static final String KEY_X_TITLES = "SummaryOfAccountsChart:XTitles";
    public static final String KEY_STACK_LABELS = "SummaryOfAccountsChart:StackLabels";
    public static final String KEY_STACK_VALUES = "SummaryOfAccountsChart:StackValues";

    private static final int[] COLORS = {
            R.color.material_red_500,
            R.color.material_deep_purple_500,
            R.color.material_light_blue_500,
            R.color.material_green_500,
            R.color.material_yellow_500,
            R.color.material_deep_orange_500,
            R.color.material_blue_grey_500,
            R.color.material_pink_500,
            R.color.material_indigo_500,
            R.color.material_cyan_500,
            R.color.material_light_green_500,
            R.color.material_amber_500,
            R.color.material_brown_500,
            R.color.material_purple_500,
            R.color.material_blue_500,
            R.color.material_teal_500,
            R.color.material_lime_500,
            R.color.material_orange_500,
            R.color.material_grey_500
    };

    private CombinedChart chart;
    private int textColor;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        textColor = new UIHelper(getActivity()).resolveAttribute(R.attr.chartTextColor);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.chart_combined_fragment, container, false);
        chart = rootView.findViewById(R.id.chartBar);
        chart.setDescription("");
        chart.setPinchZoom(false);
        chart.setDrawBarShadow(false);
        chart.setDrawGridBackground(false);
        chart.setDrawValueAboveBar(true);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        buildChart();
    }

    private void buildChart() {
        ChartInput chartInput = readChartInput();
        if (chartInput == null) {
            return;
        }

        BarData barData = buildBarData(chartInput);
        LineData lineData = buildLineData(chartInput);

        CombinedData combinedData = new CombinedData(Arrays.asList(chartInput.xTitles));
        combinedData.setData(barData);
        combinedData.setData(lineData);
        chart.setData(combinedData);
        chart.animateXY(1200, 1200);
        chart.invalidate();

        styleChartAxes(chartInput.xTitles);
        styleChartLegend();
    }

    @Nullable
    private ChartInput readChartInput() {
        Bundle args = getArguments();
        if (args == null) {
            return null;
        }

        String[] xTitles = args.getStringArray(KEY_X_TITLES);
        String[] stackLabels = args.getStringArray(KEY_STACK_LABELS);
        Object rawValues = args.getSerializable(KEY_STACK_VALUES);
        if (xTitles == null || stackLabels == null || !(rawValues instanceof float[][])) {
            return null;
        }

        return new ChartInput(xTitles, stackLabels, (float[][]) rawValues);
    }

    private BarData buildBarData(ChartInput chartInput) {
        ArrayList<BarEntry> entries = createEntries(chartInput);
        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setStackLabels(chartInput.stackLabels);
        dataSet.setColors(createDataSetColors(chartInput.stackLabels.length));

        List<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(dataSet);

        BarData data = new BarData(chartInput.xTitles, dataSets);
        if (textColor != -1) {
            data.setValueTextColor(getResources().getColor(textColor));
        }
        return data;
    }

    private LineData buildLineData(ChartInput chartInput) {
        ArrayList<Entry> entries = createLineEntries(chartInput);
        LineDataSet dataSet = new LineDataSet(entries, getString(R.string.total));
        dataSet.setColor(getResources().getColor(R.color.material_blue_grey_500));
        dataSet.setLineWidth(2f);
        dataSet.setDrawCircles(true);
        dataSet.setCircleRadius(4f);
        dataSet.setCircleColor(getResources().getColor(R.color.material_blue_grey_500));
        dataSet.setDrawValues(true);
        if (textColor != -1) {
            dataSet.setValueTextColor(getResources().getColor(textColor));
        }

        List<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(dataSet);

        return new LineData(Arrays.asList(chartInput.xTitles), dataSets);
    }

    private ArrayList<Entry> createLineEntries(ChartInput chartInput) {
        ArrayList<Entry> entries = new ArrayList<>();
        for (int i = 0; i < chartInput.xTitles.length; i++) {
            float[] values = i < chartInput.stackValues.length
                    ? chartInput.stackValues[i]
                    : new float[0];
            float total = 0f;
            for (float value : values) {
                total += value;
            }
            entries.add(new Entry(total, i));
        }
        return entries;
    }

    private ArrayList<BarEntry> createEntries(ChartInput chartInput) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < chartInput.xTitles.length; i++) {
            float[] values = i < chartInput.stackValues.length
                    ? chartInput.stackValues[i]
                    : new float[chartInput.stackLabels.length];
            entries.add(new BarEntry(values, i));
        }
        return entries;
    }

    private ArrayList<Integer> createDataSetColors(int stackCount) {
        ArrayList<Integer> colors = new ArrayList<>();
        for (int i = 0; i < stackCount; i++) {
            colors.add(getResources().getColor(COLORS[i % COLORS.length]));
        }
        return colors;
    }

    private void styleChartAxes(String[] xTitles) {
        XAxis xAxis = chart.getXAxis();
        if (xAxis != null) {
            if (textColor != -1) {
                xAxis.setTextColor(getResources().getColor(textColor));
            }
            xAxis.setLabelsToSkip(Math.max(0, xTitles.length / 8));
        }

        YAxis left = chart.getAxisLeft();
        if (left != null && textColor != -1) {
            left.setTextColor(getResources().getColor(textColor));
        }

        YAxis right = chart.getAxisRight();
        if (right != null && textColor != -1) {
            right.setTextColor(getResources().getColor(textColor));
        }
    }

    private void styleChartLegend() {
        Legend legend = chart.getLegend();
        if (legend == null) {
            return;
        }

        if (textColor != -1) {
            legend.setTextColor(getResources().getColor(textColor));
        }

        // Allow multi-row legends when many account labels are present.
        legend.setWordWrapEnabled(true);
        legend.setMaxSizePercent(0.95f);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setYEntrySpace(4f);
    }

    private static class ChartInput {
        private final String[] xTitles;
        private final String[] stackLabels;
        private final float[][] stackValues;

        ChartInput(String[] xTitles, String[] stackLabels, float[][] stackValues) {
            this.xTitles = xTitles;
            this.stackLabels = stackLabels;
            this.stackValues = stackValues;
        }
    }
}
