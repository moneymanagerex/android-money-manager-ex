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
package com.money.manager.ex.database;

import android.database.Cursor;

public class TableCategory
		extends Dataset {

	public static final String CATEGID = "CATEGID";
	public static final String CATEGNAME = "CATEGNAME";

	private int categId;
	private CharSequence categName;

	public TableCategory() {
		super("category_v1", DatasetType.TABLE, "category");
	}

	@Override
	public String[] getAllColumns() {
		return new String[] {"CATEGID AS _id", CATEGID, CATEGNAME};
	}

    @Override
	public void setValueFromCursor(Cursor c) {
		if (c == null) { return; }

		this.setCategId(c.getInt(c.getColumnIndex(CATEGID)));
		this.setCategName(c.getString(c.getColumnIndex(CATEGNAME)));
	}

	/**
	 * @return the categID
	 */
	public int getCategId() {
		return categId;
	}

	/**
	 * @param categID the categID to set
	 */
	public void setCategId(int categID) {
		this.categId = categID;
	}

	/**
	 * @return the categName
	 */
	public CharSequence getCategName() {
		return categName;
	}

	/**
	 * @param categName the categName to set
	 */
	public void setCategName(CharSequence categName) {
		this.categName = categName;
	}
}
