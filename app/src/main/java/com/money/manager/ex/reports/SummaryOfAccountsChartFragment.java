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
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.money.manager.ex.R;
import com.money.manager.ex.core.UIHelper;

import java.util.ArrayList;
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

    private LinearLayout layout;
    private BarChart chart;
    private int textColor;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        textColor = new UIHelper(getActivity()).resolveAttribute(R.attr.chartTextColor);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layout = (LinearLayout) inflater.inflate(R.layout.chart_bar_fragment, container, false);
        chart = layout.findViewById(R.id.chartBar);
        chart.setDescription("");
        chart.setPinchZoom(false);
        chart.setDrawBarShadow(false);
        chart.setDrawGridBackground(false);
        chart.setDrawValueAboveBar(true);
        return layout;
    }

    @Override
    public void onResume() {
        super.onResume();
        buildChart();
    }

    private void buildChart() {
        Bundle args = getArguments();
        if (args == null) {
            return;
        }

        String[] xTitles = args.getStringArray(KEY_X_TITLES);
        String[] stackLabels = args.getStringArray(KEY_STACK_LABELS);
        Object rawValues = args.getSerializable(KEY_STACK_VALUES);

        if (xTitles == null || stackLabels == null || !(rawValues instanceof float[][])) {
            return;
        }

        float[][] stackValues = (float[][]) rawValues;
        ArrayList<BarEntry> entries = new ArrayList<>();

        for (int i = 0; i < xTitles.length; i++) {
            float[] values = i < stackValues.length ? stackValues[i] : new float[stackLabels.length];
            entries.add(new BarEntry(values, i));
        }

        BarDataSet dataSet = new BarDataSet(entries, getString(R.string.menu_report_summary_of_accounts));
        dataSet.setStackLabels(stackLabels);

        ArrayList<Integer> colors = new ArrayList<>();
        for (int i = 0; i < stackLabels.length; i++) {
            colors.add(getResources().getColor(COLORS[i % COLORS.length]));
        }
        dataSet.setColors(colors);

        List<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(dataSet);

        BarData data = new BarData(xTitles, dataSets);
        if (textColor != -1) {
            data.setValueTextColor(getResources().getColor(textColor));
        }
        chart.setData(data);
        chart.animateXY(1200, 1200);
        chart.invalidate();

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

        Legend legend = chart.getLegend();
        if (legend != null && textColor != -1) {
            legend.setTextColor(getResources().getColor(textColor));
        }
    }
}
