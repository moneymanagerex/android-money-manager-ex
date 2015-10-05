/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
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

import android.database.Cursor;
import android.database.DatabaseUtils;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

/**
 * Asset Class
 */
public class AssetClass
    extends EntityBase {

    public static final String ID = "ID";
    public static final String NAME = "NAME";
    public static final String ALLOCATION = "ALLOCATION";
    public static final String SORTORDER = "SORTORDER";

    public static AssetClass from(Cursor c) {
        AssetClass entity = new AssetClass();
        entity.loadFromCursor(c);
        return entity;
    }

    public static AssetClass create() {
        AssetClass entity = new AssetClass();
        entity.setAllocation(MoneyFactory.fromString("0"));
        return entity;
    }

    public AssetClass() {
        super();
    }

    @Override
    public void loadFromCursor(Cursor c) {
        super.loadFromCursor(c);

        // Reload all money values.
        DatabaseUtils.cursorDoubleToCursorValues(c, ALLOCATION, this.contentValues);
    }

    public int getId() {
        return getInt(ID);
    }

    public Money getAllocation() {
        return getMoney(ALLOCATION);
    }

    public void setAllocation(Money value) {
        setMoney(ALLOCATION, value);
    }

    public String getName() {
        return getString(NAME);
    }

    public void setName(String value) {
        setString(NAME, value);
    }

    public Integer getSortOrder() {
        return getInt(SORTORDER);
    }

    public void setSortOrder(int value) {
        setInt(SORTORDER, value);
    }

}
