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
package com.money.manager.ex.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
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

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.money.manager.ex.PayeeActivity;
import com.money.manager.ex.R;
import com.money.manager.ex.adapter.MoneySimpleCursorAdapter;
import com.money.manager.ex.common.BaseListFragment;
import com.money.manager.ex.common.MmxCursorLoader;
import com.money.manager.ex.core.ContextMenuIds;
import com.money.manager.ex.core.IntentFactory;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.database.SQLTypeTransaction;
import com.money.manager.ex.datalayer.PayeeRepository;
import com.money.manager.ex.datalayer.Select;
import com.money.manager.ex.domainmodel.Payee;
import com.money.manager.ex.search.SearchParameters;
import com.money.manager.ex.servicelayer.PayeeService;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.utils.AlertDialogWrapper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

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

    private static final String SORT_BY_USAGE = "(SELECT COUNT(*) FROM CHECKINGACCOUNT_V1 WHERE PAYEE_V1.PAYEEID = CHECKINGACCOUNT_V1.PAYEEID) DESC";
    private static final String SORT_BY_NAME = "UPPER(" + Payee.PAYEENAME + ")";

    private Context mContext;
    private String mCurFilter;
    private int mSort = 0;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mContext = getActivity();

        setSearchMenuVisible(true);
        // Focus on search menu if set in preferences.
        AppSettings settings = new AppSettings(mContext);
        boolean focusOnSearch = settings.getBehaviourSettings().getFilterInSelectors();
        setMenuItemSearchIconified(!focusOnSearch);

        setEmptyText(getActivity().getResources().getString(R.string.payee_empty_list));
        setHasOptionsMenu(true);

        int layout = android.R.layout.simple_list_item_1;

        // associate adapter
        MoneySimpleCursorAdapter adapter = new MoneySimpleCursorAdapter(getActivity(),
                layout, null, new String[] { Payee.PAYEENAME },
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
        attachFloatingActionButtonToListView();
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
        menu.setHeaderTitle(cursor.getString(cursor.getColumnIndex(Payee.PAYEENAME)));

        menu.add(Menu.NONE, ContextMenuIds.EDIT.getId(), Menu.NONE, getString(R.string.edit));
        menu.add(Menu.NONE, ContextMenuIds.DELETE.getId(), Menu.NONE, getString(R.string.delete));
        menu.add(Menu.NONE, ContextMenuIds.VIEW_TRANSACTIONS.getId(), Menu.NONE, getString(R.string.view_transactions));
    }

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = null;
        if (item.getMenuInfo() instanceof AdapterView.AdapterContextMenuInfo) {
            info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } else {
            return false;
        }
//        if (item.getMenuInfo() instanceof ExpandableListView.ExpandableListContextMenuInfo) {
//            info = item.getMenuInfo();
//        }

        Cursor cursor = ((SimpleCursorAdapter) getListAdapter()).getCursor();
        cursor.moveToPosition(info.position);

        // Read values from cursor.
        Payee payee = new Payee();
        payee.loadFromCursor(cursor);

        ContextMenuIds menuId = ContextMenuIds.get(item.getItemId());
        if (menuId == null) return false;

        switch (menuId) {
            case EDIT:
                showDialogEditPayeeName(SQLTypeTransaction.UPDATE, payee.getId(), payee.getName());
                break;

            case DELETE:
                PayeeService service = new PayeeService(getActivity());
                if (!service.isPayeeUsed(payee.getId())) {
                    showDialogDeletePayee(payee.getId());
                } else {
                    new AlertDialogWrapper(getActivity())
                            .setTitle(R.string.attention)
                            .setIcon(new UIHelper(getActivity()).getIcon(GoogleMaterial.Icon.gmd_warning))
                            .setMessage(R.string.payee_can_not_deleted)
                            .setPositiveButton(android.R.string.ok)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    dialog.dismiss();
                                }
                            })
                        .create().show();
                }
                break;

            case VIEW_TRANSACTIONS:
                SearchParameters parameters = new SearchParameters();
                parameters.payeeId = payee.getId();
                parameters.payeeName = payee.getName();

                Intent intent = IntentFactory.getSearchIntent(getActivity(), parameters);
                startActivity(intent);
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
                    whereClause = Payee.PAYEENAME + " LIKE ?"; // + mCurFilter + "%'";
                    selectionArgs = new String[]{mCurFilter + '%'};
                }
                PayeeRepository repo = new PayeeRepository(getActivity());
                Select query = new Select(repo.getAllColumns())
                        .where(whereClause, selectionArgs)
                        .orderBy(mSort == 1 ? SORT_BY_USAGE : SORT_BY_NAME);

                return new MmxCursorLoader(getActivity(), repo.getUri(), query);
        }

        return null;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case ID_LOADER_PAYEE:
                MoneySimpleCursorAdapter adapter = (MoneySimpleCursorAdapter) getListAdapter();
//                adapter.swapCursor(null);
                adapter.changeCursor(null);
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
//                adapter.swapCursor(data);
                adapter.changeCursor(data);

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
        if (Intent.ACTION_PICK.equals(mAction)) {
            // Cursor that is already in the desired position, because positioned in the event onListItemClick
            Cursor cursor = ((SimpleCursorAdapter) getListAdapter()).getCursor();
            int payeeId = cursor.getInt(cursor.getColumnIndex(Payee.PAYEEID));
            String payeeName = cursor.getString(cursor.getColumnIndex(Payee.PAYEENAME));

            sendResultToActivity(payeeId, payeeName);

            return;
        }

        getActivity().setResult(PayeeActivity.RESULT_CANCELED);
    }

    private void sendResultToActivity(int payeeId, String payeeName) {
        Intent result = new Intent();
        result.putExtra(PayeeActivity.INTENT_RESULT_PAYEEID, payeeId);
        result.putExtra(PayeeActivity.INTENT_RESULT_PAYEENAME, payeeName);

        getActivity().setResult(AppCompatActivity.RESULT_OK, result);

        getActivity().finish();
    }

    private void showDialogDeletePayee(final int payeeId) {
        new AlertDialogWrapper(getContext())
            .setTitle(R.string.delete_payee)
            .setIcon(new IconicsDrawable(getActivity()).icon(GoogleMaterial.Icon.gmd_warning))
            .setMessage(R.string.confirmDelete)
            .setPositiveButton(android.R.string.ok,
                    new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                        PayeeRepository repo = new PayeeRepository(getActivity());
                        boolean success = repo.delete(payeeId);
                        if (success) {
                            Toast.makeText(getActivity(), R.string.delete_success, Toast.LENGTH_SHORT).show();
                        }

                        restartLoader();
                    }
                })
            .setNegativeButton(android.R.string.cancel,
                    new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.cancel();
                        }
                    })
            .create().show();
    }

    private void showDialogEditPayeeName(final SQLTypeTransaction type, final int payeeId, final String payeeName) {
        View viewDialog = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_new_edit_payee, null);
        final EditText edtPayeeName = (EditText) viewDialog.findViewById(R.id.editTextPayeeName);

        edtPayeeName.setText(payeeName);
        if (!TextUtils.isEmpty(payeeName)) {
            edtPayeeName.setSelection(payeeName.length());
        }

        UIHelper ui = new UIHelper(getContext());

        new AlertDialogWrapper(getContext())
            .setView(viewDialog)
            .setIcon(ui.getIcon(GoogleMaterial.Icon.gmd_person))
            .setTitle(R.string.edit_payeeName)
        .setPositiveButton(android.R.string.ok,
                new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        // take payee name from the input field.
                        String name = edtPayeeName.getText().toString();

                        PayeeService service = new PayeeService(mContext);

                        // check if action in update or insert
                        switch (type) {
                            case INSERT:
                                Payee payee = service.createNew(name);
                                if (payee != null) {
                                    // Created a new payee. But only if picking a payee for another activity.
                                    if (mAction.equalsIgnoreCase(Intent.ACTION_PICK)) {
                                        // Select it and close.
                                        sendResultToActivity(payee.getId(), name);
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
                })
        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        })
        .create().show();
    }

    @Override
    public String getSubTitle() {
        return getString(R.string.payees);
    }

    @Override
    public void onFloatingActionButtonClicked() {
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

//    private void showSearchActivityFor(SearchParameters parameters) {
//        Intent intent = new Intent(getActivity(), SearchActivity.class);
//        intent.putExtra(SearchActivity.EXTRA_SEARCH_PARAMETERS, parameters);
//        intent.setAction(Intent.ACTION_INSERT);
//        startActivity(intent);
//    }

}
