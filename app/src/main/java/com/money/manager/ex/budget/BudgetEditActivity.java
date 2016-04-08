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

package com.money.manager.ex.budget;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.os.Bundle;

import com.money.manager.ex.R;
import com.money.manager.ex.common.BaseFragmentActivity;
import com.money.manager.ex.databinding.ActivityBudgetEditBinding;
import com.money.manager.ex.domainmodel.Budget;

public class BudgetEditActivity
    extends BaseFragmentActivity {

    public static final String KEY_BUDGET_ID = "budgetId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_budget_edit);

        // this handles OK/Cancel button clicks in the toolbar.
        setToolbarStandardAction(getToolbar());

        // todo data binding, existing or new budget.
        ActivityBudgetEditBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_budget_edit);
        Budget budget = new Budget();
        budget.setName("test");
//        BudgetViewModel model = new BudgetViewModel();

        binding.setBudget(budget);

    }

    @Override
    public boolean onActionCancelClick() {
        setResult(Activity.RESULT_CANCELED);
        finish();
        return true;
    }

    @Override
    public boolean onActionDoneClick() {
//        if (saveAccount()) {
//            // If everything is okay, finish the activity
//        setResult(RESULT_OK);
//            finish();
//            return true;
//        } else {
            return false;
//        }
    }

}
