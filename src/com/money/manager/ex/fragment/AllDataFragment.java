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

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Toast;

import com.money.manager.ex.CheckingAccountActivity;
import com.money.manager.ex.R;
import com.money.manager.ex.core.AllDataAdapter;
import com.money.manager.ex.database.QueryAllData;
import com.money.manager.ex.database.TableCheckingAccount;

public class AllDataFragment extends BaseListFragment implements LoaderCallbacks<Cursor> {
	// Interface for callback fragment
	public interface AllDataFragmentLoaderCallbacks {
		public void onCallbackCreateLoader(int id, Bundle args);

		public void onCallbackLoaderFinished(Loader<Cursor> loader, Cursor data);

		public void onCallbackLoaderReset(Loader<Cursor> loader);
	}
	// ID Loader
	public static final int ID_LOADER_ALL_DATA_DETAIL = 1;
	// KEY Arguments
	public static final String KEY_ARGUMENTS_WHERE = "SearchResultFragment:ArgumentsWhere";

	public static final String KEY_ARGUMENTS_SORT = "SearchResultFragment:ArgumentsSort";

	private AllDataFragmentLoaderCallbacks mSearResultFragmentLoaderCallbacks;
	private boolean mAutoStarLoader = true;
	private boolean mShownHeader = false;
	private int mGroupId = 0;

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
		// create adapter
		AllDataAdapter adapter = new AllDataAdapter(getActivity(), null, isShownHeader());
		setListAdapter(adapter);
		// register context menu
		registerForContextMenu(getListView());
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
				switch (item.getItemId()) {
				case R.id.menu_edit:
					startCheckingAccountActivity(cursor.getInt(cursor.getColumnIndex(QueryAllData.ID)));
					break;
				case R.id.menu_delete:
					showDialogDeleteCheckingAccount(cursor.getInt(cursor.getColumnIndex(QueryAllData.ID)));
					break;
				case R.id.menu_reconciled:
					setStatusCheckingAccount(getListView().getFirstVisiblePosition(), cursor.getInt(cursor.getColumnIndex(QueryAllData.ID)), "R");
					break;
				case R.id.menu_none:
					setStatusCheckingAccount(getListView().getFirstVisiblePosition(), cursor.getInt(cursor.getColumnIndex(QueryAllData.ID)), "");
					break;
				case R.id.menu_duplicate:
					setStatusCheckingAccount(getListView().getFirstVisiblePosition(), cursor.getInt(cursor.getColumnIndex(QueryAllData.ID)), "D");
					break;
				case R.id.menu_follow_up:
					setStatusCheckingAccount(getListView().getFirstVisiblePosition(), cursor.getInt(cursor.getColumnIndex(QueryAllData.ID)), "F");
					break;
				case R.id.menu_void:
					setStatusCheckingAccount(getListView().getFirstVisiblePosition(), cursor.getInt(cursor.getColumnIndex(QueryAllData.ID)), "V");
					break;
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
		alertDialog.setIcon(android.R.drawable.ic_dialog_alert);

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
}
