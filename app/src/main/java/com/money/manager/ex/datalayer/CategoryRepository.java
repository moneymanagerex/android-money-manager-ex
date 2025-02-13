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
package com.money.manager.ex.datalayer;

import android.content.Context;

import com.money.manager.ex.Constants;
import com.money.manager.ex.database.DatasetType;
import com.money.manager.ex.domainmodel.Category;

/**
 * A repository for Categories.
 */
public class CategoryRepository
    extends RepositoryBase<Category>{

    private static final String TABLE_NAME = "category_v1";
    private static final String ID_COLUMN = Category.CATEGID;
    private static final String NAME_COLUMN = Category.CATEGNAME;

    public CategoryRepository(Context context) {
        super(context, TABLE_NAME, DatasetType.TABLE, "category", ID_COLUMN, NAME_COLUMN);
    }

    @Override
    protected Category createEntity() {
        return new Category();
    }

    @Override
    public String[] getAllColumns() {
        return new String[] {ID_COLUMN + " AS _id",
                Category.CATEGID,
                Category.CATEGNAME,
                Category.ACTIVE,
                Category.PARENTID};
    }

    public long loadIdByName(String name) {
        Category temp = first(
                new String[] { Category.CATEGID },
                Category.CATEGNAME + "=?",
                new String[] { name },
                null);

        if (temp == null) return Constants.NOT_SET;

        return temp.getId();
    }

    public long loadIdByName(String name, long parentId) {
        Category temp = first(
                new String[] { Category.CATEGID },
                Category.CATEGNAME + "=? AND" + Category.PARENTID + "=?",
                new String[] { name, Long.toString(parentId)},
                null);

        if (temp == null) return Constants.NOT_SET;

        return temp.getId();
    }
}
