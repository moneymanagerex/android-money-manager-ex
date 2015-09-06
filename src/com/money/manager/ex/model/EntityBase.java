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
package com.money.manager.ex.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;

import com.money.manager.ex.utils.DateUtils;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Base for the model entities. Keeps a reference to a cursor that contains the underlying data.
 * Created by Alen on 5/09/2015.
 */
public class EntityBase {

    /**
     * Default constructor.
     */
    protected EntityBase() {
        contentValues = new ContentValues();
    }

//    protected EntityBase(Cursor c) {
//        mCursor = c;
//    }

    public ContentValues contentValues;

    /**
     * Contains the pointer to the actual data when loading from content provider.
     */
    protected Cursor mCursor;

    public void loadFromCursor(Cursor c) {
        DatabaseUtils.cursorRowToContentValues(c, contentValues);
    }

    protected BigDecimal getBigDecimal(String fieldName) {
        String value = contentValues.getAsString(fieldName);

        if (StringUtils.isEmpty(value) && mCursor != null) {
            DatabaseUtils.cursorStringToContentValues(mCursor, fieldName, contentValues);
            value = contentValues.getAsString(fieldName);
        }

        return new BigDecimal(value);
    }

    protected void setBigDecimal(String fieldName, BigDecimal value) {
        contentValues.put(fieldName, value.toString());
    }

    protected void setDate(String fieldName, Date value) {
        String dateString = DateUtils.getIsoStringDate(value);
        contentValues.put(fieldName, dateString);
    }

    protected Integer getInt(String fieldName) {
        return contentValues.getAsInteger(fieldName);
    }

    protected void setInt(String fieldName, Integer value) {
        contentValues.put(fieldName, value);
    }

    protected String getString(String fieldName) {
        String value = contentValues.getAsString(fieldName);
        if (StringUtils.isEmpty(value) && mCursor != null) {
            DatabaseUtils.cursorStringToContentValues(mCursor, fieldName, contentValues);
            value = contentValues.getAsString(fieldName);
        }

        return value;
    }

    protected void setString(String fieldName, String value) {
        contentValues.put(fieldName, value);
    }

}
