/*
 * Copyright (C) 2012-2017 The Android Money Manager Ex Project Team
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

import com.money.manager.ex.R;
import com.money.manager.ex.view.RobotoTextView;
import com.shamanland.fonticon.FontIconView;

/**
 * View Holder pattern for edit price binaryDialog.
 */
public class EditPriceViewHolder {

    public EditPriceViewHolder(View view) {
        dateTextView = (RobotoTextView) view.findViewById(R.id.dateTextView);
        previousDayButton = (FontIconView) view.findViewById(R.id.previousDayButton);
        nextDayButton = (FontIconView) view.findViewById(R.id.nextDayButton);
        amountTextView = (RobotoTextView) view.findViewById(R.id.amountTextView);
    }

    public RobotoTextView amountTextView;
    public RobotoTextView dateTextView;
    public FontIconView previousDayButton;
    public FontIconView nextDayButton;
}
