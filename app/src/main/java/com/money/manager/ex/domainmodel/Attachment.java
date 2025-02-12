/*
 * Copyright (C) 2024 The Android Money Manager Ex Project Team
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

/**
 * Attachment model.
 */
public class Attachment
    extends EntityBase {

    public static final String ATTACHMENTID = "ATTACHMENTID";
    public static final String REFTYPE = "REFTYPE";
    public static final String REFID = "REFID";
    public static final String DESCRIPTION = "DESCRIPTION";
    public static final String FILENAME = "FILENAME";

    public Attachment() {
        super();
    }

    public Attachment(ContentValues contentValues) {
        super(contentValues);
    }

    @Override
    public String getPrimaryKeyColumn() {
        return ATTACHMENTID;  // This returns the column name specific to Report
    }

    public String getFilename() {
        return getString(Attachment.FILENAME);
    }

    public void setFilename(String value) {
        setString(Attachment.FILENAME, value);
    }

    public Long getRefId() {
        return getLong(Attachment.REFID);
    }

    public void setRefId(Long value) {
        setLong(Attachment.REFID, value);
    }

    public String getRefType() {
        return getString(Attachment.REFTYPE);
    }

    public void setRefType(String value) {
        setString(Attachment.REFTYPE, value);
    }
}
