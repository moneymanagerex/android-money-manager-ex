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

import java.util.Objects;

public class PortfolioListAdapter extends ListAdapter<Stock, PortfolioListAdapter.ViewHolder> {
    private final LayoutInflater inflater;
    private OnItemClickListener listener;
    private final Account mAccount;
    private final CurrencyService mCurrencyService;

    public interface OnItemClickListener {
        void onItemClick(long stockId);
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

    public PortfolioListAdapter(Context context, Account account) {
        super(DIFF_CALLBACK);
        this.inflater = LayoutInflater.from(context);
        this.mAccount = account;
        this.mCurrencyService = new CurrencyService(context);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
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
        holder.symbolTextView.setText(stock.getSymbol());
        holder.numSharesView.setText(String.valueOf(stock.getNumberOfShares()));
        holder.priceTextView.setText(mCurrencyService.getCurrencyFormatted(mAccount.getCurrencyId(), stock.getCurrentPrice()));

        // Zebra striping
        if (position % 2 == 0) {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.darker_gray));
        } else {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), android.R.color.white));
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView symbolTextView;
        TextView numSharesView;
        TextView priceTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            symbolTextView = itemView.findViewById(R.id.symbolTextView);
            numSharesView = itemView.findViewById(R.id.numSharesView);
            priceTextView = itemView.findViewById(R.id.priceTextView);

            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(getItem(position).getId());
                }
            });
        }
    }
}
