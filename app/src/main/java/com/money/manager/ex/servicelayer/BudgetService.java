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

package com.money.manager.ex.servicelayer;

import android.content.Context;

import com.money.manager.ex.Constants;
import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.budget.models.BudgetModel;
import com.money.manager.ex.datalayer.BudgetEntryRepository;
import com.money.manager.ex.datalayer.BudgetRepository;
import com.money.manager.ex.datalayer.CategoryRepository;
import com.money.manager.ex.domainmodel.Budget;
import com.money.manager.ex.domainmodel.BudgetEntry;
import com.money.manager.ex.domainmodel.Category;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.settings.BudgetSettings;
import com.money.manager.ex.utils.MmxDate;
import com.squareup.sqlbrite3.BriteDatabase;

import java.util.List;

import javax.inject.Inject;

import dagger.Lazy;

/**
 * Budgets business logic
 */
public class BudgetService
        extends ServiceBase {

    BudgetRepository budgetRepository;
    BudgetEntryRepository budgetEntryRepository;
    CategoryRepository categoryRepository;

    public BudgetService(Context context) {
        this(context,
                new BudgetRepository(context),
                new BudgetEntryRepository(context),
                new CategoryRepository(context));
    }

    public BudgetService(Context context,
                         BudgetRepository budgetRepository,
                         BudgetEntryRepository budgetEntryRepository,
                         CategoryRepository categoryRepository) {
        super(context);
        this.budgetRepository = budgetRepository;
        this.budgetEntryRepository = budgetEntryRepository;
        this.categoryRepository = categoryRepository;
    }


    public BudgetModel loadFullBudget(long budgetId) {
        if ( budgetId == Constants.NOT_SET ) return null;
        Budget budget = budgetRepository.load(budgetId);
        if (budget == null ) return null;
        BudgetModel budgetModel = new BudgetModel(budget,
                    budgetEntryRepository.loadForBudgetId(budgetId));
        // during load we need to be sure that all categories are available in budget
        budgetModel = mergeWithCategory(budgetModel);
        return budgetModel;

    }

    public BudgetModel loadFromDate(MmxDate date) {
        Budget budget =  budgetRepository.loadFromDate(date);
        if ( budget == null ) return null;

        return new BudgetModel(budget,
                budgetEntryRepository.loadForBudgetId(budget.getId()));

    }

    public void saveFullBudget(BudgetModel budgetModel) {
        budgetRepository.save(budgetModel.getHead());
        // rassign id to item
        for (BudgetEntry entry : budgetModel.getItemsAsList()) {
            entry.setBudgetYearId(budgetModel.getHead().getId());
        }
        budgetEntryRepository.saveAll(budgetModel.getItemsAsList());
    }

    public boolean deleteFullBudget(long budgetId) {
        budgetEntryRepository.deleteForBudgetId(budgetId);
        return budgetRepository.delete(budgetId);
    }

    private BudgetModel mergeWithCategory(BudgetModel budgetModel){
        List<Category> categories = categoryRepository.loadAll();
        for (Category category : categories) {
            if (budgetModel.getItem(category.getId()) != null) {
                continue;
            }
            BudgetEntry entry = new BudgetEntry();
            entry.setCategoryId(category.getId());
            budgetModel.addItem(entry);
        }
        return budgetModel;
    }

    /**
     * Copy budget. It will load the budget with entries and create a copy.
     * Need to get the budget destination period. The period can be only a year/month like the
     * original budget.
     *
     * @param fromBudgetId The budget to copy.
     * @param toBudgetId The existing budget destination.
     */
    public void copyFullBudget(long fromBudgetId, long toBudgetId) {
        List<BudgetEntry> result = budgetEntryRepository.loadForBudgetId(fromBudgetId);
        if (result == null) return;
        for (BudgetEntry entry : result) {
            entry.setBudgetYearId(toBudgetId);
            entry.setId(Constants.NOT_SET);
            budgetEntryRepository.save(entry);
        }
    }

    /**
     * Create new budget stargtin from another.
     * @param fromBudgetId
     * @param newName new budget name
     */
    public BudgetModel copyFullBudget(long fromBudgetId, String newName) {
        BudgetModel budgetFrom = loadFullBudget(fromBudgetId);
        if ( budgetFrom == null ) return  null;
        BudgetModel budgetTo = new BudgetModel(newName);
        budgetRepository.save(budgetTo.getHead()); // now we have the id
        for (BudgetEntry entry : budgetFrom.getItemsAsList()) {
            entry.setBudgetYearId(budgetTo.getHead().getId());
            entry.setId(Constants.NOT_SET);
            budgetEntryRepository.save(entry);
        }
        return loadFullBudget(budgetTo.getHead().getId());
    }

}
