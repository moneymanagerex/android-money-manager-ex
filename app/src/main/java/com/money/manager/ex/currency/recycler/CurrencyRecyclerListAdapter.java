/*
 * Copyright (C) 2012-2016 The Android Money Manager Ex Project Team
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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.sectionedrecyclerview.SectionedRecyclerViewAdapter;
import com.money.manager.ex.R;

/**
 * Adapter for the currency list implemented with RecyclerView.
 */
public class CurrencyRecyclerListAdapter
    extends SectionedRecyclerViewAdapter<CurrencyRecyclerListViewHolder> {

    @Override
    public int getSectionCount() {
        return 2; // number of sections.
    }

    @Override
    public int getItemCount(int section) {
        return 8; // number of items in section (section index is parameter).
    }

    @Override
    public void onBindHeaderViewHolder(CurrencyRecyclerListViewHolder holder, int section) {
        // Setup header view.
        //holder.
    }

    @Override
    public void onBindViewHolder(CurrencyRecyclerListViewHolder holder, int section, int relativePosition, int absolutePosition) {
        // Setup non-header view.
        // 'section' is section index.
        // 'relativePosition' is index in this section.
        // 'absolutePosition' is index out of all non-header items.
        // See sample project for a visual of how these indices work.

        //holder.
    }

    @Override
    public CurrencyRecyclerListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Change inflated layout based on 'header'.
        View v = LayoutInflater.from(parent.getContext())
                .inflate(viewType == VIEW_TYPE_HEADER
                        ? R.layout.item_currency_list_recycler_header
                        : R.layout.item_currency_list_recycler_item, parent, false);
        return new CurrencyRecyclerListViewHolder(v);
    }

}
