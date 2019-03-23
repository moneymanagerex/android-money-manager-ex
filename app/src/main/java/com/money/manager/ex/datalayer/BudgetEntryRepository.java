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

package com.money.manager.ex.datalayer;

import android.content.Context;
import android.database.Cursor;

import com.money.manager.ex.Constants;
import com.money.manager.ex.database.DatasetType;
import com.money.manager.ex.database.WhereStatementGenerator;
import com.money.manager.ex.domainmodel.BudgetEntry;

import java.util.HashMap;

/**
 * Budget Entry repository.
 */
public class BudgetEntryRepository
        extends RepositoryBase<BudgetEntry> {

    public static final String TABLE_NAME = "budgettable_v1";

    public BudgetEntryRepository(Context context) {
        super(context, TABLE_NAME, DatasetType.TABLE, "budgettable");
    }

    @Override
    public String[] getAllColumns() {
        return new String[] {"BUDGETENTRYID AS _id",
                BudgetEntry.BUDGETENTRYID,
                BudgetEntry.BUDGETYEARID,
                BudgetEntry.CATEGID,
                BudgetEntry.SUBCATEGID,
                BudgetEntry.PERIOD};
    }

    public BudgetEntry load(int id) {
        if (id == Constants.NOT_SET) return null;

        WhereStatementGenerator where = new WhereStatementGenerator();
        where.addStatement(BudgetEntry.BUDGETENTRYID, "=", id);

        BudgetEntry result = super.first(BudgetEntry.class,
                null,
                where.getWhere(),
                null,
                null);
        return result;
    }

    /**
     * Returns a string value which is used as a key in the budget entry thread cache
     * @param categoryId
     * @param subCategoryId
     * @return
     */
    public static String getKeyForCategories(int categoryId, int subCategoryId) {
        return Integer.toString(categoryId) + "_" + Integer.toString(subCategoryId);
    }

    public HashMap<String, BudgetEntry> loadForYear(long budgetYearId) {
        if (budgetYearId == Constants.NOT_SET) return null;

        WhereStatementGenerator where = new WhereStatementGenerator();
        where.addStatement(BudgetEntry.BUDGETYEARID, "=", budgetYearId);

        Cursor cursor = getContext().getContentResolver().query(getUri(),
                null,
                where.getWhere(),
                null,
                null);
        if (cursor == null) return null;

        HashMap<String, BudgetEntry> budgetEntryHashMap = new HashMap<>();

        while (cursor.moveToNext()) {
            BudgetEntry budgetEntry = new BudgetEntry();
            budgetEntry.loadFromCursor(cursor);

            int categoryId = cursor.getInt(cursor.getColumnIndex(BudgetEntry.CATEGID));
            int subcategoryId = cursor.getInt(cursor.getColumnIndex(BudgetEntry.SUBCATEGID));

            budgetEntryHashMap.put(getKeyForCategories(categoryId, subcategoryId), budgetEntry);
        }
        cursor.close();

        return budgetEntryHashMap;
    }

}
