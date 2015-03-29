/*
 * Copyright (C) 2012-2014 Alessandro Lazzari
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

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.MaterialDialog;
import com.money.manager.ex.CheckingAccountActivity;
import com.money.manager.ex.R;
import com.money.manager.ex.SearchActivity;
import com.money.manager.ex.adapter.AllDataAdapter;
import com.money.manager.ex.adapter.AllDataAdapter.TypeCursor;
import com.money.manager.ex.adapter.DrawerMenuItem;
import com.money.manager.ex.adapter.DrawerMenuItemAdapter;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.ExportToCsvFile;
import com.money.manager.ex.database.MoneyManagerOpenHelper;
import com.money.manager.ex.database.QueryAllData;
import com.money.manager.ex.database.TableCheckingAccount;

import java.util.ArrayList;

public class AllDataFragment extends BaseListFragment implements LoaderCallbacks<Cursor> {
    // ID Loader
    public static final int ID_LOADER_ALL_DATA_DETAIL = 1;
    // KEY Arguments
    public static final String KEY_ARGUMENTS_WHERE = "SearchResultFragment:ArgumentsWhere";
    public static final String KEY_ARGUMENTS_SORT = "SearchResultFragment:ArgumentsSort";
    private static final String LOGCAT = AllDataFragment.class.getSimpleName();
    private AllDataFragmentLoaderCallbacks mSearResultFragmentLoaderCallbacks;
    private boolean mAutoStarLoader = true;
    private boolean mShownHeader = false;
    private boolean mShownBalance = false;
    private int mGroupId = 0;
    private int mAccountId = -1;
    private AllDataMultiChoiceModeListener mMultiChoiceModeListener;
    private View mListHeader = null;

    /**
     * Create a new instance of AllDataFragment with accountId params
     *
     * @param accountId Id of account to display. If generic shown set -1
     * @return new instance AllDataFragment
     */
    public static AllDataFragment newInstance(int accountId) {
        AllDataFragment fragment = new AllDataFragment();
        fragment.mAccountId = accountId;
        return fragment;
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
     * @param prefixName
     */
    public void exportDataToCSVFile(String prefixName) {
        ExportToCsvFile csv = new ExportToCsvFile(getActivity(), (AllDataAdapter) getListAdapter());
        csv.setPrefixName(prefixName);
        csv.execute();
    }

    /**
     * @return the mGroupId
     */
    public int getContextMenuGroupId() {
        return mGroupId;
    }

    /**
     * @param mGroupId the mGroupId to set
     */
    public void setContextMenuGroupId(int mGroupId) {
        this.mGroupId = mGroupId;
    }

    /**
     * @return the mSearResultFragmentLoaderCallbacks
     */
    public AllDataFragmentLoaderCallbacks getSearResultFragmentLoaderCallbacks() {
        return mSearResultFragmentLoaderCallbacks;
    }

    /**
     * @param mSearResultFragmentLoaderCallbacks the mSearResultFragmentLoaderCallbacks to set
     */
    public void setSearResultFragmentLoaderCallbacks(AllDataFragmentLoaderCallbacks mSearResultFragmentLoaderCallbacks) {
        this.mSearResultFragmentLoaderCallbacks = mSearResultFragmentLoaderCallbacks;
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

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // set fragment
        setEmptyText(getString(R.string.no_data));
        setListShown(false);
        // option menu
        setHasOptionsMenu(Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB);
        // create adapter
        AllDataAdapter adapter = new AllDataAdapter(getActivity(), null, TypeCursor.ALLDATA);
        adapter.setAccountId(mAccountId);
        adapter.setShowAccountName(isShownHeader());
        adapter.setShowBalanceAmount(isShownBalance());
        /*if (isShownBalance()) {
            adapter.setDatabase(MoneyManagerOpenHelper.getInstance(getActivity().getApplicationContext()).getReadableDatabase());
        }*/
        // set choice mode in listview
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mMultiChoiceModeListener = new AllDataMultiChoiceModeListener();
            getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
            getListView().setMultiChoiceModeListener(mMultiChoiceModeListener);
        }
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
        // if header is not null add to listview
        if (mListHeader != null)
            getListView().addHeaderView(mListHeader);
        // set adapter
        setListAdapter(adapter);

        // register context menu
        registerForContextMenu(getListView());

        // set animation progress
        setListShown(false);

        // floating action button
        setFloatingActionButtonVisbile(true);
        setFloatingActionButtonAttachListView(true);

        // start loader
        if (isAutoStarLoader()) {
            startLoaderData();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (getSearResultFragmentLoaderCallbacks() != null)
            getSearResultFragmentLoaderCallbacks().onCallbackCreateLoader(id, args);
        //animation
        setListShown(false);

        switch (id) {
            case ID_LOADER_ALL_DATA_DETAIL:
                QueryAllData allData = new QueryAllData(getActivity());
                // compose selection and sort
                String selection = "",
                        sort = "";
                if (args != null && args.containsKey(KEY_ARGUMENTS_WHERE)) {
                    ArrayList<String> whereClause = args.getStringArrayList(KEY_ARGUMENTS_WHERE);
                    if (whereClause != null) {
                        for (int i = 0; i < whereClause.size(); i++) {
                            selection += (!TextUtils.isEmpty(selection) ? " AND " : "") + whereClause.get(i);
                        }
                    }
                }
                // set sort
                if (args != null && args.containsKey(KEY_ARGUMENTS_SORT)) {
                    sort = args.getString(KEY_ARGUMENTS_SORT);
                }
                // create loader
                return new CursorLoader(getActivity(), allData.getUri(), allData.getAllColumns(), selection, null, sort);
        }
        return null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (getActivity() != null) {
            MenuItem itemExportToCsv = menu.findItem(R.id.menu_export_to_csv);
            if (itemExportToCsv != null) itemExportToCsv.setVisible(true);
            MenuItem itemSearch = menu.findItem(R.id.menu_search_transaction);
            if (itemSearch != null) itemSearch.setVisible(!getActivity().getClass().getSimpleName().equals(SearchActivity.class.getSimpleName()));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDestroy() {
        if (mMultiChoiceModeListener != null)
            mMultiChoiceModeListener.onDestroyActionMode(null);
        super.onDestroy();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (getSearResultFragmentLoaderCallbacks() != null)
            getSearResultFragmentLoaderCallbacks().onCallbackLoaderReset(loader);

        ((CursorAdapter) getListAdapter()).swapCursor(null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (getSearResultFragmentLoaderCallbacks() != null) {
            getSearResultFragmentLoaderCallbacks().onCallbackLoaderFinished(loader, data);
        }
        switch (loader.getId()) {
            case ID_LOADER_ALL_DATA_DETAIL:
                AllDataAdapter adapter = (AllDataAdapter) getListAdapter();
                if (isShownBalance()) {
                    adapter.setDatabase(MoneyManagerOpenHelper.getInstance(getActivity().getApplicationContext()).getReadableDatabase());
                }
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

    @Override
    public void onStop() {
        super.onStop();
        try {
            BaseFragmentActivity activity = (BaseFragmentActivity) getActivity();
            if (activity != null) {
                if (activity.getSupportActionBar().getCustomView() != null) {
                    activity.getSupportActionBar().setCustomView(null);
                }
            }
        } catch (Exception e) {
            Log.e(LOGCAT, e.getMessage());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_export_to_csv) {
            exportDataToCSVFile();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private int[] convertArraryListToArray(ArrayList<Integer> list) {
        int[] ret = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            ret[i] = list.get(i);
        }
        return ret;
    }

    private boolean setStatusCheckingAccount(int transId, String status) {
        return setStatusCheckingAccount(new int[]{transId}, status);
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
            if (getActivity().getContentResolver().update(new TableCheckingAccount().getUri(), values, TableCheckingAccount.TRANSID + "=?", new String[]{Integer.toString(id)}) <= 0) {
                Toast.makeText(getActivity(), R.string.db_update_failed, Toast.LENGTH_LONG).show();
                return false;
            }
        }
        return true;
    }

    /**
     * @param transId primary key of transation
     */
    private void showDialogDeleteCheckingAccount(final int[] transId) {
        // create alert dialog and set title and message
        AlertDialogWrapper.Builder alertDialog = new AlertDialogWrapper.Builder(getActivity());

        alertDialog.setTitle(R.string.delete_transaction);
        alertDialog.setMessage(getResources().getQuantityString(R.plurals.plurals_delete_transactions, transId.length, transId.length));
        alertDialog.setIcon(R.drawable.ic_action_warning_light);

        // set listener button positive
        alertDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for (int i = 0; i < transId.length; i++) {
                    TableCheckingAccount trans = new TableCheckingAccount();
                    if (getActivity().getContentResolver().delete(trans.getUri(), TableCheckingAccount.TRANSID + "=?", new String[]{Integer.toString(transId[i])}) == 0) {
                        Toast.makeText(getActivity(), R.string.db_delete_failed, Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                // restart loader
                startLoaderData();
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
            intent.putExtra(CheckingAccountActivity.KEY_TRANS_ID, transId);
            intent.setAction(Intent.ACTION_EDIT);
        } else {
            intent.putExtra(CheckingAccountActivity.KEY_ACCOUNT_ID, mAccountId);
            intent.setAction(Intent.ACTION_INSERT);
        }
        // launch activity
        startActivity(intent);
    }

    /**
     * Start loader into fragment
     */
    public void startLoaderData() {
        getLoaderManager().restartLoader(ID_LOADER_ALL_DATA_DETAIL, getArguments(), this);
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

    // Interface for callback fragment
    public interface AllDataFragmentLoaderCallbacks {
        public void onCallbackCreateLoader(int id, Bundle args);

        public void onCallbackLoaderFinished(Loader<Cursor> loader, Cursor data);

        public void onCallbackLoaderReset(Loader<Cursor> loader);
    }

    // class to manage multi choice mode
    public class AllDataMultiChoiceModeListener implements MultiChoiceModeListener {

        @Override
        public boolean onPrepareActionMode(ActionMode mode, android.view.Menu menu) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            if (getListAdapter() != null && getListAdapter() instanceof AllDataAdapter) {
                AllDataAdapter adapter = (AllDataAdapter) getListAdapter();
                adapter.clearPositionChecked();
                adapter.notifyDataSetChanged();
            }
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, android.view.Menu menu) {
            getActivity().getMenuInflater().inflate(R.menu.menu_all_data_adapter, menu);
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, android.view.MenuItem item) {
            final ArrayList<Integer> transIds = new ArrayList<Integer>();
            if (getListAdapter() != null && getListAdapter() instanceof AllDataAdapter) {
                AllDataAdapter adapter = (AllDataAdapter) getListAdapter();
                Cursor cursor = adapter.getCursor();
                if (cursor != null) {
                    SparseBooleanArray positionChecked = getListView().getCheckedItemPositions();
                    for (int i = 0; i < getListView().getCheckedItemCount(); i++) {
                        int position = positionChecked.keyAt(i);
                        if (getListHeader() != null)
                            position--;
                        if (cursor.moveToPosition(position)) {
                            transIds.add(cursor.getInt(cursor.getColumnIndex(QueryAllData.ID)));
                        }
                    }
                }
            }
            switch (item.getItemId()) {
                case R.id.menu_change_status:
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
                            .adapter(adapter)
                            .build();

                    ListView listView = dialog.getListView();
                    if (listView != null) listView.setOnItemClickListener(new OnItemClickListener() {
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
                                    if (setStatusCheckingAccount(convertArraryListToArray(transIds), status)) {
                                        ((AllDataAdapter) getListAdapter()).clearPositionChecked();
                                        startLoaderData();
                                    }
                            }
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                    mode.finish();
                    break;
                case R.id.menu_duplicate_transactions:
                    int[] ids = convertArraryListToArray(transIds);
                    Intent[] intents = new Intent[ids.length];
                    for (int i = 0; i < ids.length; i++) {
                        intents[i] = new Intent(getActivity(), CheckingAccountActivity.class);
                        intents[i].putExtra(CheckingAccountActivity.KEY_TRANS_ID, ids[i]);
                        intents[i].setAction(Intent.ACTION_PASTE);
                    }
                    getActivity().startActivities(intents);
                    mode.finish();
                    break;
                case R.id.menu_delete:
                    showDialogDeleteCheckingAccount(convertArraryListToArray(transIds));
                    return true;
                case R.id.menu_none:
                case R.id.menu_reconciled:
                case R.id.menu_follow_up:
                case R.id.menu_duplicate:
                case R.id.menu_void:
                    String status = Character.toString(item.getAlphabeticShortcut());
                    if (setStatusCheckingAccount(convertArraryListToArray(transIds), status)) {
                        ((AllDataAdapter) getListAdapter()).clearPositionChecked();
                        startLoaderData();
                        mode.finish();
                        return true;
                    }
            }
            return false;
        }

        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            if (getListHeader() != null)
                position--;

            if (getListAdapter() != null && getListAdapter() instanceof AllDataAdapter) {
                AllDataAdapter adapter = (AllDataAdapter) getListAdapter();
                adapter.setPositionChecked(position, checked);
                adapter.notifyDataSetChanged();
            }
        }
    }
}
