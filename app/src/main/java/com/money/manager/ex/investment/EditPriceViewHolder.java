/*
 * Copyright (C) 2012-2024 The Android Money Manager Ex Project Team
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

package com.money.manager.ex.investment;

import android.view.View;

import com.mikepenz.iconics.view.IconicsImageView;
import com.money.manager.ex.R;
import com.money.manager.ex.view.RobotoTextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * View Holder pattern for edit price binaryDialog.
 */
public class EditPriceViewHolder {

    public RobotoTextView symbolTextView;
    public RobotoTextView amountTextView;
    public RobotoTextView dateTextView;
    @Nullable public IconicsImageView previousDayButton;
    @Nullable public IconicsImageView nextDayButton;

    public void bind(View view) {
        // Manually bind views
        symbolTextView = view.findViewById(R.id.symbolTextView);
        amountTextView = view.findViewById(R.id.amountTextView);
        dateTextView = view.findViewById(R.id.dateTextView);
        previousDayButton = view.findViewById(R.id.previousDayButton);
        nextDayButton = view.findViewById(R.id.nextDayButton);
    }

    public void bind(AppCompatActivity activity) {
        // Manually bind views
        symbolTextView = activity.findViewById(R.id.symbolTextView);
        amountTextView = activity.findViewById(R.id.amountTextView);
        dateTextView = activity.findViewById(R.id.dateTextView);
        previousDayButton = activity.findViewById(R.id.previousDayButton);
        nextDayButton = activity.findViewById(R.id.nextDayButton);
    }
}
