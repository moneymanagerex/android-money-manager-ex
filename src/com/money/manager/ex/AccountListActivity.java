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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.money.manager.ex.R;
import com.money.manager.ex.database.TableAccountList;
import com.money.manager.ex.database.TablePayee;
import com.money.manager.ex.fragment.BaseFragmentActivity;
import com.money.manager.ex.fragment.BaseListFragment;
/**
 * 
 * @author Alessandro Lazzari (lazzari.ale@gmail.com)
 * @version 1.0.0
 * 
 */
public class AccountListActivity extends BaseFragmentActivity {
	public static class AccountLoaderListFragment extends BaseListFragment
		implements LoaderManager.LoaderCallbacks<Cursor> {
		// id menu item add
		private static final int MENU_ITEM_ADD = 1;
		// filter
		private String mCurFilter;
		private int mLayout;

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			// set show search
			setShowMenuItemSearch(true);
			// set default value
			setEmptyText(getActivity().getResources().getString(R.string.account_empty_list));
			setHasOptionsMenu(true);
			mLayout = mAction.equals(Intent.ACTION_PICK) ? android.R.layout.simple_list_item_multiple_choice : android.R.layout.simple_list_item_1;
			// create adapter
			SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(),
                    mLayout, null,
                    new String[] { TableAccountList.ACCOUNTNAME  },
                    new int[] { android.R.id.text1 }, 0);
			// set adapter
			setListAdapter(adapter);
			// set listview
			registerForContextMenu(getListView());

			getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			getListView().setDivider(getResources().getDrawable(R.drawable.divider_ice_cream_sandwich));
			getListView().setDividerHeight(1);

			setListShown(false);
			// start loader
			getLoaderManager().initLoader(ID_LOADER_ACCOUNT, null, this);
		}
		
		@Override
		public boolean onContextItemSelected(MenuItem item) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
			// take cursor
			Cursor cursor = ((SimpleCursorAdapter)getListAdapter()).getCursor();
			cursor.moveToPosition(info.position);

			switch (item.getItemId()) {
			case 0: //EDIT
				startAccountListEditActivity(cursor.getInt(cursor.getColumnIndex(TableAccountList.ACCOUNTID)));
				break;
			case 1: //DELETE
				ContentValues contentValues = new ContentValues();
				contentValues.put(TableAccountList.ACCOUNTID, cursor.getInt(cursor.getColumnIndex(TableAccountList.ACCOUNTID)));
				if (new TablePayee().canDelete(getActivity(), contentValues)) {
					showDialogDeleteAccount(cursor.getInt(cursor.getColumnIndex(TableAccountList.ACCOUNTID)));
				} else {
					new AlertDialog.Builder(getActivity())
							.setTitle(R.string.attention)
							.setMessage(R.string.account_can_not_deleted)
							.setIcon(android.R.drawable.ic_dialog_alert)
							.setPositiveButton(android.R.string.ok,
									new OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											dialog.dismiss();
										}
									}).create().show();
				}
				break;
			}
			return false;
		}
        
		@Override
		public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			// take cursor
			Cursor cursor = ((SimpleCursorAdapter)getListAdapter()).getCursor();
			cursor.moveToPosition(info.position);

			menu.setHeaderTitle(cursor.getString(cursor.getColumnIndex(TableAccountList.ACCOUNTNAME)));

			String[] menuItems = getResources().getStringArray(R.array.context_menu);
			for (int i = 0; i < menuItems.length; i ++) {
				menu.add(Menu.NONE, i, i, menuItems[i]);
			}
		}
		
		@Override
		public Loader<Cursor> onCreateLoader(int id, Bundle args) {
			switch (id) {
			case ID_LOADER_ACCOUNT:
				String select = null;
				if (!TextUtils.isEmpty(mCurFilter)) {
					select = TableAccountList.ACCOUNTNAME + " LIKE '" + mCurFilter + "%'"; 
				}
				return new CursorLoader(getActivity(), mAccount.getUri(), mAccount.getAllColumns(), select, null, "upper(" + TableAccountList.ACCOUNTNAME + ")");
			}

			return null;
		}
		
		@Override
		public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
			super.onCreateOptionsMenu(menu, inflater);
			// item add
            MenuItem itemadd = menu.add(0, MENU_ITEM_ADD, MENU_ITEM_ADD, R.string.add);
            itemadd.setIcon(android.R.drawable.ic_menu_add);
            itemadd.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM|MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        }
		
        @Override
		public void onLoaderReset(Loader<Cursor> loader) {
			switch (loader.getId()) {
			case ID_LOADER_ACCOUNT:
				// ((SimpleCursorAdapter)getListAdapter()).swapCursor(null);
			}
		}

		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
			switch (loader.getId()) {
			case ID_LOADER_ACCOUNT:
				((SimpleCursorAdapter)getListAdapter()).swapCursor(data);
				
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
				startAccountListEditActivity();
				break;
			}
			return super.onOptionsItemSelected(item);
		}

		@Override
        public boolean onQueryTextChange(String newText) {
            // Called when the action bar search text has changed.  Update
            // the search filter, and restart the loader to do a new query
            // with this filter.
            mCurFilter = !TextUtils.isEmpty(newText) ? newText : null;
            getLoaderManager().restartLoader(ID_LOADER_ACCOUNT, null, this);
            return true;
        }
		
		@Override
		protected void setResult() {
			if (mAction.equals(Intent.ACTION_PICK)) {
				Intent result = new Intent();
				// take cursor
				Cursor cursor = ((SimpleCursorAdapter)getListAdapter()).getCursor();

				for(int i = 0; i < getListView().getCount(); i ++) {
					if (getListView().isItemChecked(i)) {
						cursor.moveToPosition(i);
						result.putExtra(INTENT_RESULT_ACCOUNTID, cursor.getInt(cursor.getColumnIndex(TableAccountList.ACCOUNTID)));
						result.putExtra(INTENT_RESULT_ACCOUNTNAME, cursor.getString(cursor.getColumnIndex(TableAccountList.ACCOUNTNAME)));
						break;
					}
				}
				// set result
				getActivity().setResult(Activity.RESULT_OK, result);
			}
			return;
		}
		private void showDialogDeleteAccount(final int ACCOUNTID) {
			// create dialog
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

			alertDialog.setTitle(R.string.delete_account);
			alertDialog.setMessage(R.string.confirmDelete);

			alertDialog.setPositiveButton(android.R.string.ok,
					new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (getActivity().getContentResolver().delete(mAccount.getUri(), "ACCOUNTID=" + ACCOUNTID, null) == 0) {
								Toast.makeText(getActivity(), R.string.db_delete_failed, Toast.LENGTH_SHORT).show();
							}
							// restart loader
							getLoaderManager().restartLoader(ID_LOADER_ACCOUNT, null, AccountLoaderListFragment.this);
						}
					});

			alertDialog.setNegativeButton(android.R.string.cancel, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// close dialog
					dialog.cancel();
				}
			});
			// show dialog
			alertDialog.create().show();
		}
		
		/**
		 * Start the account management Activity
		 */
		private void startAccountListEditActivity() {
			this.startAccountListEditActivity(null);
		}
		
		/**
		 * Start the account management Activity
		 * @param accountId is null for a new account, not null for editing accountId account
		 */		
		private void startAccountListEditActivity(Integer accountId) {
			// create intent, set Account ID
			Intent intent = new Intent(getActivity(), AccountListEditActivity.class);
			// check accountId not null
			if (accountId != null) {
				intent.putExtra(AccountListEditActivity.KEY_ACCOUNT_ID, accountId);
				intent.setAction(Intent.ACTION_EDIT);
			} else {
				intent.setAction(Intent.ACTION_INSERT);
			}
			// launch activity
			startActivity(intent);
		}
	}
	@SuppressWarnings("unused")
	private static final String LOGCAT = AccountListActivity.class.getSimpleName();
	private static final String FRAGMENTTAG = AccountListActivity.class.getSimpleName() + "_Fragment";
	public static final String INTENT_RESULT_ACCOUNTID = "AccountListActivity:ACCOUNTID";
	public static final String INTENT_RESULT_ACCOUNTNAME = "AccountListActivity:ACCOUNTNAME";
	// ID loader
	private static final int ID_LOADER_ACCOUNT = 0;
	private AccountLoaderListFragment listFragment = new AccountLoaderListFragment();
	private static TableAccountList mAccount = new TableAccountList();
	private static String mAction = Intent.ACTION_EDIT;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		// take intent send
		Intent intent = getIntent();
		if (intent != null && !(TextUtils.isEmpty(intent.getAction()))) {
			mAction = intent.getAction();
		}
		FragmentManager fm = getSupportFragmentManager();
		// attach fragment to activity
        if (fm.findFragmentById(android.R.id.content) == null) {
            fm.beginTransaction().add(android.R.id.content, listFragment, FRAGMENTTAG).commit();
        }
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			AccountLoaderListFragment fragment = (AccountLoaderListFragment)getSupportFragmentManager().findFragmentByTag(FRAGMENTTAG);
			if (fragment != null) {
				fragment.setResultAndFinish();
			}
		}
		return super.onKeyUp(keyCode, event);
	}
}
