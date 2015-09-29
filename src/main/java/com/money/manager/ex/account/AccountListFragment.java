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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.money.manager.ex.account;

import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.money.manager.ex.R;
import com.money.manager.ex.adapter.MoneySimpleCursorAdapter;
import com.money.manager.ex.common.BaseListFragment;
import com.money.manager.ex.common.MmexCursorLoader;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.database.TableAccountList;
import com.money.manager.ex.database.TablePayee;
import com.money.manager.ex.domainmodel.Account;
import com.shamanland.fonticon.FontIconDrawable;

/**
 * List of accounts.
 */
public class AccountListFragment
        extends BaseListFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    public String mAction = Intent.ACTION_EDIT;

    private static TableAccountList mAccount = new TableAccountList();
    // id menu item add
    private static final int ID_LOADER_ACCOUNT = 0;
    // filter
    private String mCurFilter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // set show search
        setShowMenuItemSearch(true);
        // set default value
        setEmptyText(getActivity().getResources().getString(R.string.account_empty_list));
        setHasOptionsMenu(true);

        int layout = Intent.ACTION_PICK.equals(mAction)
                ? android.R.layout.simple_list_item_multiple_choice
                : android.R.layout.simple_list_item_1;

        // create adapter
        MoneySimpleCursorAdapter adapter = new MoneySimpleCursorAdapter(getActivity(),
                layout, null,
                new String[]{ Account.ACCOUNTNAME },
                new int[]{android.R.id.text1}, 0);
        setListAdapter(adapter);

        registerForContextMenu(getListView());

        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        setListShown(false);
        // start loader
        getLoaderManager().initLoader(ID_LOADER_ACCOUNT, null, this);

        // set icon searched
        setMenuItemSearchIconified(!Intent.ACTION_PICK.equals(mAction));
        setFloatingActionButtonVisible(true);
        setFloatingActionButtonAttachListView(true);
    }

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        // take cursor
        Cursor cursor = ((SimpleCursorAdapter) getListAdapter()).getCursor();
        cursor.moveToPosition(info.position);

        switch (item.getItemId()) {
            case 0: //EDIT
                startAccountListEditActivity(cursor.getInt(cursor.getColumnIndex(Account.ACCOUNTID)));
                break;

            case 1: //DELETE
                ContentValues contentValues = new ContentValues();
                contentValues.put(Account.ACCOUNTID, cursor.getInt(cursor.getColumnIndex(Account.ACCOUNTID)));
                if (new TablePayee().canDelete(getActivity(), contentValues)) {
                    showDialogDeleteAccount(cursor.getInt(cursor.getColumnIndex(Account.ACCOUNTID)));
                } else {
//                    Core core = new Core(getActivity());
//                    int icon = core.usingDarkTheme()
//                            ? R.drawable.ic_action_warning_dark
//                            : R.drawable.ic_action_warning_light;

                    new AlertDialogWrapper.Builder(getActivity())
                            .setTitle(R.string.attention)
                            .setMessage(R.string.account_can_not_deleted)
                            .setIcon(FontIconDrawable.inflate(getContext(), R.xml.ic_alert))
                            .setPositiveButton(android.R.string.ok,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(
                                                DialogInterface dialog,
                                                int which) {
                                            dialog.dismiss();
                                        }
                                    }).create().show();
                }
                break;
        }
        return false;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        // take cursor
        Cursor cursor = ((SimpleCursorAdapter) getListAdapter()).getCursor();
        cursor.moveToPosition(info.position);

        menu.setHeaderTitle(cursor.getString(cursor.getColumnIndex(Account.ACCOUNTNAME)));

        String[] menuItems = getResources().getStringArray(R.array.context_menu);
        for (int i = 0; i < menuItems.length; i++) {
            menu.add(Menu.NONE, i, i, menuItems[i]);
        }
    }

    // Loader

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case ID_LOADER_ACCOUNT:
                String whereClause = null;
                String selectionArgs[] = null;
                if (!TextUtils.isEmpty(mCurFilter)) {
                    whereClause = Account.ACCOUNTNAME + " LIKE ?";
                    selectionArgs = new String[]{mCurFilter + "%"};
                }
                return new MmexCursorLoader(getActivity(), mAccount.getUri(), mAccount.getAllColumns(),
                        whereClause, selectionArgs, "upper(" + Account.ACCOUNTNAME + ")");
        }

        return null;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case ID_LOADER_ACCOUNT:
                MoneySimpleCursorAdapter adapter = (MoneySimpleCursorAdapter) getListAdapter();
                adapter.swapCursor(null);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case ID_LOADER_ACCOUNT:
                MoneySimpleCursorAdapter adapter = (MoneySimpleCursorAdapter) getListAdapter();
                adapter.setHighlightFilter(mCurFilter != null ? mCurFilter.replace("%", "") : "");
                adapter.swapCursor(data);

                if (isResumed()) {
                    setListShown(true);
                    if (data != null && data.getCount() <= 0 && getFloatingActionButton() != null) {
                        getFloatingActionButton().show(true);
                    }
                } else {
                    setListShownNoAnimation(true);
                }
        }
    }

    // End loader

    @Override
    public boolean onQueryTextChange(String newText) {
        // Called when the action bar search text has changed.  Update
        // the search filter, and restart the loader to do a new query
        // with this filter.
        mCurFilter = !TextUtils.isEmpty(newText) ? newText : null;
        getLoaderManager().restartLoader(ID_LOADER_ACCOUNT, null, this);
        return true;
    }

    @Override
    protected void setResult() {
        Intent result;
        if (Intent.ACTION_PICK.equals(mAction)) {
            // take cursor
            Cursor cursor = ((SimpleCursorAdapter) getListAdapter()).getCursor();

            for (int i = 0; i < getListView().getCount(); i++) {
                if (getListView().isItemChecked(i)) {
                    cursor.moveToPosition(i);
                    result = new Intent();
                    result.putExtra(AccountListActivity.INTENT_RESULT_ACCOUNTID,
                            cursor.getInt(cursor.getColumnIndex(Account.ACCOUNTID)));
                    result.putExtra(AccountListActivity.INTENT_RESULT_ACCOUNTNAME,
                            cursor.getString(cursor.getColumnIndex(Account.ACCOUNTNAME)));
                    getActivity().setResult(Activity.RESULT_OK, result);
                    return;
                }
            }
        }
        // return cancel
        getActivity().setResult(AccountListActivity.RESULT_CANCELED);
    }

    private void showDialogDeleteAccount(final int ACCOUNTID) {
        // create dialog
        AlertDialogWrapper.Builder alertDialog = new AlertDialogWrapper.Builder(getContext())
            .setTitle(R.string.delete_account)
            .setIcon(FontIconDrawable.inflate(getContext(), R.xml.ic_question))
            .setMessage(R.string.confirmDelete);

        alertDialog.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (getActivity().getContentResolver().delete(mAccount.getUri(), "ACCOUNTID=" + ACCOUNTID, null) == 0) {
                            Toast.makeText(getActivity(), R.string.db_delete_failed, Toast.LENGTH_SHORT).show();
                        }
                        // restart loader
                        getLoaderManager().restartLoader(ID_LOADER_ACCOUNT, null, AccountListFragment.this);
                    }
                });

        alertDialog.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // close dialog
                dialog.cancel();
            }
        });
        // show dialog
        alertDialog.create().show();
    }

    /**
     * Start the account management Activity
     */
    private void startAccountListEditActivity() {
        this.startAccountListEditActivity(null);
    }

    /**
     * Start the account management Activity
     *
     * @param accountId is null for a new account, not null for editing accountId account
     */
    private void startAccountListEditActivity(Integer accountId) {
        // create intent, set Account ID
        Intent intent = new Intent(getActivity(), AccountEditActivity.class);
        // check accountId not null
        if (accountId != null) {
            intent.putExtra(AccountEditActivity.KEY_ACCOUNT_ID, accountId);
            intent.setAction(Intent.ACTION_EDIT);
        } else {
            intent.setAction(Intent.ACTION_INSERT);
        }
        // launch activity
        startActivity(intent);
    }

    @Override
    public String getSubTitle() {
        return getString(R.string.accounts);
    }

    @Override
    public void onFloatingActionButtonClickListener() {
        startAccountListEditActivity();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        // show context menu here.
        getActivity().openContextMenu(v);
    }
}
