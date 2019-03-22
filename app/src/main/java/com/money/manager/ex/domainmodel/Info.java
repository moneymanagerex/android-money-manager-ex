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

/**
 * InfoTable entity
 */
public class Info
    extends EntityBase {

    public static final String INFOID = "INFOID";
    public static final String INFONAME = "INFONAME";
    public static final String INFOVALUE = "INFOVALUE";

    public static Info create(String key, String value) {
        Info entity = new Info();
        entity.setName(key);
        entity.setValue(value);
        return entity;
    }

    public int getId() {
        return getInt(INFOID);
    }

    public String getName() {
        return getString(INFONAME);
    }

    public void setName(String name) {
        setString(INFONAME, name);
    }

    public void setValue(String value) {
        setString(INFOVALUE, value);
    }
}
