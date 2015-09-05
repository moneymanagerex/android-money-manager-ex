package com.money.manager.ex.database;

import android.content.Context;

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


}
