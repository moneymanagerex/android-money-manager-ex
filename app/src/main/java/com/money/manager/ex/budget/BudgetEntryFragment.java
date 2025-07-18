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
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.common.BaseListFragment;
import com.money.manager.ex.common.MmxCursorLoader;
import com.money.manager.ex.core.ContextMenuIds;
import com.money.manager.ex.core.MenuHelper;
import com.money.manager.ex.datalayer.BudgetEntryRepository;
import com.money.manager.ex.datalayer.Select;
import com.money.manager.ex.domainmodel.Budget;
import com.money.manager.ex.domainmodel.BudgetEntry;
import com.money.manager.ex.nestedcategory.QueryNestedCategory;
import com.money.manager.ex.scheduled.ScheduledTransactionForecastListServices;
import com.money.manager.ex.search.CategorySub;
import com.money.manager.ex.search.SearchActivity;
import com.money.manager.ex.search.SearchParameters;
import com.money.manager.ex.settings.AppSettings;

import androidx.annotation.NonNull;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import org.parceler.Parcels;

import java.util.ArrayList;

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

        if (view == null) throw new AssertionError();
        ListView list = view.findViewById(android.R.id.list);

        // Add the column header.
        // switch to simple layout if the showSimpleView is set
        AppSettings settings = new AppSettings(getContext());
        int layout = (settings.getBudgetSettings().getShowSimpleView()) ? R.layout.item_budget_simple_header : R.layout.item_budget_header;

        mHeader = View.inflate(getActivity(), layout, null);
        list.addHeaderView(mHeader);
        // Header has to be added before the adapter is set on the list.

        setUpAdapter();

        setHasOptionsMenu(true);

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
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        displayBudget();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setFabVisible(true);
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
        adapter.setBudgetYearId(mBudgetYearId);

        adapter.setVisibleTextFieldsForView(mHeader);
        setListAdapter(adapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // set accounts Filter
        inflater.inflate(R.menu.menu_budget, menu);

        boolean useBudgetFinancialYear = (new AppSettings(getContext())).getBudgetSettings().getBudgetFinancialYear();
        if (menu.findItem(R.id.menu_budget_financial_year) != null) {
            if (Budget.isMonthlyBudget(mBudgetName)) {
                // does not sense to have financial year for monthly budget
                menu.findItem(R.id.menu_budget_financial_year).setVisible(false);
            } else {
                menu.findItem(R.id.menu_budget_financial_year).setVisible(true);
                menu.findItem(R.id.menu_budget_financial_year).setChecked(useBudgetFinancialYear);
            }
        }

        boolean useBudgetSimplifyView = (new AppSettings(getContext())).getBudgetSettings().getShowSimpleView();
        if (menu.findItem(R.id.menu_budget_use_simple_view) != null) {
            menu.findItem(R.id.menu_budget_use_simple_view).setChecked(useBudgetSimplifyView);
        }
        menu.findItem(R.id.menu_budget_columns).setVisible(!useBudgetSimplifyView);

        // TODO apply nested budget remove this and enable comment, need to tested
        if (menu.findItem(R.id.menu_budget_category_with_sub) != null) {
            menu.findItem(R.id.menu_budget_category_with_sub).setVisible(false);
        }
/*        // set menu_budget_category_with_sub
        boolean useSubCategory = (new AppSettings(getContext())).getBudgetSettings().get(R.id.menu_budget_category_with_sub,false);
        if (menu.findItem(R.id.menu_budget_category_with_sub) != null) {
            menu.findItem(R.id.menu_budget_category_with_sub).setChecked(useSubCategory);
        }
*/

        // Add selectable columns name
        ArrayList<Integer> visibleColumn = ((BudgetAdapter) getListAdapter()).getVisibleColumn();
        if (menu.findItem(R.id.menu_budget_columns) != null && menu.findItem(R.id.menu_budget_columns).isVisible()) {
            Menu menuColumns = menu.findItem(R.id.menu_budget_columns).getSubMenu();
            for(int i = 0; i < (menuColumns != null ? menuColumns.size() : 0); i++) {
                menuColumns.getItem(i).setChecked(visibleColumn.contains(menuColumns.getItem(i).getItemId()));
            }

            // add forecast sill to be implemented
            // menu.findItem(R.id.forecastRemainTextView).setVisible(false);
        }


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_budget_reload_forecast ) {
            ScheduledTransactionForecastListServices.destroyInstance();
            restartLoader();
        }
        if (item.getItemId() == R.id.menu_budget_financial_year) {
            item.setChecked(!item.isChecked());
            (new AppSettings(getContext())).getBudgetSettings().setBudgetFinancialYear(item.isChecked());
            restartLoader();
            return true;
        }
        if (item.getItemId() == R.id.menu_budget_use_simple_view) {
            item.setChecked(!item.isChecked());
            (new AppSettings(getContext())).getBudgetSettings().setShowSimpleView(item.isChecked());
            restartLoader();
            return true;
        }
        if (item.getItemId() == R.id.menu_budget_category_with_sub ) {
            item.setChecked(!item.isChecked());
            (new AppSettings(getContext())).getBudgetSettings().set(R.id.menu_budget_category_with_sub,item.isChecked());
            restartLoader();
            return true;
        }

        if (    item.getItemId() == R.id.frequencyTextView ||
                item.getItemId() == R.id.amountTextView ||
                item.getItemId() == R.id.estimatedForPeriodTextView ||
                item.getItemId() == R.id.actualTextView ||
                item.getItemId() == R.id.amountAvailableTextView ||
                item.getItemId() == R.id.forecastRemainTextView) {

            item.setChecked(!item.isChecked());
            (new AppSettings(getContext())).getBudgetSettings().setColumnVisible(item.getItemId(), item.isChecked());
            restartLoader();
            return true;
        }

        return false;
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

        if (info.id == -1) return;

        // get selected item name
        SimpleCursorAdapter adapter = (SimpleCursorAdapter) getListAdapter();
        Cursor cursor = (Cursor) adapter.getItem(info.position - 1);

        menu.setHeaderTitle(cursor.getString(cursor.getColumnIndexOrThrow(BudgetNestedQuery.CATEGNAME)));

        MenuHelper menuHelper = new MenuHelper(getActivity(), menu);
        menuHelper.addEditToContextMenu();

        BudgetEntryRepository repo = new BudgetEntryRepository(getActivity());
        menuHelper.addDeleteToContextMenu(repo.hasBudget(mBudgetYearId, info.id));

        menu.add(Menu.NONE, ContextMenuIds.VIEW_TRANSACTIONS.getId(), Menu.NONE, getString(R.string.view_transactions));

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (info == null) return false;

        long categoryId = info.id;
        int id = item.getItemId();
        ContextMenuIds menuId = ContextMenuIds.get(id);
        if (menuId == null) return false;

        // get selected item name
        BudgetAdapter adapter = (BudgetAdapter) getListAdapter();
        Cursor cursor = (Cursor) adapter.getItem(info.position - 1); // -1 because of header

        switch (menuId) {
            case EDIT:
                editBudgetEntry(mBudgetYearId, categoryId, cursor.getString(cursor.getColumnIndexOrThrow(BudgetNestedQuery.CATEGNAME)));
                break;
            case DELETE:
                confirmDelete(mBudgetYearId, categoryId);
                break;
            case VIEW_TRANSACTIONS:
                SearchParameters parameters = new SearchParameters();
                CategorySub catSub = new CategorySub();
                catSub.categId = categoryId;
                catSub.categName = cursor.getString(cursor.getColumnIndexOrThrow(BudgetNestedQuery.CATEGNAME));
                parameters.category = catSub;
                parameters.dateFrom = adapter.getDateFrom().toDate();
                parameters.dateTo = adapter.getDateTo().toDate();
                showSearchActivityFor(parameters);
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
                    BudgetEntry budgetEntry = repo.loadByYearIdAndCateID(yearId, categoryId);
                    if (budgetEntry != null) {
                        repo.delete(budgetEntry.getId());
                        restartLoader();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    public void editBudgetEntry(long budgetYearId, long categoryId, String category) {
        String budgetPeriodString;
        // Set the custom layout
        LayoutInflater inflater = getLayoutInflater();
        View customLayout = inflater.inflate(R.layout.budget_edit_entry, null);

        // Create the EditText view for numeric input
        final EditText input = customLayout.findViewById(R.id.budget_edit_value);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        input.setHint(R.string.enter_budget_value_plus_minus);

        BudgetEntryRepository budgetEntryRepository = new BudgetEntryRepository(getActivity());


        BudgetEntry budgetEntry = budgetEntryRepository.loadByYearIdAndCateID(budgetYearId, categoryId);
        if (budgetEntry != null) {
            // Set the current value as the initial value in the EditText
            input.setText(String.valueOf(budgetEntry.getAmount()));
            budgetPeriodString = budgetEntry.getPeriod();
        } else {
            budgetPeriodString = (Budget.isMonthlyBudget(mBudgetName) ? BudgetPeriodEnum.MONTHLY.getDisplayName() : BudgetPeriodEnum.YEARLY.getDisplayName());
        }

        Spinner spinnerBudget = customLayout.findViewById(R.id.spinnerBudgetPeriod);
        ArrayList<String> mBudgetListName = new ArrayList<>();
        ArrayList<BudgetPeriodEnum>  mBudgetListId = new ArrayList<>();
        int defPeriod = 0;
        for (int x = 0; x < BudgetPeriodEnum.values().length; x++) {
            mBudgetListId.add(BudgetPeriodEnum.values()[x]);
            mBudgetListName.add(BudgetPeriodEnum.getTranslation(getContext(), BudgetPeriodEnum.values()[x]));
            if (BudgetPeriodEnum.values()[x].getDisplayName().equals(budgetPeriodString)) {
                defPeriod = x;
            }
        }

        ArrayAdapter<String> budgetPeriodAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, mBudgetListName);

        budgetPeriodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBudget.setAdapter(budgetPeriodAdapter);
        spinnerBudget.setSelection(defPeriod);

        // Set up the dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        // set custom xml layout to manage both period and amount
        builder.setView(customLayout);

        builder.setTitle(R.string.enter_budget)
                .setMessage(getString(R.string.enter_budget_value,category));
        //  Old Mode
        //        .setView(input);
        builder
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newValue = input.getText().toString().trim();
                        BudgetPeriodEnum selectedPeriod = mBudgetListId.get(spinnerBudget.getSelectedItemPosition());
                        if (selectedPeriod == null) {
                            selectedPeriod = mBudgetListId.get(0);
                        }
                        if (newValue.isEmpty()) {
                            Toast.makeText(getActivity(), "Value cannot be empty", Toast.LENGTH_SHORT).show();
                        } else {
                            try {
                                // Parse the input as a double
                                double newValueNumeric = Double.parseDouble(newValue);
                                BudgetEntry budgetEntry = budgetEntryRepository.loadByYearIdAndCateID(mBudgetYearId, categoryId);
                                if (budgetEntry == null) {
                                    budgetEntry = new BudgetEntry();
                                    budgetEntry.setBudgetYearId(mBudgetYearId);
                                    budgetEntry.setCategoryId(categoryId);
                                }

                                budgetEntry.setPeriod(selectedPeriod.getDisplayName());
                                budgetEntry.setAmount(newValueNumeric);
                                budgetEntryRepository.save(budgetEntry);
                                Toast.makeText(getActivity(), "Budget entry updated", Toast.LENGTH_SHORT).show();
                                restartLoader();
                            } catch (NumberFormatException e) {
                                Toast.makeText(getActivity(), "Invalid number format", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
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
                            .orderBy(QueryNestedCategory.CATEGNAME)
                            .where(QueryNestedCategory.ACTIVE + " = 1");
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

    private void restartLoader() {
//  getloader does not restart loader????
//        getLoaderManager().restartLoader(LOADER_BUDGET, null, setUpLoaderCallbacks());
        getActivity().recreate();
    }

    private void showSearchActivityFor(SearchParameters parameters) {
        Intent intent = new Intent(getActivity(), SearchActivity.class);
        intent.putExtra(SearchActivity.EXTRA_SEARCH_PARAMETERS, Parcels.wrap(parameters));
        intent.setAction(Intent.ACTION_INSERT);
        startActivity(intent);
    }

}
