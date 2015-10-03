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

import android.content.Context;
import android.database.Cursor;

import com.money.manager.ex.database.DatasetType;
import com.money.manager.ex.database.WhereStatementGenerator;
import com.money.manager.ex.domainmodel.AssetClass;

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
        return query(null, selection, null);
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
        return this.insert(value.contentValues) > 0;
    }

    public boolean update(AssetClass value) {
        int id = value.getId();

        WhereStatementGenerator generator = new WhereStatementGenerator();
        String where = generator.getStatement(AssetClass.ID, "=", id);

        return update(id, value.contentValues, where);
    }

    public boolean delete(int id) {
        int result = delete(AssetClass.ID + "=?", new String[] { Integer.toString(id)});
        return result > 0;
    }
}
