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
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.money.manager.ex.R;
import com.money.manager.ex.datalayer.StockFields;

import java.util.ArrayList;
import java.util.List;

public class PortfolioRecyclerAdapter extends RecyclerView.Adapter<PortfolioRecyclerAdapter.ViewHolder> {

    private List<StockItem> stockItems = new ArrayList<>();
    private final LayoutInflater inflater;

    public PortfolioRecyclerAdapter(Context context, Cursor cursor) {
        this.inflater = LayoutInflater.from(context);
        if (cursor != null) {
            convertCursorToList(cursor);
        }
    }

    // Converts cursor data to stockItems list
    private void convertCursorToList(Cursor cursor) {
        if (cursor != null) {
            stockItems.clear();
            while (cursor.moveToNext()) {
                StockItem item = new StockItem(
                        cursor.getLong(cursor.getColumnIndex(StockFields.STOCKID)),
                        cursor.getString(cursor.getColumnIndex(StockFields.SYMBOL)),
                        cursor.getString(cursor.getColumnIndex(StockFields.NUMSHARES)),
                        cursor.getString(cursor.getColumnIndex(StockFields.CURRENTPRICE))
                );
                stockItems.add(item);
            }
        }
    }

    // Swap cursor and update the list
    public void swapCursor(Cursor cursor) {
        convertCursorToList(cursor);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_portfolio, parent, false);
        return new ViewHolder(view);
    }

    public StockItem getItem(int position) {
        if (position >= 0 && position < stockItems.size()) {
            return stockItems.get(position);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StockItem item = stockItems.get(position);
        holder.symbolTextView.setText(item.symbol);
        holder.numSharesView.setText(item.numShares);
        holder.priceTextView.setText(item.price);
    }

    @Override
    public int getItemCount() {
        return stockItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView symbolTextView, numSharesView, priceTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            symbolTextView = itemView.findViewById(R.id.symbolTextView);
            numSharesView = itemView.findViewById(R.id.numSharesView);
            priceTextView = itemView.findViewById(R.id.priceTextView);
        }
    }

    public static class StockItem {
        long stockId;
        String symbol;
        String numShares;
        String price;

        public StockItem(long stockId, String symbol, String numShares, String price) {
            this.stockId = stockId;
            this.symbol = symbol;
            this.numShares = numShares;
            this.price = price;
        }
    }
}
