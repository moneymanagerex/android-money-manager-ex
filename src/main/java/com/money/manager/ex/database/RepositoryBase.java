package com.money.manager.ex.database;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

/**
 * Contains common code for repositories.
 * Created by Alen on 5/09/2015.
 */
public class RepositoryBase
    extends Dataset {

    public RepositoryBase(Context context, String source, DatasetType type, String basePath) {
        super(source, type, basePath);

        mContext = context.getApplicationContext();
    }

    protected Context mContext;

    protected int insert(ContentValues values) {
        Uri insertUri = mContext.getContentResolver().insert(this.getUri(),
                values);
        long id = ContentUris.parseId(insertUri);

        return (int) id;
    }

    protected boolean update(int id, ContentValues values, String where) {
        boolean result = false;

        int updateResult = mContext.getContentResolver().update(this.getUri(),
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

    protected Cursor openCursor(String[] projection, String selection, String[] args) {
        Cursor cursor = mContext.getContentResolver().query(
                getUri(),
                projection,
                selection,
                args,
                null);
        return cursor;
    }

}
