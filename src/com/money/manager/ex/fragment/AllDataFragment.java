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
package com.money.manager.ex.fragment;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Toast;
import au.com.bytecode.opencsv.CSVWriter;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.money.manager.ex.CheckingAccountActivity;
import com.money.manager.ex.R;
import com.money.manager.ex.SearchActivity;
import com.money.manager.ex.core.AllDataAdapter;
import com.money.manager.ex.database.MoneyManagerOpenHelper;
import com.money.manager.ex.database.QueryAllData;
import com.money.manager.ex.database.TableCheckingAccount;

public class AllDataFragment extends BaseListFragment implements LoaderCallbacks<Cursor> {
	// Interface for callback fragment
	public interface AllDataFragmentLoaderCallbacks {
		public void onCallbackCreateLoader(int id, Bundle args);

		public void onCallbackLoaderFinished(Loader<Cursor> loader, Cursor data);

		public void onCallbackLoaderReset(Loader<Cursor> loader);
	}
	/**
	 * Export data to CSV file
	 *
	 */
	private class ExportDataToCSV extends AsyncTask<Void, Void, Boolean> {
		private final String LOGCAT = AllDataFragment.ExportDataToCSV.class.getSimpleName();
		private String mFileName = null;
		private ProgressDialog dialog;
		private String mPrefix = "";
		
		public ExportDataToCSV() {
			// create progress dialog
			dialog = new ProgressDialog(getActivity());
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			AllDataAdapter allDataAdapter = (AllDataAdapter)getListAdapter();
			if (allDataAdapter == null || allDataAdapter.getCursor() == null) 
				return false;
			//take cursor
			Cursor data = allDataAdapter.getCursor();
			//create object to write csv file
			try {
				CSVWriter csvWriter = new CSVWriter(new FileWriter(mFileName), CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER);
				data.moveToFirst();
				while (!data.isAfterLast()) {
					String[] record = new String[7];
					// compose a records
					record[0] = data.getString(data.getColumnIndex(QueryAllData.UserDate));
					record[1] = data.getString(data.getColumnIndex(QueryAllData.Payee));
					record[2] = Float.toString(data.getFloat(data.getColumnIndex(QueryAllData.UserDate)));
					record[3] = data.getString(data.getColumnIndex(QueryAllData.Category));
					record[4] = data.getString(data.getColumnIndex(QueryAllData.Subcategory));
					record[5] = Integer.toString(data.getInt(data.getColumnIndex(QueryAllData.TransactionNumber)));
					record[6] = data.getString(data.getColumnIndex(QueryAllData.Notes));
					// writer record
					csvWriter.writeNext(record);
					// move to next row
					data.moveToNext();
				}
				csvWriter.close();
			} catch (Exception e) {
				Log.e(LOGCAT, e.getMessage());
				return false;
			}			
			return true;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (dialog != null && dialog.isShowing()) {
				dialog.dismiss();
			}
			// prompt
			Toast.makeText(getActivity(), getString(result ? R.string.export_file_complete : R.string.export_file_failed, mFileName), Toast.LENGTH_LONG).show();
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			//file
			File externalStorage = Environment.getExternalStorageDirectory();
			if (!(externalStorage != null && externalStorage.exists() && externalStorage.isDirectory()))
				return;
			//create folder to copy database
			File folderOutput = new File(externalStorage + "/" + getActivity().getPackageName());
			//make a directory
			if (!folderOutput.exists()) {
				if (!folderOutput.mkdirs())
					return;
			}
			String prefix = getPrefixName();
			if (!TextUtils.isEmpty(prefix))
				prefix = prefix + "_";
			//compose file name
			mFileName = folderOutput + "/" + prefix + new SimpleDateFormat("yyyyMMddhhmmss").format(Calendar.getInstance().getTime()) + ".csv";
			//dialog
			dialog.setIndeterminate(true);
			dialog.setMessage(getString(R.string.export_data_in_progress));
			dialog.show();
		}

		/**
		 * @return the mPrefix
		 */
		public String getPrefixName() {
			if (TextUtils.isEmpty(mPrefix)) {
				return "";
			} else {
				return mPrefix;
			}
		}

		/**
		 * @param mPrefix the mPrefix to set
		 */
		public void setPrefixName(String mPrefix) {
			this.mPrefix = mPrefix;
		}
	}
	// ID Loader
	public static final int ID_LOADER_ALL_DATA_DETAIL = 1;
	// KEY Arguments
	public static final String KEY_ARGUMENTS_WHERE = "SearchResultFragment:ArgumentsWhere";
	public static final String KEY_ARGUMENTS_SORT = "SearchResultFragment:ArgumentsSort";
	
	/**
	 * Create a new instance of AllDataFragment with accountId params
	 * @param accountId Id of account to display. If generic shown set -1
	 * @return new instance AllDataFragment
	 */
	public static AllDataFragment newInstance(int accountId) {
		AllDataFragment fragment = new AllDataFragment();
		fragment.mAccountId = accountId;
		return fragment;
	}
	private AllDataFragmentLoaderCallbacks mSearResultFragmentLoaderCallbacks;
	private boolean mAutoStarLoader = true;
	private boolean mShownHeader = false;
	private boolean mShownBalance = false;

	private int mGroupId = 0;
	
	private int mAccountId = -1;

	/**
	 * Export data to CSV file
	 */
	public void exportDataToCSVFile() {
		exportDataToCSVFile("");
	}
	
	/**
	 * Export data to CSV file
	 * @param accountName
	 */
	public void exportDataToCSVFile(String prefixName) {
		ExportDataToCSV csv = new ExportDataToCSV();
		csv.setPrefixName(prefixName);
		csv.execute();
	}

	/**
	 * @return the mGroupId
	 */
	public int getContextMenuGroupId() {
		return mGroupId;
	}

	/**
	 * @return the mSearResultFragmentLoaderCallbacks
	 */
	public AllDataFragmentLoaderCallbacks getSearResultFragmentLoaderCallbacks() {
		return mSearResultFragmentLoaderCallbacks;
	}

	/**
	 * @return the mAutoStarLoader
	 */
	public boolean isAutoStarLoader() {
		return mAutoStarLoader;
	}

	/**
	 * @return the mShownHeader
	 */
	public boolean isShownHeader() {
		return mShownHeader;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// set fragment
		setEmptyText(getString(R.string.no_data));
		setListShown(false);
		// option menu
		setHasOptionsMenu(true);
		// create adapter
		AllDataAdapter adapter = new AllDataAdapter(getActivity(), null);
		adapter.setAccountId(mAccountId);
		adapter.setShowAccountName(isShownHeader());
		adapter.setShowBalanceAmount(isShownBalance());
		if (isShownBalance()) {
			adapter.setDatabase(new MoneyManagerOpenHelper(getActivity()).getReadableDatabase());
		}
		setListAdapter(adapter);
		// register context menu
		registerForContextMenu(getListView());
		// set divider
		getListView().setDivider(new ColorDrawable(getResources().getColor(R.color.money_background)));
		//getListView().setSelector(new ColorDrawable(getResources().getColor(R.color.money_background)));
		// set animation
		setListShown(false);
		// start loader
		if (isAutoStarLoader()) {
			startLoaderData();
		}
	}

	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		if (item.getGroupId() == getContextMenuGroupId()) {
			// take a info of the selected menu, and cursor at position
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
			Cursor cursor = (Cursor) getListAdapter().getItem(info.position);
			// check if cursor is valid
			if (cursor != null) {
				//quick-fix convert 'switch' to 'if-else'
				if (item.getItemId() == R.id.menu_edit) {
					startCheckingAccountActivity(cursor.getInt(cursor.getColumnIndex(QueryAllData.ID)));
				} else if (item.getItemId() == R.id.menu_delete) {
					showDialogDeleteCheckingAccount(cursor.getInt(cursor.getColumnIndex(QueryAllData.ID)));
				} else if (item.getItemId() == R.id.menu_reconciled) {
					setStatusCheckingAccount(getListView().getFirstVisiblePosition(), cursor.getInt(cursor.getColumnIndex(QueryAllData.ID)), "R");
				} else if (item.getItemId() == R.id.menu_none) {
					setStatusCheckingAccount(getListView().getFirstVisiblePosition(), cursor.getInt(cursor.getColumnIndex(QueryAllData.ID)), "");
				} else if (item.getItemId() == R.id.menu_duplicate) {
					setStatusCheckingAccount(getListView().getFirstVisiblePosition(), cursor.getInt(cursor.getColumnIndex(QueryAllData.ID)), "D");
				} else if (item.getItemId() == R.id.menu_follow_up) {
					setStatusCheckingAccount(getListView().getFirstVisiblePosition(), cursor.getInt(cursor.getColumnIndex(QueryAllData.ID)), "F");
				} else if (item.getItemId() == R.id.menu_void) {
					setStatusCheckingAccount(getListView().getFirstVisiblePosition(), cursor.getInt(cursor.getColumnIndex(QueryAllData.ID)), "V");
				}
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		// take info and cursor from listview adapter
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		Cursor cursor = (Cursor) getListAdapter().getItem(info.position);
		// check if cursor is valid
		if (cursor == null) {
			return;
		}
		/* getActivity().getMenuInflater().inflate(R.menu.contextmenu_accountfragment, menu); */
		// add manually
		int[] menuItem = new int[] { R.id.menu_edit, R.id.menu_delete, R.id.menu_reconciled, R.id.menu_none, R.id.menu_follow_up, R.id.menu_duplicate,
				R.id.menu_void };
		int[] menuText = new int[] { R.string.edit, R.string.delete, R.string.status_reconciled, R.string.status_none, R.string.status_follow_up,
				R.string.status_duplicate, R.string.status_void };
		for (int i = 0; i < menuItem.length; i++) {
			menu.add(getContextMenuGroupId(), menuItem[i], i, menuText[i]);
		}
		// create a context menu
		menu.setHeaderTitle(cursor.getString(cursor.getColumnIndex(QueryAllData.AccountName)));
		// hide current status
		if (menu.findItem(R.id.menu_reconciled) != null)
			menu.findItem(R.id.menu_reconciled).setVisible(cursor.getString(cursor.getColumnIndex(QueryAllData.Status)).equalsIgnoreCase("R") == false);
		if (menu.findItem(R.id.menu_none) != null)
			menu.findItem(R.id.menu_none).setVisible(TextUtils.isEmpty(cursor.getString(cursor.getColumnIndex(QueryAllData.Status))) == false);
		if (menu.findItem(R.id.menu_duplicate) != null)
			menu.findItem(R.id.menu_duplicate).setVisible(cursor.getString(cursor.getColumnIndex(QueryAllData.Status)).equalsIgnoreCase("D") == false);
		if (menu.findItem(R.id.menu_follow_up) != null)
			menu.findItem(R.id.menu_follow_up).setVisible(cursor.getString(cursor.getColumnIndex(QueryAllData.Status)).equalsIgnoreCase("F") == false);
		if (menu.findItem(R.id.menu_void) != null)
			menu.findItem(R.id.menu_void).setVisible(cursor.getString(cursor.getColumnIndex(QueryAllData.Status)).equalsIgnoreCase("V") == false);

	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		if (getSearResultFragmentLoaderCallbacks() != null)
			getSearResultFragmentLoaderCallbacks().onCallbackCreateLoader(id, args);
		//animation
		setListShown(false);
		
		switch (id) {
		case ID_LOADER_ALL_DATA_DETAIL:
			QueryAllData allData = new QueryAllData(getActivity());
			// compose selection and sort
			String selection = "",
			sort = "";
			if (args != null && args.containsKey(KEY_ARGUMENTS_WHERE)) {
				ArrayList<String> whereClause = args.getStringArrayList(KEY_ARGUMENTS_WHERE);
				if (whereClause != null) {
					for (int i = 0; i < whereClause.size(); i++) {
						selection += (!TextUtils.isEmpty(selection) ? " AND " : "") + whereClause.get(i);
					}
				}
			}
			// set sort
			if (args != null && args.containsKey(KEY_ARGUMENTS_SORT)) {
				sort = args.getString(KEY_ARGUMENTS_SORT);
			}
			// create loader
			return new CursorLoader(getActivity(), allData.getUri(), allData.getAllColumns(), selection, null, sort);
		}
		return null;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		if (getSherlockActivity() != null) {
			MenuItem itemExportToCsv = menu.findItem(R.id.menu_export_to_csv);
			if (itemExportToCsv != null) itemExportToCsv.setVisible(true);
			MenuItem itemSearch = menu.findItem(R.id.menu_search_transaction);
			if (itemSearch != null) itemSearch.setVisible(!getSherlockActivity().getClass().getSimpleName().equals(SearchActivity.class.getSimpleName()));
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		if (getSearResultFragmentLoaderCallbacks() != null)
			getSearResultFragmentLoaderCallbacks().onCallbackLoaderReset(loader);

		((CursorAdapter) getListAdapter()).swapCursor(null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		if (getSearResultFragmentLoaderCallbacks() != null) {
			getSearResultFragmentLoaderCallbacks().onCallbackLoaderFinished(loader, data);
		}
		switch (loader.getId()) {
		case ID_LOADER_ALL_DATA_DETAIL:
			((CursorAdapter) getListAdapter()).swapCursor(data);
			if (isResumed()) {
				setListShown(true);
			} else {
				setListShownNoAnimation(true);
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_export_to_csv) {
			exportDataToCSVFile();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * @param mAutoStarLoader
	 *            the mAutoStarLoader to set
	 */
	public void setAutoStarLoader(boolean mAutoStarLoader) {
		this.mAutoStarLoader = mAutoStarLoader;
	}

	/**
	 * @param mGroupId
	 *            the mGroupId to set
	 */
	public void setContextMenuGroupId(int mGroupId) {
		this.mGroupId = mGroupId;
	}

	/**
	 * @param mSearResultFragmentLoaderCallbacks
	 *            the mSearResultFragmentLoaderCallbacks to set
	 */
	public void setSearResultFragmentLoaderCallbacks(AllDataFragmentLoaderCallbacks mSearResultFragmentLoaderCallbacks) {
		this.mSearResultFragmentLoaderCallbacks = mSearResultFragmentLoaderCallbacks;
	}

	/**
	 * @param mShownHeader
	 *            the mShownHeader to set
	 */
	public void setShownHeader(boolean mShownHeader) {
		this.mShownHeader = mShownHeader;
	}
	/**
	 * set status to transaction
	 * 
	 * @param position
	 * @param transId
	 * @param status
	 * @return
	 */
	private boolean setStatusCheckingAccount(int position, int transId, String status) {
		// content value for updates
		ContentValues values = new ContentValues();
		// set new state
		values.put(TableCheckingAccount.STATUS, status);

		// update
		if (getActivity().getContentResolver().update(new TableCheckingAccount().getUri(), values, TableCheckingAccount.TRANSID + "=?",
				new String[] { Integer.toString(transId) }) <= 0) {
			Toast.makeText(getActivity(), R.string.db_update_failed, Toast.LENGTH_LONG).show();
			return false;
		} else {
			// reload data
			startLoaderData();
			return true;
		}
	}
	
	/**
	 * 
	 * @param transId
	 *            primary key of transation
	 */
	private void showDialogDeleteCheckingAccount(final int transId) {
		// create alert dialog and set title and message
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

		alertDialog.setTitle(R.string.delete_transaction);
		alertDialog.setMessage(R.string.confirmDelete);
		alertDialog.setIcon(R.drawable.ic_action_warning_light);

		// set listener button positive
		alertDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				TableCheckingAccount trans = new TableCheckingAccount();
				if (getActivity().getContentResolver().delete(trans.getUri(), TableCheckingAccount.TRANSID + "=?", new String[] { Integer.toString(transId) }) == 0) {
					Toast.makeText(getActivity(), R.string.db_delete_failed, Toast.LENGTH_SHORT).show();
				}
				// restart loader
				startLoaderData();
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
	 * 
	 * @param transId
	 *            null set if you want to do a new transaction, or transaction id
	 */
	private void startCheckingAccountActivity(Integer transId) {
		// create intent, set Account ID
		Intent intent = new Intent(getActivity(), CheckingAccountActivity.class);
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
	 * Start loader into fragment
	 */
	public void startLoaderData() {
		getLoaderManager().restartLoader(ID_LOADER_ALL_DATA_DETAIL, getArguments(), this);
	}

	/**
	 * @return the mShownBalance
	 */
	public boolean isShownBalance() {
		return mShownBalance;
	}

	/**
	 * @param mShownBalance the mShownBalance to set
	 */
	public void setShownBalance(boolean mShownBalance) {
		this.mShownBalance = mShownBalance;
	}
}
