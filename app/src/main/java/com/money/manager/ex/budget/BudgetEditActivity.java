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

package com.money.manager.ex.budget;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.codetroopers.betterpickers.numberpicker.NumberPickerBuilder;
import com.codetroopers.betterpickers.numberpicker.NumberPickerDialogFragment;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.common.MmxBaseFragmentActivity;
import com.money.manager.ex.core.MenuHelper;
import com.money.manager.ex.datalayer.BudgetRepository;
import com.money.manager.ex.domainmodel.Budget;
import com.money.manager.ex.utils.MmxDate;

import java.math.BigDecimal;
import java.math.BigInteger;

import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class BudgetEditActivity
    extends MmxBaseFragmentActivity {

    public static final String KEY_BUDGET_ID = "budgetId";

    private BudgetViewModel mModel;
    private BudgetEditViewHolder viewHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        LayoutInflaterCompat.setFactory(getLayoutInflater(), new IconicsLayoutInflater(getDelegate()));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget_edit);

        ButterKnife.bind(this);

        initializeToolbar();

        initializeModel();
        showModel();
    }

    public boolean onActionDoneClick() {
        if (save()) {
            setResult(RESULT_OK);
            finish();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuHelper menuHelper = new MenuHelper(this, menu);
        menuHelper.addSaveToolbarIcon();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // cancel clicked. Prompt to confirm?
                Timber.d("going back");
                break;
            case MenuHelper.save:
                return onActionDoneClick();
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.budgetYearTextView)
    public void onSelectYear(View v) {
        int currentYear = new MmxDate().getYear();
        int year;
        if (mModel.year != 0) {
            year = mModel.year;
        } else {
            year = currentYear;
        }

        new NumberPickerBuilder()
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
                    viewHolder.refreshYear();
                    viewHolder.refreshName();
                }
            })
            .show();
    }

    @OnClick(R.id.budgetMonthTextView)
    public void onSelectMonth(View v) {
        int month;
        if (mModel.month != 0) {
            month = mModel.month;
        } else {
            month = new MmxDate().getMonthOfYear();
        }

        new NumberPickerBuilder()
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
                    viewHolder.refreshMonth();
                    viewHolder.refreshName();
                }
            })
            .show();
    }

    /*
        Private
    */

    private int getBudgetId() {
        return getIntent().getIntExtra(KEY_BUDGET_ID, Constants.NOT_SET);
    }

    private void initializeModel() {
        Budget budget = null;
        Intent intent = getIntent();
        if (intent == null) return;
        String action = intent.getAction();
        if (TextUtils.isEmpty(action)) return;

        if (action.equals(Intent.ACTION_INSERT)) {
            // new record
            budget = new Budget();
        }
        if (action.equals(Intent.ACTION_EDIT)) {
            // existing record
            int budgetId = getBudgetId();
            BudgetRepository repo = new BudgetRepository(this);
            budget = repo.load(budgetId);
        }

        mModel = BudgetViewModel.from(budget);
    }

    private void initializeToolbar() {
        // Title
        CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle(getString(R.string.budget));

        // Back arrow / cancel.
        setDisplayHomeAsUpEnabled(true);
    }

    private boolean save() {
        int budgetId = getBudgetId();
        Budget budget = new Budget();
        budget.setId(budgetId);
        mModel.saveTo(budget);

        BudgetRepository repo = new BudgetRepository(this);
        return repo.save(budget);
    }

    private void showModel() {
        this.viewHolder = new BudgetEditViewHolder(this);
        viewHolder.bind(mModel);
    }
}
