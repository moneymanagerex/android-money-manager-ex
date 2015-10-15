package com.money.manager.ex.datalayer;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
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

import java.util.ArrayList;

/**
 * Contains common code for repositories.
 */
public abstract class RepositoryBase
    extends Dataset {

    public RepositoryBase(Context context, String source, DatasetType type, String basePath) {
        super(source, type, basePath);

        this.context = context.getApplicationContext();
    }

    protected Context context;

//    protected EntityBase get(int id) {
//        context.getContentResolver().query(this.getUri(),
//                )
//
//    }

    public int count(String selection, String[] args) {
        Cursor c = openCursor(null, selection, args);
        if (c == null) return Constants.NOT_SET;

        int result = c.getCount();
        c.close();

        return result;
    }

    public Cursor openCursor(String[] projection, String selection, String[] args) {
        return openCursor(projection, selection, args, null);
    }

    public Cursor openCursor(String[] projection, String selection, String[] args, String sort) {
        Cursor cursor = context.getContentResolver().query(getUri(),
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
//            DatabaseUtils
//        }
//        c.close();
//
//        return result;
//    }

    public int add(EntityBase entity) {
        return insert(entity.contentValues);
    }

//    /**
//     * Check if any records satisfy the condition.
//     * @param where Selection statement / where.
//     * @return A boolean indicating if there are any records that satisfy the condition.
//     */
//    protected boolean any(String where, String[] args) {
//        MmexOpenHelper helper = MmexOpenHelper.getInstance(context);
//        Cursor c = helper.getReadableDatabase().rawQuery(where, args);
//        if (c == null) return false;
//
//        boolean result = false;
//        c.moveToNext();
//        // todo: result =
//        DatabaseUtils.dumpCurrentRow(c);
//        c.close();
//        return result;
//    }

    protected int insert(ContentValues values) {
        Uri insertUri = context.getContentResolver().insert(this.getUri(),
            values);
        if (insertUri == null) return Constants.NOT_SET;

        long id = ContentUris.parseId(insertUri);

        return (int) id;
    }

    protected int bulkInsert(ContentValues[] items) {
        return context.getContentResolver().bulkInsert(this.getUri(), items);
    }

    protected boolean update(int id, ContentValues values, String where) {
        boolean result = false;

        int updateResult = context.getContentResolver().update(this.getUri(),
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
            results = context.getContentResolver()
                .applyBatch(MmexContentProvider.getAuthority(), operations);
        } catch (RemoteException | OperationApplicationException e) {
            ExceptionHandler handler = new ExceptionHandler(context, this);
            handler.handle(e, "bulk updating");
        }
        return results;
    }

    protected int delete(String where, String[] args) {
        int result = context.getContentResolver().delete(this.getUri(),
            where,
            args
        );
        return result;
    }
}
