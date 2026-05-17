/*
 * Copyright (C) 2012-2026 The Android Money Manager Ex Project Team
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
package com.money.manager.ex.reports;

import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.money.manager.ex.R;
import com.money.manager.ex.account.AccountTypes;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.datalayer.AccountRepository;
import com.money.manager.ex.datalayer.AccountTransactionRepository;
import com.money.manager.ex.datalayer.StockRepository;
import com.money.manager.ex.datalayer.ShareInfoRepository;
import com.money.manager.ex.datalayer.TransactionLinkRepository;
import com.money.manager.ex.domainmodel.Account;
import com.money.manager.ex.domainmodel.AccountTransaction;
import com.money.manager.ex.domainmodel.ShareInfo;
import com.money.manager.ex.domainmodel.Stock;
import com.money.manager.ex.domainmodel.TransactionLink;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;
import timber.log.Timber;

public class SummaryOfStocksReportFragment extends Fragment {

    private TableLayout tableLayout;
    private TextView emptyView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_summary_of_accounts_report, container, false);
        tableLayout = view.findViewById(R.id.summaryAccountsTable);
        emptyView = view.findViewById(R.id.summaryAccountsEmpty);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        loadReportAsync();
    }

    private void loadReportAsync() {
        new Thread(() -> {
            try {
                ReportModel model = buildModel();
                if (!isAdded()) {
                    return;
                }
                requireActivity().runOnUiThread(() -> renderModel(model));
            } catch (Exception e) {
                Timber.e(e, "loading summary of stocks report");
                if (!isAdded()) {
                    return;
                }
                requireActivity().runOnUiThread(() -> {
                    emptyView.setVisibility(View.VISIBLE);
                    tableLayout.setVisibility(View.GONE);
                });
            }
        }).start();
    }

    private ReportModel buildModel() {
        AccountRepository accountRepository = new AccountRepository(requireContext());
        StockRepository stockRepository = new StockRepository(requireContext());
        CurrencyService currencyService = new CurrencyService(requireContext());
        TransactionLinkRepository linkRepository = new TransactionLinkRepository(requireContext());
        ShareInfoRepository shareInfoRepository = new ShareInfoRepository(requireContext());
        AccountTransactionRepository transactionRepository = new AccountTransactionRepository(requireContext());

        List<Account> accounts = accountRepository.loadByType(AccountTypes.INVESTMENT);
        List<ReportRow> rows = new ArrayList<>();

        long baseCurrencyId = currencyService.getBaseCurrencyId();
        Money grandShares = MoneyFactory.fromDouble(0);
        Money grandCommissionBase = MoneyFactory.fromDouble(0);
        Money grandTotalCostBase = MoneyFactory.fromDouble(0);
        Money grandMarketValueBase = MoneyFactory.fromDouble(0);
        Money grandUnrealizedGainLossBase = MoneyFactory.fromDouble(0);
        Money grandRealizedGainLossBase = MoneyFactory.fromDouble(0);
        Money grandRealizedCostBasisBase = MoneyFactory.fromDouble(0);

        for (Account account : accounts) {
            AccountSummary accountSummary = buildAccountSummary(account, stockRepository,
                linkRepository, shareInfoRepository, transactionRepository, baseCurrencyId);
            if (accountSummary == null) {
                continue;
            }

            rows.addAll(accountSummary.rows);

            if (accountSummary.hasStocks) {
                rows.add(ReportRow.subtotal(getString(R.string.report_subtotal), accountSummary.currencyId,
                    accountSummary.totals.shares, accountSummary.totals.commission,
                    accountSummary.totals.totalCost, accountSummary.totals.marketValue,
                    accountSummary.totals.unrealizedGainLoss, accountSummary.totals.realizedGainLoss,
                    accountSummary.totals.realizedCostBasis));
            }

            grandShares = grandShares.add(accountSummary.totals.shares);
            grandCommissionBase = grandCommissionBase.add(convertToBase(currencyService, baseCurrencyId, accountSummary.currencyId, accountSummary.totals.commission));
            grandTotalCostBase = grandTotalCostBase.add(convertToBase(currencyService, baseCurrencyId, accountSummary.currencyId, accountSummary.totals.totalCost));
            grandMarketValueBase = grandMarketValueBase.add(convertToBase(currencyService, baseCurrencyId, accountSummary.currencyId, accountSummary.totals.marketValue));
            grandUnrealizedGainLossBase = grandUnrealizedGainLossBase.add(convertToBase(currencyService, baseCurrencyId, accountSummary.currencyId, accountSummary.totals.unrealizedGainLoss));
            grandRealizedGainLossBase = grandRealizedGainLossBase.add(convertToBase(currencyService, baseCurrencyId, accountSummary.currencyId, accountSummary.totals.realizedGainLoss));
            grandRealizedCostBasisBase = grandRealizedCostBasisBase.add(convertToBase(currencyService, baseCurrencyId, accountSummary.currencyId, accountSummary.totals.realizedCostBasis));
        }

        rows.add(ReportRow.grandTotal(getString(R.string.report_grand_total), baseCurrencyId, grandShares, grandCommissionBase,
            grandTotalCostBase, grandMarketValueBase, grandUnrealizedGainLossBase,
            grandRealizedGainLossBase, grandRealizedCostBasisBase));

        return new ReportModel(rows);
    }

    @Nullable
    private AccountSummary buildAccountSummary(@Nullable Account account,
            StockRepository stockRepository,
            TransactionLinkRepository linkRepository,
            ShareInfoRepository shareInfoRepository,
            AccountTransactionRepository transactionRepository,
            long baseCurrencyId) {
        if (account == null || account.getId() == null) {
            return null;
        }

        long accountCurrencyId = account.getCurrencyId() != null ? account.getCurrencyId() : baseCurrencyId;

        List<Stock> stocks = stockRepository.loadByAccount(account.getId());
        if (stocks == null) {
            stocks = new ArrayList<>();
        }
        stocks.sort(Comparator.comparing(Stock::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                .thenComparing(stock -> stock.getSymbol() == null ? "" : stock.getSymbol(), String.CASE_INSENSITIVE_ORDER));

        List<ReportRow> rows = new ArrayList<>();
        if (!stocks.isEmpty()) {
            rows.add(ReportRow.accountHeader(account.getName()));
        }

        AccountTotals totals = new AccountTotals();
        for (Stock stock : stocks) {
            if (stock == null) {
                continue;
            }

            ReportRow row = buildStockRow(accountCurrencyId, stock, linkRepository,
                shareInfoRepository, transactionRepository);
            rows.add(row);
            totals.add(row);
        }

        return new AccountSummary(accountCurrencyId, rows, totals, !stocks.isEmpty());
    }

    private ReportRow buildStockRow(long accountCurrencyId, Stock stock,
            TransactionLinkRepository linkRepository,
            ShareInfoRepository shareInfoRepository,
            AccountTransactionRepository transactionRepository) {
        Money shares = MoneyFactory.fromDouble(safeDouble(stock.getNumberOfShares()));
        RealizedGainLoss realizedGainLoss = calculateRealizedGainLoss(stock.getId(),
            linkRepository, shareInfoRepository, transactionRepository);
        Money marketValue = stock.getCurrentPrice().multiply(stock.getNumberOfShares());
        Money totalCost = realizedGainLoss.investedCost;
        Money commission = realizedGainLoss.totalCommission;
        Money unrealizedGainLoss = realizedGainLoss.openCostBasis.toDouble() <= 0.0
            ? MoneyFactory.fromDouble(0)
            : marketValue.subtract(realizedGainLoss.openCostBasis);

        return ReportRow.stock(accountCurrencyId, stock, new StockMetrics(shares, commission, totalCost,
            marketValue, realizedGainLoss.openCostBasis, unrealizedGainLoss, realizedGainLoss.amount,
            realizedGainLoss.costBasis));
    }

    private void renderModel(ReportModel model) {
        tableLayout.removeAllViews();

        if (model.rows.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            tableLayout.setVisibility(View.GONE);
            return;
        }

        emptyView.setVisibility(View.GONE);
        tableLayout.setVisibility(View.VISIBLE);

        tableLayout.addView(createHeaderRow());

        CurrencyService currencyService = new CurrencyService(requireContext());
        for (ReportRow row : model.rows) {
            tableLayout.addView(createRow(row, currencyService));
        }
    }

    private TableRow createHeaderRow() {
        TableRow row = new TableRow(requireContext());
        row.addView(createHeaderCell(getString(R.string.stock_name), true));
        row.addView(createHeaderCell(getString(R.string.symbol), true));
        row.addView(createHeaderCell(getString(R.string.shares), false));
        row.addView(createHeaderCell(getString(R.string.purchase_price), false));
        row.addView(createHeaderCell(getString(R.string.commission), false));
        row.addView(createHeaderCell(getString(R.string.total_cost), false));
        row.addView(createHeaderCell(getString(R.string.current_price), false));
        row.addView(createHeaderCell(getString(R.string.market_value), false));
        row.addView(createHeaderCell(getString(R.string.unrealized_gain_loss), false));
        row.addView(createHeaderCell(getString(R.string.realized_gain_loss), false));
        return row;
    }

    private TableRow createRow(ReportRow reportRow, CurrencyService currencyService) {
        if (reportRow.rowType == ReportRowType.ACCOUNT_HEADER) {
            return createAccountHeaderRow(reportRow);
        }

        TableRow row = new TableRow(requireContext());
        boolean isSummaryRow = reportRow.rowType != ReportRowType.STOCK;

        row.addView(createCell(reportRow.stockName, true, isSummaryRow));
        row.addView(createCell(reportRow.symbol, true, isSummaryRow));
        row.addView(createCell(formatShares(reportRow.shares), false, isSummaryRow));

        if (reportRow.rowType == ReportRowType.STOCK) {
            row.addView(createMoneyCell(currencyService, reportRow.displayCurrencyId, reportRow.purchasePrice, false, false));
        } else {
            row.addView(createCell("", false, true));
        }

        row.addView(createMoneyCell(currencyService, reportRow.displayCurrencyId, reportRow.commission, false, isSummaryRow));
        row.addView(createMoneyCell(currencyService, reportRow.displayCurrencyId, reportRow.totalCost, false, isSummaryRow));

        if (reportRow.rowType == ReportRowType.STOCK) {
            row.addView(createMoneyCell(currencyService, reportRow.displayCurrencyId, reportRow.currentPrice, false, false));
        } else {
            row.addView(createCell("", false, true));
        }

        row.addView(createMoneyCell(currencyService, reportRow.displayCurrencyId, reportRow.marketValue, false, isSummaryRow));
        row.addView(createMoneyCellWithPercent(currencyService, reportRow.displayCurrencyId,
            reportRow.unrealizedGainLoss, reportRow.invested, false, isSummaryRow));
        row.addView(createMoneyCellWithPercent(currencyService, reportRow.displayCurrencyId,
            reportRow.realizedGainLoss, reportRow.realizedCostBasis, false, isSummaryRow));

        if (reportRow.rowType == ReportRowType.SUBTOTAL) {
            applyRowBackground(row, Color.parseColor("#E6E6E6"));
        } else if (reportRow.rowType == ReportRowType.GRAND_TOTAL) {
            applyRowBackground(row, Color.parseColor("#D0D0D0"));
        }

        return row;
    }

    private TableRow createAccountHeaderRow(ReportRow reportRow) {
        TableRow row = new TableRow(requireContext());
        TextView headerCell = new TextView(requireContext());
        headerCell.setText(reportRow.accountName);
        headerCell.setPadding(16, 12, 16, 12);
        headerCell.setTypeface(null, android.graphics.Typeface.BOLD);
        headerCell.setTextSize(14);
        headerCell.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(headerCell);
        applyRowBackground(row, Color.parseColor("#F2F2F2"));
        return row;
    }

    private void applyRowBackground(TableRow row, int color) {
        row.setBackgroundColor(color);
        for (int i = 0; i < row.getChildCount(); i++) {
            row.getChildAt(i).setBackgroundColor(color);
        }
    }

    private TextView createHeaderCell(String text, boolean alignStart) {
        TextView cell = createCell(text, alignStart, true);
        cell.setTypeface(null, android.graphics.Typeface.BOLD);
        return cell;
    }

    private TextView createMoneyCell(CurrencyService currencyService, long currencyId, Money value,
            boolean alignStart, boolean bold) {
        String formatted = currencyService.getCurrencyFormatted(currencyId, value);
        return createCell(formatted, alignStart, bold, value, false);
    }

    private TextView createMoneyCellWithPercent(CurrencyService currencyService, long currencyId, Money value,
            Money basis, boolean alignStart, boolean bold) {
        String formatted = currencyService.getCurrencyFormatted(currencyId, value);
        String percent = formatPercent(value, basis);
        if (!percent.isEmpty()) {
            formatted = formatted + " (" + percent + ")";
        }
        return createCell(formatted, alignStart, bold, value, true);
    }

    private TextView createCell(String text, boolean alignStart, boolean bold) {
        return createCell(text, alignStart, bold, null, false);
    }

    private TextView createCell(String text, boolean alignStart, boolean bold, @Nullable Money value,
            boolean colorize) {
        TextView cell = new TextView(requireContext());
        cell.setText(text == null ? "" : text);
        cell.setPadding(16, 8, 16, 8);
        cell.setGravity(alignStart ? Gravity.START : Gravity.END);
        if (bold) {
            cell.setTypeface(null, android.graphics.Typeface.BOLD);
        }
        if (colorize && value != null) {
            if (value.toDouble() < 0) {
                cell.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            } else if (value.toDouble() > 0) {
                cell.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            }
        }
        return cell;
    }

    private Money convertToBase(CurrencyService currencyService, long baseCurrencyId, long accountCurrencyId, Money value) {
        if (value == null) {
            return MoneyFactory.fromDouble(0);
        }
        return currencyService.doCurrencyExchange(baseCurrencyId, value, accountCurrencyId);
    }

    private String formatShares(Money shares) {
        return String.format(Locale.getDefault(), "%.2f", shares.toDouble());
    }

    private String formatPercent(Money gainLoss, Money invested) {
        double investedValue = invested == null ? 0.0 : invested.toDouble();
        if (investedValue == 0.0) {
            return "";
        }
        double pct = (gainLoss.toDouble() / investedValue) * 100.0;
        return String.format(Locale.getDefault(), "%.2f%%", pct);
    }

    private double safeDouble(@Nullable Double value) {
        return value == null ? 0.0 : value;
    }

    @Nullable
    private RealizedGainLoss calculateRealizedGainLoss(@Nullable Long stockId,
            TransactionLinkRepository linkRepo,
            ShareInfoRepository shareInfoRepo,
            AccountTransactionRepository txRepo) {
        if (stockId == null) {
            return RealizedGainLoss.zero();
        }

        List<TradeData> trades = loadTrades(stockId, linkRepo, shareInfoRepo, txRepo);
        if (trades.isEmpty()) {
            return RealizedGainLoss.zero();
        }

        trades.sort(Comparator
                .comparing((TradeData trade) -> trade.date, Comparator.nullsLast(Date::compareTo))
                .thenComparingLong(trade -> trade.transactionId));

        Money realizedAmount = MoneyFactory.fromDouble(0);
        Money investedCost = MoneyFactory.fromDouble(0);
        Money totalCommission = MoneyFactory.fromDouble(0);
        double realizedCostBasis = 0.0;
        Position position = new Position();

        for (TradeData trade : trades) {
            totalCommission = totalCommission.add(MoneyFactory.fromDouble(trade.commission));
            if (trade.isBuy()) {
                position.applyBuy(trade);
                investedCost = investedCost.add(MoneyFactory.fromDouble((trade.shares * trade.price) + trade.commission));
            } else if (trade.isSell()) {
                Result result = position.applySell(trade);
                realizedAmount = realizedAmount.add(MoneyFactory.fromDouble(result.realizedAmount));
                realizedCostBasis += result.realizedCostBasis;
            }
        }

        return new RealizedGainLoss(realizedAmount, realizedCostBasis, investedCost,
            totalCommission, MoneyFactory.fromDouble(position.getHeldCostBasis()));
    }

    private List<TradeData> loadTrades(long stockId,
            TransactionLinkRepository linkRepo,
            ShareInfoRepository shareInfoRepo,
            AccountTransactionRepository txRepo) {
        List<TransactionLink> links = getStockLinks(linkRepo, stockId);
        if (links == null || links.isEmpty()) {
            return Collections.emptyList();
        }

        List<TradeData> trades = new ArrayList<>();
        for (TransactionLink link : links) {
            TradeData trade = createTrade(link, shareInfoRepo, txRepo);
            if (trade != null) {
                trades.add(trade);
            }
        }
        return trades;
    }

    @Nullable
    private TradeData createTrade(@Nullable TransactionLink link,
            ShareInfoRepository shareInfoRepo,
            AccountTransactionRepository txRepo) {
        if (link == null || link.getCheckingAccountId() == null) {
            return null;
        }

        Long transactionId = link.getCheckingAccountId();
        ShareInfo shareInfo = shareInfoRepo.loadByTransactionId(transactionId);
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

    private static List<TransactionLink> getStockLinks(TransactionLinkRepository linkRepo, long stockId) {
        return linkRepo.query(
                new com.money.manager.ex.datalayer.Select(linkRepo.getAllColumns())
                        .where("LOWER(" + TransactionLink.LINKTYPE + ")=? AND "
                                        + TransactionLink.LINKRECORDID + "=?",
                                "stock", String.valueOf(stockId))
        );
    }

    private enum ReportRowType {
        ACCOUNT_HEADER,
        STOCK,
        SUBTOTAL,
        GRAND_TOTAL
    }

    private static final class ReportModel {
        private final List<ReportRow> rows;

        private ReportModel(List<ReportRow> rows) {
            this.rows = rows;
        }
    }

    private static final class AccountSummary {
        private final long currencyId;
        private final List<ReportRow> rows;
        private final AccountTotals totals;
        private final boolean hasStocks;

        private AccountSummary(long currencyId, List<ReportRow> rows, AccountTotals totals,
                boolean hasStocks) {
            this.currencyId = currencyId;
            this.rows = rows;
            this.totals = totals;
            this.hasStocks = hasStocks;
        }
    }

    private static final class AccountTotals {
        private Money shares = MoneyFactory.fromDouble(0);
        private Money commission = MoneyFactory.fromDouble(0);
        private Money totalCost = MoneyFactory.fromDouble(0);
        private Money marketValue = MoneyFactory.fromDouble(0);
        private Money unrealizedGainLoss = MoneyFactory.fromDouble(0);
        private Money realizedGainLoss = MoneyFactory.fromDouble(0);
        private Money realizedCostBasis = MoneyFactory.fromDouble(0);

        private void add(ReportRow row) {
            shares = shares.add(row.shares);
            commission = commission.add(row.commission);
            totalCost = totalCost.add(row.totalCost);
            marketValue = marketValue.add(row.marketValue);
            unrealizedGainLoss = unrealizedGainLoss.add(row.unrealizedGainLoss);
            realizedGainLoss = realizedGainLoss.add(row.realizedGainLoss);
            realizedCostBasis = realizedCostBasis.add(row.realizedCostBasis);
        }
    }

    private static final class StockMetrics {
        private final Money shares;
        private final Money commission;
        private final Money totalCost;
        private final Money marketValue;
        private final Money invested;
        private final Money unrealizedGainLoss;
        private final Money realizedGainLoss;
        private final Money realizedCostBasis;

        private StockMetrics(Money shares, Money commission, Money totalCost, Money marketValue,
                Money invested, Money unrealizedGainLoss, Money realizedGainLoss, Money realizedCostBasis) {
            this.shares = shares;
            this.commission = commission;
            this.totalCost = totalCost;
            this.marketValue = marketValue;
            this.invested = invested;
            this.unrealizedGainLoss = unrealizedGainLoss;
            this.realizedGainLoss = realizedGainLoss;
            this.realizedCostBasis = realizedCostBasis;
        }
    }

    private static final class ReportRow {
        private final ReportRowType rowType;
        private final String accountName;
        private final long displayCurrencyId;
        private final String stockName;
        private final String symbol;
        private final Money shares;
        private final Money commission;
        private final Money totalCost;
        private final Money purchasePrice;
        private final Money currentPrice;
        private final Money invested;
        private final Money marketValue;
        private final Money unrealizedGainLoss;
        private final Money realizedGainLoss;
        private final Money realizedCostBasis;

        private ReportRow(ReportRowType rowType, String accountName, long displayCurrencyId,
                String stockName, String symbol, Money shares, Money commission, Money totalCost,
                Money purchasePrice, Money currentPrice, Money invested, Money marketValue,
                Money unrealizedGainLoss, Money realizedGainLoss, Money realizedCostBasis) {
            this.rowType = rowType;
            this.accountName = accountName;
            this.displayCurrencyId = displayCurrencyId;
            this.stockName = stockName;
            this.symbol = symbol;
            this.shares = shares;
            this.commission = commission;
            this.totalCost = totalCost;
            this.purchasePrice = purchasePrice;
            this.currentPrice = currentPrice;
            this.invested = invested;
            this.marketValue = marketValue;
            this.unrealizedGainLoss = unrealizedGainLoss;
            this.realizedGainLoss = realizedGainLoss;
            this.realizedCostBasis = realizedCostBasis;
        }

        private static ReportRow accountHeader(String accountName) {
            return new ReportRow(ReportRowType.ACCOUNT_HEADER, accountName, 0, "", "",
                    MoneyFactory.fromDouble(0), MoneyFactory.fromDouble(0), MoneyFactory.fromDouble(0),
                    MoneyFactory.fromDouble(0), MoneyFactory.fromDouble(0), MoneyFactory.fromDouble(0),
                    MoneyFactory.fromDouble(0), MoneyFactory.fromDouble(0), MoneyFactory.fromDouble(0),
                    MoneyFactory.fromDouble(0));
        }

        private static ReportRow stock(long displayCurrencyId, Stock stock, StockMetrics metrics) {
            return new ReportRow(ReportRowType.STOCK, "", displayCurrencyId,
                stock.getName(), stock.getSymbol(), metrics.shares, metrics.commission, metrics.totalCost,
                stock.getPurchasePrice(), stock.getCurrentPrice(), metrics.invested, metrics.marketValue,
                metrics.unrealizedGainLoss, metrics.realizedGainLoss, metrics.realizedCostBasis);
        }

        private static ReportRow subtotal(String label, long displayCurrencyId,
            Money shares, Money commission, Money totalCost, Money marketValue, Money unrealizedGainLoss,
            Money realizedGainLoss, Money realizedCostBasis) {
            return new ReportRow(ReportRowType.SUBTOTAL, "", displayCurrencyId,
                label, "", shares, commission, totalCost, MoneyFactory.fromDouble(0),
                MoneyFactory.fromDouble(0), totalCost, marketValue, unrealizedGainLoss,
                realizedGainLoss, realizedCostBasis);
        }

        private static ReportRow grandTotal(String label, long displayCurrencyId, Money shares,
            Money commission, Money totalCost, Money marketValue, Money unrealizedGainLoss,
            Money realizedGainLoss, Money realizedCostBasis) {
            return new ReportRow(ReportRowType.GRAND_TOTAL, "", displayCurrencyId,
                label, "", shares, commission, totalCost, MoneyFactory.fromDouble(0),
                MoneyFactory.fromDouble(0), totalCost, marketValue, unrealizedGainLoss,
                realizedGainLoss, realizedCostBasis);
        }
    }

    private static final class RealizedGainLoss {
        private final Money amount;
        private final Money costBasis;
        private final Money investedCost;
        private final Money totalCommission;
        private final Money openCostBasis;

        private RealizedGainLoss(Money amount, double costBasis, Money investedCost,
                Money totalCommission, Money openCostBasis) {
            this.amount = amount;
            this.costBasis = MoneyFactory.fromDouble(costBasis);
            this.investedCost = investedCost;
            this.totalCommission = totalCommission;
            this.openCostBasis = openCostBasis;
        }

        private static RealizedGainLoss zero() {
            return new RealizedGainLoss(MoneyFactory.fromDouble(0), 0.0, MoneyFactory.fromDouble(0),
                    MoneyFactory.fromDouble(0), MoneyFactory.fromDouble(0));
        }
    }

    private static final class TradeData {
        private final long transactionId;
        private final Date date;
        private final double shares;
        private final double price;
        private final double commission;

        private TradeData(long transactionId, Date date, double shares, double price, double commission) {
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

        private double getHeldCostBasis() {
            return heldCostBasis;
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
