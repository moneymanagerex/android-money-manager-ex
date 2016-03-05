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
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;

import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.common.BaseFragmentActivity;
import com.money.manager.ex.databinding.ActivityBudgetEditBinding;
import com.money.manager.ex.datalayer.BudgetRepository;
import com.money.manager.ex.domainmodel.Budget;

public class BudgetEditActivity
    extends BaseFragmentActivity {

    public static final String KEY_BUDGET_ID = "budgetId";

    private Budget mEntity;
    private ActivityBudgetEditBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_budget_edit);

        // todo data binding, existing or new budget.
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_budget_edit);

        // this handles OK/Cancel button clicks in the toolbar.
        setToolbarStandardAction(getToolbar());

        // handle intent

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

    // Private

    private void bindData() {
        mEntity.setName("test");
//        BudgetViewModel model = new BudgetViewModel();

        mBinding.setBudget(mEntity);
    }

    private void handleIntent() {
        if (getIntent().getAction().equals(Intent.ACTION_INSERT)) {
            // new record
            mEntity = new Budget();
        }
        if (getIntent().getAction().equals(Intent.ACTION_EDIT)) {
            // existing record
            int budgetId = getIntent().getIntExtra(KEY_BUDGET_ID, Constants.NOT_SET);
            BudgetRepository repo = new BudgetRepository(this);
            mEntity = repo.load(budgetId);
        }
    }
}
