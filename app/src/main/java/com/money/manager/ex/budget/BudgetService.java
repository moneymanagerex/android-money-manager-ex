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

import com.money.manager.ex.datalayer.BudgetRepository;
import com.money.manager.ex.servicelayer.ServiceBase;

/**
 * Budgets business logic
 */
public class BudgetService
        extends ServiceBase {

    public BudgetService(Context context) {
        super(context);
    }

    public boolean delete(int budgetId) {
        BudgetRepository repo = new BudgetRepository(getContext());
        return repo.delete(budgetId);
    }

    /**
     * Copy budget. It will load the budget with entries and create a copy.
     * Need to get the budget destination period. The period can be only a year/month like the
     * original budget.
     * @param budgetId The budget to copy.
     */
    public void copy(int budgetId) {
        //todo complete
    }
}
