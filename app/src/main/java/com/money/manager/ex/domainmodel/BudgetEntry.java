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

package com.money.manager.ex.domainmodel;

import com.money.manager.ex.datalayer.StockFields;

/**
 * A Budget Entry, part of the budget.
 */
public class BudgetEntry
    extends EntityBase {

    public static final String BUDGETENTRYID = "BUDGETENTRYID";
    public static final String BUDGETYEARID = "BUDGETYEARID";
    public static final String CATEGID = "CATEGID";
    public static final String PERIOD = "PERIOD";
    public static final String AMOUNT = "AMOUNT";
    public static final String NOTES = "NOTES";
    public static final String ACTIVE = "ACTIVE";

    public Long getCategId() {
        return getLong(CATEGID);
    }

    @Override
    public String getPrimaryKeyColumn() {
        return BudgetEntry.BUDGETENTRYID;  // This returns the column name specific to Report
    }
}
