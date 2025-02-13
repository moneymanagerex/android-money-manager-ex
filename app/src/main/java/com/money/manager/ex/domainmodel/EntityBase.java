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
package com.money.manager.ex.domainmodel;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.text.TextUtils;

import com.money.manager.ex.Constants;
import com.money.manager.ex.datalayer.IEntity;
import com.money.manager.ex.utils.MmxDate;

import org.parceler.Parcel;

import java.util.Date;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

/**
 * Base for the model entities. Keeps a reference to a cursor that contains the underlying data.
 */
public abstract class EntityBase implements IEntity {

    public ContentValues contentValues;

    public EntityBase() {
        contentValues = new ContentValues();
    }

    public EntityBase(ContentValues contentValues) {
        this.contentValues = contentValues;
    }

    public void loadFromCursor(Cursor cursor) {
        this.contentValues.clear();
        DatabaseUtils.cursorRowToContentValues(cursor, contentValues);
    }

    public ContentValues getContentValues() {
        return this.contentValues;
    }

    // Helper Methods for common data types
    protected Boolean getBoolean(String column) {
        return contentValues.getAsBoolean(column);
    }

    protected void setBoolean(String column, Boolean value) {
        contentValues.put(column, value.toString().toUpperCase());
    }

    protected Money getMoney(String fieldName) {
        String value = contentValues.getAsString(fieldName);
        if (value == null || TextUtils.isEmpty(value)) return null;

        return MoneyFactory.fromString(value).truncate(Constants.DEFAULT_PRECISION);
    }

    protected void setMoney(String fieldName, Money value) {
        contentValues.put(fieldName, value.toString());
    }

    protected Date getDate(String field) {
        String dateString = getString(field);
        return new MmxDate(dateString).toDate();
    }

    protected void setDate(String fieldName, Date value) {
        String dateString = new MmxDate(value).toIsoDateString();
        contentValues.put(fieldName, dateString);
    }

    protected Integer getInt(String column) {
        return contentValues.getAsInteger(column);
    }

    protected void setInt(String fieldName, Integer value) {
        contentValues.put(fieldName, value);
    }

    protected Long getLong(String column) {
        return contentValues.getAsLong(column);
    }

    protected void setLong(String fieldName, Long value) {
        contentValues.put(fieldName, value);
    }

    public String getString(String fieldName) {
        return contentValues.getAsString(fieldName);
    }

    protected void setString(String fieldName, String value) {
        contentValues.put(fieldName, value);
    }

    public Double getDouble(String column) {
        return contentValues.getAsDouble(column);
    }

    protected void setDouble(String column, Double value) {
        contentValues.put(column, value);
    }

    public void setId(Long id) {
        setLong(this.getPrimaryKeyColumn(), id);
    }
    public Long getId() {
        return getLong(this.getPrimaryKeyColumn());
    };

    // Abstract method to return all columns
    // public abstract String[] getAllColumns();

    // Abstract method to get primary key column (overridden by subclasses)
    public abstract String getPrimaryKeyColumn();
}
