/*******************************************************************************
 * Copyright (C) 2012 The Android Money Manager Ex Project
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
package com.android.money.manager.ex.fragment;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.money.manager.ex.MoneyManagerApplication;
import com.android.money.manager.ex.R;
import com.android.money.manager.ex.database.QueryAccountBills;
import com.android.money.manager.ex.database.TableInfoTable;
/**
 * 
 * @author Alessandro Lazzari (lazzari.ale@gmail.com)
 *
 */
@SuppressWarnings("static-access")
public class HomeFragment extends Fragment implements
		LoaderManager.LoaderCallbacks<Cursor> {
	// ID Loader Manager
	private static final int ID_LOADER_USER_NAME = 1;
	private static final int ID_LOADER_ACCOUNT_BILLS = 2;
	// application
	private MoneyManagerApplication application;
	// dataset table/view/query manage into class
	private TableInfoTable infoTable = new TableInfoTable();
	private QueryAccountBills accountBills = new QueryAccountBills(); 
	// view show in layout
	private TextView txtUserName;
	private TextView txtTotalAccounts;
	private ListView lstAccountBills;
	private ProgressBar prgAccountBills;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		application = (MoneyManagerApplication)getActivity().getApplication();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (container == null) { return null; }
		// inflate layout
		View view = (LinearLayout)inflater.inflate(R.layout.fragment_main, container, false);
		// reference view into layout
		txtUserName = (TextView)view.findViewById(R.id.textViewUserName);
		txtTotalAccounts = (TextView)view.findViewById(R.id.textViewTotalAccounts);
		lstAccountBills = (ListView)view.findViewById(R.id.listViewAccountBills);
		prgAccountBills = (ProgressBar)view.findViewById(R.id.progressAccountBills);
		
		return view;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		// start loader data
		startLoader();
	}
	
	public void startLoader() {	
		getLoaderManager().restartLoader(ID_LOADER_USER_NAME, null, this);
		getLoaderManager().restartLoader(ID_LOADER_ACCOUNT_BILLS, null, this);
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		switch (id) {
		case ID_LOADER_USER_NAME:
			return new CursorLoader(getActivity(), infoTable.getUri(),
					new String[] {infoTable.INFONAME, infoTable.INFOVALUE}, null, null, null);
		case ID_LOADER_ACCOUNT_BILLS:
			setListViewAccountBillsVisible(false);
			// compose whereClause
			String where = "";
			// check if show only open accounts
			if (application.getAccountsOpenVisible()) {
				where = "LOWER(STATUS)='open'";
			}
			// check if show fav accounts
			if (application.getAccountFavoriteVisible()) {
				where = "LOWER(FAVORITEACCT)='true'";
			}
			return new CursorLoader(getActivity(), accountBills.getUri(),
					accountBills.getAllColumns(), where, null, "upper(" + accountBills.ACCOUNTNAME + ")");
		default:
			return null;
		}
	}
	
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		switch (loader.getId()) {
		case ID_LOADER_USER_NAME:
			if (data != null && data.moveToFirst()) {
				while (data.isAfterLast() == false) {
					String infoValue = data.getString(data.getColumnIndex(infoTable.INFONAME));
					// save into preferences username and basecurrency id
					if (infoValue.equals("USERNAME")) {
						application.setUserName(data.getString(data.getColumnIndex(infoTable.INFOVALUE)));
					} else if (infoValue.equals("BASECURRENCYID")) {
						application.setBaseCurrencyId(data.getInt(data.getColumnIndex(infoTable.INFOVALUE)));
					}
					data.moveToNext();
				}
			}
			// show username
			txtUserName.setText(application.getUserName());
			break;
		case ID_LOADER_ACCOUNT_BILLS:
			if (data != null && data.moveToFirst()) {
				float curTotal = 0;
				// calculate 
				while (data.isAfterLast() == false) {
					curTotal = curTotal + data.getFloat(data.getColumnIndex("TOTAL"));
					data.moveToNext();
				}
				// write accounts total
				txtTotalAccounts.setText(application.getBaseCurrencyFormatted(curTotal));
				// adapter show data
				AccountBillsAdapter adapter = new AccountBillsAdapter(getActivity(), data);
				lstAccountBills.setAdapter(adapter);
			} else {
				txtTotalAccounts.setText(application.getBaseCurrencyFormatted(0));
				lstAccountBills.setAdapter(null);
			}
			setListViewAccountBillsVisible(true);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		switch (loader.getId()) {
		case ID_LOADER_USER_NAME:
			txtUserName.setText("");
			break;
		case ID_LOADER_ACCOUNT_BILLS:
			txtTotalAccounts.setText(application.getBaseCurrencyFormatted(0));
			lstAccountBills.setAdapter(null);
			setListViewAccountBillsVisible(false);
		}
	}
	/**
	 * @param if visible set true show the listview; false show progressbar
	 */
	private void setListViewAccountBillsVisible(boolean visible) {
		if (visible) {
			lstAccountBills.setVisibility(View.VISIBLE);
			prgAccountBills.setVisibility(View.GONE);
		} else {
			prgAccountBills.setVisibility(View.GONE);
			prgAccountBills.setVisibility(View.VISIBLE);
		}
	}
	
	public class AccountBillsAdapter extends CursorAdapter {
		private LayoutInflater inflater;
		
		public AccountBillsAdapter(Context context, Cursor c) {
			super(context, c);
			inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			return inflater.inflate(R.layout.item_account_bills, parent, false);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			TextView txtAccountName = (TextView)view.findViewById(R.id.textVievItemAccountName);
			TextView txtAccountTotal = (TextView)view.findViewById(R.id.textVievItemAccountTotal);
			// set account name
			txtAccountName.setText(cursor.getString(cursor.getColumnIndex(accountBills.ACCOUNTNAME)));
			// import formatted
			String value = application.getCurrencyFormatted(cursor
					.getInt(cursor.getColumnIndex(accountBills.CURRENCYID)),
					cursor.getFloat(cursor.getColumnIndex(accountBills.TOTAL)));
			// set amount value
			txtAccountTotal.setText(value);
		}
		
	}
}
