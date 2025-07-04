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

package com.money.manager.ex.datalayer;

import android.content.Context;

import com.money.manager.ex.database.DatasetType;
import com.money.manager.ex.domainmodel.Budget;
import com.money.manager.ex.utils.MmxDate;

/**
 * Budget repository.
 */
public class BudgetRepository
    extends RepositoryBase<Budget> {

    private static final String TABLE_NAME = "budgetyear_v1";
    private static final String ID_COLUMN = Budget.BUDGETYEARID;
    private static final String NAME_COLUMN = Budget.BUDGETYEARNAME;

    public BudgetRepository(Context context) {
        super(context, TABLE_NAME, DatasetType.TABLE, "budgetyear", ID_COLUMN, NAME_COLUMN);
    }

    @Override
    public Budget createEntity() {
        return new Budget();
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public String[] getAllColumns() {
        return new String[] {ID_COLUMN + " AS _id", Budget.BUDGETYEARID, Budget.BUDGETYEARNAME};
    }


    /**
     * Load budget for given date. If exist montly budget, return it,
     * otherwise try to get yearly...
     * @param date reference date
     * @return budget for given date
     */
    public Budget loadFromDate(MmxDate date) {
        String budgetName = date.toString("YYYY-MM");

        Budget budget = loadByName(budgetName);

        if ( budget == null ) {
            budgetName = date.toString("YYYY");
            budget = loadByName(budgetName);
        }

        return budget;
    }


}
