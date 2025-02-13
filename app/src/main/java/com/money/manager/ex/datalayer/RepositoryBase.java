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
import com.money.manager.ex.domainmodel.EntityBase;
import com.money.manager.ex.utils.MmxDatabaseUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import timber.log.Timber;

/**
 * Contains common code for repositories.
 */
public abstract class RepositoryBase<T extends EntityBase>
    extends Dataset {

    private static final AtomicLong lastId = new AtomicLong(0);
    private final Context context;
    protected final String idColumn;

    public RepositoryBase(Context context, String source, DatasetType type, String basePath, String idColumn) {
        super(source, type, basePath);

        this.context = context.getApplicationContext();
        this.idColumn = idColumn;
    }

    public long count(String selection, String[] args) {
        Cursor c = openCursor(null, selection, args);
        if (c == null) return Constants.NOT_SET;

        long result = c.getCount();
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

    // CRUD - C
    public long add(EntityBase entity) {
        if (entity.getId() == null || entity.getId() == Constants.NOT_SET)
            entity.setId(newId());
        return insert(entity.contentValues);
    }

    // CRUD - R
    public T load(Long id) {
        if (id == null || id == Constants.NOT_SET) return null;

        return first(
                getAllColumns(),
                idColumn + "=?", MmxDatabaseUtils.getArgsForId(id),
                null);
    }

    public List<T> loadAll() {
        return query(new Select(getAllColumns()));
    }

    // CURD - U
    public boolean save(T entity) {
        Long id = entity.getId();
        if (id == null || id == Constants.NOT_SET)
            return add(entity) > 0; // upsert?
        return update(entity, idColumn + "=?", MmxDatabaseUtils.getArgsForId(id));
    }

    /**
     * Fetch only the first result
     *
     * @param projection
     * @param selection
     * @param args
     * @param sort       Sort order to apply to the query results from which the first will be returned.
     * @return
     */
    public T first(String[] projection, String selection, String[] args, String sort) {
        Cursor c = null;
        T entity = null;

        try {
            c = openCursor(projection, selection, args, sort);
            if (c != null && c.moveToNext()) {
                entity = createEntity();
                entity.loadFromCursor(c);
            }
        } catch (Exception e) {
            Timber.e(e, "Error fetching first record of %s", this.getSource());
        } finally {
            if (c != null) c.close();
        }

        return entity;
    }

    public List<T> query(Select query) {
        List<T> results = new ArrayList<>();
        Cursor c = null;

        try {
            c = openCursor(query.projection, query.selection, query.selectionArgs, query.sort);
            if (c != null) {
                while (c.moveToNext()) {
                    T entity = createEntity();
                    entity.loadFromCursor(c);
                    results.add(entity);
                }
            }
        } catch (Exception e) {
            Timber.e(e, "Error querying %s", getUri());
        } finally {
            if (c != null) c.close();
        }

        return results;
    }

    // Protected

    protected long bulkInsert(ContentValues[] items) {
        return getContext().getContentResolver().bulkInsert(this.getUri(), items);
    }

    long newId() {
        long now = System.currentTimeMillis() * 1_000;
        long id;

        do {
            long last = lastId.get();
            id = Math.max(now, last + 1);
        } while (!lastId.compareAndSet(lastId.get(), id));

        return id;
    }

    /**
     * Generic insert method.
     * Called only internally.
     *
     * @param values The content values to insert (without "_id" field).
     * @return The ID of the inserted record, or {@link Constants#NOT_SET} if the insertion fails.
     */
    private long insert(ContentValues values) {
        if (values.containsKey("_id")) {
            values.remove("_id");
        }

        Uri insertUri = getContext().getContentResolver().insert(this.getUri(), values);
        if (insertUri == null) {
            Timber.e("Insert failed for values: %s", values);
            return Constants.NOT_SET;
        }

        return ContentUris.parseId(insertUri);
    }

    protected List<T> query(String selection) {
        Select query = new Select().where(selection);
        return query(query);
    }

    /**
     * Generic update method.
     * @param entity    Entity values to store.
     * @param where     Condition for entity selection.
     * @return  Boolean indicating whether the operation was successful.
     */
    protected boolean update(EntityBase entity, String where) {
        return update(entity, where, new String[0]);
    }

    protected boolean update(EntityBase entity, String where, String[] selectionArgs) {
        ContentValues values = entity.contentValues;
        values.remove("_id");

        int rowsAffected = getContext().getContentResolver().update(this.getUri(), values, where, selectionArgs);
        if (rowsAffected > 0) {
            return true;
        } else {
            Timber.w("Update failed, URI: %s, Values: %s", this.getUri(), values);
            return false;
        }
    }

    public boolean delete(Long id) {
        if (id == Constants.NOT_SET) return false;

        long result = delete(idColumn + "=?", MmxDatabaseUtils.getArgsForId(id));
        return result > 0;
    }

    protected long delete(String where, String[] args) {
        long result = getContext().getContentResolver().delete(this.getUri(),
            where,
            args
        );
        return result;
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
            ContentProviderOperation op = ContentProviderOperation.newUpdate(this.getUri())
                    .withValues(entity.contentValues)
                    .build();
            operations.add(op);
        }

        if (operations.isEmpty()) return null;

        try {
            return getContext().getContentResolver()
                    .applyBatch(MmxContentProvider.getAuthority(), operations);
        } catch (RemoteException | OperationApplicationException e) {
            Timber.e(e, "bulk updating");
            return null;
        }
    }

    protected ContentProviderResult[] bulkDelete(List<Integer> ids) {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();

        for (Integer id : ids) {
            ContentProviderOperation op = ContentProviderOperation.newDelete(this.getUri())
                    .withSelection("_id = ?", new String[]{String.valueOf(id)})
                    .build();
            operations.add(op);
        }

        if (operations.isEmpty()) return null;

        try {
            return getContext().getContentResolver()
                    .applyBatch(MmxContentProvider.getAuthority(), operations);
        } catch (RemoteException | OperationApplicationException e) {
            Timber.e(e, "bulk deleting");
            return null;
        }
    }

    protected abstract T createEntity();
}
