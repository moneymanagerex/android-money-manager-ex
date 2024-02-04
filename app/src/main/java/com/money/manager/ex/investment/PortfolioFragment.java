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
package com.money.manager.ex.investment;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.cursoradapter.widget.CursorAdapter;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import com.money.manager.ex.R;
import com.money.manager.ex.common.BaseListFragment;
import com.money.manager.ex.common.MmxCursorLoader;
import com.money.manager.ex.datalayer.Select;
import com.money.manager.ex.datalayer.StockFields;
import com.money.manager.ex.datalayer.StockRepository;
import com.money.manager.ex.domainmodel.Stock;

/**
 * Use the {@link PortfolioFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PortfolioFragment
        extends BaseListFragment {

    public static final int ID_LOADER = 1;
    private static final String ARG_ACCOUNT_ID = "accountId";
    private Integer mAccountId;

    public PortfolioFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param accountId Investment account Id
     * @return A new instance of fragment PortfolioFragment.
     */
    public static PortfolioFragment newInstance(final Integer accountId) {
        final PortfolioFragment fragment = new PortfolioFragment();
        final Bundle args = new Bundle();
        args.putInt(ARG_ACCOUNT_ID, accountId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public String getSubTitle() {
        return getString(R.string.portfolio);
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (null != savedInstanceState && savedInstanceState.containsKey(ARG_ACCOUNT_ID)) {
            // get data from saved instance state
            mAccountId = savedInstanceState.getInt(ARG_ACCOUNT_ID);
        } else {
            //if (getArguments() != null) {
            mAccountId = getArguments().getInt(ARG_ACCOUNT_ID);
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        if (null == container) return null;

        final View view = inflater.inflate(R.layout.fragment_portfolio, container, false);

        return view;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText(getString(R.string.no_stock_data));
        setListShown(false);

        // create adapter
        final PortfolioCursorAdapter adapter = new PortfolioCursorAdapter(getActivity(), null);

        initializeList();

        setListAdapter(adapter);

        initializeLoader();

        // hide the title
        // todo: uncomment this after setting the correct fragment type.
//        ActionBar actionBar = getActionBar();
//        if (actionBar != null) {
//            actionBar.setDisplayShowTitleEnabled(false);
//        }

        setFloatingActionButtonVisible(true);
        attachFloatingActionButtonToListView();
    }

    @Override
    public void onFloatingActionButtonClicked() {
        openEditInvestmentActivity(null);
    }

    @Override
    public void onSaveInstanceState(final Bundle saveInstanceState) {
        super.onSaveInstanceState(saveInstanceState);

        saveInstanceState.putInt(ARG_ACCOUNT_ID, mAccountId);
    }

    // Private

    private void initializeList() {
        // e list item click.
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                // Ignore the header row.
                if (0 < getListView().getHeaderViewsCount() && 0 == position) return;

                if (null != getListAdapter() && getListAdapter() instanceof PortfolioCursorAdapter) {
                    final Cursor cursor = (Cursor) getListAdapter().getItem(position);
                    final Stock stock = Stock.from(cursor);
                    openEditInvestmentActivity(stock.getId());
                }
            }
        });

    }

    private void initializeLoader() {
        // initialize loader
        getLoaderManager().initLoader(ID_LOADER, getArguments(), new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
                //animation
                setListShown(false);

                final StockRepository repo = new StockRepository(getActivity());
                final Select query = new Select(repo.getAllColumns())
                        .where(StockFields.HELDAT + " = " + args.getInt(ARG_ACCOUNT_ID))
                        .orderBy(StockFields.SYMBOL);
                //.orderBy(sort);

                return new MmxCursorLoader(getActivity(), repo.getUri(), query);
            }

            @Override
            public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {
                final CursorAdapter adapter = (CursorAdapter) getListAdapter();
                adapter.changeCursor(data);

                if (isResumed()) {
                    setListShown(true);

                    if (null != getFloatingActionButton()) {
                        getFloatingActionButton().show(true);
                    }
                } else {
                    setListShownNoAnimation(true);
                }
                // update the header
//   todo     displayHeaderData();
            }

            @Override
            public void onLoaderReset(final Loader<Cursor> loader) {
                ((CursorAdapter) getListAdapter()).changeCursor(null);
            }
        });
    }

    private void openEditInvestmentActivity(final Integer stockId) {
        final Intent intent = new Intent(getActivity(), InvestmentTransactionEditActivity.class);
        intent.putExtra(InvestmentTransactionEditActivity.ARG_ACCOUNT_ID, mAccountId);
        intent.putExtra(InvestmentTransactionEditActivity.ARG_STOCK_ID, stockId);
        intent.setAction(Intent.ACTION_INSERT);
        startActivity(intent);
    }

}
