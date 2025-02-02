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

/**
 * Payee model.
 */
public class Payee
    extends EntityBase {

    public static final String PAYEEID = "PAYEEID";
    public static final String PAYEENAME = "PAYEENAME";
    public static final String CATEGID = "CATEGID";
    public static final String NUMBER = "NUMBER";
    public static final String ACTIVE = "ACTIVE";

    public Payee() {
        super();
        setLong(Payee.ACTIVE, 1L);
    }

    public Payee(ContentValues contentValues) {
        super(contentValues);
    }

    public Long getId() {
        return getLong(PAYEEID);
    }

    public void setId(Long value) {
        setLong(Payee.PAYEEID, value);
    }

    public String getName() {
        return getString(Payee.PAYEENAME);
    }

    public void setName(String value) {
        setString(Payee.PAYEENAME, value);
    }

    public Long getCategoryId() {
        return getLong(Payee.CATEGID);
    }

    public void setCategoryId(Long value) {
        setLong(Payee.CATEGID, value);
    }

    public boolean hasCategory() {
        return this.getCategoryId() != null && this.getCategoryId() != Constants.NOT_SET;
    }

    public Boolean getActive() {
        return getLong(ACTIVE) == null || getLong(ACTIVE) != 0L;
    }
    public void setActive(Boolean value) { setLong(ACTIVE, value ? 1L : 0L); }

}
