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

package com.money.manager.ex.servicelayer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.datalayer.InfoRepositorySql;
import com.money.manager.ex.datalayer.Select;
import com.money.manager.ex.domainmodel.Info;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * Access and manipulation of the info in the Info Table
 */
public class InfoService
        extends ServiceBase {

    @Inject
    public InfoRepositorySql repository;

    public InfoService(final Context context) {
        super(context);

        MmexApplication.getApp().iocComponent.inject(this);
    }

    public long insertRaw(final SQLiteDatabase db, final String key, final Integer value) {
        final ContentValues values = new ContentValues();

        values.put(Info.INFONAME, key);
        values.put(Info.INFOVALUE, value);

        return db.insert(InfoRepositorySql.TABLE_NAME, null, values);
    }

    public long insertRaw(final SQLiteDatabase db, final String key, final String value) {
        final ContentValues values = new ContentValues();

        values.put(Info.INFONAME, key);
        values.put(Info.INFOVALUE, value);

        return db.insert(InfoRepositorySql.TABLE_NAME, null, values);
    }

    /**
     * Update the values via direct access to the database.
     *
     * @param db       Database to use
     * @param recordId Id of the info record. Required for the update statement.
     * @param key      Info Name
     * @param value    Info Value
     * @return the number of rows affected
     */
    public long updateRaw(final SQLiteDatabase db, final int recordId, final String key, final Integer value) {
        final ContentValues values = new ContentValues();
        values.put(Info.INFONAME, key);
        values.put(Info.INFOVALUE, value);

        return db.update(InfoRepositorySql.TABLE_NAME,
                values,
                Info.INFOID + "=?",
                new String[]{Integer.toString(recordId)}
        );
    }

    public long updateRaw(final SQLiteDatabase db, final String key, final String value) {
        final ContentValues values = new ContentValues();
        values.put(Info.INFONAME, key);
        values.put(Info.INFOVALUE, value);

        return db.update(InfoRepositorySql.TABLE_NAME, values,
                Info.INFONAME + "=?",
                new String[]{key});
    }

    /**
     * Retrieve value of info
     *
     * @param info to be retrieve
     * @return value
     */
    public String getInfoValue(final String info) {
        final Cursor cursor;
        String ret = null;

        try {
            final Select query = new Select()
                    .from(InfoRepositorySql.TABLE_NAME)
                    .where(Info.INFONAME + "=?", info);
            cursor = repository.query(query);
            if (null == cursor) return null;

            if (cursor.moveToFirst()) {
                ret = cursor.getString(cursor.getColumnIndex(Info.INFOVALUE));
            }
            cursor.close();
        } catch (final Exception e) {
            Timber.e(e, "retrieving info value: %s", info);
        }

        return ret;
    }

    /**
     * Update value of info.
     *
     * @param key   to update
     * @param value value to be used
     * @return true if update success otherwise false
     */
    public boolean setInfoValue(final String key, final String value) {
        boolean result = false;
        // check if info exists
        final boolean exists = (null != getInfoValue(key));

        final Info entity = Info.create(key, value);

        try {
            if (exists) {
                result = repository.update(entity);
            } else {
                final long id = repository.insert(entity);
                result = 0 < id;
            }
        } catch (final Exception e) {
            Timber.e(e, "writing info value");
        }

        return result;
    }
}
