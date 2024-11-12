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

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.money.manager.ex.Constants;
import com.money.manager.ex.R;
import com.money.manager.ex.common.MmxBaseFragmentActivity;
import com.money.manager.ex.core.MenuHelper;
import com.money.manager.ex.datalayer.BudgetRepository;
import com.money.manager.ex.domainmodel.Budget;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.utils.MmxDate;

import timber.log.Timber;

public class BudgetEditActivity extends MmxBaseFragmentActivity {

    public static final String KEY_BUDGET_ID = "budgetId";

    private BudgetViewModel mModel;
    private BudgetEditViewHolder viewHolder;

    private TextView budgetYearTextView;
    private TextView budgetMonthTextView;

    private boolean useNestedCategory = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget_edit);

        // Initialize views using findViewById()
        budgetYearTextView = findViewById(R.id.budgetYearTextView);
        budgetMonthTextView = findViewById(R.id.budgetMonthTextView);

        useNestedCategory = (new AppSettings(this.getBaseContext()).getBehaviourSettings().getUseNestedCategory());

        initializeToolbar();

        initializeModel();
        showModel();

        // Set up click listeners for views
        budgetYearTextView.setOnClickListener(this::onSelectYear);
        budgetMonthTextView.setOnClickListener(this::onSelectMonth);
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
                // Cancel clicked. Prompt to confirm?
                Timber.d("going back");
                break;
            case MenuHelper.save:
                return onActionDoneClick();
        }
        return super.onOptionsItemSelected(item);
    }

    private void onSelectYear(View v) {
        int currentYear = new MmxDate().getYear();
        int year = (mModel.year != 0) ? mModel.year : currentYear;

        NumberPicker numberPicker = new NumberPicker(this);
        numberPicker.setMinValue(currentYear - 10);
        numberPicker.setMaxValue(currentYear + 10);
        numberPicker.setValue(year);

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.year))
                .setView(numberPicker)
                .setPositiveButton("OK", (dialog, which) -> {
                    int selectedYear = numberPicker.getValue();
                    mModel.setYear(selectedYear);
                    viewHolder.refreshYear();
                    viewHolder.refreshName();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void onSelectMonth(View v) {
        int month = (mModel.month != 0) ? mModel.month : new MmxDate().getMonthOfYear();

        NumberPicker numberPicker = new NumberPicker(this);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(12);
        numberPicker.setValue(month);

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.month))
                .setView(numberPicker)
                .setPositiveButton("OK", (dialog, which) -> {
                    int selectedMonth = numberPicker.getValue();
                    mModel.setMonth(selectedMonth);
                    viewHolder.refreshMonth();
                    viewHolder.refreshName();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /*
        Private
    */

    private long getBudgetId() {
        return getIntent().getLongExtra(KEY_BUDGET_ID, Constants.NOT_SET);
    }

    private void initializeModel() {
        Budget budget = null;
        Intent intent = getIntent();
        if (intent == null) return;
        String action = intent.getAction();
        if (TextUtils.isEmpty(action)) return;

        if (action.equals(Intent.ACTION_INSERT)) {
            // New record
            budget = new Budget();
        } else if (action.equals(Intent.ACTION_EDIT)) {
            // Existing record
            long budgetId = getBudgetId();
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
        long budgetId = getBudgetId();
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
