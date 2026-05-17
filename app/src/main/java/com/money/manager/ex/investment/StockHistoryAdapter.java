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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.core.FormatUtilities;
import com.money.manager.ex.domainmodel.StockHistory;
import com.money.manager.ex.utils.MmxDate;
import com.money.manager.ex.utils.MmxDateTimeUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

public class StockHistoryAdapter extends RecyclerView.Adapter<StockHistoryAdapter.ViewHolder> {

    private final Context context;
    private final MmxDateTimeUtils dateTimeUtils;
    private final FormatUtilities formatUtilities;
    private final OnItemClickListener listener;
    private final OnItemDeleteListener deleteListener;
    private List<StockHistory> items = new ArrayList<>();

    public interface OnItemClickListener {
        void onItemClick(Date date, Money price);
    }

    public interface OnItemDeleteListener {
        void onItemDelete(StockHistory item, int position);
    }

    public StockHistoryAdapter(Context context, OnItemClickListener listener, OnItemDeleteListener deleteListener) {
        this.context = context;
        this.listener = listener;
        this.deleteListener = deleteListener;
        this.dateTimeUtils = new MmxDateTimeUtils();
        this.formatUtilities = new FormatUtilities(context);
    }

    public void setData(List<StockHistory> data) {
        this.items = data != null ? data : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void removeAt(int position) {
        if (position < 0 || position >= items.size()) return;
        items.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, items.size() - position);
    }

    /** Returns the index of the first row whose date is <= isoDate (list is newest-first). */
    public int findPositionForDate(String isoDate) {
        for (int i = 0; i < items.size(); i++) {
            String itemDate = items.get(i).getString(StockHistory.DATE);
            if (itemDate == null) continue;
            if (itemDate.compareTo(isoDate) <= 0) return i;
        }
        return items.isEmpty() ? -1 : items.size() - 1;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_stock_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StockHistory history = items.get(position);

        Date date = history.getDate();
        String dateDisplay = dateTimeUtils.getUserFormattedDate(context, date);
        holder.dateView.setText(dateDisplay);

        String valueStr = history.getString(StockHistory.VALUE);
        Money price = valueStr != null ? MoneyFactory.fromString(valueStr) : MoneyFactory.fromDouble(0);
        holder.priceView.setText(formatUtilities.format(price, Constants.PRICE_FORMAT));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(date, price);
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (deleteListener != null) deleteListener.onItemDelete(history, position);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView dateView;
        final TextView priceView;
        final com.mikepenz.iconics.view.IconicsImageView deleteButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            dateView = itemView.findViewById(R.id.historyDateTextView);
            priceView = itemView.findViewById(R.id.historyPriceTextView);
            deleteButton = itemView.findViewById(R.id.historyDeleteButton);
        }
    }
}
