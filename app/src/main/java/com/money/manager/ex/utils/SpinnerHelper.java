/*
 * Copyright (C) 2012-2019 The Android Money Manager Ex Project Team
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

package com.money.manager.ex.utils;

import android.database.Cursor;

/**
 * Useful functions for working with Spinners (dropdown lists).
 */
public class SpinnerHelper {
    public static int getPosition(String displayText, String fieldName, Cursor cursor) {
        int position = -1;
        cursor.moveToFirst();

        while(cursor.moveToNext()) {
            String text = cursor.getString(cursor.getColumnIndex(fieldName));
            if (text.equals(displayText)) {
                position = cursor.getPosition();
                break;
            }
        }

        return position;
    }
}
