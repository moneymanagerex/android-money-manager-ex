/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.money.manager.ex.businessobjects;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import com.money.manager.ex.database.TableCategory;

/**
 * Category
 */
public class Category {
    public Category(Context context) {
        mContext = context;
        mCategory = new TableCategory();
    }

    private Context mContext;
    private TableCategory mCategory;

    public int loadIdByName(String name) {
        int result = -1;

        if(TextUtils.isEmpty(name)) { return result; }

        String selection = TableCategory.CATEGNAME + "=?";

        Cursor cursor = mContext.getContentResolver().query(
                mCategory.getUri(),
                new String[] { TableCategory.CATEGID },
                selection,
                new String[] { name },
                null);

        if(cursor.moveToFirst()) {
            result = cursor.getInt(cursor.getColumnIndex(TableCategory.CATEGID));
        }

        cursor.close();

        return result;

    }
}
