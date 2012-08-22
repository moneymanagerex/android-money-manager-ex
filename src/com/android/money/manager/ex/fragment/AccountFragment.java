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

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.money.manager.ex.CheckingAccountActivity;
import com.android.money.manager.ex.MoneyManagerApplication;
import com.android.money.manager.ex.R;
import com.android.money.manager.ex.database.MoneyManagerOpenHelper;
import com.android.money.manager.ex.database.TableAccountList;
import com.android.money.manager.ex.database.TableCheckingAccount;
import com.android.money.manager.ex.database.ViewAllData;
/**
 * 
 * @author a.lazzari
 *
 */
public class AccountFragment extends Fragment implements
		LoaderManager.LoaderCallbacks<Cursor> {
	private static final String KEY_CONTENT = "AccountFragment:AccountId";
	private static final int ID_LOADER_ALLDATA = 1;
	// context menu
	private static final int CONTEXT_MENU_EDIT = 1;
	private static final int CONTEXT_MENU_DELETE = 2;
	// option menu
	private static final int MENU_ADD_TRANSACTION = 1000;
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
	private ViewAllData mAllData = new ViewAllData();
	// view into layout
	private TextView txtAccountName, txtAccountBalance, txtAccountReconciled; 
	private ImageView imgAccountFav;
	private ListView lstAllData;
	private ProgressBar prgAllData;
	private Button btnNewOperation;
	
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
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mApplication = (MoneyManagerApplication)getActivity().getApplication();
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
				values.put(mAccountList.FAVORITEACCT, mAccountList.getFavoriteAcct());
				// update
				if (getActivity().getContentResolver().update(mAccountList.getUri(), values, mAccountList.ACCOUNTID + "=?", new String[] {Integer.toString(mAccountId)}) != 1) {
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
		btnNewOperation.setVisibility(View.GONE);
		// set listview register for context men�
		registerForContextMenu(lstAllData);
		// set has optionmenu
		setHasOptionsMenu(true);
		
		return view;
	}
	@Override
	public void onResume() {
		super.onResume();
		// take object AccountList
		mAccountList = new MoneyManagerOpenHelper(getActivity()).getTableAccountList(mAccountId);

		if (mAccountList != null) {
			// refresh user interface
			txtAccountName.setText(mAccountList.getAccountName());
			setImageViewFavorite();
			// start loader manager
			getLoaderManager().restartLoader(ID_LOADER_ALLDATA, null, this);
		}
		// menu
		/*if ((isVisible()) && (Build.VERSION.SDK_INT >= 11)) {
			getActivity().invalidateOptionsMenu();
		}*/
	}
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		// item add
        MenuItem itemadd = menu.add(MENU_ADD_TRANSACTION, MENU_ADD_TRANSACTION, MENU_ADD_TRANSACTION, R.string.new_transaction);
        itemadd.setIcon(android.R.drawable.ic_menu_add);
        itemadd.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM|MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
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
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(KEY_CONTENT, mAccountId);
	}
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		switch (id) {
		case ID_LOADER_ALLDATA:
			setListViewAllDataVisible(false);
			String selection = ViewAllData.ACCOUNTID + "=" + Integer.toString(mAccountId) + " OR " + ViewAllData.ToAccountID + "=" + Integer.toString(mAccountId);
			return new CursorLoader(getActivity(), mAllData.getUri(), mAllData.getAllColumns(), selection, null, "DATE DESC, ID DESC");
		}
		return null;
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
			// calculate balance
			setBalanceAccount(data);
		}
	}
	
	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		lstAllData.setAdapter(null);
		// refresh user interface: hide listview, show progressbar
		setListViewAllDataVisible(false);
		getActivity().setProgressBarIndeterminateVisibility(true);
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
		menu.findItem(R.id.menu_reconciled).setVisible(cursor.getString(cursor.getColumnIndex(ViewAllData.Status)).equalsIgnoreCase("R") == false);
		menu.findItem(R.id.menu_none).setVisible(TextUtils.isEmpty(cursor.getString(cursor.getColumnIndex(ViewAllData.Status))) == false);
		menu.findItem(R.id.menu_duplicate).setVisible(cursor.getString(cursor.getColumnIndex(ViewAllData.Status)).equalsIgnoreCase("D") == false);
		menu.findItem(R.id.menu_follow_up).setVisible(cursor.getString(cursor.getColumnIndex(ViewAllData.Status)).equalsIgnoreCase("F") == false);
		menu.findItem(R.id.menu_void).setVisible(cursor.getString(cursor.getColumnIndex(ViewAllData.Status)).equalsIgnoreCase("V") == false);
		
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// take a info of the selected menu, and cursor at position 
		AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
		Cursor cursor = (Cursor)lstAllData.getAdapter().getItem(info.position);
		// check if cursor is valid
		if (cursor == null) {
			return false;
		}
		switch (item.getItemId()) {
		case R.id.menu_edit:
			startCheckingAccountActivity(cursor.getInt(cursor.getColumnIndex(ViewAllData.ID)));
			break;
		case R.id.menu_delete:
			showDialogDeleteCheckingAccount(cursor.getInt(cursor.getColumnIndex(ViewAllData.ID)));
			break;
		case R.id.menu_reconciled:
			setStatusCheckingAccount(lstAllData.getFirstVisiblePosition(), cursor.getInt(cursor.getColumnIndex(ViewAllData.ID)), "R");
			break;
		case R.id.menu_none:
			setStatusCheckingAccount(lstAllData.getFirstVisiblePosition(), cursor.getInt(cursor.getColumnIndex(ViewAllData.ID)), "");
			break;
		case R.id.menu_duplicate:
			setStatusCheckingAccount(lstAllData.getFirstVisiblePosition(), cursor.getInt(cursor.getColumnIndex(ViewAllData.ID)), "D");
			break;
		case R.id.menu_follow_up:
			setStatusCheckingAccount(lstAllData.getFirstVisiblePosition(), cursor.getInt(cursor.getColumnIndex(ViewAllData.ID)), "F");
			break;
		case R.id.menu_void:
			setStatusCheckingAccount(lstAllData.getFirstVisiblePosition(), cursor.getInt(cursor.getColumnIndex(ViewAllData.ID)), "V");
			break;
		}
		return false;
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
	 * Calcola gli importi del conto
	 * @param cursor
	 */
	@SuppressWarnings("static-access")
	private void setBalanceAccount(Cursor cursor) {
		// init balance's with InitialBalance from account
		mAccountBalance = (float) mAccountList.getInitialBal();
		mAccountReconciled = (float) mAccountList.getInitialBal();
		if (cursor != null && cursor.moveToLast()) {
			while (cursor.isBeforeFirst() == false) {
				float amount = cursor.getFloat(cursor.getColumnIndex(mAllData.Amount));
				Integer toAccountId = cursor.getInt(cursor.getColumnIndex(mAllData.ToAccountID));  
				if ((cursor.getString(cursor.getColumnIndex(mAllData.TransactionType)) != null) &&
					(cursor.getString(cursor.getColumnIndex(mAllData.TransactionType)).equals("Transfer"))) { 
					if (mAccountList.getAccountId() != cursor.getInt(cursor.getColumnIndex(mAllData.ToAccountID))) { 
						amount = -(amount);
					} else if(mAccountList.getAccountId() == cursor.getInt(cursor.getColumnIndex(mAllData.ToAccountID))) {
						amount = cursor.getFloat(cursor.getColumnIndex(mAllData.TOTRANSAMOUNT));
					}
				}
				if ((cursor.getString(cursor.getColumnIndex(mAllData.TransactionType)) != null) &&
					(cursor.getString(cursor.getColumnIndex(mAllData.Status)).equals("R"))) {
					// reconciled value
					mAccountBalance += amount;
					mAccountReconciled += amount;
				} else if ((cursor.getString(cursor.getColumnIndex(mAllData.TransactionType)) != null) &&
						   (cursor.getString(cursor.getColumnIndex(mAllData.Status)).equals("V"))) {
					// null void
				} else {
					// value is not reconciled
					mAccountBalance += amount;
				}
				cursor.moveToPrevious();
			}
		}
		getActivity().setProgressBarIndeterminateVisibility(false);
		// show balance values
		setTextViewBalance();
	}
	/**
	 * refersh UI con i saldi riconciliati istanziati
	 */
	private void setTextViewBalance() {
		// write account balance
		Spanned balance = Html.fromHtml("<b>" + mApplication.getCurrencyFormatted(mAccountList.getCurrencyId(), mAccountBalance) + "</b>"); 
		txtAccountBalance.setText(balance);
		// write account reconciled
		Spanned reconciled = Html.fromHtml("<b>" + mApplication.getCurrencyFormatted(mAccountList.getCurrencyId(), mAccountReconciled) + "</b>");
		txtAccountReconciled.setText(reconciled);
	}
	/**
	 * referesh UI, show favicon
	 */
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
						getLoaderManager().restartLoader(ID_LOADER_ALLDATA, null, AccountFragment.this);
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
			getLoaderManager().restartLoader(ID_LOADER_ALLDATA, null, this);
			return true;
		}
	}
	
	private class AllDataAdapter extends CursorAdapter {
		private LayoutInflater inflater;
		
		public AllDataAdapter(Context context, Cursor c) {
			super(context, c);
			inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			return inflater.inflate(R.layout.item_alldata_account, parent, false);
		}
		
		@SuppressWarnings({ "static-access", "unused" })
		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			// take a pointer of object UI
			TextView txtDate = (TextView)view.findViewById(R.id.textViewDate);
			TextView txtStatus = (TextView)view.findViewById(R.id.textViewStatus);
			TextView txtDeposit = (TextView)view.findViewById(R.id.textViewDeposit);
			TextView txtTransfer = (TextView)view.findViewById(R.id.textViewTransfer);
			TextView txtPayee = (TextView)view.findViewById(R.id.textViewPayee);
			LinearLayout linearToAccount = (LinearLayout)view.findViewById(R.id.linearLayoutToAccount);
			TextView txtToAccountName = (TextView)view.findViewById(R.id.textViewToAccountName);
			TextView txtCategorySub = (TextView)view.findViewById(R.id.textViewCategorySub);
			// write status and date
			txtDate.setText(cursor.getString(cursor.getColumnIndex(mAllData.UserDate)));
			txtStatus.setText(mApplication.getStatusAsString(cursor.getString(cursor.getColumnIndex(mAllData.Status))));
			// take transation amount
			float amount = cursor.getFloat(cursor.getColumnIndex(mAllData.Amount));
			// manage transfer and change amount sign
			if ((cursor.getString(cursor.getColumnIndex(mAllData.TransactionType)) != null) &&
				(cursor.getString(cursor.getColumnIndex(mAllData.TransactionType)).equals("Transfer")))  {
				if (mAccountList.getAccountId() != cursor.getInt(cursor.getColumnIndex(mAllData.ToAccountID))) {
					amount = -(amount); // giro di segno l'importo
				} else if (mAccountList.getAccountId() == cursor.getInt(cursor.getColumnIndex(mAllData.ToAccountID))) {
					amount = cursor.getFloat(cursor.getColumnIndex(mAllData.TOTRANSAMOUNT)); // conto di destino uguale
				}
			}
			// check amount sign
			if (amount > 0) {
				txtDeposit.setText(mApplication.getCurrencyFormatted(cursor.getInt(cursor.getColumnIndex(mAllData.CURRENCYID)), amount));
				txtTransfer.setText(null);
			} else {
				txtTransfer.setText(mApplication.getCurrencyFormatted(cursor.getInt(cursor.getColumnIndex(mAllData.CURRENCYID)), amount));
				txtDeposit.setText(null);
			}
			// compose payee description
			String payee = cursor.getString(cursor.getColumnIndex(mAllData.Payee));
			// write payee
			if ((!TextUtils.isEmpty(payee))) {
				txtPayee.setText(payee);
				txtPayee.setVisibility(View.VISIBLE);
			} else {
				txtPayee.setVisibility(View.GONE);
			}
			// write ToAccountName
			if ((!TextUtils.isEmpty(cursor.getString(cursor.getColumnIndex(mAllData.ToAccountName))))) {
				txtToAccountName.setText(cursor.getString(cursor.getColumnIndex(mAllData.ToAccountName)));
				linearToAccount.setVisibility(View.VISIBLE);
			} else {
				linearToAccount.setVisibility(View.GONE);
			}
			// compose category description
			String categorySub = cursor.getString(cursor.getColumnIndex(mAllData.Category));
			// controllo se ho anche la subcategory
			if (!(TextUtils.isEmpty(cursor.getString(cursor.getColumnIndex(mAllData.Subcategory))))) {
				categorySub += " : <i>" + cursor.getString(cursor.getColumnIndex(mAllData.Subcategory)) + "</i>";
			}
			// write category/subcategory format html
			if (TextUtils.isEmpty(categorySub) == false) {
				txtCategorySub.setText(Html.fromHtml(categorySub));
			} else {
				txtCategorySub.setText("");
			}
		}
	}
}
