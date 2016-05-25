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
package com.money.manager.ex.investment;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.money.manager.ex.R;
import com.money.manager.ex.common.BaseListFragment;
import com.money.manager.ex.common.MmexCursorLoader;
import com.money.manager.ex.datalayer.Query;
import com.money.manager.ex.datalayer.StockRepository;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PortfolioFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PortfolioFragment
    extends BaseListFragment
    implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String ARG_ACCOUNT_ID = "accountId";
    public static final int ID_LOADER = 1;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param accountId Investment account Id
     * @return A new instance of fragment PortfolioFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PortfolioFragment newInstance(Integer accountId) {
        PortfolioFragment fragment = new PortfolioFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_ACCOUNT_ID, accountId);
        fragment.setArguments(args);
        return fragment;
    }

    public PortfolioFragment() {
        // Required empty public constructor
    }

    private Integer mAccountId;

    @Override
    public String getSubTitle() {
        return null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null && savedInstanceState.containsKey(ARG_ACCOUNT_ID)) {
            // get data from saved instance state
            mAccountId = savedInstanceState.getInt(ARG_ACCOUNT_ID);
        } else {
            //if (getArguments() != null) {
            mAccountId = getArguments().getInt(ARG_ACCOUNT_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container == null) return null;

        View view = inflater.inflate(R.layout.fragment_portfolio, container, false);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText(getString(R.string.no_stock_data));
        setListShown(false);

        // hide the title
        // todo: uncomment this after setting the correct fragment type.
//        ActionBar actionBar = getActionBar();
//        if (actionBar != null) {
//            actionBar.setDisplayShowTitleEnabled(false);
//        }

        setFloatingActionButtonVisible(true);
        setFloatingActionButtonAttachListView(true);

    }

    @Override
    public void onFloatingActionButtonClickListener() {
        // todo: openEditInvestmentActivity();
    }

    @Override
    public void onSaveInstanceState(Bundle saveInstanceState) {
        super.onSaveInstanceState(saveInstanceState);

        saveInstanceState.putInt(ARG_ACCOUNT_ID, mAccountId);
    }

    // Loader

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        MmexCursorLoader result;

        //animation
        setListShown(false);

        StockRepository repo = new StockRepository(getActivity());
        Query query = new Query()
            .select(repo.getAllColumns());
            //.where(selection)
            //.orderBy(sort);

        result = new MmexCursorLoader(getActivity(), repo.getUri(), query);

        return result;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        ((CursorAdapter) getListAdapter()).changeCursor(null);
    }
}
