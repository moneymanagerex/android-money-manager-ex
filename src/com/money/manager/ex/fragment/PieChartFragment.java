package com.money.manager.ex.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

import com.money.manager.ex.chart.Chart;
import com.money.manager.ex.chart.ValuePieChart;

import java.util.ArrayList;

public class PieChartFragment extends Fragment {
	// LOGCAT
	@SuppressWarnings("unused")
	private static final String LOGCAT = PieChartFragment.class.getSimpleName();
	// key arguments
	public static final String KEY_TITLE = "PieChartFragment:Title";
	public static final String KEY_CATEGORIES_VALUES = "PieChartFragment:CategoriesValues";
	public static final String KEY_SAVED_INSTANCE = "PieChartFragment:SavedInstance";
	public static final String KEY_DISPLAY_AS_UP_ENABLED = "PieChartFragment:DisplayHomeAsUpEnabled";
	// bundle arguments
	private Bundle mArguments;
	// layout
	private LinearLayout mLayout;
	// show back home
	private boolean mDisplayHomeAsUpEnabled = false;
	
	public View buildChart() {
		// compose sparse array titles
		@SuppressWarnings("unchecked")
		ArrayList<ValuePieChart> pieCharts = (ArrayList<ValuePieChart>) mArguments.getSerializable(KEY_CATEGORIES_VALUES);
		// create a chart
		Chart chart = new Chart();
		
		return chart.buildPieChart(getActivity(), mArguments.getString(KEY_TITLE), pieCharts);
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
		// enabled display as home
		getActivity().getActionBar().setDisplayHomeAsUpEnabled(isDisplayHomeAsUpEnabled());
		// set has option menu
		setHasOptionsMenu(true);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		//return buildChart();
		mLayout = new LinearLayout(getActivity());
		mLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

		return mLayout;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if (mLayout != null) {
			mLayout.removeAllViews();
			mLayout.addView(buildChart());
		}
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
	
}
