/*
 * Copyright (C) 2012-2015 Alessandro Lazzari
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
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.money.manager.ex.PayeeActivity;
import com.money.manager.ex.R;
import com.money.manager.ex.adapter.MoneySimpleCursorAdapter;
import com.money.manager.ex.database.SQLTypeTransacion;
import com.money.manager.ex.database.TablePayee;
import com.money.manager.ex.preferences.PreferencesConstant;

/**
 *
 */
public class PayeeLoaderListFragment extends BaseListFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    public static String mAction = Intent.ACTION_EDIT;

    // add menu ite,
    private static final int MENU_ITEM_ADD = 1;
    private static final int ID_LOADER_PAYEE = 0;
    private static TablePayee mPayee = new TablePayee();
    // SORT BY USAGE
    private static final String SORT_BY_USAGE = "(SELECT COUNT(*) FROM CHECKINGACCOUNT_V1 WHERE PAYEE_V1.PAYEEID = CHECKINGACCOUNT_V1.PAYEEID) DESC";
    // SORT BY NAME
    private static final String SORT_BY_NAME = "UPPER(" + TablePayee.PAYEENAME + ")";

    private String mCurFilter;
    private int mSort = 0;
    private int mLayout;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setShowMenuItemSearch(true);
        setEmptyText(getActivity().getResources().getString(R.string.payee_empty_list));
        setHasOptionsMenu(true);

        //Use a single layout, it is not necessary to have the layout with the checkbox, with the single click for the selection of the pay
        //mLayout = Intent.ACTION_PICK.equals(mAction) ? android.R.layout.simple_list_item_multiple_choice : android.R.layout.simple_list_item_1;
        mLayout = android.R.layout.simple_list_item_1;
        // associate adapter
        MoneySimpleCursorAdapter adapter = new MoneySimpleCursorAdapter(getActivity(), mLayout, null, new String[]{TablePayee.PAYEENAME},
                new int[]{android.R.id.text1}, 0);
        // set adapter
        setListAdapter(adapter);

        registerForContextMenu(getListView());
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        setListShown(false);
        // init sort
        mSort = PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt(getString(PreferencesConstant.PREF_SORT_PAYEE), 0);
        // start loader
        getLoaderManager().initLoader(ID_LOADER_PAYEE, null, this);
        // set icon searched
        setMenuItemSearchIconified(!Intent.ACTION_PICK.equals(mAction));
        // set floating button visible
        setFloatingActionButtonVisbile(true);
        setFloatingActionButtonAttachListView(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_payee, menu);
        //Check the default sort order
        final MenuItem item;
        switch (PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt(getString(PreferencesConstant.PREF_SORT_PAYEE), 0)) {
            case 0:
                item = menu.findItem(R.id.menu_sort_name);
                item.setChecked(true);
                break;
            case 1:
                item = menu.findItem(R.id.menu_sort_usage);
                item.setChecked(true);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_sort_name:
                mSort = 0;
                item.setChecked(true);
                PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putInt(getString(PreferencesConstant.PREF_SORT_PAYEE), mSort).commit();
                // restart search
                restartLoader();
                return true;
            case R.id.menu_sort_usage:
                mSort = 1;
                item.setChecked(true);
                PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putInt(getString(PreferencesConstant.PREF_SORT_PAYEE), mSort).commit();
                // restart search
                restartLoader();
                return true;
            case android.R.id.home:
                getActivity().setResult(PayeeActivity.RESULT_CANCELED);
                getActivity().finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        Cursor cursor = ((SimpleCursorAdapter) getListAdapter()).getCursor();
        cursor.moveToPosition(info.position);

        switch (item.getItemId()) {
            case 0: //EDIT
                showDialogEditPayeeName(SQLTypeTransacion.UPDATE, cursor.getInt(cursor.getColumnIndex(TablePayee.PAYEEID)), cursor.getString(cursor.getColumnIndex(TablePayee.PAYEENAME)));
                break;
            case 1: //DELETE
                //if (new TablePayee().canDelete(getActivity(), cursor.getInt(cursor.getColumnIndex(TablePayee.PAYEEID)))) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(TablePayee.PAYEEID, cursor.getInt(cursor.getColumnIndex(TablePayee.PAYEEID)));
                if (new TablePayee().canDelete(getActivity(), contentValues)) {
                    showDialogDeletePayee(cursor.getInt(cursor.getColumnIndex(TablePayee.PAYEEID)));
                } else {
                    new AlertDialogWrapper.Builder(getActivity())
                            .setTitle(R.string.attention)
                            .setMessage(R.string.payee_can_not_deleted)
                            .setIcon(R.drawable.ic_action_warning_light)
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

        Cursor cursor = ((SimpleCursorAdapter) getListAdapter()).getCursor();
        cursor.moveToPosition(info.position);
        menu.setHeaderTitle(cursor.getString(cursor.getColumnIndex(TablePayee.PAYEENAME)));

        String[] menuItems = getResources().getStringArray(R.array.context_menu);
        for (int i = 0; i < menuItems.length; i++) {
            menu.add(Menu.NONE, i, i, menuItems[i]);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case ID_LOADER_PAYEE:
                String whereClause = null;
                String selectionArgs[] = null;
                if (!TextUtils.isEmpty(mCurFilter)) {
                    whereClause = TablePayee.PAYEENAME + " LIKE ?";// + mCurFilter + "%'";
                    selectionArgs = new String[]{mCurFilter + '%'};
                }
                return new CursorLoader(getActivity(), mPayee.getUri(), mPayee.getAllColumns(),
                        whereClause, selectionArgs, mSort == 1
                        ? SORT_BY_USAGE : SORT_BY_NAME);
        }

        return null;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case ID_LOADER_PAYEE:
                // mAdapter.swapCursor(null);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case ID_LOADER_PAYEE:
                MoneySimpleCursorAdapter adapter = (MoneySimpleCursorAdapter) getListAdapter();
                adapter.setHighlightFilter(mCurFilter != null ? mCurFilter.replace("%", "") : "");
                adapter.swapCursor(data);
                if (isResumed()) {
                    setListShown(true);
                    if (data.getCount() <= 0 && getFloatingActionButton() != null) {
                        getFloatingActionButton().show(true);
                    }
                } else {
                    setListShownNoAnimation(true);
                }
        }
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        // Called when the action bar search text has changed.  Update
        // the search filter, and restart the loader to do a new query
        // with this filter.
        mCurFilter = !TextUtils.isEmpty(newText) ? newText : null;
        restartLoader();
        return true;
    }

    @Override
    protected void setResult() {
        Intent result = null;
        if (Intent.ACTION_PICK.equals(mAction)) {
            // Cursor that is already in the desired position, because positioned in the event onListItemClick
            Cursor cursor = ((SimpleCursorAdapter) getListAdapter()).getCursor();

            result = new Intent();
            result.putExtra(PayeeActivity.INTENT_RESULT_PAYEEID,
                    cursor.getInt(cursor.getColumnIndex(TablePayee.PAYEEID)));
            result.putExtra(PayeeActivity.INTENT_RESULT_PAYEENAME,
                    cursor.getString(cursor.getColumnIndex(TablePayee.PAYEENAME)));

            getActivity().setResult(Activity.RESULT_OK, result);

            return;
        }

        getActivity().setResult(PayeeActivity.RESULT_CANCELED);
        return;
    }

    private void showDialogDeletePayee(final int payeeId) {
        // creating dialog
        AlertDialogWrapper.Builder alertDialog = new AlertDialogWrapper.Builder(getActivity());

        alertDialog.setTitle(R.string.delete_payee);
        alertDialog.setMessage(R.string.confirmDelete);
        alertDialog.setIcon(R.drawable.ic_action_warning_light);

        alertDialog.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (getActivity().getContentResolver().delete(mPayee.getUri(), "PAYEEID=" + payeeId, null) == 0) {
                            Toast.makeText(getActivity(), R.string.db_delete_failed, Toast.LENGTH_SHORT).show();
                        }
                        // restart loader
                        restartLoader();
                    }
                });

        alertDialog.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        // show dialog
        alertDialog.create().show();
    }

    private void showDialogEditPayeeName(final SQLTypeTransacion type, final int payeeId, final String payeeName) {
        View viewDialog = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_new_edit_payee, null);
        final EditText edtPayeeName = (EditText) viewDialog.findViewById(R.id.editTextPayeeName);

        edtPayeeName.setText(payeeName);
        if (!TextUtils.isEmpty(payeeName)) {
            edtPayeeName.setSelection(payeeName.length());
        }
        // create dialog
        AlertDialogWrapper.Builder alertDialog = new AlertDialogWrapper.Builder(getActivity());

        alertDialog.setView(viewDialog);
        alertDialog.setTitle(R.string.edit_payeeName);

        alertDialog.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // take payeename from edittext
                        String name = edtPayeeName.getText().toString();
                        ContentValues values = new ContentValues();
                        values.put(TablePayee.PAYEENAME, name);
                        // check if action in update or insert
                        switch (type) {
                            case INSERT:
                                if (getActivity().getContentResolver().insert(mPayee.getUri(), values) == null) {
                                    Toast.makeText(getActivity(), R.string.db_insert_failed, Toast.LENGTH_SHORT).show();
                                }
                                break;
                            case UPDATE:
                                if (getActivity().getContentResolver().update(mPayee.getUri(), values, "PAYEEID=" + payeeId, null) == 0) {
                                    Toast.makeText(getActivity(), R.string.db_update_failed, Toast.LENGTH_SHORT).show();
                                }
                                break;
                            case DELETE:
                                break;
                            default:
                                break;
                        }
                        // restart loader
                        restartLoader();
                    }
                });

        alertDialog.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        // show dialog
        alertDialog.create().show();
    }

    @Override
    public String getSubTitle() {
        return getString(R.string.payees);
    }

    @Override
    public void onFloatingActionButtonClickListener() {
        showDialogEditPayeeName(SQLTypeTransacion.INSERT, 0, !TextUtils.isEmpty(mCurFilter) ? mCurFilter.replace("%", "") : "");
    }

    public void restartLoader() {
        getLoaderManager().restartLoader(ID_LOADER_PAYEE, null, this);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        // On select go back to the calling activity (if there is one)
        if (getActivity().getCallingActivity() != null) {
            Cursor cursor = ((SimpleCursorAdapter) getListAdapter()).getCursor();
            if (cursor != null) {
                if (cursor.moveToPosition(position)) {
                    setResultAndFinish();
                }
            }
        }
    }
}
