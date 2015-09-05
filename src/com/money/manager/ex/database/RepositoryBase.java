package com.money.manager.ex.database;

import android.content.Context;
import android.database.Cursor;

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
