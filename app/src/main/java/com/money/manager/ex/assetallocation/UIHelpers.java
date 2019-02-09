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
package com.money.manager.ex.assetallocation;

import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

/**
 * Common UI methods.
 */
public class UIHelpers {
    /**
     * Populates an asset class row view.
     */
    public static void populateAssetClassRow(AssetClassViewHolder viewHolder, MatrixCursorColumns values) {
        // name
        viewHolder.assetClassTextView.setText(values.name);

        // set allocation
        viewHolder.allocationTextView.setText(values.allocation);

        // set value
        if (viewHolder.valueTextView != null) {
            viewHolder.valueTextView.setText(values.value);
        }

        // current allocation
        viewHolder.currentAllocationTextView.setText(values.currentAllocation);

        // current value
        if (viewHolder.currentValueTextView != null) {
            viewHolder.currentValueTextView.setText(values.currentValue);
        }

        // difference %
        viewHolder.differencePercentTextView.setText(values.differencePercent);
        // difference (value)
        viewHolder.differenceTextView.setText(values.difference);
    }

    public static Fragment getVisibleFragment(FragmentActivity activity){
        FragmentManager fragmentManager = activity.getSupportFragmentManager();

        List<Fragment> fragments = fragmentManager.getFragments();
        if (fragments == null) return null;

        for(Fragment fragment : fragments){
            if(fragment != null && fragment.isVisible())
                return fragment;
        }
        return null;
    }
}
