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

import androidx.annotation.Nullable;

import com.money.manager.ex.Constants;
import com.money.manager.ex.datalayer.IEntity;
import com.money.manager.ex.utils.MmxDate;

import java.util.Date;
import java.util.Map;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

/**
 * Base for the model entities. Keeps a reference to a cursor that contains the underlying data.
 */
public abstract class EntityBase implements IEntity {

    public static final String UPDATED_AT = "updated_at";
    public static final String IS_DELETED = "is_deleted";
    public static final String IS_DIRTY = "is_dirty";

    public ContentValues contentValues;

    public EntityBase() {
        contentValues = new ContentValues();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        // for sync we need a content equals
        if (obj == null) return false;
        if (obj instanceof EntityBase) {
            EntityBase o2 = (EntityBase) obj;
            if (o2.contentValues != null && this.contentValues != null) {
                if (o2.contentValues.size() != this.contentValues.size()) return false;
                for (Map.Entry<String, Object> kvThis : this.contentValues.valueSet()) {
                    if (kvThis != null) {
                        if (! o2.getContentValues().containsKey(kvThis.getKey())) return false;
                        Object ov2 = o2.getContentValues().get(kvThis.getKey());
                        Object ovThis = this.contentValues.get(kvThis.getKey());
                        if (ov2 == null) {
                            if (ovThis != null) return false;
                        } else {
                            if (! ov2.equals(ovThis)) return false;
                        }
                    }
                }
                return true;
            }
        }
        return super.equals(obj);
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
        contentValues.put(column, value != null && value ? 1 : 0);
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
    }

    // Incremental Sync Metadata

    public String getUpdatedAtColumn() {
        return UPDATED_AT;
    }

    public String getIsDeletedColumn() {
        return IS_DELETED;
    }

    public String getIsDirtyColumn() {
        return IS_DIRTY;
    }

    public String getUpdatedAt() {
        return getString(getUpdatedAtColumn());
    }

    public void setUpdatedAt(String value) {
        setString(getUpdatedAtColumn(), value);
    }

    public boolean isDeleted() {
        String col = getIsDeletedColumn();
        if (TextUtils.isEmpty(col)) return false;
        
        Object val = contentValues.get(col);
        if (val == null) return false;
        
        if (val instanceof Integer) {
            return (Integer) val != 0;
        } else if (val instanceof String) {
            return !TextUtils.isEmpty((String) val);
        }
        return false;
    }

    public void setDeleted(boolean value) {
        String col = getIsDeletedColumn();
        if (TextUtils.isEmpty(col)) return;

        if (value) {
            // If it's a timestamp column (like DELETEDTIME), set the current time.
            // If it's a boolean column, set 1.
            if (col.contains("TIME")) {
                setString(col, new MmxDate().toIsoString());
            } else {
                setInt(col, 1);
            }
        } else {
            if (col.contains("TIME")) {
                setString(col, null);
            } else {
                setInt(col, 0);
            }
        }
    }

    public boolean isDirty() {
        Integer val = getInt(getIsDirtyColumn());
        return val != null && val != 0;
    }

    public void setDirty(boolean value) {
        setInt(getIsDirtyColumn(), value ? 1 : 0);
    }

    // Abstract method to get primary key column (overridden by subclasses)
    public abstract String getPrimaryKeyColumn();

    /**
     * Creates a diff that a user can understand the content for sync/merge.
     * @return textual representation of the content diff
     */
    public String getDiffString(EntityBase theirs) {
        StringBuilder sb = new StringBuilder();
        if (theirs == null) {
            sb.append("theirs is empty"); // should not happen on merge
        } else if (theirs.contentValues == null && this.contentValues == null) {
            sb.append("both entities empty"); // should not happen on merge
        } else if (this.contentValues == null || this.contentValues.size() == 0) { // backwards compatibility
            sb.append("ours is empty"); // should not happen on merge
        } else if (theirs.contentValues == null || theirs.contentValues.size() == 0) { // backwards compatibility
            sb.append("theirs is empty");// should not happen on merge
        } else {
            for (Map.Entry<String, Object> kvOurs : this.contentValues.valueSet()) {
                if (kvOurs != null && !"_id".equals(kvOurs.getKey())) { // do not show _id
                    Object valTheirs = theirs.getContentValues().get(kvOurs.getKey());
                    boolean equalValues = valTheirs == null && kvOurs.getValue() == null;
                    if (valTheirs != null) {
                        equalValues = valTheirs.equals(kvOurs.getValue());
                    }
                    if (! equalValues) {
                        sb.append(kvOurs.getKey()).append(": ");
                        sb.append(kvOurs.getValue()).append(" | ").append(valTheirs);
                        sb.append("\n");
                    }
                }
            }
        }
        return sb.toString();
    }
}
