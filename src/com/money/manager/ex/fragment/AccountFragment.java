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

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.money.manager.ex.CheckingAccountActivity;
import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.core.AllDataAdapter;
import com.money.manager.ex.database.MoneyManagerOpenHelper;
import com.money.manager.ex.database.QueryAccountBills;
import com.money.manager.ex.database.QueryAllData;
import com.money.manager.ex.database.TableAccountList;
import com.money.manager.ex.database.TableCheckingAccount;
/**
 * 
 * @author a.lazzari
 *
 */
public class AccountFragment extends SherlockFragment implements
		LoaderManager.LoaderCallbacks<Cursor> {

	private static final String KEY_CONTENT = "AccountFragment:AccountId";
	private static final int ID_LOADER_ALLDATA = 1;
	private static final int ID_LOADER_SUMMARY = 2;
	// option menu
	private static final int MENU_ADD_TRANSACTION = 1000;
	/**
	 * 
	 * @param accountid ID Account to be display
	 * @return
	 */
	public static AccountFragment newIstance(int accountid) {
		AccountFragment fragment = new AccountFragment();
		fragment.mAccountId = accountid;
		return fragment;
	}
	// application
	MoneyManagerApplication mApplication;
	// id account
	private int mAccountId = 0;
	// account balance
	private float mAccountBalance = 0;
	private float mAccountReconciled = 0;
	// current position
	private Integer mCurrentPosition = null;
	// Dataset: accountlist e alldata
	private TableAccountList mAccountList; 
	private QueryAllData mAllData;
	// view into layout
	private TextView txtAccountName, txtAccountBalance, txtAccountReconciled;
	private ImageView imgAccountFav;
	private ListView lstAllData;
	
	private ProgressBar prgAllData;
	
	private Button btnNewOperation;
	
	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		// take a info of the selected menu, and cursor at position 
		AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
		Cursor cursor = (Cursor)lstAllData.getAdapter().getItem(info.position);
		// check if cursor is valid
		if (cursor != null) {
			switch (item.getItemId()) {
			case R.id.menu_edit:
				startCheckingAccountActivity(cursor.getInt(cursor.getColumnIndex(QueryAllData.ID)));
				break;
			case R.id.menu_delete:
				showDialogDeleteCheckingAccount(cursor.getInt(cursor.getColumnIndex(QueryAllData.ID)));
				break;
			case R.id.menu_reconciled:
				setStatusCheckingAccount(lstAllData.getFirstVisiblePosition(), cursor.getInt(cursor.getColumnIndex(QueryAllData.ID)), "R");
				break;
			case R.id.menu_none:
				setStatusCheckingAccount(lstAllData.getFirstVisiblePosition(), cursor.getInt(cursor.getColumnIndex(QueryAllData.ID)), "");
				break;
			case R.id.menu_duplicate:
				setStatusCheckingAccount(lstAllData.getFirstVisiblePosition(), cursor.getInt(cursor.getColumnIndex(QueryAllData.ID)), "D");
				break;
			case R.id.menu_follow_up:
				setStatusCheckingAccount(lstAllData.getFirstVisiblePosition(), cursor.getInt(cursor.getColumnIndex(QueryAllData.ID)), "F");
				break;
			case R.id.menu_void:
				setStatusCheckingAccount(lstAllData.getFirstVisiblePosition(), cursor.getInt(cursor.getColumnIndex(QueryAllData.ID)), "V");
				break;
			}
		}
		return true;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mApplication = (MoneyManagerApplication)getActivity().getApplication();
		mAllData = new QueryAllData(getActivity());
	}
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		// take info and cursor from listview adapter 
		AdapterContextMenuInfo info = (AdapterContextMenuInfo)menuInfo;
		Cursor cursor = (Cursor)lstAllData.getAdapter().getItem(info.position);
		// check if cursor is valid
		if (cursor == null) {
			return;
		}
		getActivity().getMenuInflater().inflate(R.menu.contextmenu_accountfragment, menu);
		// create a context menu
		menu.setHeaderTitle(mAccountList.getAccountName());
		// hide current status
		menu.findItem(R.id.menu_reconciled).setVisible(cursor.getString(cursor.getColumnIndex(QueryAllData.Status)).equalsIgnoreCase("R") == false);
		menu.findItem(R.id.menu_none).setVisible(TextUtils.isEmpty(cursor.getString(cursor.getColumnIndex(QueryAllData.Status))) == false);
		menu.findItem(R.id.menu_duplicate).setVisible(cursor.getString(cursor.getColumnIndex(QueryAllData.Status)).equalsIgnoreCase("D") == false);
		menu.findItem(R.id.menu_follow_up).setVisible(cursor.getString(cursor.getColumnIndex(QueryAllData.Status)).equalsIgnoreCase("F") == false);
		menu.findItem(R.id.menu_void).setVisible(cursor.getString(cursor.getColumnIndex(QueryAllData.Status)).equalsIgnoreCase("V") == false);
		
	}
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String selection = "";
		String sort = null;
		switch (id) {
		case ID_LOADER_ALLDATA:
			setListViewAllDataVisible(false);
			// compose selection and sort
			selection = "(" + QueryAllData.ACCOUNTID + "=" + Integer.toString(mAccountId) + " OR " + QueryAllData.ToAccountID + "=" + Integer.toString(mAccountId) + ")";
			if (mApplication.getShowTransaction().equalsIgnoreCase(getString(R.string.last7days))) {
				selection += " AND (julianday(date('now')) - julianday(" + QueryAllData.Date + ") <= 7)";
			} else if (mApplication.getShowTransaction().equalsIgnoreCase(getString(R.string.last15days))) {
				selection += " AND (julianday(date('now')) - julianday(" + QueryAllData.Date + ") <= 14)";
			} else if (mApplication.getShowTransaction().equalsIgnoreCase(getString(R.string.current_month))) {
				selection += " AND " + QueryAllData.Month + "=" + Integer.toString(Calendar.getInstance().get(Calendar.MONTH) + 1);
				selection += " AND " + QueryAllData.Year + "=" + Integer.toString(Calendar.getInstance().get(Calendar.YEAR));
			} else if (mApplication.getShowTransaction().equalsIgnoreCase(getString(R.string.current_year))) {
				selection += " AND " + QueryAllData.Year + "=" + Integer.toString(Calendar.getInstance().get(Calendar.YEAR));
			}
			// set sort
			sort = QueryAllData.Date + " DESC, " + QueryAllData.ID + " DESC";
			// create loader
			return new CursorLoader(getActivity(), mAllData.getUri(), mAllData.getAllColumns(), selection, null, sort);
		/*case ID_LOADER_TEST:
			selection = "(" + TableCheckingAccount.ACCOUNTID + "=? OR " + TableCheckingAccount.TOACCOUNTID + "=?)";
			return new CursorLoader(getActivity(), new TableCheckingAccount().getUri(), new TableCheckingAccount().getAllColumns(), selection, new String[] {Integer.toString(mAccountId), Integer.toString(mAccountId)}, "TRANSDATE DESC, TRANSID DESC");*/
		case ID_LOADER_SUMMARY:
			selection= QueryAccountBills.ACCOUNTID + "=?";
			return new CursorLoader(getActivity(), new QueryAccountBills(getActivity()).getUri(), null, selection, new String[] {Integer.toString(mAccountId)}, null);
		}
		return null;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, com.actionbarsherlock.view.MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		// item add
        MenuItem itemadd = menu.add(MENU_ADD_TRANSACTION, MENU_ADD_TRANSACTION, MENU_ADD_TRANSACTION, R.string.new_transaction);
        itemadd.setIcon(android.R.drawable.ic_menu_add);
        itemadd.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM|MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_CONTENT)) {
			mAccountId = savedInstanceState.getInt(KEY_CONTENT);
		}
		if (container == null) { return null; }
		// inflate layout
		View view = (LinearLayout)inflater.inflate(R.layout.fragment_account, container, false);
		// take object AccountList
		if (mAccountList == null) {
			mAccountList = new MoneyManagerOpenHelper(getActivity()).getTableAccountList(mAccountId);
		}
		// take reference textview from layout
		txtAccountName = (TextView)view.findViewById(R.id.textViewAccountName);
		txtAccountBalance = (TextView)view.findViewById(R.id.textViewAccountBalance);
		txtAccountReconciled = (TextView)view.findViewById(R.id.textViewAccountReconciled);
		// list view e progressbar
		lstAllData = (ListView)view.findViewById(R.id.listViewAllData);
		prgAllData = (ProgressBar)view.findViewById(R.id.progressBarAllData);
		// favorite icon
		imgAccountFav = (ImageView)view.findViewById(R.id.imageViewAccountFav);
		// set listener click on favorite icon for change image
		imgAccountFav.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// set status account
				mAccountList.setFavoriteAcct(!(mAccountList.isFavoriteAcct()));
				// populate contentvalues for update
				ContentValues values = new ContentValues();
				values.put(TableAccountList.FAVORITEACCT, mAccountList.getFavoriteAcct());
				// update
				if (getActivity().getContentResolver().update(mAccountList.getUri(), values, TableAccountList.ACCOUNTID + "=?", new String[] {Integer.toString(mAccountId)}) != 1) {
					Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.db_update_failed), Toast.LENGTH_LONG).show();
				} else {
					setImageViewFavorite();
				}
			}
		});
		// button for new operation
		btnNewOperation = (Button)view.findViewById(R.id.buttonNewOperation);
		// set on button listener for start activity CheckingAccountActivity
		btnNewOperation.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// start CheckingAccountActivity
				startCheckingAccountActivity();
			}
		});
		// refresh user interface
		if (mAccountList != null) {
			txtAccountName.setText(mAccountList.getAccountName());
			setImageViewFavorite();
		}
		// hide button deprecated
		btnNewOperation.setVisibility(View.GONE);
		// set listview register for context men�
		registerForContextMenu(lstAllData);
		// set has optionmenu
		setHasOptionsMenu(true);
		
		return view;
	}
	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		lstAllData.setAdapter(null);
		// refresh user interface: hide listview, show progressbar
		setListViewAllDataVisible(false);
		getActivity().setProgressBarIndeterminateVisibility(true);
	}
	
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		switch (loader.getId()) {
		case ID_LOADER_ALLDATA:
			// create adpater from return data
			AllDataAdapter adapter = new AllDataAdapter(getActivity(), data);
			lstAllData.setAdapter(adapter);
			// show listview, hide progressbar
			setListViewAllDataVisible(true);
			// check set old position
			if (mCurrentPosition != null) {
				lstAllData.setSelectionFromTop(mCurrentPosition, 0);
				mCurrentPosition = null;
			}
			break;
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
		switch (item.getItemId()) {
		case MENU_ADD_TRANSACTION:
			startCheckingAccountActivity();
		}
		return false;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		// restart loader
		restartLoader();
	}
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(KEY_CONTENT, mAccountId);
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
	 * @param if visible set true show the listview; false show progresbar
	 */
	private void setListViewAllDataVisible(boolean visible) {
		if (visible) {
			lstAllData.setVisibility(View.VISIBLE);
			prgAllData.setVisibility(View.GONE);
		} else {
			lstAllData.setVisibility(View.GONE);
			prgAllData.setVisibility(View.VISIBLE);
		}
	}
	private boolean setStatusCheckingAccount(int position, int transId, String status) {
		// content value for updates
		ContentValues values = new ContentValues();
		// set new state
		values.put(TableCheckingAccount.STATUS, status);
		
		// update
		if (getActivity().getContentResolver().update(new TableCheckingAccount().getUri(), values, TableCheckingAccount.TRANSID + "=?", new String[] {Integer.toString(transId)}) <= 0) {
			Toast.makeText(getActivity(), R.string.db_update_failed, Toast.LENGTH_LONG).show();
			return false;
		} else {
			// save current position
			mCurrentPosition = position;
			// reload data
			restartLoader();
			return true;
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
	 * 
	 * @param transId primary key of transation
	 */
	private void showDialogDeleteCheckingAccount(final int transId) {
		// create alert dialog and set title and message
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

		alertDialog.setTitle(R.string.delete_transaction);
		alertDialog.setMessage(R.string.confirmDelete);
		alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
		
		// set listener button positive
		alertDialog.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						TableCheckingAccount trans = new TableCheckingAccount();
						if (getActivity().getContentResolver().delete(
								trans.getUri(),
								TableCheckingAccount.TRANSID + "=?",
								new String[] {Integer.toString(transId)}) == 0) {
							Toast.makeText(getActivity(), R.string.db_delete_failed, Toast.LENGTH_SHORT).show();
						}
						// restart loader
						restartLoader();
					}
				});
		// set listener negative button
		alertDialog.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// close dialog
				dialog.cancel();
			}
		});

		alertDialog.create();
		alertDialog.show();
	}
	/**
	 * start the activity of transaction management
	 */
	private void startCheckingAccountActivity() {
		this.startCheckingAccountActivity(null);
	}
	
	/**
	 * start the activity of transaction management
	 * @param transId null set if you want to do a new transaction, or transaction id
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
	 * start loader of fragment
	 */
	public void restartLoader() {
		getLoaderManager().restartLoader(ID_LOADER_ALLDATA, null, this);
		//getLoaderManager().initLoader(ID_LOADER_TEST, null, this);
		getLoaderManager().restartLoader(ID_LOADER_SUMMARY, null, this);
	}
}
