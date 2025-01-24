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

import com.money.manager.ex.Constants;

import org.parceler.Parcel;

/**
 * Category
 */
@Parcel
public class Category
    extends EntityBase {

    public static final String CATEGID = "CATEGID";
    public static final String CATEGNAME = "CATEGNAME";
    public static final String ACTIVE = "ACTIVE";
    public static final String PARENTID = "PARENTID";
    public static final String BASENAME = "BASENAME";

    public Category() {
        super();
    }

    public Category(ContentValues contentValues) {
        super(contentValues);
    }

    public Long getId() {
        return getLong(CATEGID);
    }

    public void setId(Long value) {
        setLong(CATEGID, value);
    }

    public long getParentId() {
        if (getLong(PARENTID) != null && getLong(PARENTID) > 0 && getLong(PARENTID) != getId()) {
            return getLong(PARENTID);
        }
        return Constants.NOT_SET;
    }

    public void setParentId(Long value) {
        setLong(PARENTID, value);
    }

    public String getName() {
        return getString(CATEGNAME);
    }

    public void setName(String value) {
        setString(CATEGNAME, value);
    }

    public String getBasename() { return getString(BASENAME);}

    public void setBasename(String value) { setString(BASENAME, value);}

    public boolean getActive() {
        if (getLong(ACTIVE) == null ) return false;
        return (getLong(ACTIVE) != 0);
    }

    public void setActive(boolean value) {
        setLong(ACTIVE, (value ? 1L : 0L));
    }

}


