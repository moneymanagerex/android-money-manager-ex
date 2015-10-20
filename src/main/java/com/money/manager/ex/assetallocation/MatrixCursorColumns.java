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
package com.money.manager.ex.assetallocation;

import android.database.Cursor;

import com.money.manager.ex.domainmodel.AssetClass;

/**
 * Column names for the matrix cursor for Asset Allocation
 */
public class MatrixCursorColumns {
    public static final String ID = "_id";
    public static final String NAME =  AssetClass.NAME;
    public static final String ALLOCATION =  AssetClass.ALLOCATION;
    public static final String VALUE =  "Value";
    public static final String CURRENT_ALLOCATION = "CurrentAllocation";
    public static final String CURRENT_VALUE =  "CurrentValue";
    public static final String DIFFERENCE =  "Difference";
    public static final String TYPE = "Type";

    public static MatrixCursorColumns fromCursor(Cursor cursor) {
        MatrixCursorColumns values = new MatrixCursorColumns();

//        int position = cursor.getPosition();
//        int count = cursor.getCount();

        values.id = (int) cursor.getLong(cursor.getColumnIndex(MatrixCursorColumns.ID));
        values.name = cursor.getString(cursor.getColumnIndex(MatrixCursorColumns.NAME));
        values.allocation = cursor.getString(cursor.getColumnIndex(MatrixCursorColumns.ALLOCATION));
        values.value = cursor.getString(cursor.getColumnIndex(MatrixCursorColumns.VALUE));
        values.currentAllocation = cursor.getString(cursor.getColumnIndex(MatrixCursorColumns.CURRENT_ALLOCATION));
        values.currentValue = cursor.getString(cursor.getColumnIndex(MatrixCursorColumns.CURRENT_VALUE));
        values.difference = cursor.getString(cursor.getColumnIndex(MatrixCursorColumns.DIFFERENCE));
        String typeString = cursor.getString(cursor.getColumnIndex(MatrixCursorColumns.TYPE));
        values.type = ItemType.valueOf(typeString);

        return values;
    }

    public int id;
    public String name;
    public String allocation;
    public String value;
    public String currentAllocation;
    public String currentValue;
    public String difference;
    public ItemType type;
}
