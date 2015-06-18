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
package com.money.manager.ex.currency;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.money.manager.ex.R;
import com.money.manager.ex.adapter.MoneySimpleCursorAdapter;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.database.TableAccountList;
import com.money.manager.ex.database.TableCurrencyFormats;
import com.money.manager.ex.database.TablePayee;
import com.money.manager.ex.dropbox.DropboxHelper;
import com.money.manager.ex.fragment.BaseListFragment;
import com.money.manager.ex.utils.ActivityUtils;
import com.money.manager.ex.utils.CurrencyUtils;

import java.util.List;

/**
 *  Currency list.
 */
public class CurrencyFormatsLoaderListFragment
        extends BaseListFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    public String mAction = Intent.ACTION_EDIT;

    // Store previous device orientation when showing other screens (chart, etc.)
    public int PreviousOrientation = -1;

    // ID loader
    private static final int ID_LOADER_CURRENCY = 0;
    // filter
    private String mCurFilter;
    private int mLayout;
    // database table
    private static TableCurrencyFormats mCurrency = new TableCurrencyFormats();
    private CurrencyUtils mCurrencyUtils;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // show search into actionbar
        setShowMenuItemSearch(true);

        setEmptyText(getActivity().getResources().getString(R.string.account_empty_list));
        setHasOptionsMenu(true);

//        if (Intent.ACTION_PICK.equals(mAction)) {
//            mLayout = android.R.layout.simple_list_item_multiple_choice;
//        } else {
//            mLayout = android.R.layout.simple_list_item_1;
//        }
        // Always use simple list layout.
        mLayout = android.R.layout.simple_list_item_1;

        // associate adapter
        MoneySimpleCursorAdapter adapter = new MoneySimpleCursorAdapter(getActivity(), mLayout, null,
                new String[]{TableCurrencyFormats.CURRENCYNAME}, new int[]{android.R.id.text1}, 0);
        setListAdapter(adapter);

        registerForContextMenu(getListView());
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        setListShown(false);
        getLoaderManager().initLoader(ID_LOADER_CURRENCY, null, this);

        // set icon searched
        setMenuItemSearchIconified(!Intent.ACTION_PICK.equals(mAction));
        setFloatingActionButtonVisible(true);
        setFloatingActionButtonAttachListView(true);
    }

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        // take cursor and move to position
        Cursor cursor = ((SimpleCursorAdapter) getListAdapter()).getCursor();
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
                CurrencyUtils currencyUtils = this.getCurrencyUtils();
                String baseCurrencyCode = currencyUtils.getBaseCurrencyCode();
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
                    new AlertDialogWrapper.Builder(getActivity())
                        .setTitle(R.string.attention)
                        .setMessage(R.string.currency_can_not_deleted)
                        .setIcon(R.drawable.ic_action_warning_light)
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

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        // take cursor and move into position
        Cursor cursor = ((SimpleCursorAdapter) getListAdapter()).getCursor();
        cursor.moveToPosition(info.position);
        // set currency name
        menu.setHeaderTitle(cursor.getString(cursor.getColumnIndex(TableCurrencyFormats.CURRENCYNAME)));

        // compose context menu
        String[] menuItems = getResources().getStringArray(R.array.context_menu_currencies);
        for (int i = 0; i < menuItems.length; i++) {
            menu.add(Menu.NONE, i, i, menuItems[i]);
        }
    }

    // Loader event handlers.

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case ID_LOADER_CURRENCY:
                String whereClause = null;
                String selectionArgs[] = null;
                if (!TextUtils.isEmpty(mCurFilter)) {
                    whereClause = TableCurrencyFormats.CURRENCYNAME + " LIKE ?";
                    selectionArgs = new String[]{mCurFilter + "%"};
                }
                return new CursorLoader(getActivity(), mCurrency.getUri(), mCurrency.getAllColumns(),
                        whereClause, selectionArgs, "upper(" + TableCurrencyFormats.CURRENCYNAME + ")");
        }

        return null;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case ID_LOADER_CURRENCY:
                // ((SimpleCursorAdapter)getListAdapter()).swapCursor(null);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case ID_LOADER_CURRENCY:
                MoneySimpleCursorAdapter adapter = (MoneySimpleCursorAdapter) getListAdapter();
                adapter.setHighlightFilter(mCurFilter != null ? mCurFilter.replace("%", "") : "");
                adapter.swapCursor(data);

                if (isResumed()) {
                    setListShown(true);
                    if (data.getCount() <= 0 && getFloatingActionButton() != null)
                        getFloatingActionButton().show(true);
                } else {
                    setListShownNoAnimation(true);
                }
        }
    }

    // End loader event handlers.

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_currency_formats_list_activity, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_import_all_currencies:
                showDialogImportAllCurrencies();
                return true;

            case R.id.menu_update_exchange_rate:
                showDialogUpdateExchangeRateCurrencies();
        }
        return super.onOptionsItemSelected(item);
    }

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
        Intent result = null;
        if (Intent.ACTION_PICK.equals(mAction)) {
            // create intent
            Cursor cursor = ((SimpleCursorAdapter) getListAdapter()).getCursor();

            for (int i = 0; i < getListView().getCount(); i++) {
                if (getListView().isItemChecked(i)) {
                    cursor.moveToPosition(i);

                    result = new Intent();
                    result.putExtra(CurrencyFormatsListActivity.INTENT_RESULT_CURRENCYID,
                            cursor.getInt(cursor.getColumnIndex(TableCurrencyFormats.CURRENCYID)));
                    result.putExtra(CurrencyFormatsListActivity.INTENT_RESULT_CURRENCYNAME,
                            cursor.getString(cursor.getColumnIndex(TableCurrencyFormats.CURRENCYNAME)));

                    getActivity().setResult(Activity.RESULT_OK, result);

                    return;
                }
            }
        }
        getActivity().setResult(CurrencyFormatsListActivity.RESULT_CANCELED);
    }

    private void showDialogDeleteCurrency(final int currencyId) {
        // config alert dialog
        AlertDialogWrapper.Builder alertDialog = new AlertDialogWrapper.Builder(getActivity());
        alertDialog.setTitle(R.string.delete_currency);
        alertDialog.setMessage(R.string.confirmDelete);
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
                        getLoaderManager().restartLoader(ID_LOADER_CURRENCY, null, CurrencyFormatsLoaderListFragment.this);
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
        Intent intent = new Intent(getActivity(), CurrencyFormatsActivity.class);
        // check transId not null
        if (currencyId != null) {
            intent.putExtra(CurrencyFormatsActivity.KEY_CURRENCY_ID, currencyId);
            intent.setAction(Intent.ACTION_EDIT);
        } else {
            intent.setAction(Intent.ACTION_INSERT);
        }
        // launch activity
        startActivity(intent);
    }

    @Override
    public String getSubTitle() {
        return getString(R.string.currencies);
    }

    private void showDialogImportAllCurrencies() {
        // config alert dialog
        AlertDialogWrapper.Builder alertDialog = new AlertDialogWrapper.Builder(getActivity());
        alertDialog.setTitle(R.string.attention);
        alertDialog.setMessage(R.string.question_import_currencies);
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
        AlertDialogWrapper.Builder alertDialog = new AlertDialogWrapper.Builder(getActivity());
        alertDialog.setTitle(R.string.attention);
        alertDialog.setMessage(R.string.question_update_currency_exchange_rates);
        // set listener on positive button
        alertDialog.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        updateRateCurrencies();
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

    /**
     * Import all currencies from Android System
     */
    public void importAllCurrencies() {
        AsyncTask<Void, Void, Boolean> asyncTask = new AsyncTask<Void, Void, Boolean>() {
            ProgressDialog dialog = null;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                dialog = ProgressDialog.show(getActivity(), null, getString(R.string.import_currencies_in_progress));
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                Core core = new Core(getActivity().getApplicationContext());
                return core.importCurrenciesFromLocaleAvaible();
            }

            @Override
            protected void onPostExecute(Boolean result) {
                try {
                    if (dialog != null)
                        dialog.hide();
                } catch (Exception e) {
                    Log.e(CurrencyFormatsListActivity.LOGCAT, e.getMessage());
                }
                super.onPostExecute(result);
            }
        };
        asyncTask.execute();
    }

    public void updateRateCurrencies() {
        AsyncTask<Void, Integer, Boolean> asyncTask = new AsyncTask<Void, Integer, Boolean>() {
            private ProgressDialog dialog = null;
            private int mCountCurrencies = 0;
            private TableCurrencyFormats mCurrencyFormat;

            private Core mCore;

            private int mPrevOrientation;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                mPrevOrientation = ActivityUtils.forceCurrentOrientation(getActivity());

                mCore = new Core(getActivity().getApplicationContext());
                DropboxHelper.setAutoUploadDisabled(true);

                //getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

                dialog = new ProgressDialog(getActivity());
                dialog.setMessage(getString(R.string.start_currency_exchange_rates));
                dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                CurrencyUtils currencyUtils = getCurrencyUtils();
                List<TableCurrencyFormats> currencyFormats = currencyUtils.getAllCurrencyFormats();
                mCountCurrencies = currencyFormats.size();
                for (int i = 0; i < currencyFormats.size(); i++) {
                    mCurrencyFormat = currencyFormats.get(i);
                    currencyUtils.updateCurrencyRateFromBase(mCurrencyFormat.getCurrencyId());
                    publishProgress(i);
                }
                return Boolean.TRUE;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                if (dialog != null) {
                    dialog.setMax(mCountCurrencies);
                    dialog.setProgress(values[0]);
                    if (mCurrencyFormat != null) {
                        dialog.setMessage(mCore.highlight(mCurrencyFormat.getCurrencyName(), getString(R.string.update_currency_exchange_rates, mCurrencyFormat.getCurrencyName())));
                    }
                }
            }

            @Override
            protected void onPostExecute(Boolean result) {
                try {
                    if (dialog != null)
                        dialog.hide();
                } catch (Exception e) {
                    Log.e(CurrencyFormatsListActivity.LOGCAT, e.getMessage());
                }
                if (result)
                    Toast.makeText(getActivity(), R.string.success_currency_exchange_rates, Toast.LENGTH_LONG).show();

                DropboxHelper.setAutoUploadDisabled(false);
                DropboxHelper.notifyDataChanged();

                ActivityUtils.restoreOrientation(getActivity(), mPrevOrientation);

                super.onPostExecute(result);
            }
        };
        asyncTask.execute();
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

    private CurrencyUtils getCurrencyUtils() {
        if(mCurrencyUtils == null) {
            mCurrencyUtils = new CurrencyUtils(getActivity().getApplicationContext());
        }
        return mCurrencyUtils;
    }

    /**
     * Update rate for the currently selected currency.
     */
    private void updateSingleCurrencyExchangeRate(final int currencyId) {

        AsyncTask<Void, Integer, Boolean> updateAsync = new AsyncTask<Void, Integer, Boolean>() {
            private ProgressDialog dialog = null;
            private int mPrevOrientation;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                mPrevOrientation = ActivityUtils.forceCurrentOrientation(getActivity());

                DropboxHelper.setAutoUploadDisabled(true);

                dialog = new ProgressDialog(getActivity());
                // setting dialog
                // update_menu_currency_exchange_rates
                dialog.setMessage(getString(R.string.start_currency_exchange_rates));
                dialog.setIndeterminate(true);
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
                // show dialog
                dialog.show();
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                CurrencyUtils util = getCurrencyUtils();
                util.updateCurrencyRateFromBase(currencyId);
                return Boolean.TRUE;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                try {
                    if (dialog != null)
                        dialog.hide();
                } catch (Exception e) {
                    Log.e(CurrencyFormatsListActivity.LOGCAT, e.getMessage());
                }
                if (result) {
                    Toast.makeText(getActivity(), R.string.success_currency_exchange_rates, Toast.LENGTH_LONG).show();
                }

                DropboxHelper.setAutoUploadDisabled(false);
                DropboxHelper.notifyDataChanged();

                ActivityUtils.restoreOrientation(getActivity(), mPrevOrientation);

                super.onPostExecute(result);
            }
        };
        updateAsync.execute();

    }
}
