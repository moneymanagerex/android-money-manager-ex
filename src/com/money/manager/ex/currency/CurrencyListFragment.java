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
package com.money.manager.ex.currency;

import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
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
import com.money.manager.ex.database.TableAccountList;
import com.money.manager.ex.database.TableCurrencyFormats;
import com.money.manager.ex.database.TablePayee;
import com.money.manager.ex.common.BaseListFragment;
import com.money.manager.ex.investment.IPriceUpdaterFeedback;
import com.money.manager.ex.investment.ISecurityPriceUpdater;
import com.money.manager.ex.investment.SecurityPriceUpdaterFactory;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.utils.ActivityUtils;
import com.money.manager.ex.utils.MmexDatabaseUtils;
import com.shamanland.fonticon.FontIconDrawable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import info.javaperformance.money.Money;

/**
 *  Currency list.
 */
public class CurrencyListFragment
        extends BaseListFragment
        implements LoaderManager.LoaderCallbacks<Cursor>, IPriceUpdaterFeedback {

    private static final int ID_LOADER_CURRENCY = 0;

    public String mAction = Intent.ACTION_EDIT;
    // Store previous device orientation when showing other screens (chart, etc.)
    public int PreviousOrientation = -1;

    private TableCurrencyFormats mCurrency = new TableCurrencyFormats();

    private String mCurFilter;
    private CurrencyService mCurrencyService;
    private boolean mShowOnlyUsedCurrencies;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Filter currencies only if in the standalone Currencies list. Do not filter in pickers.
        mShowOnlyUsedCurrencies = !mAction.equals(Intent.ACTION_PICK);
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
        menu.setHeaderTitle(cursor.getString(cursor.getColumnIndex(TableCurrencyFormats.CURRENCYNAME)));

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

        int currencyId = cursor.getInt(cursor.getColumnIndex(TableCurrencyFormats.CURRENCYID));

        // check item selected
        int selectedItem = item.getItemId();
        switch (selectedItem) {
            case 0: //EDIT
                startCurrencyFormatActivity(currencyId);
                break;
            case 1: // Chart
                // remember the device orientation and return to it after the chart.
                this.PreviousOrientation = ActivityUtils.forceCurrentOrientation(getActivity());

                Intent intent = new Intent(getActivity(), CurrencyChartActivity.class);
                intent.setAction(Intent.ACTION_VIEW);
                // add the currency information.
                String symbol = cursor.getString(cursor.getColumnIndex(TableCurrencyFormats.CURRENCY_SYMBOL));
                intent.putExtra(TableCurrencyFormats.CURRENCY_SYMBOL, symbol);
                CurrencyService currencyService = this.getCurrencyUtils();
                String baseCurrencyCode = currencyService.getBaseCurrencyCode();
                intent.putExtra(CurrencyChartActivity.BASE_CURRENCY_SYMBOL, baseCurrencyCode);
                startActivity(intent);
                break;
            case 2: // Update exchange rate
                updateSingleCurrencyExchangeRate(currencyId);
                break;
            case 3: //DELETE
                ContentValues contentValues = new ContentValues();
                contentValues.put(TableAccountList.CURRENCYID, currencyId);
                if (new TablePayee().canDelete(getActivity(), contentValues, TableAccountList.class.getName())) {
                    showDialogDeleteCurrency(currencyId);
                } else {
//                    Core core = new Core(getActivity());
//                    int icon = core.usingDarkTheme()
//                            ? R.drawable.ic_action_warning_dark
//                            : R.drawable.ic_action_warning_light;

                    new AlertDialogWrapper.Builder(getContext())
                        .setTitle(R.string.attention)
                        .setIcon(FontIconDrawable.inflate(getContext(), R.xml.ic_alert))
                        .setMessage(R.string.currency_can_not_deleted)
//                        .setIcon(icon)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create().show();
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
                    CurrencyService currencyService = getCurrencyUtils();
                    List<TableCurrencyFormats> usedCurrencies = currencyService.getUsedCurrencies();
                    if (usedCurrencies.size() > 0) {
                        ArrayList<String> symbols = new ArrayList<>();
                        for (TableCurrencyFormats currency : usedCurrencies) {
                            symbols.add(currency.getCurrencySymbol());
                        }

                        MmexDatabaseUtils databaseUtils = new MmexDatabaseUtils(getActivity());
                        whereClause = TableCurrencyFormats.CURRENCY_SYMBOL + " IN (" +
                                databaseUtils.makePlaceholders(usedCurrencies.size()) + ")";
                        arguments.addAll(symbols);
                    }
                }

                if (!TextUtils.isEmpty(mCurFilter)) {
                    if (!TextUtils.isEmpty(whereClause)) {
                        whereClause += " AND ";
                    }

                    whereClause += TableCurrencyFormats.CURRENCYNAME + " LIKE ?";
                    arguments.add(mCurFilter + "%");
//                    selectionArgs = new String[]{ mCurFilter + "%"};
                }

                String selectionArgs[] = new String[arguments.size()];
                selectionArgs = arguments.toArray(selectionArgs);

                return new MmexCursorLoader(getActivity(), mCurrency.getUri(),
                        mCurrency.getAllColumns(),
                        whereClause,
                        selectionArgs,
                        "upper(" + TableCurrencyFormats.CURRENCYNAME + ")");
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
        switch (item.getItemId()) {
            case R.id.menu_import_all_currencies:
                showDialogImportAllCurrencies();
                return true;

            case R.id.menu_update_exchange_rate:
                showDialogUpdateExchangeRateCurrencies();
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

    // End menu.

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
                    result.putExtra(CurrenciesActivity.INTENT_RESULT_CURRENCYID,
                            cursor.getInt(cursor.getColumnIndex(TableCurrencyFormats.CURRENCYID)));
                    result.putExtra(CurrenciesActivity.INTENT_RESULT_CURRENCYNAME,
                            cursor.getString(cursor.getColumnIndex(TableCurrencyFormats.CURRENCYNAME)));

                    getActivity().setResult(Activity.RESULT_OK, result);

                    return;
                }
            }
        }
        getActivity().setResult(CurrenciesActivity.RESULT_CANCELED);
    }

    @Override
    public void onFloatingActionButtonClickListener() {
        startCurrencyFormatActivity(null);
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
        return getString(R.string.currencies);
    }

    @Override
    public void onPriceDownloaded(String symbol, Money price, Date date) {
        // extract destination currency
        String baseCurrencyCode = getCurrencyUtils().getBaseCurrencyCode();
        String destinationCurrency = symbol.replace(baseCurrencyCode, "");
        destinationCurrency = destinationCurrency.replace("=X", "");
        boolean success = false;

        try {
            // update exchange rate.
            success = saveExchangeRate(destinationCurrency, price);
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

    // Private methods.

    private boolean saveExchangeRate(String symbol, Money rate) {
        CurrencyRepository repo = new CurrencyRepository(getActivity());
        TableCurrencyFormats currency = repo.loadCurrency(symbol);
        int currencyId = currency.getCurrencyId();

        // update value on database
        int updateResult = repo.saveExchangeRate(currencyId, rate);

        return updateResult > 0;
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
                        if (getActivity().getContentResolver().delete(mCurrency.getUri(),
                                TableCurrencyFormats.CURRENCYID + "=" + currencyId, null) == 0) {
                            Toast.makeText(getActivity(), R.string.db_delete_failed, Toast.LENGTH_SHORT).show();
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

    private void startCurrencyFormatActivity(Integer currencyId) {
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

    private void showDialogImportAllCurrencies() {
        // config alert dialog
        AlertDialogWrapper.Builder alertDialog = new AlertDialogWrapper.Builder(getContext())
            .setTitle(R.string.attention)
            .setIcon(FontIconDrawable.inflate(getContext(), R.xml.ic_question))
            .setMessage(R.string.question_import_currencies);
        // set listener on positive button
        alertDialog.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        importAllCurrencies();
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

    private void showDialogUpdateExchangeRateCurrencies() {
        // config alert dialog
        AlertDialogWrapper.Builder alertDialog = new AlertDialogWrapper.Builder(getContext())
            .setTitle(R.string.download)
            .setIcon(FontIconDrawable.inflate(getContext(), R.xml.ic_question))
            .setMessage(R.string.question_update_currency_exchange_rates)
        // set listener on positive button
            .setPositiveButton(android.R.string.ok,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            updateExchangeRates();
                        }
                    })
        // set listener on negative button
            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
        // create dialog and show
        alertDialog.create().show();
    }

    /**
     * Import all currencies from Android System
     */
    public void importAllCurrencies() {
        AsyncTask<Void, Void, Boolean> asyncTask = new ImportAllCurrenciesTask(getActivity());
        asyncTask.execute();
    }

    public void updateExchangeRates() {
        // Update only the visible currencies.
        List<TableCurrencyFormats> currencies = getVisibleCurrencies();

        // updateAllExchangeRatesFromYahoo();
        updateExchangeRatesFromYahoo(currencies);
    }

    private List<TableCurrencyFormats> getVisibleCurrencies() {
        CurrencyListAdapter adapter = (CurrencyListAdapter) getListAdapter();
        if (adapter == null) return null;

        Cursor cursor = adapter.getCursor();
        if (cursor == null) return null;

        cursor.moveToPosition(Constants.NOT_SET);
        List<TableCurrencyFormats> currencies = new ArrayList<>();

        while (cursor.moveToNext()) {
            TableCurrencyFormats currency = new TableCurrencyFormats();
            currency.setValueFromCursor(cursor);
            currencies.add(currency);
        }

        return currencies;
    }

    private CurrencyService getCurrencyUtils() {
        if(mCurrencyService == null) {
            mCurrencyService = new CurrencyService(getActivity().getApplicationContext());
        }
        return mCurrencyService;
    }

    /**
     * Update rate for the currently selected currency.
     */
    private void updateSingleCurrencyExchangeRate(final int currencyId) {
        updateCurrencyFromYahoo(currencyId);
    }

//    private void updateAllExchangeRatesFromYahoo(){
//        CurrencyService utils = getCurrencyUtils();
//        List<TableCurrencyFormats> currencies = utils.getAllCurrencyFormats();
//
//        updateExchangeRatesFromYahoo(currencies);
//    }

    private void updateExchangeRatesFromYahoo(List<TableCurrencyFormats> currencies){
        if (currencies == null || currencies.size() <= 0) return;

        CurrencyService utils = getCurrencyUtils();
        ArrayList<String> currencySymbols = new ArrayList<>();
        String symbol;
        String baseCurrencySymbol = utils.getBaseCurrencyCode();

        for (TableCurrencyFormats currency : currencies) {
            symbol = currency.getCurrencySymbol();
            if (symbol == null) continue;
            if (symbol.equals(baseCurrencySymbol)) continue;

            currencySymbols.add(symbol + baseCurrencySymbol + "=X");
        }

        ISecurityPriceUpdater updater = SecurityPriceUpdaterFactory.getUpdaterInstance(getActivity(), this);
        updater.updatePrices(currencySymbols);
    }

    private boolean updateCurrencyFromYahoo(int toCurrencyId) {
        CurrencyService utils = getCurrencyUtils();

        List<TableCurrencyFormats> currencies = new ArrayList<>();
        currencies.add(utils.getCurrency(toCurrencyId));

        updateExchangeRatesFromYahoo(currencies);

        return true;
    }

    private void loadData() {
        getLoaderManager().initLoader(ID_LOADER_CURRENCY, null, this);
    }

    private void reloadData() {
        getLoaderManager().restartLoader(ID_LOADER_CURRENCY, null, this);
    }
}
