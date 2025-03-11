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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.money.manager.ex.R;
import com.money.manager.ex.common.BaseRecyclerFragment;
import com.money.manager.ex.common.MmxCursorLoader;
import com.money.manager.ex.datalayer.Select;
import com.money.manager.ex.datalayer.StockFields;
import com.money.manager.ex.datalayer.StockRepository;

import androidx.annotation.NonNull;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Use the {@link PortfolioFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PortfolioFragment extends BaseRecyclerFragment {

    private static final String ARG_ACCOUNT_ID = "PortfolioFragment:accountId";
    public static final int ID_LOADER = 1;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param accountId Investment account Id
     * @return A new instance of fragment PortfolioFragment.
     */
    public static PortfolioFragment newInstance(Long accountId) {
        PortfolioFragment fragment = new PortfolioFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_ACCOUNT_ID, accountId);
        fragment.setArguments(args);
        return fragment;
    }

    private Long mAccountId;
    private PortfolioRecyclerAdapter mAdapter;

    @Override
    public String getSubTitle() {
        return getString(R.string.portfolio);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null && savedInstanceState.containsKey(ARG_ACCOUNT_ID)) {
            mAccountId = savedInstanceState.getLong(ARG_ACCOUNT_ID);
        } else {
            mAccountId = getArguments().getLong(ARG_ACCOUNT_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container == null) return null;

        // Inflate the layout for the fragment
        return inflater.inflate(R.layout.fragment_portfolio, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setEmptyText(getString(R.string.no_stock_data));
        setRecyclerViewShown(false);

        // Create adapter and set to RecyclerView
        mAdapter = new PortfolioRecyclerAdapter(getActivity(), null);
        initializeRecyclerView();

        setAdapter(mAdapter);

        initializeLoader();

        setFabVisible(true);
    }

    @Override
    public void onFloatingActionButtonClicked() {
        openEditInvestmentActivity(null);
    }

    @Override
    public void onSaveInstanceState(Bundle saveInstanceState) {
        super.onSaveInstanceState(saveInstanceState);

        saveInstanceState.putLong(ARG_ACCOUNT_ID, mAccountId);
    }

    @Override
    public void onResume() {
        super.onResume();
        initializeLoader();
    }

    // Initialize RecyclerView
    private void initializeRecyclerView() {
        getRecyclerView().setLayoutManager(new LinearLayoutManager(getContext()));
        getRecyclerView().setNestedScrollingEnabled(false);
        getRecyclerView().setClickable(true);
        getRecyclerView().setFocusable(true);

        // 确保父布局不拦截事件
        getRecyclerView().getParent().requestDisallowInterceptTouchEvent(true);

        getRecyclerView().addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                View child = rv.findChildViewUnder(e.getX(), e.getY());
                if (child != null && e.getAction() == MotionEvent.ACTION_DOWN) {
                    int position = rv.getChildAdapterPosition(child);
                    if (position != RecyclerView.NO_POSITION) {
                        PortfolioRecyclerAdapter.StockItem stockItem = mAdapter.getItem(position);
                        if (stockItem != null) {
                            openEditInvestmentActivity(stockItem.stockId);
                        }
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private void initializeLoader() {
        // Initialize loader
        LoaderManager.getInstance(this).initLoader(ID_LOADER, getArguments(), new LoaderManager.LoaderCallbacks<Cursor>() {
            @NonNull
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                // Show loading animation
                setRecyclerViewShown(false);

                StockRepository repo = new StockRepository(getActivity());
                Select query = new Select(repo.getAllColumns())
                        .where(StockFields.HELDAT + " = " + args.getLong(ARG_ACCOUNT_ID))
                        .orderBy(StockFields.SYMBOL);

                return new MmxCursorLoader(getActivity(), repo.getUri(), query);
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                mAdapter.swapCursor(data);

                if (isResumed()) {
                    setRecyclerViewShown(true);
                    setFabVisible(true);
                } else {
                    setRecyclerViewShownNoAnimation(true);
                }
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
                mAdapter.swapCursor(null);
            }
        });
    }

    private void openEditInvestmentActivity(Long stockId) {
        Intent intent = new Intent(getActivity(), InvestmentTransactionEditActivity.class);
        intent.putExtra(InvestmentTransactionEditActivity.ARG_ACCOUNT_ID, mAccountId);
        intent.putExtra(InvestmentTransactionEditActivity.ARG_STOCK_ID, stockId);
        intent.setAction(Intent.ACTION_INSERT);
        startActivity(intent);
    }
}
