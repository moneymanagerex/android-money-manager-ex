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
import com.money.manager.ex.utils.MmxDatabaseUtils;

/**
 * A repository for Categories.
 */
public class CategoryRepository
    extends RepositoryBase<Category>{

    public static final String tableName = "category_v1";

    public CategoryRepository(Context context) {
        super(context, tableName, DatasetType.TABLE, "category");
    }

    @Override
    public String[] getAllColumns() {
        return new String[] {"CATEGID AS _id",
                Category.ID,
                Category.NAME,
                Category.ACTIVE,
                Category.PARENTID};
    }

    public Category load(long id) {
        if (id == Constants.NOT_SET) return null;

        Category category = first(Category.class,
                getAllColumns(),
                Category.ID + "=?",
                MmxDatabaseUtils.getArgsForId(id),
                null);

        return category;
    }

    public long loadIdByName(String name) {
        Category temp = first(Category.class,
                new String[] { Category.ID},
                Category.NAME + "=?",
                new String[] { name },
                null);

        if (temp == null) return Constants.NOT_SET;

        return temp.getId();
    }

    public long loadIdByName(String name, long parentId) {
        Category temp = first(Category.class,
                new String[] { Category.ID},
                Category.NAME + "=? AND" + Category.PARENTID + "=?",
                new String[] { name, Long.toString(parentId)},
                null);

        if (temp == null) return Constants.NOT_SET;

        return temp.getId();
    }
}
