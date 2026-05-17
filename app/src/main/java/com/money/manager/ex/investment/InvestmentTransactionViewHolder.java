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

package com.money.manager.ex.investment;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.money.manager.ex.R;
import com.money.manager.ex.view.RobotoCheckBox;
import com.money.manager.ex.view.RobotoEditText;
import com.money.manager.ex.view.RobotoTextView;
import com.shamanland.fonticon.FontIconView;

/**
 * View Holder for Investment transaction editing.
 */
public class InvestmentTransactionViewHolder {
    public Spinner accountSpinner;
    public Spinner transactionTypeSpinner;
    public Spinner statusSpinner;
    public TextView textViewPayee;
    public TextView categoryTextView;
    public RobotoCheckBox transferCheckBox;
    public RobotoTextView dateView;
    public RobotoTextView numSharesView;
    public RobotoEditText stockNameEdit;
    public RobotoEditText symbolEdit;
    public RobotoEditText notesEdit;
    public FontIconView previousDayButton;
    public FontIconView nextDayButton;
    public RobotoTextView purchasePriceView;
    public RobotoTextView totalPriceView;
    public RobotoTextView commissionView;
    public RobotoTextView currentPriceView;
    public RobotoTextView valueView;
    public Button buyButton;
    public Button sellButton;

    public InvestmentTransactionViewHolder(Activity activity) {
        accountSpinner = activity.findViewById(R.id.spinnerAccount);
        transactionTypeSpinner = activity.findViewById(R.id.spinnerTransactionType);
        statusSpinner = activity.findViewById(R.id.spinnerStatus);
        textViewPayee = activity.findViewById(R.id.textViewPayee);
        categoryTextView = activity.findViewById(R.id.textViewCategory);
        transferCheckBox = activity.findViewById(R.id.checkBoxTransfer);
        dateView = activity.findViewById(R.id.textViewDate);
        numSharesView = activity.findViewById(R.id.numSharesView);
        stockNameEdit = activity.findViewById(R.id.stockNameEdit);
        symbolEdit = activity.findViewById(R.id.symbolEdit);
        notesEdit = activity.findViewById(R.id.notesEdit);
        previousDayButton = activity.findViewById(R.id.previousDayButton);
        nextDayButton = activity.findViewById(R.id.nextDayButton);
        purchasePriceView = activity.findViewById(R.id.purchasePriceView);
        totalPriceView = activity.findViewById(R.id.totalPriceView);
        commissionView = activity.findViewById(R.id.commissionView);
        currentPriceView = activity.findViewById(R.id.currentPriceView);
        valueView = activity.findViewById(R.id.valueView);
        buyButton = activity.findViewById(R.id.buyButton);
        sellButton = activity.findViewById(R.id.sellButton);
    }
}
