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

import java.util.List;

/**
 * Category
 */
public class CategoryService
        extends ServiceBase {

    private CategoryRepository mRepository;

    public CategoryService(final Context context) {
        super(context);
    }

    public int loadIdByName(final String name) {
        return getRepository().loadIdByName(name);
    }

    public int loadIdByName(final String name, final int parentId) {
        return getRepository().loadIdByName(name, parentId);
    }

    public int createNew(String name) {
        if (TextUtils.isEmpty(name)) return Constants.NOT_SET;

        name = name.trim();

        final ContentValues values = new ContentValues();
        values.put(Category.CATEGNAME, name);
        values.put(Category.PARENTID, -1);

        final CategoryRepository repo = new CategoryRepository(getContext());

        final Uri result = getContext().getContentResolver()
                .insert(repo.getUri(), values);
        final long id = ContentUris.parseId(result);

        return ((int) id);
    }

    public int createNewSubcategory(String name, final int categoryId) {
        if (TextUtils.isEmpty(name)) return Constants.NOT_SET;

        name = name.trim();

        final ContentValues values = new ContentValues();
        values.put(Category.CATEGNAME, name);
        values.put(Category.PARENTID, categoryId);

        final CategoryRepository repo = new CategoryRepository(getContext());

        final Uri result = getContext().getContentResolver()
                .insert(repo.getUri(), values);
        final long id = ContentUris.parseId(result);

        return ((int) id);
    }

    public String getCategorySubcategoryName(final int categoryId) {
        String categoryName = "";

        if (Constants.NOT_SET != categoryId) {
            final CategoryRepository categoryRepository = new CategoryRepository(getContext());
            final Category category = categoryRepository.load(categoryId);
            if (null != category) {
                categoryName = category.getName();
                // TODO parent category : category
                if (0 < category.getParentId()) {
                    final Category parentCategory = categoryRepository.load(category.getParentId());
                    if (null != parentCategory)
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
        final Select query = new Select().where("PARENTID < 0").orderBy(Category.CATEGNAME);

        return getRepository().query(Category.class, query);
    }

    public int update(final int id, String name) {
        if (TextUtils.isEmpty(name)) return Constants.NOT_SET;

        name = name.trim();

        final ContentValues values = new ContentValues();
        values.put(Category.CATEGNAME, name);

        final CategoryRepository repo = new CategoryRepository(getContext());

        final int result = getContext().getContentResolver().update(repo.getUri(),
                values,
                Category.CATEGID + "=" + id, null);

        return result;
    }

    /**
     * Checks account transactions to find any that use given category
     *
     * @param categoryId Id of the category for which to check.
     * @return A boolean indicating if the category is in use.
     */
    public boolean isCategoryUsed(final int categoryId) {
        final AccountTransactionRepository repo = new AccountTransactionRepository(getContext());
        final int links = repo.count(Category.CATEGID + "=?", new String[]{Integer.toString(categoryId)});
        return 0 < links;
    }

    private CategoryRepository getRepository() {
        if (null == mRepository) {
            mRepository = new CategoryRepository(getContext());
        }
        return mRepository;
    }
}
