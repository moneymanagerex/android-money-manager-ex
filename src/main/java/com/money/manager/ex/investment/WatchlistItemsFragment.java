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
package com.money.manager.ex.investment;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.datalayer.StockHistoryRepository;
import com.money.manager.ex.common.AllDataListFragment;
import com.money.manager.ex.common.BaseFragmentActivity;
import com.money.manager.ex.common.BaseListFragment;
import com.money.manager.ex.common.MmexCursorLoader;
import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.datalayer.StockRepository;
import com.money.manager.ex.database.TableStock;
import com.money.manager.ex.domainmodel.Stock;

import java.lang.reflect.Field;
import java.util.ArrayList;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

/**
 * The list of securities.
 */
public class WatchlistItemsFragment
        extends BaseListFragment
        implements LoaderCallbacks<Cursor> {

    public static final int ID_LOADER_WATCHLIST = 1;
    public static final String KEY_ACCOUNT_ID = "WatchlistItemsFragment:AccountId";

    /**
     * Create a new instance of the fragment with accountId params
     *
     * @return new instance AllDataListFragment
     */
    public static WatchlistItemsFragment newInstance(IWatchlistItemsFragmentEventHandler eventHandler) {
        WatchlistItemsFragment fragment = new WatchlistItemsFragment();
        fragment.mEventHandler = eventHandler;
        return fragment;
    }

    // non-static

    public Integer accountId;

    private IWatchlistItemsFragmentEventHandler mEventHandler;
    private boolean mAutoStarLoader = true;
    private View mListHeader = null;
    private Context mContext;
    private StockRepository mStockRepository;
    private StockHistoryRepository mStockHistoryRepository;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        setHasOptionsMenu(true);

        this.accountId = getArguments().getInt(KEY_ACCOUNT_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        return super.onCreateView(inflater, container, savedInstanceState);
        View layout = inflater.inflate(R.layout.fragment_watchlist_item_list, container, false);

        // get data from saved instance state
        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_ACCOUNT_ID)) {
            this.accountId = savedInstanceState.getInt(KEY_ACCOUNT_ID);
        }

        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // get arguments
        mContext = getActivity();

        // set fragment
        setEmptyText(getString(R.string.no_stock_data));
        setListShown(false);

        mStockRepository =  new StockRepository(mContext);

        // create adapter
        StocksCursorAdapter adapter = new StocksCursorAdapter(mContext, null);

        // handle list item click.
        getListView().setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Ignore the header row.
                if (getListView().getHeaderViewsCount() > 0 && position == 0) return;

                if (getListAdapter() != null && getListAdapter() instanceof StocksCursorAdapter) {
                    getActivity().openContextMenu(view);
                }
            }
        });

        // if header is not null add to list view
        if (getListAdapter() == null) {
            if (mListHeader != null) {
                getListView().addHeaderView(mListHeader);
            } else {
                getListView().removeHeaderView(mListHeader);
            }
        }

        // set adapter
        setListAdapter(adapter);

        // register context menu
        registerForContextMenu(getListView());

        // set animation progress
        setListShown(false);

        // start loader
        if (isAutoStarLoader()) {
            reloadData();
        }

        // set floating button visible
        setFloatingActionButtonVisible(true);
        setFloatingActionButtonAttachListView(true);
    }

    // context menu

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

        // ignore the header row if the headers are shown.
        if (hasHeaderRow() && info.position == 0) return;

        Cursor cursor = ((StocksCursorAdapter) getListAdapter()).getCursor();

//        DatabaseUtils.dumpCursor(cursor);

        int cursorPosition = hasHeaderRow() ? info.position - 1 : info.position;
        cursor.moveToPosition(cursorPosition);

        menu.setHeaderTitle(cursor.getString(cursor.getColumnIndex(Stock.SYMBOL)));

        String[] menuItems = getResources().getStringArray(R.array.context_menu_watchlist);
        for (int i = 0; i < menuItems.length; i++) {
            menu.add(Menu.NONE, i, i, menuItems[i]);
        }
    }

    /**
     * Context menu click handler. Update individual price.
     * @param item selected context menu item.
     * @return indicator whether the action is handled or not.
     */
    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        Cursor cursor = ((StocksCursorAdapter) getListAdapter()).getCursor();
        int cursorPosition = hasHeaderRow() ? info.position - 1 : info.position;
        cursor.moveToPosition(cursorPosition);

        Stock stock = Stock.fromCursor(cursor);
//        ContentValues contents = new ContentValues();
        // get Symbol from cursor
//        DatabaseUtils.cursorStringToContentValuesIfPresent(cursor, contents, Stock.SYMBOL);
//        String symbol = contents.getAsString(Stock.SYMBOL);
        String symbol = stock.getSymbol();

        boolean result = false;
        int itemId = item.getItemId();

        switch (itemId) {
            case 0:
                // Update price
                mEventHandler.onPriceUpdateRequested(symbol);
                result = true;
                break;
            case 1:
                // Edit price
//                DatabaseUtils.cursorIntToContentValuesIfPresent(cursor, contents, Stock.HELDAT);
//                int accountId = contents.getAsInteger(Stock.HELDAT);
                int accountId = stock.getHeldAt();
//                DatabaseUtils.cursorDoubleToContentValuesIfPresent(cursor, contents, Stock.CURRENTPRICE);
//                Money currentPrice = MoneyFactory.fromString(contents.getAsString(Stock.CURRENTPRICE));
                Money currentPrice = stock.getCurrentPrice();

                EditPriceDialog dialog = new EditPriceDialog();
                dialog.setParameters(accountId, symbol, currentPrice);
                dialog.show(getChildFragmentManager(), "input-amount");
                break;
        }

        return result;
    }

    // menu

    /**
     * Add options to the action bar of the host activity.
     * This is not called in ActionBar Activity, i.e. Search.
     * @param menu main menu
     * @param inflater inflater for the menu
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

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
     * This is just to test:
     * http://stackoverflow.com/questions/15207305/getting-the-error-java-lang-illegalstateexception-activity-has-been-destroyed
     */
    @Override
    public void onDetach() {
        super.onDetach();
        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);

        } catch (NoSuchFieldException | IllegalAccessException e) {
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
        MmexCursorLoader result;

        //animation
        setListShown(false);

        switch (id) {
            case ID_LOADER_WATCHLIST:
                // compose selection and sort
                String selection = "";
                if (args != null && args.containsKey(AllDataListFragment.KEY_ARGUMENTS_WHERE)) {
                    ArrayList<String> whereClause = args.getStringArrayList(AllDataListFragment.KEY_ARGUMENTS_WHERE);
                    if (whereClause != null) {
                        for (int i = 0; i < whereClause.size(); i++) {
                            selection += (!TextUtils.isEmpty(selection) ? " AND " : "") + whereClause.get(i);
                        }
                    }
                }

                // set sort
                String sort = "";
                if (args != null && args.containsKey(AllDataListFragment.KEY_ARGUMENTS_SORT)) {
                    sort = args.getString(AllDataListFragment.KEY_ARGUMENTS_SORT);
                }

                result = new MmexCursorLoader(mContext,
                        mStockRepository.getUri(),
                        mStockRepository.getAllColumns(),
                        selection,
                        null,
                        sort);
                break;
            default:
                result = null;
        }
        return result;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // reset the cursor reference to reduce memory leaks
//        ((CursorAdapter) getListAdapter()).changeCursor(null);

        ((CursorAdapter) getListAdapter()).swapCursor(null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case ID_LOADER_WATCHLIST:
                // send the data to the view adapter.
                StocksCursorAdapter adapter = (StocksCursorAdapter) getListAdapter();
                adapter.swapCursor(data);
                if (isResumed()) {
                    setListShown(true);

                    if (getFloatingActionButton() != null) {
                        getFloatingActionButton().show(true);
                    }
                } else {
                    setListShownNoAnimation(true);
                }
        }
    }

    // End loader handlers.

    // Menu

    @Override
    public void onFloatingActionButtonClickListener() {
        openEditInvestmentActivity();
    }

    @Override
    public void onSaveInstanceState(Bundle saveInstanceState) {
        super.onSaveInstanceState(saveInstanceState);

        saveInstanceState.putInt(KEY_ACCOUNT_ID, this.accountId);
    }

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
            ExceptionHandler handler = new ExceptionHandler(mContext, this);
            handler.handle(e, "stopping watchlist items fragment");
        }
    }

    /**
     * Start loader into fragment
     */
    public void reloadData() {
        Bundle arguments = prepareArgsForChildFragment();
        // mLoaderArgs
        getLoaderManager().restartLoader(ID_LOADER_WATCHLIST, arguments, this);
    }

    @Override
    public String getSubTitle() {
        return null;
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

    public void setListHeader(View mHeaderList) {
        this.mListHeader = mHeaderList;
    }

    public StockHistoryRepository getStockHistoryRepository() {
        if (mStockHistoryRepository == null) {
            mStockHistoryRepository = new StockHistoryRepository(mContext);
        }
        return mStockHistoryRepository;
    }

    private boolean hasHeaderRow() {
        return getListView().getHeaderViewsCount() > 0;
    }

    private Bundle prepareArgsForChildFragment() {
        ArrayList<String> selection = new ArrayList<>();

        if (this.accountId != Constants.NOT_SET) {
            selection.add(Stock.HELDAT + "=" + Integer.toString(this.accountId));
        }

        Bundle args = new Bundle();
        args.putStringArrayList(AllDataListFragment.KEY_ARGUMENTS_WHERE, selection);
        args.putString(AllDataListFragment.KEY_ARGUMENTS_SORT, Stock.SYMBOL + " ASC");

        return args;
    }

    private void openEditInvestmentActivity() {
        Intent intent = new Intent(getActivity(), EditInvestmentTransactionActivity.class);
        intent.putExtra(EditInvestmentTransactionActivity.EXTRA_ACCOUNT_ID, this.accountId);
        intent.setAction(Intent.ACTION_INSERT);
        startActivity(intent);
    }
}
