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
package com.money.manager.ex.currency.list;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.mmex_icon_font_typeface_library.MMXIconFont;
import com.money.manager.ex.Constants;
import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.common.MmxCursorLoader;
import com.money.manager.ex.common.BaseListFragment;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.currency.CurrencyChartActivity;
import com.money.manager.ex.currency.CurrencyRepository;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.currency.CurrencyUIFeatures;
import com.money.manager.ex.currency.events.CurrencyDeletionConfirmedEvent;
import com.money.manager.ex.currency.events.ExchangeRateUpdateConfirmedEvent;
import com.money.manager.ex.datalayer.Select;
import com.money.manager.ex.domainmodel.Account;
import com.money.manager.ex.domainmodel.Currency;
import com.money.manager.ex.investment.events.PriceDownloadedEvent;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.utils.ActivityUtils;
import com.money.manager.ex.utils.MmxDatabaseUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

/**
 *  Currency list.
 */
public class CurrencyListFragment
    extends BaseListFragment {

    private static final int ID_LOADER_CURRENCY = 0;

    // Store previous device orientation when showing other screens (chart, etc.)
    public int mPreviousOrientation = Constants.NOT_SET;

//    @Inject Lazy<CurrencyService> mCurrencyService;
    private CurrencyService mCurrencyService;

    private String mAction = Intent.ACTION_EDIT;
    private String mCurFilter;
    private boolean mShowOnlyUsedCurrencies;
    private LoaderManager.LoaderCallbacks<Cursor> loaderCallbacks;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MmexApplication.getApp().iocComponent.inject(this);

        mAction = getActivity().getIntent().getAction();
        if (mAction.equals(Intent.ACTION_MAIN)) {
            mAction = Intent.ACTION_EDIT;
        }

        // Filter currencies only if in the standalone Currencies list. Do not filter in pickers.
        mShowOnlyUsedCurrencies = !mAction.equals(Intent.ACTION_PICK);

        loaderCallbacks = initLoaderCallbacks();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // todo initializeSwipeToRefresh();

        return super.onCreateView(inflater, container, savedInstanceState);
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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setSearchMenuVisible(true);
        // Focus on search menu if set in preferences.
        AppSettings settings = new AppSettings(getActivity());
        boolean focusOnSearch = settings.getBehaviourSettings().getFilterInSelectors();
        setMenuItemSearchIconified(!focusOnSearch);

        setEmptyText(getActivity().getResources().getString(R.string.currencies_empty));

        setHasOptionsMenu(true);

        // create and link the adapter
        CurrencyListAdapter adapter = new CurrencyListAdapter(getActivity(), null);
        setListAdapter(adapter);

        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        setListShown(false);
        loadData();

        // for some reason, the onViewCreated does not fire when expected.
        setupFloatingActionButton(getView());
        attachFloatingActionButtonToListView();
        setFloatingActionButtonVisible(true);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        registerForContextMenu(getListView());
    }

    // Menu.

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_currency_formats_list_activity, menu);

        UIHelper ui = new UIHelper(getActivity());

        MenuItem item = menu.findItem(R.id.menu_update_exchange_rate);
        item.setIcon(ui.getIcon(GoogleMaterial.Icon.gmd_file_download));

        // Customize with font icon, if needed.
        item = menu.findItem(R.id.menu_import_all_currencies);
        item.setIcon(ui.getIcon(MMXIconFont.Icon.mmx_share_square));
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem showOnlyUsed = menu.findItem(R.id.menu_show_used);
        if (showOnlyUsed != null) {
            showOnlyUsed.setChecked(mShowOnlyUsedCurrencies);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        CurrencyUIFeatures ui = new CurrencyUIFeatures(getActivity());

        switch (item.getItemId()) {
            case R.id.menu_import_all_currencies:
                ui.showDialogImportAllCurrencies();
                return true;

            case R.id.menu_update_exchange_rate:
                ui.showDialogUpdateExchangeRates();
                break;

            case R.id.menu_show_used:
                if (item.isChecked()) {
                    item.setChecked(false);
                    // list all accounts
                    mShowOnlyUsedCurrencies = false;
                    reloadData();
                } else {
                    item.setChecked(true);
                    // list only used accounts
                    mShowOnlyUsedCurrencies = true;
                    reloadData();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // Context menu

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        // take cursor and move into position
        Cursor cursor = ((CurrencyListAdapter) getListAdapter()).getCursor();
        cursor.moveToPosition(info.position);
        // set currency name
        menu.setHeaderTitle(cursor.getString(cursor.getColumnIndex(Currency.CURRENCYNAME)));

        // compose context menu
        String[] menuItems = getResources().getStringArray(R.array.context_menu_currencies);
        for (int i = 0; i < menuItems.length; i++) {
            menu.add(Menu.NONE, i, i, menuItems[i]);
        }
    }

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        // take cursor and move to position
        Cursor cursor = ((CurrencyListAdapter) getListAdapter()).getCursor();
        cursor.moveToPosition(info.position);
        int currencyId = cursor.getInt(cursor.getColumnIndex(Currency.CURRENCYID));

        CurrencyUIFeatures ui = new CurrencyUIFeatures(getActivity());

        // check item selected
        int selectedItem = item.getItemId();
        switch (selectedItem) {
            case 0: //EDIT
                ui.startCurrencyEditActivity(currencyId);
                break;

            case 1: // Chart
                // remember the device orientation and return to it after the chart.
                this.mPreviousOrientation = ActivityUtils.forceCurrentOrientation(getActivity());

                // add the currency information.
                String symbol = cursor.getString(cursor.getColumnIndex(Currency.CURRENCY_SYMBOL));
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

    // Search

    @Override
    public boolean onQueryTextChange(String newText) {
        // Called when the action bar search text has changed.  Update
        // the search filter, and restart the loader to do a new query
        // with this filter.
        mCurFilter = !TextUtils.isEmpty(newText) ? newText : null;
        reloadData();
        return true;
    }

    @Override
    protected void setResult() {
        Intent result;
        if (Intent.ACTION_PICK.equals(mAction)) {
            // create intent
            Cursor cursor = ((CurrencyListAdapter) getListAdapter()).getCursor();

            for (int i = 0; i < getListView().getCount(); i++) {
                if (getListView().isItemChecked(i)) {
                    cursor.moveToPosition(i);

                    result = new Intent();
                    result.putExtra(CurrencyListActivity.INTENT_RESULT_CURRENCYID,
                            cursor.getInt(cursor.getColumnIndex(Currency.CURRENCYID)));
                    result.putExtra(CurrencyListActivity.INTENT_RESULT_CURRENCYNAME,
                            cursor.getString(cursor.getColumnIndex(Currency.CURRENCYNAME)));

                    getActivity().setResult(Activity.RESULT_OK, result);

                    return;
                }
            }
        }
        getActivity().setResult(CurrencyListActivity.RESULT_CANCELED);
    }

    @Override
    public void onFloatingActionButtonClicked() {
        CurrencyUIFeatures ui = new CurrencyUIFeatures(getActivity());
        ui.startCurrencyEditActivity(null);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        // Show context menu only if we are displaying the list of currencies
        // but not in selection mode.
        if (mAction.equals(Intent.ACTION_EDIT)) {
            getActivity().openContextMenu(v);
        } else {
            // we are picking a currency. Select one.
            setResultAndFinish();
        }
    }

    @Override
    public String getSubTitle() {
        String result;
//        if (mShowOnlyUsedCurrencies) {
//            result = getString(R.string.currencies_used);
//        } else {
            result = getString(R.string.currencies);
//        }
        return result;
    }

    @Subscribe
    public void onEvent(PriceDownloadedEvent event) {
        CurrencyUIFeatures ui = new CurrencyUIFeatures(getContext());
        ui.onPriceDownloaded(event.symbol, event.price, event.date);
    }

    @Subscribe
    public void onEvent(ExchangeRateUpdateConfirmedEvent event) {
        // proceed with rate update
        // todo: use event.updateAll parameter

        // Update only the visible currencies.
        List<Currency> currencies = getVisibleCurrencies();

        getService().updateExchangeRates(currencies);
    }

    @Subscribe
    public void onEvent(CurrencyDeletionConfirmedEvent event) {
        CurrencyRepository repo = new CurrencyRepository(getContext());
        boolean success = repo.delete(event.currencyId);
        if (success) {
            Toast.makeText(getContext(), R.string.delete_success, Toast.LENGTH_SHORT).show();
        }
        // restart loader
        reloadData();
    }

    // Private methods.

    private List<Currency> getVisibleCurrencies() {
        CurrencyListAdapter adapter = (CurrencyListAdapter) getListAdapter();
        if (adapter == null) return null;

        Cursor cursor = adapter.getCursor();
        if (cursor == null) return null;

        cursor.moveToPosition(Constants.NOT_SET);
        List<Currency> currencies = new ArrayList<>();

        while (cursor.moveToNext()) {
            Currency currency = Currency.fromCursor(cursor);
            currencies.add(currency);
        }

        return currencies;
    }

    private CurrencyService getService() {
        // todo: redo the currency service to remove any UI interaction (dialog) and then use IoC.
//        return mCurrencyService.get();
        if (mCurrencyService == null) {
            mCurrencyService = new CurrencyService(getActivity());
        }
        return mCurrencyService;
    }

    private LoaderManager.LoaderCallbacks<Cursor> initLoaderCallbacks() {
        LoaderManager.LoaderCallbacks<Cursor> callbacks = new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                switch (id) {
                    case ID_LOADER_CURRENCY:
                        String whereClause = "";
                        ArrayList<String> arguments = new ArrayList<>();

                        // filter only used accounts?
                        if (mShowOnlyUsedCurrencies) {
                            // get the list of used currencies.
                            CurrencyService currencyService = getService();
                            List<Currency> usedCurrencies = currencyService.getUsedCurrencies();
                            if (usedCurrencies != null && usedCurrencies.size() > 0) {
                                ArrayList<String> symbols = new ArrayList<>();
                                for (Currency currency : usedCurrencies) {
                                    if (currency == null) {
                                        new UIHelper(getActivity()).showToast(R.string.currency_not_found);
                                    } else {
                                        symbols.add(currency.getCode());
                                    }
                                }

                                MmxDatabaseUtils databaseUtils = new MmxDatabaseUtils(getActivity());
                                whereClause = Currency.CURRENCY_SYMBOL + " IN (" +
                                        databaseUtils.makePlaceholders(usedCurrencies.size()) + ")";
                                arguments.addAll(symbols);
                            }
                        }

                        if (!TextUtils.isEmpty(mCurFilter)) {
                            if (!TextUtils.isEmpty(whereClause)) {
                                whereClause += " AND ";
                            }

                            whereClause += Currency.CURRENCYNAME + " LIKE ?";
                            arguments.add(mCurFilter + "%");
//                    selectionArgs = new String[]{ mCurFilter + "%"};
                        }

                        String selectionArgs[] = new String[arguments.size()];
                        selectionArgs = arguments.toArray(selectionArgs);

                        CurrencyRepository repo = new CurrencyRepository(getActivity());
                        Select query = new Select(repo.getAllColumns())
                                .where(whereClause, selectionArgs)
                                .orderBy("upper(" + Currency.CURRENCYNAME + ")");

                        return new MmxCursorLoader(getActivity(), repo.getUri(), query);
                }

                return null;
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
                switch (loader.getId()) {
                    case ID_LOADER_CURRENCY:
                        CurrencyListAdapter adapter = (CurrencyListAdapter) getListAdapter();
//                adapter.swapCursor(null);
                        adapter.changeCursor(null);
                        break;
                }
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                switch (loader.getId()) {
                    case ID_LOADER_CURRENCY:
                        CurrencyListAdapter adapter = (CurrencyListAdapter) getListAdapter();
//                adapter.swapCursor(data);
                        adapter.changeCursor(data);

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
        };
        return callbacks;
    }

    private void initializeSwipeToRefresh() {

    }

    private void loadData() {
        getLoaderManager().initLoader(ID_LOADER_CURRENCY, null, this.loaderCallbacks);
    }

    private void reloadData() {
        getLoaderManager().restartLoader(ID_LOADER_CURRENCY, null, this.loaderCallbacks);
    }
}
