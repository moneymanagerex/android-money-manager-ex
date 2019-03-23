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
package com.money.manager.ex.home;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.money.manager.ex.R;

import androidx.recyclerview.widget.RecyclerView;

/**
 * ViewHolder for the linear drawer items.
 */
public class DrawerViewHolder
    extends RecyclerView.ViewHolder {

    public DrawerViewHolder(View itemView) {
        super(itemView);

        TextView textViewItem = (TextView)itemView.findViewById(R.id.textViewItem);
        ImageView imageViewIcon = (ImageView)itemView.findViewById(R.id.imageViewIcon);
        View viewDivider = itemView.findViewById(R.id.viewDivider);

        this.textViewItem = textViewItem;
        this.imageViewIcon = imageViewIcon;
        this.viewDivider = viewDivider;
    }

    public TextView textViewItem;
    public ImageView imageViewIcon;
    public View viewDivider;

}
