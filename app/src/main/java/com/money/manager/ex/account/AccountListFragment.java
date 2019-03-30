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
package com.money.manager.ex.account;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.loader.app.LoaderManager;

import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.money.manager.ex.R;
import com.money.manager.ex.adapter.MoneySimpleCursorAdapter;
import com.money.manager.ex.common.BaseListFragment;
import com.money.manager.ex.common.MmxCursorLoader;
import com.money.manager.ex.core.ContextMenuIds;
import com.money.manager.ex.core.MenuHelper;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.datalayer.AccountRepository;
import com.money.manager.ex.datalayer.Select;
import com.money.manager.ex.domainmodel.Account;
import com.money.manager.ex.servicelayer.AccountService;

import static androidx.core.content.ContextCompat.startActivity;

/**
 * List of accounts.
 */
public class AccountListFragment
    extends BaseListFragment
    implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOADER_ACCOUNT = 0;

    public String mAction = Intent.ACTION_EDIT;

    private String mCurFilter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // set show search
        setSearchMenuVisible(true);
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
        getLoaderManager().initLoader(LOADER_ACCOUNT, null, this);

        // set icon searched
        setMenuItemSearchIconified(!Intent.ACTION_PICK.equals(mAction));
        setFloatingActionButtonVisible(true);
        attachFloatingActionButtonToListView();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

        // get selected item name
        SimpleCursorAdapter adapter = (SimpleCursorAdapter) getListAdapter();
        Cursor cursor = (Cursor) adapter.getItem(info.position);
        menu.setHeaderTitle(cursor.getString(cursor.getColumnIndex(Account.ACCOUNTNAME)));

        MenuHelper menuHelper = new MenuHelper(getActivity(), menu);
        menuHelper.addEditToContextMenu();
        menuHelper.addDeleteToContextMenu();
    }

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
        ContextMenu.ContextMenuInfo menuInfo = item.getMenuInfo();
        // ExpandableListView$ExpandableListContextMenuInfo
        if (!(menuInfo instanceof AdapterView.AdapterContextMenuInfo)) return false;

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        int accountId = (int) info.id;
        int itemId = item.getItemId();
        ContextMenuIds menuId = ContextMenuIds.get(itemId);
        if (menuId == null) return false;

        switch (menuId) {
            case EDIT:
                startAccountListEditActivity(accountId);
                break;

            case DELETE:
                AccountService service = new AccountService(getActivity());
                if (service.isAccountUsed(accountId)) {
                    new MaterialDialog.Builder(getContext())
                            .title(R.string.attention)
                            .icon(new UIHelper(getActivity()).getIcon(GoogleMaterial.Icon.gmd_warning))
                            .content(R.string.account_can_not_deleted)
                            .positiveText(android.R.string.ok)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    dialog.dismiss();
                                }
                            })
                            .build().show();
                } else {
                    showDeleteConfirmationDialog(accountId);
                }
                break;
        }
        return false;
    }

    // Loader

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_ACCOUNT:
                String whereClause = null;
                String selectionArgs[] = null;
                if (!TextUtils.isEmpty(mCurFilter)) {
                    whereClause = Account.ACCOUNTNAME + " LIKE ?";
                    selectionArgs = new String[]{mCurFilter + "%"};
                }

                AccountRepository repo = new AccountRepository(getActivity());
                Select query = new Select(repo.getAllColumns())
                        .where(whereClause, selectionArgs)
                        .orderBy("upper(" + Account.ACCOUNTNAME + ")");

                return new MmxCursorLoader(getActivity(), repo.getUri(), query);
        }

        return null;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case LOADER_ACCOUNT:
                MoneySimpleCursorAdapter adapter = (MoneySimpleCursorAdapter) getListAdapter();
//                adapter.swapCursor(null);
                adapter.changeCursor(null);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case LOADER_ACCOUNT:
                MoneySimpleCursorAdapter adapter = (MoneySimpleCursorAdapter) getListAdapter();
                adapter.setHighlightFilter(mCurFilter != null ? mCurFilter.replace("%", "") : "");
//                adapter.swapCursor(data);
                adapter.changeCursor(data);

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

    /**
     * Called when the action bar search text has changed. Update the search filter, and restart
     * the loader to do a new query with this filter.
     * @param newText The search text
     * @return whether the event was handled or not
     */
    @Override
    public boolean onQueryTextChange(String newText) {
        mCurFilter = !TextUtils.isEmpty(newText) ? newText : null;
        getLoaderManager().restartLoader(LOADER_ACCOUNT, null, this);
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

    @Override
    public String getSubTitle() {
        return getString(R.string.accounts);
    }

    @Override
    public void onFloatingActionButtonClicked() {
        startAccountListEditActivity();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        // show context menu here.
        getActivity().openContextMenu(v);
    }

    private void showDeleteConfirmationDialog(final int accountId) {
        UIHelper ui = new UIHelper(getContext());

        new MaterialDialog.Builder(getContext())
            .title(R.string.delete_account)
            .icon(ui.getIcon(FontAwesome.Icon.faw_question_circle))
            .content(R.string.confirmDelete)
            .positiveText(android.R.string.ok)
            .onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    AccountRepository repo = new AccountRepository(getActivity());
                    if (!repo.delete(accountId)) {
                        Toast.makeText(getActivity(), R.string.db_delete_failed, Toast.LENGTH_SHORT).show();
                    }
                    // restart loader
                    getLoaderManager().restartLoader(LOADER_ACCOUNT, null, AccountListFragment.this);
                }
            })
            .negativeText(android.R.string.cancel)
            .onNegative(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    dialog.cancel();
                }
            })
            .build().show();
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
}
