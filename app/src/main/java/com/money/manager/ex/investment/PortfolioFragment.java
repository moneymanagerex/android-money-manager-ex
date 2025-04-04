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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import android.view.ContextMenu;
import android.widget.Toast;

import com.money.manager.ex.R;
import com.money.manager.ex.common.BaseRecyclerFragment;
import com.money.manager.ex.core.ContextMenuIds;
import com.money.manager.ex.core.MenuHelper;
import com.money.manager.ex.datalayer.StockRepository;
import com.money.manager.ex.domainmodel.Account;
import com.money.manager.ex.domainmodel.Stock;
import com.money.manager.ex.utils.MmxDate;
import com.money.manager.ex.viewmodels.StockViewModel;
import com.money.manager.ex.viewmodels.ViewModelFactory;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.activity.result.ActivityResultLauncher;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

/**
 * Use the {@link PortfolioFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PortfolioFragment extends BaseRecyclerFragment {

    private static final String ARG_ACCOUNT_ID = "PortfolioFragment:accountId";

    private StockViewModel viewModel;
    private PortfolioListAdapter mAdapter;
    private Long mAccountId;
    private Account mAccount;
    private Stock selectedStock;

    private ActivityResultLauncher<Intent> editPriceLauncher;
    private ActivityResultLauncher<Intent> editInvestmentLauncher;

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
        return getString(R.string.portfolio) + (mAccount != null ? " : " + mAccount.getName() : "");
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
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupViewModel();
        enableFab(true);
        registerForContextMenu(getRecyclerView());
        setupActivityResultLaunchers();
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        menu.setHeaderTitle(selectedStock.getSymbol());

        MenuHelper menuHelper = new MenuHelper(requireActivity(), menu);
        menuHelper.addToContextMenu(ContextMenuIds.DownloadPrice);
        menuHelper.addToContextMenu(ContextMenuIds.EditPrice);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (selectedStock == null) return false;

        ContextMenuIds menuId = ContextMenuIds.get(item.getItemId());

        if (Objects.requireNonNull(menuId) == ContextMenuIds.EditPrice) {
            openEditPriceActivity(selectedStock);
            return true;
        } else if (Objects.requireNonNull(menuId) == ContextMenuIds.DownloadPrice) {
            viewModel.downloadStockPrice(selectedStock.getSymbol());
            return true;
        }

        return super.onContextItemSelected(item);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_portfolio;
    }

    @Override
    protected RecyclerView.Adapter<?> createAdapter() {
        mAdapter = new PortfolioListAdapter(getActivity());
        mAdapter.setOnItemClickListener(this::openEditInvestmentActivity);
        mAdapter.setOnItemLongClickListener((stock, view) -> {
            selectedStock = stock;
            requireActivity().openContextMenu(view);
        });

        return mAdapter;
    }

    private void setupViewModel() {
        StockRepository repository = new StockRepository(requireContext());
        ViewModelFactory factory = new ViewModelFactory(requireActivity().getApplication(), repository);
        viewModel = new ViewModelProvider(requireActivity(), factory).get(StockViewModel.class);

        viewModel.getStocks().observe(getViewLifecycleOwner(), stocks -> {
            mAdapter.submitList(stocks);
            checkEmpty();
        });

        viewModel.getLatestDownloadedPrice().observe(getViewLifecycleOwner(), priceModel -> {
            if (priceModel != null) {
                Toast.makeText(getContext(),
                        "Downloaded: " + priceModel.symbol + " @ " + priceModel.price,
                        Toast.LENGTH_SHORT).show();
                viewModel.loadStocks(mAccountId);
            }
        });

        viewModel.getAccount().observe(getViewLifecycleOwner(), account -> {
            mAccount = account;
            mAdapter.setAccount(account);
        });

        viewModel.loadStocks(mAccountId);
    }

    @Override
    public void onFabClicked() {
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

    private void openEditInvestmentActivity(Long stockId) {
        Intent intent = new Intent(getActivity(), InvestmentTransactionEditActivity.class);
        intent.putExtra(InvestmentTransactionEditActivity.ARG_ACCOUNT_ID, mAccountId);
        intent.putExtra(InvestmentTransactionEditActivity.ARG_STOCK_ID, stockId);
        intent.setAction(Intent.ACTION_INSERT);
        editPriceLauncher.launch(intent);
    }

    private void openEditPriceActivity(Stock stock) {
        Intent intent = new Intent(getActivity(), PriceEditActivity.class);
        intent.putExtra(EditPriceDialog.ARG_ACCOUNT, stock.getHeldAt());
        intent.putExtra(EditPriceDialog.ARG_SYMBOL, stock.getSymbol());
        intent.putExtra(EditPriceDialog.ARG_PRICE, stock.getCurrentPrice().toString());
        intent.putExtra(PriceEditActivity.ARG_CURRENCY_ID, mAccount.getCurrencyId());
        String dateString = new MmxDate().toIsoDateString();
        intent.putExtra(EditPriceDialog.ARG_DATE, dateString);
        editInvestmentLauncher.launch(intent);
    }

    private void setupActivityResultLaunchers() {
        editPriceLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        viewModel.loadStocks(mAccountId);
                    }
                }
        );

        editInvestmentLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        viewModel.loadStocks(mAccountId);
                    }
                }
        );
    }
}
