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
package com.money.manager.ex;

import java.util.Date;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Html;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.money.manager.ex.database.QueryBillDeposits;
import com.money.manager.ex.database.TableAccountList;
import com.money.manager.ex.database.TableBillsDeposits;
import com.money.manager.ex.fragment.BaseFragmentActivity;
import com.money.manager.ex.fragment.BaseListFragment;

/**
 * 
 * @author Alessandro Lazzari (lazzari.ale@gmail.com)
 * 
 */
public class RepeatingTransactionListActivity extends BaseFragmentActivity {
	public static class RepeatingTransactionListFragment extends BaseListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
		private class RepeatingTransactionAdapter extends CursorAdapter {
			private LayoutInflater inflater;
			private MoneyManagerApplication mApplication;

			@SuppressWarnings("deprecation")
			public RepeatingTransactionAdapter(Context context, Cursor c) {
				super(context, c);
				inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				mApplication = (MoneyManagerApplication) getActivity().getApplication();
			}

			@Override
			public void bindView(View view, Context context, Cursor cursor) {
				// take a pointer of object UI
				ImageView imgClock = (ImageView) view.findViewById(R.id.imageViewClock);
				TextView txtDate = (TextView) view.findViewById(R.id.textViewDate);
				TextView txtRepeat = (TextView) view.findViewById(R.id.textViewRepeat);
				TextView txtNextDueDate = (TextView)view.findViewById(R.id.textViewNextDueDate);
				TextView txtAmount = (TextView) view.findViewById(R.id.textViewAmount);
				TextView txtAccountName = (TextView) view.findViewById(R.id.textViewAccountName);
				TextView txtPayee = (TextView) view.findViewById(R.id.textViewPayee);
				LinearLayout linearToAccount = (LinearLayout) view.findViewById(R.id.linearLayoutToAccount);
				TextView txtToAccountName = (TextView) view.findViewById(R.id.textViewToAccountName);
				TextView txtCategorySub = (TextView) view.findViewById(R.id.textViewCategorySub);
				TextView txtNotes = (TextView) view.findViewById(R.id.textViewNotes);
				// account name
				txtAccountName.setText(cursor.getString(cursor.getColumnIndex(QueryBillDeposits.ACCOUNTNAME)));
				// write data
				txtDate.setText(cursor.getString(cursor.getColumnIndex(QueryBillDeposits.USERNEXTOCCURRENCEDATE)));
				// take daysleft
				int daysLeft = cursor.getInt(cursor.getColumnIndex(QueryBillDeposits.DAYSLEFT));
				if (daysLeft == 0) {
					txtNextDueDate.setText(R.string.inactive);
				} else {
					txtNextDueDate.setText(Integer.toString(Math.abs(daysLeft)) + " " + getString(daysLeft > 0 ? R.string.days_remaining : R.string.days_overdue));
					imgClock.setVisibility(daysLeft < 0 ? View.VISIBLE : View.INVISIBLE);
				}
				txtRepeat.setText(mApplication.getRepeatAsString(cursor.getInt(cursor.getColumnIndex(QueryBillDeposits.REPEATS))));
				// take transaction amount
				float amount = cursor.getFloat(cursor.getColumnIndex(QueryBillDeposits.AMOUNT));
				// manage transfer and change amount sign
				if ((cursor.getString(cursor.getColumnIndex(QueryBillDeposits.TRANSCODE)) != null)
						&& (cursor.getString(cursor.getColumnIndex(QueryBillDeposits.TRANSCODE)).equals("Transfer"))) {
					if (cursor.getInt(cursor.getColumnIndex(QueryBillDeposits.ACCOUNTID)) != cursor.getInt(cursor.getColumnIndex(QueryBillDeposits.TOACCOUNTID))) {
						amount = -(amount); // -total
					} else if (cursor.getInt(cursor.getColumnIndex(QueryBillDeposits.TOACCOUNTID)) == cursor.getInt(cursor
							.getColumnIndex(QueryBillDeposits.TOACCOUNTID))) {
						amount = cursor.getFloat(cursor.getColumnIndex(QueryBillDeposits.TOTRANSAMOUNT)); // to account = account
					}
				}
				txtAmount.setText(mApplication.getCurrencyFormatted(cursor.getInt(cursor.getColumnIndex(QueryBillDeposits.CURRENCYID)), amount));
				// check amount sign
				txtAmount.setTextColor(getResources().getColor( amount > 0 ? R.color.holo_green_light : R.color.holo_red_light ));
				// compose payee description
				String payee = cursor.getString(cursor.getColumnIndex(QueryBillDeposits.PAYEENAME));
				// write payee
				if ((!TextUtils.isEmpty(payee))) {
					txtPayee.setText(payee);
					txtPayee.setVisibility(View.VISIBLE);
				} else {
					txtPayee.setVisibility(View.GONE);
				}
				// write ToAccountName
				if ((!TextUtils.isEmpty(cursor.getString(cursor.getColumnIndex(QueryBillDeposits.TOACCOUNTNAME))))) {
					txtToAccountName.setText(cursor.getString(cursor.getColumnIndex(QueryBillDeposits.TOACCOUNTNAME)));
					linearToAccount.setVisibility(View.VISIBLE);
				} else {
					linearToAccount.setVisibility(View.GONE);
				}
				// compose category description
				String categorySub = cursor.getString(cursor.getColumnIndex(QueryBillDeposits.CATEGNAME));
				// add if not null subcategory
				if (!(TextUtils.isEmpty(cursor.getString(cursor.getColumnIndex(QueryBillDeposits.SUBCATEGNAME))))) {
					categorySub += " : <i>" + cursor.getString(cursor.getColumnIndex(QueryBillDeposits.SUBCATEGNAME)) + "</i>";
				}
				// write category / subcategory format html
				if (TextUtils.isEmpty(categorySub) == false) {
					txtCategorySub.setText(Html.fromHtml(categorySub));
				} else {
					txtCategorySub.setText("");
				}
				// notes
				String notes = cursor.getString(cursor.getColumnIndex(QueryBillDeposits.NOTES));
				if (!TextUtils.isEmpty(notes)) {
					txtNotes.setText(Html.fromHtml("<i>" + notes + "</i>"));
				} else {
					txtNotes.setVisibility(View.GONE);
				}
			}

			@Override
			public View newView(Context context, Cursor cursor, ViewGroup parent) {
				return inflater.inflate(R.layout.item_bill_deposits, parent, false);
			}
		}
		// ID request to add repeating transaction
		private static final int REQUEST_ADD_REPEATING_TRANSACTION = 1001;
		private static final int REQUEST_ADD_TRANSACTION = 1002;
		// ID item menu add
		private static final int MENU_ITEM_ADD = 1;

		// filter
		private String mCurFilter;

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			// set listview
			setEmptyText(getActivity().getResources().getString(R.string.repeating_empty_transaction));
			setHasOptionsMenu(true);
			registerForContextMenu(getListView());
			getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);

			setListShown(false);
			// start loaderapplication.getSQLiteStringDate(date)
			getLoaderManager().initLoader(ID_LOADER_REPEATING, null, this);
		}
		
		@Override
		public boolean onContextItemSelected(android.view.MenuItem item) {
			String nextOccurrence;
			int repeats, bdId;
			Date date;
			MoneyManagerApplication application = (MoneyManagerApplication)getActivity().getApplication();
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
			// take cursor and move to position
			Cursor cursor = ((RepeatingTransactionAdapter) getListAdapter()).getCursor();
			if (cursor != null) {
				cursor.moveToPosition(info.position);
				// check item select
				switch (item.getItemId()) {
				case R.id.menu_enter_next_occurrence:
					nextOccurrence = cursor.getString(cursor.getColumnIndex(TableBillsDeposits.NEXTOCCURRENCEDATE));
					repeats = cursor.getInt(cursor.getColumnIndex(TableBillsDeposits.REPEATS));
					bdId = cursor.getInt(cursor.getColumnIndex(TableBillsDeposits.BDID));
					date = application.getDateFromString(nextOccurrence, MoneyManagerApplication.PATTERN_DB_DATE);
					date = application.getDateNextOccurence(date, repeats);
					if (date != null) {
						Intent intent = new Intent(getActivity(), CheckingAccountActivity.class);
						intent.setAction(CheckingAccountActivity.INTENT_ACTION_INSERT);
						intent.putExtra(CheckingAccountActivity.KEY_BDID_ID, bdId);
						intent.putExtra(CheckingAccountActivity.KEY_NEXT_OCCURRENCE, application.getSQLiteStringDate(date));
						// start for insert new transaction
						startActivityForResult(intent, REQUEST_ADD_TRANSACTION);
					}
					break;
				case R.id.menu_skip_next_occurrence:
					nextOccurrence = cursor.getString(cursor.getColumnIndex(TableBillsDeposits.NEXTOCCURRENCEDATE));
					repeats = cursor.getInt(cursor.getColumnIndex(TableBillsDeposits.REPEATS));
					bdId = cursor.getInt(cursor.getColumnIndex(TableBillsDeposits.BDID));
					date = application.getDateFromString(nextOccurrence, MoneyManagerApplication.PATTERN_DB_DATE);
					date = application.getDateNextOccurence(date, repeats);
					if (date != null) {
						ContentValues values = new ContentValues();
						values.put(TableBillsDeposits.NEXTOCCURRENCEDATE, application.getSQLiteStringDate(date));
						// update date
						if (getActivity().getContentResolver().update(new TableBillsDeposits().getUri(), values, TableBillsDeposits.BDID + "=?", new String[] {Integer.toString(bdId)}) > 0) {
							getLoaderManager().restartLoader(ID_LOADER_REPEATING, null, this);
						} else {
							Toast.makeText(getActivity(), R.string.db_update_failed, Toast.LENGTH_SHORT);
						}	
					}
					break;
				case R.id.menu_edit: // EDIT
					startRepeatingTransactionActivity(cursor.getInt(cursor.getColumnIndex(TableBillsDeposits.BDID)));
					break;
				case R.id.menu_delete: // DELETE
					showDialogDeleteRepeatingTransaction(cursor.getInt(cursor.getColumnIndex(TableBillsDeposits.BDID)));
					break;
				}
			}
			return false;
		}

		private void showDialogDeleteRepeatingTransaction(final int BDID) {
			// create alert dialog
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
			alertDialog.setTitle(R.string.delete_account);
			alertDialog.setMessage(R.string.confirmDelete);
			// set listener
			alertDialog.setPositiveButton(android.R.string.ok,
					new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (getActivity().getContentResolver().delete(new TableBillsDeposits().getUri(), TableBillsDeposits.BDID + "=" + BDID, null) == 0) {
								Toast.makeText(getActivity(), R.string.db_delete_failed, Toast.LENGTH_SHORT).show();
							}
							// restart loader
							getLoaderManager().restartLoader(ID_LOADER_REPEATING, null, RepeatingTransactionListFragment.this);
						}
					});
			alertDialog.setNegativeButton(android.R.string.cancel, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			alertDialog.create().show();
		}
		
		@Override
		public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			// take a cursor and move to position
			Cursor cursor = ((RepeatingTransactionAdapter) getListAdapter()).getCursor();
			if (cursor != null) {
				cursor.moveToPosition(info.position);
				// set title and inflate menu
				menu.setHeaderTitle(getActivity().getTitle());
				getActivity().getMenuInflater().inflate(R.menu.contextmenu_repeating_transactions, menu);
			}
		}

		@Override
		public Loader<Cursor> onCreateLoader(int id, Bundle args) {
			switch (id) {
			case ID_LOADER_REPEATING:
				String select = null;
				if (!TextUtils.isEmpty(mCurFilter)) {
					select = TableAccountList.ACCOUNTNAME + " LIKE '" + mCurFilter + "%'";
				}
				return new CursorLoader(getActivity(), mBillDeposits.getUri(), mBillDeposits.getAllColumns(), select, null, QueryBillDeposits.NEXTOCCURRENCEDATE);
			}

			return null;
		}
		
		@Override
		public void onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu, MenuInflater inflater) {
			super.onCreateOptionsMenu(menu, inflater);
			// add menu item add
			MenuItem itemadd = menu.add(0, MENU_ITEM_ADD, MENU_ITEM_ADD, R.string.add);
			itemadd.setIcon(android.R.drawable.ic_menu_add);
			itemadd.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
		}

		@Override
		public void onLoaderReset(Loader<Cursor> loader) {
			switch (loader.getId()) {
			case ID_LOADER_REPEATING:
			}
		}

		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
			switch (loader.getId()) {
			case ID_LOADER_REPEATING:
				RepeatingTransactionAdapter adapter = new RepeatingTransactionAdapter(getActivity(), data);
				setListAdapter(adapter);

				if (isResumed()) {
					setListShown(true);
				} else {
					setListShownNoAnimation(true);
				}
			}
		}

		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			switch (item.getItemId()) {
			case android.R.id.home:
				break;
			case MENU_ITEM_ADD:
				startRepeatingTransactionActivity();
				break;
			}
			return super.onOptionsItemSelected(item);
		}

		@Override
		public boolean onQueryTextChange(String newText) {
			// Called when the action bar search text has changed. Update
			// the search filter, and restart the loader to do a new query
			// with this filter.
			mCurFilter = !TextUtils.isEmpty(newText) ? newText : null;
			getLoaderManager().restartLoader(ID_LOADER_REPEATING, null, this);
			return true;
		}
		/**
		 * start RepeatingTransaction Activity for insert
		 */
		private void startRepeatingTransactionActivity() {
			startRepeatingTransactionActivity(null);
		}
		/**
		 * start RepeatingTransaction for insert or edit transaction
		 * @param billDepositsId
		 */
		private void startRepeatingTransactionActivity(Integer billDepositsId) {
			// create intent, set Bill Deposits ID
			Intent intent = new Intent(getActivity(), RepeatingTransactionActivity.class);
			// check transId not null
			if (billDepositsId != null) {
				intent.putExtra(RepeatingTransactionActivity.KEY_BILL_DEPOSITS_ID, billDepositsId);
				intent.setAction(Intent.ACTION_EDIT);
			} else {
				intent.setAction(Intent.ACTION_INSERT);
			}
			// launch activity
			startActivityForResult(intent, REQUEST_ADD_REPEATING_TRANSACTION);
		}
		@Override
		public void onActivityResult(int requestCode, int resultCode, Intent data) {
			super.onActivityResult(requestCode, resultCode, data);
			if (resultCode == RESULT_OK) {
				switch (requestCode) {
				case REQUEST_ADD_REPEATING_TRANSACTION:
				case REQUEST_ADD_TRANSACTION:
					getLoaderManager().restartLoader(ID_LOADER_REPEATING, null, this);
				}
			}
		}
	}
	@SuppressWarnings("unused")
	private static final String LOGCAT = RepeatingTransactionListActivity.class.getSimpleName();
	private static final String FRAGMENTTAG = RepeatingTransactionListActivity.class.getSimpleName() + "_Fragment";
	public static final String INTENT_RESULT_ACCOUNTID = "AccountListActivity:ACCOUNTID";
	public static final String INTENT_RESULT_ACCOUNTNAME = "AccountListActivity:ACCOUNTNAME";

	// ID loader
	private static final int ID_LOADER_REPEATING = 0;
	private RepeatingTransactionListFragment listFragment;

	// query
	private static QueryBillDeposits mBillDeposits;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// create a object query
		mBillDeposits = new QueryBillDeposits(this);
		// set actionbar
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		// set fragment and fragment manager
		FragmentManager fm = getSupportFragmentManager();
		listFragment = new RepeatingTransactionListFragment();
		// attach fragment on activity
		if (fm.findFragmentById(android.R.id.content) == null) {
			fm.beginTransaction().add(android.R.id.content, listFragment, FRAGMENTTAG).commit();
		}
	}
}
