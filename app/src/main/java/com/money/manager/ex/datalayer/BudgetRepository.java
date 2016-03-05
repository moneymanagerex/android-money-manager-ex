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

package com.money.manager.ex.datalayer;

import android.content.Context;

import com.money.manager.ex.Constants;
import com.money.manager.ex.database.DatasetType;
import com.money.manager.ex.database.WhereStatementGenerator;
import com.money.manager.ex.domainmodel.Budget;

/**
 * Budget repository.
 */
public class BudgetRepository
    extends RepositoryBase<Budget> {

    public BudgetRepository(Context context) {
        super(context, "budgetyear_v1", DatasetType.TABLE, "budgetyear");

    }

    @Override
    public String[] getAllColumns() {
        return new String[] {"BUDGETYEARID AS _id", Budget.BUDGETYEARID, Budget.BUDGETYEARNAME};
    }

    public Budget load(int id) {
        if (id == Constants.NOT_SET) return null;

        WhereStatementGenerator where = new WhereStatementGenerator();
        where.addStatement(Budget.BUDGETYEARID, "=", id);

        return query(where.getWhere());
    }

    public Budget query(String selection) {
        //todo finish
//        return query(null, selection, null);
        return null;
    }

}
