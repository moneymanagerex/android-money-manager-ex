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

package com.money.manager.ex.currency.recycler;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.money.manager.ex.R;

import androidx.recyclerview.widget.RecyclerView;

/**
 * View holder for the currency list implemented with recycler view.
 */
public class CurrencyListItemViewHolder
    extends RecyclerView.ViewHolder {

    public CurrencyListItemViewHolder(View view) {
        super(view);

        // Setup view holder.
        row = (LinearLayout) view.findViewById(R.id.row);
        name = (TextView) view.findViewById(R.id.name);
        rate = (TextView) view.findViewById(R.id.rate);
    }

    public LinearLayout row;
    public TextView name;
    public TextView rate;
}
