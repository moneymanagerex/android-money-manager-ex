/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
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

import android.database.MatrixCursor;
import android.view.View;
import android.widget.TextView;

import com.money.manager.ex.R;
import com.money.manager.ex.view.RobotoTextView;

/**
 * Common UI methods.
 */
public class UIHelpers {
    /**
     * Populates an asset class row view.
     * @param view The view to populate with data.
     */
    public static void populateAssetClassRow(View view, MatrixCursorColumns values) {
        // name
        TextView nameView = (TextView) view.findViewById(R.id.assetClassTextView);
        nameView.setText(values.name);

        // set allocation
        TextView allocationView = (TextView) view.findViewById(R.id.allocationTextView);
        allocationView.setText(values.allocation);

        // set value
        TextView valueView = (TextView) view.findViewById(R.id.valueTextView);
        valueView.setText(values.value);

        // current allocation
        RobotoTextView currentAllocationView = (RobotoTextView) view.findViewById(R.id.currentAllocationTextView);
        currentAllocationView.setText(values.currentAllocation);

        // current value
        RobotoTextView currentValueView = (RobotoTextView) view.findViewById(R.id.currentValueTextView);
        currentValueView.setText(values.currentValue);

        // difference (value)
        RobotoTextView differenceView = (RobotoTextView) view.findViewById(R.id.differenceTextView);
        differenceView.setText(values.difference);
    }


}
