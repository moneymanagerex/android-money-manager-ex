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
package com.money.manager.ex.budget;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.money.manager.ex.R;
import com.money.manager.ex.adapter.MoneySimpleCursorAdapter;
import com.money.manager.ex.budget.events.BudgetSelectedEvent;
import com.money.manager.ex.common.BaseListFragment;
import com.money.manager.ex.common.MmxCursorLoader;
import com.money.manager.ex.core.ContextMenuIds;
import com.money.manager.ex.core.MenuHelper;
import com.money.manager.ex.datalayer.BudgetRepository;
import com.money.manager.ex.datalayer.Select;
import com.money.manager.ex.domainmodel.Budget;

import org.greenrobot.eventbus.EventBus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

/**
 * Use the {@link BudgetListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BudgetListFragment
    extends BaseListFragment
    implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final int REQUEST_EDIT_BUDGET = 1;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment BudgetListFragment.
     */
    public static BudgetListFragment newInstance() {
        BudgetListFragment fragment = new BudgetListFragment();
        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    private final int LOADER_BUDGETS = 1;

    private MoneySimpleCursorAdapter mAdapter;

    public BudgetListFragment() {
        // Required empty public constructor
    }

    @Override
    public String getSubTitle() {
        return getString(R.string.budget_list);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setFloatingActionButtonVisible(true);
        attachFloatingActionButtonToListView();

        registerForContextMenu(getListView());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_budgets_list, container, false);
//    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == AppCompatActivity.RESULT_CANCELED) return;

        switch (requestCode) {
            case REQUEST_EDIT_BUDGET:
                // refresh budget list
                getLoaderManager().restartLoader(LOADER_BUDGETS, null, this);
                break;
        }
    }

    @Override
    public void onViewCreated (View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        displayBudgets();
    }

    // Loader events

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Loader<Cursor> result = null;

        switch (id) {
            case LOADER_BUDGETS:
                BudgetRepository repo = new BudgetRepository(getActivity());
                Select query = new Select(repo.getAllColumns())
                    .orderBy(Budget.BUDGETYEARNAME);

                result = new MmxCursorLoader(getActivity(), repo.getUri(), query);
                break;
        }
        return result;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case LOADER_BUDGETS:
//                mAdapter.swapCursor(data);
                mAdapter.changeCursor(data);

                if (isResumed()) {
                    setListShown(true);
                } else {
                    setListShownNoAnimation(true);
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case LOADER_BUDGETS:
//                mAdapter.swapCursor(null);
                mAdapter.changeCursor(null);
                break;
        }
    }

    // Context Menu

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

        // get selected item name
        SimpleCursorAdapter adapter = (SimpleCursorAdapter) getListAdapter();
        Cursor cursor = (Cursor) adapter.getItem(info.position);

        menu.setHeaderTitle(cursor.getString(cursor.getColumnIndex(Budget.BUDGETYEARNAME)));

        MenuHelper menuHelper = new MenuHelper(getActivity(), menu);
        menuHelper.addEditToContextMenu();
        menuHelper.addDeleteToContextMenu();
        //todo menu.add(Menu.NONE, ContextMenuIds.COPY, Menu.NONE, getString(R.string.copy));
    }

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int budgetId = (int) info.id;
        int id = item.getItemId();
        ContextMenuIds menuId = ContextMenuIds.get(id);

        switch (menuId) {
            case EDIT:
                editBudget(budgetId);
                break;
            case DELETE:
                confirmDelete(budgetId);
                break;
            case COPY:
                BudgetService service = new BudgetService(getActivity());
                service.copy(budgetId);
                break;
            default:
                return false;
        }
        return false;
    }

    // Other

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        // Notify the parent to show the budget details.
        Cursor cursor = (Cursor) l.getItemAtPosition(position);
        String budgetName = cursor.getString(cursor.getColumnIndex(Budget.BUDGETYEARNAME));

        EventBus.getDefault().post(new BudgetSelectedEvent(id, budgetName));
    }

    @Override
    public void onFloatingActionButtonClicked() {
        createBudget();
    }

    // Private

    private void displayBudgets() {
        mAdapter = new MoneySimpleCursorAdapter(getActivity(),
                android.R.layout.simple_list_item_1,
                null,
                new String[]{ Budget.BUDGETYEARNAME },
                new int[]{ android.R.id.text1}, 0);

        setListAdapter(mAdapter);
        setListShown(false);

        getLoaderManager().initLoader(LOADER_BUDGETS, null, this);
    }

    private void editBudget(int budgetId) {
        Intent intent = new Intent(getActivity(), BudgetEditActivity.class);
        intent.putExtra(BudgetEditActivity.KEY_BUDGET_ID, budgetId);
        intent.setAction(Intent.ACTION_EDIT);
        //startActivity(intent);
        startActivityForResult(intent, REQUEST_EDIT_BUDGET);
    }

    private void createBudget() {
        Intent intent = new Intent(getActivity(), BudgetEditActivity.class);
        intent.setAction(Intent.ACTION_INSERT);
        startActivityForResult(intent, REQUEST_EDIT_BUDGET);
    }

    private void confirmDelete(final int budgetId) {
        new MaterialDialog.Builder(getActivity())
                .title(R.string.delete)
                .content(R.string.confirmDelete)
                .positiveText(android.R.string.ok)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        BudgetService service = new BudgetService(getActivity());
                        service.delete(budgetId);
                    }
                })
                .neutralText(android.R.string.cancel)
                .show();
    }
}
