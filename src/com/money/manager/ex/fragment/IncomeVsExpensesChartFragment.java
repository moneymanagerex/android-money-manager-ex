package com.money.manager.ex.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.SparseArrayCompat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

import com.money.manager.ex.chart.Chart;

public class IncomeVsExpensesChartFragment extends Fragment {
	// LOGCAT
	@SuppressWarnings("unused")
	private static final String LOGCAT = IncomeVsExpensesChartFragment.class.getSimpleName();
	// key arguments
	public static final String KEY_EXPENSES_VALUES = "IncomeExpensesChartFragment:ExpensesValues";
	public static final String KEY_INCOME_VALUES = "IncomeExpensesChartFragment:IncomeValues";
	public static final String KEY_TITLE = "IncomeExpensesChartFragment:Title";
	public static final String KEY_XTITLES = "IncomeExpensesChartFragment:XTitles";
	public static final String KEY_SAVED_INSTANCE = "IncomeExpensesChartFragment:SavedInstance";
	public static final String KEY_DISPLAY_AS_UP_ENABLED = "IncomeExpensesChartFragment:DisplayHomeAsUpEnabled";
	// bundle arguments
	private Bundle mArguments;
	// layout
	private LinearLayout mLayout;
	// show back home
	private boolean mDisplayHomeAsUpEnabled = false;
	
	public View buildChart() {
		// compose sparse array titles
		SparseArrayCompat<String> xTitles = new SparseArrayCompat<String>();
		if (getChartArguments().containsKey(KEY_XTITLES)) {
			String[] titles = getChartArguments().getStringArray(KEY_XTITLES);
			int xLabelDistance = titles.length / (titles.length < 12 ? titles.length : 12);
			int nextXLabelPrint = 0;
			for(int i = 0; i < titles.length; i ++) {
				if (i == nextXLabelPrint) {
					//xTitles.put(i, titles[i]);
					xTitles.append(i, titles[i]);
					nextXLabelPrint += xLabelDistance;
				} else {
					xTitles.append(i, null);
				}
			}
		}
		// create a chart
		Chart chart = new Chart();
		
		//debug
		double[] income = getChartArguments().getDoubleArray(KEY_INCOME_VALUES);
		double[] expenses = getChartArguments().getDoubleArray(KEY_EXPENSES_VALUES);
		
		return chart.buildIncomeExpensesChart(getActivity(), getChartArguments().getString(KEY_TITLE), income, expenses, xTitles);
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
