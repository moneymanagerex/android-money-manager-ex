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

import android.view.View;
import android.widget.TextView;

import com.money.manager.ex.R;
import com.shamanland.fonticon.FontIconView;

import androidx.recyclerview.widget.RecyclerView;

/**
 * View Holder pattern for Split Category.
 * Adapter position is provided automatically by .getAdapterPosition().
 */
public class SplitItemViewHolder extends RecyclerView.ViewHolder {

    public TextView txtSelectCategory;
    public TextView txtAmount;
    public TextView txtNotesSplit;
    public FontIconView transactionTypeButton;
    public TextView txtTagsList;

    public SplitItemViewHolder(View itemView) {
        super(itemView);

        // Initialize views using findViewById
        txtSelectCategory = itemView.findViewById(R.id.textViewCategory);
        txtAmount = itemView.findViewById(R.id.editTextTotAmount);
        txtNotesSplit = itemView.findViewById(R.id.notesEditSplit);
        transactionTypeButton = itemView.findViewById(R.id.transactionTypeButton);
        txtTagsList = itemView.findViewById(R.id.tagsList);
    }
}
