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
package com.money.manager.ex.fragment;

import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.MaterialDialog;
import com.money.manager.ex.CheckingAccountActivity;
import com.money.manager.ex.R;
import com.money.manager.ex.businessobjects.qif.QifExport;
import com.money.manager.ex.checkingaccount.CheckingAccountConstants;
import com.money.manager.ex.interfaces.IAllDataFragmentCallbacks;
import com.money.manager.ex.search.SearchActivity;
import com.money.manager.ex.adapter.AllDataAdapter;
import com.money.manager.ex.adapter.AllDataAdapter.TypeCursor;
import com.money.manager.ex.adapter.DrawerMenuItem;
import com.money.manager.ex.adapter.DrawerMenuItemAdapter;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.ExportToCsvFile;
import com.money.manager.ex.database.QueryAllData;
import com.money.manager.ex.database.TableCheckingAccount;
import com.money.manager.ex.database.TableSplitTransactions;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class AllDataFragment extends BaseListFragment
        implements LoaderCallbacks<Cursor>, IAllDataMultiChoiceModeListenerCallbacks {

    /**
     * Create a new instance of AllDataFragment with accountId params
     *
     * @param accountId Id of account to display. If generic shown set -1
     * @return new instance AllDataFragment
     */
    public static AllDataFragment newInstance(int accountId, IAllDataFragmentCallbacks callbacks) {
        AllDataFragment fragment = new AllDataFragment();
        fragment.AccountId = accountId;
        fragment.mCallbacks = callbacks;

        return fragment;
    }

    // ID Loader
    public static final int ID_LOADER_ALL_DATA_DETAIL = 1;
    // KEY Arguments
    public static final String KEY_ARGUMENTS_WHERE = "SearchResultFragment:ArgumentsWhere";
    public static final String KEY_ARGUMENTS_WHERE_PARAMS = "SearchResultFragment:ArgumentsWhereParams";
    public static final String KEY_ARGUMENTS_SORT = "SearchResultFragment:ArgumentsSort";

    public int AccountId = -1;

    private static final String LOGCAT = AllDataFragment.class.getSimpleName();

    private IAllDataFragmentLoaderCallbacks mSearResultFragmentLoaderCallbacks;
    private boolean mAutoStarLoader = true;
    private boolean mShownHeader = false;
    private boolean mShownBalance = false;
    private AllDataMultiChoiceModeListener mMultiChoiceModeListener;
    private View mListHeader = null;

    private IAllDataFragmentCallbacks mCallbacks;
    private Bundle mArguments;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // set fragment
        setEmptyText(getString(R.string.no_data));
        setListShown(false);

        // Read header indicator directly from the activity.
        if (getActivity() instanceof SearchActivity) {
            SearchActivity activity = (SearchActivity) getActivity();
            setShownHeader(activity.ShowAccountHeaders);
        }

        // create adapter
        AllDataAdapter adapter = new AllDataAdapter(getActivity(), null, TypeCursor.ALLDATA);
        adapter.setAccountId(this.AccountId);
        adapter.setShowAccountName(isShownHeader());
        adapter.setShowBalanceAmount(isShownBalance());

        // set choice mode in list view
        mMultiChoiceModeListener = new AllDataMultiChoiceModeListener();
        mMultiChoiceModeListener.setListener(this);
        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        getListView().setMultiChoiceModeListener(mMultiChoiceModeListener);

        // click item
        getListView().setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (getListAdapter() != null && getListAdapter() instanceof AllDataAdapter) {
                    Cursor cursor = ((AllDataAdapter) getListAdapter()).getCursor();
                    if (cursor.moveToPosition(position - (mListHeader != null ? 1 : 0))) {
                        startCheckingAccountActivity(cursor.getInt(cursor.getColumnIndex(QueryAllData.ID)));
                    }
                }
            }
        });
        // if header is not null add to list view
        if (getListAdapter() == null) {
            if (mListHeader != null)
                getListView().addHeaderView(mListHeader);
        }
        // set adapter
        setListAdapter(adapter);

        // register context menu
        registerForContextMenu(getListView());

        // set animation progress
        setListShown(false);

        // floating action button
        setFloatingActionButtonVisible(true);
        setFloatingActionButtonAttachListView(true);

        // start loader
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
    public IAllDataFragmentLoaderCallbacks getSearchResultFragmentLoaderCallbacks() {
        return mSearResultFragmentLoaderCallbacks;
    }

    /**
     * @param searResultFragmentLoaderCallbacks the searResultFragmentLoaderCallbacks to set
     */
    public void setSearResultFragmentLoaderCallbacks(IAllDataFragmentLoaderCallbacks searResultFragmentLoaderCallbacks) {
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
     * @return the mShownHeader
     */
    public boolean isShownHeader() {
        return mShownHeader;
    }

    /**
     * @param mShownHeader the mShownHeader to set
     */
    public void setShownHeader(boolean mShownHeader) {
        this.mShownHeader = mShownHeader;
    }

    // Loader event handlers

    /**
     * Start loader into fragment
     */
    public void loadData() {
        loadData(getLatestArguments());
    }

    public void loadData(Bundle arguments) {
        // set the account id in the data adapter
        AllDataAdapter adapter = (AllDataAdapter) getListAdapter();
        if (adapter != null) {
            adapter.setAccountId(this.AccountId);
        }

        // set the current arguments / account id
        setLatestArguments(arguments);
        // reload data with the latest arguments.
        getLoaderManager().restartLoader(ID_LOADER_ALL_DATA_DETAIL, arguments, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (getSearchResultFragmentLoaderCallbacks() != null)
            getSearchResultFragmentLoaderCallbacks().onCallbackCreateLoader(id, args);
        //animation
        setListShown(false);

        switch (id) {
            case ID_LOADER_ALL_DATA_DETAIL:
                // compose selection and sort
                String selection = "", sort = "";
                if (args != null && args.containsKey(KEY_ARGUMENTS_WHERE)) {
                    ArrayList<String> whereClause = args.getStringArrayList(KEY_ARGUMENTS_WHERE);
                    if (whereClause != null) {
                        for (int i = 0; i < whereClause.size(); i++) {
                            selection += (!TextUtils.isEmpty(selection) ? " AND " : "") + whereClause.get(i);
                        }
                    }
                }
                // where parameters
                String[] whereParams = new String[0];
                if (args != null && args.containsKey(KEY_ARGUMENTS_WHERE_PARAMS)) {
                    ArrayList<String> whereParamsList = args.getStringArrayList(KEY_ARGUMENTS_WHERE_PARAMS);
                    whereParams = whereParamsList.toArray(whereParams);
                }
                // set sort
                if (args != null && args.containsKey(KEY_ARGUMENTS_SORT)) {
                    sort = args.getString(KEY_ARGUMENTS_SORT);
                }
                // create loader
                QueryAllData allData = new QueryAllData(getActivity());
                return new CursorLoader(getActivity(), allData.getUri(),
                        allData.getAllColumns(),
                        selection,
                        whereParams,
                        sort);
        }
        return null;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        IAllDataFragmentLoaderCallbacks parent = getSearchResultFragmentLoaderCallbacks();
        if (parent != null) parent.onCallbackLoaderReset(loader);

        ((CursorAdapter) getListAdapter()).swapCursor(null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        IAllDataFragmentLoaderCallbacks parent = getSearchResultFragmentLoaderCallbacks();
        if (parent != null) parent.onCallbackLoaderFinished(loader, data);

        switch (loader.getId()) {
            case ID_LOADER_ALL_DATA_DETAIL:
                AllDataAdapter adapter = (AllDataAdapter) getListAdapter();
//                if (isShownBalance()) {
//                    Context appContext = getActivity().getApplicationContext();
//                    adapter.setDatabase(MoneyManagerOpenHelper.getInstance(appContext).getReadableDatabase());
//                }
                adapter.swapCursor(data);
                if (isResumed()) {
                    setListShown(true);
                    if (data.getCount() <= 0 && getFloatingActionButton() != null)
                        getFloatingActionButton().show(true);
                } else {
                    setListShownNoAnimation(true);
                }

                // reset the transaction groups (account name collection)
                adapter.resetAccountHeaderIndexes();
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

    // This is just to test:
    // http://stackoverflow.com/questions/15207305/getting-the-error-java-lang-illegalstateexception-activity-has-been-destroyed
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
            BaseFragmentActivity activity = (BaseFragmentActivity) getActivity();
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
            Log.e(LOGCAT, e.getMessage());
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

    private boolean setStatusCheckingAccount(int[] transId, String status) {
        // check if status = "U" convert to empty string
        if (TextUtils.isEmpty(status) || "U".equalsIgnoreCase(status))
            status = "";

        for (int id : transId) {
            // content value for updates
            ContentValues values = new ContentValues();
            // set new state
            values.put(TableCheckingAccount.STATUS, status.toUpperCase());

            // update
            if (getActivity().getContentResolver().update(new TableCheckingAccount().getUri(),
                    values,
                    TableCheckingAccount.TRANSID + "=?",
                    new String[]{Integer.toString(id)}) <= 0) {
                Toast.makeText(getActivity(), R.string.db_update_failed, Toast.LENGTH_LONG).show();
                return false;
            }
        }
        return true;
    }

    /**
     * @param transactionIds primary key of transation
     */
    private void showDialogDeleteCheckingAccount(final ArrayList<Integer> transactionIds) {
        // create alert dialog and set title and message
        AlertDialogWrapper.Builder alertDialog = new AlertDialogWrapper.Builder(getActivity());

        alertDialog.setTitle(R.string.delete_transaction);
        alertDialog.setMessage(getResources().getQuantityString(R.plurals.plurals_delete_transactions,
                transactionIds.size(), transactionIds.size()));
        alertDialog.setIcon(R.drawable.ic_action_warning_light);

        // set listener button positive
        alertDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for (int transactionId : transactionIds) {
                    // First delete any splits.
                    // See if there are any split records.
                    TableSplitTransactions split = new TableSplitTransactions();
                    Cursor curSplit = getActivity().getContentResolver().query(split.getUri(), null,
                            TableSplitTransactions.TRANSID + "=" + Integer.toString(transactionId),
                            null, TableSplitTransactions.SPLITTRANSID);
                    int splitCount = curSplit.getCount();
                    curSplit.close();

                    if (splitCount > 0) {
                        TableSplitTransactions splits = new TableSplitTransactions();
                        int deleteResult = getActivity().getContentResolver().delete(splits.getUri(),
                                TableSplitTransactions.TRANSID + "=?",
                                new String[]{Integer.toString(transactionId)});
                        if (deleteResult != splitCount) {
                            Toast.makeText(getActivity(), R.string.db_delete_failed, Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    // Delete the transaction.

                    TableCheckingAccount trans = new TableCheckingAccount();
                    if (getActivity().getContentResolver().delete(
                            trans.getUri(), TableCheckingAccount.TRANSID + "=?",
                            new String[]{Integer.toString(transactionId)}) == 0) {
                        Toast.makeText(getActivity(), R.string.db_delete_failed, Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                // restart loader
                loadData();
            }
        });
        // set listener negative button
        alertDialog.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // close dialog
                dialog.cancel();
            }
        });

        alertDialog.create();
        alertDialog.show();
    }

    /**
     * start the activity of transaction management
     *
     * @param transId null set if you want to do a new transaction, or transaction id
     */
    private void startCheckingAccountActivity(Integer transId) {
        // create intent, set Account ID
        Intent intent = new Intent(getActivity(), CheckingAccountActivity.class);
        // check transId not null
        if (transId != null) {
            intent.putExtra(CheckingAccountConstants.KEY_TRANS_ID, transId);
            intent.setAction(Intent.ACTION_EDIT);
        } else {
            intent.putExtra(CheckingAccountConstants.KEY_ACCOUNT_ID, this.AccountId);
            intent.setAction(Intent.ACTION_INSERT);
        }
        // launch activity
        startActivity(intent);
    }

    /**
     * @return the mShownBalance
     */
    public boolean isShownBalance() {
        return mShownBalance;
    }

    /**
     * @param mShownBalance the mShownBalance to set
     */
    public void setShownBalance(boolean mShownBalance) {
        this.mShownBalance = mShownBalance;
    }

    @Override
    public String getSubTitle() {
        return null;
    }

    @Override
    public void onFloatingActionButtonClickListener() {
        startCheckingAccountActivity(null);
    }

    public View getListHeader() {
        return mListHeader;
    }

    public void setListHeader(View mHeaderList) {
        this.mListHeader = mHeaderList;
    }

    // Begin multi-choice-mode listener callback handlers.

    /**
     * handler for multi-choice-mode listener
     */
    @Override
    public void onMultiChoiceCreated(android.view.Menu menu) {
//        int selectedItemPosition = getListView().getSelectedItemPosition();
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

    // Private area

    private void selectAllRecords() {
        ListAdapter listAdapter = getListAdapter();
        if (listAdapter != null && listAdapter instanceof AllDataAdapter) {
            AllDataAdapter adapter = (AllDataAdapter) getListAdapter();

            // Clear selection first.
            adapter.clearPositionChecked();

            int numRecords = adapter.getCount();
            for (int i = 0; i < numRecords; i++) {
                adapter.setPositionChecked(i, true);
            }

            adapter.notifyDataSetChanged();
        }
    }

    private ArrayList<Integer> getTransactionIds(){
        final ArrayList<Integer> transIds = new ArrayList<>();

        if (getListAdapter() != null && getListAdapter() instanceof AllDataAdapter) {
            AllDataAdapter adapter = (AllDataAdapter) getListAdapter();
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
        }

        return transIds;
    }

    private void changeTransactionStatus(final ArrayList<Integer> transIds){
        final DrawerMenuItemAdapter adapter = new DrawerMenuItemAdapter(getActivity());
        final Core core = new Core(getActivity().getApplicationContext());
        final Boolean isDarkTheme = core.getThemeApplication() == R.style.Theme_Money_Manager;
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

        // open dialog
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
            intents[i] = new Intent(getActivity(), CheckingAccountActivity.class);
            intents[i].putExtra(CheckingAccountConstants.KEY_TRANS_ID, ids[i]);
            intents[i].setAction(Intent.ACTION_PASTE);
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
