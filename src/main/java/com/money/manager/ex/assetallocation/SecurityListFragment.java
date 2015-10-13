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

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.money.manager.ex.R;
import com.money.manager.ex.adapter.MoneySimpleCursorAdapter;
import com.money.manager.ex.common.BaseListFragment;
import com.money.manager.ex.common.MmexCursorLoader;
import com.money.manager.ex.datalayer.StockRepository;
import com.money.manager.ex.domainmodel.Stock;

/**
 * A placeholder fragment containing a simple view.
 */
public class SecurityListFragment
    extends BaseListFragment
    implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOADER_SYMBOLS = 0;

    public SecurityListFragment() {
    }

    public String action = Intent.ACTION_PICK;

    private String mCurFilter;
    private Integer selectedStockId;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // set show search
        setShowMenuItemSearch(true);
        // set default value
        setEmptyText(getActivity().getResources().getString(R.string.account_empty_list));
        setHasOptionsMenu(true);

//        int layout = Intent.ACTION_PICK.equals(this.action)
//            ? android.R.layout.simple_list_item_multiple_choice
//            : android.R.layout.simple_list_item_1;
        int layout = android.R.layout.simple_list_item_1;

        // create adapter
        MoneySimpleCursorAdapter adapter = new MoneySimpleCursorAdapter(getActivity(),
            layout, null,
            new String[]{ Stock.SYMBOL },
            new int[]{ android.R.id.text1 }, 0);
        setListAdapter(adapter);

        registerForContextMenu(getListView());

        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        setListShown(false);
        // start loader
        getLoaderManager().initLoader(LOADER_SYMBOLS, null, this);

        // set icon searched
//        setMenuItemSearchIconified(!Intent.ACTION_PICK.equals(this.action));
//        setFloatingActionButtonVisible(true);
//        setFloatingActionButtonAttachListView(true);
    }

    @Override
    public String getSubTitle() {
        return null;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_SYMBOLS:
                String whereClause = null;
                String selectionArgs[] = null;
                if (!TextUtils.isEmpty(mCurFilter)) {
                    whereClause = Stock.SYMBOL + " LIKE ?";
                    selectionArgs = new String[]{ mCurFilter + "%" };
                }

                StockRepository repo = new StockRepository(getActivity());

                return new MmexCursorLoader(getActivity(), repo.getUri(),
//                    repo.getAllColumns(),
                    new String[] { "STOCKID AS _id", Stock.STOCKID, Stock.SYMBOL },
                    whereClause, selectionArgs,
                    "upper(" + Stock.SYMBOL + ")");
        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case LOADER_SYMBOLS:
                MoneySimpleCursorAdapter adapter = (MoneySimpleCursorAdapter) getListAdapter();
                adapter.setHighlightFilter(mCurFilter != null ? mCurFilter.replace("%", "") : "");
                adapter.swapCursor(data);

                if (isResumed()) {
                    setListShown(true);
                    if (data != null && data.getCount() <= 0 && getFloatingActionButton() != null) {
                        getFloatingActionButton().show(true);
                    }
                } else {
                    setListShownNoAnimation(true);
                }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case LOADER_SYMBOLS:
                MoneySimpleCursorAdapter adapter = (MoneySimpleCursorAdapter) getListAdapter();
                adapter.swapCursor(null);
        }
    }

    // Other

    @Override
    public boolean onQueryTextChange(String newText) {
        mCurFilter = !TextUtils.isEmpty(newText) ? newText : null;
        getLoaderManager().restartLoader(LOADER_SYMBOLS, null, this);
        return true;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (this.action.equals(Intent.ACTION_PICK)) {
            // select the current item and return.
            Cursor c = (Cursor) l.getItemAtPosition(position);
            Stock stock = Stock.fromCursor(c);
            selectedStockId = stock.getId();

            setResultAndFinish();
        }
    }

    @Override
    protected void setResult() {
        Intent result;
        if (Intent.ACTION_PICK.equals(this.action)) {
            result = new Intent();
            result.putExtra(AssetClassEditActivity.INTENT_RESULT_STOCK_ID, selectedStockId);
            getActivity().setResult(Activity.RESULT_OK, result);
        }

        // otherwise return cancel
        getActivity().setResult(Activity.RESULT_CANCELED);
    }

}
