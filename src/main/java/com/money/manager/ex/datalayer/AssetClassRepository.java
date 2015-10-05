/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
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
import android.database.Cursor;

import com.innahema.collections.query.functions.Converter;
import com.innahema.collections.query.queriables.Queryable;
import com.money.manager.ex.database.DatasetType;
import com.money.manager.ex.database.WhereStatementGenerator;
import com.money.manager.ex.domainmodel.AssetClass;
import com.money.manager.ex.domainmodel.EntityBase;

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
        return new String[] {AssetClass.ID + " AS _id", AssetClass.ID, AssetClass.NAME,
            AssetClass.ALLOCATION };
    }

    public AssetClass load(int id) {
        WhereStatementGenerator where = new WhereStatementGenerator();
        where.addStatement(AssetClass.ID, "=", id);

        return query(where.getWhere());
    }

    public AssetClass query(String selection) {
        return query(getAllColumns(), selection, null);
    }

    public AssetClass query(String[] projection, String selection, String[] args) {
        Cursor c = openCursor(projection, selection, args);

        if (c == null) return null;

        AssetClass account = null;

        if (c.moveToNext()) {
            account = new AssetClass();
            account.loadFromCursor(c);
        }

        c.close();

        return account;
    }

    public boolean insert(AssetClass value) {
        int id = this.insert(value.contentValues);
        value.setId(id);

        return id > 0;
    }

    public boolean bulkInsert(List<AssetClass> entities) {
        List<ContentValues> contentValues = Queryable.from(entities)
            .map(new Converter<AssetClass, ContentValues>() {
                @Override
                public ContentValues convert(AssetClass element) {
                    return element.contentValues;
                }
            })
            .toList();

        ContentValues[] values = new ContentValues[entities.size()];

//        int i = 0;
//
//        for (AssetClass entity : entities) {
//            values[i] = entity.contentValues;
//            i++;
//        }

        contentValues.toArray(values);

        int records = bulkInsert(values);
        return records == entities.size();
    }

    public boolean update(AssetClass value) {
        int id = value.getId();

        WhereStatementGenerator generator = new WhereStatementGenerator();
        String where = generator.getStatement(AssetClass.ID, "=", id);

        return update(id, value.contentValues, where);
    }

    public boolean bulkUpdate(List<AssetClass> entities) {
        EntityBase[] values = new EntityBase[entities.size()];
        entities.toArray(values);

        ContentProviderResult[] results = bulkUpdate(values);
        return results.length == entities.size();
    }

    public boolean delete(int id) {
        int result = delete(AssetClass.ID + "=?", new String[] { Integer.toString(id)});
        return result > 0;
    }
}
