/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
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
package com.money.manager.ex.budget;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.ListView;

import com.money.manager.ex.R;
import com.money.manager.ex.adapter.MoneySimpleCursorAdapter;
import com.money.manager.ex.common.BaseListFragment;
import com.money.manager.ex.database.BudgetYear;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BudgetsListFragment} interface
 * to handle interaction events.
 * Use the {@link BudgetsListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BudgetsListFragment
        extends BaseListFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private IBudgetListCallbacks mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment BudgetsListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static BudgetsListFragment newInstance() {
        BudgetsListFragment fragment = new BudgetsListFragment();
        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }

    }

//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_budgets_list, container, false);
//    }

    @Override
    public void onViewCreated (View view, Bundle savedInstanceState) {
        displayBudgets();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
//        try {
//            mListener = (OnFragmentInteractionListener) activity;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(activity.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    // Loader events

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Loader<Cursor> result = null;

        switch (id) {
            case LOADER_BUDGETS:
                BudgetYear budgetYear = new BudgetYear();
                result = new CursorLoader(getActivity(),
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

        // show context menu here.
//        getActivity().openContextMenu(v);

        // Notify the parent to show the budget details.
        Cursor cursor = (Cursor) l.getItemAtPosition(position);
        String budgetName = cursor.getString(cursor.getColumnIndex(BudgetYear.BUDGETYEARNAME));
        if (mListener != null) {
            mListener.onBudgetClicked(id, budgetName);
        }
    }

    public void setListener(IBudgetListCallbacks listener) {
        mListener = listener;
    }

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


}
