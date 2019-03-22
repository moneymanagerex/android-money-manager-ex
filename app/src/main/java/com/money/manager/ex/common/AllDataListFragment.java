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
package com.money.manager.ex.common;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
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

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.money.manager.ex.Constants;
import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.database.ITransactionEntity;
import com.money.manager.ex.datalayer.AccountTransactionRepository;
import com.money.manager.ex.datalayer.Select;
import com.money.manager.ex.datalayer.SplitCategoriesRepository;
import com.money.manager.ex.domainmodel.AccountTransaction;
import com.money.manager.ex.domainmodel.SplitCategory;
import com.money.manager.ex.sync.SyncManager;
import com.money.manager.ex.transactions.CheckingTransactionEditActivity;
import com.money.manager.ex.R;
import com.money.manager.ex.servicelayer.qif.QifExport;
import com.money.manager.ex.transactions.EditTransactionActivityConstants;
import com.money.manager.ex.search.SearchActivity;
import com.money.manager.ex.adapter.AllDataAdapter;
import com.money.manager.ex.adapter.AllDataAdapter.TypeCursor;
import com.money.manager.ex.home.DrawerMenuItem;
import com.money.manager.ex.home.DrawerMenuItemAdapter;
import com.money.manager.ex.core.ExportToCsvFile;
import com.money.manager.ex.database.QueryAllData;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

import androidx.cursoradapter.widget.CursorAdapter;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
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

    public static AllDataListFragment newInstance(int accountId) {
        return newInstance(accountId, true);
    }

    /**
     * Create a new instance of AllDataListFragment with accountId params
     *
     * @param accountId Id of account to display. If generic shown set -1
     * @return new instance AllDataListFragment
     */
    public static AllDataListFragment newInstance(int accountId, boolean showFloatingButton) {
        AllDataListFragment fragment = new AllDataListFragment();

        Bundle args = new Bundle();
        args.putInt(ARG_ACCOUNT_ID, accountId);
        args.putBoolean(ARG_SHOW_FLOATING_BUTTON, showFloatingButton);
        fragment.setArguments(args);

        return fragment;
    }

    public static final int ID_LOADER_ALL_DATA_DETAIL = 1;

    public static final String KEY_ARGUMENTS_WHERE = "SearchResultFragment:ArgumentsWhere";
    public static final String KEY_ARGUMENTS_SORT = "SearchResultFragment:ArgumentsSort";

    public int AccountId = Constants.NOT_SET;
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
        this.AccountId = getArguments().getInt(ARG_ACCOUNT_ID);

        // Read header indicator directly from the activity.
        // todo: make this a parameter or a property.
        if (getActivity() instanceof SearchActivity) {
            SearchActivity activity = (SearchActivity) getActivity();
            setShownHeader(activity.ShowAccountHeaders);
        }

        // create adapter for data.
        AllDataAdapter adapter = new AllDataAdapter(getActivity(), null, TypeCursor.ALLDATA);
        adapter.setAccountId(this.AccountId);
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
                        startEditAccountTransactionActivity(cursor.getInt(cursor.getColumnIndex(QueryAllData.ID)));
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

        switch (id) {
            case ID_LOADER_ALL_DATA_DETAIL:
                // compose selection and sort
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
                QueryAllData allData = new QueryAllData(getActivity());
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

        switch (loader.getId()) {
            case ID_LOADER_ALL_DATA_DETAIL:
                // Transactions list loaded.
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

        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
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
        int itemId = item.getItemId();

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
        ArrayList<Integer> transIds = getTransactionIds();

        showDialogDeleteCheckingAccount(transIds);
    }

    @Override
    public void onChangeTransactionStatusClicked() {
        ArrayList<Integer> transIds = getTransactionIds();
        changeTransactionStatus(transIds);
    }

    @Override
    public void onTransactionStatusClicked(String status) {
        ArrayList<Integer> transIds = getTransactionIds();

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
        ArrayList<Integer> transIds = getTransactionIds();
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

    public void displayRunningBalances(HashMap<Integer, Money> balances) {
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
            adapter.setAccountId(this.AccountId);
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

        TextView txtColumn1 = (TextView) footer.findViewById(R.id.textViewColumn1);
        TextView txtColumn2 = (TextView) footer.findViewById(R.id.textViewColumn2);

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
         display = Integer.toString(data.getCount()) + " " + getString(R.string.records) + ", ";

        // sum

        Money total = MoneyFactory.fromString("0");

        if (data.getCount() != 0) {
            total = getTotalFromCursor(data);
        }

        TextView txtColumn2 = (TextView) this.footer.findViewById(R.id.textViewColumn2);

        CurrencyService currencyService = new CurrencyService(getContext());
        display += currencyService.getBaseCurrencyFormatted(total);

        txtColumn2.setText(display);
    }

    private Money getTotalFromCursor(Cursor cursor) {
        Money total = MoneyFactory.fromString("0");
        int originalPosition = cursor.getPosition();
        AllDataAdapter adapter = getAllDataAdapter();
        CurrencyService currencyService = new CurrencyService(getContext());
        int baseCurrencyId = currencyService.getBaseCurrencyId();
        ContentValues values = new ContentValues();

        int currencyId;
        Money amount;
        Money converted;
        String transType;
        TransactionTypes transactionType;

        cursor.moveToPosition(Constants.NOT_SET);

        while(cursor.moveToNext()) {
            values.clear();

            // Read needed data.
            DatabaseUtils.cursorStringToContentValues(cursor, adapter.TRANSACTIONTYPE, values);
            DatabaseUtils.cursorIntToContentValues(cursor, adapter.CURRENCYID, values);
            DatabaseUtils.cursorIntToContentValues(cursor, adapter.TOCURRENCYID, values);
            DatabaseUtils.cursorDoubleToCursorValues(cursor, adapter.AMOUNT, values);
            DatabaseUtils.cursorDoubleToCursorValues(cursor, adapter.TOAMOUNT, values);

            transType = values.getAsString(adapter.TRANSACTIONTYPE);
            transactionType = TransactionTypes.valueOf(transType);

            if (transactionType.equals(TransactionTypes.Transfer)) {
                currencyId = values.getAsInteger(adapter.TOCURRENCYID);
                amount = MoneyFactory.fromString(values.getAsString(adapter.TOAMOUNT));
            } else {
                currencyId = values.getAsInteger(adapter.CURRENCYID);
                amount = MoneyFactory.fromString(values.getAsString(adapter.AMOUNT));
            }

            converted = currencyService.doCurrencyExchange(baseCurrencyId, amount, currencyId);
            total = total.add(converted);
        }

        cursor.moveToPosition(originalPosition);

        return total;
    }

    private boolean setStatusCheckingAccount(int[] transId, String status) {
        // check if status = "U" convert to empty string
        if (TextUtils.isEmpty(status) || "U".equalsIgnoreCase(status)) status = "";

        SyncManager sync = new SyncManager(getActivity());
        // Pause synchronization while bulk processing.
        sync.disableAutoUpload();

        for (int id : transId) {
            // content value for updates
            ContentValues values = new ContentValues();
            // set new state
            values.put(ITransactionEntity.STATUS, status.toUpperCase());

            AccountTransactionRepository repo = new AccountTransactionRepository(getActivity());

            // update
            int updateResult = getActivity().getContentResolver().update(repo.getUri(),
                    values,
                    AccountTransaction.TRANSID + "=?",
                    new String[]{Integer.toString(id)});
            if (updateResult <= 0) {
                Toast.makeText(getActivity(), R.string.db_update_failed, Toast.LENGTH_LONG).show();

                sync.enableAutoUpload();
                sync.dataChanged();

                return false;
            }
        }

        // Now notify Dropbox about modifications.
        sync.enableAutoUpload();
        sync.dataChanged();

        return true;
    }

    /**
     * @param transactionIds primary key of transaction
     */
    private void showDialogDeleteCheckingAccount(final ArrayList<Integer> transactionIds) {
        // create alert binaryDialog and set title and message
        MaterialDialog.Builder alertDialog = new MaterialDialog.Builder(getContext())
            .title(R.string.delete_transaction)
            .icon(new UIHelper(getActivity()).getIcon(GoogleMaterial.Icon.gmd_warning))
            .content(getResources().getQuantityString(R.plurals.plurals_delete_transactions,
                    transactionIds.size(), transactionIds.size()));
//        alert.setIcon(R.drawable.ic_action_warning_light);

        // set listener button positive
        alertDialog.positiveText(android.R.string.ok);
        alertDialog.onPositive(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                SyncManager sync = new SyncManager(getActivity());

                // Pause sync notification while bulk processing.
                sync.disableAutoUpload();

                for (int transactionId : transactionIds) {
                    // First delete any splits. See if there are any split records.
                    SplitCategoriesRepository splitRepo = new SplitCategoriesRepository(getActivity());
                    Cursor curSplit = getActivity().getContentResolver().query(splitRepo.getUri(), null,
                            SplitCategory.TRANSID + "=" + Integer.toString(transactionId),
                            null, SplitCategory.SPLITTRANSID);
                    int splitCount = curSplit.getCount();
                    curSplit.close();

                    if (splitCount > 0) {
                        int deleteResult = getActivity().getContentResolver().delete(splitRepo.getUri(),
                                SplitCategory.TRANSID + "=?",
                                new String[]{Integer.toString(transactionId)});
                        if (deleteResult != splitCount) {
                            Toast.makeText(getActivity(), R.string.db_delete_failed, Toast.LENGTH_SHORT).show();

                            // Now notify Dropbox about modifications.
                            sync.enableAutoUpload();
                            sync.dataChanged();

                            return;
                        }
                    }

                    // Delete the transaction.

                    AccountTransactionRepository repo = new AccountTransactionRepository(getActivity());

                    int deleteResult = getActivity().getContentResolver().delete(repo.getUri(),
                            AccountTransaction.TRANSID + "=?",
                            new String[]{Integer.toString(transactionId)});
                    if (deleteResult == 0) {
                        Toast.makeText(getActivity(), R.string.db_delete_failed, Toast.LENGTH_SHORT).show();

                        // Now notify Dropbox about modifications.
                        sync.enableAutoUpload();
                        sync.dataChanged();

                        return;
                    }
                }

                // Now notify Dropbox about modifications.
                sync.enableAutoUpload();
                sync.dataChanged();

                // restart loader
                loadData();
            }
        });
        // set listener negative button
        alertDialog.negativeText(android.R.string.cancel);
        alertDialog.onNegative(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                dialog.cancel();
            }
        });

        alertDialog.build().show();
    }

    /**
     * start the activity of transaction management
     *
     * @param transId null set if you want to do a new transaction, or transaction id
     */
    private void startEditAccountTransactionActivity(Integer transId) {
        // create intent, set Account ID
        Intent intent = new Intent(getActivity(), CheckingTransactionEditActivity.class);

        //Set the source
        intent.putExtra(EditTransactionActivityConstants.KEY_TRANS_SOURCE, "AllDataListFragment.java");

        // check transId not null
        if (transId != null) {
            intent.putExtra(EditTransactionActivityConstants.KEY_TRANS_ID, transId);
            intent.setAction(Intent.ACTION_EDIT);
        } else {
            intent.putExtra(EditTransactionActivityConstants.KEY_ACCOUNT_ID, this.AccountId);
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

        int numRecords = adapter.getCount();
        for (int i = 0; i < numRecords; i++) {
            adapter.setPositionChecked(i, true);
        }

        adapter.notifyDataSetChanged();
    }

    private ArrayList<Integer> getTransactionIds(){
        final ArrayList<Integer> transIds = new ArrayList<>();

        AllDataAdapter adapter = getAllDataAdapter();
        if(adapter == null) return transIds;

        Cursor cursor = adapter.getCursor();
        if (cursor != null) {
            // get checked items & count from the adapter, not from the list view.
            // List view only contains the one that was tapped, ignoring the Select All.
//                SparseBooleanArray positionChecked = getListView().getCheckedItemPositions();
            SparseBooleanArray positionChecked = adapter.getPositionsChecked();
//                int checkedItemsCount = getListView().getCheckedItemCount();
            int checkedItemsCount = positionChecked.size();

            for (int i = 0; i < checkedItemsCount; i++) {
                int position = positionChecked.keyAt(i);
                // This screws up the selection?
//                    if (getListHeader() != null)
//                        position--;
                if (cursor.moveToPosition(position)) {
                    transIds.add(cursor.getInt(cursor.getColumnIndex(QueryAllData.ID)));
                }
            }
        }

        return transIds;
    }

    private void changeTransactionStatus(final ArrayList<Integer> transIds){
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
        final MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .title(getString(R.string.change_status))
                .adapter(adapter, null)
                .build();

        ListView listView = dialog.getListView();
        if (listView != null) listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
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
        dialog.show();
    }

    private void showDuplicateTransactionView(ArrayList<Integer> transIds) {
        // validation
        int transactionCount = transIds.size();
        if (transactionCount <= 0) return;

        int[] ids = convertArrayListToArray(transIds);
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

    private int[] convertArrayListToArray(ArrayList<Integer> list) {
        int[] result = new int[list.size()];
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
