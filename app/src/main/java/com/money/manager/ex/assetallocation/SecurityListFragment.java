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

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ListView;

import com.money.manager.ex.R;
import com.money.manager.ex.adapter.MoneySimpleCursorAdapter;
import com.money.manager.ex.common.BaseListFragment;
import com.money.manager.ex.common.MmxCursorLoader;
import com.money.manager.ex.datalayer.AssetClassStockRepository;
import com.money.manager.ex.datalayer.Select;
import com.money.manager.ex.datalayer.StockFields;
import com.money.manager.ex.datalayer.StockRepository;
import com.money.manager.ex.domainmodel.AssetClassStock;
import com.money.manager.ex.domainmodel.Stock;

import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

/**
 * A placeholder fragment containing a simple view.
 */
public class SecurityListFragment
    extends BaseListFragment
    implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String INTENT_RESULT_STOCK_SYMBOL = "SecurityListFragment:StockSymbol";

    private static final int LOADER_SYMBOLS = 0;
    private static final String PARAM_ASSET_CLASS_ID = "assetClassId";

    public static SecurityListFragment create(Integer assetClassId) {
        SecurityListFragment instance = new SecurityListFragment();

        Bundle params = new Bundle();
        params.putInt(PARAM_ASSET_CLASS_ID, assetClassId);
        instance.setArguments(params);

        return instance;
    }

    public SecurityListFragment() {
    }

    public String action = Intent.ACTION_PICK;

    private String mCurFilter;
    private String selectedStockSymbol;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // set show search
        setSearchMenuVisible(true);
        // set default value
        setEmptyText(getActivity().getResources().getString(R.string.no_records_found_create));
        setHasOptionsMenu(true);

//        int layout = Intent.ACTION_PICK.equals(this.action)
//            ? android.R.layout.simple_list_item_multiple_choice
//            : android.R.layout.simple_list_item_1;
        int layout = android.R.layout.simple_list_item_1;

        // create adapter
        MoneySimpleCursorAdapter adapter = new MoneySimpleCursorAdapter(getActivity(),
            layout, null,
            new String[]{ StockFields.SYMBOL },
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
//        attachFloatingActionButtonToListView(true);
    }

    @Override
    public String getSubTitle() {
        return null;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_SYMBOLS:
                String whereClause;
                String selectionArgs[] = null;

                // ignore all the symbols already linked
                whereClause = StockFields.SYMBOL + " NOT IN (SELECT " + AssetClassStock.STOCKSYMBOL +
                    " FROM " + new AssetClassStockRepository(getActivity()).getSource() + ")";


                if (!TextUtils.isEmpty(mCurFilter)) {
                    whereClause += " AND " + StockFields.SYMBOL + " LIKE ?";
                    selectionArgs = new String[]{ mCurFilter + "%" };
                }

                StockRepository repo = new StockRepository(getActivity());
                Select query = new Select(new String[] { "STOCKID AS _id", StockFields.STOCKID, StockFields.SYMBOL })
                    .where(whereClause, selectionArgs)
                    .orderBy("upper(" + StockFields.SYMBOL + ")");

                return new MmxCursorLoader(getActivity(), repo.getUri(), query);
        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case LOADER_SYMBOLS:
                MoneySimpleCursorAdapter adapter = (MoneySimpleCursorAdapter) getListAdapter();
                adapter.setHighlightFilter(mCurFilter != null ? mCurFilter.replace("%", "") : "");
//                adapter.swapCursor(data);
                adapter.changeCursor(data);

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
//                adapter.swapCursor(null);
                adapter.changeCursor(null);
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
            Stock stock = Stock.from(c);
            selectedStockSymbol = stock.getSymbol();

            setResultAndFinish();
        }
    }

    @Override
    protected void setResult() {
        Intent result;
        switch (this.action) {
            case Intent.ACTION_PICK:
                result = new Intent();
                result.putExtra(INTENT_RESULT_STOCK_SYMBOL, selectedStockSymbol);
                if (TextUtils.isEmpty(selectedStockSymbol)) {
                    getActivity().setResult(Activity.RESULT_CANCELED, result);
                } else {
                    getActivity().setResult(Activity.RESULT_OK, result);
                }
                break;

            default:
                // otherwise return cancel
                getActivity().setResult(Activity.RESULT_CANCELED);
                break;
        }
    }

}
