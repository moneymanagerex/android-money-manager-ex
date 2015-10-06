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
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.money.manager.ex.R;
import com.money.manager.ex.common.BaseListFragment;
import com.money.manager.ex.common.MmexCursorLoader;
import com.money.manager.ex.datalayer.AssetClassRepository;
import com.money.manager.ex.domainmodel.AssetClass;
import com.money.manager.ex.servicelayer.AssetAllocationService;
import com.shamanland.fonticon.FontIconDrawable;

import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class AssetAllocationFragment
    extends BaseListFragment
    implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOADER_ASSET_CLASSES = 1;

    public AssetAllocationFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_asset_allocation, container, false);
//
//        return view;
//    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText(getActivity().getResources().getString(R.string.asset_classes_empty));

        // create and link the adapter
        AssetAllocationAdapter adapter = new AssetAllocationAdapter(getActivity(), null);
        setListAdapter(adapter);

        registerForContextMenu(getListView());

        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        setListShown(false);
        loadData();

        setFloatingActionButtonVisible(true);
        setFloatingActionButtonAttachListView(true);
    }

    @Override
    public String getSubTitle() {
//        return getString(R.string.asset_allocation);
        return null;
    }

    @Override
    public void onFloatingActionButtonClickListener() {
        startEditAssetClassActivityForInsert();
    }

    // data loader

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_ASSET_CLASSES:
                // create cursor loader
                AssetClassRepository repo = new AssetClassRepository(getActivity());

                return new MmexCursorLoader(getActivity(), repo.getUri(),
                    repo.getAllColumns(),
                    null, // where
                    null, // args
                    AssetClass.PARENTID // sort
                );
                //break;
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case LOADER_ASSET_CLASSES:
                AssetAllocationAdapter adapter = (AssetAllocationAdapter) getListAdapter();
//                adapter.swapCursor(data);
                // create asset allocation matrix cursor
                Cursor matrixCursor = createMatrixCursor(data);
                adapter.swapCursor(matrixCursor);

                if (isResumed()) {
                    setListShown(true);
                    if (data != null && data.getCount() <= 0 && getFloatingActionButton() != null)
                        getFloatingActionButton().show(true);
                } else {
                    setListShownNoAnimation(true);
                }
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

        getActivity().openContextMenu(v);
    }

    // private

    private void confirmDelete(final int id) {
        AlertDialogWrapper.Builder alertDialog = new AlertDialogWrapper.Builder(getContext())
            .setTitle(R.string.delete_account)
            .setIcon(FontIconDrawable.inflate(getContext(), R.xml.ic_question))
            .setMessage(R.string.confirmDelete);

        alertDialog.setPositiveButton(android.R.string.ok,
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    AssetClassRepository repo = new AssetClassRepository(getActivity());
                    if (!repo.delete(id)) {
                        Toast.makeText(getActivity(), R.string.db_delete_failed, Toast.LENGTH_SHORT).show();
                    }
                    // restart loader
                    getLoaderManager().restartLoader(LOADER_ASSET_CLASSES, null, AssetAllocationFragment.this);
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
        startActivity(intent);
    }

    private void startEditAssetClassActivityForEdit(int assetClassId) {
        Intent intent = new Intent(getActivity(), AssetClassEditActivity.class);
        intent.setAction(Intent.ACTION_EDIT);
        intent.putExtra(Intent.EXTRA_UID, assetClassId);
        startActivity(intent);
    }

    private void loadData() {
        getLoaderManager().initLoader(LOADER_ASSET_CLASSES, null, this);
    }

    private MatrixCursor createMatrixCursor(Cursor data) {
        AssetAllocationService service = new AssetAllocationService(getActivity());
        AssetClass allocation = service.loadAssetAllocation(data);

        String[] columns = new String[] {
            "_id", AssetClass.NAME, AssetClass.ALLOCATION, "CurrentValue"
        };
        MatrixCursor cursor = new MatrixCursor(columns);

        for (AssetClass item : allocation.getChildren()) {
            Object[] values = new Object[] {
                item.getId(), item.getName(), item.getAllocation(), item.getCurrentValue()
            };
            cursor.addRow(values);
        }

        return cursor;
    }
}
