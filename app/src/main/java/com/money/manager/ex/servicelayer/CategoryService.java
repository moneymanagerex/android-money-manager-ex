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
package com.money.manager.ex.servicelayer;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.money.manager.ex.Constants;
import com.money.manager.ex.datalayer.AccountTransactionRepository;
import com.money.manager.ex.datalayer.CategoryRepository;
import com.money.manager.ex.datalayer.Select;
import com.money.manager.ex.domainmodel.Category;
import com.money.manager.ex.nestedcategory.NestedCategoryEntity;
import com.money.manager.ex.nestedcategory.QueryNestedCategory;

import java.util.List;

/**
 * Category
 */
public class CategoryService
    extends ServiceBase {

    public CategoryService(Context context) {
        super(context);
    }

    private CategoryRepository mRepository;

    public long loadIdByName(String name) {
        return getRepository().loadIdByName(name);
    }

    public long loadIdByName(String name, long parentId) {
        return getRepository().loadIdByName(name, parentId);
    }

    public long createNew(String name, long parentId) {
        if (TextUtils.isEmpty(name)) return Constants.NOT_SET;

        name = name.trim();

        ContentValues values = new ContentValues();
        values.put(Category.CATEGNAME, name);
        values.put(Category.ACTIVE, 1);
        values.put(Category.PARENTID, parentId);

        CategoryRepository repo = new CategoryRepository(getContext());

        Uri result = getContext().getContentResolver()
                .insert(repo.getUri(), values);
        long id = ContentUris.parseId(result);

        return id;
    }

    public String getCategorySubcategoryName(long categoryId) {
        String categoryName = "";

        if (categoryId != Constants.NOT_SET) {
            CategoryRepository categoryRepository = new CategoryRepository(getContext());
            Category category = categoryRepository.load(categoryId);
            if (category != null) {
                categoryName = category.getName();
                // TODO parent category : category
                if (category.getParentId() > 0)
                {
                    Category parentCategory = categoryRepository.load(category.getParentId());
                    if (parentCategory != null)
                        categoryName = parentCategory.getName() + " : " + category.getName();
                }
            } else {
                categoryName = null;
            }
        }

        return categoryName;
    }

    /**
     * Return a list of all categories. Ordered by name.
     */
    public List<Category> getList() {
        Select query = new Select().where("PARENTID < 0").orderBy(Category.CATEGNAME);

        return getRepository().query(Category.class, query);
    }

    public long update(long id, String name, long parentId) {
        if(TextUtils.isEmpty(name)) return Constants.NOT_SET;

        name = name.trim();

        ContentValues values = new ContentValues();
        values.put(Category.CATEGNAME, name);
        values.put(Category.PARENTID, parentId);

        CategoryRepository repo = new CategoryRepository(getContext());

        long result = getContext().getContentResolver().update(repo.getUri(),
                values,
                Category.CATEGID + "=?", new String[]{Long.toString(id)});

        return result;
    }

    /**
     * Checks account transactions to find any that use given category
     * @param categoryId Id of the category for which to check.
     * @return A boolean indicating if the category is in use.
     */
    public boolean isCategoryUsed(long categoryId) {
        AccountTransactionRepository repo = new AccountTransactionRepository(getContext());
        long links = repo.count(Category.CATEGID + "=?", new String[]{Long.toString(categoryId)});
        return links > 0;
    }

    public boolean isCategoryUsedWithChildren( long categoryId ) {
        // First if list has more than 1 record category is used
        QueryNestedCategory query = new QueryNestedCategory(getContext());
        List<NestedCategoryEntity> ids = query.getChildrenNestedCategoryEntities(categoryId);
        assert ids != null;
        if (ids.size() > 1 ) {return true;}

        AccountTransactionRepository repo = new AccountTransactionRepository(getContext());
        long links = repo.count("( " + Category.CATEGID + "=? AND DELETEDTIME IS NULL )",
                new String[]{Long.toString(categoryId)});
        return links > 0;
    }

    private CategoryRepository getRepository() {
        if (mRepository == null) {
            mRepository = new CategoryRepository(getContext());
        }
        return mRepository;
    }
}
