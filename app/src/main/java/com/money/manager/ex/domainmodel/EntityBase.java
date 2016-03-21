/*
 * Copyright (C) 2012-2016 The Android Money Manager Ex Project Team
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
package com.money.manager.ex.domainmodel;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.text.TextUtils;

import com.money.manager.ex.Constants;
import com.money.manager.ex.utils.MyDateTimeUtils;

import org.joda.time.DateTime;
import org.parceler.Parcel;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

/**
 * Base for the model entities. Keeps a reference to a cursor that contains the underlying data.
 */
@Parcel
public class EntityBase {

    /**
     * Default constructor.
     */
    protected EntityBase() {
        contentValues = new ContentValues();
    }

    protected EntityBase(ContentValues contentValues) {
        this.contentValues = contentValues;
    }

    public ContentValues contentValues;

    /**
     * Contains the pointer to the actual data when loading from content provider.
     */
//    private Cursor mCursor;

//    protected Cursor getCursor() {
//        return mCursor;
//    }

    public void loadFromCursor(Cursor c) {
        this.contentValues.clear();

        DatabaseUtils.cursorRowToContentValues(c, contentValues);
    }

//    public void setCursor(Cursor c) {
//        this.mCursor = c;
//    }

    protected Boolean getBoolean(String column) {
        return contentValues.getAsBoolean(column);
    }

    protected void setBoolean(String column, Boolean value) {
        contentValues.put(column, value.toString().toUpperCase());
    }

    protected Money getMoney(String fieldName) {
        String value = contentValues.getAsString(fieldName);
        if (value == null || TextUtils.isEmpty(value)) return null;

        Money result = MoneyFactory.fromString(value).truncate(Constants.DEFAULT_PRECISION);
        return result;
    }

    protected void setMoney(String fieldName, Money value) {
        contentValues.put(fieldName, value.toString());
    }

    protected DateTime getDateTime(String fieldName) {
        String dateString = getString(fieldName);
        return MyDateTimeUtils.from(dateString);
    }

    protected void setDateTime(String fieldName, DateTime value) {
        String dateString = MyDateTimeUtils.getIsoStringFrom(value);
        contentValues.put(fieldName, dateString);
    }

    protected Integer getInt(String column) {
        return contentValues.getAsInteger(column);
    }

    protected void setInt(String fieldName, Integer value) {
        contentValues.put(fieldName, value);
    }

    protected String getString(String fieldName) {
        return contentValues.getAsString(fieldName);
    }

    protected void setString(String fieldName, String value) {
        contentValues.put(fieldName, value);
    }

    protected Double getDouble(String column) {
        return contentValues.getAsDouble(column);
    }

    protected void setDouble(String column, Double value) {
        contentValues.put(column, value);
    }
}
