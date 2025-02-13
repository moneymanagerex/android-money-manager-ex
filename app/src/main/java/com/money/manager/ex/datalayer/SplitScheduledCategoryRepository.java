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

import com.money.manager.ex.database.DatasetType;
import com.money.manager.ex.database.ISplitTransaction;
import com.money.manager.ex.database.WhereStatementGenerator;
import com.money.manager.ex.domainmodel.SplitRecurringCategory;
import com.money.manager.ex.domainmodel.Taglink;

import java.util.ArrayList;

/**
 * Repository for Recurring Split Categories (TableBudgetSplitTransactions).
 */
public class SplitScheduledCategoryRepository
    extends RepositoryBase <SplitRecurringCategory>
    implements IRepository {

    private static final String TABLE_NAME = SplitRecurringCategory.TABLE_NAME;
    private static final String ID_COLUMN = SplitRecurringCategory.SPLITTRANSID;
    private static final String NAME_COLUMN = "";

    public SplitScheduledCategoryRepository(Context context) {
        super(context, TABLE_NAME, DatasetType.TABLE, "budgetsplittransactions", ID_COLUMN, NAME_COLUMN);
    }

    @Override
    protected SplitRecurringCategory createEntity() {
        return new SplitRecurringCategory();
    }

    @Override
    public String[] getAllColumns() {
        return new String[] { ID_COLUMN + " AS _id",
            SplitRecurringCategory.SPLITTRANSID,
            SplitRecurringCategory.TRANSID,
            SplitRecurringCategory.CATEGID,
            SplitRecurringCategory.SUBCATEGID,
            SplitRecurringCategory.SPLITTRANSAMOUNT,
            SplitRecurringCategory.NOTES };
    }

    /**
     * Loads split transactions for the given transaction id.
     * @param transId Id of the main transaction for which to load the splits.
     * @return list of split categories for the given transaction.
     */
    public ArrayList<ISplitTransaction> loadSplitCategoriesFor(long transId) {
        Cursor curSplit = getContext().getContentResolver().query(getUri(), null,
            SplitRecurringCategory.TRANSID + "=" + transId,
            null,
            SplitRecurringCategory.SPLITTRANSID);
        if (curSplit == null) return null;

        ArrayList<ISplitTransaction> listSplitTrans = new ArrayList<>();

        TaglinkRepository taglinkRepo = new TaglinkRepository(getContext());

        while (curSplit.moveToNext()) {
            SplitRecurringCategory splitRecurringCategory = new SplitRecurringCategory();
            splitRecurringCategory.loadFromCursor(curSplit);

            splitRecurringCategory.setTags(
                    // array taglinks
                    taglinkRepo.loadTaglinksFor(splitRecurringCategory.getId(), Taglink.REFTYPE_RECURRING_TRANSACTION_SPLIT)
            );

            listSplitTrans.add(splitRecurringCategory);
        }
        curSplit.close();

        return listSplitTrans;
    }

    public boolean insert(SplitRecurringCategory item) {
        // Remove any existing id value.
        item.contentValues.remove(SplitRecurringCategory.SPLITTRANSID);

        long id = this.add(item);
        item.setId(id);

        return id > 0;
    }

    public boolean update(SplitRecurringCategory entity) {
        WhereStatementGenerator where = new WhereStatementGenerator();
        where.addStatement(SplitRecurringCategory.SPLITTRANSID, "=", entity.getId());

        return update(entity, where.getWhere());
    }

    public boolean delete(ISplitTransaction entity) {
        return delete(entity.getId());
    }

    public boolean delete(IEntity entity) {
        return delete((ISplitTransaction) entity);
    }
}
