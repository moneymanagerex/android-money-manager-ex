/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
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
 */
package com.money.manager.ex.investment;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import com.money.manager.ex.R;
import com.money.manager.ex.businessobjects.StockRepository;
import com.money.manager.ex.dropbox.DropboxHelper;
import com.money.manager.ex.fragment.AllDataFragment;
import com.money.manager.ex.fragment.BaseFragmentActivity;
import com.money.manager.ex.fragment.BaseListFragment;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

public class WatchlistItemsFragment
        extends BaseListFragment
        implements LoaderCallbacks<Cursor> {

    // ID Loader
    public static final int ID_LOADER_ALL_DATA = 1;

    /**
     * Create a new instance of the fragment with accountId params
     *
     * @param accountId Id of account to display. If generic shown set -1
     * @return new instance AllDataFragment
     */
    public static WatchlistItemsFragment newInstance(
            int accountId, IWatchlistItemsFragmentEventHandler eventHandler) {
        WatchlistItemsFragment fragment = new WatchlistItemsFragment();
        fragment.mAccountId = accountId;
        fragment.mEventHandler = eventHandler;
        return fragment;
    }

    private static final String LOGCAT = WatchlistItemsFragment.class.getSimpleName();

    // non-static

    private IWatchlistItemsFragmentEventHandler mEventHandler;
    private LoaderManager.LoaderCallbacks<Cursor> mParentLoaderCallbacks;
    private boolean mAutoStarLoader = true;
//    private boolean mShownHeader = false;
    private int mGroupId = 0;
    private int mAccountId = -1;
    private View mListHeader = null;
    private Context mContext;
    private StockRepository mStockRepository;
    private StockHistoryRepository mStockHistoryRepository;
    private Bundle mLoaderArgs;

    /**
     * @param mGroupId the mGroupId to set
     */
    public void setContextMenuGroupId(int mGroupId) {
        this.mGroupId = mGroupId;
    }

    /**
     * @return the mParentLoaderCallbacks
     */
    public LoaderManager.LoaderCallbacks<Cursor> getParentLoaderCallbacks() {
        return mParentLoaderCallbacks;
    }

    /**
     *
     */
    public void setSearResultFragmentLoaderCallbacks(LoaderCallbacks<Cursor> searResultFragmentLoaderCallbacks) {
        this.mParentLoaderCallbacks = searResultFragmentLoaderCallbacks;
    }

    /**
     * @return the mAutoStarLoader
     */
    public boolean isAutoStarLoader() {
        return mAutoStarLoader;
    }

    /**
     * @param mAutoStarLoader the mAutoStarLoader to set
     */
    public void setAutoStarLoader(boolean mAutoStarLoader) {
        this.mAutoStarLoader = mAutoStarLoader;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mContext = getActivity();
        mLoaderArgs = getArguments();

        // set fragment
        setEmptyText(getString(R.string.no_stock_data));
        setListShown(false);

        mStockRepository =  new StockRepository(mContext);

        // create adapter
        StocksCursorAdapter adapter = new StocksCursorAdapter(mContext, null);
//        adapter.setAccountId(mAccountId);

        // handle list item click.
        getListView().setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (getListAdapter() != null && getListAdapter() instanceof StocksCursorAdapter) {
                    getActivity().openContextMenu(view);
                }
            }
        });
        // if header is not null add to list view
        if (getListAdapter() == null) {
            if (mListHeader != null) getListView().addHeaderView(mListHeader);
        }
        // set adapter
        setListAdapter(adapter);

        // register context menu
        registerForContextMenu(getListView());

        // set animation progress
        setListShown(false);

        // floating action button
//        setFloatingActionButtonVisible(true);
//        setFloatingActionButtonAttachListView(true);

        // start loader
        if (isAutoStarLoader()) {
            reloadData();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        setHasOptionsMenu(true);
    }

    /**
     * Context menu click handler. Update individual price.
     * @param item
     * @return
     */
    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        Cursor cursor = ((StocksCursorAdapter) getListAdapter()).getCursor();
        boolean cursorMoved = cursor.moveToPosition(info.position - 1);

        ContentValues contents = StockRepository.getContentValues();
        DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, contents, StockRepository.SYMBOL);

        boolean result = false;
        int itemId = item.getItemId();
//        String itemTitle = item.getTitle();

        switch (itemId) {
            case 0:
                // Update price
                String symbol = contents.getAsString(StockRepository.SYMBOL);
                mEventHandler.onPriceUpdateRequested(symbol);
                result = true;
                break;
        }

        return result;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

        Cursor cursor = ((StocksCursorAdapter) getListAdapter()).getCursor();

//        int columns = cursor.getColumnCount();
//        int rows = cursor.getCount();
//        DatabaseUtils.dumpCursor(cursor);

        boolean cursorMoved = cursor.moveToPosition(info.position - 1);

        menu.setHeaderTitle(cursor.getString(cursor.getColumnIndex(StockRepository.SYMBOL)));

        String[] menuItems = getResources().getStringArray(R.array.context_menu_watchlist);
        for (int i = 0; i < menuItems.length; i++) {
            menu.add(Menu.NONE, i, i, menuItems[i]);
        }
    }

    /**
     * Add options to the action bar of the host activity.
     * This is not called in ActionBar Activity, i.e. Search.
     * @param menu
     * @param inflater
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        return super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_watchlist_item_list, container, false);
    }

    // This is just to test:
    // http://stackoverflow.com/questions/15207305/getting-the-error-java-lang-illegalstateexception-activity-has-been-destroyed
    @Override
    public void onDetach() {
        super.onDetach();
        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);

        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    // Loader callbacks

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (getParentLoaderCallbacks() != null) getParentLoaderCallbacks().onCreateLoader(id, args);
        // store the arguments for later. This is the WHERE filter.
        mLoaderArgs = args;

        //animation
        setListShown(false);

        switch (id) {
            case ID_LOADER_ALL_DATA:
                // compose selection and sort
                String selection = "", sort = "";
                if (args != null && args.containsKey(AllDataFragment.KEY_ARGUMENTS_WHERE)) {
                    ArrayList<String> whereClause = args.getStringArrayList(AllDataFragment.KEY_ARGUMENTS_WHERE);
                    if (whereClause != null) {
                        for (int i = 0; i < whereClause.size(); i++) {
                            selection += (!TextUtils.isEmpty(selection) ? " AND " : "") + whereClause.get(i);
                        }
                    }
                }
                // set sort
                if (args != null && args.containsKey(AllDataFragment.KEY_ARGUMENTS_SORT)) {
                    sort = args.getString(AllDataFragment.KEY_ARGUMENTS_SORT);
                }
                // create loader
                return new CursorLoader(mContext, mStockRepository.getUri(),
                        mStockRepository.getAllColumns(),
                        selection, null, sort);
        }
        return null;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        LoaderManager.LoaderCallbacks<Cursor> parent = getParentLoaderCallbacks();
        if (parent != null) parent.onLoaderReset(loader);

        ((CursorAdapter) getListAdapter()).swapCursor(null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        LoaderManager.LoaderCallbacks<Cursor> parent = getParentLoaderCallbacks();
        if (parent != null) parent.onLoadFinished(loader, data);

        switch (loader.getId()) {
            case ID_LOADER_ALL_DATA:
                StocksCursorAdapter adapter = (StocksCursorAdapter) getListAdapter();
                adapter.swapCursor(data);
                if (isResumed()) {
                    setListShown(true);
                } else {
                    setListShownNoAnimation(true);
                }
        }
    }

    // End loader handlers.

    @Override
    public void onStop() {
        super.onStop();
        try {
            BaseFragmentActivity activity = (BaseFragmentActivity) getActivity();
            if (activity != null) {
                ActionBar actionBar = activity.getSupportActionBar();
                if(actionBar != null) {
                    View customView = actionBar.getCustomView();
                    if (customView != null) {
                        actionBar.setCustomView(null);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(LOGCAT, e.getMessage());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        int itemId = item.getItemId();

//        if (itemId == R.id.menu_export_to_csv) {
//            exportDataToCSVFile();
//            return true;
//        }
//        if (itemId == R.id.menu_qif_export) {
//            // export visible transactions.
//            exportToQif();
//        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Start loader into fragment
     */
    public void reloadData() {
        getLoaderManager().restartLoader(ID_LOADER_ALL_DATA, mLoaderArgs, this);
//        getLoaderManager().restartLoader(ID_LOADER_ALL_DATA, getArguments(), this);
    }

    @Override
    public String getSubTitle() {
        return null;
    }

    public void setListHeader(View mHeaderList) {
        this.mListHeader = mHeaderList;
    }

    /**
     * Called from price updater.
     * @return
     */
//    @Override
//    public Context getContext() {
//        return mContext;
//    }

//    @Override
//    public void onPriceDownloaded(String symbol, BigDecimal price, Date date) {
//        // update the price in database.
//        mStockRepository.updateCurrentPrice(symbol, price);
//
//        // save price history record.
//        StockHistoryRepository historyRepo = getStockHistoryRepository();
//        historyRepo.addStockHistoryRecord(symbol, price, date);
//
//        // refresh the data.
//        reloadData();
//
//        // notify the user.
//        String message = getString(R.string.price_updated) + ": " + symbol;
//        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT)
//                .show();
//
//        // notify about db file change.
//        DropboxHelper.notifyDataChanged();
//    }

    public StockHistoryRepository getStockHistoryRepository() {
        if (mStockHistoryRepository == null) {
            mStockHistoryRepository = new StockHistoryRepository(mContext);
        }
        return mStockHistoryRepository;
    }
}
