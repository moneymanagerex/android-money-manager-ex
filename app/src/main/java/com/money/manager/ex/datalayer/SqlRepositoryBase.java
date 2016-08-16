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

import android.content.ContentValues;
import android.database.Cursor;

import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.log.ExceptionHandler;
import com.money.manager.ex.domainmodel.EntityBase;
import com.money.manager.ex.sync.SyncManager;
import com.squareup.sqlbrite.BriteDatabase;

import javax.inject.Inject;

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
    String tableName;

    public long insert(ContentValues values) {
        return database.insert(tableName, values);
    }

    public int delete(String where, String... whereArgs) {
        int result = database.delete(tableName, where, whereArgs);

        notifySync();

        return result;
    }

    public T first(Class<T> resultType, String[] projection, String selection, String[] args, String sort) {
        T entity = null;

        String sql = new Query()
                .select(projection)
                .from(tableName)
                .where(selection)
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
                    Timber.e(e, "creating " + resultType.getName());
                }
            }
            c.close();
        } catch (Exception ex) {
            Timber.e(ex, "fetching first record");
        }

        return entity;
    }

    protected boolean update(EntityBase entity, String where, String... selectionArgs) {
        boolean result = false;

        ContentValues values = entity.contentValues;
        // remove "_id" from the values.
        values.remove("_id");

        int updateResult = database.update(tableName,
                values,
                where,
                selectionArgs
        );

        if (updateResult != 0) {
            notifySync();

            result = true;
        } else {
            Timber.w("update failed, %s, values: %s", tableName, entity.contentValues);
        }

        return  result;
    }

    /**
     * Notify sync engine about the database update.
     */
    private void notifySync() {
        new SyncManager(MoneyManagerApplication.getApp()).dataChanged();
    }
}
