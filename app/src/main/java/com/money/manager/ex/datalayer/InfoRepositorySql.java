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

import android.database.Cursor;

import com.money.manager.ex.domainmodel.Info;
import com.squareup.sqlbrite.BriteDatabase;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Repository for InfoTable
 */
public class InfoRepositorySql
    extends SqlRepositoryBase<Info> {

    public static final String TABLE_NAME = "infotable_v1";

    @Inject
    public InfoRepositorySql(BriteDatabase db) {
        super(TABLE_NAME, db);
    }

//    @Override
//    public String[] getAllColumns() {
//        return new String[] {"INFOID AS _id", Info.INFOID, Info.INFONAME, Info.INFOVALUE};
//    }

    public List<Info> loadAll(String infoName) {
        Select sql = new Select()
            .from(TABLE_NAME)
            .where(Info.INFONAME + "=?", infoName);

        Cursor c = this.query(sql);
        if (c == null) return null;

        List<Info> results = new ArrayList<>();
        while (c.moveToNext()) {
            Info entity = new Info();
            entity.loadFromCursor(c);
            results.add(entity);
        }

        return results;
    }

    public int delete(long id) {
        String idString = String.valueOf(id);
        return this.delete(Info.INFOID + "=?", idString);
    }

    public long insert(Info value) {
        return insert(value.contentValues);
    }

    public boolean update(Info entity) {
        return update(entity, Info.INFONAME + "=?", entity.getName());
    }
}
