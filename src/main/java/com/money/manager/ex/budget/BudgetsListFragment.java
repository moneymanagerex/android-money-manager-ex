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
package com.money.manager.ex.budget;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.NumberPicker;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.money.manager.ex.R;
import com.money.manager.ex.adapter.MoneySimpleCursorAdapter;
import com.money.manager.ex.budget.events.BudgetSelectedEvent;
import com.money.manager.ex.common.BaseListFragment;
import com.money.manager.ex.common.MmexCursorLoader;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.database.BudgetYear;

import java.util.Calendar;

import de.greenrobot.event.EventBus;

/**
 * Activities that contain this fragment must implement the
 * {@link BudgetsListFragment} interface
 * to handle interaction events.
 * Use the {@link BudgetsListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BudgetsListFragment
    extends BaseListFragment
    implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment BudgetsListFragment.
     */
    public static BudgetsListFragment newInstance() {
        BudgetsListFragment fragment = new BudgetsListFragment();
        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    private final int LOADER_BUDGETS = 1;

    private MoneySimpleCursorAdapter mAdapter;

    public BudgetsListFragment() {
        // Required empty public constructor
    }

    @Override
    public String getSubTitle() {
        return getString(R.string.budget_list);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //todo setFloatingActionButtonVisible(true);
        //todo setFloatingActionButtonAttachListView(true);
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
    public void onViewCreated (View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        displayBudgets();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

//        outState.put(KEY_LISTENER, mListener);
    }

    // Loader events

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Loader<Cursor> result = null;

        switch (id) {
            case LOADER_BUDGETS:
                BudgetYear budgetYear = new BudgetYear();
                result = new MmexCursorLoader(getActivity(),
                        budgetYear.getUri(),
                        budgetYear.getAllColumns(),
                        null, null,
                        BudgetYear.BUDGETYEARNAME
                );
                break;
        }
        return result;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case LOADER_BUDGETS:
                mAdapter.swapCursor(data);

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
                mAdapter.swapCursor(null);
                break;
        }
    }

    // End loader events.

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        // Notify the parent to show the budget details.
        Cursor cursor = (Cursor) l.getItemAtPosition(position);
        String budgetName = cursor.getString(cursor.getColumnIndex(BudgetYear.BUDGETYEARNAME));

        EventBus.getDefault().post(new BudgetSelectedEvent(id, budgetName));
    }

    @Override
    public void onFloatingActionButtonClickListener() {
        // add new budget
        promptForBudgetName();
    }

    // Private

    private void displayBudgets() {
        mAdapter = new MoneySimpleCursorAdapter(getActivity(),
                android.R.layout.simple_list_item_1,
                null,
                new String[]{ BudgetYear.BUDGETYEARNAME },
                new int[]{ android.R.id.text1}, 0);

        setListAdapter(mAdapter);
        setListShown(false);

        getLoaderManager().initLoader(LOADER_BUDGETS, null, this);
    }

    private void promptForBudgetName() {
        View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_budget_period, null);

        // selector for year/month
        NumberPicker yearPicker = (NumberPicker) dialogView.findViewById(R.id.yearNumberPicker);
        yearPicker.setMinValue(2000);

        NumberPicker monthPicker = (NumberPicker) dialogView.findViewById(R.id.monthNumberPicker);
        monthPicker.setMinValue(1);
        monthPicker.setMaxValue(12);

        new MaterialDialog.Builder(getActivity())
            .title(R.string.add_budget)
            .customView(dialogView, false)
            .positiveText(android.R.string.ok)
            .onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    Core.alertDialog(getActivity(), "blah");
                }
            })
            .negativeText(android.R.string.cancel)
//            .neutralText(android.R.string.cancel)
            .show();
    }
}
