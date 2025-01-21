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

package com.money.manager.ex.account;

import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.mikepenz.iconics.view.IconicsImageView;
import com.money.manager.ex.R;
import com.money.manager.ex.view.RobotoTextView;
import com.shamanland.fonticon.FontIconView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * ViewHolder pattern for the account edit screen.
 */
public class AccountEditViewHolder {
    public EditText edtAccountHeldAt;
    public Spinner accountTypeSpinner;
    public CheckBox defaultAccountCheckbox;
    public RobotoTextView defaultAccountText;
    public FontIconView imageViewAccountFav;
    public RobotoTextView favouriteAccountTextView;
    public EditText webSiteEditText;
    public EditText edtAccountName;
    public EditText edtAccountNumber;
    public EditText edtContact;
    public EditText edtAccessInfo;
    public Spinner spinAccountStatus;
    public EditText edtNotes;
    public Spinner spinSymbolInitialBalance;
    public TextView txtSelectCurrency;
    public TextView txtInitialBalance;
    public TextView txtInitialDate;
    public IconicsImageView previousDayButton;
    public IconicsImageView nextDayButton;

    public AccountEditViewHolder(AppCompatActivity parent) {
        edtAccountHeldAt = parent.findViewById(R.id.editTextAccountHeldAt);
        accountTypeSpinner = parent.findViewById(R.id.spinnerAccountType);
        defaultAccountCheckbox = parent.findViewById(R.id.defaultAccountCheckbox);
        defaultAccountText = parent.findViewById(R.id.defaultAccountText);
        imageViewAccountFav = parent.findViewById(R.id.imageViewAccountFav);
        favouriteAccountTextView = parent.findViewById(R.id.favouriteAccountTextView);
        webSiteEditText = parent.findViewById(R.id.editTextWebsite);
        edtAccountName = parent.findViewById(R.id.editTextAccountName);
        edtAccountNumber = parent.findViewById(R.id.editTextAccountNumber);
        edtContact = parent.findViewById(R.id.editTextContact);
        edtAccessInfo = parent.findViewById(R.id.editTextAccessInfo);
        spinAccountStatus = parent.findViewById(R.id.spinnerAccountStatus);
        edtNotes = parent.findViewById(R.id.editTextNotes);
        spinSymbolInitialBalance = parent.findViewById(R.id.spinnerSymbolInitialBalance);
        txtSelectCurrency = parent.findViewById(R.id.textViewSelectCurrency);
        txtInitialBalance = parent.findViewById(R.id.editTextInitialBalance);
        txtInitialDate = parent.findViewById(R.id.textViewDate);
        previousDayButton = parent.findViewById(R.id.previousDayButton);
        nextDayButton = parent.findViewById(R.id.nextDayButton);
    }
}

