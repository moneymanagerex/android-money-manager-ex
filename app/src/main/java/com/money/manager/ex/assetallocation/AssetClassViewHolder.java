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
package com.money.manager.ex.assetallocation;

import android.view.View;
import android.widget.LinearLayout;

import com.money.manager.ex.R;
import com.money.manager.ex.view.RobotoTextView;

/**
 * View holder for asset allocation / class item in the list.
 */
public class AssetClassViewHolder {

    /**
     * Create and initialize an instance of the holder.
     * Ref: http://developer.android.com/training/improving-layouts/smooth-scrolling.html
     *
     * @param view Container view (.xml)
     * @return Instance of the holder with the UI control references.
     */
    public static AssetClassViewHolder initialize(View view) {
        AssetClassViewHolder instance = new AssetClassViewHolder();

        instance.container = (LinearLayout) view.findViewById(R.id.container);

        instance.assetClassTextView = (RobotoTextView) view.findViewById(R.id.assetClassTextView);
        instance.allocationTextView = (RobotoTextView) view.findViewById(R.id.allocationTextView);
        instance.valueTextView = (RobotoTextView) view.findViewById(R.id.valueTextView);
        instance.currentAllocationTextView = (RobotoTextView) view.findViewById(R.id.currentAllocationTextView);
        instance.currentValueTextView = (RobotoTextView) view.findViewById(R.id.currentValueTextView);
        instance.differenceTextView = (RobotoTextView) view.findViewById(R.id.differenceTextView);
        instance.differencePercentTextView = (RobotoTextView) view.findViewById(R.id.differencePercentTextView);

        return instance;
    }

    public LinearLayout container;
    public RobotoTextView assetClassTextView;
    public RobotoTextView allocationTextView;
    public RobotoTextView valueTextView;
    public RobotoTextView currentAllocationTextView;
    public RobotoTextView currentValueTextView;
    public RobotoTextView differenceTextView;
    public RobotoTextView differencePercentTextView;
}
