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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.Toast;

import com.android.money.manager.ex.database.MoneyManagerOpenHelper;
import com.android.money.manager.ex.database.SQLTypeTransacion;
import com.android.money.manager.ex.database.TableCategory;
import com.android.money.manager.ex.database.TablePayee;
import com.android.money.manager.ex.database.TableSubCategory;
import com.android.money.manager.ex.fragment.MoneyExpandableListFragment;
/**
 * 
 * @author Alessandro Lazzari (lazzari.ale@gmail.com)
 * @version 0.0.1
 * @deprecated
 * @see CategorySubCategoryActivity
 * 
 */
public class CategoryActivity extends FragmentActivity {
	private static final String LOGCAT = CategoryActivity.class.getSimpleName();
	public static final String INTENT_RESULT_PAYEEID = "CategoryActivity:PayeeId";
	public static final String INTENT_RESULT_PAYEENAME = "CategoryActivity:PayeeName";
	// definizione del listFragment
	CategoryLoaderListFragment listFragment = new CategoryLoaderListFragment();
	// ID degli loader
	private static final int ID_LOADER_CATEGORY = 0;
	private static final int ID_LOADER_SUBCATEGORY = 1;
	// Interfaccia alla tabella
	private static TableCategory mCategory = new TableCategory();
	private static TableSubCategory mSubCategory = new TableSubCategory();
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
            fm.beginTransaction().add(android.R.id.content, listFragment).commit();
        }
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			//imposto il risultato ed esco
			//listFragment.setResultAndFinish();
		}
		//processo classico del tasto
		return super.onKeyUp(keyCode, event);
	}
	
	public static class CategoryLoaderListFragment extends MoneyExpandableListFragment
		implements LoaderManager.LoaderCallbacks<Cursor> {
		// ID menu item
		private static final int MENU_ITEM_ADD = 1;
		//definizione del layout da usare
		private int mLayout = android.R.layout.simple_list_item_1;
		//definizione dell'adapter
		private SimpleExpandableListAdapter mAdapter;
		private String mCurFilter;
		private Cursor mCurCategory;

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			// imposto che voglio il menu search
			setShowMenuItemSearch(true);
			// imposto il testo se vuoto
			setEmptyText(getActivity().getResources().getString(R.string.payee_empty_list));
			// ha menu item
			setHasOptionsMenu(true);
			mLayout = mAction.equals(Intent.ACTION_PICK) ? android.R.layout.simple_list_item_multiple_choice : android.R.layout.simple_list_item_1; 
			// registro la listview per il contextmenu
			registerForContextMenu(getExpandableListView());
			// imposto la modalita di scelta
			getExpandableListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			// per impostare l'indicator a destra
			/*DisplayMetrics metrics = new DisplayMetrics();
		    ExpandableListView expList = getExpandableListView();
		    getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
		    int width = metrics.widthPixels;
		    //this code for adjusting the group indicator into right side of the view
		    expList.setIndicatorBounds(width - GetDipsFromPixel(50), width);*/
			// imposto di non visualizzare la lista
			setListShown(false);
			// avvio il loader
			getLoaderManager().initLoader(ID_LOADER_CATEGORY, null, this);
			getExpandableListView().setOnChildClickListener(new OnChildClickListener() {
				
				@Override
				public boolean onChildClick(ExpandableListView parent, View v,
						int groupPosition, int childPosition, long id) {
					View view = mAdapter.getChildView(groupPosition, childPosition, false, null, null);
					CheckBox cb = (CheckBox)view.findViewById(R.id.chekbox1);
					cb.setChecked(!(cb.isSelected()));
					return true;
				}
			});
		}
		
	    private int GetDipsFromPixel(float pixels)
	    {
	        // Get the screen's density scale
	        final float scale = getResources().getDisplayMetrics().density;
	        // Convert the dps to pixels, based on density scale
	        return (int) (pixels * scale + 0.5f);
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
			/*Cursor cursor = mAdapter.getCursor();
			// mi posiziono dove è stato selezionato
			cursor.moveToPosition(info.position);
			// imposto il titolo della del benificiario
			menu.setHeaderTitle(cursor.getString(cursor.getColumnIndex(TablePayee.PAYEENAME)));
			// prendo gli item da visualizzare nel menù
			String[] menuItems = getResources().getStringArray(R.array.context_menu);
			for (int i = 0; i < menuItems.length; i ++) {
				menu.add(Menu.NONE, i, i, menuItems[i]);
			}*/
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
		public boolean onContextItemSelected(MenuItem item) {
			/*AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
			// prendo il cursore
			Cursor cursor = mAdapter.getCursor();
			// posiziono sull'item selezionato
			cursor.moveToPosition(info.position);
			// gestione della selzione dell'item
			switch (item.getItemId()) {
			case 0: //EDIT
				showDialogEditPayeeName(SQLTypeTransacion.UPDATE, cursor.getInt(cursor.getColumnIndex(TablePayee.PAYEEID)), cursor.getString(cursor.getColumnIndex(TablePayee.PAYEENAME)));
				break;
			case 1: //DELETE
				showDialogDeletePayee(cursor.getInt(cursor.getColumnIndex(TablePayee.PAYEEID)));
				break;
			}*/
			return false;
		}
		
        @Override
        public boolean onQueryTextChange(String newText) {
            // Called when the action bar search text has changed.  Update
            // the search filter, and restart the loader to do a new query
            // with this filter.
            mCurFilter = !TextUtils.isEmpty(newText) ? newText : null;
            getLoaderManager().restartLoader(ID_LOADER_CATEGORY, null, this);
            return true;
        }

		@Override
		public Loader<Cursor> onCreateLoader(int id, Bundle args) {
			switch (id) {
			case ID_LOADER_CATEGORY:
				String select = null;
				if (!TextUtils.isEmpty(mCurFilter)) {
					select = TableCategory.CATEGNAME + " LIKE '" + mCurFilter + "%'" + 
							 " OR" +
							 " EXISTS (select * from SUBCATEGORY_V1 t where CATEGORY_V1.CATEGID=t.CATEGID AND " + TableSubCategory.SUBCATEGNAME  + " LIKE '" + mCurFilter + "%')"; 
				}
				return new CursorLoader(getActivity(), mCategory.getUri(), mCategory.getAllColumns(), select, null, "upper( " + TableCategory.CATEGNAME + ")");
			}
			return null;
		}

		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
			switch (loader.getId()) {
			case ID_LOADER_CATEGORY:
				// mAdapter.swapCursor(data);
				mCurCategory = data;
	            // creo l'adapter e lo associo
				mAdapter = new SimpleExpandableListAdapter(
								getActivity(), createListFromCategory(),
								R.layout.exapandable_header_item,
								new String[] { "CATEGORY" },
								new int[] { R.id.text1 },
								createListFromSubCategory(),
								R.layout.exapandable_child_checkable_item,
								new String[] { "SUBCATEGORY" },
								new int[] { R.id.text1 });
				//associo alla expandiblelistview
				getExpandableListView().setAdapter(mAdapter);
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
			case ID_LOADER_CATEGORY:
				//getExpandableListView().setAdapter((ListAdapter)null);
			}
		}

		private void showDialogEditPayeeName(final SQLTypeTransacion type, final int payeeId, final String payeeName) {
			// view da mettere nel dialog
			View viewDialog = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_new_edit_payee, null);
			// prendo dalla view l'edit che c'è dentro
			final EditText edtPayeeName = (EditText)viewDialog.findViewById(R.id.editTextPayeeName);
			// imposto il payeename
			edtPayeeName.setText(payeeName);
			if (!TextUtils.isEmpty(payeeName)) {
				edtPayeeName.setSelection(payeeName.length());
			}
			// crezione dell'alter
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
			// imposto la view
			alertDialog.setView(viewDialog);
			// imposto il titolo
			alertDialog.setTitle(R.string.edit_payeeName);
			// set il listener del button Ok
			alertDialog.setPositiveButton(android.R.string.ok,
					new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// prendo il payeeName dal EditText
							String name = edtPayeeName.getText().toString();
							ContentValues values = new ContentValues();
							values.put(TablePayee.PAYEENAME, name);
							// controllo se ho l'id è un update altrimenti devo fare l'insert
							switch (type) {
							case INSERT:
								if (getActivity().getContentResolver().insert(mCategory.getUri(), values) == null) {
									Toast.makeText(getActivity(), R.string.db_insert_failed, Toast.LENGTH_SHORT).show();
								}
								break;
							case UPDATE:
								if (getActivity().getContentResolver().update(mCategory.getUri(), values, "PAYEEID=" + payeeId, null) == 0) {
									Toast.makeText(getActivity(), R.string.db_update_failed, Toast.LENGTH_SHORT).show();
								}
								break;
							}
							// riavvio il loader
							getLoaderManager().restartLoader(ID_LOADER_CATEGORY, null, CategoryLoaderListFragment.this);
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
		
		private void showDialogDeletePayee(final int payeeId) {
			// crezione dell'alter
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
			// imposto il titolo
			alertDialog.setTitle(R.string.delete_payee);
			// imposto il testo
			alertDialog.setMessage(R.string.confirmDelete);
			// set il listener del button Ok
			alertDialog.setPositiveButton(android.R.string.ok,
					new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (getActivity().getContentResolver().delete(mCategory.getUri(), "PAYEEID=" + payeeId, null) == 0) {
								Toast.makeText(getActivity(), R.string.db_delete_failed, Toast.LENGTH_SHORT).show();
							}
							// riavvio il loader
							getLoaderManager().restartLoader(ID_LOADER_CATEGORY, null, CategoryLoaderListFragment.this);
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
		
		/*@Override
		protected void setResult() {
			if (mAction.equals(Intent.ACTION_PICK)) {
				// creazione dell'intent di restituzione dei dati
				Intent result = new Intent();
				// prendo il cursore
				Cursor cursor = mAdapter.getCursor();
				// ciclo per vedere l'item selezionato
				for(int i = 0; i < getListView().getCount(); i ++) {
					if (getListView().isItemChecked(i)) {
						// posiziono il cursore
						cursor.moveToPosition(i);
						// prendo le informazioni che mi interessano
						result.putExtra(INTENT_RESULT_PAYEEID, cursor.getInt(cursor.getColumnIndex(TablePayee.PAYEEID)));
						result.putExtra(INTENT_RESULT_PAYEENAME, cursor.getString(cursor.getColumnIndex(TablePayee.PAYEENAME)));
						// esco dal ciclo
						break;
					}
				}
				// imposto il risultato
				getActivity().setResult(Activity.RESULT_OK, result);
			}
			//esco
			return;
		}*/
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		private List createListFromCategory() {
			if (mCurCategory == null) { return null; }
			ArrayList result = new ArrayList();
			//posiziono sul primo record il cursore
			mCurCategory.moveToFirst();
			//ciclo il cursore
			while (mCurCategory.isAfterLast() == false) {
				//popolo la struttura
				HashMap map = new HashMap<String, String>();
				map.put("CATEGORY", mCurCategory.getString(mCurCategory.getColumnIndex(TableCategory.CATEGNAME)));
				result.add(map);
				//ciclo sulla riga successiva
				mCurCategory.moveToNext();
			}
			return (List) result;
		}
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		private List createListFromSubCategory() {
			if (mCurCategory == null) { return null; }
			ArrayList result = new ArrayList();
			// creo l'helper di accesso e prendo il db
			MoneyManagerOpenHelper databaseHelper = new MoneyManagerOpenHelper(getActivity());
			SQLiteDatabase db = databaseHelper.getWritableDatabase();
			// composizione della query
			SQLiteQueryBuilder query = new SQLiteQueryBuilder();
			// ciclo i record delle categorie
			mCurCategory.moveToFirst();
			//ciclo il cursore
			while (mCurCategory.isAfterLast() == false) {
				//prendo il codice della categoria
				int categId = mCurCategory.getInt(mCurCategory.getColumnIndex(TableCategory.CATEGID));				
				query.setTables(mSubCategory.getSource());
				//composizione della query
				String select = "CATEGID=" + Integer.toString(categId);
				if (!(TextUtils.isEmpty(mCurFilter))) {
					select += " AND SUBCATEGNAME LIKE '" + mCurFilter + "%'";
				}
				Cursor curSubCategory = query.query(db, mSubCategory.getAllColumns(), select, null, null, null, TableSubCategory.SUBCATEGNAME);
				// gestione del cursore
				getActivity().startManagingCursor(curSubCategory);
				// definizione della seconda lista
				ArrayList levelList = new ArrayList();
				// ciclo le sotto categorie
				if (curSubCategory != null) {
					while (curSubCategory.moveToNext()) {
						HashMap map = new HashMap<String, String>();
						map.put("SUBCATEGORY", curSubCategory.getString(curSubCategory.getColumnIndex(TableSubCategory.SUBCATEGNAME)));
						levelList.add(map);
					}
				}
				//accodo la lista
				result.add(levelList);
				//popolo la struttura
				mCurCategory.moveToNext();
			}
			return (List) result;
		}
	}
}
