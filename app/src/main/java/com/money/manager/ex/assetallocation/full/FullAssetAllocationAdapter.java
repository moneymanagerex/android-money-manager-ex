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

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.money.manager.ex.R;
import com.money.manager.ex.assetallocation.ItemType;
import com.money.manager.ex.core.FormatUtilities;

import java.util.List;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

/**
 * Adapter for the full Asset Allocation display.
 */
public class FullAssetAllocationAdapter
    extends RecyclerView.Adapter<FullAssetClassViewHolder> {

    public FullAssetAllocationAdapter(List<AssetClassViewModel> model, Money diffThreshold, FormatUtilities formatter) {
        this.model = model;
        this.differenceThreshold = diffThreshold;
        mFormatter = formatter;
    }

    private Context context;
    private List<AssetClassViewModel> model;
    private Money differenceThreshold = MoneyFactory.fromDouble(100);
//    private int expandedPosition = Constants.NOT_SET;
    private FormatUtilities mFormatter;

    @Override
    public FullAssetClassViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        View view = LayoutInflater.from(this.context).inflate(R.layout.item_full_asset_class, parent, false);
        return new FullAssetClassViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FullAssetClassViewHolder holder, int position) {
        AssetClassViewModel item = this.model.get(position);

        // color the background, depending on the level.
        if (!item.assetClass.getType().equals(ItemType.Allocation)) {
            int colorDepth = 50 * item.level;
            holder.listItem.setBackgroundColor(Color.argb(225, 0, 100 + colorDepth, 0));
        } else {
            // reset bg color
            holder.listItem.setBackgroundColor(Color.TRANSPARENT);
        }

        holder.assetClassTextView.setText(item.assetClass.getName());
        holder.setAllocationTextView.setText(item.assetClass.getAllocation().toString());
        holder.currentAllocationTextView.setText(item.assetClass.getCurrentAllocation().toString());

        // % diff
        Money diff = item.assetClass.getDiffAsPercentOfSet();
        holder.allocationDiffTextView.setText(diff.toString());
        if (diff.toDouble() >= this.differenceThreshold.toDouble()) {
            holder.allocationDiffTextView.setTextColor(Color.GREEN);
        }
        if (diff.toDouble() <= this.differenceThreshold.multiply(-1).toDouble()) {
            holder.allocationDiffTextView.setTextColor(Color.RED);
        }

        holder.setValueTextView.setText(mFormatter.getValueFormattedInBaseCurrency(item.assetClass.getValue()));
        holder.currentValueTextView.setText(mFormatter.getValueFormattedInBaseCurrency(item.assetClass.getCurrentValue()));
        holder.valueDiffTextView.setText(mFormatter.getValueFormattedInBaseCurrency(item.assetClass.getDifference()));

        holder.setLevel(item.level, this.context);

//        if (position == expandedPosition) {
//            holder.valuetPanel.setVisibility(View.VISIBLE);
//        } else {
//            holder.valuetPanel.setVisibility(View.GONE);
//        }
    }

    @Override
    public int getItemCount() {
        return this.model.size();
    }

//    public void onClick(View view) {
//        FullAssetClassViewHolder holder = (FullAssetClassViewHolder) view.getTag();
//
//        // Check for an expanded view, collapse if you find one
//        if (expandedPosition >= 0) {
//            int prev = expandedPosition;
//            notifyItemChanged(prev);
//        }
//        // Set the current position to "expanded"
//        expandedPosition = holder.getPosition();
//        notifyItemChanged(expandedPosition);
//    }
}
