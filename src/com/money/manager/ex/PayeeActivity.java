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
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.view.MenuItem;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.MoneySimpleCursorAdapter;
import com.money.manager.ex.database.SQLTypeTransacion;
import com.money.manager.ex.database.TablePayee;
import com.money.manager.ex.fragment.BaseFragmentActivity;
import com.money.manager.ex.fragment.BaseListFragment;
/**
 * 
 * @author Alessandro Lazzari (lazzari.ale@gmail.com)
 * @version 0.9.0
 */
public class PayeeActivity extends BaseFragmentActivity {
	public static class PayeeLoaderListFragment extends BaseListFragment
		implements LoaderManager.LoaderCallbacks<Cursor> {
		// add menu ite,
		private static final int MENU_ITEM_ADD = 1;
		private String mCurFilter;
		private int mLayout;
		
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			
			setShowMenuItemSearch(true);
			setEmptyText(getActivity().getResources().getString(R.string.payee_empty_list));
			setHasOptionsMenu(true);
			
			mLayout = mAction.equals(Intent.ACTION_PICK) ? android.R.layout.simple_list_item_multiple_choice : android.R.layout.simple_list_item_1;
			// associate adapter
			MoneySimpleCursorAdapter adapter = new MoneySimpleCursorAdapter(getActivity(), mLayout, null, new String[] { TablePayee.PAYEENAME },
					new int[] { android.R.id.text1 }, 0);
			// set adapter
			setListAdapter(adapter);
			
			registerForContextMenu(getListView());
			getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			
			setListShown(false);
			// start loader
			getLoaderManager().initLoader(ID_LOADER_PAYEE, null, this);
		}
		
		@Override
		public boolean onContextItemSelected(android.view.MenuItem item) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();

			Cursor cursor = ((SimpleCursorAdapter)getListAdapter()).getCursor();
			cursor.moveToPosition(info.position);
			
			switch (item.getItemId()) {
			case 0: //EDIT
				showDialogEditPayeeName(SQLTypeTransacion.UPDATE, cursor.getInt(cursor.getColumnIndex(TablePayee.PAYEEID)), cursor.getString(cursor.getColumnIndex(TablePayee.PAYEENAME)));
				break;
			case 1: //DELETE
				//if (new TablePayee().canDelete(getActivity(), cursor.getInt(cursor.getColumnIndex(TablePayee.PAYEEID)))) {
				ContentValues contentValues = new ContentValues();
				contentValues.put(TablePayee.PAYEEID, cursor.getInt(cursor.getColumnIndex(TablePayee.PAYEEID)));
				if (new TablePayee().canDelete(getActivity(), contentValues)) {
					showDialogDeletePayee(cursor.getInt(cursor.getColumnIndex(TablePayee.PAYEEID)));
				} else {
					new AlertDialog.Builder(getActivity())
							.setTitle(R.string.attention)
							.setMessage(R.string.payee_can_not_deleted)
							.setIcon(R.drawable.ic_action_warning_light)
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

			Cursor cursor = ((SimpleCursorAdapter)getListAdapter()).getCursor();
			cursor.moveToPosition(info.position);
			menu.setHeaderTitle(cursor.getString(cursor.getColumnIndex(TablePayee.PAYEENAME)));

			String[] menuItems = getResources().getStringArray(R.array.context_menu);
			for (int i = 0; i < menuItems.length; i ++) {
				menu.add(Menu.NONE, i, i, menuItems[i]);
			}
		}
		
		@Override
		public Loader<Cursor> onCreateLoader(int id, Bundle args) {
			switch (id) {
			case ID_LOADER_PAYEE:
				String select = null;
				if (!TextUtils.isEmpty(mCurFilter)) {
					select = TablePayee.PAYEENAME + " LIKE '" + mCurFilter + "%'"; 
				}
				return new CursorLoader(getActivity(), mPayee.getUri(), mPayee.getAllColumns(), select, null, "upper(" + TablePayee.PAYEENAME + ")");
			}

			return null;
		}
		
		@Override
		public void onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu, com.actionbarsherlock.view.MenuInflater inflater) {
			super.onCreateOptionsMenu(menu, inflater);
			// item add
            MenuItem itemadd = menu.add(0, MENU_ITEM_ADD, MENU_ITEM_ADD, R.string.add);
            itemadd.setIcon(new Core(getActivity()).resolveIdAttribute(R.attr.ic_action_add));
            itemadd.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM|MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        }
		
        @Override
		public void onLoaderReset(Loader<Cursor> loader) {
			switch (loader.getId()) {
			case ID_LOADER_PAYEE:
				// mAdapter.swapCursor(null);
			}
		}

		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
			switch (loader.getId()) {
			case ID_LOADER_PAYEE:
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
				showDialogEditPayeeName(SQLTypeTransacion.INSERT, 0, null);
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
            getLoaderManager().restartLoader(ID_LOADER_PAYEE, null, this);
            return true;
        }

		@Override
		protected void setResult() {
			if (mAction.equals(Intent.ACTION_PICK)) {
				Intent result = new Intent();

				Cursor cursor = ((SimpleCursorAdapter)getListAdapter()).getCursor();

				for(int i = 0; i < getListView().getCount(); i ++) {
					if (getListView().isItemChecked(i)) {
						cursor.moveToPosition(i);

						result.putExtra(INTENT_RESULT_PAYEEID, cursor.getInt(cursor.getColumnIndex(TablePayee.PAYEEID)));
						result.putExtra(INTENT_RESULT_PAYEENAME, cursor.getString(cursor.getColumnIndex(TablePayee.PAYEENAME)));

						break;
					}
				}
				// set result
				getActivity().setResult(Activity.RESULT_OK, result);
			}
			//esco
			return;
		}
		
		private void showDialogDeletePayee(final int payeeId) {
			// creating dialog
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

			alertDialog.setTitle(R.string.delete_payee);
			alertDialog.setMessage(R.string.confirmDelete);
			alertDialog.setIcon(R.drawable.ic_action_warning_light);

			alertDialog.setPositiveButton(android.R.string.ok,
					new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (getActivity().getContentResolver().delete(mPayee.getUri(), "PAYEEID=" + payeeId, null) == 0) {
								Toast.makeText(getActivity(), R.string.db_delete_failed, Toast.LENGTH_SHORT).show();
							}
							// restart loader
							getLoaderManager().restartLoader(ID_LOADER_PAYEE, null, PayeeLoaderListFragment.this);
						}
					});

			alertDialog.setNegativeButton(android.R.string.cancel, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			// show dialog
			alertDialog.create().show();
		}
		
		private void showDialogEditPayeeName(final SQLTypeTransacion type, final int payeeId, final String payeeName) {
			View viewDialog = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_new_edit_payee, null);
			final EditText edtPayeeName = (EditText)viewDialog.findViewById(R.id.editTextPayeeName);

			edtPayeeName.setText(payeeName);
			if (!TextUtils.isEmpty(payeeName)) {
				edtPayeeName.setSelection(payeeName.length());
			}
			// create dialog
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

			alertDialog.setView(viewDialog);
			alertDialog.setTitle(R.string.edit_payeeName);

			alertDialog.setPositiveButton(android.R.string.ok,
					new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// take payeename from edittext
							String name = edtPayeeName.getText().toString();
							ContentValues values = new ContentValues();
							values.put(TablePayee.PAYEENAME, name);
							// check if action in update or insert
							switch (type) {
							case INSERT:
								if (getActivity().getContentResolver().insert(mPayee.getUri(), values) == null) {
									Toast.makeText(getActivity(), R.string.db_insert_failed, Toast.LENGTH_SHORT).show();
								}
								break;
							case UPDATE:
								if (getActivity().getContentResolver().update(mPayee.getUri(), values, "PAYEEID=" + payeeId, null) == 0) {
									Toast.makeText(getActivity(), R.string.db_update_failed, Toast.LENGTH_SHORT).show();
								}
								break;
							}
							// restart loader
							getLoaderManager().restartLoader(ID_LOADER_PAYEE, null, PayeeLoaderListFragment.this);
						}
					});

			alertDialog.setNegativeButton(android.R.string.cancel, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			// show dialog
			alertDialog.create().show();
		}
	}
	@SuppressWarnings("unused")
	private static final String LOGCAT = PayeeActivity.class.getSimpleName();
	private static final String FRAGMENTTAG = PayeeActivity.class.getSimpleName() + "_Fragment";
	public static final String INTENT_RESULT_PAYEEID = "PayeeActivity:PayeeId";

	public static final String INTENT_RESULT_PAYEENAME = "PayeeActivity:PayeeName";
	PayeeLoaderListFragment listFragment = new PayeeLoaderListFragment();

	private static final int ID_LOADER_PAYEE = 0;
	private static TablePayee mPayee = new TablePayee();
	private static String mAction = Intent.ACTION_EDIT;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// enable home button
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		// process intent
		Intent intent = getIntent();
		if (intent != null && !(TextUtils.isEmpty(intent.getAction()))) {
			mAction = intent.getAction();
		}
		FragmentManager fm = getSupportFragmentManager();
		// attach fragment activity
        if (fm.findFragmentById(android.R.id.content) == null) {
            fm.beginTransaction().add(android.R.id.content, listFragment, FRAGMENTTAG).commit();
        }
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// set result
			BaseListFragment fragment = (BaseListFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENTTAG);
			if (fragment != null) {
				fragment.setResultAndFinish();
			}
		}
		return super.onKeyUp(keyCode, event);
	}
}
