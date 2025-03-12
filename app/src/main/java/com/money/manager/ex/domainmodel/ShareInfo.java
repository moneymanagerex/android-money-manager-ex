/*
 * Copyright (C) 2025-2025 The Android Money Manager Ex Project Team
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

public class ShareInfo extends EntityBase {

    public static final String SHAREINFOID = "SHAREINFOID";
    public static final String CHECKINGACCOUNTID = "CHECKINGACCOUNTID";
    public static final String SHARENUMBER = "SHARENUMBER";
    public static final String SHAREPRICE = "SHAREPRICE";
    public static final String SHARECOMMISSION = "SHARECOMMISSION";
    public static final String SHARELOT = "SHARELOT";

    public ShareInfo() {
        super();
    }

    public ShareInfo(ContentValues contentValues) {
        super(contentValues);
    }

    @Override
    public String getPrimaryKeyColumn() {
        return SHAREINFOID;
    }

    public Long getCheckingAccountId() {
        return getLong(CHECKINGACCOUNTID);
    }

    public void setCheckingAccountId(Long value) {
        setLong(CHECKINGACCOUNTID, value);
    }

    public Double getShareNumber() {
        return getDouble(SHARENUMBER);
    }

    public void setShareNumber(Double value) {
        setDouble(SHARENUMBER, value);
    }

    public Double getSharePrice() {
        return getDouble(SHAREPRICE);
    }

    public void setSharePrice(Double value) {
        setDouble(SHAREPRICE, value);
    }

    public Double getShareCommission() {
        return getDouble(SHARECOMMISSION);
    }

    public void setShareCommission(Double value) {
        setDouble(SHARECOMMISSION, value);
    }

    public String getShareLot() {
        return getString(SHARELOT);
    }

    public void setShareLot(String value) {
        setString(SHARELOT, value);
    }
}

