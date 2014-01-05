/*******************************************************************************
 * Copyright (C) 2013 The Android Money Manager Ex Project
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 ******************************************************************************/
package com.money.manager.ex.reports;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TableRow;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.money.manager.ex.R;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.CurrencyUtils;
import com.money.manager.ex.database.MoneyManagerOpenHelper;
import com.money.manager.ex.database.QueryReportIncomeVsExpenses;
import com.money.manager.ex.database.ViewMobileData;
import com.money.manager.ex.fragment.BaseFragmentActivity;
import com.money.manager.ex.fragment.IncomeVsExpensesChartFragment;

public class IncomeVsExpensesActivity extends BaseFragmentActivity {
	private static final String LOGCAT = IncomeVsExpensesActivity.class.getSimpleName();
	
	private static class IncomeVsExpensesAdapter extends CursorAdapter {
		private LayoutInflater mInflater;
		
		@SuppressWarnings("deprecation")
		public IncomeVsExpensesAdapter(Context context, Cursor c) {
			super(context, c);
			mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			TextView txtYear = (TextView)view.findViewById(R.id.textViewYear);
			TextView txtMonth = (TextView)view.findViewById(R.id.textViewMonth);
			TextView txtIncome = (TextView)view.findViewById(R.id.textViewIncome);
			TextView txtExpenses = (TextView)view.findViewById(R.id.textViewExpenses);
			TextView txtDifference = (TextView)view.findViewById(R.id.textViewDifference);
			// take data
			int year, month;
			year = cursor.getInt(cursor.getColumnIndex(QueryReportIncomeVsExpenses.Year));
			month = cursor.getInt(cursor.getColumnIndex(QueryReportIncomeVsExpenses.Month));
			Calendar calendar = Calendar.getInstance();
			calendar.set(year, month - 1, 1);
			float income = 0, expenses = 0;
			expenses = cursor.getFloat(cursor.getColumnIndex(QueryReportIncomeVsExpenses.Expenses));
			income = cursor.getFloat(cursor.getColumnIndex(QueryReportIncomeVsExpenses.Income));
			// attach data
			txtYear.setText(Integer.toString(year));
			//txtMonth.setText(new SimpleDateFormat("MMMM").format(new Date(year, month - 1, 1)));
			String formatMonth = context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? "MMM" : "MMMM";
			
			txtMonth.setText(new SimpleDateFormat(formatMonth).format(calendar.getTime()));
			txtIncome.setText(currencyUtils.getCurrencyFormatted(currencyUtils.getBaseCurrencyId(), income));
			txtExpenses.setText(currencyUtils.getCurrencyFormatted(currencyUtils.getBaseCurrencyId(), Math.abs(expenses)));
			txtDifference.setText(currencyUtils.getCurrencyFormatted(currencyUtils.getBaseCurrencyId(), income - Math.abs(expenses)));
			Core core = new Core(context);
			if (income - Math.abs(expenses) < 0) {
				txtDifference.setTextColor(context.getResources().getColor(core.resolveIdAttribute(R.attr.holo_red_color_theme)));
			} else {
				txtDifference.setTextColor(context.getResources().getColor(core.resolveIdAttribute(R.attr.holo_green_color_theme)));
			}
			view.setBackgroundColor(core.resolveColorAttribute(cursor.getPosition() % 2 == 1 ? R.attr.row_dark_theme : R.attr.row_light_theme));
		}
		
		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			return mInflater.inflate(R.layout.tablerow_income_vs_expenses, parent, false);
		}
	}
	
	public static class IncomeVsExpensesListFragment extends SherlockListFragment implements LoaderManager.LoaderCallbacks<Cursor> { 
		private static final int ID_LOADER = 1;
		private static final String SORT_ASCENDING = "ASC";
		private static final String SORT_DESCENDING = "DESC";
		private static final String KEY_BUNDLE_YEAR = "IncomeVsExpensesListFragment:Years";
		private View mFooterListView;
		private Map<Integer, Boolean> mCheckedItem = new HashMap<Integer, Boolean>();
		private String mSort = SORT_ASCENDING;
		
		/**
		 * Add footer to ListView
		 * @return View of footer
		 */
		private View addListViewFooter() {
			TableRow row = (TableRow)View.inflate(getActivity(), R.layout.tablerow_income_vs_expenses, null);
			TextView txtYear = (TextView)row.findViewById(R.id.textViewYear);
			txtYear.setText(getString(R.string.total));
			txtYear.setTypeface(null, Typeface.BOLD_ITALIC);
			TextView txtMonth = (TextView)row.findViewById(R.id.textViewMonth);
			txtMonth.setText(null);
			return row;
		}

		/**
		 * Add header to ListView
		 */
		private View addListViewHeader() {
			TableRow row = (TableRow)View.inflate(getActivity(), R.layout.tablerow_income_vs_expenses, null);
			int[] ids = new int[] {R.id.textViewYear, R.id.textViewMonth, R.id.textViewIncome, R.id.textViewExpenses, R.id.textViewDifference};
			for(int id : ids) {
				TextView textView = (TextView)row.findViewById(id);
				textView.setTypeface(null, Typeface.BOLD);
				textView.setSingleLine(true);
			}
			getListView().addHeaderView(row);
			
			return row;
		}

		/**
		 * Convert from hash map<Integer, Boolean> to int[] 
		 * @param map
		 * @return
		 */
		private int[] hashMap2IntArray(Map<Integer, Boolean> map) {
			return hashMap2IntArray(map, false);
		}
		
		/**
		 * Convert from hash map<Integer, Boolean> to int[]
		 * @param map
		 * @param allItems
		 * @return
		 */
		private int[] hashMap2IntArray(Map<Integer, Boolean> map, boolean allItems) {
			int[] ret = new int[mCheckedItem.entrySet().size()];
			int i = 0;
			// compose arrays list
			for (Map.Entry<Integer, Boolean> entry : mCheckedItem.entrySet()) {
				if (allItems || entry.getValue()) {
					ret[i ++] = entry.getKey();
				}
			}
			return ret;
		}
		
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			setHasOptionsMenu(true);
			if (savedInstanceState != null && savedInstanceState.containsKey(KEY_BUNDLE_YEAR) && savedInstanceState.getIntArray(KEY_BUNDLE_YEAR) != null) {
				for(int item : savedInstanceState.getIntArray(KEY_BUNDLE_YEAR)) {
					mCheckedItem.put(item, true);
				}
			} else {
				mCheckedItem.put(Calendar.getInstance().get(Calendar.YEAR), true);
			}
			// set home button fase
			getSherlockActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(false);
			// set listview
			setEmptyText(getString(R.string.no_data));
			// add header and footer
			try { 
				setListAdapter(null);
				addListViewHeader();
				mFooterListView = addListViewFooter();
			} catch (Exception e) {
				Log.e(LOGCAT, e.getMessage());
			}
			// create adapter
			IncomeVsExpensesAdapter adapter = new IncomeVsExpensesAdapter(getActivity(), null);
			setListAdapter(adapter);
			setListShown(false);
			// start loader
			startLoader(hashMap2IntArray(mCheckedItem));
		}
		
		@Override
		public Loader<Cursor> onCreateLoader(int id, Bundle args) {
			String selection = null;
			QueryReportIncomeVsExpenses report = new QueryReportIncomeVsExpenses(getActivity());
			if (args != null && args.containsKey(KEY_BUNDLE_YEAR) && args.getIntArray(KEY_BUNDLE_YEAR) != null) {
				selection = "";
				for(int i = 0; i < args.getIntArray(KEY_BUNDLE_YEAR).length; i ++) {
					if (!TextUtils.isEmpty(selection)) {
						selection += " OR ";
					}
					selection += QueryReportIncomeVsExpenses.Year + "=" + Integer.toString(args.getIntArray(KEY_BUNDLE_YEAR)[i]);
				}
				if (!TextUtils.isEmpty(selection)) {
					selection = "(" + selection + ")";
				}
			}
			// if don't have selection abort query
			if (TextUtils.isEmpty(selection)) {
				selection = "1=2";
			}
			return new CursorLoader(getActivity(), report.getUri(), report.getAllColumns(), selection, null, QueryReportIncomeVsExpenses.Year + " DESC, " + QueryReportIncomeVsExpenses.Month  + " " + mSort);
		}
		
		@Override
		public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
			super.onCreateOptionsMenu(menu, inflater);
			inflater.inflate(R.menu.menu_report_income_vs_expenses, menu);
			//Create a cursor for select year
			MoneyManagerOpenHelper helper = new MoneyManagerOpenHelper(getActivity());
			SQLiteDatabase database = helper.getReadableDatabase();
			Cursor cursor = database.rawQuery("SELECT DISTINCT Year FROM " + ViewMobileData.mobiledata + " ORDER BY Year DESC", null);
			if (cursor != null && cursor.moveToFirst()) {
				int order = 0;
				while (!cursor.isAfterLast()) {
					int year = cursor.getInt(cursor.getColumnIndex("Year"));
					menu.findItem(R.id.menu_period).getSubMenu().add(0, year, order ++, Integer.toString(year)).setCheckable(true).setChecked(year == Calendar.getInstance().get(Calendar.YEAR));
					//move to next
					cursor.moveToNext();
				}
				cursor.close();
			}
			helper.close();
			// chart item
			MenuItem itemChart = menu.findItem(R.id.menu_chart);
			if (itemChart != null) {
				itemChart.setVisible(!((IncomeVsExpensesActivity)getActivity()).mIsDualPanel);
			}
		}
		
		@Override
		public void onLoaderReset(Loader<Cursor> loader) {
			((IncomeVsExpensesAdapter)getListAdapter()).swapCursor(null);
		}
		
		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
			switch (loader.getId()) {
			case ID_LOADER:
				((IncomeVsExpensesAdapter)getListAdapter()).swapCursor(data);
				if (isResumed()) {
	                setListShown(true);
	            } else {
	                setListShownNoAnimation(true);
	            }
				// calculate income, expenses
				float income = 0, expenses = 0;
				if (data != null && data.moveToFirst()) {
					while (!data.isAfterLast()) {
						income += data.getFloat(data.getColumnIndex(QueryReportIncomeVsExpenses.Income));
						expenses += data.getFloat(data.getColumnIndex(QueryReportIncomeVsExpenses.Expenses));
						// move to next record
						data.moveToNext();
					}
					updateListViewFooter(mFooterListView, income, expenses);
					if (data.getCount() > 0) {
						getListView().removeFooterView(mFooterListView);
						getListView().addFooterView(mFooterListView);
					}
						
				}
				if (((IncomeVsExpensesActivity)getActivity()).mIsDualPanel) {
					Handler handler = new Handler();
					handler.postDelayed(new Runnable() {
						
						@Override
						public void run() {
							showChart();
						}
					}, 1 * 1000);
				}
				break;
			}
		}
		
		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			if (item.getItemId() == R.id.menu_sort) {
				showDialogSortMonth();
			} else if (item.getItemId() == R.id.menu_chart) {
				showChart();
			} else {
				item.setChecked(!item.isChecked());
				// put or remove map key
				if (item.isChecked()) {
					mCheckedItem.put(item.getItemId(), Boolean.TRUE);
				} else {
					mCheckedItem.remove(item.getItemId());
				}
				// start loader
				startLoader(hashMap2IntArray(mCheckedItem));
			}
			
			return super.onOptionsItemSelected(item);
		}
		
		private void showDialogSortMonth() {
			AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
			final LinearLayout layout = new LinearLayout(getActivity());
			layout.setOrientation(LinearLayout.VERTICAL);
			layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			//create radio group
			//create checkbox ascending
			final RadioButton rbtAscending = new RadioButton(getActivity());
			rbtAscending.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			rbtAscending.setText(R.string.ascending);
			//create checkbox descending
			final RadioButton rbtDescending = new RadioButton(getActivity());
			rbtDescending.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			rbtDescending.setText(R.string.descending);
			//check actual value
			rbtAscending.setChecked(SORT_ASCENDING.equals(mSort));
			rbtDescending.setChecked(SORT_DESCENDING.equals(mSort));
			OnCheckedChangeListener changeListener = new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if (buttonView.equals(rbtAscending)) {
						rbtDescending.setChecked(!isChecked);
					} else if (buttonView.equals(rbtDescending)) {
						rbtAscending.setChecked(!isChecked);
					}
				}
			};
			rbtAscending.setOnCheckedChangeListener(changeListener);
			rbtDescending.setOnCheckedChangeListener(changeListener);
			//add checkbox
			layout.addView(rbtAscending);
			layout.addView(rbtDescending);
			//set layuout
			dialog.setView(layout);
			//title
			dialog.setTitle(getString(R.string.sorting_month));
			dialog.setNeutralButton(android.R.string.ok, new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					mSort = rbtAscending.isChecked() ? SORT_ASCENDING : SORT_DESCENDING;
					startLoader(hashMap2IntArray(mCheckedItem));
				}
			});
			//show dialog
			dialog.create().show();
		}
		
		@Override
		public void onPrepareOptionsMenu(Menu menu) {
			SubMenu subMenu = menu.findItem(R.id.menu_period).getSubMenu();
			if (subMenu != null) {
				for(int i = 0; i < subMenu.size(); i ++) {
					MenuItem item = subMenu.getItem(i);
					item.setChecked(mCheckedItem.containsKey(item.getItemId()));
				}
			}
			super.onPrepareOptionsMenu(menu);
		}
		
		@Override
		public void onSaveInstanceState(Bundle outState) {
			super.onSaveInstanceState(outState);
			outState.putIntArray(KEY_BUNDLE_YEAR, hashMap2IntArray(mCheckedItem));
		}
		
		/**
		 * Start loader with arrays year
		 * @param years
		 */
		private void startLoader(int[] years) {
			Bundle bundle = new Bundle();
			bundle.putIntArray(KEY_BUNDLE_YEAR, years);
			getLoaderManager().restartLoader(ID_LOADER, bundle, this);
		}
		
		/**
		 * update View of footer with income, expenses and difference
		 * @param footer
		 * @param income
		 * @param expenses
		 */
		private void updateListViewFooter(View footer, float income, float expenses) {
			if (footer == null) {
				return;
			}
			TextView txtIncome = (TextView)footer.findViewById(R.id.textViewIncome);
			TextView txtExpenses = (TextView)footer.findViewById(R.id.textViewExpenses);
			TextView txtDifference = (TextView)footer.findViewById(R.id.textViewDifference);
			//set income
			txtIncome.setText(currencyUtils.getCurrencyFormatted(currencyUtils.getBaseCurrencyId(), income));
			txtIncome.setTypeface(null, Typeface.BOLD_ITALIC);
			//set expenses
			txtExpenses.setText(currencyUtils.getCurrencyFormatted(currencyUtils.getBaseCurrencyId(), Math.abs(expenses)));
			txtExpenses.setTypeface(null, Typeface.BOLD_ITALIC);
			//set difference
			txtDifference.setText(currencyUtils.getCurrencyFormatted(currencyUtils.getBaseCurrencyId(), income - Math.abs(expenses)));
			txtDifference.setTypeface(null, Typeface.BOLD_ITALIC);
			//change colors
			Core core = new Core(getActivity());
			if (income - Math.abs(expenses) < 0) {
				txtDifference.setTextColor(getResources().getColor(core.resolveIdAttribute(R.attr.holo_red_color_theme)));
			} else {
				txtDifference.setTextColor(getResources().getColor(core.resolveIdAttribute(R.attr.holo_green_color_theme)));
			}
		}
		
		private void showChart() {
			// take a adapter and cursor
			IncomeVsExpensesAdapter adapter = ((IncomeVsExpensesAdapter)getListAdapter());
			if (adapter == null) return;
			Cursor cursor = adapter.getCursor();
			if (cursor == null) return;
			// move to first
			if (!cursor.moveToFirst()) return;
			// arrays
			double[] incomes = new double[cursor.getCount()];
			double[] expenses = new double[cursor.getCount()];
			String[] titles = new String[cursor.getCount()];
			// cycle cursor
			while (!cursor.isAfterLast()) {
				// incomes and expenses
				incomes[cursor.getPosition()] = cursor.getFloat(cursor.getColumnIndex(QueryReportIncomeVsExpenses.Income));
				expenses[cursor.getPosition()] = Math.abs(cursor.getFloat(cursor.getColumnIndex(QueryReportIncomeVsExpenses.Expenses)));
				// titles
				int year = cursor.getInt(cursor.getColumnIndex(QueryReportIncomeVsExpenses.Year));
				int month = cursor.getInt(cursor.getColumnIndex(QueryReportIncomeVsExpenses.Month));
				// format month
				Calendar calendar = Calendar.getInstance();
				calendar.set(year, month - 1, 1);
				// titles
				titles[cursor.getPosition()] = Integer.toString(year) + "-" + new SimpleDateFormat("MMM").format(calendar.getTime());
				// move to next
				cursor.moveToNext();
			}
			//compose bundle for arguments
			Bundle args = new Bundle();
			args.putDoubleArray(IncomeVsExpensesChartFragment.KEY_EXPENSES_VALUES, expenses);
			args.putDoubleArray(IncomeVsExpensesChartFragment.KEY_INCOME_VALUES, incomes);
			args.putStringArray(IncomeVsExpensesChartFragment.KEY_XTITLES, titles);
			//get fragment manager
			FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
			if (fragmentManager != null) {
				IncomeVsExpensesChartFragment fragment;
				fragment = (IncomeVsExpensesChartFragment)fragmentManager.findFragmentByTag(IncomeVsExpensesChartFragment.class.getSimpleName());
				if (fragment == null) {
					fragment = new IncomeVsExpensesChartFragment();
				}
				fragment.setChartArguments(args);
				fragment.setDisplayHomeAsUpEnabled(true);
				
				if (fragment.isVisible()) fragment.onResume();
				
				FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
				if (((IncomeVsExpensesActivity)getActivity()).mIsDualPanel) {
					fragmentTransaction.replace(R.id.fragmentChart, fragment, IncomeVsExpensesChartFragment.class.getSimpleName());
				} else {
					fragmentTransaction.replace(R.id.fragmentContent, fragment, IncomeVsExpensesChartFragment.class.getSimpleName());
					fragmentTransaction.addToBackStack(null);
				}
				fragmentTransaction.commit();
			}
		}
	}
	
	private IncomeVsExpensesListFragment listFragment = new IncomeVsExpensesListFragment();
	private static CurrencyUtils currencyUtils;
	public boolean mIsDualPanel = false;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.report_chart_fragments_activity);
		// set actionbar
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		// check if is dual panel
		mIsDualPanel = findViewById(R.id.fragmentChart) != null;
		
		FragmentManager fm = getSupportFragmentManager();
		// get application
		currencyUtils = new CurrencyUtils(this);
		// attach fragment activity
        if (fm.findFragmentById(R.id.fragmentContent) == null) {
            fm.beginTransaction().replace(R.id.fragmentContent, listFragment, IncomeVsExpensesListFragment.class.getSimpleName()).commit();
        }
	}
}
