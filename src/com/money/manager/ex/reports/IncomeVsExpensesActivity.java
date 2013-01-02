package com.money.manager.ex.reports;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableRow;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.database.MoneyManagerOpenHelper;
import com.money.manager.ex.database.QueryReportIncomeVsExpenses;
import com.money.manager.ex.fragment.BaseFragmentActivity;

public class IncomeVsExpensesActivity extends BaseFragmentActivity {
	public static class IncomeVsExpensesListFragment extends SherlockListFragment implements LoaderManager.LoaderCallbacks<Cursor> { 
		private static final int ID_LOADER = 1;
		private static final String KEY_BUNDLE_YEAR = "IncomeVsExpensesListFragment:Years";
		private View mFooterListView;
		private Map<Integer, Boolean> mCheckedItem = new HashMap<Integer, Boolean>(); 
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
			return new CursorLoader(getActivity(), report.getUri(), report.getAllColumns(), selection, null, QueryReportIncomeVsExpenses.Year + " DESC, " + QueryReportIncomeVsExpenses.Month);
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
				}
				break;
			}
		}

		@Override
		public void onLoaderReset(Loader<Cursor> loader) {
			((IncomeVsExpensesAdapter)getListAdapter()).swapCursor(null);
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
			// set listview
			setEmptyText(getString(R.string.no_data));
			// add header and footer
			addListViewHeader();
			mFooterListView = addListViewFooter();
			// create adapter
			IncomeVsExpensesAdapter adapter = new IncomeVsExpensesAdapter(getActivity(), null);
			setListAdapter(adapter);
			setListShown(false);
			// start loader
			startLoader(hashMap2IntArray(mCheckedItem));
		}
		
		@Override
		public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
			super.onCreateOptionsMenu(menu, inflater);
			//Create a cursor for select year
			MoneyManagerOpenHelper helper = new MoneyManagerOpenHelper(getActivity());
			SQLiteDatabase database = helper.getReadableDatabase();
			Cursor cursor = database.rawQuery("SELECT DISTINCT Year FROM mobiledata ORDER BY Year DESC", null);
			if (cursor != null && cursor.moveToFirst()) {
				int order = 0;
				while (!cursor.isAfterLast()) {
					int year = cursor.getInt(cursor.getColumnIndex("Year"));
					menu.add(0, year, order ++, Integer.toString(year)).setCheckable(true);
					//move to next
					cursor.moveToNext();
				}
				cursor.close();
			}
			helper.close();
		}
		
		@Override
		public void onPrepareOptionsMenu(Menu menu) {
			for(int i = 0; i < menu.size(); i ++) {
				MenuItem item = menu.getItem(i);
				item.setChecked(mCheckedItem.containsKey(item.getItemId()));
			}
			super.onPrepareOptionsMenu(menu);
		}
		
		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			item.setChecked(!item.isChecked());
			// put or remove map key
			if (item.isChecked()) {
				mCheckedItem.put(item.getItemId(), Boolean.TRUE);
			} else {
				mCheckedItem.remove(item.getItemId());
			}
			// start loader
			startLoader(hashMap2IntArray(mCheckedItem));
			
			return super.onOptionsItemSelected(item);
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
		/**
		 * Add header to ListView
		 */
		private void addListViewHeader() {
			TableRow row = (TableRow)View.inflate(getActivity(), R.layout.tablerow_income_vs_expenses, null);
			int[] ids = new int[] {R.id.textViewYear, R.id.textViewMonth, R.id.textViewIncome, R.id.textViewExpenses, R.id.textViewDifference};
			for(int id : ids) {
				TextView textView = (TextView)row.findViewById(id);
				textView.setTypeface(null, Typeface.BOLD);
				//textView.setText(textView.getText().toString().toUpperCase());
			}
			getListView().addHeaderView(row);
		}
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
			getListView().addFooterView(row);
			return row;
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
			txtIncome.setText(application.getCurrencyFormatted(application.getBaseCurrencyId(), income));
			txtIncome.setTypeface(null, Typeface.BOLD_ITALIC);
			//set expenses
			txtExpenses.setText(application.getCurrencyFormatted(application.getBaseCurrencyId(), Math.abs(expenses)));
			txtExpenses.setTypeface(null, Typeface.BOLD_ITALIC);
			//set difference
			txtDifference.setText(application.getCurrencyFormatted(application.getBaseCurrencyId(), income - Math.abs(expenses)));
			txtDifference.setTypeface(null, Typeface.BOLD_ITALIC);
			//change colors
			if (income - Math.abs(expenses) < 0) {
				txtDifference.setTextColor(getResources().getColor(R.color.holo_red_light));
			} else {
				txtDifference.setTextColor(getResources().getColor(R.color.holo_green_dark));
			}
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
		
		@Override
		public void onSaveInstanceState(Bundle outState) {
			super.onSaveInstanceState(outState);
			outState.putIntArray(KEY_BUNDLE_YEAR, hashMap2IntArray(mCheckedItem));
		}
	}
	
	private static class IncomeVsExpensesAdapter extends CursorAdapter {
		private LayoutInflater mInflater;
		
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
			txtIncome.setText(application.getCurrencyFormatted(application.getBaseCurrencyId(), income));
			txtExpenses.setText(application.getCurrencyFormatted(application.getBaseCurrencyId(), Math.abs(expenses)));
			txtDifference.setText(application.getCurrencyFormatted(application.getBaseCurrencyId(), income - Math.abs(expenses)));
			if (income - Math.abs(expenses) < 0) {
				txtDifference.setTextColor(context.getResources().getColor(R.color.holo_red_light));
			} else {
				txtDifference.setTextColor(context.getResources().getColor(R.color.holo_green_dark));
			}
		}
		
		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			return mInflater.inflate(R.layout.tablerow_income_vs_expenses, parent, false);
		}
	}
	private IncomeVsExpensesListFragment listFragment = new IncomeVsExpensesListFragment();
	private static MoneyManagerApplication application;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// set actionbar
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		FragmentManager fm = getSupportFragmentManager();
		// get application
		application = (MoneyManagerApplication)getApplication();
		// attach fragment activity
        if (fm.findFragmentById(android.R.id.content) == null) {
            fm.beginTransaction().add(android.R.id.content, listFragment, IncomeVsExpensesListFragment.class.getSimpleName()).commit();
        }
	}
}
