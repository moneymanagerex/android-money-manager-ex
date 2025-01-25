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
import androidx.recyclerview.widget.RecyclerView;

/**
 * View holder for transaction editing (checking & recurring).
 */
public class EditTransactionViewHolder {

    public TextView dateTextView;
    public IconicsImageView previousDayButton;
    public IconicsImageView nextDayButton;

    public TextView categoryTextView;
    public TextView txtSelectPayee;
    public Spinner spinStatus;
    public Spinner spinAccount;
    public Spinner spinAccountTo;
    public TextView txtAmountTo;
    public TextView txtAmount;

    public ViewGroup tableRowPayee;
    public ViewGroup tableRowAmountTo;
    public ViewGroup tableRowAccountTo;
    public TextView accountFromLabel;
    public IconicsImageView swapAccountButton; //added by velmuruganc
    public TextView txtToAccount;
    public TextView amountHeaderTextView;
    public TextView amountToHeaderTextView;
    public ImageButton removePayeeButton;
    public FontIconView splitButton;
    public RelativeLayout withdrawalButton;
    public RelativeLayout depositButton;
    public RelativeLayout transferButton;
    public ImageButton btnTransNumber;
    public EditText edtTransNumber;
    public EditText edtNotes;
    public TextView textViewAttachments;
    public RecyclerView recyclerAttachments;
    public TextView tagsListTextView;
    public TextView colorTextView;

    public EditTransactionViewHolder(AppCompatActivity activity) {
        // Initialize views using findViewById
        dateTextView = activity.findViewById(R.id.textViewDate);
        previousDayButton = activity.findViewById(R.id.previousDayButton);
        nextDayButton = activity.findViewById(R.id.nextDayButton);

        categoryTextView = activity.findViewById(R.id.textViewCategory);
        txtSelectPayee = activity.findViewById(R.id.textViewPayee);
        spinStatus = activity.findViewById(R.id.spinnerStatus);
        spinAccount = activity.findViewById(R.id.spinnerAccount);
        swapAccountButton = activity.findViewById(R.id.swapAccountButton); //added by velmuruganc
        spinAccountTo = activity.findViewById(R.id.spinnerToAccount);
        txtAmountTo = activity.findViewById(R.id.textViewToAmount);
        txtAmount = activity.findViewById(R.id.textViewAmount);

        tableRowPayee = activity.findViewById(R.id.tableRowPayee);
        tableRowAmountTo = activity.findViewById(R.id.tableRowAmountTo);
        tableRowAccountTo = activity.findViewById(R.id.tableRowAccountTo);
        accountFromLabel = activity.findViewById(R.id.accountFromLabel);
        txtToAccount = activity.findViewById(R.id.textViewToAccount);
        amountHeaderTextView = activity.findViewById(R.id.textViewHeaderAmount);
        amountToHeaderTextView = activity.findViewById(R.id.textViewHeaderAmountTo);
        removePayeeButton = activity.findViewById(R.id.removePayeeButton);
        splitButton = activity.findViewById(R.id.splitButton);

        withdrawalButton = activity.findViewById(R.id.withdrawalButton);
        depositButton = activity.findViewById(R.id.depositButton);
        transferButton = activity.findViewById(R.id.transferButton);
        btnTransNumber = activity.findViewById(R.id.buttonTransNumber);
        edtTransNumber = activity.findViewById(R.id.editTextTransNumber);
        edtNotes = activity.findViewById(R.id.editTextNotes);
        textViewAttachments = activity.findViewById(R.id.textViewAttachments);
        recyclerAttachments = activity.findViewById(R.id.recyclerViewAttachments);

        tagsListTextView = activity.findViewById(R.id.tagsList);

        colorTextView = activity.findViewById(R.id.colorView);

        // Add custom icons
        UIHelper uiHelper = new UIHelper(activity);
        removePayeeButton.setImageDrawable(uiHelper.getIcon(GoogleMaterial.Icon.gmd_backspace));
    }
}
