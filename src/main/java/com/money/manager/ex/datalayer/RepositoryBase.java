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

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;

import com.money.manager.ex.Constants;
import com.money.manager.ex.MmexContentProvider;
import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.database.Dataset;
import com.money.manager.ex.database.DatasetType;
import com.money.manager.ex.domainmodel.AssetClass;
import com.money.manager.ex.domainmodel.EntityBase;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains common code for repositories.
 */
public abstract class RepositoryBase<T extends EntityBase>
    extends Dataset {

    public RepositoryBase(Context context, String source, DatasetType type, String basePath) {
        super(source, type, basePath);

        this.context = context.getApplicationContext();
//        this.idColumn = idColumn;
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
        Cursor cursor = getContext().getContentResolver().query(getUri(),
            projection,
            selection,
            args,
            sort);
        return cursor;
    }

    // This won't work because of the bug in reading Double values.
//    public ContentValues[] query(String[] projection, String selection, String[] args, String sort) {
//        Cursor c = openCursor(projection, selection, args, sort);
//        if (c == null) return null;
//
//        ContentValues[] result = new ContentValues[c.getCount()];
//
//        while (c.moveToNext()) {
//            DatabaseUtils.
//        }
//        c.close();
//
//        return result;
//    }

    public int add(EntityBase entity) {
        return insert(entity.contentValues);
    }

    // Protected

//    protected List<ContentValues> query(String selection) {
//        return query(null, selection, null, null);
//    }
//
//    protected List<ContentValues> query(String[] projection, String selection, String[] args) {
//        return query(projection, selection, args, null);
//    }
//
//    protected List<ContentValues> query(String selection, String[] args, String sort) {
//        return query(null, selection, args, sort);
//    }

    /**
     * Warning!
     * This works *only* if there are no numeric/double values. Otherwise, these will be cut to
     * the first 6 digits.
     */
    protected ContentValues[] query(String[] projection, String selection, String[] args, String sort) {
        Cursor c = openCursor(projection, selection, args, sort);
        if (c == null) return null;

        List<ContentValues> result = new ArrayList<>();

        while (c.moveToNext()) {
            ContentValues values = new ContentValues();
            DatabaseUtils.cursorRowToContentValues(c, values);

            result.add(values);
        }
        c.close();

        ContentValues[] array = new ContentValues[result.size()];
        return result.toArray(array);
    }

    protected int insert(ContentValues values) {
        Uri insertUri = getContext().getContentResolver().insert(this.getUri(),
            values);
        if (insertUri == null) return Constants.NOT_SET;

        long id = ContentUris.parseId(insertUri);

        return (int) id;
    }

    protected int bulkInsert(ContentValues[] items) {
        return getContext().getContentResolver().bulkInsert(this.getUri(), items);
    }

    protected boolean update(int id, ContentValues values, String where) {
        boolean result = false;

        int updateResult = getContext().getContentResolver().update(this.getUri(),
            values,
            where,
            null
        );

        if (updateResult != 0) {
            result = true;
        } else {
            Log.w(this.getClass().getSimpleName(), "update failed, " + this.getUri() + ", id:" + id);
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
                .applyBatch(MmexContentProvider.getAuthority(), operations);
        } catch (RemoteException | OperationApplicationException e) {
            ExceptionHandler handler = new ExceptionHandler(context, this);
            handler.handle(e, "bulk updating");
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
                .applyBatch(MmexContentProvider.getAuthority(), operations);
        } catch (RemoteException | OperationApplicationException e) {
            ExceptionHandler handler = new ExceptionHandler(context, this);
            handler.handle(e, "bulk updating");
        }
        return results;
    }

    protected ContentValues single(String selection, String[] selectionArgs) {
        Cursor c = openCursor(getAllColumns(),
            selection,
            selectionArgs);
        if (c == null) return null;

        if (!c.moveToNext()) return null;

        ContentValues contentValues = new ContentValues();
        DatabaseUtils.cursorRowToContentValues(c, contentValues);

        c.close();

        return contentValues;
    }
}
