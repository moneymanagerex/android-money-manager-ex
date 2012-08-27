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
package com.android.money.manager.ex;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
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

import com.android.money.manager.ex.database.TableAccountList;
import com.android.money.manager.ex.fragment.MoneyListFragment;
/**
 * 
 * @author Alessandro Lazzari (lazzari.ale@gmail.com)
 * @version 1.0.0
 * 
 */
public class AccountListActivity extends FragmentActivity {
	private static final String LOGCAT = AccountListActivity.class.getSimpleName();
	private static final String FRAGMENTTAG = AccountListActivity.class.getSimpleName() + "_Fragment";
	public static final String INTENT_RESULT_ACCOUNTID = "AccountListActivity:ACCOUNTID";
	public static final String INTENT_RESULT_ACCOUNTNAME = "AccountListActivity:ACCOUNTNAME";
	// ID degli loader
	private static final int ID_LOADER_ACCOUNT = 0;
	// definizione dell'oggetto fragmentlist
	private AccountLoaderListFragment listFragment = new AccountLoaderListFragment();
	// Interfaccia alla tabella
	private static TableAccountList mAccount = new TableAccountList();
	private static String mAction = Intent.ACTION_EDIT;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// abilito il tasto Home
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		// prendo l'intent inviato
		Intent intent = getIntent();
		// se ho l'intent controllo l'azione
		if (intent != null && !(TextUtils.isEmpty(intent.getAction()))) {
			mAction = intent.getAction();
		}
		FragmentManager fm = getSupportFragmentManager();
		// attach del fragment all'activity
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
		//processo classico del tasto
		return super.onKeyUp(keyCode, event);
	}
	
	public static class AccountLoaderListFragment extends MoneyListFragment
		implements LoaderManager.LoaderCallbacks<Cursor> {
		// ID menu item
		private static final int MENU_ITEM_ADD = 1;
		// filter
		private String mCurFilter;
		private int mLayout;

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			// imposto che voglio il menu search
			setShowMenuItemSearch(true);
			// imposto il testo se vuoto
			setEmptyText(getActivity().getResources().getString(R.string.account_empty_list));
			// ha menu item
			setHasOptionsMenu(true);
			mLayout = mAction.equals(Intent.ACTION_PICK) ? android.R.layout.simple_list_item_multiple_choice : android.R.layout.simple_list_item_1; 
			// registro la listview per il contextmenu
			registerForContextMenu(getListView());
			// imposto la modalita di scelta
			getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			// imposto di non visualizzare la lista
			setListShown(false);
			// avvio il loader
			getLoaderManager().initLoader(ID_LOADER_ACCOUNT, null, this);
		}
		
		@Override
		public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        	// richiamo l'evento base
			super.onCreateOptionsMenu(menu, inflater);
			// item add
            MenuItem itemadd = menu.add(0, MENU_ITEM_ADD, MENU_ITEM_ADD, R.string.add);
            itemadd.setIcon(android.R.drawable.ic_menu_add);
            itemadd.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM|MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        }
        
		@Override
		public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			// prendo il cursore per prendere la posizione
			Cursor cursor = ((SimpleCursorAdapter)getListView().getAdapter()).getCursor();
			// mi posiziono dove è stato selezionato
			cursor.moveToPosition(info.position);
			// imposto il titolo della del benificiario
			menu.setHeaderTitle(cursor.getString(cursor.getColumnIndex(TableAccountList.ACCOUNTNAME)));
			// prendo gli item da visualizzare nel menù
			String[] menuItems = getResources().getStringArray(R.array.context_menu);
			for (int i = 0; i < menuItems.length; i ++) {
				menu.add(Menu.NONE, i, i, menuItems[i]);
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
		public boolean onContextItemSelected(MenuItem item) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
			// prendo il cursore
			Cursor cursor = ((SimpleCursorAdapter)getListView().getAdapter()).getCursor();
			// posiziono sull'item selezionato
			cursor.moveToPosition(info.position);
			// gestione della selzione dell'item
			switch (item.getItemId()) {
			case 0: //EDIT
				startAccountListEditActivity(cursor.getInt(cursor.getColumnIndex(TableAccountList.ACCOUNTID)));
				break;
			case 1: //DELETE
				showDialogDeleteAccount(cursor.getInt(cursor.getColumnIndex(TableAccountList.ACCOUNTID)));
				break;
			}
			return false;
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
		public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
			switch (loader.getId()) {
			case ID_LOADER_ACCOUNT:
				SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(),
	                    mLayout, data,
	                    new String[] { TableAccountList.ACCOUNTNAME  },
	                    new int[] { android.R.id.text1 }, 0);
				getListView().setAdapter(adapter);
	            if (isResumed()) {
	                setListShown(true);
	            } else {
	                setListShownNoAnimation(true);
	            }
			}
		}

		@Override
		public void onLoaderReset(Loader<Cursor> loader) {
			switch (loader.getId()) {
			case ID_LOADER_ACCOUNT:
				// ((SimpleCursorAdapter)getListView().getAdapter()).swapCursor(null);
			}
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
		
		private void showDialogDeleteAccount(final int ACCOUNTID) {
			// crezione dell'alter
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
			// imposto il titolo
			alertDialog.setTitle(R.string.delete_account);
			// imposto il testo
			alertDialog.setMessage(R.string.confirmDelete);
			// set il listener del button Ok
			alertDialog.setPositiveButton(android.R.string.ok,
					new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (getActivity().getContentResolver().delete(mAccount.getUri(), "ACCOUNTID=" + ACCOUNTID, null) == 0) {
								Toast.makeText(getActivity(), R.string.db_delete_failed, Toast.LENGTH_SHORT).show();
							}
							// riavvio il loader
							getLoaderManager().restartLoader(ID_LOADER_ACCOUNT, null, AccountLoaderListFragment.this);
						}
					});
			// set il listener del button Cancel
			alertDialog.setNegativeButton(android.R.string.cancel, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// chiudo l'interfaccia
					dialog.cancel();
				}
			});
			// creo il dialog e lo rendo visibile
			alertDialog.create();
			alertDialog.show();
		}
		
		@Override
		protected void setResult() {
			if (mAction.equals(Intent.ACTION_PICK)) {
				// creazione dell'intent di restituzione dei dati
				Intent result = new Intent();
				// prendo il cursore
				Cursor cursor = ((SimpleCursorAdapter)getListView().getAdapter()).getCursor();
				// ciclo per vedere l'item selezionato
				for(int i = 0; i < getListView().getCount(); i ++) {
					if (getListView().isItemChecked(i)) {
						// posiziono il cursore
						cursor.moveToPosition(i);
						// prendo le informazioni che mi interessano
						result.putExtra(INTENT_RESULT_ACCOUNTID, cursor.getInt(cursor.getColumnIndex(TableAccountList.ACCOUNTID)));
						result.putExtra(INTENT_RESULT_ACCOUNTNAME, cursor.getString(cursor.getColumnIndex(TableAccountList.ACCOUNTNAME)));
						// esco dal ciclo
						break;
					}
				}
				// imposto il risultato
				getActivity().setResult(Activity.RESULT_OK, result);
			}
			return;
		}
	}
}
