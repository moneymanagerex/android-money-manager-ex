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

package com.money.manager.ex.assetallocation.full;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.money.manager.ex.R;
import com.money.manager.ex.view.RobotoTextView;

/**
 * View Holder for the full asset class.
 */
public class FullAssetClassViewHolder
    extends RecyclerView.ViewHolder {

    public FullAssetClassViewHolder(View itemView) {
        super(itemView);

        listItem = itemView.findViewById(R.id.list_item);
        assetClassTextView = (RobotoTextView) itemView.findViewById(R.id.assetClassTextView);
        setAllocationTextView = (RobotoTextView) itemView.findViewById(R.id.setAllocationTextView);
        currentAllocationTextView = (RobotoTextView) itemView.findViewById(R.id.currentAllocationTextView);
        allocationDiffTextView = (RobotoTextView) itemView.findViewById(R.id.allocationDiffTextView);
    }

    /**
     * This is the root element of the view.
     */
    public View listItem;
    public RobotoTextView assetClassTextView;
    public RobotoTextView setAllocationTextView;
    public RobotoTextView currentAllocationTextView;
    public RobotoTextView allocationDiffTextView;
}
