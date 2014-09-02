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

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.HeaderViewListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.money.manager.ex.AccountListEditActivity;
import com.money.manager.ex.Constants;
import com.money.manager.ex.MainActivity;
import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.core.CurrencyUtils;
import com.money.manager.ex.database.QueryAccountBills;
import com.money.manager.ex.database.QueryBillDeposits;
import com.money.manager.ex.database.QueryReportIncomeVsExpenses;
import com.money.manager.ex.database.TableInfoTable;
import com.money.manager.ex.preferences.PreferencesActivity;
import com.money.manager.ex.preferences.PreferencesConstant;

import java.util.Calendar;
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
			String value = currencyUtils.getCurrencyFormatted(cursor
					.getInt(cursor.getColumnIndex(accountBills.CURRENCYID)),
					cursor.getDouble(cursor.getColumnIndex(accountBills.TOTAL)));
			// set amount value
			txtAccountTotal.setText(value);
			// reconciled
			value = currencyUtils.getCurrencyFormatted(cursor
					.getInt(cursor.getColumnIndex(accountBills.CURRENCYID)),
					cursor.getDouble(cursor.getColumnIndex(accountBills.RECONCILED)));
			txtAccountReconciled.setText(value);
			// set imageview account type
			ImageView imgAccountType = (ImageView)view.findViewById(R.id.imageViewAccountType);
			String accountType = cursor.getString(cursor.getColumnIndex(accountBills.ACCOUNTTYPE));
			if (!TextUtils.isEmpty(accountType)) {
				if ("term".equalsIgnoreCase(accountType)) {
					imgAccountType.setImageDrawable(getResources().getDrawable(R.drawable.ic_money_finance));
				}
			}
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
	private CurrencyUtils currencyUtils;
	// dataset table/view/query manage into class
	private TableInfoTable infoTable = new TableInfoTable(); 
	private QueryAccountBills accountBills;
	// view show in layout
	private TextView txtTotalAccounts;
	private ListView lstAccountBills;
	private ViewGroup linearHome, linearFooter, linearWelcome;
	private TextView txtFooterSummary;
	private TextView txtFooterSummaryReconciled;
	
	private ProgressBar prgAccountBills;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		application = (MoneyManagerApplication)getActivity().getApplication();
		currencyUtils = new CurrencyUtils(getActivity());
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
		linearHome = (LinearLayout)view.findViewById(R.id.linearLayoutHome);
		linearWelcome = (ViewGroup)view.findViewById(R.id.linearLayoutWelcome);
		
		// add account button
		Button btnAddAccount = (Button)view.findViewById(R.id.buttonAddAccount);
		if (btnAddAccount != null) {
			btnAddAccount.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(getActivity(), AccountListEditActivity.class);
					intent.setAction(Constants.INTENT_ACTION_INSERT);
					startActivity(intent);
				}
			});
		}
		
		// link to dropbox
		Button btnLinkDropbox = (Button)view.findViewById(R.id.buttonLinkDropbox);
		if (btnLinkDropbox != null) {
			btnLinkDropbox.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(getActivity(), PreferencesActivity.class);
					intent.putExtra(Constants.INTENT_REQUEST_PREFERENCES_SCREEN, PreferencesConstant.PREF_DROPBOX_HOWITWORKS);
					startActivity(intent);
				}
			});
		}
		
		txtTotalAccounts = (TextView)view.findViewById(R.id.textViewTotalAccounts);
		lstAccountBills = (ListView)view.findViewById(R.id.listViewAccountBills);
		lstAccountBills.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				MainActivity activity = (MainActivity)getActivity();
				//Cursor cursor = ((CursorAdapter)lstAccountBills.getAdapter()).getCursor();
				HeaderViewListAdapter headerViewListAdapter = (HeaderViewListAdapter)lstAccountBills.getAdapter();
				AccountBillsAdapter accountBillsAdapter = (AccountBillsAdapter)headerViewListAdapter.getWrappedAdapter();
				Cursor cursor = accountBillsAdapter.getCursor();
				int accountId = -1;
				if (cursor != null && cursor.moveToPosition(position)) {
					accountId = cursor.getInt(cursor.getColumnIndex(QueryAccountBills.ACCOUNTID));
				}
				// show account clicked
				if (activity != null && activity instanceof MainActivity) {
					activity.showFragmentAccount(position, accountId);
				}
			}
		});
		// set highlight item
		if (getActivity() != null && getActivity() instanceof MainActivity) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
				lstAccountBills.setSelector(R.color.holo_blue_light);
			lstAccountBills.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			//lstAccountBills.setSelection(ListView.INVALID_POSITION);
		}
		
		prgAccountBills = (ProgressBar)view.findViewById(R.id.progressAccountBills);
		
		return view;
	}
	
	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		switch (loader.getId()) {
		case ID_LOADER_ACCOUNT_BILLS:
			txtTotalAccounts.setText(currencyUtils.getBaseCurrencyFormatted(Double.valueOf(0)));
			lstAccountBills.setAdapter(null);
			setListViewAccountBillsVisible(false);
		}
	}
	
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		MainActivity mainActivity = null;
		if (getActivity() != null && getActivity() instanceof MainActivity) 
			mainActivity = (MainActivity)getActivity();
		
		switch (loader.getId()) {
		case ID_LOADER_USER_NAME:
			if (data != null && data.moveToFirst()) {
				while (data.isAfterLast() == false) {
					String infoValue = data.getString(data.getColumnIndex(infoTable.INFONAME));
					// save into preferences username and basecurrency id
					if (Constants.INFOTABLE_USERNAME.equalsIgnoreCase(infoValue)) {
						application.setUserName(data.getString(data.getColumnIndex(infoTable.INFOVALUE)));
					} else if (Constants.INFOTABLE_BASECURRENCYID.equalsIgnoreCase(infoValue)) {
						//application.setBaseCurrencyId(data.getInt(data.getColumnIndex(infoTable.INFOVALUE)));
					}
					data.moveToNext();
				}
			}
			// show username
			if (!TextUtils.isEmpty(application.getUserName())) 
				((FragmentActivity)getActivity()).getActionBar().setSubtitle(application.getUserName());
			// set user name on drawer
			if (mainActivity != null) 
				mainActivity.setDrawableUserName(application.getUserName());
			
			break;
			
		case ID_LOADER_ACCOUNT_BILLS:
			double curTotal = 0, curReconciled = 0;
			AccountBillsAdapter adapter = null;
			
			linearHome.setVisibility(data != null && data.getCount() > 0 ? View.VISIBLE : View.GONE);
			linearWelcome.setVisibility(linearHome.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
			
			// cycle cursor
			if (data != null && data.moveToFirst()) {
				while (data.isAfterLast() == false) {
					curTotal += data.getDouble(data.getColumnIndex(QueryAccountBills.TOTALBASECONVRATE));
					curReconciled += data.getDouble(data.getColumnIndex(QueryAccountBills.RECONCILEDBASECONVRATE));
					data.moveToNext();
				}
				// create adapter
				adapter = new AccountBillsAdapter(getActivity(), data);
			}
			// write accounts total
			txtTotalAccounts.setText(currencyUtils.getBaseCurrencyFormatted(curTotal));
			// manage footer listview
			if (linearFooter == null) {
				linearFooter = (LinearLayout)getActivity().getLayoutInflater().inflate(R.layout.item_account_bills, null);
				// textview into layout
				txtFooterSummary = (TextView)linearFooter.findViewById(R.id.textVievItemAccountTotal);
				txtFooterSummaryReconciled = (TextView)linearFooter.findViewById(R.id.textVievItemAccountTotalReconciled);
				// set text
				TextView txtTextSummary = (TextView)linearFooter.findViewById(R.id.textVievItemAccountName);
				txtTextSummary.setText(R.string.summary);
				// invisibile image
				ImageView imgSummary = (ImageView)linearFooter.findViewById(R.id.imageViewAccountType);
				imgSummary.setVisibility(View.INVISIBLE);
				// set color textview
				txtTextSummary.setTextColor(Color.GRAY);
				txtFooterSummary.setTextColor(Color.GRAY);
				txtFooterSummaryReconciled.setTextColor(Color.GRAY);
			}
			// remove footer
			lstAccountBills.removeFooterView(linearFooter);
			// set text
			txtFooterSummary.setText(txtTotalAccounts.getText());
			txtFooterSummaryReconciled.setText(currencyUtils.getBaseCurrencyFormatted(curReconciled));
			// add footer
			lstAccountBills.addFooterView(linearFooter, null, false);
			// set adapter and shown
			lstAccountBills.setAdapter(adapter);
			setListViewAccountBillsVisible(true);
			// set total accounts in drawer
			if (mainActivity != null) {
				mainActivity.setDrawableTotalAccounts(txtTotalAccounts.getText().toString());
			}
			break;
			
		case ID_LOADER_BILL_DEPOSITS:
			mainActivity.setDrawableRepeatingTransactions(data != null ? data.getCount() : 0);
			break;
			
		case ID_LOADER_INCOME_EXPENSES:
			double income = 0, expenses = 0;
			if (data != null && data.moveToFirst()) {
				while (!data.isAfterLast()) {
					expenses = data.getDouble(data.getColumnIndex(QueryReportIncomeVsExpenses.Expenses));
					income = data.getDouble(data.getColumnIndex(QueryReportIncomeVsExpenses.Income));
					//move to next record
					data.moveToNext();
				}
			}
			TextView txtIncome = (TextView)getActivity().findViewById(R.id.textViewIncome);
			TextView txtExpenses = (TextView)getActivity().findViewById(R.id.textViewExpenses);
			TextView txtDifference = (TextView)getActivity().findViewById(R.id.textViewDifference);
			// set value
			if (txtIncome != null)
				txtIncome.setText(currencyUtils.getCurrencyFormatted(currencyUtils.getBaseCurrencyId(), income));
			if (txtExpenses != null) 
				txtExpenses.setText(currencyUtils.getCurrencyFormatted(currencyUtils.getBaseCurrencyId(), Math.abs(expenses)));
			if (txtDifference != null)
				txtDifference.setText(currencyUtils.getCurrencyFormatted(currencyUtils.getBaseCurrencyId(), income - Math.abs(expenses)));
			// manage progressbar
			final ProgressBar barIncome = (ProgressBar)getActivity().findViewById(R.id.progressBarIncome);
			final ProgressBar barExpenses = (ProgressBar)getActivity().findViewById(R.id.progressBarExpenses);
			
			if (barIncome != null && barExpenses != null) {				
				barIncome.setMax((int) (Math.abs(income) + Math.abs(expenses)));
				barExpenses.setMax((int) (Math.abs(income) + Math.abs(expenses)));

				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB){
					ObjectAnimator animationIncome = ObjectAnimator.ofInt(barIncome, "progress", (int)Math.abs(income)); 
				    animationIncome.setDuration(1000); // 0.5 second
				    animationIncome.setInterpolator(new DecelerateInterpolator());
				    animationIncome.start();
				    
				    ObjectAnimator animationExpenses = ObjectAnimator.ofInt(barExpenses, "progress", (int)Math.abs(expenses)); 
				    animationExpenses.setDuration(1000); // 0.5 second
				    animationExpenses.setInterpolator(new DecelerateInterpolator());
				    animationExpenses.start();
				} else {
					barIncome.setProgress((int)Math.abs(income));
					barExpenses.setProgress((int)Math.abs(expenses));
				}
			}
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
	 * @param visible if visible set true show the listview; false show progressbar
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
