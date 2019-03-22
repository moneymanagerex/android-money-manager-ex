/*
 * Copyright (C) 2012-2019 The Android Money Manager Ex Project Team
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
package com.money.manager.ex.currency.list;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.money.manager.ex.R;
import com.money.manager.ex.currency.recycler.CurrencyListItemViewHolder;
import com.money.manager.ex.domainmodel.Currency;

import androidx.cursoradapter.widget.CursorAdapter;

/**
 * Adapter for the list of currencies
 */
public class CurrencyListAdapter
    extends CursorAdapter {

    //private Context mContext;

    public CurrencyListAdapter(Context context, Cursor cursor) {
        super(context, cursor, -1);

        //mContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.item_currency, parent, false);

        // holder
        CurrencyListItemViewHolder holder = new CurrencyListItemViewHolder(view);
        // add holder to the view.
        view.setTag(holder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        CurrencyListItemViewHolder holder = (CurrencyListItemViewHolder) view.getTag();

        // name
        String name = cursor.getString(cursor.getColumnIndex(Currency.CURRENCYNAME));
        holder.name.setText(name);

        // exchange rate
        String rate = cursor.getString(cursor.getColumnIndex(Currency.BASECONVRATE));
        holder.rate.setText(rate);
    }
}
