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
package com.money.manager.ex.domainmodel;

import com.money.manager.ex.database.TablePayee;

/**
 * Payee model.
 */
public class Payee
    extends EntityBase {

    public Payee() {

    }

//    public Payee(Cursor c) {
//        super(c);
//    }

    public Integer getId() {
        return getInt(TablePayee.PAYEEID);
    }

    public void setId(Integer value) {
        setInt(TablePayee.PAYEEID, value);
    }

    public String getName() {
        return getString(TablePayee.PAYEENAME);
    }

    public void setName(String value) {
        setString(TablePayee.PAYEENAME, value);
    }

    public Integer getCategoryId() {
        return getInt(TablePayee.CATEGID);
    }

    public Integer getSubcategoryId() {
        return getInt(TablePayee.SUBCATEGID);
    }

}
