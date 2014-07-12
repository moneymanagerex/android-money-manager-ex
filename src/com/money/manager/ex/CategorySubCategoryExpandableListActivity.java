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

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.money.manager.ex.adapter.CategoryExpandableListAdapter;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.database.MoneyManagerOpenHelper;
import com.money.manager.ex.database.QueryCategorySubCategory;
import com.money.manager.ex.database.SQLTypeTransacion;
import com.money.manager.ex.database.TableCategory;
import com.money.manager.ex.database.TableSubCategory;
import com.money.manager.ex.fragment.BaseExpandableListFragment;
import com.money.manager.ex.fragment.BaseFragmentActivity;
/**
 * 
 * @author Alessandro Lazzari (lazzari.ale@gmail.com)
 * @version 1.0.0
 */
public class CategorySubCategoryExpandableListActivity extends BaseFragmentActivity {
	public static class CategorySubCategoryExpandableLoaderListFragment extends BaseExpandableListFragment
		implements LoaderManager.LoaderCallbacks<Cursor> {

		private static final String KEY_ID_GROUP = "CategorySubCategory:idGroup";
		private static final String KEY_ID_CHILD = "CategorySubCategory:idChild";
		private static final String KEY_CUR_FILTER = "CategorySubCategory:curFilter";

		private int mLayout;
		
		private int mIdGroupChecked = ExpandableListView.INVALID_POSITION;
		private int mIdChildChecked = ExpandableListView.INVALID_POSITION;
		
		// table or query
		private static QueryCategorySubCategory mCategorySub;
		
		private List<TableCategory> mCategories;
		private HashMap<TableCategory, List<QueryCategorySubCategory>> mSubCategories;
		
		private ArrayList<Integer> mPositionToExpand;
		
		private String mCurFilter;
		
		@Override
		public void onSaveInstanceState(Bundle outState) {
			super.onSaveInstanceState(outState);
			if (getExpandableListAdapter() != null && getExpandableListAdapter().getGroupCount() > 0) {
				outState.putInt(KEY_ID_GROUP, ((CategoryExpandableListAdapter)getExpandableListAdapter()).getIdGroupChecked());
				outState.putInt(KEY_ID_CHILD, ((CategoryExpandableListAdapter)getExpandableListAdapter()).getIdChildChecked());
				outState.putString(KEY_CUR_FILTER, mCurFilter);
			}
		}
		
		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			// create category adapter
			mCategorySub = new QueryCategorySubCategory(getActivity());
			
			mCategories = new ArrayList<TableCategory>();
			mSubCategories =  new HashMap<TableCategory, List<QueryCategorySubCategory>>();
			mPositionToExpand = new ArrayList<Integer>();
			
			if (savedInstanceState != null) {
				if (savedInstanceState.containsKey(KEY_ID_GROUP))
					mIdGroupChecked = savedInstanceState.getInt(KEY_ID_GROUP);
				if (savedInstanceState.containsKey(KEY_ID_CHILD))
					mIdChildChecked = savedInstanceState.getInt(KEY_ID_CHILD);
				if (savedInstanceState.containsKey(KEY_CUR_FILTER))
					mCurFilter = savedInstanceState.getString(KEY_CUR_FILTER, ""); 
			}
			// set visibile search menu
			setShowMenuItemSearch(true);

			setEmptyText(getActivity().getResources().getString(R.string.category_empty_list));
			setHasOptionsMenu(true);
			// define layout
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
				mLayout = android.R.layout.simple_expandable_list_item_2;
			} else {
				mLayout = Intent.ACTION_PICK.equals(mAction) ? R.layout.simple_expandable_list_item_multiple_choice_2 : android.R.layout.simple_expandable_list_item_2;
			}
			
			// associate adapter
			//CategoryExpandableListAdapter adapter = new CategoryExpandableListAdapter(getActivity(), null, null);
			//setListAdapter(adapter);
			// manage context menu
			registerForContextMenu(getExpandableListView());

			getExpandableListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			setListShown(false);

			if (Intent.ACTION_PICK.equals(mAction)) {
				getExpandableListView().setOnChildClickListener(new OnChildClickListener() {
					
					@Override
					public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
						if (getExpandableListAdapter() != null && getExpandableListAdapter() instanceof CategoryExpandableListAdapter) {
							CategoryExpandableListAdapter adapter = (CategoryExpandableListAdapter)getExpandableListAdapter();
							
							QueryCategorySubCategory data = mSubCategories.get(mCategories.get(groupPosition)).get(childPosition);
							
							adapter.setIdChildChecked(data.getCategId(), data.getSubCategId());
							adapter.notifyDataSetChanged();
						}
						return false;
					}
				});
				
				getExpandableListView().setOnGroupClickListener(new OnGroupClickListener() {
					
					@Override
					public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
						if (getExpandableListAdapter() != null && getExpandableListAdapter() instanceof CategoryExpandableListAdapter) {
							CategoryExpandableListAdapter adapter = (CategoryExpandableListAdapter)getExpandableListAdapter();
							adapter.setIdGroupChecked(mCategories.get(groupPosition).getCategId());
							adapter.notifyDataSetChanged();
						}
						return false;
					}
				});
			}
			// start loader
			getLoaderManager().initLoader(ID_LOADER_CATEGORYSUB, null, this);

			// set iconfied searched
			setMenuItemSearchIconified(!Intent.ACTION_PICK.equals(mAction));
		}
		
		@Override
		public boolean onContextItemSelected(android.view.MenuItem item) {
			ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo)item.getMenuInfo();

			int type = ExpandableListView.getPackedPositionType(info.packedPosition);
			int group = ExpandableListView.getPackedPositionGroup(info.packedPosition);
			int child = ExpandableListView.getPackedPositionChild(info.packedPosition);
			
			int categId = ExpandableListView.INVALID_POSITION;
			CharSequence categName = "";
			int subCategId = ExpandableListView.INVALID_POSITION;
			CharSequence subCategName = "";
			
			if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
				categId = mCategories.get(group).getCategId();
				categName = mCategories.get(group).getCategName();
			} else if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
				categId = mSubCategories.get(mCategories.get(group)).get(child).getCategId();
				subCategId = mSubCategories.get(mCategories.get(group)).get(child).getSubCategId();
				subCategName = mSubCategories.get(mCategories.get(group)).get(child).getSubCategName();
			}
			// manage select menu
			switch (item.getItemId()) {
			case 0: //EDIT
				if (subCategId == ExpandableListView.INVALID_POSITION)  {
					showDialogEditCategName(SQLTypeTransacion.UPDATE, categId, categName);
				} else {
					showDialogEditSubCategName(SQLTypeTransacion.UPDATE, categId, subCategId, subCategName);
				}
				break;
			case 1: //DELETE
				showDialogDeleteCategorySub(categId, subCategId);
				break;
			}
			return false;
		}
		
		@Override
		public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
			ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;

			int type = ExpandableListView.getPackedPositionType(info.packedPosition);
			int group = ExpandableListView.getPackedPositionGroup(info.packedPosition);
			int child = ExpandableListView.getPackedPositionChild(info.packedPosition);
			
			if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
				menu.setHeaderTitle(mCategories.get(group).getCategName().toString());
			} else if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
				QueryCategorySubCategory subCategory = mSubCategories.get(mCategories.get(group)).get(child);
				menu.setHeaderTitle(subCategory.getCategName().toString() + ": " + subCategory.getSubCategName().toString());
			}
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
				// save id selected
				if (getExpandableListAdapter() != null && getExpandableListAdapter().getGroupCount() > 0) {
					CategoryExpandableListAdapter adapter = (CategoryExpandableListAdapter)getExpandableListAdapter();
					mIdGroupChecked = adapter.getIdGroupChecked();
					mIdChildChecked = adapter.getIdChildChecked();
				}
				// clear arraylist and hashmap
				mCategories.clear();
				mSubCategories.clear();
				// load data
				
				String select = null;
				if (!TextUtils.isEmpty(mCurFilter)) {
					select = QueryCategorySubCategory.CATEGNAME + " LIKE '" + mCurFilter + "%' OR "
							+ QueryCategorySubCategory.SUBCATEGNAME + " LIKE '" + mCurFilter + "%'";
				}
				return new CursorLoader(getActivity(), mCategorySub.getUri(), mCategorySub.getAllColumns(), select,
						null, QueryCategorySubCategory.CATEGNAME + ", " + QueryCategorySubCategory.SUBCATEGNAME);
			}
			return null;
		}
		
        @Override
        public void onCreateOptionsMenu(Menu menu, com.actionbarsherlock.view.MenuInflater inflater) {
			super.onCreateOptionsMenu(menu, inflater);
            
            // create submenu from item add
            /*menu.addSubMenu(0, SUBMENU_ITEM_ADD_CATEGORY, SUBMENU_ITEM_ADD_CATEGORY, R.string.add_category);
            menu.addSubMenu(0, SUBMENU_ITEM_ADD_SUBCATEGORY, SUBMENU_ITEM_ADD_SUBCATEGORY, R.string.add_subcategory);*/
			inflater.inflate(R.menu.menu_category_sub_category_expandable_list, menu);
            
        }

		@Override
		public void onLoaderReset(Loader<Cursor> loader) {
			switch (loader.getId()) {
			case ID_LOADER_CATEGORYSUB:
				// clear arraylist and hashmap
				mCategories.clear();
				mSubCategories.clear();
			}
		}

		public CategoryExpandableListAdapter getAdapter(Cursor data) {
			mCategories.clear();
			mSubCategories.clear();
			mPositionToExpand.clear();
			// create core and fixed string filter to hightlight
			Core core = new Core(getActivity());
			String filter = mCurFilter != null ? mCurFilter.replace("%", "") : "";
			// compose list and hashmap
			if (data != null && data.moveToFirst()) {
				int key = -1;
				List<QueryCategorySubCategory> listSubCategories = null;
				while (!data.isAfterLast()) {
					if (key != data.getInt(data.getColumnIndex(QueryCategorySubCategory.CATEGID))) {
						// check if listCategories > 0
						if (mCategories.size() > 0 && listSubCategories != null) {
							mSubCategories.put(mCategories.get(mCategories.size() - 1), listSubCategories);
						}
						// save key
						key = data.getInt(data.getColumnIndex(QueryCategorySubCategory.CATEGID));
						// create instance cateogry
						TableCategory category = new TableCategory();
						category.setCategId(data.getInt(data.getColumnIndex(QueryCategorySubCategory.CATEGID)));
						category.setCategName(core.highlight(filter, data.getString(data.getColumnIndex(QueryCategorySubCategory.CATEGNAME))));
						// add list
						mCategories.add(category);
						listSubCategories = new ArrayList<QueryCategorySubCategory>();
					}
					// check if subcategory != -1
					if (data.getInt(data.getColumnIndex(QueryCategorySubCategory.SUBCATEGID)) != -1) {
						QueryCategorySubCategory subCategory = new QueryCategorySubCategory(getSherlockActivity());
						// subcategory
						subCategory.setSubCategId(data.getInt(data.getColumnIndex(QueryCategorySubCategory.SUBCATEGID)));
						subCategory.setSubCategName(core.highlight(filter, data.getString(data.getColumnIndex(QueryCategorySubCategory.SUBCATEGNAME))));
						subCategory.setCategId(data.getInt(data.getColumnIndex(QueryCategorySubCategory.CATEGID)));
						subCategory.setCategName(core.highlight(filter, data.getString(data.getColumnIndex(QueryCategorySubCategory.CATEGNAME))));
						// add to hashmap
						listSubCategories.add(subCategory);
						// check if expand group
						if (!TextUtils.isEmpty(filter)) {
							String normalizedText = Normalizer.normalize(subCategory.getSubCategName(), Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "").toLowerCase();
							if ((normalizedText.indexOf(filter) >= 0) && (!mPositionToExpand.contains(mCategories.size() - 1))) {
								mPositionToExpand.add(mCategories.size() - 1);
							}
						}
					}
					
					data.moveToNext();
				}
				if (mCategories.size() > 0 && listSubCategories != null) {
					mSubCategories.put(mCategories.get(mCategories.size() - 1), listSubCategories);
				}
			}
			CategoryExpandableListAdapter adapter = new CategoryExpandableListAdapter(getSherlockActivity(), mLayout, mCategories, mSubCategories);
			adapter.setIdChildChecked(mIdGroupChecked, mIdChildChecked);
			return adapter;
		}
		
		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
			switch (loader.getId()) {
			case ID_LOADER_CATEGORYSUB:				
				setListAdapter(getAdapter(data));
				
	            if (isResumed()) {
	                setListShown(true);
	            } else {
	                setListShownNoAnimation(true);
	            }
	            
	            for(int i = 0; i < mPositionToExpand.size(); i ++) {
	            	getExpandableListView().expandGroup(mPositionToExpand.get(i));
	            }
			}
		}

		@Override
		public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
			switch (item.getItemId()) {
			case R.id.menu_add_category:
				showDialogEditCategName(SQLTypeTransacion.INSERT, -1, null);
				break;
			case R.id.menu_add_subcategory:
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
			if (Intent.ACTION_PICK.equals(mAction)) {
				Intent result = null;
				
				if (getExpandableListAdapter() != null && getExpandableListAdapter() instanceof CategoryExpandableListAdapter) {
					CategoryExpandableListAdapter adapter = (CategoryExpandableListAdapter)getExpandableListAdapter();
					int categId = adapter.getIdGroupChecked();
					int subCategId = adapter.getIdChildChecked();
					
					if (categId != ExpandableListView.INVALID_POSITION) {
						for(int group = 0; group < mCategories.size(); group ++) {
							if (mCategories.get(group).getCategId() == categId) {
								if (subCategId != ExpandableListView.INVALID_POSITION) {
									for(int child = 0; child < mSubCategories.get(mCategories.get(group)).size(); child ++) {
										if (mSubCategories.get(mCategories.get(group)).get(child).getSubCategId() == subCategId) {
											result = new Intent();
											result.putExtra(INTENT_RESULT_CATEGID, categId);
											result.putExtra(INTENT_RESULT_CATEGNAME, mSubCategories.get(mCategories.get(group)).get(child).getCategName().toString());
											result.putExtra(INTENT_RESULT_SUBCATEGID, subCategId);
											result.putExtra(INTENT_RESULT_SUBCATEGNAME, mSubCategories.get(mCategories.get(group)).get(child).getSubCategName().toString());
											break;
										}
									}
								} else {
									result = new Intent();
									result.putExtra(INTENT_RESULT_CATEGID, categId);
									result.putExtra(INTENT_RESULT_CATEGNAME, mCategories.get(group).getCategName().toString());
									result.putExtra(INTENT_RESULT_SUBCATEGID, subCategId);
									result.putExtra(INTENT_RESULT_SUBCATEGNAME, "");
								}
							}
						}
					}
				}
				
				if (result != null) {
					getActivity().setResult(Activity.RESULT_OK, result);
				} else {
					getActivity().setResult(Activity.RESULT_CANCELED);
				}
				
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
				return;
			}
			// create and set alert dialog
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
			alertDialog.setTitle(R.string.delete_category);
			alertDialog.setMessage(R.string.confirmDelete);
			alertDialog.setIcon(R.drawable.ic_action_warning_light);
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
		private void showDialogEditCategName(final SQLTypeTransacion type, final int categoryId, final CharSequence categName) {
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
						@SuppressWarnings("incomplete-switch")
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
		private void showDialogEditSubCategName(final SQLTypeTransacion type, final int categoryId, final int subCategoryId, final CharSequence subCategName) {
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
			MoneyManagerOpenHelper helper = new MoneyManagerOpenHelper(getActivity());
			final List<TableCategory> categories = helper.getListCategories();
			// close connection database
			helper.close();
			
			ArrayList<String> categName = new ArrayList<String>();
			ArrayList<Integer> categId = new ArrayList<Integer>();
			for (TableCategory category : categories) {
				categId.add(category.getCategId());
				categName.add(category.getCategName().toString());
			}
			ArrayAdapter<String> adapterCategory = new ArrayAdapter<String>(getActivity(), R.layout.sherlock_spinner_item, categName);
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
						@SuppressWarnings("incomplete-switch")
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// get description category
							String name = edtSubCategName.getText().toString();
							// check position
							if (spnCategory.getSelectedItemPosition() == Spinner.INVALID_POSITION) return;
							// get categid
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

		@Override
		public String getSubTitle() {
			return getString(R.string.categories);
		}
	}
	
	@SuppressWarnings("unused")
	private static final String LOGCAT = CategorySubCategoryExpandableListActivity.class.getSimpleName();
	private static final String FRAGMENTTAG = CategorySubCategoryExpandableListActivity.class.getSimpleName() + "_Fragment";
	public static final String INTENT_RESULT_CATEGID = "CategorySubCategory:CategId";
	public static final String INTENT_RESULT_CATEGNAME = "CategorySubCategory:CategName";
	public static final String INTENT_RESULT_SUBCATEGID = "CategorySubCategory:SubCategId";
	public static final String INTENT_RESULT_SUBCATEGNAME = "CategorySubCategory:SubCategName";
	// define listFragment into FragmentActivity
	CategorySubCategoryExpandableLoaderListFragment listFragment = new CategorySubCategoryExpandableLoaderListFragment();
	// ID loader
	private static final int ID_LOADER_CATEGORYSUB = 0;

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
			CategorySubCategoryExpandableLoaderListFragment fragment = (CategorySubCategoryExpandableLoaderListFragment)getSupportFragmentManager().findFragmentByTag(FRAGMENTTAG);
			if (fragment != null) {
				fragment.setResultAndFinish();
			}
		}
		return super.onKeyUp(keyCode, event);
	}
}
