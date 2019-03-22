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

package com.money.manager.ex.currency.recycler;

import android.view.View;

import com.money.manager.ex.R;
import com.money.manager.ex.domainmodel.Currency;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;

/**
 * Used Currencies section for the Currencies recycler view
 */
public class CurrencySection
    extends StatelessSection {

    public CurrencySection(String title, List<Currency> data) {
        super(SectionParameters.builder()
                .itemResourceId(R.layout.item_currency)
                .headerResourceId(R.layout.item_currency_list_recycler_header)
                .build());

        this.title = title;

        if (data != null) {
            this.currencies = data;
        } else {
            this.currencies = new ArrayList<>();
        }
    }

    public List<Currency> currencies;
    public String title;

    @Override
    public int getContentItemsTotal() {
        return currencies.size();
    }

    @Override
    public RecyclerView.ViewHolder getItemViewHolder(View view) {
        return new CurrencyListItemViewHolder(view);
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
        CurrencyListItemViewHolder viewHolder = (CurrencyListItemViewHolder) holder;

//        Currency currency = currencies.get(position);
        Currency currency = getItemAtPosition(position);

        viewHolder.name.setText(currency.getName());

        String rate = Double.toString(currency.getBaseConversionRate());
        viewHolder.rate.setText(rate);
    }

    @Override
    public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
        return new CurrencyListItemViewHolder(view);
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
        CurrencyListItemViewHolder viewHolder = (CurrencyListItemViewHolder) holder;

        viewHolder.name.setText(title);
    }

    public Currency getItemAtPosition(int position) {
        //return (new ArrayList<>(currencies.values())).get(position);
        return currencies.get(position);
    }
}
