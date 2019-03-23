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

package com.money.manager.ex.investment;

import android.view.View;
import android.widget.Spinner;

import com.money.manager.ex.R;
import com.money.manager.ex.view.RobotoEditText;
import com.money.manager.ex.view.RobotoTextView;
import com.shamanland.fonticon.FontIconView;

/**
 * View Holder for Investment transaction editing.
 */
public class InvestmentTransactionViewHolder {
    public InvestmentTransactionViewHolder(View view) {
        accountSpinner = view.findViewById(R.id.spinnerAccount);
        dateView = view.findViewById(R.id.textViewDate);
        numSharesView = view.findViewById(R.id.numSharesView);
        stockNameEdit = view.findViewById(R.id.stockNameEdit);
        symbolEdit = view.findViewById(R.id.symbolEdit);
        notesEdit = view.findViewById(R.id.notesEdit);
        previousDayButton = view.findViewById(R.id.previousDayButton);
        nextDayButton = view.findViewById(R.id.nextDayButton);
    }

    public Spinner accountSpinner;
    public RobotoTextView dateView;
    public RobotoTextView numSharesView;
    public RobotoEditText stockNameEdit;
    public RobotoEditText symbolEdit;
    public RobotoEditText notesEdit;
    public FontIconView previousDayButton;
    public FontIconView nextDayButton;
}
