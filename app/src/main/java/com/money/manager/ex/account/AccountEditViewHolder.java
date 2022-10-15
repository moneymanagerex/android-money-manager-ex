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

package com.money.manager.ex.account;

import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.money.manager.ex.R;
import com.money.manager.ex.view.RobotoTextView;
import com.shamanland.fonticon.FontIconView;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * ViewHolder pattern for the account edit screen.
 */
public class AccountEditViewHolder {
    public AccountEditViewHolder(AppCompatActivity parent) {
        ButterKnife.bind(this, parent);
    }

    @BindView(R.id.editTextAccountHeldAt) EditText edtAccountHeldAt;

    @BindView(R.id.spinnerAccountType) Spinner accountTypeSpinner;
    @BindView(R.id.defaultAccountCheckbox) CheckBox defaultAccountCheckbox;
    @BindView(R.id.defaultAccountText) RobotoTextView defaultAccountText;
    @BindView(R.id.imageViewAccountFav) FontIconView imageViewAccountFav;
    @BindView(R.id.favouriteAccountTextView) RobotoTextView favouriteAccountTextView;
    @BindView(R.id.editTextWebsite) EditText webSiteEditText;

    @BindView(R.id.editTextAccountName) EditText edtAccountName;
    @BindView(R.id.editTextAccountNumber) EditText edtAccountNumber;
    @BindView(R.id.editTextContact) EditText edtContact;
    @BindView(R.id.editTextAccessInfo) EditText edtAccessInfo;
    @BindView(R.id.spinnerAccountStatus) Spinner spinAccountStatus;
    @BindView(R.id.editTextNotes) EditText edtNotes;
    @BindView(R.id.spinnerSymbolInitialBalance) Spinner spinSymbolInitialBalance;
    @BindView(R.id.textViewSelectCurrency) TextView txtSelectCurrency;
    @BindView(R.id.editTextInitialBalance) TextView txtInitialBalance;

}
