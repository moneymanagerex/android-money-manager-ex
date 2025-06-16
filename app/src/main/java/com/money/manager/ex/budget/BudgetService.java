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

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;

import com.money.manager.ex.Constants;
import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.database.QueryMobileData;
import com.money.manager.ex.database.WhereStatementGenerator;
import com.money.manager.ex.datalayer.BudgetEntryRepository;
import com.money.manager.ex.datalayer.BudgetRepository;
import com.money.manager.ex.domainmodel.Budget;
import com.money.manager.ex.domainmodel.BudgetEntry;
import com.money.manager.ex.servicelayer.ServiceBase;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.settings.BudgetSettings;
import com.money.manager.ex.utils.MmxDate;
import com.squareup.sqlbrite3.BriteDatabase;

import java.util.Date;

import javax.inject.Inject;

import dagger.Lazy;

/**
 * Budgets business logic
 */
public class BudgetService
        extends ServiceBase {

    @Inject
    Lazy<BriteDatabase> databaseLazy;

    public BudgetService(Context context) {
        super(context);
        MmexApplication.getApp().iocComponent.inject(this);
    }

    public boolean delete(long budgetId) {

        // we need to delete both BudgetRepository and BudgetEntryRepository
        BudgetEntryRepository entryRepo = new BudgetEntryRepository(getContext());
        entryRepo.deleteForYear(budgetId);

        BudgetRepository repo = new BudgetRepository(getContext());
        return repo.delete(budgetId);
    }

    /**
     * Copy budget. It will load the budget with entries and create a copy.
     * Need to get the budget destination period. The period can be only a year/month like the
     * original budget.
     *
     * @param budgetId The budget to copy.
     */
    public void copy(long budgetId) {
        //todo complete
    }

    public boolean isCategoryOverDueBudget(long categId, Date date) {
        MmxDate mmxDate = new MmxDate(date);
        return isCategoryOverDueBudget(categId, mmxDate, 0);
    }
    public boolean isCategoryOverDueBudget(long categId, MmxDate date) {
        return isCategoryOverDueBudget(categId, date, 0);
    }

    public boolean isCategoryOverDueBudget(long categId, Date date, double amount) {
        MmxDate mmxDate = new MmxDate(date);
        return isCategoryOverDueBudget(categId, mmxDate, amount);
    }

    public boolean isCategoryOverDueBudget(long categId, MmxDate date, double amount) {
        // we need BudgetEntry to get amount of budget
        // and actual value

        // get Budget from transaction date, if no budget no overdue
        Budget budget = new BudgetRepository(getContext()).loadFromDate(date);
        if (budget == null) return false;

        // get Budget Entry from transaction date, if no budget no overdue
        BudgetEntry budgetEntry = loadByYearIDAndCateID(budget.getId(), categId);
        if (budgetEntry == null) return false;

        // if budget not set, no overdue
        if (budgetEntry.getPeriodEnum() == BudgetPeriodEnum.NONE)
            return false;

        double actualValue = 0;
        double budgetValue = 0;
        if (budget.isMonthlyBudget()) {
            actualValue = getActualValueForCategoryAndPeriod(categId, budget.getYear(), budget.getMonth());
            budgetValue = budgetEntry.getMonthlyAmount();
        } else {
            actualValue = getActualValueForCategoryAndPeriod(categId, budget.getYear());
            budgetValue = budgetEntry.getYearlyAmount();
        }

        if ( actualValue > 0 || budgetValue > 0 ) {
            return false; // for now no overdue for income
        }
        // Amount and ActualAmount is negative for expences
        return ( actualValue + amount ) < budgetValue;

    }

    public BudgetEntry loadByDateAndCateID(MmxDate date, long categId) {
        BudgetEntryRepository budgetEntryRepository = new BudgetEntryRepository(getContext());
        return budgetEntryRepository.loadByDateAndCateID(date, categId);
    }

    public BudgetEntry loadByYearIDAndCateID(long yearId, long categId) {
        BudgetEntryRepository budgetEntryRepository = new BudgetEntryRepository(getContext());
        return budgetEntryRepository.loadByYearIdAndCateID(yearId, categId);
    }

    public double getActualValueForCategoryAndPeriod(long categId, int year) {
        return getActualValueForCategoryAndPeriod(categId, year, 0);
    }

    public double getActualValueForCategoryAndPeriod(long categId, int year, int month) {
        BudgetSettings budgetSettings = (new AppSettings(getContext()).getBudgetSettings());

        String[] projectionIn = new String[]{
                QueryMobileData.CATEGID,
                "SUM( " + QueryMobileData.AmountBaseConvRate + ") AS TOTAL"
        };

        WhereStatementGenerator where = new WhereStatementGenerator();
        where.addStatement(QueryMobileData.Status + "<>'V'");
        where.addStatement(QueryMobileData.TransactionType + " IN ('Withdrawal', 'Deposit')");
        where.addStatement(QueryMobileData.CATEGID + " = " + categId);

        if (month > 0) {
            // month
            where.addStatement(QueryMobileData.Month + "=" + month);
            where.addStatement(QueryMobileData.Year + "=" + year);
        } else {
            where.addStatement(QueryMobileData.Date + " BETWEEN '" + budgetSettings.getBudgetDateFromForYear(year).toIsoDateString() + "' AND '" + budgetSettings.getBudgetDateToForYear(year).toIsoDateString()+"'");
        }

        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        QueryMobileData mobileData = new QueryMobileData(getContext());
        builder.setTables(mobileData.getSource());
        String sql = builder.buildQuery(projectionIn, where.getWhere(), QueryMobileData.CATEGID, null, null, null);
        Cursor cursor = databaseLazy.get().query(sql);

        if (cursor == null) return 0;
        // add all the categories and subcategories together.
        double total = 0;
        while (cursor.moveToNext()) {
            total += cursor.getDouble(cursor.getColumnIndexOrThrow("TOTAL"));
        }
        cursor.close();

        return total;
    }


}
