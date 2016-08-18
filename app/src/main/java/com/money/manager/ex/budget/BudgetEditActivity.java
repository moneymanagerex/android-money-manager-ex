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
import android.view.View;

import com.codetroopers.betterpickers.numberpicker.NumberPickerBuilder;
import com.codetroopers.betterpickers.numberpicker.NumberPickerDialogFragment;
import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.common.BaseFragmentActivity;
import com.money.manager.ex.databinding.ActivityBudgetEditBinding;
import com.money.manager.ex.datalayer.BudgetRepository;
import com.money.manager.ex.domainmodel.Budget;
import com.money.manager.ex.utils.MmxDateTimeUtils;

import java.math.BigDecimal;
import java.math.BigInteger;

public class BudgetEditActivity
    extends BaseFragmentActivity {

    public static final String KEY_BUDGET_ID = "budgetId";

    private BudgetViewModel mModel;
    private ActivityBudgetEditBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_budget_edit);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_budget_edit);

        // this handles OK/Cancel button clicks in the toolbar.
        showStandardToolbarActions();

        handleIntent();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // todo save model
        //outState.
    }

    @Override
    public boolean onActionCancelClick() {
        setResult(Activity.RESULT_CANCELED);
        finish();
        return true;
    }

    @Override
    public boolean onActionDoneClick() {
        if (save()) {
            // If everything is okay, finish the activity
        setResult(RESULT_OK);
            finish();
            return true;
        } else {
            return false;
        }
    }

    public void onSelectYear(View v) {
        int currentYear = MmxDateTimeUtils.today().getYear();
        int year;
        if (mModel.year != 0) {
            year = mModel.year;
        } else {
            year = currentYear;
        }

        NumberPickerBuilder npb = new NumberPickerBuilder()
            .setFragmentManager(getSupportFragmentManager())
            .setStyleResId(R.style.BetterPickersDialogFragment)
            .setLabelText(getString(R.string.year))
            .setPlusMinusVisibility(View.INVISIBLE)
            .setDecimalVisibility(View.INVISIBLE)
            .setMinNumber(BigDecimal.valueOf(currentYear - 10))
            .setMaxNumber(BigDecimal.valueOf(currentYear + 10))
            .setCurrentNumber(year)
            .addNumberPickerDialogHandler(new NumberPickerDialogFragment.NumberPickerDialogHandlerV2() {
                @Override
                public void onDialogNumberSet(int reference, BigInteger number, double decimal, boolean isNegative, BigDecimal fullNumber) {
                    mModel.setYear(number.intValue());
                }
            });
        npb.show();
    }

    public void onSelectMonth(View v) {
        int month;
        if (mModel.month != 0) {
            month = mModel.month;
        } else {
            month = MmxDateTimeUtils.today().getMonthOfYear();
        }

        NumberPickerBuilder npb = new NumberPickerBuilder()
            .setFragmentManager(getSupportFragmentManager())
            .setStyleResId(R.style.BetterPickersDialogFragment)
            .setLabelText(getString(R.string.month))
            .setPlusMinusVisibility(View.INVISIBLE)
            .setDecimalVisibility(View.INVISIBLE)
            .setMinNumber(BigDecimal.ONE)
            .setMaxNumber(BigDecimal.valueOf(12))
            .setCurrentNumber(month)
            .addNumberPickerDialogHandler(new NumberPickerDialogFragment.NumberPickerDialogHandlerV2() {
                @Override
                public void onDialogNumberSet(int reference, BigInteger number, double decimal, boolean isNegative, BigDecimal fullNumber) {
                    mModel.setMonth(number.intValue());
                }
            });
        npb.show();
    }

    // Private

    private int getBudgetId() {
        return getIntent().getIntExtra(KEY_BUDGET_ID, Constants.NOT_SET);
    }

    private void handleIntent() {
        Budget budget = null;
        if (getIntent().getAction().equals(Intent.ACTION_INSERT)) {
            // new record
            budget = new Budget();
        }
        if (getIntent().getAction().equals(Intent.ACTION_EDIT)) {
            // existing record
            int budgetId = getBudgetId();
            BudgetRepository repo = new BudgetRepository(this);
            budget = repo.load(budgetId);
        }

        mModel = BudgetViewModel.from(budget);

        mBinding.setBudget(mModel);
    }

    private boolean save() {
        int budgetId = getBudgetId();
        Budget budget = new Budget();
        budget.setId(budgetId);
        mModel.saveTo(budget);

        BudgetRepository repo = new BudgetRepository(this);
        return repo.save(budget);
    }
}
