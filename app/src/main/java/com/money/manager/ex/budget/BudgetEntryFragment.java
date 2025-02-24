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
package com.money.manager.ex.budget;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.common.BaseListFragment;
import com.money.manager.ex.common.MmxCursorLoader;
import com.money.manager.ex.core.ContextMenuIds;
import com.money.manager.ex.core.MenuHelper;
import com.money.manager.ex.datalayer.BudgetEntryRepository;
import com.money.manager.ex.datalayer.Select;
import com.money.manager.ex.domainmodel.BudgetEntry;
import com.money.manager.ex.nestedcategory.QueryNestedCategory;
import com.money.manager.ex.settings.AppSettings;

import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

/**
 * Use the {@link BudgetEntryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BudgetEntryFragment
        extends BaseListFragment {

    private static final String ARG_BUDGET_YEAR_ID = "budgetYearId";
    private static final String ARG_BUDGET_NAME_ID = "budgetName";

    private final int LOADER_BUDGET = 1;
    private long mBudgetYearId = Constants.NOT_SET;
    private String mBudgetName;
    private View mHeader;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment BudgetEntryFragment.
     */
    public static BudgetEntryFragment newInstance(long budgetYearId, String budgetName) {
        BudgetEntryFragment fragment = new BudgetEntryFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_BUDGET_YEAR_ID, budgetYearId);
        args.putString(ARG_BUDGET_NAME_ID, budgetName);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public String getSubTitle() {
        return mBudgetName;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mBudgetYearId = getArguments().getLong(ARG_BUDGET_YEAR_ID);
            mBudgetName = getArguments().getString(ARG_BUDGET_NAME_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_budget_detail, container, false);
        View view = super.onCreateView(inflater, container, savedInstanceState);

        ListView list = view.findViewById(android.R.id.list);

        // Add the column header.
        // switch to simple layout if the showSimpleView is set
        AppSettings settings = new AppSettings(getContext());
        int layout = (settings.getBudgetSettings().getShowSimpleView()) ? R.layout.item_budget_simple_header : R.layout.item_budget_header;

        mHeader = View.inflate(getActivity(), layout, null);
        list.addHeaderView(mHeader);
        // Header has to be added before the adapter is set on the list.

        setUpAdapter();

        return view;
    }

    // Got random IndexOutOfBoundsException during loading the new fragment
    // reference: http://stackoverflow.com/a/28463811
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (getListView().getHeaderViewsCount() > 0) {
            getListView().removeHeaderView(mHeader);
        }
    }

    @Override
    public void onViewCreated (View view, Bundle savedInstanceState) {
        displayBudget();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setFloatingActionButtonVisible(true);
        attachFloatingActionButtonToListView();

        registerForContextMenu(getListView());
    }

    // Private

    private void displayBudget() {
        setListShown(false);

        LoaderManager.LoaderCallbacks<Cursor> callbacks = setUpLoaderCallbacks();
        LoaderManager.getInstance(this).initLoader(LOADER_BUDGET, null, callbacks);
    }

    private void setUpAdapter() {
        BudgetAdapter adapter = new BudgetAdapter(getActivity(),
                null,
                new String[]{BudgetNestedQuery.CATEGNAME},
                new int[]{R.id.categoryTextView},
                0);
        adapter.setBudgetName(mBudgetName);
        adapter.setBudgetYearId(mBudgetYearId);

        setListAdapter(adapter);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

        // get selected item name
        SimpleCursorAdapter adapter = (SimpleCursorAdapter) getListAdapter();
        Cursor cursor = (Cursor) adapter.getItem(info.position);

        menu.setHeaderTitle(cursor.getString(cursor.getColumnIndexOrThrow(BudgetNestedQuery.CATEGNAME)));

        MenuHelper menuHelper = new MenuHelper(getActivity(), menu);
        menuHelper.addEditToContextMenu();

        BudgetEntryRepository repo = new BudgetEntryRepository(getActivity());
        menuHelper.addDeleteToContextMenu(repo.hasBudget(mBudgetYearId, info.id));
    }

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        long categoryId = info.id;
        int id = item.getItemId();
        ContextMenuIds menuId = ContextMenuIds.get(id);

        switch (menuId) {
            case EDIT:
                editBudgetEntry(mBudgetYearId, categoryId);
                break;
            case DELETE:
                confirmDelete(mBudgetYearId, categoryId);
                break;
            default:
                return false;
        }
        return false;
    }

    private void confirmDelete(long yearId, long categoryId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.delete)
                .setMessage(R.string.confirmDelete)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    BudgetEntryRepository repo = new BudgetEntryRepository(getActivity());
                    BudgetEntry budgetEntry = repo.loadByYearAndCateID(yearId, categoryId);
                    if (budgetEntry != null)
                        repo.delete(budgetEntry.getId());
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    public void editBudgetEntry(long budgetYearId, long categoryId) {
        // Create the EditText view for numeric input
        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("Enter new budget");

        BudgetEntryRepository budgetEntryRepository = new BudgetEntryRepository(getActivity());

        BudgetEntry budgetEntry = budgetEntryRepository.loadByYearAndCateID(budgetYearId, categoryId);
        if (budgetEntry != null) {
            // Set the current value as the initial value in the EditText
            input.setText(String.valueOf(budgetEntry.getAmount()));
        }

        // Set up the dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Edit Budget Entry")
                .setMessage("Enter the new budget value:")
                .setView(input)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newValue = input.getText().toString().trim();
                        if (newValue.isEmpty()) {
                            Toast.makeText(getActivity(), "Value cannot be empty", Toast.LENGTH_SHORT).show();
                        } else {
                            try {
                                // Parse the input as a double
                                double newValueNumeric = Double.parseDouble(newValue);
                                BudgetEntry budgetEntry = budgetEntryRepository.loadByYearAndCateID(mBudgetYearId, categoryId);
                                if (budgetEntry == null) {
                                    budgetEntry = new BudgetEntry();
                                    budgetEntry.setBudgetYearId(mBudgetYearId);
                                    budgetEntry.setCategoryId(categoryId);
                                }

                                budgetEntry.setAmount(newValueNumeric);
                                budgetEntryRepository.save(budgetEntry);
                                Toast.makeText(getActivity(), "Budget entry updated", Toast.LENGTH_SHORT).show();
                            } catch (NumberFormatException e) {
                                Toast.makeText(getActivity(), "Invalid number format", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private LoaderManager.LoaderCallbacks<Cursor> setUpLoaderCallbacks() {
        return new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                Loader<Cursor> result = null;

                if (id == LOADER_BUDGET) {
                    // useNestedCategory
                    QueryNestedCategory categories = new QueryNestedCategory(getActivity());
                    Select query = new Select(categories.getAllColumns())
                            .orderBy(QueryNestedCategory.CATEGNAME);
                    result = new MmxCursorLoader(getActivity(), categories.getUri(), query);
                }
                return result;
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                if (loader.getId() == LOADER_BUDGET) {
                    BudgetAdapter adapter = (BudgetAdapter) getListAdapter();
//                        adapter.swapCursor(data);
                    adapter.changeCursor(data);

                    if (isResumed()) {
                        setListShown(true);
                    } else {
                        setListShownNoAnimation(true);
                    }
                }
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
                if (loader.getId() == LOADER_BUDGET) {
                    BudgetAdapter adapter = (BudgetAdapter) getListAdapter();
//                        adapter.swapCursor(null);
                    adapter.changeCursor(null);
                }
            }
        };
    }
}
