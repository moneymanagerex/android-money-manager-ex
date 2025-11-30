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

import static android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE;

import android.content.ContentValues;
import android.database.Cursor;

import com.money.manager.ex.domainmodel.EntityBase;
import com.squareup.sqlbrite3.BriteDatabase;

import timber.log.Timber;

/**
 * Repository base for SQLite-based data access.
 * T is the entity type (class).
 */

abstract class SqlRepositoryBase<T extends EntityBase> {
    SqlRepositoryBase(String tableName, BriteDatabase db) {
        this.tableName = tableName;
        this.database = db;
    }

    public BriteDatabase database;
    public String tableName;

    public long insert(ContentValues values) {
        return database.insert(tableName, CONFLICT_REPLACE, values);
    }

    public long delete(String where, String... whereArgs) {
        long result = database.delete(tableName, where, whereArgs);

        return result;
    }

    public boolean exists(Select query) {
        Cursor c = database.query(query.toString(), query.selectionArgs);
        if (c == null) return false;

        boolean result = c.getCount() > 0;

        c.close();

        return result;
    }

    public T first(Class<T> resultType, String[] projection, String selection, String[] args, String sort) {
        T entity = null;

        String sql = new Select(projection)
            .from(tableName)
            .where(selection)
            .orderBy(sort)
            .toString();

        try {
            Cursor c = database.query(sql, args);
            if (c == null) return null;

            if (c.moveToNext()) {
                try {
                    entity = resultType.newInstance();
                    //resultType.cast(entity);
                    entity.loadFromCursor(c);
                } catch (Exception e) {
                    Timber.e(e, "creating %s", resultType.getName());
                }
            }
            c.close();
        } catch (Exception ex) {
            Timber.e(ex, "fetching first record");
        }

        return entity;
    }

    public Cursor query(Select query) {
        return database.query(query.toString(), query.selectionArgs);
    }

    protected boolean update(EntityBase entity, String where, String... selectionArgs) {
        boolean result = false;

        ContentValues values = entity.contentValues;
        // remove "_id" from the values.
        values.remove("_id");

        long updateResult = database.update(tableName,
                CONFLICT_REPLACE,
                values,
                where,
                selectionArgs
        );

        if (updateResult != 0) {
            result = true;
        } else {
            Timber.w("update failed, %s, values: %s", tableName, entity.contentValues);
        }

        return result;
    }
}
