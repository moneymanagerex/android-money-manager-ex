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
package com.money.manager.ex.fragment;

import java.util.Calendar;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.money.manager.ex.MainActivity;
import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.database.QueryAccountBills;
import com.money.manager.ex.database.QueryBillDeposits;
import com.money.manager.ex.database.QueryReportIncomeVsExpenses;
import com.money.manager.ex.database.TableInfoTable;
/**
 * 
 * @author Alessandro Lazzari (lazzari.ale@gmail.com)
 *
 */
@SuppressWarnings("static-access")
public class HomeFragment extends Fragment implements
		LoaderManager.LoaderCallbacks<Cursor> {
	public class AccountBillsAdapter extends CursorAdapter {
		private LayoutInflater inflater;
		
		@SuppressWarnings("deprecation")
		public AccountBillsAdapter(Context context, Cursor c) {
			super(context, c);
			inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			TextView txtAccountName = (TextView)view.findViewById(R.id.textVievItemAccountName);
			TextView txtAccountTotal = (TextView)view.findViewById(R.id.textVievItemAccountTotal);
			TextView txtAccountReconciled = (TextView)view.findViewById(R.id.textVievItemAccountTotalReconciled);
			// set account name
			txtAccountName.setText(cursor.getString(cursor.getColumnIndex(accountBills.ACCOUNTNAME)));
			// import formatted
			String value = application.getCurrencyFormatted(cursor
					.getInt(cursor.getColumnIndex(accountBills.CURRENCYID)),
					cursor.getFloat(cursor.getColumnIndex(accountBills.TOTAL)));
			// set amount value
			txtAccountTotal.setText(value);
			// reconciled
			value = application.getCurrencyFormatted(cursor
					.getInt(cursor.getColumnIndex(accountBills.CURRENCYID)),
					cursor.getFloat(cursor.getColumnIndex(accountBills.RECONCILED)));
			txtAccountReconciled.setText(value);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			return inflater.inflate(R.layout.item_account_bills, parent, false);
		}
		
	}
	// ID Loader Manager
	private static final int ID_LOADER_USER_NAME = 1;
	private static final int ID_LOADER_ACCOUNT_BILLS = 2;
	private static final int ID_LOADER_BILL_DEPOSITS = 3;
	private static final int ID_LOADER_INCOME_EXPENSES = 4;
	// application
	private MoneyManagerApplication application;
	// dataset table/view/query manage into class
	private TableInfoTable infoTable = new TableInfoTable(); 
	private QueryAccountBills accountBills;
	// view show in layout
	private TextView txtUserName, txtTotalAccounts, txtOverdue;
	private ListView lstAccountBills;
	private LinearLayout linearRepeating;
	
	private ProgressBar prgAccountBills;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		application = (MoneyManagerApplication)getActivity().getApplication();
		accountBills = new QueryAccountBills(getActivity());
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
		case ID_LOADER_BILL_DEPOSITS:
			QueryBillDeposits billDeposits = new QueryBillDeposits(getActivity());
			return new CursorLoader(getActivity(), billDeposits.getUri(), null, QueryBillDeposits.DAYSLEFT + "<=0", null, null);
		case ID_LOADER_INCOME_EXPENSES:
			QueryReportIncomeVsExpenses report = new QueryReportIncomeVsExpenses(getActivity());
			return new CursorLoader(getActivity(), report.getUri(), report.getAllColumns(), QueryReportIncomeVsExpenses.Month + "="
					+ Integer.toString(Calendar.getInstance().get(Calendar.MONTH) + 1) + " AND " + QueryReportIncomeVsExpenses.Year + "="
					+ Integer.toString(Calendar.getInstance().get(Calendar.YEAR)), null, null);
		default:
			return null;
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (container == null) { return null; }
		// inflate layout
		View view = (LinearLayout)inflater.inflate(R.layout.fragment_main, container, false);
		// reference view into layout
		txtOverdue = (TextView)view.findViewById(R.id.textViewOverdue);
		txtUserName = (TextView)view.findViewById(R.id.textViewUserName);
		txtTotalAccounts = (TextView)view.findViewById(R.id.textViewTotalAccounts);
		lstAccountBills = (ListView)view.findViewById(R.id.listViewAccountBills);
		lstAccountBills.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				MainActivity activity = (MainActivity)getActivity();
				Cursor cursor = ((CursorAdapter)lstAccountBills.getAdapter()).getCursor();
				int accountId = -1;
				if (cursor != null && cursor.moveToPosition(position)) {
					accountId = cursor.getInt(cursor.getColumnIndex(QueryAccountBills.ACCOUNTID));
				}
				// show account clicked
				if (activity != null) {
					activity.showFragment(position, accountId);
				}
			}
		});
		prgAccountBills = (ProgressBar)view.findViewById(R.id.progressAccountBills);
		
		return view;
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
					curTotal = curTotal + data.getFloat(data.getColumnIndex(QueryAccountBills.TOTALBASECONVRATE));
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
			break;
		case ID_LOADER_BILL_DEPOSITS:
			LinearLayout content = (LinearLayout)getView().findViewById(R.id.content);
			if (content != null) {
				// remove view if exists
				if (linearRepeating != null)
					content.removeView(linearRepeating);
				// add view
				if (data != null && data.getCount() > 0) {
					LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					linearRepeating = (LinearLayout)inflater.inflate(R.layout.merge_main_footer_billdeposits, null);
					if (linearRepeating != null) {
						txtOverdue = (TextView)linearRepeating.findViewById(R.id.textViewOverdue);
						txtOverdue.setText(getString(R.string.num_repeating_transaction_expired, data.getCount()));
						content.addView(linearRepeating);
					}
				}
			}
			break;
		case ID_LOADER_INCOME_EXPENSES:
			float income = 0, expenses = 0;
			if (data != null && data.moveToFirst()) {
				while (!data.isAfterLast()) {
					expenses = data.getFloat(data.getColumnIndex(QueryReportIncomeVsExpenses.Expenses));
					income = data.getFloat(data.getColumnIndex(QueryReportIncomeVsExpenses.Income));
					//move to next record
					data.moveToNext();
				}
			}
			TextView txtIncome = (TextView)getActivity().findViewById(R.id.textViewIncome);
			TextView txtExpenses = (TextView)getActivity().findViewById(R.id.textViewExpenses);
			TextView txtDifference = (TextView)getActivity().findViewById(R.id.textViewDifference);
			// take application
			MoneyManagerApplication application = ((MoneyManagerApplication)getActivity().getApplication());
			// set value
			txtIncome.setText(application.getCurrencyFormatted(application.getBaseCurrencyId(), income));
			txtExpenses.setText(application.getCurrencyFormatted(application.getBaseCurrencyId(), Math.abs(expenses)));
			txtDifference.setText(application.getCurrencyFormatted(application.getBaseCurrencyId(), income - Math.abs(expenses)));
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		// start loader data
		startLoader();
	}

	public LayoutAnimationController setAnimationView(View view) {
		AnimationSet set = new AnimationSet(true);

		Animation animation = new AlphaAnimation(0.0f, 1.0f);
		animation.setDuration(250);
		set.addAnimation(animation);

		animation = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f,
				Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f
		);
		animation.setDuration(150);
		set.addAnimation(animation);

		LayoutAnimationController controller = new LayoutAnimationController(set, 0.25f);
		
		return controller;
	}
	/**
	 * @param if visible set true show the listview; false show progressbar
	 */
	private void setListViewAccountBillsVisible(boolean visible) {
		if (visible) {
			lstAccountBills.setVisibility(View.VISIBLE);
			prgAccountBills.setVisibility(View.GONE);
		} else {
			lstAccountBills.setVisibility(View.GONE);
			prgAccountBills.setVisibility(View.VISIBLE);
		}
	}
	
	public void startLoader() {	
		getLoaderManager().restartLoader(ID_LOADER_USER_NAME, null, this);
		getLoaderManager().restartLoader(ID_LOADER_ACCOUNT_BILLS, null, this);
		getLoaderManager().restartLoader(ID_LOADER_BILL_DEPOSITS, null, this);
		getLoaderManager().restartLoader(ID_LOADER_INCOME_EXPENSES, null, this);
	}
}
