package com.money.manager.ex.reports;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
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
import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.database.MoneyManagerOpenHelper;
import com.money.manager.ex.database.TableCategory;
import com.money.manager.ex.database.ViewMobileData;
import com.money.manager.ex.fragment.BaseFragmentActivity;

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
			float total = cursor.getFloat(cursor.getColumnIndex("TOTAL")); 
			String column1 = "<b>" + cursor.getString(cursor.getColumnIndex(ViewMobileData.Category)) + "</b>";
			if (!TextUtils.isEmpty(cursor.getString(cursor.getColumnIndex(ViewMobileData.Subcategory)))) {
				column1 += " : " + cursor.getString(cursor.getColumnIndex(ViewMobileData.Subcategory));
			}
			txtColumn1.setText(Html.fromHtml(column1));
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
	
	public static class PayeeReportFragment extends BaseReportFragment {
		private LinearLayout mHeaderListView, mFooterListView;
		private static final int GROUP_ID_CATEGORY = 0xFFFF;
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			
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
			//add to listview
			getListView().addFooterView(mFooterListView);
			//set adapter
			CategoriesReportAdapter adapter = new CategoriesReportAdapter(getActivity(), null);
			setListAdapter(adapter);
			//call super method
			super.onActivityCreated(savedInstanceState);
		}
		
		@Override
		public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
			super.onCreateOptionsMenu(menu, inflater);
			MenuItem itemOption = menu.findItem(R.id.menu_option1);
			if (itemOption != null) {
				itemOption.setVisible(true);
				itemOption.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
				itemOption.setIcon(getActivity().getResources().getDrawable(android.R.drawable.ic_menu_info_details));
				//take a submenu
				SubMenu subMenu = itemOption.getSubMenu();
				if (subMenu != null) {
					//create access to category
					SQLiteDatabase database = new MoneyManagerOpenHelper(getActivity()).getReadableDatabase();
					TableCategory category = new TableCategory();
					Cursor cursor = database.query(category.getSource(), new String[] {TableCategory.CATEGID, TableCategory.CATEGNAME}, null, null, null, null, TableCategory.CATEGNAME);
					if (cursor != null && cursor.moveToFirst()) {
						int order = 0;
						while (!cursor.isAfterLast()) {
							subMenu.add(GROUP_ID_CATEGORY, -cursor.getInt(cursor.getColumnIndex(TableCategory.CATEGID)), order++, cursor.getString(cursor.getColumnIndex(TableCategory.CATEGNAME)));
							cursor.moveToNext();
						}
					}
				}
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
					txtColumn2.setText(application.getBaseCurrencyFormatted(totalAmount));
				}
			}
		}
		
		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			if (item.getItemId() < 0) { // category
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
		
		@Override
		protected String prepareQuery(String whereClause) {
			SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
			ViewMobileData mobileData = new ViewMobileData();
			//data to compose builder
			String[] projectionIn = new String[] {"ROWID AS _id", ViewMobileData.CategID, ViewMobileData.Category, ViewMobileData.SubcategID, ViewMobileData.Subcategory, "SUM(" + ViewMobileData.Amount + ") AS TOTAL"};
			String selection = ViewMobileData.Status + "<>'V' AND " + ViewMobileData.TransactionType + " IN ('Withdrawal', 'Deposit')";
			if (!TextUtils.isEmpty(whereClause)) {
				selection += " AND " + whereClause;
			}
			String groupBy = ViewMobileData.CategID + ", " + ViewMobileData.Category + ", " + ViewMobileData.SubcategID + ", " + ViewMobileData.Subcategory;
			String having = null;
			String sortOrder = ViewMobileData.Category + ", " + ViewMobileData.Subcategory;
			String limit = null;
			//compose builder
			builder.setTables(mobileData.getSource());
			//return query
			return builder.buildQuery(projectionIn, selection, groupBy, having, sortOrder, limit);
		}
		
	}
	
	static MoneyManagerApplication application;
	
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
