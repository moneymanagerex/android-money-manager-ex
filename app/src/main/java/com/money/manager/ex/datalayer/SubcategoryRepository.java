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
import com.money.manager.ex.domainmodel.Subcategory;
import com.money.manager.ex.utils.MmxDatabaseUtils;

/**
 * A repository for Subcategories.
 */
public class SubcategoryRepository
    extends RepositoryBase {

    public static final String tableName = "subcategory_v1";

    public SubcategoryRepository(Context context) {
        super(context, tableName, DatasetType.TABLE, "subcategory");
    }

    @Override
    public String[] getAllColumns() {
        return new String[] { "SUBCATEGID AS _id",
            Subcategory.SUBCATEGID,
            Subcategory.SUBCATEGNAME,
            Subcategory.CATEGID
        };
    }

    public Subcategory load(int id) {
        if (id == Constants.NOT_SET) return null;

        Subcategory subcategory = (Subcategory) first(Subcategory.class,
                getAllColumns(),
                Subcategory.SUBCATEGID + "=?", MmxDatabaseUtils.getArgsForId(id),
                null);

        return subcategory;
    }

    public int loadIdByName(String name, int categoryId) {
        Subcategory temp = (Subcategory) first(Subcategory.class,
                new String[] { Subcategory.SUBCATEGID },
                Subcategory.CATEGID + "=? AND " + Subcategory.SUBCATEGNAME + "=?",
                new String[] { Integer.toString(categoryId), name },
                null);

        if (temp == null) return Constants.NOT_SET;

        return temp.getId();
    }
}
