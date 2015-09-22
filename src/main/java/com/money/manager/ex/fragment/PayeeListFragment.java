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
package com.money.manager.ex.fragment;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
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
import com.money.manager.ex.Constants;
import com.money.manager.ex.PayeeActivity;
import com.money.manager.ex.R;
import com.money.manager.ex.adapter.MoneySimpleCursorAdapter;
import com.money.manager.ex.businessobjects.PayeeService;
import com.money.manager.ex.common.BaseListFragment;
import com.money.manager.ex.common.MmexCursorLoader;
import com.money.manager.ex.database.SQLTypeTransaction;
import com.money.manager.ex.database.TablePayee;
import com.money.manager.ex.domainmodel.Payee;
import com.money.manager.ex.search.SearchActivity;
import com.money.manager.ex.search.SearchParameters;
import com.money.manager.ex.settings.AppSettings;
import com.shamanland.fonticon.FontIconDrawable;

/**
 * List of Payees. Used as a picker/selector also.
 */
public class PayeeListFragment
        extends BaseListFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    public static String mAction = Intent.ACTION_EDIT;

    // add menu ite,
//    private static final int MENU_ITEM_ADD = 1;
    private static final int ID_LOADER_PAYEE = 0;
    private static TablePayee mPayee = new TablePayee();
    // SORT BY USAGE
    private static final String SORT_BY_USAGE = "(SELECT COUNT(*) FROM CHECKINGACCOUNT_V1 WHERE PAYEE_V1.PAYEEID = CHECKINGACCOUNT_V1.PAYEEID) DESC";
    // SORT BY NAME
    private static final String SORT_BY_NAME = "UPPER(" + TablePayee.PAYEENAME + ")";

    private Context mContext;
    private String mCurFilter;
    private int mSort = 0;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mContext = getActivity();

        setShowMenuItemSearch(true);
        // Focus on search menu if set in preferences.
        AppSettings settings = new AppSettings(mContext);
        boolean focusOnSearch = settings.getBehaviourSettings().getFilterInSelectors();
        setMenuItemSearchIconified(!focusOnSearch);

        setEmptyText(getActivity().getResources().getString(R.string.payee_empty_list));
        setHasOptionsMenu(true);

        int layout = android.R.layout.simple_list_item_1;

        // associate adapter
        MoneySimpleCursorAdapter adapter = new MoneySimpleCursorAdapter(getActivity(),
                layout, null, new String[] { TablePayee.PAYEENAME },
                new int[]{android.R.id.text1}, 0);
        // set adapter
        setListAdapter(adapter);

        registerForContextMenu(getListView());
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        setListShown(false);
        // init sort
//        mSort = PreferenceManager.getDefaultSharedPreferences(getActivity())
//                .getInt(getString(PreferenceConstants.PREF_SORT_PAYEE), 0);
        mSort = settings.getPayeeSort();

        // start loader
        getLoaderManager().initLoader(ID_LOADER_PAYEE, null, this);

        // set floating button visible
        setFloatingActionButtonVisible(true);
        setFloatingActionButtonAttachListView(true);
    }

    // Menu

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_payee, menu);

        AppSettings settings = new AppSettings(mContext);
        int payeeSort = settings.getPayeeSort();

        //Check the default sort order
        final MenuItem item;
        // PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt(getString(PreferenceConstants.PREF_SORT_PAYEE), 0)
        switch (payeeSort) {
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
        AppSettings settings = new AppSettings(mContext);

        switch (item.getItemId()) {
            case R.id.menu_sort_name:
                mSort = 0;
                item.setChecked(true);
                settings.set(R.string.pref_sort_payee, mSort);
                // restart search
                restartLoader();
                return true;

            case R.id.menu_sort_usage:
                mSort = 1;
                item.setChecked(true);
                settings.set(R.string.pref_sort_payee, mSort);
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

    // Context Menu

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

        Cursor cursor = ((SimpleCursorAdapter) getListAdapter()).getCursor();
        cursor.moveToPosition(info.position);
        menu.setHeaderTitle(cursor.getString(cursor.getColumnIndex(TablePayee.PAYEENAME)));

        String[] menuItems = getResources().getStringArray(R.array.context_menu);
        int id;
        for (id = 0; id < menuItems.length; id++) {
            // 0, 1
            menu.add(Menu.NONE, id, id, menuItems[id]);
        }
        // view transactions menu item.
        id = 2;
        menu.add(Menu.NONE, id, id, getString(R.string.view_transactions));
    }

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        Cursor cursor = ((SimpleCursorAdapter) getListAdapter()).getCursor();
        cursor.moveToPosition(info.position);

        // Read values from cursor.
        Payee payee = new Payee();
        payee.loadFromCursor(cursor);

        switch (item.getItemId()) {
            case 0: //EDIT
                showDialogEditPayeeName(SQLTypeTransaction.UPDATE, payee.getId(), payee.getName());
                break;

            case 1: //DELETE
                ContentValues contentValues = new ContentValues();
                contentValues.put(TablePayee.PAYEEID, payee.getId());
                if (new TablePayee().canDelete(getActivity(), contentValues)) {
                    showDialogDeletePayee(payee.getId());
                } else {
                    new AlertDialogWrapper.Builder(getActivity())
                            .setTitle(R.string.attention)
                            .setIcon(FontIconDrawable.inflate(getContext(), R.xml.ic_alert))
                            .setMessage(R.string.payee_can_not_deleted)
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

            case 2: // view transactions
                SearchParameters parameters = new SearchParameters();
                parameters.payeeId = payee.getId();
                parameters.payeeName = payee.getName();

                showSearchActivityFor(parameters);
        }
        return false;
    }

    // Loader

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
                return new MmexCursorLoader(getActivity(), mPayee.getUri(),
                        mPayee.getAllColumns(),
                        whereClause, selectionArgs,
                        mSort == 1 ? SORT_BY_USAGE : SORT_BY_NAME);
        }

        return null;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case ID_LOADER_PAYEE:
                MoneySimpleCursorAdapter adapter = (MoneySimpleCursorAdapter) getListAdapter();
                adapter.swapCursor(null);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null) return;

        switch (loader.getId()) {
            case ID_LOADER_PAYEE:
                MoneySimpleCursorAdapter adapter = (MoneySimpleCursorAdapter) getListAdapter();
                String highlightFilter = mCurFilter != null
                        ? mCurFilter.replace("%", "")
                        : "";
                adapter.setHighlightFilter(highlightFilter);
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

//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        ListView l = getListView();
//        l.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
//        l.setStackFromBottom(true);
//
//        return l;
//    }

    @Override
    protected void setResult() {
        if (Intent.ACTION_PICK.equals(mAction)) {
            // Cursor that is already in the desired position, because positioned in the event onListItemClick
            Cursor cursor = ((SimpleCursorAdapter) getListAdapter()).getCursor();
            int payeeId = cursor.getInt(cursor.getColumnIndex(TablePayee.PAYEEID));
            String payeeName = cursor.getString(cursor.getColumnIndex(TablePayee.PAYEENAME));

            sendResultToActivity(payeeId, payeeName);

            return;
        }

        getActivity().setResult(PayeeActivity.RESULT_CANCELED);
    }

    private void sendResultToActivity(int payeeId, String payeeName) {
        Intent result = new Intent();
        result.putExtra(PayeeActivity.INTENT_RESULT_PAYEEID, payeeId);
        result.putExtra(PayeeActivity.INTENT_RESULT_PAYEENAME, payeeName);

        getActivity().setResult(Activity.RESULT_OK, result);

        getActivity().finish();
    }

    private void showDialogDeletePayee(final int payeeId) {
        // creating dialog
        AlertDialogWrapper.Builder alertDialog = new AlertDialogWrapper.Builder(getContext())
            .setTitle(R.string.delete_payee)
            .setIcon(FontIconDrawable.inflate(getContext(), R.xml.ic_alert))
            .setMessage(R.string.confirmDelete);

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

    private void showDialogEditPayeeName(final SQLTypeTransaction type, final int payeeId, final String payeeName) {
        View viewDialog = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_new_edit_payee, null);
        final EditText edtPayeeName = (EditText) viewDialog.findViewById(R.id.editTextPayeeName);

        edtPayeeName.setText(payeeName);
        if (!TextUtils.isEmpty(payeeName)) {
            edtPayeeName.setSelection(payeeName.length());
        }
        // create dialog
        AlertDialogWrapper.Builder alertDialog = new AlertDialogWrapper.Builder(getContext())
            .setView(viewDialog)
            .setIcon(FontIconDrawable.inflate(getContext(), R.xml.ic_user))
            .setTitle(R.string.edit_payeeName);

        alertDialog.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // take payee name from the input field.
                        String name = edtPayeeName.getText().toString();

                        PayeeService service = new PayeeService(mContext);

                        // check if action in update or insert
                        switch (type) {
                            case INSERT:
                                int newId = service.createNew(name);
                                if (newId != Constants.NOT_SET) {
                                    // Created a new payee. But only if picking a payee for another activity.
                                    if (mAction.equalsIgnoreCase(Intent.ACTION_PICK)) {
                                        // Select it and close.
                                        sendResultToActivity(newId, name);
                                        return;
                                    }
                                } else {
                                    // error inserting.
                                    Toast.makeText(mContext, R.string.db_insert_failed, Toast.LENGTH_SHORT).show();
                                }
                                break;
                            case UPDATE:
                                int updateResult = service.update(payeeId, name);
                                if (updateResult <= 0) {
                                    Toast.makeText(mContext, R.string.db_update_failed, Toast.LENGTH_SHORT).show();
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
        String payeeSearch = !TextUtils.isEmpty(mCurFilter) ? mCurFilter.replace("%", "") : "";
        showDialogEditPayeeName(SQLTypeTransaction.INSERT, 0, payeeSearch);
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
        } else {
            // No calling activity, this is the independent Payees view. Show context menu.
            getActivity().openContextMenu(v);
        }
    }

    public void restartLoader() {
        getLoaderManager().restartLoader(ID_LOADER_PAYEE, null, this);
    }

    private void showSearchActivityFor(SearchParameters parameters) {
        Intent intent = new Intent(getActivity(), SearchActivity.class);
        intent.putExtra(SearchActivity.EXTRA_SEARCH_PARAMETERS, parameters);
        intent.setAction(Intent.ACTION_INSERT);
        startActivity(intent);
    }

}
