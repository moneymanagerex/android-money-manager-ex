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
