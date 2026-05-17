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
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import android.view.ContextMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.common.BaseRecyclerFragment;
import com.money.manager.ex.core.ContextMenuIds;
import com.money.manager.ex.core.MenuHelper;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.datalayer.AccountTransactionRepository;
import com.money.manager.ex.datalayer.ShareInfoRepository;
import com.money.manager.ex.datalayer.SplitCategoryRepository;
import com.money.manager.ex.datalayer.StockRepository;
import com.money.manager.ex.datalayer.TaglinkRepository;
import com.money.manager.ex.datalayer.TransactionLinkRepository;
import com.money.manager.ex.database.ISplitTransaction;
import com.money.manager.ex.domainmodel.AccountTransaction;
import com.money.manager.ex.domainmodel.Account;
import com.money.manager.ex.domainmodel.Stock;
import com.money.manager.ex.domainmodel.StockHistory;
import com.money.manager.ex.domainmodel.TransactionLink;
import com.money.manager.ex.domainmodel.RefType;
import com.money.manager.ex.home.MainActivity;
import com.money.manager.ex.utils.MmxDate;
import com.money.manager.ex.viewmodels.StockViewModel;
import com.money.manager.ex.viewmodels.ViewModelFactory;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.activity.result.ActivityResultLauncher;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.preference.PreferenceManager;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;

/**
 * Use the {@link PortfolioFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PortfolioFragment extends BaseRecyclerFragment {

    private static final String ARG_ACCOUNT_ID = "PortfolioFragment:accountId";
    private static final int MENU_DOWNLOAD_ALL_PRICES = 1001;
    private static final int MENU_VIEW_CASH_LEDGER = 1002;
    private static final int MENU_HIDE_ZERO_SHARES = 1003;

    private StockViewModel viewModel;
    private PortfolioListAdapter mAdapter;
    private Long mAccountId;
    private Account mAccount;
    private Stock selectedStock;
    private boolean isBulkDownloadInProgress;
    private boolean isMenuProviderRegistered;
    private List<Stock> mDisplayedStocks;

    private ActivityResultLauncher<Intent> editPriceLauncher;
    private ActivityResultLauncher<Intent> editInvestmentLauncher;

    // Header views (from merge_header_fragment_account)
    private TextView txtTotalBalance;
    private TextView txtCashBalance;
    private TextView txtMarketValue;
    private TextView txtInvested;
    private TextView txtGainLoss;

    // Cached values for combined header update
    private Money mAccountBalance;
    private List<Stock> mStocks;

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
        bindHeaderViews(view);
        setupMenuProvider();
        setupViewModel();
        enableFab(true);
        registerForContextMenu(getRecyclerView());
        setupActivityResultLaunchers();
    }

    private void bindHeaderViews(@NonNull View root) {
        // Relabel the first "Account Balance" row to "Total Balance" and hide reconciled row.
        TextView titleView = root.findViewById(R.id.textViewAccountBalanceTitle);
        if (titleView != null) titleView.setText(R.string.total_balance);
        View reconciledTitle = root.findViewById(R.id.textViewAccountReconciledTitle);
        if (reconciledTitle != null) reconciledTitle.setVisibility(View.GONE);
        View reconciledValue = root.findViewById(R.id.textViewAccountReconciled);
        if (reconciledValue != null) reconciledValue.setVisibility(View.GONE);

        txtTotalBalance = root.findViewById(R.id.textViewAccountBalance);
        View tableRowCashBalance = root.findViewById(R.id.tableRowCashBalance);
        txtCashBalance = root.findViewById(R.id.textViewCashBalance);
        View tableRowMarketValue = root.findViewById(R.id.tableRowMarketValue);
        txtMarketValue = root.findViewById(R.id.textViewMarketValue);
        View tableRowInvested = root.findViewById(R.id.tableRowInvested);
        txtInvested = root.findViewById(R.id.textViewInvested);
        View tableRowGainLoss = root.findViewById(R.id.tableRowGainLoss);
        txtGainLoss = root.findViewById(R.id.textViewGainLoss);

        if (tableRowCashBalance != null) tableRowCashBalance.setVisibility(View.VISIBLE);
        if (tableRowMarketValue != null) tableRowMarketValue.setVisibility(View.VISIBLE);
        if (tableRowInvested != null) tableRowInvested.setVisibility(View.VISIBLE);
        if (tableRowGainLoss != null) tableRowGainLoss.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isMenuProviderRegistered = false;
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        menu.setHeaderTitle(getStockLabel(selectedStock));

        MenuHelper menuHelper = new MenuHelper(requireActivity(), menu);
        menu.add(Menu.NONE, ContextMenuIds.VIEW_TRANSACTIONS.getId(), Menu.NONE, R.string.view_transactions);
        menu.add(Menu.NONE, ContextMenuIds.OPEN_YAHOO_FINANCE.getId(), Menu.NONE, R.string.open_yahoo_finance);
        menuHelper.addToContextMenu(ContextMenuIds.PriceHistory);
        menuHelper.addToContextMenu(ContextMenuIds.DELETE);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (selectedStock == null) return false;

        ContextMenuIds menuId = ContextMenuIds.get(item.getItemId());

        if (Objects.requireNonNull(menuId) == ContextMenuIds.VIEW_TRANSACTIONS) {
            openStockTransactions(selectedStock);
            return true;
        } else if (Objects.requireNonNull(menuId) == ContextMenuIds.DELETE) {
            showDeleteStockConfirmationDialog(selectedStock);
            return true;
        } else if (Objects.requireNonNull(menuId) == ContextMenuIds.PriceHistory) {
            openEditPriceActivity(selectedStock);
            return true;
        } else if (Objects.requireNonNull(menuId) == ContextMenuIds.OPEN_YAHOO_FINANCE) {
            openYahooFinance(selectedStock);
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
        viewModel.clearDownloadEvents();

        viewModel.getStocks().observe(getViewLifecycleOwner(), stocks -> {
            mStocks = stocks;
            updateDisplayedStocks();
            checkEmpty();
            updateInvestmentHeader();
        });

        viewModel.getAccountBalance().observe(getViewLifecycleOwner(), balance -> {
            mAccountBalance = balance;
            updateInvestmentHeader();
        });

        viewModel.getLatestDownloadedPrice().observe(getViewLifecycleOwner(), priceModel -> {
            if (priceModel != null && !isBulkDownloadInProgress) {
                Toast.makeText(getContext(),
                        getString(R.string.downloaded_price_for_symbol, priceModel.symbol, priceModel.price),
                        Toast.LENGTH_SHORT).show();
                viewModel.loadStocks(mAccountId);
            }
        });

        viewModel.getAllDownloadedPricesResult().observe(getViewLifecycleOwner(), result -> {
            if (result == null || result.length < 2 || !isBulkDownloadInProgress) return;

            isBulkDownloadInProgress = false;

            Toast.makeText(
                    getContext(),
                    getString(R.string.downloaded_prices_count, result[0], result[1]),
                    Toast.LENGTH_SHORT
            ).show();
            viewModel.loadStocks(mAccountId);
        });

        viewModel.getAccount().observe(getViewLifecycleOwner(), account -> {
            mAccount = account;
            mAdapter.setAccount(account);
            updateSubtitle();
            updateInvestmentHeader();
        });

        viewModel.loadStocks(mAccountId);
    }

    private void updateInvestmentHeader() {
        if (mAccount == null || mAccountBalance == null || mStocks == null) return;
        if (txtTotalBalance == null) return;

        CurrencyService currencyService = new CurrencyService(requireContext().getApplicationContext());
        long currencyId = mAccount.getCurrencyId();
        InvestmentTotals totals = calculateInvestmentTotals(mStocks, mAccountBalance);

        txtTotalBalance.setText(currencyService.getCurrencyFormatted(currencyId, mAccountBalance));
        setHeaderAmount(txtCashBalance, currencyService, currencyId, totals.cashBalance);
        setHeaderAmount(txtMarketValue, currencyService, currencyId, totals.marketValue);
        setHeaderAmount(txtInvested, currencyService, currencyId, totals.invested);
        if (txtGainLoss != null) {
            txtGainLoss.setText(formatGainLoss(currencyService, currencyId, totals.gainLoss, totals.invested));
        }
    }

    private InvestmentTotals calculateInvestmentTotals(List<Stock> stocks, Money accountBalance) {
        Money marketValue = MoneyFactory.fromDouble(0);
        Money invested = MoneyFactory.fromDouble(0);
        for (Stock s : stocks) {
            marketValue = marketValue.add(s.getCurrentPrice().multiply(s.getNumberOfShares()));
            // invested uses cost basis (VALUE field) which includes commission
            invested = invested.add(s.getValue());
        }

        Money cashBalance = accountBalance.subtract(marketValue);
        Money gainLoss = marketValue.subtract(invested);
        return new InvestmentTotals(cashBalance, marketValue, invested, gainLoss);
    }

    private void setHeaderAmount(TextView view, CurrencyService currencyService, long currencyId,
                                 Money value) {
        if (view != null) {
            view.setText(currencyService.getCurrencyFormatted(currencyId, value));
        }
    }

    private String formatGainLoss(CurrencyService currencyService, long currencyId,
                                  Money gainLoss, Money invested) {
        String amount = currencyService.getCurrencyFormatted(currencyId, gainLoss);
        double investedDouble = invested.toDouble();
        if (investedDouble == 0) return amount;
        double pct = (gainLoss.toDouble() / investedDouble) * 100.0;
        return String.format("%s (%.2f%%)", amount, pct);
    }

    @Override
    public void onFabClicked() {
        openShareTransactionForStock(null, TransactionTypes.Withdrawal);
    }

    @Override
    public void onSaveInstanceState(Bundle saveInstanceState) {
        super.onSaveInstanceState(saveInstanceState);

        saveInstanceState.putLong(ARG_ACCOUNT_ID, mAccountId);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateSubtitle();
        viewModel.loadStocks(mAccountId);
    }

    private void updateSubtitle() {
        if (!(getActivity() instanceof AppCompatActivity)) return;
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity.getSupportActionBar() == null) return;
        activity.getSupportActionBar().setDisplayShowTitleEnabled(true);
        activity.getSupportActionBar().setSubtitle(getSubTitle());
    }

    private void openEditInvestmentActivity(Long stockId) {
        Intent intent = new Intent(getActivity(), InvestmentTransactionEditActivity.class);
        intent.putExtra(InvestmentTransactionEditActivity.ARG_ACCOUNT_ID, mAccountId);
        intent.putExtra(InvestmentTransactionEditActivity.ARG_STOCK_ID, stockId);
        intent.setAction(Intent.ACTION_INSERT);
        editInvestmentLauncher.launch(intent);
    }

    private void openStockTransactions(@NonNull Stock stock) {
        Intent intent = new Intent(getActivity(), StockTransactionListActivity.class);
        intent.putExtra(StockTransactionListActivity.EXTRA_STOCK_ID, stock.getId());
        if (stock.getHeldAt() != Constants.NOT_SET) {
            intent.putExtra(StockTransactionListActivity.EXTRA_ACCOUNT_ID, stock.getHeldAt());
        }
        startActivity(intent);
    }

    private void openShareTransactionForStock(Stock stock, TransactionTypes transactionType) {
        Intent intent = new Intent(getActivity(), InvestmentTransactionEditActivity.class);
        intent.putExtra(InvestmentTransactionEditActivity.ARG_ACCOUNT_ID, mAccountId);
        if (stock != null) {
            intent.putExtra(InvestmentTransactionEditActivity.ARG_STOCK_ID, stock.getId());
        }
        intent.putExtra(InvestmentTransactionEditActivity.ARG_NEW_SHARE_TRANSACTION, true);
        intent.putExtra(InvestmentTransactionEditActivity.ARG_INITIAL_TRANSACTION_TYPE, transactionType.name());
        intent.setAction(Intent.ACTION_INSERT);
        editInvestmentLauncher.launch(intent);
    }

    private void openEditPriceActivity(Stock stock) {
        Intent intent = new Intent(getActivity(), PriceEditActivity.class);
        intent.putExtra(EditPriceDialog.ARG_ACCOUNT, stock.getHeldAt());
        intent.putExtra(EditPriceDialog.ARG_SYMBOL, stock.getSymbol());
        intent.putExtra(EditPriceDialog.ARG_PRICE, stock.getCurrentPrice().toString());
        intent.putExtra(PriceEditActivity.ARG_CURRENCY_ID, mAccount.getCurrencyId());
        String dateString = new MmxDate().toIsoDateString();
        intent.putExtra(EditPriceDialog.ARG_DATE, dateString);
        editPriceLauncher.launch(intent);
    }

    private void openYahooFinance(Stock stock) {
        String symbol = stock.getSymbol();
        if (symbol == null || symbol.isEmpty()) return;

        String url = "https://finance.yahoo.com/quote/" + Uri.encode(symbol);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        try {
            startActivity(intent);
        } catch (android.content.ActivityNotFoundException e) {
            Toast.makeText(requireContext(), getString(R.string.no_browser_available), Toast.LENGTH_SHORT).show();
        }
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

    private void setupMenuProvider() {
        if (isMenuProviderRegistered) return;

        MenuHost menuHost = requireActivity();
        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                if (menu.findItem(MENU_DOWNLOAD_ALL_PRICES) == null) {
                    menu.add(Menu.NONE, MENU_DOWNLOAD_ALL_PRICES, Menu.NONE, R.string.download_all_prices);
                }
                if (menu.findItem(MENU_VIEW_CASH_LEDGER) == null) {
                    menu.add(Menu.NONE, MENU_VIEW_CASH_LEDGER, Menu.NONE, R.string.cash_ledger);
                }
                if (menu.findItem(MENU_HIDE_ZERO_SHARES) == null) {
                    MenuItem item = menu.add(Menu.NONE, MENU_HIDE_ZERO_SHARES, Menu.NONE, R.string.menu_hide_zero_shares);
                    item.setCheckable(true);
                }

                MenuItem hideZeroSharesItem = menu.findItem(MENU_HIDE_ZERO_SHARES);
                if (hideZeroSharesItem != null) {
                    hideZeroSharesItem.setChecked(isHideZeroSharesEnabled());
                }
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == MENU_DOWNLOAD_ALL_PRICES) {
                    downloadAllPrices();
                    return true;
                }
                if (menuItem.getItemId() == MENU_VIEW_CASH_LEDGER) {
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).showAccountFragment(mAccountId);
                    }
                    return true;
                }
                if (menuItem.getItemId() == MENU_HIDE_ZERO_SHARES) {
                    boolean newValue = !menuItem.isChecked();
                    menuItem.setChecked(newValue);
                    setHideZeroSharesEnabled(newValue);
                    updateDisplayedStocks();
                    checkEmpty();
                    updateInvestmentHeader();
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

        isMenuProviderRegistered = true;
    }

    private void downloadAllPrices() {
        List<Stock> stocks = mDisplayedStocks != null ? mDisplayedStocks : viewModel.getStocks().getValue();
        if (stocks == null || stocks.isEmpty()) {
            Toast.makeText(getContext(), R.string.no_stock_data, Toast.LENGTH_SHORT).show();
            return;
        }

        isBulkDownloadInProgress = true;
        Toast.makeText(getContext(), R.string.starting_price_update, Toast.LENGTH_SHORT).show();
        viewModel.downloadAllStockPrices(stocks);
    }

    private void updateDisplayedStocks() {
        if (mStocks == null) {
            mDisplayedStocks = null;
            mAdapter.setRealizedGainLossByStockId(Collections.emptyMap());
            mAdapter.submitList(null);
            return;
        }

        if (isHideZeroSharesEnabled()) {
            List<Stock> filteredStocks = new java.util.ArrayList<>();
            for (Stock stock : mStocks) {
                if (stock == null) continue;
                if (Double.compare(stock.getNumberOfShares(), 0.0) != 0) {
                    filteredStocks.add(stock);
                }
            }
            mDisplayedStocks = filteredStocks;
        } else {
            mDisplayedStocks = mStocks;
        }

        mAdapter.setRealizedGainLossByStockId(calculateRealizedGainLossByStockId(mDisplayedStocks));

        mAdapter.submitList(new java.util.ArrayList<>(mDisplayedStocks), mAdapter::notifyDataSetChanged);
    }

    private Map<Long, PortfolioListAdapter.RealizedGainLoss> calculateRealizedGainLossByStockId(List<Stock> stocks) {
        Map<Long, PortfolioListAdapter.RealizedGainLoss> result = new HashMap<>();
        if (stocks == null || stocks.isEmpty()) {
            return result;
        }

        TransactionLinkRepository linkRepo = new TransactionLinkRepository(requireContext());
        ShareInfoRepository shareInfoRepo = new ShareInfoRepository(requireContext());
        AccountTransactionRepository txRepo = new AccountTransactionRepository(requireContext());

        for (Stock stock : stocks) {
            if (stock == null || stock.getId() == null) continue;
            result.put(stock.getId(), calculateRealizedGainLoss(stock.getId(), linkRepo, shareInfoRepo, txRepo));
        }

        return result;
    }

    private PortfolioListAdapter.RealizedGainLoss calculateRealizedGainLoss(
            long stockId,
            TransactionLinkRepository linkRepo,
            ShareInfoRepository shareInfoRepo,
            AccountTransactionRepository txRepo) {
        return new RealizedGainLossCalculator(linkRepo, shareInfoRepo, txRepo).calculate(stockId);
    }

    private boolean isHideZeroSharesEnabled() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        return preferences.getBoolean(getString(R.string.pref_portfolio_hide_zero_shares), false);
    }

    private void setHideZeroSharesEnabled(boolean enabled) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        preferences.edit().putBoolean(getString(R.string.pref_portfolio_hide_zero_shares), enabled).apply();
    }

    private void showDeleteStockConfirmationDialog(@NonNull Stock stock) {
        UIHelper ui = new UIHelper(requireContext());
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_stock_investment)
                .setIcon(ui.getIcon(FontAwesome.Icon.faw_question_circle))
                .setMessage(R.string.confirm_delete_stock_investment)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    new Thread(() -> {
                        boolean deleted = deleteStockInvestment(stock);
                        if (!isAdded()) return;
                        requireActivity().runOnUiThread(() -> {
                            if (deleted) {
                                viewModel.loadStocks(mAccountId);
                                Toast.makeText(getContext(), R.string.delete_success, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), R.string.db_delete_failed, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }).start();
                })
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.cancel())
                .show();
    }

    private boolean deleteStockInvestment(@NonNull Stock stock) {
        Long stockId = stock.getId();
        if (stockId == null) return false;

        TransactionLinkRepository linkRepo = new TransactionLinkRepository(requireContext());
        AccountTransactionRepository txRepo = new AccountTransactionRepository(requireContext());
        ShareInfoRepository shareInfoRepo = new ShareInfoRepository(requireContext());
        SplitCategoryRepository splitRepo = new SplitCategoryRepository(requireContext());
        TaglinkRepository taglinkRepo = new TaglinkRepository(requireContext());
        StockRepository stockRepo = new StockRepository(requireContext());

        List<TransactionLink> links = getStockLinks(linkRepo, stockId);
        for (TransactionLink link : links) {
            deleteLinkedTransactionData(link, shareInfoRepo, splitRepo, taglinkRepo, txRepo);
            if (link.getId() != null) {
                linkRepo.delete(link.getId());
            }
        }

        boolean stockDeleted = stockRepo.delete(stockId);
        if (!stockDeleted) return false;

        deleteOrphanPriceHistory(stock, stockRepo);

        return true;
    }

        private static List<TransactionLink> getStockLinks(TransactionLinkRepository linkRepo, long stockId) {
        return linkRepo.query(
                new com.money.manager.ex.datalayer.Select(linkRepo.getAllColumns())
                        .where("LOWER(" + TransactionLink.LINKTYPE + ")=? AND "
                                        + TransactionLink.LINKRECORDID + "=?",
                                "stock", String.valueOf(stockId))
        );
    }

    private void deleteLinkedTransactionData(TransactionLink link,
                                             ShareInfoRepository shareInfoRepo,
                                             SplitCategoryRepository splitRepo,
                                             TaglinkRepository taglinkRepo,
                                             AccountTransactionRepository txRepo) {
        Long transactionId = link.getCheckingAccountId();
        if (transactionId == null) {
            return;
        }

        deleteShareInfo(transactionId, shareInfoRepo);
        deleteSplitData(transactionId, splitRepo, taglinkRepo);
        taglinkRepo.deleteForType(transactionId, RefType.TRANSACTION);
        txRepo.delete(transactionId);
    }

    private void deleteShareInfo(long transactionId, ShareInfoRepository shareInfoRepo) {
        com.money.manager.ex.domainmodel.ShareInfo shareInfo =
                shareInfoRepo.loadByTransactionId(transactionId);
        if (shareInfo != null && shareInfo.getId() != null) {
            shareInfoRepo.delete(shareInfo.getId());
        }
    }

    private void deleteSplitData(long transactionId, SplitCategoryRepository splitRepo,
                                 TaglinkRepository taglinkRepo) {
        List<ISplitTransaction> splitCategories = splitRepo.loadSplitCategoriesFor(transactionId);
        if (splitCategories == null) {
            return;
        }

        for (ISplitTransaction split : splitCategories) {
            if (split == null || split.getId() == null) {
                continue;
            }
            taglinkRepo.deleteForType(split.getId(), split.getTransactionModel());
            splitRepo.delete(split);
        }
    }

    private void deleteOrphanPriceHistory(@NonNull Stock stock, StockRepository stockRepo) {
        String symbol = stock.getSymbol();
        if (TextUtils.isEmpty(symbol) || !stockRepo.loadBySymbol(symbol).isEmpty()) {
            return;
        }

        com.money.manager.ex.datalayer.StockHistoryRepository historyRepo =
                new com.money.manager.ex.datalayer.StockHistoryRepository(requireContext());
        List<StockHistory> prices = historyRepo.getAllPricesForSymbol(symbol);
        for (StockHistory price : prices) {
            if (price != null && price.getId() != null) {
                historyRepo.delete(price.getId());
            }
        }
    }

    private static final class InvestmentTotals {
        private final Money cashBalance;
        private final Money marketValue;
        private final Money invested;
        private final Money gainLoss;

        private InvestmentTotals(Money cashBalance, Money marketValue, Money invested,
                                 Money gainLoss) {
            this.cashBalance = cashBalance;
            this.marketValue = marketValue;
            this.invested = invested;
            this.gainLoss = gainLoss;
        }
    }

    private static final class TradeData {
        private final long transactionId;
        private final Date date;
        private final double shares;
        private final double price;
        private final double commission;

        private TradeData(long transactionId, Date date, double shares,
                          double price, double commission) {
            this.transactionId = transactionId;
            this.date = date;
            this.shares = shares;
            this.price = price;
            this.commission = commission;
        }

        private boolean isBuy() {
            return shares > 0.0;
        }

        private boolean isSell() {
            return shares < 0.0;
        }
    }

    private static final class RealizedGainLossCalculator {
        private final TransactionLinkRepository linkRepo;
        private final ShareInfoRepository shareInfoRepo;
        private final AccountTransactionRepository txRepo;

        private RealizedGainLossCalculator(TransactionLinkRepository linkRepo,
                                           ShareInfoRepository shareInfoRepo,
                                           AccountTransactionRepository txRepo) {
            this.linkRepo = linkRepo;
            this.shareInfoRepo = shareInfoRepo;
            this.txRepo = txRepo;
        }

        private PortfolioListAdapter.RealizedGainLoss calculate(long stockId) {
            List<TradeData> trades = loadTrades(stockId);
            if (trades.isEmpty()) {
                return zeroResult();
            }

            trades.sort(Comparator
                    .comparing((TradeData trade) -> trade.date, Comparator.nullsLast(Date::compareTo))
                    .thenComparingLong(trade -> trade.transactionId));

            Money realizedAmount = MoneyFactory.fromDouble(0);
            double realizedCostBasis = 0.0;
            Position position = new Position();

            for (TradeData trade : trades) {
                if (trade.isBuy()) {
                    position.applyBuy(trade);
                    continue;
                }
                if (trade.isSell()) {
                    Result result = position.applySell(trade);
                    realizedAmount = realizedAmount.add(MoneyFactory.fromDouble(result.realizedAmount));
                    realizedCostBasis += result.realizedCostBasis;
                }
            }

            double realizedPercent = realizedCostBasis > 0.0
                    ? (realizedAmount.toDouble() / realizedCostBasis) * 100.0
                    : 0.0;
            return new PortfolioListAdapter.RealizedGainLoss(realizedAmount, realizedPercent);
        }

        private List<TradeData> loadTrades(long stockId) {
            List<TransactionLink> links = getStockLinks(linkRepo, stockId);
            if (links == null || links.isEmpty()) {
                return Collections.emptyList();
            }

            List<TradeData> trades = new ArrayList<>();
            for (TransactionLink link : links) {
                TradeData trade = createTrade(link);
                if (trade != null) {
                    trades.add(trade);
                }
            }
            return trades;
        }

        private TradeData createTrade(TransactionLink link) {
            if (link == null || link.getCheckingAccountId() == null) {
                return null;
            }

            Long transactionId = link.getCheckingAccountId();
            com.money.manager.ex.domainmodel.ShareInfo shareInfo = shareInfoRepo.loadByTransactionId(transactionId);
            if (shareInfo == null) {
                return null;
            }

            AccountTransaction tx = txRepo.load(transactionId);
            Date txDate = tx != null ? tx.getDate() : null;

            double shares = shareInfo.getShareNumber() != null ? shareInfo.getShareNumber() : 0.0;
            double price = shareInfo.getSharePrice() != null ? shareInfo.getSharePrice() : 0.0;
            double commission = shareInfo.getShareCommission() != null ? shareInfo.getShareCommission() : 0.0;
            return new TradeData(transactionId, txDate, shares, price, commission);
        }

        private PortfolioListAdapter.RealizedGainLoss zeroResult() {
            return new PortfolioListAdapter.RealizedGainLoss(MoneyFactory.fromDouble(0), 0.0);
        }

        private static final class Position {
            private double heldShares;
            private double heldCostBasis;

            private void applyBuy(TradeData trade) {
                heldShares += trade.shares;
                heldCostBasis += (trade.shares * trade.price) + trade.commission;
            }

            private Result applySell(TradeData trade) {
                double soldShares = Math.abs(trade.shares);
                double sharesMatched = Math.min(soldShares, heldShares);
                double averageCost = heldShares > 0.0 ? heldCostBasis / heldShares : 0.0;
                double costRemoved = sharesMatched * averageCost;
                double proceeds = (soldShares * trade.price) - trade.commission;

                heldShares = Math.max(0.0, heldShares - sharesMatched);
                heldCostBasis = Math.max(0.0, heldCostBasis - costRemoved);
                if (heldShares == 0.0) {
                    heldCostBasis = 0.0;
                }

                return new Result(proceeds - costRemoved, costRemoved);
            }
        }

        private static final class Result {
            private final double realizedAmount;
            private final double realizedCostBasis;

            private Result(double realizedAmount, double realizedCostBasis) {
                this.realizedAmount = realizedAmount;
                this.realizedCostBasis = realizedCostBasis;
            }
        }
    }

    private String getStockLabel(@NonNull Stock stock) {
        String name = stock.getName();
        String symbol = stock.getSymbol();

        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(symbol) && !name.equals(symbol)) {
            return name + " (" + symbol + ")";
        }

        if (!TextUtils.isEmpty(name)) {
            return name;
        }

        if (!TextUtils.isEmpty(symbol)) {
            return symbol;
        }

        return getString(R.string.portfolio);
    }
}
