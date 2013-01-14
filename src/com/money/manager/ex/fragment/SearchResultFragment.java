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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.money.manager.ex.CheckingAccountActivity;
import com.money.manager.ex.R;
import com.money.manager.ex.core.AllDataAdapter;
import com.money.manager.ex.database.QueryAllData;
import com.money.manager.ex.database.TableCheckingAccount;

public class SearchResultFragment extends BaseListFragment implements LoaderCallbacks<Cursor> {
	private static final int ID_LOADER = 1;
	public static final String KEY_ARGUMENTS_WHERE = "SearchResultFragment:ArgumentsWhere";
	public static final String KEY_ARGUMENTS_SORT = "SearchResultFragment:ArgumentsSort";
	public static final String KEY_SUBTITLE = "SearchResultFragment:ActionBarSubTitle";
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		//set fragment
		setEmptyText(getString(R.string.no_data));
		setListShown(false);
		//create adapter
		AllDataAdapter adapter = new AllDataAdapter(getActivity(), null, true);
		setListAdapter(adapter);
		//register context menu
		registerForContextMenu(getListView());
		//subtitle
		if (getArguments() != null && getArguments().containsKey(KEY_SUBTITLE)) {
			getSherlockActivity().getSupportActionBar().setSubtitle(getArguments().getString(KEY_SUBTITLE));
		}
		//set animation
		setListShown(false);
		//start loader
		startLoader();
	}

	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {
		if (getUserVisibleHint()) {
			// take a info of the selected menu, and cursor at position 
			AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
			Cursor cursor = (Cursor)getListAdapter().getItem(info.position);
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
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		// take info and cursor from listview adapter 
		AdapterContextMenuInfo info = (AdapterContextMenuInfo)menuInfo;
		Cursor cursor = (Cursor)getListAdapter().getItem(info.position);
		// check if cursor is valid
		if (cursor == null) {
			return;
		}
		getActivity().getMenuInflater().inflate(R.menu.contextmenu_accountfragment, menu);
		// create a context menu
		menu.setHeaderTitle(cursor.getString(cursor.getColumnIndex(QueryAllData.AccountName)));
		// hide current status
		menu.findItem(R.id.menu_reconciled).setVisible(cursor.getString(cursor.getColumnIndex(QueryAllData.Status)).equalsIgnoreCase("R") == false);
		menu.findItem(R.id.menu_none).setVisible(TextUtils.isEmpty(cursor.getString(cursor.getColumnIndex(QueryAllData.Status))) == false);
		menu.findItem(R.id.menu_duplicate).setVisible(cursor.getString(cursor.getColumnIndex(QueryAllData.Status)).equalsIgnoreCase("D") == false);
		menu.findItem(R.id.menu_follow_up).setVisible(cursor.getString(cursor.getColumnIndex(QueryAllData.Status)).equalsIgnoreCase("F") == false);
		menu.findItem(R.id.menu_void).setVisible(cursor.getString(cursor.getColumnIndex(QueryAllData.Status)).equalsIgnoreCase("V") == false);
		
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		switch (id) {
		case ID_LOADER:
			QueryAllData allData = new QueryAllData(getActivity());
			// compose selection and sort
			String selection = "", sort = "";
			if (args != null && args.containsKey(KEY_ARGUMENTS_WHERE)) {
				ArrayList<String> whereClause = args.getStringArrayList(KEY_ARGUMENTS_WHERE);
				if (whereClause != null) {
					for (int i = 0; i < whereClause.size(); i ++) {
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
		((CursorAdapter)getListAdapter()).swapCursor(null);
	}
	
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		((CursorAdapter)getListAdapter()).swapCursor(data);

        if (isResumed()) {
            setListShown(true);
        } else {
            setListShownNoAnimation(true);
        }
	}
	/**
	 * Start loader into fragment
	 */
	private void startLoader() {
		//start loader
		getLoaderManager().restartLoader(ID_LOADER, getArguments(), this);
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
		if (getActivity().getContentResolver().update(new TableCheckingAccount().getUri(), values, TableCheckingAccount.TRANSID + "=?", new String[] {Integer.toString(transId)}) <= 0) {
			Toast.makeText(getActivity(), R.string.db_update_failed, Toast.LENGTH_LONG).show();
			return false;
		} else {
			// reload data
			startLoader();
			return true;
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
						startLoader();
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
	 * @param transId null set if you want to do a new transaction, or transaction id
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
}
