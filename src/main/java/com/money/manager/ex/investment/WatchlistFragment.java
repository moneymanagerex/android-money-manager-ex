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
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.money.manager.ex.investment;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.money.manager.ex.account.AccountEditActivity;
import com.money.manager.ex.R;
import com.money.manager.ex.businessobjects.AccountService;
import com.money.manager.ex.businessobjects.StockHistoryRepository;
import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.database.StockRepository;
import com.money.manager.ex.database.TableAccountList;
import com.money.manager.ex.database.TableStock;
import com.money.manager.ex.dropbox.DropboxHelper;
import com.money.manager.ex.common.BaseFragmentActivity;
import com.shamanland.fonticon.FontIconDrawable;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import info.javaperformance.money.Money;

/**
 * The main fragment for the watchlist. Contains the list and everything else.
 * Not sure why it was done in two fragments. Probably because the list can not have additional items?
 */
public class WatchlistFragment
        extends Fragment
        implements IPriceUpdaterFeedback, IWatchlistItemsFragmentEventHandler {

    private static final String KEY_CONTENT = "WatchlistFragment:StockId";

    private WatchlistItemsFragment mDataFragment;
    private String mNameFragment;

    private Integer mAccountId = null;
    private String mAccountName;

    private TableAccountList mAccount;

    private ImageView imgAccountFav, imgGotoAccount;

    private Context mContext;
//    private String LOGCAT = this.getClass().getSimpleName();
    // price update counter. Used to know when all the prices are done.
    private int mUpdateCounter;
    private int mToUpdateTotal;

    /**
     * @param accountId ID Account to be display
     * @return instance of Wathchlist fragment with transactions for the given account.
     */
    public static WatchlistFragment newInstance(int accountId) {
        WatchlistFragment fragment = new WatchlistFragment();
        fragment.mAccountId = accountId;
        fragment.setNameFragment(WatchlistFragment.class.getSimpleName() + "_" + Integer.toString(accountId));

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_CONTENT)) {
            mAccountId = savedInstanceState.getInt(KEY_CONTENT);
        }

        mContext = getActivity();
        mUpdateCounter = 0;
    }

    /**
     * Called once when the menu is created.
     * @param menu
     * @param inflater
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        // add options menu for watchlist
        inflater.inflate(R.menu.menu_watchlist, menu);

        // call create option menu of fragment
        mDataFragment.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * Called every time the menu is displayed.
     * @param menu
     */
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_CONTENT)) {
            mAccountId = savedInstanceState.getInt(KEY_CONTENT);
        }

        if (container == null) return null;
        View view = inflater.inflate(R.layout.account_fragment, container, false);

        if (mAccount == null) {
            AccountService service = new AccountService(getActivity().getApplicationContext());
            mAccount = service.getTableAccountList(mAccountId);
        }

        ViewGroup header = (ViewGroup) inflater.inflate(R.layout.fragment_watchlist_header, null, false);

        // favorite icon
        imgAccountFav = (ImageView) header.findViewById(R.id.imageViewAccountFav);
        // set listener click on favorite icon for change image
        imgAccountFav.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // set status account
                mAccount.setFavoriteAcct(!(mAccount.isFavoriteAcct()));
                // populate content values for update
                ContentValues values = new ContentValues();
                values.put(TableAccountList.FAVORITEACCT, mAccount.getFavoriteAcct());
                // update
                if (mContext.getContentResolver().update(mAccount.getUri(), values, TableAccountList.ACCOUNTID + "=?",
                        new String[]{Integer.toString(mAccountId)}) != 1) {
                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.db_update_failed), Toast.LENGTH_LONG).show();
                } else {
                    setImageViewFavorite();
                }
            }
        });
        // goto account
        imgGotoAccount = (ImageView) header.findViewById(R.id.imageViewGotoAccount);
        imgGotoAccount.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, AccountEditActivity.class);
                intent.putExtra(AccountEditActivity.KEY_ACCOUNT_ID, mAccountId);
                intent.setAction(Intent.ACTION_EDIT);
                startActivity(intent);
            }
        });

        // manage fragment
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();

        mDataFragment = WatchlistItemsFragment.newInstance(this);
        // set arguments and settings of fragment
//        mDataFragment.setArguments(prepareArgsForChildFragment());
        mDataFragment.accountId = mAccountId;
        mDataFragment.setListHeader(header);
        mDataFragment.setAutoStarLoader(false);

        // add fragment
        transaction.replace(R.id.fragmentContent, mDataFragment, getNameFragment());
        transaction.commit();

        // refresh user interface
        if (mAccount != null) {
            mAccountName = mAccount.getAccountName();
            setImageViewFavorite();
        }
        // set has option menu
        setHasOptionsMenu(true);

        return view;
    }

    /**
     * Handle menu item click.
     * Update prices.
     * @param item Menu item selected
     * @return indicator whether the selection was handled
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.menu_update_prices:
                confirmPriceUpdate();
                break;
            case R.id.menu_export_prices:
                exportPrices();
                break;
            case R.id.menu_purge_history:
                purgePriceHistory();
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        // restart loader
        startLoaderData();
        // set subtitle account name
        BaseFragmentActivity activity = (BaseFragmentActivity) getActivity();
        if (activity != null) {
            activity.getSupportActionBar().setSubtitle(mAccountName);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mAccountId != null)
            outState.putInt(KEY_CONTENT, mAccountId);
    }

    /**
     * refresh UI, show favorite icon
     */
    private void setImageViewFavorite() {
        if (mAccount.isFavoriteAcct()) {
            imgAccountFav.setBackgroundResource(R.drawable.ic_star);
        } else {
            imgAccountFav.setBackgroundResource(R.drawable.ic_star_outline);
        }
    }

    /**
     * Start Loader to retrieve data
     */
    public void startLoaderData() {
        if (mDataFragment != null) {
            mDataFragment.reloadData();
        }
    }

    public String getNameFragment() {
        return mNameFragment;
    }

    public void setNameFragment(String mNameFragment) {
        this.mNameFragment = mNameFragment;
    }

    /**
     * Called from asynchronous task when a single price is downloaded.
     * @param symbol Stock symbol
     * @param price Stock price
     * @param date Date of the price
     */
    @Override
    public void onPriceDownloaded(String symbol, Money price, Date date) {
        // prices updated.

        if (StringUtils.isEmpty(symbol)) return;

        // update the current price of the stock.
        StockRepository repo = getStockRepository();
        repo.updateCurrentPrice(symbol, price);

        // save price history record.
        StockHistoryRepository historyRepo = mDataFragment.getStockHistoryRepository();
        historyRepo.addStockHistoryRecord(symbol, price, date);

        mUpdateCounter += 1;
        if (mUpdateCounter == mToUpdateTotal) {
            completePriceUpdate();
        }
    }

    private void completePriceUpdate() {
        // this call is made from async task so have to get back to the main thread.
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // refresh the data.
                mDataFragment.reloadData();

                // notify about db file change.
                DropboxHelper.notifyDataChanged();
            }
        });
    }

    private StockRepository mStockRepository;

    private StockRepository getStockRepository() {
        if (mStockRepository == null) {
            mStockRepository = new StockRepository(mContext);
        }
        return mStockRepository;
    }

    private String[] getAllShownSymbols() {
        int itemCount = mDataFragment.getListAdapter().getCount();
        String[] result = new String[itemCount];

        for(int i = 0; i < itemCount; i++) {
            Cursor cursor = (Cursor) mDataFragment.getListAdapter().getItem(i);
            String symbol = cursor.getString(cursor.getColumnIndex(TableStock.SYMBOL));

            result[i] = symbol;
        }

        return result;
    }

    private void exportPrices() {
        PriceCsvExport export = new PriceCsvExport(mContext);
        boolean result = false;

        try {
            result = export.exportPrices(mDataFragment.getListAdapter(), mAccountName);
        } catch (IOException ex) {
            ExceptionHandler handler = new ExceptionHandler(mContext, this);
            handler.handle(ex, "exporting stock prices");
        }

        // todo: handle result. (?)
    }

    /**
     * Price update requested from the securities list context menu.
     * @param symbol
     */
    @Override
    public void onPriceUpdateRequested(String symbol) {
        // reset counter & max.
        mToUpdateTotal = 1;
        mUpdateCounter = 0;

        // http://stackoverflow.com/questions/1005073/initialization-of-an-arraylist-in-one-line
        //List<String> symbols = Collections.singletonList(symbol);
        List<String> symbols = new ArrayList<>();
        symbols.add(symbol);

        ISecurityPriceUpdater updater = SecurityPriceUpdaterFactory.getUpdaterInstance(mContext, this);
        updater.updatePrices(symbols);
    }

    private void purgePriceHistory() {
        new AlertDialogWrapper.Builder(getContext())
                .setTitle(R.string.purge_history)
                .setIcon(FontIconDrawable.inflate(getContext(), R.xml.ic_question))
                .setMessage(R.string.purge_history_confirmation)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        StockHistoryRepository history = new StockHistoryRepository(mContext);
                        int deleted = history.deletePriceHistory();

                        if (deleted > 0) {
                            DropboxHelper.notifyDataChanged();
                            Toast.makeText(mContext, mContext.getString(R.string.purge_history_complete), Toast.LENGTH_SHORT)
                                    .show();
                        } else {
                            Toast.makeText(mContext, mContext.getString(R.string.purge_history_failed), Toast.LENGTH_SHORT)
                                    .show();
                        }

                        dialog.dismiss();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create()
                .show();
    }

    private void confirmPriceUpdate() {
        new AlertDialogWrapper.Builder(getContext())
                .setTitle(R.string.download)
                .setIcon(FontIconDrawable.inflate(getContext(), R.xml.ic_question))
                .setMessage(R.string.confirm_price_download)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // get the list of symbols
                        String[] symbols = getAllShownSymbols();
                        mToUpdateTotal = symbols.length;
                        mUpdateCounter = 0;

                        // update security prices
                        ISecurityPriceUpdater updater = SecurityPriceUpdaterFactory
                                .getUpdaterInstance(getContext(), WatchlistFragment.this);
                        updater.updatePrices(Arrays.asList(symbols));

                        dialog.dismiss();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create()
                .show();
    }

}
