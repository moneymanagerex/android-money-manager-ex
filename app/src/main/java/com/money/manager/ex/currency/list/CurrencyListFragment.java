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
package com.money.manager.ex.currency.list;

import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.common.MmexCursorLoader;
import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.common.BaseListFragment;
import com.money.manager.ex.currency.CurrencyChartActivity;
import com.money.manager.ex.currency.CurrencyEditActivity;
import com.money.manager.ex.currency.CurrencyRepository;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.currency.CurrencyUIFeatures;
import com.money.manager.ex.currency.events.ExchangeRateUpdateConfirmedEvent;
import com.money.manager.ex.datalayer.Query;
import com.money.manager.ex.domainmodel.Account;
import com.money.manager.ex.domainmodel.Currency;
import com.money.manager.ex.investment.events.PriceDownloadedEvent;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.utils.ActivityUtils;
import com.money.manager.ex.utils.MyDatabaseUtils;
import com.shamanland.fonticon.FontIconDrawable;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import info.javaperformance.money.Money;

/**
 *  Currency list.
 */
public class CurrencyListFragment
    extends BaseListFragment
    implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int ID_LOADER_CURRENCY = 0;

    // Store previous device orientation when showing other screens (chart, etc.)
    public int mPreviousOrientation = Constants.NOT_SET;

    private String mAction = Intent.ACTION_EDIT;
    private String mCurFilter;
    private CurrencyService mCurrencyService;
    private boolean mShowOnlyUsedCurrencies;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAction = getActivity().getIntent().getAction();
        if (mAction.equals(Intent.ACTION_MAIN)) {
            // todo: check if this adjustment is still needed.
            mAction = Intent.ACTION_EDIT;
        }

        // Filter currencies only if in the standalone Currencies list. Do not filter in pickers.
        mShowOnlyUsedCurrencies = !mAction.equals(Intent.ACTION_PICK);
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

        setShowMenuItemSearch(true);
        // Focus on search menu if set in preferences.
        AppSettings settings = new AppSettings(getActivity());
        boolean focusOnSearch = settings.getBehaviourSettings().getFilterInSelectors();
        setMenuItemSearchIconified(!focusOnSearch);

        setEmptyText(getActivity().getResources().getString(R.string.currencies_empty));

        setHasOptionsMenu(true);

        // create and link the adapter
        CurrencyListAdapter adapter = new CurrencyListAdapter(getActivity(), null);
        setListAdapter(adapter);

        registerForContextMenu(getListView());
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        setListShown(false);
        loadData();

        setFloatingActionButtonVisible(true);
        setFloatingActionButtonAttachListView(true);
    }

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

        // check item selected
        int selectedItem = item.getItemId();
        switch (selectedItem) {
            case 0: //EDIT
                startCurrencyEditActivity(currencyId);
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
                updateSingleCurrencyExchangeRate(currencyId);
                break;

            case 3: //DELETE
                CurrencyService service = new CurrencyService(getActivity());
                boolean used = service.isCurrencyUsed(currencyId);
                if (used) {
                    new AlertDialogWrapper.Builder(getContext())
                        .setTitle(R.string.attention)
                        .setIcon(FontIconDrawable.inflate(getContext(), R.xml.ic_alert))
                        .setMessage(R.string.currency_can_not_deleted)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create().show();
                } else {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(Account.CURRENCYID, currencyId);
                    showDialogDeleteCurrency(currencyId);
                }
                break;
        }
        return false;
    }

    // Loader event handlers.

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
                                ExceptionHandler handler = new ExceptionHandler(getActivity());
                                handler.showMessage(getString(R.string.currency_not_found));
                            } else {
                                symbols.add(currency.getCode());
                            }
                        }

                        MyDatabaseUtils databaseUtils = new MyDatabaseUtils(getActivity());
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
                Query query = new Query()
                        .select(repo.getAllColumns())
                        .where(whereClause, selectionArgs)
                        .orderBy("upper(" + Currency.CURRENCYNAME + ")");

                return new MmexCursorLoader(getActivity(), repo.getUri(), query);
        }

        return null;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case ID_LOADER_CURRENCY:
                CurrencyListAdapter adapter = (CurrencyListAdapter) getListAdapter();
                adapter.swapCursor(null);
                break;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case ID_LOADER_CURRENCY:
                CurrencyListAdapter adapter = (CurrencyListAdapter) getListAdapter();
                adapter.swapCursor(data);

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

    // Menu.

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_currency_formats_list_activity, menu);
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

    // Search

    @Override
    public boolean onQueryTextChange(String newText) {
        // Called when the action bar search text has changed.  Update
        // the search filter, and restart the loader to do a new query
        // with this filter.
        mCurFilter = !TextUtils.isEmpty(newText) ? newText : null;
        getLoaderManager().restartLoader(ID_LOADER_CURRENCY, null, this);
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
    public void onFloatingActionButtonClickListener() {
        startCurrencyEditActivity(null);
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
        onPriceDownloaded(event.symbol, event.price, event.date);
    }

    @Subscribe
    public void onEvent(ExchangeRateUpdateConfirmedEvent event) {
        // proceed with rate update

        // Update only the visible currencies.
        List<Currency> currencies = getVisibleCurrencies();

        getService().updateExchangeRates(currencies);
    }

    // Private methods.

    private void onPriceDownloaded(String symbol, Money price, DateTime date) {
        // extract destination currency
        String baseCurrencyCode = getService().getBaseCurrencyCode();
        String destinationCurrency = symbol.replace(baseCurrencyCode, "");
        destinationCurrency = destinationCurrency.replace("=X", "");
        boolean success = false;

        try {
            // update exchange rate.
            success = getService().saveExchangeRate(destinationCurrency, price);
        } catch (Exception ex) {
            ExceptionHandler handler = new ExceptionHandler(getActivity(), this);
            handler.handle(ex, "saving exchange rate");
        }

        if (!success) {
            String message = getString(R.string.error_update_currency_exchange_rate);
            message += " " + destinationCurrency;

            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
        }
    }

    private void showDialogDeleteCurrency(final int currencyId) {
        // config alert dialog
        AlertDialogWrapper.Builder alertDialog = new AlertDialogWrapper.Builder(getContext())
            .setTitle(R.string.delete_currency)
            .setIcon(FontIconDrawable.inflate(getContext(), R.xml.ic_question))
            .setMessage(R.string.confirmDelete);
        // set listener on positive button
        alertDialog.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CurrencyRepository repo = new CurrencyRepository(getActivity());
                        boolean success = repo.delete(currencyId);
                        if (success) {
                            Toast.makeText(getActivity(), R.string.delete_success, Toast.LENGTH_SHORT).show();
                        }
                        // restart loader
                        getLoaderManager().restartLoader(ID_LOADER_CURRENCY, null, CurrencyListFragment.this);
                    }
                });
        // set listener on negative button
        alertDialog.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        // create dialog and show
        alertDialog.create().show();
    }

    private void startCurrencyEditActivity(Integer currencyId) {
        // create intent, set Account ID
        Intent intent = new Intent(getActivity(), CurrencyEditActivity.class);
        // check transId not null
        if (currencyId != null) {
            intent.putExtra(CurrencyEditActivity.KEY_CURRENCY_ID, currencyId);
            intent.setAction(Intent.ACTION_EDIT);
        } else {
            intent.setAction(Intent.ACTION_INSERT);
        }
        // launch activity
        startActivity(intent);
    }

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
        if(mCurrencyService == null) {
            mCurrencyService = new CurrencyService(getActivity());
        }
        return mCurrencyService;
    }

    /**
     * Update rate for the currently selected currency.
     */
    private void updateSingleCurrencyExchangeRate(final int currencyId) {
        updateCurrency(currencyId);
    }

    private boolean updateCurrency(int toCurrencyId) {
        CurrencyService utils = getService();

        List<Currency> currencies = new ArrayList<>();
        currencies.add(utils.getCurrency(toCurrencyId));

        utils.updateExchangeRates(currencies);

        return true;
    }

    private void loadData() {
        getLoaderManager().initLoader(ID_LOADER_CURRENCY, null, this);
    }

    private void reloadData() {
        getLoaderManager().restartLoader(ID_LOADER_CURRENCY, null, this);
    }
}
