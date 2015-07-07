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
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ListView;

import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.common.BaseListFragment;
import com.money.manager.ex.common.MmexCursorLoader;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link IBudgetDetailCallbacks} interface
 * to handle interaction events.
 * Use the {@link BudgetDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BudgetDetailFragment
        extends BaseListFragment
        implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String ARG_BUDGET_YEAR_ID = "budgetYearId";
    private static final String ARG_BUDGET_NAME_ID = "budgetName";

    private final int LOADER_BUDGET = 1;
    private long mBudgetYearId = Constants.NOT_SET;
    private String mBudgetName;

    private IBudgetDetailCallbacks mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment BudgetDetailFragment.
     */
    public static BudgetDetailFragment newInstance(long budgetYearId, String budgetName) {
        BudgetDetailFragment fragment = new BudgetDetailFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_BUDGET_YEAR_ID, budgetYearId);
        args.putString(ARG_BUDGET_NAME_ID, budgetName);
        fragment.setArguments(args);

        return fragment;
    }

    public BudgetDetailFragment() {
        // Required empty public constructor
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

        ListView list = (ListView) view.findViewById(android.R.id.list);

        // Add the column header.
        View header = View.inflate(getActivity(), R.layout.item_budget_header, null);
        list.addHeaderView(header);
        // Header has to be added before the adapter is set on the list.

        setUpAdapter();

        return view;
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

    @Override
    public void onViewCreated (View view, Bundle savedInstanceState) {
        displayBudget();
    }

    // Loader

    /**
     * Instantiate and return a new Loader for the given ID.
     *
     * @param id   The ID whose loader is to be created.
     * @param args Any arguments supplied by the caller.
     * @return Return a new Loader instance that is ready to start loading.
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Loader<Cursor> result = null;

        switch (id) {
            case LOADER_BUDGET:
                BudgetQuery budget = new BudgetQuery(getActivity());

                 result = new MmexCursorLoader(getActivity(),
                        budget.getUri(),
                        budget.getAllColumns(),
                        BudgetQuery.BUDGETYEARID + "=?",
                        new String[] { Long.toString(mBudgetYearId) },
                        BudgetQuery.CATEGNAME + ", " + BudgetQuery.SUBCATEGNAME
                );
                break;
        }
        return result;
    }

    /**
     * Called when a previously created loader has finished its load.  Note
     * that normally an application is <em>not</em> allowed to commit fragment
     * transactions while in this call, since it can happen after an
     * activity's state is saved.  See {FragmentManager#beginTransaction()
     * FragmentManager.openTransaction()} for further discussion on this.
     * <p/>
     * <p>This function is guaranteed to be called prior to the release of
     * the last data that was supplied for this Loader.  At this point
     * you should remove all use of the old data (since it will be released
     * soon), but should not do your own release of the data since its Loader
     * owns it and will take care of that.  The Loader will take care of
     * management of its data so you don't have to.  In particular:
     * <p/>
     * <ul>
     * <li> <p>The Loader will monitor for changes to the data, and report
     * them to you through new calls here.  You should not monitor the
     * data yourself.  For example, if the data is a {@link Cursor}
     * and you place it in a {@link CursorAdapter}, use
     * the {@link CursorAdapter#CursorAdapter(Context,
     * Cursor, int)} constructor <em>without</em> passing
     * in either {@link CursorAdapter#FLAG_AUTO_REQUERY}
     * or {@link CursorAdapter#FLAG_REGISTER_CONTENT_OBSERVER}
     * (that is, use 0 for the flags argument).  This prevents the CursorAdapter
     * from doing its own observing of the Cursor, which is not needed since
     * when a change happens you will get a new Cursor throw another call
     * here.
     * <li> The Loader will release the data once it knows the application
     * is no longer using it.  For example, if the data is
     * a {@link Cursor} from a {@link CursorLoader},
     * you should not call close() on it yourself.  If the Cursor is being placed in a
     * {@link CursorAdapter}, you should use the
     * {@link CursorAdapter#swapCursor(Cursor)}
     * method so that the old Cursor is not closed.
     * </ul>
     *
     * @param loader The Loader that has finished.
     * @param data   The data generated by the Loader.
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case LOADER_BUDGET:
                BudgetAdapter adapter = (BudgetAdapter) getListAdapter();
                adapter.swapCursor(data);

                if (isResumed()) {
                    setListShown(true);
                } else {
                    setListShownNoAnimation(true);
                }
                break;
        }
    }

    /**
     * Called when a previously created loader is being reset, and thus
     * making its data unavailable.  The application should at this point
     * remove any references it has to the Loader's data.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case LOADER_BUDGET:
                BudgetAdapter adapter = (BudgetAdapter) getListAdapter();
                adapter.swapCursor(null);
                break;
        }
    }

    // Private

    private void displayBudget() {
        setListShown(false);

        getLoaderManager().initLoader(LOADER_BUDGET, null, this);
    }

    private void setUpAdapter() {
        BudgetAdapter adapter = new BudgetAdapter(getActivity(),
                null,
                new String[]{ BudgetQuery.CATEGNAME },
                new int[]{ R.id.categoryTextView },
                0);

        adapter.setBudgetName(mBudgetName);

        setListAdapter(adapter);
    }
}
