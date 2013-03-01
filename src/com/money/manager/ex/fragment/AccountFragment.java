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

import java.util.ArrayList;
import java.util.Calendar;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.money.manager.ex.CheckingAccountActivity;
import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.database.MoneyManagerOpenHelper;
import com.money.manager.ex.database.QueryAccountBills;
import com.money.manager.ex.database.QueryAllData;
import com.money.manager.ex.database.TableAccountList;
import com.money.manager.ex.fragment.AllDataFragment.AllDataFragmentLoaderCallbacks;

/**
 * 
 * @author a.lazzari
 * 
 */
public class AccountFragment extends SherlockFragment implements LoaderManager.LoaderCallbacks<Cursor>, AllDataFragmentLoaderCallbacks {

	private static final String KEY_CONTENT = "AccountFragment:AccountId";
	private static final int ID_LOADER_SUMMARY = 2;

	/**
	 * 
	 * @param accountid
	 *            ID Account to be display
	 * @return
	 */
	public static AccountFragment newIstance(int accountid) {
		AccountFragment fragment = new AccountFragment();
		fragment.mAccountId = accountid;
		// set name of child fragment
		fragment.mNameFragment = AllDataFragment.class.getSimpleName() + "_" + Integer.toString(accountid);

		return fragment;
	}

	// application
	MoneyManagerApplication mApplication;
	// id account
	private int mAccountId = 0;
	// string name fragment
	String mNameFragment;
	// account balance
	private float mAccountBalance = 0;
	private float mAccountReconciled = 0;
	// Dataset: accountlist e alldata
	private TableAccountList mAccountList;
	// view into layout
	private TextView txtAccountName, txtAccountBalance, txtAccountReconciled;
	private ImageView imgAccountFav;
	//
	AllDataFragment fragment;
	@Override
	public void onCallbackCreateLoader(int id, Bundle args) {
		return;
	}

	@Override
	public void onCallbackLoaderFinished(Loader<Cursor> loader, Cursor data) {
		getLoaderManager().restartLoader(ID_LOADER_SUMMARY, null, this);
	}

	@Override
	public void onCallbackLoaderReset(Loader<Cursor> loader) {
		return;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mApplication = (MoneyManagerApplication) getActivity().getApplication();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String selection = "";
		switch (id) {
		case ID_LOADER_SUMMARY:
			selection = QueryAccountBills.ACCOUNTID + "=?";
			return new CursorLoader(getActivity(), new QueryAccountBills(getActivity()).getUri(), null, selection,
					new String[] { Integer.toString(mAccountId) }, null);
		}
		return null;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, com.actionbarsherlock.view.MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		// item add
		MenuItem item = menu.add(10001, R.id.menu_add_transaction, 10001, R.string.new_transaction);
		item.setIcon(new Core(getActivity()).resolveIdAttribute(R.attr.ic_action_add));
		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		// find item
		MenuItem itemNew = menu.findItem(R.id.menu_new_transaction);
		if (itemNew != null) itemNew.setVisible(false);
		// call create option menu of fragment
		fragment.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_CONTENT)) {
			mAccountId = savedInstanceState.getInt(KEY_CONTENT);
		}
		if (container == null) {
			return null;
		}
		// inflate layout
		View view = (LinearLayout) inflater.inflate(R.layout.fragment_account, container, false);
		// take object AccountList
		if (mAccountList == null) {
			mAccountList = new MoneyManagerOpenHelper(getActivity()).getTableAccountList(mAccountId);
		}
		// take reference textview from layout
		txtAccountName = (TextView) view.findViewById(R.id.textViewAccountName);
		txtAccountBalance = (TextView) view.findViewById(R.id.textViewAccountBalance);
		txtAccountReconciled = (TextView) view.findViewById(R.id.textViewAccountReconciled);
		// favorite icon
		imgAccountFav = (ImageView) view.findViewById(R.id.imageViewAccountFav);
		// set listener click on favorite icon for change image
		imgAccountFav.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// set status account
				mAccountList.setFavoriteAcct(!(mAccountList.isFavoriteAcct()));
				// populate contentvalues for update
				ContentValues values = new ContentValues();
				values.put(TableAccountList.FAVORITEACCT, mAccountList.getFavoriteAcct());
				// update
				if (getActivity().getContentResolver().update(mAccountList.getUri(), values, TableAccountList.ACCOUNTID + "=?",
						new String[] { Integer.toString(mAccountId) }) != 1) {
					Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.db_update_failed), Toast.LENGTH_LONG).show();
				} else {
					setImageViewFavorite();
				}
			}
		});
		// manage fragment
		FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
		fragment = AllDataFragment.newInstance(mAccountId);
		// set arguments and settings of fragment
		fragment.setArguments(prepareArgsForChildFragment());
		fragment.setAutoStarLoader(false);
		fragment.setContextMenuGroupId(mAccountId);
		fragment.setSearResultFragmentLoaderCallbacks(this);
		// add fragment
		transaction.replace(R.id.fragmentContent, fragment, mNameFragment);
		transaction.commit();

		// refresh user interface
		if (mAccountList != null) {
			txtAccountName.setText(mAccountList.getAccountName());
			setImageViewFavorite();
		}
		// set has optionmenu
		setHasOptionsMenu(true);

		return view;
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		return;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		switch (loader.getId()) {
		case ID_LOADER_SUMMARY:
			if (data != null && data.moveToFirst()) {
				mAccountBalance = data.getFloat(data.getColumnIndex(QueryAccountBills.TOTAL));
				mAccountReconciled = data.getFloat(data.getColumnIndex(QueryAccountBills.RECONCILED));
			} else {
				mAccountBalance = 0;
				mAccountReconciled = 0;
			}
			// show balance values
			setTextViewBalance();
			break;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_add_transaction) {
			startCheckingAccountActivity();
			return true;
		} else if (item.getItemId() == R.id.menu_export_csv){
			fragment.exportDataToCSVFile(mAccountList.getAccountName());
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onResume() {
		super.onResume();
		// restart loader
		startLoaderData();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(KEY_CONTENT, mAccountId);
	}

	private Bundle prepareArgsForChildFragment() {
		// compose selection and sort
		ArrayList<String> selection = new ArrayList<String>();
		selection.add("(" + QueryAllData.ACCOUNTID + "=" + Integer.toString(mAccountId) + " OR " + QueryAllData.ToAccountID + "="
				+ Integer.toString(mAccountId) + ")");
		if (mApplication.getShowTransaction().equalsIgnoreCase(getString(R.string.last7days))) {
			selection.add("(julianday(date('now')) - julianday(" + QueryAllData.Date + ") <= 7)");
		} else if (mApplication.getShowTransaction().equalsIgnoreCase(getString(R.string.last15days))) {
			selection.add("(julianday(date('now')) - julianday(" + QueryAllData.Date + ") <= 14)");
		} else if (mApplication.getShowTransaction().equalsIgnoreCase(getString(R.string.current_month))) {
			selection.add(QueryAllData.Month + "=" + Integer.toString(Calendar.getInstance().get(Calendar.MONTH) + 1));
			selection.add(QueryAllData.Year + "=" + Integer.toString(Calendar.getInstance().get(Calendar.YEAR)));
		} else if (mApplication.getShowTransaction().equalsIgnoreCase(getString(R.string.current_year))) {
			selection.add(QueryAllData.Year + "=" + Integer.toString(Calendar.getInstance().get(Calendar.YEAR)));
		}
		// create a bundle to returns
		Bundle args = new Bundle();
		args.putStringArrayList(AllDataFragment.KEY_ARGUMENTS_WHERE, selection);
		args.putString(AllDataFragment.KEY_ARGUMENTS_SORT, QueryAllData.Date + " DESC, " + QueryAllData.ID + " DESC");

		return args;
	}

	/**
	 * refresh UI, show favorite icome
	 */
	@SuppressWarnings("deprecation")
	private void setImageViewFavorite() {
		if (mAccountList.isFavoriteAcct()) {
			imgAccountFav.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.star_on));
		} else {
			imgAccountFav.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.star_off));
		}
	}

	/**
	 * refresh user interface with total
	 */
	private void setTextViewBalance() {
		// write account balance
		if (mAccountList != null && mApplication != null) {
			Spanned balance = Html.fromHtml("<b>" + mApplication.getCurrencyFormatted(mAccountList.getCurrencyId(), mAccountBalance) + "</b>");
			txtAccountBalance.setText(balance);
			// write account reconciled
			Spanned reconciled = Html.fromHtml("<b>" + mApplication.getCurrencyFormatted(mAccountList.getCurrencyId(), mAccountReconciled) + "</b>");
			txtAccountReconciled.setText(reconciled);
		}
	}

	/**
	 * start the activity of transaction management
	 */
	private void startCheckingAccountActivity() {
		this.startCheckingAccountActivity(null);
	}

	/**
	 * start the activity of transaction management
	 * 
	 * @param transId
	 *            null set if you want to do a new transaction, or transaction id
	 */
	private void startCheckingAccountActivity(Integer transId) {
		// create intent, set Account ID
		Intent intent = new Intent(getActivity(), CheckingAccountActivity.class);
		intent.putExtra(CheckingAccountActivity.KEY_ACCOUNT_ID, mAccountId);
		// check transId not null
		if (transId != null) {
			intent.putExtra(CheckingAccountActivity.KEY_TRANS_ID, transId);
			intent.setAction(Intent.ACTION_EDIT);
		} else {
			intent.setAction(Intent.ACTION_INSERT);
		}
		// launch activity
		startActivity(intent);
	}

	/**
	 * Start Loader to retrive data
	 */
	public void startLoaderData() {
		if (fragment != null) {
			fragment.startLoaderData();
		}
	}
}
