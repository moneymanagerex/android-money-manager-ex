/*
 * Copyright (C) 2012-2018 The Android Money Manager Ex Project Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.money.manager.ex.common;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.adapter.CategoryExpandableListAdapter;
import com.money.manager.ex.core.ContextMenuIds;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.datalayer.CategoryRepository;
import com.money.manager.ex.datalayer.Select;
import com.money.manager.ex.datalayer.SubcategoryRepository;
import com.money.manager.ex.domainmodel.Category;
import com.money.manager.ex.domainmodel.Subcategory;
import com.money.manager.ex.servicelayer.CategoryService;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.database.QueryCategorySubCategory;
import com.money.manager.ex.database.SQLTypeTransaction;
import com.money.manager.ex.search.CategorySub;
import com.money.manager.ex.search.SearchActivity;
import com.money.manager.ex.search.SearchParameters;
import com.money.manager.ex.settings.AppSettings;

import org.parceler.Parcels;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import static androidx.core.content.ContextCompat.startActivity;

/**
 * Categories list fragment. Used in Main Activity for editing of categories, and own activity
 * when selecting the category for a transaction.
 */
public class CategoryListFragment
    extends BaseExpandableListFragment
    implements LoaderManager.LoaderCallbacks<Cursor> {

    public String mAction = Intent.ACTION_EDIT;
    public Integer requestId;

    private static final int ID_LOADER_CATEGORYSUB = 0;

    private static final String KEY_ID_GROUP = "CategorySubCategory:idGroup";
    private static final String KEY_ID_CHILD = "CategorySubCategory:idChild";
    private static final String KEY_CUR_FILTER = "CategorySubCategory:curFilter";
    // table or query
    private static QueryCategorySubCategory mQuery;
    private int mLayout;
    private int mIdGroupChecked = ExpandableListView.INVALID_POSITION;
    private int mIdChildChecked = ExpandableListView.INVALID_POSITION;

    private List<Category> mCategories;
    private HashMap<Category, List<QueryCategorySubCategory>> mSubCategories;

    private ArrayList<Integer> mPositionToExpand;
    private String mCurFilter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // create category adapter
        mQuery = new QueryCategorySubCategory(getActivity());

        mCategories = new ArrayList<>();
        mSubCategories = new HashMap<>();
        mPositionToExpand = new ArrayList<>();

        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState);
        }

        setShowMenuItemSearch(true);

        setHasOptionsMenu(true);

        // Focus on search menu if set in preferences.
        AppSettings settings = new AppSettings(getActivity());
        boolean focusOnSearch = settings.getBehaviourSettings().getFilterInSelectors();
        setMenuItemSearchIconified(!focusOnSearch);

        setEmptyText(getActivity().getResources().getString(R.string.category_empty_list));

        /*
            Define the layout.
            Show category selector (arrow) when used as a picker.
            Show simple list when opened independently.
        */
        mLayout = R.layout.simple_expandable_list_item_selector;

        // manage context menu
        registerForContextMenu(getExpandableListView());

        getExpandableListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        setListShown(false);

        addListClickHandlers();

        // start loader
        getLoaderManager().initLoader(ID_LOADER_CATEGORYSUB, null, this);

        setFloatingActionButtonVisible(true);
        setFloatingActionButtonAttachListView(true);

        // Hide default group indicator
        getExpandableListView().setGroupIndicator(null);
    }

    private void restoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState.containsKey(KEY_ID_GROUP)) {
            mIdGroupChecked = savedInstanceState.getInt(KEY_ID_GROUP);
        }
        if (savedInstanceState.containsKey(KEY_ID_CHILD)) {
            mIdChildChecked = savedInstanceState.getInt(KEY_ID_CHILD);
        }
        if (savedInstanceState.containsKey(KEY_CUR_FILTER)) {
            mCurFilter = savedInstanceState.getString(KEY_CUR_FILTER, "");
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;

        int type = ExpandableListView.getPackedPositionType(info.packedPosition);
        int group = ExpandableListView.getPackedPositionGroup(info.packedPosition);
        int child = ExpandableListView.getPackedPositionChild(info.packedPosition);

        if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
            menu.setHeaderTitle(mCategories.get(group).getName());
        } else if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            QueryCategorySubCategory subCategory = mSubCategories.get(mCategories.get(group)).get(child);
            menu.setHeaderTitle(subCategory.getCategName().toString() + ": " + subCategory.getSubcategoryName().toString());
        }

        // context menu from resource
        menu.add(Menu.NONE, ContextMenuIds.EDIT.getId(), Menu.NONE, getString(R.string.edit));
        menu.add(Menu.NONE, ContextMenuIds.DELETE.getId(), Menu.NONE, getString(R.string.delete));
        menu.add(Menu.NONE, ContextMenuIds.VIEW_TRANSACTIONS.getId(), Menu.NONE, getString(R.string.view_transactions));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) item.getMenuInfo();

        int type = ExpandableListView.getPackedPositionType(info.packedPosition);
        int group = ExpandableListView.getPackedPositionGroup(info.packedPosition);
        int child = ExpandableListView.getPackedPositionChild(info.packedPosition);

        CategorySub categoryIds = new CategorySub();
        categoryIds.categId = Constants.NOT_SET;
        categoryIds.categName = "";
        categoryIds.subCategId = Constants.NOT_SET;
        categoryIds.subCategName = "";

        if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
            categoryIds.categId = mCategories.get(group).getId();
            categoryIds.categName = mCategories.get(group).getName();
        } else if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            categoryIds.categId = mSubCategories.get(mCategories.get(group)).get(child).getCategId();
            categoryIds.subCategId = mSubCategories.get(mCategories.get(group)).get(child).getSubCategId();
            categoryIds.subCategName = mSubCategories.get(mCategories.get(group)).get(child)
                    .getSubcategoryName().toString();
        }
        // manage select menu
        ContextMenuIds menuId = ContextMenuIds.get(item.getItemId());
        switch (menuId) {
            case EDIT:
                if (categoryIds.subCategId == ExpandableListView.INVALID_POSITION) {
                    showDialogEditCategoryName(SQLTypeTransaction.UPDATE, categoryIds.categId,
                            categoryIds.categName);
                } else {
                    showDialogEditSubCategoryName(SQLTypeTransaction.UPDATE, categoryIds.categId,
                            categoryIds.subCategId, categoryIds.subCategName);
                }
                break;

            case DELETE:
                showDialogDeleteCategorySub(categoryIds);
                break;

            case VIEW_TRANSACTIONS: // view transactions
                SearchParameters parameters = new SearchParameters();
                parameters.category = categoryIds;

                showSearchActivityFor(parameters);
        }
        return false;
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (getExpandableListAdapter() != null && getExpandableListAdapter().getGroupCount() > 0) {
            outState.putInt(KEY_ID_GROUP, ((CategoryExpandableListAdapter) getExpandableListAdapter()).getIdGroupChecked());
            outState.putInt(KEY_ID_CHILD, ((CategoryExpandableListAdapter) getExpandableListAdapter()).getIdChildChecked());
            outState.putString(KEY_CUR_FILTER, mCurFilter);
        }
    }

    // Data loader

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case ID_LOADER_CATEGORYSUB:
                // update id selected
                if (getExpandableListAdapter() != null && getExpandableListAdapter().getGroupCount() > 0) {
                    CategoryExpandableListAdapter adapter = (CategoryExpandableListAdapter) getExpandableListAdapter();
                    mIdGroupChecked = adapter.getIdGroupChecked();
                    mIdChildChecked = adapter.getIdChildChecked();
                }
                // clear arraylist and hashmap
                mCategories.clear();
                mSubCategories.clear();

                // load data
                String whereClause = null;
                String selectionArgs[] = null;
                if (!TextUtils.isEmpty(mCurFilter)) {
                    whereClause = QueryCategorySubCategory.CATEGNAME + " LIKE ? OR "
                            + QueryCategorySubCategory.SUBCATEGNAME + " LIKE ?";
                    selectionArgs = new String[]{mCurFilter + "%", mCurFilter + "%"};
                }
                Select query = new Select(mQuery.getAllColumns())
                    .where(whereClause, selectionArgs)
                    .orderBy(QueryCategorySubCategory.CATEGNAME + ", " + QueryCategorySubCategory.SUBCATEGNAME);

                return new MmxCursorLoader(getActivity(), mQuery.getUri(), query);
        }
        return null;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case ID_LOADER_CATEGORYSUB:
                // clear the data storage collections.
                mCategories.clear();
                mSubCategories.clear();
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case ID_LOADER_CATEGORYSUB:
                setListAdapter(getAdapter(data));

                if (isResumed()) {
                    setListShown(true);

                    boolean noData = data == null || data.getCount() <= 0;
                    if (noData && getFloatingActionButton() != null) {
                        getFloatingActionButton().show(true);
                    }
                } else {
                    setListShownNoAnimation(true);
                }

                for (int i = 0; i < mPositionToExpand.size(); i++) {
                    getExpandableListView().expandGroup(mPositionToExpand.get(i));
                }
        }
    }

    // Other

    @Override
    protected void setResult() {
        if (Intent.ACTION_PICK.equals(mAction)) {
            if (getExpandableListAdapter() == null) return;

            Intent result = null;

            if (getExpandableListAdapter() instanceof CategoryExpandableListAdapter) {
                CategoryExpandableListAdapter adapter = (CategoryExpandableListAdapter) getExpandableListAdapter();
                int categId = adapter.getIdGroupChecked();
                int subCategId = adapter.getIdChildChecked();

                if (categId == ExpandableListView.INVALID_POSITION) return;

                for (int groupIndex = 0; groupIndex < mCategories.size(); groupIndex++) {
                    if (mCategories.get(groupIndex).getId() == categId) {
                        // Get subcategory
                        if (subCategId != ExpandableListView.INVALID_POSITION) {
                            for (int child = 0; child < mSubCategories.get(mCategories.get(groupIndex)).size(); child++) {
                                if (mSubCategories.get(mCategories.get(groupIndex)).get(child).getSubCategId() == subCategId) {
                                    result = new Intent();
                                    result.putExtra(CategoryListActivity.INTENT_RESULT_CATEGID, categId);
                                    result.putExtra(CategoryListActivity.INTENT_RESULT_CATEGNAME,
                                            mSubCategories.get(mCategories.get(groupIndex)).get(child).getCategName().toString());
                                    result.putExtra(CategoryListActivity.INTENT_RESULT_SUBCATEGID, subCategId);
                                    result.putExtra(CategoryListActivity.INTENT_RESULT_SUBCATEGNAME,
                                            mSubCategories.get(mCategories.get(groupIndex)).get(child).getSubcategoryName().toString());
                                    break;
                                }
                            }
                        } else {
                            result = new Intent();
                            result.putExtra(CategoryListActivity.INTENT_RESULT_CATEGID, categId);
                            result.putExtra(CategoryListActivity.INTENT_RESULT_CATEGNAME,
                                    mCategories.get(groupIndex).getName());
                            result.putExtra(CategoryListActivity.INTENT_RESULT_SUBCATEGID, subCategId);
                            result.putExtra(CategoryListActivity.INTENT_RESULT_SUBCATEGNAME, "");
                        }
                    }
                }
            }

            if (result != null) {
                result.putExtra(CategoryListActivity.KEY_REQUEST_ID, this.requestId);

                getActivity().setResult(Activity.RESULT_OK, result);
            } else {
                getActivity().setResult(Activity.RESULT_CANCELED);
            }
        }
    }

    @Override
    public String getSubTitle() {
        return getString(R.string.categories);
    }

    @Override
    public void onFloatingActionButtonClickListener() {
        showTypeSelectorDialog();
    }

    public CategoryExpandableListAdapter getAdapter(Cursor data) {
        if (data == null) return null;

        mCategories.clear();
        mSubCategories.clear();
        mPositionToExpand.clear();
        // create core and fixed string filter to highlight
        Core core = new Core(getActivity().getApplicationContext());
        String filter = mCurFilter != null ? mCurFilter.replace("%", "") : "";

        int key = -1;
        List<QueryCategorySubCategory> listSubCategories = null;

        // reset cursor if getting back on the fragment.
        if (data.getPosition() > 0) {
            data.moveToPosition(Constants.NOT_SET);
        }

        while (data.moveToNext()) {
            if (key != data.getInt(data.getColumnIndex(QueryCategorySubCategory.CATEGID))) {
                // check if listCategories > 0
                if (mCategories.size() > 0 && listSubCategories != null) {
                    mSubCategories.put(mCategories.get(mCategories.size() - 1), listSubCategories);
                }
                // update key
                key = data.getInt(data.getColumnIndex(QueryCategorySubCategory.CATEGID));

                // create instance category
                Category category = new Category();
                category.setId(data.getInt(data.getColumnIndex(QueryCategorySubCategory.CATEGID)));
                category.setName(core.highlight(filter, data.getString(data.getColumnIndex(QueryCategorySubCategory.CATEGNAME))).toString());

                // add list
                mCategories.add(category);
                listSubCategories = new ArrayList<>();
            }

            if (data.getInt(data.getColumnIndex(QueryCategorySubCategory.SUBCATEGID)) != Constants.NOT_SET) {
                QueryCategorySubCategory subCategory = new QueryCategorySubCategory(getActivity());
                // subcategory
                subCategory.setSubCategId(data.getInt(data.getColumnIndex(QueryCategorySubCategory.SUBCATEGID)));
                subCategory.setSubcategoryName(core.highlight(filter, data.getString(data.getColumnIndex(QueryCategorySubCategory.SUBCATEGNAME))));
                subCategory.setCategId(data.getInt(data.getColumnIndex(QueryCategorySubCategory.CATEGID)));
                subCategory.setCategName(core.highlight(filter, data.getString(data.getColumnIndex(QueryCategorySubCategory.CATEGNAME))));
                // add to hashmap
                listSubCategories.add(subCategory);
                // check if expand group
                if (!TextUtils.isEmpty(filter)) {
                    String normalizedText = Normalizer.normalize(subCategory.getSubcategoryName(), Normalizer.Form.NFD)
                        .replaceAll("\\p{InCombiningDiacriticalMarks}+", "").toLowerCase();
                    if ((normalizedText.indexOf(filter) >= 0) && (!mPositionToExpand.contains(mCategories.size() - 1))) {
                        mPositionToExpand.add(mCategories.size() - 1);
                    }
                }
            }
        }
        if (mCategories.size() > 0 && listSubCategories != null) {
            mSubCategories.put(mCategories.get(mCategories.size() - 1), listSubCategories);
        }

        boolean showSelector = mAction.equals(Intent.ACTION_PICK);
        CategoryExpandableListAdapter adapter = new CategoryExpandableListAdapter(getActivity(),
            mLayout, mCategories, mSubCategories, showSelector);
        adapter.setIdChildChecked(mIdGroupChecked, mIdChildChecked);
        return adapter;
    }

    // Private

    /**
     * Restart loader to view data
     */
    private void restartLoader() {
        getLoaderManager().restartLoader(ID_LOADER_CATEGORYSUB, null, this);
    }

    /**
     * Show alter binaryDialog confirm delete category or sub category
     */
    private void showDialogDeleteCategorySub(final CategorySub categoryIds) {
        boolean canDelete;
        CategoryService service = new CategoryService(getActivity());
        ContentValues values = new ContentValues();

        if (categoryIds.subCategId <= 0) {
            values.put(Category.CATEGID, categoryIds.categId);
            canDelete = !service.isCategoryUsed(categoryIds.categId);
        } else {
            values.put(Subcategory.SUBCATEGID, categoryIds.subCategId);
            canDelete = !service.isSubcategoryUsed(categoryIds.categId);
        }
        if (!(canDelete)) {
            new MaterialDialog.Builder(getContext())
                .title(R.string.attention)
                .icon(new UIHelper(getActivity()).getIcon(GoogleMaterial.Icon.gmd_warning))
                .content(R.string.category_can_not_deleted)
                .positiveText(android.R.string.ok)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .build().show();
            return;
        }

        // Prompt for deletion.
        new MaterialDialog.Builder(getContext())
            .title(R.string.delete_category)
            .icon(new UIHelper(getActivity()).getIcon(GoogleMaterial.Icon.gmd_warning))
            .content(R.string.confirmDelete)
            .positiveText(android.R.string.ok)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        int rowsDelete;
                        if (categoryIds.subCategId <= 0) {
                            CategoryRepository repo = new CategoryRepository(getActivity());
                            rowsDelete = getActivity().getContentResolver().delete(repo.getUri(),
                                    Category.CATEGID + "=" + categoryIds.categId,
                                    null);
                        } else {
                            SubcategoryRepository repo = new SubcategoryRepository(getActivity());

                            rowsDelete = getActivity().getContentResolver().delete(repo.getUri(),
                                    Subcategory.CATEGID + "=" + categoryIds.categId + " AND " +
                                            Subcategory.SUBCATEGID + "=" + categoryIds.subCategId,
                                    null);
                        }
                        if (rowsDelete == 0) {
                            Toast.makeText(getActivity(), R.string.db_delete_failed, Toast.LENGTH_SHORT).show();
                        }
                        // restart loader
                        restartLoader();
                    }
                })
            .negativeText(android.R.string.cancel)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
//                        dialog.dismiss();
                        dialog.cancel();
                    }
                })
            .build().show();
    }

    /**
     * Show alter binaryDialog, for create or edit new category
     */
    private void showDialogEditCategoryName(final SQLTypeTransaction type, final int categoryId,
                                            final CharSequence categoryName) {
        // inflate view
        View viewDialog = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_new_edit_category, null);

        final EditText edtCategName = viewDialog.findViewById(R.id.editTextCategName);
        // set category description
        edtCategName.setText(categoryName);
        if (!TextUtils.isEmpty(categoryName)) {
            edtCategName.setSelection(categoryName.length());
        }

        int titleId = type.equals(SQLTypeTransaction.INSERT)
                ? R.string.add_category
                : R.string.edit_categoryName;

        UIHelper ui = new UIHelper(getActivity());

        new MaterialDialog.Builder(getContext())
            .customView(viewDialog, true)
            .icon(ui.getIcon(FontAwesome.Icon.faw_tags))
            .title(titleId)
                .positiveText(android.R.string.ok)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        // get category description
                        String name = edtCategName.getText().toString();
                        CategoryService service = new CategoryService(getActivity());

                        switch (type) {
                            case INSERT:
                                int insertResult = service.createNew(name);

                                if (insertResult <= 0) {
                                    Toast.makeText(getActivity(), R.string.db_insert_failed, Toast.LENGTH_SHORT).show();
                                }
                                break;
                            case UPDATE:
                                int updateResult = service.update(categoryId, name);
                                if (updateResult <= 0) {
                                    Toast.makeText(getActivity(), R.string.db_update_failed, Toast.LENGTH_SHORT).show();
                                }
                                break;
                        }
                        // restart loader
                        restartLoader();
                    }
                })
                .negativeText(android.R.string.cancel)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.cancel();
                    }
                })
        .build().show();
    }

    /**
     * Show alter binaryDialog, for create or edit new category
     */
    private void showDialogEditSubCategoryName(final SQLTypeTransaction type, final int categoryId,
                                               final int subCategoryId, final CharSequence subCategName) {

        // inflate view
        View viewDialog = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_new_edit_subcategory, null);

        final EditText edtSubCategName = (EditText) viewDialog.findViewById(R.id.editTextCategName);
        final Spinner spnCategory = (Spinner) viewDialog.findViewById(R.id.spinnerCategory);
        // set category description
        edtSubCategName.setText(subCategName);
        if (!TextUtils.isEmpty(subCategName)) {
            edtSubCategName.setSelection(subCategName.length());
        }

        // Fill categories list.
        CategoryService categoryService = new CategoryService(getActivity());
        final List<Category> categories = categoryService.getList();

        ArrayList<String> categoryNames = new ArrayList<>();
        ArrayList<Integer> categoryIds = new ArrayList<>();
        for (Category category : categories) {
            categoryIds.add(category.getId());
            categoryNames.add(category.getName().toString());
        }
        ArrayAdapter<String> adapterCategory = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, categoryNames);
        adapterCategory.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnCategory.setAdapter(adapterCategory);
        //select category if present
        if (categoryId > 0) {
            spnCategory.setSelection(categoryIds.indexOf(categoryId), true);
        }

        int titleId = type.equals(SQLTypeTransaction.INSERT)
                ? R.string.add_subcategory
                : R.string.edit_categoryName;

        UIHelper ui = new UIHelper(getActivity());

        new MaterialDialog.Builder(getContext())
            .customView(viewDialog, true)
            .icon(ui.getIcon(FontAwesome.Icon.faw_tags))
            .title(titleId)
                .positiveText(android.R.string.ok)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        // get category description
                        String name = edtSubCategName.getText().toString();
                        // check position
                        if (spnCategory.getSelectedItemPosition() == Spinner.INVALID_POSITION)
                            return;
                        // get category id
                        int categId = categories.get(spnCategory.getSelectedItemPosition()).getId();
                        ContentValues values = new ContentValues();
                        values.put(Subcategory.CATEGID, categId);
                        values.put(Subcategory.SUBCATEGNAME, name);

                        SubcategoryRepository repo = new SubcategoryRepository(getActivity());

                        // check type transaction is request
                        switch (type) {
                            case INSERT:
                                if (getActivity().getContentResolver().insert(repo.getUri(), values) == null) {
                                    Toast.makeText(getActivity(), R.string.db_insert_failed, Toast.LENGTH_SHORT).show();
                                }
                                break;
                            case UPDATE:
                                if (getActivity().getContentResolver().update(
                                        repo.getUri(),
                                        values,
                                        Subcategory.CATEGID + "="
                                                + categoryId + " AND "
                                                + Subcategory.SUBCATEGID
                                                + "=" + subCategoryId, null) == 0) {
                                    Toast.makeText(getActivity(), R.string.db_update_failed, Toast.LENGTH_SHORT).show();
                                }
                                break;
                        }
                        // restart loader
                        restartLoader();
                    }
                })
                .negativeText(android.R.string.cancel)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.cancel();
                    }
                })
            .build().show();
    }

    private void addListClickHandlers() {
        // the list handlers available only when selecting a category.
        if (mAction.equals(Intent.ACTION_PICK)) {
            getExpandableListView().setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                    if (getExpandableListAdapter() != null && getExpandableListAdapter() instanceof CategoryExpandableListAdapter) {
                        CategoryExpandableListAdapter adapter = (CategoryExpandableListAdapter) getExpandableListAdapter();

                        QueryCategorySubCategory data = mSubCategories.get(mCategories.get(groupPosition)).get(childPosition);

                        adapter.setIdChildChecked(data.getCategId(), data.getSubCategId());
                        adapter.notifyDataSetChanged();

                        // select sub-categories immediately.
                        setResultAndFinish();
                    }
                    return false;
                }
            });

            getExpandableListView().setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

                @Override
                public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                    if (getExpandableListAdapter() != null && getExpandableListAdapter() instanceof CategoryExpandableListAdapter) {
                        CategoryExpandableListAdapter adapter = (CategoryExpandableListAdapter) getExpandableListAdapter();

                        Category category = mCategories.get(groupPosition);

                        adapter.setIdGroupChecked(category.getId());
                        adapter.notifyDataSetChanged();

                        int subCategoriesCount = mSubCategories.get(category).size();
                        if (subCategoriesCount == 0) {
                            setResultAndFinish();
                        }
                    }
                    return false;
                }
            });
        }
    }

    /**
     * Choose the item type: category / subcategory.
     */
    private void showTypeSelectorDialog() {
        new MaterialDialog.Builder(getActivity())
            .title(R.string.choose_type)
            .items(R.array.category_type)
            .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                @Override
                public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                    /**
                     * If you use alwaysCallSingleChoiceCallback(), which is discussed below,
                     * returning false here won't allow the newly selected radio button to actually be selected.
                     **/
//                        showNameEntryDialog();

                    // todo: depending on the choice, show the edit binaryDialog. 0-based
                    if (which == 0) {
                        showDialogEditCategoryName(SQLTypeTransaction.INSERT, -1, null);
                    } else {
                        showDialogEditSubCategoryName(SQLTypeTransaction.INSERT, -1, -1, null);
                    }

                    return true;
                }
            })
            .positiveText(android.R.string.ok)
//                .negativeText(android.R.string.cancel)
            .neutralText(android.R.string.cancel)
            .show();
    }

    private void showSearchActivityFor(SearchParameters parameters) {
        Intent intent = new Intent(getActivity(), SearchActivity.class);
        intent.putExtra(SearchActivity.EXTRA_SEARCH_PARAMETERS, Parcels.wrap(parameters));
        intent.setAction(Intent.ACTION_INSERT);
        startActivity(intent);
    }

}
