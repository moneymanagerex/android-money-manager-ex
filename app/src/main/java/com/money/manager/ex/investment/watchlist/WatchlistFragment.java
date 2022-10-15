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
package com.money.manager.ex.investment.watchlist;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.money.manager.ex.Constants;
import com.money.manager.ex.account.AccountEditActivity;
import com.money.manager.ex.R;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.datalayer.AccountRepository;
import com.money.manager.ex.datalayer.StockFields;
import com.money.manager.ex.datalayer.StockHistoryRepository;
import com.money.manager.ex.log.ErrorRaisedEvent;
import com.money.manager.ex.datalayer.StockRepository;
import com.money.manager.ex.domainmodel.Account;
import com.money.manager.ex.investment.prices.ISecurityPriceUpdater;
import com.money.manager.ex.investment.PriceCsvExport;
import com.money.manager.ex.investment.QuoteProviders;
import com.money.manager.ex.investment.SecurityPriceUpdaterFactory;
import com.money.manager.ex.investment.events.AllPricesDownloadedEvent;
import com.money.manager.ex.investment.events.PriceDownloadedEvent;
import com.money.manager.ex.investment.events.PriceUpdateRequestEvent;
import com.money.manager.ex.servicelayer.AccountService;
import com.money.manager.ex.settings.InvestmentSettings;
import com.money.manager.ex.sync.SyncManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.parceler.Parcels;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import info.javaperformance.money.Money;
import timber.log.Timber;

/**
 * The main fragment for the watchlist. Contains the list and everything else.
 * Not sure why it was done in two fragments. Probably because the list can not have additional items?
 */
public class WatchlistFragment
    extends Fragment {

    private static final String KEY_ACCOUNT_ID = "WatchlistFragment:AccountId";
    private static final String KEY_ACCOUNT = "WatchlistFragment:Account";

    /**
     * @param accountId ID Account to be display
     * @return instance of Watchlist fragment with transactions for the given account.
     */
    public static WatchlistFragment newInstance(int accountId) {
        WatchlistFragment fragment = new WatchlistFragment();
        Bundle args = new Bundle();
        args.putInt(KEY_ACCOUNT_ID, accountId);
        fragment.setArguments(args);

        fragment.setFragmentName(WatchlistFragment.class.getSimpleName() + "_" + Integer.toString(accountId));

        return fragment;
    }

    private WatchlistItemsFragment mDataFragment;
    private String mFragmentName;
    private StockRepository mStockRepository;
    private Account mAccount;

    // price update counter. Used to know when all the prices are done downloading.
    private int mUpdateCounter;
    private int mToUpdateTotal;
    private WatchlistViewHolder viewHolder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadAccount();

        if ((savedInstanceState != null)) {
            mAccount = Parcels.unwrap(savedInstanceState.getParcelable(KEY_ACCOUNT));
        }

        mUpdateCounter = 0;

//        Answers.getInstance().logCustom(new CustomEvent(AnswersEvents.Watchlist.name()));
    }

    @Override
    public void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        EventBus.getDefault().unregister(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if ((savedInstanceState != null)) {
            mAccount = Parcels.unwrap(savedInstanceState.getParcelable(KEY_ACCOUNT));
        }

        if (container == null) return null;
        View view = inflater.inflate(R.layout.fragment_account_transactions, container, false);

        if (mAccount == null) {
            loadAccount();
        }

        this.viewHolder = new WatchlistViewHolder();

        initializeListHeader(inflater);

        // manage fragment
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();

        mDataFragment = WatchlistItemsFragment.newInstance();
        // set arguments and preferences of fragment
        Bundle arguments = new Bundle();
        arguments.putInt(WatchlistItemsFragment.KEY_ACCOUNT_ID, getAccountId());
        mDataFragment.setArguments(arguments);

        mDataFragment.setListHeader(this.viewHolder.mListHeader);
        mDataFragment.setAutoStarLoader(false);

        // add fragment
        transaction.replace(R.id.fragmentMain, mDataFragment, getFragmentName());
        transaction.commit();

        // refresh user interface
        if (mAccount != null) {
            setImageViewFavorite();
        }
        setHasOptionsMenu(true);

//        initializeSwipeToRefresh(view);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // hide the title
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        initializeAccountsSelector();
        selectCurrentAccount();

        // restart loader
        reloadData();
    }

    // Menu

    /**
     * Called once when the menu is being created.
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        // add options menu for watchlist
        inflater.inflate(R.menu.menu_watchlist, menu);

        // custom icons

        UIHelper uiHelper = new UIHelper(getActivity());
        IconicsDrawable icon;
        MenuItem menuItem;

        menuItem = menu.findItem(R.id.menu_update_prices);
        if (menuItem != null) {
            icon = uiHelper.getIcon(GoogleMaterial.Icon.gmd_file_download);
            menuItem.setIcon(icon);
        }

        menuItem = menu.findItem(R.id.menu_export_prices);
        if (menuItem != null) {
            icon = uiHelper.getIcon(GoogleMaterial.Icon.gmd_share);
            menuItem.setIcon(icon);
        }

        menuItem = menu.findItem(R.id.menu_purge_history);
        if (menuItem != null) {
            icon = uiHelper.getIcon(GoogleMaterial.Icon.gmd_content_cut);
            menuItem.setIcon(icon);
        }

        // call create option menu of fragment
        mDataFragment.onCreateOptionsMenu(menu, inflater);
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
            case R.id.menu_change_provider:
                changePriceProvider();
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable(KEY_ACCOUNT, Parcels.wrap(mAccount));
    }

    // Events

    @Subscribe
    public void onEvent(AllPricesDownloadedEvent event) {
        reloadData();
    }

    @Subscribe
    public void onEvent(PriceDownloadedEvent event) {
        onPriceDownloaded(event.symbol, event.price, event.date);
    }

    @Subscribe
    public void onEvent(PriceUpdateRequestEvent event) {
        onPriceUpdateRequested(event.symbol);
    }

    @Subscribe
    public void onEvent(ErrorRaisedEvent event) {
        new UIHelper(getActivity()).showToast(event.message);
    }

    /**
     * Start Loader to retrieve data
     */
    public void reloadData() {
        if (mDataFragment != null) {
            mDataFragment.reloadData();
        }
    }

    public String getFragmentName() {
        return mFragmentName;
    }

    public void setFragmentName(String fragmentName) {
        this.mFragmentName = fragmentName;
    }

    // Private

    /**
     * Called from asynchronous task when a first price is downloaded.
     * @param symbol Stock symbol
     * @param price Stock price
     * @param date Date of the price
     */
    private void onPriceDownloaded(String symbol, Money price, Date date) {
        // prices updated.

        if (TextUtils.isEmpty(symbol)) return;

        // update the current price of the stock.
        StockRepository repo = getStockRepository();
        repo.updateCurrentPrice(symbol, price);

        // update price history record.
        StockHistoryRepository historyRepo = mDataFragment.getStockHistoryRepository();
        historyRepo.addStockHistoryRecord(symbol, price, date);

        mUpdateCounter += 1;
        if (mUpdateCounter == mToUpdateTotal) {
            completePriceUpdate();
        }
    }

    /**
     * Price update requested from the securities list context menu.
     * @param symbol Stock symbol for which to fetch the price.
     */
    private void onPriceUpdateRequested(String symbol) {
        // reset counter & max.
        mToUpdateTotal = 1;
        mUpdateCounter = 0;

        // http://stackoverflow.com/questions/1005073/initialization-of-an-arraylist-in-one-line
        List<String> symbols = new ArrayList<>();
        symbols.add(symbol);

        ISecurityPriceUpdater updater = SecurityPriceUpdaterFactory.getUpdaterInstance(getActivity());
        updater.downloadPrices(symbols);
        // result received via onEvent.
    }

    /**
     * refresh UI, show favorite icon
     */
    private void setImageViewFavorite() {
        if (mAccount.getFavorite()) {
            this.viewHolder.imgAccountFav.setBackgroundResource(R.drawable.ic_star);
        } else {
            this.viewHolder.imgAccountFav.setBackgroundResource(R.drawable.ic_star_outline);
        }
    }

    private void changePriceProvider() {
        // show the list with provider choices. Preselect the current one.
        InvestmentSettings settings = new InvestmentSettings(getActivity());
        QuoteProviders currentProvider = settings.getQuoteProvider();
        int currentIndex = QuoteProviders.indexOf(currentProvider);

        new MaterialDialog.Builder(getActivity())
                .title(R.string.quote_provider)
                .items(QuoteProviders.names())
                .itemsCallbackSingleChoice(currentIndex, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                        //change provider
                        QuoteProviders newProvider = QuoteProviders.valueOf(text.toString());

                        InvestmentSettings settings = new InvestmentSettings(getActivity());
                        settings.setQuoteProvider(newProvider);
                        return true;
                    }
                })
                .show();
    }

    private void completePriceUpdate() {
        // this call is made from async task so have to get back to the main thread.
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // refresh the data.
                mDataFragment.reloadData();

                // notify about db file change.
                new SyncManager(getActivity()).dataChanged();
            }
        });
    }

    private StockRepository getStockRepository() {
        if (mStockRepository == null) {
            mStockRepository = new StockRepository(getActivity());
        }
        return mStockRepository;
    }

    private String[] getAllShownSymbols() {
        int itemCount = mDataFragment.getListAdapter().getCount();
        String[] result = new String[itemCount];

        for(int i = 0; i < itemCount; i++) {
            Cursor cursor = (Cursor) mDataFragment.getListAdapter().getItem(i);
            String symbol = cursor.getString(cursor.getColumnIndex(StockFields.SYMBOL));

            result[i] = symbol;
        }

        return result;
    }

    private void exportPrices() {
        PriceCsvExport export = new PriceCsvExport(getActivity());
        boolean result = false;

        try {
            String prefix;
            if (mAccount != null) {
                prefix = mAccount.getName();
            } else {
                prefix = getActivity().getString(R.string.all_accounts);
            }
            result = export.exportPrices(mDataFragment.getListAdapter(), prefix);
        } catch (IOException ex) {
            Timber.e(ex, "exporting stock prices");
        }

        // todo: e result. (?)
    }

    private void confirmPriceUpdate() {
        UIHelper ui = new UIHelper(getContext());

        new MaterialDialog.Builder(getContext())
            .title(R.string.download)
            .icon(ui.getIcon(FontAwesome.Icon.faw_question_circle_o))
            .content(R.string.confirm_price_download)
                .positiveText(android.R.string.ok)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        // get the list of symbols
                        String[] symbols = getAllShownSymbols();
                        mToUpdateTotal = symbols.length;
                        mUpdateCounter = 0;

                        // update security prices
                        ISecurityPriceUpdater updater = SecurityPriceUpdaterFactory
                                .getUpdaterInstance(getContext());
                        updater.downloadPrices(Arrays.asList(symbols));
                        // results received via event

                        dialog.dismiss();
                    }
                })
                .negativeText(android.R.string.cancel)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
            .build()
            .show();
    }

    private int getAccountId() {
        if (mAccount == null) {
            return Constants.NOT_SET;
        }

        return mAccount.getId();
    }

    private ActionBar getActionBar() {
        if (!(getActivity() instanceof AppCompatActivity)) return null;

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        ActionBar actionBar = activity.getSupportActionBar();
        return actionBar;
    }

    private Spinner getAccountsSpinner() {
        // get from custom view, not the menu.

        ActionBar actionBar = getActionBar();
        if (actionBar == null) return null;

        Spinner spinner = (Spinner) actionBar.getCustomView().findViewById(R.id.spinner);
        return spinner;
    }

    private void initializeAccountsSelector() {
        ActionBar actionBar = getActionBar();
        if (actionBar == null) return;

        actionBar.setDisplayShowTitleEnabled(false);

        actionBar.setCustomView(R.layout.spinner);
        actionBar.setDisplayShowCustomEnabled(true);

        Spinner spinner = getAccountsSpinner();
        if (spinner == null) return;

        // Load accounts into the spinner.
        AccountService accountService = new AccountService(getActivity());
        accountService.loadInvestmentAccountsToSpinner(spinner, true);

        // e account switching.
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // switch account.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(i);
                Account account = Account.from(cursor);

                int accountId = account.getId();
                switchAccount(accountId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void initializeListHeader(LayoutInflater inflater) {
        this.viewHolder.mListHeader = (ViewGroup) inflater.inflate(R.layout.fragment_watchlist_header, null, false);

        // favorite icon
        this.viewHolder.imgAccountFav = (ImageView) this.viewHolder.mListHeader.findViewById(R.id.imageViewAccountFav);
        // set listener click on favorite icon for change image
        this.viewHolder.imgAccountFav.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mAccount.setFavorite(!mAccount.getFavorite());

                AccountRepository repo = new AccountRepository(getActivity());
                boolean saved = repo.save(mAccount);

                if (!saved) {
                    Toast.makeText(getActivity(),
                        getActivity().getResources().getString(R.string.db_update_failed),
                        Toast.LENGTH_LONG).show();
                } else {
                    setImageViewFavorite();
                }
            }
        });

        // Edit account
        this.viewHolder.imgGotoAccount = (ImageView) this.viewHolder.mListHeader.findViewById(R.id.imageViewGotoAccount);
        this.viewHolder.imgGotoAccount.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AccountEditActivity.class);
                intent.putExtra(AccountEditActivity.KEY_ACCOUNT_ID, getAccountId());
                intent.setAction(Intent.ACTION_EDIT);
                startActivity(intent);
            }
        });
    }

//    private void initializeSwipeToRefresh(View view) {
//        final SwipeRefreshLayout layout = (SwipeRefreshLayout) view.findViewById(R.id.swipeLayout);
//
//        layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                // update prices
//                // todo: do not display the manual progress binaryDialog
//                String[] symbols = getAllShownSymbols();
//                ISecurityPriceUpdater updater = SecurityPriceUpdaterFactory
//                        .getUpdaterInstance(getContext());
//                updater.downloadPrices(Arrays.asList(symbols));
//
//                layout.setRefreshing(false);
//            }
//        });
//    }

    private void loadAccount() {
        Bundle args = getArguments();
        if (args == null) return;
        if (!args.containsKey(KEY_ACCOUNT_ID)) return;

        int accountId = args.getInt(KEY_ACCOUNT_ID);
        this.mAccount = new AccountRepository(getActivity()).load(accountId);
    }

    private void purgePriceHistory() {
        UIHelper ui = new UIHelper(getContext());

        new MaterialDialog.Builder(getContext())
            .title(R.string.purge_history)
            .icon(ui.getIcon(FontAwesome.Icon.faw_question_circle_o))
            .content(R.string.purge_history_confirmation)
                .positiveText(android.R.string.ok)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        StockHistoryRepository history = new StockHistoryRepository(getActivity());
                        int deleted = history.deleteAllPriceHistory();

                        if (deleted > 0) {
                            new SyncManager(getActivity()).dataChanged();
                            Toast.makeText(getActivity(),
                                    getActivity().getString(R.string.purge_history_complete), Toast.LENGTH_SHORT)
                                    .show();
                        } else {
                            Toast.makeText(getActivity(),
                                    getActivity().getString(R.string.purge_history_failed), Toast.LENGTH_SHORT)
                                    .show();
                        }

                        dialog.dismiss();
                    }
                })
                .negativeText(android.R.string.cancel)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();                    }
                })
            .build()
            .show();
    }

    /**
     * Select the current account in the accounts dropdown.
     */
    private void selectCurrentAccount() {
        Spinner spinner = getAccountsSpinner();
        if (spinner == null) return;

        // find account
        SimpleCursorAdapter adapter = (SimpleCursorAdapter) spinner.getAdapter();
        if (adapter == null) return;

        Cursor cursor = adapter.getCursor();
        int position = Constants.NOT_SET;

        for (int i = 0; i < adapter.getCount(); i++) {
            cursor.moveToPosition(i);
            String accountIdString = cursor.getString(cursor.getColumnIndex(Account.ACCOUNTID));
            int accountId = Integer.parseInt(accountIdString);
            if (accountId == getAccountId()) {
                position = i;
                break;
            }
        }

        spinner.setSelection(position);
    }

    private void switchAccount(int accountId) {
        if (accountId == getAccountId()) return;

        // switch account. Reload transactions.
        mAccount = new AccountRepository(getActivity()).load(accountId);

        mDataFragment.accountId = accountId;
        mDataFragment.reloadData();

        // hide account details bar if all accounts are selected
        if (accountId == Constants.NOT_SET) {
            /*
            Can't remove header view once it has been added.
            Ref: http://stackoverflow.com/questions/13603888/remove-header-from-listview
             */
//            mDataFragment.getListView().removeHeaderView(mListHeader);
//            mListHeader.setVisibility(View.GONE);
            /*
            Just hide the contents and the row will automatically shrink (but not disappear).
             */
            this.viewHolder.mListHeader.findViewById(R.id.headerRow).setVisibility(View.GONE);
        } else {
            if (mDataFragment.getListView().getHeaderViewsCount() == 0) {
                mDataFragment.getListView().addHeaderView(this.viewHolder.mListHeader);
            }
//            mListHeader.setVisibility(View.VISIBLE);
            this.viewHolder.mListHeader.findViewById(R.id.headerRow).setVisibility(View.VISIBLE);
        }
    }
}
