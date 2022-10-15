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

package com.money.manager.ex.budget;

import android.widget.TextView;

import com.money.manager.ex.R;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * View Holder for Budget Edit screen.
 */

class BudgetEditViewHolder {
    BudgetEditViewHolder(AppCompatActivity activity) {
        ButterKnife.bind(this, activity);
    }

    @BindView(R.id.budgetNameTextView) TextView budgetNameTextView;
    @BindView(R.id.budgetYearTextView) TextView budgetYearTextView;
    @BindView(R.id.budgetMonthTextView) TextView budgetMonthTextView;

    private BudgetViewModel model;

    public void bind(BudgetViewModel model) {
        this.model = model;

        refreshName();
        refreshYear();
        refreshMonth();
    }

    public void refreshName() {
        budgetNameTextView.setText(model.getName());
    }

    public void refreshYear() {
        budgetYearTextView.setText(model.getYear());
    }

    public void refreshMonth() {
        budgetMonthTextView.setText(model.getMonth());
    }
}
