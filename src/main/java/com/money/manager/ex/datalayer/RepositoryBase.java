package com.money.manager.ex.datalayer;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.money.manager.ex.Constants;
import com.money.manager.ex.database.Dataset;
import com.money.manager.ex.database.DatasetType;
import com.money.manager.ex.domainmodel.EntityBase;

/**
 * Contains common code for repositories.
 * Created by Alen on 5/09/2015.
 */
public class RepositoryBase
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

    public int add(EntityBase entity) {
        return insert(entity.contentValues);
    }

    protected int insert(ContentValues values) {
        Uri insertUri = context.getContentResolver().insert(this.getUri(),
                values);
        if (insertUri == null) return Constants.NOT_SET;

        long id = ContentUris.parseId(insertUri);

        return (int) id;
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

    protected Cursor openCursor(String[] projection, String selection, String[] args) {
        Cursor cursor = context.getContentResolver().query(getUri(),
                projection,
                selection,
                args,
                null);
        return cursor;
    }

    protected Cursor openCursor(String[] projection, String selection, String[] args, String sort) {
        Cursor cursor = context.getContentResolver().query(getUri(),
                projection,
                selection,
                args,
                sort);
        return cursor;
    }

}
