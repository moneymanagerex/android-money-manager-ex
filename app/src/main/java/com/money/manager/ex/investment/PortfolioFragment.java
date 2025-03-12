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
import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.money.manager.ex.R;
import com.money.manager.ex.common.BaseRecyclerFragment;
import com.money.manager.ex.datalayer.AccountRepository;
import com.money.manager.ex.datalayer.StockRepository;
import com.money.manager.ex.domainmodel.Account;
import com.money.manager.ex.viewmodels.StockViewModel;
import com.money.manager.ex.viewmodels.ViewModelFactory;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.Objects;

/**
 * Use the {@link PortfolioFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PortfolioFragment extends BaseRecyclerFragment {

    private static final String ARG_ACCOUNT_ID = "PortfolioFragment:accountId";

    private StockViewModel viewModel;
    private PortfolioListAdapter adapter;
    private Long mAccountId;
    private Account mAccount;
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

    @Override
    public String getSubTitle() {
        return getString(R.string.portfolio);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(ARG_ACCOUNT_ID)) {
                mAccountId = savedInstanceState.getLong(ARG_ACCOUNT_ID);
            }
        } else {
            Bundle args = getArguments();
            if (args != null) {
                mAccountId = args.getLong(ARG_ACCOUNT_ID);
            }
        }

        if (mAccountId > 0)
            mAccount = (new AccountRepository(requireContext())).load(mAccountId);

        StockRepository repository = new StockRepository(requireContext());
        ViewModelFactory factory = new ViewModelFactory(requireActivity().getApplication(), repository);
        viewModel = new ViewModelProvider(this, factory).get(StockViewModel.class);
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

        // Initialize RecyclerView
        adapter = new PortfolioListAdapter(getActivity(), this.mAccount);
        adapter.setOnItemClickListener(this::openEditInvestmentActivity);
        initializeRecyclerView();

        // Observe ViewModel
        viewModel.getStocks().observe(getViewLifecycleOwner(), stocks -> {
            adapter.submitList(stocks);
            if (stocks == null || stocks.isEmpty()) {
                setEmptyText(getString(R.string.no_stock_data));
            }
            setRecyclerViewShown(true);
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null && isLoading) {
                setRecyclerViewShown(false);
            }
        });

        // Load initial data
        viewModel.loadStocks(mAccountId);
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
        viewModel.loadStocks(mAccountId);
    }

    // Initialize RecyclerView
    private void initializeRecyclerView() {

        adapter.setOnItemClickListener(this::openEditInvestmentActivity);

        getRecyclerView().setLayoutManager(new LinearLayoutManager(getContext()));
        getRecyclerView().addItemDecoration(new DividerItemDecoration(Objects.requireNonNull(getContext()), DividerItemDecoration.VERTICAL));
        getRecyclerView().addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.bottom = 1;
            }
        });
        getRecyclerView().setAdapter(adapter);

        getRecyclerView().setNestedScrollingEnabled(false);
        getRecyclerView().setClickable(true);
        getRecyclerView().setFocusable(true);

        getRecyclerView().getParent().requestDisallowInterceptTouchEvent(true);
    }

    private void openEditInvestmentActivity(Long stockId) {
        Intent intent = new Intent(getActivity(), InvestmentTransactionEditActivity.class);
        intent.putExtra(InvestmentTransactionEditActivity.ARG_ACCOUNT_ID, mAccountId);
        intent.putExtra(InvestmentTransactionEditActivity.ARG_STOCK_ID, stockId);
        intent.setAction(Intent.ACTION_INSERT);
        startActivity(intent);
    }
}
