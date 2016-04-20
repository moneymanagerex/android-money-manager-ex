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

package com.money.manager.ex.transactions;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.money.manager.ex.R;
import com.shamanland.fonticon.FontIconView;

/**
 * View holder for transaction editing (checking & recurring).
 */
public class EditTransactionViewHolder {

    public EditTransactionViewHolder(Activity view) {
        // Date
        dateTextView = (TextView) view.findViewById(R.id.textViewDate);

        // Status
        spinStatus = (Spinner) view.findViewById(R.id.spinnerStatus);

        // Payee
        txtSelectPayee = (TextView) view.findViewById(R.id.textViewPayee);
        removePayeeButton = (FontIconView) view.findViewById(R.id.removePayeeButton);
        tableRowPayee = (ViewGroup) view.findViewById(R.id.tableRowPayee);

        // Category / Split
        splitButton = (FontIconView) view.findViewById(R.id.splitButton);
        categoryTextView = (TextView) view.findViewById(R.id.textViewCategory);

        // Account
        spinAccount = (Spinner) view.findViewById(R.id.spinnerAccount);
        accountFromLabel = (TextView) view.findViewById(R.id.accountFromLabel);

        tableRowAccountTo = (ViewGroup) view.findViewById(R.id.tableRowAccountTo);
        txtToAccount = (TextView) view.findViewById(R.id.textViewToAccount);
        spinAccountTo = (Spinner) view.findViewById(R.id.spinnerToAccount);

        // Amounts
        amountHeaderTextView = (TextView) view.findViewById(R.id.textViewHeaderAmount);
        amountToHeaderTextView = (TextView) view.findViewById(R.id.textViewHeaderAmountTo);

        txtAmount = (TextView) view.findViewById(R.id.textViewAmount);
        txtAmountTo = (TextView) view.findViewById(R.id.textViewTotAmount);
        tableRowAmountTo = (ViewGroup) view.findViewById(R.id.tableRowAmountTo);

        // Transaction Type
        withdrawalButton = (RelativeLayout) view.findViewById(R.id.withdrawalButton);
        depositButton = (RelativeLayout) view.findViewById(R.id.depositButton);
        transferButton = (RelativeLayout) view.findViewById(R.id.transferButton);
    }

    public TextView dateTextView;
    public TextView categoryTextView;
    public TextView txtSelectPayee;
    public Spinner spinAccount, spinAccountTo, spinStatus;
    public TextView txtAmountTo, txtAmount;

    public ViewGroup tableRowPayee, tableRowAmountTo, tableRowAccountTo;
    public TextView accountFromLabel, txtToAccount;
    public TextView amountHeaderTextView, amountToHeaderTextView;
    public FontIconView removePayeeButton, splitButton;
    public RelativeLayout withdrawalButton, depositButton, transferButton;
    public ImageButton btnTransNumber;
    public EditText edtTransNumber, edtNotes;
   
}
