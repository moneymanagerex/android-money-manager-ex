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

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
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
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.money.manager.ex.database.MoneyManagerOpenHelper;
import com.money.manager.ex.database.QueryCategorySubCategory;
import com.money.manager.ex.database.SQLTypeTransacion;
import com.money.manager.ex.database.TableCategory;
import com.money.manager.ex.database.TableSubCategory;
import com.money.manager.ex.fragment.BaseFragmentActivity;
import com.money.manager.ex.fragment.BaseListFragment;
/**
 * 
 * @author Alessandro Lazzari (lazzari.ale@gmail.com)
 * @version 1.0.0
 */
public class CategorySubCategoryActivity extends BaseFragmentActivity {
	public static class CategorySubLoaderListFragment extends BaseListFragment
		implements LoaderManager.LoaderCallbacks<Cursor> {
		public class CategorySubCategoryAdapter extends CursorAdapter {
			private LayoutInflater mInflater;
			
			@SuppressWarnings("deprecation")
			public CategorySubCategoryAdapter(Context context, Cursor c) {
				super(context, c);
				mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			}

			@Override
			public void bindView(View view, Context context, Cursor cursor) {
				if ((view == null) || (cursor == null)) { return; }
				TextView text1 = (TextView)view.findViewById(android.R.id.text1);
				TextView text2 = (TextView)view.findViewById(android.R.id.text2);
			
				if (TextUtils.isEmpty(cursor.getString(cursor.getColumnIndex(QueryCategorySubCategory.SUBCATEGNAME)))) {
					text1.setText(cursor.getString(cursor.getColumnIndex(QueryCategorySubCategory.CATEGNAME)));
					text2.setText("");
				} else {
					text1.setText(cursor.getString(cursor.getColumnIndex(QueryCategorySubCategory.SUBCATEGNAME)));
					text2.setText(cursor.getString(cursor.getColumnIndex(QueryCategorySubCategory.CATEGNAME)));
				}
				
				if (mLayout == R.layout.simple_list_item_multiple_choice_2) {
					CheckedTextView chekedtext = (CheckedTextView)view.findViewById(android.R.id.text1);
					//chekedtext.setChecked(getListView().isItemChecked(cursor.getPosition()));
					chekedtext.setChecked(cursor.getInt(cursor.getColumnIndex(QueryCategorySubCategory.ID)) == idChecked);
				}
			}

			@Override
			public View newView(Context context, Cursor cursor, ViewGroup parent) {
				return mInflater.inflate(mLayout, parent, false);
			}
		}
		private static final int SUBMENU_ITEM_ADD_CATEGORY = 2;
		private static final String KEY_ID_SELECTED = "CategorySubCategory:idChecked";
		private static final int SUBMENU_ITEM_ADD_SUBCATEGORY = 3;
		private int mLayout;
		private int idChecked = -1;

		private String mCurFilter;
		
		@Override
		public void onSaveInstanceState(Bundle outState) {
			super.onSaveInstanceState(outState);
			outState.putInt(KEY_ID_SELECTED, idChecked);
		}
		
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			if (savedInstanceState != null && savedInstanceState.containsKey(KEY_ID_SELECTED)) {
				idChecked = savedInstanceState.getInt(KEY_ID_SELECTED);
			}
			// set visibile search menu
			setShowMenuItemSearch(true);

			setEmptyText(getActivity().getResources().getString(R.string.category_empty_list));
			setHasOptionsMenu(true);
			// define layout
			mLayout = mAction.equals(Intent.ACTION_PICK) ? R.layout.simple_list_item_multiple_choice_2 : android.R.layout.simple_list_item_2;
			// associate adapter
			CategorySubCategoryAdapter adapter = new CategorySubCategoryAdapter(getActivity(), null);
			setListAdapter(adapter);
			// manage context menu
			registerForContextMenu(getListView());

			getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			setListShown(false);

			if (mLayout == R.layout.simple_list_item_multiple_choice_2) {
				getListView().setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						// set id checked egual -1
						idChecked = -1;
						
						CheckedTextView text1 = (CheckedTextView)view.findViewById(android.R.id.text1);
						text1.toggle(); // change status
						// save state position
						getListView().setItemChecked(position, text1.isChecked());
						if (text1.isChecked()) {
							Cursor cursor = ((CategorySubCategoryAdapter)getListView().getAdapter()).getCursor();
							if (cursor != null) {
								cursor.moveToPosition(position);
								idChecked = cursor.getInt(cursor.getColumnIndex(QueryCategorySubCategory.ID));
							}
						}
						Log.i(LOGCAT, "Checked Id=" + Integer.toString(idChecked));
					}
				});
			}
			
			// start loader
			getLoaderManager().initLoader(ID_LOADER_CATEGORYSUB, null, this);
		}
		
		@Override
		public boolean onContextItemSelected(android.view.MenuItem item) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
			// instance of cursor and move to position
			Cursor cursor = ((CategorySubCategoryAdapter)getListView().getAdapter()).getCursor();
			cursor.moveToPosition(info.position);
			// manage select menu
			switch (item.getItemId()) {
			case 0: //EDIT
				if (cursor.getInt(cursor.getColumnIndex(QueryCategorySubCategory.SUBCATEGID)) <= 0)  {
					showDialogEditCategName(
							SQLTypeTransacion.UPDATE,
							cursor.getInt(cursor
									.getColumnIndex(QueryCategorySubCategory.CATEGID)),
							cursor.getString(cursor
									.getColumnIndex(QueryCategorySubCategory.CATEGNAME)));
				} else {
					showDialogEditSubCategName(
							SQLTypeTransacion.UPDATE,
							cursor.getInt(cursor
									.getColumnIndex(QueryCategorySubCategory.CATEGID)),
							cursor.getInt(cursor
									.getColumnIndex(QueryCategorySubCategory.SUBCATEGID)),
							cursor.getString(cursor
									.getColumnIndex(QueryCategorySubCategory.SUBCATEGNAME)));
				}
				break;
			case 1: //DELETE
				showDialogDeleteCategorySub(
						cursor.getInt(cursor
								.getColumnIndex(QueryCategorySubCategory.CATEGID)),
						cursor.getInt(cursor
								.getColumnIndex(QueryCategorySubCategory.SUBCATEGID)));
				break;
			}
			return false;
		}
		
		@Override
		public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			// take instance of cursor and move to position
			Cursor cursor = ((CategorySubCategoryAdapter)getListView().getAdapter()).getCursor();
			cursor.moveToPosition(info.position);
			// define menu
			menu.setHeaderTitle(cursor.getString(cursor.getColumnIndex(QueryCategorySubCategory.CATEGSUBNAME)));
			// context menu from resource
			String[] menuItems = getResources().getStringArray(R.array.context_menu);
			for (int i = 0; i < menuItems.length; i ++) {
				menu.add(Menu.NONE, i, i, menuItems[i]);
			}
		}
		
		@Override
		public Loader<Cursor> onCreateLoader(int id, Bundle args) {
			switch (id) {
			case ID_LOADER_CATEGORYSUB:
				String select = null;
				if (!TextUtils.isEmpty(mCurFilter)) {
					select = QueryCategorySubCategory.CATEGNAME + " LIKE '"
							+ mCurFilter + "%' OR "
							+ QueryCategorySubCategory.SUBCATEGNAME + " LIKE '"
							+ mCurFilter + "%'";
				}
				return new CursorLoader(getActivity(), mCategorySub.getUri(),
						mCategorySub.getAllColumns(), select, null,
						QueryCategorySubCategory.CATEGNAME + ", "
								+ QueryCategorySubCategory.SUBCATEGNAME);
			}
			return null;
		}
		
        @Override
        public void onCreateOptionsMenu(Menu menu, com.actionbarsherlock.view.MenuInflater inflater) {
			super.onCreateOptionsMenu(menu, inflater);
			// item add
            /*MenuItem itemadd = menu.add(0, MENU_ITEM_ADD, MENU_ITEM_ADD, R.string.add);
            itemadd.setIcon(android.R.drawable.ic_menu_add);
            itemadd.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM|MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);*/
            
            // create submenu from item add
            menu.addSubMenu(0, SUBMENU_ITEM_ADD_CATEGORY, SUBMENU_ITEM_ADD_CATEGORY, R.string.add_category);
            menu.addSubMenu(0, SUBMENU_ITEM_ADD_SUBCATEGORY, SUBMENU_ITEM_ADD_SUBCATEGORY, R.string.add_subcategory);
            
        }

		@Override
		public void onLoaderReset(Loader<Cursor> loader) {
			switch (loader.getId()) {
			case ID_LOADER_CATEGORYSUB:
				//mAdapter.swapCursor(null);
			}
		}

		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
			switch (loader.getId()) {
			case ID_LOADER_CATEGORYSUB:
				((CategorySubCategoryAdapter)getListAdapter()).swapCursor(data);
	            if (isResumed()) {
	                setListShown(true);
	            } else {
	                setListShownNoAnimation(true);
	            }
			}
		}

		@Override
		public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
			switch (item.getItemId()) {
			case android.R.id.home:
				break;
			case SUBMENU_ITEM_ADD_CATEGORY:
				showDialogEditCategName(SQLTypeTransacion.INSERT, -1, null);
				break;
			case SUBMENU_ITEM_ADD_SUBCATEGORY:
				showDialogEditSubCategName(SQLTypeTransacion.INSERT, -1, -1, null);
			}
			return super.onOptionsItemSelected(item);
		}
		
		@Override
        public boolean onQueryTextChange(String newText) {
            // Called when the action bar search text has changed.  Update
            // the search filter, and restart the loader to do a new query
            // with this filter.
            mCurFilter = !TextUtils.isEmpty(newText) ? newText : null;
            restartLoader();
            return true;
        }
		/**
		 * Restart loader to view data
		 */
		private void restartLoader() {
			getLoaderManager().restartLoader(ID_LOADER_CATEGORYSUB, null, this);
		}
		@Override
		protected void setResult() {
			if (mAction.equals(Intent.ACTION_PICK)) {
				// intent for data result
				Intent result = new Intent();
				
				Cursor cursor = ((CategorySubCategoryAdapter)getListView().getAdapter()).getCursor();
				// cycle the cursor to see the selected item
				for(int i = 0; i < getListView().getCount(); i ++) {
					if (getListView().isItemChecked(i)) {
						cursor.moveToPosition(i);
						// set info to result
						result.putExtra(INTENT_RESULT_CATEGID, cursor.getInt(cursor.getColumnIndex(QueryCategorySubCategory.CATEGID)));
						result.putExtra(INTENT_RESULT_CATEGNAME, cursor.getString(cursor.getColumnIndex(QueryCategorySubCategory.CATEGNAME)));
						result.putExtra(INTENT_RESULT_SUBCATEGID, cursor.getInt(cursor.getColumnIndex(QueryCategorySubCategory.SUBCATEGID)));
						result.putExtra(INTENT_RESULT_SUBCATEGNAME, cursor.getString(cursor.getColumnIndex(QueryCategorySubCategory.SUBCATEGNAME)));
						// exit from for
						break;
					}
				}
				// set result
				getActivity().setResult(Activity.RESULT_OK, result);
			}

			return;
		}
		/**
		 * Show alter dialog confirm delete category or sub category
		 * @param categId id of category
		 * @param subCategId id of subcategory. 0 if not sub category
		 */
		private void showDialogDeleteCategorySub(final int categId, final int subCategId) {
			boolean canDelete = false;
			ContentValues values = new ContentValues();
			if (subCategId <= 0) {
				values.put(TableCategory.CATEGID, categId);
				canDelete = new TableCategory().canDelete(getActivity(), values);
			} else {
				values.put(TableSubCategory.SUBCATEGID, subCategId);
				canDelete = new TableSubCategory().canDelete(getActivity(), values);
			}
			if (!(canDelete)) {
				new AlertDialog.Builder(getActivity())
				.setTitle(R.string.attention)
				.setMessage(R.string.category_can_not_deleted)
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
				return;
			}
			// create and set alert dialog
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
			alertDialog.setTitle(R.string.delete_category);
			alertDialog.setMessage(R.string.confirmDelete);
			alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
			// listener on positive button
			alertDialog.setPositiveButton(android.R.string.ok,
					new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							int rowsDelete = 0;
							if (subCategId <= 0) {
								rowsDelete = getActivity().getContentResolver().delete(new TableCategory().getUri(), TableCategory.CATEGID + "=" + categId, null);
							} else {
								rowsDelete = getActivity().getContentResolver().delete(new TableSubCategory().getUri(), TableSubCategory.CATEGID + "=" + categId + " AND " + TableSubCategory.SUBCATEGID + "=" + subCategId, null);
							}
							if (rowsDelete == 0) {
								Toast.makeText(getActivity(), R.string.db_delete_failed, Toast.LENGTH_SHORT).show();
							}
							// restart loader
							restartLoader();
						}
					});
			// listener on negative button
			alertDialog.setNegativeButton(android.R.string.cancel, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			// show dialog
			alertDialog.create().show();
		}
		/**
		 * Show alter dialog, for create or edit new category
		 */
		private void showDialogEditCategName(final SQLTypeTransacion type, final int categoryId, final String categName) {
			final TableCategory category = new TableCategory();
			// inflate view
			View viewDialog = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_new_edit_category, null);
			final EditText edtCategName = (EditText)viewDialog.findViewById(R.id.editTextCategName);
			// set category description
			edtCategName.setText(categName);
			if (!TextUtils.isEmpty(categName)) {
				edtCategName.setSelection(categName.length());
			}
			// create alter dialog
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
			alertDialog.setView(viewDialog);
			alertDialog.setTitle(R.string.edit_categoryName);
			// listener on positive button
			alertDialog.setPositiveButton(android.R.string.ok,
					new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// get description category
							String name = edtCategName.getText().toString();
							ContentValues values = new ContentValues();
							values.put(TableCategory.CATEGNAME, name);
							// check type transaction is request
							switch (type) {
							case INSERT:
								if (getActivity().getContentResolver().insert(category.getUri(), values) == null) {
									Toast.makeText(getActivity(), R.string.db_insert_failed, Toast.LENGTH_SHORT).show();
								}
								break;
							case UPDATE:
								if (getActivity().getContentResolver().update(category.getUri(), values, TableCategory.CATEGID  + "=" + categoryId, null) == 0) {
									Toast.makeText(getActivity(), R.string.db_update_failed, Toast.LENGTH_SHORT).show();
								}
								break;
							}
							// restart loader
							restartLoader();
						}
					});
			// listener on cancel dialog
			alertDialog.setNegativeButton(android.R.string.cancel, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			// create dialog and show
			alertDialog.create().show();
		}
		
		/**
		 * Show alter dialog, for create or edit new category
		 */
		private void showDialogEditSubCategName(final SQLTypeTransacion type, final int categoryId, final int subCategoryId, final String subCategName) {
			final TableSubCategory subCategory = new TableSubCategory();
			// inflate view
			View viewDialog = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_new_edit_subcategory, null);
			final EditText edtSubCategName = (EditText)viewDialog.findViewById(R.id.editTextCategName);
			final Spinner spnCategory = (Spinner)viewDialog.findViewById(R.id.spinnerCategory);
			// set category description
			edtSubCategName.setText(subCategName);
			if (!TextUtils.isEmpty(subCategName)) {
				edtSubCategName.setSelection(subCategName.length());
			}
			
			// populate spinner
			// take a categories list
			final List<TableCategory> categories = new MoneyManagerOpenHelper(getActivity()).getListCategories();
			ArrayList<String> categName = new ArrayList<String>();
			ArrayList<Integer> categId = new ArrayList<Integer>();
			for (TableCategory category : categories) {
				categId.add(category.getCategId());
				categName.add(category.getCategName());
			}
			ArrayAdapter<String> adapterCategory = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, categName);
			adapterCategory.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spnCategory.setAdapter(adapterCategory);
			//select category if present
			if (categoryId > 0) {
				spnCategory.setSelection(categId.indexOf(categoryId), true);
			}
			
			// create alter dialog
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
			alertDialog.setView(viewDialog);
			alertDialog.setTitle(R.string.edit_categoryName);
			// listener on positive button
			alertDialog.setPositiveButton(android.R.string.ok,
					new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// get description category
							String name = edtSubCategName.getText().toString();
							int categId = categories.get(spnCategory.getSelectedItemPosition()).getCategId();
							ContentValues values = new ContentValues();
							values.put(TableSubCategory.CATEGID, categId);
							values.put(TableSubCategory.SUBCATEGNAME, name);
							// check type transaction is request
							switch (type) {
							case INSERT:
								if (getActivity().getContentResolver().insert(subCategory.getUri(), values) == null) {
									Toast.makeText(getActivity(), R.string.db_insert_failed, Toast.LENGTH_SHORT).show();
								}
								break;
							case UPDATE:
								if (getActivity().getContentResolver().update(
										subCategory.getUri(),
										values,
										TableSubCategory.CATEGID + "="
												+ categoryId + " AND "
												+ TableSubCategory.SUBCATEGID
												+ "=" + subCategoryId, null) == 0) {
									Toast.makeText(getActivity(), R.string.db_update_failed, Toast.LENGTH_SHORT).show(); 
								}
								break;
							}
							// restart loader
							restartLoader();
						}
					});
			// listener on cancel dialog
			alertDialog.setNegativeButton(android.R.string.cancel, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
			// create dialog and show
			alertDialog.create().show();
		}
	}
	@SuppressWarnings("unused")
	private static final String LOGCAT = CategorySubCategoryActivity.class.getSimpleName();
	private static final String FRAGMENTTAG = CategorySubCategoryActivity.class.getSimpleName() + "_Fragment";
	public static final String INTENT_RESULT_CATEGID = "CategorySubCategory:CategId";
	public static final String INTENT_RESULT_CATEGNAME = "CategorySubCategory:CategName";
	public static final String INTENT_RESULT_SUBCATEGID = "CategorySubCategory:SubCategId";
	public static final String INTENT_RESULT_SUBCATEGNAME = "CategorySubCategory:SubCategName";
	// define listFragment into FragmentActivity
	CategorySubLoaderListFragment listFragment = new CategorySubLoaderListFragment();
	// ID loader
	private static final int ID_LOADER_CATEGORYSUB = 0;
	// table or query
	private static QueryCategorySubCategory mCategorySub;
	private static String mAction = Intent.ACTION_EDIT;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// enable home button into actionbar
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		// get intent
		Intent intent = getIntent();

		if (intent != null && !(TextUtils.isEmpty(intent.getAction()))) {
			mAction = intent.getAction();
		}
		
		mCategorySub = new QueryCategorySubCategory(this);
		
		// management fargment
		FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentById(android.R.id.content) == null) {
            fm.beginTransaction().add(android.R.id.content, listFragment, FRAGMENTTAG).commit();
        }
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// set result and terminate activity
			CategorySubLoaderListFragment fragment = (CategorySubLoaderListFragment)getSupportFragmentManager().findFragmentByTag(FRAGMENTTAG);
			if (fragment != null) {
				fragment.setResultAndFinish();
			}
		}
		return super.onKeyUp(keyCode, event);
	}
}
