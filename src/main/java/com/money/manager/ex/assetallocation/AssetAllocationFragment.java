/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
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
package com.money.manager.ex.assetallocation;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.MaterialDialog;
import com.money.manager.ex.R;
import com.money.manager.ex.common.BaseListFragment;
import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.domainmodel.AssetClass;
import com.money.manager.ex.domainmodel.Stock;
import com.money.manager.ex.servicelayer.AssetAllocationService;
import com.shamanland.fonticon.FontIconDrawable;

/**
 * A placeholder fragment containing a simple view.
 */
public class AssetAllocationFragment
    extends BaseListFragment
    implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOADER_ASSET_CLASSES = 1;
//    private static final String PARAM_CURRENCY_CODE = "currencyCode";

    /**
     * Creates a new fragment that display the asset class. Shows the list of child elements
     * and sum from the sent asset class.
     * @param assetClass Asset Class to show.
     * @return Fragment
     */
    public static AssetAllocationFragment create(AssetClass assetClass) {
        AssetAllocationFragment fragment = new AssetAllocationFragment();

//        Bundle arguments = new Bundle();
//        arguments.putString(PARAM_CURRENCY_CODE, currencyCode);
//        fragment.setArguments(arguments);

        fragment.assetClass = assetClass;

        return fragment;
    }

    public AssetAllocationFragment() {
    }

    public AssetClass assetClass;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_asset_allocation, container, false);
        View view = super.onCreateView(inflater, container, savedInstanceState);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        renderHeader();

        setEmptyText(getActivity().getResources().getString(R.string.asset_classes_empty));

        // create and link the adapter
        AssetAllocationAdapter adapter = new AssetAllocationAdapter(getActivity(), null);
        setListAdapter(adapter);

        registerForContextMenu(getListView());

        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        setListShown(false);

        renderFooter(this.assetClass);

//        loadData();

        setFloatingActionButtonVisible(true);
        setFloatingActionButtonAttachListView(true);

        showData(this.assetClass);
    }

    @Override
    public void onResume() {
        super.onResume();

//        registerContentObserver();

//        reloadData();
    }

    @Override
    public void onPause() {
        super.onPause();

//        unregisterObserver();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        setListAdapter(null);
    }

    @Override
    public String getSubTitle() {
//        return getString(R.string.asset_allocation);
        return null;
    }

    @Override
    public void onFloatingActionButtonClickListener() {
        ExceptionHandler handler = new ExceptionHandler(getActivity());

        // check which item to create
        ItemType type = this.assetClass.getType();
        if (type == null) {
            handler.showMessage("Item type not set.");
            return;
        }

        switch (type) {
            case Allocation:
                if (assetClass.getChildren().size() > 0) {
                    startEditAssetClassActivityForInsert();
                }
                if (assetClass.getStockLinks().size() > 0) {
                    // todo: show stock picker.
                    handler.showMessage("Stock picker goes here");
                }
                // Offer a choice here - stocks or child allocation
                showTypeSelectorDialog();
                break;
            case Group:
                startEditAssetClassActivityForInsert();
                break;
        }
    }

    // data loader

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
//        switch (id) {
//            case LOADER_ASSET_CLASSES:
//                // create cursor loader
//                AssetClassRepository repo = new AssetClassRepository(getActivity());
//
//                return new MmexCursorLoader(getActivity(), repo.getUri(),
//                    repo.getAllColumns(),
//                    null, // where
//                    null, // args
//                    AssetClass.SORTORDER // sort
//                );
//                //break;
//        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case LOADER_ASSET_CLASSES:
//                adapter.swapCursor(data);
                // create asset allocation matrix cursor
                AssetAllocationService service = new AssetAllocationService(getActivity());
                AssetClass allocation = service.loadAssetAllocation(data);

                showData(allocation);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case LOADER_ASSET_CLASSES:
                AssetAllocationAdapter adapter = (AssetAllocationAdapter) getListAdapter();
                adapter.swapCursor(null);
                break;
        }
    }

    // Context menu

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        int id = (int) info.id;
        menu.setHeaderTitle(getString(R.string.asset_allocation));

        MenuInflater inflater = this.getActivity().getMenuInflater();
        inflater.inflate(R.menu.contextmenu_asset_allocation, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        boolean handled = false;
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int id = (int) info.id;
        AssetAllocationService service = new AssetAllocationService(getActivity());

        switch (item.getItemId()) {
            case R.id.menu_move_up:
                // move item up
                service.moveClassUp(id);
                break;

            case R.id.menu_move_down:
                service.moveClassDown(id);
                break;

            case R.id.menu_edit:
                startEditAssetClassActivityForEdit(id);
                handled = true;
                break;

            case R.id.menu_delete:
                confirmDelete(id);
                handled = true;
                break;
        }
        return handled;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        MatrixCursor cursor = (MatrixCursor) l.getItemAtPosition(position);
        int selectedId = cursor.getInt(cursor.getColumnIndex(MatrixCursorColumns.ID));
        String typeName = cursor.getString(cursor.getColumnIndex(MatrixCursorColumns.TYPE));
        ItemType type = ItemType.valueOf(typeName);

        switch (type) {
            case Allocation:
                raiseAssetClassSelected(selectedId);
                break;
            case Stock:
                // ?
                break;
        }

//        getActivity().openContextMenu(v);
    }

    public void showData(AssetClass assetAllocation) {
        AssetAllocationAdapter adapter = (AssetAllocationAdapter) getListAdapter();
        Cursor matrixCursor = createMatrixCursor(assetAllocation);
        adapter.swapCursor(matrixCursor);

        if (isResumed()) {
            setListShown(true);
            if (matrixCursor != null && matrixCursor.getCount() <= 0 && getFloatingActionButton() != null)
                getFloatingActionButton().show(true);
        } else {
            setListShownNoAnimation(true);
        }

    }

    // private

    private void confirmDelete(final int id) {
        AlertDialogWrapper.Builder alertDialog = new AlertDialogWrapper.Builder(getContext())
            .setTitle(R.string.delete)
            .setIcon(FontIconDrawable.inflate(getContext(), R.xml.ic_question))
            .setMessage(R.string.confirmDelete);

        alertDialog.setPositiveButton(android.R.string.ok,
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    AssetAllocationService service = new AssetAllocationService(getActivity());
                    if (!service.deleteAllocation(id)) {
                        Toast.makeText(getActivity(), R.string.db_delete_failed, Toast.LENGTH_SHORT).show();
                    }
                    // reload data
                    raiseItemDeleted(id);
//                    getLoaderManager().restartLoader(LOADER_ASSET_CLASSES, null, AssetAllocationFragment.this);
                }
            });

        alertDialog.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // close dialog
                dialog.cancel();
            }
        });
        // show dialog
        alertDialog.create().show();
    }

    private void startEditAssetClassActivityForInsert() {
        Intent intent = new Intent(getActivity(), AssetClassEditActivity.class);
        intent.setAction(Intent.ACTION_INSERT);
        intent.putExtra(AssetClassEditActivity.KEY_PARENT_ID, this.assetClass.getId());
        startActivity(intent);
    }

    private void startEditAssetClassActivityForEdit(int assetClassId) {
        Intent intent = new Intent(getActivity(), AssetClassEditActivity.class);
        intent.setAction(Intent.ACTION_EDIT);
        intent.putExtra(Intent.EXTRA_UID, assetClassId);
        startActivity(intent);
    }

//    private void loadData() {
//        getLoaderManager().initLoader(LOADER_ASSET_CLASSES, null, this);
//    }

    private MatrixCursor createMatrixCursor(AssetClass allocation) {

        String[] columns = new String[] {
            MatrixCursorColumns.ID, MatrixCursorColumns.NAME,
            MatrixCursorColumns.ALLOCATION, MatrixCursorColumns.VALUE,
            MatrixCursorColumns.CURRENT_ALLOCATION, MatrixCursorColumns.CURRENT_VALUE,
            MatrixCursorColumns.DIFFERENCE,
            MatrixCursorColumns.TYPE
        };

        MatrixCursor cursor = new MatrixCursor(columns);

        // Now decide what data to show.
//        ItemType type = allocation.getType();
        if (allocation.getChildren().size() > 0) {
            // group
//            allocation.setType(ItemType.Group);
            fillChildren(cursor, allocation);
        } else if (allocation.getStocks().size() > 0) {
            // allocation, with stocks
//            allocation.setType(ItemType.Allocation);
            fillStocks(cursor, allocation);
        } else {
            // either empty allocation or a stock.

        }

        return cursor;
    }

//    private void reloadData() {
//        getLoaderManager().restartLoader(LOADER_ASSET_CLASSES, null, this);
//    }

    private void renderHeader() {
        ListView listView = getListView();
        if (listView.getHeaderViewsCount() > 0) return;

        View view = View.inflate(getActivity(), R.layout.item_asset_allocation, null);
        MatrixCursorColumns values = new MatrixCursorColumns();

        values.name = getString(R.string.name);
        values.allocation = getString(R.string.allocation);
        values.value = getString(R.string.value);
        values.currentAllocation = getString(R.string.current);
        values.currentValue = getString(R.string.current);
        values.difference = getString(R.string.difference);

        UIHelpers.populateAssetClassRow(view, values);

        listView.addHeaderView(view, null, false);
//        listView.addHeaderView(view);
    }

    private void renderFooter(AssetClass assetClass) {
        View view = View.inflate(getActivity(), R.layout.item_asset_allocation, null);
        MatrixCursorColumns values = new MatrixCursorColumns();

//        name.setTypeface(null, Typeface.BOLD_ITALIC);
//        allocation.setTypeface(null, Typeface.BOLD_ITALIC);

        values.name = getString(R.string.total);
        values.allocation = assetClass.getAllocation().toString();
        values.value = assetClass.getValue().toString();
        values.currentAllocation = assetClass.getCurrentAllocation().toString();
        values.currentValue = assetClass.getCurrentValue().toString();
        values.difference = assetClass.getDifference().toString();

        UIHelpers.populateAssetClassRow(view, values);

        ListView listView = getListView();
//        listView.addFooterView(view);
        listView.addFooterView(view, null, false);
    }

    private void raiseAssetClassSelected(int id) {
        // show a fragment for selected asset class
        DetailFragmentCallbacks parent = (DetailFragmentCallbacks) getActivity();
        if (parent != null) {
            parent.assetClassSelected(id);
        }
    }

    private void fillChildren(MatrixCursor cursor, AssetClass allocation) {
        int precision = 2;

        for (AssetClass item : allocation.getChildren()) {
            Object[] values = new Object[] {
                item.getId(), item.getName(),
                item.getAllocation(), item.getValue().truncate(precision),
                item.getCurrentAllocation().truncate(precision),
                item.getCurrentValue().truncate(precision),
                item.getDifference().truncate(precision),
                ItemType.Allocation.toString()
            };
            cursor.addRow(values);
        }
    }

    private void fillStocks(MatrixCursor cursor, AssetClass allocation) {
        int precision = 2;

        for (Stock item : allocation.getStocks()) {
            // Money currentAllocation = item.getValue().multiply(100).divide()

            Object[] values = new Object[] {
                item.getId(), item.getSymbol(),
                null, null,
                null, item.getValue().truncate(precision),
                null,
                ItemType.Stock.toString()
            };
            cursor.addRow(values);
        }
    }

    private void showTypeSelectorDialog() {
        new MaterialDialog.Builder(getActivity())
            .title(R.string.choose_type)
            .items(R.array.new_asset_class_type)
            .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                @Override
                public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                    /**
                     * If you use alwaysCallSingleChoiceCallback(), which is discussed below,
                     * returning false here won't allow the newly selected radio button to actually be selected.
                     **/
//                        showNameEntryDialog();

                    switch (which) {
                        case 0:
                            // Asset Allocation
                            startEditAssetClassActivityForInsert();
                            break;
                        case 1:
                            // Stock
                            // todo: pick stocks
                            break;
                    }

                    return true;
                }
            })
            .positiveText(android.R.string.ok)
//                .negativeText(android.R.string.cancel)
            .neutralText(android.R.string.cancel)
            .show();
    }

    private void raiseItemDeleted(int id) {
        DetailFragmentCallbacks parent = (DetailFragmentCallbacks) getActivity();
        if (parent != null) {
            parent.assetClassDeleted(id);
        }
    }
}
