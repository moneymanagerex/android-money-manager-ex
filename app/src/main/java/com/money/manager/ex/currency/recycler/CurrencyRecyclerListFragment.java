/*
 * Copyright (C) 2012-2016 The Android Money Manager Ex Project Team
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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.money.manager.ex.R;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.currency.CurrencyUIFeatures;
import com.money.manager.ex.currency.events.ExchangeRateUpdateConfirmedEvent;
import com.money.manager.ex.domainmodel.Currency;
import com.money.manager.ex.investment.events.PriceDownloadedEvent;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.view.recycler.DividerItemDecoration;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

/**
 * Recycler list fragment
 */
public class CurrencyRecyclerListFragment
    extends Fragment {

    public static CurrencyRecyclerListFragment createInstance() {
        CurrencyRecyclerListFragment fragment = new CurrencyRecyclerListFragment();

        // bundle

        return fragment;
    }

    private CurrencyService mCurrencyService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_currency_recycler_list, container, false);
//        View view = super.onCreateView(inflater, container, savedInstanceState);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // todo setShowMenuItemSearch(true);
        // Focus on search menu if set in preferences.
        AppSettings settings = new AppSettings(getActivity());
        boolean focusOnSearch = settings.getBehaviourSettings().getFilterInSelectors();
        // todo setMenuItemSearchIconified(!focusOnSearch);

        // todo setEmptyText(getActivity().getResources().getString(R.string.currencies_empty));

        setHasOptionsMenu(true);

//        // create and link the adapter
//        CurrencyListAdapter adapter = new CurrencyListAdapter(getActivity(), null);
//        setListAdapter(adapter);

//        registerForContextMenu(getListView());
//        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);

//        setListShown(false);
//        loadData();

        // todo setFloatingActionButtonVisible(true);
        // todo setFloatingActionButtonAttachListView(true);

        initializeList();
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
//        onPriceDownloaded(event.symbol, event.price, event.date);
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

    // Private

    private CurrencyService getService() {
        if(mCurrencyService == null) {
            mCurrencyService = new CurrencyService(getActivity());
        }
        return mCurrencyService;
    }

    private void initializeList() {
        Context context = getActivity();

        RecyclerView list = (RecyclerView) getActivity().findViewById(R.id.list);
        if (list == null) return;

        // Layout manager
        list.setLayoutManager(new LinearLayoutManager(context));

        // Adapter
//        CurrencyRecyclerListAdapter adapter = new CurrencyRecyclerListAdapter();
        SectionedRecyclerViewAdapter adapter = new SectionedRecyclerViewAdapter();
        // load data
        CurrencyService service = new CurrencyService(context);

        adapter.addSection(new CurrencySection(getString(R.string.active_currencies), service.getUsedCurrencies()));
        adapter.addSection(new CurrencySection(getString(R.string.inactive_currencies), service.getUnusedCurrencies()));

        //adapter.usedCurrencies = service.getUsedCurrencies();
        //adapter.unusedCurrencies = service.getUnusedCurrencies();

        list.setAdapter(adapter);

        // Separator
        list.addItemDecoration(new DividerItemDecoration(context, LinearLayoutManager.VERTICAL));
    }

}
