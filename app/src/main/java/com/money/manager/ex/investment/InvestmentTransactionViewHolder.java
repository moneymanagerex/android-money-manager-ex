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

package com.money.manager.ex.investment;

import android.view.View;
import android.widget.Spinner;

import com.money.manager.ex.R;
import com.money.manager.ex.view.RobotoEditTextFontIcon;
import com.money.manager.ex.view.RobotoTextViewFontIcon;
import com.shamanland.fonticon.FontIconView;

/**
 * View Holder for Investment transaction editing.
 */
public class InvestmentTransactionViewHolder {
    public InvestmentTransactionViewHolder(View view) {
        accountSpinner = (Spinner) view.findViewById(R.id.spinnerAccount);
        dateView = (RobotoTextViewFontIcon) view.findViewById(R.id.textViewDate);
        numSharesView = (RobotoTextViewFontIcon) view.findViewById(R.id.numSharesView);
        stockNameEdit = (RobotoEditTextFontIcon) view.findViewById(R.id.stockNameEdit);
        symbolEdit = (RobotoEditTextFontIcon) view.findViewById(R.id.symbolEdit);
        notesEdit = (RobotoEditTextFontIcon) view.findViewById(R.id.notesEdit);
        previousDayButton = (FontIconView) view.findViewById(R.id.previousDayButton);
        nextDayButton = (FontIconView) view.findViewById(R.id.nextDayButton);
    }

    public Spinner accountSpinner;
    public RobotoTextViewFontIcon dateView;
    public RobotoTextViewFontIcon numSharesView;
    public RobotoEditTextFontIcon stockNameEdit;
    public RobotoEditTextFontIcon symbolEdit;
    public RobotoEditTextFontIcon notesEdit;
    public FontIconView previousDayButton;
    public FontIconView nextDayButton;
}
