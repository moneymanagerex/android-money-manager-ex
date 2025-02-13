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
import com.money.manager.ex.domainmodel.SplitCategory;

import java.util.ArrayList;

/**
 * Repository for Split Categories (TableSplitTransaction).
 */
public class SplitCategoryRepository
    extends RepositoryBase<SplitCategory>
    implements IRepository {

    private static final String TABLE_NAME = SplitCategory.TABLE_NAME;
    private static final String ID_COLUMN = SplitCategory.SPLITTRANSID;
    private static final String NAME_COLUMN = "";

    public SplitCategoryRepository(Context context) {
        super(context, TABLE_NAME, DatasetType.TABLE, "splittransaction", ID_COLUMN, NAME_COLUMN);
    }

    @Override
    protected SplitCategory createEntity() {
        return new SplitCategory();
    }

    @Override
    public String[] getAllColumns() {
        return new String[] {ID_COLUMN + " AS _id",
            SplitCategory.SPLITTRANSID,
            SplitCategory.TRANSID,
            SplitCategory.CATEGID,
			
            SplitCategory.SPLITTRANSAMOUNT,
            SplitCategory.NOTES};
    }

    /**
     * Loads split transactions for the given transaction id.
     * @param transId Id of the main transaction for which to load the splits.
     * @return list of split categories for the given transaction.
     */
    public ArrayList<ISplitTransaction> loadSplitCategoriesFor(long transId) {
        Cursor curSplit = getContext().getContentResolver().query(getUri(), null,
            SplitCategory.TRANSID + "=" + transId,
            null,
            SplitCategory.SPLITTRANSID);
        if (curSplit == null) return null;

        ArrayList<ISplitTransaction> listSplitTrans = new ArrayList<>();

        TaglinkRepository taglinkRepository = new TaglinkRepository(getContext());
        while (curSplit.moveToNext()) {
            SplitCategory splitCategory = new SplitCategory();
            splitCategory.loadFromCursor(curSplit);

            // load tags from table
            splitCategory.setTags(taglinkRepository.loadTaglinksFor(splitCategory.getId(), splitCategory.getTransactionModel()));

            listSplitTrans.add(splitCategory);
        }
        curSplit.close();

        return listSplitTrans;
    }

    public void saveAllFor(Long transId, ArrayList<ISplitTransaction> split) {
        SplitCategoryRepository splitCategoryRepository = new SplitCategoryRepository(getContext());
        TaglinkRepository taglinkRepository = new TaglinkRepository(getContext());
        if (split == null) return;
        for (ISplitTransaction item : split) {
            SplitCategory scat = (SplitCategory) item;
            scat.setTransId(transId);
            splitCategoryRepository.insertOrUpdate(scat);
            taglinkRepository.saveAllFor(scat.getTransactionModel(), scat.getId(), scat.getTags());
        }
    }


    public boolean insert(SplitCategory item) {
        // Remove any existing id value.
        item.contentValues.remove(SplitCategory.SPLITTRANSID);

        long id = this.add(item);
        item.setId(id);

        return id > 0;
    }

    public boolean update(SplitCategory entity) {
        WhereStatementGenerator where = new WhereStatementGenerator();
        where.addStatement(SplitCategory.SPLITTRANSID, "=", entity.getId());

        return update(entity, where.getWhere());
    }

    public boolean insertOrUpdate(SplitCategory entity) {
        if (entity.getId() > 0) {
            return update(entity);
        } else {
            return insert(entity);
        }
    }

    public boolean delete(ISplitTransaction entity) {
        long deleted = super.delete(SplitCategory.SPLITTRANSID + "=?",
                new String[]{ Long.toString(entity.getId()) });
        return deleted == 1;
    }

    public boolean delete(IEntity entity) {
        return delete((ISplitTransaction) entity);
    }

}
