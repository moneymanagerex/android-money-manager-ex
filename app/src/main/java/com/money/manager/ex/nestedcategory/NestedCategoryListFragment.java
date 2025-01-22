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

    private static final String SORT_BY_NAME = "UPPER(" + QueryNestedCategory.CATEGNAME + ")";
// note use T. for resovle name from dinamic from
    private static final String SORT_BY_USAGE = "(SELECT COUNT(*) \n" +
            "FROM CHECKINGACCOUNT_V1 \n" +
            "WHERE T.CATEGID = CHECKINGACCOUNT_V1.CATEGID\n" +
            "  AND (CHECKINGACCOUNT_V1.DELETEDTIME IS NULL OR CHECKINGACCOUNT_V1.DELETEDTIME = '')) DESC";

//    private Context mContext;
    private String mCurFilter;
    private int mSort = 0;


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setSearchMenuVisible(true);

        setHasOptionsMenu(true);

        // Focus on search menu if set in preferences.
        AppSettings settings = new AppSettings(getActivity());
        boolean focusOnSearch = settings.getBehaviourSettings().getFilterInSelectors();
        setMenuItemSearchIconified(!focusOnSearch);

        setEmptyText(getActivity().getResources().getString(R.string.category_empty_list));

        int layout = android.R.layout.simple_list_item_1;

        // associate adapter
        MoneySimpleCursorAdapter adapter = new MoneySimpleCursorAdapter(getActivity(),
                layout, null, new String[] { QueryNestedCategory.CATEGNAME },
                new int[]{android.R.id.text1}, 0);

        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            public boolean setViewValue(View aView, Cursor aCursor, int aColumnIndex) {
//                if (aColumnIndex == 1) {
                    TextView textView = (TextView) aView;
                    boolean active = ( Integer.parseInt(aCursor.getString(aCursor.getColumnIndex(QueryNestedCategory.ACTIVE))) == 1);
                    String text = aCursor.getString(aColumnIndex);
                    if (!active) {
                        textView.setText( Html.fromHtml( "<i>"+text+ " [inactive]</i>", Html.FROM_HTML_MODE_COMPACT ) ) ;
                    } else {
                        textView.setText(text);
                    }
                    return true;
//                }
//                return false;
            }
        });


        // set adapter
        setListAdapter(adapter);

        registerForContextMenu(getListView());
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        setListShown(false);

        attachFloatingActionButtonToListView();

        // start loader
        getLoaderManager().initLoader(ID_LOADER_NESTEDCATEGORY, null, this);

        setFloatingActionButtonVisible(true);
        attachFloatingActionButtonToListView();

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_sort, menu);
        final MenuItem item = menu.findItem(R.id.menu_sort_name);
        item.setChecked(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_sort_name ||
            item.getItemId() == R.id.menu_sort_usage) {
            if (item.getItemId() == R.id.menu_sort_name )  {
                mSort = 0;
            } else {
                mSort = 1;
            }

            item.setChecked(true);
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
        menu.setHeaderTitle(cursor.getString(cursor.getColumnIndex(QueryNestedCategory.CATEGNAME)));

        // context menu from resource
        menu.add(Menu.NONE, ContextMenuIds.ADD_SUB.getId(), Menu.NONE, getString(R.string.add_subcategory));
        menu.add(Menu.NONE, ContextMenuIds.EDIT.getId(), Menu.NONE, getString(R.string.edit));
        menu.add(Menu.NONE, ContextMenuIds.DELETE.getId(), Menu.NONE, getString(R.string.delete));
        menu.add(Menu.NONE, ContextMenuIds.VIEW_TRANSACTIONS.getId(), Menu.NONE, getString(R.string.view_transactions));
        menu.add(Menu.NONE, ContextMenuIds.VIEW_TRANSACTIONS_SUB.getId(), Menu.NONE, getString(R.string.view_transactions_sub));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info ;
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
        if (menuId == null) return  false;
        switch (menuId) {
            case ADD_SUB:
                Category newCat = new Category();
                newCat.setParentId(category.getId());
                showDialogEditSubCategoryName(SQLTypeTransaction.INSERT,
                        newCat);
                break;
            case EDIT:
                if (category.getParentId() <= 0) {
                    showDialogEditCategoryName(SQLTypeTransaction.UPDATE,
                            category);
                } else {
                    showDialogEditSubCategoryName(SQLTypeTransaction.UPDATE,
                            category);
                }
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
        }
        return false;
    }


    // Data loader

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == ID_LOADER_NESTEDCATEGORY) {// update id selected
            // load data
//            String whereClause = "ACTIVE <> 0";
            String whereClause = "";
            String[] selectionArgs = null;
            if (!TextUtils.isEmpty(mCurFilter)) {
                whereClause += " AND " + QueryNestedCategory.CATEGNAME + " LIKE ?";
                selectionArgs = new String[]{mCurFilter + "%"};
            }
            QueryNestedCategory repo = new QueryNestedCategory(getActivity());
            Select query = new Select(repo.getAllColumns())
                    .where(whereClause, selectionArgs)
                    .orderBy(mSort == 1 ? SORT_BY_USAGE : SORT_BY_NAME);

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
                    getFloatingActionButton().show(true);
                }
            } else {
                setListShownNoAnimation(true);
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
            @SuppressLint("Range") long id = cursor.getLong(cursor.getColumnIndex(QueryNestedCategory.CATEGID));
            @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(QueryNestedCategory.CATEGNAME));

            sendResultToActivity(id, name);

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
        showDialogEditCategoryName(SQLTypeTransaction.INSERT, category);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

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
    private void showDialogEditCategoryName(final SQLTypeTransaction type, Category category) {
        // inflate view
        View viewDialog = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_new_edit_category, null);

        final EditText edtCategName = viewDialog.findViewById(R.id.editTextCategName);
        // set category description
        edtCategName.setText(category.getBasename());
        if (!TextUtils.isEmpty(category.getBasename())) {
            edtCategName.setSelection(category.getBasename().length());
        }

        int titleId = type.equals(SQLTypeTransaction.INSERT)
                ? R.string.add_category
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
                        String name = edtCategName.getText().toString();
                        CategoryService service = new CategoryService(getActivity());

                        switch (type) {
                            case INSERT:
                                long insertResult = service.createNew(name, Constants.NOT_SET);

                                if (insertResult <= 0) {
                                    Toast.makeText(getActivity(), R.string.db_insert_failed, Toast.LENGTH_SHORT).show();
                                }
                                break;
                            case UPDATE:
                                long updateResult = service.update(category.getId(), name, Constants.NOT_SET);
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
        for (NestedCategoryEntity category1 : categories) {
            // do not include category itself and all children form parent list
            if (category.getName() == null || !category1.getCategoryName().startsWith(category.getName())) {
                categoryIds.add(category1.getCategoryId());
                categoryNames.add(category1.getCategoryName() );
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
                        // get parent category id
                        long parentID = categories.get(spnCategory.getSelectedItemPosition()).getCategoryId();
                        CategoryService service = new CategoryService(getActivity());

                        switch (type) {
                            case INSERT:
                                long insertResult = service.createNew(name, parentID);

                                if (insertResult <= 0) {
                                    Toast.makeText(getActivity(), R.string.db_insert_failed, Toast.LENGTH_SHORT).show();
                                }
                                break;
                            case UPDATE:
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

}
