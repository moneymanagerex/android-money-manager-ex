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

package com.money.manager.ex.search;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.money.manager.ex.R;
import com.money.manager.ex.view.RobotoCheckBox;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * View holder for Search Parameters view.
 */
public class SearchParametersViewHolder {

    public SearchParametersViewHolder(View view) {
        ButterKnife.bind(this, view);
    }

    @BindView(R.id.textViewFromDate) TextView txtDateFrom;
    @BindView(R.id.checkBoxDeposit) RobotoCheckBox cbxDeposit;
    @BindView(R.id.textViewFromAmount) TextView txtAmountFrom;
    @BindView(R.id.textViewToAmount) TextView txtAmountTo;
    @BindView(R.id.editTextTransNumber) EditText txtTransNumber;
    @BindView(R.id.textViewSelectPayee) TextView txtSelectPayee;
    @BindView(R.id.textViewToDate) TextView txtDateTo;
    @BindView(R.id.editTextNotes) EditText edtNotes;
}
