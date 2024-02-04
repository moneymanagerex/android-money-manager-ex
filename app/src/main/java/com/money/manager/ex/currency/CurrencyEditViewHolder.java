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
package com.money.manager.ex.currency;

import android.widget.EditText;
import android.widget.Spinner;

import com.money.manager.ex.R;

import androidx.appcompat.app.AppCompatActivity;

/**
 * View Holder pattern for the currencies list.
 */
public class CurrencyEditViewHolder {
    public EditText edtCurrencyName, edtUnitName, edtCentsName, edtPrefix, edtSuffix,
            edtDecimal, edtGroup, edtScale, edtConversion;
    public Spinner spinCurrencySymbol;

    public static CurrencyEditViewHolder initialize(AppCompatActivity activity) {
        CurrencyEditViewHolder holder = new CurrencyEditViewHolder();

        holder.edtCurrencyName = activity.findViewById(R.id.editTextCurrencyName);
        holder.spinCurrencySymbol = activity.findViewById(R.id.spinCurrencySymbol);
        holder.edtUnitName = activity.findViewById(R.id.editTextUnitName);
        holder.edtCentsName = activity.findViewById(R.id.editTextCentsName);
        holder.edtPrefix = activity.findViewById(R.id.editTextPrefixSymbol);
        holder.edtSuffix = activity.findViewById(R.id.editTextSuffixSymbol);
        holder.edtDecimal = activity.findViewById(R.id.editTextDecimalChar);
        holder.edtGroup = activity.findViewById(R.id.editTextGroupChar);
        holder.edtScale = activity.findViewById(R.id.editTextScale);
        holder.edtConversion = activity.findViewById(R.id.editTextConversion);

        return holder;
    }

}
