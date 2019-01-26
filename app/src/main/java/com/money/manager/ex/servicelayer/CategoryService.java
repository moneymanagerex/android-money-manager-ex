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
import com.money.manager.ex.datalayer.SubcategoryRepository;
import com.money.manager.ex.domainmodel.Category;
import com.money.manager.ex.domainmodel.Subcategory;

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
    private SubcategoryRepository mSubcategoryRepository;

    public int loadIdByName(String name) {
        return getRepository().loadIdByName(name);
    }

    public int loadSubcategoryIdByName(String name, int categoryId) {
        return getSubcategoryRepository().loadIdByName(name, categoryId);
    }

    public int createNew(String name) {
        if (TextUtils.isEmpty(name)) return Constants.NOT_SET;

        name = name.trim();

        ContentValues values = new ContentValues();
        values.put(Category.CATEGNAME, name);

        CategoryRepository repo = new CategoryRepository(getContext());

        Uri result = getContext().getContentResolver()
                .insert(repo.getUri(), values);
        long id = ContentUris.parseId(result);

        return ((int) id);
    }

    public int createNewSubcategory(String name, int categoryId) {
        if (TextUtils.isEmpty(name)) return Constants.NOT_SET;

        name = name.trim();

        ContentValues values = new ContentValues();
        values.put(Subcategory.SUBCATEGNAME, name);
        values.put(Subcategory.CATEGID, categoryId);

        SubcategoryRepository repo = getSubcategoryRepository();

        Uri result = getContext().getContentResolver()
                .insert(repo.getUri(), values);
        long id = ContentUris.parseId(result);

        return ((int) id);
    }

    public String getCategorySubcategoryName(int categoryId, int subCategoryId) {
        String categoryName = "";
        String subCategoryName = "";

        if (categoryId != Constants.NOT_SET) {
            CategoryRepository categoryRepository = new CategoryRepository(getContext());
            Category category = categoryRepository.load(categoryId);
            categoryName = category != null
                ? category.getName()
                : "n/a";
        }
        if (subCategoryId != Constants.NOT_SET) {
            SubcategoryRepository subcategoryRepository = new SubcategoryRepository(getContext());
            Subcategory subcategory = subcategoryRepository.load(subCategoryId);
            subCategoryName = subcategory != null 
                ? subcategory.getName()
                : "n/a";
        }

        String result = "";
        if (!TextUtils.isEmpty(categoryName)) result += categoryName;
        if (!TextUtils.isEmpty(subCategoryName)) result += ":" + subCategoryName;

        return result;
    }

    /**
     * Return a list of all categories. Ordered by name.
     */
    public List<Category> getList() {
        Select query = new Select().orderBy(Category.CATEGNAME);

        return getRepository().query(Category.class, query);
    }

    public int update(int id, String name) {
        if(TextUtils.isEmpty(name)) return Constants.NOT_SET;

        name = name.trim();

        ContentValues values = new ContentValues();
        values.put(Category.CATEGNAME, name);

        CategoryRepository repo = new CategoryRepository(getContext());

        int result = getContext().getContentResolver().update(repo.getUri(),
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

    private CategoryRepository getRepository() {
        if (mRepository == null) {
            mRepository = new CategoryRepository(getContext());
        }
        return mRepository;
    }

    private SubcategoryRepository getSubcategoryRepository() {
        if (mSubcategoryRepository == null) {
            mSubcategoryRepository = new SubcategoryRepository(getContext());
        }
        return mSubcategoryRepository;
    }
}
