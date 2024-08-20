package com.money.manager.ex.nestedcategory;

import android.content.Context;
import android.database.Cursor;

import timber.log.Timber;

public class NestedCategoryTest {

    public void main(Context c) {
        QueryNestedCategory queryNestedCategory = new QueryNestedCategory(c);
        Cursor cursor = queryNestedCategory.getCursor();
        while (cursor.moveToNext()) {
            NestedCategoryEntity entity = new NestedCategoryEntity();
            entity.loadFromCursor(cursor);
            Timber.d("Category: %s", entity.getCategoryName());
        }
        cursor.close();

    }

}
