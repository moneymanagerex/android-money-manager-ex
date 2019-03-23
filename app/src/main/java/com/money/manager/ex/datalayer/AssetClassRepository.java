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

import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.money.manager.ex.database.DatasetType;
import com.money.manager.ex.database.WhereStatementGenerator;
import com.money.manager.ex.domainmodel.AssetClass;
import com.money.manager.ex.domainmodel.EntityBase;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository for Asset Classes.
 */
public class AssetClassRepository
    extends RepositoryBase {

    public AssetClassRepository(Context context) {
        super(context, "assetclass_v1", DatasetType.TABLE, "assetclass");

    }

    @Override
    public String[] getAllColumns() {
        return new String[] {AssetClass.ID + " AS _id",
            AssetClass.ID,
            AssetClass.PARENTID,
            AssetClass.NAME,
            AssetClass.ALLOCATION,
            AssetClass.SORTORDER
        };
    }

    public AssetClass load(int id) {
        WhereStatementGenerator where = new WhereStatementGenerator();
        where.addStatement(AssetClass.ID, "=", id);

        return first(where.getWhere());
    }

    /**
     * Loads ids for all child records.
     * @param id Id of the parent item.
     * @return List of ids of the child asset classes.
     */
    public List<Integer> loadAllChildrenIds(int id) {
        WhereStatementGenerator where = new WhereStatementGenerator();
        where.addStatement(AssetClass.PARENTID, "=", id);

        String[] fields = new String[] { AssetClass.ID };

        Select query = new Select(fields)
                .where(where.getWhere());
        List<AssetClass> children = query(AssetClass.class, query);

        List<Integer> result = new ArrayList<>();
        for (AssetClass item : children) {
            result.add(item.getId());
        }

        return result;
    }

    public AssetClass first(String selection) {
        return (AssetClass) first(AssetClass.class, null, selection, null, null);
    }

    public boolean insert(AssetClass value) {
        int id = this.insert(value.contentValues);
        value.setId(id);

        return id > 0;
    }

    public boolean bulkInsert(List<AssetClass> entities) {
//        List<ContentValues> contentValues = Queryable.from(entities)
//            .map(new Converter<AssetClass, ContentValues>() {
//                @Override
//                public ContentValues convert(AssetClass element) {
//                    return element.contentValues;
//                }
//            })
//            .toList();

        List<ContentValues> contentValues = new ArrayList<>();
        for (AssetClass entity : entities) {
            contentValues.add(entity.contentValues);
        }

        ContentValues[] values = new ContentValues[entities.size()];

        contentValues.toArray(values);

        int records = bulkInsert(values);
        return records == entities.size();
    }

    public boolean update(AssetClass value) {
        int id = value.getId();

        WhereStatementGenerator generator = new WhereStatementGenerator();
        String where = generator.getStatement(AssetClass.ID, "=", id);

        return update(value, where);
    }

    public boolean bulkUpdate(List<AssetClass> entities) {
        EntityBase[] values = new EntityBase[entities.size()];
        entities.toArray(values);

        ContentProviderResult[] results = bulkUpdate(values);
        return results.length == entities.size();
    }

    public boolean delete(int id) {
        int result = delete(AssetClass.ID + "=?", new String[]{Integer.toString(id)});
        return result > 0;
    }

    public boolean deleteAll(List<Integer> ids) {
        if (ids.size() == 0) return true;

        ContentProviderResult[] results = bulkDelete(ids);

        for (ContentProviderResult result : results) {
            Log.d("test", result.toString());
        }

        return true;
    }
}
