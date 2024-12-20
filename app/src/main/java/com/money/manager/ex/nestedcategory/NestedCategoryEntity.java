/*
 * Copyright (C) 2024-2024 The Android Money Manager Ex Project Team
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

package com.money.manager.ex.nestedcategory;

import com.money.manager.ex.Constants;
import com.money.manager.ex.domainmodel.Category;
import com.money.manager.ex.domainmodel.EntityBase;

public class NestedCategoryEntity
        extends EntityBase {

    public NestedCategoryEntity(Category category) {
        super();
        setMandatoryField(category.getId(), category.getName(), category.getParentId());
    }

    public NestedCategoryEntity() {
        super(QueryNestedCategory.CATEGID, QueryNestedCategory.CATEGNAME,QueryNestedCategory.ACTIVE);
        setCategoryId(Constants.NOT_SET);
        setCategoryName(Constants.EMPTY_STRING);
        setParentId(Constants.NOT_SET);
        setActive(false);
    }

    public NestedCategoryEntity(long categoryId, String categoryName, long parentId) {
        super();
        setMandatoryField(categoryId, categoryName, parentId);
    }

    public long getCategoryId() {
        return getId();
    }

    public void setCategoryId(long id) {
        setId(id);
    }

    public String getCategoryName() {
        return getName();
    }

    public void setCategoryName(String name) {
        setName(name);
    }

    public long getParentId() {
        return getLong(QueryNestedCategory.PARENTID);
    }

    public void setParentId(long id) {
        setLong(QueryNestedCategory.PARENTID, id);
    }

    public String getParentName() {
        return getString(QueryNestedCategory.PARENTNAME);
    }

    public void setParentName(String name) {
        setString(QueryNestedCategory.PARENTNAME, name);
    }

    public String getBasename() {
        return getString(QueryNestedCategory.BASENAME);
    }

    public void setBasename(String basename) {
        setString(QueryNestedCategory.BASENAME, basename);
    }


    public long getLevel() {
        return getLong(QueryNestedCategory.LEVEL);
    }

    public void setLevel(long level) {
        setLong(QueryNestedCategory.LEVEL, level);
    }

    public void setMandatoryField(long categoryId, String categoryName, long parentId) {
        setCategoryId(categoryId);
        setCategoryName(categoryName);
        setParentId(parentId);
        setActive(true);
    }

    public boolean hasParent() {
        return this.getParentId() != Constants.NOT_SET;
    }

    public Category asCategory() {
        Category category = new Category();
        category.setId(getCategoryId());
        category.setName(getCategoryName());
        category.setParentId(getParentId());
        category.setBasename(getBasename());
        return category;
    }

}
