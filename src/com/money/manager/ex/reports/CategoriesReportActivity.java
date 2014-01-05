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

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.chart.ValuePieChart;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.CurrencyUtils;
import com.money.manager.ex.database.MoneyManagerOpenHelper;
import com.money.manager.ex.database.TableCategory;
import com.money.manager.ex.database.ViewMobileData;
import com.money.manager.ex.fragment.BaseFragmentActivity;
import com.money.manager.ex.fragment.IncomeVsExpensesChartFragment;
import com.money.manager.ex.fragment.PieChartFragment;

public class CategoriesReportActivity extends BaseFragmentActivity {
	private static class CategoriesReportAdapter extends CursorAdapter {
		private LayoutInflater mInflater;
		
		@SuppressWarnings("deprecation")
		public CategoriesReportAdapter(Context context, Cursor c) {
			super(context, c);
			mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			TextView txtColumn1 = (TextView)view.findViewById(R.id.textViewColumn1);
			TextView txtColumn2 = (TextView)view.findViewById(R.id.textViewColumn2);
			Core core = new Core(context);
			float total = cursor.getFloat(cursor.getColumnIndex("TOTAL"));
			String column1;
			if (!TextUtils.isEmpty(cursor.getString(cursor.getColumnIndex(ViewMobileData.Category)))) {
				column1 = "<b>" + cursor.getString(cursor.getColumnIndex(ViewMobileData.Category)) + "</b>";
				if (!TextUtils.isEmpty(cursor.getString(cursor.getColumnIndex(ViewMobileData.Subcategory)))) {
					column1 += " : " + cursor.getString(cursor.getColumnIndex(ViewMobileData.Subcategory));
				}
			} else {
				column1 = "<i>" + context.getString(R.string.empty_category);
			}
			txtColumn1.setText(Html.fromHtml(column1));
			txtColumn2.setText(currencyUtils.getCurrencyFormatted(currencyUtils.getBaseCurrencyId(), total));
			if (total < 0) {
				txtColumn2.setTextColor(context.getResources().getColor(core.resolveIdAttribute(R.attr.holo_red_color_theme)));
			} else {
				txtColumn2.setTextColor(context.getResources().getColor(core.resolveIdAttribute(R.attr.holo_green_color_theme)));
			}
			view.setBackgroundColor(core.resolveColorAttribute(cursor.getPosition() % 2 == 1 ? R.attr.row_dark_theme : R.attr.row_light_theme));
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup root) {
			return mInflater.inflate(R.layout.item_generic_report_2_columns, root, false);
		}
	}
	
	public static class CategoriesReportFragment extends BaseReportFragment {
		private LinearLayout mHeaderListView, mFooterListView;
		private static final int GROUP_ID_CATEGORY = 0xFFFF;
		
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			setListAdapter(null);
			//create header view
			mHeaderListView = (LinearLayout)addListViewHeaderFooter(R.layout.item_generic_report_2_columns);
			TextView txtColumn1 = (TextView)mHeaderListView.findViewById(R.id.textViewColumn1);
			TextView txtColumn2 = (TextView)mHeaderListView.findViewById(R.id.textViewColumn2);
			//set header
			txtColumn1.setText(R.string.category);
			txtColumn1.setTypeface(null, Typeface.BOLD);
			txtColumn2.setText(R.string.amount);
			txtColumn2.setTypeface(null, Typeface.BOLD);
			//add to listview
			getListView().addHeaderView(mHeaderListView);
			//create footer view
			mFooterListView = (LinearLayout)addListViewHeaderFooter(R.layout.item_generic_report_2_columns);
			txtColumn1 = (TextView)mFooterListView.findViewById(R.id.textViewColumn1);
			txtColumn2 = (TextView)mFooterListView.findViewById(R.id.textViewColumn2);
			//set footer
			txtColumn1.setText(R.string.total);
			txtColumn1.setTypeface(null, Typeface.BOLD_ITALIC);
			txtColumn2.setText(R.string.total);
			txtColumn2.setTypeface(null, Typeface.BOLD_ITALIC);
			//add to listview --> move to load finished
			//getListView().addFooterView(mFooterListView);
			//set adapter
			CategoriesReportAdapter adapter = new CategoriesReportAdapter(getActivity(), null);
			setListAdapter(adapter);
			//call super method
			super.onActivityCreated(savedInstanceState);
		}
		
		@Override
		public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
			super.onCreateOptionsMenu(menu, inflater);
			
			Core core = new Core(getActivity());
			
			MenuItem itemOption = menu.findItem(R.id.menu_option1);
			if (itemOption != null) {
				itemOption.setVisible(true);
				itemOption.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
				itemOption.setIcon(getActivity().getResources().getDrawable(core.resolveIdAttribute(R.attr.ic_action_list)));
				//take a submenu
				SubMenu subMenu = itemOption.getSubMenu();
				if (subMenu != null) {
					//create access to category
					MoneyManagerOpenHelper helper = new MoneyManagerOpenHelper(getActivity());
					SQLiteDatabase database = helper.getReadableDatabase();
					TableCategory category = new TableCategory();
					Cursor cursor = database.query(category.getSource(), new String[] {TableCategory.CATEGID, TableCategory.CATEGNAME}, null, null, null, null, TableCategory.CATEGNAME);
					if (cursor != null && cursor.moveToFirst()) {
						int order = 0;
						while (!cursor.isAfterLast()) {
							subMenu.add(GROUP_ID_CATEGORY, -cursor.getInt(cursor.getColumnIndex(TableCategory.CATEGID)), order++, cursor.getString(cursor.getColumnIndex(TableCategory.CATEGNAME)));
							cursor.moveToNext();
						}
					}
					helper.close();
				}
			}
			// pie chart
			MenuItem itemChart = menu.findItem(R.id.menu_chart);
			if (itemChart != null) {
				itemChart.setVisible(!(((CategoriesReportActivity)getActivity()).mIsDualPanel));
				itemChart.setIcon(core.resolveIdAttribute(R.attr.ic_action_pie_chart));
			}
		}
		
		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
			super.onLoadFinished(loader, data);
			switch (loader.getId()) {
			case ID_LOADER:
				//parse cursor for calculate total
				if (data != null && data.moveToFirst()) {
					float totalAmount = 0;
					while (!data.isAfterLast()) {
						totalAmount += data.getFloat(data.getColumnIndex("TOTAL"));
						data.moveToNext();
					}
					TextView txtColumn2 = (TextView)mFooterListView.findViewById(R.id.textViewColumn2);
					txtColumn2.setText(currencyUtils.getBaseCurrencyFormatted(totalAmount));
					// soved bug chart
					if (data.getCount() > 0) {
						getListView().removeFooterView(mFooterListView);
						getListView().addFooterView(mFooterListView);
					}
				}
				if (((CategoriesReportActivity)getActivity()).mIsDualPanel) {
					Handler handler = new Handler();
					handler.postDelayed(new Runnable() {
						
						@Override
						public void run() {
							showChart();
							
						}
					}, 1000);
				}
			}
		}
		
		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			if (item.getItemId() == R.id.menu_chart) {
				showChart();
			} else if (item.getItemId() < 0) { // category
				String whereClause = getWhereClause();
				if (!TextUtils.isEmpty(whereClause))
					whereClause += " AND ";
				else
					whereClause = "";
				whereClause += " " + ViewMobileData.CategID + "=" + Integer.toString(Math.abs(item.getItemId()));
				//create arguments
				Bundle args = new Bundle();
				args.putString(KEY_WHERE_CLAUSE, whereClause);
				//starts loader
				startLoader(args);
			}
			return super.onOptionsItemSelected(item);
		}
		
		@SuppressWarnings("deprecation")
		@Override
		protected String prepareQuery(String whereClause) {
			SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
			ViewMobileData mobileData = new ViewMobileData();
			//data to compose builder
			String[] projectionIn = new String[] {"ROWID AS _id", ViewMobileData.CategID, ViewMobileData.Category, ViewMobileData.SubcategID, ViewMobileData.Subcategory, "SUM(" + ViewMobileData.AmountBaseConvRate + ") AS TOTAL"};
			String selection = ViewMobileData.Status + "<>'V' AND " + ViewMobileData.TransactionType + " IN ('Withdrawal', 'Deposit')";
			if (!TextUtils.isEmpty(whereClause)) {
				selection += " AND " + whereClause;
			}
			String groupBy = ViewMobileData.CategID + ", " + ViewMobileData.Category + ", " + ViewMobileData.SubcategID + ", " + ViewMobileData.Subcategory;
			String having = null;
			if (!TextUtils.isEmpty(((CategoriesReportActivity)getActivity()).mFilter)) {
				if (Constants.TRANSACTION_TYPE_WITHDRAWAL.equalsIgnoreCase(((CategoriesReportActivity)getActivity()).mFilter)) {
					having = "SUM(" + ViewMobileData.AmountBaseConvRate + ") < 0";
				} else {
					having = "SUM(" + ViewMobileData.AmountBaseConvRate + ") > 0";
				}	
			}
			String sortOrder = ViewMobileData.Category + ", " + ViewMobileData.Subcategory;
			String limit = null;
			//compose builder
			builder.setTables(mobileData.getSource());
			//return query
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
				return builder.buildQuery(projectionIn, selection, groupBy, having, sortOrder, limit);
			} else {
				return builder.buildQuery(projectionIn, selection, null, groupBy, having, sortOrder, limit);
			}
		}
		
		public void showChart() {
			CategoriesReportAdapter adapter = (CategoriesReportAdapter)getListAdapter();
			if (adapter == null) return;
			Cursor cursor = adapter.getCursor();
			if (cursor == null) return;
			// move first record
			if (!cursor.moveToFirst()) return;
			// create arraylist
			ArrayList<ValuePieChart> arrayList = new ArrayList<ValuePieChart>();
			// process cursor
			while (!cursor.isAfterLast()) {
				ValuePieChart item = new ValuePieChart();
				String category = cursor.getString(cursor.getColumnIndex(ViewMobileData.Category));
				if (!TextUtils.isEmpty(cursor.getString(cursor.getColumnIndex(ViewMobileData.Subcategory)))) {
					category += " : " + cursor.getString(cursor.getColumnIndex(ViewMobileData.Subcategory));
				}
				// total
				float total = Math.abs(cursor.getFloat(cursor.getColumnIndex("TOTAL")));
				// check if category is empty
				if (TextUtils.isEmpty(category))
					category = getString(R.string.empty_category);
				
				item.setCategory(category);
				item.setValue(total);
				item.setValueFormatted(currencyUtils.getCurrencyFormatted(currencyUtils.getBaseCurrencyId(), total));
				// add element
				arrayList.add(item);	
				
				// move to next recordd
				cursor.moveToNext();
			}
			Bundle args = new Bundle();
			args.putSerializable(PieChartFragment.KEY_CATEGORIES_VALUES, arrayList);
			//get fragment manager
			FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
			if (fragmentManager != null) {
				PieChartFragment fragment;
				fragment = (PieChartFragment)fragmentManager.findFragmentByTag(IncomeVsExpensesChartFragment.class.getSimpleName());
				if (fragment == null) {
					fragment = new PieChartFragment();
				}
				fragment.setChartArguments(args);
				fragment.setDisplayHomeAsUpEnabled(true);
				
				if (fragment.isVisible()) fragment.onResume();
				
				FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
				if (((CategoriesReportActivity)getActivity()).mIsDualPanel) {
					fragmentTransaction.replace(R.id.fragmentChart, fragment, PieChartFragment.class.getSimpleName());
				} else {
					fragmentTransaction.replace(R.id.fragmentContent, fragment, PieChartFragment.class.getSimpleName());
					fragmentTransaction.addToBackStack(null);
				}
				fragmentTransaction.commit();
			}
		}

		@Override
		public String getSubTitle() {
			return null;
		}	
	}
	
	public static final String REPORT_FILTERS = "CategoriesReportActivity:Filter";
	public static final String REPORT_TITLE = "CategoriesReportActivity:Title";
	
	private static CurrencyUtils currencyUtils;
	public boolean mIsDualPanel = false;
	public String mFilter = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (getIntent() != null) {
			if (!TextUtils.isEmpty(getIntent().getStringExtra(REPORT_FILTERS)))
				mFilter = getIntent().getStringExtra(REPORT_FILTERS);
			if (!TextUtils.isEmpty(getIntent().getStringExtra(REPORT_TITLE)))
				setTitle(getIntent().getStringExtra(REPORT_TITLE));
		}
		
		setContentView(R.layout.report_chart_fragments_activity);
		setDisplayHomeAsUpEnabled(true);
		//check if is dual panel
		mIsDualPanel = findViewById(R.id.fragmentChart) != null;
		//reference to application
		currencyUtils = new CurrencyUtils(this);
		//create a fragment
		CategoriesReportFragment fragment = new CategoriesReportFragment();
		FragmentManager fm = getSupportFragmentManager();
		//insert fragment
		if (fm.findFragmentById(R.id.fragmentContent) == null) {
            fm.beginTransaction().add(R.id.fragmentContent, fragment, CategoriesReportFragment.class.getSimpleName()).commit();
        }
	}
}
