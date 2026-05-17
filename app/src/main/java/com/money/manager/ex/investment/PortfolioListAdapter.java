/*
 * Copyright (C) 2025-2025 The Android Money Manager Ex Project Team
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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.money.manager.ex.R;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.domainmodel.Account;
import com.money.manager.ex.domainmodel.Stock;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

public class PortfolioListAdapter extends ListAdapter<Stock, PortfolioListAdapter.ViewHolder> {
    private final LayoutInflater inflater;
    private OnItemClickListener listener;
    private OnItemLongClickListener longClickListener;
    private Account mAccount;
    private final CurrencyService mCurrencyService;
    private final Map<Long, RealizedGainLoss> mRealizedGainLossByStockId = new HashMap<>();

    public interface OnItemClickListener {
        void onItemClick(long stockId);
    }
    public interface OnItemLongClickListener {
        void onItemLongClick(Stock stock, View view);
    }

    private static final DiffUtil.ItemCallback<Stock> DIFF_CALLBACK = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull Stock oldItem, @NonNull Stock newItem) {
            return Objects.equals(oldItem.getId(), newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Stock oldItem, @NonNull Stock newItem) {
            return oldItem.equals(newItem);
        }
    };

    public PortfolioListAdapter(Context context) {
        super(DIFF_CALLBACK);
        this.inflater = LayoutInflater.from(context);
        this.mCurrencyService = new CurrencyService(context);
    }

    public void setAccount(Account account) {
        this.mAccount = account;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    public void setRealizedGainLossByStockId(@NonNull Map<Long, RealizedGainLoss> gainLossByStockId) {
        mRealizedGainLossByStockId.clear();
        mRealizedGainLossByStockId.putAll(gainLossByStockId);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_portfolio, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Stock stock = getItem(position);

        // Column 1: Name and Symbol
        holder.nameTextView.setText(stock.getName());
        holder.symbolTextView.setText(stock.getSymbol());

        // Column 2: Market Value and Shares
        Money marketValue = stock.getCurrentPrice().multiply(stock.getNumberOfShares());
        holder.marketValueTextView.setText(mAccount == null ? "<unknown>" : mCurrencyService.getCurrencyFormatted(mAccount.getCurrencyId(), marketValue));
        holder.sharesTextView.setText(String.format("%.2f", stock.getNumberOfShares()));

        // Column 3: Current Price and Purchase Price
        holder.currentPriceTextView.setText(mAccount == null ? "<unknown>" : mCurrencyService.getCurrencyFormatted(mAccount.getCurrencyId(), stock.getCurrentPrice()));
        holder.purchasePriceTextView.setText(mAccount == null ? "<unknown>" : mCurrencyService.getCurrencyFormatted(mAccount.getCurrencyId(), stock.getPurchasePrice()));

        // Column 4: Unrealized G/L
        Money unrealizedAmount = calculateUnrealizedGainLoss(stock);
        double unrealizedPercent = calculateUnrealizedPercentage(stock);

        holder.unrealizedGLAmountTextView.setText(mAccount == null ? "<unknown>" : mCurrencyService.getCurrencyFormatted(mAccount.getCurrencyId(), unrealizedAmount));
        holder.unrealizedGLPercentTextView.setText(String.format("%.2f%%", unrealizedPercent));

        // Column 5: Realized G/L
        RealizedGainLoss realizedGainLoss = mRealizedGainLossByStockId.get(stock.getId());
        Money realizedAmount = realizedGainLoss != null ? realizedGainLoss.amount : MoneyFactory.fromDouble(0);
        double realizedPercent = realizedGainLoss != null ? realizedGainLoss.percent : 0.0;
        holder.realizedGLAmountTextView.setText(mAccount == null
            ? "<unknown>"
            : mCurrencyService.getCurrencyFormatted(mAccount.getCurrencyId(), realizedAmount));
        holder.realizedGLPercentTextView.setText(String.format("%.2f%%", realizedPercent));

        // Zebra striping
        int bgColor = (position % 2 == 0) ? android.R.color.darker_gray : android.R.color.white;
        int fgColor = (position % 2 == 0) ? android.R.color.white : android.R.color.black;
        holder.currentPriceTextView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), fgColor));
        holder.purchasePriceTextView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), fgColor));
        int gainLossColor = ContextCompat.getColor(holder.itemView.getContext(), fgColor);
        if (unrealizedAmount.toDouble() < 0) {
            gainLossColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.red);
        } else if (unrealizedAmount.toDouble() > 0) {
            gainLossColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.green);
        }
        holder.unrealizedGLAmountTextView.setTextColor(gainLossColor);
        holder.unrealizedGLPercentTextView.setTextColor(gainLossColor);

        int realizedGainLossColor = ContextCompat.getColor(holder.itemView.getContext(), fgColor);
        if (realizedAmount.toDouble() < 0) {
            realizedGainLossColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.red);
        } else if (realizedAmount.toDouble() > 0) {
            realizedGainLossColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.green);
        }
        holder.realizedGLAmountTextView.setTextColor(realizedGainLossColor);
        holder.realizedGLPercentTextView.setTextColor(realizedGainLossColor);

        holder.marketValueTextView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), fgColor));
        holder.sharesTextView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), fgColor));
        holder.nameTextView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), fgColor));
        holder.symbolTextView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), fgColor));
        holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), bgColor));
    }

    // Helper methods for calculations
    private Money calculateUnrealizedGainLoss(Stock stock) {
        // Market value - cost basis (VALUE field includes commission)
        Money marketValue = stock.getCurrentPrice().multiply(stock.getNumberOfShares());
        Money costBasis = stock.getValue();
        return marketValue.subtract(costBasis);
    }

    private double calculateUnrealizedPercentage(Stock stock) {
        Money costBasis = stock.getValue();
        if (costBasis.isZero()) return 0.0;
        Money marketValue = stock.getCurrentPrice().multiply(stock.getNumberOfShares());
        Money gainLoss = marketValue.subtract(costBasis);
        return (gainLoss.toDouble() / costBasis.toDouble()) * 100.0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        // Column 1
        TextView nameTextView;
        TextView symbolTextView;
        // Column 2
        TextView marketValueTextView;
        TextView sharesTextView;
        // Column 3
        TextView currentPriceTextView;
        TextView purchasePriceTextView;
        // Column 4
        TextView unrealizedGLAmountTextView;
        TextView unrealizedGLPercentTextView;
        // Column 5
        TextView realizedGLAmountTextView;
        TextView realizedGLPercentTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            // Column 1
            nameTextView = itemView.findViewById(R.id.nameTextView);
            symbolTextView = itemView.findViewById(R.id.symbolTextView);
            // Column 2
            marketValueTextView = itemView.findViewById(R.id.marketValueTextView);
            sharesTextView = itemView.findViewById(R.id.sharesTextView);
            // Column 3
            currentPriceTextView = itemView.findViewById(R.id.currentPriceTextView);
            purchasePriceTextView = itemView.findViewById(R.id.purchasePriceTextView);
            // Column 4
            unrealizedGLAmountTextView = itemView.findViewById(R.id.unrealizedGLAmountTextView);
            unrealizedGLPercentTextView = itemView.findViewById(R.id.unrealizedGLPercentTextView);
            // Column 5
            realizedGLAmountTextView = itemView.findViewById(R.id.realizedGLAmountTextView);
            realizedGLPercentTextView = itemView.findViewById(R.id.realizedGLPercentTextView);

            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(getItem(position).getId());
                }
            });

            itemView.setOnLongClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION && longClickListener != null) {
                    Stock stock = getItem(position);
                    longClickListener.onItemLongClick(stock, v);
                    return true;
                }
                return false;
            });
        }
    }

    public static final class RealizedGainLoss {
        public final Money amount;
        public final double percent;

        public RealizedGainLoss(@NonNull Money amount, double percent) {
            this.amount = amount;
            this.percent = percent;
        }
    }
}
