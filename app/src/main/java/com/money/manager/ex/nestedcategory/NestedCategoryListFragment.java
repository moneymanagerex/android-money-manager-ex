/*
 * Copyright (C) 2024-2024 The Android Money Manager Ex Project Team
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
package com.money.manager.ex.nestedcategory;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.adapter.MoneySimpleCursorAdapter;
import com.money.manager.ex.common.BaseListFragment;
import com.money.manager.ex.common.CategoryListActivity;
import com.money.manager.ex.common.MmxCursorLoader;
import com.money.manager.ex.core.ContextMenuIds;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.database.SQLTypeTransaction;
import com.money.manager.ex.datalayer.CategoryRepository;
import com.money.manager.ex.datalayer.Select;
import com.money.manager.ex.domainmodel.Category;
import com.money.manager.ex.search.CategorySub;
import com.money.manager.ex.search.SearchActivity;
import com.money.manager.ex.search.SearchParameters;
import com.money.manager.ex.servicelayer.CategoryService;
import com.money.manager.ex.settings.AppSettings;

import org.apache.commons.lang3.ArrayUtils;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

/**
 * List of Category. Used as a picker/selector also.
 */
public class NestedCategoryListFragment
        extends BaseListFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    public String mAction = Intent.ACTION_EDIT;
    public Integer requestId; // this is used in splittransaction. not sure that this is the correct place

    private static final int ID_LOADER_NESTEDCATEGORY = 0;

    private static final int ORDER_BY_NAME = 0;
    private static final int ORDER_BY_USAGE = 1;
    private static final int ORDER_BY_RECENT = 2;
    private static final String SORT_BY_NAME = "UPPER(" + QueryNestedCategory.CATEGNAME + ")";
    // note use T. for resovle name from dinamic from
    private static final String SORT_BY_USAGE = "(SELECT COUNT(*) \n" +
            "FROM CHECKINGACCOUNT_V1 \n" +
            "WHERE T.CATEGID = CHECKINGACCOUNT_V1.CATEGID\n" +
            "  AND (CHECKINGACCOUNT_V1.DELETEDTIME IS NULL OR CHECKINGACCOUNT_V1.DELETEDTIME = '')) DESC";
    private static final String SORT_BY_RECENT = "(SELECT max( TRANSDATE ) \n" +
            " FROM CHECKINGACCOUNT_V1 \n" +
            " WHERE T.CATEGID = CHECKINGACCOUNT_V1.CATEGID \n" +
            "   AND (CHECKINGACCOUNT_V1.DELETEDTIME IS NULL OR CHECKINGACCOUNT_V1.DELETEDTIME = '') ) DESC";

    private static int NAVMODE_UNKNOW = -1;
    private static int NAVMODE_LIST = 0;
    private static int NAVMODE_TREE = 1;


    //    private Context mContext;
    private String mCurFilter;
    private int levelMode = NAVMODE_UNKNOW;
    private long rootCategoryId = -1;


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setSearchMenuVisible(true);

        levelMode = (new AppSettings(getActivity()).getCategoryNavMode());
        if (levelMode == NAVMODE_UNKNOW) {
/*
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(R.string.attention)
                    .setIcon(new UIHelper(getActivity()).getIcon(FontAwesome.Icon.faw_question))
                    .setMessage(R.string.category_nav_mode)
                    .setPositiveButton(R.string.remember_later, (dialog, which) -> {
                    })
                    .setNegativeButton(R.string.dismiss, (dialog, which) -> {
                        new AppSettings(getActivity()).setCategoryNavMode(levelMode);
                    })
                    .show();
 */
            levelMode = NAVMODE_TREE;
            new AppSettings(getActivity()).setCategoryNavMode(levelMode);
        }
        rootCategoryId = -1; // reset filter

        setHasOptionsMenu(true);

        // Focus on search menu if set in preferences.
        AppSettings settings = new AppSettings(getActivity());
        boolean focusOnSearch = settings.getBehaviourSettings().getFilterInSelectors();
        setMenuItemSearchIconified(!focusOnSearch);

        setEmptyText(getActivity().getResources().getString(R.string.category_empty_list));

//        int layout = android.R.layout.simple_list_item_1;
        int layout = R.layout.simple_list_item_1_with_selector;

        // associate adapter
//        MoneySimpleCursorAdapter adapter = new MoneySimpleCursorAdapter(getActivity(),
//                layout, null, new String[]{QueryNestedCategory.CATEGNAME, QueryNestedCategory.ID},
//                new int[]{R.id.text1, R.id.selector}, 0);
        MoneySimpleCursorAdapter adapter = new MoneySimpleCursorAdapter(getActivity(),
                layout, null, new String[]{QueryNestedCategory.ID},
                new int[]{R.id.single_row}, 0);

        adapter.setViewBinder((aView, aCursor, aColumnIndex) -> {
            NestedCategoryEntity nestedCategory = new NestedCategoryEntity();
            nestedCategory.loadFromCursor(aCursor);

            aView.setTag(nestedCategory.getId());

            TextView foldericon = aView.findViewById(R.id.foldericon);
            TextView textView = aView.findViewById(R.id.text1);
            TextView selector = aView.findViewById(R.id.selector);

            textView.setVisibility(View.VISIBLE);
            if (levelMode == NAVMODE_LIST || !TextUtils.isEmpty(mCurFilter)) {
                foldericon.setVisibility(View.GONE);
//                selector.setVisibility(View.GONE);
            } else {
                foldericon.setVisibility(View.VISIBLE);
                // set foldericon
                if (rootCategoryId == nestedCategory.getId()) {
                    // this is actual root
                    foldericon.setText("x"); // <
                } else if (nestedCategory.hasChildren()) {
                    foldericon.setText("  C");
                } else {
                    foldericon.setText(" ");
                }

            }

            selector.setVisibility(View.VISIBLE);
            if (mAction.equals(Intent.ACTION_PICK)) {
                // pick
                selector.setText("o");
            } else {
                selector.setText("j");
            }

            if (mAction.equals(Intent.ACTION_PICK)) {
                selector.setOnClickListener(v -> {
                    sendResultToActivity(nestedCategory.getId(), nestedCategory.getCategoryName());
                });
            } else {
                selector.setOnClickListener(v -> {
                    onListItemLongClick(getListView(), v, getListView().getPositionForView(v), nestedCategory.getId());
                });

            }
            foldericon.setOnClickListener(v -> {
                onListItemClick(getListView(), v, getListView().getPositionForView(v), nestedCategory.getId());
            });

            boolean active = nestedCategory.getActiveAsBoolean();
            CharSequence text;
            if (levelMode != NAVMODE_LIST && TextUtils.isEmpty(mCurFilter)) {
                if (rootCategoryId != nestedCategory.getParentId() || rootCategoryId == -1) {
                    // this is actual root
                    text = nestedCategory.getCategoryName();
                } else {
                    text = "  " + nestedCategory.getBasename();
                }
            } else {
                text = nestedCategory.getCategoryName();
            }
            if (!TextUtils.isEmpty(adapter.getHighlightFilter())) {
                text = adapter.getCore().highlight(adapter.getHighlightFilter(), text.toString());
            }
            if (!active) {
                textView.setText(Html.fromHtml("<i>" + text + " [inactive]</i>", Html.FROM_HTML_MODE_COMPACT));
            } else {
                textView.setText(text);
            }
            return true;
        });


        // set adapter
        setListAdapter(adapter);

        registerForContextMenu(getListView());
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        setListShown(false);

        // start loader
//        LoaderManager.getInstance(getActivity()).initLoader(ID_LOADER_NESTEDCATEGORY, null, this);
        getLoaderManager().initLoader(ID_LOADER_NESTEDCATEGORY, null, this);

        setFabVisible(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_sort, menu);

        switch ((new AppSettings(getActivity())).getCategorySort()) {
            case ORDER_BY_USAGE:
                menu.findItem(R.id.menu_sort_usage).setChecked(true);
                break;
            case ORDER_BY_RECENT:
                menu.findItem(R.id.menu_sort_recent).setChecked(true);
                break;
            default:
                menu.findItem(R.id.menu_sort_name).setChecked(true);
                break;
        }
        if (mAction.equals(Intent.ACTION_PICK)) {
            menu.findItem(R.id.menu_show_inactive).setVisible(false);
        } else {
            menu.findItem(R.id.menu_show_inactive).setVisible(true);
            menu.findItem(R.id.menu_show_inactive).setChecked((new AppSettings(getActivity())).getShowInactive());
        }

        // add menu option for navigatio
        MenuItem menuNavigation = menu.findItem(R.id.menu_sort).getSubMenu().add("Navigation Mode");
        menuNavigation.setCheckable(true);
        menuNavigation.setChecked(levelMode != NAVMODE_LIST);
        menuNavigation.setOnMenuItemClickListener(item -> {
            levelMode = (levelMode == NAVMODE_LIST ? NAVMODE_TREE : NAVMODE_LIST);
            new AppSettings(getActivity()).setCategoryNavMode(levelMode);
            item.setChecked(levelMode != NAVMODE_LIST);
            rootCategoryId = -1;
            restartLoader();
            return true;
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AppSettings settings = new AppSettings(getActivity());
        if (item.getItemId() == android.R.id.home) {
            // we wont to invalidate cursor, so if back is pressed no item is select
            Cursor cursor = ((SimpleCursorAdapter) getListAdapter()).getCursor();
            cursor.moveToPosition(-1);
            // and continue with super
        }

        if (item.getItemId() == R.id.menu_sort_name ||
                item.getItemId() == R.id.menu_sort_usage ||
                item.getItemId() == R.id.menu_sort_recent) {
            if (item.getItemId() == R.id.menu_sort_usage) {
                settings.setCategorySort(ORDER_BY_USAGE);
            } else if (item.getItemId() == R.id.menu_sort_recent) {
                settings.setCategorySort(ORDER_BY_RECENT);
            } else {
                settings.setCategorySort(ORDER_BY_NAME);
            }
            item.setChecked(true);
            // restart search
            restartLoader();
            return true;
        }

        if (item.getItemId() == R.id.menu_show_inactive) {
            item.setChecked(!item.isChecked());
            settings.setShowInactive(item.isChecked());
            // restart search
            restartLoader();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("Range")
    @Override
    public void onCreateContextMenu(ContextMenu menu, @NonNull View v, ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

        Cursor cursor = ((SimpleCursorAdapter) getListAdapter()).getCursor();
        cursor.moveToPosition(info.position);
        menu.setHeaderTitle(cursor.getString(cursor.getColumnIndexOrThrow(QueryNestedCategory.CATEGNAME)));

        // context menu from resource
        menu.add(Menu.NONE, ContextMenuIds.ADD_SUB.getId(), Menu.NONE, getString(R.string.add_subcategory));
        menu.add(Menu.NONE, ContextMenuIds.EDIT.getId(), Menu.NONE, getString(R.string.edit));
        menu.add(Menu.NONE, ContextMenuIds.DELETE.getId(), Menu.NONE, getString(R.string.delete));
        menu.add(Menu.NONE, ContextMenuIds.VIEW_TRANSACTIONS.getId(), Menu.NONE, getString(R.string.view_transactions));
        menu.add(Menu.NONE, ContextMenuIds.VIEW_TRANSACTIONS_SUB.getId(), Menu.NONE, getString(R.string.view_transactions_sub));
        menu.add(Menu.NONE, ContextMenuIds.SWITCH_ACTIVE.getId(), Menu.NONE, getString(R.string.switch_active));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info;
        if (item.getMenuInfo() instanceof AdapterView.AdapterContextMenuInfo) {
            info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } else {
            return false;
        }

        Cursor cursor = ((SimpleCursorAdapter) getListAdapter()).getCursor();
        cursor.moveToPosition(info.position);

        NestedCategoryEntity nestedCategory = new NestedCategoryEntity();
        nestedCategory.loadFromCursor(cursor);

        Category category = nestedCategory.asCategory();

        // manage select menu
        ContextMenuIds menuId = ContextMenuIds.get(item.getItemId());
        if (menuId == null) return false;
        switch (menuId) {
            case ADD_SUB:
                Category newCat = new Category();
                newCat.setParentId(category.getId());
                showDialogEditSubCategoryName(SQLTypeTransaction.INSERT,
                        newCat);
                break;
            case EDIT:
                showDialogEditSubCategoryName(SQLTypeTransaction.UPDATE,
                        category);
                break;

            case DELETE:
                showDialogDeleteCategorySub(category);
                break;

            case VIEW_TRANSACTIONS: // view transactions
            case VIEW_TRANSACTIONS_SUB:
                SearchParameters parameters = new SearchParameters();
                CategorySub catSub = new CategorySub();
                catSub.categId = category.getId();
                catSub.categName = category.getName();
                parameters.category = catSub;

                if (menuId == ContextMenuIds.VIEW_TRANSACTIONS_SUB) {
                    parameters.searchSubCategory = true;
                }

                showSearchActivityFor(parameters);
                break;
            case SWITCH_ACTIVE:
                category.setActive(!category.getActive());
                CategoryService service = new CategoryService(getActivity());
                service.update(category);
                restartLoader();
                break;
        }
        return false;
    }


    // Data loader

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == ID_LOADER_NESTEDCATEGORY) {// update id selected
            if ( !TextUtils.isEmpty(mCurFilter) && mCurFilter.equalsIgnoreCase("%") ) {
                mCurFilter = "";
            }
            // load data
            String whereClause = "";
            String[] selectionArgs = new String[]{};

            if (levelMode == NAVMODE_TREE ) {
                if (!TextUtils.isEmpty(whereClause)) {
                    whereClause += " AND ";
                }
                if ( TextUtils.isEmpty(mCurFilter ) ) {
                    whereClause += "(" + QueryNestedCategory.PARENTID + " = ?  OR " + QueryNestedCategory.CATEGID + " = ? )";
                    selectionArgs = ArrayUtils.add(selectionArgs, String.valueOf(rootCategoryId));
                    selectionArgs = ArrayUtils.add(selectionArgs, String.valueOf(rootCategoryId));
                } else {
                    // search mode with level
                    whereClause += "(" + QueryNestedCategory.FULLCATID + " LIKE ? )";
                    if (rootCategoryId == -1) {
                        selectionArgs = ArrayUtils.add(selectionArgs, ":%:");
                    } else {
                        StringBuilder builder = new StringBuilder();
                        builder.append(":");
                        builder.append(rootCategoryId);
                        builder.append(":%");
                        selectionArgs = ArrayUtils.add(selectionArgs,builder.toString());
                    }
                }
            }

            if (mAction == Intent.ACTION_PICK
                    || !(new AppSettings(getContext())).getShowInactive()) {
                if (!TextUtils.isEmpty(whereClause)) {
                    whereClause += " AND ";
                }
                whereClause += "ACTIVE <> 0";
            }
            if (!TextUtils.isEmpty(mCurFilter)) {
                if (!TextUtils.isEmpty(whereClause)) {
                    whereClause += " AND ";
                }
                whereClause += QueryNestedCategory.CATEGNAME + " LIKE ?";
                selectionArgs = ArrayUtils.add(selectionArgs, mCurFilter + "%");
            }
            QueryNestedCategory repo = new QueryNestedCategory(getActivity());
            String sort;
            switch ((new AppSettings(getContext())).getCategorySort()) {
                case ORDER_BY_USAGE:
                    sort = SORT_BY_USAGE;
                    break;
                case ORDER_BY_RECENT:
                    sort = SORT_BY_RECENT;
                    break;
                default:
                    sort = SORT_BY_NAME;
                    break;
            }
            Select query = new Select(repo.getAllColumns())
                    .where(whereClause, selectionArgs)
                    .orderBy(sort);

            return new MmxCursorLoader(getActivity(), repo.getUri(), query);
        }
        return null;
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        MoneySimpleCursorAdapter adapter = (MoneySimpleCursorAdapter) getListAdapter();
        adapter.changeCursor(null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if (data == null) return;

        if (loader.getId() == ID_LOADER_NESTEDCATEGORY) {
            MoneySimpleCursorAdapter adapter = (MoneySimpleCursorAdapter) getListAdapter();
            String highlightFilter = mCurFilter != null
                    ? mCurFilter.replace("%", "")
                    : "";
            adapter.setHighlightFilter(highlightFilter);
            adapter.changeCursor(data);

            if (isResumed()) {
                setListShown(true);
                if (data.getCount() <= 0 && getFloatingActionButton() != null) {
                    setFabVisible(true);
                }
            } else {
                try {
                    setListShownNoAnimation(true);
                } catch (Exception e) {

                }
            }
        }
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
    // Other

    @Override
    protected void setResult() {
        if (Intent.ACTION_PICK.equals(mAction)) {
            // Cursor that is already in the desired position, because positioned in the event onListItemClick
            Cursor cursor = ((SimpleCursorAdapter) getListAdapter()).getCursor();
            if (cursor.getCount() == 0 || cursor.getPosition() == -1) {
                // no record or no record selected
                sendResultToActivity(-1, null);
            } else {
                @SuppressLint("Range") long id = cursor.getLong(cursor.getColumnIndexOrThrow(QueryNestedCategory.CATEGID));
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndexOrThrow(QueryNestedCategory.CATEGNAME));
                sendResultToActivity(id, name);
            }

            return;
        }

        getActivity().setResult(CategoryListActivity.RESULT_CANCELED);

    }

    private void sendResultToActivity(long id, String name) {
        Intent result = new Intent();
        result.putExtra(CategoryListActivity.INTENT_RESULT_CATEGID, id);
        result.putExtra(CategoryListActivity.INTENT_RESULT_CATEGNAME, name);

        result.putExtra(CategoryListActivity.KEY_REQUEST_ID, this.requestId);

        getActivity().setResult(AppCompatActivity.RESULT_OK, result);

        getActivity().finish();
    }


    @Override
    public String getSubTitle() {
        return getString(R.string.categories);
    }

    public void onFloatingActionButtonClicked() {
        String search = !TextUtils.isEmpty(mCurFilter) ? mCurFilter.replace("%", "") : "";
        Category category = new Category();
        category.setName(search);
        showDialogEditSubCategoryName(SQLTypeTransaction.INSERT, category);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if ( (mAction.equals(Intent.ACTION_PICK)) &&
                !TextUtils.isEmpty(mCurFilter) && !mCurFilter.equalsIgnoreCase("%") ) {
            // we hare in pick mode and search for text
            // select item means return item to edit
            setResult();
        }
        super.onListItemClick(l, v, position, id);
        if (levelMode == NAVMODE_LIST ||
                !TextUtils.isEmpty(mCurFilter) && !mCurFilter.equalsIgnoreCase("%") ) {
            onListItemLongClick(l, v, position, id);
            return;
        }
        Cursor cursor = ((SimpleCursorAdapter) getListAdapter()).getCursor();
        if (cursor != null) {
            if (cursor.moveToPosition(position)) {
                NestedCategoryEntity nestedCategory = new NestedCategoryEntity();
                nestedCategory.loadFromCursor(cursor);
                // if selected item is root level come back
                if (nestedCategory.getCategoryId() == rootCategoryId) {
                    rootCategoryId = nestedCategory.getParentId();
                    restartLoader();
                    return;
                }

                // if has children go to level
                if (nestedCategory.hasChildren()) {
                    rootCategoryId = nestedCategory.getCategoryId();
                    restartLoader();
                    return;
                }
                // no special handling. call long item
                onListItemLongClick(l, v, position, id);
            }
        }
    }

    public void onListItemLongClick(ListView l, View v, int position, long id) {
        super.onListItemLongClick(l, v, position, id);

        // On select go back to the calling activity (if there is one)
        if (getActivity().getCallingActivity() != null) {
            Cursor cursor = ((SimpleCursorAdapter) getListAdapter()).getCursor();
            if (cursor != null) {
                if (cursor.moveToPosition(position)) {
                    setResultAndFinish();
                }
            }
        } else {
            // No calling activity, this is the independent tags view. Show context menu.
            getActivity().openContextMenu(v);
        }
    }

    // Private

    /**
     * Restart loader to view data
     */
    private void restartLoader() {
//        LoaderManager.getInstance(getActivity()).restartLoader(ID_LOADER_NESTEDCATEGORY, null, this);
        getLoaderManager().restartLoader(ID_LOADER_NESTEDCATEGORY, null, this);
    }

    /**
     * Show alter binaryDialog confirm delete category or sub category
     */
    private void showDialogDeleteCategorySub(final Category category) {
        boolean canDelete = false;
        CategoryService service = new CategoryService(getActivity());
        ContentValues values = new ContentValues();

        values.put(Category.CATEGID, category.getId());
        canDelete = !service.isCategoryUsedWithChildren(category.getId());

        if (!(canDelete)) {
            UIHelper ui = new UIHelper(getActivity());

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(R.string.attention)
                    .setIcon(ui.getIcon(GoogleMaterial.Icon.gmd_warning))
                    .setMessage(R.string.category_can_not_deleted)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
            return;
        }

        // Prompt for deletion.
        UIHelper ui = new UIHelper(getActivity());

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.delete_category)
                .setIcon(ui.getIcon(GoogleMaterial.Icon.gmd_warning))
                .setMessage(R.string.confirmDelete)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        long rowsDelete = 0;
                        CategoryRepository repo = new CategoryRepository(getActivity());
                        rowsDelete = getActivity().getContentResolver().delete(repo.getUri(),
                                Category.CATEGID + "=" + category.getId(),
                                null);
                        if (rowsDelete == 0) {
                            Toast.makeText(getActivity(), R.string.db_delete_failed, Toast.LENGTH_SHORT).show();
                        }
                        // restart loader
                        restartLoader();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();
    }

    /**
     * Show alter binaryDialog, for create or edit new category
     */
    private void showDialogEditSubCategoryName(final SQLTypeTransaction type, Category category) {

        // inflate view
        View viewDialog = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_new_edit_subcategory, null);

        final EditText edtSubCategName = viewDialog.findViewById(R.id.editTextCategName);
        final Spinner spnCategory = viewDialog.findViewById(R.id.spinnerCategory);
        // set category description
        edtSubCategName.setText(category.getBasename());
        if (!TextUtils.isEmpty(category.getBasename())) {
            edtSubCategName.setSelection(category.getBasename().length());
        }

        // Fill categories list.
        final List<NestedCategoryEntity> categories = (new QueryNestedCategory(getActivity())).getNestedCategoryEntities(null);

        ArrayList<String> categoryNames = new ArrayList<>();
        ArrayList<Long> categoryIds = new ArrayList<>();
        // todo add -1 and "<root>" for moving at top level
        categoryNames.add("<root>");
        categoryIds.add(Constants.NOT_SET);
        for (NestedCategoryEntity category1 : categories) {
            // if edit do not include category itself and all children form parent list
            if (type.equals(SQLTypeTransaction.INSERT) ||
                    category.getName() == null || !category1.getCategoryName().startsWith(category.getName())) {
                categoryIds.add(category1.getCategoryId());
                categoryNames.add(category1.getCategoryName());
            }
        }
        ArrayAdapter<String> adapterCategory = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, categoryNames);
        adapterCategory.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnCategory.setAdapter(adapterCategory);
        //select category if present
        if (category.getParentId() > 0) {
            spnCategory.setSelection(categoryIds.indexOf(category.getParentId()), true);
        }

        int titleId = type.equals(SQLTypeTransaction.INSERT)
                ? R.string.add_subcategory
                : R.string.edit_categoryName;

        UIHelper ui = new UIHelper(getActivity());

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(viewDialog)
                .setIcon(ui.getIcon(FontAwesome.Icon.faw_tags))
                .setTitle(titleId)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // get category description
                        String name = edtSubCategName.getText().toString();
                        // check position
                        if (spnCategory.getSelectedItemPosition() == Spinner.INVALID_POSITION)
                            return;
                        // get parent category id from list of categories in spin
                        long parentID = categoryIds.get(spnCategory.getSelectedItemPosition());
                        CategoryService service = new CategoryService(getActivity());

                        switch (type) {
                            case INSERT:
                                long insertResult = service.createNew(name, parentID);

                                if (insertResult <= 0) {
                                    Toast.makeText(getActivity(), R.string.db_insert_failed, Toast.LENGTH_SHORT).show();
                                }
                                break;
                            case UPDATE:
                                // TODO: issue 2187. move sometime create id = parentid
                                assert category.getId() != parentID;
                                long updateResult = service.update(category.getId(), name, parentID);
                                if (updateResult <= 0) {
                                    Toast.makeText(getActivity(), R.string.db_update_failed, Toast.LENGTH_SHORT).show();
                                }
                                break;
                        }
                        // restart loader
                        restartLoader();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();
    }


    private void showSearchActivityFor(SearchParameters parameters) {
        Intent intent = new Intent(getActivity(), SearchActivity.class);
        intent.putExtra(SearchActivity.EXTRA_SEARCH_PARAMETERS, Parcels.wrap(parameters));
        intent.setAction(Intent.ACTION_INSERT);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        // force reset loader on start. try to fix 2217, not the best code
        // becouse normaly was call duble
        restartLoader();

    }

}
