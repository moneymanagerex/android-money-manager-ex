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

package com.money.manager.ex.search;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.money.manager.ex.R;
import com.money.manager.ex.view.RobotoCheckBox;

/**
 * View holder for Search Parameters view.
 */
public class SearchParametersViewHolder {

    public TextView txtDateFrom;
    public RobotoCheckBox cbxDeposit;
    public TextView txtAmountFrom;
    public TextView txtAmountTo;
    public EditText txtTransNumber;
    public TextView txtSelectPayee;
    public TextView txtDateTo;
    public EditText edtNotes;
    public TextView txtSelectTag;

    public SearchParametersViewHolder(View view) {
        // Initialize views using findViewById
        txtDateFrom = view.findViewById(R.id.textViewFromDate);
        cbxDeposit = view.findViewById(R.id.checkBoxDeposit);
        txtAmountFrom = view.findViewById(R.id.textViewFromAmount);
        txtAmountTo = view.findViewById(R.id.textViewToAmount);
        txtTransNumber = view.findViewById(R.id.editTextTransNumber);
        txtSelectPayee = view.findViewById(R.id.textViewSelectPayee);
        txtDateTo = view.findViewById(R.id.textViewToDate);
        edtNotes = view.findViewById(R.id.editTextNotes);
        txtSelectTag = view.findViewById(R.id.textViewSelectTag);
    }
}
