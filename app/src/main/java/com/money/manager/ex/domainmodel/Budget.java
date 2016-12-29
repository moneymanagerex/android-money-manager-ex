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

import org.parceler.Parcel;

/**
 * Represents a Budget.
 * Table: budgetyear_v1
 */
@Parcel
public class Budget
    extends EntityBase {

    public static final String BUDGETYEARID = "BUDGETYEARID";
    public static final String BUDGETYEARNAME = "BUDGETYEARNAME";

    public Budget() { }

    public Integer getId() {
        return getInt(BUDGETYEARID);
    }

    public void setId(Integer value) {
        setInt(BUDGETYEARID, value);
    }

    public String getName() {
        return getString(BUDGETYEARNAME);
    }

    public void setName(String value) {
        setString(BUDGETYEARNAME, value);
    }
}
