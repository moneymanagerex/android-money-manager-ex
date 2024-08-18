package com.money.manager.ex.nestedcategory;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
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

import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.adapter.CategoryExpandableListAdapter;
import com.money.manager.ex.common.BaseExpandableListFragment;
import com.money.manager.ex.common.CategoryListActivity;
import com.money.manager.ex.common.MmxCursorLoader;
import com.money.manager.ex.core.ContextMenuIds;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.database.SQLTypeTransaction;
import com.money.manager.ex.datalayer.CategoryRepository;
import com.money.manager.ex.datalayer.Select;
import com.money.manager.ex.domainmodel.Category;
import com.money.manager.ex.search.SearchActivity;
import com.money.manager.ex.search.SearchParameters;
import com.money.manager.ex.servicelayer.CategoryService;
import com.money.manager.ex.settings.AppSettings;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

public class NestedCategoryListFragment
        extends BaseExpandableListFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    public String mAction = Intent.ACTION_EDIT;
    public Integer requestId;

    private static final int ID_LOADER_CATEGORYSUB = 0;

    private static final String KEY_ID_GROUP = "CategorySubCategory:idGroup";
    private static final String KEY_CUR_FILTER = "CategorySubCategory:curFilter";
    // table or query
    private QueryNestedCastegory mQuery;
    private int mLayout;
//    private int mIdGroupChecked = ExpandableListView.INVALID_POSITION;

    private List<Category> mCategories;
    private String mCurFilter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // create category adapter
        mQuery = new QueryNestedCastegory(getActivity());

        mCategories = new ArrayList<>();

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
//            mIdGroupChecked = savedInstanceState.getInt(KEY_ID_GROUP);
        }
        if (savedInstanceState.containsKey(KEY_CUR_FILTER)) {
            mCurFilter = savedInstanceState.getString(KEY_CUR_FILTER, "");
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;

//        int type = ExpandableListView.getPackedPositionType(info.packedPosition);
        int group = ExpandableListView.getPackedPositionGroup(info.packedPosition);

        menu.setHeaderTitle(mCategories.get(group).getName());

        // context menu from resource
        menu.add(Menu.NONE, ContextMenuIds.ADD_SUB.getId(), Menu.NONE, getString(R.string.add_subcategory));
        menu.add(Menu.NONE, ContextMenuIds.EDIT.getId(), Menu.NONE, getString(R.string.edit));
        menu.add(Menu.NONE, ContextMenuIds.DELETE.getId(), Menu.NONE, getString(R.string.delete));
        menu.add(Menu.NONE, ContextMenuIds.VIEW_TRANSACTIONS.getId(), Menu.NONE, getString(R.string.view_transactions));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) item.getMenuInfo();

//        int type = ExpandableListView.getPackedPositionType(info.packedPosition);
        int group = ExpandableListView.getPackedPositionGroup(info.packedPosition);

        Category category = mCategories.get(group);
        // category dovrebbe puntare alla riga che abbiamo scelto

        // manage select menu
        ContextMenuIds menuId = ContextMenuIds.get(item.getItemId());
        switch (menuId) {
            case ADD_SUB:
                // todo
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
                SearchParameters parameters = new SearchParameters();
// Todo sistremare ricerca
//                    parameters.nestedCategory = (new NestedCategoryEntity(category));

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
            outState.putString(KEY_CUR_FILTER, mCurFilter);
        }
    }

    // Data loader

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == ID_LOADER_CATEGORYSUB) {// update id selected
            if (getExpandableListAdapter() != null && getExpandableListAdapter().getGroupCount() > 0) {
                CategoryExpandableListAdapter adapter = (CategoryExpandableListAdapter) getExpandableListAdapter();
//                mIdGroupChecked = adapter.getIdGroupChecked();
            }
            // clear arraylist and hashmap
            mCategories.clear();

            // load data
            String whereClause = null;
            String[] selectionArgs = null;
            if (!TextUtils.isEmpty(mCurFilter)) {
                whereClause = QueryNestedCastegory.CATEGNAME + " LIKE ?";
                selectionArgs = new String[]{mCurFilter + "%", mCurFilter + "%"};
            }
            Select query = new Select(mQuery.getAllColumns())
                    .where(whereClause, selectionArgs)
                    .orderBy(QueryNestedCastegory.CATEGNAME);

            return new MmxCursorLoader(getActivity(), mQuery.getUri(), query);
        }
        return null;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (loader.getId() == ID_LOADER_CATEGORYSUB) {// clear the data storage collections.
            mCategories.clear();
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == ID_LOADER_CATEGORYSUB) {
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

                if (categId == ExpandableListView.INVALID_POSITION) return;
                for (int groupIndex = 0; groupIndex < mCategories.size(); groupIndex++) {
                    if (mCategories.get(groupIndex).getId() == categId) {
                        result = new Intent();
                        result.putExtra(CategoryListActivity.INTENT_RESULT_CATEGID, categId);
                        result.putExtra(CategoryListActivity.INTENT_RESULT_CATEGNAME,
                                mCategories.get(groupIndex).getName());
                        result.putExtra(CategoryListActivity.INTENT_RESULT_SUBCATEGID, Constants.NOT_SET);
                        result.putExtra(CategoryListActivity.INTENT_RESULT_SUBCATEGNAME, "");
                        break;
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

    @SuppressLint("Range")
    public CategoryExpandableListAdapter getAdapter(Cursor data) {
        if (data == null) return null;

        mCategories.clear();
        // create core and fixed string filter to highlight
        Core core = new Core(getActivity().getApplicationContext());
        String filter = mCurFilter != null ? mCurFilter.replace("%", "") : "";

        int key = -1;

        // reset cursor if getting back on the fragment.
        if (data.getPosition() > 0) {
            data.moveToPosition(Constants.NOT_SET);
        }

        while (data.moveToNext()) {
            if (key != data.getInt(data.getColumnIndex(QueryNestedCastegory.CATEGID))) {
                // update key
                key = data.getInt(data.getColumnIndex(QueryNestedCastegory.CATEGID));
                // create instance category
                NestedCategoryEntity category = new NestedCategoryEntity();
                category.loadFromCursor(data);
                category.setCategoryName(core.highlight(filter, category.getCategoryName()).toString());

                // add list
                mCategories.add(category.asCategory());
            }

        }

        boolean showSelector = mAction.equals(Intent.ACTION_PICK);
        CategoryExpandableListAdapter adapter = new CategoryExpandableListAdapter(getActivity(),
                mLayout, mCategories, null, showSelector, true);
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
    private void showDialogDeleteCategorySub(final Category category) {
        boolean canDelete = false;
        CategoryService service = new CategoryService(getActivity());
        ContentValues values = new ContentValues();

        values.put(Category.CATEGID, category.getId());
        canDelete = !service.isCategoryUsed(category.getId());

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
                        int rowsDelete = 0;
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
                                int insertResult = service.createNew(name, Constants.NOT_SET);

                                if (insertResult <= 0) {
                                    Toast.makeText(getActivity(), R.string.db_insert_failed, Toast.LENGTH_SHORT).show();
                                }
                                break;
                            case UPDATE:
                                int updateResult = service.update(category.getId(), name, Constants.NOT_SET);
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
        final List<NestedCategoryEntity> categories = mQuery.getNestedCategoryEntities(null);

        ArrayList<String> categoryNames = new ArrayList<>();
        ArrayList<Integer> categoryIds = new ArrayList<>();
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
                        int parentID = categories.get(spnCategory.getSelectedItemPosition()).getCategoryId();
                        CategoryService service = new CategoryService(getActivity());

                        switch (type) {
                            case INSERT:
                                int insertResult = service.createNew(name, parentID);

                                if (insertResult <= 0) {
                                    Toast.makeText(getActivity(), R.string.db_insert_failed, Toast.LENGTH_SHORT).show();
                                }
                                break;
                            case UPDATE:
                                int updateResult = service.update(category.getId(), name, parentID);
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

    private void addListClickHandlers() {
        // the list handlers available only when selecting a category.
        if (mAction.equals(Intent.ACTION_PICK)) {

            getExpandableListView().setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

                @Override
                public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                    if (getExpandableListAdapter() != null && getExpandableListAdapter() instanceof CategoryExpandableListAdapter) {
                        CategoryExpandableListAdapter adapter = (CategoryExpandableListAdapter) getExpandableListAdapter();

                        Category category = mCategories.get(groupPosition);

                        adapter.setIdGroupChecked(category.getId());
                        adapter.notifyDataSetChanged();

                        setResultAndFinish();
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
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.choose_type)
                .setSingleChoiceItems(R.array.category_type, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // todo: depending on the choice, show the edit dialog. 0-based
                        NestedCategoryEntity newCategory = new NestedCategoryEntity(-1, null, -1);
                        if (which == 0) {
                            showDialogEditCategoryName(SQLTypeTransaction.INSERT, newCategory.asCategory());
                        } else {
                            showDialogEditSubCategoryName(SQLTypeTransaction.INSERT, newCategory.asCategory());
                        }

                        dialog.dismiss();
                    }
                })
                .setPositiveButton(android.R.string.ok, null) // null listener to prevent dialog from automatically dismissing
                .setNeutralButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
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
