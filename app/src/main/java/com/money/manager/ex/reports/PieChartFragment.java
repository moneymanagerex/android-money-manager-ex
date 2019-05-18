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

import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.money.manager.ex.R;
import com.money.manager.ex.core.UIHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import androidx.fragment.app.Fragment;
import timber.log.Timber;

public class PieChartFragment
        extends Fragment
        implements OnChartValueSelectedListener {

    private static final String LOGCAT = PieChartFragment.class.getSimpleName();
    private static final Integer MAX_NUM_ITEMS = 12;
    // key arguments
//    public static final String KEY_TITLE = "PieChartFragment:Title";
    public static final String KEY_CATEGORIES_VALUES = "PieChartFragment:CategoriesValues";
    public static final String KEY_SAVED_INSTANCE = "PieChartFragment:SavedInstance";
    public static final String KEY_DISPLAY_AS_UP_ENABLED = "PieChartFragment:DisplayHomeAsUpEnabled";
    // color
    private static final int COLORS[] = {R.color.material_red_500, R.color.material_deep_purple_500, R.color.material_light_blue_500,
            R.color.material_green_500, R.color.material_yellow_500, R.color.material_deep_orange_500, R.color.material_blue_grey_500,
            R.color.material_pink_500, R.color.material_indigo_500, R.color.material_cyan_500, R.color.material_light_green_500,
            R.color.material_amber_500, R.color.material_brown_500, R.color.material_purple_500, R.color.material_blue_500,
            R.color.material_teal_500, R.color.material_lime_500, R.color.material_orange_500, R.color.material_grey_500};
    // bundle arguments
    private Bundle mArguments;
    // layout
    private LinearLayout mLayout;
    private PieChart mChart;
    ArrayList<ValuePieEntry> mPieCharts;
    private int mTextColor;
    // show back home
    private boolean mDisplayHomeAsUpEnabled = false;

    public void buildChart() {
        mPieCharts = (ArrayList<ValuePieEntry>) mArguments.getSerializable(KEY_CATEGORIES_VALUES);
        //sort pie charts
        Collections.sort(mPieCharts, new Comparator<ValuePieEntry>() {

            @Override
            public int compare(ValuePieEntry lhs, ValuePieEntry rhs) {
                return lhs.getValue() > rhs.getValue() ? -1 : lhs.getValue() == rhs.getValue() ? 0 : 1;
            }
        });
        ArrayList<PieEntry> entries = new ArrayList<>();

        int length = mPieCharts.size() < MAX_NUM_ITEMS ? mPieCharts.size() : MAX_NUM_ITEMS;

        for (int i = 0; i < length; i++) {
            entries.add(new PieEntry((float) mPieCharts.get(i).getValue(),
                                      mPieCharts.get(i).getText()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setSliceSpace(3f);

        ArrayList<Integer> colors = new ArrayList<>();

        UIHelper uiHelper = new UIHelper(getActivity());
        for (int c : COLORS)
            colors.add(uiHelper.getColor(c));

        dataSet.setColors(colors);
        PieData data = new PieData(dataSet);
        // MPAndroidChart#2124
        data.setValueFormatter(new PercentFormatter(mChart));
        mChart.setUsePercentValues(true);

        if (mTextColor != -1)
            data.setValueTextColor(uiHelper.getColor(mTextColor));

        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);
        mChart.setData(data);

        // undo all highlights
        mChart.highlightValues(null);
        mChart.invalidate();

        Legend l = mChart.getLegend();
        if (l != null) {
            l.setEnabled(false);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTextColor = new UIHelper(getActivity()).resolveAttribute(R.attr.chartTextColor);
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(KEY_SAVED_INSTANCE))
                setChartArguments(savedInstanceState.getBundle(KEY_SAVED_INSTANCE));
            if (savedInstanceState.containsKey(KEY_DISPLAY_AS_UP_ENABLED))
                setDisplayHomeAsUpEnabled(savedInstanceState.getBoolean(KEY_DISPLAY_AS_UP_ENABLED));
        }
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
        mLayout = (LinearLayout) inflater.inflate(R.layout.chart_pie_fragment, container, false);

        mChart = (PieChart) mLayout.findViewById(R.id.chartPie);
        mChart.setUsePercentValues(true);

        // change the color of the center-hole
        mChart.setHoleColor(Color.TRANSPARENT);

        mChart.setHoleRadius(30f);
        mChart.getDescription().setEnabled(false);

        mChart.setDrawCenterText(true);

        mChart.setDrawHoleEnabled(true);

        mChart.setRotationAngle(0);
        // enable rotation of the chart by touch
        mChart.setRotationEnabled(true);

        // mChart.setUnit(" €");
        // mChart.setDrawUnitsInChart(true);

        // add a selection listener
        mChart.setOnChartValueSelectedListener(this);
        // mChart.setTouchEnabled(false);

        //mChart.setCenterText("MPAndroidChart\nLibrary");

        mChart.animateXY(1500, 1500);

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
        String text;
        try {
            text = mPieCharts.get((int) h.getX()).getText().concat(": ").concat(mPieCharts.get((int) h.getX()).getValueFormatted());
//            Snackbar.with(getActivity().getApplicationContext()) // context
//                    .text(text)
//                    .duration(Snackbar.SnackbarDuration.LENGTH_LONG)
//                    .show(getActivity());
            Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show();
        } catch (Exception ex) {
            Timber.e(ex, "showing chart item details");
        }
    }

    @Override
    public void onNothingSelected() {

    }
}
