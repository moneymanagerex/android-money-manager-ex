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

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDiskIOException;
import android.net.Uri;
import android.os.RemoteException;

import com.money.manager.ex.Constants;
import com.money.manager.ex.MmxContentProvider;
import com.money.manager.ex.database.Dataset;
import com.money.manager.ex.database.DatasetType;
import com.money.manager.ex.domainmodel.AssetClass;
import com.money.manager.ex.domainmodel.EntityBase;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Contains common code for repositories.
 */
public abstract class RepositoryBase<T extends EntityBase>
    extends Dataset {

    public RepositoryBase(Context context, String source, DatasetType type, String basePath) {
        super(source, type, basePath);

        this.context = context.getApplicationContext();
    }

    private Context context;

    public int count(String selection, String[] args) {
        Cursor c = openCursor(null, selection, args);
        if (c == null) return Constants.NOT_SET;

        int result = c.getCount();
        c.close();

        return result;
    }

    public Context getContext() {
        return this.context;
    }

    public Cursor openCursor(String[] projection, String selection, String[] args) {
        return openCursor(projection, selection, args, null);
    }

    public Cursor openCursor(String[] projection, String selection, String[] args, String sort) {
        try {
            Cursor cursor = getContext().getContentResolver().query(getUri(),
                projection,
                selection,
                args,
                sort);
            return cursor;
        } catch (SQLiteDiskIOException ex) {
            Timber.e(ex, "querying database");
            return null;
        }
    }

    public int add(EntityBase entity) {
        return insert(entity.contentValues);
    }

    /**
     * Fetch only the first result
     * @param resultType
     * @param projection
     * @param selection
     * @param args
     * @param sort Sort order to apply to the query results from which the first will be returned.
     * @return
     */
    public T first(Class<T> resultType, String[] projection, String selection, String[] args, String sort) {
        T entity = null;

        try {
            Cursor c = openCursor(projection, selection, args, sort);
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

    public List<T> query(Class<T> resultType, Select query) {
        // String[] projection, String selection, String[] args, String sort
        Cursor c = openCursor(query.projection, query.selection, query.selectionArgs, query.sort);
        if (c == null) return null;

        List<T> results = new ArrayList<>();
        //T entity = null;

        while (c.moveToNext()) {
            try {
                T entity = resultType.newInstance();
                entity.loadFromCursor(c);

                results.add(entity);
            } catch (Exception e) {
                Timber.e(e, "creating %s", resultType.getName());
            }
        }
        c.close();

        return results;
    }

    // Protected

    protected int bulkInsert(ContentValues[] items) {
        return getContext().getContentResolver().bulkInsert(this.getUri(), items);
    }

    /**
     * Generic insert method.
     */
    protected int insert(ContentValues values) {
        // sanitize
        values.remove("_id");

        Uri insertUri = getContext().getContentResolver().insert(this.getUri(), values);
        if (insertUri == null) return Constants.NOT_SET;

        long id = ContentUris.parseId(insertUri);

        return (int) id;
    }

    protected List<T> query(Class<T> resultType, String selection) {
        Select query = new Select().where(selection);
        return query(resultType, query);
    }

    /**
     * Generic update method.
     * @param entity    Entity values to store.
     * @param where     Condition for entity selection.
     * @return  Boolean indicating whether the operation was successful.
     */
    protected boolean update(EntityBase entity, String where) {
        return update(entity, where, null);
    }

    protected boolean update(EntityBase entity, String where, String[] selectionArgs) {
        boolean result = false;

        ContentValues values = entity.contentValues;
        // remove "_id" from the values.
        values.remove("_id");

        int updateResult = getContext().getContentResolver().update(this.getUri(),
                values,
                where,
                selectionArgs
        );

        if (updateResult != 0) {
            result = true;
        } else {
            Timber.w("update failed, %s, values: %s", this.getUri(), entity.contentValues);
        }

        return  result;
    }

    /**
     * Warning: this works only with Asset Class entities!
     * Ref:
     * http://www.grokkingandroid.com/better-performance-with-contentprovideroperation/
     * http://www.grokkingandroid.com/android-tutorial-using-content-providers/
     * @param entities array of entities to update in a transaction
     * @return results of the bulk update
     */
    protected ContentProviderResult[] bulkUpdate(EntityBase[] entities) {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();

        for (EntityBase entity : entities) {
            AssetClass assetClass = (AssetClass) entity;

            operations.add(ContentProviderOperation.newUpdate(this.getUri())
                .withValues(entity.contentValues)
                .withSelection(AssetClass.ID + "=?", new String[] {Integer.toString(assetClass.getId())})
                .build());
        }

        ContentProviderResult[] results = null;
        try {
            results = getContext().getContentResolver()
                .applyBatch(MmxContentProvider.getAuthority(), operations);
        } catch (RemoteException | OperationApplicationException e) {
            Timber.e(e, "bulk updating");
        }
        return results;
    }

    protected int delete(String where, String[] args) {
        int result = getContext().getContentResolver().delete(this.getUri(),
            where,
            args
        );
        return result;
    }

    protected ContentProviderResult[] bulkDelete(List<Integer> ids) {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();

        for (int id : ids) {
            operations.add(ContentProviderOperation.newDelete(this.getUri())
//                .withValues(entity.contentValues)
                .withSelection(AssetClass.ID + "=?", new String[]{Integer.toString(id)})
                .build());
        }

        ContentProviderResult[] results = null;
        try {
            results = getContext().getContentResolver()
                .applyBatch(MmxContentProvider.getAuthority(), operations);
        } catch (RemoteException | OperationApplicationException e) {
            Timber.e(e, "bulk updating");
        }
        return results;
    }

}
