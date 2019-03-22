/*
 * Copyright (C) 2012-2019 The Android Money Manager Ex Project Team
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

package com.money.manager.ex.currency.recycler;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.money.manager.ex.R;
import com.money.manager.ex.common.events.ListItemClickedEvent;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.currency.CurrencyChartActivity;
import com.money.manager.ex.currency.CurrencyRepository;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.currency.CurrencyUIFeatures;
import com.money.manager.ex.currency.events.CurrencyDeletionConfirmedEvent;
import com.money.manager.ex.currency.events.ExchangeRateUpdateConfirmedEvent;
import com.money.manager.ex.domainmodel.Account;
import com.money.manager.ex.domainmodel.Currency;
import com.money.manager.ex.investment.events.AllPricesDownloadedEvent;
import com.money.manager.ex.investment.events.PriceDownloadedEvent;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.view.recycler.ContextMenuRecyclerView;
import com.money.manager.ex.view.recycler.DividerItemDecoration;
import com.money.manager.ex.view.recycler.RecyclerViewContextMenuInfo;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.SubscriberExceptionEvent;

import java.util.List;
import java.util.Map;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.github.luizgrp.sectionedrecyclerviewadapter.Section;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import timber.log.Timber;

/**
 * Recycler list fragment.
 * Missing pieces:
 * - search
 * - back button
 * - context menu
 * - update data after currency edit/insert
 * - update list after currency exchange rate update.
 */
public class CurrencyRecyclerListFragment
    extends Fragment {

    public static CurrencyRecyclerListFragment createInstance() {
        CurrencyRecyclerListFragment fragment = new CurrencyRecyclerListFragment();

        // bundle

        return fragment;
    }

    private CurrencyService mCurrencyService;
    private CurrencyContentObserver mObserver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // create content observer
        mObserver = new CurrencyContentObserver(null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_currency_recycler_list, container, false);
//        View view = super.onCreateView(inflater, container, savedInstanceState);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        ContextMenuRecyclerView list = getRecyclerView();
        registerForContextMenu(list);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // todo setSearchMenuVisible(true);
        // Focus on search menu if set in preferences.
        AppSettings settings = new AppSettings(getActivity());
        boolean focusOnSearch = settings.getBehaviourSettings().getFilterInSelectors();
        // todo setMenuItemSearchIconified(!focusOnSearch);

        // todo setEmptyText(getActivity().getResources().getString(R.string.currencies_empty));

        setHasOptionsMenu(true);

        // todo setFloatingActionButtonVisible(true);
        // todo attachFloatingActionButtonToListView(true);

        initializeList();
    }

    @Override
    public void onPause() {
        super.onPause();

        // register content observer
        CurrencyRepository repo = new CurrencyRepository(getActivity());
        getActivity().getContentResolver().registerContentObserver(repo.getUri(), true, mObserver);
    }

    @Override
    public void onResume() {
        super.onResume();

        // unregister content observer
        getActivity().getContentResolver().unregisterContentObserver(mObserver);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        RecyclerViewContextMenuInfo info = (RecyclerViewContextMenuInfo) menuInfo;
        Currency currency = getCurrencyAtPosition(info.position);
        menu.setHeaderTitle(currency.getName());

        // compose context menu
        String[] menuItems = getResources().getStringArray(R.array.context_menu_currencies);
        for (int i = 0; i < menuItems.length; i++) {
            menu.add(Menu.NONE, i, i, menuItems[i]);
        }
    }

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
        RecyclerViewContextMenuInfo info = (RecyclerViewContextMenuInfo) item.getMenuInfo();
        Currency currency = getCurrencyAtPosition(info.position);
        int currencyId = currency.getCurrencyId();

        CurrencyUIFeatures ui = new CurrencyUIFeatures(getActivity());

        // check item selected
        int selectedItem = item.getItemId();
        switch (selectedItem) {
            case 0: //EDIT
                ui.startCurrencyEditActivity(currencyId);
                break;

            case 1: // Chart
                // remember the device orientation and return to it after the chart.
                // todo: this.mPreviousOrientation = ActivityUtils.forceCurrentOrientation(getActivity());

                // add the currency information.
                String symbol = currency.getCode();
                CurrencyService currencyService = this.getService();
                String baseCurrencyCode = currencyService.getBaseCurrencyCode();

                Intent intent = new Intent(getActivity(), CurrencyChartActivity.class);
                intent.setAction(Intent.ACTION_VIEW);
                intent.putExtra(Currency.CURRENCY_SYMBOL, symbol);
                intent.putExtra(CurrencyChartActivity.BASE_CURRENCY_SYMBOL, baseCurrencyCode);

                startActivity(intent);
                break;

            case 2: // Update exchange rate
                getService().updateExchangeRate(currencyId);
                break;

            case 3: //DELETE
                CurrencyService service = new CurrencyService(getActivity());
                boolean used = service.isCurrencyUsed(currencyId);

                if (used) {
                    ui.notifyCurrencyCanNotBeDeleted();
                } else {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(Account.CURRENCYID, currencyId);
                    ui.showDialogDeleteCurrency(currencyId, info.position);
                }
                break;
        }
        return false;
    }

    // Events

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

    @Subscribe
    public void onEvent(PriceDownloadedEvent event) {
        CurrencyUIFeatures ui = new CurrencyUIFeatures(getContext());
        boolean updated = ui.onPriceDownloaded(event.symbol, event.price, event.date);
        if (!updated) {
            // show error msg
            new UIHelper(getActivity()).showToast("could not update the price!");
        }
    }

    @Subscribe
    public void onEvent(AllPricesDownloadedEvent event) {
        loadData(getAdapter());
        // update ui.
        getAdapter().notifyItemRangeChanged(0, getAdapter().getItemCount());
    }

    @Subscribe
    public void onEvent(ExchangeRateUpdateConfirmedEvent event) {
        // proceed with rate update

        List<Currency> currencies = getService().getUsedCurrencies();

        if (event.updateAll) {
            // add unused currencies.
            currencies.addAll(getService().getUnusedCurrencies());
        }

        getService().updateExchangeRates(currencies);
    }

    @Subscribe
    public void onEvent(SubscriberExceptionEvent exceptionEvent) {
        Timber.e(exceptionEvent.throwable, "events");
    }

    @Subscribe
    public void onEvent(ListItemClickedEvent event) {
        // item selected. Show context menu.
        // todo: complete
        getActivity().openContextMenu(event.view);
    }

    @Subscribe
    public void onEvent(CurrencyDeletionConfirmedEvent event) {
        CurrencyRepository repo = new CurrencyRepository(getContext());
        boolean success = repo.delete(event.currencyId);
        if (success) {
            Toast.makeText(getContext(), R.string.delete_success, Toast.LENGTH_SHORT).show();

            // remove from data.
            Map<String, Section> sectionMap = getAdapter().getCopyOfSectionsMap();
            for(Section section : sectionMap.values()){
                CurrencySection currencySection = (CurrencySection) section;
                currencySection.currencies.remove(event.itemPosition);
            }

            // update ui.
            getAdapter().notifyItemRemoved(event.itemPosition);
        }
    }

    // Menu.

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_currency_formats_list_activity, menu);

        menu.findItem(R.id.menu_show_used).setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        CurrencyUIFeatures ui = new CurrencyUIFeatures(getActivity());

        switch (item.getItemId()) {
            case R.id.menu_import_all_currencies:
                ui.showDialogImportAllCurrencies();
                return true;

            case R.id.menu_update_exchange_rate:
                ui.showActiveInactiveSelectorForUpdate();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

//    @Override
//    public void onFloatingActionButtonClicked() {
//        CurrencyUIFeatures ui = new CurrencyUIFeatures(getActivity());
//        ui.startCurrencyEditActivity(null);
//    }

    // Private

    private SectionedRecyclerViewAdapter getAdapter() {
        return (SectionedRecyclerViewAdapter) getRecyclerView().getAdapter();
    }

    private Currency getCurrencyAtPosition(int position) {
        int sectionPosition = getAdapter().getSectionPosition(position);
        CurrencySection section = (CurrencySection) getAdapter().getSectionForPosition(position);
        //Currency currency = section.currencies.get(sectionPosition);
        Currency currency = section.getItemAtPosition(sectionPosition);

        return currency;
    }

    private ContextMenuRecyclerView getRecyclerView() {
        return (ContextMenuRecyclerView) getActivity().findViewById(R.id.list);
    }

    private CurrencyService getService() {
        if(mCurrencyService == null) {
            mCurrencyService = new CurrencyService(getActivity());
        }
        return mCurrencyService;
    }

    private void initializeList() {
        Context context = getActivity();

        RecyclerView recyclerView = getRecyclerView();
        if (recyclerView == null) return;

        // Layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        // Adapter
        final SectionedRecyclerViewAdapter adapter = new SectionedRecyclerViewAdapter();
        // load data
        loadData(adapter);

        recyclerView.setAdapter(adapter);

        // Separator
        recyclerView.addItemDecoration(new DividerItemDecoration(context, LinearLayoutManager.VERTICAL));

        // Behaviours. List click listener.
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Currency currency = getCurrencyAtPosition(position);
                int id = currency.getCurrencyId();
                String name = currency.getName();
                EventBus.getDefault().post(new ListItemClickedEvent(id, name, view));
            }
        }));
    }

    private void loadData(SectionedRecyclerViewAdapter adapter) {
        CurrencyService service = new CurrencyService(getActivity());

        adapter.removeAllSections();

//        List<Currency> currencies = new ArrayList<>();
        //for (Currency currency : service.getUsedCurrencies()) currencies.put(currency.getCode(), currency);
        adapter.addSection(new CurrencySection(getString(R.string.active_currencies), service.getUsedCurrencies()));

//        currencies = new ArrayList<>();
//        for (Currency currency : service.getUnusedCurrencies()) currencies.put(currency.getCode(), currency);
        adapter.addSection(new CurrencySection(getString(R.string.inactive_currencies), service.getUnusedCurrencies()));

    }
}
