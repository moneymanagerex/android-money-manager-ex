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

package com.money.manager.ex.budget;

import android.widget.TextView;

import com.money.manager.ex.R;

import androidx.appcompat.app.AppCompatActivity;

/**
 * View Holder for Budget Edit screen.
 */

public class BudgetEditViewHolder {
    private final TextView budgetNameTextView;
    private final TextView budgetYearTextView;
    private final TextView budgetMonthTextView;

    private BudgetViewModel model;

    BudgetEditViewHolder(AppCompatActivity activity) {
        // Initialize views using findViewById()
        budgetNameTextView = activity.findViewById(R.id.budgetNameTextView);
        budgetYearTextView = activity.findViewById(R.id.budgetYearTextView);
        budgetMonthTextView = activity.findViewById(R.id.budgetMonthTextView);
    }

    public void bind(BudgetViewModel model) {
        this.model = model;

        refreshName();
        refreshYear();
        refreshMonth();
    }

    public void refreshName() {
        if (budgetNameTextView == null) return;
        budgetNameTextView.setText(model.getName());
    }

    public void refreshYear() {
        if (budgetYearTextView == null) return;
        budgetYearTextView.setText(model.getYear());
    }

    public void refreshMonth() {
        if (budgetMonthTextView == null) return;
        budgetMonthTextView.setText(model.getMonth());
    }
}
