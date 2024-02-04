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
import android.database.Cursor;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.cursoradapter.widget.CursorAdapter;

import com.money.manager.ex.R;
import com.money.manager.ex.datalayer.StockFields;

import java.util.HashMap;

/**
 * Cursor adapter for stock list (portfolio).
 */
public class PortfolioCursorAdapter
        extends CursorAdapter {

    private final LayoutInflater mInflater;
    private final HashMap<Integer, Integer> mHeadersAccountIndex;
    private final SparseBooleanArray mCheckedPosition;

    public PortfolioCursorAdapter(final Context context, final Cursor cursor) {
        super(context, cursor, -1);

        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mHeadersAccountIndex = new HashMap<>();
        mCheckedPosition = new SparseBooleanArray();
        //mContext = context;
    }

    @Override
    public View newView(final Context context, final Cursor cursor, final ViewGroup parent) {
        final View view = mInflater.inflate(R.layout.item_portfolio, parent, false);

        // holder
        final PortfolioItemDataViewHolder holder = new PortfolioItemDataViewHolder();

        holder.symbolTextView = view.findViewById(R.id.symbolTextView);
        holder.numSharesView = view.findViewById(R.id.numSharesView);
        holder.priceTextView = view.findViewById(R.id.priceTextView);

        // set holder to view
        view.setTag(holder);

        return view;
    }

    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {
        // take a holder
        final PortfolioItemDataViewHolder holder = (PortfolioItemDataViewHolder) view.getTag();

        // header index
        final int accountId = cursor.getInt(cursor.getColumnIndex(StockFields.HELDAT));
        if (!mHeadersAccountIndex.containsKey(accountId)) {
            mHeadersAccountIndex.put(accountId, cursor.getPosition());
        }

        // symbol
        final String symbol = cursor.getString(cursor.getColumnIndex(StockFields.SYMBOL));
        holder.symbolTextView.setText(symbol);

        // number of shares
        final String numberOfShares = cursor.getString(cursor.getColumnIndex(StockFields.NUMSHARES));
        holder.numSharesView.setText(numberOfShares);

        // price
        final String price = cursor.getString(cursor.getColumnIndex(StockFields.CURRENTPRICE));
        holder.priceTextView.setText(price);

        // check if item is checked
        if (mCheckedPosition.get(cursor.getPosition(), false)) {
            view.setBackgroundResource(R.color.material_green_100);
        } else {
            view.setBackgroundResource(android.R.color.transparent);
        }
    }
}
