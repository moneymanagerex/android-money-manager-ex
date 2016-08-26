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
package com.money.manager.ex.recurring.transactions;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.money.manager.ex.common.MmexCursorLoader;
import com.money.manager.ex.log.ExceptionHandler;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.datalayer.Query;
import com.money.manager.ex.datalayer.RecurringTransactionRepository;
import com.money.manager.ex.domainmodel.Account;
import com.money.manager.ex.domainmodel.RecurringTransaction;
import com.money.manager.ex.transactions.CheckingTransactionEditActivity;
import com.money.manager.ex.R;
import com.money.manager.ex.adapter.AllDataAdapter;
import com.money.manager.ex.servicelayer.RecurringTransactionService;
import com.money.manager.ex.transactions.EditTransactionActivityConstants;
import com.money.manager.ex.database.QueryBillDeposits;
import com.money.manager.ex.common.BaseListFragment;
import com.shamanland.fonticon.FontIconDrawable;

/**
 * The recurring transactions list fragment.
 * Includes floating action button.
 */
public class RecurringTransactionListFragment
    extends BaseListFragment
    implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int REQUEST_ADD_REPEATING_TRANSACTION = 1001;
    private static final int REQUEST_ADD_TRANSACTION = 1002;
    private static final int REQUEST_EDIT_REPEATING_TRANSACTION = 1003;

    private static final int ID_LOADER_REPEATING = 0;

    private static QueryBillDeposits mBillDeposits;

    // filter
    private String mCurFilter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // create a object query
        mBillDeposits = new QueryBillDeposits(getActivity());
        // set list view
        setEmptyText(getActivity().getResources().getString(R.string.repeating_empty_transaction));
        setHasOptionsMenu(true);
        registerForContextMenu(getListView());
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        UIHelper uiHelper = new UIHelper(getActivity());
        getListView().setDivider(new ColorDrawable(uiHelper.getColor(R.attr.theme_background_color)));

        setListShown(false);

        getLoaderManager().initLoader(ID_LOADER_REPEATING, null, this);

        // show floating button.
        setFloatingActionButtonVisible(true);
        setFloatingActionButtonAttachListView(true);
    }

    @Override
    public void onFloatingActionButtonClickListener() {
        // create new recurring transaction.
        startRecurringTransactionEditActivity(null, REQUEST_ADD_REPEATING_TRANSACTION);
    }

    // Menu

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        // show context menu here.
        getActivity().openContextMenu(v);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        // take a cursor and move to position
        Cursor cursor = ((AllDataAdapter) getListAdapter()).getCursor();
        if (cursor != null) {
            cursor.moveToPosition(info.position);
            // set title and inflate menu
            menu.setHeaderTitle(getActivity().getTitle());
            getActivity().getMenuInflater().inflate(R.menu.contextmenu_repeating_transactions, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {

        ContextMenu.ContextMenuInfo menuInfo = item.getMenuInfo();
        if (menuInfo == null) {
            String errorMessage = "no context menu info";
            Log.w(this.getClass().getSimpleName(), errorMessage);
            ExceptionHandler handler = new ExceptionHandler(getActivity(), this);
            handler.showMessage("no context menu info");
            return false;
        }
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

        int menuItemId = item.getItemId();
        int transactionId = (int) info.id;

        switch (menuItemId) {
            case R.id.menu_enter_next_occurrence:
                showCreateTransactionActivity(transactionId);
                break;

            case R.id.menu_skip_next_occurrence:
                confirmSkip(transactionId);
                break;

            case R.id.menu_edit:
                startRecurringTransactionEditActivity(transactionId, REQUEST_EDIT_REPEATING_TRANSACTION);
                break;

            case R.id.menu_delete:
                confirmDelete(transactionId);
                break;
        }

        return false;
    }

    // Loader callbacks.

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case ID_LOADER_REPEATING:
                String select = null;
                if (!TextUtils.isEmpty(mCurFilter)) {
                    select = Account.ACCOUNTNAME + " LIKE '" + mCurFilter + "%'";
                }
                Query query = new Query()
                        .select(mBillDeposits.getAllColumns())
                        .where(select)
                        .orderBy(QueryBillDeposits.NEXTOCCURRENCEDATE);

                return new MmexCursorLoader(getActivity(), mBillDeposits.getUri(), query);
        }

        return null;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case ID_LOADER_REPEATING:
                AllDataAdapter adapter = (AllDataAdapter) getListAdapter();
                if (adapter != null) {
//                    adapter.swapCursor(null);
                    adapter.changeCursor(null);
                }
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case ID_LOADER_REPEATING:
                if (data == null) return;

                AllDataAdapter adapter = new AllDataAdapter(getActivity(), data,
                        AllDataAdapter.TypeCursor.RECURRINGTRANSACTION);
                setListAdapter(adapter);

                if (isResumed()) {
                    setListShown(true);
                    if (data.getCount() <= 0 && getFloatingActionButton() != null)
                        getFloatingActionButton().show(true);
                } else {
                    setListShownNoAnimation(true);
                }
        }
    }

    // Other

    @Override
    public boolean onQueryTextChange(String newText) {
        // Called when the action bar search text has changed. Update
        // the search filter, and restart the loader to do a new query
        // with this filter.
        mCurFilter = !TextUtils.isEmpty(newText) ? newText : null;
        getLoaderManager().restartLoader(ID_LOADER_REPEATING, null, this);
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RecurringTransactionListActivity.RESULT_OK) {
//                switch (requestCode) {
//                    case REQUEST_ADD_REPEATING_TRANSACTION:
//                        break;
//                    case REQUEST_ADD_TRANSACTION:
//                        break;
//                    case REQUEST_EDIT_REPEATING_TRANSACTION:
//                        break;
//                }
            // Always reload the activity?
            getLoaderManager().restartLoader(ID_LOADER_REPEATING, null, this);
        }
    }

    @Override
    public String getSubTitle() {
        return getString(R.string.repeating_transactions);
    }

    // private

    private void confirmDelete(final int id) {
        // create alert binaryDialog
        AlertDialogWrapper.Builder alertDialog = new AlertDialogWrapper.Builder(getContext())
            .setTitle(R.string.delete_repeating_transaction)
            .setIcon(FontIconDrawable.inflate(getContext(), R.xml.ic_question))
            .setMessage(R.string.confirmDelete);
        alertDialog.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        RecurringTransactionService recurringTransaction = new RecurringTransactionService(id, getActivity());
                        recurringTransaction.delete();

                        // restart loader
                        getLoaderManager().restartLoader(ID_LOADER_REPEATING,
                                null, RecurringTransactionListFragment.this);
                    }
                });
        alertDialog.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.create().show();
    }

    private void confirmSkip(final int id) {
        AlertDialogWrapper.Builder alertDialog = new AlertDialogWrapper.Builder(getContext())
            .setTitle(R.string.skip_next_occurrence)
            .setIcon(FontIconDrawable.inflate(getContext(), R.xml.ic_question))
            .setMessage(R.string.skip_next_occurrence_confirmation)
            .setPositiveButton(android.R.string.ok,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            RecurringTransactionService recurringTransaction = new RecurringTransactionService(id, getActivity());
                            recurringTransaction.moveNextOccurrence();
                            getLoaderManager().restartLoader(ID_LOADER_REPEATING, null,
                                    RecurringTransactionListFragment.this);
                        }
                    })
            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
        alertDialog.create().show();
    }

    private void showCreateTransactionActivity(int recurringTransactionId) {
        RecurringTransactionRepository repo = new RecurringTransactionRepository(getActivity());
        RecurringTransaction tx = repo.load(recurringTransactionId);
        if (tx == null) return;

        Intent intent = new Intent(getActivity(), CheckingTransactionEditActivity.class);
        intent.setAction(Intent.ACTION_INSERT);
        intent.putExtra(EditTransactionActivityConstants.KEY_BDID_ID, recurringTransactionId);
        // start for insert new transaction
        startActivityForResult(intent, REQUEST_ADD_TRANSACTION);
    }

    /**
     * start RepeatingTransaction for insert or edit transaction
     *
     * @param billDepositsId Id of the recurring transaction.
     * @param purposeCode       Code that indicates why we are opening the editor.
     *                          example: REQUEST_ADD_REPEATING_TRANSACTION
     */
    private void startRecurringTransactionEditActivity(Integer billDepositsId, int purposeCode) {
        // create intent, set Bill Deposits ID
        Intent intent = new Intent(getActivity(), RecurringTransactionEditActivity.class);
        // check transId not null
        if (billDepositsId != null) {
            intent.putExtra(RecurringTransactionEditActivity.KEY_BILL_DEPOSITS_ID, billDepositsId);
            intent.setAction(Intent.ACTION_EDIT);
        } else {
            intent.setAction(Intent.ACTION_INSERT);
        }
        // launch activity
        startActivityForResult(intent, purposeCode);
    }
}
