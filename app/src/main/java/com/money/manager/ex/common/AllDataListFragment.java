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
package com.money.manager.ex.common;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.cursoradapter.widget.CursorAdapter;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.adapter.AllDataAdapter;
import com.money.manager.ex.adapter.AllDataAdapter.TypeCursor;
import com.money.manager.ex.core.ExportToCsvFile;
import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.database.Dataset;
import com.money.manager.ex.database.ITransactionEntity;
import com.money.manager.ex.database.QueryAllData;
import com.money.manager.ex.database.QueryMobileData;
import com.money.manager.ex.datalayer.AccountTransactionRepository;
import com.money.manager.ex.datalayer.Select;
import com.money.manager.ex.datalayer.SplitCategoryRepository;
import com.money.manager.ex.domainmodel.AccountTransaction;
import com.money.manager.ex.domainmodel.SplitCategory;
import com.money.manager.ex.home.DrawerMenuItem;
import com.money.manager.ex.home.DrawerMenuItemAdapter;
import com.money.manager.ex.search.SearchActivity;
import com.money.manager.ex.servicelayer.qif.QifExport;
import com.money.manager.ex.transactions.CheckingTransactionEditActivity;
import com.money.manager.ex.transactions.EditTransactionActivityConstants;

import java.util.ArrayList;
import java.util.HashMap;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;
import timber.log.Timber;

/**
 * Fragment that displays the transactions.
 */
public class AllDataListFragment
    extends BaseListFragment
    implements LoaderManager.LoaderCallbacks<Cursor>, IAllDataMultiChoiceModeListenerCallbacks {

    private static final String ARG_ACCOUNT_ID = "AccountId";
    private static final String ARG_SHOW_FLOATING_BUTTON = "ShowFloatingButton";

    public static AllDataListFragment newInstance(long accountId) {
        return newInstance(accountId, true);
    }

    /**
     * Create a new instance of AllDataListFragment with accountId params
     *
     * @param accountId Id of account to display. If generic shown set -1
     * @return new instance AllDataListFragment
     */
    public static AllDataListFragment newInstance(long accountId, boolean showFloatingButton) {
        AllDataListFragment fragment = new AllDataListFragment();

        Bundle args = new Bundle();
        args.putLong(ARG_ACCOUNT_ID, accountId);
        args.putBoolean(ARG_SHOW_FLOATING_BUTTON, showFloatingButton);
        fragment.setArguments(args);

        return fragment;
    }

    public static final int ID_LOADER_ALL_DATA_DETAIL = 1;

    public static final String KEY_ARGUMENTS_WHERE = "SearchResultFragment:ArgumentsWhere";
    public static final String KEY_ARGUMENTS_SORT = "SearchResultFragment:ArgumentsSort";

    public long accountId = Constants.NOT_SET;
    private LinearLayout footer;
    private LoaderManager.LoaderCallbacks<Cursor> mSearResultFragmentLoaderCallbacks;
    private boolean mAutoStarLoader = true;
    private boolean mShowHeader = false;
    private boolean mShowBalance = false;
    private AllDataMultiChoiceModeListener mMultiChoiceModeListener;
    private View mListHeader = null;
    private Bundle mArguments;
    private boolean mShowFooter = false;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText(getString(R.string.no_data));
        setListShown(false);

        // Read arguments
        this.accountId = getArguments().getLong(ARG_ACCOUNT_ID);

        // Read header indicator directly from the activity.
        // todo: make this a parameter or a property.
        if (getActivity() instanceof SearchActivity) {
            SearchActivity activity = (SearchActivity) getActivity();
            setShownHeader(activity.ShowAccountHeaders);
        }

        // create adapter for data.
        AllDataAdapter adapter = new AllDataAdapter(getActivity(), null, TypeCursor.ALLDATA);
        adapter.setAccountId(this.accountId);
        adapter.setShowAccountName(isShownHeader());
        adapter.setShowBalanceAmount(isShownBalance());

        // set multi-choice mode in the list view.
        mMultiChoiceModeListener = new AllDataMultiChoiceModeListener();
        mMultiChoiceModeListener.setListener(this);
        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        getListView().setMultiChoiceModeListener(mMultiChoiceModeListener);

        // e item click
        getListView().setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (getListAdapter() != null && getListAdapter() instanceof AllDataAdapter) {
                    Cursor cursor = ((AllDataAdapter) getListAdapter()).getCursor();
                    if (cursor.moveToPosition(position - (mListHeader != null ? 1 : 0))) {
                        startEditAccountTransactionActivity(cursor.getLong(cursor.getColumnIndex(QueryAllData.ID)));
                    }
                }
            }
        });

        // Header and footer must be added before setAdapter().

        // Add a header to the list view if one exists.
        if (getListAdapter() == null) {
            if (mListHeader != null)
                getListView().addHeaderView(mListHeader);
        }
        if (this.mShowFooter) {
            renderFooter();
        }

        // set adapter
        setListAdapter(adapter);

        // register context menu
        registerForContextMenu(getListView());

        // set animation progress
        setListShown(false);

        boolean showAddButton = getArguments().getBoolean(ARG_SHOW_FLOATING_BUTTON);
        if (showAddButton) {
            // Show floating action button.
            setFloatingActionButtonVisible(true);
            attachFloatingActionButtonToListView();
        }

        // start loader if asked to do so by the caller.
        if (isAutoStarLoader()) {
            loadData();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    // Loader event handlers

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (getSearchResultFragmentLoaderCallbacks() != null)
            getSearchResultFragmentLoaderCallbacks().onCreateLoader(id, args);
        //animation
        setListShown(false);

        if (id == ID_LOADER_ALL_DATA_DETAIL) {// compose selection and sort
            String selection = "";
            if (args != null && args.containsKey(KEY_ARGUMENTS_WHERE)) {
                selection = args.getString(KEY_ARGUMENTS_WHERE);
            }
//                String[] whereParams = new String[0];
//                if (args != null && args.containsKey(KEY_ARGUMENTS_WHERE_PARAMS)) {
//                    ArrayList<String> whereParamsList = args.getStringArrayList(KEY_ARGUMENTS_WHERE_PARAMS);
//                    whereParams = whereParamsList.toArray(whereParams);
//                }

            // set sort
            String sort = "";
            if (args != null && args.containsKey(KEY_ARGUMENTS_SORT)) {
                sort = args.getString(KEY_ARGUMENTS_SORT);
            }
            // create loader
            Dataset allData;
            if (args.containsKey(ARG_SHOW_FLOATING_BUTTON)) {
                // coming from report, use mobile data
                allData = new QueryMobileData(getActivity());
            } else {
                allData = new QueryAllData(getActivity());
            }
            Select query = new Select(allData.getAllColumns())
                    .where(selection)
                    .orderBy(sort);

            return new MmxCursorLoader(getActivity(), allData.getUri(), query);
        }
        return null;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        LoaderManager.LoaderCallbacks<Cursor> parent = getSearchResultFragmentLoaderCallbacks();
        if (parent != null) parent.onLoaderReset(loader);

        //((CursorAdapter) getListAdapter()).swapCursor(null);
        ((CursorAdapter) getListAdapter()).changeCursor(null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        LoaderManager.LoaderCallbacks<Cursor> parent = getSearchResultFragmentLoaderCallbacks();
        if (parent != null) parent.onLoadFinished(loader, data);

        if (loader.getId() == ID_LOADER_ALL_DATA_DETAIL) {// Transactions list loaded.
            AllDataAdapter adapter = (AllDataAdapter) getListAdapter();
//                adapter.swapCursor(data);
            adapter.changeCursor(data);
            if (isResumed()) {
                setListShown(true);
                if (data != null && data.getCount() <= 0 && getFloatingActionButton() != null)
                    getFloatingActionButton().show(true);
            } else {
                setListShownNoAnimation(true);
            }

            // reset the transaction groups (account name collection)
            adapter.resetAccountHeaderIndexes();

            // Show totals
            if (this.mShowFooter) {
                try {
                    this.updateFooter(data);
                } catch (Exception e) {
                    Timber.e(e, "displaying footer");
                }
            }
        }
    }

    // End loader event handlers

    /**
     * Add options to the action bar of the host activity.
     * This is not called in ActionBar Activity, i.e. Search.
     * @param menu
     * @param inflater
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        Activity activity = getActivity();
        if (activity == null) return;

        MenuItem itemExportToCsv = menu.findItem(R.id.menu_export_to_csv);
        if (itemExportToCsv != null) itemExportToCsv.setVisible(true);
        MenuItem itemSearch = menu.findItem(R.id.menu_search_transaction);

        if (itemSearch != null) {
            itemSearch.setVisible(!activity.getClass().getSimpleName()
                    .equals(SearchActivity.class.getSimpleName()));
        }

        // show this on all transactions lists later?
        // show this menu only when on Search Activity for now.
        if (activity.getClass().getSimpleName().equals(SearchActivity.class.getSimpleName())) {
            // Add default menu options. todo: check why this is executed twice.
            // Includes menu item for .qif export
            MenuItem qifExport = menu.findItem(R.id.menu_qif_export);
            if (qifExport == null) {
                inflater.inflate(R.menu.menu_alldata_operations, menu);
            }
        }
    }

    @Override
    public void onDestroy() {
        if (mMultiChoiceModeListener != null)
            mMultiChoiceModeListener.onDestroyActionMode(null);
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            MmxBaseFragmentActivity activity = (MmxBaseFragmentActivity) getActivity();
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
            Timber.e(e, "stopping the all-data fragment");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        long itemId = item.getItemId();

        if (itemId == R.id.menu_export_to_csv) {
            exportDataToCSVFile();
            return true;
        }
        if (itemId == R.id.menu_qif_export) {
            // export visible transactions.
            exportToQif();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public String getSubTitle() {
        return null;
    }

    @Override
    public void onFloatingActionButtonClicked() {
        startEditAccountTransactionActivity(null);
    }

    // Multi-choice-mode listener callback handlers.

    /**
     * handler for multi-choice-mode listener
     */
    @Override
    public void onMultiChoiceCreated(android.view.Menu menu) {
        getActivity().getMenuInflater().inflate(R.menu.menu_all_data_adapter, menu);
    }

    @Override
    public void onDestroyActionMode() {
        if (getListAdapter() != null && getListAdapter() instanceof AllDataAdapter) {
            AllDataAdapter adapter = (AllDataAdapter) getListAdapter();
            adapter.clearPositionChecked();
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDeleteClicked() {
        ArrayList<Long> transIds = getTransactionIds();

        showDialogDeleteCheckingAccount(transIds);
    }

    @Override
    public void onChangeTransactionStatusClicked() {
        ArrayList<Long> transIds = getTransactionIds();
        changeTransactionStatus(transIds);
    }

    @Override
    public void onTransactionStatusClicked(String status) {
        ArrayList<Long> transIds = getTransactionIds();

        if (setStatusCheckingAccount(convertArrayListToArray(transIds), status)) {
            ((AllDataAdapter) getListAdapter()).clearPositionChecked();
            loadData();
        }
    }

    @Override
    public void onSelectAllRecordsClicked() {
        selectAllRecords();
    }

    @Override
    public void onDuplicateTransactionsClicked() {
        ArrayList<Long> transIds = getTransactionIds();
        showDuplicateTransactionView(transIds);
    }

    @Override
    public void onItemCheckedStateChanged(int position, boolean checked) {
        if (getListHeader() != null) {
            position--;
        }

        if (getListAdapter() != null && getListAdapter() instanceof AllDataAdapter) {
            AllDataAdapter adapter = (AllDataAdapter) getListAdapter();
            adapter.setPositionChecked(position, checked);
            adapter.notifyDataSetChanged();
        }
    }

    // Methods

    public void displayRunningBalances(HashMap<Long, Money> balances) {
        AllDataAdapter adapter = getAllDataAdapter();
        if(adapter == null) return;

        adapter.setBalances(balances);
    }

    /**
     * Export data to CSV file
     */
    public void exportDataToCSVFile() {
        exportDataToCSVFile("");
    }

    /**
     * Export data to CSV file
     *
     * @param prefixName prefix for the file
     */
    public void exportDataToCSVFile(String prefixName) {
        ExportToCsvFile csv = new ExportToCsvFile(getActivity(), (AllDataAdapter) getListAdapter());
        csv.setPrefixName(prefixName);
        csv.execute();
    }

    /**
     * @return the mSearResultFragmentLoaderCallbacks
     */
    public LoaderManager.LoaderCallbacks<Cursor> getSearchResultFragmentLoaderCallbacks() {
        return mSearResultFragmentLoaderCallbacks;
    }

    /**
     * @param searResultFragmentLoaderCallbacks the searResultFragmentLoaderCallbacks to set
     */
    public void setSearResultFragmentLoaderCallbacks(LoaderManager.LoaderCallbacks<Cursor> searResultFragmentLoaderCallbacks) {
        this.mSearResultFragmentLoaderCallbacks = searResultFragmentLoaderCallbacks;
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

    /**
     * @return the mShowHeader
     */
    public boolean isShownHeader() {
        return mShowHeader;
    }

    /**
     * Start loader into fragment
     */
    public void loadData() {
        loadData(getLatestArguments());
    }

    public void loadData(Bundle arguments) {
        // set the account id in the data adapter
        AllDataAdapter adapter = getAllDataAdapter();
        if (adapter != null) {
            adapter.setAccountId(this.accountId);
            adapter.setBalances(null);
        }

        // set the current arguments / account id
        setLatestArguments(arguments);
        // reload data with the latest arguments.
        getLoaderManager().restartLoader(ID_LOADER_ALL_DATA_DETAIL, arguments, this);
    }

    /**
     * @param mShownHeader the mShowHeader to set
     */
    public void setShownHeader(boolean mShownHeader) {
        this.mShowHeader = mShownHeader;
    }

    public View getListHeader() {
        return mListHeader;
    }

    public void setListHeader(View mHeaderList) {
        this.mListHeader = mHeaderList;
    }

    /**
     * @return the mShowBalance
     */
    public boolean isShownBalance() {
        return mShowBalance;
    }

    /**
     * @param mShownBalance the mShowBalance to set
     */
    public void setShownBalance(boolean mShownBalance) {
        this.mShowBalance = mShownBalance;

        AllDataAdapter adapter = getAllDataAdapter();
        if (adapter == null) {
            return;
        }

        adapter.setShowBalanceAmount(mShownBalance);
    }

    public void showTotalsFooter() {
        this.mShowFooter = true;
    }

    // Private methods.

    private void renderFooter() {
        this.footer = (LinearLayout) View.inflate(getActivity(),
                R.layout.item_generic_report_2_columns, null);

        TextView txtColumn1 = footer.findViewById(R.id.textViewColumn1);
        TextView txtColumn2 = footer.findViewById(R.id.textViewColumn2);

        txtColumn1.setText(R.string.total);
        txtColumn1.setTypeface(null, Typeface.BOLD_ITALIC);
        txtColumn2.setText(R.string.total);
        txtColumn2.setTypeface(null, Typeface.BOLD_ITALIC);

        ListView listView = getListView();
        listView.addFooterView(footer);
    }

    private void updateFooter(Cursor data) {
        if (data == null) return;

        String display;

        // number of records
         display = data.getCount() + " " + getString(R.string.records) + ", ";

        // sum

        Money total = MoneyFactory.fromString("0");

        if (data.getCount() != 0) {
            total = getTotalFromCursor(data);
        }

        TextView txtColumn2 = this.footer.findViewById(R.id.textViewColumn2);

        CurrencyService currencyService = new CurrencyService(getContext());
        display += currencyService.getBaseCurrencyFormatted(total);

        txtColumn2.setText(display);
    }

    private Money getTotalFromCursor(Cursor cursor) {
        Money total = MoneyFactory.fromString("0");
        int originalPosition = cursor.getPosition();
        AllDataAdapter adapter = getAllDataAdapter();
        CurrencyService currencyService = new CurrencyService(getContext());
        long baseCurrencyId = currencyService.getBaseCurrencyId();
        ContentValues values = new ContentValues();

        long searchForAccount = accountId;

        long currencyId;
        Money amount;
        Money converted;
        String transType;
        TransactionTypes transactionType;

        cursor.moveToPosition(Constants.NOT_SET_INT);

        while(cursor.moveToNext()) {
            values.clear();

            // Read needed data.
            DatabaseUtils.cursorStringToContentValues(cursor, adapter.TRANSACTIONTYPE, values);
            DatabaseUtils.cursorLongToContentValues(cursor, adapter.CURRENCYID, values);
            DatabaseUtils.cursorLongToContentValues(cursor, adapter.TOCURRENCYID, values);
            DatabaseUtils.cursorDoubleToCursorValues(cursor, adapter.AMOUNT, values);
            DatabaseUtils.cursorDoubleToCursorValues(cursor, adapter.TOAMOUNT, values);

            DatabaseUtils.cursorStringToContentValues(cursor, adapter.STATUS, values);
            if ( values.getAsString(adapter.STATUS).equalsIgnoreCase("V")) {
                // void. skip
                continue;
            }
            DatabaseUtils.cursorLongToContentValues(cursor, adapter.ACCOUNTID, values);

            transType = values.getAsString(adapter.TRANSACTIONTYPE);
            transactionType = TransactionTypes.valueOf(transType);

            if (transactionType.equals(TransactionTypes.Transfer)) {
                currencyId = values.getAsLong(adapter.TOCURRENCYID);
                // Issue 2054 adapt sign based on current account and direction
                // check mArguments(Account)
                if ( searchForAccount == Constants.NOT_SET ) {
                    // ignore transaction since this as + and -
                    continue;
                } else if ( searchForAccount == values.getAsLong(adapter.ACCOUNTID) ) {
                    // source
                    amount = MoneyFactory.fromString(values.getAsString(adapter.AMOUNT));
                } else {
                    // Dest
                    amount = MoneyFactory.fromString(values.getAsString(adapter.TOAMOUNT));
                }
            } else {
                currencyId = values.getAsLong(adapter.CURRENCYID);
                amount = MoneyFactory.fromString(values.getAsString(adapter.AMOUNT));
            }

            converted = currencyService.doCurrencyExchange(baseCurrencyId, amount, currencyId);
            total = total.add(converted);
        }

        cursor.moveToPosition(originalPosition);

        return total;
    }

    private boolean setStatusCheckingAccount(long[] transId, String status) {
        // check if status = "U" convert to empty string
        if (TextUtils.isEmpty(status) || "U".equalsIgnoreCase(status)) status = "";

        for (long id : transId) {
            // content value for updates
            ContentValues values = new ContentValues();
            // set new state
            values.put(ITransactionEntity.STATUS, status.toUpperCase());

            AccountTransactionRepository repo = new AccountTransactionRepository(getActivity());

            // update
            long updateResult = getActivity().getContentResolver().update(repo.getUri(),
                    values,
                    AccountTransaction.TRANSID + "=?",
                    new String[]{Long.toString(id)});
            if (updateResult <= 0) {
                Toast.makeText(getActivity(), R.string.db_update_failed, Toast.LENGTH_LONG).show();

                return false;
            }
        }

        return true;
    }

    /**
     * @param transactionIds primary key of transaction
     */
    private void showDialogDeleteCheckingAccount(final ArrayList<Long> transactionIds) {
        // create alert binaryDialog and set title and message
        UIHelper ui = new UIHelper(getActivity());

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.delete_transaction)
                .setIcon(ui.getIcon(GoogleMaterial.Icon.gmd_warning))
                .setMessage(getResources().getQuantityString(R.plurals.plurals_delete_transactions,
                        transactionIds.size(), transactionIds.size()))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        for (long transactionId : transactionIds) {
                            // First delete any splits. See if there are any split records.
                            SplitCategoryRepository splitRepo = new SplitCategoryRepository(getActivity());
                            Cursor curSplit = getActivity().getContentResolver().query(splitRepo.getUri(), null,
                                    SplitCategory.TRANSID + "=" + transactionId,
                                    null, SplitCategory.SPLITTRANSID);
                            long splitCount = curSplit.getCount();
                            curSplit.close();

                            if (splitCount > 0) {
                                long deleteResult = getActivity().getContentResolver().delete(splitRepo.getUri(),
                                        SplitCategory.TRANSID + "=?",
                                        new String[]{Long.toString(transactionId)});
                                if (deleteResult != splitCount) {
                                    Toast.makeText(getActivity(), R.string.db_delete_failed, Toast.LENGTH_SHORT).show();

                                    return;
                                }
                            }

                            // Delete the transaction.

                            AccountTransactionRepository repo = new AccountTransactionRepository(getActivity());

                            long deleteResult = getActivity().getContentResolver().delete(repo.getUri(),
                                    AccountTransaction.TRANSID + "=?",
                                    new String[]{Long.toString(transactionId)});
                            if (deleteResult == 0) {
                                Toast.makeText(getActivity(), R.string.db_delete_failed, Toast.LENGTH_SHORT).show();

                                return;
                            }
                        }

                        // restart loader
                        loadData();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();
    }

    /**
     * start the activity of transaction management
     *
     * @param transId null set if you want to do a new transaction, or transaction id
     */
    private void startEditAccountTransactionActivity(Long transId) {
        // create intent, set Account ID
        Intent intent = new Intent(getActivity(), CheckingTransactionEditActivity.class);

        //Set the source
        intent.putExtra(EditTransactionActivityConstants.KEY_TRANS_SOURCE, "AllDataListFragment.java");

        // check transId not null
        if (transId != null) {
            intent.putExtra(EditTransactionActivityConstants.KEY_TRANS_ID, transId);
            intent.setAction(Intent.ACTION_EDIT);
        } else {
            intent.putExtra(EditTransactionActivityConstants.KEY_ACCOUNT_ID, this.accountId);
            intent.setAction(Intent.ACTION_INSERT);
        }
        // launch activity
        startActivity(intent);
    }

    private AllDataAdapter getAllDataAdapter() {
        AllDataAdapter adapter = null;

        ListAdapter listAdapter = getListAdapter();
        if (listAdapter != null && listAdapter instanceof AllDataAdapter) {
            adapter = (AllDataAdapter) getListAdapter();
        }

        return adapter;
    }

    private void selectAllRecords() {
        AllDataAdapter adapter = getAllDataAdapter();
        if(adapter == null) return;

        // Clear selection first.
        adapter.clearPositionChecked();

        long numRecords = adapter.getCount();
        for (int i = 0; i < numRecords; i++) {
            adapter.setPositionChecked(i, true);
        }

        adapter.notifyDataSetChanged();
    }

    private ArrayList<Long> getTransactionIds(){
        final ArrayList<Long> transIds = new ArrayList<>();

        AllDataAdapter adapter = getAllDataAdapter();
        if(adapter == null) return transIds;

        Cursor cursor = adapter.getCursor();
        if (cursor != null) {
            // get checked items & count from the adapter, not from the list view.
            // List view only contains the one that was tapped, ignoring the Select All.
//                SparseBooleanArray positionChecked = getListView().getCheckedItemPositions();
            SparseBooleanArray positionChecked = adapter.getPositionsChecked();
//                long checkedItemsCount = getListView().getCheckedItemCount();
            long checkedItemsCount = positionChecked.size();

            for (int i = 0; i < checkedItemsCount; i++) {
                int position = positionChecked.keyAt(i);
                // This screws up the selection?
//                    if (getListHeader() != null)
//                        position--;
                if (cursor.moveToPosition(position)) {
                    transIds.add(cursor.getLong(cursor.getColumnIndex(QueryAllData.ID)));
                }
            }
        }

        return transIds;
    }

    private void changeTransactionStatus(final ArrayList<Long> transIds){
        final DrawerMenuItemAdapter adapter = new DrawerMenuItemAdapter(getActivity());
//        final Core core = new Core(getActivity().getApplicationContext());
        final Boolean isDarkTheme = new UIHelper(getActivity()).isUsingDarkTheme();

        // add status
        adapter.add(new DrawerMenuItem().withId(R.id.menu_none)
                .withText(getString(R.string.status_none))
                .withIcon(isDarkTheme ? R.drawable.ic_action_help_dark : R.drawable.ic_action_help_light)
                .withShortcut(""));
        adapter.add(new DrawerMenuItem().withId(R.id.menu_reconciled)
                .withText(getString(R.string.status_reconciled))
                .withIcon(isDarkTheme ? R.drawable.ic_action_done_dark : R.drawable.ic_action_done_light)
                .withShortcut("R"));
        adapter.add(new DrawerMenuItem().withId(R.id.menu_follow_up)
                .withText(getString(R.string.status_follow_up))
                .withIcon(isDarkTheme ? R.drawable.ic_action_alarm_on_dark : R.drawable.ic_action_alarm_on_light)
                .withShortcut("F"));
        adapter.add(new DrawerMenuItem().withId(R.id.menu_duplicate)
                .withText(getString(R.string.status_duplicate))
                .withIcon(isDarkTheme ? R.drawable.ic_action_copy_dark : R.drawable.ic_action_copy_light)
                .withShortcut("D"));
        adapter.add(new DrawerMenuItem().withId(R.id.menu_void)
                .withText(getString(R.string.status_void))
                .withIcon(isDarkTheme ? R.drawable.ic_action_halt_dark : R.drawable.ic_action_halt_light)
                .withShortcut("V"));

        // open binaryDialog
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.change_status));

        // Set the adapter
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int position) {
                DrawerMenuItem item = adapter.getItem(position);
                switch (item.getId()) {
                    case R.id.menu_none:
                    case R.id.menu_reconciled:
                    case R.id.menu_follow_up:
                    case R.id.menu_duplicate:
                    case R.id.menu_void:
                        String status = item.getShortcut();
                        if (setStatusCheckingAccount(convertArrayListToArray(transIds), status)) {
                            ((AllDataAdapter) getListAdapter()).clearPositionChecked();
                            loadData();
                        }
                }
                dialog.dismiss();
            }
        });

        // Create and show the AlertDialog
        final AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showDuplicateTransactionView(ArrayList<Long> transIds) {
        // validation
        int transactionCount = transIds.size();
        if (transactionCount <= 0) return;

        long[] ids = convertArrayListToArray(transIds);
        Intent[] intents = new Intent[transactionCount];
        for (int i = 0; i < transactionCount; i++) {
            intents[i] = new Intent(getActivity(), CheckingTransactionEditActivity.class);
            intents[i].putExtra(EditTransactionActivityConstants.KEY_TRANS_ID, ids[i]);
            intents[i].setAction(Intent.ACTION_PASTE);
            intents[i].putExtra(EditTransactionActivityConstants.KEY_TRANS_SOURCE, "AllDataListFragment.java");
        }
        getActivity().startActivities(intents);
    }

    // end multi-choice-mode listener callback handlers.

    private void exportToQif(){
        AllDataAdapter adapter = (AllDataAdapter) getListAdapter();
        QifExport qif = new QifExport(getActivity());
        qif.export(adapter);
    }

    private long[] convertArrayListToArray(ArrayList<Long> list) {
        long[] result = new long[list.size()];
        for (int i = 0; i < list.size(); i++) {
            result[i] = list.get(i);
        }
        return result;
    }

    /**
     * Returns the latest-set arguments. This is because the original arguments, when the
     * fragment was created, can not be altered.
     * But, when an account changes, we need to modify them. The new arguments are passed
     * through the call to loadData().
     * @return
     */
    private Bundle getLatestArguments() {
        if (mArguments == null) {
            mArguments = getArguments();
        }
        return mArguments;
    }

    private void setLatestArguments(Bundle arguments) {
        mArguments = arguments;
    }

}
