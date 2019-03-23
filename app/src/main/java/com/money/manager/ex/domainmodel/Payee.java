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
package com.money.manager.ex.domainmodel;

import android.content.ContentValues;

import com.money.manager.ex.Constants;

/**
 * Payee model.
 */
public class Payee
    extends EntityBase {

    public static final String PAYEEID = "PAYEEID";
    public static final String PAYEENAME = "PAYEENAME";
    public static final String CATEGID = "CATEGID";
    public static final String SUBCATEGID = "SUBCATEGID";

    public Payee() {
        super();
    }

    public Payee(ContentValues contentValues) {
        super(contentValues);
    }

    public Integer getId() {
        return getInt(PAYEEID);
    }

    public void setId(Integer value) {
        setInt(Payee.PAYEEID, value);
    }

    public String getName() {
        return getString(Payee.PAYEENAME);
    }

    public void setName(String value) {
        setString(Payee.PAYEENAME, value);
    }

    public Integer getCategoryId() {
        return getInt(Payee.CATEGID);
    }

    public void setCategoryId(Integer value) {
        setInt(Payee.CATEGID, value);
    }

    public boolean hasCategory() {
        return this.getCategoryId() != null && this.getCategoryId() != Constants.NOT_SET;
    }

    public Integer getSubcategoryId() {
        return getInt(Payee.SUBCATEGID);
    }

    public void setSubcategoryId(Integer value) {
        setInt(SUBCATEGID, value);
    }
}
