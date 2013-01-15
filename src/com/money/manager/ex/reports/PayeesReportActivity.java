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

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.database.ViewMobileData;
import com.money.manager.ex.fragment.BaseFragmentActivity;

public class PayeesReportActivity extends BaseFragmentActivity {
	public static class PayeeReportFragment extends BaseReportFragment {
		private LinearLayout mHeaderListView, mFooterListView;
		
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			
			//create header view
			mHeaderListView = (LinearLayout)addListViewHeaderFooter(R.layout.item_generic_report_2_columns);
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
			PayeeReportAdapter adapter = new PayeeReportAdapter(getActivity(), null);
			setListAdapter(adapter);

			super.onActivityCreated(savedInstanceState);
		}
		
		@SuppressWarnings("deprecation")
		@Override
		protected String prepareQuery(String whereClause) {
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
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
				return builder.buildQuery(projectionIn, selection, groupBy, having, sortOrder, limit);
			} else {
				return builder.buildQuery(projectionIn, selection, null, groupBy, having, sortOrder, limit);
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
		
	}
	
	private static class PayeeReportAdapter extends CursorAdapter {
		private LayoutInflater mInflater;
		
		@SuppressWarnings("deprecation")
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
