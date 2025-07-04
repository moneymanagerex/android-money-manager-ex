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
import android.database.Cursor;

import com.money.manager.ex.Constants;
import com.money.manager.ex.database.DatasetType;
import com.money.manager.ex.database.WhereStatementGenerator;
import com.money.manager.ex.domainmodel.Budget;
import com.money.manager.ex.domainmodel.BudgetEntry;
import com.money.manager.ex.nestedcategory.NestedCategoryEntity;
import com.money.manager.ex.nestedcategory.QueryNestedCategory;
import com.money.manager.ex.utils.MmxDate;

import java.util.HashMap;

/**
 * Budget Entry repository.
 */
public class BudgetEntryRepository
        extends RepositoryBase<BudgetEntry> {

    private static final String TABLE_NAME = "budgettable_v1";
    private static final String ID_COLUMN = BudgetEntry.BUDGETENTRYID;
    private static final String NAME_COLUMN = BudgetEntry.BUDGETYEARID;

    public BudgetEntryRepository(Context context) {
        super(context, TABLE_NAME, DatasetType.TABLE, "budgettable", ID_COLUMN, NAME_COLUMN);
    }

    @Override
    public BudgetEntry createEntity() {
        return new BudgetEntry();
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public String[] getAllColumns() {
        return new String[]{ID_COLUMN + " AS _id",
                BudgetEntry.BUDGETENTRYID,
                BudgetEntry.BUDGETYEARID,
                BudgetEntry.CATEGID,
                BudgetEntry.PERIOD,
                BudgetEntry.AMOUNT,
                BudgetEntry.NOTES,
                BudgetEntry.ACTIVE
        };
    }

    /**
     * Returns a string value which is used as a key in the budget entry thread cache
     *
     * @param categoryId Category ID
     * @return key for budget entry thread cache
     */
    public static String getKeyForCategories(long categoryId) {
        // Wolfsolver - adapt budget for category & sub category.
        return "_" + categoryId;
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

        // use nested category
        QueryNestedCategory categoryRepositoryNested = new QueryNestedCategory(null);
        while (cursor.moveToNext()) {
            BudgetEntry budgetEntry = new BudgetEntry();
            budgetEntry.loadFromCursor(cursor);

            NestedCategoryEntity nestedCategory = categoryRepositoryNested.getOneCategoryEntity(budgetEntry.getCategoryId());
            if (nestedCategory == null) {
                continue;
            }
            budgetEntryHashMap.put(getKeyForCategories(nestedCategory.getCategoryId()), budgetEntry);
        }
        cursor.close();

        return budgetEntryHashMap;
    }

    public boolean deleteForYear(long budgetYearId) {
        if (budgetYearId == Constants.NOT_SET) return false;

        WhereStatementGenerator where = new WhereStatementGenerator();
        where.addStatement(BudgetEntry.BUDGETYEARID, "=", budgetYearId);

        return getContext().getContentResolver().delete( getUri(),
                where.getWhere(),
                null) > 0;

    }


    // custom func
    public boolean hasBudget(long yearId, long cateId) {
        return this.count(BudgetEntry.BUDGETYEARID + " = ? AND " + BudgetEntry.CATEGID  + " = ?"
                , new String[]{Long.toString(yearId), Long.toString(cateId)} ) > 0;
    }

    public BudgetEntry loadByYearIdAndCateID(long yearId, long cateId) {
        return first(getAllColumns(), BudgetEntry.BUDGETYEARID + " = ? AND " + BudgetEntry.CATEGID  + " = ?"
                , new String[]{Long.toString(yearId), Long.toString(cateId)}
                , null);
    }


    /**
     * @param categId Category to Analize
     * @param date Date to Analize
     * @return Return BudgetEntry for given category and date.
     *         If montky budget is present this has precedences
     */
    public BudgetEntry loadByDateAndCateID( MmxDate date, long categId) {
        BudgetRepository budgetRepository = new BudgetRepository(getContext());
        Budget budget =  budgetRepository.loadFromDate(date);

        if ( budget == null ) return null;

        return loadByYearIdAndCateID(budget.getId(), categId);
    }

}
