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
package com.money.manager.ex.assetallocation;

import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.assetallocation.events.AssetAllocationReloadRequestedEvent;
import com.money.manager.ex.assetallocation.events.AssetClassSelectedEvent;
import com.money.manager.ex.common.BaseListFragment;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.FormatUtilities;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.datalayer.AssetClassStockRepository;
import com.money.manager.ex.domainmodel.AssetClass;
import com.money.manager.ex.domainmodel.Stock;
import com.money.manager.ex.servicelayer.AssetAllocationService;

import org.greenrobot.eventbus.EventBus;
import org.parceler.Parcels;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

/**
 * A list fragment that displays the Asset Class contents (stocks or child asset classes).
 */
public class AssetAllocationContentsFragment
    extends BaseListFragment {

    public static final int REQUEST_STOCK_ID = 1;
    public static final int REQUEST_EDIT_ALLOCATION = 2;

    public static final String PARAM_ASSET_CLASS_ID = "assetClassId";
    public static final String PARAM_DECIMAL_PLACES = "decimalPlaces";
    public static final String PARAM_ASSET_ALLOCATION = "assetAllocation";

    /**
     * Creates a new fragment instance. Sets the appropriate required attributes.
     * @param assetClassId Id of the Asset Class to show.
     * @return Fragment
     */
    public static AssetAllocationContentsFragment create(Integer assetClassId, int decimalPlaces, AssetClass assetAllocation) {
        AssetAllocationContentsFragment fragment = new AssetAllocationContentsFragment();

        Bundle arguments = new Bundle();
        if (assetClassId == null) {
            assetClassId = Constants.NOT_SET;
        }
        arguments.putInt(PARAM_ASSET_CLASS_ID, assetClassId);
        arguments.putInt(PARAM_DECIMAL_PLACES, decimalPlaces);
        arguments.putParcelable(PARAM_ASSET_ALLOCATION, Parcels.wrap(assetAllocation));

        fragment.setArguments(arguments);

        return fragment;
    }

    public AssetAllocationContentsFragment() {
    }

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

        setFloatingActionButtonVisible(true);
        attachFloatingActionButtonToListView();

        showData();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        setListAdapter(null);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == AppCompatActivity.RESULT_CANCELED) return;

        switch (requestCode) {
            case REQUEST_STOCK_ID:
                // get the stock id
                String symbol = data.getStringExtra(SecurityListFragment.INTENT_RESULT_STOCK_SYMBOL);
                assignStockToAssetClass(symbol);
                break;

            case REQUEST_EDIT_ALLOCATION:
                EventBus.getDefault().post(new AssetAllocationReloadRequestedEvent());
                break;
        }
    }

    @Override
    public String getSubTitle() {
        AssetClass assetClass = retrieveData();
        if (assetClass == null) return null;

        return assetClass.getName();
    }

    @Override
    public void onFloatingActionButtonClicked() {
        AssetClass assetClass = retrieveData();

        // check which item to create
        ItemType type = assetClass.getType();
        if (type == null) {
            new UIHelper(getActivity()).showToast("Item type not set.");
            return;
        }

        switch (type) {
            case Allocation:
                boolean handled = false;
                if (assetClass.getChildren().size() > 0) {
                    startEditAssetClassActivityForInsert();
                    handled = true;
                }
                if (assetClass.getStockLinks().size() > 0) {
                    pickStock();
                    handled = true;
                }
                // Offer a choice here - stocks or child allocation
                if (!handled) {
                    showTypeSelectorDialog();
                }
                break;

            case Group:
                startEditAssetClassActivityForInsert();
                break;
        }
    }

    // Context menu

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        MatrixCursorColumns item = getSelectedItemType(info);
        // This is the id of the item selected in the list.
        // int id = (int) info.id;

        menu.setHeaderTitle(getString(R.string.asset_allocation));

        MenuInflater inflater = this.getActivity().getMenuInflater();
        if (item.type.equals(ItemType.Stock)) {
            inflater.inflate(R.menu.contextmenu_asset_allocation_stock, menu);
        } else {
            inflater.inflate(R.menu.contextmenu_asset_allocation, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        boolean handled = false;

        // find out what is the type of the selected item.
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        MatrixCursorColumns selectedItem = getSelectedItemType(info);
        int id = selectedItem.id;

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
                confirmDelete(selectedItem);
                handled = true;
                break;

            default:
                super.onContextItemSelected(item);
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
                //raiseAssetClassSelected(selectedId);
                EventBus.getDefault().post(new AssetClassSelectedEvent(selectedId));
                break;
            case Stock:
                // ?
                break;
        }

//        getActivity().openContextMenu(v);
    }

    public void showData() {
        AssetClass assetAllocation = retrieveData();

        Cursor matrixCursor = createMatrixCursor(assetAllocation);

        AssetAllocationAdapter adapter = (AssetAllocationAdapter) getListAdapter();
//        adapter.swapCursor(matrixCursor);
        adapter.changeCursor(matrixCursor);

        // refresh footer/totals
        renderFooter(assetAllocation);

        if (isResumed()) {
            setListShown(true);
            if (matrixCursor != null && matrixCursor.getCount() <= 0 && getFloatingActionButton() != null)
                getFloatingActionButton().show(true);
        } else {
            setListShownNoAnimation(true);
        }

    }

    // private

    private void deleteAllocation(MatrixCursorColumns item) {
        AssetAllocationService service = new AssetAllocationService(getActivity());

        if (!service.deleteAllocation(item.id)) {
            Toast.makeText(getActivity(), R.string.db_delete_failed, Toast.LENGTH_SHORT).show();
        }
        // reload data. Handled automagically. (by observer)
    }

    private void deleteStockLink(MatrixCursorColumns item) {
        // remove stock link
        String symbol = item.name;

        AssetClassStockRepository repo = new AssetClassStockRepository(getActivity());
        repo.delete(symbol);
    }

    private void confirmDelete(final MatrixCursorColumns item) {
        UIHelper ui = new UIHelper(getContext());

        new MaterialDialog.Builder(getContext())
            .title(R.string.delete)
            .icon(ui.getIcon(FontAwesome.Icon.faw_question_circle_o))
            .content(R.string.confirmDelete)
                .positiveText(android.R.string.ok)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (item.type.equals(ItemType.Stock)) {
                            deleteStockLink(item);
                        } else {
                            deleteAllocation(item);
                        }
                    }
                })
                .negativeText(android.R.string.cancel)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.cancel();                    }
                })
                .build().show();
    }

    private void startEditAssetClassActivityForInsert() {
        Intent intent = new Intent(getActivity(), AssetClassEditActivity.class);
        intent.setAction(Intent.ACTION_INSERT);
        intent.putExtra(AssetClassEditActivity.KEY_PARENT_ID, this.getAssetClassId());
        startActivity(intent);

        // REQUEST_CREATE_ALLOCATION
        // Content observer will refresh the Asset Allocation automatically.
    }

    private void startEditAssetClassActivityForEdit(int assetClassId) {
        Intent intent = new Intent(getActivity(), AssetClassEditActivity.class);
        intent.setAction(Intent.ACTION_EDIT);
        intent.putExtra(Intent.EXTRA_UID, assetClassId);
        startActivityForResult(intent, REQUEST_EDIT_ALLOCATION);
    }

    private MatrixCursor createMatrixCursor(AssetClass allocation) {

        String[] columns = new String[] {
            MatrixCursorColumns.ID, MatrixCursorColumns.NAME,
            MatrixCursorColumns.ALLOCATION, MatrixCursorColumns.VALUE,
            MatrixCursorColumns.CURRENT_ALLOCATION, MatrixCursorColumns.CURRENT_VALUE,
            MatrixCursorColumns.DIFFERENCE, MatrixCursorColumns.DIFFERENCE_PERCENT,
            MatrixCursorColumns.TYPE
        };

        MatrixCursor cursor = new MatrixCursor(columns);

//        ItemType type = allocation.getType();
        if (allocation.getChildren().size() > 0) {
            // group
            addChildAssetsToCursor(cursor, allocation);
        } else if (allocation.getStocks().size() > 0) {
            // allocation, with stocks
            addStocksToCursor(cursor, allocation);
        } else {
            // either empty allocation or a stock.

        }

        return cursor;
    }

    private void renderHeader() {
        ListView listView = getListView();
        if (listView.getHeaderViewsCount() > 0) return;

        View view = View.inflate(getActivity(), R.layout.item_asset_allocation, null);
        AssetClassViewHolder holder = AssetClassViewHolder.initialize(view);
        MatrixCursorColumns values = new MatrixCursorColumns();

        values.name = getString(R.string.name);
        values.allocation = getString(R.string.allocation);
        values.value = getString(R.string.value);
        values.currentAllocation = getString(R.string.current);
        values.currentValue = getString(R.string.current);
        values.difference = getString(R.string.difference);

//        int decimalPlaces = getArguments().getInt(PARAM_DECIMAL_PLACES);

        UIHelpers.populateAssetClassRow(holder, values);

        // formatting
        Core core = new Core(getActivity());
        holder.container.setBackgroundColor(core.getColourFromAttribute(R.attr.color_list_header));
//        holder.container.setBackgroundResource(core.getColourFromAttribute(R.attr.color_list_header));

        listView.addHeaderView(view, null, false);
//        listView.addHeaderView(view);
    }

    private View mFooter;

    private void renderFooter(AssetClass assetClass) {
        if (assetClass == null) return;

        View view = View.inflate(getActivity(), R.layout.item_asset_allocation, null);
        AssetClassViewHolder holder = AssetClassViewHolder.initialize(view);
        MatrixCursorColumns values = new MatrixCursorColumns();
        int decimalPlaces = getArguments().getInt(PARAM_DECIMAL_PLACES);
        FormatUtilities format = new FormatUtilities(getActivity());

        values.name = getString(R.string.total);
        values.allocation = assetClass.getAllocation().toString();
        values.value = format.getValueFormattedInBaseCurrency(assetClass.getValue().truncate(decimalPlaces));
        values.currentAllocation = assetClass.getCurrentAllocation().truncate(2).toString();
        values.currentValue = format.getValueFormattedInBaseCurrency(assetClass.getCurrentValue().truncate(decimalPlaces));
        values.difference = format.getValueFormattedInBaseCurrency(assetClass.getDifference().truncate(decimalPlaces));

        UIHelpers.populateAssetClassRow(holder, values);

        // todo: formatting & colours
        Core core = new Core(getActivity());
        holder.container.setBackgroundColor(core.getColourFromAttribute(R.attr.color_list_header));
//        holder.container.setBackgroundResource(core.getColourFromAttribute(R.attr.color_list_header));
        holder.assetClassTextView.setTypeface(null, Typeface.BOLD);
//        holder.allocationTextView.setTypeface(null, Typeface.BOLD);
//        holder.valueTextView.setTypeface(null, Typeface.BOLD);

        ListView listView = getListView();

        // remove any footers
        if (mFooter != null) {
            listView.removeFooterView(mFooter);
        }

        mFooter = view;
//        listView.addFooterView(view);
        listView.addFooterView(mFooter, null, false);
    }

    private void addChildAssetsToCursor(MatrixCursor cursor, AssetClass allocation) {
        int precision = 2;

        for (AssetClass item : allocation.getChildren()) {
            Object[] values = new Object[] {
                item.getId(), item.getName(),
                item.getAllocation(), item.getValue().truncate(precision),
                item.getCurrentAllocation().truncate(precision),
                item.getCurrentValue().truncate(precision),
                item.getDifference().truncate(precision),
                item.getDiffAsPercentOfSet().truncate(precision),
                ItemType.Allocation.toString()
            };
            cursor.addRow(values);
        }
    }

    private void addStocksToCursor(MatrixCursor cursor, AssetClass allocation) {
        int precision = 2;

        for (Stock item : allocation.getStocks()) {
            Object[] values = new Object[] {
                item.getId(), item.getSymbol(),
                null, null,
                null, item.getValue().truncate(precision),
                null, null,
                ItemType.Stock.toString()
            };
            cursor.addRow(values);
        }
    }

    /**
     * Returns the asset class of the class displayed in this fragment.
     * @return Current asset class's id.
     */
    private int getAssetClassId() {
        return getArguments().getInt(PARAM_ASSET_CLASS_ID);
    }

    private MatrixCursorColumns getSelectedItemType(AdapterView.AdapterContextMenuInfo info) {
        Object fromAdapter = getListAdapter().getItem(info.position);
        MatrixCursor cursor = (MatrixCursor) fromAdapter;

        // todo: see how to solve this. Location has to be modified because of the header row.
        cursor.moveToPosition(info.position - 1);

        MatrixCursorColumns result = MatrixCursorColumns.fromCursor(getActivity(), cursor);

        return result;
    }

    private AssetClass retrieveData() {
        int id = getAssetClassId();

        AssetAllocationService service = new AssetAllocationService(getContext());
        AssetClass assetAllocation = Parcels.unwrap(getArguments().getParcelable(PARAM_ASSET_ALLOCATION));
        AssetClass assetClass = service.findChild(id, assetAllocation);

        return assetClass;
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
                            pickStock();
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

    private void pickStock() {
        Intent intent = new Intent(getActivity(), SecurityListActivity.class);
        intent.setAction(Intent.ACTION_PICK);
        intent.putExtra(SecurityListActivity.EXTRA_ASSET_CLASS_ID, this.getAssetClassId());
        // ref: http://stackoverflow.com/questions/6147884/onactivityresult-not-being-called-in-fragment
        startActivityForResult(intent, REQUEST_STOCK_ID);
        // continues in onActivityResult
    }

    private void assignStockToAssetClass(String stockSymbol) {
        AssetAllocationService service = new AssetAllocationService(getActivity());
        service.assignStockToAssetClass(stockSymbol, getAssetClassId());
    }
}
