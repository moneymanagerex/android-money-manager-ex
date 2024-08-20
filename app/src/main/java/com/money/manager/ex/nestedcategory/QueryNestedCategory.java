package com.money.manager.ex.nestedcategory;

import android.content.Context;
import android.database.Cursor;

import com.money.manager.ex.R;
import com.money.manager.ex.database.Dataset;
import com.money.manager.ex.database.DatasetType;
import com.money.manager.ex.utils.MmxFileUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Record from OneCategory query. Used for Neested Category lists
 * Source is QueryOneCategory.
 * Note: This data is readonly! Records can not be created or updated.
 */

public class QueryNestedCategory
        extends Dataset {
    // CATEGID, CATEGNAME, PARENTID, PARENTNAME, BASENAME
    public static final String ID = "_id";
    public static final String CATEGID = "CATEGID";
    public static final String CATEGNAME = "CATEGNAME";
    public static final String PARENTID = "PARENTID";
    public static final String PARENTNAME = "PARENTNAME";
    public static final String BASENAME = "BASENAME";
    public static final String FULLCATID = "FULLCATID";
    public static final String ACTIVE = "ACTIVE";
    public static final String LEVEL = "LEVEL";

    private Context mContext;

    public QueryNestedCategory(Context context) {
        super(MmxFileUtils.getRawAsString(context, R.raw.query_onecategory), DatasetType.QUERY, QueryNestedCategory.class.getSimpleName());
        mContext = context;
    }

    @Override
    public String[] getAllColumns() {
        return new String[]{CATEGID, CATEGNAME, PARENTID, PARENTNAME, BASENAME, FULLCATID, ACTIVE, LEVEL};
    }


    public Cursor getCursor() {
        return getCursor(null, null, null, null);
    }

    public Cursor getCursor(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return mContext.getContentResolver().query(getUri(), projection, selection, selectionArgs, sortOrder);
    }

    public NestedCategoryEntity getOneCategoryEntity(Integer categoryId) {
        NestedCategoryEntity entity = null;
        Cursor cursor = getCursor(null, QueryNestedCategory.CATEGID + " = " + categoryId.toString(), null, null);
        if (cursor.moveToFirst()) {
            entity = new NestedCategoryEntity();
            entity.loadFromCursor(cursor);
        }
        cursor.close();
        return entity;
    }

    public List<NestedCategoryEntity> getNestedCategoryEntities(String filter, String sort) {
        List<NestedCategoryEntity> categories = new ArrayList<>();
        Cursor cursor = getCursor(null, filter, null, sort);
        if (cursor.moveToFirst()) {
            do {
                NestedCategoryEntity entity = new NestedCategoryEntity();
                entity.loadFromCursor(cursor);
                categories.add(entity);
            } while (cursor.moveToNext());
        }
        return categories;
    }

    public List<NestedCategoryEntity> getNestedCategoryEntities(String filter) {
        return  getNestedCategoryEntities( filter, CATEGNAME);
    }

    public List<NestedCategoryEntity> getChildrenNestedCategoryEntities(Integer categoryId ){
        return getNestedCategoryEntities( FULLCATID +
                        " LIKE '%:"+Integer.toString(categoryId)+":%'");
    }

}
