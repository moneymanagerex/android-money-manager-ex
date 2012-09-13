/*******************************************************************************
 * Copyright (C) 2012 The Android Money Manager Ex Project
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
 ******************************************************************************/
package com.money.manager.ex.database;

import android.database.Cursor;

public class TableCategory extends Dataset {
	// definizione dei nomi dei campi
	public static final String CATEGID = "CATEGID";
	public static final String CATEGNAME = "CATEGNAME";
	// definizione dei campi
	private int categId;
	private String categName;
	// costruttore
	public TableCategory() {
		super("category_v1", DatasetType.TABLE, "category");
	}
	@Override
	public String[] getAllColumns() {
		return new String[] {"CATEGID AS _id", CATEGID, CATEGNAME};
	}
	@Override
	protected void setValueFromCursor(Cursor c) {
		// controllo che non sia null il cursore
		if (c == null) { return; }
		// imposto i vari valori
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
	public String getCategName() {
		return categName;
	}
	/**
	 * @param categName the categName to set
	 */
	public void setCategName(String categName) {
		this.categName = categName;
	}
}
