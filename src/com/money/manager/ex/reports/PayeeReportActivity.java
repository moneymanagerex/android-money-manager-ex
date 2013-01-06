package com.money.manager.ex.reports;

import java.util.Calendar;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.database.SQLDataSet;
import com.money.manager.ex.database.ViewMobileData;
import com.money.manager.ex.fragment.BaseFragmentActivity;
import com.money.manager.ex.fragment.BaseListFragment;

public class PayeeReportActivity extends BaseFragmentActivity {
	public static class PayeeReportAdapter extends CursorAdapter {
		private LayoutInflater mInflater;
		
		public PayeeReportAdapter(Context context, Cursor c) {
			super(context, c);
			mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			TextView txtColumn1 = (TextView)view.findViewById(R.id.textViewColumn1);
			TextView txtColumn2 = (TextView)view.findViewById(R.id.textViewColumn2);
			float total = cursor.getFloat(cursor.getColumnIndex("TOTAL")); 
			txtColumn1.setText(cursor.getString(cursor.getColumnIndex(ViewMobileData.Payee)));
			txtColumn2.setText(application.getCurrencyFormatted(application.getBaseCurrencyId(), total));
			if (total < 0) {
				txtColumn2.setTextColor(context.getResources().getColor(R.color.holo_red_light));
			} else {
				txtColumn2.setTextColor(context.getResources().getColor(R.color.holo_green_light));
			}
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup root) {
			return mInflater.inflate(R.layout.item_generic_report_2_columns, root, false);
		}
	}
	
	public static class PayeeReportFragment extends BaseListFragment implements LoaderCallbacks<Cursor> {
		private static final int ID_LOADER = 1;
		private static final String KEY_ITEM_SELECTED = "PayeeReportFragment:ItemSelected";
		private static final String KEY_WHERE_CLAUSE = "PayeeReportFragment:WhereClause";
		private int mItemSelected = R.id.menu_all_time;
		private String mWhereClause = null;
		private LinearLayout mHeaderListView, mFooterListView;
		
		private View addListViewHeaderFooter() {
			return View.inflate(getActivity(), R.layout.item_generic_report_2_columns, null);
		}

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			//set listview
			setHasOptionsMenu(true);
			setEmptyText(getString(R.string.no_data));
			//create header view
			mHeaderListView = (LinearLayout)addListViewHeaderFooter();
			TextView txtColumn1 = (TextView)mHeaderListView.findViewById(R.id.textViewColumn1);
			TextView txtColumn2 = (TextView)mHeaderListView.findViewById(R.id.textViewColumn2);
			//set header
			txtColumn1.setText(R.string.payee);
			txtColumn1.setTypeface(null, Typeface.BOLD);
			txtColumn2.setText(R.string.amount);
			txtColumn2.setTypeface(null, Typeface.BOLD);
			//add to listview
			getListView().addHeaderView(mHeaderListView);
			//create footer view
			mFooterListView = (LinearLayout)addListViewHeaderFooter();
			txtColumn1 = (TextView)mFooterListView.findViewById(R.id.textViewColumn1);
			txtColumn2 = (TextView)mFooterListView.findViewById(R.id.textViewColumn2);
			//set footer
			txtColumn1.setText(R.string.total);
			txtColumn1.setTypeface(null, Typeface.BOLD_ITALIC);
			txtColumn2.setText(R.string.total);
			txtColumn2.setTypeface(null, Typeface.BOLD_ITALIC);
			//add to listview
			getListView().addFooterView(mFooterListView);
			//set adapter
			PayeeReportAdapter adapter = new PayeeReportAdapter(getActivity(), null);
			setListAdapter(adapter);
			setListShown(false);
			//item selected
			if (savedInstanceState != null && savedInstanceState.containsKey(KEY_ITEM_SELECTED)) {
				mItemSelected = savedInstanceState.getInt(KEY_ITEM_SELECTED);
			}
			//start loader
			startLoader(savedInstanceState);
			
		}

		@Override
		public Loader<Cursor> onCreateLoader(int id, Bundle args) {
			switch (id) {
			case ID_LOADER:
				if (args != null && args.containsKey(KEY_WHERE_CLAUSE)) {
					mWhereClause = args.getString(KEY_WHERE_CLAUSE);
				}
				return new CursorLoader(getActivity(), new SQLDataSet().getUri(), null, prepareQuery(mWhereClause), null, null);
			}
			return null;
		}
		
		@Override
		public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
			super.onCreateOptionsMenu(menu, inflater);
			//inflate menu
			inflater.inflate(R.menu.menu_report, menu);
			//checked item
			MenuItem item = menu.findItem(mItemSelected);
			if (item != null) {
				item.setChecked(true);
			}
		}
		@Override
		public void onLoaderReset(Loader<Cursor> loader) {
			switch (loader.getId()) {
			case ID_LOADER:
				//((CursorAdapter) getListAdapter()).swapCursor(null);
			}
		}
		
		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
			switch (loader.getId()) {
			case ID_LOADER:
				((PayeeReportAdapter)getListAdapter()).swapCursor(data);
				if (isResumed()) {
	                setListShown(true);
	            } else {
	                setListShownNoAnimation(true);
	            }
				//parse cursor for calculate total
				if (data != null && data.moveToFirst()) {
					float totalAmount = 0;
					while (!data.isAfterLast()) {
						totalAmount += data.getFloat(data.getColumnIndex("TOTAL"));
						data.moveToNext();
					}
					TextView txtColumn2 = (TextView)mFooterListView.findViewById(R.id.textViewColumn2);
					txtColumn2.setText(application.getBaseCurrencyFormatted(totalAmount));
				}
				break;
			}
		}
		
		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			String whereClause = null;
			int currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
			int currentYear = Calendar.getInstance().get(Calendar.YEAR);
			switch (item.getItemId()) {
			case R.id.menu_current_month:
				whereClause = ViewMobileData.Month + "=" + Integer.toString(currentMonth) + " AND " + ViewMobileData.Year + "=" + Integer.toString(currentYear);
				break;
			case R.id.menu_last_month:
				if (currentMonth == 1) {
					whereClause = ViewMobileData.Month + "=" + Integer.toString(12) + " AND " + ViewMobileData.Year + "=" + Integer.toString(currentYear - 1);
				} else {
					whereClause = ViewMobileData.Month + "=" + Integer.toString(currentMonth - 1) + " AND " + ViewMobileData.Year + "=" + Integer.toString(currentYear - 1);;
				}
				break;
			case R.id.menu_last_30_days:
				whereClause = "(julianday(date('now')) - julianday(" + ViewMobileData.Date + ") <= 30)";
				break;
			case R.id.menu_current_year:
				whereClause = ViewMobileData.Year + "=" + Integer.toString(currentYear);
				break;
			case R.id.menu_last_year:
				whereClause = ViewMobileData.Year + "=" + Integer.toString(currentYear - 1);
				break;
			case R.id.menu_all_time:
				break;
			default:
				return super.onOptionsItemSelected(item);
			}
			//check item
			item.setChecked(true);
			mItemSelected = item.getItemId();
			//compose bundle
			Bundle args = new Bundle();
			args.putString(KEY_WHERE_CLAUSE, whereClause);
			//starts loader
			startLoader(args);
			return true;
		}
		
		@Override
		public void onSaveInstanceState(Bundle outState) {
			super.onSaveInstanceState(outState);
			outState.putInt(KEY_ITEM_SELECTED, mItemSelected);
			outState.putString(KEY_WHERE_CLAUSE, mWhereClause);
		}
		
		/**
		 * Prepare SQL query to execute in content provider
		 * @param whereClause
		 * @return
		 */
		private String prepareQuery(String whereClause) {
			SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
			ViewMobileData mobileData = new ViewMobileData();
			//data to compose builder
			String[] projectionIn = new String[] {ViewMobileData.PayeeID + " AS _id", ViewMobileData.PayeeID, ViewMobileData.Payee, "SUM(" + ViewMobileData.Amount + ") AS TOTAL"};
			String selection = ViewMobileData.Status + "<>'V' AND " + ViewMobileData.TransactionType + " IN ('Withdrawal', 'Deposit')";
			if (!TextUtils.isEmpty(whereClause)) {
				selection += " AND " + whereClause;
			}
			String groupBy = ViewMobileData.PayeeID + ", " + ViewMobileData.Payee;
			String having = null;
			String sortOrder = ViewMobileData.Payee;
			String limit = null;
			//compose builder
			builder.setTables(mobileData.getSource());
			//return query
			return builder.buildQuery(projectionIn, selection, groupBy, having, sortOrder, limit);
		}
		
		private void startLoader(Bundle args) {
			getLoaderManager().restartLoader(ID_LOADER, args, this);
		}
	}
	
	private static MoneyManagerApplication application;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		//reference to application
		application = (MoneyManagerApplication)getApplication();
		//create a fragment
		PayeeReportFragment fragment = new PayeeReportFragment();
		FragmentManager fm = getSupportFragmentManager();
		//insert fragment
		if (fm.findFragmentById(android.R.id.content) == null) {
            fm.beginTransaction().add(android.R.id.content, fragment, PayeeReportFragment.class.getSimpleName()).commit();
        }
	}
}
