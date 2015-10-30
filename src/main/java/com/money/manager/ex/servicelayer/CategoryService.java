/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
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
package com.money.manager.ex.servicelayer;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.money.manager.ex.Constants;
import com.money.manager.ex.database.TableCategory;
import com.money.manager.ex.datalayer.AccountTransactionRepository;
import com.money.manager.ex.datalayer.CategoryRepository;
import com.money.manager.ex.datalayer.SubcategoryRepository;
import com.money.manager.ex.domainmodel.Category;
import com.money.manager.ex.domainmodel.Subcategory;

import java.util.ArrayList;
import java.util.List;

/**
 * Category
 */
public class CategoryService {

    public CategoryService(Context context) {
        mContext = context;
        mCategory = new TableCategory();
    }

    private Context mContext;
    private TableCategory mCategory;

    public int loadIdByName(String name) {
        int result = -1;

        if(TextUtils.isEmpty(name)) { return result; }

        String selection = Category.CATEGNAME + "=?";

        Cursor cursor = getContext().getContentResolver().query(
                mCategory.getUri(),
                new String[] { Category.CATEGID },
                selection,
                new String[] { name },
                null);

        if(cursor.moveToFirst()) {
            result = cursor.getInt(cursor.getColumnIndex(Category.CATEGID));
        }

        cursor.close();

        return result;
    }

    public int createNew(String name) {
        if (TextUtils.isEmpty(name)) return Constants.NOT_SET;

        name = name.trim();

        ContentValues values = new ContentValues();
        values.put(Category.CATEGNAME, name);

        Uri result = getContext().getContentResolver()
                .insert(mCategory.getUri(), values);
        long id = ContentUris.parseId(result);

        return ((int) id);
    }

    /**
     * Return a list of all categories
     */
    public List<TableCategory> getCategoryList() {
        List<TableCategory> listCategories = new ArrayList<>();

        Cursor cursor = getContext().getContentResolver().query(new TableCategory().getUri(),
                null, null, null, Category.CATEGNAME);
        if (cursor == null) return listCategories;

        // populate list from data cursor
        while (cursor.moveToNext()) {
            TableCategory category = new TableCategory();
            category.setValueFromCursor(cursor);
            listCategories.add(category);
        }
        cursor.close();

        return listCategories;
    }

    public int update(int id, String name) {
        if(TextUtils.isEmpty(name)) return Constants.NOT_SET;

        name = name.trim();

        ContentValues values = new ContentValues();
        values.put(Category.CATEGNAME, name);

        int result = getContext().getContentResolver().update(mCategory.getUri(),
                values,
                Category.CATEGID + "=" + id, null);

        return result;
    }

    /**
     * Checks account transactions to find any that use given category
     * @param categoryId Id of the category for which to check.
     * @return A boolean indicating if the category is in use.
     */
    public boolean isCategoryUsed(int categoryId) {
        AccountTransactionRepository repo = new AccountTransactionRepository(getContext());
        int links = repo.count(Category.CATEGID + "=?", new String[]{Integer.toString(categoryId)});
        return links > 0;
    }

    public boolean isSubcategoryUsed(int subcategoryId) {
        AccountTransactionRepository repo = new AccountTransactionRepository(getContext());
        int links = repo.count(Subcategory.SUBCATEGID + "=?", new String[] { Integer.toString(subcategoryId)});
        return links > 0;
    }

    private Context getContext() {
        return mContext;
    }
}
