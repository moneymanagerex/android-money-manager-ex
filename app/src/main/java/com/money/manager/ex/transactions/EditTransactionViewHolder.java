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

package com.money.manager.ex.transactions;

import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.view.IconicsImageView;
import com.money.manager.ex.R;
import com.money.manager.ex.core.UIHelper;
import com.shamanland.fonticon.FontIconView;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * View holder for transaction editing (checking & recurring).
 */
public class EditTransactionViewHolder {

    public EditTransactionViewHolder(AppCompatActivity view) {
        ButterKnife.bind(this, view);

        // add custom icons
        UIHelper uiHelper = new UIHelper(view);
        removePayeeButton.setImageDrawable(uiHelper.getIcon(GoogleMaterial.Icon.gmd_backspace));
    }

    @BindView(R.id.textViewDate) public TextView dateTextView;
    @BindView(R.id.previousDayButton) public IconicsImageView previousDayButton;
    @BindView(R.id.nextDayButton) public IconicsImageView nextDayButton;

    @BindView(R.id.textViewCategory) TextView categoryTextView;
    @BindView(R.id.textViewPayee) TextView txtSelectPayee;
    @BindView(R.id.spinnerStatus) Spinner spinStatus;
    @BindView(R.id.spinnerAccount) Spinner spinAccount;
    @BindView(R.id.spinnerToAccount) Spinner spinAccountTo;
    @BindView(R.id.textViewToAmount) TextView txtAmountTo;
    @BindView(R.id.textViewAmount) TextView txtAmount;

    @BindView(R.id.tableRowPayee) ViewGroup tableRowPayee;
    @BindView(R.id.tableRowAmountTo) ViewGroup tableRowAmountTo;
    @BindView(R.id.tableRowAccountTo) ViewGroup tableRowAccountTo;
    @BindView(R.id.accountFromLabel) TextView accountFromLabel;
    @BindView(R.id.textViewToAccount) TextView txtToAccount;
    @BindView(R.id.textViewHeaderAmount) TextView amountHeaderTextView;
    @BindView(R.id.textViewHeaderAmountTo) TextView amountToHeaderTextView;
    @BindView(R.id.removePayeeButton) ImageButton removePayeeButton;
    @BindView(R.id.splitButton) FontIconView splitButton;
    // Transaction types
    @BindView(R.id.withdrawalButton) RelativeLayout withdrawalButton;
    @BindView(R.id.depositButton) RelativeLayout depositButton;
    @BindView(R.id.transferButton) RelativeLayout transferButton;
    @BindView(R.id.buttonTransNumber) ImageButton btnTransNumber;
    @BindView(R.id.editTextTransNumber) public EditText edtTransNumber;
    @BindView(R.id.editTextNotes) public EditText edtNotes;
}
